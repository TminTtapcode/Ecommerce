# S1-07 / S1-08 — Core tests, verification và final audit

Ngày kiểm chứng: **2026-09-09**. **Đã hoàn thành S1-07 và S1-08**, theo [Sprint 1 Plan](PHASE2_SPRINT1_PLAN.md) và [Decision Set](PHASE2_SPRINT1_DECISIONS.md). Kết quả cuối: **155/155 backend, 6/6 frontend pass; build BE/FE pass; lint exit 0 với 19 warnings; browser smoke pass**. Đây là kiểm chứng local trên DB cách ly, chưa deploy ứng dụng.

## 1. Thay đổi trong bước này

- Bổ sung 7 unit cases Voucher: code không tồn tại, hết hạn/chưa bắt đầu, hết lượt, minimum, sai scope và discount chỉ áp dụng phần shop hợp lệ. Giữ các test percentage/fixed/max/rounding đã có; không tự đổi chính sách làm tròn.
- Thêm `VoucherUsageIntegrationTest`: hai transaction cạnh tranh lượt cuối chỉ một lần thành công; caller rollback trả lại lượt đã giữ. Dùng service/repository và MySQL thật, hai worker độc lập, latch giữ transaction và kiểm tra worker còn lại phải chờ.
- Thêm 7 cases vào `PaymentGroupGuardIntegrationTest`: amount thiếu, rỗng, sai giá trị, không phải số, âm, vượt long; amount 10.50 được xác nhận đúng với gateway amount 1050.
- Khép lỗ hổng đã ghi ở S1-04: `PaymentGatewayService` yêu cầu `vnp_Amount` hợp lệ với Payment còn PENDING, so sánh với `payment.amount.movePointRight(2).longValueExact()`. Không bỏ phần thập phân trước khi nhân 100. Input sai không ghi Payment/Order/stock. Callback đã terminal vẫn giữ nhánh idempotency cũ; không đổi envelope `RspCode`/`Message`.
- Sửa fixture E2E: tự tạo user/role/shop/category/product/variant/voucher riêng thay ID cứng phụ thuộc seed. Giữ 12 assertions nghiệp vụ; buyer có ROLE_USER đúng với authorization của review API.
- Sửa fixture NPlus1: tạo Shop thật thay shop ID không tồn tại; lọc listing theo prefix riêng và dùng buyer riêng để tránh lẫn đơn seed. Giữ 5 tests và giới hạn query. Đây là sửa dữ liệu test, không nới assertion hoặc bỏ kiểm tra ban để làm xanh.
- Đổi datasource mặc định trong `src/test/resources/application.yml` sang schema test loopback riêng, hỗ trợ `TEST_DB_URL/USERNAME/PASSWORD`; không còn mặc định dùng schema ecommerce của ứng dụng. Shop integration guard chấp nhận prefix schema test để chạy chung regression S1-03/04/05/08.
- Thêm harness `frontend/tests/sprint1.browser-smoke.mjs`, dùng Node HTTP/CDP và Edge cài sẵn; không thêm dependency. Không đổi UI nghiệp vụ trong S1-07/08.

## 2. Kết quả backend

Chạy Maven `test` **không dùng selector `-Dtest`**. Surefire giữ exclusion có sẵn cho `FullSystemDataSeederTest` (tiện ích tạo dữ liệu, không phải suite kiểm chứng). Không thêm exclusion, không skip behavioral test. XML cũ của scratch DbWiper không thuộc lần chạy này đã được loại khỏi bản tổng hợp; tổng đối chiếu với log Maven cuối.

| Suite | Pass / tổng |
|---|---:|
| ErrorResponse + JwtErrorResponse + ErrorContractIntegration | 25/25 |
| InventoryCancellationIntegration | 20/20 |
| OrderServiceImpl + VendorOrderManagement + CodCheckoutNotification | 9/9 |
| ProductInternalServiceImpl | 7/7 |
| PaymentGroupGuardIntegration | 20/20 |
| PaymentIpnSecurityIntegration + PaymentAggregationIntegration | 7/7 |
| ShopService + ShopApprovalIntegration + ShopBanEnforcementIntegration | 33/33 |
| VoucherInternalServiceImpl + VoucherUsageIntegration | 13/13 |
| EndToEndPhase0ToPhase1 | 12/12 |
| NPlus1Audit | 5/5 |
| TransactionAuditIntegration | 3/3 |
| EcommerceApplicationTests | 1/1 |
| **Tổng** | **155/155** |

