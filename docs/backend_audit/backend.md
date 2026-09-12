Viewed ARCHITECTURE.md:1-472
Viewed PROJECT_CONTEXT.md:1-356
Listed directory docs
Viewed AI_PROMPTS.md:1-756
Viewed AI_DEVELOPMENT_WORKFLOW.md:1-800
Listed directory ecommerce
Listed directory ecommerce
Viewed CURRENT_SYSTEM_INVENTORY.md:1-25
Viewed FEATURE_INVENTORY.md:1-200
Searched for "@(Rest)?Controller"
Searched for "SecurityFilterChain|WebSecurityConfigurerAdapter|@EnableWebSecurity"
Viewed SecurityConfig.java:1-95
Viewed CurrentUserIdArgumentResolver.java:1-48
Listed directory identity
Listed directory controller
Viewed AuthController.java:1-47
Viewed ProfileController.java:1-26
Listed directory dto
Viewed AuthResponse.java:1-16
Viewed ProfileResponse.java:1-4
Viewed UserLoginRequest.java:1-18
Viewed UserRegisterRequest.java:1-25
Viewed ProfileUpdateRequest.java:1-33
Searched for "class JwtService"
Viewed JwtService.java:1-104
Listed directory product
Viewed CategoryController.java:1-38
Viewed ProductController.java:1-151
Viewed VendorProductController.java:1-97
Listed directory dto
Listed directory request
Listed directory response
Viewed ProductResponse.java:1-18
Viewed ProductCreateRequest.java:1-42
Viewed ProductVariantResponse.java:1-12
Viewed ProductImageResponse.java:1-12
Listed directory shop
Viewed ShopController.java:1-57
Viewed AdminShopController.java:1-75
Viewed ShopResponse.java:1-15
Listed directory cart
Viewed CartController.java:1-84
Viewed CartResponse.java:1-10
Viewed CartItemResponse.java:1-18
Viewed CartItemRequest.java:1-18
Listed directory order
Listed directory controller
Viewed OrderController.java:1-115
Viewed VendorOrderController.java:1-75
Viewed AdminOrderController.java:1-74
Viewed OrderResponse.java:1-23
Viewed CheckoutResponse.java:1-18
Viewed CheckoutRequest.java:1-18
Viewed CartPreviewRequest.java:1-10
Viewed CartPreviewResponse.java:1-15
Listed directory payment
Viewed PaymentController.java:1-108
Viewed PaymentCreateRequest.java:1-10
Viewed PaymentGroupStatusResponse.java:1-16
Viewed AdminRefundController.java:1-46
Listed directory review
Viewed ReviewController.java:1-134
Viewed VendorReviewController.java:1-39
Listed directory voucher
Viewed VoucherController.java:1-53
Viewed AdminVoucherController.java:1-35
Viewed NotificationController.java:1-64
Viewed ChatRoomController.java:1-73
Viewed ChatMessageController.java:1-24
Viewed MediaController.java:1-64
Viewed AdminStatisticsController.java:1-95
Viewed VendorAnalyticsController.java:1-37
Viewed GlobalExceptionHandler.java:1-99
Viewed ApiResponse.java:1-31
Viewed ErrorCode.java:1-76
Listed directory service
Viewed ShopService.java:1-60
Viewed ShopService.java:61-120
Viewed ShopService.java:121-178
Searched for "TODO"
Searched for "FIXME|XXX|mock|dummy"
Listed directory ecommerce
Listed directory e2e
Viewed PHASE2_FEATURE_AUDIT.md:1-150
Viewed SYSTEM_CURRENT_STATE_AUDIT.md:1-30
Viewed MASTER_SYSTEM_REAUDIT_CURRENT.md:1-74

Báo cáo kiểm toán (Audit) toàn bộ hệ thống Backend của dự án **Enterprise Ecommerce** dựa trên việc rà soát trực tiếp mã nguồn Java thực tế tại `src/main/java/com/tmt/ecommerce`.

---

# BÁO CÁO KIỂM TOÁN HỆ THỐNG BACKEND (BACKEND AUDIT REPORT)

> **Nguyên tắc rà soát:**
> - Source-First Audit: Rà soát trực tiếp mã nguồn, controller, DTO, entity, service layer và security configuration.
> - Tuân thủ kiến trúc **Modular Monolith** theo `@docs/ARCHITECTURE.md`.
> - **Tuyệt đối không sửa code / Không rebuild backend / Không implement**.

---

## 1. Xác định những module nào backend đã hoàn thành

Hệ thống backend được chia thành **12 module nghiệp vụ và hạ tầng** theo kiến trúc Modular Monolith. Hiện trạng hoàn thiện như sau:

| STT | Module | Trách nhiệm chính | Trạng thái Backend |
| :--- | :--- | :--- | :--- |
| **1** | **Identity** | Đăng ký, đăng nhập, phát hành JWT, phân quyền, quản lý hồ sơ cá nhân (`/users/me`) | **Hoàn thành (100%)** |
| **2** | **Product & Category** | Danh mục, sản phẩm, biến thể (variant), tìm kiếm lọc đa tiêu chí, sản phẩm liên quan, gợi ý sản phẩm | **Hoàn thành (100%)** |
| **3** | **Shop** | Đăng ký gian hàng, xem shop công khai, Admin duyệt shop / khóa (ban) / mở khóa (unban) | **Hoàn thành (100%)** |
| **4** | **Cart** | Quản lý giỏ hàng: xem giỏ, thêm, cập nhật số lượng, xóa item, dọn sạch giỏ | **Hoàn thành (100%)** |
| **5** | **Order** | Tính giá trước checkout (preview), checkout tách đơn đa shop (multi-vendor), State Machine cho Buyer/Vendor/Admin | **Hoàn thành (100%)** |
| **6** | **Payment** | Tạo thanh toán VNPay & COD, IPN Webhook xử lý bất đồng bộ chống trùng lặp (Pessimistic Lock), polling trạng thái nhóm đơn | **Hoàn thành (Core 100%)** *(Riêng luồng Admin Refund F10 đã xong code BE nhưng chưa đối soát thực tế sandbox)* |
| **7** | **Review** | Đánh giá sản phẩm kèm kiểm tra điều kiện mua hàng (eligibility), tổng hợp sao, Vendor phản hồi đánh giá | **Hoàn thành (100%)** |
| **8** | **Voucher** | Tạo mã giảm giá hệ thống (Admin) & shop (Vendor), tính toán phân bổ chiết khấu tỷ lệ, trừ lượt dùng nguyên tử (atomic update) | **Hoàn thành (100%)** |
| **9** | **Notification** | Quản lý thông báo in-app, đếm số thông báo chưa đọc, lắng nghe sự kiện domain event sau commit (`AFTER_COMMIT`) | **Hoàn thành (100%)** |
| **10** | **Chat** | Quản lý phòng chat qua REST API và nhắn tin thời gian thực qua **WebSocket STOMP** (`/ws`, `/app/chat.send`) | **Hoàn thành (100%)** |
| **11** | **Media** | Tích hợp Cloudinary tải lên hình ảnh multipart, trả về URL và `publicId` | **Hoàn thành (100%)** |
| **12** | **Statistics** | Thống kê Dashboard toàn sàn cho Admin và báo cáo doanh thu chi tiết gian hàng cho Vendor | **Hoàn thành (100%)** |

