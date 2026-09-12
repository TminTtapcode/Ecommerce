# DATABASE TABLE SPECIFICATION

> Tài liệu chi tiết từng bảng, bao gồm Entity → Repository → Service → Controller/API.
> Source: Hibernate `ddl-auto: update` — schema sinh từ Entity annotations.
> Database: MySQL, kết nối qua `jdbc:mysql://localhost:3306/ecommerce`

---

## Quy ước ký hiệu

| Ký hiệu | Ý nghĩa |
|---|---|
| PK | Primary Key |
| FK | Foreign Key |
| UK | Unique Key |
| NN | Not Null |
| AI | Auto Increment |
| (Tight) | @ManyToOne — cùng module |
| (Loose) | Chỉ lưu Long ID — cross-module |

---

## 1. Bảng `users`

**Mục đích:** Lưu thông tin tài khoản người dùng toàn hệ thống.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `email` | VARCHAR(150) | UK, NN | Email đăng nhập duy nhất |
| `password_hash` | VARCHAR(255) | NN | BCrypt hash |
| `full_name` | VARCHAR(100) | NN | Họ tên đầy đủ |
| `phone` | VARCHAR(20) | NULL | Số điện thoại (tùy chọn) |
| `status` | VARCHAR(20) | NN | Trạng thái: ACTIVE / INACTIVE / BANNED |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.identity.entity.User` |
| Repository | `com.tmt.ecommerce.identity.repository.UserRepository` |
| Service | `com.tmt.ecommerce.identity.service.AuthenticationService`, `UserDetailsServiceImpl` |
| Controller | `com.tmt.ecommerce.identity.controller.AuthController` |
| API | `POST /api/v1/auth/register`, `POST /api/v1/auth/login` |

### Repository methods
```java
Optional<User> findByEmail(String email);
boolean existsByEmail(String email);
```

---

## 2. Bảng `roles`

**Mục đích:** Lưu các vai trò: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `name` | VARCHAR(50) | UK, NN | Tên vai trò |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.identity.entity.Role` |
| Repository | `com.tmt.ecommerce.identity.repository.RoleRepository` |
| Service | `AuthenticationService` (gán ROLE_USER khi đăng ký, gán ROLE_VENDOR khi shop được duyệt) |
| Controller | Không expose API trực tiếp |

### Repository methods
```java
Optional<Role> findByName(String name);
boolean existsByName(String name);
```

---

## 3. Bảng `user_roles` (Join Table)

**Mục đích:** Bảng trung gian Many-to-Many giữa User và Role.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `user_id` | BIGINT | FK → users.id, NN | Khóa ghép 1 |
| `role_id` | BIGINT | FK → roles.id, NN | Khóa ghép 2 |

**PRIMARY KEY:** `(user_id, role_id)`

**JPA:** `@JoinTable(name = "user_roles", joinColumns = @JoinColumn(name = "user_id"), inverseJoinColumns = @JoinColumn(name = "role_id"))`
**Fetch:** `FetchType.EAGER` — roles được load cùng với User

### Liên kết module
Được quản lý hoàn toàn bởi JPA, không có Repository riêng.

---

## 4. Bảng `shops`

**Mục đích:** Lưu thông tin gian hàng Vendor. Mỗi User tối đa 1 Shop.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `user_id` | BIGINT | UK, NN | FK (Loose) → users.id |
| `name` | VARCHAR(100) | UK, NN | Tên gian hàng duy nhất |
| `description` | TEXT | NULL | Mô tả gian hàng |
| `status` | VARCHAR(20) | NN | PENDING / ACTIVE / BANNED |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.shop.entity.Shop` |
| Repository | `com.tmt.ecommerce.shop.repository.ShopRepository` |
| Service | `ShopService`, `ShopInternalServiceImpl` |
| Controller | `ShopController`, `AdminShopController` |
| API | `POST /api/v1/shops`, `GET /api/v1/shops/my-shop`, `PUT /api/v1/admin/shops/{id}/approve` |

### Repository methods
```java
boolean existsByUserId(Long userId);
boolean existsByName(String name);
boolean existsByIdAndUserId(Long id, Long userId);
Optional<Shop> findByUserId(Long userId);
Page<Shop> findByStatus(ShopStatus status, Pageable pageable);
```

### Business Rules
- Khi Admin approve: `status` → ACTIVE, User nhận thêm ROLE_VENDOR, `ShopApprovedEvent` published → Email gửi qua Gmail SMTP (AFTER_COMMIT async).
- Kiểm tra `existsByUserId()` để đảm bảo 1 User = 1 Shop.

---

## 5. Bảng `brands`

**Mục đích:** Lưu thương hiệu sản phẩm (Brand). Sản phẩm có thể có hoặc không có brand.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `name` | VARCHAR(100) | UK, NN | Tên thương hiệu |
| `logo_url` | VARCHAR(255) | NULL | URL logo |
| `description` | TEXT | NULL | Mô tả |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.product.entity.Brand` |
| Repository | `com.tmt.ecommerce.product.repository.BrandRepository` |
| Service | `ProductService` (validate brandId khi tạo sản phẩm) |
| Controller | Có endpoint quản lý Brand (Admin) |

