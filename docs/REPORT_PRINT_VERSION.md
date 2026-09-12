# BÁO CÁO ĐỒ ÁN NGÀNH

**ĐỀ TÀI:** XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ ĐA NGƯỜI BÁN  
*(ENTERPRISE MULTI-VENDOR E-COMMERCE SYSTEM)*

---

> **GHI CHÚ DÀNH CHO BẢN IN (PRINT VERSION):**
> 
> - Tài liệu này được biên soạn cô đọng, loại bỏ các chi tiết triển khai nội bộ không cần thiết nhằm phục vụ in ấn đóng quyển theo tiêu chuẩn học thuật (định dạng A4, lề chuẩn, cỡ chữ 12–13pt, dãn dòng 1.5 line, ước tính ≤ 90 trang).
> - Các vị trí chèn sơ đồ và hình ảnh minh họa thực tế được đánh dấu tường minh:
>   - `[INSERT FIGURE: IMG-XX]` — Vị trí dán hình ảnh chụp màn hình chạy thực tế.
>   - `[INSERT DIAGRAM: DIAGRAM-XX]` — Vị trí dán sơ đồ thiết kế UML/ERD.
> - Trạng thái tính năng được kiểm chứng nghiêm ngặt dựa trên mã nguồn thực tế: `IMPLEMENTED` (Đã hoàn thành), `PARTIAL` (Hoàn thành một phần), `MISSING` (Chưa triển khai).

---

## MỞ ĐẦU

Thương mại điện tử (TMĐT) đã và đang trở thành một trong những động lực quan trọng nhất của nền kinh tế số Việt Nam. Sự phát triển mạnh mẽ của các sàn giao dịch TMĐT đa người bán (Multi-Vendor Marketplace) như Shopee, Tiki, Lazada đã làm thay đổi căn bản thói quen mua bán hàng hóa của người tiêu dùng và doanh nghiệp. Trong mô hình này, nền tảng đóng vai trò trung gian công nghệ, giải quyết các bài toán phức tạp về phân tách dữ liệu đa người bán, điều phối giao dịch thanh toán, kiểm soát tính toàn vẹn của tồn kho và quản lý vòng đời đơn hàng.

Đề tài **"Xây dựng hệ thống thương mại điện tử đa người bán"** (Enterprise Ecommerce) được nghiên cứu và hiện thực hóa nhằm xây dựng một nền tảng TMĐT đa người bán hoàn chỉnh ở quy mô học thuật nhưng tiếp cận sát với tiêu chuẩn doanh nghiệp. Đề tài tập trung áp dụng các nguyên lý thiết kế phần mềm hiện đại: kiến trúc **Modular Monolith** trên nền tảng **Spring Boot (Java 21)** cho phía Backend; ứng dụng Single Page Application (SPA) trên nền **React, TypeScript và Vite** cho phía Frontend; đồng thời tích hợp các dịch vụ thực tế gồm cổng thanh toán **VNPay**, lưu trữ đám mây **Cloudinary**, email thông báo qua **Gmail SMTP**, và giao tiếp thời gian thực qua **WebSocket STOMP**.

Báo cáo gồm 4 chương chính:
- **Chương 1: Tổng quan đề tài** — Trình bày bối cảnh, lý do chọn đề tài, phát biểu bài toán, mục tiêu, phạm vi và khảo sát các hệ thống liên quan.
- **Chương 2: Cơ sở lý thuyết** — Trình bày các khái niệm nền tảng, mô hình, giải thuật và tổng quan công nghệ sử dụng.
- **Chương 3: Phân tích, thiết kế và xây dựng hệ thống** — Trình bày kiến trúc hệ thống, phân tích Use Case, thiết kế UML, thiết kế CSDL (ERD và chi tiết 11 bảng quan trọng), thiết kế giao diện và kết quả triển khai thực tế.
- **Chương 4: Kết luận và hướng phát triển** — Đánh giá kết quả đạt được, hạn chế còn tồn tại và đề xuất lộ trình nâng cấp hệ thống.

---

# CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI

## 1.1. Giới thiệu đề tài

Thương mại điện tử đa người bán (Multi-Vendor Marketplace) là mô hình kinh doanh trực tuyến cho phép nhiều nhà bán hàng độc lập (Vendors) cùng mở gian hàng, trưng bày và bán sản phẩm trên cùng một nền tảng website chung. Khác với mô hình TMĐT truyền thống (Single-Vendor), hệ thống đa người bán phải giải quyết bài toán phức tạp hơn rất nhiều về mặt tổ chức dữ liệu và quy trình nghiệp vụ: một người mua có thể chọn sản phẩm từ nhiều gian hàng khác nhau trong cùng một lần duyệt web, nhưng khi thanh toán hệ thống phải tự động tách thành nhiều đơn hàng độc lập cho từng gian hàng tương ứng, đồng thời vẫn đảm bảo tính thống nhất về tổng giá trị thanh toán và mã giảm giá toàn sàn.

Hệ thống được thiết kế và xây dựng theo mô hình phân tách rõ ràng giữa Backend và Frontend:
- **Backend:** Ứng dụng độc lập xây dựng bằng Spring Boot (Java 21), tổ chức mã nguồn theo kiến trúc Modular Monolith. Toàn bộ logic nghiệp vụ được chia thành 10 module độc lập, giao tiếp với nhau qua các hợp đồng nội bộ (Internal Service contracts), cung cấp hệ thống REST API chuẩn hóa và kết nối WebSocket thời gian thực.
- **Frontend:** Ứng dụng Single Page Application (SPA) xây dựng bằng React 19, TypeScript và Vite, cung cấp giao diện trực quan, tối ưu trải nghiệm cho ba tác nhân: Khách mua hàng (Customer), Chủ gian hàng (Vendor) và Quản trị viên hệ thống (Admin).

## 1.2. Lý do chọn đề tài

### 1.2.1. Xu hướng thị trường
Theo báo cáo Thương mại điện tử Việt Nam, mô hình Marketplace chiếm hơn 80% tổng doanh số bán lẻ trực tuyến cả nước. Nhu cầu chuyển đổi số của các hộ kinh doanh cá thể và doanh nghiệp vừa và nhỏ (SMEs) lên các sàn TMĐT ngày càng cấp thiết. Việc nghiên cứu cơ chế hoạt động nội tại của các sàn TMĐT mang lại giá trị thực tiễn cao.

### 1.2.2. Thách thức kỹ thuật chuyên sâu
Xây dựng một sàn TMĐT đa người bán đặt ra hàng loạt thách thức kỹ thuật quan trọng trong ngành kỹ thuật phần mềm:
- **Tổ chức kiến trúc:** Làm thế nào để hệ thống không bị biến thành một khối "Monolith nguyên khối rối rắm" (Big Ball of Mud) nhưng cũng không phải gánh chịu sự phức tạp về hạ tầng mạng, phân tán dữ liệu của kiến trúc Microservices? Kiến trúc **Modular Monolith** chính là lời giải tối ưu.
- **Quản lý giao dịch và tính nhất quán:** Xử lý giỏ hàng gồm nhiều gian hàng, tách đơn hàng tự động, áp dụng mã giảm giá theo tỷ lệ (proportional voucher split) và trừ tồn kho chính xác dưới áp lực truy cập đồng thời.
- **Tích hợp thanh toán an toàn:** Giao tiếp với cổng thanh toán thực tế (VNPay), xử lý Webhook IPN (Instant Payment Notification) đảm bảo tính lũy thừa (Idempotency) bằng kỹ thuật khóa bi quan (Pessimistic Locking).
- **Giao tiếp thời gian thực:** Đồng bộ tin nhắn trò chuyện giữa người mua và người bán thông qua giao thức WebSocket STOMP kèm cơ chế xác thực JWT trên kênh kết nối.

### 1.2.3. Tính phù hợp với chuẩn đào tạo kỹ sư CNTT
Đề tài đòi hỏi sinh viên phải tổng hợp toàn diện kiến thức từ nhiều môn học chuyên ngành: Phân tích thiết kế hệ thống, Cơ sở dữ liệu nâng cao, Lập trình ứng dụng phân tán, An toàn thông tin và Phát triển ứng dụng Web hiện đại.

## 1.3. Phát biểu bài toán

Hệ thống cần giải quyết nhu cầu của 3 tác nhân chính trong một chu trình thương mại khép kín:
1. **Khách hàng (Customer):** Cần tìm kiếm sản phẩm dễ dàng, xem chi tiết biến thể (màu sắc, kích thước), thêm sản phẩm từ nhiều shop vào giỏ hàng, áp dụng voucher toàn sàn hoặc voucher từng shop, thanh toán trực tuyến qua VNPay hoặc COD, theo dõi đơn hàng và viết đánh giá sau khi nhận hàng.
2. **Người bán (Vendor):** Cần quy trình đăng ký mở gian hàng minh bạch, chờ duyệt từ ban quản trị; quản lý sản phẩm cùng ảnh lưu trữ trên mây và các biến thể phân loại; quản lý danh sách đơn hàng thuộc quyền sở hữu của mình; chủ động phát hành voucher khuyến mãi và phản hồi đánh giá khách hàng.
3. **Quản trị viên (Admin):** Kiểm duyệt đơn đăng ký gian hàng, khóa/mở khóa gian hàng vi phạm, phát hành voucher toàn sàn (SYSTEM scope) và giám sát đơn hàng toàn hệ thống.

Để giải quyết bài toán nghiệp vụ trên, hệ thống phải xử lý 6 vấn đề kỹ thuật cốt lõi:
1. **Phân tách và gom nhóm dữ liệu đa người bán:** Giỏ hàng chung nhưng đơn hàng phải được tách theo từng `shop_id`.
2. **Ranh giới giao dịch (Transaction Boundary):** Khi checkout, việc tạo đơn hàng, tạo snapshot giá, trừ tồn kho và xóa giỏ hàng phải diễn ra trong một giao dịch cơ sở dữ liệu duy nhất (`@Transactional`). Các tác vụ phụ (gửi email, bắn thông báo) phải được tách biệt và kích hoạt sau khi giao dịch commit (`AFTER_COMMIT`).
3. **Thanh toán trực tuyến và tính lũy thừa (Idempotency):** Cổng thanh toán gọi IPN webhook có thể bị trùng lặp do cơ chế retry mạng. Hệ thống phải đảm bảo dù nhận nhiều lần cùng một IPN, đơn hàng chỉ được xác nhận một lần duy nhất.
4. **Kiểm soát đồng thời tồn kho (Concurrency Control):** Tránh hiện tượng bán vượt số lượng (overselling) khi nhiều khách hàng cùng mua một mặt hàng cuối cùng.
5. **Giao tiếp thời gian thực:** Thông báo đơn hàng mới tức thì cho Vendor và hỗ trợ trò chuyện trực tiếp giữa Customer và Vendor qua WebSocket.
6. **Tính module hóa và khả năng mở rộng:** Các module không được truy cập trực tiếp bảng hay entity của nhau mà phải giao tiếp qua contract interface.

## 1.4. Mục tiêu của đề tài

- **Về mặt nghiệp vụ:** Xây dựng hoàn chỉnh chu trình mua bán từ khâu đăng ký gian hàng, đăng sản phẩm, giỏ hàng, đặt hàng đa shop, thanh toán VNPay/COD đến đánh giá chất lượng sản phẩm.
- **Về mặt kỹ thuật:** Thiết kế kiến trúc Modular Monolith chuẩn mực với 10 module độc lập; tối ưu hiệu năng truy vấn CSDL, kiểm soát hoàn toàn hiện tượng N+1 Query; tích hợp thành công VNPay Sandbox, Cloudinary CDN và WebSocket STOMP.
- **Về mặt học thuật:** Cung cấp tài liệu phân tích thiết kế đầy đủ, trung thực về kết quả thực hiện, nêu rõ các tính năng đã hoàn thành và các hạn chế kỹ thuật còn tồn tại.

## 1.5. Phạm vi nghiên cứu

