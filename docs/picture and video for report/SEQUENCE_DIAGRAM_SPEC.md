# SEQUENCE DIAGRAM SPECIFICATION

> Tài liệu này mô tả chi tiết các sơ đồ tuần tự (Sequence Diagrams) cho các luồng nghiệp vụ cốt lõi của hệ thống.
> Toàn bộ các sơ đồ phản ánh **100% mã nguồn thực tế** và tuân thủ nghiêm ngặt nguyên tắc **Modular Monolith** (giao tiếp liên module thông qua Internal Service & DTO, không truy cập trực tiếp Entity/Repository của module khác).

---

## Danh mục sơ đồ tuần tự

| Mã SD | Tên luồng nghiệp vụ | Module liên quan | Giao thức / Phương thức |
|---|---|---|---|
| **SD01** | Đăng nhập hệ thống (Login) | identity | REST / POST |
| **SD02** | Thêm sản phẩm vào giỏ hàng (Add to Cart) | cart, product, shop | REST / POST |
| **SD03** | Đặt hàng nhiều Shop (Checkout) | order, cart, product, voucher, shop | REST / POST |
| **SD04** | Thanh toán VNPay & Xử lý IPN Webhook | payment, order, notification | REST / External Gateway |
| **SD05** | Vendor cập nhật trạng thái đơn hàng | order, shop, notification | REST / PATCH |
| **SD06** | Vendor đăng sản phẩm mới kèm biến thể | product, shop | REST / POST |
| **SD07** | Upload ảnh Cloudinary độc lập (Decoupled Media) | media, product | REST / Multipart |
| **SD08** | Nhắn tin Realtime 2 chiều (STOMP over WebSocket) | chat, shop | WebSocket / STOMP |
| **SD09** | Người mua xác nhận đã nhận hàng (Confirm Delivery) | order | REST / PATCH |
| **SD10** | Đánh giá sản phẩm sau khi nhận hàng (Review) | review, order, product | REST / POST |

---

## SD01 — Đăng nhập (Login)

**Mô tả:** Người dùng gửi thông tin đăng nhập (`email`, `password`). Spring Security `AuthenticationManager` xác thực mật khẩu qua BCrypt. Sau khi hợp lệ, `JwtService` sinh JWT Token từ `CustomUserDetails`. Frontend lưu token vào `localStorage` và tự trích xuất thông tin người dùng từ JWT payload.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend (React)
    participant AC as AuthController
    participant AS as AuthenticationService
    participant AM as AuthenticationManager
    participant UDS as UserDetailsServiceImpl
    participant UR as UserRepository
    participant JS as JwtService
    participant DB as MySQL Database

    FE->>AC: POST /api/v1/auth/login {email, password}
    AC->>AS: authenticate(UserLoginRequest)
    AS->>AM: authenticate(new UsernamePasswordAuthenticationToken(email, password))
    AM->>UDS: loadUserByUsername(email)
    UDS->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UDS: User entity with Roles
    UDS-->>AM: CustomUserDetails (user, authorities)
    AM->>AM: PasswordEncoder.matches(rawPassword, encodedHash)
    AM-->>AS: Authentication (success)

    AS->>UR: findByEmail(email)
    UR->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UR: User entity
    AS->>AS: new CustomUserDetails(user)
    AS->>JS: generateToken(userDetails)
    JS-->>AS: JWT Access Token (24h)
    AS-->>AC: AuthResponse {token}
    AC-->>FE: HTTP 200 {status: 200, message: "Đăng nhập thành công", data: {token}}

    Note over FE: localStorage.setItem("token", token)<br/>Decode JWT claims (userId, role) for UI state
