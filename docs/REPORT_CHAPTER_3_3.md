## 3.3. Phân tích sơ đồ Use Case

### 3.3.1. Xác định Actor

Dựa trên source code thực tế, hệ thống có 7 actor:

**Bảng 3.14.** Danh sách Actor trong hệ thống

| Actor | Loại | Mô tả | Bằng chứng source code |
|---|---|---|---|
| **Guest** | Primary | Người dùng chưa đăng nhập | Endpoint `/api/v1/products`, `/api/v1/categories` không yêu cầu auth |
| **Buyer** | Primary | User đăng nhập với `ROLE_USER` | `@PreAuthorize("hasRole('USER')")` trong `ReviewController`, `CartController` |
| **Vendor** | Primary | User đăng nhập với `ROLE_VENDOR`, có shop đã duyệt | `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")` trong `VendorProductController`, `VendorOrderController` |
| **Admin** | Primary | User với `ROLE_ADMIN` | `@PreAuthorize("hasRole('ADMIN')")` trong `AdminShopController` |
| **VNPay** | Secondary | Gửi IPN webhook sau khi giao dịch | `GET /api/v1/payments/vnpay/ipn` trong `PaymentController` |
| **Cloudinary** | Secondary | Lưu ảnh, trả về URL và publicId | `FileStorageService` trong media module |
| **Gmail SMTP** | Secondary | Gửi email thông báo duyệt shop | `AsyncConfig` + `ShopNotificationListener` |

Guest có thể xem sản phẩm và đánh giá mà không cần đăng nhập. Tất cả thao tác ghi đều yêu cầu xác thực JWT.

---

### 3.3.2. Danh sách Use Case

**Bảng 3.15.** Danh sách toàn bộ Use Case theo Actor

