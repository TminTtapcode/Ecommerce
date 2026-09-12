# PHASE 2 — Audit thực trạng tính năng

Ngày đối chiếu source: **2026-09-09**. Phạm vi: audit và lập kế hoạch, không sửa code nghiệp vụ.

**Bổ sung sau audit:** [S1-01 Baseline](PHASE2_S1_01_BASELINE.md) ghi nhận 41/55 pass, 12 failures, 2 errors. **Sprint 1 nay đã hoàn thành** theo [D1/D3/D4 và Error](PHASE2_SPRINT1_DECISIONS.md): [S1-07/08 final audit](PHASE2_S1_07_08_VERIFICATION.md) có 155/155 backend và 6/6 frontend pass, build/lint/browser smoke đã chạy. Test datasource mặc định đã chuyển sang schema riêng tại 127.0.0.1:33317. Các nhận định “chưa triển khai/chưa chạy test” bên dưới là snapshot ban đầu.

## Phương pháp và giới hạn

**Cập nhật đến S1-06:** Error, Inventory, Payment Guard và Ban/Unban/enforcement đã triển khai. [Báo cáo mới nhất](PHASE2_S1_05_06_SHOP_BAN_ENFORCEMENT.md): regression chọn lọc 114/114 backend và 6/6 frontend pass; migration prior_status đã kiểm chứng trên schema fixture riêng. Các mô tả chưa triển khai bên dưới giữ lại snapshot audit ban đầu, không phải trạng thái hiện tại.

**Cập nhật S1-02:** nền tảng Error Response, security envelope, mã domain Shop/Auth và FE consumers đã triển khai. [Báo cáo kiểm chứng](PHASE2_S1_02_ERROR_CONTRACT.md) có 25 test backend mới và 6 test frontend mới pass. Phần mô tả source bên dưới là snapshot audit trước implementation; không dùng để phủ nhận các thay đổi đã có evidence.

Đối chiếu controller, service, internal contract, entity/repository, frontend và test trong workspace. “Existing database” chỉ là **schema được entity ánh xạ và seed code mô tả**; chưa truy vấn MySQL để xác nhận schema/data đang triển khai. Không chạy ứng dụng, seed, migration hoặc test ghi DB. Không suy ra pass/coverage từ file cũ trong `target/`.

Thứ tự bằng chứng: source hiện tại → test hiện tại → tài liệu mô tả. `pom.xml` khai báo Java 21/Spring Boot 4.1.0; mô tả Java 17+/Spring Boot 3.x trong tài liệu nền đã cũ. JPA hiện dùng `ddl-auto: update`; chưa thấy Flyway/Liquibase trong pom hoặc migration versioned trong resources. `docs/MODULE_DEVELOPMENT_WORKFLOW.md` được rules tham chiếu nhưng không có tại đường dẫn đó.

PARTIAL/MISSING là kết luận đọc source, không phải chứng nhận runtime. Ưu tiên/ngày công là đề xuất; các lựa chọn nghiệp vụ chưa chốt không được xem là requirement đã duyệt.

## Tổng quan

| # | Feature | Trạng thái thực tế | Ưu tiên đề xuất | Độ phức tạp / ngày công còn lại |
|---|---|---|---|---|
| 1 | WebSocket Notification Push | PARTIAL: DB/REST và chat STOMP; chưa push notification | P1 | Trung bình / 2–4 |
| 2 | Admin Dashboard Statistics | PLACEHOLDER; chưa có aggregation API | P1 | Trung bình / 2–4 sau chốt metrics |
| 3 | Admin Order Status Mutation | MISSING; admin chỉ GET | P1 | Trung bình / 2–3 sau chốt transition |
| 4 | Inventory refund on cancellation | PARTIAL: listener hoàn kho đã có; thiếu bảo đảm nguyên tử/chống lặp | P0 | Cao về toàn vẹn / 2–3 |
| 5 | Admin Ban/Unban Shop | PARTIAL: BE/FE có, enforcement/audit chưa đầy đủ | P0 | Trung bình / 2–3 |
| 6 | Profile | READ-ONLY từ AuthContext, chưa profile API | P1 | Thấp–trung bình / 1–2 |
| 7 | Advanced Search | PARTIAL: keyword + paging; category UI chưa lọc | P1 | Trung bình / 2–4 |
| 8 | Unit Test | PARTIAL: 21 unit test hiện hữu | P0, xuyên suốt | Trung bình–cao / 2–4 ban đầu |
| 9 | Standardized Error Codes | PARTIAL: enum/exception/handler có, chưa nhất quán | P0 | Trung bình / 1–3 cho core |
| 10 | Payment Refund | MISSING; contract payment chưa có refund | P3, có điều kiện | Cao / khảo sát 1–2; implementation chưa chốt |
| 11 | Delivery Confirmation | PARTIAL: Vendor xác nhận DELIVERED, chưa bên độc lập | P3, chờ policy | Trung bình–cao / chưa chốt |
| 12 | Return & Refund | MISSING | P4, có điều kiện | Rất cao / chưa chốt |
| 13 | Recommendation | MISSING; “Gợi Ý Hôm Nay” vẫn là listing | P2 | Trung bình / 2–4 cơ bản |
| 14 | Shop Revenue & Analytics | MISSING API/dashboard | P2 | Trung bình–cao / 3–5 sau chốt metrics |

