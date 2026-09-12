# PHASE 2 — Implementation Plan

Ngày: **2026-09-09**. Trạng thái: **Sprint 1 đã chốt D1/D3/D4 và Error; S1-01 đã ghi nhận baseline có lỗi trước implementation**.

Xem [S1-01 Baseline](PHASE2_S1_01_BASELINE.md): build pass, unit 19/21 pass, integration MySQL 22/34 pass, lint 19 warnings. **Sprint 1 đã hoàn thành S1-01 đến S1-08**: [Error](PHASE2_S1_02_ERROR_CONTRACT.md), [Inventory](PHASE2_S1_03_INVENTORY_CANCELLATION.md), [Payment Guard](PHASE2_S1_04_PAYMENT_GROUP_GUARD.md), [Ban/Unban và enforcement](PHASE2_S1_05_06_SHOP_BAN_ENFORCEMENT.md), [Verification/final audit](PHASE2_S1_07_08_VERIFICATION.md). Full configured suite 155/155 backend (gồm E2E/NPlus1) + 6/6 frontend pass; build BE/FE và browser smoke pass, lint giữ 19 warnings. Chưa deploy ứng dụng; giới hạn và rollback theo báo cáo cuối.

Xem [Implementation contract được phê duyệt](PHASE2_SPRINT1_DECISIONS.md). Contract này thay thế các lựa chọn D1/D3/D4 và Error còn để mở bên dưới. **F3 đã hoàn thành theo D2**, [báo cáo P1-01](PHASE2_P1_01_ADMIN_ORDER.md); **F6 đã hoàn thành theo D6**, [báo cáo P1-02](PHASE2_P1_02_PROFILE.md); **F1 đã hoàn thành theo C1–C6**, [báo cáo P1-03](PHASE2_P1_03_NOTIFICATION_PUSH.md); **F7 đã hoàn thành theo D7**, [báo cáo P1-04](PHASE2_P1_04_ADVANCED_SEARCH.md); **F2 đã hoàn thành theo D5**, [báo cáo P1-05](PHASE2_P1_05_ADMIN_STATISTICS.md). D8–D10 vẫn chưa chốt. Mô tả audit trước implementation bên dưới là lịch sử, trạng thái mới nhất theo các báo cáo.

Nguồn: [Feature Audit](PHASE2_FEATURE_AUDIT.md), [Dependency Graph](PHASE2_DEPENDENCY_GRAPH.md), source được dẫn trong audit, [Architecture](ARCHITECTURE.md). Kế hoạch này thay thế các giả định trong bản Phase 2 trước; không tạo thêm business rule chưa được chốt.

## 1. Nguyên tắc triển khai

- Kế thừa code/test hiện có; không tạo lại ErrorCode, ban/unban, restoreStock hoặc test framework.
- Mỗi feature đi theo slice: xác nhận phạm vi → thiết kế → BE/FE → test → báo cáo kết quả. F8/F9 đi cùng feature.
- Cross-module chỉ dùng internal contract/DTO/event; repository chỉ truy vấn dữ liệu module sở hữu.
- Contract/API/model mới dưới đây là **đề xuất**, chưa phải endpoint hay schema đang tồn tại.
- Không chạy migration, seed, gateway request hoặc thay đổi DB trong lượt lập kế hoạch này.
- Ưu tiên thay đổi cộng thêm và giữ tương thích FE; không khẳng định rollback bằng 0 rủi ro.

## 2. Quyết định cần chốt trước phần implementation phụ thuộc

| ID | Câu hỏi cần quyết định | Hiện trạng / lựa chọn để xem xét |
|---|---|---|
| D1 | Hoàn kho đồng bộ hay eventual consistency có phục hồi? | Hiện AFTER_COMMIT không durable retry. Đề xuất kỹ thuật A: cùng transaction qua Product contract; B: outbox + dedup + retry. Không triển khai đồng thời cả hai. |
| D2 | Admin được chuyển trạng thái nào, trong điều kiện payment nào? | Chưa có policy. Không mặc định cho PENDING → CONFIRMED/CANCELLED hoặc bỏ qua điều kiện thanh toán. |
| D3 | Có ban PENDING không; unban khôi phục trạng thái nào? | BE hiện cho ban PENDING rồi unban ACTIVE; FE chỉ nút ban ACTIVE. Cần thống nhất duyệt/cấp role, không tự chọn cấm ban PENDING. |
| D4 | Shop bị ban bị hạn chế gì; ai xử lý đơn cũ? | Mục tiêu cần kiểm soát bán hàng nhưng phải chốt catalog, checkout, product mutations và vận hành đơn cũ. Reason bắt buộc không, lưu log hay DB cũng cần chốt. |
| D5 | Metric doanh thu nghĩa là giá trị đơn, cash collected hay settlement? | Có dữ liệu order value; cash COD/settlement/refund/cost chưa đủ. Chốt cohort date, timezone, tập trạng thái. |
| D6 | Profile cho sửa trường nào, quy tắc phone/avatar/email? | Có fullName/phone; chưa avatar. Đề xuất bắt đầu name/phone, không tự thêm avatar hoặc đổi email. |
| D7 | Search theo giá gốc hay variant; category cha có bao gồm con; sort nào? | Có Brand model; Product chưa createdAt. Có thể dùng id/price cho baseline nếu được chọn. |
| D8 | Ai xác nhận giao, có assignment/proof/thời hạn không? | Vendor hiện SHIPPED → DELIVERED. Không tự thêm SHIPPER hoặc auto-confirm 7 ngày. |
| D9 | Gateway hỗ trợ refund nào với merchant hiện tại; COD hoàn thế nào? | Source chỉ pay/IPN; chưa xác minh external capability. Không triển khai refund khi thiếu contract. |
| D10 | Điều kiện return, partial/full, người duyệt, disposition hàng và tiền? | Chưa policy; không tự tạo thời hạn, mức phí, enum hay quy tắc nhập kho. |

