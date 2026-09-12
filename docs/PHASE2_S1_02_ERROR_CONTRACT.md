# S1-02 — Error Response: implementation và kiểm chứng

Ngày: **2026-09-09**. Contract: [Decision Set được phê duyệt](PHASE2_SPRINT1_DECISIONS.md). Baseline: [S1-01](PHASE2_S1_01_BASELINE.md).

**Trạng thái:** Đã triển khai nền tảng Error Response S1-02 và các consumer liên quan. 25 test backend mới + 6 test frontend mới pass. Bộ backend chọn lọc 49/51 pass, chỉ còn hai lỗi baseline. Chưa triển khai S1-03 inventory hoặc S1-04 payment guard.

## 1. Response contract

```json
{
  "status": 404,
  "success": false,
  "errorCode": "SHOP_NOT_FOUND",
  "message": "Không tìm thấy gian hàng",
  "data": null
}
```

- HTTP status và body status nhất quán.
- success được tính từ status 2xx, tránh phải sửa tất cả success builders hoặc có hai cờ trái nhau.
- data được serialize cả khi null; error handlers không đưa payload vào data.
- Validation dùng optional fieldErrors, ví dụ `{ "name": "Tên không được để trống" }`, trong khi data vẫn null. Object-level errors dùng khóa `_global`, không cast mọi lỗi thành FieldError.
- Callback VNPay giữ RspCode/Message, không thêm success/errorCode vào giao thức gateway.

## 2. Thay đổi backend

- [ApiResponse](../src/main/java/com/tmt/ecommerce/common/dto/ApiResponse.java): success, data nullable xuất hiện trong JSON, fieldErrors.
- [ErrorCode](../src/main/java/com/tmt/ecommerce/common/exception/ErrorCode.java): bổ sung NOT_FOUND và mã HTTP framework 405/406/413/415; mapping status chung.
- [GlobalExceptionHandler](../src/main/java/com/tmt/ecommerce/common/exception/GlobalExceptionHandler.java): AppException, validation, auth/access denied, not found, malformed body/parameter, framework status/headers và fallback 500. Không lộ parser/SQL/exception details trong response.
- [RestSecurityExceptionHandler](../src/main/java/com/tmt/ecommerce/common/security/RestSecurityExceptionHandler.java): serializer dùng ObjectMapper của Spring cho AuthenticationEntryPoint 401 và AccessDeniedHandler 403.
- SecurityConfig nối hai handler trên. JwtAuthenticationFilter giữ xử lý token không hợp lệ theo đường anonymous nhưng lỗi hạ tầng xác thực trả 500 envelope, không âm thầm biến thành 401.
- ShopService dùng mã cụ thể cho chưa có shop, shop trùng, tên trùng và approve sai trạng thái; `GET /api/v1/shops/my-shop` không có shop trả 404/SHOP_NOT_FOUND.
- AuthenticationService dùng EMAIL_ALREADY_EXISTS và USER_NOT_FOUND thay dò chuỗi.
- Ba assertions về domain exception trong ShopServiceTest đổi sang kiểm tra AppException/errorCode tương ứng. Không sửa baseline failure ở matcher publishEvent và không thay business transition.

Order/Product/Voucher legacy exceptions còn được handler trả INVALID_INPUT để tương thích. Việc chuyển từng nhánh thành mã domain cụ thể tiếp tục theo slice liên quan; **không tuyên bố toàn bộ Feature 9 đã migration xong**.

## 3. Thay đổi frontend

- Dùng chung ApiResponse type giữa auth/common: success/status/errorCode/fieldErrors và data nullable.
- [errorCodes.ts](../frontend/src/api/types/errorCodes.ts) phản chiếu enum backend; [getApiError](../frontend/src/api/apiError.ts) đọc Axios error an toàn, giữ message khi gặp unknown code, hỗ trợ non-JSON/network failure.
- RequireShop nhận diện SHOP_NOT_FOUND bằng code. RegisterForm nhận diện EMAIL_ALREADY_EXISTS bằng code, không kiểm tra message chứa “email”.
- Login/Register, đăng ký Shop và hai form Product đọc fieldErrors. Helper vẫn đọc validation data cũ khi gặp HTTP 400 để hỗ trợ chuyển phiên bản.
- apiClient xử lý UNAUTHORIZED/HTTP 401, không logout vì 403. Request login/register lỗi không redirect lại login làm mất thông báo form; lỗi 401 của protected APIs vẫn điều hướng login.
- Những consumer list/review trước đây giả định data luôn có đã được thêm kiểm tra null để phù hợp type mới; không thay nghiệp vụ dữ liệu.

## 4. Kiểm chứng đã chạy