```

---

## SD02 — Thêm sản phẩm vào giỏ hàng (Add to Cart)

**Mô tả:** Khách hàng thêm một biến thể sản phẩm vào giỏ. `CartService` gọi `ProductInternalService` để kiểm tra tồn kho, trạng thái `ACTIVE` và gọi `ShopInternalService` kiểm tra Shop không bị khóa. Giỏ hàng được khóa ghi (`findByUserIdForUpdate`) để xử lý an toàn đồng thời. `CartItem` được quản lý theo mô hình Aggregate Root trong `Cart`.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant Filter as JwtAuthenticationFilter
    participant CC as CartController
    participant CS as CartServiceImpl
    participant PIS as ProductInternalService
    participant SIS as ShopInternalService
    participant CR as CartRepository
    participant DB as MySQL Database

    FE->>Filter: POST /api/v1/carts/items {productVariantId, quantity}
    Note over Filter: Giải mã JWT → Thiết lập SecurityContext (userId)
    Filter->>CC: addToCart(@CurrentUserId Long userId, CartItemRequest)
    CC->>CS: addToCart(userId, request)

    Note over CS: @Transactional
    CS->>PIS: getVariantInfo(request.productVariantId)
    PIS->>DB: SELECT pv, p FROM product_variants pv JOIN products p WHERE pv.id = ?
    DB-->>PIS: ProductVariantInfoDto {id, productId, shopId, sku, price, stockQuantity, status}
    PIS-->>CS: ProductVariantInfoDto

    CS->>CS: Validate variant.status == 'ACTIVE' && stockQuantity >= request.quantity
    alt Sản phẩm hết hàng hoặc ngừng kinh doanh
        CS-->>CC: throw IllegalStateException("Số lượng tồn kho không đủ...")
        CC-->>FE: HTTP 400 Bad Request
    end

    CS->>SIS: requireNotBannedForSale(variantInfo.shopId)
    SIS->>DB: SELECT status FROM shops WHERE id = ?
    DB-->>SIS: Shop status
    Note over SIS: Validate shop.status != 'BANNED'

    CS->>CR: findByUserIdForUpdate(userId)
    CR->>DB: SELECT * FROM carts WHERE user_id = ? FOR UPDATE
    DB-->>CR: Cart entity (kèm List<CartItem>)

    alt Cart chưa tồn tại
        CS->>CR: save(new Cart(userId))
        CR->>DB: INSERT INTO carts (user_id) VALUES (?)
    end

    CS->>CS: Check existing CartItem trong cart.getItems()
    alt CartItem đã tồn tại trong giỏ
        CS->>CS: existingItem.setQuantity(existingItem.quantity + request.quantity)
        CS->>CS: Validate tổng số lượng <= variantInfo.stockQuantity
    else CartItem mới
        CS->>CS: cart.getItems().add(new CartItem(cart, variantId, quantity))
    end

    CS->>CR: save(cart)
    CR->>DB: UPDATE / INSERT cart_items (Cascade via Aggregate Root)
    Note over CS: COMMIT Transaction

    CS-->>CC: void
    CC-->>FE: HTTP 200 {status: 200, message: "Đã thêm sản phẩm vào giỏ hàng"}
```

---

## SD03 — Đặt hàng nhiều Shop (Checkout)

