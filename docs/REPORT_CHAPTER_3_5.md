## 3.5. Thiết kế cơ sở dữ liệu hệ thống

Hệ thống Enterprise Ecommerce sử dụng MySQL làm hệ quản trị cơ sở dữ liệu quan hệ, với schema được quản lý tự động bởi Hibernate (`ddl-auto: update`). Phần này phân tích cấu trúc cơ sở dữ liệu thực tế được suy ra từ các Entity class, annotation, và Repository query trong source code.

---

### 3.5.1. Lược đồ cơ sở dữ liệu quan hệ

#### 3.5.1.1. Tổng quan các bảng

Cơ sở dữ liệu bao gồm 20 bảng, phân nhóm theo chức năng:

**Bảng 3.31.** Danh sách toàn bộ bảng trong cơ sở dữ liệu

| STT | Bảng | Module | Mô tả chức năng |
|---|---|---|---|
| 1 | `users` | identity | Lưu thông tin tài khoản người dùng |
| 2 | `roles` | identity | Lưu các vai trò trong hệ thống |
| 3 | `user_roles` | identity | Bảng trung gian User-Role (Many-to-Many) |
| 4 | `shops` | shop | Lưu thông tin gian hàng Vendor |
| 5 | `brands` | product | Lưu thương hiệu sản phẩm |
| 6 | `categories` | product | Lưu danh mục sản phẩm (cây đệ quy) |
| 7 | `products` | product | Lưu sản phẩm của các gian hàng |
| 8 | `product_variants` | product | Lưu biến thể sản phẩm (màu, size, ...) |
| 9 | `product_images` | product | Lưu ảnh sản phẩm trên Cloudinary |
| 10 | `carts` | cart | Lưu giỏ hàng của Buyer |
| 11 | `cart_items` | cart | Lưu từng sản phẩm trong giỏ hàng |
| 12 | `orders` | order | Lưu đơn hàng sau khi checkout |
| 13 | `order_items` | order | Lưu chi tiết từng mặt hàng trong đơn |
| 14 | `payments` | payment | Lưu giao dịch thanh toán |
| 15 | `vouchers` | voucher | Lưu mã giảm giá |
| 16 | `reviews` | review | Lưu đánh giá sản phẩm của Buyer |
| 17 | `review_images` | review | Lưu URL ảnh đính kèm đánh giá |
| 18 | `notifications` | notification | Lưu thông báo hệ thống |
| 19 | `chat_rooms` | chat | Lưu phòng chat Buyer-Shop |
| 20 | `chat_messages` | chat | Lưu tin nhắn trong phòng chat |

#### 3.5.1.2. Chi tiết từng bảng quan trọng

##### Bảng `users`

**Bảng 3.32.** Cấu trúc bảng `users`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `email` | VARCHAR(150) | NOT NULL | — | Email đăng nhập, duy nhất |
| `password_hash` | VARCHAR(255) | NOT NULL | — | Mật khẩu mã hóa BCrypt |
| `full_name` | VARCHAR(100) | NOT NULL | — | Họ tên đầy đủ |
| `phone` | VARCHAR(20) | NULL | NULL | Số điện thoại |
| `status` | VARCHAR(20) | NOT NULL | — | Trạng thái tài khoản |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`email`)

##### Bảng `roles`

**Bảng 3.33.** Cấu trúc bảng `roles`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `name` | VARCHAR(50) | NOT NULL | — | Tên vai trò: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`name`)

##### Bảng `user_roles` (Join Table)

**Bảng 3.34.** Cấu trúc bảng `user_roles`

| Cột | Kiểu dữ liệu | Null | Mô tả |
|---|---|---|---|
| `user_id` | BIGINT | NOT NULL | FK → users.id |
| `role_id` | BIGINT | NOT NULL | FK → roles.id |

**Constraints:** PRIMARY KEY (`user_id`, `role_id`), FK CASCADE DELETE khi User bị xóa

##### Bảng `shops`

