# 3.7 KẾT QUẢ TRIỂN KHAI HỆ THỐNG

## 3.7.1 Triển khai hệ thống

Hệ thống hiện tại được triển khai trong **môi trường phát triển cục bộ (localhost)** trên máy tính lập trình viên. Chưa có bằng chứng về triển khai trên server sản xuất (production) hay cloud hosting. Toàn bộ mô tả dưới đây phản ánh cấu hình thực tế từ `application.yml`, `.env`, và `package.json`.

---

### 3.7.1.1 Backend

**Bảng 3.64.** Thông tin kỹ thuật Backend

| Thành phần | Công nghệ / Phiên bản | Ghi chú |
|---|---|---|
| Ngôn ngữ | Java 21 | `java.version=21` trong `pom.xml` |
| Framework | Spring Boot 4.1.0 | `spring-boot-starter-parent 4.1.0` |
| Build tool | Maven | `pom.xml` |
| API Documentation | SpringDoc OpenAPI (Swagger UI) 3.0.3 | Truy cập `/swagger-ui/index.html` |
| Spring Security | Spring Security + JJWT 0.12.5 | JWT Bearer token authentication |
| Persistence | Spring Data JPA (Hibernate) | `ddl-auto: update` |
| WebSocket | Spring WebSocket + STOMP | `spring-boot-starter-websocket` |
| Mail | Spring Boot Mail (Gmail SMTP) | STARTTLS, port 587 |
| Media | Cloudinary SDK 1.36.0 | `cloudinary-http44` |
| Monitoring | Spring Actuator | `spring-boot-starter-actuator` |
| Lombok | Lombok (compile-time code gen) | `optional: true` |

**Cổng mặc định:** `8080` (Spring Boot default).

**Cấu hình khởi động:** Backend nhận cấu hình qua biến môi trường (`.env` file, không commit lên git):
- `DB_PASSWORD` — mật khẩu MySQL
- `JWT_SECRET_KEY` — khóa bí mật JWT (256-bit, hex-encoded)
- `MAIL_USERNAME`, `MAIL_PASSWORD` — Gmail App Password
- `CLOUDINARY_CLOUD_NAME`, `CLOUDINARY_API_KEY`, `CLOUDINARY_API_SECRET`
- `VNPAY_TMN_CODE`, `VNPAY_SECRET_KEY`

**Cách chạy:** Thông qua IntelliJ IDEA với cấu hình Run/Debug, load biến môi trường từ `.env`.

---

### 3.7.1.2 Frontend

**Bảng 3.65.** Thông tin kỹ thuật Frontend

| Thành phần | Công nghệ / Phiên bản | Ghi chú |
|---|---|---|
| Framework | React 19.2.8 | `react@^19.2.8` |
| Ngôn ngữ | TypeScript 6.0.2 | `typescript: ~6.0.2` |
| Build tool | Vite 8.2.0 | `vite@^8.2.0` |
| Routing | React Router DOM 7.18.2 | `react-router-dom@^7.18.2` |
| HTTP Client | Axios 1.19.0 | `axios@^1.19.0` |
| WebSocket Client | @stomp/stompjs 7.3.0 + SockJS 1.6.1 | Chat realtime |
| CSS Framework | Tailwind CSS 4.3.3 | PostCSS pipeline |
| Icon Library | lucide-react 1.31.0 | UI icons |
| Linter | oxlint 1.75.0 | Thay thế ESLint |

**Cổng mặc định:** `5173` (Vite dev server).

**Cách chạy:** `npm run dev` trong thư mục `frontend/`.

**URL backend:** Frontend gọi API tại `http://localhost:8080/api/v1/` (hardcoded trong Axios base URL). Cấu hình CORS Backend cho phép `http://localhost:5173` (cấu hình trong `application.yml → app.frontend.url`).

---

### 3.7.1.3 Database

**Bảng 3.66.** Cấu hình Database

