# CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI

---

## 1.1. Giới thiệu đề tài

Thương mại điện tử (TMĐT) là lĩnh vực ứng dụng công nghệ thông tin vào hoạt động mua bán hàng hóa và dịch vụ thông qua mạng Internet. Trong những năm gần đây, mô hình sàn giao dịch đa người bán (Multi-Vendor Marketplace) đã trở thành một trong những mô hình kinh doanh trực tuyến phổ biến và có tốc độ tăng trưởng cao tại Việt Nam, tiêu biểu là các nền tảng như Shopee, Tiki và Lazada. Trong mô hình này, nền tảng đóng vai trò trung gian kết nối người mua với nhiều người bán độc lập, đồng thời cung cấp các dịch vụ hỗ trợ như xử lý đơn hàng, thanh toán trực tuyến và giải quyết tranh chấp.

Đề tài **"Xây dựng hệ thống thương mại điện tử đa người bán"** (Enterprise Ecommerce) hướng đến việc thiết kế và triển khai một nền tảng TMĐT đa người bán hoàn chỉnh, áp dụng kiến trúc phần mềm hiện đại và tích hợp các tính năng cốt lõi của một sàn giao dịch trực tuyến thực tế.

Hệ thống được xây dựng gồm hai thành phần chính:

- **Backend**: Ứng dụng Spring Boot viết bằng Java 21, tổ chức theo kiến trúc **Modular Monolith** — một kiến trúc phần mềm trong đó toàn bộ hệ thống được triển khai như một đơn vị duy nhất nhưng được tổ chức nội bộ thành các module nghiệp vụ độc lập, giao tiếp với nhau qua các hợp đồng nội bộ (Internal Service contracts) thay vì phụ thuộc trực tiếp vào nhau. Hệ thống cung cấp REST API và hỗ trợ giao tiếp thời gian thực qua giao thức WebSocket (STOMP).

- **Frontend**: Ứng dụng Single Page Application (SPA) xây dựng bằng React + TypeScript + Vite, cung cấp giao diện riêng biệt cho ba nhóm người dùng: Người mua (Buyer), Người bán (Vendor) và Quản trị viên (Admin).

Hệ thống hỗ trợ đầy đủ luồng nghiệp vụ cốt lõi của một sàn TMĐT: từ đăng ký tài khoản, đăng ký mở gian hàng, đăng sản phẩm, thêm vào giỏ hàng, đặt hàng, thanh toán trực tuyến (VNPay) và thanh toán khi nhận hàng (COD), đến theo dõi trạng thái đơn hàng, đánh giá sản phẩm và nhắn tin thời gian thực giữa người mua và người bán.

---

## 1.2. Lý do chọn đề tài

### 1.2.1. Xu hướng thị trường

Thị trường TMĐT Việt Nam ghi nhận mức tăng trưởng đáng kể trong giai đoạn gần đây. Mô hình Multi-Vendor Marketplace, cho phép nhiều người bán cùng kinh doanh trên một nền tảng, chiếm tỷ trọng lớn và tiếp tục mở rộng. Sự phổ biến của các nền tảng như Shopee, Tiki, Lazada tại Việt Nam cho thấy nhu cầu thực tế của thị trường đối với loại hệ thống này.

### 1.2.2. Nhu cầu học tập và thực hành kỹ năng kỹ thuật

Việc xây dựng một hệ thống TMĐT đa người bán đòi hỏi vận dụng đồng thời nhiều kỹ thuật và kiến thức quan trọng trong phát triển phần mềm hiện đại:

- **Thiết kế kiến trúc phần mềm**: Modular Monolith — một giải pháp trung gian giữa kiến trúc Monolith truyền thống và Microservices, phù hợp với quy mô nhóm nhỏ nhưng vẫn đảm bảo khả năng mở rộng và bảo trì.
- **Xây dựng REST API**: Thiết kế và triển khai các API endpoint tuân thủ chuẩn REST, xử lý phân quyền theo vai trò người dùng.
- **Xác thực và phân quyền**: JSON Web Token (JWT), Spring Security với phân quyền theo vai trò (ROLE_USER, ROLE_VENDOR, ROLE_ADMIN).
- **Tích hợp cổng thanh toán thực tế**: VNPay — cổng thanh toán phổ biến tại Việt Nam, bao gồm xử lý Webhook IPN và đảm bảo idempotency thông qua Pessimistic Locking.
- **Quản lý giao dịch (Transaction Management)**: Phân tích và thiết kế ranh giới giao dịch, xử lý sự kiện sau khi commit (`@TransactionalEventListener AFTER_COMMIT`).
- **Giao tiếp thời gian thực**: WebSocket với giao thức STOMP, tích hợp xác thực JWT cho STOMP channel.
- **Tối ưu hóa truy vấn cơ sở dữ liệu**: Phân tích và giải quyết vấn đề N+1 Query, tối ưu với `@BatchSize`, bulk queries.
- **Lưu trữ tệp trên đám mây**: Tích hợp Cloudinary để lưu trữ và quản lý ảnh sản phẩm.