**0 failures, 0 errors, 0 skipped; Maven exit 0.** So với baseline S1-01 (41/55, 12 failures + 2 errors), các suite baseline đã xanh ở lần kiểm chứng cuối. Lần chạy đầu bước này có 148 tests, 10 failures và 6 errors do fixture E2E/NPlus1; log đầu giữ lại để phân biệt với kết quả sau sửa. Không dùng kết quả từ nhiều lần chạy ghép thành một suite xanh.

Coverage transaction/lock vẫn bao gồm: rollback item thứ hai/DB overflow/caller rollback; duplicate cancel và cancel đối đầu SHIPPED; IPN trễ/lặp, hai payment attempts, hai thứ tự IPN/cancel; ban đối đầu checkout/approve/unban, trực tiếp API authorization; Voucher tranh lượt cuối. Các mô tả case chi tiết nằm trong báo cáo S1-03/04/05/06 và source test. Test lock có worker/transaction riêng, không chỉ Mockito; chưa phải stress test mọi lịch xen kẽ.

## 3. Frontend và browser smoke

`npm.cmd run build` pass (TypeScript + Vite); `npm.cmd run lint` exit 0, **19 warnings** giữ nguyên; `node --experimental-strip-types --test tests/apiError.test.mjs` **6/6 pass**. Build còn cảnh báo chunk lớn hơn 500 kB; chưa tối ưu bundle ngoài phạm vi Sprint 1.

Edge headless **152.0.4191.66**, viewport 1440×1000, dùng frontend **dist đã build**, backend JAR mới và MySQL thật. Harness tạo tài khoản/fixture bằng HTTP, chỉ bootstrap ROLE_ADMIN trong schema smoke cố định; không ghi JWT vào evidence. Thao tác nút dùng CDP mouse event, không gọi React handler giả.

1. Đăng ký, approve shop, tạo sản phẩm stock 10, Cart → checkout COD quantity 2 → stock 8; thêm lại item vào Cart để kiểm tra ban.
2. Admin bấm **Khóa Shop**, prompt reason; kiểm tra API trả BANNED và UI có hành động mở khóa.
3. Vendor xem detail sản phẩm của mình: form disabled, banner giải thích vẫn xử lý đơn cũ; route tạo mới bị chặn.
4. Vendor bấm **Hủy đơn**, nhập lý do, xác nhận: Order CANCELLED và stock trở lại **10** dù shop BANNED.
5. Buyer mở Cart: nút tăng disabled, nút giảm enabled. Click chuột đi qua overlay unavailable và quantity được lưu từ 2 xuống 1.
6. Admin **Mở Khóa**: status ACTIVE được phục hồi; public product detail truy cập lại thành công.

Smoke **PASS, process exit 0**. Đã xem screenshot vendor detail để kiểm tra bố cục/banner. Đây là desktop smoke theo luồng Sprint 1, chưa phải kiểm thử đa trình duyệt/mobile hoặc mọi màn hình của ứng dụng.

## 4. Môi trường và tái chạy

Java 21, Maven 3.9.16 offline, MySQL **8.0.46** riêng tại **127.0.0.1:33317**, datadir `target/s1-baseline/mysql-data`. Regression schema **ecommerce_s103_s104_s105_s108**; smoke schema **ecommerce_s108_smoke**. User `s1baseline`, password `s1-local-only`, chỉ dành DB test. Không dùng schema ecommerce, không đổi service MySQL80 của máy. SMTP localhost:1, timeout 1000ms; lỗi kết nối mail trong log là dự kiến, không gửi email ngoài.

Sau kiểm chứng đã dừng backend smoke, đóng Edge/profile test và shutdown MySQL test; service MySQL80 của máy vẫn Running. Dữ liệu/evidence test được giữ lại.

Khởi động/xác minh đúng instance test và grant schema trước khi chạy. Maven có sẵn tại `.m2/wrapper/dists`; truy cập cache jakarta.mail-api đã cần chạy lại qua approval review. Không đổi pom hoặc nâng dependency.

```text
mvn -o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
  -Dspring.mail.host=127.0.0.1 -Dspring.mail.port=1
  -Dspring.mail.properties.mail.smtp.connectiontimeout=1000
  -Dspring.mail.properties.mail.smtp.timeout=1000 test
mvn -o -Dmaven.repo.local=C:/Users/Dell/.m2/repository -DskipTests package
```

