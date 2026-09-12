# THIẾT KẾ CƠ SỞ DỮ LIỆU
# BÁO CÁO ĐỒ ÁN NGÀNH — Enterprise Ecommerce

Tài liệu này trình bày thiết kế cơ sở dữ liệu chi tiết của hệ thống thương mại điện tử đa người bán. Lược đồ cơ sở dữ liệu được thiết kế theo hướng chuẩn hoá (Normalization), đảm bảo tính toàn vẹn dữ liệu (Data Integrity) và tuân thủ chặt chẽ kiến trúc Modular Monolith. Tổng cộng hệ thống bao gồm **22 bảng thực thể (Entity tables)** và các bảng phụ trợ (Join tables, Element Collections).

---

## 1. MÔ ĐUN XÁC THỰC VÀ PHÂN QUYỀN (IDENTITY MODULE)

### 1.1 Bảng `users` (Người dùng)
- **Mục đích nghiệp vụ:** Lưu trữ thông tin tài khoản dùng để đăng nhập và định danh người dùng trên toàn hệ thống.
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:**
  - `email` (Varchar, `UNIQUE`, Không null): Tên đăng nhập.
  - `password_hash` (Varchar, Không null): Mật khẩu đã mã hoá bcrypt.
  - `full_name` (Varchar), `phone` (Varchar, 20 ký tự).
  - `status` (Enum: `ACTIVE`, `INACTIVE`, `BANNED`).
- **Entity Backend:** `User.java`

### 1.2 Bảng `roles` (Vai trò)
- **Mục đích nghiệp vụ:** Quản lý các phân quyền (RBAC) trong hệ thống (VD: `ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`).
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Trường quan trọng:** `name` (Varchar, `UNIQUE`).
- **Entity Backend:** `Role.java`

### 1.3 Bảng `user_roles` (Bảng trung gian)
- **Mục đích nghiệp vụ:** Thể hiện mối quan hệ nhiều-nhiều (N-N) giữa Người dùng và Vai trò.
- **Khoá ngoại:** `user_id` (tham chiếu `users.id`), `role_id` (tham chiếu `roles.id`).

> **Giải thích thiết kế:** Việc tách `roles` và `user_roles` cho phép hệ thống dễ dàng mở rộng quyền (Scale-out permissions). Một người dùng ban đầu chỉ có `ROLE_USER`, khi đăng ký gian hàng thành công sẽ được cấp thêm `ROLE_VENDOR` mà không cần thay đổi cấu trúc bảng `users`.

---

## 2. MÔ ĐUN GIAN HÀNG (SHOP MODULE)

### 2.1 Bảng `shops` (Gian hàng)
- **Mục đích nghiệp vụ:** Lưu trữ thông tin về các gian hàng ảo của người bán (Vendor).
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:**
  - `user_id` (Long, Không null, `UNIQUE`): Khóa ngoại tham chiếu người sở hữu gian hàng.
  - `name` (Varchar, Không null, `UNIQUE`).
  - `status` (Enum: `PENDING`, `ACTIVE`, `BANNED`).
- **Entity Backend:** `Shop.java`

> **Giải thích thiết kế:** Ràng buộc `UNIQUE` trên cột `user_id` đảm bảo mỗi người dùng chỉ được sở hữu duy nhất một gian hàng. Cột `status` hỗ trợ quy trình kiểm duyệt: gian hàng mới đăng ký sẽ ở trạng thái `PENDING` và chỉ được bán hàng khi Admin chuyển sang `ACTIVE`.

---

## 3. MÔ ĐUN SẢN PHẨM (PRODUCT MODULE)

### 3.1 Bảng `categories` (Danh mục)
- **Mục đích nghiệp vụ:** Phân loại sản phẩm theo hình cây đa cấp (Hierarchical Tree).
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Khóa ngoại quan trọng:** `parent_id` (Tham chiếu đệ quy về `categories.id` để tạo danh mục cha/con).
- **Entity Backend:** `Category.java`

### 3.2 Bảng `brands` (Thương hiệu)
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Trường quan trọng:** `name` (Varchar), `logo_url` (Varchar).
- **Entity Backend:** `Brand.java`

### 3.3 Bảng `products` (Sản phẩm gốc)
- **Mục đích nghiệp vụ:** Lưu trữ thông tin chung của một mặt hàng (Tên, mô tả) không phụ thuộc vào kích cỡ hay màu sắc.
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:**
  - `shop_id` (Long, Khóa ngoại mềm).
  - `category_id` (Long), `brand_id` (Long).
  - `name` (Varchar), `status` (Enum: `ACTIVE`, `HIDDEN`).
- **Chỉ mục (Indexes):** `idx_product_shop` (truy vấn theo shop), `idx_product_status`.
- **Entity Backend:** `Product.java`

