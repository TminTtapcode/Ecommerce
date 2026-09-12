# FINAL SCREENSHOT CHECKLIST

> Danh sách ảnh chụp màn hình **ưu tiên cao nhất** để chứng minh hệ thống chạy thực tế.
> Đây là bộ tuyển chọn — không cần chụp mọi page.
> Mục tiêu: Chứng minh luồng nghiệp vụ hoàn chỉnh từ đầu đến cuối.

---

## Nguyên tắc chọn ảnh

1. **Ưu tiên ảnh chứng minh luồng nghiệp vụ** — không phải ảnh trang tĩnh
2. **Ưu tiên ảnh có dữ liệu thực tế** — không phải trang rỗng
3. **Chứng minh tính năng đặc thù** — multi-shop checkout, STOMP chat, IPN payment
4. **Mỗi actor có ảnh đại diện** — Customer, Vendor, Admin

---

## NHÓM 1 — LUỒNG CUSTOMER (Ưu tiên cao nhất)

### F01 — Trang danh sách sản phẩm

| Trường | Nội dung |
|---|---|
| **ID** | F01 |
| **Tên** | Trang danh sách sản phẩm với filter danh mục |
| **Mục đích** | Chứng minh hệ thống có dữ liệu thực, giao diện tìm kiếm hoạt động |
| **URL** | http://localhost:5173/products |
| **Yêu cầu dữ liệu** | ≥12 sản phẩm, ≥3 danh mục |
| **Bước** | 1. Mở URL khi đã đăng nhập<br>2. Chọn 1 danh mục để filter<br>3. Chụp grid sản phẩm đã filter |
| **Caption** | "Trang danh sách sản phẩm với bộ lọc danh mục" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F02 — Chi tiết sản phẩm với biến thể

| Trường | Nội dung |
|---|---|
| **ID** | F02 |
| **Tên** | Chi tiết sản phẩm — chọn biến thể |
| **Mục đích** | Chứng minh gallery ảnh, biến thể (màu/size), tồn kho |
| **URL** | http://localhost:5173/products/{id} |
| **Yêu cầu dữ liệu** | Sản phẩm có ≥2 ảnh và ≥2 biến thể |
| **Bước** | 1. Vào trang chi tiết sản phẩm<br>2. Click vào một biến thể (màu/size)<br>3. Chụp phần trên trang: gallery + thông tin + biến thể đã chọn |
| **Caption** | "Trang chi tiết sản phẩm với gallery ảnh và lựa chọn biến thể" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F03 — Giỏ hàng đa gian hàng

| Trường | Nội dung |
|---|---|
| **ID** | F03 |
| **Tên** | Giỏ hàng với sản phẩm từ nhiều shop |
| **Mục đích** | Chứng minh multi-shop cart — tính năng đặc thù của hệ thống |
| **URL** | http://localhost:5173/cart |
| **Yêu cầu dữ liệu** | ≥3 sản phẩm từ ≥2 gian hàng khác nhau trong giỏ |
| **Bước** | 1. Thêm sản phẩm từ shop A vào giỏ<br>2. Thêm sản phẩm từ shop B vào giỏ<br>3. Vào /cart<br>4. Chụp giỏ hàng với tổng tiền |
| **Caption** | "Giỏ hàng với sản phẩm từ nhiều gian hàng khác nhau" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F04 — Trang Checkout với Voucher

| Trường | Nội dung |
|---|---|
| **ID** | F04 |
| **Tên** | Trang đặt hàng với mã giảm giá đã áp dụng |
| **Mục đích** | Chứng minh checkout, voucher preview, và chọn phương thức TT |
| **URL** | http://localhost:5173/checkout |
| **Yêu cầu dữ liệu** | Có voucher hợp lệ trong DB |
| **Bước** | 1. Từ giỏ hàng → vào checkout<br>2. Điền địa chỉ giao hàng<br>3. Nhập mã voucher → nhấn "Áp dụng" → thấy số tiền giảm<br>4. Chụp trang với voucher đã áp dụng + chọn VNPAY |
| **Caption** | "Trang xác nhận đặt hàng với mã giảm giá và thanh toán VNPay" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F05 — Kết quả thanh toán thành công

| Trường | Nội dung |
|---|---|
| **ID** | F05 |
| **Tên** | Màn hình thanh toán thành công |
| **Mục đích** | Chứng minh luồng payment hoàn thành — payment result page |
| **URL** | http://localhost:5173/payment-result |
| **Bước** | 1. Hoàn tất thanh toán COD (đơn giản hơn)<br>2. Chụp màn hình SUCCESS với icon tích xanh |
| **Caption** | "Xác nhận thanh toán thành công" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F06 — VNPay Sandbox Payment Gateway

