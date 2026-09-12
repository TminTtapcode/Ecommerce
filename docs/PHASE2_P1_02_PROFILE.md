# P1-02 / F6 — Profile

Ngày kiểm chứng: **2026-09-09**. **Đã triển khai và kiểm chứng F6** theo [D6 đã duyệt](PHASE2_P1_02_D6_DECISIONS.md), theo các giai đoạn BE → FE → tests → verification. **221/221 backend, 9/9 frontend tests pass; build BE/FE và browser smoke pass; lint 18 warnings.** Chưa deploy ứng dụng hoặc triển khai F1.

## 1. Backend — profile chỉ của principal

Identity bổ sung ProfileController, ProfileService, ProfileUpdateRequest và ProfileResponse. HTTP/service yêu cầu authenticated principal qua CurrentUserPrincipal; không nhận userId đích từ client. Buyer/vendor/admin đều dùng cùng API.

| API | Contract |
|---|---|
| GET `/api/v1/users/me` | Đọc DB, trả id/email/fullName/phone |
| PUT `/api/v1/users/me` | Chỉ fullName/phone, trả profile chuẩn hóa sau update |

```json
{
  "fullName": "Nguyễn Thị Ánh",
  "phone": "+84 123"
}
```

- fullName trim, không blank, tối đa 100 ký tự; giữ Unicode và khoảng trắng bên trong.
- phone trim, tối đa 20; thiếu/null/rỗng lưu null. Không regex/unique/OTP, không đổi đầu số. Đây là chuỗi liên hệ chưa xác minh.
- DTO dùng setters nhận Object rồi kiểm kiểu chuỗi/null, nên số/boolean/array/object không bị tự ép thành String. JsonAnySetter từ chối trường không thuộc whitelist, gồm id/userId/email/roles/status/password/passwordHash/avatar. Không đổi Jackson toàn ứng dụng hoặc bind User entity từ body.
- Controller Bean Validation trả fieldErrors khi lỗi theo trường. Service validate lại khi gọi trực tiếp. Body/unknown/type/validation sai trả 400 INVALID_INPUT theo S1-02, success=false/data=null. Service thiếu user trả 404 USER_NOT_FOUND; lỗi auth tại filter vẫn theo 401/403 hiện có.
- Response chỉ bốn trường đã chốt, không roles/status/passwordHash. Không sửa register/login policy; chỉ sửa comment FE cũ về phone đã được backend hỗ trợ.

## 2. Transaction và đồng thời

```text
ProfileService.update @Transactional
  → principal ID + validate request
  → UPDATE users SET full_name=?, phone=? WHERE id=?
  → kiểm row count (0: USER_NOT_FOUND; khác 1: system error)
  → constructor projection đọc profile từ DB
  → commit hoặc caller rollback
```

Không save User entity để cập nhật profile, không ghi email/password/status/roles. Query projection không lấy giá trị fullName/phone cũ từ managed User đã có trong persistence context; không clear/detach toàn bộ entities của caller. Repository bulk update flush pending changes trước khi chạy; caller vẫn chịu trách nhiệm các entity mutation khác trong transaction của mình.

Hai update profile cùng user được DB serialize theo row update; bản cuối commit thắng cả cặp name/phone. Chưa có version hoặc stale-edit conflict UI theo D6. Integration đã kiểm caller rollback và trường hợp managed User cũ tồn tại khi password/status được thay đổi bằng SQL trước profile update: profile không ghi đè hai trường đó.

Không dependency qua Order/Product/Payment/Shop, không schema mới. Không sửa JWT/backend auth semantics để đổi tên.

## 3. Frontend và phiên đăng nhập

ProfilePage đọc dữ liệu server trước khi cho sửa; form fullName/phone, email read-only, avatar chữ cái. Có loading, retry, saving, validation, thông báo lỗi/thành công. Lỗi GET không dùng phone thiếu từ JWT để submit; lỗi PUT giữ draft. Thành công dùng response đã normalize để cập nhật form và tên.

AuthContext hydrate profile khi mount/login, cập nhật fullName sau PUT mà giữ userId/roles/identity từ cơ chế auth hiện có. Helper decode base64url/UTF-8 giữ đúng tên tiếng Việt; decode này chỉ phục vụ hiển thị, không xác minh chữ ký hoặc cấp quyền. Token không bị sửa/đổi khi save; reload với token còn tên cũ vẫn lấy profile DB.