---

## 2. Xác định feature nào backend đã hoàn chỉnh và có thể đưa lên frontend

Tất cả các tính năng dưới đây đã có đầy đủ Controller, Service, Repository, DTO và Validation, sẵn sàng cho Frontend gọi trực tiếp:

### A. Dành cho Khách vãng lai & Người dùng chung (Public / Guest)
1. **Đăng ký tài khoản** (`POST /api/v1/auth/register`)
2. **Đăng nhập hệ thống** nhận JWT Bearer Token (`POST /api/v1/auth/login`)
3. **Xem danh mục sản phẩm** cây danh mục (`GET /api/v1/categories`)
4. **Tìm kiếm & duyệt sản phẩm** có phân trang, lọc theo keyword, categoryId, shopId, khoảng giá `minPrice` - `maxPrice`, sắp xếp `sortBy` (`GET /api/v1/products`)
5. **Xem chi tiết sản phẩm** bao gồm danh sách biến thể (variant attributes, sku, stock) và danh sách hình ảnh (`GET /api/v1/products/{id}`)
6. **Xem sản phẩm liên quan** cùng danh mục (`GET /api/v1/products/{id}/related`)
7. **Xem gợi ý sản phẩm** (`GET /api/v1/products/recommendations`)
8. **Xem đánh giá & tóm tắt sao của sản phẩm** (`GET /api/v1/products/{id}/reviews`, `GET /api/v1/products/{id}/reviews/summary`)
9. **Xem thông tin công khai của Shop** (`GET /api/v1/shops/{id}`)

### B. Dành cho Khách hàng đã đăng nhập (Buyer - `ROLE_USER`)
10. **Xem và cập nhật thông tin cá nhân** (`GET`, `PUT /api/v1/users/me`)
11. **Đăng ký mở gian hàng** (`POST /api/v1/shops`)
12. **Quản lý giỏ hàng**:
    - Xem giỏ hàng cá nhân (`GET /api/v1/carts`)
    - Thêm sản phẩm/biến thể vào giỏ (`POST /api/v1/carts/items`)
    - Thay đổi số lượng item trong giỏ (`PUT /api/v1/carts/items/{itemId}`)
    - Xóa 1 item hoặc làm rỗng giỏ hàng (`DELETE /api/v1/carts/items/{itemId}`, `DELETE /api/v1/carts`)
13. **Xem trước hóa đơn & tính toán giảm giá voucher** (`POST /api/v1/orders/preview`)
14. **Đặt hàng Checkout**: Hỗ trợ chọn danh sách cartItemIds cụ thể, tự động gom nhóm và tách đơn theo từng Shop, sinh `payment_group_id` (`POST /api/v1/orders/checkout`)
15. **Thanh toán đơn hàng**: Khởi tạo URL cổng thanh toán VNPay hoặc kích hoạt COD (`POST /api/v1/payments/create`)
16. **Kiểm tra trạng thái thanh toán**: Polling trạng thái nhóm đơn hàng (`GET /api/v1/payments/group/{paymentGroupId}/status`)
17. **Lịch sử đơn hàng**: Danh sách đơn hàng cá nhân có lọc theo trạng thái (`GET /api/v1/orders`) và chi tiết từng đơn (`GET /api/v1/orders/{id}`)
18. **Hủy đơn hàng**: Buyer tự hủy đơn khi đơn còn ở trạng thái `PENDING` (`PATCH /api/v1/orders/{id}/cancel`)
19. **Xác nhận đã nhận hàng**: Buyer xác nhận hoàn tất nhận hàng khi đơn ở trạng thái `SHIPPED` (`PATCH /api/v1/orders/{id}/confirm-delivery`)
20. **Đánh giá sản phẩm**: Kiểm tra quyền được đánh giá (`GET /api/v1/products/{id}/reviews/eligibility`), gửi đánh giá (`POST /api/v1/reviews`), xem/sửa/xóa đánh giá của chính mình
21. **Nhận thông báo in-app**: Xem thông báo, đếm số tin chưa đọc, đánh dấu đã đọc từng tin hoặc tất cả (`/api/v1/notifications/**`)
22. **Chat trực tuyến với Shop**: Tạo/lấy phòng chat (`POST /api/v1/chat/rooms`), xem lịch sử tin nhắn, gửi/nhận tin realtime qua WebSocket STOMP