### 1.2.3. Tính phù hợp với chương trình đào tạo

Đề tài mang tính tổng hợp, yêu cầu vận dụng kiến thức từ nhiều môn học trong chương trình đào tạo ngành Công nghệ thông tin: lập trình hướng đối tượng, cơ sở dữ liệu quan hệ, thiết kế hệ thống, lập trình web và bảo mật ứng dụng. Việc xây dựng một hệ thống có quy mô thực tế giúp sinh viên tiếp cận gần hơn với môi trường phát triển phần mềm chuyên nghiệp.

---

## 1.3. Phát biểu bài toán

### 1.3.1. Bối cảnh bài toán

Một nền tảng TMĐT đa người bán cần phục vụ đồng thời ba nhóm đối tượng với nhu cầu và quyền truy cập khác nhau:

**Người mua (Buyer)** cần:
- Tìm kiếm và xem thông tin sản phẩm từ nhiều gian hàng khác nhau.
- Quản lý giỏ hàng, đặt hàng và lựa chọn phương thức thanh toán.
- Theo dõi trạng thái đơn hàng sau khi đặt.
- Đánh giá sản phẩm sau khi nhận hàng.
- Liên lạc trực tiếp với người bán để hỏi thêm thông tin.

**Người bán (Vendor)** cần:
- Đăng ký và quản lý gian hàng của mình.
- Đăng bán sản phẩm với đầy đủ thông tin: tên, mô tả, giá, tồn kho, biến thể (kích thước, màu sắc), hình ảnh.
- Quản lý các đơn hàng thuộc gian hàng của mình và cập nhật trạng thái xử lý.
- Tạo và quản lý mã giảm giá (voucher) cho gian hàng.
- Nhận phản hồi từ người mua và có thể trả lời đánh giá.

**Quản trị viên (Admin)** cần:
- Duyệt hoặc từ chối các yêu cầu mở gian hàng từ Vendor.
- Quản lý toàn bộ sản phẩm và đơn hàng trên hệ thống.
- Tạo và quản lý mã giảm giá cấp hệ thống (áp dụng cho tất cả gian hàng).

### 1.3.2. Các vấn đề kỹ thuật cần giải quyết

Bên cạnh các yêu cầu nghiệp vụ, hệ thống cần giải quyết một số vấn đề kỹ thuật đặc trưng của nền tảng TMĐT đa người bán:

**1. Tách đơn hàng theo người bán (Multi-vendor Order Splitting)**

*WHAT:* Một lần đặt hàng có thể bao gồm sản phẩm từ nhiều gian hàng khác nhau.
*WHY:* Người bán chỉ có quyền xem và xử lý đơn hàng thuộc gian hàng của mình. Không thể dùng chung một đơn hàng cho nhiều gian hàng.
*HOW:* Hệ thống tự động tách thành N đơn hàng riêng biệt (1 đơn/gian hàng) trong cùng một giao dịch, được liên kết với nhau qua một `payment_group_id` UUID chung để phục vụ thanh toán.
*WHEN:* Xảy ra tại thời điểm người mua xác nhận đặt hàng (checkout).

**2. Tích hợp cổng thanh toán và xử lý webhook**

*WHAT:* VNPay gửi kết quả thanh toán về hệ thống qua IPN (Instant Payment Notification) webhook sau khi người mua hoàn tất trên giao diện VNPay.
*WHY:* Kết quả thanh toán phải được cập nhật chính xác vào hệ thống mà không bị xử lý trùng lặp khi nhiều IPN request đến đồng thời.
*HOW:* Sử dụng Pessimistic Lock (`SELECT FOR UPDATE`) khi truy vấn Payment record — đảm bảo chỉ một request được xử lý tại một thời điểm.
*WHEN:* Sau khi người mua hoàn tất thanh toán trên trang VNPay Sandbox.

