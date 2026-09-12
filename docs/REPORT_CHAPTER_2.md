# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT

---

## 2.1. Các khái niệm nền tảng liên quan bài toán

### 2.1.1. Thương mại điện tử đa người bán (Multi-Vendor Ecommerce)

**WHAT:** Thương mại điện tử (E-commerce) là hình thức thực hiện các hoạt động mua bán hàng hóa, dịch vụ thông qua các phương tiện điện tử, chủ yếu là mạng Internet. Trong mô hình đa người bán (Multi-Vendor), một nền tảng cho phép nhiều người bán (Vendor) độc lập cùng kinh doanh, trong khi nền tảng đóng vai trò trung gian quản lý giao dịch.

**WHY:** Mô hình Multi-Vendor tạo ra hiệu ứng mạng lưới (network effect): càng nhiều Vendor tham gia, danh mục sản phẩm càng phong phú, thu hút thêm người mua, từ đó lại thu hút thêm Vendor. Đây là lý do mô hình này có tốc độ tăng trưởng vượt trội so với mô hình bán hàng một người bán truyền thống.

**Các thành phần cốt lõi** của một hệ thống Multi-Vendor Ecommerce:

- **Danh mục sản phẩm (Product Catalog):** Hệ thống tổ chức sản phẩm theo danh mục, hỗ trợ tìm kiếm và lọc.
- **Giỏ hàng (Shopping Cart):** Vùng lưu trữ tạm thời các sản phẩm người mua dự định mua, trước khi xác nhận đặt hàng.
- **Đơn hàng (Order):** Bản ghi chính thức của giao dịch mua bán, bao gồm thông tin sản phẩm, số lượng, giá và trạng thái xử lý.
- **Tách đơn đa người bán (Multi-vendor Order Splitting):** Khi một lần mua hàng bao gồm sản phẩm từ nhiều gian hàng khác nhau, hệ thống tự động tạo ra các đơn hàng riêng biệt tương ứng với từng gian hàng, nhưng vẫn liên kết chúng trong một phiên thanh toán chung.
- **Cổng thanh toán (Payment Gateway):** Dịch vụ trung gian xử lý giao dịch tài chính giữa người mua và nền tảng.
- **Hệ thống đánh giá (Review System):** Cho phép người mua chia sẻ nhận xét về sản phẩm sau khi nhận hàng, tạo niềm tin cho các người mua tiếp theo.

### 2.1.2. Kiến trúc phần mềm — Modular Monolith

**WHAT:** Modular Monolith là một kiến trúc phần mềm trong đó toàn bộ hệ thống được đóng gói và triển khai như một đơn vị duy nhất (monolith), nhưng mã nguồn được tổ chức nội bộ thành các module nghiệp vụ tách biệt, rõ ràng, với ranh giới được kiểm soát nghiêm ngặt.

**WHY:** Kiến trúc này đưa ra câu trả lời cho câu hỏi thực tiễn: làm thế nào để có được sự tổ chức code tốt và ranh giới nghiệp vụ rõ ràng của Microservices mà không phải chịu độ phức tạp vận hành của nó (service discovery, network latency, distributed transactions)? Đối với hệ thống quy mô vừa hoặc đội phát triển nhỏ, Modular Monolith thường là lựa chọn tối ưu hơn.

**So sánh với các kiến trúc khác:**

**Bảng 2.1.** So sánh các kiến trúc phần mềm phổ biến

| Tiêu chí | Monolith | Microservices | Modular Monolith |
|---|---|---|---|
| Triển khai | 1 đơn vị | N đơn vị độc lập | 1 đơn vị |
| Ranh giới module | Không bắt buộc | Vật lý (network) | Logic (code boundary) |
| Độ phức tạp vận hành | Thấp | Cao | Thấp |
| Ranh giới nghiệp vụ | Mờ nhạt | Rõ ràng | Rõ ràng |
| Giao tiếp cross-module | Trực tiếp | HTTP/gRPC/message bus | Interface contracts |
| Phù hợp quy mô | Nhỏ | Lớn | Vừa |

**HOW — Nguyên tắc trong hệ thống:**
- Mỗi module sở hữu Entity, Repository, Service, Controller và DTO của riêng mình.
- Giao tiếp giữa module A và module B phải đi qua Internal Service interface — không được inject trực tiếp Repository hay Entity của module khác.
- Hướng phụ thuộc phải rõ ràng và không vòng tròn.

*Ví dụ trong hệ thống:* Module `order` cần thông tin sản phẩm từ module `product`, nhưng không inject `ProductRepository` mà gọi qua `ProductInternalService`:

```java
// Đúng: giao tiếp qua Internal Service contract
ProductInternalData variant = productInternalService.getProductVariant(variantId);

// Sai: vi phạm ranh giới module
Product product = productRepository.findById(productId); // ← KHÔNG được phép
```

### 2.1.3. REST API

**WHAT:** REST (Representational State Transfer) là phong cách kiến trúc cho hệ thống phân tán, được Roy Fielding định nghĩa năm 2000. RESTful API là giao diện lập trình ứng dụng tuân theo các ràng buộc của kiến trúc REST, sử dụng giao thức HTTP.

**Các ràng buộc cốt lõi của REST:**
- **Stateless (Phi trạng thái):** Mỗi request chứa đầy đủ thông tin cần thiết để xử lý; server không lưu trạng thái phiên làm việc của client.
- **Uniform Interface (Giao diện thống nhất):** Sử dụng HTTP verbs (GET, POST, PUT, PATCH, DELETE) và URI có ý nghĩa ngữ nghĩa.
- **Client-Server:** Tách biệt giao diện người dùng khỏi logic lưu trữ dữ liệu.

**HOW — Quy ước trong hệ thống:**

**Bảng 2.2.** Quy ước HTTP method và ý nghĩa trong hệ thống

| HTTP Method | Hành động | Ví dụ trong hệ thống |
|---|---|---|
| `GET` | Đọc tài nguyên | `GET /api/v1/products` — lấy danh sách sản phẩm |
| `POST` | Tạo tài nguyên mới | `POST /api/v1/orders/checkout` — tạo đơn hàng |
| `PUT` | Cập nhật toàn bộ | `PUT /api/v1/products/{id}` — sửa sản phẩm |
| `PATCH` | Cập nhật một phần | `PATCH /api/v1/vendor/orders/{id}/status` — cập nhật trạng thái |
| `DELETE` | Xóa tài nguyên | `DELETE /api/v1/carts/items/{itemId}` — xóa khỏi giỏ |

