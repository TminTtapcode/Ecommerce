# Enterprise Ecommerce - Project Context

## 1. Project Overview

Project name:

Enterprise Ecommerce

Project type:

Enterprise Ecommerce / Omnicommerce System

Main goal:

Build a realistic ecommerce platform that demonstrates both business functionality and enterprise backend development practices.

The system should be developed incrementally.

The priority is:

```text
Basic Business Functions
        ↓
Core Ecommerce Flow
        ↓
Advanced Features
        ↓
Infrastructure
        ↓
Optimization
```
2. Learning Goals

This project is not only about finishing the application.

The developer should understand:

Business requirements
Ecommerce workflows
REST API
Spring Boot
Spring Security
JWT
JPA/Hibernate
MySQL
Modular Monolith
DTO design
Transaction management
Redis
WebSocket
Docker
Deployment
Software architecture
Request/Response lifecycle

AI should explain important concepts while helping implement the system.

3. Technology Stack
Backend
Java 17+
Spring Boot 3.x
Spring Web
Spring Data JPA
Spring Security
JWT
Bean Validation
Maven
Lombok
Database
MySQL
Frontend (Not Started)
React
TypeScript
Vite
Advanced Technologies

To be introduced later when appropriate:

Redis
WebSocket
Docker
Docker Compose
AWS
4. Architecture

Architecture:

Modular Monolith

The application is one deployable Spring Boot application with separated business modules.

See:

docs/ARCHITECTURE.md

for detailed architecture rules.

5. Main Modules

Current actual and planned modules:

1. Identity (Combines Auth and User)
2. Product (Combines Category and Product)
3. Shop
4. Media (Currently structured as `infrastructure.media`)
5. Cart
6. Order
7. Payment
8. Review
9. Notification (Planned)
10. Chat (Planned)

The list can change as requirements evolve.

6. Development Priority
Phase 1 — Foundation (Completed)
Project setup
Architecture
Database
Common exception handling
Common API conventions
Security foundation
Phase 2 — Authentication (Completed)
Register
Login
JWT
Authorization
Roles
Phase 3 — Product Catalog (Completed)
Category
Product
Product Variant
Product CRUD
Product Search
Product Images
Phase 3.5 — Shop (Completed)
Shop Creation
Shop Approval
Phase 4 — Shopping (Completed)
Cart
Cart Items
Stock validation
Phase 5 — Order (Completed)
Checkout (Completed)
Create Order (Completed)
Order Items (Completed)
Order Status (Completed)
Order History (Completed)
Order Cancellation (Completed)
Phase 6 — Payment (Completed)
Payment creation (Completed)
Payment provider integration (Completed - VNPay, COD)
Webhook (Completed with Idempotency & Order update)
Payment status (Completed)
Payment confirmation (Completed)
Phase 7 — Supporting Features (In Progress)
Review (Completed)
Notification
Media (Completed Cloudinary integration)
Phase 8 — Advanced Features (Pending)
Redis
Realtime Chat
WebSocket
Caching
Phase 9 — Infrastructure (Pending)
Docker
Docker Compose
AWS
Deployment
Monitoring
7. Core Business Flow

The most important business flow is:

User
 |
 v
Browse Products
 |
 v
Product Details
 |
 v
Add to Cart
 |
 v
Checkout
 |
 v
Create Order
 |
 v
Payment
 |
 v
Payment Confirmation
 |
 v
Order Confirmed

The system should prioritize getting this end-to-end flow working before adding many advanced features.

8. Current Development Status

