# VIDEO DEMO SCRIPT — Hệ thống Ecommerce

> Script quay video demo toàn bộ hệ thống.
> Tổng thời lượng ước tính: **12–15 phút**
> Quay một lần liên tục theo VIDEO_FINAL_FLOW.md

---

## CHUẨN BỊ TRƯỚC KHI QUAY

### Tài khoản cần chuẩn bị sẵn

| Tài khoản | Email | Role | Mục đích |
|---|---|---|---|
| Customer | buyer@test.com | ROLE_USER | Quay luồng mua hàng |
| Vendor | vendor@test.com | ROLE_VENDOR (shop ACTIVE) | Quay luồng bán hàng |
| Admin | admin@test.com | ROLE_ADMIN | Quay luồng quản trị |

### Dữ liệu cần seede trước

- **Sản phẩm:** ≥10 sản phẩm ACTIVE với ảnh Cloudinary và ≥2 biến thể/sản phẩm
- **Danh mục:** ≥3 danh mục (Thời trang, Điện tử, Phụ kiện)
- **Voucher:** 1 voucher SHOP 20% có hiệu lực + 1 voucher SYSTEM 50k
- **Shop PENDING:** 1 shop chưa duyệt (dùng cho phần Admin)
- **Đơn hàng cũ:** ≥2 đơn DELIVERED (dùng để demo review)
- **Reviews:** ≥2 review có sẵn trên 1 sản phẩm (dùng demo rating)

### Browser setup

- Browser: Chrome, cửa sổ 1440×900
- Xóa cache trước khi quay
- Mở sẵn 3 tab đặt tên: [Customer] [Vendor] [Admin]
- Bật âm thanh nếu có thuyết minh

---

## PHẦN 1 — CUSTOMER (0:00–7:00)

### SCENE 1.1 — Đăng nhập (0:00–0:45)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 0:00 → 0:45 |
| **Màn hình bắt đầu** | `http://localhost:5173/login` (chưa đăng nhập) |
| **Action** | Nhập email + password → nhấn "Đăng nhập" |
| **API được gọi** | `POST /api/v1/auth/login` → trả về JWT token |
| **Business flow** | JWT lưu vào `localStorage` → `AuthContext` cập nhật → redirect về `/products` |
| **Kết quả cần thấy** | Header hiện tên người dùng + avatar + icon giỏ hàng + icon chuông |
| **Thuyết minh** | "Hệ thống sử dụng JWT để xác thực. Sau đăng nhập, token được lưu client-side và gửi kèm mọi request có xác thực." |
| **Feature chứng minh** | Authentication, JWT, AuthContext |
| **Screenshot từ video** | F_LOGIN — Form đăng nhập + trạng thái sau đăng nhập |

---

### SCENE 1.2 — Browse sản phẩm + Tìm kiếm (0:45–1:45)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 0:45 → 1:45 |
| **Màn hình bắt đầu** | `/products` — trang danh sách sản phẩm |
| **Action** | 1. Nhìn overview trang danh sách (grid sản phẩm)<br>2. Nhấn vào 1 danh mục để filter<br>3. Nhập từ khóa vào ô tìm kiếm → nhấn Enter |
| **API được gọi** | `GET /api/v1/products?categoryId=X` → `GET /api/v1/products?keyword=áo` |
| **Business flow** | Query params thay đổi → React re-fetch → grid cập nhật |
| **Kết quả cần thấy** | Grid sản phẩm filter theo danh mục → grid thu hẹp khi search |
| **Thuyết minh** | "Customer có thể duyệt sản phẩm theo danh mục và tìm kiếm theo từ khóa." |
| **Feature chứng minh** | Product listing, Category filter, Keyword search |
| **Screenshot từ video** | F01 — Product list với filter active |

---