Hệ thống sử dụng định dạng response thống nhất cho tất cả API:

```json
{
  "status": "SUCCESS",
  "message": "Thao tác thành công",
  "data": { ... }
}
```

### 2.1.4. Xác thực và Phân quyền

**WHAT:**
- **Xác thực (Authentication):** Quá trình xác minh danh tính của một chủ thể (người dùng, dịch vụ). Trả lời câu hỏi: "Bạn là ai?"
- **Phân quyền (Authorization):** Quá trình kiểm tra xem một chủ thể đã được xác thực có quyền thực hiện một hành động cụ thể không. Trả lời câu hỏi: "Bạn được phép làm gì?"

**WHY:** Trong hệ thống Multi-Vendor, ba nhóm người dùng (Buyer, Vendor, Admin) có quyền hạn hoàn toàn khác nhau. Nếu không có cơ chế phân quyền, một Vendor có thể thao túng đơn hàng của gian hàng khác, hoặc Buyer có thể truy cập trang quản trị.

**HOW — Trong hệ thống:**
- **JWT** (xem mục 2.2.3) được dùng cho xác thực.
- Spring Security với annotation `@PreAuthorize` kiểm soát quyền truy cập theo vai trò:
  - `@PreAuthorize("hasRole('ADMIN')")` — chỉ Admin.
  - `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")` — Vendor hoặc Admin.
  - `@PreAuthorize("hasRole('USER')")` — chỉ người dùng thông thường.
- Ngoài phân quyền theo vai trò, hệ thống còn kiểm tra quyền sở hữu dữ liệu (ownership check): Vendor A không thể xem đơn hàng của Vendor B, dù cả hai đều có vai trò `ROLE_VENDOR`.

### 2.1.5. ORM và Quản lý Persistence

**WHAT:** ORM (Object-Relational Mapping) là kỹ thuật ánh xạ giữa đối tượng trong ngôn ngữ lập trình hướng đối tượng và bảng trong cơ sở dữ liệu quan hệ. ORM cho phép lập trình viên thao tác với dữ liệu thông qua các đối tượng Java thay vì viết SQL trực tiếp.

**WHY:** ORM giúp tăng năng suất phát triển, giảm code boilerplate, và tăng khả năng bảo trì. Tuy nhiên, ORM cũng có nhược điểm: nếu không hiểu cách ORM tạo ra câu SQL, dễ xảy ra vấn đề hiệu năng như N+1 Query.

**HOW — Trong hệ thống:** Hệ thống sử dụng **Spring Data JPA** với Hibernate làm ORM implementation. Mỗi Entity được ánh xạ từ một lớp Java sang một bảng trong MySQL:

```java
@Entity
@Table(name = "products")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;
    // ...
}
```

**Vấn đề N+1 Query và cách giải quyết trong hệ thống:**

N+1 Query là hiện tượng xảy ra khi ORM thực thi 1 câu query để lấy danh sách N bản ghi, sau đó thực thi thêm N câu query riêng biệt để lấy dữ liệu liên quan cho từng bản ghi — tổng cộng N+1 câu query thay vì chỉ 1-2 câu.

Hệ thống giải quyết N+1 bằng:
- `@BatchSize(size=50)` trên các association như `Order.items`, `Product.variants`, `Product.images` — Hibernate gom thành một câu `IN (...)` thay vì N câu riêng lẻ.
- Bulk internal API như `ProductInternalService.getVariantInfos(variantIds)` — một câu query cho nhiều ID thay vì vòng lặp N câu.

### 2.1.6. Quản lý Giao dịch (Transaction Management)

**WHAT:** Giao dịch (Transaction) là một đơn vị công việc không thể chia nhỏ hơn, đảm bảo tính toàn vẹn dữ liệu khi thực hiện nhiều thao tác ghi liên quan. Một giao dịch thỏa mãn bốn thuộc tính ACID:

- **Atomicity (Nguyên tử):** Hoặc tất cả thao tác trong giao dịch đều thành công, hoặc tất cả đều bị hủy bỏ (rollback).
- **Consistency (Nhất quán):** Dữ liệu luôn ở trạng thái hợp lệ trước và sau giao dịch.
- **Isolation (Độc lập):** Các giao dịch thực hiện đồng thời không ảnh hưởng lẫn nhau.
- **Durability (Bền vững):** Khi giao dịch đã commit, dữ liệu được lưu vĩnh viễn kể cả khi hệ thống gặp sự cố.

**WHY:** Trong luồng checkout của hệ thống, nhiều thao tác ghi phải xảy ra nguyên tử: trừ tồn kho, cập nhật lượt dùng voucher, tạo đơn hàng, xóa giỏ hàng. Nếu bất kỳ bước nào thất bại, toàn bộ phải được rollback để tránh dữ liệu không nhất quán (ví dụ: đã trừ tồn kho nhưng chưa tạo được đơn hàng).

**HOW — Các kỹ thuật trong hệ thống:**

1. **`@Transactional`** đặt trên method service để bọc toàn bộ logic trong một giao dịch.
2. **`@Transactional(readOnly = true)`** cho các read-only method — tối ưu hiệu năng, bỏ qua dirty-checking.
3. **`@TransactionalEventListener(phase = AFTER_COMMIT)`** cho notification và email — đảm bảo các tác dụng phụ chỉ xảy ra khi giao dịch chính đã commit thành công, tránh gửi email khi giao dịch bị rollback.
4. **`Propagation.REQUIRES_NEW`** cho notification listeners — mở giao dịch mới độc lập để lưu notification, tránh ảnh hưởng giao dịch gốc.

### 2.1.7. WebSocket và Giao thức STOMP

**WHAT:** WebSocket (RFC 6455) là giao thức truyền thông cung cấp kênh giao tiếp song song (full-duplex) qua một kết nối TCP duy nhất. Khác với HTTP request-response thông thường, WebSocket cho phép server chủ động đẩy dữ liệu đến client mà không cần client phải gửi request trước.

**STOMP** (Simple Text Oriented Message Protocol) là một giao thức messaging dựa trên text, thường được dùng phía trên WebSocket để cung cấp cú pháp publish/subscribe có cấu trúc rõ ràng hơn.

**WHY:** Chức năng chat thời gian thực không thể dùng HTTP thông thường vì:
- HTTP là giao thức request-response: client phải gửi request trước mới nhận được response.
- Để nhận tin nhắn mới, client sẽ phải liên tục polling server — tốn băng thông và tạo độ trễ.
- WebSocket giải quyết vấn đề này bằng kết nối liên tục, cho phép server đẩy tin nhắn ngay khi có.