### 1.5.1. Trong phạm vi đề tài (In-Scope)
- Chu trình đăng ký, xác thực, phân quyền tài khoản (ROLE_CUSTOMER, ROLE_VENDOR, ROLE_ADMIN) và cập nhật thông tin cá nhân (Profile).
- Quản lý gian hàng: Đăng ký, chờ duyệt, phê duyệt/từ chối/khóa (Ban) và mở khóa (Unban) trực tiếp trên giao diện Admin.
- Danh mục đa cấp (tự tham chiếu) kèm giải thuật tìm kiếm phân cấp theo chiều rộng (BFS); quản lý sản phẩm kèm biến thể (SKU, giá, tồn kho, thuộc tính JSON) và gallery ảnh Cloudinary.
- Công cụ gợi ý sản phẩm lai (Hybrid Recommendation Engine) dựa trên đánh giá, danh mục và độ phổ biến.
- Giỏ hàng đa shop và quy trình Checkout tách đơn tự động theo từng gian hàng.
- Tích hợp cổng thanh toán VNPay Sandbox (tạo URL, xử lý IPN có khóa bi quan và chữ ký HMAC-SHA512) và COD.
- Hệ thống Voucher với 2 phạm vi (SYSTEM, SHOP) và thuật toán phân bổ chiết khấu theo tỷ lệ giá trị đơn hàng.
- Quản lý vòng đời đơn hàng khép kín: Vendor xác nhận xuất kho (`SHIPPED`) và Khách hàng xác nhận nhận hàng (`DELIVERED`) hoặc Vendor hủy đơn hoàn kho tự động.
- Hệ thống Đánh giá sản phẩm (Review) có ràng buộc chỉ người đã nhận hàng mới được viết đánh giá kèm ảnh.
- Hệ thống thông báo thời gian thực hướng sự kiện và khung chat nổi trực tiếp (Floating Chat Widget) qua WebSocket STOMP.
- Báo cáo phân tích doanh thu cho từng Gian hàng (Vendor Analytics) và Bảng số liệu thống kê Dashboard tổng thể cho Quản trị viên (Admin Dashboard).

### 1.5.2. Ngoài phạm vi đề tài (Out-of-Scope)
- Tích hợp cổng thanh toán production thật (đòi hỏi tư cách pháp nhân doanh nghiệp).
- Tích hợp API đơn vị vận chuyển bên thứ ba (Giao Hàng Nhanh, Giao Hàng Tiết Kiệm) — hiện tại quy trình vận chuyển được phối hợp trực tiếp giữa Vendor cập nhật xuất kho (`SHIPPED`) và Khách hàng xác nhận nhận hàng (`DELIVERED`).
- Ứng dụng di động native (iOS / Android).
- Quy trình hoàn tiền tự động qua API cổng thanh toán ngoài (Refund API Gateway).

## 1.6. Phương pháp và quy trình thực hiện

Hệ thống được phát triển theo mô hình lặp và tăng trưởng (Iterative & Incremental) qua 9 giai đoạn (Phases) đã hoàn thành toàn diện:
1. **Phase 1 — Foundation:** Thiết lập khung dự án, cấu hình Spring Boot, JPA, MySQL, chuẩn hóa mã lỗi `ApiException` và phản hồi chuẩn `ApiResponse<T>`.
2. **Phase 2 — Authentication & Identity:** Xây dựng bảng `users`, `roles`, cấu hình Spring Security, sinh và xác thực JSON Web Token (JWT), trang hồ sơ cá nhân (`/profile`).
3. **Phase 3 — Product Catalog:** Thiết kế danh mục cây, sản phẩm, biến thể, upload ảnh đa phương tiện lên Cloudinary CDN.
4. **Phase 4 — Shop Management:** Xây dựng vòng đời gian hàng (PENDING, ACTIVE, BANNED), kiểm duyệt, khóa và mở khóa shop bởi Admin, cơ chế bảo vệ route `RequireShop`.
5. **Phase 5 — Shopping Cart & Voucher:** Quản lý giỏ hàng đa shop và triển khai cơ chế tính toán voucher tỷ lệ (scope SYSTEM, SHOP).
6. **Phase 6 — Order & Payment:** Thiết kế luồng Checkout giao dịch nguyên khối, tích hợp VNPay Sandbox, khóa bi quan chống duplicate IPN.
7. **Phase 7 — Supporting Features:** Hệ thống đánh giá sản phẩm có điều kiện đơn nhận, thông báo event-driven (`AFTER_COMMIT`).
8. **Phase 8 — Realtime Communication:** Xây dựng hạ tầng WebSocket STOMP bảo mật qua JWT Interceptor, hoàn thiện cửa sổ trò chuyện nổi toàn website (`FloatingChatWidget`) và dropdown thông báo trực tiếp.
9. **Phase 9 — Business Analytics & Advanced Search:** Hoàn thiện bảng điều khiển thống kê Admin Dashboard (`/admin`), trang phân tích doanh thu người bán (`/vendor/analytics`), thuật toán tìm kiếm danh mục đa cấp BFS và động cơ gợi ý sản phẩm lai (Hybrid Recommendation).

## 1.7. Khảo sát các hệ thống liên quan

**Bảng 1.1.** So sánh hệ thống đề tài với các nền tảng TMĐT thực tế

| Tiêu chí | Shopee Vietnam | Tiki Vietnam | WooCommerce Multi-Vendor | Đề tài (Enterprise Ecommerce) |
|---|---|---|---|---|
| **Mô hình kiến trúc** | Microservices phân tán cao | Microservices | Monolith (PHP/WordPress) | **Modular Monolith (Spring Boot 4)** |
| **Phân tách đơn hàng** | Tách tự động theo shop | Hỗ trợ tách đơn và TikiNow | Plugin phân chia | **Tách đơn tự động theo shop** |
| **Thanh toán** | ShopeePay, VNPay, Thẻ, COD | ViettelMoney, VNPay, COD | PayPal, Stripe, COD | **VNPay Sandbox, COD** |
| **Quản lý biến thể** | Ma trận 2 thuộc tính (Màu, Size) | Biến thể cố định | Tự do cấu hình | **Biến thể động (JSON attributes)** |
| **Khuyến mãi** | Voucher sàn, Shop, Freeship | Voucher sàn, Shop, Astra | Coupon plugin | **Voucher SYSTEM, SHOP (tính tỷ lệ)** |
| **Giao tiếp** | Shopee Chat nội bộ | Khung chat CSKH | Extension rời rạc | **WebSocket STOMP + JWT** |

---

# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT

## 2.1. Các khái niệm nền tảng

### 2.1.1. Kiến trúc Modular Monolith
Modular Monolith là phong cách kiến trúc phần mềm trong đó toàn bộ hệ thống được đóng gói và triển khai như một ứng dụng đơn khối (Single Deployment Unit), nhưng bên trong mã nguồn được phân chia nghiêm ngặt thành các module nghiệp vụ tách biệt.

**Bảng 2.1.** So sánh các mô hình kiến trúc phần mềm

| Đặc điểm | Traditional Monolith | Modular Monolith (Đề tài) | Microservices |
|---|---|---|---|
| **Đơn vị triển khai** | 1 artifact duy nhất | **1 artifact duy nhất** | Hàng chục/trăm service riêng |
| **Ranh giới module** | Mờ nhạt, phụ thuộc chéo | **Rõ ràng, kiểm soát qua contract** | Rõ ràng qua HTTP/gRPC |
| **Chia sẻ CSDL** | Dùng chung tự do, JOIN chéo | **Bảng riêng theo module, không FK cứng chéo** | CSDL riêng biệt hoàn toàn |
| **Độ phức tạp vận hành** | Thấp | **Thấp – Trung bình** | Rất cao (k8s, service mesh, network) |
| **Phù hợp với** | Ứng dụng nhỏ | **Dự án vừa, cần kiểm soát cấu trúc** | Hệ thống lớn, hàng trăm lập trình viên |

### 2.1.2. Xác thực và Phân quyền với JWT và RBAC
Hệ thống sử dụng cơ chế Stateless Authentication với **JSON Web Token (JWT)**:
- Token được ký bằng thuật toán HMAC-SHA256, hạn sử dụng 24 giờ, chứa thông tin định danh `sub` (email), `userId` và danh sách quyền hạn `roles`.
- Phân quyền theo vai trò (Role-Based Access Control - RBAC) gồm 3 vai trò: `ROLE_CUSTOMER`, `ROLE_VENDOR`, `ROLE_ADMIN`.

### 2.1.3. Cơ sở dữ liệu quan hệ và tối ưu hóa ORM
Hệ thống sử dụng **MySQL** kết hợp với **Spring Data JPA / Hibernate**:
- **Giải quyết bài toán N+1 Query:** Khi truy vấn danh sách đơn hàng hoặc sản phẩm kèm các quan hệ `OneToMany` (như `Product` → `ProductVariant`), việc dùng `FetchType.EAGER` hoặc truy vấn mặc định sẽ gây ra 1 câu truy vấn cha và N câu truy vấn con. Đề tài giải quyết triệt để bằng cách áp dụng annotation `@BatchSize(size = 30)` trên các collection và sử dụng phép `LEFT JOIN FETCH` trong Spring Data JPA JPQL queries.

### 2.1.4. Giao thức WebSocket và STOMP
- **WebSocket (RFC 6455):** Cung cấp kênh truyền thông hai chiều, toàn song công (full-duplex) trên một kết nối TCP duy nhất.
- **STOMP (Simple Text Oriented Messaging Protocol):** Định nghĩa cấu trúc khung truyền thông điệp (Frame) trên nền WebSocket gồm lệnh (COMMAND), các tiêu đề (HEADERS) và phần thân (BODY).
- Trong hệ thống, kênh WebSocket `/ws` được bảo vệ bằng `JwtChannelInterceptor` — kiểm tra token trong STOMP `CONNECT` frame trước khi chấp nhận phiên kết nối.

### 2.1.5. Kiến trúc hướng sự kiện (Event-Driven) và `@TransactionalEventListener`
Khi một hành động nghiệp vụ hoàn thành, hệ thống phát sinh các sự kiện thông qua `ApplicationEventPublisher`. Điểm mấu chốt là sử dụng annotation:
```java
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
```
Cơ chế này đảm bảo các tác vụ phụ như gửi email chào mừng, tạo thông báo hệ thống chỉ được thực hiện sau khi giao dịch cơ sở dữ liệu đã commit thành công 100%. Nếu giao dịch chính bị rollback, sự kiện sẽ tự động bị hủy bỏ, ngăn ngừa tình trạng dữ liệu ảo hoặc gửi thông báo sai.

### 2.1.6. Cơ chế IPN và Tính lũy thừa (Idempotency) trong thanh toán
- **IPN (Instant Payment Notification):** Cổng thanh toán VNPay gửi tín hiệu HTTP Server-to-Server để thông báo kết quả giao dịch của khách hàng.
- **Tính lũy thừa (Idempotency):** Do mạng Internet có thể chập chờn, VNPay có thể gửi lại cùng một thông báo IPN nhiều lần. Để chống xử lý trùng lặp, hệ thống sử dụng khóa bi quan (`@Lock(LockModeType.PESSIMISTIC_WRITE)`) trên bảng `payments`. Khi request IPN đầu tiên đang xử lý, request thứ hai phải chờ; sau khi khóa được giải phóng, request thứ hai kiểm tra trạng thái thấy khác `PENDING` sẽ lập tức trả về mã thông báo "Giao dịch đã được xử lý" mà không cộng đơn hay cập nhật lại trạng thái.

### 2.1.7. Thuật toán phân bổ mã giảm giá theo tỷ lệ (Proportional Voucher Split)
Khi khách hàng mua sản phẩm từ nhiều gian hàng trong một lần thanh toán và áp dụng một voucher toàn sàn (SYSTEM scope), hệ thống không giảm giá tùy tiện mà tính toán chiết khấu phân bổ theo tỷ trọng giá trị đơn hàng của từng gian hàng:
$$\text{Giảm giá của Gian hàng } i = \text{Tổng số tiền giảm của Voucher} \times \frac{\text{Giá trị đơn hàng của Gian hàng } i}{\text{Tổng giá trị các mặt hàng đủ điều kiện}}$$

### 2.1.8. Thuật toán Gợi ý sản phẩm lai (Hybrid Recommendation Engine)
Nhằm cá nhân hóa trải nghiệm duyệt hàng cho khách mua và kích cầu tiêu dùng, hệ thống áp dụng cơ chế gợi ý sản phẩm lai kết hợp giữa độ phổ biến (Popularity-based) và lọc nội dung (Content-based):
- **Khách vãng lai (Guest):** Điểm số gợi ý được tính dựa trên số sao đánh giá trung bình (`average_rating`), tổng lượt đánh giá (`review_count`) và số lượng đã bán (`sold_count`).
- **Khách hàng đã đăng nhập (Authenticated Buyer):** Hệ thống phân tích lịch sử các đơn hàng đã mua trong quá khứ để trích xuất danh mục quan tâm nhiều nhất, từ đó ưu tiên đề xuất các sản phẩm cùng danh mục có điểm rating cao nhất chưa được mua. Trường hợp lịch sử mua còn trống hoặc thuật toán không đủ dữ liệu, hệ thống tự động fallback mượt mà về danh sách sản phẩm phổ biến nhất toàn sàn.