**Bảng 3.35.** Cấu trúc bảng `shops`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | — | FK → users.id (UNIQUE: 1 user = 1 shop) |
| `name` | VARCHAR(100) | NOT NULL | — | Tên gian hàng, duy nhất |
| `description` | TEXT | NULL | NULL | Mô tả gian hàng |
| `status` | VARCHAR(20) | NOT NULL | — | PENDING / ACTIVE / BANNED |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`user_id`), UNIQUE KEY (`name`)

##### Bảng `categories`

**Bảng 3.36.** Cấu trúc bảng `categories`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `name` | VARCHAR(100) | NOT NULL | — | Tên danh mục, duy nhất |
| `description` | TEXT | NULL | NULL | Mô tả |
| `parent_id` | BIGINT | NULL | NULL | FK tự tham chiếu → categories.id |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`name`), FK (`parent_id`) ON DELETE SET NULL

##### Bảng `brands`

**Bảng 3.37.** Cấu trúc bảng `brands`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `name` | VARCHAR(100) | NOT NULL | — | Tên thương hiệu, duy nhất |
| `logo_url` | VARCHAR(255) | NULL | NULL | URL logo thương hiệu |
| `description` | TEXT | NULL | NULL | Mô tả thương hiệu |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`name`)

##### Bảng `products`

**Bảng 3.38.** Cấu trúc bảng `products`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `shop_id` | BIGINT | NOT NULL | — | FK (Loose) → shops.id |
| `name` | VARCHAR(255) | NOT NULL | — | Tên sản phẩm |
| `description` | TEXT | NULL | NULL | Mô tả sản phẩm |
| `price` | DECIMAL(15,2) | NOT NULL | — | Giá cơ bản |
| `stock_quantity` | INT | NOT NULL | — | Tổng tồn kho |
| `category_id` | BIGINT | NOT NULL | — | FK → categories.id |
| `brand_id` | BIGINT | NULL | NULL | FK → brands.id (nullable) |
| `status` | VARCHAR(20) | NOT NULL | 'ACTIVE' | ACTIVE / OUT_OF_STOCK / HIDDEN |

**Constraints:** PRIMARY KEY (`id`), FK (`category_id`), FK (`brand_id`)

**Indexes:**
- `idx_product_shop` ON (`shop_id`)
- `idx_product_status` ON (`status`)

##### Bảng `product_variants`

**Bảng 3.39.** Cấu trúc bảng `product_variants`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `product_id` | BIGINT | NOT NULL | — | FK → products.id |
| `sku` | VARCHAR(100) | NOT NULL | — | Stock Keeping Unit (duy nhất) |
| `price` | DECIMAL(15,2) | NOT NULL | — | Giá của biến thể này |
| `stock_quantity` | INT | NOT NULL | — | Tồn kho của biến thể |
| `attributes` | JSON | NULL | NULL | Thuộc tính động: {"color":"red","size":"L"} |
| `status` | VARCHAR(20) | NOT NULL | 'ACTIVE' | ACTIVE / OUT_OF_STOCK / HIDDEN |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`sku`), FK (`product_id`) ON DELETE CASCADE

##### Bảng `product_images`

**Bảng 3.40.** Cấu trúc bảng `product_images`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `product_id` | BIGINT | NOT NULL | — | FK → products.id |
| `image_url` | VARCHAR(255) | NOT NULL | — | URL ảnh trên Cloudinary CDN |
| `public_id` | VARCHAR(255) | NULL | NULL | Cloudinary publicId để xóa |
| `is_thumbnail` | TINYINT(1) | NULL | 0 | Đánh dấu ảnh bìa |
| `sort_order` | INT | NULL | 0 | Thứ tự hiển thị |

**Constraints:** PRIMARY KEY (`id`), FK (`product_id`) ON DELETE CASCADE

##### Bảng `carts`

**Bảng 3.41.** Cấu trúc bảng `carts`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id (UNIQUE) |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`user_id`)

##### Bảng `cart_items`