Các quyết định trên không chặn audit, test baseline hoặc thiết kế phần độc lập. Trước implementation nhánh phụ thuộc phải có câu trả lời, không xem thời gian trôi qua là phê duyệt.

## 3. P0 — Nền tảng và toàn vẹn

### Feature 8 — Unit tests và kiểm chứng

- **Backend changes:** Tái sử dụng 4 lớp unit hiện có (21 case). Bổ sung PaymentGatewayServiceTest, VnPayStrategy/HashUtil test, GlobalExceptionHandlerTest; mở rộng OrderServiceImplTest, ProductInternalServiceImplTest, ShopServiceTest, VoucherInternalServiceImplTest.
- **Database changes:** Không đổi schema nghiệp vụ. Cấu hình/fixture integration tách DB và kiểm soát seed; repository test dùng DB có hành vi lock/atomic tương ứng MySQL.
- **Frontend changes:** Build/lint baseline; smoke test UI thuộc slice. Chỉ thêm test tooling khi cần xác minh logic FE, không dựng framework rộng trước feature.
- **API:** Không endpoint mới.
- **Security:** Test không quyền, sai ownership, token thiếu/sai; test trực tiếp HTTP/method security bổ sung cho mock service.
- **Tests:** Checkout N shop → N order; voucher scope/expiry/min/max/usage/rounding; stock rollback và concurrent cancel; payment signature, duplicate attempt callback, amount sai/thiếu, IPN trễ; handler/binding/security errors. Unit mock không được dùng làm bằng chứng transaction/lock.
- **Migration impact:** Không có data migration.
- **Rollback risk:** Thấp với test độc lập; test fixture/seed có thể ảnh hưởng DB nếu cấu hình sai. Không chạy full suite trước khi xác nhận target DB.

Baseline source có 56 @Test (21 unit, 34 Spring-context, 1 seeder excluded), chưa phải execution result. Chưa có % coverage đo được. Báo cáo thực thi sau này phải ghi command, số pass/fail/skipped và môi trường.

### Feature 9 — Standardized error codes

- **Backend changes:** Hoàn thiện enum/handler đã có, thống nhất NOT_FOUND và mã validation; chuyển nhánh domain core từ raw exceptions sang AppException. Xử lý JSON sai/type mismatch và security entry point/denied handler bằng REST envelope; không lộ lỗi hệ thống.
- **Database changes:** Không có; errorCode không phải cột DB.
- **Frontend changes:** Thêm errorCode vào common.types theo khả năng nullable; mapping `api/types/errorCodes.ts` và helper fallback message/unknown code. Cập nhật RequireShop cùng backend để bỏ phụ thuộc chuỗi tiếng Việt; giữ HTTP 401 compatibility, không logout vì 403.
- **API:** Giữ status/message/data, ổn định mã symbolic. Mọi REST error path cần kiểm thử envelope; callback VNPay tiếp tục giao thức RspCode/Message riêng.
- **Security:** 401/403 từ filter không mặc định qua controller advice; thống nhất ở đúng tầng.
- **Tests:** Default/custom message, domain status/code, validation/body sai, 401/403/404/500; FE chưa có shop và chưa đăng nhập.
- **Migration impact:** API contract mở rộng; HTTP status đổi phải cập nhật consumer cùng slice. Triển khai FE chấp nhận cả response cũ/mới trong giai đoạn chuyển.
- **Rollback risk:** Trung bình nếu FE chỉ hiểu mã mới hoặc handler đổi sai status. Giữ fallback; rollback BE/FE có kế hoạch tương thích.