Profile request giữ **token và session generation ngay lúc bắt đầu**. Axios dùng token đã gắn thay vì lấy token của tài khoản mới ở thời điểm interceptor chạy. Success/error đến sau logout/đổi phiên bị bỏ qua, kể cả login lại cùng user/token. Request write tăng version để loại GET cũ; các hydration GET đồng thời được chia sẻ version, không làm trang Profile mất kết quả vì một GET nền bắt đầu sau.

apiClient ghi generation/token của request; 401 của phiên cũ không clear token/redirect phiên mới. 401 của phiên hiện tại giữ xử lý cũ, 403/mạng/500 không logout. ProfilePage có lifecycle guard để response sau unmount không cập nhật form. Đây là phần chỉnh apiClient cần thiết cho contract chống response cũ của F6; đã smoke lại F3 trên frontend mới.

## 4. Test evidence

| Suite/check | Kết quả |
|---|---:|
| ProfileIntegrationTest — mới, Spring/HTTP/MySQL | **33/33** |
| Full configured Maven suite, gồm 188 tests trước F6 | **221/221** |
| Failures / errors / skipped full run cuối | **0 / 0 / 0** |
| FE apiError tests | **6/6** |
| FE authSession tests — mới | **3/3** |
| Backend package -DskipTests | PASS |
| Frontend TypeScript + Vite build | PASS |
| Frontend lint | Exit 0, **18 warnings** |
| Profile browser smoke | PASS, exit 0 |
| Admin F3 browser regression trên frontend mới | PASS, exit 0 |

Profile integration chạy riêng lần đầu 33/33 pass, sau đó full regression không selector `-Dtest` đạt 221/221. Giữ exclusion có sẵn FullSystemDataSeederTest, không thêm skip/exclusion. Tổng hợp XML chỉ các suite xuất hiện trong log full cuối, không cộng report cũ.

33 cases mới bao gồm ba role, dữ liệu A/B cách ly, whitelist response và request; chín loại unknown field; 11 input/body/type không hợp lệ; name/phone limits sau trim, fieldErrors; phone thiếu/null/blank, duplicate phone và chuỗi không regex; HTTP/service auth; caller rollback; stale managed snapshot không ghi đè password/status; user đã xóa không được báo update thành công; hai worker/transaction tranh update phải đợi row lock và ghi đúng cặp cuối.

3 FE tests kiểm Unicode/base64url, token malformed/missing identity và session generation khi logout/re-login. Không coi helper tests là bằng chứng React/network race; các luồng đó có browser smoke dưới đây.

### Browser smoke

Harness `frontend/tests/profile.browser-smoke.mjs`: Edge **152.0.4191.66**, desktop 1440×1000, frontend dist + JAR + MySQL thật. Fixture users đăng ký qua API, thao tác form/login/logout bằng browser. CDP giữ/thay response để tạo network failure/race có kiểm soát; không mock persistence backend.

1. Save tên tiếng Việt/phone có khoảng trắng → normalize và DB đúng; localStorage JWT không đổi. Reload token cũ → profile server mới. Đã xem screenshot form sau save.
2. Xóa phone → null DB; name blank không submit; logout/login thực bằng form giữ tên đúng.
3. Inject 500 cho profile GET → giữ session, không có form editable, retry khôi phục.
4. Giữ response PUT, logout/login tài khoản khác, trả 401 cho request cũ → không logout hoặc ghi đè profile mới.
5. Lặp với cùng user đăng nhập lại → session mới vẫn còn, dữ liệu đã commit ở DB vẫn đọc được.
6. Giữ response PUT thành công, đổi account rồi nhả response cũ → không ghi đè profile account mới.

PUT trong case giữ response đã commit DB trước khi logout; bỏ qua response client **không hủy một mutation server đã nhận**. Các lần smoke trước khi hoàn thiện harness vướng selector menu logout và timing fault injection giữa hydration/page GET; đã sửa harness và chạy lại toàn luồng cuối pass.