**Bảng 3.42.** Cấu trúc bảng `cart_items`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `cart_id` | BIGINT | NOT NULL | — | FK → carts.id (ON DELETE CASCADE) |
| `product_variant_id` | BIGINT | NOT NULL | — | FK (Loose) → product_variants.id |
| `quantity` | INT | NOT NULL | — | Số lượng |

**Constraints:** PRIMARY KEY (`id`), FK (`cart_id`) ON DELETE CASCADE

##### Bảng `orders`

**Bảng 3.43.** Cấu trúc bảng `orders`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id |
| `shop_id` | BIGINT | NOT NULL | — | FK (Loose) → shops.id |
| `payment_group_id` | VARCHAR(36) | NULL | NULL | UUID nhóm đơn hàng (multi-shop checkout) |
| `status` | VARCHAR(20) | NOT NULL | — | PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED |
| `total_amount` | DECIMAL(15,2) | NOT NULL | — | Tổng tiền sau giảm giá |
| `original_total_amount` | DECIMAL(15,2) | NOT NULL | 0 | Tổng tiền trước giảm giá |
| `discount_amount` | DECIMAL(15,2) | NOT NULL | 0 | Số tiền được giảm |
| `shipping_address` | VARCHAR(500) | NOT NULL | — | Địa chỉ giao hàng |
| `payment_method` | VARCHAR(20) | NOT NULL | — | VNPAY / MOMO / COD |
| `applied_voucher_id` | BIGINT | NULL | NULL | ID voucher đã áp dụng |
| `applied_voucher_code` | VARCHAR(50) | NULL | NULL | Mã voucher (snapshot) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm tạo đơn |
| `updated_at` | DATETIME | NULL | — | Thời điểm cập nhật cuối |

**Constraints:** PRIMARY KEY (`id`)

**Indexes:**
- `idx_order_user_status_created` ON (`user_id`, `status`, `created_at`)
- `idx_order_user_created` ON (`user_id`, `created_at`)
- `idx_order_shop_status_created` ON (`shop_id`, `status`, `created_at`)
- `idx_order_shop_created` ON (`shop_id`, `created_at`)
- `idx_order_payment_group` ON (`payment_group_id`)

##### Bảng `order_items`

**Bảng 3.44.** Cấu trúc bảng `order_items`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `order_id` | BIGINT | NOT NULL | — | FK → orders.id (ON DELETE CASCADE) |
| `product_variant_id` | BIGINT | NOT NULL | — | FK (Loose) → product_variants.id (snapshot ref) |
| `quantity` | INT | NOT NULL | — | Số lượng |
| `unit_price` | DECIMAL(15,2) | NOT NULL | — | **Snapshot giá** tại thời điểm đặt |
| `product_name` | VARCHAR(255) | NOT NULL | — | **Snapshot tên** tại thời điểm đặt |
| `sub_total` | DECIMAL(15,2) | NOT NULL | — | quantity × unit_price |

**Constraints:** PRIMARY KEY (`id`), FK (`order_id`) ON DELETE CASCADE

**Đặc biệt — Snapshot Pattern:** `unit_price` và `product_name` là bản sao dữ liệu tại thời điểm đặt hàng. Nếu Vendor thay đổi giá sau, đơn hàng cũ không bị ảnh hưởng. Đây là pattern quan trọng trong thiết kế Ecommerce.

##### Bảng `payments`

**Bảng 3.45.** Cấu trúc bảng `payments`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `payment_group_id` | VARCHAR(36) | NOT NULL | — | UUID liên kết nhóm đơn hàng |
| `amount` | DECIMAL(15,2) | NOT NULL | — | Số tiền giao dịch |
| `method` | VARCHAR(20) | NOT NULL | — | VNPAY / MOMO / COD |
| `status` | VARCHAR(20) | NOT NULL | — | PENDING / SUCCESS / FAILED / CANCELED |
| `transaction_id` | VARCHAR(100) | NOT NULL | — | Mã giao dịch nội bộ (UNIQUE) |
| `gateway_transaction_id` | VARCHAR(100) | NULL | NULL | Mã từ VNPay/MOMO |
| `raw_data` | TEXT | NULL | NULL | Toàn bộ response từ gateway (đối soát) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm tạo |
| `updated_at` | DATETIME | NULL | — | Thời điểm cập nhật |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`transaction_id`)

