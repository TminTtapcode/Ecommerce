# REPORT_PROJECT_ANALYSIS.md
# Phân Tích Dự Án — Enterprise Ecommerce

> **Nguồn sự thật:** Source code thực tế (ưu tiên) > Database/Entities > ARCHITECTURE.md > PROJECT_CONTEXT.md

---

## 1. Tổng Quan Hệ Thống

| Thuộc tính | Giá trị |
|---|---|
| Tên dự án | Enterprise Ecommerce |
| Loại hệ thống | Multi-Vendor Ecommerce Platform |
| Group ID | `com.tmt` |
| Artifact ID | `ecommerce` |
| Phiên bản | `0.0.1-SNAPSHOT` |
| Kiến trúc | Modular Monolith |
| Mô hình triển khai | 1 Spring Boot Application + 1 React SPA |
| Database | MySQL (local, `ecommerce` schema) |
| Java version | 21 |
| Spring Boot | 4.1.0 |

**Mô tả:**
Nền tảng thương mại điện tử đa người bán (Multi-Vendor), cho phép người dùng đăng ký làm Vendor, mở gian hàng, đăng sản phẩm, và Buyer có thể duyệt sản phẩm, thêm vào giỏ hàng, thanh toán qua VNPay hoặc COD. Admin quản trị toàn bộ hệ thống.

---

## 2. Technology Stack

### Backend
| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| Java | 21 | Ngôn ngữ lập trình chính |
| Spring Boot | 4.1.0 | Framework chính |
| Spring Web MVC | (Spring Boot) | REST API |
| Spring Data JPA | (Spring Boot) | ORM / Persistence |
| Spring Security | (Spring Boot) | Authentication & Authorization |
| Spring WebSocket | (Spring Boot) | Realtime Chat (STOMP) |
| Spring Mail | 4.0.1 | Gửi email thông báo |
| Spring Actuator | (Spring Boot) | Health monitoring |
| Hibernate | (Spring Data JPA) | ORM Implementation |
| JWT (jjwt) | 0.12.5 | Token-based Authentication |
| Lombok | latest | Code generation |
| Bean Validation | (Spring Boot) | Input validation |
| Springdoc OpenAPI | 3.0.3 | API Documentation (Swagger UI) |
| Cloudinary SDK | 1.36.0 | Cloud image storage |
| MySQL Connector | latest | Database driver |
| Maven | - | Build tool |

### Database
| Công nghệ | Mục đích |
|---|---|
| MySQL | Cơ sở dữ liệu chính (quan hệ) |

**Lưu ý:** Redis được ghi trong kế hoạch nhưng **KHÔNG có** trong `pom.xml` — **CHƯA triển khai**. Docker/Docker Compose **CHƯA triển khai** (không có Dockerfile).

### Frontend
| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| React | 19.2.8 | UI Framework |
| TypeScript | ~6.0.2 | Type-safe JavaScript |
| Vite | 8.2.0 | Build tool |
| React Router DOM | 7.18.2 | Client-side routing |
| Axios | 1.19.0 | HTTP client |
| Tailwind CSS | 4.3.3 | Utility-first CSS |
| STOMP.js | 7.3.0 | WebSocket STOMP client |
| SockJS Client | 1.6.1 | WebSocket fallback |
| Lucide React | 1.31.0 | Icon library |

### External Services
| Service | Mục đích |
|---|---|
| Cloudinary | Lưu trữ ảnh sản phẩm, review |
| VNPay (Sandbox) | Cổng thanh toán trực tuyến |
| Gmail SMTP | Gửi email thông báo duyệt shop |

---

## 3. Kiến Trúc Hệ Thống

**Kiến trúc:** Modular Monolith

