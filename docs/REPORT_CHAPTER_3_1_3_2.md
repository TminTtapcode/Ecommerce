# CHƯƠNG 3. XÂY DỰNG HỆ THỐNG

---

## 3.1. Giới thiệu hệ thống

### 3.1.1. Mục đích hệ thống

Hệ thống **Enterprise Ecommerce** là một nền tảng thương mại điện tử đa người bán (Multi-Vendor Marketplace) được xây dựng từ đầu (from scratch) với mục đích kép:

1. **Mục đích nghiệp vụ:** Cung cấp một nền tảng trực tuyến cho phép nhiều người bán (Vendor) kinh doanh sản phẩm, trong khi người mua (Buyer) có thể tìm kiếm, đặt hàng và thanh toán trong một lần duy nhất cho dù sản phẩm đến từ nhiều gian hàng khác nhau.

2. **Mục đích kỹ thuật:** Trình diễn và thực hành các nguyên tắc phát triển phần mềm backend hiện đại ở quy mô hệ thống thực tế, bao gồm kiến trúc Modular Monolith, quản lý giao dịch phức tạp, tích hợp cổng thanh toán, xây dựng hệ thống realtime, và tối ưu hóa truy vấn cơ sở dữ liệu.

### 3.1.2. Vấn đề hệ thống giải quyết

Hệ thống được xây dựng để giải quyết các vấn đề đặc trưng của một sàn giao dịch trực tuyến đa người bán:

**Vấn đề 1 — Tách đơn hàng đa người bán:**
Khi người mua chọn sản phẩm từ nhiều gian hàng khác nhau trong cùng một lần mua, hệ thống phải tự động tách thành N đơn hàng riêng biệt (mỗi gian hàng một đơn) nhưng vẫn xử lý một lần thanh toán duy nhất cho người mua. Mỗi nhóm đơn hàng được liên kết bởi `payment_group_id` chung.

**Vấn đề 2 — Kiểm soát quyền đa vai trò:**
Ba nhóm người dùng có quyền hạn hoàn toàn tách biệt. Vendor chỉ xem và xử lý đơn hàng của gian hàng mình. Admin có quyền duyệt gian hàng và quản lý toàn hệ thống. Buyer chỉ thao tác trong phạm vi tài khoản của mình.

**Vấn đề 3 — Tính toàn vẹn giao dịch trong checkout:**
Quá trình đặt hàng bao gồm nhiều thao tác ghi cần thực hiện nguyên tử (trừ tồn kho, cập nhật voucher, tạo đơn hàng, xóa giỏ hàng). Thất bại ở bất kỳ bước nào phải rollback toàn bộ.

**Vấn đề 4 — Idempotency trong xử lý webhook thanh toán:**
VNPay có thể gửi nhiều IPN request đồng thời cho cùng giao dịch. Hệ thống phải đảm bảo chỉ xử lý một lần, tránh cập nhật đơn hàng trùng lặp — được giải quyết bằng Pessimistic Lock.

**Vấn đề 5 — Giao tiếp thời gian thực:**
Chức năng chat giữa người mua và người bán yêu cầu giao tiếp hai chiều tức thời, không thể thực hiện qua HTTP request-response thông thường.

**Vấn đề 6 — Phân bổ giảm giá công bằng:**
Voucher cấp hệ thống (SYSTEM) phải được phân bổ theo tỷ lệ giá trị cho từng gian hàng khi đơn hàng bao gồm nhiều người bán.

### 3.1.3. Các nhóm người dùng

Hệ thống phục vụ ba nhóm người dùng với vai trò và quyền hạn khác nhau:

**Bảng 3.1.** Các nhóm người dùng và vai trò trong hệ thống

| Nhóm | Vai trò (Role) | Mô tả | Quyền hạn chính |
|---|---|---|---|
| **Người mua (Buyer)** | `ROLE_USER` | Người dùng đã đăng ký, chưa có gian hàng | Duyệt sản phẩm, giỏ hàng, đặt hàng, thanh toán, đánh giá, chat |
| **Người bán (Vendor)** | `ROLE_VENDOR` | Người dùng đã được duyệt gian hàng | Quản lý sản phẩm, đơn hàng gian hàng mình, voucher shop, chat |
| **Quản trị viên (Admin)** | `ROLE_ADMIN` | Tài khoản quản trị hệ thống | Duyệt gian hàng, quản lý toàn bộ sản phẩm/đơn hàng, voucher hệ thống |

**Lưu ý:** Một tài khoản có thể giữ đồng thời `ROLE_USER` và `ROLE_VENDOR`. Người bán vẫn có thể mua sản phẩm với tư cách Buyer.

