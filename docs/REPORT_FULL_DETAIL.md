# BÁO CÁO ĐỒ ÁN NGÀNH

**TÊN ĐỀ TÀI:** XÂY DỰNG HỆ THỐNG THƯƠNG MẠI ĐIỆN TỬ ĐA NGƯỜI BÁN

---

> **Ghi chú về trạng thái tài liệu:**
>
> Các vị trí cần bổ sung trong quá trình nộp báo cáo chính thức:
> - `[INSERT FIGURE: IMG-XX]` — Vị trí chèn ảnh chụp màn hình
> - `[INSERT DIAGRAM: DIAGRAM-XX]` — Vị trí chèn sơ đồ UML
> - `[INSERT TABLE: TABLE-XX]` — Vị trí chèn bảng dữ liệu lớn
> - `[TODO: CẦN BỔ SUNG]` — Nội dung cần bổ sung thủ công
>
> Trạng thái tính năng: `IMPLEMENTED` / `PARTIAL` / `UI_ONLY` / `BACKEND_ONLY` / `MISSING`

---

## MỞ ĐẦU

Thương mại điện tử (TMĐT) đã và đang trở thành một trong những lĩnh vực có tốc độ tăng trưởng mạnh nhất trong nền kinh tế số tại Việt Nam. Sự xuất hiện và phát triển của các sàn giao dịch đa người bán (Multi-Vendor Marketplace) như Shopee, Tiki, Lazada đã chứng minh sức hút của mô hình này đối với cả người mua lẫn người bán hàng.

Đề tài **"Xây dựng hệ thống thương mại điện tử đa người bán"** (Enterprise Ecommerce) được thực hiện nhằm mục đích thiết kế và triển khai một nền tảng TMĐT đa người bán hoàn chỉnh ở quy mô học thuật, vận dụng các kiến trúc phần mềm và công nghệ hiện đại. Hệ thống được xây dựng trên nền tảng **Spring Boot (Java 21)** cho backend theo kiến trúc **Modular Monolith**, và **React + TypeScript + Vite** cho frontend. Ngoài ra, hệ thống tích hợp các dịch vụ thực tế như cổng thanh toán **VNPay**, dịch vụ lưu trữ ảnh **Cloudinary** và giao tiếp thời gian thực qua **WebSocket STOMP**.

Báo cáo này trình bày toàn bộ quá trình phân tích, thiết kế và xây dựng hệ thống, bao gồm kiến trúc tổng thể, thiết kế cơ sở dữ liệu, giao diện người dùng, kết quả đạt được và hạn chế còn tồn tại.

**Bố cục báo cáo gồm 4 chương:**

- **Chương 1:** Tổng quan đề tài — bài toán, mục tiêu, phạm vi và phương pháp thực hiện.
- **Chương 2:** Cơ sở lý thuyết — các khái niệm nền tảng, mô hình và công nghệ sử dụng.
- **Chương 3:** Xây dựng hệ thống — kiến trúc, phân tích, thiết kế, giao diện và kết quả triển khai.
- **Chương 4:** Kết luận và hướng phát triển.

---

# CHƯƠNG 1. TỔNG QUAN ĐỀ TÀI

---

## 1.1. Giới thiệu đề tài

Thương mại điện tử (TMĐT) là lĩnh vực ứng dụng công nghệ thông tin vào hoạt động mua bán hàng hóa và dịch vụ thông qua mạng Internet. Trong những năm gần đây, mô hình sàn giao dịch đa người bán (Multi-Vendor Marketplace) đã trở thành một trong những mô hình kinh doanh trực tuyến phổ biến và có tốc độ tăng trưởng cao tại Việt Nam, tiêu biểu là các nền tảng như Shopee, Tiki và Lazada. Trong mô hình này, nền tảng đóng vai trò trung gian kết nối người mua với nhiều người bán độc lập, đồng thời cung cấp các dịch vụ hỗ trợ như xử lý đơn hàng, thanh toán trực tuyến và giải quyết tranh chấp.

Đề tài **"Xây dựng hệ thống thương mại điện tử đa người bán"** (Enterprise Ecommerce) hướng đến việc thiết kế và triển khai một nền tảng TMĐT đa người bán hoàn chỉnh, áp dụng kiến trúc phần mềm hiện đại và tích hợp các tính năng cốt lõi của một sàn giao dịch trực tuyến thực tế.

Hệ thống được xây dựng gồm hai thành phần chính:

- **Backend:** Ứng dụng Spring Boot viết bằng Java 21, tổ chức theo kiến trúc **Modular Monolith** — một kiến trúc phần mềm trong đó toàn bộ hệ thống được triển khai như một đơn vị duy nhất nhưng được tổ chức nội bộ thành các module nghiệp vụ độc lập, giao tiếp với nhau qua các hợp đồng nội bộ (Internal Service contracts). Hệ thống cung cấp REST API và hỗ trợ giao tiếp thời gian thực qua giao thức WebSocket (STOMP).

- **Frontend:** Ứng dụng Single Page Application (SPA) xây dựng bằng React + TypeScript + Vite, cung cấp giao diện riêng biệt cho ba nhóm người dùng: Người mua (Buyer), Người bán (Vendor) và Quản trị viên (Admin).

Hệ thống hỗ trợ đầy đủ luồng nghiệp vụ cốt lõi của một sàn TMĐT: từ đăng ký tài khoản, đăng ký mở gian hàng, đăng sản phẩm, thêm vào giỏ hàng, đặt hàng, thanh toán trực tuyến (VNPay) và thanh toán khi nhận hàng (COD), đến theo dõi trạng thái đơn hàng, đánh giá sản phẩm và nhắn tin thời gian thực giữa người mua và người bán.

---

## 1.2. Lý do chọn đề tài

### 1.2.1. Xu hướng thị trường

Thị trường TMĐT Việt Nam ghi nhận mức tăng trưởng đáng kể trong giai đoạn gần đây. Mô hình Multi-Vendor Marketplace, cho phép nhiều người bán cùng kinh doanh trên một nền tảng, chiếm tỷ trọng lớn và tiếp tục mở rộng. Sự phổ biến của các nền tảng như Shopee, Tiki, Lazada tại Việt Nam cho thấy nhu cầu thực tế của thị trường đối với loại hệ thống này.

### 1.2.2. Nhu cầu học tập và thực hành kỹ năng kỹ thuật

Việc xây dựng một hệ thống TMĐT đa người bán đòi hỏi vận dụng đồng thời nhiều kỹ thuật và kiến thức quan trọng trong phát triển phần mềm hiện đại:

- **Thiết kế kiến trúc phần mềm:** Modular Monolith — một giải pháp trung gian giữa kiến trúc Monolith truyền thống và Microservices.
- **Xây dựng REST API:** Thiết kế và triển khai các API endpoint tuân thủ chuẩn REST, xử lý phân quyền theo vai trò người dùng.
- **Xác thực và phân quyền:** JSON Web Token (JWT), Spring Security với phân quyền theo vai trò (ROLE_USER, ROLE_VENDOR, ROLE_ADMIN).
- **Tích hợp cổng thanh toán thực tế:** VNPay — cổng thanh toán phổ biến tại Việt Nam, bao gồm xử lý IPN webhook và đảm bảo idempotency thông qua Pessimistic Locking.
- **Quản lý giao dịch (Transaction Management):** Phân tích và thiết kế ranh giới giao dịch, xử lý sự kiện sau khi commit (`@TransactionalEventListener AFTER_COMMIT`).
- **Giao tiếp thời gian thực:** WebSocket với giao thức STOMP, tích hợp xác thực JWT cho STOMP channel.
- **Lưu trữ tệp trên đám mây:** Tích hợp Cloudinary để lưu trữ và quản lý ảnh sản phẩm.

### 1.2.3. Tính phù hợp với chương trình đào tạo

Đề tài mang tính tổng hợp, yêu cầu vận dụng kiến thức từ nhiều môn học trong chương trình đào tạo ngành Công nghệ thông tin: lập trình hướng đối tượng, cơ sở dữ liệu quan hệ, thiết kế hệ thống, lập trình web và bảo mật ứng dụng.

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

**1. Tách đơn hàng theo người bán (Multi-vendor Order Splitting)**

- *WHAT:* Một lần đặt hàng có thể bao gồm sản phẩm từ nhiều gian hàng khác nhau.
- *WHY:* Người bán chỉ có quyền xem và xử lý đơn hàng thuộc gian hàng của mình.
- *HOW:* Hệ thống tự động tách thành N đơn hàng riêng biệt (1 đơn/gian hàng), liên kết qua `payment_group_id` UUID chung.

**2. Tích hợp cổng thanh toán và xử lý IPN webhook**

- *WHAT:* VNPay gửi kết quả thanh toán về hệ thống qua IPN webhook.
- *HOW:* Sử dụng Pessimistic Lock (`SELECT FOR UPDATE`) khi truy vấn Payment record — đảm bảo idempotency.

**3. Phân bổ giảm giá trong môi trường đa người bán**

- *HOW:* Proportional split — `Giảm giá gian hàng X = Tổng giảm giá × (Giá trị GH-X / Tổng giá trị đủ điều kiện)`.

**4. Kiểm soát quyền truy cập theo vai trò**

- *HOW:* Spring Security với `@PreAuthorize`, kiểm tra shop ownership qua `ShopInternalService.getShopIdByUserId()`.

**5. Giao tiếp thời gian thực**

- *HOW:* WebSocket STOMP — server chủ động đẩy tin nhắn đến client; JWT xác thực qua STOMP channel interceptor.

**6. Tính toàn vẹn dữ liệu trong giao dịch phức tạp**

- *HOW:* `@Transactional` bao quanh toàn bộ checkout; atomic voucher update `UPDATE ... SET used_count = used_count + 1`.

---

## 1.4. Mục tiêu đề tài

### 1.4.1. Mục tiêu về chức năng

| Nhóm | Mục tiêu chức năng |
|---|---|
| **Người mua** | Đăng ký, đăng nhập; duyệt và tìm kiếm sản phẩm; quản lý giỏ hàng; đặt hàng đa gian hàng; thanh toán VNPay và COD; xem lịch sử đơn hàng; đánh giá sản phẩm; chat với người bán |
| **Người bán** | Đăng ký gian hàng; quản lý sản phẩm (CRUD) với ảnh Cloudinary; quản lý đơn hàng; quản lý mã giảm giá shop; phản hồi đánh giá |
| **Quản trị viên** | Duyệt gian hàng; quản lý sản phẩm toàn hệ thống; xem đơn hàng; quản lý mã giảm giá hệ thống |
| **Hệ thống** | Phân quyền theo vai trò; thông báo tự động theo sự kiện; giao tiếp thời gian thực; tài liệu API tự động (Swagger UI) |

### 1.4.2. Mục tiêu về kỹ thuật

- Áp dụng kiến trúc **Modular Monolith** với ranh giới module rõ ràng, giao tiếp cross-module qua Internal Service contracts.
- Xây dựng REST API nhất quán với định dạng response thống nhất (`ApiResponse<T>`).
- Tích hợp cổng thanh toán **VNPay** sandbox, bảo đảm idempotency qua Pessimistic Locking.
- Triển khai giao tiếp thời gian thực bằng **WebSocket STOMP** với xác thực JWT.
- Tối ưu hóa truy vấn cơ sở dữ liệu, tránh N+1 Query.
- Sử dụng Event-Driven pattern (`@TransactionalEventListener AFTER_COMMIT`) để tách biệt các tác dụng phụ khỏi giao dịch chính.

---

## 1.5. Phạm vi đề tài

### 1.5.1. Phạm vi chức năng — TRONG phạm vi

**Quản lý người dùng và phân quyền:** Đăng ký, đăng nhập, JWT (token 24 giờ); phân quyền ba vai trò: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN; xem và chỉnh sửa thông tin hồ sơ cá nhân (Profile) tại `/profile`.

**Quản lý gian hàng:** Đăng ký (PENDING) → Admin duyệt (ACTIVE) hoặc khóa (BANNED) / mở khóa gian hàng vi phạm → Tự động gửi email thông báo qua Gmail SMTP và cập nhật quyền hạn.

**Quản lý sản phẩm:** CRUD với danh mục tự tham chiếu đa cấp (duyệt cây theo giải thuật BFS), thương hiệu, biến thể (JSON attributes), gallery ảnh Cloudinary CDN; tìm kiếm đa tiêu chí theo từ khóa, danh mục đệ quy và khoảng giá; xóa mềm (HIDDEN).

**Gợi ý sản phẩm (Recommendation):** Tích hợp động cơ gợi ý sản phẩm lai (Hybrid Recommendation Engine) dựa trên danh mục tương đồng, điểm đánh giá và doanh số bán ra.

**Giỏ hàng và đặt hàng:** Thêm/cập nhật/xóa sản phẩm; giỏ hàng gom nhóm đa shop; đặt hàng đa gian hàng (tự động tách $N$ đơn hàng độc lập theo shop trong cùng `payment_group_id`); preview checkout với mã giảm giá; cơ chế tự động hoàn trả tồn kho biến thể khi đơn hàng bị hủy (`restoreStockOnCancellation`).

**Thanh toán:** Tích hợp cổng thanh toán VNPay Sandbox + COD; xử lý IPN webhook bất đồng bộ với khóa bi quan (`PESSIMISTIC_WRITE`) trên bảng `payments` ngăn chặn duplicate transaction; cơ chế payment retry.

**Quản lý đơn hàng:** Buyer theo dõi lịch sử đơn hàng và chủ động bấm xác nhận nhận hàng (`DELIVERED`); Vendor cập nhật trạng thái đơn (`PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`); Admin giám sát và lọc toàn bộ đơn hàng trên toàn hệ thống.

**Đánh giá sản phẩm:** Người mua đánh giá chất lượng (1–5 sao) kèm hình ảnh thực tế sau khi đơn hàng chuyển sang `DELIVERED`; 1 review/sản phẩm/đơn; Vendor phản hồi công khai đánh giá của khách.

**Mã giảm giá:** Phạm vi SYSTEM (toàn sàn) và SHOP (gian hàng); loại PERCENTAGE và FIXED_AMOUNT; thuật toán phân bổ tỷ lệ chiết khấu (Proportional Split) cho SYSTEM voucher theo doanh số từng shop.

**Hệ thống thông báo thời gian thực:** Thông báo đẩy (Push Notification) qua WebSocket STOMP tới người dùng ngay khi có sự kiện mới, kết hợp lưu trữ lịch sử qua `@TransactionalEventListener AFTER_COMMIT`.

**Chat thời gian thực:** Khung chat nổi toàn diện (`FloatingChatWidget`) tích hợp toàn cục tại `PublicLayout.tsx`; giao tiếp hai chiều tức thì giữa Buyer và Vendor qua WebSocket STOMP kèm xác thực JWT trên kết nối.

**Báo cáo phân tích kinh doanh:** Admin Dashboard (`/admin`) hiển thị các thẻ chỉ số KPI toàn sàn, biểu đồ doanh thu và bảng đơn hàng mới; Vendor Analytics (`/vendor/analytics`) phân tích chỉ số kinh doanh và biểu đồ doanh thu 30 ngày của gian hàng.

**Lưu trữ ảnh đám mây:** Cloudinary Media Cloud SDK; upload đa phương tiện và xóa ảnh an toàn qua `publicId`.

**Tài liệu API:** SpringDoc OpenAPI 3.0.3 (Swagger UI tại `/swagger-ui/index.html`).

### 1.5.2. Phạm vi chức năng — NGOÀI phạm vi

| Tính năng | Ghi chú |
|---|---|
| Caching phân tán (Redis Cache) | Dự kiến lộ trình tương lai (Phase 10) |
| Container hóa (Docker / Docker Compose) | Dự kiến lộ trình tương lai (Phase 10) |
| Triển khai môi trường đám mây (AWS/GCP) | Ngoài phạm vi đề tài học thuật |
| Tìm kiếm chuyên sâu (Elasticsearch Engine) | Hiện tại hệ thống dùng duyệt cây BFS kết hợp Index CSDL |
| Cổng thanh toán môi trường Production | Chỉ thử nghiệm trên môi trường VNPay Sandbox |
| Ứng dụng di động (Mobile App iOS/Android) | Chỉ xây dựng Single Page Application trên nền tảng Web |
| Hoàn tiền tự động E2E trên VNPay thật | Chưa tích hợp trực tiếp do giới hạn IP Sandbox của cổng thanh toán |

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