---

## 6. Bảng `categories`

**Mục đích:** Lưu danh mục sản phẩm dạng cây đệ quy (self-referencing).

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `name` | VARCHAR(100) | UK, NN | Tên danh mục |
| `description` | TEXT | NULL | Mô tả |
| `parent_id` | BIGINT | FK → categories.id, NULL | NULL = danh mục gốc (Root) |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.product.entity.Category` |
| Repository | `com.tmt.ecommerce.product.repository.CategoryRepository` |
| Service | `ProductService`, `CategoryService` |
| Controller | `CategoryController` |
| API | `GET /api/v1/categories`, `GET /api/v1/categories/{id}` |

### Repository methods
```java
List<Category> findByParentIsNull(); // Lấy root categories
```

### Cấu trúc cây
```
Thời trang (parent_id=NULL)
  ├── Áo (parent_id=1)
  │     ├── Áo phông (parent_id=2)
  │     └── Áo sơ mi (parent_id=2)
  └── Quần (parent_id=1)
Điện tử (parent_id=NULL)
  └── Điện thoại (parent_id=7)
```

---

## 7. Bảng `products`

**Mục đích:** Lưu thông tin sản phẩm do Vendor đăng bán.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `shop_id` | BIGINT | NN | FK (Loose) → shops.id |
| `name` | VARCHAR(255) | NN | Tên sản phẩm |
| `description` | TEXT | NULL | Mô tả chi tiết |
| `price` | DECIMAL(15,2) | NN | Giá cơ bản |
| `stock_quantity` | INT | NN | Tổng tồn kho (sum variants) |
| `category_id` | BIGINT | FK → categories.id, NN | Danh mục (Tight) |
| `brand_id` | BIGINT | FK → brands.id, NULL | Thương hiệu (nullable) |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | ACTIVE / OUT_OF_STOCK / HIDDEN |

### Indexes
```sql
INDEX idx_product_shop (shop_id)
INDEX idx_product_status (status)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.product.entity.Product` |
| Repository | `com.tmt.ecommerce.product.repository.ProductRepository` |
| Service | `ProductService` (public), `VendorProductService` |
| Controller | `ProductController` (public), `VendorProductController` |
| API | `GET /api/v1/products`, `GET /api/v1/products/{id}`, `POST /api/v1/vendor/products`, `PUT /api/v1/vendor/products/{id}`, `DELETE /api/v1/vendor/products/{id}` |

### Business Rules
- `status = HIDDEN` là soft delete (không xóa vật lý).
- `stock_quantity = 0` → status tự động hoặc thủ công chuyển `OUT_OF_STOCK`.
- Chỉ sản phẩm `ACTIVE` mới hiển thị trên public catalog.

---

## 8. Bảng `product_variants`

**Mục đích:** Lưu các biến thể của sản phẩm (màu sắc, kích thước, chất liệu...).

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `product_id` | BIGINT | FK → products.id, NN | Sản phẩm gốc |
| `sku` | VARCHAR(100) | UK, NN | Stock Keeping Unit |
| `price` | DECIMAL(15,2) | NN | Giá của biến thể này |
| `stock_quantity` | INT | NN | Tồn kho riêng |
| `attributes` | JSON | NULL | `{"color":"red","size":"L","material":"cotton"}` |
| `status` | VARCHAR(20) | NN, DEFAULT 'ACTIVE' | ACTIVE / OUT_OF_STOCK / HIDDEN |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.product.entity.ProductVariant` |
| Repository | `com.tmt.ecommerce.product.repository.ProductVariantRepository` |
| Service | `ProductService`, `ProductInternalServiceImpl`, `CartService`, `OrderService` |
| Controller | (qua Product controller) |