```
Client (React SPA)
       |
       | HTTP/HTTPS (REST API)
       | WebSocket (STOMP over SockJS)
       v
Spring Boot Application (Port: mặc định 8080)
       |
       +-- SecurityConfig (JWT Filter Chain)
       |
       +-- [identity]   AuthController, UserController
       +-- [shop]       ShopController, AdminShopController
       +-- [product]    ProductController, VendorProductController, CategoryController
       +-- [cart]       CartController
       +-- [order]      OrderController, VendorOrderController, AdminOrderController
       +-- [payment]    PaymentController (VNPay IPN + COD)
       +-- [review]     ReviewController, VendorReviewController
       +-- [voucher]    VoucherController, AdminVoucherController
       +-- [notification] NotificationController
       +-- [chat]       ChatRoomController, ChatMessageController (WebSocket)
       +-- [media]      MediaController (Cloudinary upload)
       |
       v
MySQL Database (ecommerce schema)
```

**Nguyên tắc module:**
- Mỗi module sở hữu: Entity, Repository, Service, Controller, DTO
- Cross-module communication qua Internal Service contracts (interface)
- Không trực tiếp inject Repository của module khác

**Ví dụ Internal Service pattern:**
- `ProductInternalService` → Cart, Order dùng để lấy thông tin sản phẩm
- `ShopInternalService` → Chat, Order dùng để lấy thông tin shop
- `OrderInternalService` → Payment dùng để lấy dữ liệu đơn hàng
- `VoucherInternalService` → Order dùng để tính toán giảm giá
- `CartInternalService` → Order dùng để lấy giỏ hàng

---

## 4. Backend Modules

| Module | Package | Controllers | Entities | Repository | Service |
|---|---|---|---|---|---|
| identity | `com.tmt.ecommerce.identity` | AuthController | User, Role | UserRepository, RoleRepository | AuthenticationService |
| shop | `com.tmt.ecommerce.shop` | ShopController, AdminShopController | Shop | ShopRepository | ShopService |
| product | `com.tmt.ecommerce.product` | ProductController, VendorProductController, CategoryController | Product, ProductVariant, ProductImage, Category, Brand | ProductRepository, ProductVariantRepository, CategoryRepository, BrandRepository | ProductService |
| cart | `com.tmt.ecommerce.cart` | CartController | Cart, CartItem | CartRepository | CartService |
| order | `com.tmt.ecommerce.order` | OrderController, VendorOrderController, AdminOrderController | Order, OrderItem | OrderRepository, OrderItemRepository | OrderService |
| payment | `com.tmt.ecommerce.payment` | PaymentController | Payment | PaymentRepository | PaymentGatewayService |
| review | `com.tmt.ecommerce.review` | ReviewController, VendorReviewController | Review | ReviewRepository | ReviewService |
| voucher | `com.tmt.ecommerce.voucher` | VoucherController, AdminVoucherController | Voucher | VoucherRepository | VoucherService |
| notification | `com.tmt.ecommerce.notification` | NotificationController | Notification | NotificationRepository | NotificationService |
| chat | `com.tmt.ecommerce.chat` | ChatRoomController, ChatMessageController | ChatRoom, ChatMessage | ChatRoomRepository, ChatMessageRepository | ChatService |
| media | `com.tmt.ecommerce.media` | MediaController | - | - | FileStorageService (Cloudinary) |
| common | `com.tmt.ecommerce.common` | GlobalExceptionHandler | - | - | - |
| infrastructure | `com.tmt.ecommerce.infrastructure` | - | - | - | EmailService, AsyncConfig |

**Event-Driven (Spring Application Events):**
- `OrderConfirmedEvent`, `OrderShippedEvent`, `OrderDeliveredEvent`, `OrderCancelledEvent` → `OrderNotificationListener`
- `ReviewCreatedEvent` → `ReviewNotificationListener`
- `ShopApprovedEvent` → `ShopNotificationListener` (gửi email)

**Payment Strategy Pattern:**
- `PaymentStrategy` (interface) → `VnPayStrategy`, `CodStrategy`
- `PaymentStrategyFactory` → chọn strategy dựa trên PaymentMethod

---

## 5. Frontend Modules

