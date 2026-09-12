## 3.4. Mô hình hóa hệ thống

Phần này trình bày các sơ đồ UML mô hình hóa hệ thống Enterprise Ecommerce, bao gồm sơ đồ lớp, sơ đồ tuần tự, sơ đồ hoạt động và sơ đồ trạng thái. Tất cả đều được phân tích trực tiếp từ source code thực tế.

---

### 3.4.1. Sơ đồ lớp (Class Diagram)

#### 3.4.1.1. Tổng quan các lớp thực thể

Hệ thống Enterprise Ecommerce có 18 Entity class chính, phân bố trong 8 module:

**Bảng 3.27.** Danh sách Entity class theo module

| Module | Entity | Bảng DB |
|---|---|---|
| identity | User, Role | `users`, `roles`, `user_roles` |
| shop | Shop | `shops` |
| product | Brand, Category, Product, ProductVariant, ProductImage | `brands`, `categories`, `products`, `product_variants`, `product_images` |
| cart | Cart, CartItem | `carts`, `cart_items` |
| order | Order, OrderItem | `orders`, `order_items` |
| payment | Payment | `payments` |
| voucher | Voucher | `vouchers` |
| review | Review | `reviews`, `review_images` |
| notification | Notification | `notifications` |
| chat | ChatRoom, ChatMessage | `chat_rooms`, `chat_messages` |

#### 3.4.1.2. Đặc điểm kiến trúc Modular Monolith trong sơ đồ lớp

Một điểm đáng chú ý trong sơ đồ lớp là hệ thống áp dụng triệt để nguyên tắc **Loose Coupling** giữa các module:

- **Tight Coupling (cùng module):** Sử dụng `@ManyToOne` với object reference. Ví dụ: `Product` → `Category` (cùng module product).
- **Loose Coupling (khác module):** Chỉ lưu `Long id`. Ví dụ: `Cart.userId` thay vì `@ManyToOne User user`. Điều này ngăn module cart phụ thuộc trực tiếp vào module identity.

**Bảng 3.28.** Phân biệt Tight Coupling và Loose Coupling trong Entity

| Quan hệ | Loại | Cách implement | Lý do |
|---|---|---|---|
| `Product` → `Category` | Tight (same module) | `@ManyToOne Category category` | Cùng module product, hiệu năng JPA tốt hơn |
| `Product` → `Shop` | Loose (cross-module) | `Long shopId` | Tránh dependency giữa module product và shop |
| `Cart` → `User` | Loose (cross-module) | `Long userId` | Tránh dependency giữa module cart và identity |
| `Order` → `Shop` | Loose (cross-module) | `Long shopId` | Tránh dependency giữa module order và shop |
| `Review` → `Product` | Loose (cross-module) | `Long productId` | Tránh dependency giữa module review và product |

#### 3.4.1.3. Mô tả các lớp quan trọng

**Lớp User và Role:**

`User` lưu thông tin người dùng với `email` (UNIQUE), `passwordHash` (BCrypt), `fullName`, `phone`, và `status`. Quan hệ với `Role` là Many-to-Many thông qua bảng trung gian `user_roles`. Hệ thống có 3 vai trò: `ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`.

**Lớp Shop:**

`Shop` có quan hệ 1-1 với `User` thông qua `userId` (UNIQUE constraint). Trạng thái gian hàng được kiểm soát bởi enum `ShopStatus` (PENDING, ACTIVE, BANNED).

**Lớp Product và ProductVariant:**

`Product` là lớp sản phẩm cơ bản với các thuộc tính chung (name, description, price, stockQuantity). `ProductVariant` chứa biến thể cụ thể (màu sắc, kích thước) với `attributes` lưu dạng JSON. Mỗi biến thể có `sku` (Stock Keeping Unit) riêng biệt, `price` và `stockQuantity` độc lập. Ảnh sản phẩm được quản lý qua `ProductImage` với `publicId` để xóa an toàn khỏi Cloudinary.

**Lớp Category:**

`Category` tự tham chiếu để tạo cây danh mục nhiều cấp. Nếu `parent == null`, đây là danh mục gốc.

**Lớp Cart và CartItem:**

`Cart` là giỏ hàng của người dùng (1-1 với User). `CartItem` tham chiếu đến `productVariantId` (Loose Coupling) và lưu `quantity`.

**Lớp Order và OrderItem:**

`Order` áp dụng **Snapshot Pattern** — `OrderItem` lưu `unitPrice` và `productName` tại thời điểm đặt hàng, không bị ảnh hưởng khi Vendor thay đổi giá sau. `paymentGroupId` (UUID) là khóa nhóm các đơn hàng từ cùng một lần checkout đa gian hàng.

**Lớp Payment:**

`Payment` quản lý giao dịch tài chính với `transactionId` (nội bộ) và `gatewayTransactionId` (từ VNPay). Trường `rawData` lưu toàn bộ response của VNPay để đối soát. Liên kết với `Order` qua `paymentGroupId`.