Completed Modules
Identity, Product, Cart, Shop, Media, Order, Payment, Review, Voucher, Notification, Realtime Chat (Backend WebSocket STOMP + Shopee-style Floating Chat Widget Frontend, Verified E2E Realtime Bidirectional Messaging, Optimistic UI), Database Index Audit (Phase 3.1 & 3.2), Query / N+1 Audit (Phase 3.3), Transaction Audit & Boundary Implementation (Phase 3.4), Admin & Voucher P2-FE.
Current Module
Phase 3 (E2E Full System Audit)
Next Module
TBD
Known Problems
- (Đã fix) WebSocket HTTP Handshake 401: Cho phép permitAll cho HTTP handshake `/ws/**`, xác thực JWT nghiêm ngặt trong STOMP `JwtChannelInterceptor`.
- (Đã fix) WebSocket Principal Mismatch (Lệch định danh STOMP): `Principal.getName()` được chuẩn hóa trả về `userId.toString()` thay vì email, giúp `convertAndSendToUser` định tuyến chính xác đến cả người gửi và người nhận.
- (Đã fix) 500 Rollback-only on getMyRooms: Bổ sung `ShopInternalService.findShopIdByUserId` trả về `Optional<Long>` thay vì ném exception khi User là Buyer thuần túy.
Technical Debt & Enterprise Architectural Notes
- **Shop Registration Error Handling (P1-FE)**: Backend hiện tại trả về `400 Bad Request` kèm text lỗi tiếng Việt khi user chưa có shop. P1-FE vẫn đang phải matching string này thay vì một mã lỗi chuẩn hóa (`404` hoặc Error Code cụ thể).
- **Admin Status Mutation API**: Admin Order hiện tại ở chế độ "View-Only" vì Backend chưa có API Mutation cho Status của Order từ phía Admin.
- **Transaction Boundaries & AFTER_COMMIT Isolation (Phase 3.4)**:
  - `ReviewNotificationListener`: Handler `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` được gán `@Transactional(propagation = Propagation.REQUIRES_NEW)` để mở một transaction độc lập, đảm bảo ghi `Notification` an toàn vào DB sau khi review transaction gốc đã commit thành công.
  - `Shop Approval Email Decoupling`: Loại bỏ hoàn toàn việc gọi trực tiếp `EmailService` trong transaction body của `ShopService.approveShop()`. Thay vào đó, publish semantic domain event `ShopApprovedEvent` trong `shop.api.event`. `ShopNotificationListener` lắng nghe với `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` và dispatch email bất đồng bộ. Ngăn chặn triệt để rủi ro gửi email nếu transaction duyệt shop bị rollback.
  - `@Transactional(readOnly = true)`: Bổ sung có chọn lọc cho các query methods thuần đọc trong `OrderServiceImpl`, `ProductServiceImpl`, `ProductInternalServiceImpl`, `ReviewServiceImpl`, và `IdentityInternalServiceImpl`. Giữ Hibernate session mở an toàn cho lazy associations (`Order.items`, `Product.variants`, `Product.images`) và tối ưu bỏ qua dirty-checking (`FlushMode.MANUAL`), không ngộ nhận routing read-replica khi chưa có multi-datasource.
  - Audit `rollbackFor`: Xác nhận tất cả write methods chỉ throw unchecked exceptions (`RuntimeException`, `IllegalArgumentException`, `IllegalStateException`). Giữ nguyên `@Transactional` mặc định của Spring (rollback on `RuntimeException` & `Error`), không blanket `@Transactional(rollbackFor = Exception.class)`.
  - Bulk UPDATE & Persistence Context: Thêm `@Modifying(clearAutomatically = true)` vào `OrderRepository.updateStatusByPaymentGroupId` để tự động invalidate Hibernate L1 cache sau bulk update SQL, tránh stale managed `Order` entities trong session.
  - Empirical Verification: `TransactionAuditIntegrationTest` kiểm thử thực nghiệm 3 kịch bản: (1) Multi-shop checkout all-or-nothing rollback khi 1 shop thiếu hàng; (2) Review notification AFTER_COMMIT & rollback; (3) VNPay IPN atomicity, rollback, và idempotency. Toàn bộ 26/26 tests trong test suite đều pass.
  - Pending Business Rule: Logic hoàn trả tồn kho và voucher khi Vendor huỷ đơn (`OrderStatus.CANCELLED`) được định vị là pending business rule thuộc flow huỷ đơn/hoàn tiền, không mở rộng scope trong Phase 3.4.
- **Realtime Chat Module & Modular Monolith Boundary**:
  - Chat Module hoàn toàn độc lập, chỉ lưu trữ các ID nguyên thủy (`buyerId`, `shopId`, `senderId`) trong `ChatRoom` và `ChatMessage`.
  - Tuyệt đối không `@ManyToOne` sang `User` hay `Shop` entity. Mọi giao tiếp kiểm tra quyền sở hữu Shop, lấy tên Shop, lấy ID chủ Shop đều qua contract `ShopInternalService` (`findShopIdByUserId`, `getShopNameById`, `getUserIdByShopId`).