Hệ thống được xây dựng theo phương pháp **phát triển lặp tăng dần (Incremental Development)**, ưu tiên hoàn thiện từng use case theo chiều dọc (từ API đến giao diện người dùng).

Chu kỳ mỗi use case:

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
Checkpoint (cập nhật PROJECT_CONTEXT.md)
```

### 1.6.2. Phân chia giai đoạn phát triển

| Phase | Nội dung | Trạng thái |
|---|---|---|
| Phase 1 — Foundation | Thiết lập dự án, kiến trúc Modular Monolith, xử lý ngoại lệ, chuẩn API | Hoàn thành |
| Phase 2 — Authentication | Đăng ký, đăng nhập, JWT (24h), phân quyền RBAC | Hoàn thành |
| Phase 3 — Product Catalog | Danh mục đa cấp BFS, thương hiệu, sản phẩm, biến thể, ảnh Cloudinary | Hoàn thành |
| Phase 3.5 — Shop | Đăng ký gian hàng, duyệt/khóa gian hàng, gửi mail Gmail SMTP | Hoàn thành |
| Phase 4 — Shopping | Giỏ hàng đa shop, kiểm tra tồn kho, tính toán tạm tính | Hoàn thành |
| Phase 5 — Order | Checkout đa gian hàng, ranh giới giao dịch atomicity, quản lý đơn hàng | Hoàn thành |
| Phase 6 — Payment | Cổng thanh toán VNPay Sandbox, COD, IPN webhook, khóa bi quan chống duplicate | Hoàn thành |
| Phase 7 — Supporting Features | Đánh giá kèm ảnh, thông báo event-driven, voucher proportional split, chat STOMP | Hoàn thành |
| Phase 8 — Advanced Features & Optimization | STOMP Push Notification, FloatingChatWidget, Profile cá nhân, hoàn trả kho khi hủy đơn | Hoàn thành |
| Phase 9 — Business Analytics & Advanced Search | Admin Dashboard KPI toàn sàn, Vendor Analytics 30 ngày, BFS Category search, Gợi ý sản phẩm lai | Hoàn thành |
| Phase 10 — Future Infrastructure | Redis Cache, Docker & Docker Compose, CI/CD, Triển khai Cloud | Dự kiến tương lai |

### 1.6.3. Phương pháp kiểm thử

**Integration Tests** (Spring Boot Test framework):
- `TransactionAuditIntegrationTest`: Kiểm thử 3 kịch bản ranh giới giao dịch (checkout rollback, review AFTER_COMMIT, VNPay IPN idempotency). **Kết quả: 26/26 test cases pass.**
- `NPlus1AuditTest`: Kiểm thử giới hạn số câu truy vấn.
- `VendorOrderManagementTest`: **Kết quả: 4/4 test cases pass.**

**Kiểm thử thủ công:** Giao diện người dùng và luồng E2E qua trình duyệt; Swagger UI cho kiểm thử API trực tiếp.

---

## 1.7. Khảo sát hệ thống liên quan

### 1.7.1. Shopee (shopee.vn)

Sàn TMĐT đa người bán quy mô lớn, hoạt động tại Đông Nam Á. Đặc điểm nổi bật: kết nối hàng triệu người bán; hỗ trợ đa phương thức thanh toán; hệ thống voucher đa tầng; chat trực tiếp (Shopee Chat); đánh giá gắn với đơn hàng đã giao.

**Điểm tham khảo:** Floating Chat Widget; mô hình tách đơn hàng theo gian hàng; hệ thống voucher SYSTEM + SHOP.

### 1.7.2. Tiki (tiki.vn)

Sàn TMĐT Việt Nam với kiểm soát chất lượng sản phẩm chặt chẽ. **Điểm tham khảo:** Cơ chế xác minh đủ điều kiện đánh giá (Review Eligibility) — chỉ người đã mua mới được đánh giá (tương tự cơ chế kiểm tra đơn DELIVERED trong đề tài).

### 1.7.3. WooCommerce (woocommerce.com)

Plugin TMĐT mã nguồn mở cho WordPress. **Điểm tham khảo:** Cấu trúc quản lý sản phẩm với biến thể; quy trình quản lý đơn hàng theo trạng thái.

### 1.7.4. So sánh tổng hợp

| Tiêu chí | Shopee / Tiki | WooCommerce | Enterprise Ecommerce (Đề tài) |
|---|---|---|---|
| Quy mô | Enterprise | Vừa và nhỏ | Prototype học thuật |
| Kiến trúc | Microservices | Monolith Plugin-based | Modular Monolith |
| Multi-vendor | Có | Qua plugin | Có (built-in) |
| Thanh toán | Đa dạng | Đa dạng (plugin) | VNPay + COD |
| Chat realtime | Có (Shopee Chat) | Không có sẵn | Có (WebSocket STOMP) |
| Voucher | Có (đa tầng) | Qua plugin | Có (SYSTEM + SHOP) |
| Triển khai | Cloud production | Shared hosting | Local development |

---

## 1.8. Bố cục báo cáo

**Chương 1 — Tổng quan đề tài:** Bối cảnh, lý do chọn đề tài, phát biểu bài toán, mục tiêu, phạm vi, phương pháp thực hiện, khảo sát hệ thống liên quan.

**Chương 2 — Cơ sở lý thuyết:** Các khái niệm nền tảng (TMĐT đa người bán, REST API, Modular Monolith, JWT, WebSocket/STOMP, Event-Driven Architecture); các mô hình và thuật toán áp dụng; tổng quan công nghệ sử dụng.

**Chương 3 — Xây dựng hệ thống:** Kiến trúc tổng thể; phân tích use case; mô hình hóa (Class Diagram, Sequence Diagram, Activity Diagram, State Diagram); thiết kế cơ sở dữ liệu; thiết kế giao diện (Customer, Vendor, Admin); kết quả triển khai và hạn chế.

**Chương 4 — Kết luận và hướng phát triển:** Tổng kết kết quả, đánh giá hạn chế, đề xuất hướng phát triển.

---

*Kết thúc Chương 1.*

---

# CHƯƠNG 2. CƠ SỞ LÝ THUYẾT

---

## 2.1. Các khái niệm nền tảng liên quan bài toán

### 2.1.1. Thương mại điện tử đa người bán (Multi-Vendor Ecommerce)

**WHAT:** Thương mại điện tử (E-commerce) là hình thức thực hiện các hoạt động mua bán hàng hóa, dịch vụ thông qua các phương tiện điện tử, chủ yếu là mạng Internet. Trong mô hình đa người bán (Multi-Vendor), một nền tảng cho phép nhiều người bán (Vendor) độc lập cùng kinh doanh, trong khi nền tảng đóng vai trò trung gian quản lý giao dịch.

**WHY:** Mô hình Multi-Vendor tạo ra hiệu ứng mạng lưới (network effect): càng nhiều Vendor tham gia, danh mục sản phẩm càng phong phú, thu hút thêm người mua, từ đó lại thu hút thêm Vendor.

**Các thành phần cốt lõi** của hệ thống Multi-Vendor Ecommerce:

- **Danh mục sản phẩm (Product Catalog):** Hệ thống tổ chức sản phẩm theo danh mục, hỗ trợ tìm kiếm và lọc.
- **Giỏ hàng (Shopping Cart):** Vùng lưu trữ tạm thời các sản phẩm người mua dự định mua.
- **Đơn hàng (Order):** Bản ghi chính thức của giao dịch mua bán.
- **Tách đơn đa người bán (Multi-vendor Order Splitting):** Tự động tạo N đơn hàng riêng (1/gian hàng), liên kết qua `payment_group_id`.
- **Cổng thanh toán (Payment Gateway):** Dịch vụ trung gian xử lý giao dịch tài chính.
- **Hệ thống đánh giá (Review System):** Cho phép người mua chia sẻ nhận xét sau khi nhận hàng.

### 2.1.2. Kiến trúc phần mềm — Modular Monolith

**WHAT:** Modular Monolith là một kiến trúc phần mềm trong đó toàn bộ hệ thống được đóng gói và triển khai như một đơn vị duy nhất (monolith), nhưng mã nguồn được tổ chức nội bộ thành các module nghiệp vụ tách biệt, rõ ràng, với ranh giới được kiểm soát nghiêm ngặt.

**WHY:** Kiến trúc này đưa ra câu trả lời cho câu hỏi thực tiễn: làm thế nào để có tổ chức code tốt và ranh giới nghiệp vụ rõ ràng của Microservices mà không phải chịu độ phức tạp vận hành của nó? Đối với hệ thống quy mô vừa hoặc đội phát triển nhỏ, Modular Monolith thường là lựa chọn tối ưu hơn.

**Bảng 2.1.** So sánh các kiến trúc phần mềm phổ biến

| Tiêu chí | Monolith truyền thống | Modular Monolith | Microservices |
|---|---|---|---|
| Triển khai | 1 unit | 1 unit | N services riêng biệt |
| Ranh giới module | Yếu / không có | Rõ ràng, được enforce | Rõ ràng qua network |
| Giao tiếp cross-module | Trực tiếp, bất kỳ | Qua Internal Service contracts | REST / gRPC / Message Queue |
| Phức tạp vận hành | Thấp | Thấp | Cao |
| Phù hợp với | Dự án nhỏ | Dự án vừa, đội nhỏ | Dự án lớn, nhiều team |
| Ứng dụng trong đề tài | Không | **Có** | Không |

**Trong hệ thống Enterprise Ecommerce**, kiến trúc Modular Monolith được thực thi theo quy tắc:
1. Mỗi module là một package Java riêng biệt.
2. Module không được truy cập trực tiếp Entity hay Repository của module khác.
3. Giao tiếp cross-module CHỈ qua `InternalService` interface.
4. Database vẫn là một instance MySQL chung — phân chia là ở tầng code, không phải tầng data.

### 2.1.3. REST API (Representational State Transfer)

**WHAT:** REST là kiểu kiến trúc phần mềm cho hệ thống phân tán, sử dụng giao thức HTTP. API theo chuẩn REST (RESTful API) là giao diện lập trình ứng dụng tuân theo các nguyên tắc REST.

**Các nguyên tắc REST:**
- **Stateless:** Mỗi request chứa đủ thông tin để xử lý; server không lưu trạng thái client.
- **Client-Server:** Phân tách giữa giao diện người dùng và logic xử lý.
- **Uniform Interface:** Sử dụng HTTP methods chuẩn (GET, POST, PUT, PATCH, DELETE).
- **Resource-based:** Tài nguyên được định danh qua URI.

**Áp dụng trong đề tài:** Toàn bộ Backend cung cấp REST API qua các Controller. Response được bọc trong `ApiResponse<T>` với cấu trúc thống nhất:

```json
{
  "success": true,
  "message": "Operation successful",
  "data": { ... }
}
```

### 2.1.4. JSON Web Token (JWT)

**WHAT:** JWT (JSON Web Token) là một tiêu chuẩn mở (RFC 7519) định nghĩa cách truyền thông tin an toàn giữa các bên dưới dạng đối tượng JSON được ký số (digitally signed).

**Cấu trúc JWT:** `Header.Payload.Signature`
- **Header:** Thuật toán ký (thường là HS256 hoặc RS256).
- **Payload:** Claims — thông tin về subject (userId, email, roles) và thời hạn (`exp`).
- **Signature:** HMAC-SHA256 của `base64(Header) + "." + base64(Payload)` với secret key.

**Áp dụng trong đề tài:**
- Sau đăng nhập thành công, server phát JWT token (expiration: 86400000ms = 24 giờ).
- Client lưu token trong `localStorage`, gửi kèm mọi request qua header `Authorization: Bearer {token}`.
- `JwtAuthenticationFilter` xác thực token trước khi xử lý request.
- Thư viện sử dụng: `jjwt-api 0.12.5` (io.jsonwebtoken).

**Hạn chế:** JWT không có cơ chế thu hồi token (revocation) — token hợp lệ cho đến khi hết hạn, ngay cả sau khi người dùng đăng xuất. Đây là hạn chế được ghi nhận trong phạm vi đề tài.

### 2.1.5. ORM và JPA/Hibernate

**WHAT:** ORM (Object-Relational Mapping) là kỹ thuật ánh xạ giữa đối tượng trong lập trình hướng đối tượng và bảng trong cơ sở dữ liệu quan hệ. JPA (Jakarta Persistence API) là đặc tả Java cho ORM; Hibernate là implementation phổ biến nhất của JPA.

**Áp dụng trong đề tài:**
- Entity class Java ánh xạ trực tiếp sang bảng MySQL qua annotation `@Entity`, `@Table`.
- Naming strategy: `CamelCaseToUnderscoresNamingStrategy` — `productVariant` → `product_variant`.
- DDL: `ddl-auto: update` — Hibernate tự cập nhật schema khi khởi động.
- Vấn đề N+1 Query được kiểm thử qua `NPlus1AuditTest` và giải quyết bằng `@BatchSize`, bulk queries.

### 2.1.6. WebSocket và Giao thức STOMP

**WHAT:** WebSocket là giao thức truyền thông hai chiều (bidirectional) qua một kết nối TCP duy nhất, cho phép server chủ động đẩy dữ liệu đến client mà không cần client gửi request trước. Đây là sự khác biệt căn bản so với HTTP request-response thông thường.

**STOMP (Simple Text Oriented Messaging Protocol):** Là giao thức messaging xây dựng trên WebSocket, cung cấp các khái niệm như destination (topic/queue), subscribe và send — tương tự message broker.

**Áp dụng trong đề tài:**
- Backend: `spring-boot-starter-websocket` với in-memory STOMP broker.
- Endpoint kết nối: `/ws` (với SockJS fallback).
- Destination: `/app/chat.send` (client → server), `/user/queue/messages` (server → client).
- Authentication: `JwtChannelInterceptor` xác thực JWT trong STOMP CONNECT frame.
- Frontend: `@stomp/stompjs 7.3.0` + `sockjs-client 1.6.1`.

### 2.1.7. Event-Driven Architecture và Transaction Management

**WHAT:** Event-Driven Architecture (EDA) là kiểu kiến trúc trong đó các thành phần hệ thống giao tiếp với nhau thông qua việc phát sinh và phản ứng với các sự kiện (events) thay vì gọi trực tiếp.

**`@TransactionalEventListener(AFTER_COMMIT)`:** Annotation của Spring cho phép một listener chỉ được kích hoạt sau khi transaction publishing event đã commit thành công. Điều này đảm bảo:
- Side effects (gửi email, tạo notification) chỉ xảy ra khi data đã được lưu chắc chắn vào DB.
- Nếu transaction chính rollback, side effects không xảy ra.

**Áp dụng trong đề tài:**
- `ShopApprovedEvent` → `AFTER_COMMIT` → cấp `ROLE_VENDOR` + gửi email thông báo.
- `OrderCreatedEvent` → `AFTER_COMMIT` → tạo notification cho Vendor.
- `OrderPaidEvent` → `AFTER_COMMIT` → chuyển đơn sang `CONFIRMED` + notification.

### 2.1.8. Payment IPN (Instant Payment Notification)

**WHAT:** IPN là cơ chế trong đó cổng thanh toán (VNPay) gửi thông báo HTTP POST đến endpoint của hệ thống sau khi giao dịch hoàn tất, bất kể người dùng có ngồi chờ hay không.

**Vấn đề idempotency:** IPN có thể được gửi nhiều lần cho cùng một giao dịch. Hệ thống phải đảm bảo chỉ xử lý một lần.

**Áp dụng trong đề tài:**
- Endpoint: `GET /api/v1/payments/vnpay/ipn`
- Xử lý: Verify HMAC-SHA512 signature từ VNPay → Pessimistic Lock (`SELECT FOR UPDATE`) trên Payment record → Kiểm tra `payment.status != PENDING` để phát hiện duplicate → Cập nhật status → Phát `OrderPaidEvent`.
- **Workaround localhost:** Do VNPay Sandbox không gọi được `localhost:5173`, frontend chủ động gọi IPN endpoint sau khi nhận redirect từ VNPay.

### 2.1.9. Voucher và Giảm giá trong đa người bán

**Hai phạm vi voucher:**
- `SYSTEM` — Admin tạo, áp dụng cho tất cả gian hàng.
- `SHOP` — Vendor tạo, chỉ áp dụng cho gian hàng đó.

**Hai loại giảm giá:**
- `PERCENTAGE` — Phần trăm (ví dụ: 20%), có thể giới hạn tối đa giảm (`maxDiscount`).
- `FIXED_AMOUNT` — Số tiền cố định (ví dụ: 50.000 VND).

**Proportional Split cho SYSTEM voucher:**

Khi 1 voucher SYSTEM áp dụng cho giỏ hàng có sản phẩm từ nhiều gian hàng, giảm giá được phân bổ theo tỷ lệ:

```
Giảm giá gian hàng X = Tổng giảm giá × (Giá trị GH-X / Tổng giá trị đủ điều kiện)
```

### 2.1.10. Thuật toán Gợi ý sản phẩm lai (Hybrid Recommendation Engine)

Hệ thống triển khai cơ chế gợi ý sản phẩm lai (Hybrid Recommendation Engine) kết hợp giữa hai phương pháp nhằm tối ưu hóa trải nghiệm khám phá sản phẩm của khách hàng:
1. **Lọc dựa trên nội dung (Content-based Filtering):** Dựa vào mối tương quan danh mục (`category_id`), thương hiệu (`brand_id`) và khoảng giá của các sản phẩm khách hàng đang xem hoặc đã mua.
2. **Độ phổ biến và phản hồi cộng đồng (Popularity & Rating Score):** Tính toán điểm số trọng số tổng hợp từ số lượng đơn hàng bán ra thành công (`order_items.quantity`) và điểm đánh giá trung bình (`AVG(rating)`) từ bảng `reviews`.

Công thức tính điểm ưu tiên gợi ý cho một sản phẩm $p$:
$$Score(p) = w_1 \cdot Sim(p, p_{viewed}) + w_2 \cdot RatingScore(p) + w_3 \cdot SalesVolume(p)$$

Trong đó $w_1, w_2, w_3$ là các hệ số trọng số. Cơ chế lai này giải quyết hiệu quả bài toán "Cold Start" (khi người dùng mới chưa có nhiều lịch sử hành vi) bằng cách ưu tiên các sản phẩm thịnh hành (Trending) có điểm đánh giá chất lượng cao.

### 2.1.11. Thuật toán duyệt cây danh mục đa cấp theo chiều rộng (BFS Category Traversal) kết hợp Indexing

Để hỗ trợ người mua tìm kiếm sản phẩm theo cấu trúc phân cấp danh mục (ví dụ: khi chọn danh mục cha *"Thời trang"*, hệ thống phải tìm thấy toàn bộ sản phẩm thuộc các danh mục con như *"Áo nam"*, *"Áo sơ mi"*, *"Quần jean"*), hệ thống áp dụng giải thuật **Duyệt theo chiều rộng (Breadth-First Search - BFS)**:
- **Cấu trúc dữ liệu:** Bảng `categories` lưu trữ dạng cây tự tham chiếu (`parent_id` liên kết khóa ngoại với `categories.id`).
- **Quy trình giải thuật:**
  1. Khởi tạo hàng đợi `Queue<Long> queue` chứa `categoryId` gốc do người dùng chọn và tập hợp kết quả `Set<Long> subCategoryIds`.
  2. Trong khi hàng đợi chưa rỗng: lấy mã danh mục hiện tại, truy vấn toàn bộ danh mục con trực tiếp (`WHERE parent_id = :currentId`), đưa các mã con vào cả `queue` và `subCategoryIds`.
  3. Lặp lại cho đến khi duyệt hết các nhánh con cháu. Độ phức tạp thời gian đạt $O(V + E)$ (với $V$ là số danh mục và $E$ là số quan hệ cha-con).
  4. Truy vấn sản phẩm bằng mệnh đề `WHERE category_id IN (:subCategoryIds)` được tăng tốc nhờ chỉ mục `idx_product_category` trên bảng `products`.

---

## 2.2. Các phương pháp, mô hình và thuật toán sử dụng

### 2.2.1. State Machine (Máy trạng thái hữu hạn)

**WHAT:** State Machine là mô hình tính toán trong đó hệ thống tại mỗi thời điểm ở một trong số hữu hạn các trạng thái (states), và chuyển từ trạng thái này sang trạng thái khác khi nhận được sự kiện (event) hoặc điều kiện thích hợp.

**Áp dụng trong đề tài:**

*State Machine của Order:*
```
PENDING → CONFIRMED → SHIPPED → DELIVERED
    └──────────────────────────→ CANCELLED