Hoàn thiện core P0 trước, tiếp tục chuyển các service còn lại theo module và kiểm tra toàn bộ REST error paths trước khi đánh dấu feature hoàn tất. Không blanket-catch mọi lỗi lập trình thành INVALID_INPUT.

### Feature 4 — Inventory refund on cancellation

- **Backend changes:** Khóa/version hoặc conditional transition bảo vệ đọc–kiểm tra–ghi Order; giữ matrix Vendor hiện tại **CONFIRMED → CANCELLED**. Restore cả variant và product, validate input, kiểm tra row count; thống nhất xử lý group update/IPN không ghi đè trạng thái cuối. Chốt D1.
- **Database changes:** Phương án A cùng transaction có thể không cần schema mới nếu dùng pessimistic lock; nếu optimistic lock cần version migration. Phương án B cần outbox/processed-operation unique key và trạng thái retry.
- **Frontend changes:** Giữ action hủy hiện có, pending/disable gửi lặp, báo lỗi đúng. Chỉ nói “đã hoàn kho” khi semantics backend bảo đảm; phương án async phải phản ánh trạng thái chưa hoàn tất nếu UI cần.
- **API:** Giữ PATCH `/api/v1/vendor/orders/{id}/status`. Internal `restoreStockBatch` là đề xuất; không mở Admin endpoint trong P0.
- **Security:** Vendor ownership và role hiện có; không tăng quyền/mở hủy PENDING. Không lấy quantities từ body client mà dùng OrderItem snapshot.
- **Tests:** Checkout → hủy phục hồi cả hai stock; nhiều variant cùng product; request hủy lặp/đồng thời, hủy đối đầu SHIPPED/IPN; item thứ hai restore lỗi; variant mất; duplicate event nếu dùng phương án B. Bằng chứng atomicity phải từ integration.
- **Migration impact:** Không tự hoàn lại đơn CANCELLED lịch sử vì chưa biết đã restore hay chưa. Nếu B, quyết định dữ liệu nào được enqueue khi cutover; không replay toàn lịch sử.
- **Rollback risk:** Cao với dữ liệu kho. Không tắt listener và tiếp tục nhận hủy như biện pháp rollback. A phải gỡ listener cộng kho cũ cùng release, không để hai nơi cùng restore. B phải giữ ledger/dedup khi rollback; đối soát pending trước khi ngừng worker.

**Đề xuất A để cân nhắc:** gọi Product internal contract trong transaction hủy; bất kỳ restore lỗi rollback Order và stock; notification vẫn sau commit. Giảm khoảng trống dữ liệu mà không thêm hạ tầng. Đây là đề xuất kiến trúc, không phải quyết định đã duyệt.

Payment integrity trong phạm vi liên quan: bảo vệ Order group transitions; test nhiều attempts SUCCESS và callback thiếu amount. Mọi sửa interpretation gateway fields phải dựa contract xác minh, không suy từ tên field.

### Feature 5 — Admin ban/unban shop

- **Backend changes:** Hoàn thiện endpoint/service đã có, DTO thay Map, policy D3/D4, khóa/version khi approve/ban/unban. ShopInternalService cung cấp eligibility theo batch; enforce các đường mua/bán đã chốt trên backend. Không trực tiếp đọc ShopRepository từ Product/Order.
- **Database changes:** Chưa mặc định thêm cột. Nếu cần reason bền vững/audit hoặc trạng thái trước ban, bổ sung nullable field hoặc audit table tương ứng quyết định; ghi actor từ principal.
- **Frontend changes:** Hoàn thiện prompt/confirm hiện có, pending/error/success/refresh; type reason theo DTO. Cập nhật RequireShop để phản ánh policy đơn cũ và không phụ thuộc message text.
- **API:** Giữ PUT `/api/v1/admin/shops/{shopId}/ban` và `/unban`; request reason bắt buộc/tùy chọn và validation chốt qua D4.
- **Security:** ADMIN; ownership ở Vendor APIs; check backend dù FE state cũ. Checkout và ban đồng thời cần kết quả được serialize hoặc reject phù hợp transaction design.
- **Tests:** Các transition được chốt, thiếu role/not found/reason sai, concurrent approve/ban/unban; public list/detail, giỏ cũ checkout, API product trực tiếp, unban không làm mất status HIDDEN trước đó; đường đơn cũ theo policy.
- **Migration impact:** Enum BANNED đã có; migration chỉ khi cần lưu thêm dữ liệu. Ban PENDING cũ không có prior status nên không tự đoán để backfill.
- **Rollback risk:** Trung bình–cao: rollback enforcement có thể mở bán cho shop vẫn BANNED. Giữ data/status, không mass-update product ACTIVE khi unban.

Lọc catalog phải diễn ra trước phân trang. Lựa chọn implementation eligibility: contract batch ID shop hoặc projection cục bộ sau đánh giá quy mô/độ trễ; không lọc Page rồi giữ total cũ, không JOIN bảng shop xuyên module.

