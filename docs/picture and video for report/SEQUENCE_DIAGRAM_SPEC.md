# SEQUENCE DIAGRAM SPECIFICATION

> Tài liệu này mô tả các sơ đồ tuần tự cho các luồng nghiệp vụ cốt lõi.
> Tất cả đều phản ánh implementation thực tế trong source code.

---

## SD01 — Đăng nhập (Login)

**Luồng:** Frontend → AuthController → AuthenticationService → UserDetailsServiceImpl → JwtService → DB

```mermaid
sequenceDiagram
    participant FE as Frontend (React)
    participant AC as AuthController
    participant AS as AuthenticationService
    participant AM as AuthenticationManager
    participant UDS as UserDetailsServiceImpl
    participant JS as JwtService
    participant DB as MySQL

    FE->>AC: POST /api/v1/auth/login {email, password}
    AC->>AS: authenticate(UserLoginRequest)
    AS->>AM: authenticate(UsernamePasswordAuthenticationToken)
    AM->>UDS: loadUserByUsername(email)
    UDS->>DB: SELECT * FROM users WHERE email = ?
    DB-->>UDS: User entity
    UDS-->>AM: UserDetails (userId, email, roles)
    AM->>AM: BCrypt.matches(rawPwd, hash)
    AM-->>AS: Authentication (success)
    AS->>JS: generateToken(userId, roles)
    JS-->>AS: JWT token (24h)
    AS-->>AC: AuthResponse {token, userId, email, roles}
    AC-->>FE: HTTP 200 {token, userId, email, roles}

    Note over FE: localStorage.setItem("token", jwt)
```

---

## SD02 — Thêm sản phẩm vào giỏ hàng (Add to Cart)

**Luồng:** Frontend → CartController → CartService → ProductInternalService → CartRepository → DB

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant Filter as JwtAuthenticationFilter
    participant CC as CartController
    participant CS as CartService
    participant PIS as ProductInternalServiceImpl
    participant CR as CartRepository
    participant CIR as CartItemRepository
    participant DB as MySQL

    FE->>Filter: POST /api/v1/carts/items {productVariantId, quantity}
    Note over Filter: Validate JWT → set SecurityContext (userId)
    Filter->>CC: Request with @CurrentUserId injected
    CC->>CS: addToCart(userId, CartItemRequest)
    CS->>PIS: getProductVariant(productVariantId)
    PIS->>DB: SELECT * FROM product_variants WHERE id = ?
    DB-->>PIS: ProductInternalData {price, stockQty, shopId}
    PIS-->>CS: ProductInternalData

    CS->>CS: Check stockQuantity >= request.quantity
    alt Hết hàng
        CS-->>CC: throw InsufficientStockException
        CC-->>FE: HTTP 400
    end

    CS->>CR: findByUserId(userId)
    CR->>DB: SELECT * FROM carts WHERE user_id = ?
    DB-->>CR: Cart (or empty)

    alt Cart chưa tồn tại
        CS->>CR: save(new Cart {userId})
        CR->>DB: INSERT INTO carts
    end

    CS->>CIR: findByCartIdAndProductVariantId(cartId, variantId)
    DB-->>CIR: CartItem or null

    alt CartItem đã tồn tại
        CS->>CIR: save(item.quantity += request.quantity)
        CIR->>DB: UPDATE cart_items SET quantity = ?
    else CartItem chưa tồn tại
        CS->>CIR: save(new CartItem {cartId, variantId, quantity})
        CIR->>DB: INSERT INTO cart_items
    end

    CS-->>CC: void
    CC-->>FE: HTTP 200 {message: "Đã thêm sản phẩm vào giỏ hàng"}