```
- `PENDING`: Đơn vừa tạo, chờ thanh toán (COD) hoặc chờ IPN xác nhận (VNPay).
- `CONFIRMED`: Thanh toán thành công; Vendor có thể xử lý.
- `SHIPPED`: Vendor đã giao cho đơn vị vận chuyển.
- `DELIVERED`: Đã giao thành công.
- `CANCELLED`: Vendor hoặc hệ thống hủy.

*State Machine của Payment:*
```
PENDING → SUCCESS
        → FAILED
```

*State Machine của Shop:*
```
PENDING → ACTIVE
        → BANNED
ACTIVE  → BANNED
BANNED  → ACTIVE (mở khóa)
```

### 2.2.2. Strategy Pattern

**WHAT:** Strategy Pattern là Design Pattern hành vi (Behavioral Pattern) cho phép định nghĩa một họ thuật toán, đóng gói từng thuật toán thành một class riêng biệt và làm cho chúng có thể hoán đổi cho nhau.

**Áp dụng trong đề tài:** `VoucherDiscountStrategy` — tính toán giảm giá theo loại voucher:
- `PercentageDiscountStrategy`: Tính `giá trị × percentage / 100`, áp dụng `maxDiscount` nếu có.
- `FixedAmountDiscountStrategy`: Giảm đúng `fixedAmount`, không giảm quá tổng giá trị.

### 2.2.3. Observer / Event Pattern (Spring Application Event)

**WHAT:** Observer Pattern là Design Pattern trong đó một subject (publisher) duy trì danh sách các observer (subscriber) và tự động thông báo khi có thay đổi.

**Áp dụng trong đề tài:** Spring ApplicationEvent system:
- Publisher: Service dùng `ApplicationEventPublisher.publishEvent(new XxxEvent(data))`.
- Subscriber: `@EventListener` hoặc `@TransactionalEventListener` method trong component khác.
- Ưu điểm: Decoupling — Order module không cần biết về Notification module.

### 2.2.4. Pessimistic Locking

**WHAT:** Pessimistic Locking là kỹ thuật kiểm soát đồng thời (concurrency control) trong đó một transaction giữ khóa trên tài nguyên trước khi đọc/ghi, ngăn các transaction khác truy cập cho đến khi khóa được giải phóng.

**Áp dụng trong đề tài:** IPN payment processing:
```java
@Lock(LockModeType.PESSIMISTIC_WRITE)
Optional<Payment> findByPaymentGroupId(UUID paymentGroupId);
```
Đảm bảo: Khi IPN đến đồng thời, chỉ 1 request được xử lý; các request còn lại phải chờ → phát hiện duplicate → bỏ qua.

### 2.2.5. Idempotency

**WHAT:** Một operation được gọi là idempotent nếu thực hiện nhiều lần cho kết quả giống như thực hiện một lần.

**Áp dụng trong đề tài:**
- IPN handler: Sau khi lock, kiểm tra `if (payment.status != PENDING) → return "already processed"`.
- Voucher usage: Atomic SQL `UPDATE vouchers SET used_count = used_count + 1 WHERE id = ? AND used_count < usage_limit`.

### 2.2.6. Snapshot Pattern trong đặt hàng

**WHAT:** Tại thời điểm đặt hàng, snapshot (bản sao) thông tin quan trọng được lưu vào `OrderItem` — đảm bảo nếu sản phẩm thay đổi giá hay tên sau này, thông tin đơn hàng cũ không bị ảnh hưởng.

**Áp dụng:** `OrderItem` lưu `unitPrice` (giá tại thời điểm đặt) và `productName` (tên snapshot) — không phải foreign key đến `Product`.

---

## 2.3. Tổng quan công nghệ sử dụng

### 2.3.1. Backend — Spring Boot (Java 21)

**Spring Boot 4.1.0** là framework Java cho phép xây dựng ứng dụng production-ready nhanh chóng với cấu hình tối thiểu. Các module Spring được sử dụng:

| Dependency | Mục đích |
|---|---|
| `spring-boot-starter-webmvc` | REST API, DispatcherServlet |
| `spring-boot-starter-data-jpa` | JPA/Hibernate ORM |
| `spring-boot-starter-security` | Authentication, Authorization |
| `spring-boot-starter-validation` | Bean Validation (JSR-380) |
| `spring-boot-starter-websocket` | WebSocket + STOMP |
| `spring-boot-starter-mail` | Email gửi qua SMTP |
| `spring-boot-starter-actuator` | Health check, monitoring |
| `jjwt-api 0.12.5` | JWT generation và validation |
| `springdoc-openapi 3.0.3` | Swagger UI documentation |
| `cloudinary-http44 1.36.0` | Cloudinary image storage |
| `mysql-connector-j` | MySQL JDBC driver |
| `lombok` | Code generation (getters, constructors) |

### 2.3.2. Frontend — React + TypeScript + Vite

| Công nghệ | Phiên bản | Mục đích |
|---|---|---|
| React | 19.2.8 | UI component framework |
| TypeScript | 6.0.2 | Type safety |
| Vite | 8.2.0 | Build tool, dev server |
| React Router DOM | 7.18.2 | Client-side routing |
| Axios | 1.19.0 | HTTP client |
| @stomp/stompjs | 7.3.0 | WebSocket STOMP client |
| sockjs-client | 1.6.1 | WebSocket fallback |
| Tailwind CSS | 4.3.3 | Utility-first CSS |
| lucide-react | 1.31.0 | Icon library |
| oxlint | 1.75.0 | Linting |

### 2.3.3. Cơ sở dữ liệu — MySQL

MySQL là hệ quản trị cơ sở dữ liệu quan hệ (RDBMS) mã nguồn mở phổ biến. Hệ thống sử dụng MySQL với các đặc điểm:
- Kết nối: `localhost:3306/ecommerce`
- Engine: InnoDB (hỗ trợ Foreign Key và ACID transactions)
- Schema quản lý: Hibernate `ddl-auto: update`

### 2.3.4. VNPay — Cổng thanh toán

VNPay là cổng thanh toán điện tử phổ biến tại Việt Nam, hỗ trợ thanh toán qua thẻ ATM nội địa, thẻ quốc tế và ví điện tử.

**Môi trường sử dụng:** Sandbox (`sandbox.vnpayment.vn`) — môi trường kiểm thử, không thực hiện giao dịch thật.

**Luồng tích hợp:**
1. Backend tạo URL thanh toán có chữ ký HMAC-SHA512 → Frontend redirect người dùng.
2. Người dùng thanh toán trên trang VNPay → VNPay redirect về `return_url`.
3. VNPay gửi IPN POST đến backend (hoặc Frontend gọi thủ công trong môi trường localhost).
4. Backend verify HMAC → xử lý kết quả với Pessimistic Lock.

### 2.3.5. Cloudinary — Dịch vụ lưu trữ ảnh

Cloudinary là dịch vụ cloud cho quản lý, lưu trữ và phục vụ ảnh và video. Hệ thống sử dụng Cloudinary để:
- Upload ảnh sản phẩm từ Vendor/Admin.
- Lưu trữ `imageUrl` (URL CDN) và `publicId` (để xóa ảnh sau này).
- Phục vụ ảnh qua CDN HTTPS — hiệu suất cao, tải nhanh.

### 2.3.6. Gmail SMTP — Email thông báo

Hệ thống gửi email tự động qua Gmail SMTP (port 587, STARTTLS) trong các trường hợp:
- Admin duyệt gian hàng → Email thông báo cho chủ shop.
- Cấu hình: App Password (không dùng mật khẩu Gmail trực tiếp).

---

*Kết thúc Chương 2.*

---

# CHƯƠNG 3. XÂY DỰNG HỆ THỐNG

---

## 3.1. Giới thiệu hệ thống

### 3.1.1. Mục đích hệ thống

Hệ thống **Enterprise Ecommerce** là một nền tảng thương mại điện tử đa người bán (Multi-Vendor Marketplace) được xây dựng từ đầu (from scratch) với mục đích kép:

1. **Mục đích nghiệp vụ:** Cung cấp một nền tảng trực tuyến cho phép nhiều người bán (Vendor) kinh doanh sản phẩm, trong khi người mua (Buyer) có thể tìm kiếm, đặt hàng và thanh toán trong một lần duy nhất cho dù sản phẩm đến từ nhiều gian hàng khác nhau.

2. **Mục đích kỹ thuật:** Trình diễn và thực hành các nguyên tắc phát triển phần mềm backend hiện đại ở quy mô hệ thống thực tế, bao gồm kiến trúc Modular Monolith, quản lý giao dịch phức tạp, tích hợp cổng thanh toán, xây dựng hệ thống realtime và tối ưu hóa truy vấn cơ sở dữ liệu.

### 3.1.2. Đối tượng sử dụng

Hệ thống phục vụ ba nhóm người dùng chính:

| Nhóm | Vai trò | Quyền truy cập |
|---|---|---|
| **Guest (Khách vãng lai)** | Người dùng chưa đăng nhập | Xem sản phẩm, danh mục, đánh giá |
| **Buyer (Người mua)** | User đã đăng nhập với ROLE_USER | Giỏ hàng, đặt hàng, thanh toán, đánh giá, chat |
| **Vendor (Người bán)** | User với ROLE_VENDOR + shop ACTIVE | Quản lý sản phẩm, đơn hàng, voucher, phản hồi review |
| **Admin (Quản trị viên)** | User với ROLE_ADMIN | Quản lý toàn bộ hệ thống: duyệt shop, sản phẩm, đơn hàng, voucher SYSTEM |

### 3.1.3. Chức năng chính của hệ thống

**Bảng 3.1.** Nhóm chức năng chính

| STT | Nhóm | Chức năng |
|---|---|---|
| 1 | Xác thực & Phân quyền | Đăng ký, đăng nhập, JWT, 3 vai trò |
| 2 | Quản lý gian hàng | Đăng ký → Duyệt → Hoạt động → Bị khóa |
| 3 | Quản lý sản phẩm | CRUD sản phẩm, biến thể, ảnh Cloudinary |
| 4 | Giỏ hàng | Thêm/sửa/xóa, kiểm tra tồn kho |
| 5 | Đặt hàng đa gian hàng | Tách đơn tự động, `payment_group_id` |
| 6 | Thanh toán | VNPay Sandbox + COD, IPN webhook |
| 7 | Quản lý đơn hàng | State machine, phân quyền theo shop |
| 8 | Đánh giá sản phẩm | Gated by DELIVERED order, vendor reply |
| 9 | Mã giảm giá | SYSTEM + SHOP scope, proportional split |
| 10 | Thông báo | Event-driven, REST polling |
| 11 | Chat realtime | WebSocket STOMP, JWT auth |
| 12 | Lưu trữ ảnh | Cloudinary CDN |

---

## 3.2. Kiến trúc hệ thống

### 3.2.1. Kiến trúc tổng thể

Hệ thống Enterprise Ecommerce sử dụng kiến trúc **Modular Monolith** — toàn bộ backend được đóng gói và triển khai như một ứng dụng Spring Boot duy nhất, nhưng mã nguồn được tổ chức thành các module nghiệp vụ tách biệt với ranh giới được kiểm soát nghiêm ngặt.

[INSERT DIAGRAM: DIAGRAM-ARCHITECTURE-OVERVIEW]

*Hình 3.1. Sơ đồ kiến trúc tổng thể hệ thống Enterprise Ecommerce*

**Các thành phần chính:**

```
┌─────────────────────────────────────────────────────────────────┐
│                        BROWSER / CLIENT                         │
│   React 19 + TypeScript + Vite (localhost:5173)                │
│   [CustomerUI] [VendorUI] [AdminUI]                            │
└─────────────────┬───────────────────┬───────────────────────────┘
                  │ REST API (Axios)   │ WebSocket (STOMP)
                  ↓                   ↓
