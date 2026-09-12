# FEATURE_INVENTORY.md
# Danh Mục Chức Năng — Enterprise Ecommerce

> **Quy ước Status:**
> - `IMPLEMENTED` — Có đủ bằng chứng Backend + Frontend hoạt động
> - `BACKEND_ONLY` — Backend có nhưng Frontend chưa sử dụng
> - `UI_ONLY` — Frontend có nhưng Backend chưa implement
> - `PARTIALLY_IMPLEMENTED` — Có nhưng chưa hoàn chỉnh
> - `UNVERIFIED` — Không xác minh được đủ bằng chứng

---

## NHÓM CUSTOMER (Buyer)

### F-C01: Đăng ký tài khoản
- **Actor:** Khách vãng lai (Anonymous)
- **Frontend:** `RegisterPage` (`/register`)
- **Backend:** `POST /api/v1/auth/register` → `AuthController.register()`
- **Service:** `AuthenticationService.register()`
- **Database:** INSERT `users`, INSERT `user_roles` (gán ROLE_USER)
- **API:** `POST /api/v1/auth/register`
- **Status:** `IMPLEMENTED`
- **Files:** `AuthController.java`, `AuthenticationService.java`, `User.java`, `RegisterPage.tsx`

### F-C02: Đăng nhập
- **Actor:** User đã đăng ký
- **Frontend:** `LoginPage` (`/login`)
- **Backend:** `POST /api/v1/auth/login` → `AuthController.authenticate()`
- **Service:** `AuthenticationService.authenticate()`
- **Database:** SELECT `users`, `user_roles`, `roles`
- **API:** `POST /api/v1/auth/login`
- **Returns:** JWT token (24h expiry) + role info
- **Status:** `IMPLEMENTED`
- **Files:** `AuthController.java`, `AuthenticationService.java`, `JwtService.java`, `LoginPage.tsx`, `AuthContext.tsx`

### F-C03: Duyệt danh sách sản phẩm
- **Actor:** Public (không cần đăng nhập)
- **Frontend:** `ProductListPage` (`/products`)
- **Backend:** `GET /api/v1/products?keyword=&page=&size=`
- **Service:** `ProductService.getAllProducts()`
- **Database:** SELECT `products`, `product_images`, `categories`, `shops`
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `ProductServiceImpl.java`, `ProductListPage.tsx`, `productApi.ts`

### F-C04: Xem chi tiết sản phẩm
- **Actor:** Public
- **Frontend:** `ProductDetailPage` (`/products/:id`)
- **Backend:** `GET /api/v1/products/{id}`
- **Service:** `ProductService.getProductById()`
- **Database:** SELECT `products`, `product_variants`, `product_images`, `categories`
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `ProductDetailPage.tsx`

### F-C05: Xem giỏ hàng
- **Actor:** User (đăng nhập)
- **Frontend:** `CartPage` (`/cart`) + mini cart trong header
- **Backend:** `GET /api/v1/carts`
- **Service:** `CartService.getCart()`
- **Database:** SELECT `carts`, `cart_items` → JOIN `product_variants`
- **Status:** `IMPLEMENTED`
- **Files:** `CartController.java`, `CartServiceImpl.java`, `CartPage.tsx`, `CartContext.tsx`

### F-C06: Thêm sản phẩm vào giỏ hàng
- **Actor:** User (đăng nhập)
- **Frontend:** `ProductDetailPage` (nút "Thêm vào giỏ")
- **Backend:** `POST /api/v1/carts/items`
- **Validation:** Kiểm tra tồn kho (stock validation)
- **Database:** INSERT/UPDATE `cart_items`
- **Status:** `IMPLEMENTED`
- **Files:** `CartController.java`, `CartServiceImpl.java`, `productApi.ts`

### F-C07: Cập nhật số lượng trong giỏ hàng
- **Actor:** User
- **Frontend:** `CartPage`
- **Backend:** `PUT /api/v1/carts/items/{itemId}`
- **Database:** UPDATE `cart_items`
- **Status:** `IMPLEMENTED`
- **Files:** `CartController.java`, `CartPage.tsx`

