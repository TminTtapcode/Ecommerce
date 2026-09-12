# S1-04 — Payment Group Guard

Cập nhật sau bước này: [S1-07/08](PHASE2_S1_07_08_VERIFICATION.md) đã bổ sung kiểm tra amount bắt buộc/hợp lệ và quy đổi chính xác, PaymentGroupGuardIntegrationTest đạt 20/20. Giới hạn về amount ghi phía dưới mô tả thời điểm S1-04, đã được xử lý trong verification; các giới hạn protocol/refund khác vẫn còn.

Cập nhật sau bước này: **S1-05/06 đã hoàn tất** theo [báo cáo Ban/Unban và enforcement](PHASE2_S1_05_06_SHOP_BAN_ENFORCEMENT.md). Các mốc chưa triển khai bên dưới là snapshot tại thời điểm kết thúc S1-04.

Ngày kiểm chứng: **2026-09-09**. **Đã triển khai và kiểm chứng** theo [Decision Set](PHASE2_SPRINT1_DECISIONS.md). S1-05/06 chưa triển khai.

## 1. Thay đổi

Trước đây IPN khóa Payment nhưng Order bị bulk-update theo payment_group_id không kiểm tra trạng thái. Callback trễ hoặc payment attempt khác có thể ghi CONFIRMED lên CANCELLED/SHIPPED/DELIVERED. Sau S1-03, hồi sinh CANCELLED còn có thể cho phép hủy lại và hoàn kho thêm.

Đã thay internal API tổng quát `updateOrderStatusByGroup(group, status)` bằng `confirmPendingOrdersByGroup(group)`; chỉ có PaymentGatewayService là caller trong source. Đã xóa repository bulk-update không guard.

```text
Checkout → N Order chung payment_group_id → Payment attempt
IPN → verify signature → lock Payment by transactionId
    → Payment không PENDING: return (giữ idempotency hiện có)
    → kiểm tra amount theo logic hiện có
    → responseCode 00:
        Payment = SUCCESS
        Order contract → SELECT group orders ORDER BY id FOR UPDATE
            PENDING → CONFIRMED + OrderConfirmedEvent
            mọi status khác → giữ nguyên, không event
    → lưu gatewayTransactionId + rawData
    → commit Payment và Order cùng transaction
    → notification / log đối soát sau commit
```

- Order không truy cập Payment entity/repository; Payment chỉ gọi Order internal contract. Không thêm dependency qua Product/Cart/Shop.
- JPA dirty checking cập nhật các Order đã khóa; không cần clearAutomatically, không detach Payment đang được xử lý.
- Payment SUCCESS được ghi nhận kể cả khi Order đã hủy hoặc group không có Order. Không sửa Order để ép khớp trạng thái tiền.
- Log sau commit: `event=payment_order_confirmation_skipped paymentGroupId=... skippedOrders={id=status} missingGroup=...`. Bao gồm cả CONFIRMED đã tồn tại; việc bị bỏ qua không tự chứng minh một lỗi thanh toán. Dùng group ID đối chiếu Payment, gatewayTransactionId và rawData đã lưu.
- IPN không gọi restoreStock. Hủy lặp sau late IPN vẫn bị từ chối và stock không tăng thêm.
- Giữ envelope callback `RspCode`/`Message`, không đổi thành ApiResponse. Callback đã xử lý vẫn trả `RspCode=00` theo hành vi endpoint hiện tại.

## 2. Transaction và lock

IPN khóa Payment attempt trước, sau đó khóa các Order của nhóm theo ID. Vendor cancellation/shipping khóa cùng hàng Order theo S1-03 và không gọi ngược Payment. Hai attempt khác nhau có khóa Payment riêng nhưng cùng phải chờ Order; request thứ hai thấy trạng thái mới và không phát confirmation event thêm.

Nếu cancellation commit trước, IPN đọc CANCELLED và bỏ qua. Nếu late IPN giữ khóa Order trước, cancellation đợi rồi thực hiện transition hợp lệ trên trạng thái hiện tại. Trong cả hai thứ tự đã test, Order cuối cùng CANCELLED, stock chỉ hoàn một lần, Payment SUCCESS vẫn được lưu.

Không thay đổi checkout grouping, không thêm khóa toàn bộ hệ thống hoặc tự động retry deadlock. Các vấn đề cạnh tranh stock giữa nhiều checkout/product mutation ngoài phạm vi S1-03/04 vẫn giữ giới hạn đã ghi trong [báo cáo S1-03](PHASE2_S1_03_INVENTORY_CANCELLATION.md).

## 3. Kiểm chứng

**PaymentGroupGuardIntegrationTest: 13/13 pass**, dùng Spring + MySQL thật, chữ ký HMAC với test secret và strategy thật, không mock Payment/Order/Product/transaction.