**HOW — Trong hệ thống:**

```
Client (Browser)                  Server (Spring Boot)
     |                                     |
     |---- HTTP GET /ws -----------------> |  (HTTP Upgrade Handshake)
     |<--- 101 Switching Protocols ------- |
     |                                     |
     |---- STOMP CONNECT ----------------> |  (JwtChannelInterceptor xác thực JWT)
     |<--- STOMP CONNECTED -------------- |
     |                                     |
     |---- SUBSCRIBE /user/queue/messages->|
     |                                     |
     |---- STOMP SEND /app/chat.send ----> |  (ChatMessageController)
     |                                     |---- convertAndSendToUser(recipientId)
     |<--- tin nhắn (server push) -------- |
```

**Hình 2.1.** Luồng kết nối WebSocket STOMP trong hệ thống

Hệ thống sử dụng User Destination (`/user/queue/messages`) để định tuyến tin nhắn điểm-điểm (P2P) đến đúng người nhận, thay vì broadcast cho tất cả client.

### 2.1.8. Thanh toán trực tuyến — Mô hình IPN (Instant Payment Notification)

**WHAT:** IPN (Instant Payment Notification) là cơ chế thông báo từ phía cổng thanh toán đến máy chủ của người bán để xác nhận kết quả giao dịch. Khi người mua hoàn tất thanh toán trên trang của cổng thanh toán, cổng thanh toán gọi một URL webhook đã đăng ký trước để thông báo kết quả.

**WHY:** Không thể tin tưởng vào URL redirect sau thanh toán để xác nhận kết quả, vì người dùng có thể đóng trình duyệt trước khi redirect, hoặc URL redirect có thể bị giả mạo. IPN đảm bảo server nhận được kết quả xác thực trực tiếp từ cổng thanh toán.

**HOW — Luồng VNPay trong hệ thống:**

```
Người mua → hệ thống → VNPay Sandbox (redirect)
                              ↓ (sau khi thanh toán xong)
              VNPay → GET /api/v1/payments/vnpay/ipn (IPN webhook)
                              ↓
              Hệ thống xác thực HMAC-SHA512
                              ↓
              Cập nhật Payment.status → SUCCESS
                              ↓
              Cập nhật tất cả Orders → CONFIRMED
```

**Hình 2.2.** Luồng xử lý IPN callback từ VNPay

### 2.1.9. Hệ thống Thông báo Hướng sự kiện (Event-Driven Notification)

**WHAT:** Kiến trúc hướng sự kiện (Event-Driven Architecture) là mô hình thiết kế trong đó các thành phần hệ thống giao tiếp với nhau thông qua việc phát và lắng nghe sự kiện, thay vì gọi nhau trực tiếp.

**WHY:** Trong hệ thống, module `order` cần kích hoạt thông báo khi đơn hàng thay đổi trạng thái. Nếu `OrderService` gọi trực tiếp `NotificationService`, hai module sẽ bị kết hợp chặt (tight coupling) — vi phạm ranh giới module. Với Event-Driven, `OrderService` chỉ phát sự kiện; module `notification` tự lắng nghe và xử lý.

**HOW — Trong hệ thống:**

```java
// OrderServiceImpl phát sự kiện ngữ nghĩa (không biết ai lắng nghe)
applicationEventPublisher.publishEvent(new OrderConfirmedEvent(order));

// OrderNotificationListener lắng nghe — trong module notification
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
@Transactional(propagation = Propagation.REQUIRES_NEW)
public void handleOrderConfirmed(OrderConfirmedEvent event) {
    notificationRepository.save(buildNotification(event));
}
```

Việc dùng `AFTER_COMMIT` đảm bảo thông báo chỉ được tạo khi giao dịch đặt hàng đã commit thành công — tránh trường hợp gửi thông báo nhưng đơn hàng lại bị rollback.

### 2.1.10. Mã giảm giá (Voucher)

**WHAT:** Voucher là mã giảm giá do nền tảng hoặc người bán phát hành, cho phép người mua giảm một phần chi phí khi đặt hàng. Trong hệ thống đa người bán, có hai phạm vi voucher:

- **Voucher hệ thống (SYSTEM):** Admin phát hành, áp dụng cho tất cả gian hàng trong một đơn hàng.
- **Voucher gian hàng (SHOP):** Vendor phát hành, chỉ áp dụng cho sản phẩm của gian hàng đó.

**WHY — Vấn đề phân bổ giảm giá trong đa người bán:** Khi một SYSTEM voucher được áp dụng cho đơn hàng có sản phẩm từ nhiều gian hàng, cần một công thức phân bổ công bằng để quyết định mỗi gian hàng được giảm bao nhiêu.

**HOW — Công thức Proportional Split:**

Cho đơn hàng gồm $n$ gian hàng, gọi:
- $S_i$: giá trị đơn hàng của gian hàng thứ $i$ (subtotal).
- $S_{total} = \sum_{i=1}^{n} S_i$: tổng giá trị đơn hàng đủ điều kiện.
- $D_{total}$: tổng số tiền giảm giá của voucher.

Số tiền giảm giá phân bổ cho gian hàng $i$:

$$D_i = D_{total} \times \frac{S_i}{S_{total}} \quad (2.1)$$

*Ví dụ:* Đơn hàng có 2 gian hàng: Shop A (500.000đ) và Shop B (1.000.000đ). Voucher giảm 150.000đ.
$$D_A = 150.000 \times \frac{500.000}{1.500.000} = 50.000 \text{ đ} \quad (2.2)$$
$$D_B = 150.000 \times \frac{1.000.000}{1.500.000} = 100.000 \text{ đ} \quad (2.3)$$

---

## 2.2. Các phương pháp, mô hình, thuật toán sử dụng

### 2.2.1. Mô hình MVC (Model-View-Controller)

**WHAT:** MVC là mô hình thiết kế phân tách ứng dụng thành ba thành phần có trách nhiệm riêng biệt:
- **Model:** Đại diện cho dữ liệu và logic nghiệp vụ.
- **View:** Giao diện hiển thị dữ liệu cho người dùng.
- **Controller:** Điều phối request từ View, gọi Model xử lý, trả kết quả về View.

**HOW — Trong hệ thống:** Spring MVC là framework nền tảng của backend. Mỗi HTTP request đi qua chuỗi:

```
HTTP Request → DispatcherServlet → Controller → Service → Repository → MySQL
                                                                 ↓
HTTP Response ← DispatcherServlet ← Controller ← DTO ← Service
```