### 3.1.4. Chức năng chính

**Bảng 3.2.** Danh sách chức năng chính theo nhóm người dùng

| Nhóm | Chức năng |
|---|---|
| **Chung** | Đăng ký, đăng nhập, JWT, xem sản phẩm, tìm kiếm |
| **Buyer** | Quản lý giỏ hàng; Checkout đa gian hàng; Thanh toán VNPay / COD; Xem lịch sử và trạng thái đơn; Đánh giá sản phẩm sau khi nhận; Nhận thông báo; Chat với người bán |
| **Vendor** | Đăng ký và quản lý gian hàng; CRUD sản phẩm với ảnh Cloudinary; Xem và cập nhật trạng thái đơn hàng gian hàng mình; Quản lý voucher gian hàng; Chat với khách hàng |
| **Admin** | Duyệt / từ chối gian hàng; Quản lý sản phẩm toàn hệ thống; Xem đơn hàng toàn hệ thống; Quản lý voucher hệ thống |
| **Hệ thống** | Phân quyền theo vai trò; Thông báo tự động theo sự kiện; Tách đơn đa người bán; IPN Webhook idempotency; API documentation (Swagger UI) |

### 3.1.5. Phạm vi hệ thống

**Trong phạm vi:**
- Nền tảng web (trình duyệt) — không có ứng dụng di động.
- Môi trường phát triển local: Backend `localhost:8080`, Frontend `localhost:5173`.
- Tích hợp VNPay Sandbox (không giao dịch tiền thực).
- Lưu trữ ảnh trên Cloudinary (cloud, không lưu local).

**Ngoài phạm vi:**
- Redis caching, Docker deployment, triển khai AWS.
- WebSocket push notification (hiện dùng REST polling 60 giây).
- Hoàn trả tồn kho khi hủy đơn, tìm kiếm nâng cao (filter nhiều tiêu chí).

---

## 3.2. Kiến trúc hệ thống

### 3.2.1. Kiến trúc tổng thể

Hệ thống Enterprise Ecommerce sử dụng kiến trúc **Modular Monolith** — toàn bộ backend được triển khai như một Spring Boot application duy nhất, nhưng mã nguồn được tổ chức nội bộ thành các module nghiệp vụ độc lập, giao tiếp với nhau qua các hợp đồng nội bộ (Internal Service contracts) được định nghĩa rõ ràng.

**Hình 3.1.** Sơ đồ kiến trúc tổng thể hệ thống Enterprise Ecommerce