### F-C08: Xóa sản phẩm khỏi giỏ hàng
- **Actor:** User
- **Frontend:** `CartPage`
- **Backend:** `DELETE /api/v1/carts/items/{itemId}`
- **Database:** DELETE `cart_items`
- **Status:** `IMPLEMENTED`
- **Files:** `CartController.java`, `CartPage.tsx`

### F-C09: Xem trước đơn hàng (Preview checkout)
- **Actor:** User
- **Frontend:** `CheckoutPage`
- **Backend:** `POST /api/v1/orders/preview`
- **Service:** `OrderService.previewCheckout()`
- **Purpose:** Tính toán giảm giá voucher TRƯỚC khi tạo đơn
- **Status:** `IMPLEMENTED`
- **Files:** `OrderController.java`, `CheckoutPage.tsx`

### F-C10: Áp dụng mã giảm giá (Voucher)
- **Actor:** User
- **Frontend:** `CheckoutPage` (input voucher code)
- **Backend:** tích hợp trong preview + checkout flow
- **Service:** `VoucherInternalService.calculateDiscount()`
- **Status:** `IMPLEMENTED`
- **Files:** `VoucherInternalServiceImpl.java`, `CheckoutPage.tsx`

### F-C11: Đặt hàng (Checkout)
- **Actor:** User
- **Frontend:** `CheckoutPage`
- **Backend:** `POST /api/v1/orders/checkout`
- **Service:** `OrderService.checkout()`
- **Logic:**
  - Tách đơn theo shop (Multi-vendor)
  - Gán `payment_group_id` UUID chung
  - Trừ tồn kho (`product_variants.stock_quantity`)
  - Atomic voucher usage (`used_count + 1`)
  - Xóa cart items đã checkout
- **Database:** INSERT `orders`, INSERT `order_items`, UPDATE `product_variants`, UPDATE `vouchers`, DELETE `cart_items`
- **Status:** `IMPLEMENTED`
- **Files:** `OrderController.java`, `OrderServiceImpl.java`, `CheckoutPage.tsx`

### F-C12: Thanh toán VNPay
- **Actor:** User
- **Frontend:** `CheckoutPage` → redirect đến VNPay → `PaymentResultPage`
- **Backend:**
  - `POST /api/v1/payments/create` → tạo VNPay URL
  - `GET /api/v1/payments/vnpay/ipn` → webhook nhận kết quả
  - `GET /api/v1/payments/group/{id}/status` → polling trạng thái
- **Idempotency:** Pessimistic Lock
- **Status:** `IMPLEMENTED`
- **Files:** `PaymentController.java`, `VnPayStrategy.java`, `PaymentGatewayService.java`, `CheckoutPage.tsx`, `PaymentResultPage.tsx`

### F-C13: Thanh toán COD
- **Actor:** User
- **Frontend:** `CheckoutPage` (chọn COD)
- **Backend:** `POST /api/v1/payments/create` (method=COD) → `CodStrategy`
- **Logic:** Không redirect, tạo Payment record trực tiếp với status SUCCESS
- **Status:** `IMPLEMENTED`
- **Files:** `CodStrategy.java`, `PaymentStrategyFactory.java`

### F-C14: Xem kết quả thanh toán
- **Actor:** User
- **Frontend:** `PaymentResultPage` (`/payment-result`)
- **Backend:** `GET /api/v1/payments/group/{paymentGroupId}/status`
- **Logic:** Polling cho đến khi Payment status != PENDING
- **Status:** `IMPLEMENTED`
- **Files:** `PaymentController.java`, `PaymentResultPage.tsx`

### F-C15: Xem lịch sử đơn hàng
- **Actor:** User
- **Frontend:** `OrderHistoryPage` (`/orders`)
- **Backend:** `GET /api/v1/orders?status=&page=&size=`
- **Database:** SELECT `orders`, `order_items`
- **Status:** `IMPLEMENTED`
- **Files:** `OrderController.java`, `OrderHistoryPage.tsx`

