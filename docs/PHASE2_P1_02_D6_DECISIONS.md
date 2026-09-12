# P1-02 / F6 — Inspect và Decision Set D6

**Cập nhật thực thi:** [F6 đã triển khai và kiểm chứng](PHASE2_P1_02_PROFILE.md): 221/221 backend, 9/9 FE tests, build/browser smoke pass, lint 18 warnings. Nội dung inspect dưới đây là lịch sử; D6 1–7 vẫn là contract có hiệu lực.

Ngày inspect và phê duyệt: **2026-09-09**. Người dùng đã chốt toàn bộ Decision Set D6, mục 1–7. Theo [P1 Feature Plan](PHASE2_P1_FEATURE_PLAN.md). Các phân tích bên dưới giữ bối cảnh trước implementation; kết quả mới theo báo cáo F6 phía trên. Baseline lúc inspect là F3: 188/188 backend, 6/6 frontend, lint 19 warnings.

## 1. Hiện trạng và điểm cần thống nhất

| Chủ đề | Source hiện tại | Hệ quả cho F6 |
|---|---|---|
| User fields | [User.java](../src/main/java/com/tmt/ecommerce/identity/entity/User.java): fullName non-null, length 100; phone nullable, length 20, không unique | Sửa hai trường này dự kiến không cần migration; không tự yêu cầu unique phone |
| Register | [AuthenticationService](../src/main/java/com/tmt/ecommerce/identity/service/AuthenticationService.java) đã lưu fullName/phone. Register DTO chỉ NotBlank name, chưa có name max/phone validation | Không suy ra đã tồn tại regex phone hoặc normalization. Không tự mở rộng sửa registration policy trong F6 |
| Frontend comment | auth.types.ts nói phone chưa được backend hỗ trợ | Comment cũ, trái source register; có thể sửa comment khi implement |
| Profile | [ProfilePage](../frontend/src/pages/public/ProfilePage.tsx) chỉ đọc AuthContext, chưa form/API save | Cần profile API Identity và userApi/form riêng |
| JWT | [JwtService](../src/main/java/com/tmt/ecommerce/common/security/JwtService.java) ký fullName/userId/roles lúc login/register | Token đang dùng giữ tên cũ sau khi DB đổi; không sửa payload token tại client |
| AuthContext | [AuthContext](../frontend/src/contexts/AuthContext.tsx) chỉ decode token bằng atob; loading cố định false | Cần hydrate tên từ server, chống response của user cũ ghi vào session mới; decode base64url/UTF-8 để tên tiếng Việt không bị lỗi |
| Principal | Jwt filter nạp UserDetails từ DB; CurrentUserPrincipal cung cấp ID | GET/PUT lấy đúng principal, không nhận ID mục tiêu từ client |
| Lỗi auth | apiClient xử lý 401 bằng clear token/redirect, 403 không logout | Giữ semantics; lỗi mạng/500 của profile không được giả thành logout |

Plan yêu cầu whitelist editable fields, bảo vệ userId/email/roles/status/password và xử lý tên sau refresh. Những điểm chưa có policy là format phone, trường được sửa và unknown-field handling; đề xuất bên dưới không phải yêu cầu đã được duyệt.

## 2. DECISION SET D6 đã duyệt

1. **Chỉ sửa fullName và phone** của chính người dùng đăng nhập, áp dụng buyer/vendor/admin. Email/ID chỉ đọc. Không thêm avatar, đổi email/password, OTP hoặc quản lý hồ sơ người khác.
2. **fullName:** trim đầu/cuối, bắt buộc 1–100 ký tự sau trim; cho phép Unicode/tên tiếng Việt, giữ khoảng trắng bên trong. Không tự đặt regex tên hoặc thay đổi chữ hoa/thường.
3. **phone:** tùy chọn, trim đầu/cuối, tối đa 20 ký tự sau trim; rỗng/null lưu null. **Đề xuất giữ dạng chuỗi liên hệ như schema hiện tại, chưa siết regex quốc gia/đầu số và không unique**. Không tự bỏ dấu `+`, đổi `0` thành `+84`, xác minh chủ số hoặc chặn hai tài khoản dùng cùng số. Nếu muốn giới hạn số điện thoại Việt Nam cần chốt riêng trước code.
4. **GET/PUT `/api/v1/users/me`**. PUT cập nhật bộ đôi fullName/phone; fullName bắt buộc, phone thiếu/null/rỗng nghĩa là xóa phone. FE luôn gửi cả hai trường để tránh xóa ngoài ý muốn. Response chỉ `id`, `email`, `fullName`, `phone`, bọc ApiResponse; không passwordHash/status/roles trong profile DTO.
5. **Unknown fields bị từ chối 400 INVALID_INPUT tại request profile**, bao gồm userId/id/email/roles/status/password. Không đổi cấu hình Jackson toàn ứng dụng. Dù request bị chế tác, service chỉ có khả năng ghi hai cột editable, không bind User entity từ body.
6. **Không đổi JWT khi save.** AuthContext cập nhật tên từ PUT thành công và GET lúc khôi phục session/login; giữ quyền từ cơ chế auth hiện tại. Dữ liệu profile không được thay userId/roles hay xác lập đăng nhập. Response đến sau logout/đổi token/user bị bỏ qua, kể cả session mới đăng nhập cùng userId.
7. **Hai lần cập nhật profile đồng thời:** bản ghi cuối commit thắng cho bộ đôi name/phone; chưa thêm version/conflict UI. SQL update chỉ hai cột này để tránh ghi đè status/password hoặc thay đổi khác từ snapshot User cũ. Caller rollback phải rollback profile; lỗi/row count không đúng không được báo save thành công.