```
┌─────────────────────────────────────────────────────────────────────────┐
│                           CLIENT TIER                                   │
│                                                                         │
│  ┌───────────────────────────────────────────────────────────────────┐  │
│  │              React SPA (Vite + TypeScript)                        │  │
│  │                  localhost:5173                                   │  │
│  │                                                                   │  │
│  │   ┌──────────────┐  ┌──────────────┐  ┌──────────────────────┐   │  │
│  │   │ Public Pages │  │ Vendor Pages │  │    Admin Pages       │   │  │
│  │   │  (Buyer)     │  │  (Shop Mgr)  │  │   (Admin Panel)      │   │  │
│  │   └──────────────┘  └──────────────┘  └──────────────────────┘   │  │
│  │                                                                   │  │
│  │   ┌───────────────┐              ┌───────────────────────────┐   │  │
│  │   │  Axios HTTP   │              │  STOMP.js + SockJS        │   │  │
│  │   │  Client       │              │  WebSocket Client         │   │  │
│  │   └───────┬───────┘              └─────────────┬─────────────┘   │  │
│  └───────────┼─────────────────────────────────────┼───────────────-┘  │
└──────────────┼─────────────────────────────────────┼────────────────────┘
               │ REST API (HTTP/HTTPS)                │ WebSocket (WS)
               │ Port 8080                            │ Port 8080 /ws
               ▼                                      ▼
┌─────────────────────────────────────────────────────────────────────────┐
│                        SERVER TIER (Spring Boot 4.1.0)                  │
│                           localhost:8080                                │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                    COMMON LAYER                                  │   │
│  │  ┌────────────────────┐  ┌──────────────────┐  ┌────────────┐  │   │
│  │  │  Spring Security   │  │  JwtAuthFilter   │  │ GlobalExc  │  │   │
│  │  │  (Filter Chain)    │  │  + JwtService    │  │ Handler    │  │   │
│  │  └────────────────────┘  └──────────────────┘  └────────────┘  │   │
│  │  ┌────────────────────┐  ┌──────────────────┐                  │   │
│  │  │  AsyncConfig       │  │ WebMvcConfig     │                  │   │
│  │  │  (@Async pool)     │  │ (CORS, resolvers)│                  │   │
│  │  └────────────────────┘  └──────────────────┘                  │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌─────────────────────────────────────────────────────────────────┐   │
│  │                   BUSINESS MODULES                               │   │
│  │                                                                  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │   │
│  │  │ identity │ │ product  │ │   shop   │ │      cart        │   │   │
│  │  │(auth+usr)│ │(cat+prod)│ │          │ │                  │   │   │
│  │  └──────────┘ └──────────┘ └──────────┘ └──────────────────┘   │   │
│  │                                                                  │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────────────┐   │   │
│  │  │  order   │ │ payment  │ │  review  │ │    notification  │   │   │
│  │  │          │ │(VNPay+   │ │          │ │  (Event-Driven)  │   │   │
│  │  └──────────┘ │  COD)    │ └──────────┘ └──────────────────┘   │   │
│  │               └──────────┘                                      │   │
│  │  ┌──────────┐ ┌──────────┐ ┌──────────────────────────────┐    │   │
│  │  │   chat   │ │ voucher  │ │      media                   │    │   │
│  │  │(STOMP WS)│ │          │ │   (Cloudinary)               │    │   │
│  │  └──────────┘ └──────────┘ └──────────────────────────────┘    │   │
│  └─────────────────────────────────────────────────────────────────┘   │
│                                                                         │
│  ┌────────────────────────────────────────────────────────────────-┐   │
│  │                  CROSS-MODULE CONTRACTS                          │   │
│  │  ProductInternalService │ ShopInternalService │ OrderInternal   │   │
│  │  VoucherInternalService │ IdentityInternal    │ ...             │   │
│  └─────────────────────────────────────────────────────────────────┘   │
└─────────────────────────────────────────────────────────────────────────┘
               │                                      │
               ▼                                      ▼
┌──────────────────────────────┐   ┌──────────────────────────────────────┐
│        DATA TIER              │   │         EXTERNAL SERVICES            │
│                              │   │                                      │
│  ┌──────────────────────┐    │   │  ┌──────────┐  ┌─────────────────┐  │
│  │       MySQL          │    │   │  │  VNPay   │  │   Cloudinary    │  │
│  │  localhost:3306      │    │   │  │ Sandbox  │  │  (CDN Images)   │  │
│  │  Schema: ecommerce   │    │   │  │  (IPN    │  │                 │  │
│  │  20 Tables           │    │   │  │  Webhook)│  └─────────────────┘  │
│  └──────────────────────┘    │   │  └──────────┘                       │
│                              │   │  ┌────────────────────────────────┐  │
│                              │   │  │   Gmail SMTP (Notification)   │  │
│                              │   │  └────────────────────────────────┘  │
└──────────────────────────────┘   └──────────────────────────────────────┘
```

**Giải thích kiến trúc ba tầng:**

- **Client Tier:** React SPA với ba layout riêng biệt (Public/Buyer, Vendor, Admin). Giao tiếp với backend qua hai kênh: REST API (Axios HTTP) và WebSocket STOMP (cho chat realtime).
- **Server Tier:** Spring Boot Modular Monolith. Tất cả module được deploy trong cùng một JVM process. Không có network call giữa các module — giao tiếp qua Java interface (Internal Service contracts).
- **Data Tier + External Services:** MySQL là RDBMS duy nhất. Ba dịch vụ external: VNPay (payment gateway), Cloudinary (image storage), Gmail SMTP (email).

**Tại sao chọn Modular Monolith thay vì Microservices:**

Microservices phù hợp với hệ thống quy mô lớn, đội phát triển lớn, và có yêu cầu scale độc lập từng service. Với hệ thống này (đội phát triển nhỏ, phạm vi có kiểm soát), Modular Monolith cho phép:
- Ranh giới nghiệp vụ rõ ràng (giống Microservices) mà không có chi phí vận hành (service discovery, distributed transaction, network latency).
- Giao dịch ACID trên nhiều module trong cùng một transaction boundary (không thể thực hiện với Microservices thuần túy).
- Triển khai đơn giản: một Spring Boot JAR.

### 3.2.2. Các thành phần của kiến trúc

#### 3.2.2.1. Common Layer (Tầng dùng chung)

Common Layer chứa các thành phần kỹ thuật dùng chung toàn hệ thống, không thuộc bất kỳ module nghiệp vụ cụ thể nào.

**Bảng 3.3.** Các thành phần của Common Layer