Ước lượng có phần dùng chung, không cộng máy móc thành thời gian toàn dự án.

## 1. WebSocket Notification Push

- **Current status:** PARTIAL. Unread count polling 60.000 ms, danh sách tải khi mở dropdown.
- **Existing backend:** [NotificationServiceImpl](../src/main/java/com/tmt/ecommerce/notification/service/NotificationServiceImpl.java) đọc/đánh dấu đọc. Các listener Order/Shop/Review trực tiếp lưu NotificationRepository. Order listener AFTER_COMMIT + REQUIRES_NEW; hook hủy đơn chỉ log. [WebSocketConfig](../src/main/java/com/tmt/ecommerce/chat/config/WebSocketConfig.java) có `/ws`, `/queue`, `/topic`, `/app`, `/user` phục vụ chat.
- **Existing frontend:** [NotificationDropdown](../frontend/src/components/notification/NotificationDropdown.tsx), notificationApi; [websocket.ts](../frontend/src/services/websocket.ts) dùng native brokerURL và `/user/queue/messages`. Có dependency SockJS nhưng đường này không dùng SockJS.
- **Existing database:** notifications có userId/type/referenceId/title/message/isRead/createdAt. Kiểm tra tồn tại trước save có cho confirmed/new order, chưa đồng nhất mọi event.
- **Missing components:** Push sau commit transaction lưu notification; subscribe/reconnect/reconcile HTTP, dedup ID, lifecycle logout; STOMP authorization.
- **Dependencies:** Notification persistence và hạ tầng chat/JWT. [JwtChannelInterceptor](../src/main/java/com/tmt/ecommerce/chat/config/JwtChannelInterceptor.java) dùng userId chuỗi nếu có, fallback email; không mặc định gửi theo email. IdentityInternalService chưa có getUserEmailById.
- **Risks:** CONNECT thiếu token vẫn đi tiếp chưa gán user; chưa có rule SUBSCRIBE/SEND trong interceptor; wildcard origin. Cần kiểm chứng truy cập destination trái phép. Push trước commit, message trùng/multi-tab gây lệch badge. Chỉ inject template vào NotificationService không bao phủ nơi tạo notification ở listener.
- **Estimated complexity:** Trung bình, 2–4 ngày. **Priority:** P1; authorization trước khi bật push.

## 2. Admin Dashboard Statistics

- **Current status:** PLACEHOLDER tại route /admin.
- **Existing backend:** Repository Order/Payment/Shop/Identity/Product, chưa có dashboard aggregate service/controller/contract.
- **Existing frontend:** [App.tsx](../frontend/src/App.tsx) chỉ có chào mừng trong AdminLayout.
- **Existing database:** Order có totalAmount/originalTotalAmount/discountAmount/status/createdAt/updatedAt; Payment có amount/method/status/paymentGroupId/createdAt. User/Shop/Product entity không có createdAt. Không có cost/commission/refund ledger/deliveredAt.
- **Missing components:** Metric contract, aggregate DTO trong từng module, API/admin UI và loading/error/empty; ngày/timezone và chống đếm trùng.
- **Dependencies:** Order integrity, internal aggregate contracts. Không inject repository xuyên module vào Order service.
- **Risks:** CONFIRMED không có nghĩa đã thu tiền vì COD được xác nhận khi checkout. Một group có nhiều Order và nhiều Payment attempts; JOIN trực tiếp gây nhân tổng. Không tính khách hàng mới theo ngày từ entity hiện tại.
- **Estimated complexity:** Trung bình, 2–4 ngày sau chốt metrics. **Priority:** P1.