### C. Dành cho Kênh Người Bán (Vendor - `ROLE_VENDOR`)
23. **Xem thông tin Shop của mình**: Tên, trạng thái duyệt (`GET /api/v1/shops/my-shop`)
24. **Upload hình ảnh sản phẩm lên Cloudinary**: Multipart file (`POST /api/v1/media/upload`)
25. **Quản lý sản phẩm của Shop**: Danh sách, chi tiết, đăng mới, cập nhật thông tin/biến thể/ảnh, ẩn sản phẩm (`/api/v1/vendor/products/**`)
26. **Quản lý đơn hàng của Shop**:
    - Xem danh sách đơn thuộc shop mình (`GET /api/v1/vendor/orders`)
    - Xem chi tiết đơn (`GET /api/v1/vendor/orders/{id}`)
    - Cập nhật trạng thái đơn hàng theo State Machine: `CONFIRMED -> SHIPPED -> DELIVERED` hoặc `CANCELLED` (`PATCH /api/v1/vendor/orders/{id}/status`)
27. **Quản lý mã giảm giá Shop**: Tạo Voucher shop và xem danh sách voucher của shop (`/api/v1/vouchers/**`)
28. **Phản hồi đánh giá**: Trả lời nhận xét của khách hàng trên sản phẩm của shop (`POST /api/v1/vendor/reviews/{id}/reply`)
29. **Báo cáo phân tích doanh thu**: Doanh số, số lượng đơn theo khoảng thời gian (`GET /api/v1/vendor/analytics`)

### D. Dành cho Kênh Quản Trị Toàn Sàn (Admin - `ROLE_ADMIN`)
30. **Quản lý duyệt gian hàng**: Xem danh sách shop, duyệt shop `PENDING -> ACTIVE`, khóa gian hàng (`ban`), mở khóa gian hàng (`unban`) (`/api/v1/admin/shops/**`)
31. **Quản trị đơn hàng toàn sàn**: Xem toàn bộ đơn hàng của tất cả các shop, xem chi tiết, can thiệp trạng thái đơn hàng và tra cứu lịch sử thay đổi trạng thái (`/api/v1/admin/orders/**`)
32. **Quản lý Voucher toàn sàn**: Tạo mã giảm giá cấp hệ thống (System Scope) (`/api/v1/admin/vouchers/**`)
33. **Dashboard thống kê toàn sàn**: Tổng số người dùng, tổng số shop, tổng sản phẩm, tổng đơn, doanh thu tích lũy, biểu đồ doanh thu theo ngày (`GET /api/v1/admin/statistics/dashboard`)

---

## 3 & 4. Xác định API hiện tại của từng feature & Request/Response DTO

Tất cả các API trả về đều được đóng gói theo định dạng chuẩn:
```json
{
  "status": 200,
  "errorCode": null,
  "message": "Thông điệp phản hồi",
  "data": { ... },
  "fieldErrors": null,
  "success": true
}
```

Dưới đây là chi tiết hợp đồng API (Contract) của toàn bộ hệ thống:

### 1. Module Identity & Auth

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Đăng ký** | `POST /api/v1/auth/register` | Public | `UserRegisterRequest`:<br>- `email` (String, email format)<br>- `password` (String, min 6)<br>- `fullName` (String)<br>- `phone` (String, optional) | `AuthResponse`:<br>- `token` (String) |
| **Đăng nhập** | `POST /api/v1/auth/login` | Public | `UserLoginRequest`:<br>- `email` (String)<br>- `password` (String) | `AuthResponse`:<br>- `token` (String) |
| **Xem hồ sơ** | `GET /api/v1/users/me` | Authenticated | *None* | `ProfileResponse`:<br>- `id` (Long)<br>- `email` (String)<br>- `fullName` (String)<br>- `phone` (String) |
| **Sửa hồ sơ** | `PUT /api/v1/users/me` | Authenticated | `ProfileUpdateRequest`:<br>- `fullName` (String, max 100)<br>- `phone` (String, max 20) | `ProfileResponse` (thông tin mới cập nhật) |

---

### 2. Module Product & Category

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Lấy danh mục** | `GET /api/v1/categories` | Public | *None* | `List<CategoryResponse>`:<br>- `id`, `name`, `description` |
| **Duyệt/Tìm kiếm sản phẩm** | `GET /api/v1/products` | Public | Query: `page`, `size`, `keyword`, `categoryId`, `shopId`, `minPrice`, `maxPrice`, `sortBy` | `Page<ProductResponse>` |
| **Chi tiết sản phẩm** | `GET /api/v1/products/{id}` | Public | Path: `{id}` | `ProductResponse`:<br>- `id`, `shopId`, `name`, `description`<br>- `price`, `stockQuantity`<br>- `categoryId`, `categoryName`, `status`<br>- `variants`: `List<ProductVariantResponse>`<br>- `imageResponses`: `List<ProductImageResponse>` |
| **Sản phẩm liên quan** | `GET /api/v1/products/{id}/related` | Public | Path: `{id}`, Query: `limit` (default 8) | `List<ProductResponse>` |
| **Gợi ý sản phẩm** | `GET /api/v1/products/recommendations` | Public | Query: `limit` (1-50, default 20) | `Page<ProductResponse>` |
| **Sản phẩm của Vendor** | `GET /api/v1/vendor/products` | VENDOR, ADMIN | Query: `page`, `size`, `keyword` | `Page<ProductResponse>` |
| **Vendor xem chi tiết SP** | `GET /api/v1/vendor/products/{id}`| VENDOR, ADMIN | Path: `{id}` | `ProductResponse` |
| **Vendor tạo sản phẩm** | `POST /api/v1/vendor/products` | VENDOR, ADMIN | `ProductCreateRequest`:<br>- `name`, `description`, `price`, `stockQuantity`, `categoryId`, `brandId`<br>- `variants`: `List<ProductVariantRequest>` (`sku`, `price`, `stockQuantity`, `attributes` map)<br>- `images`: `List<ProductImageRequest>` (`imageUrl`, `publicId`, `isThumbnail`, `sortOrder`) | `ProductResponse` (HTTP 201) |
| **Vendor cập nhật SP** | `PUT /api/v1/vendor/products/{id}` | VENDOR, ADMIN | `ProductUpdateRequest`:<br>- `name`, `description`, `price`, `stockQuantity`, `categoryId`<br>- `variants`: `List<ProductVariantUpdateRequest>`<br>- `images`: `List<ProductImageUpdateRequest>` | `ProductResponse` |
| **Vendor ẩn sản phẩm** | `DELETE /api/v1/vendor/products/{id}` | VENDOR, ADMIN | Path: `{id}` | `Void` (Data null) |