**Lớp Voucher:**

`Voucher` hỗ trợ hai phạm vi: `SYSTEM` (áp dụng toàn hệ thống) và `SHOP` (chỉ áp dụng cho gian hàng cụ thể). Hai loại giảm giá: `PERCENTAGE` (theo %) và `FIXED_AMOUNT` (số tiền cố định). Trường `usedCount` được cập nhật bằng atomic SQL để đảm bảo tính nhất quán trong môi trường concurrent.

**Lớp Review:**

`Review` có unique constraint `(userId, orderId, productId)` để đảm bảo mỗi người dùng chỉ đánh giá một sản phẩm một lần trên một đơn hàng cụ thể. Chứa `vendorReply` để Vendor có thể phản hồi đánh giá.

**Lớp ChatRoom và ChatMessage:**

`ChatRoom` có unique constraint `(buyerId, shopId)` — mỗi cặp Buyer-Shop chỉ có một phòng chat. `ChatMessage` lưu `isRead` để theo dõi tin nhắn chưa đọc.

**Hình 3.8.** Sơ đồ lớp tổng thể hệ thống *(xem file CLASS_DIAGRAM_SPEC.md để vẽ bằng Mermaid hoặc Draw.io)*

---

### 3.4.2. Sơ đồ tuần tự (Sequence Diagram)

Phần này trình bày sơ đồ tuần tự cho các luồng nghiệp vụ cốt lõi.

#### 3.4.2.1. Luồng Đăng nhập

**Hình 3.9.** Sơ đồ tuần tự — Đăng nhập (SD01)

Luồng đăng nhập đi qua các thành phần: `AuthController` → `AuthenticationService` → `AuthenticationManager` → `UserDetailsServiceImpl` → `JwtService`. Sau khi `AuthenticationManager` xác thực thông tin qua `BCrypt`, `JwtService` tạo JWT token 24h chứa `userId` và `roles`. Token được trả về client và lưu tại `localStorage`.

#### 3.4.2.2. Luồng Đặt hàng (Checkout)

**Hình 3.10.** Sơ đồ tuần tự — Checkout đa gian hàng (SD03)

Checkout là luồng phức tạp nhất, diễn ra trong một `@Transactional`:
1. Với mỗi CartItem: `ProductInternalService.getProductVariant()` kiểm tra tồn kho.
2. `VoucherInternalService.calculateDiscount()` tính giảm giá.
3. Nhóm CartItem theo `shopId` → tạo N `Order` entity với chung `paymentGroupId`.
4. Bulk update tồn kho, tăng `usedCount` voucher, xóa CartItem.
5. Nếu bất kỳ bước nào thất bại → rollback toàn bộ.

#### 3.4.2.3. Luồng Thanh toán VNPay

**Hình 3.11.** Sơ đồ tuần tự — Thanh toán VNPay 2 giai đoạn (SD04)

Luồng thanh toán VNPay có 2 giai đoạn rõ ràng:

**Giai đoạn 1 — Tạo URL:** `PaymentGatewayService` tạo URL có chữ ký HMAC-SHA512, tạo `Payment` record với `status=PENDING`, trả về URL để frontend redirect.

**Giai đoạn 2 — IPN Callback:** VNPay gọi vào `GET /api/v1/payments/vnpay/ipn`. Hệ thống:
- Xác thực HMAC-SHA512 (chống giả mạo).
- Lấy Payment bằng `SELECT FOR UPDATE` (Pessimistic Lock — chống race condition).
- Kiểm tra `status == PENDING` (Idempotency — chống xử lý 2 lần).
- Cập nhật Payment và Orders.
- Phát `OrderConfirmedEvent`.
- AFTER_COMMIT: Tạo Notification cho Buyer và Vendor.

#### 3.4.2.4. Luồng Chat realtime

**Hình 3.12.** Sơ đồ tuần tự — Chat WebSocket STOMP (SD08)

Chat sử dụng WebSocket STOMP với `JwtChannelInterceptor` xác thực JWT tại bước CONNECT (không phải từng message). `SimpMessagingTemplate.convertAndSendToUser()` push message đến cả Buyer và Vendor qua `/user/queue/messages`. Frontend áp dụng **Optimistic UI** — tạo message tạm với ID âm ngay khi gửi (0ms latency), thay bằng ID thật khi server echo về.

---

### 3.4.3. Sơ đồ hoạt động (Activity Diagram)

#### 3.4.3.1. Luồng Checkout đa gian hàng

**Hình 3.13.** Sơ đồ hoạt động — Checkout (AD01)

Luồng checkout có 3 điểm quyết định chính: (1) Kiểm tra tồn kho từng sản phẩm, (2) Kiểm tra tính hợp lệ của voucher, (3) COMMIT hoặc ROLLBACK toàn bộ transaction. Việc nhóm CartItem theo `shopId` diễn ra sau khi tất cả kiểm tra đã qua.

#### 3.4.3.2. Luồng Thanh toán VNPay end-to-end

