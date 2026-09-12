# REPORT_WRITING_PLAN.md
# Kế Hoạch Viết Báo Cáo — Enterprise Ecommerce

---

## MỞ ĐẦU
**Nội dung cần:** Lý do chọn đề tài, tổng quan đề tài, bố cục báo cáo (1 trang)
**Nguồn:** PROJECT_CONTEXT.md → Section 1 (Project Overview)
**Có thể viết ngay:** ✅

---

## CHƯƠNG 1 — TỔNG QUAN ĐỀ TÀI

### 1.1 Giới thiệu đề tài
**Nội dung:** Giới thiệu hệ thống Enterprise Ecommerce — nền tảng TMĐT đa người bán
**Nguồn:**
- PROJECT_CONTEXT.md → "Project Overview"
- ARCHITECTURE.md → "Project Architecture"
**Có thể viết ngay:** ✅

### 1.2 Lý do chọn đề tài
**Nội dung:** Xu hướng TMĐT, nhu cầu hệ thống Multi-vendor, cơ hội học kiến thức enterprise
**Nguồn:** PROJECT_CONTEXT.md → Section 2 (Learning Goals)
**Có thể viết ngay:** ✅

### 1.3 Phát biểu bài toán
**Nội dung:**
- Người mua cần: browse sản phẩm → giỏ hàng → thanh toán → theo dõi đơn
- Người bán cần: mở shop → đăng sản phẩm → quản lý đơn hàng
- Admin cần: duyệt shop → quản trị hệ thống
- Hệ thống cần: multi-vendor, payment gateway, realtime features
**Nguồn:** PROJECT_CONTEXT.md → Section 7 (Core Business Flow)
**Có thể viết ngay:** ✅

### 1.4 Mục tiêu đề tài
**Nội dung:**
- Xây dựng nền tảng TMĐT đa người bán hoàn chỉnh
- Áp dụng kiến trúc Modular Monolith
- Tích hợp cổng thanh toán VNPay
- Xây dựng realtime chat
- Hệ thống voucher
**Nguồn:** PROJECT_CONTEXT.md → Section 6 (Development Priority)
**Có thể viết ngay:** ✅

### 1.5 Phạm vi đề tài
**Nội dung:**
- **Trong phạm vi:** Auth, Product, Cart, Order, Payment (VNPay+COD), Review, Voucher, Notification, Realtime Chat, Media (Cloudinary), Admin/Vendor Portal
- **Ngoài phạm vi:** Redis cache, Docker deployment, AWS hosting, Mobile app
**Nguồn:** REPORT_PROJECT_ANALYSIS.md → Sections 18-19
**Có thể viết ngay:** ✅

### 1.6 Phương pháp thực hiện đề tài
**Nội dung:**
- Phát triển lặp (incremental) theo phase
- Phân tích bài toán → thiết kế module → implement → test → checkpoint
- AI-assisted development (mentor + implementation)
**Nguồn:** PROJECT_CONTEXT.md → Section 9 (Development Rules)
**Có thể viết ngay:** ✅

### 1.7 Khảo sát hệ thống liên quan
**Nội dung:** So sánh với Shopee, Tiki, WooCommerce (multi-vendor)
**Nguồn:** Cần viết mới (tham khảo thực tế + học thuật)
**Có thể viết ngay:** ✅ (nội dung tổng quát)

### 1.8 Bố cục báo cáo
**Nội dung:** Tóm tắt nội dung từng chương
**Có thể viết ngay:** ✅ (viết cuối cùng sau khi hoàn thành các chương)

---

## CHƯƠNG 2 — CƠ SỞ LÝ THUYẾT

### 2.1 Các khái niệm nền tảng
**Nội dung cần viết:**
- Ecommerce, Multi-vendor Marketplace
- REST API (HTTP methods, status codes)
- MVC Pattern
- ORM (Object-Relational Mapping)
- JWT (JSON Web Token)
- WebSocket và STOMP Protocol
- Event-Driven Architecture
- Modular Monolith
**Nguồn:** Kiến thức lý thuyết + minh họa từ source code
**Có thể viết ngay:** ✅

