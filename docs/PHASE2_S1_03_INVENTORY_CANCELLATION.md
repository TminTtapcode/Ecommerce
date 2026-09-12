# S1-03 — Hủy đơn và hoàn kho đồng bộ

Ngày kiểm chứng: **2026-09-09**. Trạng thái: **đã triển khai và kiểm chứng S1-03** theo [D1 đã duyệt](PHASE2_SPRINT1_DECISIONS.md). S1-04 Payment Guard chưa triển khai; không coi kết quả này là hoàn tất an toàn toàn bộ luồng IPN/cancellation.

## 1. Thay đổi và transaction boundary

Trước đây, Order commit CANCELLED rồi ProductOrderCancelledListener mới hoàn kho trong REQUIRES_NEW. Listener bắt và nuốt exception; restoreStock bỏ qua input/variant lỗi và không kiểm tra row count. Vì vậy status và stock có thể lệch nhau.

Luồng hiện tại:

```text
PATCH /api/v1/vendor/orders/{id}/status
  → kiểm tra role VENDOR/ADMIN tại controller (giữ nguyên)
  → OrderServiceImpl.updateVendorOrderStatus @Transactional
      → OrderRepository.findByIdForUpdate: PESSIMISTIC_WRITE
      → kiểm tra ownership qua ShopInternalService
      → kiểm tra matrix: chỉ CONFIRMED → CANCELLED
      → set CANCELLED
      → từng OrderItem, sắp theo variant ID
          → ProductInternalService.restoreStock (MANDATORY)
          → kiểm tra ID/quantity, tìm variant
          → atomic UPDATE variant stock + quantity, phải đúng 1 row
          → atomic UPDATE product stock + quantity, phải đúng 1 row
      → save Order và publish event cho notification sau commit
  → commit status + toàn bộ stock, hoặc rollback tất cả
```

- Product chỉ được gọi qua internal contract; Order không truy cập entity/repository Product. MANDATORY buộc restoreStock tham gia transaction của caller, không tự mở transaction độc lập. Caller chịu trách nhiệm authorization và chống gọi lặp.
- Quantity lấy từ OrderItem đã lưu, không nhận quantity từ request hủy. Đơn không có item bị từ chối bằng lỗi hệ thống để tránh coi dữ liệu bất thường là đã hoàn kho thành công.
- Tất cả vendor status transitions dùng cùng khóa Order, bao gồm SHIPPED. Request đến sau đọc trạng thái sau commit và phải kiểm tra lại matrix.
- Duplicate cancellation trả `INVALID_ORDER_STATE_TRANSITION` (400). Missing Order trả `ORDER_NOT_FOUND` (404), invalid restore input `INVALID_INPUT` (400), missing variant `VARIANT_NOT_FOUND` (404), row count bất thường `INTERNAL_SERVER_ERROR` (500). Envelope theo S1-02.
- Đã xóa ProductOrderCancelledListener. Event không còn làm tăng stock; notification listener vẫn AFTER_COMMIT. Không thêm outbox, refund, voucher restoration hoặc đưa item về Cart.
- Hoàn kho không kiểm tra trạng thái kinh doanh Shop/Product; chủ shop BANNED vẫn xử lý đơn cũ theo ownership. Không triển khai các nhánh ban enforcement S1-05/06 trong bước này.

## 2. Lock và phạm vi bảo đảm

- Một Order: khóa bi quan giữ đến hết transaction. Hai hủy đồng thời chỉ một request thành công; hủy đối đầu SHIPPED chỉ transition thắng khóa được commit.
- Stock dùng phép cộng atomic tại DB, không đọc rồi ghi `stock += quantity` từ entity. Nhiều variant chung product cộng tổng đúng theo quantity từng item.
- Thứ tự cancellation: Order → từng variant ID tăng dần → product tương ứng. Checkout hiện vẫn variant → product theo item của checkout và không khóa một Order cũ. Sắp variant giúp ổn định thứ tự nhưng **không chứng minh loại bỏ mọi deadlock giữa nhiều Order/checkout/product mutation**. Nếu DB phát hiện deadlock hoặc update thất bại, transaction phải rollback; chưa thêm tự động retry. Các test cạnh tranh trong báo cáo tập trung cùng Order.
- Không dùng clearAutomatically trên stock UPDATE vì có thể detach Order đang đổi status. Luồng này không sửa stock trên managed Product/Variant sau bulk UPDATE; kiểm chứng stock bằng lần đọc DB độc lập sau transaction.
- **Rủi ro còn lại đã xác định:** updateOrderStatusByGroup vẫn bulk-update không guard. IPN trễ có thể ghi CONFIRMED lên CANCELLED; sau đó hủy lại sẽ có nguy cơ hoàn kho thêm. Khóa Order trong S1-03 không ngăn được update sai semantics sau khi lock được giải phóng. S1-04 phải guard PENDING → CONFIRMED và kiểm chứng IPN lặp/race trước khi xem toàn luồng đủ điều kiện phát hành.

## 3. Test evidence

