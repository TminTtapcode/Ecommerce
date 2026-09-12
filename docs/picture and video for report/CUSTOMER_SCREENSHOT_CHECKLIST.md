# CUSTOMER SCREENSHOT CHECKLIST

> Danh sách ảnh màn hình cần chụp cho phần 3.6 (Giao diện Customer).
> Frontend đang chạy tại: http://localhost:5173
> Hướng dẫn: Chụp từng màn hình theo thứ tự. Lưu file theo Screenshot ID.

---

## Chuẩn bị trước khi chụp

### Tài khoản cần chuẩn bị
| Tài khoản | Mục đích |
|---|---|
| Buyer (ROLE_USER) | Chụp màn hình customer — giỏ hàng, đặt hàng, đánh giá |
| Buyer đã có đơn DELIVERED | Chụp màn hình viết đánh giá (cần đơn đã giao) |
| Tài khoản chưa đăng nhập | Chụp màn hình login, register, product list (guest) |

### Dữ liệu cần có trong DB
- Ít nhất 8-12 sản phẩm Active với ảnh
- Ít nhất 1 đơn hàng ở trạng thái DELIVERED
- Ít nhất 1-2 đánh giá có sẵn
- Ít nhất 1 thông báo chưa đọc

### Kích thước màn hình khuyến nghị
- Desktop: 1440×900 hoặc 1280×800
- Browser: Chrome với DevTools đóng

---

## SS01 — Trang Đăng nhập

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS01 |
| **Tên** | Màn hình Đăng nhập |
| **Mục đích** | Minh họa giao diện xác thực người dùng |
| **URL** | http://localhost:5173/login |
| **Bước thực hiện** | 1. Mở trình duyệt, truy cập URL trên khi chưa đăng nhập<br>2. Chụp màn hình form đang trống (chưa điền gì) |
| **Trạng thái cần đạt** | Form hiển thị đầy đủ 2 ô Email + Password + nút Đăng nhập + link Đăng ký |
| **Caption đề xuất** | "Hình 3.21. Màn hình đăng nhập hệ thống" |
| **Vị trí trong báo cáo** | Mục 3.6.2 — Màn hình Đăng nhập |

---

## SS02 — Trang Đăng ký

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS02 |
| **Tên** | Màn hình Đăng ký tài khoản |
| **Mục đích** | Minh họa giao diện tạo tài khoản mới |
| **URL** | http://localhost:5173/register |
| **Bước thực hiện** | 1. Truy cập URL khi chưa đăng nhập<br>2. Chụp form đăng ký trống |
| **Trạng thái cần đạt** | Form hiển thị: Họ tên, Email, Mật khẩu, Xác nhận mật khẩu + nút Đăng ký |
| **Caption đề xuất** | "Hình 3.22. Màn hình đăng ký tài khoản mới" |
| **Vị trí trong báo cáo** | Mục 3.6.3 — Màn hình Đăng ký |

---

## SS03 — Danh sách sản phẩm (Guest)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS03 |
| **Tên** | Trang danh sách sản phẩm — Chế độ khách |
| **Mục đích** | Hiển thị layout sản phẩm với header đầy đủ, không đăng nhập |
| **URL** | http://localhost:5173/products |
| **Bước thực hiện** | 1. Mở URL khi chưa đăng nhập<br>2. Đảm bảo có ít nhất 8 sản phẩm hiển thị<br>3. Chụp toàn bộ trang (scroll lên đầu trang trước khi chụp) |
| **Trạng thái cần đạt** | Grid sản phẩm hiển thị ≥8 sản phẩm, header với logo + ô tìm kiếm, nút Đăng nhập ở góc phải |
| **Caption đề xuất** | "Hình 3.23. Trang danh sách sản phẩm (chế độ khách)" |
| **Vị trí trong báo cáo** | Mục 3.6.4 — Danh sách sản phẩm |

---