**3. Phân bổ giảm giá trong môi trường đa người bán**

*WHAT:* Mã giảm giá cấp hệ thống (SYSTEM voucher) áp dụng cho đơn hàng có sản phẩm từ nhiều gian hàng.
*WHY:* Giảm giá không thể áp dụng tùy tiện; cần phân bổ công bằng theo giá trị của từng gian hàng.
*HOW:* Tính theo công thức proportional split: `Giảm giá của gian hàng X = Tổng giảm giá × (Giá trị gian hàng X / Tổng giá trị đủ điều kiện)`.
*WHEN:* Tại bước preview checkout và checkout thực tế.

**4. Kiểm soát quyền truy cập theo vai trò**

*WHAT:* Ba nhóm người dùng (Buyer, Vendor, Admin) có quyền hạn khác nhau. Vendor chỉ được xem và sửa dữ liệu của gian hàng mình.
*WHY:* Đảm bảo bảo mật và tính đúng đắn của dữ liệu — không cho phép một Vendor thao tác trên đơn hàng của gian hàng khác.
*HOW:* Spring Security với `@PreAuthorize`, kiểm tra shop ownership thông qua `ShopInternalService.getShopIdByUserId()` và `isShopOwner()`.
*WHEN:* Mọi request đến protected endpoint.

**5. Giao tiếp thời gian thực**

*WHAT:* Chức năng chat giữa người mua và người bán yêu cầu nhận và gửi tin nhắn ngay lập tức mà không cần tải lại trang.
*WHY:* Mô hình request-response HTTP thông thường không đáp ứng được yêu cầu realtime — người dùng phải chủ động gửi request để kiểm tra tin nhắn mới.
*HOW:* WebSocket STOMP cho phép server chủ động đẩy tin nhắn đến client; JWT xác thực qua STOMP channel interceptor.
*WHEN:* Từ khi người dùng đăng nhập và kết nối WebSocket đến khi ngắt kết nối.

**6. Tính toàn vẹn dữ liệu trong giao dịch phức tạp**

*WHAT:* Quá trình đặt hàng gồm nhiều thao tác ghi: trừ tồn kho, tăng lượt sử dụng voucher, tạo đơn hàng, xóa giỏ hàng.
*WHY:* Nếu một bước thất bại (ví dụ: sản phẩm hết hàng sau khi đã tạo đơn), dữ liệu sẽ không nhất quán.
*HOW:* Bọc toàn bộ trong một `@Transactional` — nếu bất kỳ bước nào ném exception, toàn bộ thao tác bị rollback. Cập nhật voucher dùng atomic SQL `UPDATE ... SET used_count = used_count + 1`.
*WHEN:* Trong suốt quá trình xử lý checkout.

---

## 1.4. Mục tiêu đề tài

### 1.4.1. Mục tiêu về chức năng

| Nhóm | Mục tiêu chức năng |
|---|---|
| **Người mua** | Đăng ký, đăng nhập; duyệt và tìm kiếm sản phẩm; quản lý giỏ hàng; đặt hàng đa gian hàng; thanh toán VNPay và COD; xem lịch sử đơn hàng; đánh giá sản phẩm; chat với người bán; nhận thông báo |
| **Người bán** | Đăng ký gian hàng; quản lý sản phẩm (CRUD) với ảnh Cloudinary; quản lý đơn hàng và cập nhật trạng thái; quản lý mã giảm giá shop; chat với khách hàng |
| **Quản trị viên** | Duyệt gian hàng; quản lý sản phẩm toàn hệ thống; xem đơn hàng toàn hệ thống; quản lý mã giảm giá hệ thống |
| **Hệ thống** | Phân quyền theo vai trò; thông báo tự động theo sự kiện; giao tiếp thời gian thực; tài liệu API tự động (Swagger UI) |

### 1.4.2. Mục tiêu về kỹ thuật