| Thành phần | Lớp/Class | Vai trò |
|---|---|---|
| **Security Filter Chain** | `SecurityConfig` | Cấu hình Spring Security, định nghĩa endpoint public/protected, thêm `JwtAuthenticationFilter` |
| **JWT Authentication Filter** | `JwtAuthenticationFilter` | Trích xuất JWT từ `Authorization` header, xác minh signature, gán `SecurityContext` |
| **JWT Service** | `JwtService` | Tạo JWT (24h), xác minh, trích xuất claims (userId, roles) |
| **Global Exception Handler** | `@RestControllerAdvice` | Xử lý tập trung tất cả exception, trả về `ApiResponse` với HTTP status phù hợp |
| **Current User Resolver** | `CurrentUserIdArgumentResolver` | Inject `userId` từ `SecurityContext` vào method parameter qua `@CurrentUserId` annotation |
| **Async Config** | `AsyncConfig` | Thread pool cho `@Async` — dùng bởi email sender |
| **WebMvc Config** | `WebMvcConfig` | Cấu hình CORS, đăng ký argument resolvers |
| **OpenAPI Config** | `OpenApiConfig` | Cấu hình Springdoc OpenAPI, JWT bearer scheme cho Swagger UI |

#### 3.2.2.2. Module Identity (Xác thực và người dùng)

Kết hợp chức năng Authentication và User Management trong một module duy nhất.

**Package:** `com.tmt.ecommerce.identity`

**Bảng 3.4.** Cấu trúc module Identity

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `User` (id, email, password, roles, createdAt) |
| `repository` | `UserRepository` |
| `service` | `AuthService` (register/login), `UserService` |
| `controller` | `AuthController` (`/api/v1/auth/**`), `UserController` |
| `dto` | `RegisterRequest`, `LoginRequest`, `AuthResponse` (JWT), `UserProfileResponse` |
| `security` | `UserDetailsServiceImpl` (load user for Spring Security) |
| `api` (Internal) | `IdentityInternalService` → `IdentityInternalData` |
| `initializer` | `AdminInitializer` (tạo tài khoản Admin mặc định khi khởi động) |

**Chức năng:** Đăng ký tài khoản, đăng nhập (trả JWT), xem/sửa profile, phân vai trò (`ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`).

#### 3.2.2.3. Module Product (Sản phẩm và danh mục)

Kết hợp Category và Product trong một module, vì Category là phụ thuộc nội tại của Product.

**Package:** `com.tmt.ecommerce.product`

**Bảng 3.5.** Cấu trúc module Product

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Product`, `ProductVariant`, `ProductImage`, `Category` (self-referencing tree), `Brand` |
| `repository` | `ProductRepository`, `ProductVariantRepository`, `CategoryRepository`, `BrandRepository` |
| `service` | `ProductService` (CRUD, search, image management), `CategoryService`, `BrandService` |
| `controller` | `ProductController`, `AdminProductController`, `VendorProductController`, `CategoryController` |
| `dto` | `ProductCreateRequest`, `ProductResponse`, `ProductSummaryResponse`, `CategoryResponse` |
| `api` (Internal) | `ProductInternalService` → `ProductInternalData`, `ProductVariantInfo` |
| `enums` | `ProductStatus` (ACTIVE, HIDDEN) |

**Chức năng:** CRUD sản phẩm (với ảnh Cloudinary), CRUD danh mục phân cấp, tìm kiếm theo từ khóa với phân trang, quản lý biến thể sản phẩm.

**Internal API cung cấp cho module khác:**
- `getProductVariant(variantId)` → `ProductInternalData` (cart, order dùng)
- `getVariantInfos(variantIds)` → bulk query cho order mapping
- `getProductIdsByVariantIds(ids)` → review module dùng

#### 3.2.2.4. Module Shop (Gian hàng)

**Package:** `com.tmt.ecommerce.shop`

**Bảng 3.6.** Cấu trúc module Shop

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Shop` (id, userId, name, description, status: PENDING/ACTIVE/BANNED) |
| `repository` | `ShopRepository` |
| `service` | `ShopService` (register, approve/reject, get) |
| `controller` | `ShopController`, `AdminShopController` |
| `dto` | `ShopCreateRequest`, `ShopResponse` |
| `api` (Internal) | `ShopInternalService` → `getShopIdByUserId`, `getUserIdByShopId`, `getShopNameById`, `isShopOwner`, `findShopIdByUserId` |
| `api/event` | `ShopApprovedEvent` (phát sau khi Admin duyệt gian hàng) |

**Chức năng:** Đăng ký gian hàng, Admin duyệt/từ chối, quản lý thông tin gian hàng.

**Sự kiện phát ra:** `ShopApprovedEvent` → `ShopNotificationListener` gửi email Gmail SMTP cho Vendor.

#### 3.2.2.5. Module Cart (Giỏ hàng)

**Package:** `com.tmt.ecommerce.cart`

