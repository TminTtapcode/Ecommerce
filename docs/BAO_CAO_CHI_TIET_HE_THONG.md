# BÁO CÁO TOÀN DIỆN VỀ CẤU TRÚC HỆ THỐNG, CƠ SỞ DỮ LIỆU VÀ CÁC HÀM THÀNH VIÊN
# ENTERPRISE MULTI-VENDOR E-COMMERCE SYSTEM

> **Đề tài:** Hệ thống Thương mại Điện tử Đa người bán (Multi-Vendor E-Commerce Platform)  
> **Kiến trúc:** Modular Monolith (Spring Boot 3.4.x / Java 21) + Single Page Application (React 19 / TypeScript / Vite)  
> **Cơ sở dữ liệu:** MySQL 8.0 (22 Bảng, Quản lý toàn vẹn dữ liệu và phân tách ranh giới module)  
> **Ngày cập nhật:** 2026-09-12  

---

## MỤC LỤC TỔNG QUAN

1. [Cấu trúc tổng thể của Ứng dụng & Website](#1-cấu-trúc-tổng-thể-của-ứng-dụng--website)
   - [1.1. Kiến trúc Backend (Modular Monolith)](#11-kiến-trúc-backend-modular-monolith)
   - [1.2. Kiến trúc Frontend (React 19 + TypeScript + Vite)](#12-kiến-trúc-frontend-react-19--typescript--vite)
   - [1.3. Mô hình Tương tác & Luồng Dữ liệu (Communication Flow)](#13-mô-hình-tương-tác--luồng-dữ-liệu-communication-flow)
2. [Đặc tả Chi tiết Cơ sở Dữ liệu (Database Schema — 22 Bảng)](#2-đặc-tả-chi-tiết-cơ-sở-dữ-liệu-database-schema--22-bảng)
   - [2.1. Module Identity (Người dùng & Phân quyền)](#21-module-identity-người-dùng--phân-quyền)
   - [2.2. Module Shop (Cửa hàng / Người bán)](#22-module-shop-cửa-hàng--người-bán)
   - [2.3. Module Product (Sản phẩm, Danh mục, Thương hiệu, Biến thể, Hình ảnh)](#23-module-product-sản-phẩm-danh-mục-thương-hiệu-biến-thể-hình-ảnh)
   - [2.4. Module Cart (Giỏ hàng & Mục giỏ hàng)](#24-module-cart-giỏ-hàng--mục-giỏ-hàng)
   - [2.5. Module Order (Đơn hàng, Chi tiết đơn hàng, Lịch sử can thiệp Admin)](#25-module-order-đơn-hàng-chi-tiết-đơn-hàng-lịch-sử-can-thiệp-admin)
   - [2.6. Module Payment (Thanh toán & Lịch sử hoàn tiền Refund)](#26-module-payment-thanh-toán--lịch-sử-hoàn-tiền-refund)
   - [2.7. Module Voucher (Khuyến mãi & Mã giảm giá)](#27-module-voucher-khuyến-mãi--mã-giảm-giá)
   - [2.8. Module Review (Đánh giá & Hình ảnh đánh giá)](#28-module-review-đánh-giá--hình-ảnh-đánh-giá)
   - [2.9. Module Notification (Thông báo hệ thống)](#29-module-notification-thông-báo-hệ-thống)
   - [2.10. Module Chat (Phòng chat & Tin nhắn Realtime)](#210-module-chat-phòng-chat--tin-nhắn-realtime)
   - [2.11. Các Pattern Thiết kế Cơ sở Dữ liệu Cốt lõi](#211-các-pattern-thiết-kế-cơ-sở-dữ-liệu-cốt-lõi)
3. [Chi tiết Các Hàm Thành viên và Chức năng Nghiệp vụ (Backend Member Methods)](#3-chi-tiết-các-hàm-thành-viên-và-chức-năng-nghiệp-vụ-backend-member-methods)
   - [3.1. Module Identity & Authentication](#31-module-identity--authentication)
   - [3.2. Module Shop](#32-module-shop)
   - [3.3. Module Product & Category](#33-module-product--category)
   - [3.4. Module Cart](#34-module-cart)
   - [3.5. Module Order](#35-module-order)
   - [3.6. Module Payment & Refund](#36-module-payment--refund)
   - [3.7. Module Voucher](#37-module-voucher)
   - [3.8. Module Review](#38-module-review)
   - [3.9. Module Notification](#39-module-notification)
   - [3.10. Module Chat (WebSocket STOMP)](#310-module-chat-websocket-stomp)
   - [3.11. Module Media (Cloudinary)](#311-module-media-cloudinary)
   - [3.12. Module Statistics & Analytics](#312-module-statistics--analytics)
4. [Đặc tả Các Hàm API Client và Cấu trúc Giao diện Frontend](#4-đặc-tả-các-hàm-api-client-và-cấu-trúc-giao-diện-frontend)
   - [4.1. Hệ thống API Services của Frontend (`frontend/src/api/`)](#41-hệ-thống-api-services-của-frontend-frontendsrcapi)
   - [4.2. Cấu trúc Các Màn hình Chức năng (`frontend/src/pages/`)](#42-cấu-trúc-các-màn-hình-chức-năng-frontendsrcpages)
5. [Cơ chế Bảo mật, Xử lý Bất đồng bộ & Giao dịch Toàn hệ thống](#5-cơ-chế-bảo-mật-xử-lý-bất-đồng-bộ--giao-dịch-toàn-hệ-thống)

---

# 1. CẤU TRÚC TỔNG THỂ CỦA ỨNG DỤNG & WEBSITE

## 1.1. Kiến trúc Backend (Modular Monolith)

Hệ thống Backend được xây dựng trên nền tảng **Java 21** và **Spring Boot 3.4.x**, tổ chức theo mô hình kiến trúc **Modular Monolith** (Đơn khối module hóa).

### Nguyên tắc tổ chức Package:
Mã nguồn được phân tách thành **12 module nghiệp vụ độc lập** đặt tại `com.tmt.ecommerce.<module_name>`:
1. `identity`: Đăng ký, đăng nhập, phân quyền RBAC (`ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`), quản lý hồ sơ cá nhân.
2. `shop`: Đăng ký gian hàng, xét duyệt shop, cấm/mở khóa shop (Ban/Unban enforcement).
3. `product`: Quản lý danh mục, sản phẩm, biến thể (SKU, giá, tồn kho), bộ sưu tập ảnh, gợi ý sản phẩm (Recommendation Engine).
4. `cart`: Giỏ hàng người dùng, kiểm soát tồn kho realtime, khóa giỏ hàng chống race condition.
5. `order`: Xử lý checkout đa gian hàng (Split Order), quản lý vòng đời đơn hàng, xác nhận nhận hàng, hủy đơn hàng và hoàn tồn kho.
6. `payment`: Tích hợp cổng VNPay (HMAC-SHA512), thanh toán COD, xử lý IPN Webhook với khóa bi quan (Pessimistic Lock), quản lý hoàn tiền Refund tự động.
7. `voucher`: Khuyến mãi toàn sàn (SYSTEM) và khuyến mãi của Shop (SHOP), thuật toán tính chiết khấu tối ưu và khóa trừ lượt dùng an toàn.
8. `review`: Đánh giá sản phẩm sau khi đơn hoàn tất, xếp hạng sao (1-5 sao), thống kê rating summary, vendor phản hồi đánh giá.
9. `notification`: Hệ thống thông báo đẩy hướng sự kiện (`ApplicationEventPublisher`), ghi nhận thông báo đơn hàng và đánh giá.
10. `chat`: Nhắn tin thời gian thực 2 chiều giữa Khách mua và Chủ shop qua giao thức WebSocket STOMP.
11. `media`: Tải lên và quản lý tệp tin đa phương tiện độc lập qua Cloudinary CDN.
12. `statistics`: Thống kê doanh thu, biểu đồ tài chính cho Vendor và bảng điều khiển tổng quan cho Admin.

### Nguyên tắc cốt lõi của Modular Monolith:
1. **Phân lập dữ liệu (No Direct Cross-Module Entity/Repository Access):** Module $A$ tuyệt đối không bao giờ inject `@Repository` hay import `@Entity` của Module $B$.
2. **Giao tiếp qua Internal Service & DTO:** Khi Module $A$ cần dữ liệu từ Module $B$, tương tác bắt buộc phải thông qua `InternalService` (ví dụ: `ProductInternalService`, `ShopInternalService`, `OrderInternalService`) với các DTO đóng gói riêng.
3. **Liên kết cơ sở dữ liệu lỏng (Loose DB Coupling):** Khóa ngoại liên module (như `shop_id` trong `products`, `user_id` trong `orders`) không khai báo `FOREIGN KEY` cấp cơ sở dữ liệu, mà được kiểm soát toàn vẹn bằng logic tại tầng Service.
4. **Sự kiện miền (Domain Events):** Sử dụng `ApplicationEventPublisher` và `@TransactionalEventListener(phase = AFTER_COMMIT)` để kích hoạt các tác vụ thứ cấp (gửi thông báo, cập nhật số liệu) mà không làm nghẽn luồng giao dịch chính.

---

## 1.2. Kiến trúc Frontend (React 19 + TypeScript + Vite)

Ứng dụng Web Client là một Single Page Application (SPA) xây dựng trên nền tảng **React 19**, **TypeScript**, công cụ build **Vite**, định kiểu giao diện bằng **Vanilla CSS + TailwindCSS utility**, và giao tiếp mạng qua **Axios** + **STOMP/SockJS**.

### Cấu trúc thư mục Frontend (`frontend/src/`):
```
frontend/src/
├── api/                  # Toàn bộ API Client modules (Axios calls)
│   ├── apiClient.ts      # Axios Instance cấu hình baseURL, timeout, Interceptor đính kèm Bearer Token
│   ├── authApi.ts        # Gọi API /auth (login, register)
│   ├── cartApi.ts        # Gọi API /carts
│   ├── orderApi.ts       # Gọi API /orders, /vendor/orders, confirm-delivery
│   ├── paymentApi.ts     # Gọi API /payments (create, status)
│   ├── productApi.ts     # Gọi API /products, /vendor/products
│   ├── reviewApi.ts      # Gọi API /reviews, /vendor/reviews
│   ├── shopApi.ts        # Gọi API /shops, /admin/shops
│   ├── voucherApi.ts     # Gọi API /vouchers, /admin/vouchers
│   ├── chatApi.ts        # Gọi API /chat (rooms, message history)
│   └── ...
├── components/           # Các UI Component tái sử dụng (Header, Footer, Modals, Spinners, Breadcrumbs)
├── contexts/             # React Contexts (AuthContext: quản lý session, roles; CartContext: số lượng giỏ hàng)
├── hooks/                # Custom Hooks (useAuth, useDebounce, useNotification)
├── pages/                # Các trang màn hình chức năng phân chia theo Role:
│   ├── auth/             # LoginPage, RegisterPage
│   ├── public/           # ProductListPage, ProductDetailPage, CartPage, CheckoutPage, OrderHistoryPage, OrderDetailPage, PaymentResultPage, ProfilePage, ShopProfilePage, ChatTestPage
│   ├── vendor/           # VendorProductListPage, VendorProductFormPage, VendorOrderListPage, VendorVoucherListPage, VendorAnalyticsPage, VendorShopRegistrationPage, ShopPendingPage
│   └── admin/            # AdminDashboardPage, AdminShopListPage, AdminProductListPage, AdminProductFormPage, AdminOrderListPage, AdminOrderDetailPage, AdminVoucherListPage
├── services/             # WebSocket Service (STOMP Client kết nối /ws)
├── utils/                # Helper functions (formatCurrency, formatDate, tokenDecoder)
├── App.tsx               # Cấu hình Router (react-router-dom) & Route Guards (Role-based Protected Routes)
└── main.tsx              # Điểm khởi tạo ứng dụng React
```

---

## 1.3. Mô hình Tương tác & Luồng Dữ liệu (Communication Flow)

```
+-------------------------------------------------------------------------------+
|                             CLIENT (React 19 SPA)                             |
|  - AuthContext (JWT)     - Public Pages (Buyer)    - Vendor/Admin Dashboards  |
|  - Axios Interceptor     - WebSocket / STOMP Client (Realtime Chat & Alerts)   |
+---------------------------------------+---------------------------------------+
                                        | HTTP (REST) / WSS (STOMP)
                                        v
+-------------------------------------------------------------------------------+
|                       SECURITY & GATEWAY (Spring Security)                    |
|  - JwtAuthenticationFilter: Trích xuất Bearer token, gán SecurityContext      |
|  - JwtChannelInterceptor: Xác thực STOMP CONNECT frame cho WebSocket          |
|  - Method Security: @PreAuthorize("hasRole(...)"), @CurrentUserId             |
+---------------------------------------+---------------------------------------+
                                        | Dispatching
                                        v
+-------------------------------------------------------------------------------+
|                    MODULAR MONOLITH BACKEND (Spring Boot)                     |
|                                                                               |
|  [Identity]       [Shop]            [Product]         [Cart]                  |
|      |               |                  |                |                    |
|      +---------------+------------------+----------------+                    |
|                      |  Internal Services (Java Contracts)                    |
|                      v                                                        |
|  [Order] <=======> [Payment] <=======> [Voucher] <=====> [Review]             |
|      |                                                       |                |
|      +------------> EventPublisher (ApplicationEvents) <-----+                |
|                           |                                                   |
|                           v                                                   |
|                 [Notification] & [Chat] (WebSocket Broadcast)                 |
+---------------------------------------+---------------------------------------+
                                        | JPA / Hibernate
                                        v
+-------------------------------------------------------------------------------+
|                             DATABASE (MySQL 8.0)                              |
|  - 22 Tables                                                                  |
|  - Isolation: READ_COMMITTED                                                  |
|  - Locking: Pessimistic Write Lock (FOR UPDATE) cho Inventory, Cart & Payment |
+-------------------------------------------------------------------------------+
```

---

# 2. ĐẶC TẢ CHI TIẾT CƠ SỞ DỮ LIỆU (DATABASE SCHEMA — 22 BẢNG)

Hệ thống sử dụng cơ sở dữ liệu quan hệ **MySQL 8.0**, gồm chính xác **22 bảng dữ liệu**.

---

## 2.1. Module Identity (Người dùng & Phân quyền)

### Bảng 1: `users`
Lưu trữ thông tin tài khoản người dùng của toàn hệ thống (Khách mua, Chủ shop, Quản trị viên).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh người dùng duy nhất |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE (`uk_users_email`) | Địa chỉ email dùng để đăng nhập |
| `password_hash` | VARCHAR(255) | NOT NULL | Mật khẩu đã được băm bằng thuật toán BCrypt |
| `full_name` | VARCHAR(100) | NOT NULL | Họ và tên đầy đủ |
| `phone` | VARCHAR(20) | NULL | Số điện thoại liên hệ |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái tài khoản (`ACTIVE`, `LOCKED`) |

### Bảng 2: `roles`
Danh mục các vai trò trong hệ thống.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh vai trò |
| `name` | VARCHAR(50) | NOT NULL, UNIQUE (`uk_roles_name`) | Tên vai trò (`ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`) |

### Bảng 3: `user_roles`
Bảng nối thể hiện mối quan hệ nhiều - nhiều ($N:N$) giữa người dùng và vai trò.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `user_id` | BIGINT | PK, FK $\rightarrow$ `users(id)` | Mã người dùng |
| `role_id` | BIGINT | PK, FK $\rightarrow$ `roles(id)` | Mã vai trò được gán |

---

## 2.2. Module Shop (Cửa hàng / Người bán)

### Bảng 4: `shops`
Thông tin gian hàng của người bán trên sàn thương mại điện tử.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh gian hàng |
| `user_id` | BIGINT | NOT NULL, UNIQUE (`uk_shops_user_id`) | ID chủ sở hữu (Loose coupling $\rightarrow$ `users.id`) |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE (`uk_shops_name`) | Tên gian hàng thương mại |
| `description` | TEXT | NULL | Mô tả giới thiệu gian hàng |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái shop (`PENDING`, `ACTIVE`, `BANNED`) |
| `prior_status` | VARCHAR(20) | NULL | Trạng thái trước khi bị khóa (dùng để khôi phục khi Admin Unban) |

---

## 2.3. Module Product (Sản phẩm, Danh mục, Thương hiệu, Biến thể, Hình ảnh)

### Bảng 5: `categories`
Danh mục phân loại sản phẩm đa cấp (hỗ trợ quan hệ cha - con).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh danh mục |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE (`uk_categories_name`) | Tên danh mục (ví dụ: "Thời trang nam") |
| `description` | TEXT | NULL | Mô tả chi tiết danh mục |
| `parent_id` | BIGINT | NULL, FK $\rightarrow$ `categories(id)` | Danh mục cha (Self-reference; `NULL` nếu là danh mục gốc) |

### Bảng 6: `brands`
Danh mục thương hiệu sản phẩm.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh thương hiệu |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE (`uk_brands_name`) | Tên thương hiệu (ví dụ: "Apple", "Samsung") |
| `logo_url` | VARCHAR(255) | NULL | Đường dẫn logo thương hiệu |
| `description` | TEXT | NULL | Thông tin giới thiệu thương hiệu |

### Bảng 7: `products`
Thông tin sản phẩm cốt lõi (Aggregate Root của module Product).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh sản phẩm |
| `shop_id` | BIGINT | NOT NULL, INDEX (`idx_product_shop`) | ID Shop sở hữu (Loose coupling $\rightarrow$ `shops.id`) |
| `name` | VARCHAR(255) | NOT NULL | Tên hiển thị của sản phẩm |
| `description` | TEXT | NULL | Mô tả chi tiết tính năng, thông số sản phẩm |
| `price` | DECIMAL(15,2) | NOT NULL | Giá niêm yết cơ sở (đơn vị: VNĐ) |
| `stock_quantity`| INT | NOT NULL | Tổng số lượng tồn kho tổng hợp |
| `category_id` | BIGINT | NOT NULL, FK $\rightarrow$ `categories(id)` | Danh mục sản phẩm trực thuộc |
| `brand_id` | BIGINT | NULL, FK $\rightarrow$ `brands(id)` | Thương hiệu sản phẩm (nếu có) |
| `status` | VARCHAR(20) | NOT NULL, INDEX (`idx_product_status`) | Trạng thái hiển thị (`ACTIVE`, `OUT_OF_STOCK`, `HIDDEN`) |

### Bảng 8: `product_variants`
Các biến thể bán hàng cụ thể của sản phẩm (theo phân loại màu sắc, kích cỡ, dung lượng).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh biến thể |
| `product_id` | BIGINT | NOT NULL, FK $\rightarrow$ `products(id)` ON DELETE CASCADE | Thuộc sản phẩm nào |
| `sku` | VARCHAR(100) | NOT NULL, UNIQUE (`uk_product_variants_sku`) | Mã quản lý tồn kho duy nhất (SKU code) |
| `price` | DECIMAL(15,2) | NOT NULL | Giá bán thực tế của biến thể này |
| `stock_quantity`| INT | NOT NULL | Tồn kho khả dụng của biến thể |
| `attributes` | JSON | NULL | Thuộc tính phân loại dạng JSON (`{"color":"Đỏ","size":"XL"}`) |
| `status` | VARCHAR(20) | NOT NULL DEFAULT 'ACTIVE' | Trạng thái kinh doanh của biến thể |

### Bảng 9: `product_images`
Bộ sưu tập hình ảnh chi tiết của sản phẩm.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh hình ảnh |
| `product_id` | BIGINT | NOT NULL, FK $\rightarrow$ `products(id)` ON DELETE CASCADE | Thuộc sản phẩm nào |
| `image_url` | VARCHAR(255) | NOT NULL | Đường dẫn ảnh trên CDN (Cloudinary) |
| `public_id` | VARCHAR(255) | NULL | Mã Public ID trên Cloudinary để quản lý xóa/sửa |
| `is_thumbnail`| TINYINT(1) | DEFAULT 0 | Đánh dấu là ảnh đại diện thu nhỏ (`1` = thumbnail) |
| `sort_order` | INT | DEFAULT 0 | Thứ tự hiển thị trong bộ sưu tập (0, 1, 2,...) |

---

## 2.4. Module Cart (Giỏ hàng & Mục giỏ hàng)

### Bảng 10: `carts`
Giỏ hàng tổng thể của từng người dùng.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh giỏ hàng |
| `user_id` | BIGINT | NOT NULL, UNIQUE (`uk_carts_user_id`) | ID người sở hữu giỏ hàng (Loose coupling $\rightarrow$ `users.id`) |

### Bảng 11: `cart_items`
Các mục sản phẩm cụ thể đang được giữ trong giỏ hàng.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã mục giỏ hàng |
| `cart_id` | BIGINT | NOT NULL, FK $\rightarrow$ `carts(id)` ON DELETE CASCADE | Thuộc giỏ hàng nào |
| `product_variant_id` | BIGINT | NOT NULL | ID biến thể sản phẩm (Loose coupling $\rightarrow$ `product_variants.id`) |
| `quantity` | INT | NOT NULL | Số lượng khách hàng dự định mua |

---

## 2.5. Module Order (Đơn hàng, Chi tiết đơn hàng, Lịch sử can thiệp Admin)

### Bảng 12: `orders`
Thông tin đơn hàng chính thức sau khi checkout (mỗi đơn hàng đại diện cho 1 Shop cụ thể).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã đơn hàng |
| `user_id` | BIGINT | NOT NULL | ID người mua (Loose coupling $\rightarrow$ `users.id`) |
| `shop_id` | BIGINT | NOT NULL | ID Shop bán hàng (Loose coupling $\rightarrow$ `shops.id`) |
| `payment_group_id` | VARCHAR(36) | NULL, INDEX (`idx_order_payment_group`) | Mã UUID gom nhóm thanh toán khi mua hàng từ nhiều shop |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái đơn (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`) |
| `total_amount` | DECIMAL(15,2) | NOT NULL | Tổng tiền thực trả sau chiết khấu |
| `original_total_amount` | DECIMAL(15,2) | NOT NULL DEFAULT 0 | Tổng tiền hàng ban đầu trước khi giảm giá |
| `discount_amount` | DECIMAL(15,2) | NOT NULL DEFAULT 0 | Số tiền được giảm giá từ voucher |
| `shipping_address` | VARCHAR(500) | NOT NULL | Địa chỉ giao hàng do khách nhập |
| `payment_method` | VARCHAR(20) | NOT NULL | Phương thức thanh toán (`VNPAY`, `COD`) |
| `applied_voucher_id` | BIGINT | NULL | ID mã giảm giá đã áp dụng (Loose coupling $\rightarrow$ `vouchers.id`) |
| `applied_voucher_code` | VARCHAR(50) | NULL | Bản sao mã code voucher tại thời điểm đặt |
| `delivered_at` | DATETIME | NULL | Mốc thời gian đơn hàng được xác nhận đã giao thành công |
| `delivery_confirmed_by_user_id` | BIGINT | NULL | ID người bấm xác nhận nhận hàng (Buyer hoặc Admin) |
| `delivery_confirmation_source` | VARCHAR(20) | NULL | Nguồn xác nhận (`BUYER`, `ADMIN`, `TIMEOUT`) |
| `created_at` | DATETIME | NOT NULL | Ngày giờ tạo đơn |
| `updated_at` | DATETIME | NULL | Ngày giờ cập nhật trạng thái gần nhất |

### Bảng 13: `order_items`
Chi tiết từng mặt hàng trong đơn hàng (Áp dụng **Snapshot Pattern**).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh mục đơn hàng |
| `order_id` | BIGINT | NOT NULL, FK $\rightarrow$ `orders(id)` ON DELETE CASCADE | Thuộc đơn hàng nào |
| `product_variant_id` | BIGINT | NOT NULL | ID biến thể sản phẩm (Loose coupling snapshot) |
| `quantity` | INT | NOT NULL | Số lượng đặt mua |
| `unit_price` | DECIMAL(15,2) | NOT NULL | **Snapshot:** Đơn giá tại thời điểm đặt hàng |
| `product_name` | VARCHAR(255) | NOT NULL | **Snapshot:** Tên sản phẩm tại thời điểm đặt hàng |
| `image_url` | VARCHAR(255) | NULL | **Snapshot:** Link ảnh thumbnail sản phẩm tại thời điểm đặt |
| `sub_total` | DECIMAL(15,2) | NOT NULL | Thành tiền (`unit_price` $\times$ `quantity`) |

### Bảng 14: `order_admin_status_history`
Lịch sử kiểm toán (Audit Trail) khi Quản trị viên can thiệp thay đổi trạng thái đơn hàng.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh bản ghi lịch sử |
| `order_id` | BIGINT | NOT NULL, INDEX (`idx_admin_history_order_id`) | ID đơn hàng bị can thiệp |
| `actor_user_id` | BIGINT | NOT NULL | ID của Admin hoặc ID hệ thống tự động |
| `old_status` | VARCHAR(20) | NOT NULL | Trạng thái trước khi thay đổi |
| `new_status` | VARCHAR(20) | NOT NULL | Trạng thái mới sau khi can thiệp |
| `reason` | VARCHAR(500) | NOT NULL | Lý do can thiệp bắt buộc |
| `created_at` | DATETIME | NOT NULL | Mốc thời gian thực hiện thao tác |

---

## 2.6. Module Payment (Thanh toán & Lịch sử hoàn tiền Refund)

### Bảng 15: `payments`
Giao dịch thanh toán trực tuyến hoặc ghi nhận COD.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã bản ghi thanh toán |
| `payment_group_id` | VARCHAR(36) | NOT NULL, INDEX (`idx_payment_group_created`) | Khớp với `orders.payment_group_id` (Không dùng FK) |
| `amount` | DECIMAL(15,2) | NOT NULL | Tổng số tiền phải thanh toán |
| `method` | VARCHAR(20) | NOT NULL | Hình thức (`VNPAY`, `COD`) |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái giao dịch (`PENDING`, `SUCCESS`, `FAILED`, `CANCELED`) |
| `transaction_id` | VARCHAR(100) | NOT NULL, UNIQUE (`uk_payments_transaction_id`) | Mã giao dịch nội bộ hệ thống sinh (`PAY_xxxxxxxx`) |
| `gateway_transaction_id` | VARCHAR(100) | NULL | Mã giao dịch do cổng VNPay trả về (`vnp_TransactionNo`) |
| `gateway_bank_code` | VARCHAR(50) | NULL | Mã ngân hàng khách dùng thanh toán (`NCB`, `VCB`,...) |
| `vnpay_transaction_date` | VARCHAR(14) | NULL | Định dạng `yyyyMMddHHmmss` phục vụ cho API hoàn tiền |
| `raw_data` | TEXT | NULL | Toàn bộ dữ liệu phản hồi thô từ cổng thanh toán để kiểm toán |
| `created_at` | DATETIME | NOT NULL | Thời gian bắt đầu giao dịch |
| `updated_at` | DATETIME | NULL | Thời gian hoàn tất thanh toán |

### Bảng 16: `refund_attempts`
Quản lý các yêu cầu hoàn tiền cho đơn hàng đã thanh toán qua VNPay.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã bản ghi hoàn tiền |
| `order_id` | BIGINT | NOT NULL, INDEX (`idx_refund_order_created`) | Đơn hàng cần hoàn tiền |
| `payment_id` | BIGINT | NOT NULL, INDEX (`idx_refund_payment_status`) | Bản ghi thanh toán liên quan |
| `payment_group_id` | VARCHAR(36) | NOT NULL | Nhóm thanh toán tương ứng |
| `amount` | DECIMAL(15,2) | NOT NULL | Số tiền hoàn lại cho đơn này |
| `currency` | VARCHAR(3) | NOT NULL | Loại tiền tệ (`VND`) |
| `idempotency_key` | VARCHAR(128) | NOT NULL, UNIQUE (`uk_refund_idempotency_key`) | Khóa chống trùng lặp yêu cầu hoàn tiền |
| `merchant_request_id`| VARCHAR(32) | NOT NULL, UNIQUE (`uk_refund_merchant_request_id`) | Mã yêu cầu gửi sang cổng VNPay |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái hoàn tiền (`PENDING`, `SUCCESS`, `FAILED`) |
| `actor_user_id` | BIGINT | NOT NULL | ID Admin hoặc người ra lệnh hoàn tiền |
| `reason` | VARCHAR(500) | NOT NULL | Lý do hoàn tiền (ví dụ: Hủy đơn hàng sau thanh toán) |
| `original_txn_ref` | VARCHAR(100) | NOT NULL | Mã tham chiếu giao dịch gốc |
| `original_gateway_txn_no` | VARCHAR(32) | NULL | Mã giao dịch gốc của cổng thanh toán |
| `original_gateway_txn_at` | VARCHAR(14) | NULL | Thời điểm giao dịch gốc |
| `refund_gateway_txn_no` | VARCHAR(32) | NULL | Mã giao dịch hoàn tiền cổng thanh toán cấp |
| `gateway_response_code` | VARCHAR(16) | NULL | Mã kết quả từ VNPay (`00` = Thành công) |
| `sanitized_gateway_metadata` | TEXT | NULL | Metadata phản hồi an toàn từ cổng thanh toán |
| `requested_at` | DATETIME | NOT NULL | Thời điểm tạo yêu cầu hoàn tiền |
| `resolved_at` | DATETIME | NULL | Thời điểm hoàn tất/kết thúc hoàn tiền |
| `created_at` | DATETIME | NOT NULL | Ngày tạo bản ghi |

---

## 2.7. Module Voucher (Khuyến mãi & Mã giảm giá)

### Bảng 17: `vouchers`
Danh sách mã giảm giá do Sàn hoặc Shop phát hành.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh voucher |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE (`uk_vouchers_code`) | Mã ký tự giảm giá (ví dụ: `SUMMER2026`) |
| `scope` | VARCHAR(20) | NOT NULL | Phạm vi áp dụng (`SYSTEM`: Toàn sàn, `SHOP`: Gian hàng cụ thể) |
| `shop_id` | BIGINT | NULL | ID gian hàng (Bắt buộc nếu `scope = 'SHOP'`; `NULL` nếu toàn sàn) |
| `type` | VARCHAR(20) | NOT NULL | Loại giảm giá (`PERCENTAGE`: Theo %, `FIXED_AMOUNT`: Số tiền cố định) |
| `discount_value` | DECIMAL(15,2) | NOT NULL | Giá trị giảm (% hoặc số tiền cụ thể) |
| `max_discount` | DECIMAL(15,2) | NULL | Số tiền giảm tối đa (Áp dụng khi `type = 'PERCENTAGE'`) |
| `min_order_value` | DECIMAL(15,2) | NULL | Giá trị đơn hàng tối thiểu để được áp dụng |
| `start_date` | DATETIME | NULL | Ngày bắt đầu hiệu lực |
| `end_date` | DATETIME | NULL | Ngày hết hạn |
| `usage_limit` | INT | NULL | Tổng số lượt dùng tối đa (`NULL` = không giới hạn) |
| `used_count` | INT | NOT NULL DEFAULT 0 | Số lượt đã sử dụng thực tế |
| `created_at` | DATETIME | NOT NULL | Ngày tạo |
| `updated_at` | DATETIME | NULL | Ngày chỉnh sửa |

---

## 2.8. Module Review (Đánh giá & Hình ảnh đánh giá)

### Bảng 18: `reviews`
Đánh giá chất lượng sản phẩm từ khách hàng đã mua.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh đánh giá |
| `user_id` | BIGINT | NOT NULL | ID người đánh giá (Loose coupling $\rightarrow$ `users.id`) |
| `product_id` | BIGINT | NOT NULL, INDEX (`idx_review_product_created`) | ID sản phẩm được đánh giá |
| `order_id` | BIGINT | NOT NULL | ID đơn hàng đã mua chứa sản phẩm này |
| `rating` | INT | NOT NULL | Điểm đánh giá (từ 1 đến 5 sao) |
| `comment` | VARCHAR(1000) | NOT NULL | Nội dung nhận xét chi tiết |
| `vendor_reply` | TEXT | NULL | Phản hồi của chủ shop về đánh giá này |
| `vendor_replied_at` | DATETIME | NULL | Mốc thời gian chủ shop gửi phản hồi |
| `created_at` | DATETIME | NOT NULL | Ngày đăng đánh giá |
| `updated_at` | DATETIME | NULL | Ngày cập nhật |
| — | UNIQUE (`uk_user_order_product`) | `user_id, order_id, product_id` | **Chống spam:** 1 khách hàng chỉ được đánh giá 1 sản phẩm trong 1 đơn hàng duy nhất |

### Bảng 19: `review_images`
Hình ảnh đính kèm bài đánh giá (được ánh xạ bằng `@ElementCollection` trong JPA).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `review_id` | BIGINT | NOT NULL, FK $\rightarrow$ `reviews(id)` ON DELETE CASCADE | Thuộc bài đánh giá nào |
| `image_url` | VARCHAR(255) | NOT NULL | Đường dẫn ảnh feedback trên CDN |

---

## 2.9. Module Notification (Thông báo hệ thống)

### Bảng 20: `notifications`
Thông báo gửi tới người dùng trong hệ thống.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh thông báo |
| `user_id` | BIGINT | NOT NULL, INDEX (`idx_notif_user_created`) | ID người nhận thông báo (Loose coupling $\rightarrow$ `users.id`) |
| `type` | VARCHAR(30) | NOT NULL | Loại thông báo (`ORDER_CONFIRMED`, `ORDER_SHIPPED`, `NEW_REVIEW`, `SYSTEM`) |
| `title` | VARCHAR(255) | NOT NULL | Tiêu đề thông báo ngắn gọn |
| `message` | VARCHAR(500) | NOT NULL | Nội dung chi tiết thông báo |
| `reference_id` | VARCHAR(50) | NULL | Mã đối tượng liên quan (ID đơn hàng, ID bài đánh giá,...) |
| `is_read` | TINYINT(1) | NOT NULL DEFAULT 0 | Trạng thái đọc (`0` = Chưa đọc, `1` = Đã đọc) |
| `created_at` | DATETIME | NOT NULL | Ngày tạo thông báo |

---

## 2.10. Module Chat (Phòng chat & Tin nhắn Realtime)

### Bảng 21: `chat_rooms`
Phòng trò chuyện riêng giữa Khách mua (`buyer`) và Gian hàng (`shop`).

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh phòng chat |
| `buyer_id` | BIGINT | NOT NULL | ID khách mua hàng (Loose coupling $\rightarrow$ `users.id`) |
| `shop_id` | BIGINT | NOT NULL | ID gian hàng (Loose coupling $\rightarrow$ `shops.id`) |
| `created_at` | DATETIME | NOT NULL | Ngày mở phòng chat |
| `updated_at` | DATETIME | NULL | Mốc thời gian có tin nhắn mới nhất |
| — | UNIQUE (`uk_chat_rooms_buyer_shop`) | `buyer_id, shop_id` | Đảm bảo giữa 1 khách và 1 shop chỉ có 1 phòng duy nhất |

### Bảng 22: `chat_messages`
Các tin nhắn được trao đổi bên trong phòng chat.

| Tên Cột | Kiểu Dữ Liệu | Ràng Buộc | Ý Nghĩa Nghiệp Vụ |
|---|---|---|---|
| `id` | BIGINT | PK, AUTO_INCREMENT | Mã định danh tin nhắn |
| `room_id` | BIGINT | NOT NULL, INDEX (`idx_chat_room_created`) | Thuộc phòng chat nào (Loose coupling $\rightarrow$ `chat_rooms.id`) |
| `sender_id` | BIGINT | NOT NULL | ID người gửi (Loose coupling $\rightarrow$ `users.id`) |
| `content` | TEXT | NOT NULL | Nội dung văn bản tin nhắn |
| `is_read` | TINYINT(1) | NOT NULL DEFAULT 0 | Trạng thái đối phương đã đọc (`0` = Chưa, `1` = Đã đọc) |
| `created_at` | DATETIME | NOT NULL | Thời điểm gửi tin nhắn |

---

## 2.11. Các Pattern Thiết kế Cơ sở Dữ liệu Cốt lõi

1. **Loose Coupling Pattern (Phân ly liên module):** Các trường quan hệ như `shop_id`, `user_id`, `product_variant_id` giữa các bảng ở module khác nhau không sử dụng ràng buộc `FOREIGN KEY` cấp DB. Lợi ích: Cho phép phát triển độc lập, không bị khóa schema chéo, và sẵn sàng chuyển đổi sang kiến trúc Microservices trong tương lai nếu muốn tách riêng Database.
2. **Snapshot Pattern trong `order_items`:** Các trường `unit_price`, `product_name`, `image_url` được copy trực tiếp vào bản ghi `order_items` tại thời điểm đặt hàng. Lợi ích: Đảm bảo tính toàn vẹn lịch sử đơn hàng và tính chính xác của báo cáo tài chính ngay cả khi người bán thay đổi giá, đổi tên hoặc sửa ảnh sản phẩm gốc sau này.
3. **UUID Grouping Pattern trong `orders` & `payments`:** Hệ thống giải quyết bài toán sàn thương mại điện tử: 1 lần checkout có thể chứa sản phẩm của $N$ shop khác nhau. Giải pháp: Tạo ra $N$ đơn hàng độc lập (mỗi đơn ứng với 1 `shop_id`), và gắn chung một mã UUID `payment_group_id`. Cổng thanh toán chỉ cần tạo 1 giao dịch cho mã `payment_group_id` này.
4. **Idempotency Pattern trong `payments` & `refund_attempts`:** Sử dụng các khóa duy nhất `transaction_id`, `idempotency_key`, `merchant_request_id` kết hợp với Pessimistic Lock (`SELECT ... FOR UPDATE`) để ngăn chặn hoàn toàn việc trừ tiền hoặc hoàn tiền lặp lại do mạng chập chờn hay người dùng double-click.

---

# 3. CHI TIẾT CÁC HÀM THÀNH VIÊN VÀ CHỨC NĂNG NGHIỆP VỤ (BACKEND MEMBER METHODS)

Phần này đặc tả chi tiết toàn bộ các lớp Controller và Service trong Backend, bao gồm các phương thức public, tham số đầu vào, kiểu dữ liệu trả về và logic nghiệp vụ bên trong.

---

## 3.1. Module Identity & Authentication

### Lớp `AuthController`
- **Đường dẫn:** `@RequestMapping("/api/v1/auth")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<AuthResponse>> register(@Valid @RequestBody UserRegisterRequest request)`
     - *Chức năng:* Tiếp nhận yêu cầu đăng ký tài khoản từ người dùng mới.
     - *Luồng thực thi:* Chuyển tiếp tới `AuthenticationService.register(request)`. Trả về HTTP 201 Created cùng token JWT nếu thành công.
  2. `public ResponseEntity<ApiResponse<AuthResponse>> authenticate(@Valid @RequestBody UserLoginRequest request)`
     - *Chức năng:* Xác thực đăng nhập qua email và mật khẩu.
     - *Luồng thực thi:* Chuyển tiếp tới `AuthenticationService.authenticate(request)`. Trả về HTTP 200 OK cùng token JWT.

### Lớp `ProfileController`
- **Đường dẫn:** `@RequestMapping("/api/v1/profile")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<ProfileResponse>> getProfile(@CurrentUserId Long userId)`
     - *Chức năng:* Lấy thông tin tài khoản cá nhân hiện tại của người dùng đang đăng nhập.
  2. `public ResponseEntity<ApiResponse<ProfileResponse>> updateProfile(@CurrentUserId Long userId, @Valid @RequestBody ProfileUpdateRequest request)`
     - *Chức năng:* Cập nhật họ tên (`fullName`) và số điện thoại (`phone`).

### Lớp `AuthenticationService`
- **Annotation:** `@Service`, `@RequiredArgsConstructor`
- **Các hàm thành viên:**
  1. `@Transactional public AuthResponse register(UserRegisterRequest request)`
     - *Chức năng:* Đăng ký người dùng mới vào hệ thống.
     - *Logic chi tiết:*
       + Kiểm tra email đã tồn tại trong `UserRepository` chưa. Nếu có $\rightarrow$ ném lỗi `AppException(ErrorCode.EMAIL_ALREADY_EXISTS)`.
       + Tìm vai trò mặc định `ROLE_USER` trong `RoleRepository` (hoặc tạo mới nếu chưa có).
       + Mã hóa mật khẩu bằng `passwordEncoder.encode(request.getPassword())`.
       + Khởi tạo entity `User` với trạng thái `ACTIVE`, thêm `ROLE_USER` vào danh sách roles và lưu vào `userRepository`.
       + Khởi tạo `CustomUserDetails(user)` và gọi `jwtService.generateToken(userDetails)` để sinh JWT Access Token (24h).
       + Trả về `AuthResponse.builder().token(jwtToken).build()`.
  2. `public AuthResponse authenticate(UserLoginRequest request)`
     - *Chức năng:* Xác thực thông tin đăng nhập.
     - *Logic chi tiết:*
       + Gọi `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))` để Spring Security kiểm tra mật khẩu qua BCrypt.
       + Tìm kiếm entity `User` theo email qua `userRepository.findByEmail(email)`. Nếu không thấy $\rightarrow$ ném lỗi `USER_NOT_FOUND`.
       + Tạo `CustomUserDetails` từ `User` và gọi `jwtService.generateToken(userDetails)` sinh token.
       + Trả về `AuthResponse` chứa chuỗi token.

### Lớp `ProfileService`
- **Annotation:** `@Service`, `@RequiredArgsConstructor`
- **Các hàm thành viên:**
  1. `@Transactional(readOnly = true) public ProfileResponse getProfile(Long userId)`
     - *Chức năng:* Tìm người dùng theo ID, ném ngoại lệ nếu không thấy, đóng gói trả về `ProfileResponse` (id, email, fullName, phone, roles, status).
  2. `@Transactional public ProfileResponse updateProfile(Long userId, ProfileUpdateRequest request)`
     - *Chức năng:* Cập nhật thông tin profile của user (họ tên và số điện thoại).

### Lớp `IdentityInternalServiceImpl` (implements `IdentityInternalService`)
- **Chức năng:** Cung cấp dịch vụ nội bộ cho các module khác để tra cứu thông tin tóm tắt của User.
- **Các hàm thành viên:**
  1. `public UserSummaryDto getUserSummary(Long userId)`: Lấy họ tên và email của 1 user.
  2. `public Map<Long, UserSummaryDto> getUserSummaries(Collection<Long> userIds)`: Lấy hàng loạt họ tên của danh sách user IDs (tối ưu hóa N+1 query).

---

## 3.2. Module Shop

### Lớp `ShopController`
- **Đường dẫn:** `@RequestMapping("/api/v1/shops")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<ShopResponse>> registerShop(@CurrentUserId Long userId, @Valid @RequestBody ShopCreateRequest request)`: Người dùng đăng ký trở thành Vendor (mở shop).
  2. `public ResponseEntity<ApiResponse<ShopResponse>> getMyShop(@CurrentUserId Long userId)`: Lấy thông tin Shop của tài khoản hiện tại.
  3. `public ResponseEntity<ApiResponse<ShopResponse>> getShopById(@PathVariable Long id)`: Lấy thông tin công khai của Shop theo ID.

### Lớp `AdminShopController`
- **Đường dẫn:** `@RequestMapping("/api/v1/admin/shops")`
- **Annotation:** `@PreAuthorize("hasRole('ADMIN')")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<List<ShopResponse>>> getShopsByStatus(@RequestParam(required = false) String status)`: Danh sách Shop theo bộ lọc trạng thái.
  2. `public ResponseEntity<ApiResponse<ShopResponse>> approveShop(@PathVariable Long id)`: Phê duyệt Shop đang `PENDING` thành `ACTIVE`.
  3. `public ResponseEntity<ApiResponse<ShopResponse>> banShop(@PathVariable Long id)`: Khóa Shop vi phạm (`BANNED`).
  4. `public ResponseEntity<ApiResponse<ShopResponse>> unbanShop(@PathVariable Long id)`: Mở khóa Shop, hoàn trả trạng thái cũ.

### Lớp `ShopService` / `ShopServiceImpl`
- **Các hàm thành viên:**
  1. `@Transactional public ShopResponse registerShop(Long userId, ShopCreateRequest request)`
     - *Logic:* Kiểm tra user đã sở hữu shop chưa (mỗi user chỉ được mở 1 shop). Kiểm tra tên shop có trùng không. Tạo Shop mới với `status = ShopStatus.PENDING`.
  2. `@Transactional public ShopResponse approveShop(Long id)`
     - *Logic:* Tìm shop theo ID, kiểm tra trạng thái có phải `PENDING` không. Đổi `status = ShopStatus.ACTIVE`. Tự động cấp thêm quyền `ROLE_VENDOR` cho chủ sở hữu shop trong `user_roles`.
  3. `@Transactional public ShopResponse banShop(Long id)`
     - *Logic:* Tìm shop, lưu trạng thái hiện tại vào `priorStatus = shop.getStatus()`, sau đó gán `status = ShopStatus.BANNED`. Ngăn chặn người dùng đặt hàng sản phẩm của shop này ngay lập tức.
  4. `@Transactional public ShopResponse unbanShop(Long id)`
     - *Logic:* Khôi phục `status = (priorStatus != null ? priorStatus : ShopStatus.ACTIVE)`, gán `priorStatus = null`.
  5. `@Transactional(readOnly = true) public ShopResponse getMyShop(Long userId)`: Tra cứu shop của user.
  6. `@Transactional(readOnly = true) public ShopResponse getShopById(Long id)`: Tra cứu shop công khai.

### Lớp `ShopInternalServiceImpl` (implements `ShopInternalService`)
- **Chức năng:** Cung cấp kiểm tra quyền sở hữu và trạng thái Shop cho các module khác.
- **Các hàm thành viên:**
  1. `public boolean isShopOwner(Long shopId, Long userId)`: Kiểm tra xem user có phải chủ của `shopId` không.
  2. `public Long getShopIdByUserId(Long userId)`: Lấy shopId đang `ACTIVE` của user.
  3. `public Optional<Long> findShopIdByUserId(Long userId)`: Tìm shopId của user (kể cả khi chưa active).
  4. `public Optional<Long> getUserIdByShopId(Long shopId)`: Lấy userId của chủ shop.
  5. `public void requireNotBannedForSale(Long shopId)`: Ném lỗi `BusinessException` nếu Shop đang ở trạng thái `BANNED`.
  6. `public Set<Long> getBannedShopIds()`: Lấy danh sách ID các Shop đang bị cấm để loại trừ khỏi tìm kiếm và giỏ hàng.
  7. `public Map<Long, String> getShopNames(Collection<Long> shopIds)`: Lấy map tên các Shop theo danh sách ID.

---

## 3.3. Module Product & Category

### Lớp `ProductController`
- **Đường dẫn:** `@RequestMapping("/api/v1/products")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<Page<ProductResponse>>> getAllProducts(...)`: Tìm kiếm sản phẩm công khai với bộ lọc phân trang, từ khóa, danh mục, khoảng giá, sắp xếp (`price_asc`, `price_desc`, `newest`).
  2. `public ResponseEntity<ApiResponse<ProductResponse>> getProductById(@PathVariable Long id)`: Lấy chi tiết sản phẩm kèm danh sách biến thể và hình ảnh.
  3. `public ResponseEntity<ApiResponse<List<ProductResponse>>> getRelatedProducts(@PathVariable Long id, @RequestParam(defaultValue = "4") int limit)`: Sản phẩm liên quan cùng danh mục.
  4. `public ResponseEntity<ApiResponse<List<ProductResponse>>> getRecommendedProducts(@CurrentUserId Long userId, @RequestParam(defaultValue = "8") int limit)`: Gợi ý sản phẩm thông minh cá nhân hóa.

### Lớp `VendorProductController`
- **Đường dẫn:** `@RequestMapping("/api/v1/vendor/products")`
- **Annotation:** `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<Page<ProductResponse>>> getVendorProducts(@CurrentUserId Long userId, ...)`: Danh sách sản phẩm thuộc quyền quản lý của Vendor.
  2. `public ResponseEntity<ApiResponse<ProductResponse>> getVendorProductById(@CurrentUserId Long userId, @PathVariable Long id)`: Chi tiết sản phẩm phía Vendor.
  3. `public ResponseEntity<ApiResponse<ProductResponse>> createProduct(@CurrentUserId Long userId, @Valid @RequestBody ProductCreateRequest request)`: Thêm sản phẩm mới kèm hình ảnh và biến thể SKU.
  4. `public ResponseEntity<ApiResponse<ProductResponse>> updateProduct(@CurrentUserId Long userId, @PathVariable Long id, @Valid @RequestBody ProductUpdateRequest request)`: Cập nhật thông tin sản phẩm, hình ảnh và biến thể.
  5. `public ResponseEntity<ApiResponse<Void>> deleteProduct(@CurrentUserId Long userId, @PathVariable Long id)`: Ẩn sản phẩm (`status = HIDDEN`).

### Lớp `CategoryController`
- **Đường dẫn:** `@RequestMapping("/api/v1/categories")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<List<CategoryResponse>>> getAllCategories()`: Lấy toàn bộ danh mục sản phẩm trên sàn.
  2. `public ResponseEntity<ApiResponse<CategoryResponse>> getCategoryById(@PathVariable Long id)`: Xem chi tiết danh mục.

### Lớp `ProductServiceImpl` (implements `ProductService`)
- **Các hàm thành viên:**
  1. `@Transactional public ProductResponse createProduct(Long userId, ProductCreateRequest request)`
     - *Logic:* Kiểm tra category hợp lệ; lấy `shopId` của user và kiểm tra shop không bị cấm bán (`requireNotBannedForSale`). Tạo `Product` entity. Nếu có ảnh, gán vào product và chuẩn hóa thumbnail. Lưu `productRepository.save(product)`. Lưu danh sách biến thể qua `productVariantRepository.saveAll(...)`.
  2. `@Transactional public ProductResponse updateProduct(Long userId, Long id, ProductUpdateRequest request)`
     - *Logic:* Khóa sản phẩm `findById`, kiểm tra quyền sở hữu qua `shopInternalService.isShopOwner(product.getShopId(), userId)`. Cập nhật tên, giá, mô tả. Cập nhật danh sách ảnh và đồng bộ các biến thể (thêm mới, cập nhật giá/kho, hoặc xóa biến thể cũ).
  3. `@Transactional public void deleteProduct(Long userId, Long id)`
     - *Logic:* Chuyển `status = ProductStatus.HIDDEN` (Soft delete) để không làm hỏng dữ liệu các đơn hàng cũ.
  4. `@Transactional(readOnly = true) public List<ProductResponse> getRecommendedProducts(Long userId, int limit)`
     - *Logic (Recommendation Engine):*
       + Nếu người dùng đã đăng nhập: Gọi `orderInternalService.getRecentlyPurchasedVariantIds(userId, limit)` để lấy các biến thể khách vừa mua gần đây, tìm các sản phẩm tương tự cùng danh mục.
       + Nếu chưa đăng nhập hoặc chưa đủ số lượng: Gọi `orderInternalService.getTopSellingVariantIds(limit)` lấy các sản phẩm bán chạy nhất toàn sàn bù vào danh sách gợi ý.
  5. `@Transactional(readOnly = true) public Page<ProductResponse> getAllProducts(...)`: Thực hiện truy vấn phân trang với Specification động và EntityGraph (`@EntityGraph(attributePaths = {"category"})`) để loại bỏ hoàn toàn lỗi N+1 Query.

### Lớp `ProductInternalServiceImpl` (implements `ProductInternalService`)
- **Chức năng:** Cung cấp dịch vụ quản trị biến thể và tồn kho nội bộ cho Cart, Order, Review.
- **Các hàm thành viên:**
  1. `public ProductVariantInfoDto getVariantInfo(Long variantId)`: Lấy thông tin giá, tồn kho, shop sở hữu của một biến thể.
  2. `public Map<Long, ProductVariantInfoDto> getVariantInfos(Collection<Long> variantIds)`: Lấy hàng loạt thông tin biến thể (tối ưu truy vấn).
  3. `public void deductStock(Long variantId, int quantity)`: Trừ tồn kho biến thể một cách an toàn (`UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`). Nếu không cập nhật được bản ghi nào $\rightarrow$ ném lỗi hết hàng.
  4. `public void restoreStock(Long variantId, int quantity)`: Hoàn trả tồn kho khi đơn hàng bị hủy (`stock_quantity = stock_quantity + ?`).
  5. `public Long getProductIdByVariantId(Long variantId)`: Tra cứu ID sản phẩm từ ID biến thể.
  6. `public Long getShopIdByProductId(Long productId)`: Tra cứu Shop sở hữu sản phẩm.

---

## 3.4. Module Cart

### Lớp `CartController`
- **Đường dẫn:** `@RequestMapping("/api/v1/carts")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<CartResponse>> getMyCart(@CurrentUserId Long userId)`: Lấy thông tin giỏ hàng chi tiết (kèm giá cập nhật và trạng thái tồn kho).
  2. `public ResponseEntity<ApiResponse<Void>> addToCart(@CurrentUserId Long userId, @Valid @RequestBody CartItemRequest request)`: Thêm một biến thể sản phẩm vào giỏ hàng.
  3. `public ResponseEntity<ApiResponse<Void>> updateCartItemQuantity(@CurrentUserId Long userId, @PathVariable Long itemId, @Valid @RequestBody CartItemUpdateRequest request)`: Đổi số lượng của một mục trong giỏ.
  4. `public ResponseEntity<ApiResponse<Void>> removeCartItem(@CurrentUserId Long userId, @PathVariable Long itemId)`: Xóa 1 mục khỏi giỏ hàng.
  5. `public ResponseEntity<ApiResponse<Void>> clearCart(@CurrentUserId Long userId)`: Xóa sạch toàn bộ giỏ hàng.

### Lớp `CartServiceImpl` (implements `CartService`)
- **Các hàm thành viên:**
  1. `@Transactional public void addToCart(Long userId, CartItemRequest request)`
     - *Logic:* Gọi `productInternalService.getVariantInfo` kiểm tra trạng thái `ACTIVE` và số lượng tồn kho. Gọi `shopInternalService.requireNotBannedForSale` kiểm tra shop không bị cấm bán. Dùng `cartRepository.findByUserIdForUpdate(userId)` để khóa ghi chống race condition. Nếu item đã có trong giỏ $\rightarrow$ cộng dồn số lượng (kiểm tra không vượt tồn kho); nếu chưa có $\rightarrow$ tạo mới `CartItem` thêm vào `cart.getItems()`. Gọi `cartRepository.save(cart)`.
  2. `@Transactional(readOnly = true) public CartResponse getCart(Long userId)`
     - *Logic:* Lấy giỏ hàng, lấy danh sách `variantIds`, gọi `productInternalService.getVariantInfos` hàng loạt để lấy giá và tồn kho realtime. Lấy danh sách shop bị cấm qua `shopInternalService.getBannedShopIds()`. Đánh dấu `isAvailable = false` nếu sản phẩm hết hàng hoặc shop bị khóa. Tính toán `totalAmount` chỉ cho các món hàng khả dụng.
  3. `@Transactional public void updateItemQuantity(Long userId, Long itemId, int quantity)`: Cập nhật số lượng mới sau khi đối soát tồn kho khả dụng.
  4. `@Transactional public void removeItem(Long userId, Long itemId)`: Xóa mục khỏi danh sách `cart.getItems()`.
  5. `@Transactional public void clearCart(Long userId)`: Làm rỗng giỏ hàng.

### Lớp `CartInternalServiceImpl` (implements `CartInternalService`)
- **Chức năng:** Cung cấp thông tin giỏ hàng cho Module Order khi thanh toán.
- **Các hàm thành viên:**
  1. `public List<CartItemInternalDto> getCartItems(Long userId)`: Lấy danh sách toàn bộ các món trong giỏ của người dùng chuyển đổi thành DTO phục vụ checkout.
  2. `public void removeCartItems(Long userId, List<Long> cartItemIds)`: Xóa chính xác các mục đã được đặt mua thành công khỏi giỏ hàng.

---

## 3.5. Module Order

### Lớp `OrderController`
- **Đường dẫn:** `@RequestMapping("/api/v1/orders")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<CheckoutResponse>> checkout(@CurrentUserId Long userId, @Valid @RequestBody CheckoutRequest request)`: Thực hiện đặt hàng.
  2. `public ResponseEntity<ApiResponse<CartPreviewResponse>> previewCheckout(@CurrentUserId Long userId, @Valid @RequestBody CartPreviewRequest request)`: Xem trước giá trị đơn hàng, phí giảm giá từ voucher trước khi bấm đặt.
  3. `public ResponseEntity<ApiResponse<Page<OrderResponse>>> getMyOrders(@CurrentUserId Long userId, ...)`: Xem danh sách đơn hàng đã mua của khách.
  4. `public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@CurrentUserId Long userId, @PathVariable Long id)`: Xem chi tiết một đơn hàng.
  5. `public ResponseEntity<ApiResponse<OrderResponse>> cancelOrder(@CurrentUserId Long userId, @PathVariable Long id)`: Người mua hủy đơn hàng khi đơn còn ở trạng thái `PENDING`.
  6. `public ResponseEntity<ApiResponse<OrderResponse>> confirmDelivery(@CurrentUserId Long userId, @PathVariable Long id)`: Người mua bấm "Đã nhận hàng" khi đơn ở trạng thái `SHIPPED`.

### Lớp `VendorOrderController`
- **Đường dẫn:** `@RequestMapping("/api/v1/vendor/orders")`
- **Annotation:** `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<Page<OrderResponse>>> getVendorOrders(@CurrentUserId Long userId, ...)`: Xem danh sách đơn hàng thuộc gian hàng của Vendor.
  2. `public ResponseEntity<ApiResponse<OrderResponse>> getVendorOrderDetail(@CurrentUserId Long userId, @PathVariable Long id)`: Xem chi tiết đơn hàng phía Vendor.
  3. `public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(@CurrentUserId Long userId, @PathVariable Long id, @Valid @RequestBody VendorOrderStatusUpdateRequest request)`: Cập nhật trạng thái đơn (`CONFIRMED` $\rightarrow$ `SHIPPED` hoặc `CANCELLED`).

### Lớp `AdminOrderController`
- **Đường dẫn:** `@RequestMapping("/api/v1/admin/orders")`
- **Annotation:** `@PreAuthorize("hasRole('ADMIN')")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<Page<OrderResponse>>> getAllOrders(...)`: Quản trị viên xem tất cả đơn hàng toàn sàn.
  2. `public ResponseEntity<ApiResponse<OrderResponse>> getOrderDetail(@PathVariable Long id)`: Admin xem chi tiết đơn hàng bất kỳ.
  3. `public ResponseEntity<ApiResponse<OrderResponse>> updateOrderStatus(@PathVariable Long id, @Valid @RequestBody AdminOrderStatusUpdateRequest request)`: Admin cưỡng chế đổi trạng thái đơn (ghi nhật ký kiểm toán).
  4. `public ResponseEntity<ApiResponse<OrderResponse>> confirmDeliveryByAdmin(@PathVariable Long id, @RequestParam Long adminUserId)`: Admin xác nhận giao hàng thay thế (ghi nhận `DeliveryConfirmationSource.ADMIN`).

### Lớp `OrderServiceImpl` (implements `OrderService` & `OrderInternalService`)
- **Các hàm thành viên cốt lõi:**
  1. `@Transactional public CheckoutResponse checkout(Long userId, CheckoutRequest request)`
     - *Logic:*
       + Gọi `cartInternalService.getCartItems(userId)` lấy các món cần thanh toán.
       + Kiểm tra các Shop tương ứng có bị cấm bán không qua `shopInternalService.requireNotBannedForSale`.
       + Gom nhóm các mục theo `shopId` $\rightarrow$ mỗi shop sẽ là 1 đơn hàng riêng biệt.
       + Sinh mã nhóm thanh toán chung `paymentGroupId = UUID.randomUUID().toString()`.
       + Nếu có `voucherCode`: Gọi `voucherInternalService.calculateDiscount` để tính toán số tiền giảm cho từng Shop. Sau đó gọi `voucherInternalService.incrementUsage` để trừ lượt dùng.
       + Với từng Shop: Khởi tạo `Order (status = PENDING)`. Với từng món hàng trong shop: Gọi `productInternalService.deductStock` để trừ kho, tạo `OrderItem` (copy giá, tên, link ảnh snapshot).
       + Lưu các Order vào `orderRepository.save(order)`.
       + Xóa các món đã mua khỏi giỏ hàng qua `cartInternalService.removeCartItems`.
       + Nếu chọn phương thức thanh toán `COD`: Bắn `OrderConfirmedEvent` ngay lập tức để gửi thông báo cho khách và chủ shop.
       + Đóng gói trả về `CheckoutResponse(paymentGroupId, orderResponses)`.
  2. `@Transactional public OrderResponse confirmDelivery(Long userId, Long orderId)`
     - *Logic:* Khóa đơn `orderRepository.findByIdForUpdate(orderId)`. Kiểm tra `order.userId == userId` và `order.status == SHIPPED`. Chuyển trạng thái sang `DELIVERED`, gán `deliveredAt = now()`, gán `deliveryConfirmedByUserId = userId`, gán `deliveryConfirmationSource = DeliveryConfirmationSource.BUYER`. Lưu đơn hàng.
  3. `@Transactional public OrderResponse cancelOrder(Long userId, Long orderId)`
     - *Logic:* Khóa đơn. Chỉ cho phép hủy nếu `status == PENDING`. Chuyển trạng thái sang `CANCELLED`. Duyệt danh sách các món trong đơn và gọi `productInternalService.restoreStock(variantId, quantity)` để hoàn trả tồn kho về kho hàng.
  4. `@Transactional public OrderResponse updateVendorOrderStatus(Long userId, Long orderId, VendorOrderStatusUpdateRequest request)`
     - *Logic:* Khóa đơn `findByIdForUpdate`. Kiểm tra quyền sở hữu qua `shopInternalService.isShopOwner(order.getShopId(), userId)`. Kiểm tra tính hợp lệ của máy trạng thái (`VENDOR_STATE_TRANSITIONS`: chỉ cho phép `PENDING` $\rightarrow$ `CONFIRMED`, `CONFIRMED` $\rightarrow$ `SHIPPED` hoặc `CANCELLED`). Nếu chuyển sang `SHIPPED`: Bắn sự kiện `OrderShippedEvent` để thông báo cho khách.
  5. `@Transactional public void confirmPendingOrdersByGroup(String paymentGroupId)` (Hàm nội bộ cho Payment IPN):
     - *Logic:* Tìm tất cả đơn hàng theo `payment_group_id` với khóa `FOR UPDATE`. Lọc các đơn đang `PENDING`, chuyển sang `CONFIRMED`, lưu DB và phát sự kiện `OrderConfirmedEvent`.

---

## 3.6. Module Payment & Refund

### Lớp `PaymentController`
- **Đường dẫn:** `@RequestMapping("/api/v1/payments")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<PaymentCreateResponse>> createPaymentUrl(@CurrentUserId Long userId, @Valid @RequestBody PaymentCreateRequest request, HttpServletRequest httpRequest)`: Khởi tạo giao dịch thanh toán VNPay và sinh URL redirect.
  2. `public ResponseEntity<?> vnpayIpn(@RequestParam Map<String, String> queryParams)`: Endpoint Webhook tiếp nhận kết quả thanh toán từ VNPay server.
  3. `public ResponseEntity<ApiResponse<PaymentGroupStatusResponse>> getPaymentGroupStatus(@PathVariable String paymentGroupId)`: Cho phép client polling trạng thái thanh toán của nhóm đơn hàng.

### Lớp `AdminRefundController`
- **Đường dẫn:** `@RequestMapping("/api/v1/admin/refunds")`
- **Annotation:** `@PreAuthorize("hasRole('ADMIN')")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<RefundResponse>> processRefund(@Valid @RequestBody AdminRefundRequest request, @CurrentUserId Long adminUserId)`: Thực hiện lệnh hoàn tiền qua cổng VNPay.
  2. `public ResponseEntity<ApiResponse<List<RefundAttemptResponse>>> getRefundHistoryByOrder(@PathVariable Long orderId)`: Tra cứu lịch sử hoàn tiền của một đơn hàng.

### Lớp `PaymentGatewayService`
- **Annotation:** `@Service`, `@RequiredArgsConstructor`
- **Các hàm thành viên:**
  1. `@Transactional public String generatePaymentUrl(PaymentOrderData orderData, String clientIp)`
     - *Logic:* Lấy chiến lược xử lý thanh toán từ `PaymentStrategyFactory.getStrategy(orderData.method())`. Sinh mã giao dịch duy nhất `txnRef = "PAY_" + UUID`. Khởi tạo bản ghi `Payment (status = PENDING)` lưu vào `PaymentRepository`. Gọi `strategy.createPaymentUrl(...)` để tạo URL có chữ ký bảo mật HMAC-SHA512.
  2. `@Transactional public void processVnPayIpn(Map<String, String> params)`
     - *Logic (IPN Webhook Callback):*
       + Gọi `strategy.verifyIpnSignature(params)` kiểm tra tính toàn vẹn chữ ký HMAC. Nếu giả mạo $\rightarrow$ ném lỗi ngay.
       + Khóa bản ghi thanh toán: `paymentRepository.findByTransactionIdForUpdate(txnRef)`.
       + Kiểm tra tính Idempotent: Nếu `payment.getStatus() != PENDING` (đã xử lý trước đó) $\rightarrow$ bỏ qua, trả về `RspCode: 00`.
       + Đối soát số tiền thanh toán: So sánh `vnp_Amount` với `payment.getAmount() * 100`. Nếu không khớp $\rightarrow$ ném lỗi gian lận giá.
       + Nếu `vnp_ResponseCode == "00"` (Thành công): Cập nhật `payment.status = SUCCESS`, gọi `orderInternalService.confirmPendingOrdersByGroup(paymentGroupId)` để xác nhận tất cả đơn hàng trong nhóm.
       + Nếu `vnp_ResponseCode != "00"` (Thất bại/Hủy): Cập nhật `payment.status = FAILED`.
       + Lưu thông tin `gatewayTransactionId`, `gatewayBankCode`, `rawData` vào bản ghi thanh toán.
  3. `@Transactional(readOnly = true) public PaymentGroupStatusResponse getPaymentGroupStatus(String paymentGroupId)`: Kiểm tra trạng thái các payment trong nhóm (`SUCCESS`, `PENDING`, `FAILED`).

### Lớp `RefundService`
- **Chức năng:** Thực hiện quy trình hoàn tiền tự động qua VNPay.
- **Hàm thành viên:**
  1. `@Transactional public RefundResponse processRefund(Long orderId, String reason, Long actorUserId)`
     - *Logic:* Lấy snapshot đơn hàng từ `orderInternalService.getRefundableOrderSnapshot(orderId)`. Kiểm tra đơn đã từng thanh toán online thành công chưa. Dùng `RefundReservationService` tạo bản ghi `RefundAttempt (status = PENDING)` với `idempotencyKey` duy nhất. Gọi cổng thanh toán `VnPayRefundGateway.executeRefund(...)`. Nếu cổng trả về `00` $\rightarrow$ cập nhật `RefundAttempt (SUCCESS)`, gọi `orderInternalService.recordRefundSuccess(orderId)` để đồng bộ trạng thái đơn hàng.

---

## 3.7. Module Voucher

### Lớp `VoucherController` & `AdminVoucherController`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<List<VoucherResponse>>> getAvailableVouchers()`: Danh sách voucher khả dụng cho khách hàng.
  2. `public ResponseEntity<ApiResponse<List<VoucherResponse>>> getShopVouchers(@PathVariable Long shopId)`: Danh sách voucher riêng của một gian hàng.
  3. `public ResponseEntity<ApiResponse<VoucherResponse>> createShopVoucher(@CurrentUserId Long userId, ...)`: Vendor tạo voucher mới cho shop mình.
  4. `public ResponseEntity<ApiResponse<VoucherResponse>> createSystemVoucher(@Valid @RequestBody VoucherCreateRequest request)`: Admin tạo voucher toàn sàn (`SYSTEM`).

### Lớp `VoucherInternalServiceImpl` (implements `VoucherInternalService`)
- **Chức năng:** Hợp đồng dịch vụ nội bộ cho Module Order để tính toán và trừ lượt dùng khuyến mãi.
- **Các hàm thành viên:**
  1. `@Transactional public VoucherDiscountResult calculateDiscount(VoucherCalculationRequest request)`
     - *Logic:* Khóa bản ghi voucher theo code `voucherRepository.findByCodeForUpdate(code)`. Kiểm tra thời gian hiệu lực (`startDate` $\le$ now $\le$ `endDate`). Kiểm tra lượt dùng (`usedCount < usageLimit`). Nếu là voucher của Shop $\rightarrow$ chỉ áp dụng cho giá trị đơn hàng của Shop đó; nếu là voucher toàn sàn $\rightarrow$ áp dụng trên tổng đơn. Tính toán số tiền giảm (tôn trọng giới hạn `maxDiscount` nếu là loại giảm theo %). Trả về `VoucherDiscountResult` phân bổ số tiền giảm cho từng shop.
  2. `@Transactional public boolean incrementUsage(Long voucherId)`: Khóa voucher, kiểm tra lại giới hạn lượt dùng và tăng `used_count = used_count + 1`.

---

## 3.8. Module Review

### Lớp `ReviewController` & `VendorReviewController`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<ReviewResponse>> createReview(@CurrentUserId Long userId, @Valid @RequestBody ReviewCreateRequest request)`: Khách hàng gửi bài đánh giá sản phẩm.
  2. `public ResponseEntity<ApiResponse<Page<ReviewResponse>>> getProductReviews(@PathVariable Long productId, ...)`: Xem danh sách đánh giá của 1 sản phẩm.
  3. `public ResponseEntity<ApiResponse<RatingSummaryResponse>> getRatingSummary(@PathVariable Long productId)`: Xem điểm sao trung bình và số lượng từng loại sao (1-5 sao).
  4. `public ResponseEntity<ApiResponse<ReviewResponse>> replyReview(@CurrentUserId Long userId, @PathVariable Long reviewId, @Valid @RequestBody VendorReplyRequest request)`: Chủ shop gửi phản hồi tới bài đánh giá của khách.

### Lớp `ReviewServiceImpl` (implements `ReviewService`)
- **Các hàm thành viên:**
  1. `@Transactional public ReviewResponse createReview(Long userId, ReviewCreateRequest request)`
     - *Logic:*
       + Gọi `orderInternalService.isOrderDeliveredAndBelongsToUser(orderId, userId)`: Đảm bảo đơn hàng đã ở trạng thái `DELIVERED` và thuộc về khách hàng này.
       + Gọi `orderInternalService.getProductVariantIdsByOrderId(orderId)` kết hợp `productInternalService.getProductIdByVariantId`: Đảm bảo sản phẩm được đánh giá thực sự nằm trong đơn hàng đó.
       + Kiểm tra `reviewRepository.existsByUserIdAndOrderIdAndProductId`: Chống khách hàng đánh giá trùng lặp nhiều lần cho cùng 1 đơn.
       + Khởi tạo entity `Review (rating, comment, imageUrls)`, lưu vào `reviewRepository`.
       + Bắn sự kiện `ReviewCreatedEvent` để thông báo cho Chủ shop.
  2. `@Transactional public ReviewResponse replyReview(Long userId, Long reviewId, VendorReplyRequest request)`
     - *Logic:* Tìm review theo ID, lấy `productId` và gọi `productInternalService.getShopIdByProductId` để xác minh người dùng hiện tại có đúng là chủ shop bán sản phẩm này không. Cập nhật `vendorReply` và `vendorRepliedAt = now()`.

---

## 3.9. Module Notification

### Lớp `NotificationController`
- **Đường dẫn:** `@RequestMapping("/api/v1/notifications")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getMyNotifications(@CurrentUserId Long userId, ...)`: Lấy danh sách thông báo phân trang của người dùng.
  2. `public ResponseEntity<ApiResponse<Long>> getUnreadCount(@CurrentUserId Long userId)`: Đếm số lượng thông báo chưa đọc (hiển thị Badge chuông trên Header).
  3. `public ResponseEntity<ApiResponse<Void>> markAsRead(@CurrentUserId Long userId, @PathVariable Long id)`: Đánh dấu 1 thông báo đã đọc.
  4. `public ResponseEntity<ApiResponse<Void>> markAllAsRead(@CurrentUserId Long userId)`: Đánh dấu tất cả thông báo của tôi là đã đọc.

### Lớp `OrderNotificationListener` & `ReviewNotificationListener`
- **Chức năng:** Lắng nghe sự kiện Domain Event và tạo thông báo tự động.
- **Các hàm thành viên:**
  1. `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void onOrderConfirmed(OrderConfirmedEvent event)`
     - *Chức năng:* Khi đơn hàng được xác nhận thanh toán/đặt hàng, tạo 2 thông báo: 1 gửi cho Buyer ("Đơn hàng #ID đã được xác nhận") và 1 gửi cho Vendor ("Bạn có đơn hàng mới #ID").
  2. `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void onOrderShipped(OrderShippedEvent event)`
     - *Chức năng:* Khi shop giao hàng cho vận chuyển, tạo thông báo gửi cho Buyer ("Đơn hàng #ID của bạn đang trên đường giao").
  3. `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT) public void onReviewCreated(ReviewCreatedEvent event)`
     - *Chức năng:* Khi có đánh giá mới, tạo thông báo gửi tới Chủ shop ("Sản phẩm của bạn vừa nhận được đánh giá X sao").

---

## 3.10. Module Chat (WebSocket STOMP)

### Lớp `ChatRoomController`
- **Đường dẫn:** `@RequestMapping("/api/v1/chat")`
- **Các hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<ChatRoomResponse>> createOrGetRoom(@CurrentUserId Long buyerId, @Valid @RequestBody CreateChatRoomRequest request)`: Mở hoặc tìm phòng chat giữa khách và shop.
  2. `public ResponseEntity<ApiResponse<List<ChatRoomResponse>>> getMyRooms(@CurrentUserId Long userId)`: Lấy danh sách các cuộc hội thoại của tôi.
  3. `public ResponseEntity<ApiResponse<Page<ChatMessageResponse>>> getMessages(@PathVariable Long roomId, @CurrentUserId Long userId, ...)`: Lấy lịch sử tin nhắn trong phòng chat.
  4. `public ResponseEntity<ApiResponse<Void>> markAsRead(@PathVariable Long roomId, @CurrentUserId Long userId)`: Đánh dấu đã đọc tin nhắn trong phòng.

### Lớp `ChatMessageController` (STOMP Controller)
- **Annotation:** `@Controller`
- **Hàm thành viên:**
  1. `@MessageMapping("/chat.send") public void sendMessage(@Validated ChatMessageRequest request, Principal principal)`
     - *Chức năng:* Nhận frame STOMP từ client gửi tới đích `/app/chat.send`.
     - *Logic:* Trích xuất `senderId = Long.parseLong(principal.getName())`, chuyển tiếp xử lý tới `chatService.sendMessage(request, senderId)`.

### Lớp `ChatServiceImpl` (implements `ChatService`)
- **Các hàm thành viên:**
  1. `@Transactional public void sendMessage(ChatMessageRequest request, Long senderId)`
     - *Logic:*
       + Tìm phòng chat theo ID, kiểm tra người gửi có quyền tham gia phòng không.
       + Khởi tạo `ChatMessage (roomId, senderId, content)`, lưu vào `chatMessageRepository`.
       + Cập nhật mốc thời gian `chatRoom.updatedAt = now()`.
       + Xác định người nhận (`recipientId`): nếu người gửi là Buyer thì người nhận là chủ shop (truy vấn qua `shopInternalService.getUserIdByShopId`); nếu người gửi là Vendor thì người nhận là Buyer.
       + Chuyển đổi thành `ChatMessageResponse`.
       + Gọi `messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/messages", response)` để đẩy tin nhắn thời gian thực cho người nhận.
       + Gọi `messagingTemplate.convertAndSendToUser(senderId.toString(), "/queue/messages", response)` để phản hồi xác nhận tin nhắn cho người gửi (hỗ trợ đồng bộ đa thiết bị).

### Lớp `JwtChannelInterceptor`
- **Chức năng:** Chặn frame kết nối WebSocket ban đầu (`StompHeaderAccessor.getCommand() == StompCommand.CONNECT`), trích xuất token từ header `Authorization: Bearer <token>`, giải mã userId và thiết lập `accessor.setUser(new StompPrincipal(userId))`.

---

## 3.11. Module Media (Cloudinary)

### Lớp `MediaController`
- **Đường dẫn:** `@RequestMapping("/api/v1/media")`
- **Hàm thành viên:**
  1. `@PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE) public ResponseEntity<ApiResponse<UploadResponse>> uploadImage(@RequestParam("file") MultipartFile file)`
     - *Chức năng:* Nhận file ảnh từ client, kiểm tra định dạng (`image/jpeg`, `image/png`), gọi `fileStorageService.uploadImage(file)` để đẩy trực tiếp lên Cloudinary CDN và trả về HTTP 200 kèm `{ imageUrl, publicId }`.

---

## 3.12. Module Statistics & Analytics

### Lớp `VendorAnalyticsController`
- **Đường dẫn:** `@RequestMapping("/api/v1/vendor/analytics")`
- **Hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<VendorAnalyticsResponse>> getAnalytics(@CurrentUserId Long userId, @RequestParam String period, ...)`: Cung cấp số liệu doanh thu, số đơn thành công, giá trị trung bình đơn hàng và danh sách sản phẩm bán chạy cho Vendor.

### Lớp `AdminStatisticsController`
- **Đường dẫn:** `@RequestMapping("/api/v1/admin/statistics")`
- **Hàm thành viên:**
  1. `public ResponseEntity<ApiResponse<AdminDashboardStatsResponse>> getDashboardStats(@RequestParam(required = false) String startDate, ...)`: Cung cấp báo cáo toàn sàn cho Admin (Tổng GMV, biểu đồ doanh thu theo ngày, số lượng shop mới, số lượng user).

---

# 4. ĐẶC TẢ CÁC HÀM API CLIENT VÀ CẤU TRÚC GIAO DIỆN FRONTEND

## 4.1. Hệ thống API Services của Frontend (`frontend/src/api/`)

Toàn bộ việc gửi yêu cầu mạng từ React Client lên Backend được module hóa thành các tệp tin chuyên biệt tại thư mục `frontend/src/api/`:

| Tệp API Client | Hàm Thành Viên Chính | Endpoint Backend Tương Ứng | Mô Tả Chức Năng |
|---|---|---|---|
| `authApi.ts` | `login(data)`<br/>`register(data)` | `POST /api/v1/auth/login`<br/>`POST /api/v1/auth/register` | Xác thực người dùng, nhận JWT lưu vào LocalStorage |
| `productApi.ts` | `getAllProducts(params)`<br/>`getProductById(id)`<br/>`getRelatedProducts(id)`<br/>`getRecommendedProducts(limit)` | `GET /api/v1/products`<br/>`GET /api/v1/products/{id}`<br/>`GET /api/v1/products/{id}/related`<br/>`GET /api/v1/products/recommendations` | Tìm kiếm sản phẩm, xem chi tiết, lấy sản phẩm liên quan và gợi ý |
| `vendorApi.ts` | `getVendorProducts(params)`<br/>`createProduct(data)`<br/>`updateProduct(id, data)`<br/>`deleteProduct(id)` | `GET /api/v1/vendor/products`<br/>`POST /api/v1/vendor/products`<br/>`PUT /api/v1/vendor/products/{id}`<br/>`DELETE /api/v1/vendor/products/{id}` | Quản lý sản phẩm toàn diện cho Vendor |
| `cartApi.ts` | `getCart()`<br/>`addToCart(data)`<br/>`updateCartItem(id, data)`<br/>`removeCartItem(id)`<br/>`clearCart()` | `GET /api/v1/carts`<br/>`POST /api/v1/carts/items`<br/>`PUT /api/v1/carts/items/{id}`<br/>`DELETE /api/v1/carts/items/{id}`<br/>`DELETE /api/v1/carts` | Quản trị giỏ hàng realtime |
| `orderApi.ts` | `checkout(data)`<br/>`previewCheckout(data)`<br/>`getMyOrders(params)`<br/>`getOrderDetail(id)`<br/>`cancelOrder(id)`<br/>`confirmDelivery(id)` | `POST /api/v1/orders/checkout`<br/>`POST /api/v1/orders/preview`<br/>`GET /api/v1/orders`<br/>`GET /api/v1/orders/{id}`<br/>`PATCH /api/v1/orders/{id}/cancel`<br/>`PATCH /api/v1/orders/{id}/confirm-delivery` | Đặt hàng, xem trước giá, lịch sử mua hàng, hủy đơn, xác nhận đã nhận hàng |
| `paymentApi.ts` | `createPaymentUrl(data)`<br/>`getPaymentGroupStatus(groupId)` | `POST /api/v1/payments/create`<br/>`GET /api/v1/payments/group/{id}/status` | Khởi tạo link thanh toán VNPay và kiểm tra kết quả giao dịch |
| `reviewApi.ts` | `createReview(data)`<br/>`getProductReviews(productId)`<br/>`getRatingSummary(productId)`<br/>`replyReview(reviewId, data)` | `POST /api/v1/reviews`<br/>`GET /api/v1/products/{id}/reviews`<br/>`GET /api/v1/products/{id}/reviews/summary`<br/>`POST /api/v1/vendor/reviews/{id}/reply` | Đánh giá sao, nhận xét ảnh và chủ shop phản hồi |
| `chatApi.ts` | `createOrGetRoom(data)`<br/>`getMyRooms()`<br/>`getMessages(roomId)`<br/>`markAsRead(roomId)` | `POST /api/v1/chat/rooms`<br/>`GET /api/v1/chat/rooms`<br/>`GET /api/v1/chat/rooms/{id}/messages`<br/>`POST /api/v1/chat/rooms/{id}/read` | Quản lý danh sách hội thoại và tin nhắn lịch sử |
| `notificationApi.ts`| `getMyNotifications(params)`<br/>`getUnreadCount()`<br/>`markAsRead(id)`<br/>`markAllAsRead()` | `GET /api/v1/notifications`<br/>`GET /api/v1/notifications/unread-count`<br/>`PATCH /api/v1/notifications/{id}/read`<br/>`PATCH /api/v1/notifications/read-all` | Tải danh sách thông báo và cập nhật trạng thái đọc |
| `mediaApi.ts` | `uploadImage(formData)` | `POST /api/v1/media/upload` | Tải ảnh lên Cloudinary độc lập |
| `shopApi.ts` | `registerShop(data)`<br/>`getMyShop()`<br/>`getShopById(id)` | `POST /api/v1/shops`<br/>`GET /api/v1/shops/my-shop`<br/>`GET /api/v1/shops/{id}` | Đăng ký mở shop và xem trang cửa hàng |
| `adminApi.ts` | `getShops(status)`<br/>`approveShop(id)`<br/>`banShop(id)`<br/>`unbanShop(id)`<br/>`getAllOrders(params)`<br/>`updateOrderStatus(id, data)`<br/>`getDashboardStats(params)` | `GET /api/v1/admin/shops`<br/>`PATCH /api/v1/admin/shops/{id}/approve`<br/>`PATCH /api/v1/admin/shops/{id}/ban`<br/>`PATCH /api/v1/admin/shops/{id}/unban`<br/>`GET /api/v1/admin/orders`<br/>`PATCH /api/v1/admin/orders/{id}/status`<br/>`GET /api/v1/admin/statistics/dashboard` | Toàn bộ chức năng quản trị hệ thống cho Admin |

---

## 4.2. Cấu trúc Các Màn hình Chức năng (`frontend/src/pages/`)

### Màn hình Khách hàng & Công khai (`/pages/public`):
1. **`ProductListPage.tsx` (Trang chủ & Danh mục):** Hiển thị danh sách sản phẩm dạng lưới, thanh tìm kiếm từ khóa kèm debounce, thanh lọc danh mục, bộ lọc khoảng giá và các tùy chọn sắp xếp.
2. **`ProductDetailPage.tsx` (Chi tiết sản phẩm):** Bộ sưu tập ảnh (Thumbnail Gallery), lựa chọn biến thể sản phẩm động (Color, Size) với kiểm tra tồn kho realtime, nút "Thêm vào giỏ" và "Mua ngay", danh sách đánh giá từ khách hàng kèm form thống kê sao, và nút bấm mở cửa sổ chat trực tiếp với người bán.
3. **`CartPage.tsx` (Giỏ hàng):** Hiển thị danh sách sản phẩm theo từng Shop, checkbox chọn mua từng món hàng, nút tăng giảm số lượng có kiểm tra tồn kho khả dụng, nút xóa mục và bảng tổng hợp tiền hàng.
4. **`CheckoutPage.tsx` (Thanh toán):** Nhập địa chỉ giao hàng, lựa chọn phương thức thanh toán (`VNPAY` hoặc `COD`), nhập mã voucher và gọi API `previewCheckout` để hiển thị tức thời số tiền được giảm giá theo từng shop, nút "Xác nhận đặt hàng".
5. **`PaymentResultPage.tsx` (Kết quả thanh toán):** Hiển thị sau khi được VNPay redirect về, thực hiện gọi API polling trạng thái của `paymentGroupId` để thông báo cho khách hàng đặt hàng thành công hoặc thất bại.
6. **`OrderHistoryPage.tsx` (Lịch sử đơn hàng):** Tab lọc trạng thái đơn (`Tất cả`, `Chờ xử lý`, `Đã xác nhận`, `Đang giao`, `Đã nhận`, `Đã hủy`), nút "Hủy đơn" (nếu PENDING), nút "Đã nhận hàng" (nếu SHIPPED), và nút mở Modal "Đánh giá sản phẩm" (nếu DELIVERED).
7. **`OrderDetailPage.tsx` (Chi tiết đơn hàng):** Tiến trình vòng đời đơn hàng (Timeline), địa chỉ nhận hàng, thông tin thanh toán, danh sách sản phẩm chi tiết kèm ảnh snapshot và nút mở Modal đánh giá.
8. **`ProfilePage.tsx` (Hồ sơ người dùng):** Xem và chỉnh sửa thông tin cá nhân (Họ tên, số điện thoại, email, vai trò).
9. **`ShopProfilePage.tsx` (Gian hàng):** Trang công khai của một Shop cụ thể, hiển thị thông tin gian hàng và các sản phẩm do Shop đó cung cấp.
10. **`ChatTestPage.tsx` (Trò chuyện Realtime):** Cửa sổ chat danh sách hội thoại bên trái, khung tin nhắn realtime bên phải, kết nối trực tiếp với STOMP WebSocket.

### Màn hình Dành cho Người bán (`/pages/vendor`):
1. **`VendorProductListPage.tsx`:** Quản lý kho hàng, tìm kiếm sản phẩm theo tên, xem số lượng tồn kho từng biến thể, nút chuyển nhanh tới màn hình sửa hoặc ẩn sản phẩm.
2. **`VendorProductFormPage.tsx`:** Form tạo mới/chỉnh sửa sản phẩm, tích hợp upload nhiều ảnh lên Cloudinary, tạo biến thể động dạng bảng với các thuộc tính linh hoạt.
3. **`VendorOrderListPage.tsx`:** Bộ lọc đơn hàng của shop, nút bấm cập nhật trạng thái đơn hàng (Xác nhận đơn $\rightarrow$ Giao hàng cho vận chuyển $\rightarrow$ Hủy đơn).
4. **`VendorVoucherListPage.tsx`:** Tạo mã giảm giá riêng của Shop (loại %, số tiền cố định, thời hạn áp dụng).
5. **`VendorAnalyticsPage.tsx`:** Biểu đồ doanh thu theo thời gian thực, thống kê số lượng đơn thành công và danh sách sản phẩm bán chạy nhất.
6. **`VendorShopRegistrationPage.tsx` & `ShopPendingPage.tsx`:** Form gửi hồ sơ mở shop và màn hình chờ Admin phê duyệt.

### Màn hình Dành cho Quản trị viên (`/pages/admin`):
1. **`AdminDashboardPage.tsx`:** Bảng số liệu tổng quan (Tổng doanh thu GMV, tổng số đơn, tổng số shop, người dùng mới) và biểu đồ phân tích.
2. **`AdminShopListPage.tsx`:** Danh sách shop chờ duyệt (kèm nút Phê duyệt) và danh sách shop đang hoạt động (kèm nút Khóa Shop / Mở khóa Shop).
3. **`AdminOrderListPage.tsx` & `AdminOrderDetailPage.tsx`:** Giám sát toàn bộ đơn hàng của toàn sàn, can thiệp đổi trạng thái khi có khiếu nại, kích hoạt lệnh hoàn tiền tự động qua cổng VNPay.
4. **`AdminVoucherListPage.tsx`:** Tạo mã giảm giá toàn sàn (`SYSTEM`) áp dụng cho toàn bộ các gian hàng.

---

# 5. CƠ CHẾ BẢO MẬT, XỬ LÝ BẤT ĐỒNG BỘ & GIAO DỊCH TOÀN HỆ THỐNG

1. **Bảo mật Xác thực & Phân quyền (Spring Security + Stateless JWT):**
   - Mật khẩu người dùng được băm một chiều an toàn bằng **BCryptPasswordEncoder**.
   - Phiên làm việc hoàn toàn không lưu trạng thái (Stateless Session) thông qua **JWT Access Token (HMAC-SHA256)** có thời hạn 24 giờ.
   - Phân quyền theo vai trò chặt chẽ tại tầng Controller bằng annotation `@PreAuthorize("hasRole('ADMIN')")`, `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`.
   - Annotation tự chế `@CurrentUserId` tự động trích xuất mã định danh người dùng từ token mà không cần parse thủ công ở từng controller.
2. **Cơ chế Khóa Cơ sở Dữ liệu & Xử lý Tranh chấp Đồng thời (Concurrency Control):**
   - **Pessimistic Write Lock (`SELECT ... FOR UPDATE`):** Được áp dụng triệt để tại các vị trí nhạy cảm về tài chính và tồn kho:
     + Trừ tồn kho biến thể khi Checkout (`deductStock`).
     + Khóa giỏ hàng khi thêm hàng (`findByUserIdForUpdate`).
     + Khóa bản ghi thanh toán khi nhận IPN Webhook (`findByTransactionIdForUpdate`).
     + Khóa voucher khi tính toán chiết khấu và tăng lượt sử dụng.
   - **Tối ưu N+1 Query:** Toàn bộ các truy vấn danh sách sản phẩm, đơn hàng đều sử dụng `@EntityGraph` hoặc `@BatchSize(size = 50)` trên các quan hệ `@OneToMany`, đảm bảo tốc độ phản hồi API luôn dưới 50ms.
3. **Kiến trúc Sự kiện Miền Bất đồng bộ (Domain Events):**
   - Khi có các thay đổi quan trọng (Đơn hàng xác nhận, Đơn hàng xuất kho, Đánh giá mới), hệ thống bắn `ApplicationEvent` qua `ApplicationEventPublisher`.
   - Các Listener xử lý với `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` đảm bảo thông báo chỉ được tạo khi giao dịch database chính đã commit thành công 100%, cô lập hoàn toàn lỗi của tầng thông báo khỏi luồng đặt hàng.
4. **Nhắn tin Thời gian thực (STOMP over WebSocket):**
   - Kết nối hai chiều bảo mật sử dụng JWT handshake thông qua `JwtChannelInterceptor`.
   - Định tuyến tin nhắn đích danh từng người dùng qua `SimpMessagingTemplate.convertAndSendToUser`, loại bỏ nguy cơ lộ lọt tin nhắn giữa các khách hàng khác nhau.