### SCENE 1.3 — Chi tiết sản phẩm + Chọn biến thể (1:45–2:45)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 1:45 → 2:45 |
| **Màn hình bắt đầu** | Nhấn vào 1 sản phẩm từ grid → `/products/:id` |
| **Action** | 1. Xem gallery ảnh (click thumbnail đổi ảnh chính)<br>2. Chọn biến thể (màu/size) — thấy giá/tồn kho cập nhật<br>3. Chỉnh số lượng lên 2<br>4. Nhấn "Thêm vào giỏ hàng" |
| **API được gọi** | `GET /api/v1/products/:id` → `POST /api/v1/carts/items` |
| **Business flow** | CartContext cập nhật → badge số lượng trên header tăng lên |
| **Kết quả cần thấy** | Biến thể đổi → tồn kho thay đổi → badge giỏ hàng tăng sau khi thêm |
| **Thuyết minh** | "Mỗi sản phẩm có nhiều biến thể (màu sắc, kích thước). Khi thêm vào giỏ, hệ thống ghi nhận đúng biến thể đã chọn." |
| **Feature chứng minh** | Product detail, ProductVariant, Add to Cart |
| **Screenshot từ video** | F02 — Chi tiết sản phẩm với biến thể đã chọn |

---

### SCENE 1.4 — Giỏ hàng đa gian hàng (2:45–3:30)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 2:45 → 3:30 |
| **Màn hình bắt đầu** | Quay lại `/products` → thêm 1 sản phẩm khác từ **shop khác** → vào `/cart` |
| **Action** | 1. (Đã có sản phẩm shop A từ scene 1.3)<br>2. Thêm 1 sản phẩm từ shop B<br>3. Click icon giỏ hàng → `/cart`<br>4. Tăng số lượng 1 item<br>5. Xem tổng tiền cập nhật |
| **API được gọi** | `PUT /api/v1/carts/items/:id` → `GET /api/v1/carts` |
| **Business flow** | Giỏ hàng chứa items từ 2 shop → checkout sẽ tạo 2 đơn riêng biệt |
| **Kết quả cần thấy** | Giỏ hàng hiển thị items từ 2 shop, tổng tiền cập nhật khi đổi số lượng |
| **Thuyết minh** | "Hệ thống hỗ trợ multi-shop cart. Khi checkout, mỗi gian hàng sẽ nhận một đơn hàng riêng." |
| **Feature chứng minh** | Cart, Multi-shop cart, CartContext |
| **Screenshot từ video** | F03 — Giỏ hàng multi-shop |

---

### SCENE 1.5 — Checkout + Voucher + Chọn thanh toán (3:30–5:00)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 3:30 → 5:00 |
| **Màn hình bắt đầu** | Nhấn "Tiến hành đặt hàng" → `/checkout` |
| **Action** | 1. Điền địa chỉ giao hàng<br>2. Nhập mã voucher → nhấn "Áp dụng" → thấy số tiền giảm<br>3. Xem tổng tiền sau giảm giá<br>4. Chọn "Thanh toán VNPay"<br>5. Nhấn "Đặt hàng" |
| **API được gọi** | `POST /api/v1/orders/preview` → `POST /api/v1/orders/checkout` → `POST /api/v1/payments/create` → nhận `paymentUrl` |
| **Business flow** | N orders tạo (1 per shop) + 1 `payment_group_id` UUID → redirect sang VNPay |
| **Kết quả cần thấy** | Số tiền giảm hiển thị → tổng mới thấp hơn → browser redirect sang VNPay sandbox |
| **Thuyết minh** | "Hệ thống hỗ trợ mã giảm giá theo shop và toàn hệ thống. Khi xác nhận, các đơn hàng được tạo riêng cho từng gian hàng." |
| **Feature chứng minh** | Checkout, Voucher validation, Multi-shop order creation, VNPay redirect |
| **Screenshot từ video** | F04 — Checkout với voucher đã áp dụng |

---

### SCENE 1.6 — VNPay Payment (5:00–6:00)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 5:00 → 6:00 |
| **Màn hình bắt đầu** | Trang VNPay Sandbox (sau khi redirect) |
| **Action** | 1. Hiển thị trang VNPay với thông tin giao dịch (số tiền, mã giao dịch)<br>2. Nhập thông tin thẻ sandbox<br>3. Nhấn xác nhận thanh toán<br>4. VNPay redirect về `http://localhost:5173/payment-result?vnp_ResponseCode=00&...` |
| **API được gọi** | Frontend gọi `GET /api/v1/payments/vnpay/ipn?{params}` (manual IPN sync) → polling `GET /api/v1/payments/group/:id/status` |
| **Business flow** | IPN handler verify HMAC-SHA512 signature → update payment status → emit OrderPaidEvent → orders CONFIRMED |
| **Kết quả cần thấy** | Trang VNPay → quay về → spinner "Đang xác nhận" → SUCCESS với icon tích xanh |
| **Thuyết minh** | "Sau khi thanh toán, hệ thống xác minh chữ ký HMAC từ VNPay, cập nhật trạng thái đơn hàng và thông báo cho Vendor." |
| **Feature chứng minh** | VNPay payment, IPN verification, HMAC-SHA512, Payment polling |
| **Screenshot từ video** | F06 — VNPay sandbox page; F05 — Payment success |