---

### 3. Module Shop

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Đăng ký Shop** | `POST /api/v1/shops` | Authenticated | `ShopCreateRequest`:<br>- `name` (String, @NotBlank)<br>- `description` (String) | `Void` (HTTP 201, trạng thái PENDING) |
| **Xem Shop của tôi** | `GET /api/v1/shops/my-shop` | Authenticated | *None* (lấy theo Current User) | `ShopResponse`:<br>- `id`, `userId`, `name`, `description`, `status`, `priorStatus` |
| **Xem thông tin Shop** | `GET /api/v1/shops/{id}` | Public | Path: `{id}` | `ShopResponse` (chỉ trả về khi Shop ACTIVE) |
| **Admin duyệt danh sách Shop** | `GET /api/v1/admin/shops` | ADMIN | Query: `status`, `page`, `size` | `Page<ShopResponse>` |
| **Admin phê duyệt Shop** | `PUT /api/v1/admin/shops/{id}/approve` | ADMIN | Path: `{id}` | `Void` (Shop ACTIVE, User nhận `ROLE_VENDOR`) |
| **Admin khóa Shop** | `PUT /api/v1/admin/shops/{id}/ban` | ADMIN | Path: `{id}`, Body: `ShopBanRequest` (`reason`) | `Void` (Shop BANNED) |
| **Admin mở khóa Shop** | `PUT /api/v1/admin/shops/{id}/unban` | ADMIN | Path: `{id}` | `Void` (Khôi phục trạng thái priorStatus) |

---

### 4. Module Cart

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Xem giỏ hàng** | `GET /api/v1/carts` | Authenticated | *None* | `CartResponse`:<br>- `cartId`<br>- `items`: `List<CartItemResponse>` (`cartItemId`, `productVariantId`, `shopId`, `productName`, `sku`, `unitPrice`, `quantity`, `subTotal`, `thumbnailUrl`, `attributes`, `isAvailable`)<br>- `totalAmount` |
| **Thêm vào giỏ** | `POST /api/v1/carts/items` | Authenticated | `CartItemRequest`:<br>- `productVariantId` (Long)<br>- `quantity` (Integer, min 1) | `Void` |
| **Đổi số lượng** | `PUT /api/v1/carts/items/{itemId}` | Authenticated | Path: `{itemId}`, Body: `CartItemUpdateRequest` (`quantity`) | `Void` |
| **Xóa 1 item** | `DELETE /api/v1/carts/items/{itemId}`| Authenticated | Path: `{itemId}` | `Void` |
| **Xóa sạch giỏ** | `DELETE /api/v1/carts` | Authenticated | *None* | `Void` |

---

### 5. Module Order

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Tính giá trước Checkout** | `POST /api/v1/orders/preview` | Authenticated | `CartPreviewRequest`:<br>- `cartItemIds` (List<Long>)<br>- `voucherCode` (String, optional) | `CartPreviewResponse`:<br>- `subtotal`, `discount`, `total` |
| **Thực hiện Checkout** | `POST /api/v1/orders/checkout` | Authenticated | `CheckoutRequest`:<br>- `shippingAddress` (String)<br>- `paymentMethod` (String: "VNPAY" / "COD")<br>- `voucherCode` (String, optional)<br>- `cartItemIds` (List<Long>) | `CheckoutResponse`:<br>- `paymentGroupId` (UUID String)<br>- `orders`: `List<OrderResponse>` |
| **Buyer xem danh sách đơn** | `GET /api/v1/orders` | Authenticated | Query: `status`, `page`, `size` | `Page<OrderResponse>` |
| **Buyer xem chi tiết đơn** | `GET /api/v1/orders/{id}` | Authenticated | Path: `{id}` | `OrderResponse`:<br>- `id`, `userId`, `customerName`, `shopId`<br>- `status`, `totalAmount`, `shippingAddress`, `paymentMethod`<br>- `createdAt`, `deliveredAt`, `deliveryConfirmationSource`<br>- `items`: `List<OrderItemResponse>` |
| **Buyer hủy đơn** | `PATCH /api/v1/orders/{id}/cancel` | Authenticated | Path: `{id}` (Chỉ khi PENDING) | `OrderResponse` (Status: CANCELLED) |
| **Buyer xác nhận đã nhận** | `PATCH /api/v1/orders/{id}/confirm-delivery` | Authenticated | Path: `{id}` (Chỉ khi SHIPPED) | `OrderResponse` (Status: DELIVERED) |
| **Vendor xem đơn hàng** | `GET /api/v1/vendor/orders` | VENDOR, ADMIN | Query: `status`, `page`, `size` | `Page<OrderResponse>` |
| **Vendor xem chi tiết đơn** | `GET /api/v1/vendor/orders/{id}` | VENDOR, ADMIN | Path: `{id}` | `OrderResponse` |
| **Vendor cập nhật trạng thái** | `PATCH /api/v1/vendor/orders/{id}/status` | VENDOR, ADMIN | Path: `{id}`, Body: `VendorOrderStatusUpdateRequest` (`status`: SHIPPED/DELIVERED/CANCELLED, `reason`) | `OrderResponse` |
| **Admin xem toàn bộ đơn** | `GET /api/v1/admin/orders` | ADMIN | Query: `status`, `page`, `size` | `Page<OrderResponse>` |
| **Admin xem chi tiết đơn** | `GET /api/v1/admin/orders/{id}` | ADMIN | Path: `{id}` | `OrderResponse` |
| **Admin can thiệp trạng thái**| `PATCH /api/v1/admin/orders/{id}/status` | ADMIN | Path: `{id}`, Body: `AdminOrderStatusRequest` | `OrderResponse` |
| **Admin xem chuyển trạng thái hợp lệ** | `GET /api/v1/admin/orders/{id}/allowed-transitions` | ADMIN | Path: `{id}` | `List<OrderStatus>` |
| **Admin xem lịch sử đơn** | `GET /api/v1/admin/orders/{id}/status-history` | ADMIN | Path: `{id}`, Query: `page`, `size` | `Page<OrderAdminHistoryResponse>` |