## SS04 — Tìm kiếm sản phẩm

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS04 |
| **Tên** | Kết quả tìm kiếm sản phẩm |
| **Mục đích** | Minh họa chức năng tìm kiếm |
| **URL** | http://localhost:5173/products?keyword=áo (hoặc từ khóa phù hợp) |
| **Bước thực hiện** | 1. Nhập từ khóa vào ô tìm kiếm trên header<br>2. Nhấn Enter hoặc click tìm<br>3. Chụp kết quả hiển thị |
| **Trạng thái cần đạt** | Danh sách sản phẩm thu hẹp theo từ khóa, ô tìm kiếm có từ khóa |
| **Caption đề xuất** | "Hình 3.24. Kết quả tìm kiếm sản phẩm" |
| **Vị trí trong báo cáo** | Mục 3.6.4 — Danh sách sản phẩm |

---

## SS05 — Chi tiết sản phẩm

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS05 |
| **Tên** | Trang chi tiết sản phẩm |
| **Mục đích** | Hiển thị thông tin đầy đủ sản phẩm: gallery ảnh, thông tin, chọn biến thể |
| **URL** | http://localhost:5173/products/{id} |
| **Bước thực hiện** | 1. Nhấn vào một sản phẩm từ trang list<br>2. Chụp phần trên màn hình (ảnh + thông tin + biến thể + nút thêm vào giỏ) |
| **Trạng thái cần đạt** | Ảnh sản phẩm hiển thị, giá, các button biến thể, nút "Thêm vào giỏ hàng" màu cam |
| **Caption đề xuất** | "Hình 3.25. Trang chi tiết sản phẩm" |
| **Vị trí trong báo cáo** | Mục 3.6.5 — Chi tiết sản phẩm |

---

## SS06 — Phần đánh giá sản phẩm

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS06 |
| **Tên** | Phần đánh giá sản phẩm |
| **Mục đích** | Hiển thị rating tổng, histogram sao, danh sách review |
| **URL** | http://localhost:5173/products/{id}#reviews |
| **Bước thực hiện** | 1. Vào trang chi tiết sản phẩm có ≥2 đánh giá<br>2. Scroll xuống phần đánh giá<br>3. Chụp phần rating summary + danh sách review |
| **Trạng thái cần đạt** | Điểm trung bình, histogram thanh sao, ít nhất 2 review hiển thị |
| **Caption đề xuất** | "Hình 3.26. Phần đánh giá sản phẩm với rating và histogram" |
| **Vị trí trong báo cáo** | Mục 3.6.5 — Chi tiết sản phẩm |

---

## SS07 — Modal viết đánh giá

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS07 |
| **Tên** | Modal tạo đánh giá sản phẩm |
| **Mục đích** | Minh họa giao diện viết đánh giá (chỉ hiện khi đã nhận hàng) |
| **URL** | Mở từ `/orders/:id` → nhấn "Viết đánh giá" trên đơn DELIVERED |
| **Bước thực hiện** | 1. Đăng nhập với tài khoản có đơn DELIVERED<br>2. Vào /orders → chọn đơn DELIVERED → nhấn "Viết đánh giá"<br>3. Modal mở → chụp modal đang mở với form chọn sao + textarea |
| **Trạng thái cần đạt** | Modal mở, 5 sao chọn được, ô nhập comment, nút Gửi |
| **Caption đề xuất** | "Hình 3.27. Modal viết đánh giá sản phẩm" |
| **Vị trí trong báo cáo** | Mục 3.6.5 — Chi tiết sản phẩm |

---