**Mô tả:** Khách hàng thanh toán giỏ hàng. `OrderServiceImpl` tương tác với các module qua Internal Service (`CartInternalService`, `ProductInternalService`, `VoucherInternalService`, `ShopInternalService`). Đơn hàng được tự động gom nhóm theo Shop (`shopId`), trừ tồn kho biến thể, tăng lượt dùng voucher và xóa các item đã mua khỏi giỏ hàng.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant OC as OrderController
    participant OS as OrderServiceImpl
    participant CIS as CartInternalService
    participant SIS as ShopInternalService
    participant PIS as ProductInternalService
    participant VIS as VoucherInternalService
    participant OR as OrderRepository
    participant DB as MySQL Database

    FE->>OC: POST /api/v1/orders/checkout {cartItemIds, shippingAddress, paymentMethod, voucherCode}
    OC->>OS: checkout(@CurrentUserId Long userId, CheckoutRequest)

    Note over OS: @Transactional BEGIN
    OS->>CIS: getCartItems(userId)
    CIS->>DB: SELECT * FROM cart_items ci JOIN carts c ON ci.cart_id = c.id WHERE c.user_id = ?
    DB-->>CIS: List<CartItemInternalDto>
    CIS-->>OS: List<CartItemInternalDto>

    OS->>OS: Lọc các items theo request.cartItemIds (hoặc lấy tất cả nếu rỗng)
    OS->>SIS: requireNotBannedForSale(shopId) [cho từng shop liên quan]

    opt Có voucherCode áp dụng
        OS->>VIS: calculateDiscount(new VoucherCalculationRequest(voucherCode, shopTotals))
        VIS->>DB: SELECT * FROM vouchers WHERE code = ? FOR UPDATE
        DB-->>VIS: Voucher entity
        VIS->>VIS: Validate điều kiện (minOrderValue, validity, usageLimit)
        VIS-->>OS: VoucherDiscountResult {voucherId, code, discountPerShop}
        OS->>VIS: incrementUsage(voucherDiscount.voucherId)
        VIS->>DB: UPDATE vouchers SET used_count = used_count + 1 WHERE id = ?
    end

    OS->>OS: Group CartItems theo shopId (N shop = N đơn hàng)
    OS->>OS: Sinh paymentGroupId chung = UUID.randomUUID()

    loop Cho từng nhóm Shop
        OS->>OS: Tạo Order {userId, shopId, paymentGroupId, status: PENDING, shippingAddress...}
        loop Cho từng item của Shop
            OS->>PIS: deductStock(variantId, quantity)
            PIS->>DB: UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?
            OS->>OS: order.addOrderItem(new OrderItem(...))
        end
        OS->>OS: order.setOriginalTotalAmount(), order.setDiscountAmount(), order.setTotalAmount()
        OS->>OR: save(order)
        OR->>DB: INSERT INTO orders; INSERT INTO order_items (Cascade)
    end

    OS->>CIS: removeCartItems(userId, checkedOutItemIds)
    CIS->>DB: DELETE FROM cart_items WHERE cart_id = ? AND id IN (?)

    Note over OS: COMMIT Transaction
    OS-->>OC: CheckoutResponse {paymentGroupId, orders: List<OrderResponse>}
    OC-->>FE: HTTP 200 {status: 200, message: "Đặt hàng thành công", data: CheckoutResponse}