```

---

## SD03 — Đặt hàng (Checkout)

**Luồng:** Frontend → OrderController → OrderService → ProductInternalService + VoucherInternalService → OrderRepository → DB

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant OC as OrderController
    participant OS as OrderService
    participant PIS as ProductInternalService
    participant VIS as VoucherInternalService
    participant OR as OrderRepository
    participant PR as ProductVariantRepo
    participant DB as MySQL

    FE->>OC: POST /api/v1/orders/checkout {cartItemIds, shippingAddress, paymentMethod, voucherCodes}
    OC->>OS: checkout(userId, CheckoutRequest)

    Note over OS: @Transactional BEGIN

    loop For each cartItemId
        OS->>PIS: getProductVariant(variantId)
        PIS->>DB: SELECT * FROM product_variants WHERE id = ?
        DB-->>PIS: variant {price, stockQty, shopId}
        PIS-->>OS: ProductInternalData

        OS->>OS: Check stockQty >= qty
        alt Hết hàng
            OS-->>OC: throw InsufficientStockException
            Note over OS: ROLLBACK
            OC-->>FE: HTTP 400
        end
    end

    OS->>VIS: calculateDiscount(VoucherCalculationRequest)
    VIS->>DB: SELECT * FROM vouchers WHERE code IN (?) FOR UPDATE
    DB-->>VIS: Voucher entities
    VIS->>VIS: Validate (scope, dates, usageLimit, minOrderValue)
    VIS-->>OS: VoucherDiscountResult {discountByShop}

    OS->>OS: Group CartItems by shopId → N orders

    loop For each shop group
        OS->>OR: save(new Order {userId, shopId, paymentGroupId, status=PENDING, items...})
        OR->>DB: INSERT INTO orders; INSERT INTO order_items
    end

    loop For each variant
        OS->>PR: decrementStock(variantId, qty)
        PR->>DB: UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ?
    end

    OS->>DB: UPDATE vouchers SET used_count = used_count + 1 WHERE code = ?
    OS->>DB: DELETE FROM cart_items WHERE id IN (?)

    Note over OS: COMMIT

    OS-->>OC: CheckoutResponse {paymentGroupId, orderIds, totalAmount}
    OC-->>FE: HTTP 201 {paymentGroupId, orderIds, totalAmount}
```

---

## SD04 — Thanh toán VNPay (Payment Flow)

**Luồng 2 giai đoạn:** (1) Tạo URL → (2) VNPay IPN callback

```mermaid
sequenceDiagram
    participant FE as Frontend
    participant PC as PaymentController
    participant OIS as OrderInternalService
    participant PGS as PaymentGatewayService
    participant VNS as VnPayStrategy
    participant VNP as VNPay (External)
    participant PR as PaymentRepository
    participant OR as OrderRepository
    participant EV as ApplicationEventPublisher
    participant NL as OrderNotificationListener
    participant DB as MySQL

    %% === PHASE 1: TẠO PAYMENT URL ===
    FE->>PC: POST /api/v1/payments/create {paymentGroupId, paymentMethod: VNPAY}
    PC->>OIS: validateGroupOwnershipAndStatus(paymentGroupId, userId)
    OIS->>DB: SELECT orders WHERE payment_group_id = ? AND user_id = ?
    DB-->>OIS: Orders (status=PENDING)

    PC->>OIS: getPaymentDataForGroup(paymentGroupId)
    OIS->>DB: SELECT SUM(total_amount) FROM orders WHERE payment_group_id = ?
    DB-->>OIS: OrderPaymentDto {amount, description}

    PC->>PGS: generatePaymentUrl(paymentData, clientIp)
    PGS->>VNS: createPaymentUrl(...)
    VNS->>VNS: Build params + HMAC-SHA512 sign
    VNS-->>PGS: paymentUrl

    PGS->>PR: save(new Payment {paymentGroupId, amount, status=PENDING, transactionId})
    PR->>DB: INSERT INTO payments

    PGS-->>PC: paymentUrl
    PC-->>FE: HTTP 200 {paymentUrl}

    FE->>VNP: redirect browser to paymentUrl
    Note over VNP: Người dùng hoàn tất thanh toán

    %% === PHASE 2: IPN CALLBACK ===
    VNP->>PC: GET /api/v1/payments/vnpay/ipn?vnp_ResponseCode=00&vnp_TxnRef=...&vnp_SecureHash=...
    PC->>PGS: processVnPayIpn(queryParams)

    PGS->>PGS: VnPayHashUtil.validateHash(params, secretKey)
    alt HMAC invalid
        PGS-->>PC: throw InvalidSignatureException
        PC-->>VNP: {RspCode: "97", Message: "Invalid Signature"}
    end

    PGS->>PR: findByTransactionId(txnRef) FOR UPDATE
    PR->>DB: SELECT * FROM payments WHERE transaction_id = ? FOR UPDATE

    Note over PGS: Pessimistic Lock — chống race condition

    PGS->>PGS: Check payment.status == PENDING (idempotency)
    alt Payment đã xử lý
        PGS-->>PC: (skip)
        PC-->>VNP: {RspCode: "00"}
    end

    alt vnp_ResponseCode == "00"
        PGS->>PR: update payment.status = SUCCESS
        PR->>DB: UPDATE payments SET status = 'SUCCESS'
        PGS->>OR: updateOrdersStatus(paymentGroupId, CONFIRMED)
        OR->>DB: UPDATE orders SET status = 'CONFIRMED' WHERE payment_group_id = ?
        PGS->>EV: publish(OrderConfirmedEvent)
        Note over PGS: COMMIT transaction
        PGS->>NL: onOrderConfirmed() [AFTER_COMMIT, REQUIRES_NEW]
        NL->>DB: INSERT INTO notifications (for Buyer and Vendor)
    else vnp_ResponseCode != "00"
        PGS->>PR: update payment.status = FAILED
        PR->>DB: UPDATE payments SET status = 'FAILED'
        Note over PGS: COMMIT
    end

    PGS-->>PC: (success)
    PC-->>VNP: {RspCode: "00", Message: "Confirm Success"}

    Note over FE: Poll GET /api/v1/payments/group/{id}/status
    FE->>PC: GET /api/v1/payments/group/{paymentGroupId}/status
    PC-->>FE: {status: "SUCCESS", orders: [...]}
```