### Cross-module usage
```
CartService     → ProductInternalService.getProductVariant(variantId)
OrderService    → ProductInternalService.getProductVariant(variantId) + decrementStock()
```

### Business Rules
- `sku` UNIQUE toàn hệ thống — quan trọng cho quản lý kho.
- `attributes` là JSON linh hoạt — không cần schema cứng cho từng loại biến thể.
- Khi checkout: `stock_quantity` bị trừ atomic: `UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ?`.

---

## 9. Bảng `product_images`

**Mục đích:** Lưu ảnh sản phẩm được upload lên Cloudinary CDN.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `product_id` | BIGINT | FK → products.id, NN | Sản phẩm sở hữu ảnh |
| `image_url` | VARCHAR(255) | NN | CDN URL trả về từ Cloudinary |
| `public_id` | VARCHAR(255) | NULL | Cloudinary publicId (dùng để xóa) |
| `is_thumbnail` | TINYINT(1) | NULL, DEFAULT 0 | 1 = ảnh bìa |
| `sort_order` | INT | NULL, DEFAULT 0 | Thứ tự hiển thị |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.product.entity.ProductImage` |
| Repository | `com.tmt.ecommerce.product.repository.ProductImageRepository` |
| Service | `ProductImageService`, `FileStorageService` (Cloudinary) |
| Controller | `VendorProductController` |
| API | `POST /api/v1/vendor/products/{id}/images`, `DELETE /api/v1/vendor/products/{id}/images/{imgId}` |

### Cloudinary Integration
- Upload: `FileStorageService.upload(MultipartFile)` → Cloudinary SDK → trả về `{secure_url, public_id}` → lưu vào DB.
- Delete: DB record xóa trước (trong transaction), Cloudinary file xóa sau `AFTER_COMMIT` để tránh inconsistency.

---

## 10. Bảng `carts`

**Mục đích:** Giỏ hàng của Buyer. Mỗi User có tối đa 1 Cart hoạt động.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `user_id` | BIGINT | UK, NN | FK (Loose) → users.id |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.cart.entity.Cart` |
| Repository | `com.tmt.ecommerce.cart.repository.CartRepository` |
| Service | `CartService` |
| Controller | `CartController` |
| API | `GET /api/v1/carts`, `POST /api/v1/carts/items`, `PUT /api/v1/carts/items/{id}`, `DELETE /api/v1/carts/items/{id}` |

### Repository methods
```java
@EntityGraph(attributePaths = {"items"})
Optional<Cart> findByUserId(Long userId); // Load cart + items cùng lúc
```

---

## 11. Bảng `cart_items`