| Thành phần | Giá trị |
|---|---|
| Hệ quản trị CSDL | MySQL |
| URL kết nối | `jdbc:mysql://localhost:3306/ecommerce` |
| Username | `root` |
| Password | `${DB_PASSWORD}` (biến môi trường) |
| SSL | Tắt (`useSSL=false`) |
| Timezone | UTC |
| DDL Auto | `update` (Hibernate tự cập nhật schema khi khởi động) |
| Naming Strategy | `CamelCaseToUnderscoresNamingStrategy` |
| SQL Logging | Bật (`show-sql: true`, `format_sql: true`) |

**Lưu ý:** Không sử dụng công cụ migration chuyên biệt (Flyway/Liquibase). Schema được quản lý bởi Hibernate `ddl-auto: update`. Không có thư mục `resources/db/migration/`.

---

### 3.7.1.4 Redis

> **Không có Redis.** Kiểm tra `pom.xml` không tìm thấy dependency `spring-boot-starter-data-redis` hay `lettuce-core`. Không có cấu hình Redis trong `application.yml`.

Hệ thống không sử dụng Redis cache hay session store. Các tính năng thường cần Redis (session, token blacklist, rate limiting) được xử lý theo cách khác hoặc chưa triển khai.

---

### 3.7.1.5 WebSocket / Realtime

**STOMP over WebSocket** được triển khai sử dụng `spring-boot-starter-websocket`:
- Backend: `WebSocketConfig` cấu hình endpoint `/ws`, message broker `/app` và `/user/queue`
- Authentication: `JwtChannelInterceptor` — xác thực JWT trong STOMP CONNECT frame
- Frontend: `@stomp/stompjs` + `SockJS` client (`sockjs-client 1.6.1`)
- Kết nối URL: `http://localhost:8080/ws`

WebSocket được sử dụng cho chức năng **chat realtime** giữa Buyer và Shop (Vendor).

---

### 3.7.1.6 Dịch vụ bên ngoài (External Services)

**Bảng 3.67.** Các dịch vụ bên ngoài tích hợp

| Dịch vụ | Mục đích | Môi trường | Cấu hình |
|---|---|---|---|
| **Cloudinary** | Lưu trữ và phục vụ ảnh sản phẩm (CDN) | Sản xuất (cloud) | `CLOUDINARY_*` env vars |
| **VNPay** | Cổng thanh toán trực tuyến | **Sandbox** (`sandbox.vnpayment.vn`) | `VNPAY_TMN_CODE`, `VNPAY_SECRET_KEY` |
| **Gmail SMTP** | Gửi email thông báo (duyệt shop, v.v.) | Sản xuất (Gmail) | `MAIL_USERNAME`, `MAIL_PASSWORD` (App Password) |

**Quan trọng về VNPay:**
- URL thanh toán: `https://sandbox.vnpayment.vn/paymentv2/vpcpay.html` — đây là **môi trường sandbox (kiểm thử)**, không phải production
- Return URL: `http://localhost:5173/payment-result` — chỉ hoạt động trên localhost
- IPN URL: Do VNPay không gọi được localhost, hệ thống triển khai cơ chế **đồng bộ IPN thủ công** tại `GET /api/v1/payments/vnpay/ipn` — Frontend chủ động gọi sau khi VNPay redirect về, thay vì nhận callback từ VNPay

**Cloudinary:**
- Ảnh sản phẩm được upload lên Cloudinary cloud, lưu `imageUrl` (HTTPS CDN) và `publicId` trong database
- Hoạt động ở môi trường production-ready (cloud CDN thực sự)

---

### 3.7.1.7 Docker

> **Không có Docker.** Kiểm tra toàn bộ project không tìm thấy `Dockerfile` hay `docker-compose.yml`. Hệ thống chưa container hóa.

Backend và Database được chạy trực tiếp trên môi trường máy tính lập trình viên (bare-metal / native install).

---

### 3.7.1.8 Tóm tắt môi trường triển khai

```
[Máy tính lập trình viên — localhost]
├── Backend:  Spring Boot 4.1.0 → localhost:8080
├── Frontend: Vite + React 19   → localhost:5173
├── Database: MySQL              → localhost:3306/ecommerce
│
├── [External — Cloud]
│   ├── Cloudinary CDN   (ảnh sản phẩm)
│   ├── VNPay Sandbox    (thanh toán — kiểm thử)
│   └── Gmail SMTP       (email thông báo)
```