---

## SD05 — Vendor cập nhật trạng thái đơn hàng

**Luồng:** VendorOrderController → OrderService → ShopInternalService → State Machine → EventPublisher → NotificationListener

```mermaid
sequenceDiagram
    participant VFE as Vendor Frontend
    participant VOC as VendorOrderController
    participant OS as OrderService
    participant SIS as ShopInternalService
    participant OR as OrderRepository
    participant EV as EventPublisher
    participant NL as OrderNotificationListener
    participant DB as MySQL

    VFE->>VOC: PATCH /api/v1/vendor/orders/{id}/status {newStatus: "SHIPPED"}
    Note over VOC: @PreAuthorize("hasAnyRole('VENDOR','ADMIN')")

    VOC->>OS: updateVendorOrderStatus(userId, orderId, request)
    OS->>OR: findById(orderId)
    OR->>DB: SELECT * FROM orders WHERE id = ?
    DB-->>OR: Order {shopId, status: CONFIRMED}

    OS->>SIS: isShopOwner(userId, order.shopId)
    SIS->>DB: SELECT id FROM shops WHERE user_id = ? AND id = ?
    DB-->>SIS: shopId (found)
    SIS-->>OS: true

    OS->>OS: validateStateTransition(CONFIRMED → SHIPPED)
    Note over OS: State Machine: CONFIRMED→SHIPPED OK

    OS->>OR: update order.status = SHIPPED
    OR->>DB: UPDATE orders SET status = 'SHIPPED' WHERE id = ?

    OS->>EV: publish(OrderShippedEvent {orderId, buyerId})

    Note over OS: COMMIT transaction

    EV->>NL: onOrderShipped() [AFTER_COMMIT, REQUIRES_NEW]
    NL->>DB: INSERT INTO notifications {userId=buyerId, type=ORDER_SHIPPED, title="Đơn hàng đang giao"}

    OS-->>VOC: OrderResponse {id, status: SHIPPED, ...}
    VOC-->>VFE: HTTP 200 {status: SHIPPED}
```

---

## SD06 — Thêm sản phẩm (Vendor)

**Luồng:** VendorProductController → ProductService → ShopInternalService → ProductRepository → DB

```mermaid
sequenceDiagram
    participant VFE as Vendor Frontend
    participant VPC as VendorProductController
    participant PS as ProductService
    participant SIS as ShopInternalService
    participant PR as ProductRepository
    participant DB as MySQL

    VFE->>VPC: POST /api/v1/vendor/products {name, price, categoryId, variants: [...]}
    Note over VPC: @PreAuthorize("hasAnyRole('VENDOR','ADMIN')")

    VPC->>PS: createProduct(userId, ProductCreateRequest)

    PS->>SIS: getShopIdByUserId(userId)
    SIS->>DB: SELECT id FROM shops WHERE user_id = ? AND status = 'ACTIVE'
    DB-->>SIS: shopId
    SIS-->>PS: shopId

    PS->>DB: SELECT * FROM categories WHERE id = ?
    DB-->>PS: Category entity

    PS->>DB: SELECT * FROM brands WHERE id = ? (if brandId provided)
    DB-->>PS: Brand entity

    PS->>PR: save(new Product {shopId, name, price, category, brand, status=ACTIVE})
    PR->>DB: INSERT INTO products
    DB-->>PR: productId

    loop For each variant in request
        PS->>DB: INSERT INTO product_variants {productId, sku, price, stockQty, attributes}
    end

    PS-->>VPC: ProductResponse {id, name, shopId, variants, ...}
    VPC-->>VFE: HTTP 201 {product}
```

---

## SD07 — Upload ảnh sản phẩm

**Luồng:** VendorProductController → ProductService → FileStorageService → Cloudinary (External) → DB