## 3. Admin Order Status Mutation

**Cập nhật sau audit:** F3 đã triển khai theo D2-A, gồm Admin command/Payment read guard/history/UI và 33 integration cases mới. [Báo cáo P1-01](PHASE2_P1_01_ADMIN_ORDER.md) ghi full regression 188/188 và browser smoke pass. Mô tả MISSING phía dưới là snapshot trước implementation.

- **Current status:** MISSING.
- **Existing backend:** [AdminOrderController](../src/main/java/com/tmt/ecommerce/order/controller/AdminOrderController.java) chỉ GET list/detail, ADMIN. [OrderServiceImpl](../src/main/java/com/tmt/ecommerce/order/service/OrderServiceImpl.java) có Vendor matrix: CONFIRMED → SHIPPED/CANCELLED; SHIPPED → DELIVERED. Không cho Vendor transition từ PENDING. Payment group bulk update chưa áp dụng matrix.
- **Existing frontend:** [AdminOrderDetailPage](../frontend/src/pages/admin/AdminOrderDetailPage.tsx) chỉ xem; adminApi không mutation order.
- **Existing database:** Order/OrderItem; chưa có history/audit entity. Vendor DTO có reason nhưng service đổi trạng thái chưa dùng.
- **Missing components:** Chốt quyền/transition Admin và điều kiện payment; command dùng chung, API/DTO, UI, actor/reason audit.
- **Dependencies:** F4 trước khi mở Admin cancellation; F9/F8; Payment nếu guard liên quan tiền.
- **Risks:** ADMIN được vào Vendor controller nhưng service vẫn kiểm tra ownership; không phải API can thiệp mọi shop. Không mặc định Admin được xác nhận PENDING hoặc hủy đơn đã trả tiền. IPN bulk update có thể ghi đè trạng thái cuối.
- **Estimated complexity:** Trung bình, 2–3 ngày sau chốt policy. **Priority:** P1.

## 4. Inventory refund on cancellation

- **Current status:** PARTIAL; đã có code hoàn kho.
- **Existing backend:** [ProductInternalServiceImpl](../src/main/java/com/tmt/ecommerce/product/service/ProductInternalServiceImpl.java) deduct/restore cả variant và product. [ProductOrderCancelledListener](../src/main/java/com/tmt/ecommerce/product/listener/ProductOrderCancelledListener.java) AFTER_COMMIT + REQUIRES_NEW, catch lỗi từng item rồi log; event có variantId/quantity.
- **Existing frontend:** VendorOrderListPage có hủy và nhập reason; response chỉ nói cập nhật status thành công.
- **Existing database:** orders → order_items(product_variant_id, quantity) → product_variants.stock_quantity và products.stock_quantity. Atomic cộng/trừ; chưa có ledger/dấu xử lý event.
- **Transaction trace:** Checkout @Transactional → Cart DTO → nhóm shop → deductStock → snapshot OrderItem → lưu Order → xóa item giỏ. COD thành CONFIRMED. Vendor hủy CONFIRMED → CANCELLED → publish event → commit Order → transaction listener mới → cộng kho.
- **Missing components:** Atomicity hoặc retry bền vững; chống cạnh tranh theo orderId; kiểm tra số dòng restore; xử lý input/variant mất; test rollback/duplicate. Chưa có restoreStockBatch trong contract.
- **Dependencies:** Order event/DTO, Product contract, transaction DB; chưa có module Inventory riêng.
- **Risks:** Lần hủy thứ hai tuần tự bị matrix chặn; hai request đồng thời chưa được bảo vệ tại đường findById. Atomic increment không chống event trùng. Crash sau Order commit hoặc restore lỗi gây chênh kho; catch không bảo đảm transaction mới commit được. restoreStock bỏ qua input sai/variant mất. Group update nếu gọi CANCELLED chưa phát cancel event.
- **Estimated complexity:** Cao về toàn vẹn, 2–3 ngày. **Priority:** P0. Mở Vendor hủy PENDING là policy mới, không thuộc sửa lỗi mặc định.