| Trang | Path | Role | Status |
|---|---|---|---|
| ProductListPage | `/products` | Public | IMPLEMENTED |
| ProductDetailPage | `/products/:id` | Public | IMPLEMENTED |
| LoginPage | `/login` | Public | IMPLEMENTED |
| RegisterPage | `/register` | Public | IMPLEMENTED |
| CartPage | `/cart` | User | IMPLEMENTED |
| CheckoutPage | `/checkout` | User | IMPLEMENTED |
| PaymentResultPage | `/payment-result` | User | IMPLEMENTED |
| OrderHistoryPage | `/orders` | User | IMPLEMENTED |
| OrderDetailPage | `/orders/:id` | User | IMPLEMENTED |
| ProfilePage | `/profile` | User | IMPLEMENTED |
| ChatTestPage | `/chat-test` | User | IMPLEMENTED |
| VendorProductListPage | `/vendor/products` | Vendor | IMPLEMENTED |
| VendorProductFormPage | `/vendor/products/new`, `/vendor/products/:id/edit` | Vendor | IMPLEMENTED |
| VendorOrderListPage | `/vendor/orders` | Vendor | IMPLEMENTED |
| VendorVoucherListPage | `/vendor/vouchers` | Vendor | IMPLEMENTED |
| ShopPendingPage | (khi shop chờ duyệt) | Vendor | IMPLEMENTED |
| VendorShopRegistrationPage | (đăng ký shop) | Vendor | IMPLEMENTED |
| AdminProductListPage | `/admin/products` | Admin | IMPLEMENTED |
| AdminProductFormPage | `/admin/products/create`, `/admin/products/:id/edit` | Admin | IMPLEMENTED |
| AdminShopListPage | `/admin/shop` | Admin | IMPLEMENTED |
| AdminOrderListPage | `/admin/orders` | Admin | IMPLEMENTED |
| AdminOrderDetailPage | `/admin/orders/:id` | Admin | IMPLEMENTED |
| AdminVoucherListPage | `/admin/vouchers` | Admin | IMPLEMENTED |
| Admin Dashboard | `/admin` (index) | Admin | UI_ONLY (chỉ text "Tổng quan Dashboard") |

**Frontend Components (quan trọng):**
- `FloatingChatWidget` — Widget chat nổi Shopee-style (góc phải màn hình)
- `NotificationDropdown` — Dropdown thông báo (polling 60s)
- `PublicLayout` — Layout chính cho User/Vendor
- `AdminLayout` — Layout riêng cho Admin
- `ProtectedRoute` / `RequireShop` — Route guards

**Frontend State Management:**
- `AuthContext` — quản lý JWT, user info
- `CartContext` — quản lý giỏ hàng local + sync API
- `ChatContext` / `useChat` — quản lý WebSocket, rooms, messages, unread count

---

## 6. Database

### Tất cả các bảng được xác minh qua Entity classes:

| Bảng | Entity | Module |
|---|---|---|
| `users` | User | identity |
| `roles` | Role | identity |
| `user_roles` | (join table) | identity |
| `shops` | Shop | shop |
| `categories` | Category | product |
| `brands` | Brand | product |
| `products` | Product | product |
| `product_variants` | ProductVariant | product |
| `product_images` | ProductImage | product |
| `carts` | Cart | cart |
| `cart_items` | CartItem | cart |
| `orders` | Order | order |
| `order_items` | OrderItem | order |
| `payments` | Payment | payment |
| `reviews` | Review | review |
| `review_images` | (ElementCollection trong Review) | review |
| `vouchers` | Voucher | voucher |
| `notifications` | Notification | notification |
| `chat_rooms` | ChatRoom | chat |
| `chat_messages` | ChatMessage | chat |

**Tổng: 20 bảng** (18 entity table + 1 join table `user_roles` + 1 element collection `review_images`)

---

## 7. Authentication

**Cơ chế:** JWT (JSON Web Token) — IMPLEMENTED