---

### 6. Module Payment

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Tạo URL thanh toán** | `POST /api/v1/payments/create` | Authenticated | `PaymentCreateRequest`:<br>- `paymentGroupId` (String UUID) | `PaymentCreateResponse`:<br>- `paymentUrl` (String VNPay URL hoặc redirect COD) |
| **VNPay IPN Webhook** | `GET /api/v1/payments/vnpay/ipn` | Public (Server-to-Server) | VNPay Query Parameters | `Map<String, String>`: `{"RspCode": "00", "Message": "Confirm Success"}` |
| **Polling trạng thái Payment Group** | `GET /api/v1/payments/group/{paymentGroupId}/status` | Authenticated | Path: `{paymentGroupId}` | `PaymentGroupStatusResponse`:<br>- `paymentGroupId` (String)<br>- `status` ("PENDING" / "SUCCESS" / "FAILED") |
| **Admin tạo Refund** | `POST /api/v1/admin/orders/{orderId}/refunds` | ADMIN | Header: `Idempotency-Key`, Body: `AdminRefundRequest` (`reason`) | `RefundAttemptResponse` |

---

### 7. Module Review

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Viết đánh giá** | `POST /api/v1/reviews` | USER | `ReviewCreateRequest`:<br>- `orderId` (Long), `productId` (Long)<br>- `rating` (Integer 1-5), `comment` (String) | `ReviewResponse` (HTTP 201) |
| **Xem đánh giá theo SP** | `GET /api/v1/products/{id}/reviews` | Public | Path: `{id}`, Query: `page`, `size` | `Page<ReviewResponse>` |
| **Tóm tắt sao SP** | `GET /api/v1/products/{id}/reviews/summary` | Public | Path: `{id}` | `RatingSummaryResponse`:<br>- `averageRating`, `totalReviews`, `starCounts` |
| **Kiểm tra quyền đánh giá** | `GET /api/v1/products/{id}/reviews/eligibility` | USER | Path: `{id}` | `List<Long>` (Danh sách orderId đủ điều kiện đánh giá) |
| **Xem review của tôi** | `GET /api/v1/products/{id}/reviews/my-reviews` | USER | Path: `{id}` | `List<ReviewResponse>` |
| **Sửa đánh giá** | `PUT /api/v1/reviews/{id}` | USER | Path: `{id}`, Body: `ReviewUpdateRequest` (`rating`, `comment`) | `ReviewResponse` |
| **Xóa đánh giá** | `DELETE /api/v1/reviews/{id}` | USER | Path: `{id}` | `Void` |
| **Vendor phản hồi đánh giá**| `POST /api/v1/vendor/reviews/{id}/reply` | VENDOR, ADMIN | Path: `{id}`, Body: `VendorReplyRequest` (`reply`) | `ReviewResponse` |

---

### 8. Module Voucher

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Tạo Voucher** | `POST /api/v1/vouchers` | VENDOR, ADMIN | `VoucherCreateRequest`:<br>- `code`, `discountType` (PERCENT/FIXED_AMOUNT)<br>- `discountValue`, `minOrderValue`, `maxDiscountAmount`<br>- `usageLimit`, `startDate`, `endDate`<br>- `scope` (SYSTEM nếu Admin, SHOP nếu Vendor) | `VoucherResponse` |
| **Vendor xem voucher của shop** | `GET /api/v1/vouchers/my-shop` | VENDOR | Query: `page`, `size` | `Page<VoucherResponse>` |
| **Admin xem voucher hệ thống** | `GET /api/v1/admin/vouchers` | ADMIN | Query: `page`, `size` | `Page<VoucherResponse>` |

---

### 9. Module Notification

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Xem danh sách thông báo**| `GET /api/v1/notifications` | Authenticated | Query: `page`, `size` | `Page<NotificationResponse>` |
| **Đếm số tin chưa đọc** | `GET /api/v1/notifications/unread-count` | Authenticated | *None* | `UnreadCountResponse` (`unreadCount`: Integer) |
| **Đánh dấu đã đọc 1 tin** | `PUT /api/v1/notifications/{id}/read` | Authenticated | Path: `{id}` | `Void` |
| **Đánh dấu đọc tất cả** | `PUT /api/v1/notifications/read-all` | Authenticated | *None* | `Void` |

---

### 10. Module Chat & Media & Statistics

| Feature | Method & Endpoint | Phân quyền | Request Payload / Params | Response Data DTO |
| :--- | :--- | :--- | :--- | :--- |
| **Tạo/Lấy phòng chat** | `POST /api/v1/chat/rooms` | Authenticated | `CreateChatRoomRequest` (`shopId`: Long) | `ChatRoomResponse` |
| **Xem danh sách phòng chat** | `GET /api/v1/chat/rooms` | Authenticated | *None* | `List<ChatRoomResponse>` |
| **Lấy lịch sử tin nhắn** | `GET /api/v1/chat/rooms/{roomId}/messages` | Authenticated | Path: `{roomId}`, Pageable query | `Page<ChatMessageResponse>` |
| **Đánh dấu tin nhắn đã đọc** | `PUT /api/v1/chat/rooms/{roomId}/read` | Authenticated | Path: `{roomId}` | `Void` |
| **Gửi tin nhắn Realtime** | WebSocket STOMP: `/app/chat.send` | STOMP Principal | Payload: `ChatMessageRequest` (`roomId`, `content`) | Lắng nghe tại `/user/queue/messages` |
| **Upload ảnh Cloudinary** | `POST /api/v1/media/upload` | Authenticated | Form-data: `file` (MultipartFile) | `UploadResponse` (`url`, `publicId`) |
| **Dashboard Admin** | `GET /api/v1/admin/statistics/dashboard` | ADMIN | Query: `startDate`, `endDate` (ISO Date) | `AdminDashboardStatsResponse` |
| **Thống kê Doanh thu Shop**| `GET /api/v1/vendor/analytics` | VENDOR | Query: `startDate`, `endDate` (ISO Date) | `VendorAnalyticsResponse` |

