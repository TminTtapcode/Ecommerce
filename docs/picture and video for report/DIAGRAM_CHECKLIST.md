# DIAGRAM_CHECKLIST.md
# Danh Sách Diagram Cần Tạo — Enterprise Ecommerce

> Tất cả diagram có bằng chứng từ source code thực tế.
> Gợi ý tool: draw.io (diagrams.net), PlantUML, Lucidchart, hoặc Mermaid

---

## DIA-01: Architecture Diagram (Kiến trúc hệ thống)
**Mục đích:** Minh họa kiến trúc tổng thể Modular Monolith
**Vị trí báo cáo:** 3.2.1 Kiến trúc tổng thể
**Loại:** Deployment / Component Diagram

### Đối tượng cần có:
```
[Browser]
    ↓ HTTP REST / WebSocket STOMP
[Spring Boot Application]
    ├── Security Filter Chain (JWT)
    ├── [identity module] AuthController
    ├── [shop module] ShopController, AdminShopController
    ├── [product module] ProductController, VendorProductController, CategoryController
    ├── [cart module] CartController
    ├── [order module] OrderController, VendorOrderController, AdminOrderController
    ├── [payment module] PaymentController
    ├── [review module] ReviewController, VendorReviewController
    ├── [voucher module] VoucherController, AdminVoucherController
    ├── [notification module] NotificationController
    ├── [chat module] ChatRoomController, ChatMessageController
    └── [media module] MediaController
    ↓
[MySQL Database]

External Services:
- [VNPay] ← PaymentController IPN
- [Cloudinary] ← MediaController
- [Gmail SMTP] ← EmailService
```

### Source code evidence:
- `pom.xml` (Spring Boot 4.1.0)
- `application.yml` (MySQL, Cloudinary, VNPay, Gmail)
- Package structure: 13 modules

**Gợi ý thiết kế:**
- Box màu xanh: Spring Boot app
- Box màu xám: modules bên trong
- Màu cam: external services
- Mũi tên: luồng data

---

## DIA-02: Module Dependency Diagram (Phụ thuộc giữa modules)
**Mục đích:** Minh họa cross-module communication qua Internal Services
**Vị trí báo cáo:** 3.2.2 Các thành phần
**Loại:** Component Diagram

### Quan hệ cần có:
```
Order Module
  → ProductInternalService (kiểm tra tồn kho)
  → ShopInternalService (lấy shopId)
  → CartInternalService (lấy cart items)
  → VoucherInternalService (tính discount)

Payment Module
  → OrderInternalService (lấy payment data, validate group)

Chat Module
  → ShopInternalService (findShopIdByUserId, getShopNameById)

Review Module
  → OrderInternalService (check delivered order eligibility)

Notification Module
  ← OrderNotificationListener (lắng nghe Order events)
  ← ReviewNotificationListener (lắng nghe Review events)
  ← ShopNotificationListener (lắng nghe Shop events)
```

### Source code evidence:
- `OrderServiceImpl.java` (gọi CartInternalService, ProductInternalService, VoucherInternalService)
- `PaymentController.java` (gọi OrderInternalService)
- `ChatMessageController.java` (chỉ dùng ShopInternalService)
- `OrderNotificationListener.java`, `ReviewNotificationListener.java`, `ShopNotificationListener.java`

---

## DIA-03: Use Case Diagram — Toàn hệ thống
**Mục đích:** Tổng quan tất cả actors và use cases
**Vị trí báo cáo:** 3.3.1 Sơ đồ Use Case
**Loại:** UML Use Case Diagram

### Actors:
- **Buyer (User)** — primary actor
- **Vendor** — primary actor (extends User)
- **Admin** — primary actor
- **VNPay System** — external actor
- **Cloudinary** — external actor

### Use Cases:

**Buyer:**
- Đăng ký tài khoản
- Đăng nhập
- Duyệt sản phẩm
- Tìm kiếm sản phẩm
- Xem chi tiết sản phẩm
- Thêm vào giỏ hàng
- Quản lý giỏ hàng
- Áp dụng voucher
- Đặt hàng (Checkout)
- Thanh toán VNPay
- Thanh toán COD
- Xem kết quả thanh toán
- Xem lịch sử đơn hàng
- Viết đánh giá
- Xem đánh giá
- Chat với Shop
- Xem thông báo