- Áp dụng kiến trúc **Modular Monolith** với ranh giới module rõ ràng, giao tiếp cross-module qua Internal Service contracts (không truy cập trực tiếp Entity hay Repository của module khác).
- Xây dựng REST API nhất quán theo chuẩn RESTful với định dạng response thống nhất (`ApiResponse<T>`).
- Tích hợp cổng thanh toán **VNPay** trong môi trường sandbox, bảo đảm idempotency qua Pessimistic Locking.
- Triển khai giao tiếp thời gian thực bằng **WebSocket STOMP** với xác thực JWT.
- Tối ưu hóa truy vấn cơ sở dữ liệu để tránh vấn đề N+1 Query (kiểm thử bằng `NPlus1AuditTest`).
- Thiết kế ranh giới giao dịch hợp lý, sử dụng Event-Driven pattern (`@TransactionalEventListener AFTER_COMMIT`) để tách biệt các tác dụng phụ (notification, email) khỏi giao dịch chính.

### 1.4.3. Mục tiêu về học thuật

- Hiểu và vận dụng được kiến trúc Modular Monolith trong thực tiễn.
- Nắm vững quy trình thiết kế hệ thống TMĐT từ phân tích yêu cầu đến triển khai.
- Thực hành tích hợp hệ thống với các dịch vụ bên ngoài (VNPay, Cloudinary, Gmail SMTP).

---

## 1.5. Phạm vi đề tài

### 1.5.1. Phạm vi chức năng — TRONG phạm vi

Hệ thống được xây dựng và xác minh hoàn thành các chức năng sau (dựa trên source code thực tế):

**Quản lý người dùng và phân quyền:**
- Đăng ký tài khoản, đăng nhập, xác thực JWT (token 24 giờ).
- Phân quyền theo ba vai trò: ROLE_USER (Buyer), ROLE_VENDOR, ROLE_ADMIN.
- Bảo vệ endpoint bằng `@PreAuthorize` và Spring Security filter chain.

**Quản lý gian hàng:**
- Người bán đăng ký mở gian hàng (trạng thái PENDING).
- Quản trị viên duyệt gian hàng (chuyển sang ACTIVE); hệ thống gửi email thông báo tự động qua Gmail SMTP.
- Người bán chỉ hoạt động được sau khi gian hàng được duyệt.

**Quản lý sản phẩm:**
- CRUD sản phẩm với đầy đủ thông tin: danh mục (có cấu trúc cha-con), thương hiệu, biến thể (JSON attributes), hình ảnh.
- Upload ảnh sản phẩm lên Cloudinary; hỗ trợ chọn ảnh thumbnail, sắp xếp thứ tự.
- Tìm kiếm sản phẩm theo từ khóa, phân trang.
- Xóa mềm sản phẩm (chuyển sang trạng thái HIDDEN).

**Giỏ hàng và đặt hàng:**
- Thêm, cập nhật số lượng, xóa sản phẩm khỏi giỏ hàng; kiểm tra tồn kho khi thêm.
- Đặt hàng đa gian hàng: tự động tách thành N đơn hàng, gom chung bởi `payment_group_id`.
- Xem trước chi phí (preview checkout) bao gồm tính toán giảm giá voucher.

**Thanh toán:**
- Tích hợp VNPay Sandbox: tạo URL thanh toán, nhận và xử lý IPN webhook.
- Thanh toán khi nhận hàng (COD).
- Xử lý idempotency bằng Pessimistic Lock.
- Cơ chế thử lại thanh toán: đơn hàng giữ trạng thái PENDING cho đến khi thanh toán thành công.

**Quản lý đơn hàng:**
- Người mua xem lịch sử và chi tiết đơn hàng.
- Người bán xem và cập nhật trạng thái đơn hàng của gian hàng mình: `PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`.
- Quản trị viên xem toàn bộ đơn hàng hệ thống (chỉ xem).

**Đánh giá sản phẩm:**
- Người mua viết đánh giá sau khi nhận hàng (hệ thống kiểm tra điều kiện đơn hàng DELIVERED).
- Ràng buộc: 1 đánh giá/sản phẩm/đơn hàng.
- Người bán trả lời đánh giá (Backend đã triển khai).

**Mã giảm giá (Voucher):**
- Hai phạm vi: SYSTEM (Admin tạo) và SHOP (Vendor tạo).
- Hai loại: PERCENTAGE (phần trăm, có giới hạn tối đa) và FIXED_AMOUNT (số tiền cố định).
- Phân bổ giảm giá SYSTEM voucher theo tỷ lệ giá trị từng gian hàng.