**Bảng 3.7.** Cấu trúc module Cart

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `CartItem` (userId, variantId, quantity) |
| `repository` | `CartItemRepository` |
| `service` | `CartService` (add, update, remove, get) |
| `controller` | `CartController` (`/api/v1/carts/**`) |
| `dto` | `AddToCartRequest`, `CartResponse`, `CartItemResponse` |

**Phụ thuộc Internal:** Gọi `ProductInternalService.getProductVariant(variantId)` để kiểm tra tồn kho và lấy thông tin sản phẩm khi hiển thị giỏ hàng.

**Chức năng:** Thêm sản phẩm (kiểm tra tồn kho), cập nhật số lượng, xóa item, xem giỏ hàng.

#### 3.2.2.6. Module Order (Đặt hàng)

**Package:** `com.tmt.ecommerce.order`

**Bảng 3.8.** Cấu trúc module Order

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Order` (id, buyerId, shopId, paymentGroupId, status, subtotal, discount, total), `OrderItem` (orderId, variantId, quantity, price) |
| `repository` | `OrderRepository`, `OrderItemRepository` |
| `service` | `OrderService` (checkout, preview, getHistory, updateStatus) |
| `controller` | `OrderController` (buyer), `VendorOrderController`, `AdminOrderController` |
| `dto` | `CheckoutRequest`, `CheckoutResponse`, `PreviewCheckoutRequest`, `OrderResponse`, `OrderItemResponse` |
| `api` (Internal) | `OrderInternalService` → `DeliveredOrderData` |
| `initializer` | Logic tách đơn đa gian hàng, phân bổ voucher |
| `enums` | `OrderStatus` (PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED) |

**Chức năng cốt lõi — Checkout đa gian hàng:**
1. Nhận `CheckoutRequest` (danh sách item, địa chỉ, phương thức thanh toán, voucher codes).
2. Kiểm tra tồn kho từng item qua `ProductInternalService`.
3. Tính giảm giá voucher qua `VoucherInternalService.calculateDiscount(request)`.
4. Nhóm items theo shopId → tạo N đơn hàng trong một `@Transactional`.
5. Gán `payment_group_id` (UUID) cho tất cả đơn hàng trong nhóm.
6. Trả về `paymentGroupId` để `payment` module xử lý.

**Sự kiện phát ra:** `OrderConfirmedEvent`, `OrderShippedEvent`, `OrderDeliveredEvent`, `OrderCancelledEvent`.

#### 3.2.2.7. Module Payment (Thanh toán)

**Package:** `com.tmt.ecommerce.payment`

**Bảng 3.9.** Cấu trúc module Payment

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Payment` (id, paymentGroupId, txnRef, method, status, amount, createdAt) |
| `repository` | `PaymentRepository` (với `@Lock PESSIMISTIC_WRITE` trên findByTxnRef) |
| `service` | `PaymentGatewayService` (orchestrate), `VnPayService`, `CodService` |
| `strategy` | `PaymentStrategy` (interface), `VnPayStrategy`, `CodStrategy` |
| `factory` | `PaymentStrategyFactory` |
| `controller` | `PaymentController` (`/api/v1/payments/**`, `/api/v1/payments/vnpay/ipn`) |
| `dto` | `PaymentInitRequest`, `PaymentInitResponse`, `VnPayIpnRequest`, `PaymentStatusResponse` |
| `util` | `VnPayHashUtil` (HMAC-SHA512) |

**Luồng VNPay:**
```
POST /api/v1/payments/init → VnPayStrategy → tạo URL redirect → trả về client
VNPay → GET /api/v1/payments/vnpay/ipn → xác thực HMAC → Pessimistic Lock
       → cập nhật Payment.status → SUCCESS / FAILED
       → nếu SUCCESS: cập nhật tất cả Order của group → CONFIRMED
       → phát OrderConfirmedEvent (AFTER_COMMIT)
```

**Luồng COD:**
```
POST /api/v1/payments/init → CodStrategy → tạo Payment (status=SUCCESS ngay lập tức)
       → cập nhật tất cả Order của group → CONFIRMED
       → phát OrderConfirmedEvent (AFTER_COMMIT)
```

#### 3.2.2.8. Module Review (Đánh giá)

**Package:** `com.tmt.ecommerce.review`