**Hình 2.3.** Luồng xử lý request theo mô hình MVC trong Spring Boot

Controller không chứa logic nghiệp vụ — chỉ nhận request, delegate xuống Service, và trả response. Toàn bộ logic nghiệp vụ nằm ở Service layer.

### 2.2.2. Strategy Pattern (Chiến lược)

**WHAT:** Strategy Pattern là mẫu thiết kế hành vi (behavioral design pattern) cho phép định nghĩa một nhóm các thuật toán/hành vi có thể hoán đổi cho nhau, đóng gói từng cái vào một class riêng biệt, và cho phép client chọn thuật toán phù hợp tại runtime mà không cần thay đổi code sử dụng.

**WHY:** Hệ thống hỗ trợ hai phương thức thanh toán (VNPay và COD) với luồng xử lý hoàn toàn khác nhau. Nếu dùng `if-else` trong một method, code sẽ vi phạm Open/Closed Principle và khó mở rộng thêm phương thức thanh toán mới.

**HOW — Trong hệ thống:**

```
PaymentStrategy (interface)
    ├── VnPayStrategy    ← xử lý redirect + IPN
    └── CodStrategy      ← xử lý trực tiếp, không redirect

PaymentStrategyFactory → chọn strategy dựa trên PaymentMethod enum
```

```java
// PaymentController
PaymentStrategy strategy = strategyFactory.getStrategy(request.getMethod());
PaymentResult result = strategy.process(paymentData, clientIp);
```

Khi cần thêm phương thức thanh toán mới (ví dụ: MoMo), chỉ cần tạo `MoMoStrategy` implement interface `PaymentStrategy` — không cần sửa code hiện có.

### 2.2.3. JSON Web Token (JWT) và HMAC-SHA256

**WHAT:** JWT (RFC 7519) là một chuẩn mở để truyền thông tin an toàn giữa các bên dưới dạng đối tượng JSON được ký số. JWT gồm ba phần được mã hóa Base64Url và nối bởi dấu chấm:

```
Header.Payload.Signature
```

- **Header:** Loại token và thuật toán ký (ví dụ: `{"alg": "HS256", "typ": "JWT"}`).
- **Payload (Claims):** Dữ liệu về chủ thể và thông tin bổ sung (userId, roles, expiry).
- **Signature:** Chữ ký xác minh tính toàn vẹn.

**HOW — Công thức tạo Signature:**

Với thuật toán HMAC-SHA256 (HS256):

$$\text{Signature} = \text{HMAC-SHA256}(\text{Base64Url(Header)} + \text{"."} + \text{Base64Url(Payload)},\ \text{secret}) \quad (2.4)$$

Trong đó `secret` là khóa bí mật được lưu ở phía server (biến môi trường `JWT_SECRET_KEY`).

**WHEN — Luồng trong hệ thống:**

**Hình 2.4.** Luồng xác thực JWT trong hệ thống

```
[1] Đăng nhập: POST /api/v1/auth/login
      Server tạo JWT (24 giờ) → trả về Client

[2] Mỗi request tiếp theo:
      Client gửi: Authorization: Bearer <token>
      JwtAuthenticationFilter → giải mã → xác minh signature → extract userId, roles
      SecurityContext ← UserDetails

[3] Controller nhận userId đã xác thực qua @CurrentUserId annotation
```

**WHY — Ưu điểm của JWT trong hệ thống:**
- **Stateless:** Server không cần lưu session — phù hợp với kiến trúc REST.
- **Self-contained:** Token chứa đủ thông tin (userId, roles) để phân quyền mà không cần truy vấn database mỗi request.
- **Bảo mật WebSocket:** JWT được gửi trong STOMP CONNECT header, `JwtChannelInterceptor` xác thực và gán `StompPrincipal` cho kết nối WebSocket.

### 2.2.4. State Machine (Máy trạng thái hữu hạn)

**WHAT:** State Machine (FSM — Finite State Machine) là mô hình toán học mô tả một hệ thống có thể tồn tại ở một trong số hữu hạn các trạng thái, chuyển giữa các trạng thái theo quy tắc được định nghĩa trước khi nhận đầu vào (trigger).

Một State Machine được định nghĩa bởi bộ $(\Sigma, S, s_0, \delta, F)$ trong đó:
- $\Sigma$: Tập các sự kiện/trigger đầu vào.
- $S$: Tập hữu hạn các trạng thái.
- $s_0 \in S$: Trạng thái ban đầu.
- $\delta: S \times \Sigma \rightarrow S$: Hàm chuyển trạng thái.
- $F \subseteq S$: Tập các trạng thái kết thúc.

**WHY — Áp dụng cho Order Status:** Đơn hàng trong hệ thống TMĐT có vòng đời phức tạp với nhiều ràng buộc chuyển trạng thái. Ví dụ: không thể chuyển từ `DELIVERED` về `SHIPPED`, hay không thể `CONFIRM` một đơn chưa thanh toán. State Machine giúp mã hóa các ràng buộc này một cách rõ ràng và có thể kiểm thử được.

**HOW — State machine trong hệ thống:**

**Bảng 2.3.** Ma trận chuyển trạng thái đơn hàng

| Trạng thái hiện tại | Trạng thái tiếp theo hợp lệ | Điều kiện / Trigger |
|---|---|---|
| `PENDING` | `CONFIRMED` | VNPay IPN SUCCESS hoặc COD tạo đơn |
| `PENDING` | `PENDING` (giữ nguyên) | VNPay IPN FAILED — cho phép retry |
| `CONFIRMED` | `SHIPPED` | Vendor cập nhật — đang giao hàng |
| `CONFIRMED` | `CANCELLED` | Vendor/Admin hủy đơn |
| `SHIPPED` | `DELIVERED` | Vendor xác nhận đã giao |
| `SHIPPED` | `CANCELLED` | Trường hợp đặc biệt |
| `DELIVERED` | (không có) | Trạng thái kết thúc |
| `CANCELLED` | (không có) | Trạng thái kết thúc |

Logic kiểm tra hợp lệ được thực thi trong `OrderServiceImpl.updateVendorOrderStatus()` trước khi cho phép chuyển trạng thái.

**Hình 2.5.** Sơ đồ trạng thái đơn hàng