---

### SCENE 1.7 — Xem đơn hàng + Viết đánh giá (6:00–7:00)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 6:00 → 7:00 |
| **Màn hình bắt đầu** | Nhấn "Xem Đơn Hàng" → `/orders` |
| **Action** | 1. Thấy đơn vừa tạo với trạng thái CONFIRMED<br>2. Nhấn vào đơn cũ đã DELIVERED (chuẩn bị sẵn)<br>3. Nhấn "Viết đánh giá" trên 1 sản phẩm<br>4. Modal mở → chọn 5 sao → nhập nội dung → nhấn Gửi<br>5. Quay lại trang sản phẩm xem review vừa tạo |
| **API được gọi** | `GET /api/v1/orders` → `GET /api/v1/orders/:id` → `POST /api/v1/reviews` |
| **Business flow** | Review tạo thành công → hiện trong `ProductReviewSection` → rating average cập nhật |
| **Kết quả cần thấy** | Review của mình hiển thị trên trang sản phẩm với sao và nội dung |
| **Thuyết minh** | "Sau khi nhận hàng (DELIVERED), Customer có thể viết đánh giá. Đánh giá chỉ được mở khi đơn hàng đã hoàn thành." |
| **Feature chứng minh** | Order tracking, Review gating (chỉ DELIVERED), Create review |
| **Screenshot từ video** | F07 — Order list; F08 — Review section |

---

## PHẦN 2 — VENDOR (7:00–10:30)

### SCENE 2.1 — Đăng nhập Vendor + VendorNav (7:00–7:30)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 7:00 → 7:30 |
| **Màn hình bắt đầu** | Tab [Vendor] — `/login` |
| **Action** | Đăng nhập Vendor → navigate tự động đến `/vendor/products` |
| **API được gọi** | `POST /api/v1/auth/login` → `GET /api/v1/shops/my-shop` (RequireShop check) |
| **Business flow** | RequireShop xác nhận shop ACTIVE → render VendorNav + product list |
| **Kết quả cần thấy** | VendorNav với tên shop + badge "Đang hoạt động" + 3 tabs |
| **Thuyết minh** | "Giao diện Vendor có layout riêng với VendorNav hiển thị thông tin gian hàng." |
| **Feature chứng minh** | Vendor login, RequireShop gate, VendorNav |
| **Screenshot từ video** | VS03 — VendorNav |

---

### SCENE 2.2 — Tạo sản phẩm mới (7:30–8:45)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 7:30 → 8:45 |
| **Màn hình bắt đầu** | Nhấn "+ Thêm Sản Phẩm Mới" → `/vendor/products/new` |
| **Action** | 1. Điền: Tên sản phẩm, Mô tả, Giá, Danh mục<br>2. Upload 2 ảnh → thấy spinner upload → ảnh hiện trong grid<br>3. Thêm biến thể 1: SKU=SP001, Giá=150000, Size=M, Color=Đỏ<br>4. Thêm biến thể 2: SKU=SP002, Giá=160000, Size=L, Color=Xanh<br>5. Nhấn "Lưu Sản Phẩm" |
| **API được gọi** | `POST /api/v1/media/upload` (×2) → `POST /api/v1/vendor/products` |
| **Business flow** | Ảnh upload lên Cloudinary → URL trả về → lưu cùng product → product xuất hiện trên public catalog |
| **Kết quả cần thấy** | Ảnh upload thành công (URL Cloudinary), biến thể trong bảng, save thành công → redirect về danh sách |
| **Thuyết minh** | "Vendor tạo sản phẩm với ảnh lưu trên Cloudinary CDN. Biến thể sản phẩm cho phép mô tả màu sắc, kích thước dưới dạng JSON attributes." |
| **Feature chứng minh** | Product creation, Cloudinary image upload, Product variants, JSON attributes |
| **Screenshot từ video** | V01 — Product form với ảnh và biến thể |