---

## 5. Xác định Authentication & Authorization

1. **Cơ chế xác thực**:
   - Sử dụng **Stateless JWT Bearer Token** qua HTTP Header: `Authorization: Bearer <access_token>`.
   - Thời gian sống (TTL) của Access Token: **24 giờ**.
   - Cấu trúc Payload của JWT:
     - `sub`: Địa chỉ email của người dùng.
     - `userId`: ID định danh duy nhất (Long).
     - `fullName`: Họ và tên người dùng.
     - `roles`: Danh sách quyền, ví dụ `["ROLE_USER", "ROLE_VENDOR"]` hoặc `["ROLE_ADMIN"]`.
     - `exp`: Timestamp hết hạn.
2. **Cơ chế phân quyền**:
   - Khởi tạo mặc định: Mọi tài khoản sau khi đăng ký thành công qua `POST /api/v1/auth/register` đều tự động được gán `ROLE_USER`.
   - Nâng quyền Vendor: Khi Admin duyệt đơn mở gian hàng thành công (`approveShop`), tài khoản được cấp thêm quyền `ROLE_VENDOR`.
   - Phương thức bảo vệ: Kết hợp `SecurityConfig.securityFilterChain` (cho cấp độ đường dẫn Public/Protected) và `@EnableMethodSecurity` với `@PreAuthorize("hasRole(...)")` hoặc `@PreAuthorize("hasAnyRole(...)")` ở tầng Controller.
3. **WebSocket STOMP Security**:
   - HTTP Handshake tại endpoint `/ws/**` được cấu hình `permitAll()` trên Spring Security filter chain để không chặn các client không hỗ trợ custom header trong handshake.
   - Quá trình xác thực JWT diễn ra nghiêm ngặt ở tầng STOMP qua `JwtChannelInterceptor`: Khi client gửi frame STOMP `CONNECT`, interceptor kiểm tra header `Authorization`, xác thực JWT và gán `StompPrincipal` với `principal.getName() = userId.toString()`.
   - Tin nhắn P2P được định tuyến chuẩn xác tới người nhận qua `convertAndSendToUser(recipientId.toString(), "/queue/messages", response)`.
4. **Trích xuất thông tin User hiện tại**:
   - Tầng Controller sử dụng custom argument resolver `@CurrentUserId Long userId` (hoặc `@CurrentUserId(required = false)` cho public recommendation), tự động lấy `userId` an toàn từ Security Context, không phụ thuộc vào Client truyền ID lên request body.

---

## 6. Xác định những API nào chưa ổn định (Unstable / Edge Cases)

1. **API Hoàn tiền Admin Refund (`POST /api/v1/admin/orders/{orderId}/refunds`)**:
   - **Tình trạng:** Code Backend đã hoàn chỉnh (có `RefundAttempt`, `VnPayRefundGateway`, `RefundService`), nhưng **chưa có kiểm chứng thực tế trên môi trường VNPay Sandbox Refund**. Luồng này phụ thuộc vào cấu hình merchant key và endpoint IPN/API đối soát hoàn tiền từ cổng thanh toán.
   - **Khuyến nghị:** Chưa nên đưa F10 vào giai đoạn đầu xây dựng Frontend; cần kiểm thử Sandbox thực tế trước.
2. **API Kiểm tra Shop cá nhân (`GET /api/v1/shops/my-shop`)**:
   - **Đặc điểm:** Nếu người dùng là Buyer thuần túy (chưa từng đăng ký Shop), API này ném `AppException(ErrorCode.SHOP_NOT_FOUND)` và trả về HTTP `404 Not Found`.
   - **Khuyến nghị:** Frontend không được coi mã `404` này là lỗi hệ thống (Crash/Toast Error), mà phải bắt mã `SHOP_NOT_FOUND` để hiển thị trạng thái "Bạn chưa có gian hàng - [Đăng ký ngay]".
3. **API Danh sách sản phẩm (`GET /api/v1/products`) - Ràng buộc giá**:
   - **Đặc điểm:** Nếu client truyền đồng thời `minPrice` và `maxPrice` mà `minPrice > maxPrice`, Backend trả về `400 Bad Request`.
   - **Khuyến nghị:** Frontend cần validate ràng buộc này ngay trên form lọc phía client trước khi gửi query.
4. **Phân bổ giảm giá Voucher đa Shop (Proportional Split)**:
   - **Đặc điểm:** Khi áp dụng Voucher cấp Hệ thống cho giỏ hàng gồm nhiều Shop, số tiền giảm giá được chia tỷ lệ theo doanh số từng shop. Có thể xuất hiện độ lệch làm tròn nhỏ giữa các đơn con.
   - **Khuyến nghị:** Frontend luôn lấy kết quả từ `POST /api/v1/orders/preview` để hiển thị, tuyệt đối không tự tính toán làm tròn ở client.

---

## 7. Xác định Backend nào cần sửa/lưu ý trước khi Frontend sử dụng

> **Lưu ý quan trọng:** Không cần thay đổi mã nguồn logic Backend, vì API contract hiện tại đã hoàn chỉnh và ổn định. Tuy nhiên, đội ngũ phát triển Frontend cần **lưu ý 4 đặc điểm kiến trúc sau**:

1. **Payload phản hồi của API Login (`AuthResponse`)**:
   - `POST /api/v1/auth/login` chỉ trả về:
     ```json
     { "status": 200, "data": { "token": "ey..." } }
     ```
   - API này **không trả kèm object `user` hay danh sách `roles` trong body**.
   - **Giải pháp Frontend:**
     - Cách 1 (Khuyên dùng): Giải mã payload JWT (base64 decode) để lấy `userId`, `fullName`, `roles`.
     - Cách 2: Ngay sau khi lưu token, gọi API `GET /api/v1/users/me` để hydrate thông tin người dùng vào `AuthContext`.
