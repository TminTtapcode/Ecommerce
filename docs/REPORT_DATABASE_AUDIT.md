# REPORT_DATABASE_AUDIT.md
# Database Audit Report — Enterprise Ecommerce

This document contains the verified exact state of the database schemas and relationships directly from the CURRENT source code (Hibernate Entities).

> **Total tables verified:** 22 tables (20 Entity tables + 2 Collection/Join tables).
> **Discrepancies from previous reports:** The previous report mentioned 20 tables and assumed the existence of a `voucher_usage` table. Source code confirms `voucher_usage` does not exist; instead, `vouchers.used_count` tracks usage. The actual count is 22 tables, including `order_admin_status_history` and `refund_attempts` which were recently added.

---

## 1. IDENTITY MODULE

### 1.1 `users`
- **Purpose:** Store user accounts (authentication & basic info).
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `email` (unique), `password_hash`, `full_name`, `phone`, `status`.
- **Relationships:**
  - `@ManyToMany` with `Role` (JoinTable: `user_roles`).
- **Entity:** `src/main/java/com/tmt/ecommerce/identity/entity/User.java`

### 1.2 `roles`
- **Purpose:** RBAC roles (`ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`).
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `name` (String, unique).
- **Entity:** `src/main/java/com/tmt/ecommerce/identity/entity/Role.java`

### 1.3 `user_roles`
- **Purpose:** Join table for User-Role Many-to-Many relationship.
- **Foreign Keys:** `user_id` -> `users.id`, `role_id` -> `roles.id`.

---

## 2. SHOP MODULE

### 2.1 `shops`
- **Purpose:** Store vendor shop information and lifecycle status.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `user_id` (Long, owner of the shop), `name`, `description`, `status` (PENDING, ACTIVE, BANNED).
- **Constraints:** `user_id` is UNIQUE (1 User : 1 Shop), `name` is UNIQUE.
- **Entity:** `src/main/java/com/tmt/ecommerce/shop/entity/Shop.java`

---

## 3. PRODUCT MODULE

### 3.1 `categories`
- **Purpose:** Product categorization (hierarchical).
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `name`, `parent_id` (Self-referencing FK).
- **Entity:** `src/main/java/com/tmt/ecommerce/product/entity/Category.java`

### 3.2 `brands`
- **Purpose:** Product brands.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `name`.
- **Entity:** `src/main/java/com/tmt/ecommerce/product/entity/Brand.java`

### 3.3 `products`
- **Purpose:** Core product catalog data.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `shop_id` (Long), `category_id` (Long), `brand_id` (Long), `name`, `description`, `status` (ACTIVE, HIDDEN).
- **Relationships:**
  - `@OneToMany` with `ProductVariant`.
  - `@OneToMany` with `ProductImage`.
- **Indexes:** `idx_product_shop` (shop_id), `idx_product_status` (status).
- **Entity:** `src/main/java/com/tmt/ecommerce/product/entity/Product.java`

### 3.4 `product_variants`
- **Purpose:** Physical SKUs, pricing, and inventory.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `product_id` (FK -> products), `sku` (UNIQUE), `price` (BigDecimal), `stock_quantity` (Integer), `attributes` (JSON map for size/color), `status`.
- **Note:** **Inventory is tracked here.** There is no separate `inventory` table.
- **Entity:** `src/main/java/com/tmt/ecommerce/product/entity/ProductVariant.java`

### 3.5 `product_images`
- **Purpose:** Product gallery images stored on Cloudinary.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `product_id` (FK), `image_url`, `public_id`, `is_thumbnail`, `sort_order`.
- **Entity:** `src/main/java/com/tmt/ecommerce/product/entity/ProductImage.java`

---

## 4. CART MODULE

### 4.1 `carts`
- **Purpose:** Shopping cart per user.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `user_id` (Long, UNIQUE).
- **Entity:** `src/main/java/com/tmt/ecommerce/cart/entity/Cart.java`

### 4.2 `cart_items`
- **Purpose:** Items within a cart.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `cart_id` (FK), `product_variant_id` (Long), `quantity` (Integer).
- **Entity:** `src/main/java/com/tmt/ecommerce/cart/entity/CartItem.java`

---

## 5. ORDER MODULE

### 5.1 `orders`
- **Purpose:** Transaction record between a Buyer and a SINGLE Shop.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `user_id` (Long), `shop_id` (Long), `payment_group_id` (String UUID), `status` (Enum: PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED), `total_amount`, `original_total_amount`, `discount_amount`, `shipping_address`, `applied_voucher_id`, `applied_voucher_code`.
- **Indexes:** `idx_order_user_status_created`, `idx_order_payment_group`.
- **Entity:** `src/main/java/com/tmt/ecommerce/order/entity/Order.java`