### F-C16: Xem chi tiết đơn hàng
- **Actor:** User
- **Frontend:** `OrderDetailPage` (`/orders/:id`)
- **Backend:** `GET /api/v1/orders/{id}`
- **Database:** SELECT `orders`, `order_items`
- **Status:** `IMPLEMENTED`
- **Files:** `OrderController.java`, `OrderDetailPage.tsx`

### F-C17: Viết đánh giá sản phẩm
- **Actor:** User (phải có đơn hàng DELIVERED với sản phẩm đó)
- **Frontend:** `ProductDetailPage`
- **Backend:** `POST /api/v1/reviews`
- **Service:** `ReviewService.createReview()`
- **Validation:** Kiểm tra `ReviewEligibility` — phải mua sản phẩm qua đơn đã giao
- **Constraint:** unique(user_id, order_id, product_id) — 1 review / đơn hàng / sản phẩm
- **Event:** publish `ReviewCreatedEvent` → Notification
- **Database:** INSERT `reviews`
- **Status:** `IMPLEMENTED`
- **Files:** `ReviewController.java`, `ReviewServiceImpl.java`, `ProductDetailPage.tsx`

### F-C18: Xem đánh giá sản phẩm
- **Actor:** Public
- **Frontend:** `ProductDetailPage`
- **Backend:**
  - `GET /api/v1/products/{id}/reviews` — danh sách đánh giá
  - `GET /api/v1/products/{id}/reviews/summary` — tóm tắt rating
- **Status:** `IMPLEMENTED`
- **Files:** `ReviewController.java`, `ProductDetailPage.tsx`

### F-C19: Sửa / Xóa đánh giá
- **Actor:** User (chủ đánh giá)
- **Frontend:** (UNVERIFIED — cần xác minh UI)
- **Backend:**
  - `PUT /api/v1/reviews/{reviewId}`
  - `DELETE /api/v1/reviews/{reviewId}`
- **Status:** `BACKEND_ONLY` (Backend có, Frontend cần xác minh)
- **Files:** `ReviewController.java`

### F-C20: Chat với Shop
- **Actor:** User
- **Frontend:** `FloatingChatWidget` (nổi góc phải) + nút "Chat ngay" trên `ProductDetailPage`
- **Backend REST:**
  - `POST /api/v1/chat/rooms` — tạo/lấy phòng chat
  - `GET /api/v1/chat/rooms` — danh sách phòng chat
  - `GET /api/v1/chat/rooms/{roomId}/messages` — lịch sử tin nhắn
  - `PUT /api/v1/chat/rooms/{roomId}/read` — đánh dấu đã đọc
- **Backend WebSocket:** STOMP `@MessageMapping("/chat.send")`
- **Status:** `IMPLEMENTED`
- **Files:** `ChatRoomController.java`, `ChatMessageController.java`, `ChatService.java`, `FloatingChatWidget.tsx`, `ChatContext.tsx`, `websocketService.ts`

### F-C21: Xem thông báo
- **Actor:** User
- **Frontend:** `NotificationDropdown` (polling 60s)
- **Backend:**
  - `GET /api/v1/notifications` — lấy thông báo chưa đọc
  - `GET /api/v1/notifications/unread-count`
  - `PUT /api/v1/notifications/{id}/read`
- **Status:** `IMPLEMENTED`
- **Files:** `NotificationController.java`, `NotificationDropdown` component

---

## NHÓM VENDOR

### F-V01: Đăng ký mở gian hàng
- **Actor:** User có ROLE_VENDOR
- **Frontend:** `VendorShopRegistrationPage`
- **Backend:** `POST /api/v1/shops`
- **Service:** `ShopService.createShop()`
- **Logic:** Tạo shop với status=PENDING, chờ Admin duyệt
- **Database:** INSERT `shops`
- **Status:** `IMPLEMENTED`
- **Files:** `ShopController.java`, `ShopServiceImpl.java`, `VendorShopRegistrationPage.tsx`