┌─────────────────────────────────────────────────────────────────┐
│              SPRING BOOT BACKEND (localhost:8080)               │
│  ┌────────────────────────────────────────────────────────────┐ │
│  │                   INFRASTRUCTURE LAYER                     │ │
│  │  Security Filter Chain │ JWT Filter │ CORS │ Exception     │ │
│  │  WebSocket Config │ STOMP Broker │ JWT Channel Interceptor │ │
│  └────────────────────────────────────────────────────────────┘ │
│  ┌──────────┬──────────┬──────────┬──────────┬───────────────┐  │
│  │ identity │   shop   │ product  │   cart   │    order      │  │
│  ├──────────┼──────────┼──────────┼──────────┼───────────────┤  │
│  │ payment  │  review  │ voucher  │  notify  │     chat      │  │
│  ├──────────┼──────────┼──────────┼──────────┼───────────────┤  │
│  │  media   │ (shared module)                                 │  │
│  └──────────┴──────────┴──────────┴──────────┴───────────────┘  │
└──────────────────┬───────────────────────────────────────────────┘
                   │ JDBC / JPA (Hibernate)
                   ↓
┌─────────────────────────────────────────────────────────────────┐
│               MySQL (localhost:3306/ecommerce)                  │
│               20 bảng | ddl-auto: update                       │
└─────────────────────────────────────────────────────────────────┘

External Services:
   Cloudinary (media storage) | VNPay Sandbox (payment) | Gmail SMTP (email)
```

### 3.2.2. Cấu trúc module Backend

Backend được tổ chức thành **10 module nghiệp vụ** + **3 module hạ tầng**:

**Bảng 3.2.** Danh sách module backend

| Module | Package | Trách nhiệm chính |
|---|---|---|
| `identity` | `com.tmt.ecommerce.identity` | User, Role, Authentication, JWT |
| `shop` | `com.tmt.ecommerce.shop` | Shop lifecycle, approval |
| `product` | `com.tmt.ecommerce.product` | Product, Variant, Image, Category, Brand |
| `cart` | `com.tmt.ecommerce.cart` | Shopping Cart, CartItem |
| `order` | `com.tmt.ecommerce.order` | Order, OrderItem, order state machine |
| `payment` | `com.tmt.ecommerce.payment` | Payment, VNPay, IPN, idempotency |
| `voucher` | `com.tmt.ecommerce.voucher` | Voucher CRUD, discount calculation |
| `review` | `com.tmt.ecommerce.review` | Review, rating, vendor reply |
| `notification` | `com.tmt.ecommerce.notification` | Notification events, delivery |
| `chat` | `com.tmt.ecommerce.chat` | ChatRoom, ChatMessage, STOMP |
| `media` | `com.tmt.ecommerce.media` | Cloudinary upload/delete |
| `common` | `com.tmt.ecommerce.common` | Shared DTOs, exceptions, utils |
| `config` | `com.tmt.ecommerce.config` | Security, WebSocket, CORS, Async config |

**Quy tắc kiến trúc (được enforce):**
- Module KHÔNG được import `Entity` hay `Repository` của module khác.
- Giao tiếp cross-module CHỈ qua `XxxInternalService` interface.
- Tránh circular dependency giữa các module.

### 3.2.3. Giao tiếp cross-module qua Internal Service

**Bảng 3.3.** Các Internal Service contract

| Từ module | Gọi Internal Service | Thông tin cần |
|---|---|---|
| `order` | `ShopInternalService.getShopIdByUserId()` | Lấy shopId của Vendor đang đăng nhập |
| `order` | `ProductInternalService.getVariantForCheckout()` | Lấy giá, tồn kho, shopId của variant |
| `order` | `VoucherInternalService.validateAndApply()` | Xác nhận voucher + tăng usedCount |
| `payment` | `OrderInternalService.confirmOrders()` | Xác nhận đơn hàng sau khi thanh toán |
| `notification` | `IdentityInternalService.getUserById()` | Lấy thông tin user để tạo notification |
| `chat` | `ShopInternalService.getShopByUserId()` | Xác định shop khi Vendor kết nối chat |

### 3.2.4. Luồng xử lý tổng quát

**Luồng request thông thường:**
```
Browser
  → Axios (attach JWT Bearer token)
  → Spring Security Filter Chain (JwtAuthenticationFilter xác thực token)
  → DispatcherServlet
  → Controller (validate input, @PreAuthorize check)
  → Service (business logic, @Transactional)
  → Repository (Spring Data JPA → Hibernate → MySQL)
  → Service (publish ApplicationEvent nếu cần)
  → @TransactionalEventListener(AFTER_COMMIT) (side effects: notification, email)
  → Controller (wrap response trong ApiResponse<T>)
  → Browser nhận JSON
```

**Luồng WebSocket Chat:**
```
Browser
  → SockJS + STOMP Client connect /ws
  → JwtChannelInterceptor (xác thực JWT trong CONNECT frame)
  → STOMP Broker subscriptions (/user/queue/messages)
  → Client SEND /app/chat.send {roomId, content}
  → ChatController @MessageMapping
  → ChatService (lưu DB)
  → SimpMessagingTemplate.convertAndSendToUser() × 2
  → Cả Buyer và Vendor nhận MESSAGE frame realtime
