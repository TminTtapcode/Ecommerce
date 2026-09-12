## 3.6. Thiết kế giao diện hệ thống — Customer (Người mua)

Phần này mô tả các màn hình giao diện dành cho người dùng Customer (Buyer) dựa trên source code frontend thực tế. Hệ thống frontend được xây dựng bằng **React + TypeScript + Vite**, sử dụng **Tailwind CSS** cho styling và **React Router v6** cho điều hướng.

> **Màu chủ đạo:** Cam (`orange-500` — `#f97316`) — xuyên suốt toàn bộ giao diện.
> **Layout chính:** `PublicLayout` — header cố định trên cùng + nội dung chính + footer.

---

### 3.6.1. Tổng quan các màn hình Customer

**Bảng 3.58.** Danh sách màn hình Customer và route

| STT | Tên màn hình | Route | Cần đăng nhập | File |
|---|---|---|---|---|
| 1 | Đăng nhập | `/login` | Không | `LoginPage.tsx` |
| 2 | Đăng ký | `/register` | Không | `RegisterPage.tsx` |
| 3 | Danh sách sản phẩm | `/products` | Không | `ProductListPage.tsx` |
| 4 | Chi tiết sản phẩm | `/products/:id` | Không (đánh giá cần) | `ProductDetailPage.tsx` |
| 5 | Giỏ hàng | `/cart` | Có | `CartPage.tsx` |
| 6 | Thanh toán | `/checkout` | Có | `CheckoutPage.tsx` |
| 7 | Kết quả thanh toán | `/payment-result` | Có | `PaymentResultPage.tsx` |
| 8 | Lịch sử đơn hàng | `/orders` | Có | `OrderHistoryPage.tsx` |
| 9 | Chi tiết đơn hàng | `/orders/:id` | Có | `OrderDetailPage.tsx` |
| 10 | Hồ sơ cá nhân | `/profile` | Có | `ProfilePage.tsx` |
| 11 | Chat (Developer Test) | `/chat-test` | Có | `ChatTestPage.tsx` |

---

### 3.6.2. Màn hình Đăng nhập (Login)

**1. Tên nghiệp vụ:** Đăng nhập tài khoản

**2. Mục đích:** Xác thực người dùng và cấp JWT token để truy cập các chức năng yêu cầu đăng nhập.

**3. Màn hình:** `/login` — Toàn màn hình, không có header/footer (không dùng `PublicLayout`).

**4. Thành phần giao diện:**
- Form đăng nhập gồm: trường Email, trường Password (toggle hiển thị), nút "Đăng nhập"
- Link "Chưa có tài khoản? Đăng ký ngay" → `/register`
- Thông báo lỗi nếu sai thông tin

**5. User interaction:**
- Nhập email và password → nhấn "Đăng nhập"
- Validation phía client (required fields)
- Nếu thành công: redirect đến `/products` hoặc trang trước đó (từ `location.state`)
- Nếu thất bại: hiển thị thông báo lỗi từ API

**6. API backend:** `POST /api/v1/auth/login` → JWT token lưu vào `AuthContext` và `localStorage`

**7. Kết quả:** User được xác thực, JWT token lưu trữ, redirect về trang chính

---

### 3.6.3. Màn hình Đăng ký (Register)

**1. Tên nghiệp vụ:** Tạo tài khoản mới

**2. Mục đích:** Cho phép người dùng mới tạo tài khoản Buyer.

**3. Màn hình:** `/register` — Toàn màn hình, không có header/footer.

**4. Thành phần giao diện:**
- Form gồm: Họ tên, Email, Mật khẩu, Xác nhận mật khẩu
- Nút "Đăng ký"
- Link "Đã có tài khoản? Đăng nhập" → `/login`

**5. User interaction:**
- Nhập thông tin đầy đủ → nhấn "Đăng ký"
- Validation: email hợp lệ, mật khẩu khớp nhau
- Nếu thành công: redirect đến `/login` hoặc tự động đăng nhập

**6. API backend:** `POST /api/v1/auth/register`

**7. Kết quả:** Tài khoản tạo với `ROLE_USER`, JWT token trả về

---

### 3.6.4. Màn hình Danh sách sản phẩm (Product List)

**1. Tên nghiệp vụ:** Xem và tìm kiếm sản phẩm

**2. Mục đích:** Hiển thị toàn bộ sản phẩm đang hoạt động trong hệ thống, hỗ trợ tìm kiếm và lọc.

**3. Màn hình:** `/products` — Accessible bởi Guest lẫn Buyer.