## 5. Admin Ban/Unban Shop

- **Current status:** PARTIAL, đã có cả BE và FE.
- **Existing backend:** [AdminShopController](../src/main/java/com/tmt/ecommerce/shop/controller/AdminShopController.java): PUT approve/ban/unban, ADMIN. [ShopService](../src/main/java/com/tmt/ecommerce/shop/service/ShopService.java): ban PENDING/ACTIVE → BANNED, unban BANNED → ACTIVE. Body Map/reason tùy chọn nhưng reason bị bỏ qua. Approve cấp ROLE_VENDOR; unban không cấp.
- **Existing frontend:** [adminApi](../frontend/src/api/adminApi.ts), [AdminShopListPage](../frontend/src/pages/admin/AdminShopListPage.tsx): nút ban ACTIVE bằng prompt, unban BANNED bằng confirm; badge/filter. [RequireShop](../frontend/src/components/vendor/RequireShop.tsx) chặn children khi BANNED, fetch khi mount.
- **Existing database:** Shop.status PENDING/ACTIVE/BANNED, chưa banReason/trạng thái trước ban/audit entity.
- **Missing components:** Chốt ban PENDING/unban và xử lý đơn cũ; enforce backend ở catalog/checkout/product; DTO, audit/lưu reason theo policy; refresh UI/pending state.
- **Dependencies:** Shop/Identity, Product/Cart/Order qua contract. ShopInternalService hiện chỉ lookup/ownership, chưa validate ACTIVE.
- **Risks:** Ban PENDING rồi unban thành ACTIVE nhưng chưa duyệt/cấp role. Public list chỉ product ACTIVE; detail dùng findById không lọc status. Checkout/createProduct chưa check shop ACTIVE tại đường khảo sát. FE guard không chống API trực tiếp và có thể chặn quản lý đơn cũ.
- **Estimated complexity:** Trung bình, 2–3 ngày tùy policy. **Priority:** P0. Không tự đổi thành chỉ ban ACTIVE.

## 6. Frontend Profile Page

**Cập nhật sau audit:** [F6 đã hoàn thành theo D6](PHASE2_P1_02_PROFILE.md): GET/PUT profile whitelist, form và AuthContext hydration/session guards; 33 integration cases mới, full regression 221/221, 9/9 FE tests và browser smoke pass. Các mô tả read-only/chưa có API phía dưới là snapshot trước implementation.

- **Current status:** READ-ONLY.
- **Existing backend:** Identity AuthController/AuthenticationService/UserRepository, chưa UserController profile GET/PUT.
- **Existing frontend:** [ProfilePage](../frontend/src/pages/public/ProfilePage.tsx) từ AuthContext: userId/sub/fullName/roles, avatar chữ cái; loading/auth redirect, chưa form save/API.
- **Existing database:** [User](../src/main/java/com/tmt/ecommerce/identity/entity/User.java): email/passwordHash/fullName(100)/phone(20)/status/roles. Không avatar URL/createdAt.
- **Missing components:** API /me, safe DTO, validation, editable fields được chốt, success/error, đồng bộ AuthContext sau save.
- **Dependencies:** Identity/current-user resolver/auth FE/F9. Media có sẵn không có nghĩa profile hỗ trợ avatar.
- **Risks:** JWT fullName cũ sau save; không cho body userId/roles/status đổi account/quyền. Không tự đặt regex quốc gia hoặc thêm avatar/email update khi chưa yêu cầu.
- **Estimated complexity:** Thấp–trung bình, 1–2 ngày cho name/phone nếu được chọn. **Priority:** P1.

## 7. Advanced Search