```

---

## SD04 — Thanh toán VNPay & Xử lý IPN Webhook

**Mô tả:** Quy trình gồm 2 giai đoạn:
1. **Khởi tạo thanh toán:** Sinh mã giao dịch duy nhất `txnRef`, lưu record `Payment (PENDING)`, sinh URL thanh toán có chữ ký HMAC-SHA512 để redirect người dùng sang cổng VNPay.
2. **Xử lý IPN Webhook:** VNPay gọi Webhook ngầm `GET /api/v1/payments/vnpay/ipn`. `PaymentGatewayService` đối soát chữ ký, dùng Pessimistic Lock khóa record `Payment`, kiểm tra chống lặp giao dịch (Idempotency) và đối soát số tiền `vnp_Amount`. Khi hợp lệ, gọi `OrderInternalService` xác nhận đơn và bắn `OrderConfirmedEvent` để gửi thông báo.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend
    participant PC as PaymentController
    participant PGS as PaymentGatewayService
    participant OIS as OrderInternalService
    participant VNS as VnPayStrategy
    participant PR as PaymentRepository
    participant VNP as Cổng VNPay (External)
    participant EV as EventPublisher
    participant NL as OrderNotificationListener
    participant DB as MySQL Database

    %% === PHASE 1: TẠO URL THANH TOÁN ===
    rect rgb(240, 248, 255)
    Note over FE, PC: GIAI ĐOẠN 1: TẠO PAYMENT URL
    FE->>PC: POST /api/v1/payments/create {paymentGroupId, paymentMethod: "VNPAY"}
    PC->>OIS: validateGroupOwnershipAndStatus(paymentGroupId, userId)
    OIS->>DB: SELECT * FROM orders WHERE payment_group_id = ?
    Note over OIS: Kiểm tra userId sở hữu đơn & tất cả đơn đang PENDING
    PC->>OIS: getPaymentDataForGroup(paymentGroupId)
    OIS-->>PC: OrderPaymentDto {paymentGroupId, totalAmount, description, method}

    PC->>PGS: generatePaymentUrl(PaymentOrderData, clientIp)
    PGS->>PR: save(new Payment {paymentGroupId, amount, method: VNPAY, status: PENDING, transactionId: txnRef})
    PR->>DB: INSERT INTO payments
    PGS->>VNS: createPaymentUrl(orderData, clientIp, txnRef)
    VNS->>VNS: Xây dựng params + Ký chữ ký HMAC-SHA512
    VNS-->>PGS: paymentUrl
    PGS-->>PC: paymentUrl
    PC-->>FE: HTTP 200 {data: {paymentUrl: "https://sandbox.vnpayment.vn/..."}}

    FE->>VNP: Redirect trình duyệt sang VNPay thực hiện thanh toán
    end

    %% === PHASE 2: IPN CALLBACK ===
    rect rgb(245, 255, 245)
    Note over VNP, PC: GIAI ĐOẠN 2: XỬ LÝ IPN WEBHOOK TỪ VNPAY
    VNP->>PC: GET /api/v1/payments/vnpay/ipn?vnp_TxnRef=...&vnp_ResponseCode=00&vnp_Amount=...&vnp_SecureHash=...
    PC->>PGS: processVnPayIpn(queryParams)

    PGS->>VNS: verifyIpnSignature(queryParams)
    alt Chữ ký giả mạo / không hợp lệ
        PGS-->>PC: throw IllegalArgumentException("Chữ ký không hợp lệ")
        PC-->>VNP: HTTP 200 {RspCode: "97", Message: "Invalid Signature"}
    end

    PGS->>PR: findByTransactionIdForUpdate(txnRef)
    PR->>DB: SELECT * FROM payments WHERE transaction_id = ? FOR UPDATE
    DB-->>PR: Payment entity (Pessimistic Lock)

    alt Giao dịch đã xử lý trước đó (status != PENDING)
        PGS-->>PC: skip (Idempotent)
        PC-->>VNP: HTTP 200 {RspCode: "00", Message: "Confirm Success"}
    end

    PGS->>PGS: Đối soát số tiền (vnp_Amount == payment.amount * 100)

    alt vnp_ResponseCode == "00" (Thành công)
        PGS->>PGS: payment.setStatus(SUCCESS)
        PGS->>OIS: confirmPendingOrdersByGroup(paymentGroupId)
        OIS->>DB: SELECT * FROM orders WHERE payment_group_id = ? FOR UPDATE
        OIS->>DB: UPDATE orders SET status = 'CONFIRMED' WHERE payment_group_id = ?
        OIS->>EV: publishEvent(new OrderConfirmedEvent(orderId, userId, shopId, method))
        Note over OIS, EV: Giao dịch commit thành công
        EV->>NL: onOrderConfirmed(event) [AFTER_COMMIT]
        NL->>DB: INSERT INTO notifications (Gửi thông báo Buyer & Vendor)
    else vnp_ResponseCode != "00" (Thanh toán thất bại)
        PGS->>PGS: payment.setStatus(FAILED)
    end

    PGS->>PR: save(payment)
    PR->>DB: UPDATE payments SET status = ?, gateway_txn_id = ?
    PGS-->>PC: void
    PC-->>VNP: HTTP 200 {RspCode: "00", Message: "Confirm Success"}
    end

    %% === PHASE 3: CLIENT POLLING ===
    Note over FE, PC: Client kiểm tra trạng thái nhóm đơn
    FE->>PC: GET /api/v1/payments/group/{paymentGroupId}/status
    PC->>PGS: getPaymentGroupStatus(paymentGroupId)
    PGS-->>PC: PaymentGroupStatusResponse {paymentGroupId, status: "SUCCESS"}
    PC-->>FE: HTTP 200 {data: {status: "SUCCESS"}}
```

---

## SD05 — Vendor cập nhật trạng thái đơn hàng

**Mô tả:** Người bán (`VENDOR`) chuyển trạng thái đơn hàng (ví dụ: `CONFIRMED` $\rightarrow$ `SHIPPED`). Hệ thống xác thực quyền sở hữu Shop qua `ShopInternalService`, xác thực tính hợp lệ của máy trạng thái (`State Machine`), cập nhật trạng thái đơn và phát sự kiện `OrderShippedEvent` để gửi thông báo cho Buyer.