### 2.1.9. Thuật toán duyệt cây danh mục đa cấp theo chiều rộng (BFS Category Traversal)
Trong cấu trúc dữ liệu danh mục sản phẩm tự tham chiếu (`parent_id`), một danh mục cha (ví dụ: *Điện tử*) có thể chứa nhiều danh mục con (*Điện thoại*, *Máy tính*) và danh mục cháu (*Phụ kiện*, *Ốp lưng*). Khi người dùng nhấn lọc theo danh mục cha, hệ thống phải truy vấn được toàn bộ sản phẩm thuộc danh mục cha và tất cả các danh mục con cháu.
- Đề tài áp dụng giải thuật **Duyệt theo chiều rộng (Breadth-First Search - BFS)** sử dụng hàng đợi (`Queue<Long>`): xuất phát từ `categoryId` được chọn, hệ thống duyệt tầng bậc để thu thập toàn bộ tập hợp `Set<Long> subCategoryIds`.
- Sau đó, câu lệnh truy vấn JPQL sử dụng điều kiện `p.category.id IN (:categoryIds)` kết hợp đánh chỉ mục (Index) trên cột `category_id`, mang lại hiệu năng lọc dữ liệu vượt trội $O(V + E)$ thay vì sử dụng các câu truy vấn đệ quy SQL nặng nề.

## 2.2. Các mẫu thiết kế (Design Patterns) áp dụng

- **State Machine Pattern:** Quản lý vòng đời chặt chẽ của Đơn hàng (`PENDING` → `CONFIRMED` → `SHIPPED` → `DELIVERED` / `CANCELLED`) và Gian hàng (`PENDING` → `ACTIVE` / `BANNED`).
- **Strategy Pattern:** Đóng gói các thuật toán tính giảm giá (`PercentageDiscountStrategy`, `FixedAmountDiscountStrategy`) cho phép mở rộng loại voucher mới mà không sửa đổi mã nguồn xử lý checkout.
- **Snapshot Pattern:** Tại thời điểm đặt hàng, giá bán (`unit_price`) và tên sản phẩm (`product_name`) được sao chép trực tiếp vào bảng `order_items`, đảm bảo sau này nếu người bán thay đổi giá hay sửa tên sản phẩm thì các đơn hàng lịch sử trong quá khứ vẫn giữ nguyên giá trị pháp lý kế toán.

## 2.3. Tổng quan công nghệ sử dụng

**Bảng 2.2.** Danh mục công nghệ chính của hệ thống

| Thành phần | Công nghệ / Thư viện | Phiên bản | Vai trò kỹ thuật |
|---|---|---|---|
| **Ngôn ngữ Backend** | Java (JDK) | 21 (LTS) | Nền tảng thực thi backend, hỗ trợ Virtual Threads |
| **Backend Framework** | Spring Boot | 4.1.0 | Khung ứng dụng chính, IoC Container, Web MVC |
| **Truy cập dữ liệu** | Spring Data JPA / Hibernate | 6.x | ORM quản lý thực thể, chuyển đổi quan hệ - đối tượng |
| **Bảo mật** | Spring Security & JJWT | 0.12.5 | Xác thực Stateless, mã hóa BCrypt, lọc JWT |
| **Giao tiếp Realtime** | Spring WebSocket & STOMP | — | Kênh truyền thông tin nhắn hai chiều trực tiếp |
| **Tài liệu API** | SpringDoc OpenAPI | 3.0.3 | Swagger UI kiểm thử trực quan các REST endpoints |
| **Lưu trữ đám mây** | Cloudinary SDK | 1.36.0 | Tải lên, tối ưu và phục vụ ảnh sản phẩm qua CDN |
| **Gửi thư điện tử** | Spring Mail (Gmail SMTP) | — | Gửi email thông báo tự động (port 587 STARTTLS) |
| **Cơ sở dữ liệu** | MySQL Server | 8.0 | Hệ quản trị CSDL quan hệ lưu trữ toàn bộ dữ liệu |
| **Ngôn ngữ Frontend** | TypeScript | 6.0.2 | Tăng cường tính chặt chẽ về kiểu dữ liệu trên giao diện |
| **Thư viện UI** | React | 19.2.8 | Xây dựng giao diện Single Page Application hướng thành phần |
| **Công cụ đóng gói** | Vite | 8.2.0 | Hot-module replacement cực nhanh, đóng gói tối ưu |
| **Định tuyến Frontend** | React Router DOM | 7.18.2 | Quản lý điều hướng trang và kiểm soát Protected Routes |
| **HTTP Client** | Axios | 1.19.0 | Gửi request bất đồng bộ kèm interceptor đính kèm JWT |
| **Thiết kế Styling** | Tailwind CSS | 4.3.3 | CSS tiện ích tối ưu hóa giao diện hiện đại, responsive |
| **Cổng thanh toán** | VNPay Payment Gateway | Sandbox | Tích hợp giải pháp thanh toán thẻ và ví điện tử |

---

# CHƯƠNG 3. PHÂN TÍCH, THIẾT KẾ VÀ XÂY DỰNG HỆ THỐNG

## 3.1. Giới thiệu tổng quan hệ thống

Hệ thống phục vụ ba nhóm tác nhân với không gian nghiệp vụ độc lập:
- **Khách hàng (Customer):** Đối tượng mua sắm, thực hiện các thao tác tìm kiếm, xem chi tiết, quản lý giỏ hàng, đặt hàng, thanh toán trực tuyến và phản hồi sau mua sắm.
- **Người bán (Vendor):** Đối tượng kinh doanh, sở hữu gian hàng, quản lý danh mục sản phẩm, biến thể tồn kho, theo dõi đơn hàng của gian hàng mình, xuất kho và chăm sóc khách hàng.
- **Quản trị viên (Admin):** Đối tượng quản lý toàn sàn, kiểm duyệt hồ sơ mở shop, khóa gian hàng vi phạm, phát hành chính sách khuyến mãi toàn sàn và giám sát giao dịch.

## 3.2. Thiết kế kiến trúc hệ thống

### 3.2.1. Mô hình kiến trúc Modular Monolith
Hệ thống được thiết kế theo mô hình phân lớp kết hợp chia module chức năng nghiêm ngặt:

```
+-----------------------------------------------------------------------+
|                       FRONTEND: React 19 SPA                          |
|         [Customer Portal]    [Vendor Portal]    [Admin Portal]        |
+-----------------------------------------------------------------------+
                                   | (HTTPS / REST API / WSS)
                                   v
+-----------------------------------------------------------------------+
|                    BACKEND: Spring Boot (Java 21)                     |
|                                                                       |
|  +-----------------------------------------------------------------+  |
|  |             API Gateway / Security / JWT Interceptors           |  |
|  +-----------------------------------------------------------------+  |
|                                  |                                    |
|  +-------------------------------+---------------------------------+  |
|  |                 10 MODULAR BUSINESS COMPONENTS                 |  |
|  |  [Identity]      [Shop]        [Product]      [Cart]            |  |
|  |  [Order]         [Payment]     [Voucher]      [Review]          |  |
|  |  [Notification]  [Chat]                                        |  |
|  +-----------------------------------------------------------------+  |
|                                  | (Internal Contracts / Events)      |
|  +-----------------------------------------------------------------+  |
|  |             Spring Data JPA / Hibernate ORM Layer               |  |
|  +-----------------------------------------------------------------+  |
+-----------------------------------------------------------------------+
        |                     |                     |             |
        v                     v                     v             v
   [MySQL 8.0]        [Cloudinary CDN]      [VNPay Sandbox]  [Gmail SMTP]
```

### 3.2.2. Phân chia các module nghiệp vụ

**Bảng 3.1.** Phân chia 10 module nghiệp vụ trong hệ thống

| Tên Module | Trách nhiệm nghiệp vụ | Thực thể chính quản lý |
|---|---|---|
| `identity` | Quản lý tài khoản người dùng, phân quyền, xác thực JWT | `User`, `Role` |
| `shop` | Đăng ký gian hàng, kiểm duyệt, vòng đời trạng thái shop | `Shop` |
| `product` | Quản lý thương hiệu, danh mục cây, sản phẩm, biến thể và hình ảnh | `Brand`, `Category`, `Product`, `ProductVariant`, `ProductImage` |
| `cart` | Quản lý giỏ hàng của khách hàng, thêm/sửa/xóa mục hàng | `Cart`, `CartItem` |
| `order` | Tiếp nhận checkout, tách đơn đa shop, quản lý trạng thái đơn hàng | `Order`, `OrderItem` |
| `payment` | Xử lý giao dịch thanh toán, tạo link VNPay, webhook IPN | `Payment` |
| `voucher` | Quản lý mã giảm giá hệ thống và gian hàng, tính toán chiết khấu | `Voucher` |
| `review` | Tiếp nhận và quản lý đánh giá sao, hình ảnh và phản hồi của shop | `Review`, `ReviewImage` |
| `notification` | Lưu trữ và phát thông báo hệ thống cho các bên | `Notification` |
| `chat` | Quản lý phòng chat giữa khách và shop, truyền nhận tin nhắn | `ChatRoom`, `ChatMessage` |

### 3.2.3. Quy tắc giao tiếp liên module (Cross-Module Communication)
1. **Tuyệt đối không truy cập trực tiếp:** Một module không bao giờ được phép `@Autowired` Repository hoặc Entity của một module khác.
2. **Giao tiếp qua Internal Service:** Mọi truy xuất phải đi qua Interface được khai báo tường minh trong package `internal`. Ví dụ:
   - Module `order` gọi `ProductInternalService.getVariantForCheckout(variantId)` để xác thực giá và trừ tồn kho.
   - Module `order` gọi `VoucherInternalService.validateAndApply(...)` để tính chiết khấu.
   - Module `payment` phát `OrderPaidEvent`, module `order` lắng nghe để chuyển đơn sang `CONFIRMED`.

## 3.3. Phân tích yêu cầu chức năng (Use Case)

### 3.3.1. Danh mục tác nhân
- **Guest (Khách vãng lai):** Người chưa xác thực, có thể xem danh sách sản phẩm, chi tiết sản phẩm, đăng ký tài khoản hoặc đăng nhập.
- **Customer (Khách hàng):** Kế thừa Guest, có quyền quản lý giỏ hàng, đặt hàng, thanh toán, xem lịch sử đơn hàng, viết đánh giá và chat với người bán.
- **Vendor (Người bán hàng):** Chủ sở hữu gian hàng đã được phê duyệt, có quyền quản lý sản phẩm, biến thể, xử lý đơn hàng, tạo voucher gian hàng.
- **Admin (Quản trị viên):** Quản lý tối cao, duyệt hồ sơ mở gian hàng, phát hành voucher sàn, giám sát toàn diện hệ thống.

### 3.3.2. Sơ đồ Use Case tổng quát

[INSERT DIAGRAM: DIAGRAM-USE-CASE-OVERVIEW]

*Hình 3.1. Sơ đồ Use Case tổng quát của hệ thống*

### 3.3.3. Đặc tả 8 Use Case cốt lõi

#### UC01: Đăng ký mở gian hàng (Vendor)
- **Actor:** Customer đã đăng nhập.
- **Tiền điều kiện:** Tài khoản chưa sở hữu gian hàng nào.
- **Luồng chính:** Người dùng điền form thông tin gian hàng (Tên, mô tả, số điện thoại, địa chỉ) → Hệ thống kiểm tra trùng tên → Tạo bản ghi `Shop` trạng thái `PENDING` → Hiển thị màn hình chờ Admin phê duyệt.
- **Hậu điều kiện:** Bản ghi gian hàng được tạo, người dùng bị chặn vào bảng điều khiển Vendor bởi bộ lọc `RequireShop` cho đến khi được duyệt.

#### UC02: Phê duyệt gian hàng (Admin)
- **Actor:** Admin.
- **Tiền điều kiện:** Có ít nhất một gian hàng ở trạng thái `PENDING`.
- **Luồng chính:** Admin duyệt danh sách gian hàng chờ → Bấm nút "Phê duyệt" → Hệ thống cập nhật trạng thái `Shop` thành `ACTIVE` → Gán bổ sung quyền `ROLE_VENDOR` cho người dùng sở hữu → Kích hoạt sự kiện `ShopApprovedEvent` gửi email chúc mừng qua Gmail SMTP.
- **Hậu điều kiện:** Chủ gian hàng có quyền truy cập đầy đủ các chức năng quản lý gian hàng.

#### UC03: Đăng bán sản phẩm kèm biến thể và hình ảnh (Vendor)
- **Actor:** Vendor (gian hàng `ACTIVE`).
- **Luồng chính:** Vendor nhập tên, mô tả, danh mục → Chọn tải lên các tệp hình ảnh (hệ thống đẩy trực tiếp lên Cloudinary CDN và nhận về URL) → Tạo các biến thể sản phẩm (SKU, giá bán, số lượng tồn kho ban đầu, thuộc tính màu sắc/kích cỡ dưới dạng JSON) → Bấm "Lưu sản phẩm" → Hệ thống lưu đồng thời `Product`, danh sách `ProductVariant` và `ProductImage` trong một giao dịch.

#### UC04: Thêm sản phẩm vào giỏ hàng (Customer)
- **Actor:** Customer.
- **Luồng chính:** Tại trang chi tiết sản phẩm, khách hàng chọn biến thể cụ thể (ví dụ: Màu Đen, Size L) và số lượng mua → Bấm "Thêm vào giỏ hàng" → Hệ thống kiểm tra số lượng tồn kho của biến thể → Nếu biến thể đã tồn tại trong giỏ thì cộng dồn số lượng, nếu chưa thì thêm mới `CartItem` liên kết với `Cart` của người dùng.