### F-V02: Xem trạng thái gian hàng
- **Actor:** Vendor
- **Frontend:** `ShopPendingPage`, `RequireShop` guard
- **Backend:** `GET /api/v1/shops/my`
- **Status:** `IMPLEMENTED`
- **Files:** `ShopController.java`, `ShopPendingPage.tsx`

### F-V03: Tạo sản phẩm
- **Actor:** Vendor (có ACTIVE shop)
- **Frontend:** `VendorProductFormPage` (`/vendor/products/new`)
- **Backend:** `POST /api/v1/products`
- **Service:** `ProductService.createProduct()`
- **Database:** INSERT `products`, INSERT `product_variants`, INSERT `product_images`
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `VendorProductController.java`, `VendorProductFormPage.tsx`

### F-V04: Upload ảnh sản phẩm (Cloudinary)
- **Actor:** Vendor
- **Frontend:** `VendorProductFormPage` (Dropzone)
- **Backend:** `POST /api/v1/media/upload`
- **Service:** `FileStorageService` (CloudinaryStorageServiceImpl)
- **Returns:** URL + publicId
- **Status:** `IMPLEMENTED`
- **Files:** `MediaController.java`, `CloudinaryStorageServiceImpl.java`, `VendorProductFormPage.tsx`

### F-V05: Sửa sản phẩm
- **Actor:** Vendor (chủ sản phẩm) hoặc Admin
- **Frontend:** `VendorProductFormPage` (`/vendor/products/:id/edit`)
- **Backend:** `PUT /api/v1/products/{id}`
- **Service:** `ProductService.updateProduct()`
- **Database:** UPDATE `products`, UPDATE `product_variants`, UPDATE `product_images`
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `VendorProductFormPage.tsx`

### F-V06: Xóa / Ẩn sản phẩm
- **Actor:** Vendor hoặc Admin
- **Frontend:** `VendorProductListPage`
- **Backend:** `DELETE /api/v1/products/{id}`
- **Service:** `ProductService.deleteProduct()`
- **Logic:** Soft delete — chuyển status → HIDDEN (không xóa hẳn)
- **Database:** UPDATE `products.status = 'HIDDEN'`
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `VendorProductListPage.tsx`

### F-V07: Xem danh sách sản phẩm của shop
- **Actor:** Vendor
- **Frontend:** `VendorProductListPage` (`/vendor/products`)
- **Backend:** `GET /api/v1/vendor/products`
- **Service:** (VendorProductController → ProductService)
- **Status:** `IMPLEMENTED`
- **Files:** `VendorProductController.java`, `VendorProductListPage.tsx`

### F-V08: Xem đơn hàng của shop
- **Actor:** Vendor
- **Frontend:** `VendorOrderListPage` (`/vendor/orders`)
- **Backend:** `GET /api/v1/vendor/orders?status=&page=&size=`
- **Service:** `OrderService.getVendorOrders()`
- **Security:** Chỉ thấy đơn của shop mình (qua `ShopInternalService.getShopIdByUserId`)
- **Status:** `IMPLEMENTED`
- **Files:** `VendorOrderController.java`, `VendorOrderListPage.tsx`

### F-V09: Cập nhật trạng thái đơn hàng
- **Actor:** Vendor
- **Frontend:** `VendorOrderListPage`
- **Backend:** `PATCH /api/v1/vendor/orders/{id}/status`
- **State Machine:** `CONFIRMED → SHIPPED → DELIVERED` hoặc `→ CANCELLED`
- **Events:** Publish Semantic Domain Events → `OrderNotificationListener`
- **Status:** `IMPLEMENTED`
- **Files:** `VendorOrderController.java`, `OrderServiceImpl.java`, `VendorOrderListPage.tsx`