- **Current status:** PARTIAL: keyword theo tên, page/size, sort id desc cố định.
- **Existing backend:** [ProductController](../src/main/java/com/tmt/ecommerce/product/controller/ProductController.java), ProductServiceImpl/Repository. Category có parent; [Brand](../src/main/java/com/tmt/ecommerce/product/entity/Brand.java)/BrandRepository và Product.brand nullable đã có. Create DTO nhận brandId nhưng create service chưa gán brand, response chưa có brand.
- **Existing frontend:** [ProductListPage](../frontend/src/pages/public/ProductListPage.tsx) sync page/size/keyword URL; category click chỉ console.log; chưa filter giá/brand/sort.
- **Existing database:** products.price/category_id/brand_id, variants.price, categories.parent_id, brands. Seed có gắn Brand cho sản phẩm mẫu; chưa xác nhận dữ liệu DB thực. Product không có createdAt/rating/soldCount.
- **Missing components:** Query đa filter trước paging, sort whitelist/validation; brand options API nếu dùng; chốt giá gốc hay variant, parent category có lấy con không; FE URL sync/reset page.
- **Dependencies:** Product/Category/Brand cùng module; F5 shop eligibility. Review/Order chỉ cần nếu chọn rating/sales sort.
- **Risks:** Không tạo thêm Brand model. Không sort createdAt chưa tồn tại; id desc không chứng minh ngày tạo. Join variant dễ duplicate, lọc sau paging sai total. Không tự backfill brand theo suy đoán.
- **Estimated complexity:** Trung bình, 2–4 ngày. **Priority:** P1.

## 8. Unit Test

- **Current status:** PARTIAL, không chỉ integration test.
- **Existing backend:** 14 file, **56 method @Test**: 21 unit trong 4 lớp; 34 test Spring context trong 9 lớp; 1 test seeder bị Surefire exclude. Chưa thấy JaCoCo trong pom, không có kết luận % line/branch coverage.
- **Existing frontend:** package.json chỉ có build/lint/dev/preview, chưa test script/framework FE.
- **Existing database:** Unit mock không cần DB. Integration cần xác minh DB riêng/side effect; @SpringBootTest không tự bảo đảm cách ly.
- **Existing unit scope:** [OrderServiceImplTest](../src/test/java/com/tmt/ecommerce/order/service/OrderServiceImplTest.java) 4 case; [ProductInternalServiceImplTest](../src/test/java/com/tmt/ecommerce/product/service/ProductInternalServiceImplTest.java) 4; [ShopServiceTest](../src/test/java/com/tmt/ecommerce/shop/service/ShopServiceTest.java) 9; [VoucherInternalServiceImplTest](../src/test/java/com/tmt/ecommerce/voucher/service/VoucherInternalServiceImplTest.java) 4.
- **Missing components:** Checkout nhiều shop, rollback/concurrency, quyền/state đầy đủ, voucher lỗi, unit PaymentGatewayService/VnPayStrategy, error mapping. Voucher split test đang chấp nhận tổng 9 cho discount 10; chưa kiểm tra bảo toàn tổng phân bổ.
- **Dependencies:** F4/F5/F9 và các feature được sửa; JUnit/Mockito đã có.
- **Risks:** Mock không chứng minh lock/commit; test count không phải coverage %. Integration NPlus1/transaction/IPN/e2e chưa chạy lại. Không hứa pass hoặc thời gian dưới 5 giây từ source.
- **Estimated complexity:** 2–4 ngày bổ sung ban đầu, tiếp tục theo feature. **Priority:** P0, xuyên suốt.

## 9. Standardized Error Codes

- **Current status:** PARTIAL.
- **Existing backend:** [ErrorCode](../src/main/java/com/tmt/ecommerce/common/exception/ErrorCode.java), AppException, ApiResponse.errorCode, [GlobalExceptionHandler](../src/main/java/com/tmt/ecommerce/common/exception/GlobalExceptionHandler.java) đã có. Nhiều service vẫn throw text; handler gom IllegalArgumentException/IllegalStateException/BusinessException → INVALID_INPUT. NOT_FOUND ở handler chưa có trong enum.
- **Existing frontend:** apiClient theo HTTP 401; [common.types](../frontend/src/api/types/common.types.ts) chưa errorCode. RequireShop dò message tiếng Việt; chưa errorCodes mapping file.
- **Existing database:** Không có thay đổi schema cần thiết; errorCode là response field.
- **Missing components:** Catalog mã nhất quán, chuyển nhánh nghiệp vụ, validation/binding/security envelope; FE mapping fallback; test tương thích.
- **Dependencies:** Common/security/module được chuyển/FE API và RequireShop.
- **Risks:** SecurityConfig sendError không bảo đảm envelope. Đổi 400 → 404 làm hỏng RequireShop nếu không cập nhật cùng. Callback VNPay dùng RspCode/Message riêng, không bọc ApiResponse.
- **Estimated complexity:** Trung bình, 1–3 ngày core; phần còn lại theo slice. **Priority:** P0.