```mermaid
sequenceDiagram
    participant VFE as Vendor Frontend
    participant VPC as VendorProductController
    participant PS as ProductService
    participant FSS as FileStorageService
    participant CLD as Cloudinary API
    participant PIR as ProductImageRepository
    participant DB as MySQL

    VFE->>VPC: POST /api/v1/vendor/products/{id}/images (multipart file)

    VPC->>PS: uploadProductImage(userId, productId, file)
    PS->>DB: SELECT * FROM products WHERE id = ? AND shop_id = ?
    DB-->>PS: Product entity (ownership verified)

    PS->>FSS: upload(MultipartFile)
    FSS->>CLD: POST /image/upload (Cloudinary SDK)
    CLD-->>FSS: {secure_url, public_id}

    Note over PS: @Transactional
    PS->>PIR: save(new ProductImage {productId, imageUrl, publicId, isThumbnail, sortOrder})
    PIR->>DB: INSERT INTO product_images

    Note over PS: COMMIT

    PS-->>VPC: ProductImageResponse {imageId, imageUrl, publicId}
    VPC-->>VFE: HTTP 201 {imageId, imageUrl}
```

---

## SD08 — Chat realtime (WebSocket STOMP)

**Luồng:** Frontend WebSocket → ChatMessageController (STOMP) → ChatService → DB → messagingTemplate → Frontend

```mermaid
sequenceDiagram
    participant BFE as Buyer Frontend
    participant VFE as Vendor Frontend
    participant WS as WebSocket Endpoint (/ws)
    participant JCI as JwtChannelInterceptor
    participant CMC as ChatMessageController (STOMP)
    participant CS as ChatService
    participant CMR as ChatMessageRepository
    participant MT as SimpMessagingTemplate
    participant DB as MySQL

    %% === CONNECT ===
    BFE->>WS: HTTP GET /ws (SockJS Upgrade)
    WS-->>BFE: 101 Switching Protocols
    BFE->>WS: STOMP CONNECT {Authorization: Bearer <token>}
    WS->>JCI: preSend(CONNECT frame)
    JCI->>JCI: JwtService.extractUserId(token)
    JCI-->>WS: StompPrincipal(buyerId)
    BFE->>WS: SUBSCRIBE /user/queue/messages
    VFE->>WS: SUBSCRIBE /user/queue/messages

    %% === SEND MESSAGE ===
    BFE->>WS: STOMP SEND /app/chat.send {roomId: 5, content: "Xin chào!"}
    WS->>CMC: @MessageMapping("/chat.send") handleMessage(ChatMessageRequest, Principal)

    CMC->>CS: sendMessage(ChatMessageRequest, senderId)
    CS->>DB: SELECT * FROM chat_rooms WHERE id = ? AND (buyer_id = ? OR shop_id IN (...))
    DB-->>CS: ChatRoom {buyerId, shopId}

    CS->>CMR: save(new ChatMessage {roomId, senderId, content, isRead=false})
    CMR->>DB: INSERT INTO chat_messages
    DB-->>CMR: ChatMessage with id

    CS->>CS: Determine shopOwnerId from shopId
    CS-->>CMC: ChatMessageResponse {id, roomId, senderId, content, createdAt}

    %% === PUSH TO BOTH PARTIES ===
    CMC->>MT: convertAndSendToUser(shopOwnerId, "/queue/messages", response)
    MT-->>VFE: STOMP MESSAGE {id, content, senderId, createdAt}

    CMC->>MT: convertAndSendToUser(buyerId, "/queue/messages", response)
    MT-->>BFE: STOMP MESSAGE {id (real), content, senderId, createdAt}

    Note over BFE: Replace optimistic temp message (negative id) with real message
```

---

## Tóm tắt sequence diagrams

| SD | Tên | Actors | External System |
|---|---|---|---|
| SD01 | Đăng nhập | Frontend, AuthController, AuthService, JwtService, DB | — |
| SD02 | Thêm vào giỏ | Frontend, CartController, CartService, ProductInternalService, DB | — |
| SD03 | Checkout | Frontend, OrderController, OrderService, ProductInternalService, VoucherInternalService, DB | — |
| SD04 | Thanh toán VNPay | Frontend, PaymentController, OrderInternalService, PaymentGatewayService, VNPay, DB | VNPay |
| SD05 | Cập nhật đơn (Vendor) | VendorFE, VendorOrderController, OrderService, ShopInternalService, EventPublisher, DB | — |
| SD06 | Thêm sản phẩm (Vendor) | VendorFE, VendorProductController, ProductService, ShopInternalService, DB | — |
| SD07 | Upload ảnh | VendorFE, VendorProductController, ProductService, FileStorageService, Cloudinary, DB | Cloudinary |
| SD08 | Chat realtime | BuyerFE, VendorFE, WebSocket, JwtInterceptor, ChatMessageController, ChatService, DB | — |
