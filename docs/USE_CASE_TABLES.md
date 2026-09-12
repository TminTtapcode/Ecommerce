# USE CASE TABLES — Bảng đặc tả Use Case tổng hợp

> Tài liệu này liệt kê toàn bộ 45 Use Case dưới dạng bảng tóm tắt.
> Tất cả đều có bằng chứng trong source code.

---

## Nhóm 1: Quản lý tài khoản

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC01 | Đăng ký tài khoản | Guest | `POST /api/v1/auth/register` | Email chưa tồn tại | User tạo với ROLE_USER; JWT trả về |
| UC02 | Đăng nhập | Guest/Buyer/Vendor/Admin | `POST /api/v1/auth/login` | Tài khoản tồn tại | JWT token 24h trả về |

---

## Nhóm 2: Sản phẩm (Public — không cần đăng nhập)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC03 | Xem danh sách sản phẩm | Guest, Buyer | `GET /api/v1/products` | — | Danh sách sản phẩm phân trang |
| UC04 | Tìm kiếm sản phẩm | Guest, Buyer | `GET /api/v1/products?keyword=` | — | Kết quả tìm kiếm |
| UC05 | Xem chi tiết sản phẩm | Guest, Buyer | `GET /api/v1/products/{id}` | Sản phẩm tồn tại | Thông tin đầy đủ sản phẩm, biến thể, ảnh |
| UC06 | Xem đánh giá sản phẩm | Guest, Buyer | `GET /api/v1/products/{id}/reviews` | Sản phẩm tồn tại | Danh sách đánh giá phân trang |

---

## Nhóm 3: Giỏ hàng (Buyer)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC07 | Thêm sản phẩm vào giỏ | Buyer | `POST /api/v1/carts/items` | Đã đăng nhập; sản phẩm active, còn hàng | CartItem tạo hoặc quantity tăng |
| UC08 | Cập nhật số lượng trong giỏ | Buyer | `PUT /api/v1/carts/items/{itemId}` | CartItem thuộc giỏ của Buyer | Quantity được cập nhật |
| UC09 | Xóa sản phẩm khỏi giỏ | Buyer | `DELETE /api/v1/carts/items/{itemId}` | CartItem thuộc giỏ của Buyer | CartItem bị xóa |
| UC10 | Xem giỏ hàng | Buyer | `GET /api/v1/carts` | Đã đăng nhập | Danh sách CartItem với thông tin sản phẩm |

---

## Nhóm 4: Đặt hàng & Thanh toán (Buyer)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC11 | Xem trước tổng giá trị đơn | Buyer | `POST /api/v1/orders/preview` | Đã đăng nhập | Giá tổng, giảm giá, phí vận chuyển tính trước |
| UC12 | Đặt hàng (Checkout) | Buyer | `POST /api/v1/orders/checkout` | Giỏ không rỗng; tồn kho đủ | N Order tạo với paymentGroupId; giỏ xóa |
| UC13 | Thanh toán VNPay | Buyer + VNPay | `POST /api/v1/payments/create` + IPN | paymentGroupId hợp lệ; method=VNPAY | Payment SUCCESS; Order CONFIRMED; Notification tạo |
| UC14 | Thanh toán COD | Buyer | `POST /api/v1/payments/create` | paymentGroupId hợp lệ; method=COD | Payment SUCCESS ngay lập tức; Order CONFIRMED |
| UC15 | Xem trạng thái thanh toán | Buyer | `GET /api/v1/payments/group/{id}/status` | paymentGroupId tồn tại | Trạng thái tổng hợp của payment group |

---

## Nhóm 5: Lịch sử đơn hàng (Buyer)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC16 | Xem lịch sử đơn hàng | Buyer | `GET /api/v1/orders` | Đã đăng nhập | Danh sách Order phân trang, filter by status |
| UC17 | Xem chi tiết đơn hàng | Buyer | `GET /api/v1/orders/{id}` | Order thuộc buyerId | Thông tin đầy đủ Order và OrderItem |