### 2.2 Các phương pháp, mô hình, thuật toán
**Nội dung cần viết:**
- State Machine cho Order Status
- Strategy Pattern (Payment)
- Observer/Event Pattern (Notification)
- Pessimistic Locking (concurrency)
- Proportional Split Algorithm (voucher)
**Nguồn:**
- `OrderStatus.java` (enum PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED)
- `PaymentStrategy.java`, `VnPayStrategy.java`, `CodStrategy.java`
- `OrderNotificationListener.java`, `ReviewNotificationListener.java`
- `PaymentRepository.java` (PESSIMISTIC_WRITE)
- `VoucherInternalServiceImpl.java` (proportional split)
**Có thể viết ngay:** ✅

### 2.3 Công nghệ sử dụng

#### 2.3.1 Spring Boot (Backend Framework)
- Giới thiệu: Framework Java enterprise
- Đặc điểm: Auto-configuration, embedded server, starter POMs
- Vai trò: Framework chính của hệ thống
- Lý do chọn: Mature ecosystem, Spring Security, Spring Data JPA
- **Nguồn:** `pom.xml` (Spring Boot 4.1.0)

#### 2.3.2 React + TypeScript + Vite (Frontend)
- Giới thiệu: UI library + type-safe JS + build tool
- Đặc điểm: Component-based, SPA
- Vai trò: Giao diện người dùng
- Lý do chọn: Phổ biến, TypeScript tăng type safety
- **Nguồn:** `package.json` (React 19, TypeScript 6, Vite 8)

#### 2.3.3 MySQL (Database)
- Giới thiệu: RDBMS
- Vai trò: Cơ sở dữ liệu chính
- Lý do chọn: Quan hệ phức tạp giữa các entity, ACID
- **Nguồn:** `application.yml`

#### 2.3.4 JWT (Authentication)
- Giới thiệu: JSON Web Token
- Đặc điểm: Stateless, self-contained
- Vai trò: Authentication token
- **Nguồn:** `pom.xml` (jjwt 0.12.5), `JwtService.java`

#### 2.3.5 Spring Security
- Giới thiệu: Security framework
- Đặc điểm: Filter chain, role-based access
- Vai trò: Authentication & Authorization
- **Nguồn:** `SecurityConfig.java`, `JwtAuthenticationFilter.java`

#### 2.3.6 WebSocket + STOMP
- Giới thiệu: Full-duplex communication
- Đặc điểm: Realtime, bidirectional
- Vai trò: Realtime chat
- **Nguồn:** `pom.xml` (spring-boot-starter-websocket), `WebSocketConfig.java`

#### 2.3.7 Cloudinary
- Giới thiệu: Cloud image management
- Đặc điểm: CDN, transform, storage
- Vai trò: Lưu trữ ảnh sản phẩm
- **Nguồn:** `pom.xml` (cloudinary-http44 1.36.0), `CloudinaryStorageServiceImpl.java`

#### 2.3.8 VNPay
- Giới thiệu: Cổng thanh toán Việt Nam
- Đặc điểm: Redirect payment, IPN webhook
- Vai trò: Tích hợp thanh toán trực tuyến
- **Nguồn:** `VnPayStrategy.java`, `application.yml`

#### 2.3.9 Tailwind CSS
- Giới thiệu: Utility-first CSS framework
- Vai trò: Styling Frontend
- **Nguồn:** `package.json`, `tailwind.config.js`

### 2.4 Các nghiên cứu/hệ thống liên quan
**Nội dung:** So sánh kiến trúc với Monolith / Microservices / Modular Monolith
**Nguồn:** ARCHITECTURE.md, học thuật
**Có thể viết ngay:** ✅

---