| Trường | Nội dung |
|---|---|
| **ID** | F06 |
| **Tên** | Trang thanh toán VNPay Sandbox |
| **Mục đích** | Chứng minh tích hợp cổng thanh toán VNPay thực tế |
| **URL** | https://sandbox.vnpayment.vn/... (sau khi redirect) |
| **Bước** | 1. Chọn VNPAY → đặt hàng<br>2. Browser redirect sang VNPay sandbox<br>3. Chụp trang VNPay với thông tin đơn hàng (giá, mã đơn) |
| **Caption** | "Cổng thanh toán VNPay Sandbox với thông tin giao dịch" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### F07 — Lịch sử đơn hàng với nhiều trạng thái

| Trường | Nội dung |
|---|---|
| **ID** | F07 |
| **Tên** | Lịch sử đơn hàng — tab Tất cả |
| **Mục đích** | Chứng minh tracking đơn hàng với màu sắc trạng thái |
| **URL** | http://localhost:5173/orders |
| **Yêu cầu dữ liệu** | ≥4 đơn ở các trạng thái PENDING, CONFIRMED, DELIVERED, CANCELLED |
| **Bước** | Chụp tab "Tất cả" với đơn đa trạng thái |
| **Caption** | "Danh sách đơn hàng với các trạng thái khác nhau" |
| **Ưu tiên** | ⭐⭐ Trung bình |

---

### F08 — Đánh giá sản phẩm

| Trường | Nội dung |
|---|---|
| **ID** | F08 |
| **Tên** | Trang chi tiết sản phẩm — phần đánh giá |
| **Mục đích** | Chứng minh hệ thống review với rating histogram |
| **URL** | http://localhost:5173/products/{id}#reviews |
| **Yêu cầu dữ liệu** | Sản phẩm có ≥3 đánh giá, ít nhất 1 có vendor reply |
| **Bước** | 1. Scroll xuống phần reviews<br>2. Chụp: điểm trung bình + histogram + 2-3 reviews (1 có vendor reply) |
| **Caption** | "Phần đánh giá sản phẩm với rating và phản hồi từ người bán" |
| **Ưu tiên** | ⭐⭐ Trung bình |

---

### F09 — Notification Dropdown

| Trường | Nội dung |
|---|---|
| **ID** | F09 |
| **Tên** | Dropdown thông báo với thông báo thực |
| **Mục đích** | Chứng minh hệ thống notification event-driven |
| **URL** | Bất kỳ trang nào có header |
| **Yêu cầu dữ liệu** | ≥2 thông báo (NEW_ORDER, ORDER_STATUS_CHANGED) |
| **Bước** | Nhấn icon chuông → dropdown hiển thị thông báo |
| **Caption** | "Hệ thống thông báo realtime" |
| **Ưu tiên** | ⭐⭐ Trung bình |

---

## NHÓM 2 — LUỒNG VENDOR

### V01 — Form tạo sản phẩm hoàn chỉnh

| Trường | Nội dung |
|---|---|
| **ID** | V01 |
| **Tên** | Form tạo sản phẩm với ảnh đã upload và biến thể |
| **Mục đích** | Chứng minh Vendor tạo được sản phẩm đầy đủ (ảnh Cloudinary + variants) |
| **URL** | http://localhost:5173/vendor/products/new |
| **Bước** | 1. Điền form: tên, mô tả, giá, danh mục<br>2. Upload ≥2 ảnh (đã lên Cloudinary)<br>3. Thêm ≥2 biến thể<br>4. Chụp form đầy đủ chưa submit |
| **Caption** | "Form tạo sản phẩm với ảnh Cloudinary và biến thể" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### V02 — Quản lý đơn hàng Vendor — Xử lý đơn

| Trường | Nội dung |
|---|---|
| **ID** | V02 |
| **Tên** | Vendor xử lý đơn hàng CONFIRMED |
| **Mục đích** | Chứng minh Vendor có thể thay đổi trạng thái đơn hàng |
| **URL** | http://localhost:5173/vendor/orders |
| **Yêu cầu dữ liệu** | Có đơn CONFIRMED (đã thanh toán) |
| **Bước** | 1. Vào /vendor/orders<br>2. Chụp đơn CONFIRMED với nút "Giao cho ĐVVC" và "Hủy đơn" |
| **Caption** | "Vendor xử lý đơn hàng — chuyển trạng thái giao hàng" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### V03 — Admin duyệt Gian hàng