```
[INITIAL]
    ↓ Checkout thành công
  PENDING ──── VNPay/COD Success ────→ CONFIRMED
    │                                      │
    │ (payment failed,                     ├── Vendor xác nhận ──→ SHIPPED
    │  giữ nguyên để retry)                │                          │
    ↑                                      └── Vendor hủy ──→ CANCELLED ←──┘
                                                               │           │
                                                           [FINAL]     DELIVERED [FINAL]
                                                                            ↑
                                                                    Vendor xác nhận
                                                                    đã giao thành công
```

### 2.2.5. Observer / Event-Driven Pattern

**WHAT:** Observer Pattern là mẫu thiết kế trong đó một đối tượng (Subject/Publisher) duy trì danh sách các observer và tự động thông báo cho họ khi trạng thái thay đổi. Event-Driven Architecture là phiên bản mở rộng của Observer, trong đó các sự kiện được truyền qua một event bus thay vì liên kết trực tiếp.

**HOW — Spring Application Events trong hệ thống:**

```java
// Publisher (OrderServiceImpl) — không biết ai lắng nghe
applicationEventPublisher.publishEvent(new OrderShippedEvent(orderId, buyerId));

// Subscriber (OrderNotificationListener)
@TransactionalEventListener(phase = AFTER_COMMIT)
public void onOrderShipped(OrderShippedEvent event) {
    // Tạo thông báo cho Buyer
}
```

**Bảng 2.4.** Các sự kiện và listener trong hệ thống

| Sự kiện (Event) | Nguồn phát (Publisher) | Listener | Hành động |
|---|---|---|---|
| `OrderConfirmedEvent` | `PaymentGatewayService` | `OrderNotificationListener` | Thông báo buyer đơn đã xác nhận |
| `OrderShippedEvent` | `OrderServiceImpl` | `OrderNotificationListener` | Thông báo buyer đơn đang giao |
| `OrderDeliveredEvent` | `OrderServiceImpl` | `OrderNotificationListener` | Thông báo buyer đơn đã giao |
| `OrderCancelledEvent` | `OrderServiceImpl` | `OrderNotificationListener` | Thông báo buyer đơn đã hủy |
| `ReviewCreatedEvent` | `ReviewServiceImpl` | `ReviewNotificationListener` | Thông báo vendor có đánh giá mới |
| `ShopApprovedEvent` | `ShopServiceImpl` | `ShopNotificationListener` | Gửi email cho vendor qua Gmail SMTP |

### 2.2.6. Pessimistic Locking — Kiểm soát đồng thời

**WHAT:** Pessimistic Locking (khóa bi quan) là kỹ thuật kiểm soát đồng thời (concurrency control) trong đó một giao dịch khóa tài nguyên ngay khi truy cập, ngăn các giao dịch khác truy cập hoặc sửa đổi tài nguyên đó cho đến khi giao dịch đầu tiên hoàn thành.

**WHY — Vấn đề Race Condition trong IPN:** Khi thanh toán VNPay thành công, VNPay có thể gửi nhiều IPN request đến endpoint webhook gần như đồng thời (do retry mechanism). Nếu không có cơ chế khóa, hai request xử lý song song đều thấy `Payment.status = PENDING`, đều đổi thành `SUCCESS`, dẫn đến xử lý trùng lặp (double processing).

**HOW — Trong hệ thống:**

```java
// PaymentRepository
@Lock(LockModeType.PESSIMISTIC_WRITE)
@Query("SELECT p FROM Payment p WHERE p.transactionId = :txnId")
Optional<Payment> findByTransactionIdWithLock(@Param("txnId") String txnId);
```

Khi IPN thứ nhất đến, nó giữ lock. IPN thứ hai phải chờ. Khi IPN thứ nhất hoàn thành và release lock, IPN thứ hai vào kiểm tra thấy `status != PENDING` → dừng lại an toàn.

**Hình 2.6.** Cơ chế Pessimistic Lock ngăn Race Condition trong IPN

```
IPN Request 1 ──→ SELECT ... FOR UPDATE ──→ giữ lock
                                                 │ xử lý Payment (PENDING → SUCCESS)
IPN Request 2 ──→ SELECT ... FOR UPDATE ──→ WAIT ─────→ đọc status=SUCCESS → return
                                                 │ commit, release lock
```

### 2.2.7. HMAC-SHA512 — Xác minh tính toàn vẹn IPN

**WHAT:** HMAC (Hash-based Message Authentication Code) là cơ chế xác minh tính toàn vẹn và tính xác thực của một thông điệp, sử dụng hàm hash (SHA-512 trong trường hợp VNPay) kết hợp với một khóa bí mật.

**HOW — Công thức:**

$$\text{HMAC-SHA512}(K, M) = \text{SHA512}((K \oplus \text{opad}) \| \text{SHA512}((K \oplus \text{ipad}) \| M)) \quad (2.5)$$

Trong đó $K$ là khóa bí mật (`vnp_HashSecret`), $M$ là chuỗi dữ liệu cần xác minh, `opad` và `ipad` là các hằng số padding cố định.

**WHEN — Trong hệ thống:** Khi VNPay gửi IPN, hệ thống:
1. Lấy giá trị `vnp_SecureHash` từ request.
2. Loại bỏ field `vnp_SecureHash` khỏi danh sách params, sắp xếp theo key.
3. Tính lại HMAC-SHA512 của chuỗi params đã sắp xếp với `vnp_HashSecret`.
4. So sánh với `vnp_SecureHash` nhận được — nếu khớp, IPN hợp lệ và đến từ VNPay.

### 2.2.8. Optimistic UI Pattern

**WHAT:** Optimistic UI là kỹ thuật lập trình frontend trong đó giao diện cập nhật ngay lập tức (trước khi nhận phản hồi từ server), giả định rằng thao tác sẽ thành công. Nếu server phản hồi lỗi, giao diện mới rollback về trạng thái trước.

**WHY:** Trong ứng dụng chat, nếu chờ server xác nhận mới hiển thị tin nhắn, người dùng sẽ cảm nhận độ trễ mạng (latency) rõ rệt — trải nghiệm kém. Optimistic UI làm cho tin nhắn hiển thị tức thời (0ms latency theo góc nhìn người dùng), tạo cảm giác mượt mà như ứng dụng native.

**HOW — Trong hệ thống:**
1. Người dùng gửi tin nhắn → UI tạo một message object với ID âm tạm thời (ví dụ: `-Date.now()`) và hiển thị ngay.
2. STOMP SEND gửi tin nhắn lên server.
3. Server echo lại tin nhắn đã lưu (với ID thật từ database).
4. UI nhận echo → thay thế message với ID âm bằng message với ID thật.

---

## 2.3. Công nghệ sử dụng