## 4. P1 — Vận hành và trải nghiệm

Kế hoạch thực thi chi tiết: **[P1 — từng feature một lượt](PHASE2_P1_FEATURE_PLAN.md)**. Thứ tự: P1-01/F3 → P1-02/F6 → P1-03/F1 → P1-04/F7 → P1-05/F2; mỗi lượt gồm contract, BE/FE, test và báo cáo riêng. F3/F6 đã hoàn thành theo D2-A/D6; F1/F7/F2 chưa triển khai. D5/D7 vẫn cần chốt trước nhánh phụ thuộc.


### Feature 3 — Admin order status mutation

**Đã hoàn thành P1-01/F3:** [D2 lựa chọn A đã duyệt](PHASE2_P1_01_D2_DECISIONS.md), [báo cáo BE/FE/test/verification](PHASE2_P1_01_ADMIN_ORDER.md). Full regression 188/188 backend + 6/6 frontend, build và browser smoke pass; lint 19 warnings. Các đề xuất dưới đây giữ bối cảnh lập kế hoạch, contract và API thực tế theo báo cáo F3. P1-02…05 chưa triển khai.

- **Backend changes:** Sau D2, tạo command/DTO Admin dùng chung transition/inventory guard với Order; tách role authorization khỏi invariant trạng thái/payment; lưu lý do/actor theo audit policy.
- **Database changes:** Audit history chỉ thêm khi chọn lưu bền vững: orderId, actorId, old/new status, reason, timestamp. Không tự thêm OrderStatus mới.
- **Frontend changes:** Action/modal tại AdminOrderDetailPage; chỉ hiển thị transition được backend cho phép, xử lý conflict và refetch.
- **API:** Đề xuất PATCH `/api/v1/admin/orders/{orderId}/status`, status và reason theo policy. Không cho client gửi “force bypass”.
- **Security:** ADMIN ở HTTP/service command, payment/state validation vẫn bắt buộc.
- **Tests:** Từng transition cho phép/bị cấm, non-admin, reason validation, race Vendor/IPN, cancellation hoàn kho đúng một lần.
- **Migration impact:** Additive audit nếu chọn; FE chỉ bật action khi BE đã sẵn sàng.
- **Rollback risk:** Status đã đổi không thể rollback bằng xóa log hoặc đưa lùi hàng loạt; tắt action mới, giữ audit và state invariants.
- **Điều kiện bắt đầu:** D2, F4 cho cancel, F9/F8. Không mặc định PENDING → CONFIRMED/CANCELLED được phép.

### Feature 2 — Admin dashboard statistics

**Đã hoàn thành P1-05/F2 theo D5:** [Báo cáo hoàn thành](PHASE2_P1_05_ADMIN_STATISTICS.md). 6/6 test cases trong `AdminStatisticsIntegrationTest` pass, 278/278 full test suite pass, frontend build & oxlint pass, live API xác thực thành công. Triển khai đầy đủ GET `/api/v1/admin/statistics/dashboard` với 6 KPI cards, status breakdown, daily order chart (bar), date range filter (`startDate`, `endDate`) và recent orders table. Không vi phạm ranh giới module (Modular Monolith), sử dụng internal contracts.

- **Backend changes:** Module `statistics` riêng biệt (`AdminStatisticsController`, `AdminDashboardStatsResponse`). Tập hợp data qua `IdentityInternalService.countUsers()`, `ShopInternalService.countShops()`, `ProductInternalService.countProducts()` và `OrderInternalService.getDashboardOrderStats(startDate, endDate)`.
- **Database changes:** Không thay đổi schema; bổ sung tối ưu JPQL queries trong `OrderRepository` (`countByStatusAndCreatedAtBetween`, `sumDeliveredRevenueByCreatedAtBetween`, `getDailyOrderChartData`, `findTop10ByOrderByCreatedAtDesc`).
- **Frontend changes:** `AdminDashboardPage.tsx` thay thế placeholder tại route `/admin`, đầy đủ KPI cards, bộ lọc thời gian tiện lợi, progress bar phân bố trạng thái, biểu đồ cột sản lượng đơn theo ngày và bảng đơn hàng mới nhất.
- **API:** GET `/api/v1/admin/statistics/dashboard`, hỗ trợ `startDate` và `endDate` (ISO Date: `yyyy-MM-dd`).
- **Security:** `@PreAuthorize("hasRole('ADMIN')")`; kiểm thử xác nhận 401/403 cho unauthenticated, BUYER và VENDOR.
- **Tests:** 6 test cases mới trong `AdminStatisticsIntegrationTest` xác thực an ninh, độ chính xác doanh thu `DELIVERED`, tính toán AOV, lọc thời gian và dữ liệu chart/recent orders. Full suite 278/278 tests pass.
- **Migration impact:** Hoàn toàn là read-only aggregate queries, không tác động dữ liệu cũ hay yêu cầu migration.
- **Rollback risk:** Thấp.


