# ĐẶC TẢ SƠ ĐỒ THỰC THỂ LIÊN KẾT (ERD SPECIFICATION)
# BÁO CÁO ĐỒ ÁN NGÀNH — Enterprise Ecommerce

Tài liệu này cung cấp bản đặc tả chi tiết (Specification) để thiết kế Sơ đồ Thực thể Liên kết (Entity Relationship Diagram - ERD) phục vụ báo cáo. Thay vì vẽ ngay lập tức, tài liệu này hướng dẫn cách nhóm các thực thể và xác định mối quan hệ để công cụ tạo sơ đồ (như Mermaid hoặc Draw.io) có thể hiển thị một cách rõ ràng và khoa học nhất.

---

## 1. PHÂN NHÓM VÙNG NGHIỆP VỤ (MODULE BOUNDARIES)
Trong sơ đồ ERD, các bảng cần được đóng khung (Cluster) theo Module để thể hiện rõ kiến trúc Modular Monolith:

- **Identity Cluster**: `users`, `roles`, `user_roles`
- **Shop Cluster**: `shops`
- **Product Cluster**: `products`, `product_variants`, `product_images`, `categories`, `brands`
- **Order & Cart Cluster**: `carts`, `cart_items`, `orders`, `order_items`, `order_admin_status_history`
- **Payment Cluster**: `payments`, `refund_attempts`
- **Interaction Cluster**: `vouchers`, `reviews`, `review_images`, `notifications`, `chat_rooms`, `chat_messages`

---

## 2. ĐẶC TẢ THỰC THỂ VÀ THUỘC TÍNH
Mỗi thực thể trên sơ đồ cần hiển thị các trường dữ liệu lõi:

### Identity & Shop
* **USERS**: `id` (PK), `email`, `password_hash`, `full_name`, `status`
* **ROLES**: `id` (PK), `name`
* **USER_ROLES**: `user_id` (PK, FK), `role_id` (PK, FK)
* **SHOPS**: `id` (PK), `user_id` (FK, UNIQUE), `name`, `status`

### Products
* **CATEGORIES**: `id` (PK), `name`, `parent_id` (FK)
* **BRANDS**: `id` (PK), `name`
* **PRODUCTS**: `id` (PK), `shop_id` (FK), `category_id` (FK), `brand_id` (FK), `name`, `status`
* **PRODUCT_VARIANTS**: `id` (PK), `product_id` (FK), `sku` (UNIQUE), `price`, `stock_quantity`, `attributes` (JSON)
* **PRODUCT_IMAGES**: `id` (PK), `product_id` (FK), `image_url`

### Carts & Orders
* **CARTS**: `id` (PK), `user_id` (FK, UNIQUE)
* **CART_ITEMS**: `id` (PK), `cart_id` (FK), `product_variant_id` (FK), `quantity`
* **ORDERS**: `id` (PK), `user_id` (FK), `shop_id` (FK), `payment_group_id` (UUID), `status`, `total_amount`
* **ORDER_ITEMS**: `id` (PK), `order_id` (FK), `product_variant_id` (FK), `product_name`, `unit_price`, `quantity`
* **ORDER_ADMIN_STATUS_HISTORY**: `id` (PK), `order_id` (FK), `admin_id` (FK)

### Payment
* **PAYMENTS**: `id` (PK), `payment_group_id` (UUID, Liên kết Orders), `amount`, `status`, `method`
* **REFUND_ATTEMPTS**: `id` (PK), `order_id` (FK), `payment_id` (FK), `status`

### Interactions
* **VOUCHERS**: `id` (PK), `code`, `shop_id` (FK), `discount_value`, `used_count`
* **REVIEWS**: `id` (PK), `user_id` (FK), `product_id` (FK), `order_id` (FK), `rating`, `comment`
* **REVIEW_IMAGES**: `review_id` (FK), `image_url`
* **NOTIFICATIONS**: `id` (PK), `user_id` (FK), `type`, `message`
* **CHAT_ROOMS**: `id` (PK), `buyer_id` (FK), `shop_id` (FK)
* **CHAT_MESSAGES**: `id` (PK), `room_id` (FK), `sender_id` (FK), `content`

---

## 3. ĐẶC TẢ ĐƯỜNG LIÊN KẾT (RELATIONSHIPS & CARDINALITY)

### Core User Relationships
* `USERS` (1) ──── (N) `USER_ROLES` ──── (1) `ROLES`
* `USERS` (1) ─── (1) `SHOPS` *(Xác định người sở hữu)*
* `USERS` (1) ─── (1) `CARTS`
* `USERS` (1) ──── (N) `ORDERS`

### Product Hierarchy
* `CATEGORIES` (1) ──── (N) `CATEGORIES` *(Self-referencing Parent/Child)*
* `CATEGORIES` (1) ──── (N) `PRODUCTS`
* `BRANDS` (1) ──── (N) `PRODUCTS`
* `SHOPS` (1) ──── (N) `PRODUCTS`
* `PRODUCTS` (1) ──── (N) `PRODUCT_VARIANTS` *(Master-Detail)*
* `PRODUCTS` (1) ──── (N) `PRODUCT_IMAGES`

### Order Lifecycle
* `CARTS` (1) ──── (N) `CART_ITEMS`
* `PRODUCT_VARIANTS` (1) ──── (N) `CART_ITEMS`
* `ORDERS` (1) ──── (N) `ORDER_ITEMS`
* `PRODUCT_VARIANTS` (1) ──── (N) `ORDER_ITEMS` *(Tạo Snapshot)*
* `ORDERS` (1) ──── (N) `ORDER_ADMIN_STATUS_HISTORY`

### Payment (The Logical Bridge)
* `ORDERS` (N) ──── (1) `PAYMENTS` 
  *(Lưu ý: Liên kết qua trường ảo `payment_group_id` kiểu String thay vì khoá ngoại DB cứng, do tính chất Aggregate Checkout).*
* `ORDERS` (1) ──── (1) `REFUND_ATTEMPTS`
* `PAYMENTS` (1) ──── (N) `REFUND_ATTEMPTS`

### Feedback & Communication
* `USERS` (1) ──── (N) `REVIEWS`
* `PRODUCTS` (1) ──── (N) `REVIEWS`
* `REVIEWS` (1) ──── (N) `REVIEW_IMAGES`
* `SHOPS` (1) ──── (N) `VOUCHERS`
* `USERS` (1) ──── (N) `NOTIFICATIONS`
* `USERS` (1) ──── (N) `CHAT_ROOMS` *(Đóng vai trò Buyer)*
* `SHOPS` (1) ──── (N) `CHAT_ROOMS`
* `CHAT_ROOMS` (1) ──── (N) `CHAT_MESSAGES`

---

## 4. LƯU Ý CHO NGƯỜI VẼ/CÔNG CỤ
- Vẽ các nét đứt (dashed line) cho các mối quan hệ liên module không có ràng buộc Foreign Key cứng ở cấp database (Soft Foreign Key), ví dụ từ `Product` sang `Shop` hoặc từ `Order` sang `Payment`. Điều này để nhấn mạnh với giảng viên về thiết kế Modular.
- Sử dụng màu sắc để làm nổi bật các Module.
- Lược bỏ bớt các cột thời gian chuẩn (`created_at`, `updated_at`) trên bản vẽ ERD để giảm sự rối rắm không cần thiết.