```mermaid
sequenceDiagram
    autonumber
    participant VFE as Vendor Frontend
    participant VOC as VendorOrderController
    participant OS as OrderServiceImpl
    participant SIS as ShopInternalService
    participant OR as OrderRepository
    participant EV as ApplicationEventPublisher
    participant NL as OrderNotificationListener
    participant DB as MySQL Database

    VFE->>VOC: PATCH /api/v1/vendor/orders/{id}/status {status: "SHIPPED", reason: "..."}
    Note over VOC: @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    VOC->>OS: updateVendorOrderStatus(@CurrentUserId Long userId, Long id, request)

    Note over OS: @Transactional BEGIN
    OS->>OR: findByIdForUpdate(orderId)
    OR->>DB: SELECT * FROM orders WHERE id = ? FOR UPDATE
    DB-->>OR: Order entity {shopId, status: CONFIRMED, userId: buyerId}

    OS->>SIS: isShopOwner(order.shopId, userId)
    SIS->>DB: SELECT user_id FROM shops WHERE id = ?
    DB-->>SIS: shopOwnerUserId
    SIS-->>OS: true (Chính chủ Shop)

    OS->>OS: validateVendorStateTransition(currentStatus, targetStatus)
    Note over OS: Hợp lệ: CONFIRMED → SHIPPED

    OS->>OS: order.setStatus(OrderStatus.SHIPPED)
    OS->>OR: save(order)
    OR->>DB: UPDATE orders SET status = 'SHIPPED', updated_at = NOW() WHERE id = ?

    OS->>EV: publishEvent(new OrderShippedEvent(orderId, buyerId, shopId))
    Note over OS: COMMIT Transaction

    EV->>NL: onOrderShipped(event) [AFTER_COMMIT]
    NL->>DB: INSERT INTO notifications {userId: buyerId, type: ORDER_SHIPPED, title: "Đơn hàng đang giao"}

    OS-->>VOC: OrderResponse {id, status: "SHIPPED", items, ...}
    VOC-->>VFE: HTTP 200 {status: 200, message: "Cập nhật trạng thái đơn hàng thành công", data: OrderResponse}
```

---

## SD06 — Thêm sản phẩm mới kèm biến thể (Vendor)

**Mô tả:** Người bán đăng sản phẩm mới. `ProductServiceImpl` xác thực danh mục `Category`, xác thực quyền sở hữu Shop qua `ShopInternalService` và đảm bảo Shop không bị cấm bán. Sản phẩm được lưu xuống DB, sau đó toàn bộ biến thể (`ProductVariant`) được tạo và lưu hàng loạt (`saveAll`).

```mermaid
sequenceDiagram
    autonumber
    participant VFE as Vendor Frontend
    participant VPC as VendorProductController
    participant PS as ProductServiceImpl
    participant SIS as ShopInternalService
    participant CR as CategoryRepository
    participant PR as ProductRepository
    participant PVR as ProductVariantRepository
    participant DB as MySQL Database

    VFE->>VPC: POST /api/v1/vendor/products {name, price, stockQuantity, categoryId, images: [...], variants: [...]}
    Note over VPC: @PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")
    VPC->>PS: createProduct(@CurrentUserId Long userId, ProductCreateRequest)

    Note over PS: @Transactional BEGIN
    PS->>CR: findById(request.categoryId)
    CR->>DB: SELECT * FROM categories WHERE id = ?
    DB-->>CR: Category entity

    PS->>SIS: getShopIdByUserId(userId)
    SIS->>DB: SELECT id FROM shops WHERE user_id = ?
    DB-->>SIS: shopId
    SIS-->>PS: shopId

    PS->>SIS: requireNotBannedForSale(shopId)
    SIS->>DB: SELECT status FROM shops WHERE id = ?
    DB-->>SIS: ACTIVE

    PS->>PS: Build Product entity (shopId, name, price, stockQuantity, category, status: ACTIVE)
    opt Có danh sách ảnh truyền lên
        PS->>PS: Map request.images -> List<ProductImage> (gán product, normalize thumbnail & sortOrder)
    end

    PS->>PR: save(product)
    PR->>DB: INSERT INTO products; INSERT INTO product_images
    DB-->>PR: savedProduct (có generated id)

    opt Có danh sách biến thể truyền lên
        PS->>PS: Map request.variants -> List<ProductVariant> (gán savedProduct, status: ACTIVE)
        PS->>PVR: saveAll(variantsToSave)
        PVR->>DB: Batch INSERT INTO product_variants
    end

    Note over PS: COMMIT Transaction
    PS-->>VPC: ProductResponse (id, name, price, images, variants...)
    VPC-->>VFE: HTTP 201 Created {status: 201, message: "Đăng sản phẩm thành công", data: ProductResponse}
```