| Suite/check | Kết quả |
|---|---|
| ErrorResponseTest — MockMvc/serialization | 13/13 pass |
| JwtErrorResponseTest — filter infrastructure/denied writer | 2/2 pass |
| ErrorContractIntegrationTest — HTTP thật, Spring + MySQL | 10/10 pass |
| Frontend apiError tests | 6/6 pass |
| Existing OrderServiceImplTest | 3/4 pass, lỗi baseline PENDING cancellation |
| Existing ProductInternalServiceImplTest | 4/4 pass |
| Existing ShopServiceTest | 8/9 pass, lỗi baseline overload publishEvent |
| Existing VoucherInternalServiceImplTest | 4/4 pass |
| Existing PaymentIpnSecurityIntegrationTest | 4/4 pass |
| Existing ShopApprovalIntegrationTest | 1/1 pass |
| Backend package -DskipTests | PASS |
| Frontend TypeScript/Vite build | PASS, bundle warning như baseline |
| Frontend lint | Exit 0, 19 warnings, bằng baseline |

Backend cuối cùng: **51 tests, 1 failure, 1 error, 0 skipped**. Không có failure mới trong tập kiểm tra này; Maven test vẫn exit 1 vì hai lỗi baseline. Không chạy lại full E2E/NPlus1/những suite ngoài phạm vi chọn lọc, không suy ra toàn dự án xanh.

Các case mới kiểm chứng: 400 body/parameter/validation, global validation error, 401 thiếu/sai/hết hạn JWT, 403 non-admin, 404 domain/route, 405 có Allow header, 415 media type, 500 không lộ chi tiết, success response cũ, duplicate email, fieldErrors và VNPay envelope.

## 5. Môi trường và evidence

MySQL 8.0.46 riêng trên 127.0.0.1:33317; dùng datadir test S1-01 nhưng **tạo schema ecommerce_s102 mới**, không chạy trên schema ecommerce của ứng dụng. Grant application account chỉ schema test. SMTP override 127.0.0.1:1; không gửi email ra ngoài. Instance đã shutdown sau kiểm chứng.

Maven 3.9.16 offline với `-Dmaven.repo.local=C:/Users/Dell/.m2/repository`. Biên dịch trong sandbox gặp lỗi truy cập jakarta.mail-api trong cache; đã chạy lại với quyền được người dùng cho phép và kiểm chứng thành công. Không nâng dependency hoặc sửa pom/wrapper.

Selector backend:

```text
-Dtest=ErrorResponseTest,JwtErrorResponseTest,ErrorContractIntegrationTest,OrderServiceImplTest,ProductInternalServiceImplTest,ShopServiceTest,VoucherInternalServiceImplTest,PaymentIpnSecurityIntegrationTest,ShopApprovalIntegrationTest
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s102?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline
-Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1
-Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000
```

Password là giá trị local-test-only. Không chạy integration với datasource mặc định trong src/test/resources/application.yml; instance test đã dừng nên phải khởi động và xác minh port/datadir trước khi tái chạy.

Frontend: `npm.cmd run build`, `npm.cmd run lint`, `node --test tests/apiError.test.mjs`. Tests frontend dùng Node hiện có, không thêm dependency test.

Evidence:

- [Backend tests log](../target/s1-02/tests-final.log), [test summary JSON](../target/s1-02/test-summary.json); bản text reports lưu tại target/s1-02/reports.
- [Backend package](../target/s1-02/build.log).
- [Frontend build](../target/s1-02/frontend-build.log), [lint](../target/s1-02/frontend-lint.log), [tests](../target/s1-02/frontend-tests.log).
- [MySQL identity](../target/s1-02/mysql-identity.txt), [server lifecycle](../target/s1-02/mysql-server.log).

## 6. Migration, rollback và giới hạn

- Không migration DB nghiệp vụ. success/errorCode/fieldErrors là response properties.
- Deploy frontend và backend cùng slice: client cũ lấy validation từ data sẽ không đọc được fieldErrors mới. Frontend mới có fallback đọc validation cũ nhưng RequireShop cần mã SHOP_NOT_FOUND mới, không quay lại dò text.
- Nếu rollback, phối hợp BE/FE cho status 400 cũ và errorCode mới của Shop. Không rollback riêng getMyShop thành 400 trong khi UI chỉ dùng code mới.
- Không thêm role, prior shop status, ban enforcement, Order lock, synchronous inventory hoặc refund trong S1-02.
- Chưa thực hiện browser smoke test tương tác; HTTP integration và frontend helper/build/lint đã kiểm chứng. Không công bố test coverage %.
- Git workspace chưa được nhận diện là repository; không tạo commit. Các log trong target có thể bị mất khi clean.

Bước tiếp theo: S1-03 theo D1 đã chốt, với baseline errors được giữ riêng để không nhầm regression.