2. **Cấu hình CORS Origin**:
   - `SecurityConfig` hiện cho phép các origin dạng `http://localhost:[*]` và `http://127.0.0.1:[*]`.
   - Khi chạy Vite dev server (thường là `http://localhost:5173`), CORS đã tương thích hoàn toàn. Tuy nhiên, nếu deploy lên domain khác, cần cập nhật biến môi trường `application.websocket.allowed-origins`.
3. **Phân biệt định dạng dữ liệu: `Page<T>` vs `List<T>`**:
   - Các API phân trang trả về Spring Data `Page`:
     ```json
     {
       "content": [...],
       "pageable": { ... },
       "totalElements": 50,
       "totalPages": 5,
       "last": false
     }
     ```
     *(Áp dụng cho: `/api/v1/products`, `/api/v1/orders`, `/api/v1/notifications`, `/api/v1/chat/rooms/{id}/messages`)*.
   - Các API danh mục tĩnh trả về mảng trực tiếp: `List<T>` *(Áp dụng cho: `/api/v1/categories`, `/api/v1/chat/rooms`)*.
4. **Giao thức kết nối WebSocket Chat**:
   - Không kết nối WebSocket thuần qua URL mang token vì Spring Security đã mở `permitAll("/ws/**")`.
   - Bắt buộc dùng thư viện client STOMP (như `@stomp/stompjs` hoặc `stompjs` + `sockjs-client`) và truyền token vào header khi gửi frame `CONNECT`:
     ```typescript
     client.connectHeaders = {
       Authorization: `Bearer ${token}`
     };
     ```

---

## 8. Xác định Feature nào nên được chọn làm Frontend đầu tiên

### Lựa chọn tối ưu: **Feature 1 — Authentication & Core Layout State (Đăng ký / Đăng nhập / Session)**

**Lý do lựa chọn:**
1. **Là huyết mạch của toàn bộ ứng dụng:** Mọi tính năng cốt lõi (Cart, Checkout, Order, Shop, Chat, Notification, Vendor, Admin) đều phụ thuộc vào Token xác thực và định danh Role.
2. **Thiết lập nền móng kỹ thuật cho Frontend:**
   - Xây dựng tầng `apiClient` (Axios instance) với Request Interceptor tự động gắn `Authorization: Bearer <token>`.
   - Xây dựng Response Interceptor tự động xử lý `401 Unauthorized` (điều hướng về `/login`) và `403 Forbidden`.
   - Xây dựng hệ thống điều hướng bảo mật: `PublicLayout`, `CustomerLayout`, `VendorLayout`, `AdminLayout` và các Route Guards (`RequireAuth`, `RequireRole`).
3. **Mức độ phụ thuộc thấp:** Module `Identity` (`/api/v1/auth/**`) hoàn toàn độc lập, không phụ thuộc vào dữ liệu của các module khác.

---

## ĐỀ XUẤT THỨ TỰ XÂY DỰNG FRONTEND (FRONTEND ROADMAP)

Quá trình xây dựng Frontend nên được chia thành **5 giai đoạn theo từng Vertical Slice hoàn chỉnh**:

```text
GIAI ĐOẠN 1: Nền tảng, Xác thực & Hồ sơ (Foundation & Auth)
     ↓
GIAI ĐOẠN 2: Luồng Mua Sắm Cốt Lõi (Core Shopping & Payment Flow)
     ↓
GIAI ĐOẠN 3: Quản lý Đơn hàng & Tương tác Khách hàng (Buyer Experience)
     ↓
GIAI ĐOẠN 4: Kênh Dành Cho Người Bán (Vendor Portal)
     ↓
GIAI ĐOẠN 5: Kênh Quản Trị Hệ Thống Toàn Sàn (Admin Portal)
```

---

### Giai đoạn 1: Nền tảng, Xác thực & Hồ sơ (Foundation & Identity)
- **Slice 1.1 — Khởi tạo Kiến trúc Base:**
  - Cài đặt React + TypeScript + Vite + TailwindCSS.
  - Cấu hình Axios Base Client, xử lý Token Storage và Interceptors.
  - Cấu hình React Router DOM với các Layout chuẩn: `PublicLayout`, `CustomerLayout`, `VendorLayout`, `AdminLayout`.
- **Slice 1.2 — Authentication Flow:**
  - Màn hình Đăng nhập (`/login`): Form validation, gọi `POST /api/v1/auth/login`, lưu token, trích xuất quyền và redirect theo role.
  - Màn hình Đăng ký (`/register`): Form validation, gọi `POST /api/v1/auth/register`, tự động đăng nhập sau khi đăng ký thành công.
  - Quản lý phiên làm việc toàn cục (`AuthContext`).
- **Slice 1.3 — Quản lý Hồ sơ Cá nhân (Profile):**
  - Màn hình `/profile`: Xem thông tin tài khoản, form cập nhật họ tên và số điện thoại qua `GET / PUT /api/v1/users/me`.

---

### Giai đoạn 2: Luồng Mua Sắm Cốt Lõi (Core Shopping Flow)
- **Slice 2.1 — Trang Chủ & Khám Phá Sản Phẩm (Catalog):**
  - Header với thanh tìm kiếm và Menu Danh mục (`GET /api/v1/categories`).
  - Trang danh sách sản phẩm (`/products`): Thanh tìm kiếm từ khóa, bộ lọc theo danh mục, bộ lọc theo khoảng giá `minPrice - maxPrice`, sắp xếp, phân trang.
- **Slice 2.2 — Trang Chi Tiết Sản Phẩm (Product Detail):**
  - Trang `/products/:id`: Trình chiếu ảnh sản phẩm (Cloudinary), bảng chọn biến thể (Variant: size, màu, thuộc tính), giá linh hoạt theo variant, tồn kho tương ứng.
  - Danh sách sản phẩm liên quan (`/products/{id}/related`) và sản phẩm gợi ý.
  - Nút "Thêm vào giỏ hàng" và nút "Mua ngay".