**Mục đích:** Lưu từng mặt hàng trong giỏ hàng.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `cart_id` | BIGINT | FK → carts.id, NN | Giỏ hàng chứa item |
| `product_variant_id` | BIGINT | NN | FK (Loose) → product_variants.id |
| `quantity` | INT | NN | Số lượng |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.cart.entity.CartItem` |
| Repository | `com.tmt.ecommerce.cart.repository.CartItemRepository` |
| Service | `CartService` |
| Controller | `CartController` |

### Business Rules
- Khi thêm cùng `productVariantId` vào giỏ: `quantity += requestQty` (update thay vì insert).
- Khi Checkout: toàn bộ CartItem được chọn sẽ bị `DELETE` trong cùng transaction.
- Kiểm tra `stockQuantity >= requestQty` qua `ProductInternalService` trước khi thêm vào giỏ.

---

## 12. Bảng `orders`

**Mục đích:** Lưu đơn hàng sau khi Buyer hoàn tất checkout. Mỗi gian hàng tạo 1 Order riêng.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `user_id` | BIGINT | NN | FK (Loose) → users.id (Buyer) |
| `shop_id` | BIGINT | NN | FK (Loose) → shops.id |
| `payment_group_id` | VARCHAR(36) | NULL | UUID nhóm multi-shop checkout |
| `status` | VARCHAR(20) | NN | PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED |
| `total_amount` | DECIMAL(15,2) | NN | Tổng sau giảm |
| `original_total_amount` | DECIMAL(15,2) | NN, DEFAULT 0 | Tổng trước giảm |
| `discount_amount` | DECIMAL(15,2) | NN, DEFAULT 0 | Số tiền giảm |
| `shipping_address` | VARCHAR(500) | NN | Địa chỉ nhận hàng |
| `payment_method` | VARCHAR(20) | NN | VNPAY / MOMO / COD |
| `applied_voucher_id` | BIGINT | NULL | ID voucher đã áp dụng |
| `applied_voucher_code` | VARCHAR(50) | NULL | Mã voucher (snapshot) |
| `created_at` | DATETIME | NN | Hibernate CreationTimestamp |
| `updated_at` | DATETIME | NULL | Hibernate UpdateTimestamp |

### Indexes
```sql
INDEX idx_order_user_status_created (user_id, status, created_at)  -- Buyer filter theo status
INDEX idx_order_user_created        (user_id, created_at)          -- Buyer lịch sử
INDEX idx_order_shop_status_created (shop_id, status, created_at)  -- Vendor filter
INDEX idx_order_shop_created        (shop_id, created_at)          -- Vendor dashboard
INDEX idx_order_payment_group       (payment_group_id)             -- IPN lookup
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.order.entity.Order` |
| Repository | `com.tmt.ecommerce.order.repository.OrderRepository` |
| Service | `OrderService`, `OrderInternalServiceImpl` |
| Controller | `OrderController` (Buyer), `VendorOrderController` (Vendor), `AdminOrderController` (Admin) |
| API | `GET /api/v1/orders`, `GET /api/v1/orders/{id}`, `POST /api/v1/orders/checkout`, `PATCH /api/v1/vendor/orders/{id}/status` |

### Repository methods quan trọng
```java
Page<Order> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable p);
Page<Order> findByShopIdOrderByCreatedAtDesc(Long shopId, Pageable p);
List<Order> findByPaymentGroupId(String paymentGroupId);
void updateStatusByPaymentGroupId(String pgId, OrderStatus status); // Bulk update
List<OrderVariantProjection> findDeliveredOrderVariants(Long userId, OrderStatus status); // Review check
```

---

## 13. Bảng `order_items`

**Mục đích:** Lưu chi tiết từng mặt hàng trong đơn hàng. Áp dụng Snapshot Pattern.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `order_id` | BIGINT | FK → orders.id, NN | Đơn hàng chứa item |
| `product_variant_id` | BIGINT | NN | FK (Loose) → product_variants.id |
| `quantity` | INT | NN | Số lượng đặt |
| `unit_price` | DECIMAL(15,2) | NN | **Snapshot giá** tại thời điểm đặt |
| `product_name` | VARCHAR(255) | NN | **Snapshot tên sản phẩm** tại thời điểm đặt |
| `sub_total` | DECIMAL(15,2) | NN | `quantity × unit_price` |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.order.entity.OrderItem` |
| Repository | (quản lý qua OrderRepository / cascade) |
| Service | `OrderService` (tạo tại checkout) |

### Snapshot Pattern
`unit_price` và `product_name` là bản sao tại thời điểm đặt hàng. Đây là pattern quan trọng:
- Nếu Vendor tăng giá sản phẩm sau, đơn hàng cũ hiển thị đúng giá cũ.
- Nếu Vendor đổi tên sản phẩm, đơn hàng cũ hiển thị đúng tên cũ.
- Đảm bảo tính toàn vẹn dữ liệu tài chính và pháp lý.

---

## 14. Bảng `payments`

**Mục đích:** Lưu giao dịch thanh toán. Liên kết với Orders qua `payment_group_id`.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `payment_group_id` | VARCHAR(36) | NN | UUID nhóm đơn hàng |
| `amount` | DECIMAL(15,2) | NN | Tổng tiền giao dịch |
| `method` | VARCHAR(20) | NN | VNPAY / MOMO / COD |
| `status` | VARCHAR(20) | NN | PENDING / SUCCESS / FAILED / CANCELED |
| `transaction_id` | VARCHAR(100) | UK, NN | Mã giao dịch nội bộ (PAY_xxxxxxxx) |
| `gateway_transaction_id` | VARCHAR(100) | NULL | Mã từ VNPay (vnp_TxnRef) |
| `raw_data` | TEXT | NULL | Full JSON response VNPay (đối soát) |
| `created_at` | DATETIME | NN | CreationTimestamp |
| `updated_at` | DATETIME | NULL | UpdateTimestamp |