### F-V10: Tạo voucher gian hàng
- **Actor:** Vendor
- **Frontend:** `VendorVoucherListPage` (`/vendor/vouchers`)
- **Backend:** `POST /api/v1/vouchers` (scope=SHOP, shopId từ user's shop)
- **Service:** `VoucherService.createVoucher()`
- **Database:** INSERT `vouchers`
- **Status:** `IMPLEMENTED`
- **Files:** `VoucherController.java`, `VoucherServiceImpl.java`, `VendorVoucherListPage.tsx`

### F-V11: Xem danh sách voucher của shop
- **Actor:** Vendor
- **Frontend:** `VendorVoucherListPage`
- **Backend:** `GET /api/v1/vouchers/my-shop`
- **Status:** `IMPLEMENTED`
- **Files:** `VoucherController.java`, `VendorVoucherListPage.tsx`

### F-V12: Trả lời đánh giá của khách hàng
- **Actor:** Vendor
- **Frontend:** (UNVERIFIED)
- **Backend:** `POST /api/v1/vendor/reviews/{id}/reply` (VendorReviewController)
- **Database:** UPDATE `reviews.vendor_reply`, UPDATE `reviews.vendor_replied_at`
- **Status:** `BACKEND_ONLY`
- **Files:** `VendorReviewController.java`, `ReviewServiceImpl.java`

### F-V13: Chat với khách hàng
- **Actor:** Vendor (qua FloatingChatWidget)
- **Frontend:** `FloatingChatWidget`
- **Status:** `IMPLEMENTED`
- **Files:** `FloatingChatWidget.tsx`, `ChatRoomController.java`

---

## NHÓM ADMIN

### F-A01: Xem danh sách gian hàng
- **Actor:** Admin
- **Frontend:** `AdminShopListPage` (`/admin/shop`)
- **Backend:** `GET /api/v1/admin/shops?status=&page=&size=`
- **Service:** `ShopService.getAdminShops()`
- **Database:** SELECT `shops`
- **Status:** `IMPLEMENTED`
- **Files:** `AdminShopController.java`, `AdminShopListPage.tsx`

### F-A02: Duyệt gian hàng
- **Actor:** Admin
- **Frontend:** `AdminShopListPage` (nút "Approve")
- **Backend:** `PUT /api/v1/admin/shops/{shopId}/approve`
- **Service:** `ShopService.approveShop()`
- **Event:** publish `ShopApprovedEvent` → email via `ShopNotificationListener`
- **Database:** UPDATE `shops.status = 'ACTIVE'`
- **Status:** `IMPLEMENTED`
- **Files:** `AdminShopController.java`, `ShopServiceImpl.java`, `AdminShopListPage.tsx`

### F-A03: Quản lý sản phẩm toàn hệ thống
- **Actor:** Admin
- **Frontend:** `AdminProductListPage`, `AdminProductFormPage`
- **Backend:** dùng chung `ProductController` (có role ADMIN)
- **Status:** `IMPLEMENTED`
- **Files:** `ProductController.java`, `AdminProductListPage.tsx`, `AdminProductFormPage.tsx`

### F-A04: Xem danh sách đơn hàng toàn hệ thống
- **Actor:** Admin
- **Frontend:** `AdminOrderListPage` (`/admin/orders`)
- **Backend:** `GET /api/v1/admin/orders`
- **Status:** `IMPLEMENTED`
- **Files:** `AdminOrderController.java`, `AdminOrderListPage.tsx`

### F-A05: Xem chi tiết đơn hàng (View-only)
- **Actor:** Admin
- **Frontend:** `AdminOrderDetailPage` (`/admin/orders/:id`)
- **Backend:** `GET /api/v1/admin/orders/{id}`
- **Status:** `IMPLEMENTED` (VIEW ONLY — không có mutation API)
- **Files:** `AdminOrderController.java`, `AdminOrderDetailPage.tsx`

### F-A06: Tạo voucher hệ thống
- **Actor:** Admin
- **Frontend:** `AdminVoucherListPage` (`/admin/vouchers`)
- **Backend:** `POST /api/v1/vouchers` (scope=SYSTEM)
- **Status:** `IMPLEMENTED`
- **Files:** `AdminVoucherController.java`, `VoucherController.java`, `AdminVoucherListPage.tsx`

### F-A07: Xem danh sách voucher hệ thống
- **Actor:** Admin
- **Frontend:** `AdminVoucherListPage`
- **Backend:** `GET /api/v1/admin/vouchers` (AdminVoucherController)
- **Status:** `PARTIALLY_IMPLEMENTED` (cần verify API)
- **Files:** `AdminVoucherController.java`, `AdminVoucherListPage.tsx`

### F-A08: Admin Dashboard
- **Actor:** Admin
- **Frontend:** `/admin` index → text placeholder "Tổng quan Dashboard"
- **Backend:** Không có API thống kê
- **Status:** `UI_ONLY`
- **Files:** `App.tsx` (inline JSX)

---

## NHÓM SYSTEM

### F-S01: JWT Authentication Filter
- **Purpose:** Xác thực mọi request qua JWT
- **Implementation:** `JwtAuthenticationFilter` (OncePerRequestFilter)
- **Status:** `IMPLEMENTED`
- **Files:** `JwtAuthenticationFilter.java`, `JwtService.java`, `SecurityConfig.java`

### F-S02: Global Exception Handler
- **Purpose:** Xử lý tập trung tất cả exceptions
- **Implementation:** `GlobalExceptionHandler` (`@RestControllerAdvice`)
- **Status:** `IMPLEMENTED`
- **Files:** `GlobalExceptionHandler.java`

### F-S03: Consistent API Response Format
- **Purpose:** Chuẩn hóa response: `{status, message, data}`
- **Implementation:** `ApiResponse<T>` generic wrapper
- **Status:** `IMPLEMENTED`
- **Files:** `ApiResponse.java`

### F-S04: Current User Injection
- **Purpose:** Inject `userId` vào Controller từ JWT token
- **Implementation:** `@CurrentUserId` annotation + `CurrentUserIdArgumentResolver`
- **Status:** `IMPLEMENTED`
- **Files:** `CurrentUserId.java`, `CurrentUserIdArgumentResolver.java`

### F-S05: WebSocket STOMP Configuration
- **Purpose:** Realtime chat endpoint
- **Implementation:** `WebSocketConfig.java`, `JwtChannelInterceptor.java`
- **Status:** `IMPLEMENTED`
- **Files:** `WebSocketConfig.java`, `JwtChannelInterceptor.java`

### F-S06: Email Service
- **Purpose:** Gửi email thông báo (Gmail SMTP)
- **Implementation:** `EmailService.java`, async via `AsyncConfig`
- **Trigger:** `ShopNotificationListener` (shop approval)
- **Status:** `IMPLEMENTED`
- **Files:** `EmailService.java`, `AsyncConfig.java`

### F-S07: Data Seeder
- **Purpose:** Seed dữ liệu mẫu ban đầu
- **Implementation:** `*DataInitializer` per module, `SeedDataIds` constants
- **Status:** `IMPLEMENTED`
- **Files:** `IdentityDataInitializer.java`, `ProductDataInitializer.java`, `ShopDataInitializer.java`, v.v.

### F-S08: OpenAPI Documentation
- **Purpose:** Swagger UI cho API documentation
- **Implementation:** Springdoc OpenAPI (`/swagger-ui.html`)
- **Status:** `IMPLEMENTED`
- **Files:** `OpenApiConfig.java`

### F-S09: N+1 Query Optimization
- **Purpose:** Tránh N+1 queries, tối ưu performance
- **Implementation:**
  - `@BatchSize(size=50)` trên `Order.items`, `Product.variants`, `Product.images`
  - Bulk internal API (e.g., `getProductIdsByVariantIds`)
  - `@Transactional(readOnly = true)` cho read methods
- **Verified by:** `NPlus1AuditTest` (integration tests)
- **Status:** `IMPLEMENTED`

### F-S10: Transaction Boundary Management
- **Purpose:** Đảm bảo tính toàn vẹn dữ liệu
- **Implementation:**
  - `@TransactionalEventListener(phase = AFTER_COMMIT)` cho Notification & Email
  - `Propagation.REQUIRES_NEW` cho notification listeners
  - `@Modifying(clearAutomatically = true)` cho bulk UPDATE
- **Verified by:** `TransactionAuditIntegrationTest` (26/26 tests pass)
- **Status:** `IMPLEMENTED`