### 3.4 Bảng `product_variants` (Biến thể sản phẩm & Tồn kho)
- **Mục đích nghiệp vụ:** Quản lý các biến thể vật lý cụ thể của sản phẩm (SKU, giá riêng, lượng tồn kho).
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:**
  - `product_id` (Long, FK tham chiếu `products.id`).
  - `sku` (Varchar, `UNIQUE`): Mã quản lý kho.
  - `price` (BigDecimal).
  - `stock_quantity` (Integer): Số lượng tồn kho.
  - `attributes` (JSON): Chứa dữ liệu động như màu sắc, kích thước (`{"size": "XL", "color": "Red"}`).
- **Entity Backend:** `ProductVariant.java`

> **Giải thích thiết kế:** 
> 1. **Tại sao tách Product và ProductVariant?** Một sản phẩm áo thun (Product) có thể có nhiều màu sắc và kích cỡ. Mỗi tổ hợp màu/size có mã SKU, giá bán và lượng tồn kho khác nhau. Việc tách bảng giúp chuẩn hóa dữ liệu (1-N). Cột `attributes` dùng kiểu JSON thay vì tạo các cột vật lý `size`, `color` nhằm cung cấp sự linh hoạt cực đại (Schemaless flexibility) cho đa dạng mặt hàng.
> 2. **Tại sao Tồn kho (Inventory) không nằm ở bảng riêng?** Đối với hệ thống có quy mô vừa, việc gộp tồn kho trực tiếp vào biến thể (`stock_quantity`) giúp giảm độ phức tạp khi truy vấn (JOIN) và tránh deadlock ở mức cao. Các lệnh khoá bi quan (Pessimistic Lock) khi thanh toán vẫn đảm bảo được tính đồng thời (Concurrency) trên Record của Variant này.

### 3.5 Bảng `product_images` (Hình ảnh sản phẩm)
- **Mục đích nghiệp vụ:** Lưu URL hình ảnh sản phẩm (đã tải lên Cloudinary).
- **Trường quan trọng:** `product_id` (FK), `image_url` (Varchar), `public_id` (Mã định danh Cloudinary dùng để xoá), `is_thumbnail` (Boolean).
- **Entity Backend:** `ProductImage.java`

---

## 4. MÔ ĐUN GIỎ HÀNG (CART MODULE)

### 4.1 Bảng `carts`
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Trường quan trọng:** `user_id` (Long, `UNIQUE`). Đảm bảo một User chỉ có 1 giỏ hàng duy nhất đang active.
- **Entity Backend:** `Cart.java`

### 4.2 Bảng `cart_items`
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Khóa ngoại:** `cart_id` (FK), `product_variant_id` (Long).
- **Entity Backend:** `CartItem.java`

---

## 5. MÔ ĐUN ĐƠN HÀNG (ORDER MODULE)