| ID | Tên Use Case | Actor | Endpoint bằng chứng |
|---|---|---|---|
| **UC01** | Đăng ký tài khoản | Guest | `POST /api/v1/auth/register` |
| **UC02** | Đăng nhập | Guest | `POST /api/v1/auth/login` |
| **UC03** | Xem danh sách sản phẩm | Guest, Buyer | `GET /api/v1/products` |
| **UC04** | Tìm kiếm sản phẩm | Guest, Buyer | `GET /api/v1/products?keyword=` |
| **UC05** | Xem chi tiết sản phẩm | Guest, Buyer | `GET /api/v1/products/{id}` |
| **UC06** | Xem đánh giá sản phẩm | Guest, Buyer | `GET /api/v1/products/{id}/reviews` |
| **UC07** | Thêm sản phẩm vào giỏ | Buyer | `POST /api/v1/carts/items` |
| **UC08** | Cập nhật số lượng trong giỏ | Buyer | `PUT /api/v1/carts/items/{itemId}` |
| **UC09** | Xóa sản phẩm khỏi giỏ | Buyer | `DELETE /api/v1/carts/items/{itemId}` |
| **UC10** | Xem giỏ hàng | Buyer | `GET /api/v1/carts` |
| **UC11** | Xem trước giá trị đơn hàng | Buyer | `POST /api/v1/orders/preview` |
| **UC12** | Đặt hàng (Checkout) | Buyer | `POST /api/v1/orders/checkout` |
| **UC13** | Thanh toán VNPay | Buyer + VNPay | `POST /api/v1/payments/create` + IPN |
| **UC14** | Thanh toán COD | Buyer | `POST /api/v1/payments/create` (method=COD) |
| **UC15** | Xem trạng thái thanh toán | Buyer | `GET /api/v1/payments/group/{id}/status` |
| **UC16** | Xem lịch sử đơn hàng | Buyer | `GET /api/v1/orders` |
| **UC17** | Xem chi tiết đơn hàng | Buyer | `GET /api/v1/orders/{id}` |
| **UC18** | Viết đánh giá | Buyer | `POST /api/v1/reviews` |
| **UC19** | Sửa đánh giá | Buyer | `PUT /api/v1/reviews/{reviewId}` |
| **UC20** | Xóa đánh giá | Buyer | `DELETE /api/v1/reviews/{reviewId}` |
| **UC21** | Áp dụng mã giảm giá | Buyer | `POST /api/v1/orders/preview` (voucherCodes) |
| **UC22** | Chat với người bán | Buyer + Vendor | STOMP `/app/chat.send` |
| **UC23** | Xem thông báo | Buyer, Vendor | `GET /api/v1/notifications` |
| **UC24** | Đăng ký mở gian hàng | Buyer | `POST /api/v1/shops` |
| **UC25** | Xem thông tin gian hàng mình | Vendor | `GET /api/v1/shops/my-shop` |
| **UC26** | Thêm sản phẩm | Vendor | `POST /api/v1/vendor/products` |
| **UC27** | Cập nhật sản phẩm | Vendor | `PUT /api/v1/vendor/products/{id}` |
| **UC28** | Ẩn/xóa sản phẩm | Vendor | `DELETE /api/v1/vendor/products/{id}` |
| **UC29** | Upload ảnh sản phẩm | Vendor + Cloudinary | `POST /api/v1/vendor/products/{id}/images` |
| **UC30** | Xóa ảnh sản phẩm | Vendor + Cloudinary | `DELETE /api/v1/vendor/products/{id}/images/{imgId}` |
| **UC31** | Xem đơn hàng gian hàng | Vendor | `GET /api/v1/vendor/orders` |
| **UC32** | Xem chi tiết đơn hàng gian hàng | Vendor | `GET /api/v1/vendor/orders/{id}` |
| **UC33** | Cập nhật trạng thái đơn | Vendor | `PATCH /api/v1/vendor/orders/{id}/status` |
| **UC34** | Tạo voucher gian hàng | Vendor | `POST /api/v1/vouchers` (scope=SHOP) |
| **UC35** | Xem voucher gian hàng | Vendor | `GET /api/v1/vouchers/my-shop` |
| **UC36** | Chat với người mua | Vendor + Buyer | STOMP `/app/chat.send` |
| **UC37** | Xem danh sách gian hàng | Admin | `GET /api/v1/admin/shops` |
| **UC38** | Duyệt gian hàng | Admin + Gmail | `PUT /api/v1/admin/shops/{id}/approve` |
| **UC39** | Xem đơn hàng toàn hệ thống | Admin | `GET /api/v1/admin/orders` |
| **UC40** | Xem chi tiết đơn hàng (Admin) | Admin | `GET /api/v1/admin/orders/{id}` |
| **UC41** | Tạo voucher hệ thống | Admin | `POST /api/v1/vouchers` (scope=SYSTEM) |
| **UC42** | Quản lý sản phẩm (Admin) | Admin | `GET/PUT/DELETE /api/v1/admin/products` |
| **UC43** | Xử lý IPN Webhook VNPay | VNPay | `GET /api/v1/payments/vnpay/ipn` |
| **UC44** | Gửi email duyệt shop | Gmail SMTP | `ShopNotificationListener` (AFTER_COMMIT) |
| **UC45** | Tạo thông báo theo sự kiện | System | `OrderNotificationListener`, `ReviewNotificationListener` |

---

### 3.3.3. Sơ đồ Use Case tổng thể

**Hình 3.7.** Sơ đồ Use Case tổng thể (text representation)

