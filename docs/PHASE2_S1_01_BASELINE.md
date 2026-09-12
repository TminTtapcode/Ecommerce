# S1-01 — Baseline trước implementation Sprint 1

Ngày chạy: **2026-09-09**, khoảng 18:07–18:11 (UTC+07). Decision Set đã được người dùng phê duyệt tại [contract](PHASE2_SPRINT1_DECISIONS.md).

**Kết luận:** Đã thực hiện baseline build/test/dependency và kiểm tra DB test. Baseline chưa xanh: **41/55 test pass, 12 failures, 2 errors, 0 skipped**. Không sửa source nghiệp vụ, fixture hoặc assertions để làm xanh kết quả. S1-02 trở đi chưa triển khai.

## 1. Kết quả

| Kiểm tra | Kết quả | Bằng chứng |
|---|---|---|
| Backend package, skip test execution | PASS | [backend-build.log](../target/s1-baseline/backend-build.log) |
| Unit: 4 suite / 21 tests | 19 pass, 1 failure, 1 error | [unit-tests.log](../target/s1-baseline/unit-tests.log) |
| Integration: 9 suite / 34 tests, MySQL thật | 22 pass, 11 failures, 1 error | [integration-tests.log](../target/s1-baseline/integration-tests.log) |
| Frontend TypeScript + Vite build | PASS, warning bundle >500 kB | [frontend-build.log](../target/s1-baseline/frontend-build.log) |
| Frontend oxlint | Exit 0, 19 warnings | [frontend-lint.log](../target/s1-baseline/frontend-lint.log) |
| Backend dependency tree offline | PASS | [backend-dependencies.log](../target/s1-baseline/backend-dependencies.log) |
| Frontend installed dependencies | PASS | [frontend-dependencies.log](../target/s1-baseline/frontend-dependencies.log) |

Tóm tắt machine-readable: [test-summary.json](../target/s1-baseline/test-summary.json). Text reports của 13 suite đã sao chép vào `target/s1-baseline/reports/` để không bị lần chạy sau ghi đè trong `target/surefire-reports`.

Không chạy FullSystemDataSeederTest. Không chạy test class cũ chỉ có trong target mà không còn thuộc source. Các file trong target là artifact cục bộ, có thể mất khi clean; cần lưu trữ riêng nếu muốn giữ lâu dài. Báo cáo Markdown này giữ số liệu và danh sách lỗi.

## 2. Môi trường và dependency

- Java 21.0.11; Maven 3.9.16; Spring Boot parent 4.1.0.
- MySQL Community 8.0.46 cài sẵn trên Windows; Docker không có trong PATH.
- Node 24.19.0; dependency cài thực tế: TypeScript 6.0.3, Vite 8.2.1, React 19.2.8, oxlint 1.78.0, axios 1.19.0.
- Maven chạy offline bằng cache có sẵn; không nâng cấp/cài dependency.
- `mvnw.cmd` lỗi `Cannot index into a null array` ở đoạn đọc `(Get-Item $MAVEN_M2_PATH).Target[0]`. Chưa sửa wrapper; dùng trực tiếp Maven 3.9.16 cache.
- Maven mặc định trong môi trường tool tìm repository ở `C:\.m2\repository`; chạy thành công khi truyền `-Dmaven.repo.local=C:/Users/Dell/.m2/repository`.
- `npm` gọi npm.ps1 bị PowerShell execution policy chặn; dùng npm.cmd thành công, không thay execution policy.
- Đây là package build theo cấu hình hiện có, không chạy `clean` vì instance DB tạm nằm trong target.

Lệnh Maven thực thi dùng executable:

```text
C:\Users\Dell\.m2\wrapper\dists\apache-maven-3.9.16\0daed3be3ebd1c706f0e69e8b07c6b73f5cc4ea3dfce72a8d0ec2e849ca2ddb0\bin\mvn.cmd
```