**Vendor (includes Buyer):**
- Đăng ký gian hàng
- Tạo/Sửa/Xóa sản phẩm
- Upload ảnh sản phẩm
- Xem đơn hàng của shop
- Cập nhật trạng thái đơn
- Tạo/Xem voucher shop
- Chat với khách hàng
- Trả lời đánh giá

**Admin:**
- Xem/Duyệt gian hàng
- Quản lý sản phẩm toàn hệ thống
- Xem đơn hàng toàn hệ thống
- Tạo voucher hệ thống

**Extends/Includes:**
- "Thanh toán" includes "Thanh toán VNPay" XOR "Thanh toán COD"
- "Đặt hàng" includes "Áp dụng voucher" (optional)
- "Viết đánh giá" requires "Đã mua hàng" (precondition)

### Source code evidence:
- `FEATURE_INVENTORY.md` — toàn bộ features
- Controller files (xác minh endpoints)

---

## DIA-04: Class Diagram — Entity Layer (Core Domain)
**Mục đích:** Mô tả các Entity chính và quan hệ
**Vị trí báo cáo:** 3.4.1 Sơ đồ lớp
**Loại:** UML Class Diagram (chỉ Entity layer)

### Classes cần có (nhóm 1 — Identity & Shop):
```
User {
  id: Long
  email: String
  passwordHash: String
  fullName: String
  phone: String
  status: String
  roles: Set<Role>
}

Role {
  id: Long
  name: String
}

Shop {
  id: Long
  userId: Long  // loose coupling
  name: String
  description: String
  status: ShopStatus
}
```

### Classes cần có (nhóm 2 — Product):
```
Category {
  id: Long
  name: String
  description: String
  parent: Category  // self-ref
  subCategories: Set<Category>
}

Brand {
  id: Long
  name: String
  logoUrl: String
  description: String
}

Product {
  id: Long
  shopId: Long  // loose coupling
  name: String
  description: String
  price: BigDecimal
  stockQuantity: Integer
  category: Category
  brand: Brand
  status: ProductStatus
  variants: List<ProductVariant>
  images: List<ProductImage>
}

ProductVariant {
  id: Long
  product: Product
  sku: String
  price: BigDecimal
  stockQuantity: Integer
  attributes: Map<String,Object>  // JSON
  status: ProductStatus
}

ProductImage {
  id: Long
  product: Product
  imageUrl: String
  publicId: String
  isThumbnail: boolean
  sortOrder: int
}
```

### Classes cần có (nhóm 3 — Cart & Order & Payment):
```
Cart {
  id: Long
  userId: Long  // loose coupling
  items: List<CartItem>
}

CartItem {
  id: Long
  cart: Cart
  productVariantId: Long  // loose coupling
  quantity: Integer
}

Order {
  id: Long
  userId: Long
  shopId: Long
  paymentGroupId: String
  status: OrderStatus
  totalAmount: BigDecimal
  shippingAddress: String
  paymentMethod: String
  appliedVoucherId: Long
  appliedVoucherCode: String
  originalTotalAmount: BigDecimal
  discountAmount: BigDecimal
  items: List<OrderItem>
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}

OrderItem {
  id: Long
  order: Order
  productVariantId: Long  // loose coupling
  quantity: Integer
  unitPrice: BigDecimal
  productName: String
  subTotal: BigDecimal
}

Payment {
  id: Long
  paymentGroupId: String
  amount: BigDecimal
  method: PaymentMethod
  status: PaymentStatus
  transactionId: String
  gatewayTransactionId: String
  rawData: String
  createdAt: LocalDateTime
  updatedAt: LocalDateTime
}
```

### Classes cần có (nhóm 4 — Supporting):
```
Review {
  id: Long
  userId: Long
  productId: Long
  orderId: Long
  rating: Integer
  comment: String
  imageUrls: List<String>
  vendorReply: String
  vendorRepliedAt: LocalDateTime
  createdAt: LocalDateTime
}

Voucher {
  id: Long
  code: String
  scope: VoucherScope
  shopId: Long
  type: VoucherType
  discountValue: BigDecimal
  maxDiscount: BigDecimal
  minOrderValue: BigDecimal
  startDate: LocalDateTime
  endDate: LocalDateTime
  usageLimit: Integer
  usedCount: Integer
}

Notification {
  id: Long
  userId: Long
  type: NotificationType
  title: String
  message: String
  referenceId: String
  isRead: boolean
  createdAt: LocalDateTime
}

ChatRoom {
  id: Long
  buyerId: Long
  shopId: Long
  createdAt: LocalDateTime
}

ChatMessage {
  id: Long
  roomId: Long
  senderId: Long
  content: String
  isRead: boolean
  createdAt: LocalDateTime
}
```