### Indexes
```sql
INDEX idx_payment_group_created (payment_group_id, created_at)
UNIQUE KEY (transaction_id)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.payment.entity.Payment` |
| Repository | `com.tmt.ecommerce.payment.repository.PaymentRepository` |
| Service | `PaymentGatewayService`, `VnPayStrategy` |
| Controller | `PaymentController` |
| API | `POST /api/v1/payments/create`, `GET /api/v1/payments/vnpay/ipn`, `GET /api/v1/payments/group/{id}/status` |

### Repository methods quan trọng
```java
Optional<Payment> findByTransactionId(String txId);

@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Payment p WHERE p.transactionId = :transactionId")
Optional<Payment> findByTransactionIdForUpdate(String txId); // IPN xử lý

List<Payment> findByPaymentGroupIdOrderByCreatedAtDesc(String pgId);
```

### Thiết kế đặc biệt
- `raw_data` lưu toàn bộ response từ VNPay để phục vụ đối soát tài chính.
- `transaction_id` nội bộ (PAY_xxx) ≠ `gateway_transaction_id` (VNPay ref) — cho phép retry nhiều lần.
- Pessimistic Lock (`SELECT FOR UPDATE`) + Idempotency check → chống xử lý IPN 2 lần.

---

## 15. Bảng `vouchers`

**Mục đích:** Lưu mã giảm giá. Hỗ trợ cả System voucher và Shop voucher.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `code` | VARCHAR(50) | UK, NN | Mã giảm giá (VD: SUMMER2026) |
| `scope` | VARCHAR(20) | NN | SYSTEM / SHOP |
| `shop_id` | BIGINT | NULL | FK (Loose) → shops.id; NULL = SYSTEM |
| `type` | VARCHAR(20) | NN | PERCENTAGE / FIXED_AMOUNT |
| `discount_value` | DECIMAL(15,2) | NN | Giá trị: 10 (10%) hoặc 20000 (VND) |
| `max_discount` | DECIMAL(15,2) | NULL | Giới hạn tối đa cho PERCENTAGE |
| `min_order_value` | DECIMAL(15,2) | NULL | Giá trị đơn tối thiểu |
| `start_date` | DATETIME | NULL | Ngày bắt đầu |
| `end_date` | DATETIME | NULL | Ngày hết hạn |
| `usage_limit` | INT | NULL | Số lần dùng tối đa (NULL = không giới hạn) |
| `used_count` | INT | NN, DEFAULT 0 | Số lần đã dùng |
| `created_at` | DATETIME | NN | — |
| `updated_at` | DATETIME | NULL | — |

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.voucher.entity.Voucher` |
| Repository | `com.tmt.ecommerce.voucher.repository.VoucherRepository` |
| Service | `VoucherService`, `VoucherInternalServiceImpl` |
| Controller | `VoucherController` |
| API | `POST /api/v1/vouchers` (Admin/Vendor), `GET /api/v1/vouchers/my-shop` (Vendor) |

### Repository methods
```java
Optional<Voucher> findByCode(String code);
Page<Voucher> findByShopId(Long shopId, Pageable p);
Page<Voucher> findByScope(VoucherScope scope, Pageable p);

@Modifying
@Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 " +
       "WHERE v.id = :id AND (v.usageLimit IS NULL OR v.usedCount < v.usageLimit)")