## 10. Refund khi hủy/trả hàng

- **Current status:** MISSING trong contract/source.
- **Existing backend:** [PaymentGatewayService](../src/main/java/com/tmt/ecommerce/payment/service/PaymentGatewayService.java) tạo payment attempts/process IPN; VnPayStrategy URL pay/HMAC-SHA512; CodStrategy redirect. PaymentMethod có MOMO nhưng chưa strategy tương ứng. PaymentStrategy chưa refund/query refund.
- **Existing frontend:** Checkout/payment result/retry, chưa refund UI.
- **Existing database:** PaymentStatus PENDING/SUCCESS/FAILED/CANCELED; txnRef/gatewayTransactionId/paymentGroupId/amount/rawData. Chưa refund ledger/status/idempotency.
- **Missing components:** Xác minh gateway contract và quyền merchant trước; chốt eligible amount/partial/full/COD thực thu, approval, đối soát timeout; API/schema/UI sau đó.
- **Dependencies:** Payment attempt identity/SUCCESS, Order amount/group; F4 chỉ nhánh có hoàn hàng, F12 cho return. Hoàn tiền và hoàn kho không phải luôn đi cùng.
- **Risks:** IPN lock chỉ theo payment attempt, chưa chống nhiều attempt SUCCESS của group. Amount chỉ kiểm tra khi field có mặt; service chưa xét vnp_TransactionStatus: cần đối chiếu contract trước khi sửa. Không mặc định COD refund = 0 sau thu tiền. Payment group status API chưa kiểm tra ownership tại controller/service.
- **Estimated complexity:** Khảo sát 1–2 ngày; implementation chưa đủ cơ sở ước lượng. **Priority:** P3; IPN integrity liên quan P0 làm trước.

## 11. Shipper Delivery Confirmation

- **Current status:** PARTIAL: Vendor đã xác nhận giao, chưa actor độc lập.
- **Existing backend:** OrderServiceImpl SHIPPED → DELIVERED/event; seed roles USER/VENDOR/ADMIN, chưa SHIPPER/delivery assignment.
- **Existing frontend:** VendorOrderListPage có hoàn tất; buyer OrderDetailPage hiển thị status/review khi DELIVERED, chưa confirm delivery.
- **Existing database:** OrderStatus PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED; chưa carrier/tracking/deliveredAt/proof/assignedShipper trong entity.
- **Missing components:** Chốt actor: Vendor/buyer/shipper/carrier; ownership/assignment/proof nếu cần; sau đó chốt API/status/schema.
- **Dependencies:** Order state machine, Identity nếu role mới, F8/F9; ảnh hưởng review eligibility/F12.
- **Risks:** Không tự thay Shipper bằng buyer hoặc thêm auto-delivery 7 ngày. DELIVERED hiện không chứng minh COD thu tiền/hàng thực sự tới nơi.
- **Estimated complexity:** Trung bình–cao tùy lựa chọn, chưa chốt. **Priority:** P3.

## 12. Return & Refund

- **Current status:** MISSING.
- **Existing backend:** Order/Product stock/Payment/Media/ownership contracts; chưa ReturnRequest/controller/service.
- **Existing frontend:** Chưa form return hoặc queue buyer/vendor/admin.
- **Existing database:** Chưa return_requests/return_items/refund ledger mapping; OrderStatus chưa returned.
- **Missing components:** Điều kiện/thời hạn/toàn đơn hay từng item/người duyệt/tình trạng hàng/thời điểm nhập kho/số tiền/phương thức; audit/tranh chấp và API/UI theo policy.
- **Dependencies:** F11 completion, F10 nếu có nghĩa vụ tiền, F4 nếu nhập lại kho; Order/Media/Identity. Analytics là downstream consumer, không chặn tạo return.
- **Risks:** Hàng trả không luôn bán lại được; không restore khi mới yêu cầu. Chỉ lưu amount/status trên Order không đủ partial return. Không tự thêm enum policy.
- **Estimated complexity:** Rất cao; chưa ước lượng implementation khi thiếu policy. **Priority:** P4.

## 13. Recommendation