---

### SCENE 2.3 — Quản lý đơn hàng Vendor (8:45–9:45)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 8:45 → 9:45 |
| **Màn hình bắt đầu** | Nhấn tab "Quản Lý Đơn Hàng" → `/vendor/orders` |
| **Action** | 1. Xem tab "Tất cả" — đơn hàng với các màu trạng thái<br>2. Chọn tab "Chờ xác nhận" (CONFIRMED) — thấy đơn vừa tạo từ Phần 1<br>3. Nhấn "Giao cho ĐVVC" → toast "Cập nhật thành công"<br>4. Đơn chuyển sang SHIPPED<br>5. (Optional) Nhấn "Hủy đơn" trên đơn khác → modal nhập lý do → xác nhận |
| **API được gọi** | `GET /api/v1/vendor/orders?status=CONFIRMED` → `PATCH /api/v1/vendor/orders/:id/status {status: "SHIPPED"}` |
| **Business flow** | Status cập nhật → notification gửi đến Customer → đơn chuyển tab |
| **Kết quả cần thấy** | Toast xanh "Cập nhật trạng thái thành công!" → đơn biến khỏi tab CONFIRMED, xuất hiện ở tab SHIPPED |
| **Thuyết minh** | "Vendor cập nhật trạng thái đơn hàng theo quy trình: Xác nhận → Giao hàng → Hoàn thành. Mỗi thay đổi tự động gửi thông báo đến người mua." |
| **Feature chứng minh** | Vendor order management, Status transition, Toast notification |
| **Screenshot từ video** | V02 — Order với nút action; VS12 — Toast notification |

---

### SCENE 2.4 — Tạo Voucher (9:45–10:30)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 9:45 → 10:30 |
| **Màn hình bắt đầu** | Nhấn tab "Quản Lý Voucher" → `/vendor/vouchers` |
| **Action** | 1. Xem danh sách voucher hiện có<br>2. Nhấn "+ Tạo Voucher Mới" → modal mở<br>3. Nhập: Mã = SALE20, Loại = PERCENTAGE, Giá trị = 20, Đơn tối thiểu = 200000, Bắt đầu → Kết thúc<br>4. Nhấn "Tạo Voucher" |
| **API được gọi** | `POST /api/v1/vouchers` với `scope=SHOP` |
| **Business flow** | Voucher tạo thuộc shop → Customer áp dụng khi checkout từ shop này |
| **Kết quả cần thấy** | Modal đóng → voucher mới xuất hiện trong bảng với thông tin đầy đủ |
| **Thuyết minh** | "Vendor tạo mã giảm giá riêng cho gian hàng. Customer nhập mã khi checkout để được giảm giá." |
| **Feature chứng minh** | Vendor voucher creation, Scope=SHOP |
| **Screenshot từ video** | V04 — Voucher modal |

---

## PHẦN 3 — ADMIN (10:30–12:30)

### SCENE 3.1 — Đăng nhập Admin + Layout (10:30–11:00)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 10:30 → 11:00 |
| **Màn hình bắt đầu** | Tab [Admin] — `/login` |
| **Action** | Đăng nhập Admin → redirect về `/admin` |
| **API được gọi** | `POST /api/v1/auth/login` → JWT với `ROLE_ADMIN` |
| **Business flow** | `ProtectedRoute requireAdmin={true}` check pass → render `AdminLayout` |
| **Kết quả cần thấy** | Sidebar trái với 5 menu items, topbar với search + notification + avatar "AD" |
| **Thuyết minh** | "Admin có giao diện riêng biệt với sidebar điều hướng, tách hoàn toàn khỏi giao diện Customer và Vendor." |
| **Feature chứng minh** | Admin login, AdminLayout, ProtectedRoute |
| **Screenshot từ video** | A01 — Admin layout |

---