## CHƯƠNG 3 — HỆ THỐNG

### 3.1 Giới thiệu hệ thống
**Nội dung:**
- Tên: Enterprise Ecommerce
- Loại: Multi-Vendor Marketplace
- Actors: Buyer, Vendor, Admin
- Core flow: Browse → Cart → Checkout → Payment → Order
**Nguồn:** PROJECT_CONTEXT.md, REPORT_PROJECT_ANALYSIS.md
**Có thể viết ngay:** ✅

### 3.2 Kiến trúc hệ thống

#### 3.2.1 Kiến trúc tổng thể
**Nội dung:** Modular Monolith, 1 Spring Boot app
**Diagram cần:** Architecture diagram (Client → Spring Boot → MySQL + External Services)
**Nguồn:** ARCHITECTURE.md, `REPORT_PROJECT_ANALYSIS.md` → Section 3
**Có thể viết ngay:** ✅ (cần DIAGRAM)

#### 3.2.2 Các thành phần
**Nội dung:** 13 modules (identity, shop, product, cart, order, payment, review, voucher, notification, chat, media, common, infrastructure)
**Nguồn:** `REPORT_PROJECT_ANALYSIS.md` → Section 4
**Có thể viết ngay:** ✅

#### 3.2.3 Luồng xử lý tổng quát
**Nội dung:** Request → Security Filter → Controller → Service → Repository → MySQL
**Diagram cần:** Sequence diagram (HTTP request flow)
**Nguồn:** ARCHITECTURE.md → Section 9 (Layering Inside a Module)
**Có thể viết ngay:** ✅ (cần DIAGRAM)

### 3.3 Phân tích Use Case

#### 3.3.1 Sơ đồ Use Case
**Actors:** Buyer (User), Vendor, Admin, System
**Nguồn:** `FEATURE_INVENTORY.md` → tất cả features
**Cần:** Use Case Diagram
**Phân nhóm:**
- Buyer UC: đăng ký, đăng nhập, duyệt SP, giỏ hàng, checkout, thanh toán, đánh giá, chat, thông báo
- Vendor UC: tạo shop, quản lý SP, quản lý đơn, tạo voucher, chat
- Admin UC: duyệt shop, quản lý SP, xem đơn, tạo voucher system

#### 3.3.2 Đặc tả Use Case
**Nội dung:** Đặc tả chi tiết 5-7 use case quan trọng nhất:
1. Đặt hàng (Checkout Multi-vendor)
2. Thanh toán VNPay + IPN
3. Cập nhật trạng thái đơn hàng (Vendor)
4. Tạo đánh giá sản phẩm
5. Nhắn tin realtime
6. Duyệt gian hàng (Admin)
**Nguồn:** `FEATURE_INVENTORY.md` (F-C11, F-C12, F-V09, F-C17, F-C20, F-A02)

### 3.4 Mô hình hóa hệ thống

#### 3.4.1 Sơ đồ lớp (Class Diagram)
**Cần vẽ:**
- Entity classes (User, Shop, Product, ProductVariant, Cart, CartItem, Order, OrderItem, Payment, Review, Voucher, Notification, ChatRoom, ChatMessage)
- Relationships (1-N, M-N, composition)
**Nguồn:** Tất cả Entity files trong `src/main/java/com/tmt/ecommerce/*/entity/`
**Lưu ý:** Loose coupling pattern (chỉ lưu ID, không @ManyToOne cross-module)

#### 3.4.2 Sơ đồ tuần tự (Sequence Diagram)
**Cần vẽ ít nhất 3 flow quan trọng:**