- **WebSocket STOMP Security & User Destination Routing**:
  - Endpoint WebSocket: `/ws` với SockJS fallback.
  - HTTP Handshake: `permitAll("/ws/**")` trên Spring Security filter chain để không chặn upgrade request.
  - STOMP Authentication: `JwtChannelInterceptor` chặn command `CONNECT`, kiểm tra Bearer JWT, trích xuất `userId` và gán `StompPrincipal` có `getName() == userId.toString()`.
  - P2P Message Routing: `messagingTemplate.convertAndSendToUser(recipientId.toString(), "/queue/messages", response)` và gửi echo cho `senderId.toString()`. Spring STOMP `UserDestinationMessageHandler` ánh xạ chính xác đến session người dùng.
  - `ChatMessageController`: Lắng nghe `@MessageMapping("/chat.send")`, lấy `senderId` trực tiếp từ `Long.parseLong(principal.getName())`, loại bỏ hoàn toàn sự phụ thuộc vào `User` entity của Identity module.
- **Frontend Realtime Chat Architecture & Shopee Floating Widget**:
  - `websocketService`: Singleton STOMP client quản lý kết nối, tự động reconnect, và lắng nghe `/user/queue/messages`.
  - `ChatContext` & `useChat`: Quản lý danh sách hội thoại, phòng đang chọn, bộ đếm tin chưa đọc toàn cục (`totalUnreadCount`), và hỗ trợ **Optimistic UI (0ms latency)** với ID tạm âm và tự động thay thế khi server echo.
  - `FloatingChatWidget`: Khung chat nổi Shopee chuẩn thiết kế (ảnh đính kèm): Docked góc dưới bên phải (`fixed bottom-0 right-6 z-50`), nút thu nhỏ pill cam Shopee `#ee4d2d`, banner vàng cảnh báo, danh sách hội thoại có tìm kiếm/bộ lọc, màn hình chờ máy tính xách tay minh họa ("Chào mừng bạn đến với Shopee Chat"), và thanh chat đầy đủ biểu cảm/nút gửi.
  - Tích hợp 1 chạm: Nút "Chat ngay" tại `ProductDetailPage` kết nối trực tiếp với Shop bán hàng qua `openChatWithShop(product.shopId)`.
- **Multi-Vendor Payment Aggregation (1:N)**:
  - Một lượt Checkout nhiều Shop sinh ra N `Order` (mỗi Shop 1 Order) nhưng được gom chung bởi `payment_group_id` (UUID).
  - Không tạo entity `OrderGroup` để giữ hệ thống đơn giản, nhẹ nhàng nhưng vẫn đáp ứng hoàn hảo nghiệp vụ.
  - Quan hệ giữa `payment_group_id` và `Payment` là **1:N** (1 Group có thể có nhiều lượt thử thanh toán `Payment` khác nhau với mã `txn_ref` duy nhất riêng biệt).
- **Vendor Order Management & State Machine Matrix**:
  - Tách biệt Endpoint Kênh Người Bán (`/api/v1/vendor/orders`) sử dụng `VendorOrderController` với bảo mật `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")`.
  - Ma trận chuyển đổi trạng thái (State Machine): `CONFIRMED` → `SHIPPED` → `DELIVERED`, hoặc `CANCELLED`.
  - Phân lập dữ liệu Shop nghiêm ngặt qua contract `ShopInternalService.getShopIdByUserId` và `isShopOwner`.
- **Order Lifecycle & Retry Payment Logic**:
  - Khi VNPAY IPN báo `SUCCESS`: `Payment` → `SUCCESS`, tất cả các `Order` thuộc group chuyển sang **`CONFIRMED`** (Chờ Shop chuẩn bị hàng).
  - Khi VNPAY IPN báo `FAILED`: `Payment` → `FAILED`, nhưng `Order` **vẫn giữ `PENDING`**. Điều này cho phép người dùng bấm "Thanh toán lại" để phát hành một Payment URL mới cho cùng `payment_group_id` mà không bị hủy đơn nhầm.