```
  ┌─────────────────────────────────────────────────────────────────────┐
  │                 HỆ THỐNG ENTERPRISE ECOMMERCE                       │
  │                                                                     │
  │  [QUẢN LÝ TÀI KHOẢN]                                               │
  │   UC01 Đăng ký ──── Guest                                          │
  │   UC02 Đăng nhập ── Guest/Buyer/Vendor/Admin                       │
  │                                                                     │
  │  [SẢN PHẨM - PUBLIC]                                               │
  │   UC03 Xem danh sách ── Guest, Buyer                               │
  │   UC04 Tìm kiếm ─────── Guest, Buyer                               │
  │   UC05 Xem chi tiết ─── Guest, Buyer                               │
  │   UC06 Xem đánh giá ─── Guest, Buyer                               │
  │                                                                     │
  │  [MUA SẮM - BUYER]                                                 │
  │   UC07-UC10 Quản lý giỏ hàng ── Buyer                              │
  │   UC11-UC12 Checkout ─────────── Buyer                             │
  │     UC12 <<include>> UC11 (preview)                                │
  │     UC12 <<include>> UC21 (áp dụng voucher)                        │
  │   UC13 Thanh toán VNPay ─ Buyer ─── VNPay (UC43 IPN callback)     │
  │   UC14 Thanh toán COD ── Buyer                                     │
  │   UC15-UC17 Theo dõi đơn ─ Buyer                                   │
  │   UC18-UC20 Đánh giá ─── Buyer                                     │
  │     UC18 <<extend>> [đã nhận hàng]                                 │
  │   UC22 Chat ──────────── Buyer ────── Vendor                       │
  │   UC23 Thông báo ─────── Buyer, Vendor                             │
  │                                                                     │
  │  [QUẢN LÝ GIAN HÀNG - VENDOR]                                      │
  │   UC24 Đăng ký shop ─── Buyer (trước khi có ROLE_VENDOR)          │
  │   UC25 Xem shop mình ── Vendor                                     │
  │   UC26-UC30 Quản lý sản phẩm ─ Vendor ─── Cloudinary (UC29,30)   │
  │   UC31-UC33 Quản lý đơn hàng ─ Vendor                             │
  │   UC34-UC35 Quản lý voucher ── Vendor                              │
  │   UC36 Chat ──────────────────── Vendor ─── Buyer                 │
  │                                                                     │
  │  [QUẢN TRỊ - ADMIN]                                                │
  │   UC37-UC38 Quản lý shop ──── Admin                                │
  │     UC38 <<include>> UC44 (gửi email)                              │
  │   UC39-UC40 Xem đơn hàng ──── Admin                               │
  │   UC41 Tạo voucher system ─── Admin                                │
  │   UC42 Quản lý sản phẩm ───── Admin                               │
  │                                                                     │
  │  [SYSTEM/EXTERNAL]                                                  │
  │   UC43 IPN VNPay ──── VNPay                                        │
  │   UC44 Email ─────── Gmail SMTP                                    │
  │   UC45 Notification ─ System (Event-Driven AFTER_COMMIT)           │
  └─────────────────────────────────────────────────────────────────────┘
```

---

### 3.3.4. Đặc tả Use Case chi tiết

#### UC01 — Đăng ký tài khoản

**Bảng 3.16.** Đặc tả UC01

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC01 |
| **Tên** | Đăng ký tài khoản |
| **Mô tả** | Người dùng tạo tài khoản mới bằng email và mật khẩu |
| **Actor chính** | Guest |
| **Actor phụ** | — |
| **Tiền điều kiện** | Email chưa tồn tại trong hệ thống |
| **Hậu điều kiện** | Tài khoản được tạo với `ROLE_USER`; hệ thống trả JWT token |

**Luồng chính:**
1. Guest gửi `POST /api/v1/auth/register` với `{firstName, lastName, email, password}`.
2. Hệ thống validate `@Valid` (email format, password not blank).
3. `AuthService.register()` kiểm tra email đã tồn tại chưa.
4. Mã hóa password bằng `BCryptPasswordEncoder`.
5. Tạo `User` entity với `ROLE_USER`, lưu vào database.
6. Tạo JWT token 24h.
7. Trả về `AuthResponse {token, userId, email, roles}` với HTTP 201.

**Luồng thay thế — Email đã tồn tại:**
- Bước 3: Email trùng → ném `DuplicateEmailException` → `GlobalExceptionHandler` trả HTTP 400.

**Luồng ngoại lệ — Validation thất bại:**
- Bước 2: Dữ liệu không hợp lệ → `MethodArgumentNotValidException` → HTTP 400.

---

#### UC02 — Đăng nhập

**Bảng 3.17.** Đặc tả UC02

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC02 |
| **Tên** | Đăng nhập |
| **Mô tả** | Xác thực bằng email/password, nhận JWT token |
| **Actor chính** | Guest (User/Vendor/Admin) |
| **Actor phụ** | — |
| **Tiền điều kiện** | Tài khoản đã tồn tại |
| **Hậu điều kiện** | Hệ thống trả JWT token; client lưu token cho các request tiếp theo |