**Indexes:** `idx_payment_group_created` ON (`payment_group_id`, `created_at`)

**Liên kết với `orders`:** Không có FK trực tiếp. Liên kết qua `payment_group_id` — một Payment ứng với N Order cùng UUID. Đây là thiết kế đặc biệt để hỗ trợ multi-shop checkout.

##### Bảng `vouchers`

**Bảng 3.46.** Cấu trúc bảng `vouchers`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `code` | VARCHAR(50) | NOT NULL | — | Mã giảm giá (UNIQUE) |
| `scope` | VARCHAR(20) | NOT NULL | — | SYSTEM / SHOP |
| `shop_id` | BIGINT | NULL | NULL | FK (Loose) → shops.id; NULL nếu scope=SYSTEM |
| `type` | VARCHAR(20) | NOT NULL | — | PERCENTAGE / FIXED_AMOUNT |
| `discount_value` | DECIMAL(15,2) | NOT NULL | — | Giá trị giảm (% hoặc VND) |
| `max_discount` | DECIMAL(15,2) | NULL | NULL | Giới hạn tối đa cho PERCENTAGE |
| `min_order_value` | DECIMAL(15,2) | NULL | NULL | Giá trị đơn tối thiểu để áp dụng |
| `start_date` | DATETIME | NULL | NULL | Ngày bắt đầu hiệu lực |
| `end_date` | DATETIME | NULL | NULL | Ngày hết hiệu lực |
| `usage_limit` | INT | NULL | NULL | Số lần dùng tối đa (NULL = không giới hạn) |
| `used_count` | INT | NOT NULL | 0 | Số lần đã dùng (atomic increment) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | — |
| `updated_at` | DATETIME | NULL | — | — |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`code`)

**Đặc biệt:** `used_count` được cập nhật bằng atomic SQL `UPDATE vouchers SET used_count = used_count + 1 WHERE id = ? AND (usage_limit IS NULL OR used_count < usage_limit)` để tránh race condition trong môi trường concurrent.

##### Bảng `reviews`

**Bảng 3.47.** Cấu trúc bảng `reviews`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id |
| `product_id` | BIGINT | NOT NULL | — | FK (Loose) → products.id |
| `order_id` | BIGINT | NOT NULL | — | FK (Loose) → orders.id |
| `rating` | INT | NOT NULL | — | Điểm đánh giá: 1–5 |
| `comment` | VARCHAR(1000) | NOT NULL | — | Nội dung đánh giá |
| `vendor_reply` | TEXT | NULL | NULL | Phản hồi của Vendor |
| `vendor_replied_at` | DATETIME | NULL | NULL | Thời điểm Vendor phản hồi |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | — |
| `updated_at` | DATETIME | NULL | — | — |

**Constraints:**
- PRIMARY KEY (`id`)
- UNIQUE KEY `uk_user_order_product` (`user_id`, `order_id`, `product_id`)
- INDEX `idx_review_product_created` ON (`product_id`, `created_at`)

##### Bảng `review_images` (ElementCollection)

**Bảng 3.48.** Cấu trúc bảng `review_images`

| Cột | Kiểu dữ liệu | Null | Mô tả |
|---|---|---|---|
| `review_id` | BIGINT | NOT NULL | FK → reviews.id |
| `image_url` | VARCHAR(255) | NOT NULL | URL ảnh đính kèm đánh giá |

**Constraints:** FK (`review_id`) ON DELETE CASCADE

##### Bảng `notifications`

**Bảng 3.49.** Cấu trúc bảng `notifications`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id (người nhận) |
| `type` | VARCHAR(30) | NOT NULL | — | ORDER_CONFIRMED / ORDER_SHIPPED / ORDER_DELIVERED / NEW_ORDER / NEW_REVIEW / SYSTEM |
| `title` | VARCHAR(255) | NOT NULL | — | Tiêu đề thông báo |
| `message` | VARCHAR(500) | NOT NULL | — | Nội dung thông báo |
| `reference_id` | VARCHAR(50) | NULL | NULL | ID tham chiếu (orderId, reviewId...) |
| `is_read` | TINYINT(1) | NOT NULL | 0 | Đã đọc chưa |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | — |

**Indexes:** `idx_notif_user_created` ON (`user_id`, `created_at`)

##### Bảng `chat_rooms`

**Bảng 3.50.** Cấu trúc bảng `chat_rooms`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `buyer_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id |
| `shop_id` | BIGINT | NOT NULL | — | FK (Loose) → shops.id |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | — |
| `updated_at` | DATETIME | NULL | — | Cập nhật khi có tin nhắn mới |

**Constraints:** PRIMARY KEY (`id`), UNIQUE KEY (`buyer_id`, `shop_id`)

##### Bảng `chat_messages`

**Bảng 3.51.** Cấu trúc bảng `chat_messages`

| Cột | Kiểu dữ liệu | Null | Default | Mô tả |
|---|---|---|---|---|
| `id` | BIGINT | NOT NULL | AUTO_INCREMENT | Khóa chính |
| `room_id` | BIGINT | NOT NULL | — | FK (Loose) → chat_rooms.id |
| `sender_id` | BIGINT | NOT NULL | — | FK (Loose) → users.id |
| `content` | TEXT | NOT NULL | — | Nội dung tin nhắn |
| `is_read` | TINYINT(1) | NOT NULL | 0 | Đã đọc chưa |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | — |

**Indexes:** `idx_chat_room_created` ON (`room_id`, `created_at`)

---

### 3.5.2. Các mối quan hệ quan trọng

**Bảng 3.52.** Tổng hợp các mối quan hệ trong cơ sở dữ liệu

| Bảng cha | Bảng con | Loại quan hệ | Cột liên kết | Ghi chú |
|---|---|---|---|---|
| `users` | `user_roles` | 1:N | `user_roles.user_id` | JPA Join Table |
| `roles` | `user_roles` | 1:N | `user_roles.role_id` | JPA Join Table |
| `users` | `shops` | 1:1 | `shops.user_id` UNIQUE | 1 User = 1 Shop |
| `shops` | `products` | 1:N | `products.shop_id` | Loose Coupling |
| `categories` | `categories` | 1:N (self) | `categories.parent_id` | Cây danh mục |
| `categories` | `products` | 1:N | `products.category_id` | Tight Coupling |
| `brands` | `products` | 1:N | `products.brand_id` | Nullable |
| `products` | `product_variants` | 1:N | `product_variants.product_id` | Cascade ALL |
| `products` | `product_images` | 1:N | `product_images.product_id` | Cascade ALL |
| `users` | `carts` | 1:1 | `carts.user_id` UNIQUE | Loose Coupling |
| `carts` | `cart_items` | 1:N | `cart_items.cart_id` | Cascade ALL |
| `product_variants` | `cart_items` | 1:N | `cart_items.product_variant_id` | Loose Coupling |
| `users` | `orders` | 1:N | `orders.user_id` | Loose Coupling |
| `shops` | `orders` | 1:N | `orders.shop_id` | Loose Coupling |
| `orders` | `order_items` | 1:N | `order_items.order_id` | Cascade ALL |
| `product_variants` | `order_items` | 1:N | `order_items.product_variant_id` | Snapshot — Loose |
| `orders` | `payments` | N:1 (qua UUID) | `orders.payment_group_id = payments.payment_group_id` | Không có FK trực tiếp |
| `shops` | `vouchers` | 1:N | `vouchers.shop_id` | Loose; NULL = SYSTEM scope |
| `users` | `reviews` | 1:N | `reviews.user_id` | Loose Coupling |
| `products` | `reviews` | 1:N | `reviews.product_id` | Loose Coupling |
| `orders` | `reviews` | 1:N | `reviews.order_id` | Constraint: đã DELIVERED |
| `reviews` | `review_images` | 1:N | `review_images.review_id` | ElementCollection |
| `users` | `notifications` | 1:N | `notifications.user_id` | Loose Coupling |
| `users` | `chat_rooms` | 1:N | `chat_rooms.buyer_id` | Buyer role |
| `shops` | `chat_rooms` | 1:N | `chat_rooms.shop_id` | Loose Coupling |
| `chat_rooms` | `chat_messages` | 1:N | `chat_messages.room_id` | Loose Coupling |
| `users` | `chat_messages` | 1:N | `chat_messages.sender_id` | Loose Coupling |

#### 3.5.2.1. Quan hệ đặc biệt: Payment — Order

Quan hệ giữa `payments` và `orders` không sử dụng Foreign Key truyền thống mà thông qua `payment_group_id` (UUID):

```
payments.payment_group_id = orders.payment_group_id
```

Một giao dịch thanh toán (`Payment`) có thể ứng với N đơn hàng (`Order`) từ N gian hàng khác nhau trong cùng một lần checkout. Đây là thiết kế hỗ trợ tính năng **Multi-Vendor Checkout** — người mua có thể mua hàng từ nhiều shop cùng lúc và thanh toán một lần duy nhất.

---

### 3.5.3. Các ràng buộc dữ liệu

#### 3.5.3.1. Ràng buộc khóa chính (Primary Key)

Tất cả 20 bảng đều sử dụng `id BIGINT AUTO_INCREMENT` làm khóa chính, ngoại trừ `user_roles` dùng khóa chính ghép `(user_id, role_id)`.

#### 3.5.3.2. Ràng buộc duy nhất (Unique Key)

**Bảng 3.53.** Các ràng buộc UNIQUE trong cơ sở dữ liệu

| Bảng | Unique Constraint | Ý nghĩa nghiệp vụ |
|---|---|---|
| `users` | `email` | Không cho phép 2 tài khoản cùng email |
| `roles` | `name` | Không trùng tên vai trò |
| `shops` | `user_id` | 1 User chỉ có 1 Shop |
| `shops` | `name` | Tên gian hàng không trùng nhau |
| `categories` | `name` | Tên danh mục không trùng |
| `brands` | `name` | Tên thương hiệu không trùng |
| `product_variants` | `sku` | SKU phải duy nhất toàn hệ thống |
| `carts` | `user_id` | 1 User chỉ có 1 Cart |
| `payments` | `transaction_id` | Mã giao dịch không được trùng |
| `vouchers` | `code` | Mã voucher không được trùng |
| `reviews` | `(user_id, order_id, product_id)` | 1 User chỉ đánh giá 1 sản phẩm 1 lần trên 1 đơn |
| `chat_rooms` | `(buyer_id, shop_id)` | 1 cặp Buyer-Shop chỉ có 1 phòng chat |

#### 3.5.3.3. Ràng buộc Enum (String-stored)

Hibernate lưu Enum dưới dạng VARCHAR với `@Enumerated(EnumType.STRING)`:

**Bảng 3.54.** Các cột Enum trong cơ sở dữ liệu

| Bảng | Cột | Giá trị hợp lệ |
|---|---|---|
| `shops` | `status` | PENDING, ACTIVE, BANNED |
| `products` | `status` | ACTIVE, OUT_OF_STOCK, HIDDEN |
| `product_variants` | `status` | ACTIVE, OUT_OF_STOCK, HIDDEN |
| `orders` | `status` | PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED |
| `payments` | `method` | VNPAY, MOMO, COD |
| `payments` | `status` | PENDING, SUCCESS, FAILED, CANCELED |
| `vouchers` | `scope` | SYSTEM, SHOP |
| `vouchers` | `type` | PERCENTAGE, FIXED_AMOUNT |
| `notifications` | `type` | ORDER_CONFIRMED, ORDER_SHIPPED, ORDER_DELIVERED, NEW_ORDER, NEW_REVIEW, SYSTEM |

#### 3.5.3.4. Ràng buộc Cascade

**Bảng 3.55.** Các ràng buộc Cascade

| Parent | Child | Cascade Type | Orphan Removal |
|---|---|---|---|
| `products` | `product_variants` | ALL (Insert, Update, Delete) | true |
| `products` | `product_images` | ALL | true |
| `carts` | `cart_items` | ALL | true |
| `orders` | `order_items` | ALL | true |
| `reviews` | `review_images` | ALL (ElementCollection) | true |
| `categories` | `subcategories` | ALL | false |

#### 3.5.3.5. Index và hiệu năng truy vấn

**Bảng 3.56.** Danh sách Index trong cơ sở dữ liệu

| Bảng | Index Name | Columns | Mục đích |
|---|---|---|---|
| `products` | `idx_product_shop` | `shop_id` | Lấy sản phẩm theo gian hàng |
| `products` | `idx_product_status` | `status` | Filter sản phẩm theo trạng thái |
| `orders` | `idx_order_user_status_created` | `user_id, status, created_at` | Buyer xem lịch sử đơn với filter |
| `orders` | `idx_order_user_created` | `user_id, created_at` | Buyer xem tất cả đơn, sort mới nhất |
| `orders` | `idx_order_shop_status_created` | `shop_id, status, created_at` | Vendor quản lý đơn với filter |
| `orders` | `idx_order_shop_created` | `shop_id, created_at` | Vendor xem tất cả đơn |
| `orders` | `idx_order_payment_group` | `payment_group_id` | Tìm đơn theo payment group (IPN) |
| `payments` | `idx_payment_group_created` | `payment_group_id, created_at` | Kiểm tra trạng thái thanh toán |
| `reviews` | `idx_review_product_created` | `product_id, created_at` | Lấy đánh giá sản phẩm, sort mới nhất |
| `notifications` | `idx_notif_user_created` | `user_id, created_at` | Lấy thông báo của user, sort mới nhất |
| `chat_messages` | `idx_chat_room_created` | `room_id, created_at` | Lấy lịch sử chat theo phòng |

#### 3.5.3.6. Ràng buộc nghiệp vụ quan trọng

Ngoài các ràng buộc cơ sở dữ liệu thuần túy, hệ thống còn áp dụng các ràng buộc nghiệp vụ tại tầng Service:

**Bảng 3.57.** Ràng buộc nghiệp vụ thực thi tại tầng ứng dụng

| Ràng buộc | Nơi thực thi | Mô tả |
|---|---|---|
| Buyer chỉ đánh giá khi DELIVERED | `ReviewService` | Kiểm tra `orders.status = DELIVERED` qua `OrderInternalService` |
| Voucher dùng atomic | `VoucherRepository` | `UPDATE ... WHERE used_count < usage_limit` — atomic SQL |
| Payment Idempotency | `PaymentGatewayService` | Kiểm tra `payment.status == PENDING` trước khi xử lý IPN |
| Pessimistic Lock (IPN) | `PaymentRepository` | `@Lock(PESSIMISTIC_WRITE)` chống race condition |
| Stock deduction atomic | `ProductVariantRepository` | `UPDATE ... SET stock_quantity = stock_quantity - ?` |
| Vendor chỉ quản lý shop mình | `ShopInternalService` | `isShopOwner(userId, shopId)` |
| Checkout Transaction nguyên tử | `OrderService` | `@Transactional` — rollback nếu bất kỳ bước nào thất bại |
| Shop UNIQUE per user | DB UNIQUE KEY | `shops.user_id` UNIQUE — 1 user = 1 shop |
| Review UNIQUE per order | DB UNIQUE KEY | `uk_user_order_product` — không đánh giá 2 lần |

#### 3.5.3.7. Kiểu dữ liệu tiền tệ

Tất cả các cột tiền tệ trong hệ thống đều sử dụng `DECIMAL(15, 2)` thay vì `DOUBLE` hay `FLOAT`, nhằm tránh sai số dấu phẩy động — lỗi nghiêm trọng trong hệ thống tài chính. Trong Java, kiểu tương ứng là `BigDecimal`. Đây là thực hành bắt buộc trong thiết kế Ecommerce.

---

*Kết thúc phần 3.5.*