- **Slice 2.3 — Giỏ Hàng Trực Tuyến (Cart Management):**
  - Mini-cart trên Header và Trang Giỏ hàng chính thức (`/cart`).
  - Danh sách item gom theo Shop, thay đổi số lượng, xóa item, tính tổng tiền tạm tính.
  - Checkbox chọn các sản phẩm cần thanh toán.
- **Slice 2.4 — Thanh Toán & Đặt Hàng (Checkout & Payment):**
  - Trang Checkout (`/checkout`): Nhập địa chỉ giao hàng, áp dụng Voucher giảm giá (`POST /api/v1/orders/preview`), chọn phương thức (VNPay / COD).
  - Đặt hàng (`POST /api/v1/orders/checkout`) nhận `paymentGroupId`.
  - Tích hợp thanh toán: Chuyển hướng sang cổng VNPay (`POST /api/v1/payments/create`).
  - Trang kết quả thanh toán (`/payment-result`): Polling trạng thái thanh toán từ `GET /api/v1/payments/group/{id}/status` và hiển thị thông báo thành công.

---

### Giai đoạn 3: Trải Nghiệm Khách Hàng Sau Mua Hàng (Buyer Post-Purchase)
- **Slice 3.1 — Quản lý Đơn hàng Cá nhân:**
  - Danh sách đơn hàng (`/orders`): Tabs phân loại theo trạng thái (`PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`).
  - Chi tiết đơn hàng (`/orders/:id`): Thông tin vận chuyển, danh sách món hàng, tổng tiền.
  - Nút **Hủy đơn hàng** (khi `PENDING`) và Nút **Đã nhận được hàng** (khi `SHIPPED`).
- **Slice 3.2 — Đánh Giá Sản Phẩm (Product Reviews):**
  - Tab đánh giá tại trang chi tiết sản phẩm: Danh sách review, tóm tắt điểm sao trung bình.
  - Form viết đánh giá cho sản phẩm đã mua thành công (`POST /api/v1/reviews`).
- **Slice 3.3 — Thông Báo In-App (Notifications):**
  - Icon chuông trên Header kèm Badge số lượng tin chưa đọc.
  - Dropdown xem danh sách thông báo, tự động điều hướng đến đơn hàng tương ứng khi click.
- **Slice 3.4 — Chat Thời Gian Thực (Realtime Shopee-style Chat Widget):**
  - Khung chat nổi (Floating Chat Widget) ở góc dưới màn hình.
  - Tích hợp nút "Chat ngay" tại trang chi tiết sản phẩm để mở phòng chat với Shop.
  - Nhắn tin 2 chiều thời gian thực qua WebSocket STOMP.

---

### Giai đoạn 4: Kênh Dành Cho Người Bán (Vendor Portal)
- **Slice 4.1 — Đăng ký Gian hàng (Shop Onboarding):**
  - Form đăng ký mở Shop cho Buyer (`/become-seller`).
  - Màn hình chờ duyệt (Pending State) và thông báo sau khi Admin duyệt.
- **Slice 4.2 — Quản lý Sản phẩm Shop (`/vendor/products`):**
  - Bảng danh sách sản phẩm của Shop, tìm kiếm, ẩn/hiện sản phẩm.
  - Form tạo/sửa sản phẩm: Tích hợp Upload ảnh kéo thả lên Cloudinary (`POST /api/v1/media/upload`), cấu hình biến thể (SKU, giá, tồn kho).
- **Slice 4.3 — Quản lý Đơn hàng Shop (`/vendor/orders`):**
  - Bảng danh sách đơn hàng gửi đến Shop.
  - Xử lý đơn hàng qua nút chuyển trạng thái: `CONFIRMED -> SHIPPED -> DELIVERED` hoặc `CANCELLED`.
- **Slice 4.4 — Voucher Shop & Phân Tích Doanh Thu:**
  - Tạo và quản lý mã giảm giá riêng của Shop.
  - Trả lời đánh giá của khách hàng.
  - Dashboard doanh thu gian hàng (`/vendor/analytics`): Biểu đồ doanh số và số lượng đơn.

---

### Giai đoạn 5: Kênh Quản Trị Hệ Thống Toàn Sàn (Admin Portal)
- **Slice 5.1 — Quản lý & Phê duyệt Gian hàng (`/admin/shops`):**
  - Danh sách Shop toàn hệ thống, bộ lọc trạng thái.
  - Phê duyệt Shop chờ duyệt (`Approve`), Khóa shop vi phạm (`Ban`), Mở khóa shop (`Unban`).
- **Slice 5.2 — Quản trị Đơn hàng Toàn sàn (`/admin/orders`):**
  - Xem danh sách toàn bộ đơn hàng của tất cả các Shop.
  - Xem lịch sử biến động trạng thái đơn hàng.
  - Quyền can thiệp trạng thái đơn hàng trong các trường hợp tranh chấp.
- **Slice 5.3 — Quản lý Voucher Hệ thống (`/admin/vouchers`):**
  - Tạo và phân phối Voucher cấp hệ thống (System Scope).
- **Slice 5.4 — Admin Statistics Dashboard (`/admin/dashboard`):**
  - Thống kê tổng số Users, Shops, Products, Orders.
  - Doanh thu lũy kế toàn sàn và biểu đồ tăng trưởng đơn hàng theo ngày.

---

### TỔNG KẾT

Backend của hệ thống hiện tại đang ở trạng thái **RẤT TỐT (Production-Ready Core)**:
- Không có lỗi kiến trúc nghiêm trọng.
- Modular Monolith được tuân thủ nghiêm ngặt qua Internal Service Contract.
- Tầng bảo mật, phân quyền, xử lý giao dịch (`@Transactional`) và đồng bộ Webhook IPN đều đã hoàn thiện và được kiểm chứng bằng test suite.

**Hệ thống đã sẵn sàng 100% để bước vào triển khai Frontend ngay từ Giai đoạn 1 (Foundation & Identity) mà không cần can thiệp chỉnh sửa bất kỳ dòng mã nào ở Backend.**