## SS08 — Giỏ hàng có sản phẩm

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS08 |
| **Tên** | Giỏ hàng có sản phẩm |
| **Mục đích** | Hiển thị giỏ hàng với ít nhất 2 sản phẩm từ các shop khác nhau |
| **URL** | http://localhost:5173/cart |
| **Bước thực hiện** | 1. Đăng nhập<br>2. Thêm 2-3 sản phẩm vào giỏ (từ ít nhất 2 gian hàng khác nhau)<br>3. Vào /cart<br>4. Chụp toàn bộ trang giỏ hàng |
| **Trạng thái cần đạt** | ≥2 CartItem hiển thị với ảnh, tên, số lượng, đơn giá; tổng tiền hiển thị; nút "Đặt hàng" |
| **Caption đề xuất** | "Hình 3.28. Màn hình giỏ hàng" |
| **Vị trí trong báo cáo** | Mục 3.6.6 — Giỏ hàng |

---

## SS09 — Trang thanh toán (Checkout)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS09 |
| **Tên** | Trang xác nhận đặt hàng |
| **Mục đích** | Hiển thị form thanh toán với địa chỉ, voucher, phương thức thanh toán |
| **URL** | http://localhost:5173/checkout |
| **Bước thực hiện** | 1. Có sản phẩm trong giỏ → nhấn "Đặt hàng" từ giỏ hàng → vào /checkout<br>2. Điền địa chỉ giao hàng<br>3. Chụp trang checkout với form đầy đủ, chưa submit |
| **Trạng thái cần đạt** | Form địa chỉ đã điền, ô voucher, 2 option thanh toán (VNPAY/COD), nút Đặt hàng cam |
| **Caption đề xuất** | "Hình 3.29. Màn hình xác nhận đặt hàng" |
| **Vị trí trong báo cáo** | Mục 3.6.7 — Thanh toán |

---

## SS10 — Kết quả thanh toán thành công

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS10 |
| **Tên** | Kết quả thanh toán thành công |
| **Mục đích** | Hiển thị màn hình xác nhận thanh toán thành công |
| **URL** | http://localhost:5173/payment-result |
| **Bước thực hiện** | 1. Thực hiện thanh toán COD (đơn giản hơn VNPAY trong môi trường test)<br>2. Chờ navigate đến /payment-result<br>3. Chụp khi hiển thị icon tích xanh + "Thanh Toán Thành Công!" |
| **Trạng thái cần đạt** | Icon tích xanh, text "Thanh Toán Thành Công!", nút "Xem Đơn Hàng" màu cam |
| **Caption đề xuất** | "Hình 3.30. Màn hình xác nhận thanh toán thành công" |
| **Vị trí trong báo cáo** | Mục 3.6.8 — Kết quả thanh toán |

---

## SS11 — Kết quả thanh toán đang chờ (VNPay)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS11 |
| **Tên** | Màn hình chờ xác nhận thanh toán VNPay |
| **Mục đích** | Hiển thị trạng thái loading/polling khi chờ IPN từ VNPay |
| **URL** | http://localhost:5173/payment-result |
| **Bước thực hiện** | 1. Thực hiện thanh toán VNPay sandbox → hoàn tất payment → VNPay redirect về<br>2. Chụp trong 3 giây đầu khi spinner đang xoay |
| **Trạng thái cần đạt** | Spinner cam xoay, text "Đang xác nhận thanh toán", message đang polling |
| **Caption đề xuất** | "Hình 3.31. Màn hình chờ xác nhận thanh toán VNPay" |
| **Vị trí trong báo cáo** | Mục 3.6.8 — Kết quả thanh toán |

---

## SS12 — Lịch sử đơn hàng (Tab Tất cả)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS12 |
| **Tên** | Danh sách đơn hàng — Tab Tất cả |
| **Mục đích** | Hiển thị danh sách đơn hàng với nhiều trạng thái khác nhau |
| **URL** | http://localhost:5173/orders |
| **Bước thực hiện** | 1. Đăng nhập với tài khoản có ≥3 đơn hàng ở các trạng thái khác nhau<br>2. Chụp tab "Tất cả" đang active |
| **Trạng thái cần đạt** | ≥3 đơn hàng với màu trạng thái khác nhau (vàng/xanh/tím/xanh lá/đỏ) |
| **Caption đề xuất** | "Hình 3.32. Danh sách đơn hàng của người mua" |
| **Vị trí trong báo cáo** | Mục 3.6.9 — Lịch sử đơn hàng |