### SCENE 3.2 — Duyệt Gian hàng (11:00–12:00)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 11:00 → 12:00 |
| **Màn hình bắt đầu** | Nhấn "Cửa hàng" trên sidebar → `/admin/shop` |
| **Action** | 1. Filter trạng thái = PENDING<br>2. Thấy shop PENDING (chuẩn bị sẵn)<br>3. Nhấn "Duyệt"<br>4. Trang reload → shop chuyển ACTIVE<br>5. Chuyển filter = ACTIVE để xác nhận |
| **API được gọi** | `GET /api/v1/admin/shops?status=PENDING` → `PUT /api/v1/admin/shops/:id/approve` |
| **Business flow** | `ShopApprovedEvent` → `@TransactionalEventListener(AFTER_COMMIT)` → User nhận ROLE_VENDOR → Email gửi qua Gmail SMTP |
| **Kết quả cần thấy** | Shop PENDING → nhấn Duyệt → badge đổi sang ACTIVE xanh |
| **Thuyết minh** | "Khi Admin duyệt gian hàng, hệ thống tự động cấp quyền ROLE_VENDOR cho chủ shop và gửi email xác nhận qua Gmail SMTP." |
| **Feature chứng minh** | Admin shop approval, Event-driven role assignment, Email notification |
| **Screenshot từ video** | AS03 — Pending shop; AS05 — Active shop |

---

### SCENE 3.3 — Quản lý Đơn hàng Admin + Tạo Voucher Hệ thống (12:00–12:30)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 12:00 → 12:30 |
| **Màn hình bắt đầu** | Nhấn "Đơn hàng" → `/admin/orders` |
| **Action** | 1. Xem danh sách đơn toàn hệ thống — nhiều shop, nhiều user<br>2. Nhấn vào 1 đơn → xem chi tiết<br>3. Quay lại → nhấn "Voucher" → `/admin/vouchers`<br>4. Tạo voucher SYSTEM: FREESHIP, FREE_SHIPPING |
| **API được gọi** | `GET /api/v1/admin/orders` → `GET /api/v1/admin/orders/:id` → `POST /api/v1/vouchers` (scope=SYSTEM) |
| **Business flow** | Admin xem toàn hệ thống không giới hạn shop; voucher SYSTEM áp dụng cho mọi shop |
| **Kết quả cần thấy** | Bảng đơn hàng từ nhiều shop; chi tiết đơn với snapshot giá; voucher mới trong danh sách |
| **Thuyết minh** | "Admin có quyền xem toàn bộ đơn hàng hệ thống và tạo voucher áp dụng cho tất cả gian hàng." |
| **Feature chứng minh** | Admin order overview, System voucher (scope=SYSTEM), FREE_SHIPPING type |
| **Screenshot từ video** | A01 — Admin orders; AS12 — System voucher modal |

---

## PHẦN 4 — REALTIME: CHAT WEBSOCKET (12:30–14:30)

> Phần này chứng minh WebSocket STOMP hoạt động hai chiều.
> Cần 2 browser session: 1 tab Buyer + 1 tab Vendor.

### SCENE 4.1 — Kết nối STOMP + Tạo phòng chat (12:30–13:15)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 12:30 → 13:15 |
| **Màn hình bắt đầu** | Tab [Customer] → `/chat-test` |
| **Action** | 1. Vào /chat-test → thấy kết nối STOMP tự động<br>2. STOMP log hiển thị "CONNECTED" màu xanh lá<br>3. Nhập Shop ID của shop Vendor đang dùng<br>4. Nhấn "Vào Chat" → phòng chat tạo/mở |
| **API được gọi** | `WebSocket /ws` handshake → `POST /api/v1/chat/rooms` → `GET /api/v1/chat/rooms` |
| **Business flow** | JWT xác thực trong STOMP CONNECT → `JwtChannelInterceptor` → Room tạo với `(buyerId, shopId)` pair |
| **Kết quả cần thấy** | STOMP log: "CONNECTED" + "SUBSCRIBE to /user/queue/messages"; Phòng chat xuất hiện ở cột trái |
| **Thuyết minh** | "Kết nối WebSocket sử dụng STOMP protocol với xác thực JWT. Mỗi cuộc hội thoại tạo một phòng chat riêng giữa Buyer và Shop." |
| **Feature chứng minh** | WebSocket STOMP, JWT auth over WebSocket, ChatRoom creation |
| **Screenshot từ video** | R01 (một phần) — STOMP connected log |

---

### SCENE 4.2 — Chat hai chiều realtime (13:15–14:30)