### Relationships:
- User (1) — (N) User_Roles — (N) Role
- Cart (1) — (1) User [via userId, loose]
- Cart (1) — (N) CartItem
- CartItem (N) — (1) ProductVariant [via ID, loose]
- Product (N) — (1) Category
- Product (N) — (1) Brand
- Product (1) — (N) ProductVariant
- Product (1) — (N) ProductImage
- Category (1) — (N) Category [self-referencing]
- Order (1) — (N) OrderItem
- ChatRoom (1) — (N) ChatMessage
- Shop (1) — (1) User [via userId, loose]

**Lưu ý:** Highlight loose coupling pattern (dùng ID thay vì @ManyToOne cross-module)

### Source code evidence:
- Tất cả Entity files trong `src/main/java/com/tmt/ecommerce/*/entity/`

---

## DIA-05: Class Diagram — Service & Internal Contract Layer
**Mục đích:** Minh họa kiến trúc service layer và internal contracts
**Vị trí báo cáo:** 3.4.1 (phụ lục / mở rộng)
**Loại:** UML Class Diagram (Service layer)

### Cần có:
```
<<interface>> ProductInternalService
  + getProductVariant(variantId): ProductInternalData
  + exists(productId): boolean
  + getProductIdsByVariantIds(variantIds): Map
  + getVariantInfos(variantIds): List<ProductVariantInfoDto>

<<interface>> ShopInternalService
  + getShopIdByUserId(userId): Long
  + findShopIdByUserId(userId): Optional<Long>
  + isShopOwner(userId, shopId): boolean
  + getShopNameById(shopId): String
  + getUserIdByShopId(shopId): Long

<<interface>> OrderInternalService
  + getPaymentDataForGroup(groupId): OrderPaymentDto
  + validateGroupOwnershipAndStatus(groupId, userId)
  + getDeliveredOrdersByBuyerAndProduct(buyerId, productId): List<DeliveredOrderData>

<<interface>> VoucherInternalService
  + calculateDiscount(request): VoucherDiscountResult

<<interface>> CartInternalService
  + getCartItems(userId): List<CartItemInternalDto>
```

### Source code evidence:
- `ProductInternalService.java`, `ShopInternalService.java`
- `OrderInternalService.java`, `VoucherInternalService.java`, `CartInternalService.java`

---

## DIA-06: Sequence Diagram — Multi-vendor Checkout + VNPay Payment
**Mục đích:** Minh họa luồng nghiệp vụ phức tạp nhất của hệ thống
**Vị trí báo cáo:** 3.4.2 Sơ đồ tuần tự
**Loại:** UML Sequence Diagram

### Participants:
- Browser (React)
- CheckoutPage
- OrderController
- OrderServiceImpl
- CartInternalService
- ProductInternalService
- VoucherInternalService
- OrderRepository
- PaymentController
- VnPayStrategy
- VNPay (External)
- PaymentGatewayService (IPN handler)
- PaymentRepository
- OrderNotificationListener
- NotificationRepository

### Flow:
```
Browser → CheckoutPage → POST /api/v1/orders/checkout (CheckoutRequest)
OrderController → OrderServiceImpl.checkout()
  → CartInternalService.getCartItems(userId)
  → ProductInternalService.getProductVariant(variantId) [validate stock]
  → VoucherInternalService.calculateDiscount(request)
  → Save Orders per Shop (N orders, same paymentGroupId)
  → UPDATE product_variants stock
  → UPDATE vouchers.used_count (atomic)
  → DELETE cart_items
  → Return CheckoutResponse (paymentGroupId, orderIds)

Browser → CheckoutPage → POST /api/v1/payments/create
PaymentController → OrderInternalService.validateGroupOwnershipAndStatus()
  → OrderInternalService.getPaymentDataForGroup()
  → VnPayStrategy.generateUrl(paymentData, clientIp)
  → Return paymentUrl

Browser → Redirect to VNPay

VNPay → GET /api/v1/payments/vnpay/ipn (IPN callback)
PaymentController → PaymentGatewayService.processVnPayIpn()
  → PaymentRepository.findByTransactionId() [PESSIMISTIC_WRITE]
  → Validate hash, status
  → UPDATE Payment.status = SUCCESS
  → OrderRepository.updateStatusByPaymentGroupId(CONFIRMED) [@Modifying]
  → Publish OrderConfirmedEvent (per order, per shop owner)

OrderNotificationListener [@TransactionalEventListener AFTER_COMMIT]
  → NotificationRepository.save() [REQUIRES_NEW]

Browser → PaymentResultPage → polling GET /api/v1/payments/group/{id}/status
  → PaymentGatewayService.getPaymentGroupStatus()
  → Return CONFIRMED → display success
```