**Flow 1: Multi-vendor Checkout + Payment VNPay**
```
Browser → CheckoutPage → OrderController.checkout()
       → OrderServiceImpl → CartInternalService (lấy cart)
       → ProductInternalService (kiểm tra tồn kho)
       → VoucherInternalService (tính giảm giá)
       → OrderRepository (tạo orders)
       → PaymentController.createPaymentUrl()
       → VnPayStrategy.generateUrl()
       → Redirect VNPay
       → VNPay IPN → PaymentController.vnpayIpn()
       → PaymentGatewayService.processVnPayIpn()
       → PaymentRepository (update status, PESSIMISTIC_WRITE)
       → OrderRepository (update all orders → CONFIRMED)
       → Publish OrderConfirmedEvent → OrderNotificationListener
       → NotificationRepository (insert)
       → Browser polling → PaymentResultPage
```
**Nguồn:** `OrderServiceImpl.java`, `PaymentController.java`, `VnPayStrategy.java`, `PaymentGatewayService.java`

**Flow 2: Realtime Chat**
```
Browser → FloatingChatWidget → ChatRoomController.createOrGetRoom()
       → WebSocket CONNECT → JwtChannelInterceptor (xác thực JWT)
       → STOMP SEND "/app/chat.send" → ChatMessageController
       → ChatService.sendMessage() → ChatMessageRepository
       → convertAndSendToUser(recipientId, "/queue/messages")
       → Recipient browser receives real-time message
       → Optimistic UI (sender sees message instantly)
```
**Nguồn:** `ChatMessageController.java`, `ChatServiceImpl.java`, `JwtChannelInterceptor.java`

**Flow 3: Review sau khi nhận hàng**
```
Browser → ProductDetailPage → ReviewController.createReview()
       → ReviewService.createReview()
       → OrderInternalService.checkDeliveredOrder() (eligibility)
       → ReviewRepository (insert)
       → Publish ReviewCreatedEvent
       → ReviewNotificationListener @TransactionalEventListener(AFTER_COMMIT)
       → NotificationRepository (insert in new TX)
       → Vendor's NotificationDropdown (polling 60s) → hiển thị thông báo
```
**Nguồn:** `ReviewController.java`, `ReviewServiceImpl.java`, `ReviewNotificationListener.java`

#### 3.4.3 Sơ đồ hoạt động (Activity Diagram)
**Cần vẽ:**
- Activity Diagram: Luồng Checkout (từ Cart → Payment Confirmed)
- Activity Diagram: Vendor Order Status Flow
**Nguồn:** `OrderServiceImpl.java`, `VendorOrderController.java`

#### 3.4.4 Sơ đồ trạng thái (State Diagram)
**Cần vẽ:**
- **Order State Machine:** PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED
- **Payment State Machine:** PENDING → SUCCESS / FAILED
- **Shop State Machine:** PENDING → ACTIVE (/ BANNED — chưa implement)
**Nguồn:** `OrderStatus.java`, `PaymentStatus.java`, `ShopStatus.java`

### 3.5 Thiết kế cơ sở dữ liệu

#### 3.5.1 Lược đồ CSDL quan hệ (ERD)
**Tất cả 20 bảng cần có trong ERD:**
1. `users` (id, email, password_hash, full_name, phone, status)
2. `roles` (id, name)
3. `user_roles` (user_id, role_id)
4. `shops` (id, user_id, name, description, status)
5. `categories` (id, name, description, parent_id)
6. `brands` (id, name, logo_url, description)
7. `products` (id, shop_id, name, description, price, stock_quantity, category_id, brand_id, status)
8. `product_variants` (id, product_id, sku, price, stock_quantity, attributes JSON, status)
9. `product_images` (id, product_id, image_url, public_id, is_thumbnail, sort_order)
10. `carts` (id, user_id)
11. `cart_items` (id, cart_id, product_variant_id, quantity)
12. `orders` (id, user_id, shop_id, payment_group_id, status, total_amount, shipping_address, payment_method, applied_voucher_id, applied_voucher_code, original_total_amount, discount_amount, created_at, updated_at)
13. `order_items` (id, order_id, product_variant_id, quantity, unit_price, product_name, sub_total)
14. `payments` (id, payment_group_id, amount, method, status, transaction_id, gateway_transaction_id, raw_data, created_at, updated_at)
15. `reviews` (id, user_id, product_id, order_id, rating, comment, vendor_reply, vendor_replied_at, created_at, updated_at)
16. `review_images` (review_id, image_url)
17. `vouchers` (id, code, scope, shop_id, type, discount_value, max_discount, min_order_value, start_date, end_date, usage_limit, used_count, created_at, updated_at)
18. `notifications` (id, user_id, type, title, message, reference_id, is_read, created_at)
19. `chat_rooms` (id, buyer_id, shop_id, created_at, updated_at)
20. `chat_messages` (id, room_id, sender_id, content, is_read, created_at)
**Nguồn:** Tất cả Entity classes