**4. Thành phần giao diện:**
- **Header:** Thanh tìm kiếm với ô input và nút tìm kiếm
- **Filter sidebar / Filter bar:** Lọc theo danh mục (Category), thương hiệu (Brand), khoảng giá
- **Lưới sản phẩm (Grid):** Hiển thị `ProductCard` 4 cột trên desktop, 2 cột trên mobile
- **ProductCard:** Ảnh thumbnail, tên sản phẩm, giá, số đánh giá (nếu có)
- **Pagination:** Phân trang cuối trang

**5. User interaction:**
- Nhập từ khóa vào ô tìm kiếm → nhấn Enter hoặc nút tìm → reload danh sách với `keyword` param
- Chọn danh mục → filter theo `categoryId`
- Nhấn vào `ProductCard` → navigate đến `/products/:id`
- Chuyển trang → fetch trang mới

**6. API backend:**
- `GET /api/v1/products?keyword=&categoryId=&brandId=&page=&size=`
- `GET /api/v1/categories` (lấy danh sách danh mục cho filter)

**7. Kết quả:** Danh sách sản phẩm hiển thị đúng theo filter, phân trang

---

### 3.6.5. Màn hình Chi tiết sản phẩm (Product Detail)

**1. Tên nghiệp vụ:** Xem chi tiết sản phẩm, chọn biến thể, thêm vào giỏ

**2. Mục đích:** Hiển thị đầy đủ thông tin sản phẩm, cho phép Buyer chọn biến thể và thêm vào giỏ hàng.

**3. Màn hình:** `/products/:id`

**4. Thành phần giao diện:**
- **Gallery ảnh:** Ảnh lớn chính + thumbnail row, click để đổi ảnh chính
- **Thông tin sản phẩm:** Tên, giá, trạng thái tồn kho, mô tả
- **Chọn biến thể:** Danh sách `ProductVariant` — chọn màu/size/attributes (JSON attributes render thành buttons)
- **Chọn số lượng:** Input số lượng với nút `+/-`
- **Nút hành động:** "Thêm vào giỏ hàng" (cam), "Mua ngay" (nếu có)
- **Phần đánh giá (ProductReviewSection):** Rating tổng, histogram điểm theo sao (1–5), danh sách review phân trang, nút "Viết đánh giá" (khi đã mua và đơn DELIVERED)
- **Nút Vendor Reply:** Vendor có thể reply từng review trực tiếp trên trang này

**5. User interaction:**
- Chọn biến thể → giá và tồn kho cập nhật
- Nhập số lượng → nhấn "Thêm vào giỏ" → CartContext cập nhật, badge giỏ hàng trên header tăng
- Cuộn xuống phần đánh giá → xem reviews
- Nếu đã đăng nhập và có đơn DELIVERED chứa sản phẩm này: hiện nút "Viết đánh giá" → mở `CreateReviewModal`
- Trong modal: chọn số sao (1–5), nhập comment → submit → review hiện ngay

**6. API backend:**
- `GET /api/v1/products/:id` — chi tiết sản phẩm
- `POST /api/v1/carts/items` — thêm vào giỏ
- `GET /api/v1/products/:id/reviews` — danh sách đánh giá
- `POST /api/v1/reviews` — tạo đánh giá mới
- `PUT /api/v1/reviews/:id` — sửa đánh giá
- `DELETE /api/v1/reviews/:id` — xóa đánh giá

**7. Kết quả:** Buyer thêm được sản phẩm vào giỏ; Buyer đã nhận hàng có thể viết đánh giá

---

### 3.6.6. Màn hình Giỏ hàng (Cart)

**1. Tên nghiệp vụ:** Xem và quản lý giỏ hàng

**2. Mục đích:** Hiển thị các sản phẩm đã thêm vào giỏ, cho phép thay đổi số lượng, xóa, và tiến hành thanh toán.

**3. Màn hình:** `/cart` — Yêu cầu đăng nhập (`ProtectedRoute`).

**4. Thành phần giao diện:**
- **Danh sách CartItem:** Mỗi item gồm ảnh thumbnail, tên sản phẩm, attributes (màu/size), đơn giá, input số lượng, nút xóa
- **Tóm tắt đơn hàng (Order Summary):** Tổng số sản phẩm, tổng tiền
- **Nút "Tiến hành đặt hàng"** → navigate đến `/checkout`
- **Trạng thái giỏ trống:** Thông báo + link về trang sản phẩm