- Late IPN sau cancellation, callback lặp và thử hủy lần nữa: không hồi sinh/hoàn kho thêm.
- Giữ nguyên CONFIRMED, SHIPPED, DELIVERED (3 cases).
- Mixed group đủ các trạng thái: chỉ PENDING đổi; probe AFTER_COMMIT xác nhận event đúng một lần cho Order được đổi.
- Failed IPN giữ Order PENDING; callback tiếp theo không ghi lại Payment đã terminal theo semantics hiện có.
- Payment tham chiếu group không có Order: vẫn lưu SUCCESS để đối soát.
- Caller rollback: Payment và Order đều về PENDING, confirmation event không chạy sau commit.
- Cancellation giữ khóa trước IPN và IPN giữ khóa trước cancellation.
- Hai callback đồng thời cùng attempt và hai attempt khác nhau cùng group: một confirmation event.
- HTTP IPN ký bằng test secret, không JWT, late và duplicate trả envelope gateway; stock/status đúng.

Concurrency tests dùng worker, connection và transaction độc lập; latch giữ transaction đầu và xác nhận request thứ hai chưa hoàn thành trước khi nhả khóa. Đọc lại trạng thái/stock sau commit. Đây không phải stress test hoặc chứng minh loại bỏ mọi deadlock.

| Suite | Kết quả |
|---|---|
| PaymentGroupGuardIntegrationTest (mới) | 13/13 |
| InventoryCancellationIntegrationTest | 20/20 |
| OrderServiceImplTest + VendorOrderManagementTest | 8/8 |
| ProductInternalServiceImplTest | 7/7 |
| ErrorResponseTest + JwtErrorResponseTest + ErrorContractIntegrationTest | 25/25 |
| PaymentIpnSecurityIntegrationTest | 4/4 |
| VoucherInternalServiceImplTest | 4/4 |
| ShopServiceTest | 8/9, lỗi baseline overload publishEvent |
| Backend package -DskipTests | PASS |

**Tổng: 90 tests, 89 pass, 1 failure, 0 errors, 0 skipped.** Maven test exit 1 vì Shop baseline. Không sửa lỗi Shop trong S1-04. Không chạy lại full E2E/NPlus1, không thay frontend hoặc chạy browser smoke; không công bố frontend build/lint mới.

## 4. Môi trường và evidence

MySQL 8.0.46 riêng tại 127.0.0.1:33317, datadir test S1-01, schema mới **ecommerce_s103_s104** để chạy cả test S1-03 có guard tên database. Account s1baseline được grant riêng schema test. SMTP localhost:1, timeout 1000ms. Không sử dụng schema ecommerce của ứng dụng.

Maven 3.9.16 offline, Java 21; lỗi sandbox truy cập jakarta.mail-api cache được xử lý bằng chạy lại sau approval review. Không nâng dependency hoặc đổi pom. Tham số tái chạy (sau khi khởi động, xác minh đúng instance test):

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
-Dtest=PaymentGroupGuardIntegrationTest,InventoryCancellationIntegrationTest,OrderServiceImplTest,ProductInternalServiceImplTest,VendorOrderManagementTest,ErrorResponseTest,JwtErrorResponseTest,ErrorContractIntegrationTest,ShopServiceTest,VoucherInternalServiceImplTest,PaymentIpnSecurityIntegrationTest
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103_s104?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline
-Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1
-Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000
test
```

Password trên chỉ dành cho local test. Không bỏ datasource override để chạy trên DB mặc định.

Evidence: [tests-final.log](../target/s1-04/tests-final.log), [test-summary.json](../target/s1-04/test-summary.json), [build.log](../target/s1-04/build.log), [MySQL identity](../target/s1-04/mysql-identity.txt), [MySQL lifecycle](../target/s1-04/mysql-server.log). XML reports lưu tại target/s1-04/reports; có thể mất khi clean.

## 5. Migration, rollback và giới hạn

- Không đổi schema hoặc dữ liệu lịch sử. Deploy Payment caller + Order contract/service/repository cùng artifact. Không để instance cũ với bulk-update không guard tiếp tục nhận IPN.
- Không rollback riêng guard trong khi S1-03 vẫn nhận cancellation: sẽ mở lại nguy cơ hồi sinh Order và hoàn kho lần hai. Nếu phải rollback, dừng nhận luồng liên quan, phối hợp bản deploy và đối soát trước khi mở lại.
- Không triển khai refund, chặn khách thanh toán nhiều attempt tại gateway, hay tự động đối soát. Hai attempt thành công vẫn được lưu là hai Payment SUCCESS; Order chỉ được confirm một lần.
- Log sau commit phục vụ vận hành, chưa có bảng reconciliation/outbox đảm bảo lưu log khi process chết. RawData/gatewayTransactionId vẫn lưu cùng Payment.
- Không mở rộng sửa parser/contract VNPay trong bước này: amount hiện còn được kiểm tra có điều kiện nếu field tồn tại, phép tính amount hiện dùng longValue, success dựa responseCode; chưa siết vnp_TransactionStatus hay đối chiếu lại protocol gateway. Đây là giới hạn của xử lý IPN hiện có, không suy ra toàn bộ payment security đã hoàn thiện từ test guard.
- Bước tiếp theo: S1-05 Ban/Unban theo D3, rồi S1-06 enforcement/UI theo D4.