#### 3.5.2 Các mối quan hệ quan trọng
**Nội dung:**
- User (1) ↔ (1) Cart (1 user 1 cart)
- User (1) ↔ (1) Shop (1 user 1 shop)
- User (M) ↔ (N) Role (qua user_roles)
- Shop (1) ↔ (N) Product
- Product (1) ↔ (N) ProductVariant
- Product (1) ↔ (N) ProductImage
- Category (1) ↔ (N) Category (self-referencing hierarchy)
- Cart (1) ↔ (N) CartItem ↔ (N) ProductVariant
- Order (1) ↔ (N) OrderItem
- Orders (N) ↔ grouped by payment_group_id ↔ (1) Payment group
- Review (N) ↔ (1) Product, (1) User, (1) Order
- ChatRoom (1) ↔ (N) ChatMessage

#### 3.5.3 Các ràng buộc dữ liệu
**Nội dung:**
- UNIQUE: `users.email`, `shops.user_id` (1 user 1 shop), `shops.name`, `product_variants.sku`, `vouchers.code`
- UNIQUE COMPOSITE: `chat_rooms(buyer_id, shop_id)`, `reviews(user_id, order_id, product_id)`
- NOT NULL: các cột bắt buộc
- Indexes: `idx_product_shop`, `idx_product_status`, `idx_order_user_status_created`, `idx_order_payment_group`, `idx_review_product_created`, `idx_notif_user_created`, `idx_chat_room_created`, `idx_payment_group_created`
**Nguồn:** Entity annotations `@Table(indexes=...)`, `@UniqueConstraint`

### 3.6 Thiết kế giao diện
**Cần ảnh chụp màn hình thực tế**

**Nội dung cần mô tả:**
- Layout tổng thể (PublicLayout cho User/Vendor, AdminLayout cho Admin)
- ProductListPage — tìm kiếm, grid sản phẩm, phân trang
- ProductDetailPage — chi tiết sản phẩm, variants, đánh giá, Chat
- CartPage — giỏ hàng, tổng tiền
- CheckoutPage — tách đơn theo shop, voucher, phương thức thanh toán
- PaymentResultPage — kết quả sau thanh toán
- OrderHistoryPage — lịch sử đơn hàng
- VendorProductListPage — quản lý sản phẩm
- VendorOrderListPage — quản lý đơn hàng, cập nhật trạng thái
- VendorVoucherListPage — quản lý voucher
- AdminShopListPage — duyệt gian hàng
- FloatingChatWidget — widget chat nổi
- NotificationDropdown — thông báo
**Nguồn:** Frontend pages trong `frontend/src/pages/`
**⚠️ CẦN CHỤP ẢNH THỰC TẾ**

### 3.7 Kết quả triển khai

#### 3.7.1 Triển khai hệ thống
**Nội dung:**
- Backend: Spring Boot, Java 21, MySQL, port 8080
- Frontend: Vite dev server, port 5173
- External: Cloudinary, VNPay Sandbox, Gmail SMTP
- Môi trường: Local development (chưa deploy production)
**Nguồn:** `application.yml`, `REPORT_PROJECT_ANALYSIS.md` → Section 17