#### UC05: Đặt hàng đa gian hàng kèm mã giảm giá (Customer)
- **Actor:** Customer.
- **Tiền điều kiện:** Giỏ hàng có ít nhất một mặt hàng hợp lệ.
- **Luồng chính:** 
  1. Khách hàng chọn các món hàng trong giỏ, nhập địa chỉ nhận hàng, chọn phương thức thanh toán (VNPay/COD), nhập mã voucher (nếu có).
  2. Hệ thống kiểm tra tồn kho từng món.
  3. Gom nhóm các món hàng theo từng `shop_id` riêng biệt.
  4. Nếu có voucher: kiểm tra tính hợp lệ và phân bổ tiền giảm giá theo tỷ lệ.
  5. Tạo N đơn hàng (`Order`) tương ứng cho N gian hàng khác nhau trong cùng một `payment_group_id`.
  6. Trừ trực tiếp số lượng tồn kho của các biến thể tương ứng.
  7. Xóa các món đã đặt khỏi giỏ hàng.
  8. Trả về thông tin đơn hàng (kèm URL thanh toán VNPay nếu chọn VNPay).

#### UC06: Thanh toán trực tuyến qua VNPay và nhận IPN (Customer / Hệ thống)
- **Actor:** Customer và Hệ thống VNPay.
- **Luồng chính:**
  1. Khách hàng được chuyển hướng sang trang cổng thanh toán VNPay và hoàn tất nhập thẻ/quét QR.
  2. VNPay chuyển hướng khách hàng về URL kết quả trên hệ thống.
  3. Đồng thời, máy chủ VNPay gửi request ngầm (IPN) đến webhook của hệ thống.
  4. Hệ thống xác thực chữ ký số HMAC-SHA512.
  5. Áp dụng khóa bi quan (`PESSIMISTIC_WRITE`) trên bản ghi thanh toán: kiểm tra nếu đang ở `PENDING` thì cập nhật thành `SUCCESS`, phát sự kiện `OrderPaidEvent`.
  6. Module `order` nhận sự kiện, chuyển trạng thái toàn bộ N đơn hàng con sang `CONFIRMED`.

#### UC07: Quản lý và xử lý đơn hàng (Vendor)
- **Actor:** Vendor.
- **Luồng chính:** Vendor theo dõi đơn hàng theo các tab trạng thái → Khi đơn ở trạng thái `CONFIRMED`, Vendor chuẩn bị hàng và bấm "Xác nhận gửi hàng" → Trạng thái đơn chuyển thành `SHIPPED` → Khi đơn vị vận chuyển giao thành công, Vendor bấm "Đã giao hàng" → Trạng thái đơn chuyển thành `DELIVERED` (hoàn tất đơn).

#### UC08: Đánh giá sản phẩm đã mua (Customer)
- **Actor:** Customer.
- **Tiền điều kiện:** Khách hàng đã mua sản phẩm trong một đơn hàng có trạng thái `DELIVERED` và chưa từng đánh giá sản phẩm này trong đơn đó.
- **Luồng chính:** Khách hàng chọn số sao (1–5 sao), viết nhận xét văn bản và đính kèm ảnh chụp thực tế → Hệ thống kiểm tra điều kiện hợp lệ qua API `checkReviewEligibility` → Lưu bản ghi `Review` và `ReviewImage` → Hệ thống tự động tính toán lại điểm rating trung bình của sản phẩm.

#### UC09: Xác nhận đã nhận được hàng (Customer)
- **Actor:** Customer.
- **Tiền điều kiện:** Đơn hàng thuộc sở hữu của khách hàng đang ở trạng thái `SHIPPED`.
- **Luồng chính:** Khách hàng truy cập trang chi tiết đơn hàng `/orders/:id` → Nhấn nút "Đã nhận được hàng" → Hệ thống gọi API `POST /api/v1/orders/{id}/confirm-delivery` → Chuyển trạng thái đơn hàng từ `SHIPPED` sang `DELIVERED` → Mở khóa quyền viết đánh giá cho sản phẩm thuộc đơn này.

#### UC10: Trò chuyện trực tiếp qua Floating Chat Widget (Customer & Vendor)
- **Actor:** Customer và Vendor.
- **Tiền điều kiện:** Đã đăng nhập tài khoản hợp lệ.
- **Luồng chính:** Khách hàng nhấn biểu tượng Chat ở góc màn hình (FloatingChatWidget) → Chọn gian hàng muốn liên hệ → Mở kết nối WebSocket STOMP bảo mật qua JWT → Gửi tin nhắn trao đổi thời gian thực → Tin nhắn được lưu trữ vào CSDL (`chat_messages`) và lập tức chuyển đến đích danh đối phương qua kênh `/user/queue/messages`.

#### UC11: Thống kê số liệu kinh doanh và Dashboard (Vendor & Admin)
- **Actor:** Vendor và Admin.
- **Luồng chính:**
  - **Vendor:** Truy cập `/vendor/analytics`, chọn khoảng ngày (mặc định 30 ngày gần nhất) → Xem 4 thẻ chỉ số KPI cốt lõi (Doanh thu thực nhận, Số đơn thành công, Tỷ lệ hủy, Doanh thu trung bình/đơn), biểu đồ phân bổ trạng thái đơn và chi tiết doanh thu theo mốc thời gian.
  - **Admin:** Truy cập `/admin`, xem Dashboard điều hành toàn diện gồm tổng doanh thu toàn sàn, số lượng đơn hàng, số shop đang hoạt động, biểu đồ doanh thu theo ngày và bảng danh sách 10 đơn hàng mới nhất cần giám sát.

## 3.4. Thiết kế hệ thống (UML)

### 3.4.1. Sơ đồ lớp (Class Diagram)

Hệ thống bao gồm 18 lớp thực thể chính đại diện cho các đối tượng nghiệp vụ được quản lý trong cơ sở dữ liệu.

[INSERT DIAGRAM: DIAGRAM-CLASS-PRODUCT]

*Hình 3.2. Sơ đồ lớp nhóm Sản phẩm và Gian hàng*

[INSERT DIAGRAM: DIAGRAM-CLASS-ORDER]

*Hình 3.3. Sơ đồ lớp nhóm Đơn hàng, Giỏ hàng và Thanh toán*

**Các quan hệ cấu trúc then chốt:**
- Quan hệ giữa `Category` và chính nó: Tự liên kết (Self-referencing) qua thuộc tính `parent_id` để tạo cây phân cấp danh mục đa tầng.
- Quan hệ giữa `Product` và `ProductVariant`: Một - Nhiều (`cascade = CascadeType.ALL`, `orphanRemoval = true`), quản lý toàn bộ các phân loại hàng hóa của một sản phẩm.
- Quan hệ giữa N `Order` và 1 `Payment`: Liên kết logic lỏng (Loose Coupling) thông qua trường định danh chuỗi `payment_group_id` kiểu UUID, loại bỏ ràng buộc khóa ngoại cứng chéo module.

### 3.4.2. Sơ đồ tuần tự (Sequence Diagram)

#### 3.4.2.1. Luồng Checkout đặt hàng đa gian hàng

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-CHECKOUT]

*Hình 3.4. Sơ đồ tuần tự quy trình Checkout đặt hàng đa gian hàng*

*Mô tả luồng:*
1. Khách hàng gửi request checkout kèm danh sách ID món hàng trong giỏ, địa chỉ và mã voucher.
2. `OrderService` mở giao dịch `@Transactional`, gọi `ProductInternalService` để khóa và kiểm tra tồn kho.
3. Phân tách danh sách món hàng theo từng `shop_id` của các gian hàng khác nhau.
4. Gọi `VoucherInternalService` để tính toán chiết khấu và phân bổ theo tỷ lệ cho từng gian hàng.
5. Tạo các thực thể `Order` và `OrderItem` (kèm snapshot giá và tên sản phẩm).
6. Trừ tồn kho tại bảng `product_variants` và xóa sản phẩm tương ứng khỏi giỏ hàng `cart_items`.
7. Giao dịch commit thành công; kích hoạt sự kiện gửi thông báo đơn hàng mới cho các chủ shop liên quan.

#### 3.4.2.2. Luồng xử lý thanh toán VNPay IPN với khóa bi quan

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-IPN]

*Hình 3.5. Sơ đồ tuần tự xử lý VNPay IPN bảo đảm Idempotency*

*Mô tả luồng:*
1. Webhook nhận request IPN kèm các tham số và mã băm bảo mật checksum từ VNPay.
2. Kiểm tra tính hợp lệ của chữ ký HMAC-SHA512.
3. Thực hiện truy vấn `findByPaymentGroupId` kèm khóa `PESSIMISTIC_WRITE` (câu lệnh SQL tương ứng `SELECT ... FOR UPDATE`).
4. Kiểm tra trạng thái hiện tại của bản ghi: nếu trạng thái khác `PENDING` (chứng tỏ request trùng lặp đã được xử lý xong trước đó), hệ thống lập tức ngắt xử lý và phản hồi mã xác nhận cho VNPay.
5. Nếu trạng thái đang là `PENDING`, cập nhật thành `SUCCESS`, commit giao dịch và phát sinh sự kiện `OrderPaidEvent`.

#### 3.4.2.3. Luồng nhắn tin thời gian thực qua WebSocket STOMP

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-CHAT]

*Hình 3.6. Sơ đồ tuần tự nhắn tin thời gian thực giữa Khách hàng và Người bán*

*Mô tả luồng:*
1. Client gửi yêu cầu bắt tay WebSocket tới `/ws` kèm JWT trong STOMP Connect Header.
2. `JwtChannelInterceptor` chặn frame, giải mã token và thiết lập Principal định danh người dùng.
3. Client đăng ký nhận tin tại kênh riêng `/user/queue/messages`.
4. Khi gửi tin nhắn, client publish lên `/app/chat.send` kèm `{roomId, content}`.
5. Server lưu tin nhắn vào CSDL (`chat_messages`) và điều hướng tới đích danh người nhận thông qua `SimpMessagingTemplate.convertAndSendToUser()`.

### 3.4.3. Sơ đồ hoạt động (Activity Diagram)

[INSERT DIAGRAM: DIAGRAM-ACTIVITY-ORDER-PAYMENT]

*Hình 3.7. Sơ đồ hoạt động quy trình Mua hàng và Thanh toán*

[INSERT DIAGRAM: DIAGRAM-ACTIVITY-SHOP-APPROVAL]

*Hình 3.8. Sơ đồ hoạt động quy trình Đăng ký và Kiểm duyệt gian hàng*

### 3.4.4. Sơ đồ trạng thái (State Machine Diagram)

#### 3.4.4.1. Vòng đời đơn hàng (Order State Machine)

[INSERT DIAGRAM: DIAGRAM-STATE-ORDER]

*Hình 3.9. Sơ đồ chuyển đổi trạng thái của Đơn hàng*

- `PENDING`: Đơn vừa khởi tạo (chờ thanh toán nếu là VNPay, hoặc chờ xác nhận nếu là COD).
- `CONFIRMED`: Đã thanh toán thành công hoặc đã được hệ thống ghi nhận; người bán bắt đầu đóng gói.
- `SHIPPED`: Người bán đã bàn giao hàng hóa cho đơn vị vận chuyển.
- `DELIVERED`: Khách hàng đã nhận hàng thành công (Trạng thái kết thúc - cho phép đánh giá).
- `CANCELLED`: Đơn hàng bị hủy bỏ (bởi người bán hoặc do lỗi thanh toán).

#### 3.4.4.2. Vòng đời gian hàng (Shop State Machine)

[INSERT DIAGRAM: DIAGRAM-STATE-SHOP]

*Hình 3.10. Sơ đồ chuyển đổi trạng thái của Gian hàng*

- `PENDING`: Mới nộp đơn đăng ký mở shop, chờ Admin xét duyệt.
- `ACTIVE`: Được Admin duyệt thành công, gian hàng hoạt động bình thường trên sàn.
- `BANNED`: Bị Admin khóa quyền hoạt động do vi phạm quy chế hoặc bị từ chối đơn đăng ký.

---

## 3.5. Thiết kế cơ sở dữ liệu hệ thống

Cơ sở dữ liệu của hệ thống được xây dựng trên hệ quản trị **MySQL 8.0**, quản lý cấu trúc bảng và cập nhật tự động thông qua JPA/Hibernate ORM.

### 3.5.1. Lược đồ tổng quan 22 bảng cơ sở dữ liệu

**Bảng 3.2.** Tổng mục 22 bảng trong cơ sở dữ liệu hệ thống

