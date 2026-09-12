# CURRENT SYSTEM INVENTORY

## 1. Backend Modules
- **Identity**: UserController, AuthService, JwtService, Role/User entities
- **Product**: ProductController, CategoryController, Variant entities, Cloudinary integration
- **Shop**: ShopController, ShopApproval workflow
- **Order**: OrderController, OrderInternalService, VendorOrderController
- **Payment**: PaymentController, VnPayGateway, IPN Webhook
- **Cart**: CartController, CartItem entities
- **Review**: ReviewController, ReviewNotificationListener
- **Voucher**: VoucherController, Atomic update logic
- **Notification**: NotificationController, STOMP integration, TransactionalEventListener
- **Chat**: ChatController, WebSocket STOMP, JwtChannelInterceptor
- **Statistics**: AdminStatisticsController
- **Common/Infrastructure**: ExceptionHandler, ApiResponse, FileStorageService

## 2. Frontend Structure
- **Pages**: ProductList, ProductDetail, Cart, Checkout, PaymentResult, AdminDashboard, AdminOrders, VendorOrders, Profile
- **Components**: FloatingChatWidget, NotificationDropdown, VoucherSelector
- **Services**: API clients (Axios), WebSocketService (STOMP)
- **State**: React Context (ChatContext, AuthContext)

## 3. Flow Example: Checkout
CheckoutPage (FE) -> POST /api/v1/orders/checkout -> OrderController -> OrderService.checkout -> PaymentGroup creation -> DB -> Response -> FE redirect to VNPay.