---

## 3.7.2 Chức năng đã hoàn thành

Bảng dưới đây phân loại toàn bộ chức năng hệ thống dựa trên bằng chứng thực tế từ source code frontend và backend.

**Quy ước phân loại:**
- `IMPLEMENTED` — Có đầy đủ cả Frontend UI + Backend API + hoạt động đúng
- `PARTIAL` — Có một phần; hoặc UI hoạt động nhưng thiếu tính năng phụ
- `UI_ONLY` — Giao diện có nhưng chưa kết nối API / chưa có logic backend
- `BACKEND_ONLY` — API backend hoạt động nhưng frontend chưa có UI đầy đủ
- `MISSING` — Chưa triển khai ở cả hai phía

**Bảng 3.68.** Phân loại chức năng theo actor

| Actor | Feature | Status | Evidence |
|---|---|---|---|
| **Customer** | Đăng ký tài khoản | IMPLEMENTED | `POST /api/v1/auth/register`, `RegisterPage.tsx` |
| **Customer** | Đăng nhập | IMPLEMENTED | `POST /api/v1/auth/login`, JWT, `LoginPage.tsx` |
| **Customer** | Đăng xuất | IMPLEMENTED | `AuthContext.logout()`, clear localStorage |
| **Customer** | Xem danh sách sản phẩm | IMPLEMENTED | `ProductListPage.tsx`, `GET /api/v1/products` |
| **Customer** | Tìm kiếm sản phẩm | IMPLEMENTED | Param `keyword` trong API + UI |
| **Customer** | Lọc sản phẩm theo danh mục | IMPLEMENTED | Param `categoryId` + filter UI |
| **Customer** | Xem chi tiết sản phẩm | IMPLEMENTED | `ProductDetailPage.tsx`, gallery ảnh, chọn biến thể |
| **Customer** | Thêm sản phẩm vào giỏ hàng | IMPLEMENTED | `POST /api/v1/carts/items`, `CartContext` |
| **Customer** | Xem giỏ hàng | IMPLEMENTED | `CartPage.tsx`, `GET /api/v1/carts` |
| **Customer** | Cập nhật số lượng trong giỏ | IMPLEMENTED | `PUT /api/v1/carts/items/:id` |
| **Customer** | Xóa sản phẩm khỏi giỏ | IMPLEMENTED | `DELETE /api/v1/carts/items/:id` |
| **Customer** | Checkout (đặt hàng) | IMPLEMENTED | `CheckoutPage.tsx`, `POST /api/v1/orders/checkout` |
| **Customer** | Áp dụng mã giảm giá | IMPLEMENTED | `POST /api/v1/orders/preview`, voucher validation |
| **Customer** | Thanh toán VNPay (Sandbox) | IMPLEMENTED | `PaymentResultPage.tsx`, redirect + IPN sync |
| **Customer** | Thanh toán COD | IMPLEMENTED | Đơn xác nhận ngay, `paymentMethod=COD` |
| **Customer** | Xem lịch sử đơn hàng | IMPLEMENTED | `OrderHistoryPage.tsx`, tab filter 5 trạng thái |
| **Customer** | Xem chi tiết đơn hàng | IMPLEMENTED | `OrderDetailPage.tsx`, snapshot giá |
| **Customer** | Viết đánh giá sản phẩm | IMPLEMENTED | `CreateReviewModal`, `POST /api/v1/reviews` |
| **Customer** | Sửa / Xóa đánh giá | IMPLEMENTED | `PUT/DELETE /api/v1/reviews/:id` |
| **Customer** | Xem đánh giá + rating summary | IMPLEMENTED | `ProductReviewSection`, histogram sao |
| **Customer** | Nhận thông báo hệ thống | IMPLEMENTED | `NotificationDropdown`, `GET /api/v1/notifications` |
| **Customer** | Đánh dấu thông báo đã đọc | IMPLEMENTED | `PUT /api/v1/notifications/mark-all-read` |
| **Customer** | Chat realtime với Shop | PARTIAL | Backend + STOMP hoạt động; UI `/chat-test` là developer page, chưa có UI production-ready |
| **Customer** | Xem hồ sơ cá nhân | PARTIAL | `ProfilePage.tsx` hiển thị từ JWT; chưa có chức năng chỉnh sửa |
| **Customer** | Chỉnh sửa thông tin cá nhân | MISSING | Không có API `PUT /api/v1/users/me` hay UI update |
| **Customer** | Đổi mật khẩu | MISSING | Không có API hay UI |
| **Customer** | Theo dõi gian hàng (Follow) | MISSING | Không có feature |
| **Vendor** | Đăng ký gian hàng | IMPLEMENTED | `VendorShopRegistrationPage.tsx`, `POST /api/v1/shops` |
| **Vendor** | Chờ duyệt gian hàng | IMPLEMENTED | `ShopPendingPage.tsx`, trạng thái PENDING |
| **Vendor** | Xem danh sách sản phẩm của shop | IMPLEMENTED | `VendorProductListPage.tsx`, `GET /api/v1/vendor/products` |
| **Vendor** | Tạo sản phẩm + biến thể + ảnh | IMPLEMENTED | `VendorProductFormPage.tsx`, Cloudinary upload |
| **Vendor** | Chỉnh sửa sản phẩm | IMPLEMENTED | Form pre-fill, `PUT /api/v1/vendor/products/:id` |
| **Vendor** | Quản lý đơn hàng của shop | IMPLEMENTED | `VendorOrderListPage.tsx`, tab filter |
| **Vendor** | Cập nhật trạng thái đơn (SHIPPED/DELIVERED) | IMPLEMENTED | `PATCH /api/v1/vendor/orders/:id/status` |
| **Vendor** | Hủy đơn hàng (có lý do) | IMPLEMENTED | Modal nhập lý do, `status=CANCELLED` |
| **Vendor** | Tạo mã giảm giá (voucher) | IMPLEMENTED | `VendorVoucherListPage.tsx`, `POST /api/v1/vouchers` (scope=SHOP) |
| **Vendor** | Phản hồi đánh giá (reply) | IMPLEMENTED | Inline form, `PUT /api/v1/reviews/:id/vendor-reply` |
| **Vendor** | Nhận thông báo đơn hàng mới | IMPLEMENTED | `NEW_ORDER` event → `@TransactionalEventListener` |
| **Vendor** | Nhận thông báo review mới | IMPLEMENTED | `NEW_REVIEW` event → notification |
| **Vendor** | Chat realtime với Buyer | PARTIAL | Backend STOMP hoạt động; UI production chưa hoàn thiện |
| **Vendor** | Chỉnh sửa thông tin shop | MISSING | Không có UI `PUT /api/v1/shops/:id` |
| **Vendor** | Thống kê doanh thu shop | MISSING | Không có API hay UI dashboard thống kê |
| **Vendor** | Quản lý tồn kho trực tiếp | PARTIAL | Tồn kho cập nhật qua `ProductVariant.stockQuantity` khi sửa sản phẩm; không có màn hình Inventory riêng |
| **Admin** | Xem danh sách gian hàng | IMPLEMENTED | `AdminShopListPage.tsx`, `GET /api/v1/admin/shops` |
| **Admin** | Duyệt gian hàng (PENDING → ACTIVE) | IMPLEMENTED | `PUT /api/v1/admin/shops/:id/approve` + email + ROLE_VENDOR |
| **Admin** | Từ chối / Khóa gian hàng | IMPLEMENTED | `PUT /api/v1/admin/shops/:id/ban` |
| **Admin** | Xem danh sách đơn hàng toàn hệ thống | IMPLEMENTED | `AdminOrderListPage.tsx`, `GET /api/v1/admin/orders` |
| **Admin** | Xem chi tiết đơn hàng | IMPLEMENTED | `AdminOrderDetailPage.tsx` |
| **Admin** | Xem danh sách sản phẩm | IMPLEMENTED | `AdminProductListPage.tsx` |
| **Admin** | Tạo / Sửa sản phẩm | IMPLEMENTED | `AdminProductFormPage.tsx`, tương đương Vendor form |
| **Admin** | Tạo voucher hệ thống | IMPLEMENTED | `AdminVoucherListPage.tsx`, scope=SYSTEM, FREE_SHIPPING |
| **Admin** | Xem danh sách voucher (SYSTEM + SHOP) | IMPLEMENTED | `GET /api/v1/admin/vouchers` |
| **Admin** | Dashboard thống kê | MISSING | Chỉ có text placeholder; không có widget |
| **Admin** | Quản lý người dùng (User list) | MISSING | Không có màn hình `/admin/users` |
| **Admin** | Quản lý danh mục (Category CRUD) | MISSING | Không có UI; chỉ có API backend |
| **Admin** | Quản lý thương hiệu (Brand CRUD) | MISSING | Không có UI; chỉ có API backend |
| **System** | Đồng bộ trạng thái payment (VNPay IPN) | IMPLEMENTED | `PaymentIpnService`, PESSIMISTIC_WRITE lock |
| **System** | Idempotency payment (chống trùng) | IMPLEMENTED | Check `payment.status != PENDING` trước xử lý |
| **System** | Gửi email duyệt shop | IMPLEMENTED | Gmail SMTP, `ShopApprovalIntegrationTest` |
| **System** | Snapshot giá khi đặt hàng | IMPLEMENTED | `OrderItem.unitPrice`, `productName` snapshot |
| **System** | Tự động xác nhận đơn khi payment | IMPLEMENTED | `PENDING → CONFIRMED` sau IPN thành công |