### 2.3.1. Spring Boot (Backend Framework)

**Bảng 2.5.** Thông tin Spring Boot trong dự án

| Thuộc tính | Giá trị |
|---|---|
| Phiên bản | 4.1.0 |
| Ngôn ngữ | Java 21 |
| Build tool | Maven |

**Giới thiệu:** Spring Boot là framework phát triển ứng dụng Java dựa trên Spring Framework, cung cấp cấu hình tự động (auto-configuration), server nhúng (embedded Tomcat) và hệ sinh thái thư viện phong phú qua các "starter" dependency.

**Đặc điểm chính:**
- Auto-configuration: tự động cấu hình các bean dựa trên classpath.
- Convention over Configuration: giảm thiểu cấu hình thủ công.
- Actuator: cung cấp endpoint giám sát sức khỏe hệ thống.
- Tích hợp chặt chẽ với Spring Security, Spring Data JPA, Spring WebSocket.

**Vai trò trong đồ án:** Framework chính của backend, tổ chức 13 module nghiệp vụ, xử lý toàn bộ REST API và WebSocket.

**Lý do sử dụng:** Spring Boot là lựa chọn phổ biến nhất cho ứng dụng Java enterprise. Hệ sinh thái Spring cung cấp đầy đủ các module cần thiết (Security, Data JPA, WebSocket, Mail) trong một framework nhất quán, giảm thiểu tích hợp thủ công giữa các thư viện.

---

### 2.3.2. Spring Security + JWT (jjwt 0.12.5)

**Giới thiệu:** Spring Security là module bảo mật của Spring Framework, cung cấp xác thực và phân quyền cho ứng dụng Spring Boot. Thư viện `jjwt` (Java JWT) phiên bản 0.12.5 của io.jsonwebtoken được dùng để tạo và xác minh JWT token.

**Đặc điểm chính:**
- Filter chain: chuỗi filter xử lý request theo thứ tự trước khi đến Controller.
- `@PreAuthorize`: phân quyền tại method level bằng Spring Expression Language.
- `OncePerRequestFilter`: base class cho `JwtAuthenticationFilter` — đảm bảo chỉ chạy một lần mỗi request.

**Vai trò trong đồ án:** Xác thực JWT mọi request đến (trừ endpoint công khai), phân quyền theo ba vai trò (ROLE_USER, ROLE_VENDOR, ROLE_ADMIN), bảo vệ WebSocket STOMP channel.

**Lý do sử dụng:** Spring Security là tiêu chuẩn de facto cho bảo mật ứng dụng Spring. Tích hợp sâu với Spring MVC giúp phân quyền trở nên khai báo (declarative) thay vì lập trình thủ công (imperative).

---

### 2.3.3. Spring Data JPA + Hibernate

**Giới thiệu:** Spring Data JPA cung cấp abstraction layer phía trên JPA (Java Persistence API), giúp đơn giản hóa truy cập cơ sở dữ liệu quan hệ. Hibernate là ORM implementation của JPA được sử dụng trong hệ thống.

**Đặc điểm chính:**
- Repository pattern: `JpaRepository<Entity, ID>` tự động tạo các query CRUD.
- JPQL/HQL: ngôn ngữ query hướng đối tượng, độc lập với database vendor.
- `@BatchSize`: kiểm soát Hibernate batch loading để tránh N+1.
- `@Modifying`: cho phép bulk UPDATE/DELETE qua JPQL.

**Vai trò trong đồ án:** Ánh xạ 20 Entity sang 20 bảng MySQL, quản lý toàn bộ persistence layer của hệ thống.

**Lý do sử dụng:** Spring Data JPA giảm thiểu boilerplate code (không cần viết `findById`, `save`, `delete` thủ công). Repository interface đủ để xử lý hầu hết các query đơn giản; JPQL xử lý các query phức tạp.

---

### 2.3.4. Spring WebSocket + STOMP

**Giới thiệu:** Spring Boot Starter WebSocket cung cấp hỗ trợ WebSocket với SockJS fallback và STOMP messaging protocol, thông qua `spring-boot-starter-websocket`.

**Đặc điểm chính:**
- SockJS fallback: nếu trình duyệt không hỗ trợ WebSocket thuần, SockJS mô phỏng kết nối WebSocket qua HTTP long-polling.
- `MessageBroker`: định tuyến STOMP message trong server.
- `UserDestinationMessageHandler`: ánh xạ `/user/{id}/queue/messages` đến đúng STOMP session.
- `ChannelInterceptor`: interceptor cho các STOMP command (CONNECT, SEND, SUBSCRIBE).

**Vai trò trong đồ án:** Cung cấp hạ tầng realtime cho module chat — kết nối WebSocket STOMP giữa Buyer và Vendor.

**Lý do sử dụng:** Spring WebSocket tích hợp tự nhiên với Spring Security và Spring Boot, hỗ trợ User Destination routing sẵn có — giảm đáng kể công sức triển khai P2P messaging.

---

### 2.3.5. MySQL

**Bảng 2.6.** Thông tin MySQL trong dự án

| Thuộc tính | Giá trị |
|---|---|
| Loại | RDBMS (Relational Database Management System) |
| Driver | mysql-connector-j (runtime) |
| Schema | `ecommerce` |
| Số bảng | 20 |

**Giới thiệu:** MySQL là hệ quản trị cơ sở dữ liệu quan hệ mã nguồn mở, sử dụng SQL làm ngôn ngữ truy vấn.

**Đặc điểm chính:**
- ACID compliance: đảm bảo tính toàn vẹn giao dịch.
- InnoDB storage engine: hỗ trợ row-level locking, foreign keys, và MVCC (Multi-Version Concurrency Control).
- Index support: B-Tree index cho tìm kiếm nhanh.
- `SELECT ... FOR UPDATE`: hỗ trợ Pessimistic Lock.

**Vai trò trong đồ án:** Cơ sở dữ liệu duy nhất của hệ thống, lưu trữ toàn bộ dữ liệu của 20 bảng từ 13 module.

**Lý do sử dụng:** MySQL là RDBMS phổ biến nhất trong phát triển web, đặc biệt kết hợp với Java/Spring. Dữ liệu TMĐT có tính quan hệ cao (User → Order → OrderItem → Product) — phù hợp với mô hình quan hệ hơn là NoSQL.

---

### 2.3.6. Cloudinary SDK (cloudinary-http44 1.36.0)

**Giới thiệu:** Cloudinary là dịch vụ cloud lưu trữ và quản lý media (ảnh, video), cung cấp Java SDK để tích hợp upload/delete.