**Thông báo:**
- Thông báo tự động: xác nhận đơn hàng, đang giao, đã giao, hủy; đánh giá mới; duyệt gian hàng.
- Giao diện thông báo với REST polling.

**Chat thời gian thực:**
- Nhắn tin hai chiều giữa người mua và người bán qua WebSocket STOMP.
- Giao diện Floating Chat Widget; Optimistic UI cho trải nghiệm nhắn tin tức thời.

**Lưu trữ ảnh:** Tích hợp Cloudinary; xóa ảnh an toàn qua `publicId` từ database.

**Tài liệu API:** Swagger UI tại `/swagger-ui.html`.

### 1.5.2. Phạm vi chức năng — NGOÀI phạm vi

Các tính năng sau không được triển khai trong phạm vi đề tài này:

| Tính năng | Ghi chú |
|---|---|
| Redis Cache | Dự kiến Phase 8, chưa thực hiện |
| Docker / Docker Compose | Dự kiến Phase 9, chưa thực hiện |
| Triển khai cloud (AWS) | Ngoài phạm vi đề tài |
| WebSocket Push Notification | Hiện dùng REST polling; push notification dự kiến Phase 8 |
| Hoàn trả tồn kho khi hủy đơn | Technical debt đã xác định, chưa triển khai |
| Admin Dashboard thống kê | Chưa có API thống kê |
| Cấm gian hàng (Ban Shop) | Trạng thái BANNED được định nghĩa nhưng chưa có API |
| Tìm kiếm nâng cao | Chỉ hỗ trợ tìm kiếm theo từ khóa |
| Ứng dụng di động | Chỉ giao diện web |

### 1.5.3. Phạm vi môi trường triển khai

Hệ thống được xây dựng và kiểm thử trong môi trường phát triển local:
- Backend: `localhost:8080` (Spring Boot)
- Frontend: `localhost:5173` (Vite dev server)
- Database: MySQL tại `localhost:3306`
- VNPay Sandbox (môi trường thử nghiệm)
- Chưa triển khai lên môi trường production.

---

## 1.6. Phương pháp thực hiện đề tài

### 1.6.1. Phương pháp phát triển phần mềm

Hệ thống được xây dựng theo phương pháp **phát triển lặp tăng dần (Incremental Development)**, ưu tiên hoàn thiện từng use case theo chiều dọc (từ API đến giao diện người dùng) thay vì xây dựng toàn bộ hệ thống rồi tích hợp cuối cùng.

Mỗi use case được thực hiện theo chu kỳ:

```
Phân tích yêu cầu nghiệp vụ
         ↓
Thiết kế (Entity, API contract, Service logic)
         ↓
Triển khai Backend (Controller → Service → Repository)
         ↓
Triển khai Frontend (UI → API integration)
         ↓
Kiểm thử (Integration test / Manual E2E test)
         ↓
Ghi chép vấn đề phát sinh và technical debt
         ↓
Checkpoint (cập nhật PROJECT_CONTEXT.md)
```

Ưu điểm của phương pháp này: mỗi use case sau khi hoàn thành đều có thể chạy và kiểm thử độc lập, giúp phát hiện lỗi sớm và tránh tích lũy nợ kỹ thuật lớn.

### 1.6.2. Phân chia giai đoạn phát triển

| Phase | Nội dung | Trạng thái |
|---|---|---|
| Phase 1 — Foundation | Thiết lập dự án, kiến trúc, xử lý ngoại lệ, chuẩn API | Hoàn thành |
| Phase 2 — Authentication | Đăng ký, đăng nhập, JWT, phân quyền theo vai trò | Hoàn thành |
| Phase 3 — Product Catalog | Danh mục, thương hiệu, sản phẩm, biến thể, CRUD, tìm kiếm, ảnh | Hoàn thành |
| Phase 3.5 — Shop | Đăng ký gian hàng, duyệt gian hàng | Hoàn thành |
| Phase 4 — Shopping | Giỏ hàng, kiểm tra tồn kho | Hoàn thành |
| Phase 5 — Order | Checkout đa gian hàng, quản lý đơn hàng, state machine | Hoàn thành |
| Phase 6 — Payment | VNPay, COD, IPN webhook, idempotency, payment retry | Hoàn thành |
| Phase 7 — Supporting Features | Đánh giá, thông báo, voucher, media, chat thời gian thực | Hoàn thành |
| Phase 8 — Advanced Features | Redis cache, WebSocket notification push | Chưa thực hiện |
| Phase 9 — Infrastructure | Docker, Docker Compose, AWS deployment | Chưa thực hiện |