---

## 3.7.3 Hình ảnh chạy thực tế

Tham khảo file [`FINAL_SCREENSHOT_CHECKLIST.md`](./FINAL_SCREENSHOT_CHECKLIST.md) để biết danh sách ảnh cần chụp theo thứ tự ưu tiên.

Các nhóm ảnh quan trọng nhất:
1. **Luồng mua hàng** — Product List → Detail → Cart → Checkout → Payment Result → Orders
2. **Quản lý Vendor** — Đăng ký shop → Product Form → Order Management → Voucher
3. **Admin** — Shop approval → Order overview
4. **Realtime** — Chat STOMP, Notification dropdown

---

## 3.7.4 Hạn chế còn tồn tại

### 3.7.4.1 Frontend chưa hoàn thiện

| Hạn chế | Bằng chứng |
|---|---|
| `ProfilePage` chỉ hiển thị — không có chỉnh sửa thông tin cá nhân | Source code `ProfilePage.tsx` không có form edit |
| `FloatingChatWidget` chưa có panel mở rộng hoàn chỉnh | Component import nhưng expanded UI chưa implement |
| `/chat-test` là developer interface — không phải UI end-user | Trang có STOMP Event Log, nhập Shop ID thủ công |
| Admin Dashboard là placeholder | Chỉ có text "Chào mừng bạn đến với trang quản trị" |
| Ô tìm kiếm nhanh trên Admin topbar không có chức năng | Input HTML không có handler backend |
| Không có màn hình quản lý danh mục và thương hiệu (Admin) | Không tồn tại `/admin/categories`, `/admin/brands` |
| Không có màn hình quản lý người dùng (Admin) | Không tồn tại `/admin/users` |