**Đặc điểm chính:**
- CDN tích hợp: ảnh được phân phối qua CDN toàn cầu, tải nhanh.
- Transform on-the-fly: thay đổi kích thước, định dạng ảnh qua URL parameter.
- Secure delete: mỗi tài nguyên có `publicId` duy nhất để xóa.

**Vai trò trong đồ án:** Lưu trữ ảnh sản phẩm và ảnh đánh giá. Hệ thống không lưu ảnh trên local server — toàn bộ lưu trên Cloudinary và chỉ lưu URL vào database.

**Lý do sử dụng:** Giải quyết vấn đề lưu trữ ảnh mà không cần quản lý hạ tầng file server. Cloudinary cung cấp free tier đủ cho môi trường phát triển.

**Kỹ thuật Zero-Trust publicId Deletion:** Khi xóa ảnh, hệ thống lấy `publicId` từ database (không nhận từ client). Điều này ngăn client giả mạo `publicId` để xóa ảnh của người khác. Đồng thời, việc xóa Cloudinary được đặt sau commit MySQL (`afterCommit`) để đảm bảo: nếu MySQL transaction rollback, Cloudinary cũng không bị xóa nhầm.

---

### 2.3.7. Springdoc OpenAPI (springdoc-openapi-starter-webmvc-ui 3.0.3)

**Giới thiệu:** Springdoc OpenAPI tự động tạo tài liệu API theo chuẩn OpenAPI 3.0 từ annotation trong Controller, và cung cấp giao diện Swagger UI tương tác.

**Vai trò trong đồ án:** Cung cấp tài liệu API tự động truy cập tại `http://localhost:8080/swagger-ui.html`, phục vụ kiểm thử API trong quá trình phát triển và làm phụ lục tài liệu cho báo cáo.

---

### 2.3.8. React + TypeScript + Vite (Frontend)

**Bảng 2.7.** Thông tin Frontend stack trong dự án

| Công nghệ | Phiên bản | Vai trò |
|---|---|---|
| React | 19.2.8 | UI library |
| TypeScript | ~6.0.2 | Type-safe JavaScript |
| Vite | 8.2.0 | Build tool + dev server |
| React Router DOM | 7.18.2 | Client-side routing |
| Axios | 1.19.0 | HTTP client |
| Tailwind CSS | 4.3.3 | Utility-first CSS framework |

**React:**

**Giới thiệu:** React là thư viện JavaScript mã nguồn mở do Meta phát triển, dùng để xây dựng giao diện người dùng theo mô hình component-based.

**Đặc điểm chính:**
- Component-based: UI được xây dựng từ các component tái sử dụng.
- Virtual DOM: React duy trì bản sao ảo của DOM, chỉ cập nhật phần thay đổi thực sự — hiệu năng cao.
- Hooks: `useState`, `useEffect`, `useContext`, `useCallback` — quản lý state và side effects.
- SPA (Single Page Application): không tải lại trang khi điều hướng.

**Vai trò trong đồ án:** Xây dựng toàn bộ giao diện người dùng, bao gồm 24 trang cho ba nhóm: Buyer, Vendor và Admin.

**TypeScript:**

**Giới thiệu:** TypeScript là ngôn ngữ lập trình mở rộng của JavaScript, bổ sung hệ thống kiểu tĩnh (static type system).

**Vai trò trong đồ án:** Kiểm tra kiểu tại compile-time, phát hiện lỗi trước khi chạy. Đặc biệt quan trọng khi định nghĩa kiểu cho API response DTO, đảm bảo frontend và backend có cùng hiểu biết về cấu trúc dữ liệu.

**Vite:**

**Giới thiệu:** Vite là công cụ build frontend thế hệ mới, sử dụng ES modules native và esbuild để cung cấp Hot Module Replacement (HMR) cực nhanh trong quá trình phát triển.

**Vai trò trong đồ án:** Dev server với HMR, build production bundle.

**Lý do sử dụng React + TypeScript + Vite:** Ba công nghệ này là stack frontend hiện đại và phổ biến nhất hiện nay. Vite thay thế webpack truyền thống với khởi động dev server nhanh hơn đáng kể.

---

### 2.3.9. STOMP.js + SockJS Client (Frontend WebSocket)

**Bảng 2.8.** Thư viện WebSocket phía Frontend

| Thư viện | Phiên bản | Vai trò |
|---|---|---|
| @stomp/stompjs | 7.3.0 | STOMP protocol client |
| sockjs-client | 1.6.1 | WebSocket fallback transport |

**Giới thiệu:**
- **@stomp/stompjs:** Thư viện JavaScript implement STOMP protocol, cho phép kết nối và giao tiếp với Spring WebSocket STOMP server.
- **sockjs-client:** Thư viện cung cấp WebSocket-compatible API với fallback về HTTP long-polling nếu WebSocket bị block.

**Vai trò trong đồ án:** Hai thư viện này phối hợp tạo kết nối WebSocket STOMP từ trình duyệt đến backend. `websocketService` (singleton) quản lý kết nối, tự động reconnect khi mất kết nối.

---

### 2.3.10. Axios (HTTP Client)

**Giới thiệu:** Axios là thư viện HTTP client promise-based cho JavaScript, hỗ trợ cả browser và Node.js.

**Đặc điểm chính trong hệ thống:**
- Interceptor: tự động gắn `Authorization: Bearer <token>` vào mọi request sau khi đăng nhập.
- Response interceptor: xử lý tập trung lỗi 401 (token hết hạn → redirect về login).

**Vai trò trong đồ án:** Toàn bộ giao tiếp HTTP giữa React frontend và Spring Boot backend đi qua Axios, cấu hình với base URL `http://localhost:8080`.

---

### 2.3.11. VNPay (Payment Gateway)

**Giới thiệu:** VNPay là cổng thanh toán trực tuyến phổ biến tại Việt Nam, hỗ trợ thanh toán qua thẻ ngân hàng nội địa, thẻ quốc tế và ví điện tử.

**Đặc điểm kỹ thuật:**
- **Redirect payment model:** Người mua được chuyển hướng đến trang VNPay để nhập thông tin thẻ, sau đó redirect về hệ thống.
- **IPN Webhook:** VNPay gọi URL callback để xác nhận kết quả — độc lập với redirect.
- **Xác minh HMAC-SHA512:** Mọi request và response đều có chữ ký để xác minh tính toàn vẹn.
- **Môi trường Sandbox:** Cho phép kiểm thử với thẻ test mà không giao dịch tiền thực.