| STT | Tên bảng | Module | Mục đích lưu trữ nghiệp vụ |
|---|---|---|---|
| 1 | `users` | identity | Thông tin tài khoản người dùng, email, mật khẩu mã hóa, họ tên |
| 2 | `roles` | identity | Danh mục các vai trò trong hệ thống (CUSTOMER, VENDOR, ADMIN) |
| 3 | `user_roles` | identity | Bảng liên kết trung gian N-N giữa tài khoản và vai trò |
| 4 | `shops` | shop | Thông tin hồ sơ gian hàng, tên shop, mô tả, trạng thái hoạt động |
| 5 | `categories` | product | Danh mục sản phẩm tổ chức theo cây phân cấp đa tầng |
| 6 | `brands` | product | Thương hiệu sản phẩm, logo và mô tả |
| 7 | `products` | product | Thông tin sản phẩm chung, liên kết gian hàng và danh mục |
| 8 | `product_variants` | product | Chi tiết biến thể sản phẩm, tồn kho, giá bán và thuộc tính JSON |
| 9 | `product_images` | product | Danh sách URL hình ảnh sản phẩm lưu trữ trên Cloudinary |
| 10 | `carts` | cart | Giỏ hàng cá nhân đại diện cho từng người dùng |
| 11 | `cart_items` | cart | Các mục sản phẩm (biến thể) và số lượng nằm trong giỏ hàng |
| 12 | `orders` | order | Đơn hàng chi tiết của từng gian hàng sau khi khách hàng checkout |
| 13 | `order_items` | order | Chi tiết từng món hàng trong đơn, lưu snapshot giá và tên tại lúc mua |
| 14 | `order_admin_status_history` | order | Lịch sử can thiệp trạng thái đơn hàng và lý do của Admin |
| 15 | `payments` | payment | Giao dịch thanh toán tổng hợp theo nhóm đơn hàng |
| 16 | `refund_attempts` | payment | Sổ cái ghi nhận nỗ lực hoàn tiền từ Admin (Idempotency Key) |
| 17 | `vouchers` | voucher | Mã khuyến mãi giảm giá, phạm vi áp dụng và giới hạn sử dụng |
| 18 | `reviews` | review | Đánh giá sao và nhận xét của khách hàng đối với sản phẩm đã mua |
| 19 | `review_images` | review | Hình ảnh đính kèm minh chứng cho đánh giá sản phẩm |
| 20 | `notifications` | notification | Lịch sử thông báo hệ thống gửi đến người dùng |
| 21 | `chat_rooms` | chat | Không gian phòng trò chuyện trực tiếp giữa khách hàng và gian hàng |
| 22 | `chat_messages` | chat | Nội dung chi tiết các tin nhắn trao đổi trong phòng chat |

### 3.5.2. Thiết kế chi tiết 11 bảng cơ sở dữ liệu cốt lõi

Dưới đây là đặc tả chi tiết cấu trúc cột, kiểu dữ liệu, ràng buộc khóa chính, khóa ngoại của 11 bảng trọng yếu nhất hệ thống:

#### 1. Bảng `users` (Tài khoản người dùng)
Lưu trữ thông tin định danh của mọi người dùng tham gia vào sàn TMĐT.

**Bảng 3.3.** Đặc tả cấu trúc bảng `users`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh người dùng duy nhất |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | — | Địa chỉ email dùng để đăng nhập hệ thống |
| `password_hash` | VARCHAR(255) | NOT NULL | — | Chuỗi băm mật khẩu bảo mật chuẩn BCrypt |
| `full_name` | VARCHAR(100) | NOT NULL | — | Họ và tên đầy đủ của người dùng |
| `phone` | VARCHAR(20) | NULL | NULL | Số điện thoại liên lạc |
| `status` | VARCHAR(20) | NOT NULL | 'ACTIVE' | Trạng thái tài khoản (ACTIVE, INACTIVE, BLOCKED) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm khởi tạo tài khoản |
| `updated_at` | DATETIME | NULL | NULL | Thời điểm chỉnh sửa thông tin gần nhất |

#### 2. Bảng `shops` (Gian hàng người bán)
Lưu trữ thông tin các gian hàng kinh doanh do người bán đăng ký và được Admin phê duyệt.

**Bảng 3.4.** Đặc tả cấu trúc bảng `shops`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh duy nhất của gian hàng |
| `user_id` | BIGINT | NOT NULL, UNIQUE, FK → users.id | — | Khóa ngoại trỏ đến chủ sở hữu (1 User có tối đa 1 Shop) |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | — | Tên hiển thị công khai của gian hàng |
| `description` | TEXT | NULL | NULL | Đoạn văn bản giới thiệu về gian hàng |
| `phone` | VARCHAR(20) | NULL | NULL | Số điện thoại hotline chăm sóc của gian hàng |
| `address` | VARCHAR(255) | NULL | NULL | Địa chỉ bưu chính/kho lấy hàng của gian hàng |
| `status` | VARCHAR(20) | NOT NULL | 'PENDING' | Trạng thái gian hàng (PENDING, ACTIVE, BANNED) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Ngày đăng ký mở gian hàng |

#### 3. Bảng `categories` (Danh mục sản phẩm)
Lưu trữ cây danh mục phân cấp đa tầng (ví dụ: Điện tử → Điện thoại → Phụ kiện).

**Bảng 3.5.** Đặc tả cấu trúc bảng `categories`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh danh mục |
| `name` | VARCHAR(100) | NOT NULL, UNIQUE | — | Tên gọi danh mục sản phẩm |
| `description` | TEXT | NULL | NULL | Mô tả quy cách danh mục |
| `parent_id` | BIGINT | NULL, FK → categories.id | NULL | Khóa ngoại tự tham chiếu đến danh mục cha (đệ quy) |

#### 4. Bảng `products` (Sản phẩm tổng thể)
Lưu thông tin định danh và giới thiệu sản phẩm của từng gian hàng cụ thể.

**Bảng 3.6.** Đặc tả cấu trúc bảng `products`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh duy nhất của sản phẩm |
| `shop_id` | BIGINT | NOT NULL, FK → shops.id | — | Khóa ngoại liên kết với gian hàng sở hữu |
| `category_id` | BIGINT | NOT NULL, FK → categories.id | — | Khóa ngoại liên kết danh mục phân loại |
| `brand_id` | BIGINT | NULL, FK → brands.id | NULL | Khóa ngoại liên kết thương hiệu sản xuất (nếu có) |
| `name` | VARCHAR(255) | NOT NULL | — | Tên gọi thương mại của sản phẩm |
| `description` | TEXT | NULL | NULL | Bài viết mô tả chi tiết công năng sản phẩm |
| `price` | DECIMAL(15,2) | NOT NULL | — | Giá bán niêm yết cơ bản |
| `stock_quantity` | INT | NOT NULL | 0 | Tổng số lượng tồn kho cộng dồn các biến thể |
| `status` | VARCHAR(20) | NOT NULL | 'ACTIVE' | Trạng thái hiển thị (ACTIVE, OUT_OF_STOCK, HIDDEN) |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm đăng tải sản phẩm |

#### 5. Bảng `product_variants` (Biến thể sản phẩm & Quản lý tồn kho)
Đóng vai trò cốt lõi trong phân hệ quản lý tồn kho (Inventory). Mỗi bản ghi đại diện cho một phân loại hàng hóa cụ thể (ví dụ: Áo thun - Màu Trắng - Size L).

**Bảng 3.7.** Đặc tả cấu trúc bảng `product_variants`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh biến thể |
| `product_id` | BIGINT | NOT NULL, FK → products.id | — | Khóa ngoại liên kết với sản phẩm cha |
| `sku` | VARCHAR(100) | NOT NULL, UNIQUE | — | Mã đơn vị quản lý kho hàng (Stock Keeping Unit) |
| `price` | DECIMAL(15,2) | NOT NULL | — | Giá bán chính xác áp dụng riêng cho biến thể này |
| `stock_quantity` | INT | NOT NULL | 0 | **Số lượng tồn kho thực tế trong kho hàng** |
| `attributes` | JSON / TEXT | NULL | NULL | Thuộc tính động dạng JSON (VD: `{"color":"Đỏ","size":"XL"}`) |
| `status` | VARCHAR(20) | NOT NULL | 'ACTIVE' | Trạng thái hàng hóa (ACTIVE, OUT_OF_STOCK, HIDDEN) |

#### 6. Bảng `carts` (Giỏ hàng người dùng)
Đại diện cho giỏ hàng duy nhất của một tài khoản khách hàng.

**Bảng 3.8.** Đặc tả cấu trúc bảng `carts`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh giỏ hàng |
| `user_id` | BIGINT | NOT NULL, UNIQUE, FK → users.id | — | Khóa ngoại trỏ đến chủ sở hữu giỏ hàng |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm khởi tạo giỏ |

#### 7. Bảng `cart_items` (Mặt hàng trong giỏ)
Lưu chi tiết các mặt hàng cụ thể mà khách đã chọn nhưng chưa tiến hành đặt mua.

**Bảng 3.9.** Đặc tả cấu trúc bảng `cart_items`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Khóa chính mục hàng giỏ |
| `cart_id` | BIGINT | NOT NULL, FK → carts.id | — | Khóa ngoại liên kết tới giỏ hàng cha |
| `product_variant_id` | BIGINT | NOT NULL, FK → product_variants.id | — | Khóa ngoại trỏ tới biến thể sản phẩm được chọn |
| `quantity` | INT | NOT NULL | 1 | Số lượng dự định mua (yêu cầu > 0) |

#### 8. Bảng `orders` (Đơn hàng)
Đại diện cho đơn hàng đã chốt của một gian hàng sau khi khách hàng thực hiện checkout.

**Bảng 3.10.** Đặc tả cấu trúc bảng `orders`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh duy nhất của đơn hàng |
| `user_id` | BIGINT | NOT NULL, FK → users.id | — | Khóa ngoại người mua hàng (Customer) |
| `shop_id` | BIGINT | NOT NULL, FK → shops.id | — | Khóa ngoại gian hàng sở hữu đơn |
| `payment_group_id` | VARCHAR(36) | NULL | NULL | Chuỗi UUID liên kết các đơn cùng đợt checkout |
| `status` | VARCHAR(20) | NOT NULL | 'PENDING' | Vòng đời đơn (PENDING, CONFIRMED, SHIPPED, DELIVERED, CANCELLED) |
| `payment_method` | VARCHAR(20) | NOT NULL | — | Phương thức thanh toán (VNPAY, COD) |
| `total_amount` | DECIMAL(15,2) | NOT NULL | — | Tổng tiền thanh toán thực tế (sau khi trừ chiết khấu) |
| `original_total_amount` | DECIMAL(15,2) | NOT NULL | 0 | Tổng giá trị hàng hóa ban đầu trước giảm giá |
| `discount_amount` | DECIMAL(15,2) | NOT NULL | 0 | Số tiền được giảm giá theo voucher |
| `shipping_address` | VARCHAR(500) | NOT NULL | — | Địa chỉ người nhận hàng |
| `applied_voucher_id` | BIGINT | NULL | NULL | Khóa ngoại logic trỏ tới voucher đã áp dụng |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm lập đơn hàng |

#### 9. Bảng `order_items` (Chi tiết mặt hàng trong đơn)
Lưu chi tiết từng sản phẩm bên trong đơn hàng, áp dụng kỹ thuật Snapshot bảo lưu giá trị quá khứ.

**Bảng 3.11.** Đặc tả cấu trúc bảng `order_items`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã định danh bản ghi |
| `order_id` | BIGINT | NOT NULL, FK → orders.id | — | Khóa ngoại trỏ đến đơn hàng cha |
| `product_variant_id` | BIGINT | NULL | NULL | Khóa ngoại tham chiếu đến biến thể (có thể null nếu xóa sản phẩm) |
| `product_name` | VARCHAR(255) | NOT NULL | — | **Snapshot:** Bản sao tên sản phẩm tại thời điểm đặt |
| `unit_price` | DECIMAL(15,2) | NOT NULL | — | **Snapshot:** Bản sao đơn giá tại thời điểm đặt mua |
| `quantity` | INT | NOT NULL | — | Số lượng đặt mua |
| `sub_total` | DECIMAL(15,2) | NOT NULL | — | Thành tiền tính toán (`quantity` × `unit_price`) |

#### 10. Bảng `payments` (Giao dịch thanh toán)
Lưu trữ thông tin tài chính và trạng thái giao dịch thanh toán trực tuyến hoặc tiền mặt.

**Bảng 3.12.** Đặc tả cấu trúc bảng `payments`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Mã giao dịch thanh toán |
| `payment_group_id` | VARCHAR(36) | NOT NULL, UNIQUE | — | Chuỗi UUID định danh phiên thanh toán (khớp với các orders) |
| `amount` | DECIMAL(15,2) | NOT NULL | — | Tổng số tiền cần thanh toán thực tế của cả phiên |
| `method` | VARCHAR(20) | NOT NULL | — | Cổng thanh toán (VNPAY, COD) |
| `status` | VARCHAR(20) | NOT NULL | 'PENDING' | Trạng thái giao dịch (PENDING, SUCCESS, FAILED) |
| `vnpay_txn_ref` | VARCHAR(100) | NULL | NULL | Mã tham chiếu giao dịch do hệ thống tạo gửi sang VNPay |
| `vnpay_transaction_no` | VARCHAR(100) | NULL | NULL | Mã giao dịch thực tế do VNPay phản hồi về qua IPN |
| `created_at` | DATETIME | NOT NULL | CURRENT_TIMESTAMP | Thời điểm khởi tạo giao dịch |