---

## SD07 — Upload ảnh Cloudinary độc lập (Decoupled Media)

**Mô tả:** Kiến trúc upload ảnh của hệ thống được tách biệt 2 bước (Decoupled):
1. **Upload ảnh tĩnh:** Client gửi file Multipart lên `MediaController`. `FileStorageService` đẩy ảnh trực tiếp lên Cloudinary CDN và nhận về URL an toàn cùng `publicId`.
2. **Lưu trữ nghiệp vụ:** Client sử dụng `imageUrl` và `publicId` này để nhúng vào payload tạo sản phẩm (`ProductCreateRequest`), lưu avatar hoặc đánh giá.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Frontend Client
    participant MC as MediaController
    participant FSS as FileStorageService
    participant CLD as Cloudinary API (External)
    participant VPC as VendorProductController / Service
    participant DB as MySQL Database

    %% === BƯỚC 1: UPLOAD ẢNH TĨNH ===
    rect rgb(240, 248, 255)
    Note over FE, CLD: BƯỚC 1: UPLOAD FILE MULTIPART LÊN CLOUDINARY
    FE->>MC: POST /api/v1/media/upload (multipart/form-data: file)
    Note over MC: @PreAuthorize("isAuthenticated()")<br/>Validate file: not empty, contentType.startsWith("image/")

    MC->>FSS: uploadImage(MultipartFile file)
    FSS->>CLD: uploader().upload(file.getBytes(), ObjectUtils.emptyMap())
    CLD-->>FSS: Map result {secure_url, public_id, bytes, format}
    FSS-->>MC: FileStorageResponse(secure_url, public_id)
    MC-->>FE: HTTP 200 {status: 200, message: "Upload thành công", data: {imageUrl, publicId}}
    end

    %% === BƯỚC 2: SỬ DỤNG URL CHO NGHIỆP VỤ ===
    rect rgb(245, 255, 245)
    Note over FE, DB: BƯỚC 2: NHÚNG URL VÀO FORM SẢN PHẨM
    FE->>VPC: POST /api/v1/vendor/products {..., images: [{imageUrl, publicId, isThumbnail: true, sortOrder: 0}]}
    VPC->>DB: INSERT INTO product_images (product_id, image_url, public_id, is_thumbnail, sort_order)
    VPC-->>FE: HTTP 201 Created (Sản phẩm hiển thị ảnh đầy đủ)
    end