- **Current status:** MISSING; tiêu đề “Gợi Ý Hôm Nay” vẫn là catalog.
- **Existing backend:** Product/Category/Brand, Order/OrderItem, Cart/CartItem, Review; chưa recommendation API/service hoặc search/view history/wishlist.
- **Existing frontend:** Catalog/detail/cart có thể gắn gợi ý sau; hiện chưa request recommendations.
- **Existing database:** Order history và cart; OrderItem snapshot tên/giá, có variantId nhưng không productId/category snapshot. Cần map qua Product contract. Không thấy entity history search/view/wishlist.
- **Missing components:** Chọn rule-based baseline, tiêu chí đánh giá, API, eligibility/dedup/cold-start, UI. Co-purchase đa shop cần cân nhắc paymentGroupId thay vì chỉ order con.
- **Dependencies:** Product eligibility/F5/category; Order/Cart/Review contracts nếu dùng tín hiệu đó. Không cần chờ F7 hoàn tất để gợi ý cùng category.
- **Risks:** Không gọi là AI hoặc dùng history chưa thu thập. Variant mất/category đổi làm thiếu/sai tín hiệu; không lộ history người khác; loại HIDDEN/BANNED.
- **Estimated complexity:** Trung bình, 2–4 ngày baseline. **Priority:** P2.

## 14. Shop Revenue & Business Analytics

- **Current status:** MISSING API/dashboard.
- **Existing backend:** OrderRepository có shopId/status/createdAt, OrderItem quantity/subTotal; ShopInternalService user → shop. Chưa analytics service/projection.
- **Existing frontend:** VendorNav quản lý đơn/sản phẩm/voucher, chưa analytics page/API.
- **Existing database:** Order totals/discount/status/createdAt; item tên/giá snapshot; Product category hiện tại; Payment cấp group. Thiếu deliveredAt/category snapshot/item discount allocation/cost/commission/refund/settlement.
- **Missing components:** Metric definition, date/timezone, aggregate shop contract, ownership API, chart/table/loading/error/empty.
- **Dependencies:** Order correctness/F4/F8/F9; Product contract cho category. Payment/Refund chỉ cần cho cash/refund metrics, không chặn order counts.
- **Risks:** SUM DELIVERED không phải net profit/settlement. updatedAt không phải deliveredAt. Net value theo category/item thiếu phân bổ voucher cấp item. Không trả refund rate 0 khi chưa có dữ liệu.
- **Estimated complexity:** Trung bình–cao, 3–5 ngày sau chốt metrics. **Priority:** P2.

## Khả năng tính metrics

| Metric | Có thể tính | Giới hạn |
|---|---|---|
| Count user/shop/product và shop/product theo status | Có | Không có time series mới tạo cho ba entity |
| Count order/status, đơn gần đây | Có | Đơn con theo shop khác số checkout group |
| SUM orders.totalAmount theo status | Có | Giá trị lưu sau discount, không phải cash/profit |
| Giá trị đơn DELIVERED theo ngày tạo | Có | Cohort createdAt, không phải ngày giao/thu tiền |
| Doanh thu theo ngày giao/thực thu COD | Chưa đủ | Thiếu deliveredAt/collection ledger |
| AOV tập đơn đã chọn | Có: SUM/COUNT | Chốt status/date cohort, tránh chia 0 |
| Tỷ lệ hủy theo cohort tạo đơn | Có: CANCELLED/tổng cohort | Không phải số sự kiện hủy xảy ra trong ngày |
| Tỷ trọng phương thức đặt hàng | Có: count order/paymentMethod | Không phải tỷ trọng payment thành công |
| Tổng records Payment SUCCESS | Có thể tổng hợp | Đối soát duplicate attempt; không cộng lần nữa với Order |
| Top product quantity | Có theo tập status đã chọn | Map variant/product; xử lý dữ liệu mất |
| Category performance | Theo category hiện tại | Không lịch sử category; thiếu item discount cho net value |
| Net profit/commission/settlement/refund rate | Chưa đủ | Không hiển thị số giả hoặc mặc định 0 |

## Kết luận dùng cho kế hoạch

P0 cần hoàn thiện phần đã có. Admin transition, ban PENDING/unban, delivery actor, refund và return policy phải được chốt riêng; behavior source không tự động là policy đúng.

Xem [dependency graph](PHASE2_DEPENDENCY_GRAPH.md) và [implementation plan](PHASE2_IMPLEMENTATION_PLAN.md). Chưa feature nào được đánh dấu hoàn tất kiểm chứng trong lượt này.