#### 11. Bảng `vouchers` (Mã giảm giá khuyến mãi)
Lưu trữ các chính sách khuyến mại và mã giảm giá phát hành bởi Admin hoặc từng Vendor.

**Bảng 3.13.** Đặc tả cấu trúc bảng `vouchers`

| Tên cột | Kiểu dữ liệu | Ràng buộc | Giá trị mặc định | Diễn giải |
|---|---|---|---|---|
| `id` | BIGINT | PRIMARY KEY, AUTO_INCREMENT | — | Khóa chính mã khuyến mãi |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE | — | Chuỗi ký tự mã nhập giảm giá (không phân biệt hoa thường) |
| `scope` | VARCHAR(10) | NOT NULL | — | Phạm vi áp dụng (`SYSTEM` - Toàn sàn, `SHOP` - Riêng gian hàng) |
| `shop_id` | BIGINT | NULL, FK → shops.id | NULL | Khóa ngoại gian hàng phát hành (bắt buộc NULL nếu scope=SYSTEM) |
| `type` | VARCHAR(20) | NOT NULL | — | Cách tính giảm (`PERCENTAGE` - Theo %, `FIXED_AMOUNT` - Số tiền cố định) |
| `discount_value` | DECIMAL(15,2) | NOT NULL | — | Mức giảm (tỷ lệ % hoặc số tiền cụ thể) |
| `max_discount` | DECIMAL(15,2) | NULL | NULL | Mức giảm kịch kim tối đa (áp dụng cho dạng PERCENTAGE) |
| `min_order_value` | DECIMAL(15,2) | NULL | 0 | Giá trị đơn hàng tối thiểu đủ điều kiện áp dụng |
| `start_date` | DATETIME | NOT NULL | — | Thời điểm mã bắt đầu có hiệu lực |
| `end_date` | DATETIME | NOT NULL | — | Thời điểm mã hết hạn sử dụng |
| `usage_limit` | INT | NULL | NULL | Số lượt sử dụng tối đa toàn sàn/shop (NULL = vô hạn) |
| `used_count` | INT | NOT NULL | 0 | Số lượt khách hàng đã sử dụng thành công |

### 3.5.3. Sơ đồ thực thể liên kết (ERD)

[INSERT DIAGRAM: DIAGRAM-ERD]

*Hình 3.11. Sơ đồ thực thể liên kết (ERD) hoàn chỉnh của hệ thống (20 bảng)*

### 3.5.4. Các mối quan hệ quan trọng trong cơ sở dữ liệu

**Bảng 3.14.** Bảng tổng hợp quan hệ cốt lõi giữa các bảng dữ liệu

| Mối quan hệ | Bản chất liên kết | Ràng buộc & Cơ chế đảm bảo tính toàn vẹn |
|---|---|---|
| `users` ↔ `shops` | 1 – 1 | Khóa `user_id` trong `shops` là duy nhất (`UNIQUE`), ngăn một tài khoản mở nhiều gian hàng. |
| `shops` ↔ `products` | 1 – N | Khóa ngoại `shop_id` trong `products`, đảm bảo sản phẩm phải thuộc sở hữu của một gian hàng hợp lệ. |
| `categories` ↔ `categories` | 1 – N (Tự tham chiếu) | Khóa ngoại `parent_id` trong `categories` trỏ đến `id` của chính bảng đó để tạo cấu trúc danh mục cha - con. |
| `products` ↔ `product_variants` | 1 – N | Bật `CascadeType.ALL` và `orphanRemoval = true`, xóa sản phẩm sẽ xóa đồng bộ toàn bộ biến thể. |
| `users` ↔ `carts` | 1 – 1 | Cột `user_id` trong `carts` là `UNIQUE`, mỗi khách hàng sở hữu chính xác một giỏ hàng. |
| `carts` ↔ `cart_items` | 1 – N | Một giỏ hàng chứa nhiều mục hàng; xóa giỏ hàng sẽ cascade xóa toàn bộ mục con. |
| `orders` ↔ `order_items` | 1 – N | Một đơn hàng gồm nhiều chi tiết món hàng; `order_items` lưu snapshot độc lập với sản phẩm gốc. |
| `orders` ↔ `payments` | N – 1 (Logic) | Liên kết qua trường `payment_group_id` kiểu chuỗi UUID, cho phép gom N đơn của N shop vào 1 giao dịch thanh toán duy nhất. |
| `products` ↔ `reviews` | 1 – N | Một sản phẩm có nhiều đánh giá; ràng buộc khóa ngoại kép kiểm tra tính hợp lệ của đơn hàng trước khi đánh giá. |

---

## 3.6. Thiết kế giao diện hệ thống

### 3.6.1. Giao diện phía Khách hàng (Customer)

Giao diện khách hàng được thiết kế theo phong cách hiện đại với thanh điều hướng cố định (Header) gồm Logo, thanh tìm kiếm sản phẩm thông minh theo thời gian thực kèm lọc đa tiêu chí, nút chuyển đổi danh mục, icon giỏ hàng kèm số lượng hiển thị nổi, chuông thông báo Realtime, menu tài khoản cá nhân và cửa sổ trò chuyện nổi toàn website (`FloatingChatWidget`).

**Bảng 3.15.** Danh mục các màn hình chính phía Khách hàng

| Tên màn hình | Đường dẫn URL (Route) | Trạng thái thực tế | Mục đích nghiệp vụ chính |
|---|---|---|---|
| Đăng nhập / Đăng ký | `/login`, `/register` | IMPLEMENTED | Xác thực danh tính người dùng, cấp phát JWT |
| Trang chủ & Danh sách sản phẩm | `/products` | IMPLEMENTED | Duyệt sản phẩm, tìm kiếm từ khóa, lọc danh mục BFS, lọc giá |
| Chi tiết sản phẩm | `/products/:id` | IMPLEMENTED | Xem thông số, gallery ảnh, chọn biến thể, gợi ý sản phẩm liên quan |
| Giỏ hàng mua sắm | `/cart` | IMPLEMENTED | Xem danh mục món hàng đã chọn phân tách theo từng gian hàng |
| Xác nhận đặt hàng (Checkout) | `/checkout` | IMPLEMENTED | Điền địa chỉ, áp dụng voucher tỷ lệ, chọn VNPay/COD |
| Kết quả thanh toán | `/payment-result` | IMPLEMENTED | Hiển thị kết quả thành công/thất bại từ VNPay |
| Lịch sử & Chi tiết đơn hàng | `/orders`, `/orders/:id` | IMPLEMENTED | Theo dõi quá trình vận chuyển, bấm "Đã nhận được hàng" |
| Đánh giá sản phẩm | Modal trên trang chi tiết | IMPLEMENTED | Gửi số sao đánh giá (1–5 sao), ảnh chụp và nhận xét |
| Hồ sơ cá nhân | `/profile` | IMPLEMENTED | Cập nhật họ tên, số điện thoại người dùng qua API `/users/me` |
| Cửa sổ chat nổi (Floating Chat) | Widget nổi toàn website | IMPLEMENTED | Trao đổi tin nhắn trực tiếp với Shop qua WebSocket STOMP |

[INSERT FIGURE: IMG-PRODUCT-LIST]  
*Hình 3.12. Màn hình duyệt danh sách sản phẩm với bộ lọc danh mục và khoảng giá*

[INSERT FIGURE: IMG-PRODUCT-DETAIL]  
*Hình 3.13. Màn hình chi tiết sản phẩm với gallery ảnh Cloudinary và bộ chọn biến thể động*

[INSERT FIGURE: IMG-CART]  
*Hình 3.14. Màn hình giỏ hàng phân tách sản phẩm rõ ràng theo từng gian hàng*

[INSERT FIGURE: IMG-CHECKOUT]  
*Hình 3.15. Màn hình xác nhận đặt hàng (Checkout) tích hợp áp dụng mã giảm giá*

[INSERT FIGURE: IMG-PAYMENT-RESULT]  
*Hình 3.16. Màn hình xác nhận kết quả thanh toán trực tuyến thành công*

[INSERT FIGURE: IMG-PROFILE]  
*Hình 3.17. Màn hình quản lý và cập nhật hồ sơ thông tin cá nhân của người dùng*

[INSERT FIGURE: IMG-CHAT-WIDGET]  
*Hình 3.18. Cửa sổ chat trực tiếp FloatingChatWidget kết nối thời gian thực với gian hàng*

### 3.6.2. Giao diện phía Người bán (Vendor)

Toàn bộ các tuyến đường quản trị của người bán (`/vendor/*`) được bảo vệ nghiêm ngặt bởi thành phần kiểm soát `RequireShop`. Khi người dùng truy cập, hệ thống tự động kiểm tra trạng thái gian hàng của tài khoản:
- Nếu chưa có gian hàng: Điều hướng về màn hình đăng ký mở shop.
- Nếu gian hàng đang ở trạng thái `PENDING`: Hiển thị thông báo yêu cầu chờ Ban quản trị phê duyệt.
- Nếu gian hàng bị `BANNED`: Khóa truy cập và hiển thị lý do tạm dừng hoạt động.
- Nếu gian hàng đang `ACTIVE`: Cho phép truy cập vào bảng điều khiển VendorNav.

**Bảng 3.16.** Danh mục các màn hình phía Người bán

| Tên màn hình | Đường dẫn URL | Trạng thái thực tế | Mục đích nghiệp vụ |
|---|---|---|---|
| Đăng ký gian hàng | Điều hướng từ RequireShop | IMPLEMENTED | Nhập thông tin tên shop, mô tả, số điện thoại |
| Danh sách sản phẩm | `/vendor/products` | IMPLEMENTED | Xem toàn bộ sản phẩm của shop, trạng thái tồn kho |
| Thêm/Sửa sản phẩm | `/vendor/products/new`, `/:id/edit` | IMPLEMENTED | Tải ảnh lên Cloudinary, tạo biến thể giá và tồn kho |
| Quản lý đơn hàng | `/vendor/orders` | IMPLEMENTED | Lọc đơn theo 5 trạng thái, xác nhận xuất kho hoặc hủy đơn hoàn kho |
| Quản lý khuyến mãi | `/vendor/vouchers` | IMPLEMENTED | Phát hành voucher riêng cho gian hàng (scope SHOP) |
| Báo cáo doanh thu | `/vendor/analytics` | IMPLEMENTED | Xem 4 thẻ KPI doanh thu, biểu đồ phân bổ trạng thái và doanh thu theo ngày |

[INSERT FIGURE: IMG-VENDOR-PRODUCT-FORM]  
*Hình 3.19. Form tạo sản phẩm mới của Vendor tích hợp tải ảnh Cloudinary và cấu hình biến thể*

[INSERT FIGURE: IMG-VENDOR-ORDERS]  
*Hình 3.20. Màn hình quản lý đơn hàng của Vendor với bộ lọc trạng thái và thao tác xuất kho*

[INSERT FIGURE: IMG-VENDOR-ANALYTICS]  
*Hình 3.21. Màn hình phân tích báo cáo doanh thu và biểu đồ kinh doanh của Vendor*

### 3.6.3. Giao diện phía Quản trị viên (Admin)

Giao diện Quản trị viên sử dụng khung mẫu riêng biệt (`AdminLayout`) với thanh điều hướng Sidebar đặt dọc bên trái màn hình, hoàn toàn tách biệt với layout của người mua và người bán.

**Bảng 3.17.** Danh mục các màn hình phía Quản trị viên

| Tên màn hình | Đường dẫn URL | Trạng thái thực tế | Mục đích nghiệp vụ |
|---|---|---|---|
| Bảng điều khiển (Dashboard) | `/admin` | IMPLEMENTED | Thống kê số liệu toàn sàn, biểu đồ doanh thu, danh sách đơn hàng mới |
| Phê duyệt gian hàng | `/admin/shop` | IMPLEMENTED | Xem hồ sơ shop chờ duyệt, bấm duyệt/từ chối, Khóa Shop (Ban) và Mở Khóa |
| Quản lý sản phẩm toàn sàn | `/admin/products` | IMPLEMENTED | Xem danh sách và kiểm soát toàn bộ sản phẩm trên hệ thống |
| Quản lý đơn hàng toàn sàn | `/admin/orders`, `/:id` | IMPLEMENTED | Tra cứu thông tin mọi đơn hàng và lịch sử thanh toán |
| Quản lý Voucher toàn sàn | `/admin/vouchers` | IMPLEMENTED | Tạo mã giảm giá cấp hệ thống (scope SYSTEM) |

[INSERT FIGURE: IMG-ADMIN-DASHBOARD]  
*Hình 3.22. Màn hình Dashboard quản trị toàn diện với biểu đồ doanh thu và các thẻ chỉ số KPI*