**5. User interaction:**
- Thay đổi số lượng → `PUT /api/v1/carts/items/:id` → tổng tiền cập nhật realtime
- Nhấn nút xóa → `DELETE /api/v1/carts/items/:id` → item biến mất
- Nhấn "Đặt hàng" → navigate đến `/checkout`

**6. API backend:**
- `GET /api/v1/carts` — lấy giỏ hàng hiện tại
- `PUT /api/v1/carts/items/:itemId` — cập nhật số lượng
- `DELETE /api/v1/carts/items/:itemId` — xóa item

**7. Kết quả:** Giỏ hàng luôn đồng bộ với server; Buyer tiến hành đặt hàng

---

### 3.6.7. Màn hình Thanh toán (Checkout)

**1. Tên nghiệp vụ:** Đặt hàng và chọn phương thức thanh toán

**2. Mục đích:** Buyer điền địa chỉ giao hàng, áp dụng mã giảm giá, chọn phương thức thanh toán, và xác nhận đặt hàng.

**3. Màn hình:** `/checkout` — Yêu cầu đăng nhập.

**4. Thành phần giao diện:**
- **Danh sách sản phẩm thanh toán:** Tóm tắt các item được chọn từ giỏ
- **Form địa chỉ giao hàng:** Ô input địa chỉ đầy đủ
- **Ô mã giảm giá (Voucher):** Input mã + nút "Áp dụng" + thông báo kết quả
- **Tóm tắt giá:** Tổng gốc, giảm giá, tổng thanh toán
- **Phương thức thanh toán:** Radio buttons: "Thanh toán VNPay" / "Thanh toán COD"
- **Nút "Đặt hàng":** Xác nhận checkout

**5. User interaction:**
- Điền địa chỉ → nhập voucher code → nhấn "Áp dụng" → hiển thị giảm giá preview (`POST /api/v1/orders/preview`)
- Chọn VNPAY → nhấn "Đặt hàng" → Backend tạo N orders + 1 payment group → Frontend nhận `paymentUrl` → redirect sang VNPay
- Chọn COD → nhấn "Đặt hàng" → Đơn xác nhận ngay → navigate đến `/payment-result`

**6. API backend:**
- `POST /api/v1/orders/preview` — preview tổng tiền + voucher
- `POST /api/v1/orders/checkout` — tạo đơn hàng (trả về `paymentGroupId`)
- `POST /api/v1/payments/create` — tạo payment URL (VNPay hoặc COD)

**7. Kết quả:** N đơn hàng được tạo (1 per shop), `paymentGroupId` lưu vào `sessionStorage`, redirect tới VNPay hoặc `/payment-result`

---

### 3.6.8. Màn hình Kết quả thanh toán (Payment Result)

**1. Tên nghiệp vụ:** Xác nhận kết quả giao dịch

**2. Mục đích:** Hiển thị trạng thái thanh toán sau khi VNPay redirect về, tự động polling cho đến khi có kết quả.

**3. Màn hình:** `/payment-result` — Yêu cầu đăng nhập.

**4. Thành phần giao diện:**

Màn hình hiển thị 4 trạng thái khác nhau:

| Trạng thái | Icon | Màu | Nội dung |
|---|---|---|---|
| `loading` | Spinner xoay | Cam | "Đang xác nhận thanh toán..." |
| `success` | ✓ Tích xanh lá | Xanh | "Thanh Toán Thành Công!" + nút "Xem Đơn Hàng" |
| `timeout` | ⏱ Đồng hồ | Vàng | "Đang Chờ Xác Nhận" + nút "Kiểm Tra Lại" + "Vào Xem Đơn Hàng" |
| `failed` | ✗ Vòng tròn đỏ | Đỏ | "Thanh Toán Thất Bại" + nút "Về Giỏ Hàng" + "Xem Đơn Hàng" |

**5. User interaction:**
- Trang tự động chạy khi load:
  1. Nếu URL chứa `vnp_ResponseCode` (callback từ VNPay): gọi `paymentApi.syncVnpayIpn()` để đồng bộ IPN (workaround cho môi trường localhost — VNPay không thể gọi localhost)
  2. Polling `GET /api/v1/payments/group/:id/status` mỗi 3 giây, tối đa 10 lần
  3. Khi SUCCESS/FAILED: dừng polling, hiển thị kết quả tương ứng
- Nhấn "Xem Đơn Hàng" → navigate `/orders`
- Nhấn "Kiểm Tra Lại" → restart polling