---

## DIA-07: Sequence Diagram — WebSocket Realtime Chat
**Mục đích:** Minh họa luồng chat thời gian thực
**Vị trí báo cáo:** 3.4.2 Sơ đồ tuần tự
**Loại:** UML Sequence Diagram

### Participants:
- BuyerBrowser
- VendorBrowser
- ChatRoomController
- ChatMessageController (WebSocket)
- JwtChannelInterceptor
- ChatServiceImpl
- ChatMessageRepository

### Flow:
```
BuyerBrowser → POST /api/v1/chat/rooms (CreateChatRoomRequest)
ChatRoomController → ChatService.createOrGetRoom() → Return ChatRoomResponse

BuyerBrowser → WebSocket CONNECT /ws (SockJS)
JwtChannelInterceptor → Validate JWT Bearer → Set StompPrincipal(userId)

VendorBrowser → WebSocket CONNECT /ws (SockJS)
JwtChannelInterceptor → Validate JWT Bearer → Set StompPrincipal(vendorUserId)

BuyerBrowser → SUBSCRIBE /user/queue/messages
VendorBrowser → SUBSCRIBE /user/queue/messages

BuyerBrowser → STOMP SEND "/app/chat.send" {roomId, content}
ChatMessageController.sendMessage() → ChatService.sendMessage()
  → ChatMessageRepository.save()
  → messagingTemplate.convertAndSendToUser(vendorId, "/queue/messages", response)
  → messagingTemplate.convertAndSendToUser(buyerId, "/queue/messages", response)

VendorBrowser ← receives message (realtime, no reload)
BuyerBrowser ← receives echo (replaces optimistic temp message)
```

---

## DIA-08: Sequence Diagram — Review sau nhận hàng + Event-Driven Notification
**Mục đích:** Minh họa review eligibility + event-driven notification
**Vị trí báo cáo:** 3.4.2 Sơ đồ tuần tự
**Loại:** UML Sequence Diagram

### Flow:
```
Browser → POST /api/v1/reviews (ReviewCreateRequest)
ReviewController → ReviewService.createReview(userId, request)
  → OrderInternalService.getDeliveredOrdersByBuyerAndProduct()
    [Check: has buyer purchased this product in DELIVERED order?]
  → ReviewRepository.findByUserIdAndOrderId() [check duplicate]
  → ReviewRepository.save()
  → Publish ReviewCreatedEvent

ReviewNotificationListener [@TransactionalEventListener(AFTER_COMMIT)]
  [Propagation.REQUIRES_NEW — NEW transaction]
  → NotificationRepository.save() [notify vendor]

VendorBrowser → GET /api/v1/notifications (polling 60s) → receives notification
```

---

## DIA-09: Activity Diagram — Checkout Flow
**Mục đích:** Minh họa luồng hoạt động Checkout đa điều kiện
**Vị trí báo cáo:** 3.4.3 Sơ đồ hoạt động
**Loại:** UML Activity Diagram

### Steps:
```
START
→ User nhập địa chỉ giao hàng
→ User chọn phương thức thanh toán (VNPay / COD)
→ [Optional] User nhập mã voucher
→ [Decision] Voucher hợp lệ?
  → NO: Hiển thị lỗi, cho nhập lại
  → YES: Tính giảm giá
→ User xác nhận đặt hàng
→ [Fork — Song song]:
  → Kiểm tra tồn kho tất cả items
  → Validate quyền sở hữu cart
→ [Join] Cả hai OK?
  → NO: Rollback, thông báo lỗi
  → YES: Tiến hành tạo đơn
→ Tách đơn theo Shop (1 order per shop)
→ Gán payment_group_id
→ Trừ tồn kho
→ Tăng used_count voucher (atomic)
→ Xóa cart items
→ [Decision] Phương thức thanh toán?
  → COD: Tạo Payment(status=PENDING_COD), chuyển đến kết quả
  → VNPay: Tạo VNPay URL, redirect
→ [VNPay path] VNPay xử lý → IPN callback
  → Payment SUCCESS: cập nhật Orders → CONFIRMED
  → Payment FAILED: giữ Orders PENDING (cho retry)
END
```