### Feature 6 — Profile

**Đã hoàn thành P1-02/F6** theo [D6](PHASE2_P1_02_D6_DECISIONS.md): [báo cáo kiểm chứng](PHASE2_P1_02_PROFILE.md) có 221/221 backend, 9/9 frontend tests, build/browser smoke pass; lint 18 warnings. GET/PUT users/me, whitelist name/phone và session-safe hydration đã triển khai, không migration.

### Feature 1 — WebSocket notification push

**Đã hoàn thành P1-03/F1 theo C1–C6:** [Báo cáo hoàn thành](PHASE2_P1_03_NOTIFICATION_PUSH.md). 34/34 integration test cases pass (`NotificationPushIntegrationTest`), 1/1 listener test pass, 15/15 frontend tests pass, browser smoke pass. Đã triển khai STOMP `/user/queue/notifications`, push sau commit transaction Notification, HTTP reconcile/fallback 60s khi mất kết nối, session token expiry và dedup cache.

- **Backend changes:** Tạo điểm lưu notification dùng chung từ các listener, push sau commit của chính transaction Notification. Tái sử dụng STOMP, chuẩn hóa Principal theo userId đang dùng và xử lý fallback tương thích chat; chặn CONNECT chưa auth, SUBSCRIBE/SEND trái phép. Đưa config dùng chung về common/infrastructure nếu cần, không refactor chat rộng.
- **Database changes:** Không bắt buộc bảng mới. Nếu chọn unique dedup key, audit dữ liệu trùng trước thêm constraint; không tự xóa notifications.
- **Frontend changes:** Mở rộng websocket service dùng chung có subscription riêng notification; reconnect/resubscribe, clear state khi đổi user; dedup ID và reconcile count/list qua HTTP. Dừng polling khi connected ổn định, fallback khi disconnected; tần suất cụ thể là lựa chọn kỹ thuật cần đo.
- **API:** REST hiện có giữ nguyên; STOMP đề xuất `/user/queue/notifications`. Read REST thực tế là PUT `/{id}/read` và PUT `/read-all`, không PATCH.
- **Security:** Đích recipient do server xác định, không tin userId client; test cả subscribe raw broker queue và client SEND giả notification; origin allowlist phù hợp deployment.
- **Tests:** User A không nhận/đọc của B, missing/invalid token, expired/logout, reconnect, duplicate/push-HTTP race, DB rollback không push; chat regression.
- **Migration impact:** Nếu đổi Principal naming cần migrate connection đồng bộ FE/BE; session cũ phải reconnect.
- **Rollback risk:** Giữ REST/polling fallback; không tắt persistence khi tắt push. Push không thay NotificationRepository làm nguồn dữ liệu.

Không gọi getUserEmailById chưa tồn tại hoặc gửi mặc định bằng email khi Principal đang là userId. Hook cancellation hiện chỉ log; việc thêm loại notification hủy là phạm vi riêng cần xác nhận, không ngầm gộp vào realtime.

### Feature 7 — Advanced search

**Đã hoàn thành P1-04/F7 theo D7:** [Báo cáo hoàn thành](PHASE2_P1_04_ADVANCED_SEARCH.md). 16/16 test cases trong `AdvancedSearchIntegrationTest` pass, 28/28 regression test pass, TypeScript pass. Hỗ trợ đầy đủ BFS category descendants (D7.2), lọc theo `product.price` (D7.1), whitelist sort `price_asc`/`price_desc`/`id_desc` (D7.4), kẹp trần size 50 và bảo đảm eligibility ACTIVE/Non-banned shop (D7.5). Frontend có filter sidebar, đồng bộ URL SearchParams, reset page khi đổi filter và stale-response guard.

- **Backend changes:** Dynamic query/Specification trong Product kết hợp keyword/category/min/max/sort/paging theo D7; filter eligibility trước paging; whitelist sort, bound page/size, ổn định tie-breaker ID.
- **Database changes:** Không đổi schema. Duyệt con đệ quy BFS bằng `CategoryRepository.findByParentId`.
- **Frontend changes:** Filter sidebar tại ProductListPage, URL SearchParams cho toàn bộ tiêu chí, reset page khi đổi filter, loading/error/empty, abortController guard.
- **API:** Mở rộng GET `/api/v1/products` bằng optional categoryId/minPrice/maxPrice/sort; validate `minPrice <= maxPrice` (HTTP 400).
- **Security:** Public chỉ dữ liệu được phép hiển thị; không cho client inject field/expression sort hoặc bypass status/shop filter.
- **Tests:** 16 test cases integration kiểm thử toàn diện keyword, danh mục con, khoảng giá, sắp xếp, shop ban, hidden product và ràng buộc HTTP 400.
- **Migration impact:** Optional params giữ tương thích 100% với callers cũ. Không có data migration.
- **Rollback risk:** Thấp. Backend có default overload giữ nguyên interface cũ; UI và API đồng bộ.

