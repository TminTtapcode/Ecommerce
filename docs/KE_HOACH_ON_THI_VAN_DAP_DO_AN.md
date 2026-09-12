# KẾ HOẠCH ÔN TẬP & CẨM NANG VẤN ĐÁP BẢO VỆ ĐỒ ÁN
## TỪ CƠ BẢN ĐẾN LÀM CHỦ TOÀN BỘ SOURCE CODE & HỆ THỐNG

> **Dành cho:** Sinh viên chuẩn bị bảo vệ đồ án tốt nghiệp / đồ án chuyên ngành.  
> **Mục tiêu:** Hiểu bản chất từng dòng code, đọc vanh vách cú pháp (`useState`, `async/await`, `@Transactional`, Java Streams), nắm vững kiến trúc Modular Monolith và tự tin trả lời xuất sắc mọi câu hỏi phản biện của Hội đồng.  

---

## MỤC LỤC BÀI HỌC

1. [Tư duy & Chiến lược Vấn đáp với Hội đồng](#1-tư-duy--chiến-lược-vấn-đáp-với-hội-đồng)
2. [Phần 1: Cẩm nang Cú pháp "Đọc Hiểu Bản Chất Code"](#phần-1-cẩm-nang-cú-pháp-đọc-hiểu-bản-chất-code)
   - [1.1. Phía Frontend: React 19, TypeScript & JavaScript ES6+](#11-phía-frontend-react-19-typescript--javascript-es6)
   - [1.2. Phía Backend: Java 21 & Spring Boot 3.4.x](#12-phía-backend-java-21--spring-boot-34x)
3. [Phần 2: Lộ trình 5 Chặng "Chia Để Trị" Hệ Thống](#phần-2-lộ-trình-5-chặng-chia-để-trị-hệ-thống)
   - [Chặng 1: Nền tảng Cơ sở dữ liệu (22 Bảng & Các Pattern thiết kế)](#chặng-1-nền-tảng-cơ-sở-dữ-liệu-22-bảng--các-pattern-thiết-kế)
   - [Chặng 2: Luồng Xác thực & Phân quyền (Identity & JWT Lifecycle)](#chặng-2-luồng-xác-thực--phân-quyền-identity--jwt-lifecycle)
   - [Chặng 3: Luồng Khách hàng (Buyer Journey: Giỏ hàng $\rightarrow$ Checkout $\rightarrow$ VNPay $\rightarrow$ Đã nhận hàng $\rightarrow$ Review)](#chặng-3-luồng-khách-hàng-buyer-journey)
   - [Chặng 4: Luồng Người bán (Vendor Journey: Đăng ký $\rightarrow$ Đăng SP $\rightarrow$ Duyệt đơn $\rightarrow$ Doanh thu)](#chặng-4-luồng-người-bán-vendor-journey)
   - [Chặng 5: Các tính năng Kỹ thuật Ăn điểm (STOMP Chat, Gợi ý, Refund VNPay)](#chặng-5-các-tính-năng-kỹ-thuật-ăn-điểm)
4. [Phần 3: Bóc Tách Chi Tiết Từng Dòng Code Trong Các Hàm Cốt Lõi](#phần-3-bóc-tách-chi-tiết-từng-dòng-code-trong-các-hàm-cốt-lõi)
   - [Hàm 1: `OrderServiceImpl.checkout(...)` (Đặt hàng chia shop)](#hàm-1-orderserviceimplcheckout-đặt-hàng-chia-shop)
   - [Hàm 2: `PaymentGatewayService.processVnPayIpn(...)` (Webhook IPN)](#hàm-2-paymentgatewayserviceprocessvnpayipn-webhook-ipn)
   - [Hàm 3: `CartServiceImpl.addToCart(...)` (Khóa giỏ hàng & Aggregate)](#hàm-3-cartserviceimpladdtocart-khóa-giỏ-hàng--aggregate)
   - [Hàm 4: Frontend `CheckoutPage.tsx` & Gọi API](#hàm-4-frontend-checkoutpagetsx--gọi-api)
   - [Hàm 5: `ChatServiceImpl.sendMessage(...)` (Realtime STOMP)](#hàm-5-chatserviceimplsendmessage-realtime-stomp)
5. [Phần 4: 25 Câu Hỏi Vấn Đáp Thường Gặp & Cách Trả Lời Ghi Điểm 10](#phần-4-25-câu-hỏi-vấn-đáp-thường-gặp--cách-trả-lời-ghi-điểm-10)

---

# 1. TƯ DUY & CHIẾN LƯỢC VẤN ĐÁP VỚI HỘI ĐỒNG

Thầy cô chấm đồ án thường đánh giá sinh viên dựa trên 3 tiêu chí:
1. **Có phải do chính em tự làm / hiểu code không?** (Hội đồng sẽ chỉ vào 1 dòng code bất kỳ hoặc 1 annotation và hỏi: *"Dòng này để làm gì? Bỏ dòng này đi thì hệ thống bị sao?"*).
2. **Tại sao lại thiết kế như vậy?** (Hội đồng hỏi lý do kiến trúc: *"Tại sao dùng Modular Monolith?", "Tại sao không dùng Foreign Key giữa orders và users?"*).
3. **Xử lý các ca biên (Edge cases & Concurrency):** *"Hai người cùng mua 1 món hàng cuối cùng thì ai được ai mất?", "Thanh toán VNPay trừ tiền rồi nhưng rớt mạng thì xử lý sao?"*.

> **Nguyên tắc vàng khi trả lời:**
> - Luôn trả lời theo cấu trúc: **[Khái niệm/Bản chất]** $\rightarrow$ **[Vị trí áp dụng trong code]** $\rightarrow$ **[Lý do kỹ thuật/Lợi ích mang lại]**.
> - Không học vẹt lý thuyết suông; luôn liên hệ trực tiếp với tên file và tên hàm trong project.

---

# PHẦN 1: CẨM NANG CÚ PHÁP "ĐỌC HIỂU BẢN CHẤT CODE"

## 1.1. Phía Frontend: React 19, TypeScript & JavaScript ES6+

### 1. `useState` là gì? Tại sao phải dùng?
- **Bản chất:** Trong JavaScript thuần, khi em đổi giá trị của một biến `let count = 1; count = 2;`, giao diện HTML **không tự cập nhật**. `useState` là một **React Hook** khai báo một biến trạng thái (`state`). Khi biến này thay đổi thông qua hàm setter, React sẽ **tự động kích hoạt Render lại (Re-render)** component trên màn hình.
- **Cú pháp trong code dự án:**
  ```tsx
  // Khai báo state 'orders' có kiểu mảng OrderResponse, giá trị khởi tạo là mảng rỗng []
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState<boolean>(true);

  // Khi gọi API xong, cập nhật state -> Giao diện tự động vẽ lại danh sách đơn hàng
  setOrders(response.data);
  setLoading(false);
  ```

---

### 2. `useEffect` là gì? Dependency Array hoạt động thế nào?
- **Bản chất:** Dùng để thực hiện **Side-Effects** (các tác vụ phụ bên ngoài quá trình render) như: Gọi API qua mạng, đăng ký lắng nghe sự kiện, hoặc kết nối WebSocket.
- **Quy tắc Dependency Array (Mảng phụ thuộc `[]`):**
  1. `useEffect(() => { ... }, [])`: **Mảng rỗng** $\rightarrow$ Chỉ chạy **duy nhất 1 lần** khi Component vừa xuất hiện (Mount). Thường dùng để nạp dữ liệu ban đầu.
  2. `useEffect(() => { ... }, [status, page])`: **Có biến phụ thuộc** $\rightarrow$ Chạy lần đầu và chạy lại **mỗi khi giá trị của `status` hoặc `page` thay đổi**.
  ```tsx
  // Ví dụ trong OrderHistoryPage.tsx:
  useEffect(() => {
    fetchOrders(); // Mỗi khi người dùng bấm chọn tab Status khác hoặc đổi Trang, tự động gọi lại hàm lấy dữ liệu mới
  }, [selectedStatus, currentPage]);
  ```

---

### 3. `async` / `await` và `Promise` là gì?
- **Bản chất:** JavaScript chạy đơn luồng (Single-thread). Khi gửi yêu cầu mạng lên Backend, mất khoảng vài chục mili-giây đến 1 giây để server phản hồi. Nếu chương trình dừng lại đợi thì trang web sẽ bị "đơ" (freeze).
- **Giải pháp:** `async` đánh dấu một hàm bất đồng bộ. Từ khóa `await` yêu cầu JavaScript chờ kết quả trả về từ Backend **nhưng không làm đơ giao diện**, trong lúc chờ nó vẫn xử lý các animation hoặc thao tác click khác.
- **Ví dụ chuẩn trong dự án:**
  ```tsx
  const handleCheckout = async () => {
    try {
      setLoading(true);
      // await tạm dừng hàm này cho đến khi server trả lời, rồi gán kết quả vào res
      const res = await orderApi.checkout(payload);
      // Nếu thành công thì chuyển trang
      navigate(`/payment-result?groupId=${res.data.paymentGroupId}`);
    } catch (err: any) {
      // Bắt lỗi nếu mạng rớt hoặc server trả về 400 Bad Request
      toast.error(err.response?.data?.message || "Đặt hàng thất bại!");
    } finally {
      // Luôn luôn tắt trạng thái xoay vòng loading dù thành công hay thất bại
      setLoading(false);
    }
  };
  ```

---

### 4. Custom Hook & React Context (`useAuth`, `AuthContext`)
- **Vấn đề:** Nếu trang nào cũng phải kiểm tra token trong `localStorage`, code sẽ bị lặp lại hàng chục lần (Prop Drilling).
- **Giải pháp:** `AuthContext` tạo một vùng dữ liệu chung cho toàn ứng dụng. `useAuth()` là một Custom Hook giúp bất kỳ component con nào (Header, CheckoutPage, VendorOrderListPage) cũng có thể lấy được:
  `const { user, isAuthenticated, logout } = useAuth();`

---

### 5. Toán tử TypeScript thường gặp trong code
- **Optional Chaining (`?.`):** `order?.shippingAddress` $\rightarrow$ Nếu `order` là `null` hoặc `undefined`, code không bị văng lỗi màn hình trắng crash ứng dụng (Cannot read properties of null), mà trả về `undefined`.
- **Nullish Coalescing (`??`):** `total ?? 0` $\rightarrow$ Nếu `total` là `null` hoặc `undefined`, lấy giá trị mặc định là `0`.
- **Destructuring:** `const { id, name } = product;` $\rightarrow$ Bóc tách trực tiếp thuộc tính `id` và `name` từ object.

---

## 1.2. Phía Backend: Java 21 & Spring Boot 3.4.x

### 1. Kiến trúc 3 tầng chuẩn của Spring Boot
```
Client (HTTP Request) 
      ↓
[Controller] (@RestController): Tiếp nhận Request, validate dữ liệu (@Valid), định tuyến URL
      ↓
[Service] (@Service): Xử lý toàn bộ logic nghiệp vụ (Kiểm tra kho, tính tiền, gọi module khác)
      ↓
[Repository] (@Repository): Giao tiếp Database qua Spring Data JPA / Hibernate (SQL queries)
      ↓
MySQL Database
```

---

### 2. Các Annotation cốt lõi & Bản chất kỹ thuật

| Annotation | Bản Chất & Ý Nghĩa Kỹ Thuật | Nếu Thầy/Cô hỏi: "Bỏ đi thì sao?" |
|---|---|---|
| `@RestController` | Kết hợp giữa `@Controller` và `@ResponseBody`. Mọi dữ liệu trả về từ hàm sẽ tự động được chuyển hóa thành JSON gửi về cho Client. | Nếu bỏ đi, Spring sẽ hiểu trả về tên tệp giao diện HTML (JSP/Thymeleaf) $\rightarrow$ Lỗi 404. |
| `@RequiredArgsConstructor` (Lombok) | Tự động sinh một Constructor chứa tất cả các biến có từ khóa `private final`. | Đây là cơ chế **Constructor Dependency Injection** chuẩn nhất. Nếu bỏ đi, các Service/Repository sẽ bị `null` $\rightarrow$ Ném lỗi `NullPointerException`. |
| `@Transactional` | Đảm bảo nguyên tắc **ACID**. Mọi câu lệnh SQL trong hàm chạy trong 1 Transaction duy nhất. Nếu có bất kỳ lỗi RuntimeException nào xảy ra, toàn bộ dữ liệu sẽ được **Rollback** về trạng thái ban đầu. | Nếu bỏ đi: Khi đặt hàng, kho bị trừ nhưng lưu đơn thất bại $\rightarrow$ Khách mất tồn kho nhưng không có đơn hàng (hỏng dữ liệu). |
| `@PreAuthorize("hasRole('VENDOR')")` | Bảo vệ API ở mức hàm (Method Security). Chặn đứng người dùng nếu trong JWT Token không có quyền tương ứng, trả về HTTP 403 Forbidden. | Nếu bỏ đi: Bất kỳ ai biết URL cũng có thể gọi API duyệt đơn hoặc xóa sản phẩm của người khác. |
| `@CurrentUserId` | Custom Annotation do dự án tự viết. Tự động lấy `userId` từ `SecurityContext` (do JWT Filter giải mã) truyền thẳng vào tham số hàm. | Tiết kiệm công sức, không phải giải mã thủ công request header ở từng Controller. |
| `@Entity` & `@Table` | Đánh dấu class Java này là một bảng trong cơ sở dữ liệu MySQL (JPA ánh xạ quan hệ). | Không có annotation này thì Hibernate sẽ không nhận diện được bảng. |

---

### 3. Java 21 Record vs Class thông thường
Trong toàn bộ DTO của dự án (`CheckoutRequest`, `ProductCreateRequest`), ta dùng:
`public record CheckoutRequest(List<Long> cartItemIds, String shippingAddress, String paymentMethod, String voucherCode) {}`
- **Tại sao dùng Record?**
  1. Record là tính năng hiện đại của Java, tạo ra đối tượng **Immutable (Bất biến)**: Dữ liệu sau khi tạo không thể bị sửa đổi lén lút, cực kỳ an toàn cho DTO.
  2. Tự động sinh `getter`, `equals()`, `hashCode()`, `toString()` ngắn gọn chỉ trong 1 dòng code, không cần viết rườm rà hay phụ thuộc Lombok.

---

### 4. Java Streams API (Biểu thức Lambda)
Hệ thống sử dụng rất nhiều Java Stream để xử lý dữ liệu sạch sẽ:
```java
// Ví dụ gom nhóm các món trong giỏ hàng theo Shop ID:
Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
    .collect(Collectors.groupingBy(CartItemInternalDto::shopId));
```
- *Giải thích:* Lấy danh sách `cartItems`, mở luồng dữ liệu (`.stream()`), nhóm (`groupingBy`) theo hàm trích xuất `shopId`. Kết quả là một Map: Khóa là `shopId`, Giá trị là danh sách các món của shop đó.

---

# PHẦN 2: LỘ TRÌNH 5 CHẶNG "CHIA ĐỂ TRỊ" HỆ THỐNG

Để không bị "ngợp" trước toàn bộ dự án, bạn hãy ôn tập theo đúng 5 chặng sau:

```
[Chặng 1: Database 22 Bảng] 
          ↓
[Chặng 2: Auth & JWT Security] 
          ↓
[Chặng 3: Luồng Khách Mua (Buyer Flow)] 
          ↓
[Chặng 4: Luồng Người Bán (Vendor Flow)] 
          ↓
[Chặng 5: Tính năng Nâng cao (Realtime, Refund, Recommendation)]
```

---

## Chặng 1: Nền tảng Cơ sở dữ liệu (22 Bảng & Các Pattern thiết kế)

Học thuộc 3 Pattern quan trọng nhất trong Database của mình:

### 1. Pattern 1: Loose Coupling (Liên kết lỏng đa module)
- **Hội đồng hỏi:** *"Tại sao giữa bảng `orders` và bảng `users` không có FOREIGN KEY?"*
- **Trả lời:** *"Dạ thưa thầy/cô, hệ thống của em thiết kế theo kiến trúc **Modular Monolith**. Để đảm bảo ranh giới độc lập giữa module `order` và module `identity`, em áp dụng Loose DB Coupling: Không dùng FK cấp cơ sở dữ liệu để tránh ràng buộc khóa cứng. Mọi kiểm tra toàn vẹn được thực hiện ở tầng Service thông qua `IdentityInternalService`. Thiết kế này giúp hệ thống sẵn sàng phân tách thành Microservices trong tương lai mà không phải đập bỏ database."*

### 2. Pattern 2: Snapshot Pattern (Lưu vết lịch sử trong `order_items`)
- **Hội đồng hỏi:** *"Tại sao trong `order_items` em lại lưu `unit_price`, `product_name`, `image_url` thay vì JOIN sang `product_variants` để lấy?"*
- **Trả lời:** *"Dạ vì giá và tên sản phẩm của người bán có thể thay đổi sau này. Nếu JOIN động, khi Vendor tăng giá từ 100k lên 200k, lịch sử đơn hàng cũ của khách hàng sẽ bị hiển thị sai giá và báo cáo doanh thu tài chính bị sai lệch. Do đó, em snapshot lại giá, tên và ảnh chính xác tại thời điểm khách bấm nút Mua."*

### 3. Pattern 3: UUID Payment Grouping
- **Hội đồng hỏi:** *"Khi khách mua hàng từ 3 shop khác nhau trong 1 giỏ hàng thì lưu thế nào?"*
- **Trả lời:** *"Dạ hệ thống sẽ tự động tách thành 3 bản ghi trong bảng `orders` (mỗi shop 1 đơn riêng để Vendor tự quản lý), nhưng cả 3 đơn này đều được gán chung một mã UUID `payment_group_id`. Khi thanh toán VNPay, em chỉ cần gửi mã `payment_group_id` này với tổng số tiền của cả 3 đơn."*

---

## Chặng 2: Luồng Xác thực & Phân quyền (Identity & JWT Lifecycle)

```
[Client] --(POST email, pwd)--> [AuthController] 
                                       ↓
                           [AuthenticationManager] (BCrypt check)
                                       ↓
                             [JwtService] (Sinh Token 24h)
                                       ↓
[Client] <--(Token JWT)----------------+
   ↓
(Lưu localStorage, đính kèm header "Authorization: Bearer <token>" ở mọi request sau)
```

- **File cần nắm:**
  - `AuthController.java` & `AuthenticationService.java`
  - `JwtService.java` & `JwtAuthenticationFilter.java`
  - `CustomUserDetails.java` & `SecurityConfig.java`
- **Các Role trong hệ thống:** `ROLE_USER` (Khách mua), `ROLE_VENDOR` (Người bán), `ROLE_ADMIN` (Quản trị viên).
- **Điểm ăn điểm:** Khi người dùng mở shop thành công (`approveShop`), hệ thống tự động cấp thêm quyền `ROLE_VENDOR` vào danh sách roles của user đó.

---

## Chặng 3: Luồng Khách hàng (Buyer Journey)

Đây là xương sống của đồ án. Hãy thuộc lòng chuỗi 5 bước:

1. **Xem hàng & Gợi ý (`ProductController`):**
   - Tìm kiếm phân trang, lọc giá, danh mục.
   - Gợi ý sản phẩm thông minh (`getRecommendedProducts`): Dựa vào lịch sử đơn hàng đã mua hoặc fallback sang sản phẩm bán chạy.
2. **Thêm vào giỏ hàng (`CartServiceImpl.addToCart`):**
   - Gọi `ProductInternalService` kiểm tra tồn kho và trạng thái `ACTIVE`.
   - Gọi `ShopInternalService` kiểm tra Shop không bị khóa (`BANNED`).
   - Dùng `cartRepository.findByUserIdForUpdate(userId)` khóa ghi chống người dùng spam click.
3. **Đặt hàng (`OrderServiceImpl.checkout`):**
   - Chia đơn theo từng shop.
   - Tính chiết khấu voucher qua `VoucherInternalService`.
   - Trừ kho nguyên tử bằng câu lệnh SQL: `UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`.
   - Xóa các món đã mua khỏi giỏ hàng.
4. **Thanh toán VNPay (`PaymentGatewayService`):**
   - Sinh link thanh toán VNPay có chữ ký số HMAC-SHA512.
   - Xử lý Webhook IPN: Đối soát chữ ký $\rightarrow$ Khóa bi quan `findByTransactionIdForUpdate` $\rightarrow$ So khớp số tiền `vnp_Amount` $\rightarrow$ Đổi trạng thái đơn thành `CONFIRMED`.
5. **Vòng đời sau mua:**
   - Khi đơn là `SHIPPED` $\rightarrow$ Khách có nút "Đã nhận hàng" (`confirmDelivery`) $\rightarrow$ Đơn chuyển sang `DELIVERED`.
   - Khi đơn là `DELIVERED` $\rightarrow$ Mở khóa cho phép viết Đánh giá sản phẩm (`ReviewController.createReview`).

---

## Chặng 4: Luồng Người bán (Vendor Journey)

1. **Đăng ký Shop (`ShopController.registerShop`):** Khách gửi form $\rightarrow$ Shop có trạng thái `PENDING`. Admin vào trang duyệt shop (`approveShop`) $\rightarrow$ Shop thành `ACTIVE`, tài khoản user được nâng cấp lên `ROLE_VENDOR`.
2. **Quản lý sản phẩm (`VendorProductController`):**
   - Upload ảnh lên Cloudinary qua `MediaController` độc lập $\rightarrow$ Lấy URL ảnh.
   - Thêm sản phẩm kèm danh mục và các biến thể SKU, giá, tồn kho.
3. **Xử lý đơn hàng (`VendorOrderController.updateOrderStatus`):**
   - Máy trạng thái: `PENDING` $\rightarrow$ `CONFIRMED` $\rightarrow$ `SHIPPED`.
   - Khi Vendor bấm giao hàng `SHIPPED` $\rightarrow$ Hệ thống tự động bắn thông báo cho khách hàng.
4. **Thống kê doanh thu (`VendorAnalyticsController`):** Xem biểu đồ doanh thu theo tuần/tháng, số đơn thành công.

---

## Chặng 5: Các tính năng Kỹ thuật Ăn điểm

1. **Chat Realtime STOMP (`ChatServiceImpl`):**
   - Kết nối WebSocket SockJS `/ws`.
   - Xác thực token qua `JwtChannelInterceptor`.
   - Khi gửi tin nhắn `/app/chat.send`, server lưu tin nhắn vào database và lập tức dùng `SimpMessagingTemplate.convertAndSendToUser` đẩy tin nhắn trực tiếp đến tài khoản người nhận.
2. **Cơ chế Hoàn tiền VNPay tự động (`AdminRefundController` & `RefundService`):**
   - Cho phép Admin kích hoạt hoàn tiền đơn hàng bị hủy qua cổng thanh toán VNPay.
   - Kiểm soát khóa chống trùng lặp `idempotency_key` và ghi nhật ký giao dịch cổng vào bảng `refund_attempts`.

---

# PHẦN 3: BÓC TÁCH CHI TIẾT TỪNG DÒNG CODE TRONG CÁC HÀM CỐT LÕI

## Hàm 1: `OrderServiceImpl.checkout(...)` (Đặt hàng chia shop)
*Vị trí file:* [OrderServiceImpl.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/order/service/OrderServiceImpl.java#L73-L187)

```java
@Override
@Transactional // 1. Bọc toàn bộ quá trình tạo đơn trong 1 Transaction, lỗi bất kỳ dòng nào sẽ rollback sạch
public CheckoutResponse checkout(Long userId, CheckoutRequest request) {
    // 2. Sinh mã UUID duy nhất đại diện cho nhóm thanh toán của đợt checkout này
    String paymentGroupId = java.util.UUID.randomUUID().toString();

    // 3. Lấy toàn bộ món hàng trong giỏ từ CartInternalService
    List<CartItemInternalDto> allCartItems = cartInternalService.getCartItems(userId);

    // 4. Lọc chỉ lấy những món hàng mà khách hàng tick chọn mua trên giao diện
    List<CartItemInternalDto> cartItems = allCartItems.stream()
            .filter(item -> request.cartItemIds().contains(item.cartItemId()))
            .toList();

    // 5. Kiểm tra Shop bán các mặt hàng này có đang bị Admin cấm (BANNED) hay không
    cartItems.stream().map(CartItemInternalDto::shopId).distinct()
            .forEach(shopInternalService::requireNotBannedForSale);

    // 6. Gom nhóm các món hàng theo từng Shop: Shop A gom riêng, Shop B gom riêng
    Map<Long, List<CartItemInternalDto>> itemsByShop = cartItems.stream()
            .collect(Collectors.groupingBy(CartItemInternalDto::shopId));

    // 7. Nếu khách có nhập Voucher: Gọi VoucherInternalService tính tiền giảm và trừ lượt dùng
    VoucherDiscountResult voucherDiscount = null;
    if (request.voucherCode() != null && !request.voucherCode().trim().isEmpty()) {
        voucherDiscount = voucherInternalService.calculateDiscount(...);
        voucherInternalService.incrementUsage(voucherDiscount.voucherId());
    }

    // 8. Duyệt qua từng nhóm Shop để tạo đơn hàng
    for (Map.Entry<Long, List<CartItemInternalDto>> entry : itemsByShop.entrySet()) {
        Long shopId = entry.getKey();
        List<CartItemInternalDto> shopItems = entry.getValue();

        // Tạo Entity Order với status ban đầu là PENDING
        Order order = Order.builder()
                .userId(userId)
                .shopId(shopId)
                .paymentGroupId(paymentGroupId) // Dùng chung UUID
                .status(OrderStatus.PENDING)
                .shippingAddress(request.shippingAddress())
                .paymentMethod(request.paymentMethod())
                .build();

        for (CartItemInternalDto cartItem : shopItems) {
            // Trừ kho nguyên tử của từng biến thể
            productInternalService.deductStock(cartItem.productVariantId(), cartItem.quantity());
            // Snapshot giá, tên, ảnh vào OrderItem
            OrderItem orderItem = OrderItem.builder()
                    .productVariantId(cartItem.productVariantId())
                    .productName(cartItem.productName())
                    .imageUrl(itemImageUrl)
                    .unitPrice(cartItem.unitPrice())
                    .quantity(cartItem.quantity())
                    .build();
            order.addOrderItem(orderItem);
        }
        orderRepository.save(order); // Lưu đơn hàng và chi tiết đơn hàng xuống DB
    }

    // 9. Xóa các món đã mua thành công ra khỏi giỏ hàng
    cartInternalService.removeCartItems(userId, checkedOutItemIds);

    // 10. Trả về mã paymentGroupId và danh sách các đơn hàng vừa tạo cho Frontend
    return new CheckoutResponse(paymentGroupId, mapToOrderResponses(savedOrders));
}
```

---

## Hàm 2: `PaymentGatewayService.processVnPayIpn(...)` (Webhook IPN)
*Vị trí file:* [PaymentGatewayService.java](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/payment/service/PaymentGatewayService.java#L53-L102)

```java
@Transactional
public void processVnPayIpn(Map<String, String> params) {
    PaymentStrategy strategy = paymentFactory.getStrategy(PaymentMethod.VNPAY);

    // 1. Kiểm tra chữ ký HMAC-SHA512 để phát hiện ngay yêu cầu giả mạo dữ liệu
    if (!strategy.verifyIpnSignature(params)) {
        throw new IllegalArgumentException("Chữ ký VNPAY không hợp lệ! Nghi vấn giả mạo.");
    }

    String txnRef = params.get("vnp_TxnRef");
    String responseCode = params.get("vnp_ResponseCode");

    // 2. KHÓA BI QUAN (Pessimistic Lock FOR UPDATE) bản ghi thanh toán để chống xử lý đồng thời
    Payment payment = paymentRepository.findByTransactionIdForUpdate(txnRef)
            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch: " + txnRef));

    // 3. KIỂM TRA IDEMPOTENCY: Nếu đơn này đã được xử lý trước đó (status != PENDING) thì thoát ngay
    if (payment.getStatus() != PaymentStatus.PENDING) {
        return;
    }

    // 4. BẢO MẬT ĐỐI SOÁT GIÁ: So khớp số tiền VNPay báo về (đã nhân 100) với số tiền thực của đơn
    long vnpAmount = Long.parseLong(params.get("vnp_Amount"));
    long expectedAmount = payment.getAmount().movePointRight(2).longValueExact();
    if (vnpAmount != expectedAmount) {
        throw new IllegalArgumentException("Số tiền thanh toán không khớp với hóa đơn.");
    }

    // 5. Nếu mã phản hồi là "00" (Thành công)
    if ("00".equals(responseCode)) {
        payment.setStatus(PaymentStatus.SUCCESS);
        // Gọi OrderInternalService xác nhận chuyển tất cả đơn hàng trong nhóm sang CONFIRMED
        orderInternalService.confirmPendingOrdersByGroup(payment.getPaymentGroupId());
    } else {
        payment.setStatus(PaymentStatus.FAILED);
    }

    paymentRepository.save(payment); // Cập nhật trạng thái giao dịch
}
```

---

# PHẦN 4: 25 CÂU HỎI VẤN ĐÁP THƯỜNG GẶP & CÁCH TRẢ LỜI GHI ĐIỂM 10

### Câu 1: Tại sao em lựa chọn kiến trúc Modular Monolith thay vì Microservices?
- **Trả lời:** *"Dạ thưa thầy/cô, đối với quy mô của hệ thống hiện tại, nếu triển khai Microservices sẽ gây ra chi phí vận hành hạ tầng rất lớn, phức tạp trong việc chia tách mạng lưới (Network Latency), quản lý Distributed Transaction và đồng bộ dữ liệu. Kiến trúc **Modular Monolith** mang lại ưu thế tuyệt đối: Vừa đảm bảo tính đóng gói cao, ranh giới rõ ràng giữa các module qua các Internal Service/DTO, dễ dàng bảo trì và deploy đơn giản trong 1 tiến trình, đồng thời cấu trúc code đã được phân ly hoàn toàn sẵn sàng tách thành Microservices khi lưu lượng thực sự yêu cầu."*

---

### Câu 2: Giả sử sản phẩm chỉ còn 1 cái trong kho, 2 người cùng bấm Mua ngay tại cùng 1 giây, hệ thống xử lý thế nào để không bị âm kho?
- **Trả lời:** *"Dạ thưa thầy/cô, hệ thống em áp dụng **Pessimistic Concurrency Control** (Khóa ghi bi quan) ở tầng Database. Trong hàm trừ kho `ProductInternalServiceImpl.deductStock`, em sử dụng câu lệnh SQL có điều kiện kiểm tra tồn kho nguyên tử:
  `UPDATE product_variants SET stock_quantity = stock_quantity - ? WHERE id = ? AND stock_quantity >= ?`
  Khi 2 request đến cùng lúc, Database sẽ khóa dòng bản ghi của biến thể đó. Request thứ nhất trừ kho từ 1 về 0 thành công. Request thứ hai chạy sau sẽ thấy điều kiện `stock_quantity >= 1` không còn thỏa mãn, số dòng cập nhật trả về là 0, hệ thống lập tức bắt được và ném lỗi `Hết hàng` để Rollback giao dịch của người thứ hai, đảm bảo kho không bao giờ bị âm."*

---

### Câu 3: Khách hàng thanh toán qua VNPay xong, tài khoản ngân hàng đã trừ tiền, nhưng khách lỡ tay tắt trình duyệt thì đơn hàng có được xác nhận không?
- **Trả lời:** *"Dạ thưa thầy/cô, đơn hàng **vẫn được xác nhận 100%**. Vì hệ thống của em không phụ thuộc vào trang Redirect trình duyệt của người dùng. VNPay có cơ chế **IPN (Instant Payment Notification)** là một Webhook server-to-server gọi ngầm trực tiếp từ máy chủ VNPay đến endpoint `/api/v1/payments/vnpay/ipn` trên backend của em. Ngay khi giao dịch hoàn tất, server VNPay sẽ bắn thông báo này độc lập, backend xác thực chữ ký và cập nhật đơn hàng thành công bất kể trình duyệt người dùng đóng hay mở."*

---

### Câu 4: Em xử lý vấn đề N+1 Query trong Spring Data JPA như thế nào?
- **Trả lời:** *"Dạ thưa thầy/cô, lỗi N+1 Query xảy ra khi truy vấn 1 danh sách cha và Hibernate bắn thêm N câu lệnh phụ để lấy danh sách con (Lazy Loading). Để khắc phục:
  1. Với quan hệ ManyToOne (như Product và Category), em sử dụng `@EntityGraph(attributePaths = {"category"})` trong Repository để Hibernate tự động sinh câu lệnh `JOIN FETCH` chỉ trong 1 truy vấn duy nhất.
  2. Với quan hệ OneToMany (như Order và OrderItems), em cấu hình `@BatchSize(size = 50)` trên entity. Thay vì truy vấn từng đơn hàng riêng lẻ, Hibernate sẽ gom thành câu lệnh `WHERE order_id IN (?, ?, ...)` với tối đa 50 đơn 1 lần, giảm thiểu số lượng truy vấn xuống hàng chục lần."*

---

### Câu 5: Realtime Chat bằng WebSocket STOMP khác gì so với WebSocket thuần (Raw WebSocket)?
- **Trả lời:** *"Dạ thưa thầy/cô, Raw WebSocket chỉ cung cấp một đường ống truyền byte/text thô hai chiều, không có quy ước về định dạng tin nhắn hay định tuyến. Trong khi đó, **STOMP (Simple Text Oriented Messaging Protocol)** là giao thức tầng ứng dụng chạy trên WebSocket, cung cấp sẵn các khung tin (Frame) với cấu trúc `CONNECT`, `SUBSCRIBE`, `SEND`. Nhờ có STOMP kết hợp với `SimpMessagingTemplate` của Spring, em có thể định tuyến tin nhắn đến đúng đích danh từng người dùng qua destination `/user/queue/messages` một cách bảo mật và dễ quản lý hơn rất nhiều."*

---

### Câu 6: Token JWT được lưu ở đâu trên Frontend? Có nhược điểm gì và cách phòng chống?
- **Trả lời:** *"Dạ thưa thầy/cô, hiện tại trên Frontend token JWT được lưu tại `localStorage` để duy trì trạng thái đăng nhập khi người dùng F5 hoặc mở tab mới. Nhược điểm của `localStorage` là có nguy cơ bị tấn công XSS (Cross-Site Scripting) nếu website bị chèn mã độc JavaScript. Để phòng chống, hệ thống sử dụng React (tự động escape dữ liệu chống chèn mã script), cấu hình CORS chặt chẽ ở Backend, và giải pháp nâng cao tối ưu nhất trong môi trường sản xuất là chuyển sang lưu token trong `HttpOnly Cookie` kết hợp CSRF protection."*

---

### Câu 7: Ý nghĩa của trường `prior_status` trong bảng `shops` là gì?
- **Trả lời:** *"Dạ thưa thầy/cô, đây là điểm nghiệp vụ tinh tế em cài đặt: Khi Admin khóa một gian hàng (`BANNED`), shop đó có thể trước đây đang ở trạng thái `ACTIVE` hoặc đang chờ xét duyệt `PENDING`. Em lưu trạng thái trước đó vào `prior_status`. Khi Admin bấm Mở khóa (`UNBAN`), hệ thống sẽ đọc lại `prior_status` để trả shop về đúng trạng thái nguyên bản của nó, tránh trường hợp một shop chưa từng được duyệt hồ sơ sau khi unban lại vô tình trở thành active."*

---

### Câu 8: Tại sao hệ thống lại dùng `@TransactionalEventListener(phase = AFTER_COMMIT)` cho việc gửi thông báo?
- **Trả lời:** *"Dạ thưa thầy/cô, nếu dùng EventListener thông thường, sự kiện sẽ chạy ngay trong luồng transaction đặt hàng. Nếu việc lưu đơn vào database bị lỗi rollback nhưng thông báo đã lỡ gửi đi, khách hàng sẽ nhận được thông báo 'Đặt hàng thành công' cho một đơn hàng không hề tồn tại. Sử dụng `phase = AFTER_COMMIT` đảm bảo thông báo chỉ được tạo khi và chỉ khi toàn bộ dữ liệu đơn hàng đã được ghi xuống đĩa MySQL thành công 100%."*

---

# LỜI KHUYÊN CUỐI CÙNG CHO BUỔI BẢO VỆ

1. **Giữ bình tĩnh và tự tin:** Đồ án của bạn được thiết kế rất chỉn chu, sạch đẹp, đúng chuẩn doanh nghiệp từ Modular Monolith, Xử lý giao dịch, Pessimistic Lock, đến Gợi ý sản phẩm và Realtime chat.
2. **Khi thầy cô yêu cầu mở code:** Hãy mở đúng các file trọng tâm: `OrderServiceImpl.java` (luồng đặt hàng), `PaymentGatewayService.java` (luồng VNPay), hoặc `CartPage.tsx` / `CheckoutPage.tsx`.
3. **Nếu gặp câu hỏi chưa biết rõ:** Đừng im lặng hay trả lời bừa. Hãy trả lời: *"Dạ thưa thầy/cô, hiện tại trong phạm vi đồ án em đang giải quyết bài toán theo hướng [nêu giải pháp hiện tại], em xin ghi nhận đóng góp của thầy/cô để nghiên cứu và nâng cấp thêm [hướng tối ưu thầy cô vừa gợi ý] trong tương lai."*