### 3.7.4.2 Backend chưa hoàn thiện

| Hạn chế | Bằng chứng |
|---|---|
| Không có API chỉnh sửa thông tin người dùng | Không có `PUT /api/v1/users/me` trong controller |
| Không có API đổi mật khẩu | Không tìm thấy endpoint `changePassword` |
| Không có tính năng tìm kiếm toàn văn (Full-text search) | Chỉ filter `keyword LIKE %...%` — không có Elasticsearch |
| Không có API thống kê doanh thu cho Vendor/Admin | Không có `/api/v1/admin/statistics` hay `/vendor/statistics` |
| DDL auto-update thay vì migration tool | `ddl-auto: update` — rủi ro trong production |

### 3.7.4.3 Feature thiếu

| Feature | Phân loại |
|---|---|
| Quên mật khẩu / Reset password | MISSING |
| Đăng nhập bằng Google/Social | MISSING |
| Wishlist / Danh sách yêu thích | MISSING |
| Theo dõi gian hàng (Follow Shop) | MISSING |
| Địa chỉ giao hàng đa dạng (Address Book) | MISSING — chỉ có 1 địa chỉ text free-form |
| Phí vận chuyển tính theo khu vực | MISSING |
| Xếp hạng shop (Shop rating) | MISSING |
| Báo cáo sản phẩm / Tố giác | MISSING |
| Hệ thống điểm thưởng / Loyalty | MISSING |