---

## DIA-10: Activity Diagram — Vendor Order Status Update
**Mục đích:** Minh họa luồng cập nhật trạng thái đơn hàng
**Vị trí báo cáo:** 3.4.3 Sơ đồ hoạt động
**Loại:** UML Activity Diagram

### Steps:
```
START
→ Vendor chọn đơn hàng
→ Hệ thống kiểm tra Shop ownership
  → NOT OWNER: Access Denied
  → IS OWNER: tiếp tục
→ Vendor chọn trạng thái mới
→ [Decision] Trạng thái hợp lệ theo State Machine?
  → INVALID: Thông báo lỗi
  → VALID: Cập nhật status
→ Lưu Order vào DB
→ Publish Semantic Domain Event
  → SHIPPED: OrderShippedEvent → Notification buyer
  → DELIVERED: OrderDeliveredEvent → Notification buyer
  → CANCELLED: OrderCancelledEvent → Notification buyer
→ Notification lưu vào DB (AFTER_COMMIT)
→ Buyer thấy thông báo (next polling 60s)
END
```

---

## DIA-11: State Diagram — Order Status
**Mục đích:** Minh họa vòng đời trạng thái đơn hàng
**Vị trí báo cáo:** 3.4.4 Sơ đồ trạng thái
**Loại:** UML State Diagram

### States & Transitions:
```
[INITIAL]
  → PENDING : Đơn vừa được tạo (sau checkout)

PENDING
  → CONFIRMED : VNPay IPN SUCCESS / COD tạo đơn
  → [PENDING giữ nguyên nếu Payment FAILED, cho retry]

CONFIRMED
  → SHIPPED : Vendor cập nhật (đang giao)
  → CANCELLED : Vendor/Admin hủy

SHIPPED
  → DELIVERED : Vendor xác nhận đã giao
  → CANCELLED : (edge case)

DELIVERED [FINAL]
CANCELLED [FINAL]
```

### Source code evidence:
- `OrderStatus.java` (enum)
- `OrderServiceImpl.updateVendorOrderStatus()` (state machine logic)
- `VendorOrderController.java` (`PATCH /api/v1/vendor/orders/{id}/status`)

---

## DIA-12: State Diagram — Payment Status
**Mục đích:** Minh họa vòng đời thanh toán
**Vị trí báo cáo:** 3.4.4 Sơ đồ trạng thái
**Loại:** UML State Diagram

### States & Transitions:
```
[INITIAL]
  → PENDING : Payment record được tạo

PENDING
  → SUCCESS : VNPay IPN xác nhận thành công / COD created
  → FAILED : VNPay IPN xác nhận thất bại

SUCCESS [FINAL]
FAILED [FINAL]
  → (User có thể retry → tạo Payment mới cho cùng payment_group_id)
```

### Source code evidence:
- `PaymentStatus.java` (enum)
- `PaymentGatewayService.processVnPayIpn()`

---

## DIA-13: State Diagram — Shop Status
**Mục đích:** Minh họa vòng đời gian hàng
**Vị trí báo cáo:** 3.4.4 Sơ đồ trạng thái
**Loại:** UML State Diagram

### States & Transitions:
```
[INITIAL]
  → PENDING : User tạo shop (chờ Admin duyệt)

PENDING
  → ACTIVE : Admin Approve (gửi email thông báo)
  → BANNED : (Chưa implement — ghi nhận là hạn chế)

ACTIVE [OPERATIONAL]
  → BANNED : (Planned — chưa có API)

BANNED [FINAL/SUSPENDED]
```

### Source code evidence:
- `ShopStatus.java` (enum: PENDING, ACTIVE, BANNED)
- `AdminShopController.approveShop()`
- `ShopNotificationListener.java` (email AFTER_COMMIT)

---