## 5. P2 — Analytics và rule-based recommendation

### Feature 14 — Shop revenue & analytics

- **Backend changes:** Aggregate Order/OrderItem theo shop lấy từ principal qua Shop contract; dùng Product batch DTO cho category/product mapping. Áp dụng D5, tách order value khỏi cash/refund; AOV và cancellation theo cohort rõ ràng.
- **Database changes:** Không bắt buộc cho counts/order-value baseline. Delivery date/category snapshot/item discount allocation chỉ thêm nếu metric yêu cầu và được chốt; không phỏng đoán lịch sử.
- **Frontend changes:** VendorAnalyticsPage và nav; KPI/series/top products/category table chỉ cho metrics đủ data; empty/error/loading, ghi rõ date basis.
- **API:** Đề xuất GET `/api/v1/vendor/analytics` với khoảng ngày; không nhận shopId tùy ý để bỏ qua ownership.
- **Security:** Shop scope server-side, behavior shop BANNED theo D4; Admin muốn scope khác phải qua API có quyền riêng.
- **Tests:** Cách ly shop A/B, cùng product nhiều variants, cancelled, empty AOV, discount, data mapping mất, thời gian; nếu join Payment phải chống attempt/group duplication.
- **Migration impact:** Additive index/snapshot nếu cần; dữ liệu thiếu không được biểu diễn như số 0 thực tế.
- **Rollback risk:** Read-only thấp; thay nghĩa revenue/category lịch sử có rủi ro báo cáo sai, cần label/version rõ.

### Feature 13 — Recommendation

> **Trạng thái: IMPLEMENTED, scoped verification PASS (2026-09-10).** Baseline public rule-based cùng exact category đã có API/UI và 5 HTTP/MySQL integration tests pass. Không dùng F10/F12, Order, Cart, Review hoặc Payment. Chưa phải final verification toàn dự án: còn full regression/package và đo query count/N+1.