int incrementUsage(Long id); // Atomic — chống race condition
```

### Cross-module usage
- `VoucherInternalServiceImpl.calculateDiscount()` được gọi bởi `OrderService` trong checkout.
- Sau khi đơn hàng tạo thành công: `incrementUsage()` tăng `used_count` atomic.

---

## 16. Bảng `reviews`

**Mục đích:** Lưu đánh giá sản phẩm của Buyer sau khi nhận hàng.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `user_id` | BIGINT | NN | FK (Loose) → users.id |
| `product_id` | BIGINT | NN | FK (Loose) → products.id |
| `order_id` | BIGINT | NN | FK (Loose) → orders.id |
| `rating` | INT | NN | Điểm đánh giá: 1–5 |
| `comment` | VARCHAR(1000) | NN | Nội dung đánh giá |
| `vendor_reply` | TEXT | NULL | Phản hồi của Vendor |
| `vendor_replied_at` | DATETIME | NULL | Thời điểm Vendor reply |
| `created_at` | DATETIME | NN | — |
| `updated_at` | DATETIME | NULL | — |

### Constraints
```sql
UNIQUE KEY uk_user_order_product (user_id, order_id, product_id)
INDEX idx_review_product_created (product_id, created_at)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.review.entity.Review` |
| Repository | `com.tmt.ecommerce.review.repository.ReviewRepository` |
| Service | `ReviewService` |
| Controller | `ReviewController` |
| API | `POST /api/v1/reviews`, `PUT /api/v1/reviews/{id}`, `DELETE /api/v1/reviews/{id}`, `GET /api/v1/products/{id}/reviews` |

### Repository methods
```java
Page<Review> findByProductIdOrderByCreatedAtDesc(Long productId, Pageable p);
boolean existsByUserIdAndOrderIdAndProductId(Long u, Long o, Long p); // Unique check
List<Object[]> getRatingCountsByProductId(Long productId); // Rating histogram
Double getAverageRatingByProductId(Long productId);
Long countByProductId(Long productId);
```

---

## 17. Bảng `review_images`

**Mục đích:** Lưu URL ảnh đính kèm review. Được quản lý bởi `@ElementCollection`.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `review_id` | BIGINT | FK → reviews.id, NN | Review sở hữu ảnh |
| `image_url` | VARCHAR(255) | NN | URL ảnh |

**JPA:** `@ElementCollection` + `@CollectionTable(name = "review_images")`. Không có Entity riêng.

---

## 18. Bảng `notifications`

**Mục đích:** Lưu thông báo hệ thống. Tạo tự động qua Domain Events (AFTER_COMMIT).

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `user_id` | BIGINT | NN | FK (Loose) → users.id (người nhận) |
| `type` | VARCHAR(30) | NN | Enum: ORDER_CONFIRMED, ORDER_SHIPPED, ... |
| `title` | VARCHAR(255) | NN | Tiêu đề thông báo |
| `message` | VARCHAR(500) | NN | Nội dung thông báo |
| `reference_id` | VARCHAR(50) | NULL | ID tham chiếu (orderId, reviewId) |
| `is_read` | TINYINT(1) | NN, DEFAULT 0 | Trạng thái đọc |
| `created_at` | DATETIME | NN | — |

### Index
```sql
INDEX idx_notif_user_created (user_id, created_at)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.notification.entity.Notification` |
| Repository | `com.tmt.ecommerce.notification.repository.NotificationRepository` |
| Service | `NotificationService`, `OrderNotificationListener`, `ReviewNotificationListener`, `ShopNotificationListener` |
| Controller | `NotificationController` |
| API | `GET /api/v1/notifications`, `PATCH /api/v1/notifications/{id}/read`, `PATCH /api/v1/notifications/read-all` |

### Repository methods
```java
Page<Notification> findByUserIdOrderByCreatedAtDesc(Long userId, Pageable p);
long countByUserIdAndIsReadFalse(Long userId); // Badge count
void markAllAsReadByUserId(Long userId);
boolean existsByUserIdAndTypeAndReferenceId(Long u, NotificationType t, String ref); // Idempotency
```

### Event Flow
```
OrderService.checkout()  →  COMMIT
    → @TransactionalEventListener(AFTER_COMMIT, @Transactional(REQUIRES_NEW))
    → OrderNotificationListener.onOrderConfirmed()
    → notificationRepository.save(Notification{userId=buyerId, type=ORDER_CONFIRMED})
    → notificationRepository.save(Notification{userId=vendorId, type=NEW_ORDER})