**Flow:**
1. `POST /api/v1/auth/register` → tạo User, gán ROLE_USER
2. `POST /api/v1/auth/login` → xác thực, trả về JWT token
3. Client gửi token qua `Authorization: Bearer <token>`
4. `JwtAuthenticationFilter` xác thực token mọi request
5. `CustomUserDetails` load user từ DB

**Token config:**
- Expiration: 86400000ms = 24 giờ
- Secret key: từ env var `JWT_SECRET_KEY`

**WebSocket Auth:**
- HTTP Handshake `/ws/**`: permitAll (không block)
- STOMP CONNECT: `JwtChannelInterceptor` kiểm tra Bearer JWT → gán `StompPrincipal`

---

## 8. Authorization (Phân Quyền)

**3 roles trong hệ thống:**
| Role | Quyền |
|---|---|
| `ROLE_USER` (Buyer) | Duyệt sản phẩm, giỏ hàng, đặt hàng, đánh giá, chat |
| `ROLE_VENDOR` | Tất cả quyền USER + quản lý sản phẩm, đơn hàng shop, voucher shop |
| `ROLE_ADMIN` | Tất cả quyền + quản lý shop (duyệt/từ chối), quản lý toàn bộ hệ thống |

**Phân quyền API (xác minh từ Controller annotations):**
- `@PreAuthorize("hasRole('ADMIN')")` — admin endpoints
- `@PreAuthorize("hasAnyRole('ADMIN', 'VENDOR')")` — product, voucher creation
- `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")` — vendor order management
- `@PreAuthorize("hasRole('USER')")` — review creation, update
- Không có annotation → mặc định theo SecurityConfig

**Frontend Route Guard:**
- `ProtectedRoute(requireAdmin=true)` → Admin routes
- `ProtectedRoute(requireVendor=true)` → Vendor routes
- `RequireShop` — chuyển hướng đến đăng ký shop nếu chưa có

---

## 9. Customer Features (Buyer)

| Feature | Backend API | Frontend | Status |
|---|---|---|---|
| Duyệt danh sách sản phẩm (search, phân trang) | `GET /api/v1/products?keyword=&page=&size=` | ProductListPage | IMPLEMENTED |
| Xem chi tiết sản phẩm | `GET /api/v1/products/{id}` | ProductDetailPage | IMPLEMENTED |
| Đăng ký tài khoản | `POST /api/v1/auth/register` | RegisterPage | IMPLEMENTED |
| Đăng nhập | `POST /api/v1/auth/login` | LoginPage | IMPLEMENTED |
| Xem giỏ hàng | `GET /api/v1/carts` | CartPage | IMPLEMENTED |
| Thêm vào giỏ hàng | `POST /api/v1/carts/items` | ProductDetailPage | IMPLEMENTED |
| Cập nhật số lượng | `PUT /api/v1/carts/items/{itemId}` | CartPage | IMPLEMENTED |
| Xóa sản phẩm khỏi giỏ | `DELETE /api/v1/carts/items/{itemId}` | CartPage | IMPLEMENTED |
| Preview checkout (tính giảm giá) | `POST /api/v1/orders/preview` | CheckoutPage | IMPLEMENTED |
| Checkout / Tạo đơn hàng | `POST /api/v1/orders/checkout` | CheckoutPage | IMPLEMENTED |
| Áp dụng voucher | (trong checkout request) | CheckoutPage | IMPLEMENTED |
| Thanh toán VNPay | `POST /api/v1/payments/create` | CheckoutPage → redirect | IMPLEMENTED |
| Thanh toán COD | (trong checkout request) | CheckoutPage | IMPLEMENTED |
| Xem kết quả thanh toán (polling) | `GET /api/v1/payments/group/{id}/status` | PaymentResultPage | IMPLEMENTED |
| Lịch sử đơn hàng | `GET /api/v1/orders?status=&page=&size=` | OrderHistoryPage | IMPLEMENTED |
| Chi tiết đơn hàng | `GET /api/v1/orders/{id}` | OrderDetailPage | IMPLEMENTED |
| Viết đánh giá sản phẩm | `POST /api/v1/reviews` | ProductDetailPage | IMPLEMENTED |
| Xem đánh giá sản phẩm | `GET /api/v1/products/{id}/reviews` | ProductDetailPage | IMPLEMENTED |
| Xem tóm tắt rating | `GET /api/v1/products/{id}/reviews/summary` | ProductDetailPage | IMPLEMENTED |
| Chat với shop | WebSocket STOMP + REST | FloatingChatWidget | IMPLEMENTED |
| Xem thông báo | `GET /api/v1/notifications` (polling 60s) | NotificationDropdown | IMPLEMENTED |
| Profile người dùng | (ProfilePage) | ProfilePage | UI_ONLY (UNVERIFIED — chưa kiểm tra API) |