**Hình 3.14.** Sơ đồ hoạt động — Thanh toán VNPay (AD02)

Luồng thanh toán phân nhánh tại 4 điểm: (1) Người dùng có thanh toán không, (2) HMAC có hợp lệ không, (3) Payment đã được xử lý chưa (idempotency), (4) ResponseCode từ VNPay là 00 hay không.

#### 3.4.3.3. Luồng Vendor quản lý đơn hàng

**Hình 3.15.** Sơ đồ hoạt động — Vendor quản lý đơn (AD03)

Luồng áp dụng State Machine để kiểm soát chuyển trạng thái đơn. Các chuyển đổi hợp lệ: `CONFIRMED → SHIPPED → DELIVERED` và `CONFIRMED → CANCELLED`. Mỗi chuyển trạng thái phát sinh Notification cho Buyer (AFTER_COMMIT event-driven).

#### 3.4.3.4. Luồng Viết đánh giá

**Hình 3.16.** Sơ đồ hoạt động — Viết đánh giá (AD05)

Luồng kiểm soát chặt điều kiện đánh giá: Buyer phải có đơn hàng trạng thái DELIVERED chứa sản phẩm đó, và chưa đánh giá cho `orderId` cụ thể này (unique constraint). Sau khi lưu Review, hệ thống phát `ReviewCreatedEvent` để tạo Notification cho Vendor (AFTER_COMMIT).

---

### 3.4.4. Sơ đồ trạng thái (State Diagram)

#### 3.4.4.1. Sơ đồ trạng thái Đơn hàng

**Hình 3.17.** Sơ đồ trạng thái — Order (ST01)

Đơn hàng có 5 trạng thái: `PENDING → CONFIRMED → SHIPPED → DELIVERED` (luồng thành công) và `PENDING → CANCELLED`, `CONFIRMED → CANCELLED` (luồng hủy). Hai trạng thái kết thúc: `DELIVERED` (hoàn tất, Buyer có thể đánh giá) và `CANCELLED` (hủy vĩnh viễn).

**Bảng 3.29.** State Machine chuyển trạng thái Order

| Từ trạng thái | Sang trạng thái | Actor | Điều kiện |
|---|---|---|---|
| — | PENDING | System | Checkout thành công |
| PENDING | CONFIRMED | System (VNPay IPN) | ResponseCode = "00", HMAC valid |
| PENDING | CANCELLED | System | Payment FAILED |
| CONFIRMED | SHIPPED | Vendor | PATCH /vendor/orders/{id}/status |
| CONFIRMED | CANCELLED | Vendor | PATCH /vendor/orders/{id}/status |
| SHIPPED | DELIVERED | Vendor | PATCH /vendor/orders/{id}/status |
| DELIVERED | — | — | Terminal state |
| CANCELLED | — | — | Terminal state |

#### 3.4.4.2. Sơ đồ trạng thái Thanh toán

**Hình 3.18.** Sơ đồ trạng thái — Payment (ST02)

Payment có 4 trạng thái: `PENDING` (chờ thanh toán), `SUCCESS` (thành công), `FAILED` (thất bại), `CANCELED` (người dùng hủy). Cơ chế **Idempotency** được áp dụng: nếu IPN đến lần 2 khi Payment đã ở SUCCESS, hệ thống bỏ qua và trả về `RspCode=00` (không xử lý lại). Cơ chế **Pessimistic Lock** (`SELECT FOR UPDATE`) chống race condition khi VNPay gửi nhiều IPN cùng lúc.

**Bảng 3.30.** State Machine chuyển trạng thái Payment

| Từ trạng thái | Sang trạng thái | Trigger | Cơ chế bảo vệ |
|---|---|---|---|
| — | PENDING | generatePaymentUrl() | — |
| PENDING | SUCCESS | IPN ResponseCode = "00" | HMAC verify + Pessimistic Lock + Idempotency |
| PENDING | FAILED | IPN ResponseCode ≠ "00" | HMAC verify |
| PENDING | CANCELED | Người dùng hủy | — |

#### 3.4.4.3. Sơ đồ trạng thái Gian hàng và Sản phẩm

**Hình 3.19.** Sơ đồ trạng thái — Shop (ST03)

Shop có 3 trạng thái: `PENDING` (chờ duyệt) → `ACTIVE` (hoạt động) → `BANNED` (bị cấm). Chỉ Shop `ACTIVE` mới được đăng sản phẩm. Khi Admin approve, `ShopApprovedEvent` được phát, email thông báo được gửi qua Gmail SMTP (async, AFTER_COMMIT).

**Hình 3.20.** Sơ đồ trạng thái — Product (ST04)

Product có 3 trạng thái: `ACTIVE` (đang hiển thị), `OUT_OF_STOCK` (hết hàng — tự động khi stockQuantity = 0 sau checkout), `HIDDEN` (Vendor ẩn — soft delete). `ProductVariant` cũng có cùng enum `ProductStatus`.

---

*Kết thúc phần 3.4.*