```

---

## 19. Bảng `chat_rooms`

**Mục đích:** Lưu phòng chat giữa Buyer và Shop. Unique constraint đảm bảo 1 cặp = 1 phòng.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `buyer_id` | BIGINT | NN | FK (Loose) → users.id |
| `shop_id` | BIGINT | NN | FK (Loose) → shops.id |
| `created_at` | DATETIME | NN | — |
| `updated_at` | DATETIME | NULL | Cập nhật khi có message mới |

### Constraint
```sql
UNIQUE KEY (buyer_id, shop_id)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.chat.entity.ChatRoom` |
| Repository | `com.tmt.ecommerce.chat.repository.ChatRoomRepository` |
| Service | `ChatService` |
| Controller | `ChatController` (REST), `ChatMessageController` (STOMP) |
| API | `GET /api/v1/chat/rooms`, `GET /api/v1/chat/rooms/{id}/messages` |
| WebSocket | `SEND /app/chat.send` (STOMP) |

### Repository methods
```java
Optional<ChatRoom> findByBuyerIdAndShopId(Long buyerId, Long shopId);
List<ChatRoom> findAllByBuyerIdOrderByUpdatedAtDesc(Long buyerId);
List<ChatRoom> findAllByShopIdOrderByUpdatedAtDesc(Long shopId);
```

---

## 20. Bảng `chat_messages`

**Mục đích:** Lưu từng tin nhắn trong phòng chat. Index hỗ trợ phân trang lịch sử.

### Schema

| Cột | Kiểu | Constraint | Mô tả |
|---|---|---|---|
| `id` | BIGINT | PK, AI, NN | Khóa chính |
| `room_id` | BIGINT | NN | FK (Loose) → chat_rooms.id |
| `sender_id` | BIGINT | NN | FK (Loose) → users.id |
| `content` | TEXT | NN | Nội dung tin nhắn |
| `is_read` | TINYINT(1) | NN, DEFAULT 0 | Đã đọc chưa |
| `created_at` | DATETIME | NN | — |

### Index
```sql
INDEX idx_chat_room_created (room_id, created_at)
```

### Liên kết module

| Layer | Class |
|---|---|
| Entity | `com.tmt.ecommerce.chat.entity.ChatMessage` |
| Repository | `com.tmt.ecommerce.chat.repository.ChatMessageRepository` |
| Service | `ChatService` |
| Controller | `ChatMessageController` (STOMP `@MessageMapping`) |
| WebSocket | SUBSCRIBE `/user/queue/messages` (STOMP) |

### Repository methods
```java
Page<ChatMessage> findByRoomIdOrderByCreatedAtDesc(Long roomId, Pageable p);
Optional<ChatMessage> findFirstByRoomIdOrderByCreatedAtDesc(Long roomId); // Last message preview
long countByRoomIdAndSenderIdNotAndIsReadFalse(Long roomId, Long senderId); // Unread badge
int markAsReadByRoomIdAndNotSenderId(Long roomId, Long userId); // Bulk mark read
```

### WebSocket Flow
```
Buyer: STOMP SEND /app/chat.send {roomId, content}
→ ChatMessageController.handleMessage()
→ ChatService.sendMessage()
→ chatMessageRepository.save()
→ simpMessagingTemplate.convertAndSendToUser(shopOwnerId, "/queue/messages", response)
→ simpMessagingTemplate.convertAndSendToUser(buyerId, "/queue/messages", response)
```

---

## Tóm tắt toàn bộ bảng

| Bảng | Module | Rows Type | Key Constraint |
|---|---|---|---|
| `users` | identity | Per user | UK(email) |
| `roles` | identity | Lookup | UK(name) |
| `user_roles` | identity | Junction | PK(user_id, role_id) |
| `shops` | shop | Per vendor | UK(user_id), UK(name) |
| `brands` | product | Lookup | UK(name) |
| `categories` | product | Tree | UK(name), FK self |
| `products` | product | Per product | IDX(shop_id), IDX(status) |
| `product_variants` | product | Per variant | UK(sku) |
| `product_images` | product | Per image | FK(product_id) |
| `carts` | cart | Per buyer | UK(user_id) |
| `cart_items` | cart | Per item | FK(cart_id) |
| `orders` | order | Per order | 5x IDX |
| `order_items` | order | Per item | FK(order_id) + Snapshot |
| `payments` | payment | Per payment | UK(transaction_id), IDX(payment_group_id) |
| `vouchers` | voucher | Per voucher | UK(code), Atomic usedCount |
| `reviews` | review | Per review | UK(user,order,product), IDX(product_id) |
| `review_images` | review | Per image | FK(review_id) |
| `notifications` | notification | Per notif | IDX(user_id,created_at) |
| `chat_rooms` | chat | Per pair | UK(buyer_id,shop_id) |
| `chat_messages` | chat | Per message | IDX(room_id,created_at) |