---

## Nhóm 6: Đánh giá (Buyer)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC18 | Viết đánh giá | Buyer | `POST /api/v1/reviews` | Order chứa sản phẩm đã DELIVERED; chưa đánh giá orderId này | Review lưu; Notification cho Vendor |
| UC19 | Sửa đánh giá | Buyer | `PUT /api/v1/reviews/{reviewId}` | Review thuộc Buyer | Review được cập nhật |
| UC20 | Xóa đánh giá | Buyer | `DELETE /api/v1/reviews/{reviewId}` | Review thuộc Buyer | Review bị xóa |
| UC21 | Áp dụng mã giảm giá | Buyer | (via UC12 và UC11) | Voucher hợp lệ, còn lượt, đơn đạt tối thiểu | Discount được tính và áp dụng |

---

## Nhóm 7: Chat & Thông báo (Buyer + Vendor)

| UC ID | Tên | Actor | Protocol | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC22 | Chat với người bán | Buyer | STOMP `/app/chat.send` | Đã kết nối WebSocket và xác thực JWT | ChatMessage lưu; push đến cả 2 bên |
| UC23 | Xem thông báo | Buyer, Vendor | `GET /api/v1/notifications` | Đã đăng nhập | Danh sách Notification phân trang |
| UC36 | Chat với người mua | Vendor | STOMP `/app/chat.send` | Đã kết nối WebSocket và xác thực JWT | ChatMessage lưu; push đến cả 2 bên |

---

## Nhóm 8: Quản lý gian hàng (Vendor)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC24 | Đăng ký mở gian hàng | Buyer | `POST /api/v1/shops` | Đã đăng nhập | Shop tạo với status PENDING; chờ Admin duyệt |
| UC25 | Xem thông tin gian hàng | Vendor | `GET /api/v1/shops/my-shop` | Đã đăng nhập; shop đã tồn tại | Thông tin shop |
| UC26 | Thêm sản phẩm | Vendor | `POST /api/v1/vendor/products` | Shop ACTIVE | Product và ProductVariant tạo |
| UC27 | Cập nhật sản phẩm | Vendor | `PUT /api/v1/vendor/products/{id}` | Product thuộc shop Vendor | Product cập nhật |
| UC28 | Ẩn/xóa sản phẩm | Vendor | `DELETE /api/v1/vendor/products/{id}` | Product thuộc shop Vendor | Product status = HIDDEN |
| UC29 | Upload ảnh sản phẩm | Vendor + Cloudinary | `POST /api/v1/vendor/products/{id}/images` | Product thuộc Vendor | ProductImage lưu với URL và publicId |
| UC30 | Xóa ảnh sản phẩm | Vendor + Cloudinary | `DELETE /api/v1/vendor/products/{id}/images/{imgId}` | ProductImage thuộc Vendor | ProductImage xóa; Cloudinary file xóa (afterCommit) |

---

## Nhóm 9: Quản lý đơn hàng gian hàng (Vendor)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC31 | Xem đơn hàng gian hàng | Vendor | `GET /api/v1/vendor/orders` | Đã đăng nhập; có shop | Danh sách Order của shop, filter by status |
| UC32 | Xem chi tiết đơn hàng gian hàng | Vendor | `GET /api/v1/vendor/orders/{id}` | Order thuộc shop Vendor | Thông tin Order đầy đủ |
| UC33 | Cập nhật trạng thái đơn | Vendor | `PATCH /api/v1/vendor/orders/{id}/status` | Order thuộc shop; State Machine hợp lệ | Order.status cập nhật; Notification cho Buyer |

**State Machine Vendor Order:**
```
CONFIRMED → SHIPPED → DELIVERED
CONFIRMED → CANCELLED
```

---