---

## 10. Vendor Features

| Feature | Backend API | Frontend | Status |
|---|---|---|---|
| Đăng ký gian hàng | `POST /api/v1/shops` | VendorShopRegistrationPage | IMPLEMENTED |
| Xem trạng thái shop (pending/active) | `GET /api/v1/shops/my` | ShopPendingPage | IMPLEMENTED |
| Tạo sản phẩm | `POST /api/v1/products` | VendorProductFormPage | IMPLEMENTED |
| Sửa sản phẩm | `PUT /api/v1/products/{id}` | VendorProductFormPage | IMPLEMENTED |
| Xóa/ẩn sản phẩm | `DELETE /api/v1/products/{id}` | VendorProductListPage | IMPLEMENTED |
| Upload ảnh sản phẩm (Cloudinary) | `POST /api/v1/media/upload` | VendorProductFormPage | IMPLEMENTED |
| Quản lý danh sách sản phẩm của shop | `GET /api/v1/vendor/products` | VendorProductListPage | IMPLEMENTED |
| Xem đơn hàng của shop | `GET /api/v1/vendor/orders` | VendorOrderListPage | IMPLEMENTED |
| Cập nhật trạng thái đơn hàng | `PATCH /api/v1/vendor/orders/{id}/status` | VendorOrderListPage | IMPLEMENTED |
| Tạo voucher shop | `POST /api/v1/vouchers` | VendorVoucherListPage | IMPLEMENTED |
| Xem voucher của shop | `GET /api/v1/vouchers/my-shop` | VendorVoucherListPage | IMPLEMENTED |
| Trả lời đánh giá | `POST /api/v1/vendor/reviews/{id}/reply` | (UNVERIFIED - controller exists) | BACKEND_ONLY |
| Chat với khách hàng | WebSocket STOMP | FloatingChatWidget | IMPLEMENTED |

---

## 11. Admin Features

| Feature | Backend API | Frontend | Status |
|---|---|---|---|
| Xem danh sách shop (filter, phân trang) | `GET /api/v1/admin/shops?status=` | AdminShopListPage | IMPLEMENTED |
| Duyệt shop | `PUT /api/v1/admin/shops/{id}/approve` | AdminShopListPage | IMPLEMENTED |
| Quản lý sản phẩm (tất cả) | (dùng chung ProductController) | AdminProductListPage, AdminProductFormPage | IMPLEMENTED |
| Quản lý đơn hàng (view-only) | `GET /api/v1/admin/orders` | AdminOrderListPage, AdminOrderDetailPage | IMPLEMENTED |
| Tạo voucher hệ thống | `POST /api/v1/vouchers` (scope=SYSTEM) | AdminVoucherListPage | IMPLEMENTED |
| Xem voucher hệ thống | (UNVERIFIED - không thấy GET admin vouchers) | AdminVoucherListPage | PARTIALLY_IMPLEMENTED |
| Admin cập nhật trạng thái đơn | (KHÔNG có API) | AdminOrderDetailPage | UI_ONLY (VIEW ONLY) |
| Admin Dashboard thống kê | (KHÔNG có API) | `/admin` index | UI_ONLY |

---

## 12. Payment