**Luồng chính:**
1. Gửi `POST /api/v1/auth/login` với `{email, password}`.
2. `AuthService.authenticate()` gọi `AuthenticationManager.authenticate()`.
3. Spring Security tải `UserDetails` qua `UserDetailsServiceImpl`.
4. So khớp password BCrypt.
5. Tạo JWT token 24h chứa `userId` và `roles`.
6. Trả về `AuthResponse {token, userId, email, roles}` với HTTP 200.

**Luồng ngoại lệ — Sai thông tin:**
- Bước 4: Mật khẩu không khớp → `BadCredentialsException` → HTTP 401.

---

#### UC07 — Thêm sản phẩm vào giỏ hàng

**Bảng 3.18.** Đặc tả UC07

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC07 |
| **Tên** | Thêm sản phẩm vào giỏ hàng |
| **Mô tả** | Buyer chọn biến thể sản phẩm và số lượng, thêm vào giỏ |
| **Actor chính** | Buyer |
| **Actor phụ** | — |
| **Tiền điều kiện** | Buyer đã đăng nhập; sản phẩm tồn tại và đang active |
| **Hậu điều kiện** | CartItem được tạo hoặc cập nhật số lượng |

**Luồng chính:**
1. Buyer gửi `POST /api/v1/carts/items` với `{productVariantId, quantity}`.
2. `JwtAuthenticationFilter` xác minh JWT, gán `SecurityContext`.
3. `CartService.addToCart(userId, request)`.
4. Gọi `ProductInternalService.getProductVariant(variantId)` → lấy `ProductInternalData`.
5. Kiểm tra `variant.stockQuantity >= request.quantity`.
6. Kiểm tra CartItem (userId, variantId) đã tồn tại: nếu có thì cộng quantity; nếu chưa thì tạo mới.
7. Trả về HTTP 200.

**Luồng ngoại lệ — Hết hàng:**
- Bước 5: `stockQuantity < quantity` → `InsufficientStockException` → HTTP 400.

**Luồng ngoại lệ — Sản phẩm không tồn tại:**
- Bước 4: Variant không tìm thấy → `ProductNotFoundException` → HTTP 404.

---

#### UC12 — Đặt hàng (Checkout đa gian hàng)

**Bảng 3.19.** Đặc tả UC12

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC12 |
| **Tên** | Đặt hàng (Checkout đa gian hàng) |
| **Mô tả** | Buyer xác nhận đơn từ giỏ hàng; hệ thống tách đơn theo gian hàng, kiểm tra tồn kho, tính giảm giá, tạo đơn hàng nguyên tử |
| **Actor chính** | Buyer |
| **Actor phụ** | — |
| **Tiền điều kiện** | Buyer đã đăng nhập; giỏ hàng không rỗng; tất cả sản phẩm còn hàng |
| **Hậu điều kiện** | N đơn hàng được tạo (N = số gian hàng); `payment_group_id` được gán; giỏ hàng bị xóa |

**Luồng chính:**
1. Buyer gửi `POST /api/v1/orders/checkout` với `{cartItemIds, shippingAddress, paymentMethod, voucherCodes}`.
2. `OrderService.checkout(userId, request)` bắt đầu `@Transactional`.
3. Với mỗi cartItemId: gọi `ProductInternalService.getProductVariant()` kiểm tra tồn kho.
4. Gọi `VoucherInternalService.calculateDiscount(VoucherCalculationRequest)` → nhận `VoucherDiscountResult`.
5. Nhóm CartItem theo `shopId` → tạo N `Order` entity, mỗi Order có danh sách `OrderItem`.
6. Gán `payment_group_id` (UUID) chung cho tất cả N đơn hàng.
7. Trừ tồn kho từng biến thể (bulk update).
8. Tăng `used_count` voucher bằng atomic SQL update.
9. Xóa CartItem đã checkout.
10. COMMIT transaction.
11. Trả về `CheckoutResponse {paymentGroupId, orderIds, totalAmount}` với HTTP 201.