## Nhóm 10: Voucher (Vendor + Admin)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC34 | Tạo voucher gian hàng | Vendor | `POST /api/v1/vouchers` (scope=SHOP) | Đã đăng nhập với ROLE_VENDOR | Voucher SHOP tạo với shopId của Vendor |
| UC35 | Xem voucher gian hàng | Vendor | `GET /api/v1/vouchers/my-shop` | Đã đăng nhập với ROLE_VENDOR | Danh sách Voucher của shop |
| UC41 | Tạo voucher hệ thống | Admin | `POST /api/v1/vouchers` (scope=SYSTEM) | Đã đăng nhập với ROLE_ADMIN | Voucher SYSTEM tạo (không có shopId) |

---

## Nhóm 11: Quản trị hệ thống (Admin)

| UC ID | Tên | Actor | Endpoint | Pre-condition | Post-condition |
|---|---|---|---|---|---|
| UC37 | Xem danh sách gian hàng | Admin | `GET /api/v1/admin/shops` | ROLE_ADMIN | Danh sách Shop phân trang, filter by status |
| UC38 | Duyệt gian hàng | Admin + Gmail | `PUT /api/v1/admin/shops/{id}/approve` | Shop đang PENDING | Shop.status = ACTIVE; email gửi cho Vendor |
| UC39 | Xem đơn hàng toàn hệ thống | Admin | `GET /api/v1/admin/orders` | ROLE_ADMIN | Tất cả Order phân trang |
| UC40 | Xem chi tiết đơn hàng (Admin) | Admin | `GET /api/v1/admin/orders/{id}` | ROLE_ADMIN | Thông tin Order đầy đủ (view-only) |
| UC42 | Quản lý sản phẩm (Admin) | Admin | `GET/PUT/DELETE /api/v1/admin/products` | ROLE_ADMIN | Xem và chỉnh sửa bất kỳ sản phẩm nào |

---

## Nhóm 12: System / External

| UC ID | Tên | Actor | Trigger | Cơ chế |
|---|---|---|---|---|
| UC43 | Xử lý IPN Webhook VNPay | VNPay | `GET /api/v1/payments/vnpay/ipn` | HMAC-SHA512 verify + Pessimistic Lock |
| UC44 | Gửi email thông báo duyệt shop | Gmail SMTP | `ShopApprovedEvent` AFTER_COMMIT | `@Async` + `JavaMailSender` |
| UC45 | Tạo Notification theo sự kiện | System | Domain Events AFTER_COMMIT | `@TransactionalEventListener` + `REQUIRES_NEW` |

**UC45 — Các sự kiện trigger Notification:**

| Sự kiện | Listener | Notification cho |
|---|---|---|
| `OrderConfirmedEvent` | `OrderNotificationListener` | Buyer (đặt hàng thành công) + Vendor (có đơn mới) |
| `OrderShippedEvent` | `OrderNotificationListener` | Buyer (hàng đang giao) |
| `OrderDeliveredEvent` | `OrderNotificationListener` | Buyer (đã nhận hàng) |
| `OrderCancelledEvent` | `OrderNotificationListener` | Buyer (đơn bị hủy) |
| `ReviewCreatedEvent` | `ReviewNotificationListener` | Vendor (có đánh giá mới) |
| `ShopApprovedEvent` | `ShopNotificationListener` | Vendor (shop được duyệt) + Gmail email |

---

## Tổng kết

| Nhóm | Số UC | Actor chính |
|---|---|---|
| Quản lý tài khoản | 2 | Guest |
| Sản phẩm (Public) | 4 | Guest, Buyer |
| Giỏ hàng | 4 | Buyer |
| Đặt hàng & Thanh toán | 5 | Buyer + VNPay |
| Lịch sử đơn | 2 | Buyer |
| Đánh giá | 4 | Buyer |
| Chat & Thông báo | 3 | Buyer, Vendor |
| Quản lý gian hàng | 7 | Vendor + Cloudinary |
| Quản lý đơn (Vendor) | 3 | Vendor |
| Voucher | 3 | Vendor, Admin |
| Quản trị | 5 | Admin + Gmail |
| System/External | 3 | VNPay, Gmail, System |
| **Tổng** | **45** | |