```

---

## SD08 — Nhắn tin Realtime 2 chiều (STOMP over WebSocket)

**Mô tả:** Luồng trao đổi tin nhắn thời gian thực giữa Khách mua (`Buyer`) và Chủ shop (`Vendor`):
1. **Xác thực kết nối:** Khách kết nối WebSocket qua SockJS (`/ws`). `JwtChannelInterceptor` chặn frame STOMP `CONNECT`, trích xuất `userId` từ Bearer Token để gán `StompPrincipal`.
2. **Gửi & Chuyển phát tin nhắn:** Client gửi frame STOMP đến `/app/chat.send`. `ChatMessageController` chuyển tiếp sang `ChatServiceImpl`. Dịch vụ lưu tin nhắn vào MySQL và trực tiếp dùng `SimpMessagingTemplate` để đẩy tin nhắn qua destination riêng `/user/queue/messages` đến **cả người nhận và người gửi** (đảm bảo đồng bộ đa thiết bị).

```mermaid
sequenceDiagram
    autonumber
    participant BFE as Buyer Frontend
    participant VFE as Vendor Frontend
    participant WS as WebSocket Endpoint (/ws)
    participant JCI as JwtChannelInterceptor
    participant CMC as ChatMessageController
    participant CS as ChatServiceImpl
    participant SIS as ShopInternalService
    participant CMR as ChatMessageRepository
    participant SMT as SimpMessagingTemplate
    participant DB as MySQL Database

    %% === KẾT NỐI VÀ SUBSCRIBE ===
    rect rgb(240, 248, 255)
    Note over BFE, WS: THIẾT LẬP KẾT NỐI WEBSOCKET & XÁC THỰC JWT
    BFE->>WS: HTTP GET /ws (SockJS Handshake)
    WS-->>BFE: HTTP 101 Switching Protocols
    BFE->>WS: STOMP CONNECT {Authorization: "Bearer <jwt>"}
    WS->>JCI: preSend(message, channel)
    JCI->>JCI: jwtService.extractUserId(token)
    JCI-->>WS: accessor.setUser(new StompPrincipal(userId))
    BFE->>WS: STOMP SUBSCRIBE /user/queue/messages
    VFE->>WS: STOMP SUBSCRIBE /user/queue/messages
    end

    %% === GỬI VÀ PHÁT TIN NHẮN ===
    rect rgb(245, 255, 245)
    Note over BFE, SMT: GỬI TIN NHẮN REALTIME
    BFE->>WS: STOMP SEND /app/chat.send {roomId: 10, content: "Shop ơi tư vấn giúp mình!"}
    WS->>CMC: @MessageMapping("/chat.send") sendMessage(ChatMessageRequest, Principal)
    CMC->>CS: sendMessage(request, senderId)

    Note over CS: @Transactional BEGIN
    CS->>DB: SELECT * FROM chat_rooms WHERE id = ?
    DB-->>CS: ChatRoom entity {id: 10, buyerId: 5, shopId: 2}
    CS->>SIS: getUserIdByShopId(room.shopId)
    SIS-->>CS: vendorUserId (ID: 99)
    CS->>CS: validateAccess (senderId == buyerId || senderId == vendorUserId)

    CS->>CMR: save(new ChatMessage {roomId: 10, senderId: 5, content: "..."})
    CMR->>DB: INSERT INTO chat_messages (room_id, sender_id, content, created_at)
    DB-->>CMR: savedMessage with ID

    CS->>DB: UPDATE chat_rooms SET updated_at = NOW() WHERE id = 10
    CS->>CS: Map savedMessage -> ChatMessageResponse

    %% Bắn tin nhắn qua SimpMessagingTemplate
    CS->>SMT: convertAndSendToUser("99", "/queue/messages", response)
    SMT-->>VFE: STOMP MESSAGE /user/queue/messages {id: 101, senderId: 5, content: "..."}

    CS->>SMT: convertAndSendToUser("5", "/queue/messages", response)
    SMT-->>BFE: STOMP MESSAGE /user/queue/messages {id: 101 (real), senderId: 5, content: "..."}
    Note over CS: COMMIT Transaction
    end
```

---

## SD09 — Người mua xác nhận đã nhận hàng (Confirm Delivery)

**Mô tả:** Khi đơn hàng ở trạng thái `SHIPPED`, người mua nhấn "Đã nhận hàng" trên giao diện. `OrderServiceImpl` kiểm tra quyền sở hữu, khóa đơn (`findByIdForUpdate`), chuyển trạng thái sang `DELIVERED`, lưu thời gian giao hàng `deliveredAt` và ghi nhận người xác nhận là `DeliveryConfirmationSource.BUYER`.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Buyer Frontend
    participant OC as OrderController
    participant OS as OrderServiceImpl
    participant OR as OrderRepository
    participant DB as MySQL Database

    FE->>OC: PATCH /api/v1/orders/{id}/confirm-delivery
    Note over OC: @PreAuthorize("isAuthenticated()")
    OC->>OS: confirmDelivery(@CurrentUserId Long userId, Long id)

    Note over OS: @Transactional BEGIN
    OS->>OR: findByIdForUpdate(orderId)
    OR->>DB: SELECT * FROM orders WHERE id = ? FOR UPDATE
    DB-->>OR: Order entity {userId, status, deliveredAt, ...}

    OS->>OS: Validate order.userId == userId
    alt Đơn hàng không thuộc về User
        OS-->>OC: throw AppException(ORDER_NOT_FOUND)
        OC-->>FE: HTTP 404 Not Found
    end

    OS->>OS: Validate order.status == OrderStatus.SHIPPED
    alt Đơn hàng chưa ở trạng thái SHIPPED
        OS-->>OC: throw AppException(INVALID_ORDER_STATE_TRANSITION)
        OC-->>FE: HTTP 400 Bad Request
    end

    OS->>OS: order.setStatus(OrderStatus.DELIVERED)
    OS->>OS: order.setDeliveredAt(LocalDateTime.now())
    OS->>OS: order.setDeliveryConfirmedByUserId(userId)
    OS->>OS: order.setDeliveryConfirmationSource(DeliveryConfirmationSource.BUYER)

    OS->>OR: save(order)
    OR->>DB: UPDATE orders SET status = 'DELIVERED', delivered_at = ?, delivery_confirmation_source = 'BUYER' WHERE id = ?

    Note over OS: COMMIT Transaction
    OS-->>OC: OrderResponse {id, status: "DELIVERED", deliveredAt, ...}
    OC-->>FE: HTTP 200 {status: 200, message: "Xác nhận đã nhận hàng thành công", data: OrderResponse}
    Note over FE: Cập nhật giao diện: Mở khóa nút "Đánh giá sản phẩm"
```