**Luồng thay thế — Một sản phẩm hết hàng:**
- Bước 3: Bất kỳ variant nào hết hàng → rollback toàn bộ transaction → HTTP 400.

**Luồng ngoại lệ — Voucher không hợp lệ:**
- Bước 4: Voucher hết lượt / hết hạn / giá trị đơn chưa đạt tối thiểu → HTTP 400.

---

#### UC13 — Thanh toán VNPay

**Bảng 3.20.** Đặc tả UC13

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC13 |
| **Tên** | Thanh toán qua VNPay |
| **Mô tả** | Buyer khởi tạo VNPay URL, redirect đến cổng thanh toán; VNPay gửi IPN callback để xác nhận |
| **Actor chính** | Buyer |
| **Actor phụ** | VNPay |
| **Tiền điều kiện** | Đã có `payment_group_id` hợp lệ từ UC12; phương thức là VNPAY |
| **Hậu điều kiện** | Payment entity với status SUCCESS; tất cả Order → CONFIRMED; Notification được tạo |

**Luồng chính:**
1. Buyer gửi `POST /api/v1/payments/create` với `{paymentGroupId, paymentMethod: VNPAY}`.
2. `PaymentController` gọi `OrderInternalService.validateGroupOwnershipAndStatus(paymentGroupId, userId)`.
3. Lấy `OrderPaymentDto` (amount, description) qua `OrderInternalService.getPaymentDataForGroup()`.
4. `PaymentGatewayService` chọn `VnPayStrategy` qua `PaymentStrategyFactory`.
5. `VnPayStrategy` tạo URL redirect có HMAC-SHA512 signature.
6. Trả về `{paymentUrl}` → frontend redirect browser đến VNPay.
7. Người dùng hoàn tất thanh toán tại VNPay.
8. VNPay gửi `GET /api/v1/payments/vnpay/ipn?vnp_ResponseCode=00&vnp_SecureHash=...`.
9. `processVnPayIpn()` xác minh HMAC-SHA512.
10. Lấy Payment theo `txnRef` với `SELECT ... FOR UPDATE` (Pessimistic Lock).
11. Kiểm tra `payment.status == PENDING` (idempotency check).
12. Cập nhật `Payment.status = SUCCESS`.
13. Cập nhật tất cả Order trong group → `CONFIRMED`.
14. Phát `OrderConfirmedEvent`.
15. COMMIT. Trả `{RspCode: "00"}` cho VNPay.
16. AFTER_COMMIT: tạo Notification cho Buyer và Vendor.

**Luồng thay thế — Giao dịch thất bại (IPN ResponseCode != 00):**
- Bước 9: `Payment.status = FAILED`; `Order.status` giữ nguyên `PENDING` → Buyer có thể thử lại.

**Luồng ngoại lệ — HMAC không hợp lệ:**
- Bước 9: Hash không khớp → từ chối xử lý, log cảnh báo.

**Luồng ngoại lệ — IPN trùng lặp (Race Condition):**
- Bước 11: `payment.status != PENDING` → bỏ qua xử lý (idempotent response).

---

#### UC26 — Thêm sản phẩm (Vendor)

**Bảng 3.21.** Đặc tả UC26

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC26 |
| **Tên** | Thêm sản phẩm |
| **Mô tả** | Vendor tạo sản phẩm mới với thông tin cơ bản và biến thể |
| **Actor chính** | Vendor |
| **Actor phụ** | — |
| **Tiền điều kiện** | Vendor đã đăng nhập; gian hàng đang ACTIVE |
| **Hậu điều kiện** | `Product` và các `ProductVariant` được tạo, liên kết `shopId` của Vendor |

**Luồng chính:**
1. Vendor gửi `POST /api/v1/vendor/products` với `{name, description, categoryId, variants: [{name, price, stockQuantity}]}`.
2. Spring Security kiểm tra `ROLE_VENDOR`.
3. `ProductService.createProduct(userId, request)`.
4. Gọi `ShopInternalService.getShopIdByUserId(userId)` → lấy `shopId`.
5. Tạo `Product` entity với `shopId`, `status = ACTIVE`.
6. Tạo từng `ProductVariant` entity.
7. Lưu vào database.
8. Trả về `ProductResponse` với HTTP 201.