### 1.6.3. Phương pháp kiểm thử

**Integration Tests** (Spring Boot Test framework):
- `TransactionAuditIntegrationTest`: Kiểm thử 3 kịch bản ranh giới giao dịch — (1) checkout đa gian hàng rollback khi 1 gian hàng hết hàng; (2) review notification AFTER_COMMIT và rollback; (3) VNPay IPN atomicity và idempotency. **Kết quả: 26/26 test cases pass.**
- `NPlus1AuditTest`: Kiểm thử giới hạn số câu truy vấn qua Hibernate statistics (ví dụ: `getCart` = 3 queries cố định; `checkout` = O(số gian hàng) queries).
- `VendorOrderManagementTest`: Kiểm thử luồng quản lý đơn hàng Vendor. **Kết quả: 4/4 test cases pass.**

**Kiểm thử thủ công**: Kiểm thử giao diện người dùng và luồng end-to-end qua trình duyệt; sử dụng Swagger UI để kiểm thử API trực tiếp.

### 1.6.4. Quản lý kiến trúc và tài liệu

Kiến trúc hệ thống được mô tả và kiểm soát qua:
- `ARCHITECTURE.md`: Định nghĩa các quy tắc kiến trúc (ranh giới module, hướng phụ thuộc, giao tiếp cross-module).
- `PROJECT_CONTEXT.md`: Nguồn tài liệu sự thật trong quá trình phát triển — ghi lại trạng thái hiện tại, technical debt và các quyết định kiến trúc quan trọng.

---

## 1.7. Khảo sát hệ thống liên quan

### 1.7.1. Shopee (shopee.vn)

**WHAT:** Sàn TMĐT đa người bán quy mô lớn, hoạt động tại Đông Nam Á.

**Đặc điểm nổi bật:**
- Kết nối hàng triệu người bán và người mua trong cùng một nền tảng.
- Hỗ trợ đa phương thức thanh toán: ví điện tử ShopeePay, thẻ ngân hàng, COD.
- Hệ thống voucher đa tầng: voucher shop, voucher nền tảng, voucher ngân hàng.
- Tính năng chat trực tiếp giữa người mua và người bán với giao diện widget nổi (Shopee Chat).
- Hệ thống đánh giá sản phẩm gắn với đơn hàng đã giao.

**Hạn chế ở góc độ học thuật:** Shopee là sản phẩm enterprise với kiến trúc Microservices phức tạp, không phù hợp để nghiên cứu và tái hiện toàn bộ trong phạm vi đề tài.

**Điểm tham khảo cho đề tài:**
- Ý tưởng Floating Chat Widget cho chức năng nhắn tin.
- Mô hình tách đơn hàng theo gian hàng.
- Hệ thống mã giảm giá đa tầng (SYSTEM + SHOP voucher).

### 1.7.2. Tiki (tiki.vn)

**WHAT:** Sàn TMĐT Việt Nam với mô hình kết hợp bán hàng trực tiếp và đa người bán.

**Đặc điểm nổi bật:**
- Kiểm soát chất lượng sản phẩm chặt chẽ; hệ thống giao hàng nhanh (TikiNOW).
- Đánh giá sản phẩm có xác minh mua hàng: chỉ người đã mua mới được đánh giá.

**Điểm tham khảo:** Cơ chế xác minh đủ điều kiện đánh giá (Review Eligibility) — tương tự cơ chế kiểm tra `DELIVERED order` trong hệ thống đề tài.

### 1.7.3. WooCommerce (woocommerce.com)

**WHAT:** Plugin TMĐT mã nguồn mở cho WordPress, hỗ trợ Multi-Vendor qua plugin bên thứ ba.

**Đặc điểm nổi bật:**
- Cho phép tùy biến sâu; kiến trúc plugin-based.
- Tích hợp nhiều cổng thanh toán qua plugin.

**Điểm tham khảo:** Cấu trúc quản lý sản phẩm với biến thể; quy trình quản lý đơn hàng theo trạng thái.

### 1.7.4. So sánh tổng hợp