- **Backend changes:** Đề xuất baseline rule-based cùng category, loại product hiện tại/không eligible. Sau đó mới cân nhắc popularity/co-purchase/personalized từ Order/Cart qua contracts; deterministic fallback và giới hạn query.
- **Database changes:** Baseline không cần bảng mới. Không tạo search/view history/wishlist nếu chưa có yêu cầu thu thập; không gọi dữ liệu chưa tồn tại.
- **Frontend changes:** Khối sản phẩm liên quan hoặc gợi ý có loading/empty; không gắn nhãn AI.
- **API:** Đề xuất GET `/api/v1/products/{id}/related`; personalized route chỉ thêm khi chọn rule và phải có auth requirement rõ. Public products/** hiện permit GET nên cần rule cụ thể cho endpoint personalized.
- **Security:** User identity từ principal; không lộ history hay suy ra dữ liệu người khác; Shop/Product eligibility dùng chung catalog.
- **Tests:** Không gợi ý chính nó/trùng/HIDDEN/BANNED, cold-start, mapping variant mất, history đúng user, query bound; nếu co-purchase đa shop chốt group semantics.
- **Migration impact:** Không cho baseline; bổ sung tracking tương lai cần policy/data lifecycle riêng.
- **Rollback risk:** Thấp, ẩn block UI và giữ catalog; không phụ thuộc checkout vào recommendation.

## 6. P3 — Delivery và payment refund có điều kiện

### Feature 11 — Delivery confirmation

**Trạng thái: hoàn thành theo D8, kiểm chứng local 2026-09-10.** Xem [báo cáo P3-01](PHASE2_P3_01_DELIVERY_CONFIRMATION.md). Buyer xác nhận `SHIPPED → DELIVERED`; Vendor không còn quyền DELIVERED; Admin giữ ngoại lệ D2 có audit. Migration delivery evidence là additive và chưa deploy.

- **Backend changes:** Sau D8, mở rộng flow hiện Vendor SHIPPED → DELIVERED theo actor đã chốt. Nếu SHIPPER: assignment/ownership và Identity role; nếu buyer/carrier: contract riêng. Không thêm auto-complete/time limit mặc định.
- **Database changes:** Chỉ bổ sung timestamp, assignment/tracking/proof theo thiết kế được chọn; OrderStatus mới chỉ khi mô hình yêu cầu.
- **Frontend changes:** UI cho actor đã chọn và hiển thị tiến độ/proof cần thiết cho buyer/vendor; không tự dựng app shipper khi chưa chốt.
- **API:** Chưa chốt route. Candidate PATCH `/api/v1/orders/{id}/confirm-delivery` chỉ phù hợp nếu chọn buyer; shipper/callback cần route/authorization khác.
- **Security:** Quyền confirm gắn owner/assignee/provider signature; proof không cho người ngoài sửa/xem trái phép.
- **Tests:** Sai actor/assignment, status chưa SHIPPED, lặp/đồng thời, proof validation nếu dùng; review chỉ sau điều kiện hoàn tất đã chốt.
- **Migration impact:** deliveredAt lịch sử chưa biết phải nullable/unknown; updatedAt không đủ làm backfill.
- **Rollback risk:** Cao nếu rollback role/status mới khi dữ liệu đang vận hành; giữ compatibility và xử lý các delivery đang mở.

### Feature 10 — Payment refund

> **Trạng thái: FROZEN, 2026-09-10.** D9 baseline chỉ được mở cho sandbox. Refund ledger/API/locking và automated regression đã được implement; chưa có real VNPay sandbox refund evidence vì môi trường hiện không kết nối được `sandbox.vnpayment.vn:443`. Không đánh dấu COMPLETED, không mở production/COD/Return, không tự retry `UNKNOWN`. Xem [D9 gate](PHASE2_P3_02_D9_PAYMENT_REFUND_DESIGN.md) và [F10 implementation design](PHASE2_P3_03_F10_PAYMENT_REFUND_IMPLEMENTATION_DESIGN.md). Mở lại F10 chỉ để hoàn tất real sandbox payment → IPN → CANCELLED order → Admin refund → reconciliation/idempotency evidence và báo cáo nghiệm thu.

- **Backend changes:** Trước tiên lập capability checklist từ tài liệu gateway chính thức và quyền merchant thực tế: full/partial, signature, required fields, query/status, retry/idempotency. Source hiện chưa hỗ trợ; **chỉ implementation sau khi xác minh D9**. Khi khả thi, tạo refund orchestration/ledger riêng, server tính amount và phân biệt requested/unknown/confirmed result.
- **Database changes:** Refund transaction/attempt ledger với idempotency key unique, payment/order/group references, amount/currency, trạng thái và gateway references sau khi contract được chốt. Không trộn refund với PaymentStatus SUCCESS.
- **Frontend changes:** UI yêu cầu/theo dõi cho actor đã cho phép, trạng thái đang xử lý/không rõ/thất bại/thành công; không thông báo hoàn thành khi chỉ đã gửi request.
- **API:** Chưa chốt endpoint/body/webhook khi thiếu gateway contract và policy. Không giả định `vnp_Command=refund` đủ triển khai.
- **Security:** Server tính số tiền còn được hoàn; validate ownership/role và gateway response; không lộ secret/raw payload, idempotency chống refund vượt paid amount.
- **Tests:** Contract fixtures chính thức, chữ ký sai, partial từng shop, retries/timeout/query reconciliation, duplicate event, nhiều payment attempts, COD trước/sau thu tiền theo policy; sandbox integration chỉ sau capability xác nhận.
- **Migration impact:** Không backfill tất cả CANCELLED thành đã refund hoặc tự gọi gateway cho lịch sử; đối soát riêng.
- **Rollback risk:** Rất cao: rollback code không đảo ngược tiền đã hoàn. Giữ ledger và reconciliation, dừng lệnh mới khi cần; không xóa pending records.

COD chưa thu tiền không phát sinh khoản tiền thực thu để hoàn; sau thu tiền phải theo D9. Hiện chưa có ledger thu tiền COD, không mặc định mọi COD refund bằng 0.

## 7. P4 — Return & refund flow

### Feature 12 — Return & Refund

> **Trạng thái: BLOCKED pending D10, 2026-09-10.** Source inspection đã hoàn thành tại [P4-01 D10 gate](PHASE2_P4_01_D10_RETURN_REFUND_DESIGN.md). D10 business policy/state machine chưa được duyệt; chưa tạo implementation design, Return module, schema, Payment refund integration, F12 tests hoặc final verification. Candidate model không phải implementation progress và không cho phép đánh dấu F12 `IN PROGRESS` hay `COMPLETED`.

| Hạng mục | Trạng thái |
|---|---|
| P4/F12 source inspection | Complete |
| D10 policy | Pending approval |
| Return implementation design | Chưa tạo |
| Return module/code | Chưa implement |
| Database schema | Chưa tạo |
| Payment refund integration | Chưa tích hợp |
| F12 tests | Chưa chạy |
| F12 final verification | Chưa có |
| F12 | **BLOCKED** |

- **Backend changes:** Sau D10, thiết kế Return aggregate/ownership, toàn đơn hay items, reason/evidence/approval/disposition; liên kết F4/F10 tại mốc nghiệp vụ được chọn. Không mặc định request/approval đồng nghĩa nhập lại kho hoặc đã hoàn tiền.
- **Database changes:** Candidate return_requests và return_items (nếu partial), history/evidence references/refund link; fields/status được chốt từ state machine. Không tự thêm RETURNED_AND_REFUNDED vào Order enum.
- **Frontend changes:** Buyer request/status; Vendor review/receive theo policy; Admin dispute/intervention theo quyền đã duyệt; validation/error/loading và trạng thái tiến trình độc lập.
- **API:** Thiết kế command-oriented endpoints theo transition đã chốt; chưa cố định endpoint/schema trước policy.
- **Security:** Buyer chỉ yêu cầu trên đơn của mình, vendor chỉ xử lý shop mình, admin đúng phạm vi; evidence access và qty không vượt đủ điều kiện return.
- **Tests:** Ineligible/partial/duplicate requests, quyền, approval/rejection, hàng không bán lại, restore/refund chỉ một lần, retry/timeout, không chuyển hoàn tất khi còn nghĩa vụ chưa xong.
- **Migration impact:** Schema additive; dữ liệu đơn cũ không tự tạo return. Analytics phải phân biệt chưa có dữ liệu return với không có return.
- **Rollback risk:** Rất cao nếu đã nhận hàng/hoàn tiền. Giữ audit/ledger, có đường xử lý request đang mở, không rollback bằng xóa bảng hoặc đưa lùi Order.

## 8. Thứ tự tối ưu và tiêu chí từng tầng

| Giai đoạn | Thứ tự đề xuất | Tiêu chí hoàn thành |
|---|---|---|
| P0 / Sprint 1 | F8 baseline + F9 → F4 → F5, test cùng từng slice | Không hoàn kho trùng/mất khi cancel; mã lỗi core/FE nhất quán; shop policy đã chốt và enforce; có execution evidence |
| P1 | F3 sau D2/F4; F6, F1, F7 độc lập; F2 sau D5 | Admin không bypass state/payment; profile save đúng user; realtime có authorization/reconcile; filter đúng paging; metrics có định nghĩa |
| P2 | F14 và F13 theo dữ liệu sẵn có | Analytics đúng scope/meaning; rule-based eligibility/cold-start kiểm chứng được |
| P3 | F11 hoàn thành theo D8; F10 FROZEN sau sandbox implementation | Confirmation đúng actor; F10 chỉ hoàn thành sau real sandbox evidence, reconciliation và regression cuối |
| P4 | F12 sau policy và các nhánh prerequisite cần dùng | Return state/stock/payment nhất quán, có audit và xử lý retry |

Counts dashboard có thể làm trước F3; recommendation cùng category không cần chờ advanced search. Khảo sát gateway có thể làm sớm để biết blocker, nhưng không mở implementation refund sớm. F8/F9 không kết thúc sau một sprint.

### Sprint 1 đề xuất để bắt đầu

1. Ghi baseline source/test và chuẩn bị DB integration riêng; không nhận report cũ là pass.
2. Hoàn thiện error contract + RequireShop consumer; test handler/security.
3. Chốt D1, sửa cancellation theo matrix hiện tại, kiểm thử atomicity/idempotency và race IPN.
4. Chốt D3/D4, hoàn thiện ban/unban/enforcement/FE.
5. Bổ sung checkout/voucher/payment tests; ghi rõ rounding behavior hiện tại để chốt cách sửa, không viết test chỉ lặp implementation.
6. Build/lint/smoke và báo cáo nghiệm thu, cập nhật audit bằng kết quả thật.

Ước lượng P0 khoảng **7–10 ngày công** sau khi các quyết định cần thiết đã chốt và môi trường test sẵn sàng; đây là ước lượng kế hoạch, không cam kết lịch.

Lệnh unit baseline dự kiến tại root, chưa chạy trong lượt audit:
```powershell
.\mvnw.cmd "-Dtest=OrderServiceImplTest,ProductInternalServiceImplTest,ShopServiceTest,VoucherInternalServiceImplTest" test
```

Tại frontend: `npm run build`, `npm run lint`. Integration phải chọn suite sau khi kiểm tra fixture và DB target. Seeder bị exclude không chứng minh các test còn lại không thay đổi dữ liệu.

## 9. Điều kiện kiểm chứng chung

- Kết quả ghi rõ test nào đã chạy, pass/fail/skipped và lỗi môi trường; không dùng số case thay cho coverage.
- DB migration có script/cách áp dụng theo môi trường thật; ddl-auto update không thay thế migration/rollback versioned.
- Thay đổi có quyền/tiền/kho phải kiểm thử cả concurrency/rollback bằng DB phù hợp.
- Báo cáo nêu các policy còn mở và feature chưa triển khai; không tự bổ sung requirement để đóng checklist.
- Workspace hiện không được git status nhận diện là repository; cần xác định cách checkpoint khi implementation. Không tự git init trong audit.