### 5.1 Bảng `orders` (Đơn hàng)
- **Mục đích nghiệp vụ:** Đại diện cho một giao dịch mua bán giữa 1 Khách hàng và 1 Gian hàng.
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:**
  - `user_id` (Long), `shop_id` (Long).
  - `payment_group_id` (Varchar/UUID): Mã liên kết các đơn hàng thanh toán cùng một lúc.
  - `status` (Enum: `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
  - `total_amount` (BigDecimal).
  - `applied_voucher_id` (Long), `applied_voucher_code` (Varchar).
- **Chỉ mục (Indexes):** `idx_order_user_status_created`, `idx_order_payment_group`.
- **Entity Backend:** `Order.java`

### 5.2 Bảng `order_items` (Chi tiết đơn hàng)
- **Khóa chính:** `id` (Long, Tự động tăng).
- **Các trường quan trọng:** `order_id` (FK), `product_variant_id` (Long), `product_name` (Varchar), `unit_price` (BigDecimal), `quantity` (Integer).
- **Entity Backend:** `OrderItem.java`

> **Giải thích thiết kế Snapshot:** Bảng `order_items` phải lưu trữ bản sao của `product_name` và `unit_price` tại thời điểm mua hàng (Historical Snapshot). Nếu người bán thay đổi giá hoặc xoá sản phẩm trong tương lai, dữ liệu tài chính của đơn hàng cũ trong lịch sử sẽ không bị ảnh hưởng.

### 5.3 Bảng `order_admin_status_history` (Lịch sử can thiệp Admin)
- **Mục đích nghiệp vụ:** Lưu vết (Audit log) khi Admin quản trị viên can thiệp thay đổi trạng thái đơn hàng (Ví dụ: Bắt buộc hủy đơn hàng vi phạm).
- **Trường quan trọng:** `order_id`, `admin_id`, `old_status`, `new_status`, `reason`.
- **Entity Backend:** `OrderAdminStatusHistory.java`

---

## 6. MÔ ĐUN THANH TOÁN (PAYMENT MODULE)

### 6.1 Bảng `payments` (Thanh toán)
- **Mục đích nghiệp vụ:** Quản lý giao dịch thanh toán tài chính (VNPay, COD).
- **Khóa chính:** `id` (Long).
- **Các trường quan trọng:**
  - `payment_group_id` (Varchar/UUID): Liên kết với `payment_group_id` bên bảng `orders`.
  - `amount` (BigDecimal).
  - `method` (Enum: `VN_PAY`, `COD`).
  - `status` (Enum: `PENDING`, `SUCCESS`, `FAILED`, `CANCELLED`).
  - `transaction_id` (Mã giao dịch nội bộ), `gateway_transaction_id` (Mã tham chiếu từ VNPay).
- **Entity Backend:** `Payment.java`

> **Giải thích thiết kế PaymentGroup:** Do một Giỏ hàng có thể chứa sản phẩm từ nhiều Shop, khi Checkout hệ thống sẽ tách thành N đơn hàng (`orders`) để dễ quản lý. Tuy nhiên, cổng thanh toán VNPay yêu cầu thanh toán một cục duy nhất. `PaymentGroup` (chuỗi UUID) đóng vai trò là chiếc cầu nối (Aggregator) ràng buộc N Đơn hàng vào 1 phiên Thanh toán. Khi Payment chuyển sang SUCCESS, tất cả đơn hàng thuộc group đó mới được xác nhận hợp lệ.

### 6.2 Bảng `refund_attempts` (Lịch sử hoàn tiền)
- **Mục đích nghiệp vụ:** Sổ cái theo dõi các lệnh hoàn tiền qua VNPay (Refund Idempotency).
- **Trường quan trọng:** `order_id` (Long), `payment_id` (Long), `status` (REQUESTED, CONFIRMED, REJECTED).
- **Ràng buộc:** Cặp `(order_id, payment_id)` là `UNIQUE`.

---

## 7. MÔ ĐUN KHUYẾN MÃI (VOUCHER MODULE)

### 7.1 Bảng `vouchers` (Mã giảm giá)
- **Khóa chính:** `id` (Long).
- **Các trường quan trọng:**
  - `code` (Varchar, `UNIQUE`).
  - `scope` (Enum: `SYSTEM` toàn hệ thống, `SHOP` riêng từng gian hàng).
  - `shop_id` (Long, Nullable).
  - `type` (Enum: `PERCENTAGE`, `FIXED_AMOUNT`).
  - `discount_value`, `max_discount`, `min_order_value`.
  - `usage_limit` (Integer), `used_count` (Integer).
- **Entity Backend:** `Voucher.java`

---

## 8. MÔ ĐUN ĐÁNH GIÁ VÀ TƯƠNG TÁC (REVIEW & COMMUNICATION)

### 8.1 Bảng `reviews` (Đánh giá)
- **Khóa chính:** `id` (Long).
- **Trường quan trọng:** `user_id`, `product_id`, `order_id`, `rating` (1-5), `comment`.
- **Ràng buộc duy nhất:** `UNIQUE (user_id, order_id, product_id)`. Đảm bảo khách hàng chỉ được đánh giá 1 lần cho 1 sản phẩm trên mỗi đơn hàng hoàn tất.
- **Entity Backend:** `Review.java`

### 8.2 Bảng `review_images`
- Là một bảng `@ElementCollection` lưu danh sách URL ảnh đính kèm theo Review.

### 8.3 Bảng `notifications` (Thông báo)
- **Khóa chính:** `id` (Long).
- **Trường quan trọng:** `user_id`, `type`, `title`, `message`, `is_read`.
- **Entity Backend:** `Notification.java`

### 8.4 Bảng `chat_rooms` (Phòng Chat)
- **Mục đích:** Kênh trao đổi 1-1 giữa Khách hàng và Shop.
- **Ràng buộc:** `UNIQUE (buyer_id, shop_id)`. Giữa một khách và một shop chỉ duy trì một kênh trò chuyện liên tục.
- **Entity Backend:** `ChatRoom.java`

### 8.5 Bảng `chat_messages` (Tin nhắn Chat)
- **Trường quan trọng:** `room_id` (FK), `sender_id` (Long), `content` (Text), `is_read` (Boolean).

---

## TỔNG KẾT VỀ MỐI QUAN HỆ RÀNG BUỘC (LOOSE COUPLING)
Hệ thống tuân thủ nghiêm ngặt nguyên tắc của **Kiến trúc Modular Monolith**. Các khóa ngoại trỏ từ Module này sang Module khác (VD: `Product` tham chiếu `Shop`, `Order` tham chiếu `Payment`) được lưu dưới dạng `Long ID` hoặc `String UUID` vật lý, thay vì sử dụng annotation ORM `@ManyToOne` cấp đối tượng. 
Thiết kế này ngăn chặn các truy vấn vòng (Circular Dependency), loại bỏ nguy cơ N+1 queries mất kiểm soát qua ranh giới module, đồng thời tạo tiền đề cực kỳ thuận lợi để tách thành Microservices trong tương lai.