[INSERT FIGURE: IMG-ADMIN-SHOP]  
*Hình 3.23. Màn hình quản trị kiểm duyệt gian hàng với các tác vụ Phê duyệt, Khóa Shop và Mở Khóa*

---

## 3.7. Kết quả triển khai hệ thống

### 3.7.1. Môi trường triển khai thực tế

Hệ thống hiện tại được triển khai và kiểm thử toàn diện trên **môi trường cục bộ (Local Development Environment)** kết hợp liên kết với các dịch vụ đám mây thực tế:

```
[Máy chủ phát triển Cục bộ - Localhost Developer Machine]
├── Backend Service:    Spring Boot 4.1.0 (Java 21)  -->  Cổng: localhost:8080
├── Frontend Client:    React 19 + TypeScript + Vite  -->  Cổng: localhost:5173
├── Cơ sở dữ liệu:      MySQL Server 8.0              -->  Cổng: localhost:3306/ecommerce
│
├── [Các dịch vụ Đám mây bên ngoài - External Cloud Services]
│   ├── Lưu trữ hình ảnh:  Cloudinary Cloud CDN (Môi trường thật - Production ready)
│   ├── Cổng thanh toán:   VNPay Payment Gateway (Môi trường kiểm thử Sandbox)
│   └── Thư điện tử:       Google Gmail SMTP (Gửi mail qua tài khoản bảo mật App Password)
```

> **Lưu ý minh bạch về hiện trạng:** Hệ thống chưa được đóng gói bằng Docker Container, chưa thiết lập pipeline CI/CD tự động và chưa triển khai lên các máy chủ ảo hóa trên môi trường Internet công cộng. Cổng thanh toán VNPay hoạt động trên máy chủ thử nghiệm Sandbox.

### 3.7.2. Tổng hợp chức năng đã hoàn thành

**Bảng 3.18.** Ma trận đánh giá trạng thái hoàn thiện các chức năng hệ thống

| Phân hệ / Tác nhân | Tính năng nghiệp vụ | Trạng thái thực tế | Minh chứng kỹ thuật từ mã nguồn |
|---|---|---|---|
| **Khách hàng** | Đăng ký, đăng nhập tài khoản | `IMPLEMENTED` | Endpoint `/api/v1/auth/*`, mã hóa mật khẩu BCrypt, cấp JWT |
| **Khách hàng** | Xem sản phẩm, tìm kiếm BFS, lọc giá | `IMPLEMENTED` | Endpoint `/api/v1/products`, phân trang Spring Data, lọc đa tiêu chí |
| **Khách hàng** | Xem chi tiết & Gợi ý sản phẩm lai | `IMPLEMENTED` | Endpoint `/api/v1/products/recommendations`, fallback thuật toán mượt |
| **Khách hàng** | Quản lý giỏ hàng mua sắm | `IMPLEMENTED` | Thêm, cập nhật số lượng, xóa món hàng khỏi `cart_items` |
| **Khách hàng** | Đặt hàng đa gian hàng kèm voucher | `IMPLEMENTED` | Tách đơn tự động theo shop, chia giảm giá voucher theo tỷ lệ |
| **Khách hàng** | Thanh toán trực tuyến VNPay Sandbox | `IMPLEMENTED` | Sinh URL thanh toán có chữ ký HMAC, xử lý kết quả trả về |
| **Khách hàng** | Thanh toán khi nhận hàng (COD) | `IMPLEMENTED` | Tạo đơn tức thì ở trạng thái PENDING |
| **Khách hàng** | Lịch sử đơn & Xác nhận nhận hàng | `IMPLEMENTED` | Snapshot giá lịch sử, nút "Đã nhận được hàng" (`confirmDelivery`) |
| **Khách hàng** | Đánh giá sao và đăng ảnh nhận xét | `IMPLEMENTED` | Kiểm soát điều kiện đơn đã `DELIVERED`, upload ảnh đánh giá |
| **Khách hàng** | Nhận thông báo tự động Realtime | `IMPLEMENTED` | Sự kiện `AFTER_COMMIT` đẩy STOMP qua `/user/queue/notifications` |
| **Khách hàng** | Nhắn tin trực tiếp với người bán | `IMPLEMENTED` | Cửa sổ `FloatingChatWidget` nổi toàn sàn, WebSocket STOMP + JWT |
| **Khách hàng** | Chỉnh sửa thông tin cá nhân | `IMPLEMENTED` | Endpoint `/api/v1/users/me` và trang giao diện `ProfilePage.tsx` |
| **Người bán** | Đăng ký gian hàng & kiểm soát mở shop | `IMPLEMENTED` | Tạo bản ghi `Shop` trạng thái `PENDING`, bộ lọc `RequireShop` |
| **Người bán** | Tạo và chỉnh sửa sản phẩm, biến thể | `IMPLEMENTED` | Tải ảnh lên Cloudinary CDN, lưu biến thể dạng JSON attributes |
| **Người bán** | Quản lý vòng đời đơn hàng gian hàng | `IMPLEMENTED` | Cập nhật chuyển trạng thái CONFIRMED → SHIPPED |
| **Người bán** | Hủy đơn hàng kèm hoàn kho tự động | `IMPLEMENTED` | Modal xác nhận lý do hủy đơn, tự động rollback `stock_quantity` |
| **Người bán** | Tạo mã giảm giá riêng của shop | `IMPLEMENTED` | Tạo voucher có `scope = SHOP`, liên kết chính xác `shop_id` |
| **Người bán** | Phản hồi đánh giá của khách | `IMPLEMENTED` | Cập nhật nội dung trả lời của shop vào bản ghi `Review` |
| **Người bán** | Báo cáo thống kê doanh thu | `IMPLEMENTED` | Endpoint `/api/v1/vendor/analytics`, 4 thẻ KPI và chart trên UI |
| **Quản trị viên** | Phê duyệt hoặc từ chối mở gian hàng | `IMPLEMENTED` | Cập nhật trạng thái shop, cấp quyền `ROLE_VENDOR`, gửi email |
| **Quản trị viên** | Khóa hoặc mở khóa gian hàng | `IMPLEMENTED` | Nút Khóa Shop (`BANNED`) và Mở Khóa (`ACTIVE`) trên `AdminShopListPage` |
| **Quản trị viên** | Tra cứu toàn bộ đơn hàng trên sàn | `IMPLEMENTED` | API và giao diện xem danh sách đơn của tất cả các shop |
| **Quản trị viên** | Tạo mã khuyến mãi toàn sàn | `IMPLEMENTED` | Tạo voucher có `scope = SYSTEM`, áp dụng cho toàn bộ shop |
| **Quản trị viên** | Bảng số liệu thống kê Dashboard | `IMPLEMENTED` | Endpoint `/api/v1/admin/statistics/*`, biểu đồ và bảng 10 đơn mới |
| **Hạ tầng lõi** | Kiểm soát N+1 Query | `IMPLEMENTED` | Áp dụng `@BatchSize` và Fetch Join, kiểm thử qua Integration Tests |
| **Hạ tầng lõi** | Bảo đảm tính Idempotent của IPN | `IMPLEMENTED` | Khóa bi quan `PESSIMISTIC_WRITE` trên bảng `payments` |

### 3.7.3. Hình ảnh minh chứng hoạt động thực tế

[INSERT FIGURE: IMG-PRODUCT-LIST]  
*Hình 3.24. Minh chứng: Giao diện duyệt sản phẩm và tìm kiếm hoạt động trên môi trường thật*

[INSERT FIGURE: IMG-CHECKOUT]  
*Hình 3.25. Minh chứng: Luồng Checkout đa shop và áp dụng voucher thành công*

[INSERT FIGURE: IMG-VENDOR-ORDERS]  
*Hình 3.26. Minh chứng: Bảng điều khiển quản lý và chuyển trạng thái đơn hàng của Vendor*

[INSERT FIGURE: IMG-VENDOR-ANALYTICS]  
*Hình 3.27. Minh chứng: Báo cáo phân tích doanh thu và số liệu kinh doanh của Vendor*

[INSERT FIGURE: IMG-ADMIN-DASHBOARD]  
*Hình 3.28. Minh chứng: Bảng điều khiển Admin Dashboard với số liệu thống kê toàn sàn*

[INSERT FIGURE: IMG-ADMIN-SHOP]  
*Hình 3.29. Minh chứng: Admin phê duyệt gian hàng thành công và tác vụ Khóa/Mở khóa shop*

### 3.7.4. Đánh giá các hạn chế kỹ thuật còn tồn tại

Nhằm duy trì tính khách quan và liêm chính trong học thuật, hệ thống ghi nhận rõ ràng 3 nhóm hạn chế kỹ thuật:
1. **Về giao diện người dùng và nghiệp vụ:**
   - Yêu cầu Trả hàng/Hoàn tiền (Return & Refund) từ phía khách hàng (Buyer) chưa được hỗ trợ trên giao diện người dùng.
   - Chưa tích hợp tính năng quên mật khẩu / đặt lại mật khẩu tự động qua email OTP.
2. **Về tích hợp nghiệp vụ và thanh toán:**
   - Cơ chế hoàn tiền tự động (Refund) thông qua cổng VNPay đã được xây dựng logic sổ cái trên Backend (FROZEN state), nhưng chưa được xác minh E2E do thiếu quyền truy cập danh sách IP Sandbox từ VNPAY.
   - Chưa tích hợp API tính toán phí vận chuyển tự động (Shipping Cost) từ đối tác thứ ba như Giao Hàng Nhanh (hiện tại tính đồng giá hoặc miễn phí vận chuyển nội bộ).
3. **Về an toàn, bảo mật và khả năng mở rộng:**
   - Chuỗi mã JWT hiện chưa có cơ chế thu hồi tức thì (Token Revocation / Blacklist) khi người dùng chủ động đăng xuất.
   - Chưa đóng gói ứng dụng bằng Docker Container và chưa thiết lập quy trình tích hợp/triển khai liên tục (CI/CD).
   - Chưa tích hợp cơ sở dữ liệu Redis để phân phối tải truy vấn và quản lý Session Message Broker ngoại vi.

---

# CHƯƠNG 4. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

## 4.1. Kết quả đạt được

Đề tài **"Xây dựng hệ thống thương mại điện tử đa người bán"** đã hoàn thành việc nghiên cứu, thiết kế và triển khai một nền tảng TMĐT đa người bán hoàn chỉnh ở quy mô học thuật, đáp ứng đầy đủ các mục tiêu cốt lõi đã đề ra:

### 4.1.1. Về mặt chức năng
- Hiện thực hóa thành công chu trình thương mại khép kín: Khách hàng đăng ký tài khoản, tìm kiếm sản phẩm BFS đa tiêu chí, quản lý giỏ hàng đa shop, thanh toán trực tuyến qua VNPay hoặc COD, nhận thông báo thời gian thực, trò chuyện trực tiếp qua `FloatingChatWidget`, theo dõi trạng thái đơn, bấm xác nhận nhận hàng và đánh giá chất lượng sản phẩm.
- Xây dựng hoàn chỉnh không gian nghiệp vụ dành riêng cho Người bán: Đăng ký mở shop, chờ duyệt hồ sơ, đăng tải sản phẩm kèm hình ảnh tải lên Cloudinary CDN và biến thể phân loại, quản lý quy trình đóng gói xuất kho theo máy trạng thái, hủy đơn hoàn kho tự động, tạo mã giảm giá riêng, phản hồi nhận xét của khách và theo dõi báo cáo doanh thu (`VendorAnalyticsPage`).
- Nâng cấp chức năng dành cho Quản trị viên (Admin): Đã hoàn thiện giao diện Dashboard trang chủ `/admin` với số liệu thống kê đầy đủ (doanh thu, biểu đồ cột, danh sách đơn hàng mới nhất), quản trị đơn hàng toàn sàn, phê duyệt hồ sơ mở shop và thực hiện thao tác Khóa Shop (`BANNED`) / Mở Khóa (`ACTIVE`) trực quan trên giao diện.
- Tích hợp thành công tìm kiếm nâng cao (Advanced Search) theo danh mục đệ quy BFS, cùng công nghệ Notification Push (WebSocket STOMP) gửi thông báo Realtime và động cơ gợi ý sản phẩm lai (Hybrid Recommendation).