| Suite | Kết quả |
|---|---|
| InventoryCancellationIntegrationTest — mới, Spring/HTTP/MySQL thật | 20/20 pass |
| OrderServiceImplTest | 4/4 pass |
| ProductInternalServiceImplTest | 7/7 pass, gồm 3 test row-count/missing variant mới |
| VendorOrderManagementTest | 4/4 pass |
| ErrorResponseTest | 13/13 pass |
| JwtErrorResponseTest | 2/2 pass |
| ErrorContractIntegrationTest | 10/10 pass |
| PaymentIpnSecurityIntegrationTest | 4/4 pass (không phải evidence late IPN guard) |
| VoucherInternalServiceImplTest | 4/4 pass |
| ShopServiceTest | 8/9 pass, lỗi baseline overload publishEvent |
| Backend package -DskipTests | PASS |

**Tổng cuối: 77 tests, 76 pass, 1 failure, 0 errors, 0 skipped.** Maven test exit 1 do lỗi Shop baseline. Không sửa lỗi này ngoài phạm vi S1-03.

Test Order baseline trước đó dùng PENDING cho case cancellation đã đổi thành CONFIRMED theo contract, bổ sung verify restoreStock. Test invalid transition ở VendorOrderManagementTest đã đổi expected exception sang AppException + domain code. Product tests trước đây mong input sai bị bỏ qua đã đổi thành mong exception. Đây là cập nhật expectation có chủ đích, không đổi matrix để làm test xanh.

20 integration cases bao gồm:

- Nhiều variant chung product, duplicate cancellation và bốn trạng thái nguồn không hợp lệ.
- Ownership khác shop; API owner thiếu role; API missing Order; HTTP hủy thành công và duplicate đúng error envelope.
- Variant thứ hai thiếu hoặc quantity thứ hai bằng 0: rollback stock item đầu và status.
- Lỗi DB thật khi stock product vượt INT ở lần restore thứ hai: rollback cả hai variant, product và status.
- Caller rollback sau khi service trả về: status/stock vẫn rollback.
- Shop BANNED/product HIDDEN vẫn hoàn kho; replay event không cộng thêm; gọi restore ngoài transaction bị từ chối.
- Hai hủy đồng thời, hủy thắng SHIPPED và SHIPPED thắng hủy: dùng hai worker/transaction riêng, latch giữ khóa, xác nhận request còn lại chưa hoàn thành trước khi nhả khóa, rồi kiểm tra trạng thái/stock đã commit.
- Cart → checkout COD giảm variant/product → hủy phục hồi đúng stock ban đầu.

Không chạy lại full E2E/NPlus1 suites hoặc browser smoke. Không sửa frontend; dùng HTTP integration để kiểm chứng endpoint/envelope thay đổi, không tuyên bố frontend build/lint mới trong S1-03.

## 4. Môi trường và cách chạy

MySQL 8.0.46 riêng tại **127.0.0.1:33317**, datadir test S1-01; schema mới **ecommerce_s103**, account s1baseline chỉ có quyền schema test. Test mới kiểm tra tên database trước fixture; không dùng datasource mặc định trỏ schema ecommerce. Không thay đổi MySQL80 service của máy. SMTP override localhost:1 và timeout 1000ms, không gửi email ngoài.

Maven 3.9.16 offline, Java 21. Cache dependency ngoài workspace cần chạy với quyền được approval review chấp thuận do lỗi sandbox truy cập jakarta.mail-api. Không nâng dependency hoặc đổi pom.

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
-Dtest=InventoryCancellationIntegrationTest,OrderServiceImplTest,ProductInternalServiceImplTest,VendorOrderManagementTest,ErrorResponseTest,JwtErrorResponseTest,ErrorContractIntegrationTest,ShopServiceTest,VoucherInternalServiceImplTest,PaymentIpnSecurityIntegrationTest
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline
-Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1
-Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000
test
```

Password trên chỉ dành cho local test. Khởi động và kiểm tra đúng instance test trước khi chạy; không copy selector này rồi bỏ datasource override.

Evidence: [tests-final.log](../target/s1-03/tests-final.log), [test-summary.json](../target/s1-03/test-summary.json), [build.log](../target/s1-03/build.log), [MySQL identity](../target/s1-03/mysql-identity.txt), [MySQL lifecycle](../target/s1-03/mysql-server.log). XML reports lưu trong target/s1-03/reports; target có thể bị xóa khi clean.

## 5. Migration, deploy và rollback

- Không đổi schema; không backfill stock lịch sử CANCELLED.
- Deploy đồng bộ Order service + Product contract/implementation + xóa listener. Không chạy bản cũ và mới cùng nhận cancellation; bản cũ không tham gia khóa và hoàn kho sau commit.
- Artifact phải không còn ProductOrderCancelledListener.class. Không hot-copy source xóa listener nhưng giữ compiled class cũ. Đóng gói backend đã được kiểm chứng.
- Không rollback bằng cách chỉ tắt synchronous restore hoặc chỉ bật lại listener. Nếu cần rollback, dừng nhận cancellation, triển khai bộ code nhất quán và đối soát các order/stock liên quan trước khi mở lại. Quay về bản cũ đồng nghĩa khôi phục rủi ro hoàn kho sau commit.
- Không tự sửa dữ liệu lịch sử, refund hoặc payment reconciliation. Bước kế tiếp là **S1-04 — Payment Group Guard** theo contract đã chốt.