**Providers được triển khai:**
- **VNPay** — Cổng thanh toán online (Sandbox environment)
  - URL: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html`
  - Return URL: `http://localhost:5173/payment-result`
  - IPN Endpoint: `GET /api/v1/payments/vnpay/ipn`
  - **Idempotency:** Pessimistic Lock (`PESSIMISTIC_WRITE`) trên PaymentRepository
- **COD** — Thanh toán khi nhận hàng (không cần redirect)

**Multi-vendor payment flow:**
- 1 Checkout → N Orders (mỗi Shop 1 Order) → 1 `payment_group_id` (UUID)
- 1 `payment_group_id` → N `Payment` records (mỗi lần retry 1 Payment mới)
- Payment SUCCESS → tất cả Orders trong group → `CONFIRMED`
- Payment FAILED → Orders giữ `PENDING` (cho phép retry)

**Status:** IMPLEMENTED

---

## 13. Voucher

**Hai loại scope:**
- `SYSTEM` — Admin tạo, áp dụng cho toàn hệ thống
- `SHOP` — Vendor tạo, chỉ áp dụng cho shop đó

**Hai loại type:**
- `PERCENTAGE` — Giảm theo phần trăm (có `maxDiscount` cap)
- `FIXED_AMOUNT` — Giảm cố định (VND)

**Tính năng đặc biệt:**
- Multi-vendor proportional split: `SYSTEM` voucher phân bổ theo tỉ lệ subtotal mỗi shop
- Atomic update: `UPDATE vouchers SET used_count = used_count + 1 WHERE ...`
- Điều kiện: `minOrderValue`, `usageLimit`, `startDate`, `endDate`

**Status:** IMPLEMENTED (Backend + Frontend)

---

## 14. Media

**Provider:** Cloudinary

**Chức năng:**
- Upload ảnh: `POST /api/v1/media/upload` → trả về URL + publicId
- Xóa ảnh: xử lý qua `publicId` trong DB (Zero-Trust: không tin client)
- TransactionSynchronizationManager `afterCommit` → tách xóa Cloudinary khỏi MySQL transaction

**Status:** IMPLEMENTED

---

## 15. Notification

**Cơ chế:** Event-Driven + REST polling

**Events được handle:**
- `OrderConfirmedEvent` → thông báo "Đơn hàng đã xác nhận"
- `OrderShippedEvent` → thông báo "Đơn hàng đang giao"
- `OrderDeliveredEvent` → thông báo "Đơn hàng đã giao"
- `OrderCancelledEvent` → thông báo "Đơn hàng đã hủy"
- `ReviewCreatedEvent` → thông báo cho vendor
- `ShopApprovedEvent` → gửi email (Gmail SMTP)

**Frontend:** NotificationDropdown với polling 60s (REST), đọc/xóa thông báo

**Status:** IMPLEMENTED (REST polling — WebSocket push PENDING)

---

## 16. Chat (Realtime)

**Giao thức:** WebSocket STOMP over SockJS

**Backend:**
- Endpoint: `/ws`
- STOMP destination: `@MessageMapping("/chat.send")`
- P2P routing: `convertAndSendToUser(userId, "/queue/messages", msg)`
- REST: tạo phòng, lấy lịch sử tin nhắn, đánh dấu đã đọc

**Frontend:**
- `websocketService` — Singleton STOMP client, auto-reconnect
- `FloatingChatWidget` — Widget nổi góc phải, Shopee-style
- Optimistic UI — tin nhắn hiển thị ngay (0ms latency), ID âm tạm thời

**Status:** IMPLEMENTED (E2E verified)

---

## 17. Deployment

**Hiện tại:** Local development only
- Backend: `localhost:8080`
- Frontend: `localhost:5173` (Vite dev server)
- Database: `localhost:3306` MySQL

**Không có:**
- Docker / Docker Compose — **CHƯA triển khai**
- Production deployment — **CHƯA triển khai**
- Redis — **CHƯA triển khai**
- Cloud hosting (AWS) — **CHƯA triển khai**

---

## 18. Các Feature Đã Hoàn Thành