### 4.1.2. Về mặt kỹ thuật
- **Kiến trúc Modular Monolith chuẩn mực:** Tổ chức mã nguồn chặt chẽ thành 10 module nghiệp vụ độc lập, kiểm soát tuyệt đối ranh giới dữ liệu và giao tiếp hoàn toàn thông qua các hợp đồng nội bộ (Internal Service contracts).
- **Thiết kế ranh giới giao dịch an toàn:** Áp dụng `@Transactional` bảo đảm tính nguyên tử (Atomicity) khi tách đơn hàng đa shop; phân tách các tác vụ phụ bằng `@TransactionalEventListener(AFTER_COMMIT)` giúp hệ thống hoạt động ổn định, tránh dữ liệu rác.
- **Tích hợp thanh toán trực tuyến an toàn:** Tích hợp thành công cổng thanh toán VNPay Sandbox, xử lý Webhook IPN chuẩn xác với cơ chế kiểm tra chữ ký số HMAC-SHA512 và khóa bi quan (`PESSIMISTIC_WRITE`) loại bỏ nguy cơ xử lý trùng lặp giao dịch.
- **Tối ưu hóa hiệu năng CSDL:** Nhận diện và loại bỏ triệt để hiện tượng N+1 Query thông qua cấu hình `@BatchSize` và phép kết nối `JOIN FETCH`, được kiểm chứng qua các bài kiểm thử tự động.

## 4.2. Đánh giá hạn chế

Bên cạnh những kết quả tích cực đã đạt được, hệ thống vẫn còn một số điểm hạn chế cần được tiếp tục hoàn thiện:
- **Hạ tầng triển khai:** Ứng dụng mới chỉ dừng lại ở việc chạy cục bộ trên máy tính cá nhân (localhost), chưa được container hóa bằng Docker và chưa triển khai thực tế trên các nền tảng điện toán đám mây.
- **Tính năng mở rộng:** Còn thiếu các tính năng tiện ích nâng cao như khôi phục mật khẩu qua email OTP, tích hợp đơn vị vận chuyển (Giao Hàng Nhanh) tự động tính phí vận chuyển theo khoảng cách, và form yêu cầu Trả hàng/Hoàn tiền từ phía người mua.
- **Khả năng chịu tải phân tán:** Việc sử dụng in-memory message broker cho WebSocket STOMP và lưu trữ session xác thực trên một tiến trình duy nhất sẽ gây khó khăn nếu cần mở rộng hệ thống chạy trên nhiều máy chủ cùng lúc.

## 4.3. Hướng phát triển của đề tài

Để phát triển hệ thống từ một dự án học thuật tiến tới một nền tảng thương mại sẵn sàng phục vụ thực tế, lộ trình phát triển được phân chia theo 3 giai đoạn:

### 4.3.1. Giai đoạn ngắn hạn (Ưu tiên cao)
1. **Xác thực hoàn tiền E2E (VNPay Refund):** Đăng ký địa chỉ IP máy chủ với VNPay Sandbox để kích hoạt môi trường IPN hai chiều, kiểm thử tự động E2E luồng hoàn tiền số tiền giao dịch vào tài khoản khách.
2. **Giao diện yêu cầu Trả hàng/Hoàn tiền (Return & Refund):** Xây dựng form gửi yêu cầu trả hàng từ phía Buyer kèm tải ảnh bằng chứng lỗi sản phẩm, tích hợp quy trình xét duyệt của Admin/Vendor.
3. **Thu hồi token JWT khi đăng xuất:** Triển khai cơ chế Blacklist token trong bộ nhớ hoặc Redis tạm thời nhằm vô hiệu hóa tức thì JWT khi người dùng bấm Đăng xuất.

### 4.3.2. Giai đoạn trung hạn
4. **Container hóa và CI/CD:** Viết `Dockerfile` tối ưu hóa cho Backend và Frontend; xây dựng tệp cấu hình `docker-compose.yml` gom cụm ứng dụng cùng CSDL MySQL; thiết lập quy trình tự động kiểm thử và đóng gói bản build bằng GitHub Actions.
5. **Tích hợp bộ nhớ đệm Redis:** Áp dụng Redis Cache để lưu đệm thông tin danh mục, sản phẩm phổ biến nhằm giảm tải truy vấn cho CSDL; sử dụng Redis Pub/Sub làm message broker ngoài cho WebSocket, mở đường cho việc nhân rộng đa cụm server.
6. **Thuật toán máy học Recommendation:** Nâng cấp gợi ý sản phẩm lai (Hybrid Recommendation) từ heuristic thuần túy sang ứng dụng Machine Learning (Lọc cộng tác - Collaborative Filtering).

### 4.3.3. Giai đoạn dài hạn
7. **Mở rộng phương thức thanh toán:** Đăng ký hồ sơ doanh nghiệp để tích hợp cổng thanh toán VNPay thật trên môi trường Production; tích hợp thêm các ví điện tử phổ biến tại Việt Nam như MoMo, ZaloPay.
8. **Kết nối đối tác logistics:** Tích hợp API đơn vị vận chuyển (Giao Hàng Nhanh, Giao Hàng Tiết Kiệm) để tự động in vận đơn và đồng bộ hành trình di chuyển của gói hàng theo thời gian thực.
9. **Phát triển ứng dụng di động:** Xây dựng ứng dụng di động đa nền tảng (React Native hoặc Flutter) tái sử dụng toàn bộ hệ thống Backend REST API hiện có.

## 4.4. Kết luận chung

Đề tài "Xây dựng hệ thống thương mại điện tử đa người bán" đã chứng minh tính khả thi, hiệu quả và sự đúng đắn của việc áp dụng kiến trúc Modular Monolith trong quá trình phát triển các hệ thống phần mềm doanh nghiệp có độ phức tạp nghiệp vụ cao. 

Dự án không chỉ giải quyết trọn vẹn bài toán kinh doanh mua bán đa đối tác mà còn đóng vai trò là một tài liệu thực hành kỹ thuật phần mềm giá trị, vận dụng nhuần nhuyễn các chuẩn mực công nghệ hàng đầu hiện nay từ Spring Boot, JPA, bảo mật JWT, cổng thanh toán VNPay cho đến kiến trúc Frontend hiện đại với React và TypeScript. Các kinh nghiệm thực tế và giải pháp kỹ thuật đúc kết được trong suốt quá trình thực hiện đề tài này sẽ là hành trang kiến thức vững chắc cho việc nghiên cứu và xây dựng các hệ thống quy mô lớn hơn trong tương lai.

---

# TÀI LIỆU THAM KHẢO

[1] Craig Walls, *Spring in Action*, 6th Edition, Manning Publications, 2021.

[2] Martin Fowler, *Patterns of Enterprise Application Architecture*, Addison-Wesley Professional, 2002.

[3] Sam Newman, *Building Microservices: Designing Fine-Grained Systems*, 2nd Edition, O'Reilly Media, 2021.

[4] Spring Framework Team, *Spring Boot Reference Documentation*, 2024. [Online]. Available: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/

[5] Spring Security Team, *Spring Security Reference Documentation*, 2024. [Online]. Available: https://docs.spring.io/spring-security/reference/

[6] React Documentation Team, *React: The Library for Web and Native User Interfaces*, 2024. [Online]. Available: https://react.dev/

[7] Công ty Cổ phần Giải pháp Thanh toán Việt Nam (VNPAY), *Tài liệu đặc tả tích hợp Cổng thanh toán VNPAY*, 2023. [Online]. Available: https://sandbox.vnpayment.vn/apis/

[8] Cloudinary Inc., *Cloudinary Image and Video API Reference Documentation*, 2024. [Online]. Available: https://cloudinary.com/documentation

[9] Internet Engineering Task Force (IETF), *RFC 7519: JSON Web Token (JWT)*, May 2015. [Online]. Available: https://datatracker.ietf.org/doc/html/rfc7519

[10] STOMP Protocol Specification, *Simple Text Oriented Messaging Protocol (Version 1.2)*, Oct. 2012. [Online]. Available: https://stomp.github.io/stomp-specification-1.2.html

[11] Oracle Corporation, *MySQL 8.0 Reference Manual*, 2024. [Online]. Available: https://dev.mysql.com/doc/refman/8.0/en/

[12] Hibernate Community, *Hibernate ORM 6.x User Guide*, 2024. [Online]. Available: https://hibernate.org/orm/documentation/

[13] Erich Gamma, Richard Helm, Ralph Johnson, and John Vlissides, *Design Patterns: Elements of Reusable Object-Oriented Software*, Addison-Wesley, 1994.

[14] Shopee Vietnam, *Sàn giao dịch Thương mại Điện tử Shopee*, [Online]. Available: https://shopee.vn/

[15] Tiki Corporation, *Sàn giao dịch Thương mại Điện tử Tiki*, [Online]. Available: https://tiki.vn/

---

# PHỤ LỤC

## PHỤ LỤC A — BẢNG TỔNG HỢP TÍNH NĂNG HỆ THỐNG (FEATURE MATRIX)

**Bảng A.1.** Bảng ma trận đối soát tính năng cốt lõi và bằng chứng kỹ thuật

| Tác nhân | Tính năng | Trạng thái | Bằng chứng kiểm chứng mã nguồn |
|---|---|---|---|
| Customer | Đăng ký & Đăng nhập tài khoản | `IMPLEMENTED` | `POST /api/v1/auth/*`, BCrypt hash, JWT cấp phát |
| Customer | Xem sản phẩm & Lọc nâng cao BFS | `IMPLEMENTED` | `ProductListPage.tsx`, `GET /api/v1/products` với lọc khoảng giá |
| Customer | Xem chi tiết & Gợi ý Hybrid | `IMPLEMENTED` | Lọc sản phẩm liên quan và gợi ý theo lịch sử mua |
| Customer | Quản lý giỏ hàng mua sắm | `IMPLEMENTED` | `CartPage.tsx`, `POST/PUT/DELETE /api/v1/carts/items` |
| Customer | Đặt hàng đa shop & Chiết khấu voucher | `IMPLEMENTED` | `OrderService.checkout()` tách đơn theo shop, chia tỷ lệ |
| Customer | Thanh toán VNPay Sandbox | `IMPLEMENTED` | Ký HMAC-SHA512, redirect cổng thanh toán, nhận IPN |
| Customer | Thanh toán khi nhận hàng (COD) | `IMPLEMENTED` | Khởi tạo đơn hàng tức thời ở trạng thái `PENDING` |
| Customer | Lịch sử đơn hàng & Xác nhận giao | `IMPLEMENTED` | Customer nhấn "Đã nhận được hàng" gọi `/confirm-delivery` |
| Customer | Đánh giá sao & Gửi hình ảnh | `IMPLEMENTED` | `POST /api/v1/reviews`, ràng buộc đơn hàng phải `DELIVERED` |
| Customer | Nhận thông báo tự động Realtime | `IMPLEMENTED` | Đẩy STOMP qua `/user/queue/notifications` (`AFTER_COMMIT`) |
| Customer | Nhắn tin trực tiếp với Shop | `IMPLEMENTED` | `FloatingChatWidget` nổi toàn website, WebSocket STOMP + JWT |
| Customer | Cập nhật hồ sơ người dùng | `IMPLEMENTED` | Giao diện cập nhật fullName, phone qua `ProfilePage.tsx` |
| Vendor | Đăng ký mở gian hàng | `IMPLEMENTED` | `VendorShopRegistrationPage.tsx`, `RequireShop` kiểm soát |
| Vendor | Đăng tải & Chỉnh sửa sản phẩm | `IMPLEMENTED` | Upload ảnh Cloudinary, cấu hình biến thể JSON attributes |
| Vendor | Quản lý đơn hàng & Xuất kho | `IMPLEMENTED` | `VendorOrderListPage.tsx`, cập nhật chuyển trạng thái đơn |
| Vendor | Hủy đơn hàng tự động hoàn kho | `IMPLEMENTED` | `OrderService`, tự động roll-back `stock_quantity` |
| Vendor | Phát hành voucher gian hàng | `IMPLEMENTED` | `POST /api/v1/vouchers` với `scope = SHOP` |
| Vendor | Phản hồi đánh giá của khách | `IMPLEMENTED` | `PUT /api/v1/reviews/:id/vendor-reply` |
| Vendor | Báo cáo thống kê doanh thu | `IMPLEMENTED` | `VendorAnalyticsPage.tsx`, `GET /api/v1/vendor/analytics` |
| Admin | Phê duyệt / Từ chối mở gian hàng | `IMPLEMENTED` | Cập nhật shop, tự động cấp `ROLE_VENDOR`, gửi email thông báo |
| Admin | Khóa / Mở khóa hoạt động gian hàng | `IMPLEMENTED` | Nút Khóa Shop và Mở Khóa trên `AdminShopListPage.tsx` |
| Admin | Quản trị đơn hàng toàn hệ thống | `IMPLEMENTED` | Tra cứu đơn hàng toàn diện trên `AdminOrderListPage.tsx` |
| Admin | Phát hành voucher toàn sàn | `IMPLEMENTED` | `POST /api/v1/vouchers` với `scope = SYSTEM` |
| Admin | Bảng thống kê số liệu Dashboard | `IMPLEMENTED` | Báo cáo doanh thu, biểu đồ cột và đơn hàng trên `AdminDashboardPage` |
| System | Chống trùng lặp thanh toán IPN | `IMPLEMENTED` | Khóa bi quan `PESSIMISTIC_WRITE` trên bảng `payments` |
| System | Tối ưu hóa truy vấn CSDL | `IMPLEMENTED` | Khử N+1 Query bằng `@BatchSize` và Fetch Join |