### 3.7.4.4 UX

| Hạn chế | Mô tả |
|---|---|
| Không có loading skeleton | Màn hình chỉ hiển thị spinner hoặc trống khi load |
| Không có infinite scroll | Danh sách dùng button phân trang |
| Không có real-time stock update | Tồn kho hiển thị tại thời điểm load page |
| Không có bộ lọc nâng cao (giá, màu, kích thước) | Product list chỉ lọc theo `keyword` và `categoryId` |

### 3.7.4.5 Security

| Hạn chế | Bằng chứng |
|---|---|
| JWT không có cơ chế thu hồi (token revocation) | Không có `TokenBlacklist` hay Redis session |
| Không có Rate Limiting | Không có `spring-boot-starter-cache` hay rate limit middleware |
| SQL logging bật trong config mặc định | `show-sql: true` ghi log câu SQL production |
| CORS cấu hình chỉ cho localhost | `app.frontend.url: http://localhost:5173` |

### 3.7.4.6 Testing

| Hạn chế | Bằng chứng |
|---|---|
| Không có unit test cho service layer | Các test file là Integration Test (`@SpringBootTest`) |
| Test có file `DataSeeder` — test phụ thuộc DB thật | `FullSystemDataSeederTest.java` |
| Không có frontend test | Không có Jest, Vitest, hay Playwright trong `package.json` |
| Test coverage không đo lường | Không có Jacoco hay coverage plugin trong `pom.xml` |

### 3.7.4.7 Payment

| Hạn chế | Bằng chứng |
|---|---|
| VNPay chỉ hoạt động ở môi trường sandbox | `pay-url: sandbox.vnpayment.vn` |
| IPN callback không tự động ở localhost | Workaround: Frontend gọi manual IPN sync sau redirect |
| Không có Webhook tự động cho production | `return-url: localhost:5173` không khả dụng ngoài môi trường dev |
| Không hỗ trợ hoàn tiền (Refund) | Không có API refund hay logic hoàn tiền |

### 3.7.4.8 Realtime & Chat

| Hạn chế | Bằng chứng |
|---|---|
| Chat UI production chưa hoàn thiện | `FloatingChatWidget` mở rộng chưa implement |
| Không có chỉ báo "đang gõ..." (typing indicator) | Không tìm thấy STOMP frame loại này |
| Không có push notification (Web Push / FCM) | Chỉ có in-app notification dropdown |
| WebSocket không scale horizontal | Dùng In-memory broker — không phải Redis Pub/Sub hay RabbitMQ |

### 3.7.4.9 Triển khai & Scalability

| Hạn chế | Bằng chứng |
|---|---|
| Không có Docker / Container hóa | Không có `Dockerfile` hay `docker-compose.yml` |
| Không có CI/CD pipeline | Không có `.github/workflows/` hay Jenkinsfile |
| Không có load balancer hay reverse proxy | Chỉ có single-instance localhost |
| WebSocket dùng in-memory broker | Không thể scale horizontal nếu nhiều instance |
| Database không có migration versioning | `ddl-auto: update` — không kiểm soát được thay đổi schema |
| Không có môi trường staging | Chỉ có development localhost |

---

*Kết thúc phần 3.7.*