**Luồng ngoại lệ — Chưa có gian hàng:**
- Bước 4: Không tìm thấy shop → `ShopNotFoundException` → HTTP 404.

---

#### UC29 — Upload ảnh sản phẩm

**Bảng 3.22.** Đặc tả UC29

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC29 |
| **Tên** | Upload ảnh sản phẩm |
| **Mô tả** | Vendor tải ảnh lên Cloudinary; URL và publicId được lưu vào database |
| **Actor chính** | Vendor |
| **Actor phụ** | Cloudinary |
| **Tiền điều kiện** | Sản phẩm đã được tạo; Vendor sở hữu sản phẩm |
| **Hậu điều kiện** | `ProductImage` được lưu với `imageUrl` và `publicId`; ảnh có trên Cloudinary CDN |

**Luồng chính:**
1. Vendor gửi `POST /api/v1/vendor/products/{id}/images` với file upload (multipart).
2. `ProductService` xác minh Vendor sở hữu sản phẩm.
3. `FileStorageService.upload(file)` gọi Cloudinary SDK.
4. Cloudinary trả về `{secure_url, public_id}`.
5. Lưu `ProductImage {productId, imageUrl, publicId}` vào database.
6. Trả về `{imageUrl, imageId}` với HTTP 201.

**Luồng ngoại lệ — Cloudinary lỗi:**
- Bước 3: SDK ném exception → HTTP 500.

---

#### UC33 — Cập nhật trạng thái đơn hàng (Vendor)

**Bảng 3.23.** Đặc tả UC33

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC33 |
| **Tên** | Cập nhật trạng thái đơn hàng |
| **Mô tả** | Vendor chuyển trạng thái đơn theo State Machine: CONFIRMED → SHIPPED → DELIVERED hoặc CONFIRMED → CANCELLED |
| **Actor chính** | Vendor |
| **Actor phụ** | — |
| **Tiền điều kiện** | Đơn hàng thuộc gian hàng Vendor; chuyển trạng thái hợp lệ theo State Machine |
| **Hậu điều kiện** | `Order.status` được cập nhật; Notification được tạo cho Buyer |

**Luồng chính:**
1. Vendor gửi `PATCH /api/v1/vendor/orders/{id}/status` với `{newStatus}`.
2. Spring Security kiểm tra `ROLE_VENDOR`.
3. `OrderService.updateVendorOrderStatus(userId, orderId, request)`.
4. Gọi `ShopInternalService.isShopOwner(userId, order.shopId)` → xác minh quyền.
5. Kiểm tra State Machine: `(currentStatus → newStatus)` có được phép không.
6. Cập nhật `Order.status`.
7. Phát semantic event (`OrderShippedEvent` / `OrderDeliveredEvent` / `OrderCancelledEvent`).
8. COMMIT transaction.
9. AFTER_COMMIT: `OrderNotificationListener` tạo Notification cho Buyer.
10. Trả về `OrderResponse` với HTTP 200.

**Luồng ngoại lệ — Chuyển trạng thái không hợp lệ:**
- Bước 5: `SHIPPED → CONFIRMED` không được phép → `InvalidOrderStateException` → HTTP 400.

**Luồng ngoại lệ — Không phải chủ shop:**
- Bước 4: `isShopOwner` trả false → HTTP 403.

---

#### UC38 — Duyệt gian hàng (Admin)

**Bảng 3.24.** Đặc tả UC38

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC38 |
| **Tên** | Duyệt gian hàng |
| **Mô tả** | Admin phê duyệt gian hàng PENDING, cấp quyền kinh doanh cho Vendor |
| **Actor chính** | Admin |
| **Actor phụ** | Gmail SMTP |
| **Tiền điều kiện** | Admin đã đăng nhập; gian hàng đang ở trạng thái PENDING |
| **Hậu điều kiện** | `Shop.status = ACTIVE`; Vendor nhận email thông báo |