| Trường | Nội dung |
|---|---|
| **Timestamp** | 13:15 → 14:30 |
| **Màn hình bắt đầu** | Split screen: Tab [Customer] bên trái + Tab [Vendor] bên phải (cả 2 tại `/chat-test`) |
| **Action** | 1. Vendor mở chat-test → load phòng chat đã tạo từ Buyer → chọn phòng<br>2. Buyer gõ "Xin chào, sản phẩm còn hàng không?" → nhấn Gửi<br>3. **Thấy ngay:** Tin hiện bên Buyer (bubble cam) + đồng thời hiện bên Vendor (bubble trắng)<br>4. Vendor trả lời "Dạ còn hàng, bạn muốn size nào?" → Gửi<br>5. **Thấy ngay:** Tin hiện bên Vendor + đồng thời hiện bên Buyer<br>6. Gửi thêm 2–3 tin nhắn qua lại |
| **Protocol** | `SEND /app/chat.send {roomId, content}` → Server → `SEND /user/queue/messages` đến cả 2 user |
| **Business flow** | Message gửi → lưu DB → STOMP broadcast → cả 2 subscriber nhận realtime |
| **Kết quả cần thấy** | Tin nhắn xuất hiện đồng thời ở cả 2 tab KHÔNG CẦN reload; STOMP log hiện "MESSAGE IN" màu vàng |
| **Thuyết minh** | "Chat realtime sử dụng WebSocket STOMP. Tin nhắn được gửi và nhận tức thì ở cả hai phía mà không cần tải lại trang." |
| **Feature chứng minh** | Realtime chat, STOMP message broker, Two-way communication |
| **Screenshot từ video** | R01 — Chat hai chiều với bubble messages + STOMP log |

---

## PHẦN 5 — NOTIFICATIONS: Luồng thông báo tự động (Optional, 14:30–15:00)

### SCENE 5.1 — Vendor nhận thông báo đơn hàng mới

| Trường | Nội dung |
|---|---|
| **Timestamp** | 14:30 → 15:00 (optional) |
| **Màn hình bắt đầu** | Tab [Vendor] → bất kỳ trang nào có header |
| **Action** | 1. Customer (tab khác) tạo đơn hàng từ shop Vendor<br>2. Xem tab Vendor → icon chuông có badge đỏ tăng lên<br>3. Click icon chuông → dropdown mở → thấy "Bạn có đơn hàng mới #..." |
| **Business flow** | `OrderCreatedEvent` → `@TransactionalEventListener(AFTER_COMMIT)` → tạo Notification → API polling cập nhật count |
| **Kết quả cần thấy** | Badge đỏ trên chuông → dropdown có thông báo NEW_ORDER |
| **Thuyết minh** | "Hệ thống thông báo dựa trên event-driven — thông báo chỉ gửi sau khi transaction commit thành công, đảm bảo tính nhất quán dữ liệu." |
| **Feature chứng minh** | Event-driven notification, @TransactionalEventListener, Notification dropdown |
| **Screenshot từ video** | F09, VS17 — Notification dropdown |

---

## TỔNG KẾT VIDEO

| Phần | Nội dung | Thời lượng |
|---|---|---|
| Phần 1 — Customer | Login → Browse → Detail → Cart → Checkout → VNPay → Result → Review | 7:00 |
| Phần 2 — Vendor | Login → Create Product → Orders → Voucher | 3:30 |
| Phần 3 — Admin | Login → Shop Approval → Orders → System Voucher | 2:00 |
| Phần 4 — Realtime | STOMP Connect → Two-way Chat | 2:00 |
| Phần 5 — Notification | Event-driven notification (optional) | 0:30 |
| **TỔNG** | | **~15 phút** |

### Screenshot có thể lấy từ video (map)

| Screenshot ID | Lấy từ Scene | Timestamp |
|---|---|---|
| F01 | 1.2 | ~1:00 |
| F02 | 1.3 | ~2:15 |
| F03 | 1.4 | ~3:10 |
| F04 | 1.5 | ~4:30 |
| F05 | 1.6 | ~5:50 |
| F06 | 1.6 | ~5:10 |
| F07 | 1.7 | ~6:05 |
| F08 | 1.7 | ~6:45 |
| V01 | 2.2 | ~8:30 |
| V02 | 2.3 | ~9:00 |
| V04 | 2.4 | ~10:15 |
| A01 | 3.1 | ~10:45 |
| AS03 | 3.2 | ~11:10 |
| AS05 | 3.2 | ~11:35 |
| R01 | 4.2 | ~13:30 |
| F09/VS17 | 5.1 | ~14:45 |