```

---

## 3.3. Phân tích sơ đồ Use Case

### 3.3.1. Xác định Actor

Dựa trên source code thực tế, hệ thống có 7 actor:

**Bảng 3.4.** Danh sách Actor trong hệ thống

| Actor | Loại | Mô tả | Bằng chứng source code |
|---|---|---|---|
| **Guest** | Primary | Người dùng chưa đăng nhập | Endpoint `/api/v1/products`, `/api/v1/categories` không yêu cầu auth |
| **Buyer** | Primary | User với `ROLE_USER` | `@PreAuthorize("hasRole('USER')")` trong `ReviewController`, `CartController` |
| **Vendor** | Primary | User với `ROLE_VENDOR` + shop ACTIVE | `@PreAuthorize("hasAnyRole('VENDOR', 'ADMIN')")` trong `VendorProductController` |
| **Admin** | Primary | User với `ROLE_ADMIN` | `@PreAuthorize("hasRole('ADMIN')")` trong `AdminShopController` |
| **VNPay** | Secondary | Gửi IPN webhook sau giao dịch | `GET /api/v1/payments/vnpay/ipn` |
| **Cloudinary** | Secondary | Lưu ảnh, trả về URL và publicId | `FileStorageService` trong media module |
| **Gmail SMTP** | Secondary | Gửi email thông báo duyệt shop | `ShopNotificationListener` |

### 3.3.2. Sơ đồ Use Case tổng thể

[INSERT DIAGRAM: DIAGRAM-USE-CASE-OVERVIEW]

*Hình 3.2. Sơ đồ Use Case tổng thể hệ thống*

### 3.3.3. Đặc tả Use Case chi tiết

#### UC01 — Đăng ký tài khoản

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC01 |
| **Tên** | Đăng ký tài khoản |
| **Actor chính** | Guest |
| **Tiền điều kiện** | Guest chưa có tài khoản; email chưa được sử dụng |
| **Luồng chính** | 1. Guest nhập: email, mật khẩu, họ tên, số điện thoại (optional) → 2. Hệ thống validate (email unique, password format) → 3. Mã hóa mật khẩu BCrypt → 4. Lưu User với ROLE_USER mặc định → 5. Trả về thông báo thành công |
| **Luồng thay thế** | Email đã tồn tại → 409 Conflict "Email already registered" |
| **Hậu điều kiện** | User mới được tạo với ROLE_USER; có thể đăng nhập |
| **API** | `POST /api/v1/auth/register` |

#### UC02 — Đăng nhập

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC02 |
| **Tên** | Đăng nhập |
| **Actor chính** | Guest (Guest, Buyer, Vendor, Admin) |
| **Tiền điều kiện** | Đã có tài khoản |
| **Luồng chính** | 1. Nhập email + password → 2. Verify BCrypt hash → 3. Tạo JWT token (24h) chứa userId, email, roles → 4. Trả về token |
| **Luồng thay thế** | Sai email/password → 401 Unauthorized |
| **Hậu điều kiện** | Client lưu JWT; các request tiếp theo gửi `Authorization: Bearer {token}` |
| **API** | `POST /api/v1/auth/login` |

#### UC03 — Thêm vào giỏ hàng

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC03 |
| **Tên** | Thêm sản phẩm vào giỏ hàng |
| **Actor chính** | Buyer |
| **Tiền điều kiện** | Đã đăng nhập; ProductVariant tồn tại và có tồn kho |
| **Luồng chính** | 1. Buyer chọn variant + số lượng → 2. Kiểm tra `stockQuantity ≥ quantity` → 3. Nếu CartItem đã tồn tại → cập nhật quantity; Nếu chưa → tạo mới CartItem → 4. Trả về giỏ hàng cập nhật |
| **Luồng thay thế** | `stockQuantity < quantity` → 400 "Insufficient stock" |
| **API** | `POST /api/v1/carts/items` |

#### UC04 — Checkout đa gian hàng

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC04 |
| **Tên** | Đặt hàng (Checkout) |
| **Actor chính** | Buyer |
| **Actor phụ** | VNPay (nếu chọn thanh toán VNPay) |
| **Tiền điều kiện** | Giỏ hàng không rỗng; địa chỉ giao hàng được cung cấp |
| **Luồng chính** | 1. Buyer xác nhận thông tin, chọn phương thức thanh toán, nhập voucher → 2. Preview tính toán giảm giá (proportional split) → 3. `@Transactional` checkout: tạo N orders (1/shop) với `payment_group_id` chung; trừ `stockQuantity`; tăng `usedCount` voucher; xóa CartItems → 4a. COD: đơn `PENDING`, phát `OrderCreatedEvent` → 4b. VNPay: tạo URL thanh toán có HMAC signature → 5. Trả về orders + paymentUrl |
| **Luồng thay thế** | Sản phẩm hết tồn kho sau khi đã thêm vào giỏ → 409; toàn bộ transaction rollback |
| **Hậu điều kiện** | N orders tạo thành công; voucher usedCount +1; CartItems xóa; Vendor nhận notification |
| **API** | `POST /api/v1/orders/checkout` |

#### UC05 — Xử lý thanh toán VNPay

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC05 |
| **Tên** | Xử lý kết quả thanh toán VNPay |
| **Actor chính** | VNPay (IPN), Buyer (redirect) |
| **Tiền điều kiện** | Buyer đã được redirect sang VNPay; giao dịch đã xử lý |
| **Luồng chính** | 1. VNPay redirect về `payment-result` với params → 2. Frontend gọi `GET /api/v1/payments/vnpay/ipn?{params}` → 3. Backend verify HMAC-SHA512 → 4. `PESSIMISTIC_WRITE` lock Payment → 5. Check `payment.status != PENDING` (idempotency) → 6. Update `payment.status = SUCCESS` → 7. Phát `OrderPaidEvent` → 8. `AFTER_COMMIT`: Orders chuyển `CONFIRMED`, gửi notification |
| **Luồng thay thế** | HMAC không khớp → 400 "Invalid signature"; Payment đã xử lý → bỏ qua (idempotency) |
| **API** | `GET /api/v1/payments/vnpay/ipn` |

#### UC06 — Vendor cập nhật trạng thái đơn hàng

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC06 |
| **Tên** | Cập nhật trạng thái đơn hàng |
| **Actor chính** | Vendor |
| **Tiền điều kiện** | Order thuộc shop của Vendor; transition hợp lệ |
| **Luồng chính** | 1. Vendor chọn đơn → nhấn "Giao hàng" (CONFIRMED → SHIPPED) → 2. Verify shop ownership → 3. Validate transition hợp lệ → 4. Update `order.status` → 5. Phát `OrderStatusChangedEvent` → 6. `AFTER_COMMIT`: Buyer nhận notification |
| **Luồng thay thế** | Transition không hợp lệ → 400; Không phải shop owner → 403 |
| **API** | `PATCH /api/v1/vendor/orders/:id/status` |

#### UC07 — Admin duyệt gian hàng

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC07 |
| **Tên** | Duyệt gian hàng |
| **Actor chính** | Admin |
| **Actor phụ** | Gmail SMTP |
| **Tiền điều kiện** | Shop đang ở trạng thái PENDING |
| **Luồng chính** | 1. Admin nhấn "Duyệt" → 2. `shop.status = ACTIVE` → 3. `@Transactional` commit → 4. `AFTER_COMMIT`: phát `ShopApprovedEvent` → 5. `ShopNotificationListener`: cấp ROLE_VENDOR cho user; gửi email thông báo qua Gmail SMTP |
| **Hậu điều kiện** | Shop ACTIVE; User có ROLE_VENDOR; Email gửi thành công |
| **API** | `PUT /api/v1/admin/shops/:id/approve` |

#### UC08 — Viết đánh giá sản phẩm

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC08 |
| **Tên** | Viết đánh giá sản phẩm |
| **Actor chính** | Buyer |
| **Tiền điều kiện** | Buyer đã có đơn hàng DELIVERED chứa sản phẩm cần đánh giá; Chưa có review cho sản phẩm + đơn hàng này |
| **Luồng chính** | 1. Buyer nhập rating (1-5 sao) + nội dung + ảnh (optional) → 2. Kiểm tra ReviewEligibility (DELIVERED + chưa review) → 3. Tạo Review → 4. Phát `ReviewCreatedEvent` → 5. `AFTER_COMMIT`: Notification đến Vendor |
| **Luồng thay thế** | Chưa đủ điều kiện → 400 "Không đủ điều kiện để đánh giá" |
| **API** | `POST /api/v1/reviews` |

#### UC09 — Khách hàng xác nhận nhận hàng (Confirm Delivery)

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC09 |
| **Tên** | Khách hàng xác nhận nhận hàng |
| **Actor chính** | Buyer (Khách hàng) |
| **Tiền điều kiện** | Đơn hàng đang ở trạng thái `SHIPPED` và thuộc quyền sở hữu của Buyer đang đăng nhập |
| **Luồng chính** | 1. Khách hàng vào trang chi tiết đơn hàng `/orders/:id` → 2. Nhấn nút "Đã nhận được hàng" → 3. Hệ thống kiểm tra quyền sở hữu và trạng thái `SHIPPED` → 4. Cập nhật `order.status = DELIVERED` → 5. Ghi nhận thời gian hoàn tất đơn hàng → 6. Mở quyền viết đánh giá sản phẩm (`ReviewEligibility`) |
| **Luồng thay thế** | Đơn hàng không ở trạng thái SHIPPED hoặc không phải chủ đơn hàng → Trả về mã lỗi 400/403 |
| **API** | `POST /api/v1/orders/:id/confirm-delivery` |

#### UC10 — Nhắn tin thời gian thực qua Khung chat nổi (Floating Chat Widget)

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC10 |
| **Tên** | Nhắn tin thời gian thực qua Khung chat nổi |
| **Actor chính** | Buyer, Vendor |
| **Tiền điều kiện** | Đã đăng nhập vào hệ thống, có JWT token hợp lệ |
| **Luồng chính** | 1. Người dùng bấm biểu tượng khung chat nổi ở góc dưới bên phải màn hình (`FloatingChatWidget`) → 2. Hệ thống kết nối WebSocket STOMP tại `/ws` với Header `Authorization: Bearer <token>` → 3. Đăng ký nhận tin nhắn tại `/topic/chat/{roomId}` → 4. Người dùng nhập tin nhắn và gửi đến `/app/chat.send` → 5. Backend lưu tin nhắn vào bảng `chat_messages` và broadcast tức thì đến phòng chat |
| **Luồng thay thế** | Mất kết nối mạng hoặc token hết hạn → Hiển thị thông báo kết nối lại tự động |
| **Giao thức/API** | WebSocket STOMP: `/ws`, `/app/chat.send`, `/topic/chat/{roomId}` |

#### UC11 — Xem báo cáo thống kê & Phân tích kinh doanh

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC11 |
| **Tên** | Xem báo cáo thống kê & Phân tích kinh doanh |
| **Actor chính** | Admin (Thống kê toàn sàn), Vendor (Phân tích gian hàng) |
| **Tiền điều kiện** | Đăng nhập với quyền `ROLE_ADMIN` hoặc `ROLE_VENDOR` (shop ACTIVE) |
| **Luồng chính** | 1. Admin truy cập `/admin`: Hệ thống truy vấn tổng doanh thu toàn sàn, số lượng đơn hàng, số người dùng và biểu đồ doanh thu theo thời gian qua API `/api/v1/admin/statistics/overview`.<br>2. Vendor truy cập `/vendor/analytics`: Hệ thống tổng hợp doanh thu 30 ngày gần nhất, tỷ lệ hoàn tất đơn hàng và giá trị trung bình đơn qua API `/api/v1/vendor/analytics/revenue`. |
| **API** | `GET /api/v1/admin/statistics/overview`, `GET /api/v1/vendor/analytics/revenue` |

---

## 3.4. Mô hình hóa hệ thống

### 3.4.1. Sơ đồ lớp (Class Diagram)

#### 3.4.1.1. Tổng quan các lớp thực thể

Hệ thống Enterprise Ecommerce có 18 Entity class chính, phân bố trong 8 module:

**Bảng 3.5.** Danh sách Entity class theo module

| Module | Entity | Bảng DB |
|---|---|---|
| identity | User, Role | `users`, `roles`, `user_roles` |
| shop | Shop | `shops` |
| product | Brand, Category, Product, ProductVariant, ProductImage | `brands`, `categories`, `products`, `product_variants`, `product_images` |
| cart | Cart, CartItem | `carts`, `cart_items` |
| order | Order, OrderItem | `orders`, `order_items` |
| payment | Payment | `payments` |
| voucher | Voucher | `vouchers` |
| review | Review, ReviewImage | `reviews`, `review_images` |
| notification | Notification | `notifications` |
| chat | ChatRoom, ChatMessage | `chat_rooms`, `chat_messages` |

#### 3.4.1.2. Sơ đồ lớp — nhóm Product

[INSERT DIAGRAM: DIAGRAM-CLASS-PRODUCT]

*Hình 3.3. Sơ đồ lớp nhóm Product*

**Các quan hệ chính:**
- `Category` tự liên kết: `parent_id → Category.id` (cây đệ quy cha-con)
- `Product` — `Category`: Many-to-One (`category_id`)
- `Product` — `ProductVariant`: One-to-Many; `cascade = ALL`, `orphanRemoval = true`
- `Product` — `ProductImage`: One-to-Many; `cascade = ALL`, `orphanRemoval = true`
- `ProductVariant.attributes` kiểu `String` (JSON serialized): `{"color":"Đỏ","size":"M"}`

#### 3.4.1.3. Sơ đồ lớp — nhóm Order/Payment

[INSERT DIAGRAM: DIAGRAM-CLASS-ORDER]

*Hình 3.4. Sơ đồ lớp nhóm Order và Payment*

**Các quan hệ chính:**
- `Order` — `OrderItem`: One-to-Many
- `OrderItem.unitPrice` và `OrderItem.productName` — snapshot tại thời điểm đặt hàng
- N `Order` ↔ 1 `Payment` qua `payment_group_id` UUID (không phải FK trực tiếp)
- `Payment.paymentGroupId` là UUID random tạo khi checkout

### 3.4.2. Sơ đồ tuần tự (Sequence Diagram)

#### 3.4.2.1. Luồng Checkout đa gian hàng

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-CHECKOUT]

*Hình 3.5. Sơ đồ tuần tự luồng Checkout đa gian hàng*

**Mô tả các bước:**
1. Buyer gọi `POST /api/v1/orders/checkout` với danh sách CartItem IDs, địa chỉ, phương thức TT, voucher code.
2. `OrderController` → `OrderService.checkout()` với `@Transactional`.
3. `OrderService` gọi `ProductInternalService.getVariantForCheckout()` lấy giá + tồn kho + shopId.
4. Nhóm CartItems theo shopId → tạo N `Order` object (1/shop).
5. Nếu có voucher: `VoucherInternalService.validateAndApply()` → proportional split.
6. Trừ `stockQuantity` từng variant.
7. Xóa CartItems.
8. `@Transactional` commit.
9. `AFTER_COMMIT`: phát `OrderCreatedEvent` → Vendor notification.
10. Nếu VNPay: tạo paymentUrl có HMAC → trả về Buyer.

#### 3.4.2.2. Luồng VNPay IPN

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-IPN]

*Hình 3.6. Sơ đồ tuần tự luồng VNPay IPN*

**Mô tả:**
1. Buyer hoàn tất thanh toán trên VNPay → VNPay redirect về `localhost:5173/payment-result?vnp_*`.
2. Frontend gọi `GET /api/v1/payments/vnpay/ipn?{tất cả params VNPay}`.
3. `PaymentController` → `PaymentIpnService.processVNPayIpn()`.
4. Verify HMAC-SHA512 signature.
5. `Repository.findByPaymentGroupId()` với `@Lock(PESSIMISTIC_WRITE)`.
6. Kiểm tra `payment.status != PENDING` (idempotency check).
7. Update `payment.status = SUCCESS`.
8. `@Transactional` commit.
9. `AFTER_COMMIT`: phát `OrderPaidEvent` → `OrderInternalService.confirmOrders()` → orders `CONFIRMED`.
10. Frontend nhận response → hiển thị SUCCESS.

#### 3.4.2.3. Luồng Chat realtime

[INSERT DIAGRAM: DIAGRAM-SEQUENCE-CHAT]

*Hình 3.7. Sơ đồ tuần tự luồng Chat realtime*

**Mô tả:**
1. Buyer kết nối WebSocket: `SockJS → /ws`.
2. STOMP CONNECT frame chứa JWT token trong header.
3. `JwtChannelInterceptor.preSend()` xác thực JWT → set authentication.
4. Buyer subscribe `/user/queue/messages`.
5. Buyer SEND đến `/app/chat.send` với `{roomId, content}`.
6. `ChatController.sendMessage()` → `ChatService.saveMessage()` (lưu DB).
7. `SimpMessagingTemplate.convertAndSendToUser(buyerEmail, "/queue/messages", message)`.
8. `SimpMessagingTemplate.convertAndSendToUser(vendorEmail, "/queue/messages", message)`.
9. Cả Buyer và Vendor nhận tin nhắn realtime.

### 3.4.3. Sơ đồ hoạt động (Activity Diagram)

#### 3.4.3.1. Luồng đặt hàng và thanh toán

[INSERT DIAGRAM: DIAGRAM-ACTIVITY-ORDER-PAYMENT]

*Hình 3.8. Sơ đồ hoạt động luồng đặt hàng và thanh toán*

#### 3.4.3.2. Luồng đăng ký và duyệt gian hàng

[INSERT DIAGRAM: DIAGRAM-ACTIVITY-SHOP-APPROVAL]

*Hình 3.9. Sơ đồ hoạt động luồng đăng ký và duyệt gian hàng*

### 3.4.4. Sơ đồ trạng thái (State Diagram)

#### 3.4.4.1. State Machine của Order

[INSERT DIAGRAM: DIAGRAM-STATE-ORDER]

*Hình 3.10. Sơ đồ trạng thái đơn hàng*

```
PENDING ──(COD created / VNPay IPN SUCCESS)──→ CONFIRMED
CONFIRMED ──(Vendor: ship)──→ SHIPPED
SHIPPED ──(Vendor: delivered)──→ DELIVERED
CONFIRMED ──(Vendor: cancel)──→ CANCELLED
PENDING ──(Vendor: cancel)──→ CANCELLED
```

**Quy tắc transition (enforce trong service):**
- Chỉ Vendor của shop đó mới có thể thay đổi trạng thái.
- `DELIVERED` và `CANCELLED` là trạng thái cuối — không thể thay đổi.
- `PENDING → CONFIRMED` chỉ do hệ thống (payment IPN), không phải Vendor thủ công.

#### 3.4.4.2. State Machine của Shop

[INSERT DIAGRAM: DIAGRAM-STATE-SHOP]

*Hình 3.11. Sơ đồ trạng thái gian hàng*

```
PENDING ──(Admin: approve)──→ ACTIVE
PENDING ──(Admin: reject)──→ BANNED
ACTIVE ──(Admin: ban)──→ BANNED
BANNED ──(Admin: unban)──→ ACTIVE
```

---

## 3.5. Thiết kế cơ sở dữ liệu

Hệ thống Enterprise Ecommerce sử dụng MySQL làm hệ quản trị cơ sở dữ liệu quan hệ, với schema được quản lý tự động bởi Hibernate (`ddl-auto: update`).

### 3.5.1. Tổng quan 22 bảng dữ liệu

**Bảng 3.6.** Danh sách toàn bộ bảng trong cơ sở dữ liệu

| STT | Bảng | Module | Mô tả chức năng |
|---|---|---|---|
| 1 | `users` | identity | Lưu thông tin tài khoản người dùng |
| 2 | `roles` | identity | Lưu các vai trò trong hệ thống |
| 3 | `user_roles` | identity | Bảng trung gian User-Role (Many-to-Many) |
| 4 | `shops` | shop | Lưu thông tin gian hàng Vendor |
| 5 | `brands` | product | Lưu thương hiệu sản phẩm |
| 6 | `categories` | product | Lưu danh mục sản phẩm (cây đệ quy BFS) |
| 7 | `products` | product | Lưu sản phẩm của các gian hàng |
| 8 | `product_variants` | product | Lưu biến thể sản phẩm (màu, size, tồn kho, SKU) |
| 9 | `product_images` | product | Lưu ảnh sản phẩm trên Cloudinary |
| 10 | `carts` | cart | Lưu giỏ hàng của Buyer |
| 11 | `cart_items` | cart | Lưu từng sản phẩm trong giỏ hàng |
| 12 | `orders` | order | Lưu đơn hàng sau khi checkout đa shop |
| 13 | `order_items` | order | Lưu chi tiết từng mặt hàng trong đơn (với snapshot giá) |
| 14 | `order_admin_status_history` | order | Lưu vết lịch sử thay đổi trạng thái đơn hàng do Admin can thiệp |
| 15 | `payments` | payment | Lưu giao dịch thanh toán của payment group |
| 16 | `refund_attempts` | payment | Lưu nhật ký các lần yêu cầu hoàn tiền VNPay của đơn hàng |
| 17 | `vouchers` | voucher | Lưu mã giảm giá (quản lý số lượt dùng qua cột `used_count`) |
| 18 | `reviews` | review | Lưu đánh giá sản phẩm của Buyer |
| 19 | `review_images` | review | Lưu URL ảnh đính kèm đánh giá |
| 20 | `notifications` | notification | Lưu thông báo hệ thống |
| 21 | `chat_rooms` | chat | Lưu phòng chat Buyer-Shop |
| 22 | `chat_messages` | chat | Lưu tin nhắn thời gian thực trong phòng chat |

### 3.5.2. Chi tiết các bảng quan trọng

#### Bảng `users`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `email` | VARCHAR(150) | NOT NULL, UNIQUE | Email đăng nhập |
| `password_hash` | VARCHAR(255) | NOT NULL | Mật khẩu BCrypt |
| `full_name` | VARCHAR(100) | NOT NULL | Họ tên |
| `phone` | VARCHAR(20) | NULL | Số điện thoại |
| `status` | VARCHAR(20) | NOT NULL | Trạng thái tài khoản |

#### Bảng `shops`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL, UNIQUE | FK → users.id; 1 user chỉ có 1 shop |
| `name` | VARCHAR(150) | NOT NULL | Tên gian hàng |
| `description` | TEXT | NULL | Mô tả gian hàng |
| `status` | VARCHAR(20) | NOT NULL | PENDING / ACTIVE / BANNED |
| `created_at` | TIMESTAMP | NOT NULL | Thời điểm đăng ký |

#### Bảng `products`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `shop_id` | BIGINT | NOT NULL | FK → shops.id |
| `category_id` | BIGINT | NULL | FK → categories.id |
| `brand_id` | BIGINT | NULL | FK → brands.id |
| `name` | VARCHAR(255) | NOT NULL | Tên sản phẩm |
| `description` | TEXT | NULL | Mô tả chi tiết |
| `status` | VARCHAR(20) | NOT NULL | ACTIVE / HIDDEN |

#### Bảng `product_variants`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `product_id` | BIGINT | NOT NULL | FK → products.id |
| `sku` | VARCHAR(100) | NOT NULL, UNIQUE | Mã biến thể (do Vendor nhập) |
| `price` | DECIMAL(15,2) | NOT NULL | Giá biến thể |
| `stock_quantity` | INT | NOT NULL | Tồn kho; giảm khi checkout |
| `attributes` | TEXT | NULL | JSON: `{"color":"Đỏ","size":"M"}` |

#### Bảng `orders`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `user_id` | BIGINT | NOT NULL | FK → users.id (Buyer) |
| `shop_id` | BIGINT | NOT NULL | FK → shops.id |
| `payment_group_id` | VARCHAR(36) | NULL | UUID gom nhóm đơn hàng cùng phiên thanh toán |
| `status` | VARCHAR(20) | NOT NULL | PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED |
| `payment_method` | VARCHAR(20) | NOT NULL | VNPAY / COD |
| `total_amount` | DECIMAL(15,2) | NOT NULL | Tổng tiền sau giảm giá |
| `shipping_address` | TEXT | NOT NULL | Địa chỉ giao hàng (text free-form) |
| `created_at` | TIMESTAMP | NOT NULL | Thời điểm đặt hàng |

#### Bảng `order_items`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `order_id` | BIGINT | NOT NULL | FK → orders.id |
| `product_variant_id` | BIGINT | NULL | FK → product_variants.id |
| `product_name` | VARCHAR(255) | NOT NULL | **Snapshot** tên sản phẩm |
| `unit_price` | DECIMAL(15,2) | NOT NULL | **Snapshot** giá tại thời điểm đặt |
| `quantity` | INT | NOT NULL | Số lượng |

#### Bảng `payments`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `payment_group_id` | VARCHAR(36) | NOT NULL, UNIQUE | UUID gom nhóm (FK logic tới orders) |
| `amount` | DECIMAL(15,2) | NOT NULL | Tổng tiền thanh toán |
| `method` | VARCHAR(20) | NOT NULL | VNPAY / COD |
| `status` | VARCHAR(20) | NOT NULL | PENDING / SUCCESS / FAILED |
| `vnpay_txn_ref` | VARCHAR(100) | NULL | Mã giao dịch VNPay |
| `created_at` | TIMESTAMP | NOT NULL | Thời điểm tạo |

#### Bảng `vouchers`

| Cột | Kiểu | Null | Mô tả |
|---|---|---|---|
| `id` | BIGINT | NOT NULL, AUTO_INCREMENT | Khóa chính |
| `code` | VARCHAR(50) | NOT NULL, UNIQUE | Mã voucher (case-insensitive lookup) |
| `scope` | VARCHAR(10) | NOT NULL | SYSTEM / SHOP |
| `shop_id` | BIGINT | NULL | FK → shops.id (NULL nếu SYSTEM) |
| `type` | VARCHAR(20) | NOT NULL | PERCENTAGE / FIXED_AMOUNT |
| `discount_value` | DECIMAL(15,2) | NOT NULL | Giá trị giảm |
| `max_discount` | DECIMAL(15,2) | NULL | Tối đa giảm (cho PERCENTAGE) |
| `min_order_value` | DECIMAL(15,2) | NULL | Đơn tối thiểu để áp dụng |
| `start_date` | TIMESTAMP | NOT NULL | Bắt đầu hiệu lực |
| `end_date` | TIMESTAMP | NOT NULL | Kết thúc hiệu lực |
| `usage_limit` | INT | NULL | Giới hạn lần dùng; NULL = không giới hạn |
| `used_count` | INT | NOT NULL, DEFAULT 0 | Số lần đã sử dụng |

### 3.5.3. ERD (Entity Relationship Diagram)

[INSERT DIAGRAM: DIAGRAM-ERD]

*Hình 3.12. Sơ đồ ERD toàn bộ cơ sở dữ liệu (20 bảng)*

### 3.5.4. Các mối quan hệ quan trọng

**Bảng 3.7.** Các mối quan hệ chính trong cơ sở dữ liệu

| Quan hệ | Cardinality | Ghi chú |
|---|---|---|
| User ↔ Role | Many-to-Many | Qua `user_roles` |
| User → Shop | One-to-One | `user_id UNIQUE` trong `shops` |
| Shop → Product | One-to-Many | `shop_id` trong `products` |
| Category → Category | Self-referential | `parent_id` nullable |
| Product → ProductVariant | One-to-Many | `orphanRemoval=true` |
| Product → ProductImage | One-to-Many | `orphanRemoval=true` |
| Cart → CartItem | One-to-Many | 1 Cart per User |
| CartItem → ProductVariant | Many-to-One | — |
| Order → OrderItem | One-to-Many | — |
| N Orders ↔ Payment | Many-to-One (logic) | Via `payment_group_id` UUID |
| User → Review | One-to-Many | — |
| Product → Review | One-to-Many | — |
| Order → Review | One-to-Many | Gating: chỉ từ DELIVERED order |
| Chat Room ↔ ChatMessage | One-to-Many | — |

---

## 3.6. Thiết kế giao diện hệ thống

### 3.6.1. Thiết kế giao diện — Customer (Người mua)

#### 3.6.1.1. Cấu trúc giao diện Customer

Frontend Customer sử dụng `PublicLayout` với header cố định (logo, tìm kiếm, icon giỏ hàng, icon chuông thông báo, avatar người dùng). Routing được bảo vệ bởi `ProtectedRoute` component.

**Bảng 3.8.** Danh sách màn hình Customer

| STT | Màn hình | Route / Component | Trạng thái |
|---|---|---|---|
| 1 | Đăng nhập | `/login` | IMPLEMENTED |
| 2 | Đăng ký | `/register` | IMPLEMENTED |
| 3 | Danh sách sản phẩm & Lọc BFS | `/products` | IMPLEMENTED |
| 4 | Chi tiết sản phẩm & Biến thể | `/products/:id` | IMPLEMENTED |
| 5 | Giỏ hàng đa shop | `/cart` | IMPLEMENTED |
| 6 | Checkout đa shop & Áp voucher | `/checkout` | IMPLEMENTED |
| 7 | Kết quả thanh toán | `/payment-result` | IMPLEMENTED |
| 8 | Lịch sử đơn hàng | `/orders` | IMPLEMENTED |
| 9 | Chi tiết đơn & Xác nhận nhận hàng | `/orders/:id` | IMPLEMENTED |
| 10 | Hồ sơ cá nhân | `/profile` | IMPLEMENTED |
| 11 | Khung chat nổi đa trang | `FloatingChatWidget` (Toàn cục) | IMPLEMENTED |

#### 3.6.1.2. Màn hình Danh sách sản phẩm

[INSERT FIGURE: IMG-PRODUCT-LIST]

*Hình 3.13. Trang danh sách sản phẩm với bộ lọc danh mục*

**Thành phần:** Thanh tìm kiếm (keyword), bộ lọc danh mục (tab), grid sản phẩm (ảnh thumbnail, tên, giá, rating), phân trang.

**API:** `GET /api/v1/products?keyword=&categoryId=&page=&size=`

#### 3.6.1.3. Màn hình Chi tiết sản phẩm

[INSERT FIGURE: IMG-PRODUCT-DETAIL]

*Hình 3.14. Trang chi tiết sản phẩm với gallery ảnh và lựa chọn biến thể*

**Thành phần:** Gallery ảnh (thumbnail + main image), thông tin sản phẩm (tên, giá, mô tả, danh mục), bộ chọn biến thể (cập nhật giá/tồn kho khi chọn), input số lượng, nút "Thêm vào giỏ hàng", phần đánh giá (rating histogram + danh sách reviews).

**API:** `GET /api/v1/products/:id`, `POST /api/v1/carts/items`

#### 3.6.1.4. Màn hình Giỏ hàng

[INSERT FIGURE: IMG-CART]

*Hình 3.15. Giỏ hàng với sản phẩm từ nhiều gian hàng*

**Thành phần:** Danh sách CartItems (ảnh, tên, biến thể, số lượng điều chỉnh, xóa), tổng tiền, nút "Tiến hành đặt hàng".

**Đặc điểm:** Hiển thị phân nhóm theo gian hàng (chứng minh multi-shop cart).

#### 3.6.1.5. Màn hình Checkout

[INSERT FIGURE: IMG-CHECKOUT]

*Hình 3.16. Trang xác nhận đặt hàng với mã giảm giá*

**Thành phần:** Form địa chỉ giao hàng, ô nhập voucher (Áp dụng → hiển thị số tiền giảm), danh sách sản phẩm + giá, tổng thanh toán, radio chọn phương thức (VNPay / COD), nút "Đặt hàng".

**API:** `POST /api/v1/orders/preview` (validate voucher), `POST /api/v1/orders/checkout`

#### 3.6.1.6. Màn hình Kết quả thanh toán

[INSERT FIGURE: IMG-PAYMENT-RESULT]

*Hình 3.17. Màn hình xác nhận thanh toán thành công*

**Thành phần:** Spinner "Đang xác nhận..." → icon tích xanh / icon X đỏ + thông điệp kết quả + nút "Xem Đơn Hàng".

**Luồng:** Frontend polling `GET /api/v1/payments/group/:id/status` + gọi IPN sync.

#### 3.6.1.7. Màn hình Hồ sơ cá nhân (Profile)

**Thành phần:** Hiển thị thông tin định danh (Email, Họ và tên, Số điện thoại, Vai trò tài khoản); Form cập nhật cho phép người dùng chỉnh sửa thông tin họ tên và số điện thoại; Nút "Lưu thay đổi" gọi API đồng bộ dữ liệu.

**API:** `GET /api/v1/users/me`, `PUT /api/v1/users/me`

#### 3.6.1.8. Khung chat nổi toàn diện (FloatingChatWidget)

**Thành phần:** Biểu tượng nút tròn nổi cố định ở góc dưới cùng bên phải màn hình trên mọi trang (`PublicLayout.tsx`); Badge đếm số lượng tin nhắn chưa đọc; Khi bấm mở ra khung chat hoàn chỉnh gồm danh sách phòng chat với các shop, khung hiển thị lịch sử tin nhắn cuộn tự động, input nhập nội dung và nút gửi; Tự động kết nối WebSocket STOMP kèm JWT token ngay khi người dùng đăng nhập.

**Giao thức/API:** WebSocket STOMP (`/ws`, `/app/chat.send`, `/topic/chat/{roomId}`)

---

### 3.6.2. Thiết kế giao diện — Vendor (Người bán)

#### 3.6.2.1. Cổng vào giao diện Vendor — RequireShop

`RequireShop` là Higher-Order Component bảo vệ toàn bộ route `/vendor/*`. Component này kiểm tra trạng thái shop và điều hướng phù hợp:

| Tình huống | Màn hình hiển thị |
|---|---|
| User chưa có shop | `VendorShopRegistrationPage` — form đăng ký |
| Shop đang PENDING | `ShopPendingPage` — chờ Admin duyệt |
| Shop bị BANNED | Màn hình thông báo bị khóa |
| Shop ACTIVE | Nội dung trang Vendor thực sự |

#### 3.6.2.2. VendorNav — Thanh điều hướng Vendor

[INSERT FIGURE: IMG-VENDOR-NAV]

*Hình 3.18. VendorNav với thông tin gian hàng và tab điều hướng*

**Thành phần:** Logo + Tên shop (từ `GET /api/v1/shops/my-shop`) + Badge trạng thái + 3 tabs: Quản Lý Sản Phẩm / Quản Lý Đơn Hàng / Quản Lý Voucher.

**Bảng 3.9.** Danh sách màn hình Vendor

| Màn hình | Route | Trạng thái |
|---|---|---|
| Đăng ký gian hàng | Trigger bởi RequireShop | IMPLEMENTED |
| Chờ duyệt | Trigger bởi RequireShop | IMPLEMENTED |
| Danh sách sản phẩm | `/vendor/products` | IMPLEMENTED |
| Tạo sản phẩm | `/vendor/products/new` | IMPLEMENTED |
| Chỉnh sửa sản phẩm | `/vendor/products/:id/edit` | IMPLEMENTED |
| Quản lý đơn hàng | `/vendor/orders` | IMPLEMENTED |
| Quản lý voucher | `/vendor/vouchers` | IMPLEMENTED |
| Phản hồi review | `/products/:id#reviews` | IMPLEMENTED |
| Phân tích doanh thu | `/vendor/analytics` | IMPLEMENTED |

#### 3.6.2.3. Màn hình Tạo/Chỉnh sửa sản phẩm

[INSERT FIGURE: IMG-VENDOR-PRODUCT-FORM]

*Hình 3.19. Form tạo sản phẩm với upload ảnh Cloudinary và biến thể*

**Thành phần:** Thông tin cơ bản (Tên, Mô tả, Giá, Danh mục); Upload ảnh Cloudinary (preview grid, chọn ảnh bìa `isThumbnail`); Bảng biến thể (SKU, Giá, Tồn kho, JSON Attributes).

**Luồng upload ảnh:** `POST /api/v1/media/upload` → Cloudinary → nhận URL + publicId → lưu cùng sản phẩm.

#### 3.6.2.4. Màn hình Quản lý đơn hàng Vendor

[INSERT FIGURE: IMG-VENDOR-ORDERS]

*Hình 3.20. Vendor quản lý đơn hàng với filter trạng thái*

**Thành phần:** 5 tab filter (Tất cả / Chờ / Đã xác nhận / Đang giao / Hoàn thành); Mỗi đơn hiển thị sản phẩm (snapshot), tổng tiền, địa chỉ; Nút hành động theo trạng thái; Modal nhập lý do khi hủy.

#### 3.6.2.5. Màn hình Phân tích doanh thu Vendor

**Thành phần:** 3 thẻ chỉ số KPI kinh doanh nổi bật (Tổng doanh thu 30 ngày, Tổng số đơn hàng hoàn thành, Giá trị trung bình đơn); Biểu đồ cột trực quan hóa doanh thu theo ngày; Bảng chi tiết các đơn hàng đóng góp doanh thu gần nhất.

**API:** `GET /api/v1/vendor/analytics/revenue`

---

### 3.6.3. Thiết kế giao diện — Admin (Quản trị viên)

Admin sử dụng layout riêng biệt (`AdminLayout`) với sidebar cố định bên trái — hoàn toàn tách khỏi layout Customer/Vendor.

**Bảng 3.10.** Danh sách màn hình Admin

| Màn hình | Route | Trạng thái |
|---|---|---|
| Dashboard toàn sàn | `/admin` | IMPLEMENTED |
| Quản lý gian hàng & Khóa shop | `/admin/shop` | IMPLEMENTED |
| Danh sách sản phẩm | `/admin/products` | IMPLEMENTED |
| Tạo/Sửa sản phẩm | `/admin/products/create`, `/:id/edit` | IMPLEMENTED |
| Danh sách đơn hàng | `/admin/orders` | IMPLEMENTED |
| Chi tiết đơn hàng & Hoàn tiền | `/admin/orders/:id` | IMPLEMENTED |
| Quản lý voucher hệ thống | `/admin/vouchers` | IMPLEMENTED |

#### 3.6.3.1. Màn hình Duyệt Gian hàng

[INSERT FIGURE: IMG-ADMIN-SHOP]

*Hình 3.21. Admin phê duyệt gian hàng — filter PENDING*

**Nút hành động theo trạng thái:**
- PENDING: "Duyệt" (xanh) + "Từ chối" (đỏ)
- ACTIVE: "Khoá" (đỏ)
- BANNED: "Mở khóa" (xanh)

**Sau khi duyệt:** `ShopApprovedEvent` → ROLE_VENDOR được cấp + email gửi qua Gmail SMTP.

**Thao tác kiểm soát gian hàng vi phạm:**
- Trên trang `/admin/shop`, đối với gian hàng đang hoạt động (`ACTIVE`), Admin có nút **"Khóa Shop"** (`BANNED`). Khi bấm, hệ thống chuyển trạng thái shop và tức thì ngăn chặn việc đăng sản phẩm mới hoặc bán hàng.
- Đối với shop bị khóa (`BANNED`), Admin có nút **"Mở Khóa"** (`ACTIVE`) để phục hồi quyền kinh doanh cho người bán sau khi giải quyết vi phạm.

#### 3.6.3.2. Màn hình Admin Dashboard

**Thành phần:** 4 thẻ KPI toàn diện của toàn sàn (Tổng doanh thu hệ thống, Tổng số đơn hàng, Tổng số người dùng đăng ký, Tổng số gian hàng đang hoạt động); Biểu đồ trực quan hóa doanh thu theo mốc thời gian; Bảng theo dõi 10 đơn hàng mới nhất trên toàn hệ thống kèm trạng thái thanh toán và gian hàng xử lý.

**API:** `GET /api/v1/admin/statistics/overview`

---

## 3.7. Kết quả triển khai hệ thống

### 3.7.1. Môi trường triển khai thực tế

Hệ thống hiện tại được triển khai trong **môi trường phát triển cục bộ (localhost)**. Chưa có bằng chứng về triển khai trên server sản xuất (production) hay cloud hosting.

```
[Máy tính lập trình viên — localhost]
├── Backend:  Spring Boot 4.1.0 → localhost:8080
├── Frontend: Vite + React 19   → localhost:5173
├── Database: MySQL              → localhost:3306/ecommerce
│
├── [External — Cloud]
│   ├── Cloudinary CDN   (ảnh sản phẩm — production-ready)
│   ├── VNPay Sandbox    (thanh toán — môi trường kiểm thử)
│   └── Gmail SMTP       (email thông báo)
```

**Lưu ý quan trọng:**
- Không có Docker / Container hóa.
- Không có CI/CD pipeline.
- VNPay chỉ ở môi trường **Sandbox** — không thực hiện giao dịch thật.
- Cloudinary hoạt động ở môi trường thực (production-ready cloud CDN).

### 3.7.2. Chức năng đã hoàn thành

**Bảng 3.11.** Phân loại trạng thái chức năng theo actor

| Actor | Feature | Status |
|---|---|---|
| Customer | Đăng ký / Đăng nhập / Đăng xuất (JWT 24h) | IMPLEMENTED |
| Customer | Xem danh sách + tìm kiếm BFS + lọc khoảng giá | IMPLEMENTED |
| Customer | Chi tiết sản phẩm + chọn biến thể SKU | IMPLEMENTED |
| Customer | Thêm / Cập nhật / Xóa khỏi giỏ hàng đa shop | IMPLEMENTED |
| Customer | Checkout đa gian hàng (tách N đơn) + áp voucher tỷ lệ | IMPLEMENTED |
| Customer | Thanh toán VNPay Sandbox & COD an toàn | IMPLEMENTED |
| Customer | Xem lịch sử đơn hàng + Chi tiết đơn hàng | IMPLEMENTED |
| Customer | Khách chủ động xác nhận nhận hàng (`DELIVERED`) | IMPLEMENTED |
| Customer | Viết / Sửa / Xóa đánh giá kèm ảnh thực tế | IMPLEMENTED |
| Customer | Nhận thông báo đẩy thời gian thực (STOMP Push) | IMPLEMENTED |
| Customer | Nhắn tin thời gian thực qua Khung chat nổi (`FloatingChatWidget`) | IMPLEMENTED |
| Customer | Xem và chỉnh sửa hồ sơ cá nhân (`/profile`) | IMPLEMENTED |
| Customer | Gợi ý sản phẩm lai (Hybrid Recommendation) | IMPLEMENTED |
| Vendor | Đăng ký gian hàng + Cổng kiểm soát RequireShop | IMPLEMENTED |
| Vendor | Tạo / Sửa sản phẩm + upload ảnh Cloudinary + biến thể | IMPLEMENTED |
| Vendor | Quản lý đơn hàng + cập nhật trạng thái (Confirm → Ship) | IMPLEMENTED |
| Vendor | Hủy đơn hàng với lý do kèm tự động hoàn tồn kho | IMPLEMENTED |
| Vendor | Tạo và quản lý voucher riêng của Shop | IMPLEMENTED |
| Vendor | Phản hồi công khai đánh giá của khách hàng | IMPLEMENTED |
| Vendor | Nhận thông báo đơn hàng mới qua WebSocket STOMP | IMPLEMENTED |
| Vendor | Chat trực tiếp với khách hàng thời gian thực | IMPLEMENTED |
| Vendor | Phân tích doanh thu & chỉ số kinh doanh 30 ngày | IMPLEMENTED |
| Admin | Duyệt / Từ chối / Khóa / Mở khóa gian hàng + gửi mail | IMPLEMENTED |
| Admin | Giám sát danh sách + chi tiết đơn hàng toàn hệ thống | IMPLEMENTED |
| Admin | Quản lý sản phẩm toàn sàn (CRUD) | IMPLEMENTED |
| Admin | Tạo voucher toàn sàn (`SYSTEM`) phân bổ tỷ lệ chiết khấu | IMPLEMENTED |
| Admin | Dashboard thống kê doanh thu KPI toàn sàn | IMPLEMENTED |
| System | Xử lý VNPay IPN với khóa bi quan chống duplicate | IMPLEMENTED |
| System | Cơ chế sự kiện `@TransactionalEventListener AFTER_COMMIT` | IMPLEMENTED |
| System | Snapshot giá và thuộc tính khi đặt hàng | IMPLEMENTED |
| System | Thuật toán phân bổ chiết khấu voucher tỷ lệ (Proportional Split) | IMPLEMENTED |
| System | Tự động hoàn trả tồn kho biến thể khi hủy đơn hàng | IMPLEMENTED |

### 3.7.3. Hình ảnh thực tế hệ thống

[INSERT FIGURE: IMG-PRODUCT-LIST]  
*Hình 3.22. Trang danh sách sản phẩm*

[INSERT FIGURE: IMG-CHECKOUT]  
*Hình 3.23. Trang checkout với mã giảm giá*

[INSERT FIGURE: IMG-VNPAY]  
*Hình 3.24. Cổng thanh toán VNPay Sandbox*

[INSERT FIGURE: IMG-PAYMENT-SUCCESS]  
*Hình 3.25. Thanh toán thành công*

[INSERT FIGURE: IMG-VENDOR-PRODUCT-FORM]  
*Hình 3.26. Form tạo sản phẩm (Vendor)*

[INSERT FIGURE: IMG-VENDOR-ORDERS]  
*Hình 3.27. Quản lý đơn hàng (Vendor)*

[INSERT FIGURE: IMG-ADMIN-SHOP]  
*Hình 3.28. Duyệt gian hàng (Admin)*

[INSERT FIGURE: IMG-CHAT]  
*Hình 3.29. Chat realtime WebSocket STOMP*

### 3.7.4. Hạn chế còn tồn tại

#### 3.7.4.1. Giao diện người dùng (Frontend)

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Chưa có giao diện quản lý danh mục (Category) riêng cho Admin | Trung bình | Hiện cây danh mục được khởi tạo hoặc quản lý qua API/CSDL |
| Chưa có giao diện quản trị người dùng (User Management) chuyên sâu | Trung bình | Hiện Admin quản lý tài khoản người bán thông qua duyệt/khóa Shop |
| Chưa có tính năng đổi mật khẩu / quên mật khẩu trên giao diện | Trung bình | Người dùng chưa thể tự reset password trực tiếp trên UI |

#### 3.7.4.2. Xử lý nghiệp vụ Backend

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Hoàn tiền VNPay tự động E2E bị hạn chế Sandbox IP whitelist | Cao | Backend đã có Service và bảng `refund_attempts`, nhưng API VNPay QueryDR/Refund yêu cầu IP tĩnh public |
| Schema cơ sở dữ liệu quản lý bằng `ddl-auto: update` | Trung bình | Phù hợp môi trường phát triển local, nhưng cần chuyển sang Flyway/Liquibase khi lên production |
| Chưa tích hợp Search Engine ngoài chuyên biệt (Elasticsearch) | Thấp | Hiện hệ thống tối ưu bằng cây đệ quy BFS + LIKE keyword và chỉ mục CSDL |

#### 3.7.4.3. Bảo mật

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| JWT chưa có cơ chế thu hồi token (Token Revocation/Blacklist) | Cao | Token có hiệu lực 24 giờ sau khi cấp phát |
| Chưa cấu hình Rate Limiting chống tấn công DoS/Brute Force | Cao | Cần tích hợp Bucket4j hoặc API Gateway trong tương lai |
| Chưa kích hoạt giao thức HTTPS trong cấu hình local mặc định | Trung bình | Đang chạy trên giao thức HTTP tại cổng 8080/5173 |

#### 3.7.4.4. Hạ tầng triển khai (Deployment)

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Chưa đóng gói container Docker và Docker Compose | Cao | Hiện tại ứng dụng vận hành native trên máy chủ vật lý local |
| Chưa thiết lập CI/CD pipeline tự động hóa kiểm thử và đóng gói | Cao | Kiểm thử và build hiện đang thực hiện thủ công qua terminal |
| WebSocket sử dụng in-memory STOMP broker | Cao | Không thể scale ngang nhiều backend instance nếu không có Redis Pub/Sub |
| Cổng thanh toán VNPay chỉ dừng ở môi trường Sandbox thử nghiệm | Cao | Chưa tiến hành ký hợp đồng và đấu nối môi trường production thật |

---

*Kết thúc Chương 3.*

---

# CHƯƠNG 4. KẾT LUẬN VÀ HƯỚNG PHÁT TRIỂN

---

## 4.1. Kết quả đạt được

### 4.1.1. Về chức năng

Sau quá trình phát triển qua 9 Phase hoàn thành toàn diện, hệ thống Enterprise Ecommerce đã triển khai đầy đủ chu trình nghiệp vụ khép kín của một sàn TMĐT đa người bán hiện đại:

- **Luồng hoàn chỉnh từ đầu đến cuối:** Khách hàng có thể đăng ký, tìm kiếm sản phẩm phân cấp cây BFS, lọc khoảng giá, gom sản phẩm từ nhiều shop vào giỏ hàng, checkout đa gian hàng (tự động tách N đơn), thanh toán trực tuyến qua VNPay hoặc COD, nhận thông báo đẩy tức thì, trao đổi trực tiếp qua khung chat nổi `FloatingChatWidget`, theo dõi trạng thái đơn hàng, chủ động bấm xác nhận nhận hàng và đánh giá chất lượng sản phẩm.

- **Luồng Vendor hoàn chỉnh:** Người bán đăng ký gian hàng, chờ Admin duyệt, đăng bán sản phẩm kèm biến thể (SKU, tồn kho, giá) và upload ảnh Cloudinary, quản lý đơn hàng theo máy trạng thái, hủy đơn kèm tự động hoàn kho, tạo mã giảm giá riêng của shop, phản hồi đánh giá và theo dõi báo cáo phân tích doanh thu 30 ngày trực quan.

- **Luồng Admin hoàn chỉnh:** Admin duyệt hoặc từ chối hồ sơ shop, khóa hoặc mở khóa gian hàng vi phạm, giám sát tất cả đơn hàng trên toàn hệ thống, tạo mã giảm giá toàn sàn phân bổ tỷ lệ chiết khấu, và theo dõi các chỉ số KPI doanh thu toàn diện tại Admin Dashboard (`/admin`).

- **Giao tiếp & Thông báo Realtime:** Hệ thống thông báo đẩy (Push Notification) và khung chat nổi (`FloatingChatWidget`) hoạt động thời gian thực hai chiều giữa Buyer và Vendor qua WebSocket STOMP với xác thực JWT trên kết nối.

- **Gợi ý sản phẩm thông minh:** Động cơ gợi ý sản phẩm lai (Hybrid Recommendation) kết hợp giữa độ tương quan danh mục/giá và điểm số đánh giá cộng đồng.

### 4.1.2. Về kỹ thuật

- **Kiến trúc Modular Monolith** được triển khai và kiểm soát nghiêm ngặt: 10 module nghiệp vụ giao tiếp qua Internal Service contracts, không vi phạm ranh giới.

- **Transaction Management:** Ranh giới giao dịch được thiết kế cẩn thận; `@Transactional` checkout bảo đảm atomicity; `AFTER_COMMIT` tách biệt side effects khỏi giao dịch chính.

- **Payment Integration:** VNPay được tích hợp với HMAC-SHA512 signature verification, Pessimistic Locking cho idempotency, và cơ chế payment retry.

- **Kiểm thử:** Integration tests quan trọng đạt kết quả: `TransactionAuditIntegrationTest` 26/26 pass, `VendorOrderManagementTest` 4/4 pass, `NPlus1AuditTest` kiểm soát query count.

- **Tối ưu hóa:** N+1 Query được phát hiện và giải quyết qua `@BatchSize` và bulk queries; kiểm thử bằng Hibernate Statistics.

### 4.1.3. So sánh mục tiêu và kết quả thực tế

| Mục tiêu | Kết quả |
|---|---|
| Luồng mua hàng hoàn chỉnh | ✅ Đạt (Đầy đủ từ tìm kiếm BFS, giỏ hàng, checkout đa shop, xác nhận nhận hàng và review) |
| Luồng Vendor hoàn chỉnh | ✅ Đạt (Bao gồm quản lý sản phẩm, đơn hàng, voucher shop và phân tích doanh thu 30 ngày) |
| Luồng Admin | ✅ Đạt (Bao gồm duyệt/khóa shop, quản lý đơn toàn sàn, voucher toàn sàn và Admin Dashboard) |
| Kiến trúc Modular Monolith | ✅ Đạt (10 module độc lập, kiểm soát ranh giới qua Internal Service và Event) |
| Tích hợp VNPay an toàn | ✅ Đạt (Môi trường Sandbox với chữ ký HMAC-SHA512 và khóa bi quan chống duplicate IPN) |
| Chat realtime WebSocket | ✅ Đạt (Tích hợp Khung chat nổi FloatingChatWidget toàn cục với STOMP Messaging) |
| Thông báo event-driven | ✅ Đạt (Kết hợp STOMP Push tức thì và lưu trữ CSDL sau commit giao dịch) |
| Tối ưu hóa N+1 Query | ✅ Đạt (Kiểm thử tự động bằng Hibernate Statistics và giải quyết bằng BatchSize) |
| Docker / Container hóa | ❌ Chưa thực hiện (Dự kiến chuyển sang Phase 10 - Lộ trình tương lai) |
| Redis cache | ❌ Chưa thực hiện (Dự kiến chuyển sang Phase 10 - Lộ trình tương lai) |

---

## 4.2. Đánh giá hạn chế

### 4.2.1. Hạn chế kỹ thuật

**Deployment & Infrastructure:** Hệ thống chưa được container hóa (Docker) và chưa có CI/CD pipeline. Việc chỉ chạy trên localhost giới hạn khả năng kiểm thử trong điều kiện thực tế (VNPay IPN không tự động hoạt động trên localhost).

**Bảo mật:** JWT không có cơ chế thu hồi token (revocation), không có Rate Limiting, và cấu hình mặc định thiếu HTTPS. Đây là những hạn chế quan trọng cần giải quyết trước khi triển khai production.

**Scalability:** WebSocket sử dụng in-memory STOMP broker — không thể scale horizontal nếu chạy nhiều instance. Không có Redis để chia sẻ session hay cache giữa các instance.

### 4.2.2. Hạn chế nghiệp vụ

**Tính năng còn thiếu:** Quên mật khẩu/Reset password, Customer tự hủy đơn, Address Book, tính phí vận chuyển theo khu vực.

**Testing:** Chỉ có Integration Tests, thiếu Unit Tests cho Service layer và hoàn toàn thiếu Frontend tests.

---

## 4.3. Hướng phát triển

### 4.3.1. Ngắn hạn (ưu tiên cao)

**1. Tính năng tài khoản người dùng mở rộng**
- Triển khai quy trình quên mật khẩu và Reset password tự động qua email Gmail SMTP.
- Cho phép khách hàng tự gửi yêu cầu hủy đơn hàng khi đơn còn ở trạng thái `PENDING`.

**2. Quản trị hệ thống chuyên sâu**
- Xây dựng giao diện quản lý người dùng (User Management) trực quan cho Quản trị viên.
- Xây dựng giao diện quản trị cây danh mục sản phẩm (Category Tree Management) cho Admin.

**3. Tăng cường bảo mật ứng dụng**
- Tích hợp Rate Limiting (sử dụng Bucket4j) trên các endpoint nhạy cảm như đăng nhập, checkout, tìm kiếm.
- Triển khai cơ chế JWT Refresh Token kết hợp xoay vòng (Token Rotation) nhằm tăng cường bảo mật phiên làm việc.

### 4.3.2. Trung hạn

**4. Hạ tầng Caching và Scalability (Redis)**
- Triển khai Redis Cache để lưu tạm thông tin danh mục, sản phẩm nổi bật và dữ liệu session.
- Sử dụng Redis Pub/Sub làm Message Broker ngoài cho WebSocket STOMP, cho phép hệ thống scale ngang nhiều instance backend.

**5. Container hóa và Tự động hóa CI/CD**
- Đóng gói Dockerfile chuẩn cho Spring Boot Backend và multi-stage Dockerfile cho React Frontend (Nginx).
- Xây dựng `docker-compose.yml` điều phối toàn bộ hệ thống gồm MySQL Server, Redis Cache, Backend Service và Frontend.
- Thiết lập GitHub Actions tự động chạy Integration Test và đóng gói Docker image khi có commit mới.

**6. Công cụ tìm kiếm chuyên dụng (Elasticsearch)**
- Tích hợp cụm Elasticsearch để lập chỉ mục toàn văn (Full-Text Search) cho sản phẩm, hỗ trợ tìm kiếm mờ (Fuzzy Search), gợi ý từ khóa (Autocomplete) và tự sửa lỗi chính tả.

### 4.3.3. Dài hạn

**7. Đấu nối cổng thanh toán môi trường Production**
- Đăng ký hợp đồng thương mại với VNPay để chuyển đổi từ Sandbox sang môi trường thanh toán thật.
- Mở rộng thêm các ví điện tử phổ biến tại Việt Nam: MoMo, ZaloPay, ShopeePay.

**8. Phát triển ứng dụng di động (Mobile App)**
- Phát triển ứng dụng di động cho cả iOS và Android bằng React Native hoặc Flutter, tái sử dụng toàn bộ hệ thống REST API và WebSocket backend sẵn có.
- Tích hợp Firebase Cloud Messaging (FCM) để gửi thông báo đẩy trực tiếp lên thiết bị di động.

---

## 4.4. Kết luận

Đề tài "Xây dựng hệ thống thương mại điện tử đa người bán" đã hoàn thành việc thiết kế và triển khai một nền tảng TMĐT đa người bán hoạt động được, với các luồng nghiệp vụ cốt lõi được triển khai đầy đủ và kiểm thử.

Điểm nổi bật về mặt kỹ thuật của hệ thống là việc áp dụng kiến trúc Modular Monolith với ranh giới module được kiểm soát nghiêm ngặt, thiết kế transaction boundary cẩn thận với Event-Driven pattern, tích hợp cổng thanh toán VNPay với cơ chế idempotency, và giao tiếp thời gian thực qua WebSocket STOMP.

Mặc dù còn một số hạn chế về deployment infrastructure, bảo mật và một số tính năng chưa hoàn thiện — tất cả đều được ghi nhận trung thực trong báo cáo — hệ thống đã đạt được mục tiêu học thuật đề ra: hiểu và vận dụng được các nguyên tắc phát triển phần mềm backend hiện đại trong một bối cảnh hệ thống thực tế có độ phức tạp nghiệp vụ cao.

---

*Kết thúc Chương 4.*

---

# TÀI LIỆU THAM KHẢO

[1] Craig Walls, *Spring in Action*, 6th Edition, Manning Publications, 2021.

[2] Martin Fowler, *Patterns of Enterprise Application Architecture*, Addison-Wesley, 2002.

[3] Sam Newman, *Building Microservices: Designing Fine-Grained Systems*, 2nd Edition, O'Reilly Media, 2021.

[4] Spring Framework Documentation — Spring Boot Reference Documentation. [Online] Available: https://docs.spring.io/spring-boot/docs/current/reference/htmlsingle/

[5] Spring Security Reference Documentation. [Online] Available: https://docs.spring.io/spring-security/reference/

[6] React Documentation. [Online] Available: https://react.dev/

[7] VNPay Integration Documentation. [Online] Available: https://sandbox.vnpayment.vn/apis/

[8] Cloudinary Developer Documentation. [Online] Available: https://cloudinary.com/documentation

[9] RFC 7519 — JSON Web Token (JWT). [Online] Available: https://datatracker.ietf.org/doc/html/rfc7519

[10] STOMP Protocol Specification. [Online] Available: https://stomp.github.io/stomp-specification-1.2.html

[11] MySQL 8.0 Reference Manual. [Online] Available: https://dev.mysql.com/doc/refman/8.0/en/

[12] Hibernate ORM Documentation. [Online] Available: https://hibernate.org/orm/documentation/

[13] Erich Gamma et al., *Design Patterns: Elements of Reusable Object-Oriented Software*, Addison-Wesley, 1994.

[14] Shopee Vietnam. [Online] Available: https://shopee.vn/

[15] Tiki Vietnam. [Online] Available: https://tiki.vn/

---

# PHỤ LỤC

---

## APPENDIX A — Feature Matrix (Ma trận tính năng)

**Bảng A.1.** Phân loại đầy đủ tất cả tính năng hệ thống

| Actor | Feature | Status | Bằng chứng |
|---|---|---|---|
| Customer | Đăng ký | IMPLEMENTED | `POST /api/v1/auth/register`, `RegisterPage.tsx` |
| Customer | Đăng nhập | IMPLEMENTED | `POST /api/v1/auth/login`, JWT |
| Customer | Đăng xuất | IMPLEMENTED | `AuthContext.logout()`, clear localStorage |
| Customer | Xem danh sách sản phẩm | IMPLEMENTED | `ProductListPage.tsx` |
| Customer | Tìm kiếm sản phẩm | IMPLEMENTED | Param `keyword` |
| Customer | Lọc theo danh mục | IMPLEMENTED | Param `categoryId` |
| Customer | Chi tiết sản phẩm + biến thể | IMPLEMENTED | `ProductDetailPage.tsx` |
| Customer | Thêm vào giỏ hàng | IMPLEMENTED | `POST /api/v1/carts/items` |
| Customer | Xem / Cập nhật / Xóa giỏ hàng | IMPLEMENTED | `CartPage.tsx` |
| Customer | Checkout đa gian hàng | IMPLEMENTED | `POST /api/v1/orders/checkout` |
| Customer | Áp dụng voucher | IMPLEMENTED | `POST /api/v1/orders/preview` |
| Customer | Thanh toán VNPay Sandbox | IMPLEMENTED | Redirect + IPN sync |
| Customer | Thanh toán COD | IMPLEMENTED | Đơn PENDING ngay |
| Customer | Xem lịch sử đơn hàng | IMPLEMENTED | `OrderHistoryPage.tsx` |
| Customer | Xem chi tiết đơn hàng | IMPLEMENTED | `OrderDetailPage.tsx` |
| Customer | Viết đánh giá | IMPLEMENTED | `POST /api/v1/reviews` |
| Customer | Sửa / Xóa đánh giá | IMPLEMENTED | `PUT/DELETE /api/v1/reviews/:id` |
| Customer | Xem rating histogram | IMPLEMENTED | `ProductReviewSection` |
| Customer | Nhận thông báo | IMPLEMENTED | `NotificationDropdown` |
| Customer | Chat realtime đa trang | IMPLEMENTED | `FloatingChatWidget.tsx`, WebSocket STOMP |
| Customer | Chỉnh sửa hồ sơ cá nhân | IMPLEMENTED | `PUT /api/v1/users/me`, `ProfilePage.tsx` |
| Customer | Xác nhận nhận hàng (`DELIVERED`) | IMPLEMENTED | `POST /api/v1/orders/:id/confirm-delivery`, `OrderDetailPage.tsx` |
| Customer | Tìm kiếm nâng cao BFS & Lọc giá | IMPLEMENTED | `ProductListPage.tsx`, `GET /api/v1/products` |
| Customer | Gợi ý sản phẩm lai (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations`, `RecommendationSection.tsx` |
| Customer | Đổi mật khẩu | MISSING | Chưa có endpoint riêng |
| Customer | Quên mật khẩu | MISSING | Chưa có flow gửi token reset |
| Customer | Hủy đơn hàng | MISSING | Khách hàng chưa thể tự hủy đơn trên giao diện |
| Vendor | Đăng ký gian hàng | IMPLEMENTED | `VendorShopRegistrationPage.tsx` |
| Vendor | Chờ duyệt | IMPLEMENTED | `ShopPendingPage.tsx` |
| Vendor | Tạo sản phẩm + ảnh + biến thể | IMPLEMENTED | `VendorProductFormPage.tsx` |
| Vendor | Chỉnh sửa sản phẩm | IMPLEMENTED | Form pre-fill |
| Vendor | Quản lý đơn hàng | IMPLEMENTED | `VendorOrderListPage.tsx` |
| Vendor | Cập nhật trạng thái đơn (Confirm → Ship) | IMPLEMENTED | `PATCH /api/v1/vendor/orders/:id/status` |
| Vendor | Hủy đơn (với lý do + hoàn kho) | IMPLEMENTED | Modal nhập lý do, tự động hồi số lượng tồn biến thể |
| Vendor | Tạo voucher SHOP | IMPLEMENTED | `POST /api/v1/vouchers` (scope=SHOP) |
| Vendor | Phản hồi đánh giá | IMPLEMENTED | `PUT /api/v1/reviews/:id/vendor-reply` |
| Vendor | Nhận thông báo đơn hàng mới | IMPLEMENTED | NEW_ORDER qua WebSocket STOMP |
| Vendor | Chat realtime với khách | IMPLEMENTED | Giao diện Chat WebSocket STOMP |
| Vendor | Phân tích doanh thu 30 ngày | IMPLEMENTED | `GET /api/v1/vendor/analytics/revenue`, `VendorAnalyticsPage.tsx` |
| Vendor | Chỉnh sửa thông tin shop | MISSING | Chưa có màn hình chỉnh sửa mô tả shop |
| Admin | Xem danh sách shop | IMPLEMENTED | `GET /api/v1/admin/shops`, `AdminShopListPage.tsx` |
| Admin | Duyệt / Từ chối / Khóa / Mở khóa shop | IMPLEMENTED | Thao tác đổi trạng thái PENDING, ACTIVE, BANNED + email |
| Admin | Xem đơn hàng toàn hệ thống | IMPLEMENTED | `AdminOrderListPage.tsx` |
| Admin | Xem chi tiết đơn hàng | IMPLEMENTED | `AdminOrderDetailPage.tsx` |
| Admin | Quản lý sản phẩm (CRUD) | IMPLEMENTED | `AdminProductFormPage.tsx` |
| Admin | Tạo voucher SYSTEM tỷ lệ | IMPLEMENTED | `AdminVoucherListPage.tsx`, scope=SYSTEM |
| Admin | Dashboard thống kê doanh thu | IMPLEMENTED | `GET /api/v1/admin/statistics/overview`, `AdminDashboardPage.tsx` |
| Admin | Quản lý người dùng chuyên sâu | MISSING | Chưa có route giao diện riêng cho bảng users |
| Admin | Quản lý cây danh mục UI | MISSING | Chưa có màn hình thêm/sửa node danh mục |
| System | IPN idempotency | IMPLEMENTED | Khóa bi quan PESSIMISTIC_WRITE trên bảng payments |
| System | Email duyệt shop | IMPLEMENTED | Gmail SMTP, `@TransactionalEventListener AFTER_COMMIT` |
| System | Snapshot giá và thuộc tính khi đặt hàng | IMPLEMENTED | `OrderItem.unitPrice`, `OrderItem.variantAttributesSnapshot` |
| System | Proportional voucher split | IMPLEMENTED | Thuật toán phân bổ giảm giá theo tỷ trọng doanh thu |
| System | Tự động hoàn tồn kho khi hủy đơn | IMPLEMENTED | `restoreStockOnCancellation` trong OrderServiceImpl |
| Admin | Hoàn tiền VNPay (Refund) | IMPLEMENTED | `POST /api/v1/admin/orders/:id/refunds`, `AdminOrderDetailPage.tsx` |
| Admin | Lịch sử can thiệp trạng thái đơn hàng | IMPLEMENTED | Bảng `order_admin_status_history`, `GET /api/v1/admin/orders/:id/status-history` |


---

## APPENDIX B — Database Table Summary (Tóm tắt cơ sở dữ liệu)

**Bảng B.1.** Tóm tắt 22 bảng cơ sở dữ liệu

| Bảng | Khóa chính | Khóa ngoại quan trọng | Ràng buộc đặc biệt |
|---|---|---|---|
| `users` | `id` | — | UNIQUE(`email`) |
| `roles` | `id` | — | UNIQUE(`name`) |
| `user_roles` | (`user_id`, `role_id`) | FK: `users.id`, `roles.id` | Composite PK |
| `shops` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 shop |
| `brands` | `id` | — | — |
| `categories` | `id` | FK: `categories.id` (self) | `parent_id` nullable, Cây đệ quy BFS |
| `products` | `id` | FK: `shops.id`, `categories.id`, `brands.id` | Index tìm kiếm và lọc danh mục |
| `product_variants` | `id` | FK: `products.id` | UNIQUE(`sku`), tồn kho không âm |
| `product_images` | `id` | FK: `products.id` | Lưu URL và publicId Cloudinary |
| `carts` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 cart |
| `cart_items` | `id` | FK: `carts.id`, `product_variants.id` | UNIQUE(`cart_id`, `variant_id`) |
| `orders` | `id` | FK: `users.id`, `shops.id` | Gom nhóm qua `payment_group_id` UUID |
| `order_items` | `id` | FK: `orders.id`, `product_variants.id` | Snapshot giá mua và thuộc tính biến thể |
| `order_admin_status_history` | `id` | FK: `orders.id` | Lưu vết thời gian và lý do can thiệp của Admin |
| `payments` | `id` | — | Index `payment_group_id`, Khóa bi quan |
| `refund_attempts` | `id` | FK: `orders.id`, `payments.id` | UNIQUE(`order_id`, `payment_id`) |
| `vouchers` | `id` | FK: `shops.id` (nullable) | UNIQUE(`code`), quản lý qua `used_count` |
| `reviews` | `id` | FK: `users.id`, `products.id`, `orders.id` | UNIQUE(`user_id`, `product_id`, `order_id`) |
| `review_images` | `id` | FK: `reviews.id` | Danh sách URL ảnh đính kèm đánh giá |
| `notifications` | `id` | FK: `users.id` | Lưu thông báo hệ thống |
| `chat_rooms` | `id` | FK: `users.id`, `shops.id` | UNIQUE(`user_id`, `shop_id`) |
| `chat_messages` | `id` | FK: `chat_rooms.id`, `users.id` | Tin nhắn STOMP thời gian thực |

---

## APPENDIX C — Screenshot Checklist

**Bảng C.1.** Danh sách ảnh chụp màn hình cần thiết cho báo cáo

| ID | Tên | Vị trí trong báo cáo | Ưu tiên |
|---|---|---|---|
| IMG-PRODUCT-LIST | Trang danh sách sản phẩm | Hình 3.13 | ⭐⭐⭐ |
| IMG-PRODUCT-DETAIL | Chi tiết sản phẩm với biến thể | Hình 3.14 | ⭐⭐⭐ |
| IMG-CART | Giỏ hàng multi-shop | Hình 3.15 | ⭐⭐⭐ |
| IMG-CHECKOUT | Checkout với voucher | Hình 3.16 | ⭐⭐⭐ |
| IMG-VNPAY | VNPay Sandbox page | Hình 3.24 | ⭐⭐⭐ |
| IMG-PAYMENT-SUCCESS | Payment result SUCCESS | Hình 3.25 | ⭐⭐⭐ |
| IMG-VENDOR-NAV | VendorNav với tên shop | Hình 3.18 | ⭐⭐ |
| IMG-VENDOR-PRODUCT-FORM | Form tạo sản phẩm + ảnh + variants | Hình 3.26 | ⭐⭐⭐ |
| IMG-VENDOR-ORDERS | Vendor quản lý đơn hàng | Hình 3.27 | ⭐⭐⭐ |
| IMG-ADMIN-SHOP | Admin duyệt shop | Hình 3.28 | ⭐⭐⭐ |
| IMG-CHAT | Chat STOMP realtime | Hình 3.29 | ⭐⭐⭐ |

Tham khảo chi tiết: `docs/FINAL_SCREENSHOT_CHECKLIST.md`

---

## APPENDIX D — Video Demo Checklist

**Bảng D.1.** Thứ tự quay video demo hệ thống

| Scene | Tab | Nội dung | Timestamp |
|---|---|---|---|
| ①–⑪ | [Customer] | Login → Browse → Cart → Checkout → VNPay → Payment Result → Review | 0:00–7:30 |
| ⑫–⑮ | [Vendor] | Login → Create Product (Cloudinary) → Orders → Voucher | 7:30–10:30 |
| ⑯–⑱ | [Admin] | Login → Shop Approval → Orders → System Voucher | 10:30–12:30 |
| ⑲ | Split | Chat STOMP realtime hai chiều | 12:30–14:30 |
| ⑳ | [Vendor] | Vendor notification NEW_ORDER | 14:30–15:00 |

Tham khảo chi tiết: `docs/VIDEO_FINAL_FLOW.md`

---

## APPENDIX E — Diagram Checklist

**Bảng E.1.** Danh sách sơ đồ cần tạo cho báo cáo

| ID | Tên sơ đồ | Công cụ khuyến nghị | Vị trí trong báo cáo |
|---|---|---|---|
| DIAGRAM-ARCHITECTURE-OVERVIEW | Kiến trúc tổng thể hệ thống | draw.io / PlantUML | Hình 3.1 |
| DIAGRAM-USE-CASE-OVERVIEW | Use Case tổng thể | draw.io | Hình 3.2 |
| DIAGRAM-CLASS-PRODUCT | Class Diagram nhóm Product | PlantUML | Hình 3.3 |
| DIAGRAM-CLASS-ORDER | Class Diagram nhóm Order/Payment | PlantUML | Hình 3.4 |
| DIAGRAM-SEQUENCE-CHECKOUT | Sequence Diagram checkout | PlantUML | Hình 3.5 |
| DIAGRAM-SEQUENCE-IPN | Sequence Diagram VNPay IPN | PlantUML | Hình 3.6 |
| DIAGRAM-SEQUENCE-CHAT | Sequence Diagram Chat STOMP | PlantUML | Hình 3.7 |
| DIAGRAM-ACTIVITY-ORDER-PAYMENT | Activity Diagram đặt hàng + thanh toán | draw.io | Hình 3.8 |
| DIAGRAM-ACTIVITY-SHOP-APPROVAL | Activity Diagram duyệt gian hàng | draw.io | Hình 3.9 |
| DIAGRAM-STATE-ORDER | State Diagram Order | PlantUML | Hình 3.10 |
| DIAGRAM-STATE-SHOP | State Diagram Shop | PlantUML | Hình 3.11 |
| DIAGRAM-ERD | ERD toàn bộ cơ sở dữ liệu | dbdiagram.io | Hình 3.12 |

Tham khảo spec chi tiết: `docs/CLASS_DIAGRAM_SPEC.md`, `docs/SEQUENCE_DIAGRAM_SPEC.md`, `docs/STATE_DIAGRAM_SPEC.md`, `docs/ERD_SPEC.md`

---

*Kết thúc báo cáo.*