- **Product Image Management**:
  - Hỗ trợ Upload/Xóa ảnh Cloudinary qua `FileStorageService`. Áp dụng cơ chế **Zero-Trust publicId Deletion** (chỉ lấy `publicId` từ DB, không tin Client) và sử dụng **TransactionSynchronizationManager** (`afterCommit`) để tách biệt việc xóa Cloudinary khỏi MySQL Transaction, ngăn ngừa sai lệch dữ liệu khi Rollback. Server tự động normalize Thumbnail và Sort Order.
- **Concurrency & Strict Idempotency**:
  - Sử dụng **Pessimistic Lock (`@Lock(LockModeType.PESSIMISTIC_WRITE)`)** trên `PaymentRepository` khi query `Payment` trong IPN Webhook (`SELECT ... FOR UPDATE`).
  - Ngăn chặn triệt để Race Condition khi có 2 Webhook gửi đồng thời cùng millisecond. Webhook đến sau sẽ bị ngắt an toàn (`status != PENDING`).
- **Decoupled Module Boundary & Polling**:
  - Frontend `PaymentResultPage` polling trạng thái trực tiếp từ Payment Module via `GET /api/v1/payments/group/{paymentGroupId}/status`.
  - Payment Module chủ động phản hồi trạng thái tổng hợp mà không cần Order Module tiết lộ logic trạng thái chi tiết của từng Order.
- **Voucher Multi-vendor Proportional Split & Atomic Updates**:
  - Hệ thống hỗ trợ Voucher cấp `SYSTEM` và `SHOP`. Voucher `SYSTEM` được giảm giá theo công thức tỉ lệ (Proportional split): `Shop Discount = Total Discount * (Shop Subtotal / Total Eligible Subtotal)` để đảm bảo công bằng.
  - Sử dụng Atomic update trong cơ sở dữ liệu (`UPDATE vouchers SET used_count = used_count + 1 WHERE ...`) thay vì JPA `@Version` để ngăn ngừa Race condition triệt để khi sử dụng Voucher.
- **Notification System & Semantic Events**:
  - Implemented Event-Driven architecture using `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` to decouple from Order/Review core transactions.
  - Replaced generic `OrderStatusChangedEvent` with Semantic Domain Events (`OrderConfirmedEvent`, `OrderShippedEvent`, `OrderDeliveredEvent`) to prevent enum/domain leakage from Order to Notification.
  - Frontend uses a unified `NotificationDropdown` across layouts with a 60s REST polling mechanism for MVP (WebSocket push is deferred to Phase 8).
- **Strict Modular Monolith Boundaries (No Entity Leakage)**:
  - Cross-module communication exclusively relies on Internal Services and DTOs.
  - For example, `OrderInternalService` returns `DeliveredOrderData` instead of `List<Order>` to prevent N+1 queries and Entity leakage to the `Review` module.
  - Similarly, `VoucherInternalService` uses `VoucherCalculationRequest` and `VoucherDiscountResult` to decouple from the `Order` module.
  - `DataInitializer` components are strictly isolated per module, relying on predefined constants (`SeedDataIds`) instead of cross-module repository imports.
- **Database Query & N+1 Optimizations**:
  - Replaced `@EntityGraph` on paginated collections (e.g., `Order.items`) with `@BatchSize(size = 50)` to prevent Cartesian product explosions and in-memory pagination warnings.
  - Replaced iterative cross-module N+1 loops during DTO mapping with bulk internal APIs (e.g., `ProductInternalService.getProductIdsByVariantIds`, `ProductInternalService.getVariantInfos`).
  - Complex nested existence checks refactored into bulk `IN` clause queries (e.g., `ReviewRepository.findReviewedOrderIds`).
  - Validated by custom Hibernate statistics integration tests (`NPlus1AuditTest`) achieving fixed complexity bounds (e.g. `getCart` = O(1) fixed 3 queries; `checkout` = O(ShopCount) fixed 15 queries).