Các goal/selector đã chạy:

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository -DskipTests package
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository -Dtest=OrderServiceImplTest,ProductInternalServiceImplTest,ShopServiceTest,VoucherInternalServiceImplTest test
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository dependency:tree
```

Frontend tại thư mục frontend: `npm.cmd run build`, `npm.cmd run lint`, `npm.cmd ls --depth=0`.

## 3. Test configuration và cách ly DB

Phát hiện mới so với audit trước: **src/test/resources/application.yml hiện tồn tại**, trỏ `localhost:3306/ecommerce`, user root và ddl-auto update. Nhãn “dummy values” không biến schema ecommerce thành DB test an toàn. Nhiều test gọi deleteAll; seeder có TRUNCATE.

Không chạy integration với datasource mặc định đó. Đã tạo **instance mysqld riêng**, không dùng service MySQL80 đang chạy:

| Thuộc tính | Giá trị đã xác minh |
|---|---|
| Bind | 127.0.0.1 |
| Port | 33317 |
| Datadir | workspace/target/s1-baseline/mysql-data |
| Schema | ecommerce_s1_baseline, tạo mới trống |
| Application account | s1baseline, chỉ grant trên schema test |
| MySQL X protocol | OFF |
| Datasource xác nhận runtime | processlist chỉ ra s1baseline / ecommerce_s1_baseline |
| Cleanup | Gửi shutdown đúng instance cổng 33317; giữ datadir/log để đối chiếu |

[mysql-identity.txt](../target/s1-baseline/mysql-identity.txt) và [mysql-server.log](../target/s1-baseline/mysql-server.log) ghi port/datadir/version/shutdown. Instance khởi tạo bằng --no-defaults và --initialize-insecure trong datadir mới; root không mật khẩu chỉ dùng bootstrap instance loopback tạm. Application dùng account test riêng.

Integration đã override ở command line:

```text
-Dtest=EcommerceApplicationTests,NPlus1AuditTest,TransactionAuditIntegrationTest,EndToEndPhase0ToPhase1Test,CodCheckoutNotificationTest,VendorOrderManagementTest,PaymentAggregationIntegrationTest,PaymentIpnSecurityIntegrationTest,ShopApprovalIntegrationTest
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s1_baseline?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline
-Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1
-Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000
```

Mật khẩu trên chỉ dành cho instance test tạm. SMTP bị hướng vào loopback cổng đóng để test không gửi mail ra ngoài. Initializer hiện có vẫn chạy trong DB test, giúp thấy sự lệch giữa fixture E2E và seed mặc định.

Lưu ý tái chạy: instance đã dừng; phải khởi động đúng datadir/port, xác minh identity và giữ overrides trước khi chạy integration. Datadir đã có dữ liệu lần baseline, không được initialize lại đè lên. Để có baseline tương đương từ DB trống, dùng instance/datadir/schema mới. Chưa thay đổi cấu hình test mặc định nguy hiểm trong source; cần chuẩn hóa runner/profile cách ly trước các vòng integration implementation tiếp theo.

Tham số `-Dsurefire.reportsDirectory=...` đã thử nhưng run này vẫn ghi default target/surefire-reports; báo cáo được sao chép sau run, không giả định custom path có hiệu lực. Lần gọi đầu có URL chứa & bị cmd tách tham số, Maven chưa chạy test; đã chạy lại thành công bằng URL một query parameter như trên.

## 4. Unit failures có sẵn

| Test | Hiện tượng | Đánh giá / bước xử lý sau baseline |
|---|---|---|
| OrderServiceImplTest.updateVendorOrderStatus_ShouldPublishOrderCancelledEventWithItems_WhenCancelled | IllegalArgumentException: PENDING → CANCELLED | Fixture đặt PENDING, trái matrix hiện tại và D1 đã chốt. Sửa fixture/test theo CONFIRMED trong slice inventory, không mở transition để chiều test. |
| ShopServiceTest.approveShop_ShouldSuccess_WhenPending | verify publishEvent(any()) thất bại dù mock ghi nhận ShopApprovedEvent | Matcher chọn overload ApplicationEvent khác lời gọi Object chứa record event. Dùng matcher/captor kiểu ShopApprovedEvent khi sửa test. Chưa sửa ở S1-01. |

ProductInternalServiceImplTest 4/4, VoucherInternalServiceImplTest 4/4 pass. Voucher rounding test pass hiện vẫn chấp nhận tổng phân bổ 9 cho discount 10; pass không chứng minh bảo toàn tổng.

## 5. Integration failures có sẵn

EndToEndPhase0ToPhase1Test: **1 pass, 10 failures, 1 error**:

| Test | Assertion/error |
|---|---|
| test1_Authentication | Vendor login expected 200, actual 401 |
| test2_CatalogAndProductDetail | Expected 10 products, actual 5 |
| test4_CheckoutPreview | Expected 200, actual 400; log có voucher không tồn tại |
| test5_CheckoutOrder | Expected 201, actual 400; log có voucher không tồn tại |
| test6_CustomerOrderHistoryAndDetail | Expected 200, actual 500; request id = chuỗi null |
| test7_VendorStateMachineTransitions | Expected 200, actual 401 |
| test8_CustomerReview | NullPointerException |
| test9_VendorReplyReview | Expected 200, actual 401 |
| test10_VerifyPublicReviewAndSummary | Expected true, actual false |
| test11_VendorCancelOrder | Expected 201, actual 400 |
| test12_PaymentGroupStatus | Java null khác chuỗi "null" |

Suite E2E có thứ tự, giữ static token/order/group IDs và giả định account/catalog/voucher mẫu. Khi login/checkout fail, các bước sau có lỗi dây chuyền; không kết luận 11 bug nghiệp vụ độc lập. Cần fixture tự dựng đúng yêu cầu test trước khi dùng suite này làm regression đáng tin cậy; không chạy full-system seeder vào DB ứng dụng để đáp ứng fixture.

NPlus1AuditTest.testOrderListingBatchFetching: **expected 2 items, actual 1**. Fail ở assertion dữ liệu, trước phép kiểm tra ngưỡng query; chưa có bằng chứng đây là regression N+1. Cần cách ly dữ liệu user/order fixture khỏi initializer và các suite khác rồi xác minh lại.

Các suite integration còn lại pass:

| Suite | Pass/total |
|---|---|
| EcommerceApplicationTests | 1/1 |
| NPlus1AuditTest (toàn suite) | 4/5 |
| CodCheckoutNotificationTest | 1/1 |
| VendorOrderManagementTest | 4/4 |
| PaymentAggregationIntegrationTest | 3/3 |
| PaymentIpnSecurityIntegrationTest | 4/4 |
| ShopApprovalIntegrationTest | 1/1 |
| TransactionAuditIntegrationTest | 3/3 |

## 6. Frontend warnings

19 warnings: 3 only-export-components (Auth/Cart/Chat contexts), 8 exhaustive-deps, 8 unused catch parameters. Build bundle JS khoảng 537.91 kB minified / 147.84 kB gzip. Không sửa warning ngoài phạm vi để làm đẹp baseline.

## 7. Phạm vi hoàn tất và giới hạn

- Đã ghi nhận Decision Set vào tài liệu và chạy baseline trước thay đổi nghiệp vụ.
- Chưa sửa application/test source, pom, dependencies hoặc datasource mặc định.
- Chưa triển khai Error success/data-null, pessimistic Order lock, synchronous restore, prior shop status hoặc ban enforcement.
- Các test hiện tại pass không chứng minh concurrency requirements mới. S1-03/S1-04 vẫn phải thêm integration dùng transaction/kết nối độc lập.
- Baseline sử dụng DB riêng với seed mặc định và suite chung; một số test chưa độc lập dữ liệu. Không công bố line/branch coverage.
- Không chạy browser smoke test ở S1-01; đây là phần kiểm chứng UI sau implementation, ngoài HTTP E2E đã chạy.
- Workspace chưa được Git nhận diện; chưa tạo commit. MODULE_DEVELOPMENT_WORKFLOW.md vẫn thiếu tại đường dẫn rules chỉ định.

**Bước kế tiếp:** S1-02 Error Contract theo [Decision Set](PHASE2_SPRINT1_DECISIONS.md), dùng danh sách lỗi hiện hữu trên để phân biệt regression mới. Chuẩn hóa cách chạy test cách ly trước khi tiếp tục integration; review baseline này trước khi chuyển bước theo workflow đã thống nhất.