Các dòng trên dùng test datasource mặc định mới; kiểm tra biến TEST_DB_* không override ngoài ý muốn. Có thể truyền datasource rõ ràng cùng giá trị test đã nêu. Frontend commands chạy tại frontend; browser harness chạy từ root bằng `node frontend/tests/sprint1.browser-smoke.mjs` sau khi có:

- JAR chạy cổng 8080 với `--spring.config.location=file:src/test/resources/application.yml`, override datasource sang ecommerce_s108_smoke và SMTP local như trên.
- Edge headless dùng profile riêng dưới target, remote debugging cổng 9222. Harness tự phục vụ dist tại 5173; dùng schema smoke mới để shop fixture còn nằm trong trang đầu Admin.
- MySQL CLI tại đường dẫn Windows trong harness; bootstrap admin bị giới hạn schema cố định, không phải script quản trị production.

Evidence: [full tests](../target/s1-07-08/full-tests-final.log), [lần đầu](../target/s1-07-08/full-tests-initial.log), [JSON tổng hợp](../target/s1-07-08/test-summary.json), [backend build](../target/s1-07-08/build.log), [frontend build](../target/s1-07-08/frontend-build.log), [lint](../target/s1-07-08/frontend-lint.log), [FE tests](../target/s1-07-08/frontend-tests.log), [browser JSON](../target/s1-07-08/browser-smoke.json), [browser log](../target/s1-07-08/browser-smoke.log), [MySQL identity](../target/s1-07-08/mysql-identity.txt), [artifact check](../target/s1-07-08/artifact-check.txt). XML tại target/s1-07-08/reports; screenshots tại target/s1-07-08/screenshots. Evidence trong target có thể mất khi clean; báo cáo này giữ kết quả và cách tái chạy.

## 5. Migration, rollback và final audit

- **S1-07/08 không thêm schema migration.** Sprint 1 cần [S1_05_shop_prior_status.sql](migrations/S1_05_shop_prior_status.sql), đã kiểm chứng bằng migration fixture S1-05/06; runtime regression/smoke có cột nullable prior_status. Không tự backfill BANNED cũ; unban thiếu prior status vẫn yêu cầu đối soát thủ công. Chưa áp dụng migration vào DB ứng dụng trong lượt này.
- Artifact đã kiểm tra không còn `ProductOrderCancelledListener.class`. Deploy đồng bộ Order/Product/Payment/Shop/FE tương ứng; không trộn instance cũ dùng hoàn kho sau commit hoặc bulk-update group không guard.
- Khi rollback, dừng các mutation liên quan, triển khai bộ artifact nhất quán và đối soát Order/stock/Payment/Shop trước khi mở lại. Không rollback riêng guard trong lúc synchronous cancellation vẫn hoạt động; không chỉ bật lại listener; nên giữ prior_status khi rollback ứng dụng để tránh mất thông tin phục hồi. Không tự sửa stock/tiền lịch sử.
- **F4:** synchronous restore, row-count validation và khóa Order được chứng minh trong các case đã chạy. **F5:** state/role/prior status và backend enforcement được kiểm thử trực tiếp HTTP, bổ sung browser smoke. **F8:** core + regression full configured suite có evidence mới. **F9:** core envelope và FE consumer theo errorCode được kiểm chứng; không tuyên bố mọi exception legacy ở mọi module đã có mã domain riêng.
- Thay đổi production trong bước này chỉ siết amount trong Payment; vẫn gọi Order qua internal API, không thêm truy cập entity/repository chéo module. Fixture test có quyền tạo entity nhiều module để kiểm chứng toàn luồng.
- Giới hạn còn lại: Voucher rounding hiện vẫn có case discount 10 chia ba thành tổng 9; cần chốt policy trước khi sửa. Chưa mở rộng diễn giải `vnp_TransactionStatus`/toàn bộ protocol gateway, refund hoặc ngăn thanh toán nhiều attempt tại gateway. Log after-commit chưa phải durable outbox. Chưa chứng minh hết deadlock/race stock giữa nhiều checkout/product mutation, chưa tối ưu catalog với số banned shop rất lớn. 19 lint warnings và bundle warning vẫn còn.

Sprint 1 hoàn tất theo contract đã duyệt và phạm vi kiểm chứng trên; các giới hạn không được diễn giải thành đã hoàn thiện mọi feature Phase 2.