**Vai trò trong đồ án:** Phương thức thanh toán trực tuyến chính. Hệ thống tích hợp VNPay Sandbox để demo luồng thanh toán end-to-end.

**Lý do sử dụng:** VNPay là một trong những cổng thanh toán phổ biến nhất tại Việt Nam, cung cấp sandbox environment miễn phí cho developer — phù hợp cho mục đích học tập và demo.

---

### 2.3.12. Gmail SMTP (Email Service)

**Giới thiệu:** Gmail SMTP được sử dụng thông qua `spring-boot-starter-mail` để gửi email thông báo.

**Vai trò trong đồ án:** Gửi email thông báo cho Vendor khi gian hàng được Admin duyệt (`ShopApprovedEvent`). Email được gửi bất đồng bộ (async) qua `AsyncConfig` để không block giao dịch chính.

---

### 2.3.13. Bảng tổng hợp công nghệ

**Bảng 2.9.** Tổng hợp technology stack của hệ thống Enterprise Ecommerce

| Công nghệ | Phiên bản | Nhóm | Vai trò |
|---|---|---|---|
| Java | 21 | Backend | Ngôn ngữ lập trình chính |
| Spring Boot | 4.1.0 | Backend | Framework chính |
| Spring Web MVC | (starter) | Backend | REST API |
| Spring Data JPA + Hibernate | (starter) | Backend | ORM / Persistence |
| Spring Security | (starter) | Backend | Authentication & Authorization |
| Spring WebSocket | (starter) | Backend | Realtime chat (STOMP) |
| Spring Mail | 4.0.1 | Backend | Email notification |
| Spring Actuator | (starter) | Backend | Health monitoring |
| jjwt | 0.12.5 | Backend | JWT tạo và xác minh token |
| Springdoc OpenAPI | 3.0.3 | Backend | API documentation (Swagger UI) |
| Cloudinary SDK | 1.36.0 | Backend | Cloud image storage |
| Lombok | (latest) | Backend | Code generation (boilerplate reduction) |
| MySQL | — | Database | RDBMS chính |
| React | 19.2.8 | Frontend | UI library |
| TypeScript | ~6.0.2 | Frontend | Type-safe JavaScript |
| Vite | 8.2.0 | Frontend | Build tool + dev server |
| React Router DOM | 7.18.2 | Frontend | Client-side routing |
| Axios | 1.19.0 | Frontend | HTTP client |
| Tailwind CSS | 4.3.3 | Frontend | Utility-first CSS |
| @stomp/stompjs | 7.3.0 | Frontend | WebSocket STOMP client |
| sockjs-client | 1.6.1 | Frontend | WebSocket fallback |
| VNPay Sandbox | — | External | Payment gateway |
| Gmail SMTP | — | External | Email delivery |
| Maven | — | Build | Dependency management |

---

## 2.4. Các nghiên cứu, hệ thống liên quan

### 2.4.1. Kiến trúc Modular Monolith — Bối cảnh học thuật

Khái niệm Modular Monolith nổi lên như một giải pháp thực tiễn sau giai đoạn "Microservices mania" trong cộng đồng phát triển phần mềm. Nhiều tổ chức nhận ra rằng việc chuyển đổi sớm sang Microservices khi hệ thống còn nhỏ mang lại nhiều chi phí vận hành hơn là lợi ích về khả năng mở rộng.

Một số tài liệu học thuật và thực tiễn đáng tham khảo:

- **"Monolith First" (Martin Fowler, 2015)** \[*[cần bổ sung nguồn trích dẫn đầy đủ]*\]: Đề xuất nên bắt đầu với Monolith có tổ chức tốt trước khi chuyển sang Microservices khi thực sự cần thiết.
- **"Building Microservices" (Sam Newman, 2015)** \[*[cần bổ sung nguồn trích dẫn đầy đủ]*\]: Nhấn mạnh tầm quan trọng của ranh giới ngữ nghĩa (semantic boundaries) — nền tảng của cả Microservices lẫn Modular Monolith.

### 2.4.2. Event-Driven Architecture trong hệ thống phân tán

Event-Driven Architecture (EDA) được áp dụng rộng rãi trong hệ thống TMĐT quy mô lớn để tách rời (decouple) các thành phần và tăng khả năng mở rộng. Trong hệ thống đề tài, EDA được triển khai ở mức trong quá trình (in-process) thông qua Spring Application Events, phù hợp với quy mô Modular Monolith.

Tài liệu tham khảo:

- **Spring Framework Reference — Application Events** \[*[cần bổ sung URL/phiên bản đầy đủ]*\]: Tài liệu chính thức mô tả `ApplicationEventPublisher` và `@TransactionalEventListener`.

### 2.4.3. Hệ thống TMĐT tương tự — Phương diện kỹ thuật

> **Lưu ý:** Phần này tổng hợp từ tài liệu công khai của các nền tảng TMĐT. Các kiến trúc nội bộ thực tế của Shopee, Tiki không được công bố đầy đủ. Thông tin chỉ mang tính tham khảo từ các bài viết kỹ thuật và blog engineering.

Các nền tảng TMĐT lớn tại Việt Nam và khu vực áp dụng nhiều kỹ thuật tương tự được sử dụng trong đề tài:

- **Tách đơn hàng đa người bán:** Là cơ chế cốt lõi của bất kỳ sàn Multi-Vendor nào. Shopee và Tiki đều thực hiện tách đơn và liên kết qua payment session.
- **Payment IPN/Webhook:** Tất cả tích hợp cổng thanh toán nghiêm túc đều sử dụng server-side callback thay vì chỉ dựa vào redirect URL.
- **Optimistic UI cho chat:** Shopee Chat sử dụng Optimistic UI, tin nhắn hiển thị trước khi được server xác nhận — đây là UX pattern phổ biến trong ứng dụng messaging hiện đại.
- **Event-driven notification:** Hệ thống thông báo của các nền tảng lớn thường dùng message queue (Kafka, RabbitMQ) thay vì Spring Events; tuy nhiên nguyên lý tách biệt (decoupling) là tương đồng.

> *[Ghi chú cho tác giả: Bổ sung tài liệu tham khảo học thuật hoặc bài báo kỹ thuật cụ thể khi có, ví dụ: IEEE transactions, ACM papers về ecommerce architecture hoặc payment systems. Mục này có thể được mở rộng dựa trên tài liệu tham khảo do giảng viên hướng dẫn yêu cầu.]*

---

*Kết thúc Chương 2.*