Với mục 3, giới hạn length là kiểm tra dữ liệu theo schema, **không tuyên bố phone đã hợp lệ hoặc được xác minh**. Với mục 4, PUT khác PATCH: client muốn giữ số phải gửi lại số hiện tại.

## 3. Thiết kế triển khai sau duyệt

### Giai đoạn BE

- Identity sở hữu controller/service/DTO; xác thực tại HTTP và service, principal phải là người dùng hiện tại.
- GET đọc DB, không lấy fullName cũ trong JWT làm profile trả về. PUT validate/normalize, update whitelist trong transaction, đọc lại response từ DB mà không bị persistence context cũ ghi đè.
- Dùng profile DTO riêng; không truy cập entity/repository module khác. `USER_NOT_FOUND` 404 khi service không tìm thấy user; 401/403 theo security layer thực tế, không ép mọi thiếu user thành 404 qua filter.
- Validation/body sai/type sai/unknown fields trả envelope S1-02 (success=false, data=null); fieldErrors khi có lỗi validation theo trường. Không đổi validation register/login ngoài phạm vi.
- Không cần migration cho name/phone theo entity đã đọc; kiểm tra schema test trước verification, không sửa dữ liệu lịch sử tự động.

### Giai đoạn FE

- userApi + profile types; form loading/error/saving/success, field validation, email read-only, avatar chữ cái giữ nguyên.
- Load DB trước khi cho save; lỗi GET không dùng giá trị phone thiếu từ JWT để ghi đè DB. Khi PUT lỗi giữ draft, khi thành công dùng response chuẩn hóa để đồng bộ form và tên hiển thị.
- AuthContext hydrate server profile sau khôi phục session/login; GET thất bại không đồng nghĩa invalid credentials. Profile page có retry; không tạo vòng fetch/login/logout.
- Chống request cũ bằng session/token generation và route lifecycle. Correct base64url/UTF-8 decode ở helper đang phục vụ hiển thị JWT; không biến client decode thành kiểm chứng chữ ký/quyền.

### Giai đoạn test và verification

- HTTP GET/PUT user A chỉ tác động A; user B không đọc/sửa được qua payload giả; thiếu/sai token, role buyer/vendor/admin theo auth hiện tại; unknown fields bị từ chối và không làm thay đổi DB.
- Name null/blank/100/101, trim và tiếng Việt; phone null/blank/20/21, leading zero/plus, cùng phone giữa hai user theo D6; phone thiếu trong PUT xóa theo contract.
- MySQL kiểm persistence, rollback và chỉ cập nhật name/phone; password/status/roles/email không đổi, không lộ passwordHash. Không dùng Mockito làm evidence DB.
- FE/browser: save → tên cập nhật → reload token cũ vẫn hiện tên DB → logout/login vẫn đúng; phone clear; validation/error/retry; delayed response sau đổi account/logout không làm khôi phục tên/account cũ.
- Test helper UTF-8/token malformed và auth regression phù hợp; chạy full regression trên DB test giữ các guard tên schema của Sprint 1/F3, build BE/FE, lint và browser smoke.

Đầu ra triển khai: `PHASE2_P1_02_PROFILE.md` với số test thực chạy, commands/evidence, migration/rollback và giới hạn. Không đánh dấu hoàn tất từ số tests dự kiến.

## 4. Rollback và phạm vi

Không đổi token format/schema cho phạm vi đề xuất. Rollback UI/API không đảo tên/phone đã lưu; giữ dữ liệu. Auth hydration phải không gây lỗi dây chuyền nếu API unavailable khi chuyển phiên bản. Không mở rộng vào thay đổi role/status/ban, reset password hoặc unique-phone cleanup.

**Thực thi đã hoàn tất:** BE → FE → tests → verification theo D6 1–7, xem [báo cáo F6](PHASE2_P1_02_PROFILE.md). Contract giữ hiệu lực; chỉ chốt bổ sung nếu cần thay đổi phạm vi/policy về sau.