Frontend Progress
- Initialized Vite + React + TypeScript in `/frontend`.
- Configured Tailwind CSS, Axios, and React Router.
- **[VERIFIED]** Implemented `ProductListPage` và `ProductDetailPage` với tính năng tìm kiếm (search bar), phân trang, và fetch API thực tế.
- **[VERIFIED]** Implemented `AdminLayout` và `PublicLayout` với thiết kế UI hoàn chỉnh.
- **[VERIFIED]** Implemented Authentication (Register, Login, JWT, Roles).
- **[VERIFIED]** Implemented Frontend Cart Management (View, Add, Update, Remove).
- **[VERIFIED]** Implemented `AdminProductFormPage` hỗ trợ Upload nhiều ảnh lên Cloudinary, chọn Ảnh bìa (Thumbnail Badge), Dropzone Empty State, Spinner Upload. Đã tích hợp `publicId` Cloudinary để Backend xử lý xóa an toàn.
- **[VERIFIED]** Implemented `CheckoutPage` hỗ trợ tách đơn Multi-Vendor, nhận `paymentGroupId`, lưu `sessionStorage`, chuyển hướng VNPAY/COD, và xem trước/áp dụng **Mã giảm giá Voucher** tính toán trực tiếp từ Backend.
- **[VERIFIED]** Implemented `PaymentResultPage` dùng kỹ thuật Polling trạng thái Payment Group từ Backend, không phụ thuộc Query Params của VNPAY.
- **[VERIFIED]** Luồng End-to-End Multi-Vendor đã hoạt động từ Chọn hàng -> Giỏ hàng -> Checkout -> Payment Grouping -> IPN Idempotent -> Cập nhật CONFIRMED.
- **[VERIFIED]** Implemented **Shop Manager Portal (Vendor Order Management)** — Cho phép Vendor xem đơn hàng của Shop mình, cập nhật trạng thái đơn qua Ma trận State Machine (`CONFIRMED` → `SHIPPED` → `DELIVERED`, hoặc `CANCELLED`), có bảo mật phân quyền Shop và kiểm thử tự động `VendorOrderManagementTest` (4/4 tests passed).
- **[VERIFIED]** Implemented Notification System UI: Reusable NotificationDropdown integrated into both AdminLayout and PublicLayout. Includes 60s polling for unread counts, optimistic UI updates, and intelligent routing based on notification type and referenceId.
- **[VERIFIED]** Implemented **Realtime Shopee Chat Widget UI & WebSocket Flow**:
  - Khung chat nổi chuẩn Shopee (`FloatingChatWidget`) với đầy đủ giao diện nút pill cam, banner thông báo vàng, danh sách hội thoại (tìm kiếm + phân loại), empty state minh họa laptop, bong bóng tin nhắn phân biệt Buyer/Shop, thanh công cụ nhập liệu.
  - Tích hợp nút "Chat ngay" tại trang chi tiết sản phẩm (`ProductDetailPage`).
  - Trải nghiệm nhắn tin thời gian thực với **Optimistic UI (0ms latency)**, đồng bộ hoàn hảo qua WebSocket STOMP và REST API.
- **[VERIFIED]** Implemented **Admin Shop Management**: Trang danh sách cửa hàng hỗ trợ filter trạng thái, phân trang, duyệt shop trực tiếp (`Approve`).
- **[VERIFIED]** Implemented **Admin Order Management**: Trang danh sách đơn hàng toàn hệ thống và trang chi tiết đơn hàng (View-only), tuân thủ triệt để contract Backend, không query N+1 lấy tên Shop/User.
- **[VERIFIED]** Implemented **Voucher Management (Admin & Vendor)**: Giao diện quản lý Hệ thống Voucher cho Admin (Scope: SYSTEM) và Shop Voucher cho Vendor (Scope: SHOP). Đảm bảo rule ownership bảo mật cao khi Vendor không truyền `shopId`.

9. Important Development Rules

The project should be developed incrementally.

Do not build the entire system at once.

Prefer:

Module
  ↓
Use Case
  ↓
Implementation
  ↓
Test
  ↓
Verify
  ↓
Checkpoint

over:

Build entire module
  ↓
Test everything at the end
10. AI Development Rules

AI is a development assistant and mentor.

AI should:

Inspect existing code before changing it.
Explain important business logic.
Explain important architecture decisions.
Reuse existing patterns.
Avoid unnecessary refactoring.
Avoid changing unrelated modules.
Identify risks before implementation.
Keep the architecture consistent.

AI should not:

Rewrite the project without permission.
Introduce unnecessary technologies.
Create unnecessary abstractions.
Guess unclear business requirements.
Directly access another module's repository/entity.
Modify unrelated modules without justification.
11. Project Progress Rule

After completing an important module/use case, update:

Current Development Status
Completed Modules
Current Module
Known Problems
Technical Debt

This allows AI to understand the current project state in future sessions.