#### 3.7.2 Chức năng đã hoàn thành
**Nội dung:** Liệt kê tất cả features từ `REPORT_PROJECT_ANALYSIS.md` → Section 18
**Có thể viết ngay:** ✅

#### 3.7.3 Hình ảnh chạy thực tế
**⚠️ CẦN CHỤP ẢNH / QUAY VIDEO — xem SCREENSHOT_CHECKLIST.md**

#### 3.7.4 Hạn chế
**Nội dung:** Từ `REPORT_PROJECT_ANALYSIS.md` → Section 20
**Có thể viết ngay:** ✅

---

## CHƯƠNG 4 — KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

### 4.1 Kết luận
**Nội dung:**
- Đã hoàn thành gì? (core ecommerce flow, multi-vendor, payment, realtime chat, voucher, review, notification)
- Kiến thức đạt được (Modular Monolith, Spring Boot, JPA, Security, WebSocket, Event-Driven)
- Đã đáp ứng mục tiêu đề tài không?
**Có thể viết ngay:** ✅

### 4.2 Hướng phát triển
**Nội dung:**
- Redis caching (đã trong kế hoạch Phase 8)
- Docker / Docker Compose
- AWS deployment
- WebSocket Notification push
- Admin Dashboard statistics
- Order cancellation + inventory refund
- Mobile app (React Native)
- Rating-based product sorting
- Advanced search (Elasticsearch)
- Multi-language support
**Nguồn:** `REPORT_PROJECT_ANALYSIS.md` → Sections 19-20, PROJECT_CONTEXT.md Phase 8-9
**Có thể viết ngay:** ✅

---

## TÀI LIỆU THAM KHẢO
**Gợi ý nguồn:**
1. Spring Boot Documentation — https://docs.spring.io/spring-boot/
2. Spring Security Documentation — https://docs.spring.io/spring-security/
3. JWT RFC 7519 — https://tools.ietf.org/html/rfc7519
4. WebSocket RFC 6455 — https://tools.ietf.org/html/rfc6455
5. VNPay Documentation
6. Cloudinary Documentation
7. React Documentation — https://react.dev/
8. "Microservices Patterns" — Chris Richardson
9. MySQL Documentation

---

## PHỤ LỤC
**Gợi ý:**
- Phụ lục A: Danh sách API endpoints đầy đủ (Swagger UI export)
- Phụ lục B: Cấu hình môi trường (environment variables)
- Phụ lục C: Hướng dẫn cài đặt và chạy dự án

---

## TÓM TẮT — PHẦN CÓ THỂ VIẾT NGAY VS CẦN ẢNH/DIAGRAM

### ✅ Có thể viết ngay (không cần ảnh/diagram):
- MỞ ĐẦU
- Toàn bộ Chương 1
- Chương 2 (lý thuyết)
- 3.1 Giới thiệu hệ thống
- 3.2.2 Các thành phần (module table)
- 3.3.2 Đặc tả Use Case (text flow)
- 3.5.1 Mô tả bảng CSDL (có thể không cần ERD đồ họa)
- 3.5.2 Mối quan hệ
- 3.5.3 Ràng buộc
- 3.7.1 Triển khai hệ thống
- 3.7.2 Chức năng đã hoàn thành
- 3.7.4 Hạn chế
- Chương 4

### 🖼️ Cần ảnh chụp màn hình:
- 3.6 Thiết kế giao diện (toàn bộ)
- 3.7.3 Hình ảnh chạy thực tế

### 📐 Cần vẽ diagram:
- 3.2.1 Architecture Diagram
- 3.2.3 Request Flow Diagram
- 3.3.1 Use Case Diagram
- 3.4.1 Class Diagram
- 3.4.2 Sequence Diagrams (3 flows)
- 3.4.3 Activity Diagrams (2 flows)
- 3.4.4 State Diagrams (3 state machines)
- 3.5.1 ERD (Entity Relationship Diagram)