---

## SS13 — Lịch sử đơn hàng (Tab Hoàn thành)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS13 |
| **Tên** | Danh sách đơn hàng — Tab Hoàn thành |
| **Mục đích** | Minh họa filter trạng thái đơn hàng |
| **URL** | http://localhost:5173/orders (click tab Hoàn thành) |
| **Bước thực hiện** | 1. Nhấn vào tab "Hoàn thành"<br>2. Chụp danh sách đơn DELIVERED |
| **Trạng thái cần đạt** | Chỉ hiển thị đơn DELIVERED, tab "Hoàn thành" active (màu cam) |
| **Caption đề xuất** | "Hình 3.33. Lọc đơn hàng theo trạng thái Hoàn thành" |
| **Vị trí trong báo cáo** | Mục 3.6.9 — Lịch sử đơn hàng |

---

## SS14 — Chi tiết đơn hàng (DELIVERED)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS14 |
| **Tên** | Chi tiết đơn hàng đã hoàn thành |
| **Mục đích** | Hiển thị đầy đủ đơn hàng + nút Viết đánh giá |
| **URL** | http://localhost:5173/orders/{id} (đơn DELIVERED) |
| **Bước thực hiện** | 1. Vào đơn hàng có trạng thái DELIVERED<br>2. Chụp toàn bộ trang: thông tin đơn + danh sách sản phẩm + nút Viết đánh giá |
| **Trạng thái cần đạt** | Trạng thái "Hoàn thành" màu xanh, nút "Viết đánh giá" hiển thị trên từng sản phẩm |
| **Caption đề xuất** | "Hình 3.34. Chi tiết đơn hàng đã hoàn thành" |
| **Vị trí trong báo cáo** | Mục 3.6.10 — Chi tiết đơn hàng |

---

## SS15 — Chi tiết đơn hàng (PENDING hoặc CONFIRMED)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS15 |
| **Tên** | Chi tiết đơn hàng đang xử lý |
| **Mục đích** | Hiển thị trạng thái đơn đang trong quá trình xử lý |
| **URL** | http://localhost:5173/orders/{id} (đơn CONFIRMED hoặc SHIPPED) |
| **Bước thực hiện** | 1. Vào đơn hàng có trạng thái CONFIRMED hoặc SHIPPED<br>2. Chụp trang chi tiết |
| **Trạng thái cần đạt** | Trạng thái hiển thị màu xanh/tím, không có nút Viết đánh giá |
| **Caption đề xuất** | "Hình 3.35. Chi tiết đơn hàng đang xử lý" |
| **Vị trí trong báo cáo** | Mục 3.6.10 — Chi tiết đơn hàng |

---

## SS16 — Hồ sơ cá nhân

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS16 |
| **Tên** | Màn hình hồ sơ cá nhân |
| **Mục đích** | Hiển thị thông tin tài khoản người dùng |
| **URL** | http://localhost:5173/profile |
| **Bước thực hiện** | 1. Đăng nhập → click avatar/menu → chọn "Hồ sơ"<br>2. Chụp trang profile |
| **Trạng thái cần đạt** | Avatar hình tròn cam với ký tự đầu, các ô thông tin: ID, Email, Họ tên, Vai trò |
| **Caption đề xuất** | "Hình 3.36. Màn hình hồ sơ cá nhân" |
| **Vị trí trong báo cáo** | Mục 3.6.11 — Hồ sơ cá nhân |

---