✅ Authentication (Register, Login, JWT)
✅ Role-based Authorization (USER, VENDOR, ADMIN)
✅ Product Catalog (CRUD, tìm kiếm, phân trang)
✅ Category tree (category parent/child)
✅ Brand management
✅ Product Variants (size/color với JSON attributes)
✅ Product Images (Cloudinary, thumbnail, sort order)
✅ Shop Management (đăng ký, duyệt, PENDING/ACTIVE)
✅ Cart Management (thêm, xóa, cập nhật số lượng)
✅ Multi-vendor Checkout (tách đơn per shop)
✅ Order Management (tạo, xem lịch sử, chi tiết)
✅ Order Status Machine (PENDING → CONFIRMED → SHIPPED → DELIVERED/CANCELLED)
✅ Vendor Order Management (xem + cập nhật trạng thái)
✅ VNPay Integration (redirect + IPN webhook)
✅ COD Payment
✅ Payment Idempotency (Pessimistic Lock)
✅ Payment Retry (PENDING orders cho retry)
✅ Voucher System (SYSTEM + SHOP, PERCENTAGE + FIXED_AMOUNT)
✅ Multi-vendor voucher proportional split
✅ Review System (tạo, sửa, xóa, rating summary)
✅ Vendor Reply to Review
✅ Review Eligibility Check (phải mua hàng mới được review)
✅ Event-Driven Notification (AFTER_COMMIT)
✅ Email Notification (shop approval)
✅ Frontend Notification Dropdown (60s polling)
✅ Realtime Chat (WebSocket STOMP, E2E verified)
✅ Chat Widget (Shopee-style floating)
✅ Optimistic UI for Chat
✅ Media Upload (Cloudinary)
✅ N+1 Query Optimization (BatchSize, bulk queries)
✅ Database Indexes (orders, products, reviews, notifications, payments, chat_messages)
✅ Transaction Boundaries (AFTER_COMMIT, readOnly, rollbackFor audit)
✅ Admin Shop Approval
✅ Admin Order View
✅ Admin Voucher Management (SYSTEM scope)
✅ OpenAPI / Swagger UI (`/swagger-ui.html`)

---

## 19. Các Feature Chưa Hoàn Thành

❌ **Redis** — Kế hoạch caching chưa triển khai
❌ **Docker / Docker Compose** — Chưa có Dockerfile
❌ **Production Deployment** (AWS) — Chưa triển khai
❌ **WebSocket Notification Push** — Hiện dùng polling 60s
❌ **Admin Dashboard Statistics** — Frontend có placeholder, không có API data
❌ **Admin Order Status Mutation** — View-only, không có API cập nhật từ Admin
❌ **Inventory refund on cancellation** — Tồn kho chưa được hoàn trả khi vendor hủy đơn
❌ **Admin Ban/Unban Shop** — Chỉ có Approve, chưa có Ban
❌ **Frontend Profile Page** — UI tồn tại nhưng chức năng cần xác minh thêm

---

## 20. Các Vấn Đề Cần Đưa Vào Hạn Chế

1. **Không có Redis Cache** — Chưa implement caching, mọi query đều hit DB
2. **Notification là REST polling** — Chưa dùng WebSocket push cho notification, độ trễ tối đa 60s
3. **Chưa có Docker/Deployment** — Chỉ chạy được ở local development
4. **Admin Order Management View-Only** — Admin không thể thay đổi trạng thái đơn hàng
5. **Inventory không hoàn trả khi hủy đơn** — Technical debt đã ghi nhận
6. **Chưa có order cancellation flow đầy đủ** — Buyer không thể tự hủy đơn qua Frontend
7. **ProfilePage chưa đầy đủ** — Chưa có API update profile
8. **Chưa có search nâng cao** — Chỉ tìm kiếm theo keyword, chưa có filter theo category, brand, price range
9. **Không có unit test đầy đủ** — Chỉ có integration tests cho một số flow quan trọng
10. **Error code chưa chuẩn hóa** — Một số error trả về text tiếng Việt thay vì error code chuẩn