**6. API backend:**
- `GET /api/v1/payments/vnpay/ipn?{vnp_params}` — đồng bộ IPN thủ công (localhost workaround)
- `GET /api/v1/payments/group/:paymentGroupId/status` — kiểm tra trạng thái

**7. Kết quả:** Buyer biết rõ kết quả giao dịch; nếu thành công → đơn hàng đã CONFIRMED

---

### 3.6.9. Màn hình Lịch sử đơn hàng (Order History)

**1. Tên nghiệp vụ:** Xem danh sách đơn hàng đã đặt

**2. Mục đích:** Cho phép Buyer xem toàn bộ đơn hàng, lọc theo trạng thái, và theo dõi tiến trình.

**3. Màn hình:** `/orders` — Yêu cầu đăng nhập.

**4. Thành phần giao diện:**
- **Tab bar (Status Filter):** Các tab: Tất cả / Chờ xác nhận / Đã xác nhận / Đang giao / Hoàn thành / Đã hủy
- **Danh sách đơn hàng:** Mỗi card đơn hiển thị: Mã đơn (#id), Ngày đặt, Tổng tiền, Trạng thái (màu sắc theo status), danh sách sản phẩm tóm tắt
- **Phân trang:** Pagination ở cuối (5 đơn/trang)
- **Trạng thái rỗng:** Thông báo khi chưa có đơn hàng

**5. User interaction:**
- Nhấn tab trạng thái → filter đơn theo status, reset về trang 1
- Nhấn vào card đơn → navigate đến `/orders/:id`
- Chuyển trang → fetch trang tiếp theo

**6. API backend:**
- `GET /api/v1/orders?page=&size=5&status=` — danh sách đơn phân trang, filter

**7. Kết quả:** Danh sách đơn hiển thị đúng theo filter, màu sắc trạng thái trực quan

**Màu trạng thái:**
- PENDING → Vàng (`text-yellow-600`)
- CONFIRMED → Xanh dương (`text-blue-500`)
- SHIPPED → Xanh tím (`text-indigo-500`)
- DELIVERED → Xanh lá (`text-green-500`)
- CANCELLED → Đỏ (`text-red-500`)

---

### 3.6.10. Màn hình Chi tiết đơn hàng (Order Detail)

**1. Tên nghiệp vụ:** Xem chi tiết một đơn hàng

**2. Mục đích:** Hiển thị đầy đủ thông tin đơn hàng: sản phẩm, giá, địa chỉ giao hàng, trạng thái, và link đến đánh giá (khi DELIVERED).

**3. Màn hình:** `/orders/:id` — Yêu cầu đăng nhập.

**4. Thành phần giao diện:**
- **Header đơn hàng:** Mã đơn (#id), ngày tạo, phương thức thanh toán, trạng thái (badge màu)
- **Địa chỉ giao hàng**
- **Danh sách sản phẩm:** Mỗi OrderItem: ảnh, tên (snapshot), số lượng, đơn giá, thành tiền
- **Nút "Viết đánh giá":** Hiện khi `status == DELIVERED` — click → navigate đến `/products/:productId?action=review&orderId=:orderId#reviews`
- **Tóm tắt giá:** Tổng gốc, giảm giá (nếu có voucher), tổng thanh toán
- **Thông tin voucher:** Nếu đơn đã áp dụng voucher, hiện mã và số tiền giảm

**5. User interaction:**
- Xem thông tin đơn
- Nhấn "Viết đánh giá" → redirect đến trang product detail với query params → `CreateReviewModal` tự mở

**6. API backend:**
- `GET /api/v1/orders/:id` — chi tiết đơn hàng

**7. Kết quả:** Buyer xem đầy đủ thông tin đơn; có thể tiến hành đánh giá sau khi nhận hàng

---

### 3.6.11. Màn hình Hồ sơ cá nhân (Profile)

**1. Tên nghiệp vụ:** Xem thông tin tài khoản

**2. Mục đích:** Hiển thị thông tin cơ bản của tài khoản đang đăng nhập.

**3. Màn hình:** `/profile` — Yêu cầu đăng nhập.

**4. Thành phần giao diện:**
- **Avatar placeholder:** Hình tròn cam với ký tự đầu tiên của tên
- **Thông tin hiển thị:** User ID, Email, Họ tên, Vai trò (badges)

**5. User interaction:** Chỉ xem, không có chức năng chỉnh sửa.

**6. API backend:** Không gọi API — lấy data từ `AuthContext` (JWT payload đã decode)

> **Lưu ý:** Giao diện `ProfilePage` đã được xây dựng nhưng chức năng **chỉnh sửa thông tin cá nhân** (họ tên, số điện thoại) **chưa được triển khai**. Trang hiện tại chỉ hiển thị thông tin đọc từ JWT token.

**7. Kết quả:** Người dùng xem được thông tin tài khoản hiện tại

---

### 3.6.12. Màn hình Chat (ChatTestPage — Developer Interface)

**1. Tên nghiệp vụ:** Chat realtime với gian hàng (WebSocket STOMP)

**2. Mục đích:** Cho phép Buyer giao tiếp trực tiếp với Vendor qua WebSocket.

**3. Màn hình:** `/chat-test` — Yêu cầu đăng nhập.

> **Lưu ý quan trọng:** `/chat-test` là **màn hình kiểm thử dành cho developer**, không phải giao diện người dùng cuối hoàn chỉnh. Trang này hiển thị STOMP Event Log và yêu cầu nhập Shop ID thủ công. Chức năng chat realtime **đã hoạt động đầy đủ ở backend**, nhưng giao diện chat tích hợp cho người dùng cuối **đang trong quá trình hoàn thiện**.

**4. Thành phần giao diện (3 cột):**
- **Cột trái:** Form nhập Shop ID để tạo/vào phòng chat, danh sách phòng chat với unread count và last message preview
- **Cột giữa:** Cửa sổ chat — danh sách tin nhắn (bubble trái/phải theo sender), form input gửi tin
- **Cột phải:** STOMP Event Log (terminal đen) — hiển thị realtime logs: CONNECT, SUBSCRIBE, MESSAGE IN/OUT, ERROR

**5. User interaction:**
- Kết nối WebSocket tự động khi vào trang (JWT xác thực qua `JwtChannelInterceptor`)
- Nhập Shop ID → nhấn "Vào Chat" → tạo hoặc mở ChatRoom `(buyerId, shopId)`
- Chọn phòng chat từ danh sách → load lịch sử tin nhắn
- Nhập tin → nhấn "Gửi" → STOMP SEND → tin hiển thị ngay (Optimistic UI) → server echo về với ID thật

**6. API/Protocol backend:**
- **REST:** `GET /api/v1/chat/rooms`, `GET /api/v1/chat/rooms/:id/messages`, `POST /api/v1/chat/rooms` (tạo phòng)
- **WebSocket STOMP:** `SEND /app/chat.send`, `SUBSCRIBE /user/queue/messages`

**7. Kết quả:** Chat realtime hoạt động đúng qua WebSocket STOMP

---

### 3.6.13. Các thành phần giao diện dùng chung (Shared Components)

#### 3.6.13.1. Header (PublicLayout)

Header cố định trên cùng gồm:
- **Logo/Tên hệ thống** — link về `/products`
- **Thanh tìm kiếm** — input + button (kết hợp với `ProductListPage`)
- **Nút "Giỏ hàng"** (CartIcon) với badge số lượng item — từ `CartContext`
- **`NotificationDropdown`** — icon chuông với badge số thông báo chưa đọc, click mở dropdown danh sách 10 thông báo mới nhất
- **User Menu** — hiển thị avatar (chữ cái đầu), dropdown: Hồ sơ / Đơn hàng / (Vendor Dashboard nếu có ROLE_VENDOR) / Đăng xuất
- **Nút "Đăng nhập / Đăng ký"** (khi chưa đăng nhập)

#### 3.6.13.2. Notification Dropdown

`NotificationDropdown` là dropdown mở khi nhấn icon chuông:
- Badge đỏ hiển thị số thông báo chưa đọc (từ API `GET /api/v1/notifications/unread-count`)
- Danh sách tối đa 10 thông báo gần nhất
- Mỗi thông báo: icon theo type, tiêu đề, message, thời gian
- Thông báo chưa đọc có nền khác biệt
- Nút "Đánh dấu tất cả đã đọc"
- Link "Xem tất cả thông báo"

#### 3.6.13.3. Floating Chat Widget

`FloatingChatWidget` là nút chat nổi góc phải màn hình (icon chat cam). Chức năng tích hợp chat cho toàn bộ các trang trong `PublicLayout`.

> **Lưu ý:** `FloatingChatWidget` đã được import và đặt trong layout, nhưng giao diện mở rộng (expanded chat panel) **đang trong quá trình hoàn thiện**. Chức năng chat đầy đủ hiện tại chỉ có tại `/chat-test`.

---

*Kết thúc phần 3.6.*