## SS17 — Notification Dropdown

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS17 |
| **Tên** | Dropdown thông báo hệ thống |
| **Mục đích** | Hiển thị danh sách thông báo với badge unread |
| **URL** | http://localhost:5173/products (bất kỳ trang nào có header) |
| **Bước thực hiện** | 1. Đảm bảo tài khoản có ≥2 thông báo (đặt hàng hoặc cập nhật trạng thái)<br>2. Nhấn vào icon chuông trên header<br>3. Chụp dropdown đang mở với danh sách thông báo |
| **Trạng thái cần đạt** | Badge đỏ trên icon chuông, dropdown mở với ≥2 thông báo, thông báo chưa đọc có nền khác |
| **Caption đề xuất** | "Hình 3.37. Dropdown thông báo hệ thống" |
| **Vị trí trong báo cáo** | Mục 3.6.13 — Thành phần dùng chung |

---

## SS18 — Chat Test Page (STOMP Debug)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS18 |
| **Tên** | Giao diện kiểm thử Chat WebSocket STOMP |
| **Mục đích** | Minh họa chức năng chat realtime đã hoạt động (developer interface) |
| **URL** | http://localhost:5173/chat-test |
| **Bước thực hiện** | 1. Đăng nhập<br>2. Vào /chat-test<br>3. Nhập Shop ID, nhấn "Vào Chat"<br>4. Gửi 2-3 tin nhắn test<br>5. Chụp màn hình 3 cột: danh sách phòng + cửa sổ chat + STOMP log |
| **Trạng thái cần đạt** | Tin nhắn hiển thị bubble orange (người gửi) và bubble trắng (người nhận), STOMP log có "CONNECTED", "MESSAGE IN" màu xanh lá |
| **Caption đề xuất** | "Hình 3.38. Giao diện kiểm thử chat realtime WebSocket STOMP" |
| **Vị trí trong báo cáo** | Mục 3.6.12 — Chat |

---

## SS19 — Header (Đã đăng nhập)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS19 |
| **Tên** | Header khi đã đăng nhập |
| **Mục đích** | Hiển thị đầy đủ header với cart badge, notification badge, user menu |
| **URL** | http://localhost:5173/products |
| **Bước thực hiện** | 1. Đăng nhập với tài khoản có hàng trong giỏ và thông báo chưa đọc<br>2. Chụp riêng phần header (crop nếu cần) |
| **Trạng thái cần đạt** | Logo, ô tìm kiếm, icon giỏ hàng với số (≥1), icon chuông với badge đỏ, avatar/tên user |
| **Caption đề xuất** | "Hình 3.39. Header điều hướng chính khi đã đăng nhập" |
| **Vị trí trong báo cáo** | Mục 3.6.13 — Thành phần dùng chung |

---

## SS20 — Giỏ hàng trống

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | SS20 |
| **Tên** | Giỏ hàng trống |
| **Mục đích** | Hiển thị trạng thái empty state của giỏ hàng |
| **URL** | http://localhost:5173/cart (khi giỏ trống) |
| **Bước thực hiện** | 1. Đăng nhập với tài khoản không có sản phẩm trong giỏ<br>2. Vào /cart<br>3. Chụp màn hình |
| **Trạng thái cần đạt** | Thông báo giỏ hàng trống + link về trang sản phẩm |
| **Caption đề xuất** | "Hình 3.40. Màn hình giỏ hàng trống" |
| **Vị trí trong báo cáo** | Mục 3.6.6 — Giỏ hàng |

---

## Tổng kết

| Tổng số ảnh | Bắt buộc | Tùy chọn |
|---|---|---|
| 20 | SS01–SS17, SS19 (18 ảnh) | SS18 (Chat), SS20 (empty cart) |

### Thứ tự ưu tiên chụp

**Ưu tiên cao (cần nhất):**
SS03 → SS05 → SS08 → SS09 → SS10 → SS12 → SS14 → SS17

**Ưu tiên trung bình:**
SS01 → SS02 → SS06 → SS07 → SS15 → SS16 → SS19

**Ưu tiên thấp (bổ sung):**
SS04 → SS11 → SS13 → SS18 → SS20