**Bảng 3.10.** Cấu trúc module Review

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Review` (id, buyerId, productId, orderId, rating, comment, createdAt) |
| `repository` | `ReviewRepository` |
| `service` | `ReviewService` (create, list, getRatingSummary) |
| `controller` | `ReviewController` (`/api/v1/reviews/**`) |
| `dto` | `ReviewCreateRequest`, `ReviewResponse`, `RatingSummaryResponse` |

**Điều kiện viết đánh giá:** Hệ thống gọi `OrderInternalService.getDeliveredOrders(buyerId, productId)` để xác minh người mua đã nhận hàng trước khi cho phép đánh giá. Ràng buộc: 1 đánh giá/sản phẩm/đơn hàng (unique constraint trên `(orderId, productId)`).

**Sự kiện phát ra:** `ReviewCreatedEvent` → `ReviewNotificationListener` tạo thông báo cho Vendor.

#### 3.2.2.9. Module Notification (Thông báo)

**Package:** `com.tmt.ecommerce.notification`

**Bảng 3.11.** Cấu trúc module Notification

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Notification` (id, userId, type, message, referenceId, isRead, createdAt) |
| `repository` | `NotificationRepository` |
| `service` | `NotificationService` |
| `controller` | `NotificationController` (`/api/v1/notifications/**`) |
| Listeners | `OrderNotificationListener`, `ReviewNotificationListener`, `ShopNotificationListener` |
| `enums` | `NotificationType` (ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, ORDER_CANCELLED, REVIEW_CREATED, SHOP_APPROVED) |

**Cơ chế:** Hoàn toàn Event-Driven — không có module nào gọi trực tiếp vào Notification. Tất cả listeners đều dùng `@TransactionalEventListener(phase = AFTER_COMMIT)` + `@Transactional(propagation = REQUIRES_NEW)`.

**Frontend polling:** REST polling 60 giây qua `GET /api/v1/notifications/unread-count`.

#### 3.2.2.10. Module Chat (Nhắn tin thời gian thực)

**Package:** `com.tmt.ecommerce.chat`

**Bảng 3.12.** Cấu trúc module Chat

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `ChatRoom` (id, buyerId, shopId), `ChatMessage` (id, roomId, senderId, content, sentAt) |
| `repository` | `ChatRoomRepository`, `ChatMessageRepository` |
| `service` | `ChatService` (getOrCreateRoom, sendMessage, getMessages, getRooms) |
| `controller` | `ChatController` (REST: `/api/v1/chats/**`), `ChatMessageController` (STOMP: `@MessageMapping("/chat.send")`) |
| `config` | `WebSocketConfig` (cấu hình STOMP broker, endpoint `/ws`, interceptors) |
| Security | `JwtChannelInterceptor` (xác thực JWT trong STOMP CONNECT) |

**Thiết kế ranh giới module nghiêm ngặt:** Chat entity chỉ lưu ID nguyên thủy (`buyerId`, `shopId`, `senderId`). Không có `@ManyToOne` sang `User` hay `Shop` entity. Mọi thông tin bổ sung (tên shop, userId của shop owner) đều lấy qua `ShopInternalService`.

#### 3.2.2.11. Module Voucher (Mã giảm giá)

**Package:** `com.tmt.ecommerce.voucher`

**Bảng 3.13.** Cấu trúc module Voucher

| Sublayer | Nội dung chính |
|---|---|
| `entity` | `Voucher` (id, code, scope: SYSTEM/SHOP, discountType: PERCENTAGE/FIXED_AMOUNT, value, maxDiscount, minOrderValue, usedCount, maxUsage, shopId, expiryDate) |
| `repository` | `VoucherRepository` |
| `service` | `VoucherService` (CRUD, validate, calculateDiscount) |
| `controller` | `AdminVoucherController`, `VendorVoucherController`, `PublicVoucherController` |
| `api` (Internal) | `VoucherInternalService` → nhận `VoucherCalculationRequest`, trả `VoucherDiscountResult` |

**Cơ chế Atomic Update:** Tăng `used_count` bằng bulk SQL: `UPDATE vouchers SET used_count = used_count + 1 WHERE id = ? AND used_count < max_usage` — tránh race condition không cần `@Version`.

#### 3.2.2.12. Module Media (Lưu trữ ảnh)

**Package:** `com.tmt.ecommerce.media` (hiện nằm trong `infrastructure.media`)

**Vai trò:** Cung cấp `FileStorageService` để upload/delete ảnh lên Cloudinary. Được gọi bởi module Product khi Vendor upload ảnh sản phẩm.

**Cơ chế Zero-Trust publicId Deletion:** Server lấy `publicId` từ database (bảng `product_images`), không nhận từ client request. Việc gọi xóa Cloudinary được thực hiện qua `TransactionSynchronizationManager.registerSynchronization(afterCommit)` — đảm bảo chỉ xóa khi MySQL transaction đã commit thành công.

### 3.2.3. Luồng xử lý tổng quát

#### 3.2.3.1. Luồng xử lý HTTP Request thông thường

Đây là luồng chuẩn cho tất cả REST API request, áp dụng cho hầu hết các chức năng của hệ thống.

**Hình 3.2.** Luồng xử lý HTTP Request chuẩn

```
  Client (Browser)                    Spring Boot Application
       │                                         │
       │── GET/POST/PUT/DELETE /api/v1/** ──────>│
       │                                         │
       │                         ┌───────────────┴──────────────┐
       │                         │      Security Filter Chain   │
       │                         │  1. JwtAuthenticationFilter  │
       │                         │     - Extract JWT            │
       │                         │     - Verify signature        │
       │                         │     - Set SecurityContext     │
       │                         │  2. @PreAuthorize check       │
       │                         └───────────────┬──────────────┘
       │                                         │ (if authorized)
       │                         ┌───────────────┴──────────────┐
       │                         │    @RestController            │
       │                         │  - Validate request DTO      │
       │                         │    (@Valid, Bean Validation)  │
       │                         │  - Extract @CurrentUserId     │
       │                         │  - Delegate to Service        │
       │                         └───────────────┬──────────────┘
       │                                         │
       │                         ┌───────────────┴──────────────┐
       │                         │    @Service (@Transactional)  │
       │                         │  - Business logic             │
       │                         │  - Ownership check            │
       │                         │  - Call Internal Services     │
       │                         │    (cross-module)             │
       │                         │  - Map Entity → Response DTO  │
       │                         └───────────────┬──────────────┘
       │                                         │
       │                         ┌───────────────┴──────────────┐
       │                         │    @Repository (JPA)          │
       │                         │  - JPQL / derived query       │
       │                         │  - Hibernate ORM              │
       │                         └───────────────┬──────────────┘
       │                                         │
       │                                    ┌────┴────┐
       │                                    │  MySQL  │
       │                                    └────┬────┘
       │                                         │
       │<── ApiResponse<T> (JSON) ───────────────┘
```

**Đặc điểm quan trọng:**
- Controller không chứa business logic — chỉ validate request và delegate.
- Service là nơi duy nhất chứa business rules và transaction boundary.
- Entity không bao giờ được trả về trực tiếp — luôn map sang DTO trước khi trả về API.
- Exception ném ra tại bất kỳ layer nào đều được bắt bởi `GlobalExceptionHandler`.

#### 3.2.3.2. Luồng xử lý thanh toán VNPay

Đây là luồng phức tạp nhất trong hệ thống, bao gồm cả redirect và webhook callback.

**Hình 3.3.** Luồng thanh toán VNPay end-to-end

```
  Buyer (Browser)         Backend (Spring Boot)            VNPay Sandbox
       │                           │                              │
  [1]  ├─POST /payments/init ─────>│                              │
       │  {paymentGroupId,         │── VnPayStrategy.process() ──>│
       │   method: VNPAY}          │<── vnpay_url ────────────────│
  [2]  │<── {paymentUrl} ──────────│                              │
       │                           │                              │
  [3]  ├─ redirect đến VNPay ─────────────────────────────────────>
       │    (người mua nhập thẻ)                                  │
       │                           │                              │
  [4]  │<─ redirect về /payment-result ──────────────────────────-│
       │   (chỉ để hiển thị UI,                                   │
       │    KHÔNG dùng để xác nhận)│                              │
       │                           │                              │
  [5]  │  (đồng thời, VNPay gọi webhook)                         │
       │                           │<─ GET /payments/vnpay/ipn ───│
       │                           │   ?vnp_TransactionNo=...     │
       │                           │   &vnp_SecureHash=...        │
       │                           │                              │
       │                    [6] Verify HMAC-SHA512                │
       │                    [7] SELECT ... FOR UPDATE (Pessimistic Lock)
       │                    [8] if status == PENDING:            │
       │                         Update Payment → SUCCESS        │
       │                         Update all Orders → CONFIRMED   │
       │                         Publish OrderConfirmedEvent     │
       │                    [9] Commit Transaction               │
       │                    [10] AFTER_COMMIT:                   │
       │                          Create Notification            │
       │                           │                              │
  [11] ├─ GET /payments/group/{id}/status ─>│                     │
       │<── {overallStatus: CONFIRMED} ─────│                     │
       │   (frontend polling để hiển thị)   │                     │
```

#### 3.2.3.3. Luồng WebSocket Chat thời gian thực

**Hình 3.4.** Luồng WebSocket STOMP cho chức năng chat

```
  Buyer (Browser)              Spring Boot               Vendor (Browser)
       │                          │                            │
  [1] Đăng nhập → nhận JWT token  │                            │
       │                          │                    [1] Đăng nhập → JWT
       │                          │                            │
  [2]  ├── HTTP GET /ws ─────────>│ (SockJS Upgrade)           │
       │<── 101 Switching Proto ──│                     [2] ├── HTTP GET /ws
       │                          │                            │<── 101
       │                          │                            │
  [3]  ├── STOMP CONNECT ────────>│                     [3] ├── STOMP CONNECT
       │   {Authorization: Bearer}│◄─ JwtChannelInterceptor    │   {Authorization}
       │                          │   xác thực JWT,            │
       │<── STOMP CONNECTED ──────│   gán StompPrincipal(userId)│
       │                          │                            │<── CONNECTED
       │                          │                            │
  [4]  ├── SUBSCRIBE ────────────>│                     [4] ├── SUBSCRIBE
       │   /user/queue/messages   │                     │   /user/queue/messages
       │                          │                            │
  [5] Buyer nhấn "Chat ngay"      │                            │
       ├─POST /chats/rooms ───────>│ (REST: getOrCreate room)   │
       │<── {roomId, shopId} ──────│                            │
       │                          │                            │
  [6]  ├── STOMP SEND ────────────>│                            │
       │   /app/chat.send         │──convertAndSendToUser(shopOwnerId)──>│
       │   {roomId, content}      │──convertAndSendToUser(buyerId)──>│   │
       │<── echo tin nhắn ─────────│ (Optimistic ID replace)    │   │
       │                          │                     ├── nhận tin nhắn
       │                          │                     │  server push
```

**Đặc điểm Optimistic UI:** Frontend tạo tin nhắn với ID tạm âm (ví dụ: `-1703000000000`) và hiển thị ngay (0ms latency). Khi server echo về với ID thật từ database, frontend thay thế tin nhắn tạm bằng tin nhắn thật.

#### 3.2.3.4. Luồng xử lý Media (Ảnh sản phẩm)

**Hình 3.5.** Luồng upload và xóa ảnh sản phẩm qua Cloudinary

```
  Upload:
  Vendor ──POST /products/{id}/images────> Controller
                                               │
                                          FileStorageService.upload()
                                               │
                                          Cloudinary SDK ────> Cloudinary CDN
                                               │<── {url, publicId}
                                          Lưu ProductImage (url, publicId) vào MySQL
                                               │
                                          <── {imageUrl, imageId}

  Delete (Zero-Trust):
  Vendor ──DELETE /products/{id}/images/{imageId}────> Controller
                                               │
                                          Lấy publicId từ DB (KHÔNG từ client)
                                               │
                                          MySQL: xóa ProductImage record
                                               │
                                          Trong @Transactional:
                                          TransactionSynchronizationManager
                                               .registerSynchronization(afterCommit)
                                               │
                                          [Sau khi commit MySQL thành công]
                                               │
                                          Cloudinary SDK.destroy(publicId)
                                               │
                                          <── 200 OK
```

#### 3.2.3.5. Luồng thông báo hướng sự kiện

**Hình 3.6.** Luồng Event-Driven Notification (ví dụ: đặt hàng COD thành công)

```
  OrderService (Checkout)
       │
       ├── @Transactional (BEGIN)
       │     trừ tồn kho
       │     tạo N Order entities
       │     cập nhật voucher used_count
       │     PaymentService.createCodPayment() → Payment(SUCCESS)
       │     Order status → CONFIRMED
       │     applicationEventPublisher.publishEvent(OrderConfirmedEvent)
       │     ← sự kiện được buffered, CHƯA gửi đến listener
       ├── @Transactional (COMMIT)
       │
       │ [AFTER_COMMIT] Spring gọi listener
       │
       ├──> OrderNotificationListener.handleOrderConfirmed()
       │         @Transactional(REQUIRES_NEW)  ← giao dịch mới độc lập
       │         Notification.save(buyerNotification)
       │         COMMIT (notification transaction)
       │
       ├──> ShopNotificationListener.handleOrderConfirmed()
       │         @Transactional(REQUIRES_NEW)
       │         Notification.save(vendorNotification)
       │         COMMIT
       │
   Client ── GET /notifications/unread-count ──> (polling 60s)
```

**Lý do thiết kế này:** Nếu `OrderService.checkout()` ném exception và rollback sau khi đã phát sự kiện, notification sẽ không được tạo (vì listener chỉ chạy sau COMMIT). Nếu notification listener ném exception, nó không rollback giao dịch đặt hàng (vì `REQUIRES_NEW` tạo giao dịch độc lập).

---

*Kết thúc phần 3.1 và 3.2. Xem tiếp 3.3 (Use Case), 3.4 (Class Diagram), 3.5 (Database Design), v.v.*