### 5.2 `order_items`
- **Purpose:** Snapshot of purchased variants and historical prices.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `order_id` (FK), `product_variant_id` (Long), `product_name`, `unit_price`, `quantity`, `sub_total`.
- **Entity:** `src/main/java/com/tmt/ecommerce/order/entity/OrderItem.java`

### 5.3 `order_admin_status_history`
- **Purpose:** Audit log of Admin interventions on order statuses.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `order_id` (Long), `admin_id` (Long), `old_status`, `new_status`, `reason`, `created_at`.
- **Indexes:** `idx_admin_history_order_id`
- **Entity:** `src/main/java/com/tmt/ecommerce/order/entity/OrderAdminStatusHistory.java`

---

## 6. PAYMENT MODULE

### 6.1 `payments`
- **Purpose:** Record of payment attempts for a Payment Group (which covers multiple Orders).
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `payment_group_id` (String UUID, ties multiple orders together), `amount` (BigDecimal), `method` (VN_PAY, COD), `status` (PENDING, SUCCESS, FAILED, CANCELLED), `transaction_id`, `gateway_transaction_id`, `raw_data`.
- **Indexes:** `idx_payment_group_created`.
- **Concurrency:** Uses `PESSIMISTIC_WRITE` locking in Repository during IPN callbacks.
- **Entity:** `src/main/java/com/tmt/ecommerce/payment/entity/Payment.java`

### 6.2 `refund_attempts`
- **Purpose:** Ledger tracking of Admin refund commands for VNPay.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `order_id` (Long), `payment_id` (Long), `amount`, `status` (REQUESTED, CONFIRMED, REJECTED, UNKNOWN), `gateway_refund_transaction_id`, `idempotency_key`.
- **Constraints:** UNIQUE on `(order_id, payment_id)` indicating an order can only have one refund attempt ledger.
- **Entity:** `src/main/java/com/tmt/ecommerce/payment/entity/RefundAttempt.java`

---

## 7. VOUCHER MODULE

### 7.1 `vouchers`
- **Purpose:** Discounts at SYSTEM or SHOP scope.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `code` (UNIQUE), `scope` (SYSTEM, SHOP), `shop_id` (Long, nullable), `type` (PERCENTAGE, FIXED_AMOUNT), `discount_value`, `max_discount`, `min_order_value`, `usage_limit`, `used_count`, `start_date`, `end_date`.
- **Note:** `voucher_usage` table does NOT exist. Usage is tracked monotonically via `used_count`.
- **Entity:** `src/main/java/com/tmt/ecommerce/voucher/entity/Voucher.java`

---

## 8. REVIEW MODULE

### 8.1 `reviews`
- **Purpose:** Buyer feedback for delivered products.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `user_id` (Long), `product_id` (Long), `order_id` (Long), `rating` (Integer), `comment`, `vendor_reply`, `vendor_replied_at`.
- **Constraints:** UNIQUE on `(user_id, order_id, product_id)` (one review per product per order).
- **Indexes:** `idx_review_product_created`.
- **Entity:** `src/main/java/com/tmt/ecommerce/review/entity/Review.java`

### 8.2 `review_images`
- **Purpose:** Element collection for review image URLs.
- **Columns:** `review_id` (FK), `image_url`.

---

## 9. NOTIFICATION MODULE

### 9.1 `notifications`
- **Purpose:** In-app alerts for users.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `user_id` (Long), `type` (String Enum), `title`, `message`, `reference_id` (String), `is_read`.
- **Indexes:** `idx_notif_user_created`.
- **Entity:** `src/main/java/com/tmt/ecommerce/notification/entity/Notification.java`

---

## 10. CHAT MODULE

### 10.1 `chat_rooms`
- **Purpose:** P2P chat channel between a Buyer and a Shop.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `buyer_id` (Long), `shop_id` (Long).
- **Constraints:** UNIQUE on `(buyer_id, shop_id)`
- **Entity:** `src/main/java/com/tmt/ecommerce/chat/entity/ChatRoom.java`

### 10.2 `chat_messages`
- **Purpose:** Messages within a chat room.
- **Primary Key:** `id` (Long, auto-increment)
- **Important Columns:** `room_id` (FK -> chat_rooms), `sender_id` (Long), `content` (Text), `is_read` (Boolean).
- **Indexes:** `idx_chat_room_created`.
- **Entity:** `src/main/java/com/tmt/ecommerce/chat/entity/ChatMessage.java`