## DIA-14: ERD — Entity Relationship Diagram
**Mục đích:** Mô tả cơ sở dữ liệu đầy đủ
**Vị trí báo cáo:** 3.5.1 Lược đồ CSDL
**Loại:** ERD (Crow's Foot Notation)

### Tất cả bảng và quan hệ:

```
users (PK: id)
  ← user_roles (FK: user_id, FK: role_id) → roles (PK: id)

shops (PK: id, FK: user_id → users.id [UNIQUE])

categories (PK: id, FK: parent_id → categories.id [nullable, self-ref])

brands (PK: id)

products (PK: id, FK: shop_id → shops.id, FK: category_id → categories.id, FK: brand_id → brands.id [nullable])
  → product_variants (PK: id, FK: product_id → products.id)
  → product_images (PK: id, FK: product_id → products.id)

carts (PK: id, FK: user_id → users.id [UNIQUE])
  → cart_items (PK: id, FK: cart_id → carts.id, FK: product_variant_id → product_variants.id [LOOSE - just ID])

orders (PK: id, FK: user_id, FK: shop_id, FK: applied_voucher_id → vouchers.id [nullable])
  → order_items (PK: id, FK: order_id → orders.id, product_variant_id [LOOSE - just ID])

payments (PK: id, payment_group_id [NOT FK - links to orders.payment_group_id])

reviews (PK: id, FK: user_id, FK: product_id, FK: order_id [LOOSE - just ID])
  → review_images (FK: review_id → reviews.id, ElementCollection)

vouchers (PK: id, FK: shop_id → shops.id [nullable for SYSTEM scope])

notifications (PK: id, FK: user_id [LOOSE - just ID])

chat_rooms (PK: id, UNIQUE(buyer_id, shop_id), buyer_id and shop_id [LOOSE - just IDs])
  → chat_messages (PK: id, FK: room_id → chat_rooms.id, sender_id [LOOSE - just ID])
```

### Indexes cần highlight trong ERD:
- `orders`: idx_order_user_status_created, idx_order_shop_status_created, idx_order_payment_group
- `products`: idx_product_shop, idx_product_status
- `reviews`: idx_review_product_created
- `notifications`: idx_notif_user_created
- `payments`: idx_payment_group_created
- `chat_messages`: idx_chat_room_created

---

## TỔNG KẾT DIAGRAM

| ID | Tên | Loại | Vị trí báo cáo | Ưu tiên |
|---|---|---|---|---|
| DIA-01 | Architecture Diagram | Deployment | 3.2.1 | 🔴 CAO |
| DIA-02 | Module Dependency | Component | 3.2.2 | 🟠 TRUNG BÌNH |
| DIA-03 | Use Case Diagram | Use Case | 3.3.1 | 🔴 CAO |
| DIA-04 | Class Diagram — Entities | Class | 3.4.1 | 🔴 CAO |
| DIA-05 | Class Diagram — Services | Class | 3.4.1 | 🟡 THẤP |
| DIA-06 | Sequence — Checkout+Payment | Sequence | 3.4.2 | 🔴 CAO |
| DIA-07 | Sequence — Realtime Chat | Sequence | 3.4.2 | 🔴 CAO |
| DIA-08 | Sequence — Review + Notification | Sequence | 3.4.2 | 🟠 TRUNG BÌNH |
| DIA-09 | Activity — Checkout Flow | Activity | 3.4.3 | 🔴 CAO |
| DIA-10 | Activity — Vendor Status Update | Activity | 3.4.3 | 🟠 TRUNG BÌNH |
| DIA-11 | State — Order Status | State | 3.4.4 | 🔴 CAO |
| DIA-12 | State — Payment Status | State | 3.4.4 | 🟠 TRUNG BÌNH |
| DIA-13 | State — Shop Status | State | 3.4.4 | 🟡 THẤP |
| DIA-14 | ERD | ERD | 3.5.1 | 🔴 CAO |

**Tổng: 14 diagrams**
**Ưu tiên cao (bắt buộc): DIA-01, DIA-03, DIA-04, DIA-06, DIA-07, DIA-09, DIA-11, DIA-14**

## GỢI Ý TOOLS

| Tool | Ưu điểm | Nhược điểm |
|---|---|---|
| draw.io (diagrams.net) | Miễn phí, web-based, nhiều template | Kéo thả mất thời gian |
| PlantUML | Text-based, nhanh, version control | Cần học cú pháp |
| Mermaid | Tích hợp Markdown, GitHub render | Hạn chế tùy chỉnh |
| Lucidchart | Đẹp, professional | Có phí |
| StarUML | Desktop, full UML | Có phí |

**Đề xuất:** Dùng **draw.io** (diagrams.net) cho Architecture + ERD, **PlantUML/Mermaid** cho Sequence + State diagrams (nhanh hơn).