| Trường | Nội dung |
|---|---|
| **ID** | V03 |
| **Tên** | Admin duyệt shop PENDING |
| **Mục đích** | Chứng minh luồng Shop Approval với event email |
| **URL** | http://localhost:5173/admin/shop |
| **Yêu cầu dữ liệu** | Có shop PENDING |
| **Bước** | Chụp shop PENDING với nút "Duyệt" + "Từ chối" |
| **Caption** | "Admin phê duyệt gian hàng mới đăng ký" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### V04 — Vendor tạo Voucher

| Trường | Nội dung |
|---|---|
| **ID** | V04 |
| **Tên** | Vendor tạo mã giảm giá — Modal form |
| **Mục đích** | Chứng minh Vendor quản lý được voucher của shop |
| **URL** | http://localhost:5173/vendor/vouchers |
| **Bước** | Mở modal tạo voucher → điền PERCENTAGE 20% → chụp modal đầy đủ |
| **Caption** | "Vendor tạo mã giảm giá cho gian hàng" |
| **Ưu tiên** | ⭐⭐ Trung bình |

---

## NHÓM 3 — ADMIN

### A01 — Admin Layout với sidebar

| Trường | Nội dung |
|---|---|
| **ID** | A01 |
| **Tên** | Giao diện Admin panel với sidebar đầy đủ |
| **Mục đích** | Chứng minh Admin có layout riêng biệt khác Customer |
| **URL** | http://localhost:5173/admin/orders |
| **Bước** | Chụp trang đơn hàng Admin với sidebar + dữ liệu thực |
| **Caption** | "Giao diện quản trị hệ thống — danh sách đơn hàng" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

### A02 — Admin Shop Management

| Trường | Nội dung |
|---|---|
| **ID** | A02 |
| **Tên** | Danh sách gian hàng với nhiều trạng thái |
| **Mục đích** | Chứng minh Admin quản lý toàn bộ shop |
| **URL** | http://localhost:5173/admin/shop |
| **Bước** | Chụp bảng shop với PENDING, ACTIVE, BANNED cùng lúc |
| **Caption** | "Admin quản lý trạng thái các gian hàng" |
| **Ưu tiên** | ⭐⭐ Trung bình |

---

## NHÓM 4 — REALTIME

### R01 — Chat WebSocket STOMP hoạt động

| Trường | Nội dung |
|---|---|
| **ID** | R01 |
| **Tên** | Chat realtime giữa Buyer và Shop |
| **Mục đích** | Chứng minh WebSocket STOMP hoạt động end-to-end |
| **URL** | http://localhost:5173/chat-test |
| **Bước** | 1. Đăng nhập Buyer<br>2. Vào /chat-test → tạo room với Shop ID<br>3. Mở tab khác với tài khoản Vendor → vào chat-test<br>4. Gửi tin nhắn từ cả 2 bên<br>5. Chụp: tin nhắn bubble 2 chiều + STOMP log "CONNECTED" + "MESSAGE IN" |
| **Caption** | "Chat realtime WebSocket STOMP hoạt động hai chiều" |
| **Ưu tiên** | ⭐⭐⭐ Cao nhất |

---

## Tổng kết và thứ tự chụp khuyến nghị

### Thứ tự ưu tiên

| Thứ tự | ID | Lý do |
|---|---|---|
| 1 | F06 | VNPay page — khó chụp nhất, cần chụp trong quá trình thanh toán |
| 2 | R01 | Chat — cần 2 tab browser cùng lúc |
| 3 | F05 | Payment result — phải chụp ngay sau khi checkout |
| 4 | V01 | Product form với ảnh — cần upload thực tế |
| 5 | F04 | Checkout với voucher |
| 6 | F03 | Cart multi-shop |
| 7 | V02 | Vendor order management |
| 8 | A01 | Admin panel |
| 9 | V03 | Admin duyệt shop |
| 10 | F01 | Product list |
| 11 | F02 | Product detail |
| 12 | F07 | Order history |
| 13 | F08 | Reviews |
| 14 | A02 | Admin shop list |
| 15 | V04 | Vendor voucher |
| 16 | F09 | Notification |

### Tổng số: 16 ảnh — đủ để chứng minh toàn bộ hệ thống

**Bắt buộc (10 ảnh):** F01, F02, F03, F04, F05, F06, V01, V02, V03, A01

**Khuyến nghị thêm (6 ảnh):** R01, F07, F08, F09, A02, V04