| Tiêu chí | Shopee / Tiki | WooCommerce | Enterprise Ecommerce (Đề tài) |
|---|---|---|---|
| Quy mô | Enterprise, hàng triệu gian hàng | Vừa và nhỏ | Prototype học thuật |
| Kiến trúc | Microservices (Shopee) | Monolith Plugin-based | Modular Monolith |
| Multi-vendor | Có | Qua plugin bên thứ ba | Có (built-in) |
| Thanh toán | Đa dạng | Đa dạng (plugin) | VNPay + COD |
| Chat realtime | Có (Shopee Chat) | Không có sẵn | Có (WebSocket STOMP) |
| Voucher | Có (đa tầng) | Qua plugin | Có (SYSTEM + SHOP) |
| Kiểm thử tự động | Enterprise CI/CD | Tuỳ dự án | Integration tests (26/26 pass) |
| Triển khai | Cloud production | Shared hosting / VPS | Local development |

**Nhận xét:** Các hệ thống thương mại như Shopee và Tiki là sản phẩm enterprise được phát triển bởi đội ngũ lớn trong nhiều năm. Mục tiêu của đề tài không phải là sao chép các hệ thống này, mà là hiểu và tái hiện các cơ chế nghiệp vụ cốt lõi ở quy mô vừa đủ để học tập, đồng thời áp dụng các nguyên tắc kiến trúc phần mềm hiện đại: Modular Monolith, Clean Transaction Boundary và Event-Driven Architecture.

---

## 1.8. Bố cục báo cáo

Báo cáo được tổ chức thành 4 chương:

**Chương 1 — Tổng quan đề tài** (chương hiện tại)

Trình bày bối cảnh và lý do lựa chọn đề tài; phát biểu bài toán cần giải quyết với 6 vấn đề kỹ thuật đặc trưng; mục tiêu về chức năng, kỹ thuật và học thuật; phạm vi triển khai rõ ràng (trong phạm vi và ngoài phạm vi); phương pháp phát triển theo từng phase; khảo sát các hệ thống TMĐT liên quan (Shopee, Tiki, WooCommerce).

**Chương 2 — Cơ sở lý thuyết**

Trình bày nền tảng lý thuyết và công nghệ được sử dụng trong đề tài. Bao gồm: các khái niệm về TMĐT đa người bán, REST API, kiến trúc Modular Monolith, JSON Web Token, WebSocket và STOMP Protocol, Event-Driven Architecture; các mô hình và thuật toán được áp dụng (State Machine, Strategy Pattern, Observer/Event Pattern, Pessimistic Locking, Proportional Split); tổng quan các công nghệ cụ thể trong hệ thống (Spring Boot, React + TypeScript + Vite, MySQL, VNPay, Cloudinary, Tailwind CSS).

**Chương 3 — Xây dựng hệ thống**

Nội dung trọng tâm của báo cáo. Bao gồm:
- **Kiến trúc**: Sơ đồ kiến trúc tổng thể Modular Monolith và 13 module nghiệp vụ; biểu đồ phụ thuộc cross-module qua Internal Service contracts.
- **Phân tích Use Case**: Sơ đồ use case tổng thể và đặc tả chi tiết các luồng quan trọng (Checkout đa gian hàng, VNPay IPN, Cập nhật trạng thái đơn hàng, Viết đánh giá, Realtime Chat, Duyệt gian hàng).
- **Mô hình hóa hệ thống**: Sơ đồ lớp (Class Diagram), sơ đồ tuần tự (Sequence Diagram) cho 3 luồng chính, sơ đồ hoạt động (Activity Diagram), sơ đồ trạng thái (State Diagram) cho Order / Payment / Shop.
- **Thiết kế cơ sở dữ liệu**: ERD với 20 bảng dữ liệu, các ràng buộc và chỉ mục.
- **Thiết kế giao diện**: Minh họa giao diện người dùng cho cả ba nhóm Buyer, Vendor và Admin.
- **Kết quả triển khai**: Danh sách chức năng đã hoàn thành, hình ảnh thực tế và hạn chế hiện tại.

**Chương 4 — Kết luận và hướng phát triển**

Tổng kết kết quả đạt được so với mục tiêu đề ra; đánh giá điểm mạnh và hạn chế của hệ thống; đề xuất các hướng phát triển tiếp theo: Redis caching, WebSocket Notification push, Docker deployment, tìm kiếm nâng cao, hoàn trả tồn kho khi hủy đơn, ứng dụng di động.

---

*Kết thúc Chương 1.*