**Luồng chính:**
1. Admin gửi `PUT /api/v1/admin/shops/{shopId}/approve`.
2. Spring Security kiểm tra `ROLE_ADMIN`.
3. `ShopService.approveShop(shopId)`.
4. Tải `Shop` từ database, kiểm tra status là PENDING.
5. Cập nhật `Shop.status = ACTIVE`.
6. Phát `ShopApprovedEvent`.
7. COMMIT transaction.
8. AFTER_COMMIT: `ShopNotificationListener` gửi email qua Gmail SMTP (async).
9. Trả về HTTP 200.

**Luồng ngoại lệ — Shop không PENDING:**
- Bước 4: `status != PENDING` → HTTP 400.

---

#### UC18 — Viết đánh giá sản phẩm

**Bảng 3.25.** Đặc tả UC18

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC18 |
| **Tên** | Viết đánh giá sản phẩm |
| **Mô tả** | Buyer viết đánh giá (rating + comment) cho sản phẩm đã mua và đã nhận |
| **Actor chính** | Buyer |
| **Actor phụ** | — |
| **Tiền điều kiện** | Buyer đã đăng nhập; đơn hàng chứa sản phẩm ở trạng thái DELIVERED; chưa đánh giá cho orderId này |
| **Hậu điều kiện** | `Review` được lưu; Notification được tạo cho Vendor |

**Luồng chính:**
1. Buyer gửi `POST /api/v1/reviews` với `{productId, orderId, rating (1–5), comment}`.
2. `ReviewService.createReview(userId, request)`.
3. Gọi `OrderInternalService.getDeliveredOrders(userId, productId)` → danh sách đơn đã DELIVERED.
4. Kiểm tra `request.orderId` nằm trong danh sách (đã mua và đã nhận).
5. Kiểm tra unique constraint `(orderId, productId)` — chưa đánh giá order này.
6. Lưu `Review {buyerId, productId, orderId, rating, comment}`.
7. Phát `ReviewCreatedEvent`.
8. COMMIT transaction.
9. AFTER_COMMIT: `ReviewNotificationListener` tạo Notification cho Vendor.
10. Trả về `ReviewResponse` với HTTP 201.

**Luồng ngoại lệ — Chưa nhận hàng:**
- Bước 4: `orderId` không trong danh sách DELIVERED → HTTP 403.

**Luồng ngoại lệ — Đã đánh giá:**
- Bước 5: Unique constraint vi phạm → HTTP 409.

---

#### UC22 — Chat realtime với người bán/mua

**Bảng 3.26.** Đặc tả UC22

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC22 |
| **Tên** | Chat realtime |
| **Mô tả** | Buyer gửi tin nhắn tức thì đến gian hàng qua WebSocket STOMP |
| **Actor chính** | Buyer |
| **Actor phụ** | Vendor |
| **Tiền điều kiện** | Cả hai đã kết nối WebSocket STOMP và xác thực JWT |
| **Hậu điều kiện** | `ChatMessage` được lưu database; cả Buyer và Shop owner nhận message qua STOMP push |

**Luồng chính:**
1. Buyer kết nối `HTTP GET /ws` (SockJS upgrade → 101 Switching Protocols).
2. Gửi `STOMP CONNECT` với header `Authorization: Bearer {token}`.
3. `JwtChannelInterceptor` xác thực JWT, gán `StompPrincipal(userId)`.
4. Buyer `SUBSCRIBE /user/queue/messages`.
5. Buyer gọi `POST /api/v1/chat/rooms {shopId}` để get-or-create `ChatRoom`.
6. Buyer gửi `STOMP SEND /app/chat.send {roomId, content}`.
7. `ChatMessageController` lưu `ChatMessage` vào database.
8. `convertAndSendToUser(shopOwnerId, "/queue/messages", response)` — push đến Vendor.
9. `convertAndSendToUser(buyerId, "/queue/messages", response)` — echo đến Buyer (Optimistic UI replace).
10. Vendor nhận tin nhắn tức thì.

**Đặc điểm Optimistic UI:** Frontend Buyer tạo tin nhắn với ID tạm âm ngay khi gửi (0ms latency). Khi server echo về với ID thật, frontend thay thế tin nhắn tạm.

---

*Kết thúc phần 3.3.*