Smoke F3 chạy harness hiện có với schema/evidence override riêng trong target: Admin hủy COD shop BANNED, history/stock, replay 409, Vendor/Admin stale conflict và Admin DELIVERED đều pass. Không chạy mọi browser/mobile hoặc toàn bộ màn hình ứng dụng. Bundle vẫn cảnh báo >500 kB; lint giảm 19 → 18 do bỏ catch variable không dùng trong decode helper, không dọn warning ngoài phạm vi.

## 5. Môi trường và tái chạy

Java 21.0.11, Maven 3.9.16 offline; MySQL 8.0.46 riêng **127.0.0.1:33317**, datadir `target/s1-baseline/mysql-data`. Regression schema **ecommerce_s103_s104_s105_p101_p102**, smoke **ecommerce_p102_smoke**. User s1baseline/password s1-local-only chỉ cho local test. SMTP 127.0.0.1:1, timeout 1000ms, không gửi mail ngoài. Cache jakarta.mail-api cần chạy Maven qua approval review sau lỗi sandbox; Edge test cũng dùng approval review cho CDP.

Sau verification đã dừng backend/Edge smoke và shutdown MySQL test; service MySQL80 của máy vẫn Running. Data/evidence test được giữ lại.

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103_s104_s105_p101_p102?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline -Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1 -Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000 test
```

Khởi động/xác minh instance test trước chạy. Guard F6 yêu cầu prefix schema trên; giữ các guard Sprint 1/F3. Không bỏ override hoặc chạy vào schema ứng dụng. FE chạy `npm.cmd run build`, `npm.cmd run lint`, `node --experimental-strip-types --test tests/apiError.test.mjs tests/authSession.test.mjs`.

Browser harness chạy từ root bằng `node frontend/tests/profile.browser-smoke.mjs` sau khi backend chạy 8080 bằng test config, datasource ecommerce_p102_smoke/SMTP local; Edge profile riêng CDP 9222. Harness serve frontend/dist ở 5173. Bản F3 regression trong target đổi bootstrap schema sang chính smoke schema này, không ghi ROLE_ADMIN vào DB ứng dụng.

Evidence: [full tests](../target/p1-02/full-tests.log), [test summary](../target/p1-02/test-summary.json), [Profile riêng](../target/p1-02/tests-initial.log), [backend build](../target/p1-02/build.log), [FE build](../target/p1-02/frontend-build.log), [lint](../target/p1-02/frontend-lint.log), [FE tests](../target/p1-02/frontend-tests.log), [Profile browser JSON](../target/p1-02/browser-smoke.json), [browser log](../target/p1-02/browser-smoke.log), [F3 browser regression](../target/p1-02/admin-regression/browser-smoke.json), [schema check](../target/p1-02/schema-check.txt). XML tại target/p1-02/reports, screenshots tại target/p1-02/screenshots. Evidence trong target có thể mất khi clean; báo cáo giữ kết quả/cách tái chạy.

## 6. Migration, rollback và giới hạn

- **Không migration F6.** SHOW COLUMNS/INDEX trên schema test xác nhận full_name varchar(100) NOT NULL, phone varchar(20) NULL, chỉ email có unique ngoài primary key. Không chỉnh dữ liệu/schema ứng dụng.
- Deploy API trước frontend hydration/form. Nếu rollback API, frontend cần rollback tương ứng; trang Profile có lỗi/retry khi API không có, lỗi 404/500 không làm logout. Không đảo tên/phone đã commit hoặc sửa JWT để rollback.
- Phone chưa được kiểm chứng định dạng/quyền sở hữu, không unique theo D6. PUT thiếu phone xóa số; UI gửi cả hai trường. Hai update cùng profile vẫn last-commit-wins, không có optimistic conflict UI.
- Request server đã nhận có thể commit sau logout; các guard chống response cũ ở client, không phải cơ chế revoke/cancel transaction. Chưa mở rộng multi-tab live synchronization; reload/login hydrate từ server.
- Không sửa register validation, role/status policy, email/password, avatar/OTP. Các giới hạn Sprint 1/F3 về payment/refund/stock/outbox giữ nguyên.
- Chưa deploy hoặc tạo commit (workspace không được Git nhận diện ở các bước trước). Feature tiếp theo là P1-03/F1 Notification realtime, chưa thực hiện trong lượt F6.