---

## SD10 — Đánh giá sản phẩm sau khi nhận hàng (Review)

**Mô tả:** Khách hàng đánh giá sản phẩm sau khi đơn hàng hoàn tất. `ReviewServiceImpl` giao tiếp với `OrderInternalService` để kiểm tra đơn hàng đã giao thành công và thuộc về khách hàng. Sau đó kiểm tra tính hợp lệ của sản phẩm trong đơn qua `ProductInternalService`, lưu đánh giá vào DB và bắn `ReviewCreatedEvent` để cập nhật thống kê/gửi thông báo.

```mermaid
sequenceDiagram
    autonumber
    participant FE as Buyer Frontend
    participant RC as ReviewController
    participant RS as ReviewServiceImpl
    participant OIS as OrderInternalService
    participant PIS as ProductInternalService
    participant RR as ReviewRepository
    participant EV as ApplicationEventPublisher
    participant DB as MySQL Database

    FE->>RC: POST /api/v1/reviews {productId: 10, orderId: 50, rating: 5, comment: "Rất tốt!", imageUrls: [...]}
    Note over RC: @PreAuthorize("hasRole('USER')")
    RC->>RS: createReview(@CurrentUserId Long userId, ReviewCreateRequest)

    Note over RS: @Transactional BEGIN
    RS->>OIS: isOrderDeliveredAndBelongsToUser(orderId, userId)
    OIS->>DB: SELECT status, user_id FROM orders WHERE id = ?
    DB-->>OIS: Order {status: DELIVERED, userId: 5}
    OIS-->>RS: true (Đơn đã giao và chính chủ)

    RS->>OIS: getProductVariantIdsByOrderId(orderId)
    OIS->>DB: SELECT product_variant_id FROM order_items WHERE order_id = ?
    DB-->>OIS: List<Long> variantIds
    OIS-->>RS: List<Long> variantIds

    RS->>PIS: getProductIdByVariantId(variantId)
    PIS->>DB: SELECT product_id FROM product_variants WHERE id = ?
    DB-->>PIS: productId
    PIS-->>RS: productId (Khớp với request.productId)

    RS->>RR: existsByUserIdAndOrderIdAndProductId(userId, orderId, productId)
    RR->>DB: SELECT count(*) FROM reviews WHERE user_id = ? AND order_id = ? AND product_id = ?
    DB-->>RR: false (Chưa từng đánh giá)

    RS->>RS: Build Review entity (userId, productId, orderId, rating, comment, imageUrls)
    RS->>RR: save(review)
    RR->>DB: INSERT INTO reviews (user_id, product_id, order_id, rating, comment, image_urls, created_at)
    DB-->>RR: savedReview with ID

    RS->>PIS: getShopIdByProductId(productId)
    PIS-->>RS: shopId
    RS->>EV: publishEvent(new ReviewCreatedEvent(reviewId, userId, shopId, productId, rating))

    Note over RS: COMMIT Transaction
    RS-->>RC: ReviewResponse {id, rating, comment, userName, createdAt, ...}
    RC-->>FE: HTTP 201 Created {status: 201, message: "Đánh giá sản phẩm thành công", data: ReviewResponse}
```
