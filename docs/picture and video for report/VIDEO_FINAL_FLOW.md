# VIDEO FINAL FLOW — Thứ tự quay tối ưu

> Đây là thứ tự quay một lần duy nhất, liên tục từ đầu đến cuối.
> Mục tiêu: Không phải đăng nhập/đăng xuất nhiều lần, không phải mở lại trang.
> Sử dụng 3 tab browser đặt sẵn: **[TAB-C] Customer** · **[TAB-V] Vendor** · **[TAB-A] Admin**

---

## CHUẨN BỊ (Trước khi bấm Record)

### Bước 1 — Mở 3 tab browser

```
[TAB-C] → http://localhost:5173/login    (chưa đăng nhập — Customer)
[TAB-V] → http://localhost:5173/login    (chưa đăng nhập — Vendor)
[TAB-A] → http://localhost:5173/login    (chưa đăng nhập — Admin)
```

### Bước 2 — Kiểm tra hệ thống

- [ ] Backend đang chạy port 8080
- [ ] Frontend đang chạy port 5173 (`npm run dev`)
- [ ] MySQL đang chạy, DB `ecommerce` có dữ liệu
- [ ] Cloudinary kết nối được (upload test)
- [ ] VNPay sandbox credentials hợp lệ

### Bước 3 — Kiểm tra dữ liệu DB

- [ ] ≥10 sản phẩm ACTIVE với ảnh và ≥2 biến thể
- [ ] ≥3 danh mục
- [ ] Tài khoản Customer (`buyer@test.com`) KHÔNG có gì trong giỏ hàng
- [ ] Tài khoản Vendor (`vendor@test.com`) có shop ACTIVE tên ví dụ "Shop ABC"
- [ ] Có 1 shop PENDING chờ Admin duyệt
- [ ] Tài khoản Customer có ít nhất 1 đơn DELIVERED (để demo review)
- [ ] Có voucher hợp lệ: mã `SALE20` — 20% — shop Vendor

### Bước 4 — Cài đặt màn hình quay

- Độ phân giải: 1440×900 hoặc 1280×800
- Ẩn bookmark bar
- Zoom browser: 100%
- Bật tab audio nếu thuyết minh

---

## LUỒNG QUAY CHÍNH

---

### ① [TAB-C] 0:00 — Login Customer

**Màn hình:** `http://localhost:5173/login`

```
1. Nhập email: buyer@test.com
2. Nhập password
3. Click "Đăng nhập"
4. Đợi redirect → /products
5. PAUSE 2 giây → xác nhận header hiện avatar + icon giỏ hàng + icon chuông
```

> 📸 Có thể lấy ảnh: Header sau đăng nhập

---

### ② [TAB-C] 0:45 — Browse sản phẩm + Filter danh mục

**Màn hình:** `/products`

```
1. Nhìn overview grid sản phẩm 3 giây
2. Click vào 1 danh mục → grid filter
3. PAUSE 2 giây
4. Nhập từ khóa vào ô tìm kiếm → nhấn Enter
5. PAUSE 2 giây xem kết quả
6. Xóa từ khóa → trở về tất cả sản phẩm
```

> 📸 Có thể lấy ảnh F01 — Product list với filter

---

### ③ [TAB-C] 1:45 — Vào chi tiết sản phẩm

**Màn hình:** `/products/:id`

```
1. Click vào sản phẩm có nhiều biến thể (ít nhất màu + size)
2. Click thumbnail → ảnh chính đổi
3. Click chọn biến thể 1 → xem tồn kho
4. Click chọn biến thể 2 → xem giá/tồn kho thay đổi
5. Đặt số lượng = 2
6. Click "Thêm vào giỏ hàng"
7. PAUSE — badge giỏ hàng trên header tăng lên "2"
```

> 📸 Có thể lấy ảnh F02 — Product detail + F_ADD_CART (badge tăng)

---

### ④ [TAB-C] 2:45 — Thêm sản phẩm từ shop khác

**Màn hình:** Quay lại `/products`

```
1. Click nút Back hoặc click logo về /products
2. Scroll → tìm 1 sản phẩm từ shop khác (khác shopId)
3. Click vào sản phẩm → vào detail
4. Chọn biến thể → Click "Thêm vào giỏ"
5. Badge giỏ tăng → "3"
6. Click icon giỏ hàng trên header → vào /cart
```

---

### ⑤ [TAB-C] 3:00 — Giỏ hàng đa gian hàng

**Màn hình:** `/cart`

```
1. PAUSE 3 giây — nhìn giỏ hàng với 2 shop
2. Tăng số lượng 1 item (+) → tổng tiền cập nhật
3. PAUSE 2 giây
4. Click "Tiến hành đặt hàng"
```

> 📸 Có thể lấy ảnh F03 — Cart multi-shop

---

### ⑥ [TAB-C] 3:30 — Checkout + Voucher

**Màn hình:** `/checkout`

```
1. Điền địa chỉ: "123 Nguyễn Văn A, Quận 1, TP.HCM"
2. Nhập mã voucher: SALE20 → Click "Áp dụng"
3. PAUSE 2 giây — thấy số tiền giảm, tổng mới
4. Chọn "Thanh toán VNPay" (radio button)
5. PAUSE 1 giây
6. Click "Đặt hàng"
7. Đợi redirect sang VNPay sandbox (3-5 giây)
```

> 📸 Có thể lấy ảnh F04 — Checkout với voucher applied

---

### ⑦ [TAB-C] 4:30 — VNPay Sandbox Payment

**Màn hình:** `https://sandbox.vnpayment.vn/...`

```
1. PAUSE 3 giây — nhìn trang VNPay với số tiền, mã giao dịch
2. Nhập thẻ sandbox:
   - Số thẻ: 9704198526191432198
   - Tên: NGUYEN VAN A
   - Ngày: 07/15
   - OTP: 123456 (VNPay sandbox OTP)
3. Click "Thanh toán"
4. Đợi VNPay xử lý → redirect về localhost:5173/payment-result
```

> 📸 Có thể lấy ảnh F06 — VNPay sandbox page

> ⚠️ **Note:** Thẻ test sandbox VNPay — kiểm tra VNPay docs cho thẻ test mới nhất

---

### ⑧ [TAB-C] 5:30 — Payment Result

**Màn hình:** `/payment-result`

```
1. Thấy spinner "Đang xác nhận thanh toán..."
2. (Hệ thống tự gọi IPN sync + polling)
3. PAUSE — đợi SUCCESS icon xuất hiện
4. Thấy: icon tích xanh + "Thanh Toán Thành Công!"
5. Click "Xem Đơn Hàng"
```

> 📸 Có thể lấy ảnh F05 — Payment success

---

### ⑨ [TAB-C] 6:00 — Lịch sử đơn hàng

**Màn hình:** `/orders`

```
1. PAUSE 2 giây — thấy đơn vừa tạo (CONFIRMED)
2. Click tab "Hoàn thành" → xem đơn DELIVERED cũ (chuẩn bị sẵn)
3. Click vào đơn DELIVERED → vào /orders/:id
4. Nhìn chi tiết: sản phẩm (snapshot giá), địa chỉ, trạng thái DELIVERED
5. Click "Viết đánh giá" trên 1 sản phẩm
```

> 📸 Có thể lấy ảnh F07 — Order history

---

### ⑩ [TAB-C] 6:30 — Viết đánh giá

**Màn hình:** Redirect đến `/products/:id#reviews` (với modal auto-open)

```
1. Modal CreateReviewModal tự mở
2. Click 5 sao
3. Nhập nội dung: "Sản phẩm chất lượng tốt, giao hàng nhanh!"
4. Click "Gửi đánh giá"
5. Scroll xuống phần reviews → thấy review vừa tạo
6. PAUSE 3 giây — nhìn rating summary + review mới
```

> 📸 Có thể lấy ảnh F08 — Review section với rating histogram

---

### ⑪ [TAB-C] 7:00 — Notification Dropdown

**Màn hình:** Bất kỳ trang nào có header

```
1. Click icon chuông trên header
2. Dropdown mở → thấy thông báo hệ thống
3. PAUSE 2 giây
4. Click "Đánh dấu tất cả đã đọc"
5. Dropdown đóng
```

> 📸 Có thể lấy ảnh F09 — Notification dropdown

---

### ⑫ [TAB-V] 7:30 — Login Vendor + VendorNav

**Chuyển sang TAB-V**

```
1. Nhập: vendor@test.com + password → Đăng nhập
2. Redirect → /vendor/products
3. RequireShop check → shop ACTIVE → hiển thị VendorNav
4. PAUSE 3 giây — nhìn VendorNav: tên shop + badge + 3 tabs
```

> 📸 Có thể lấy ảnh VS03 — VendorNav

---

### ⑬ [TAB-V] 8:00 — Tạo sản phẩm mới

**Màn hình:** Click "+ Thêm Sản Phẩm Mới" → `/vendor/products/new`

```
1. Điền Tên: "Áo Thun Premium Demo"
2. Điền Mô tả: "Áo thun chất liệu cotton 100%"
3. Điền Giá: 250000
4. Chọn Danh mục: Thời trang
5. --- Upload ảnh ---
6. Click "Upload ảnh" → chọn 2 file ảnh (chuẩn bị sẵn)
7. PAUSE — spinner upload → thấy ảnh hiện trong grid (Cloudinary URL)
8. --- Thêm biến thể ---
9. Nhập: SKU=PREMIUM-M-RED, Giá=250000, Tồn kho=50, Attributes={"color":"Đỏ","size":"M"}
10. Click "Thêm Biến Thể" → biến thể hiện trong bảng
11. Nhập: SKU=PREMIUM-L-BLUE, Giá=260000, Tồn kho=30, Attributes={"color":"Xanh","size":"L"}
12. Click "Thêm Biến Thể"
13. PAUSE 2 giây — xem form đầy đủ
14. Click "Lưu Sản Phẩm"
15. Redirect về danh sách → sản phẩm mới xuất hiện
```

> 📸 Có thể lấy ảnh V01 — Product form với ảnh + variants

---

### ⑭ [TAB-V] 9:00 — Quản lý đơn hàng Vendor

**Màn hình:** Click tab "Quản Lý Đơn Hàng" → `/vendor/orders`

```
1. PAUSE 2 giây — nhìn tab "Tất cả"
2. Click tab "Chờ xác nhận" (CONFIRMED)
3. Thấy đơn hàng từ Customer (từ Scene ⑧)
4. Click "Giao cho ĐVVC" → đợi toast
5. Toast xanh "Cập nhật trạng thái thành công!" → PAUSE 2 giây
6. Đơn biến khỏi tab CONFIRMED → click tab "Đang giao" → xác nhận đơn ở đây
```

> 📸 Có thể lấy ảnh V02 — Vendor order action; VS12 — Toast

---

### ⑮ [TAB-V] 9:45 — Tạo Voucher

**Màn hình:** Click tab "Quản Lý Voucher" → `/vendor/vouchers`

```
1. PAUSE 2 giây — xem danh sách
2. Click "+ Tạo Voucher Mới"
3. Modal mở → điền:
   - Mã: DEMO30
   - Loại: PERCENTAGE
   - Giá trị: 30
   - Đơn tối thiểu: 300000
   - Bắt đầu: [hôm nay]
   - Kết thúc: [1 tháng sau]
4. Click "Tạo Voucher"
5. Modal đóng → voucher DEMO30 xuất hiện trong bảng
```

> 📸 Có thể lấy ảnh V04 — Voucher modal; VS13 — Voucher list

---

### ⑯ [TAB-A] 10:30 — Login Admin + Overview

**Chuyển sang TAB-A**

```
1. Nhập: admin@test.com + password → Đăng nhập
2. Redirect → /admin
3. PAUSE 2 giây — nhìn AdminLayout: sidebar + topbar "AD"
4. Thử click sidebar thu gọn và mở lại
```

> 📸 Có thể lấy ảnh A01 — Admin layout

---

### ⑰ [TAB-A] 11:00 — Admin duyệt gian hàng

**Màn hình:** Click "Cửa hàng" → `/admin/shop`

```
1. Dropdown filter → chọn PENDING
2. Thấy shop PENDING (chuẩn bị sẵn)
3. PAUSE 2 giây
4. Click "Duyệt"
5. Đợi reload → shop chuyển ACTIVE
6. PAUSE 2 giây — nhìn badge ACTIVE xanh
7. Click filter → ACTIVE → xác nhận shop có ở đây
```

> 📸 Có thể lấy ảnh AS03 — Pending; AS05 — Approved

---

### ⑱ [TAB-A] 11:45 — Admin xem đơn hàng + tạo voucher hệ thống

**Màn hình:** Click "Đơn hàng" → `/admin/orders`

```
1. PAUSE 2 giây — nhìn bảng đơn hàng toàn hệ thống
2. Click vào 1 đơn → xem chi tiết
3. PAUSE 2 giây — thấy thông tin khách + shop + sản phẩm (snapshot)
4. Click "← Quay lại"
5. Click "Voucher" → /admin/vouchers
6. Click "+ Tạo Voucher Hệ Thống"
7. Modal mở → điền: Mã=FREESHIP, Loại=FREE_SHIPPING, Giá trị=0
8. Điền ngày bắt đầu + kết thúc
9. Click "Tạo Voucher"
10. Voucher xuất hiện với scope=SYSTEM
```

> 📸 Có thể lấy ảnh A01 (orders); AS12 — System voucher modal

---

### ⑲ [TAB-C + TAB-V SPLIT] 12:30 — Chat WebSocket STOMP

**Chuẩn bị:** Resize 2 cửa sổ browser: [TAB-C] bên trái, [TAB-V] bên phải

```
--- TAB-C (Customer) ---
1. Navigate /chat-test
2. PAUSE — thấy STOMP log "CONNECTED" màu xanh lá
3. Nhập Shop ID của shop Vendor
4. Click "Vào Chat"
5. Phòng chat xuất hiện trong danh sách

--- TAB-V (Vendor) ---
6. Navigate /chat-test
7. STOMP log "CONNECTED"
8. Click vào phòng chat đã tạo

--- Gửi tin nhắn qua lại ---
9. [TAB-C] Gõ "Xin chào, sản phẩm còn hàng không?" → Gửi
10. [TAB-V] Thấy tin nhắn xuất hiện NGAY LẬP TỨC (không reload)
11. [TAB-V] Gõ "Dạ còn hàng, bạn muốn size nào?" → Gửi
12. [TAB-C] Thấy tin nhắn Vendor xuất hiện ngay
13. Tiếp tục 2-3 tin nữa
14. PAUSE — nhìn STOMP log "MESSAGE IN" màu vàng
```

> 📸 Có thể lấy ảnh R01 — Chat two-way + STOMP log

---

### ⑳ [TAB-V] 14:30 — Notification Vendor (Optional)

```
1. Chuyển TAB-V → click icon chuông
2. Thấy thông báo NEW_ORDER (từ đơn tạo ở Scene ⑧)
3. Click vào thông báo
4. PAUSE 2 giây
5. DỪNG QUAY
```

> 📸 Có thể lấy ảnh VS17 — Vendor notification

---

## CHECKLIST TRƯỚC KHI QUAY

### Môi trường

- [ ] Spring Boot backend đang chạy `http://localhost:8080`
- [ ] Vite frontend đang chạy `http://localhost:5173`
- [ ] MySQL đang chạy, DB `ecommerce` accessible
- [ ] `.env` đã cấu hình đầy đủ: DB, JWT, Cloudinary, VNPay, Gmail

### Dữ liệu

- [ ] Tài khoản `buyer@test.com` — giỏ hàng RỖNG
- [ ] Tài khoản `vendor@test.com` — shop ACTIVE tên "Shop ABC" (hoặc tương tự)
- [ ] Tài khoản `admin@test.com` — ROLE_ADMIN
- [ ] 1 shop PENDING chưa duyệt
- [ ] ≥10 sản phẩm ACTIVE với ảnh Cloudinary
- [ ] Sản phẩm từ ≥2 shop khác nhau (để demo multi-shop cart)
- [ ] Voucher `SALE20` hợp lệ (PERCENTAGE 20%, đơn tối thiểu 100k)
- [ ] Tài khoản buyer có ≥1 đơn DELIVERED với sản phẩm chưa review
- [ ] 2 file ảnh sản phẩm chuẩn bị sẵn trên Desktop để upload nhanh

### Browser

- [ ] 3 tab mở sẵn và đặt tên rõ ràng
- [ ] Xóa localStorage của cả 3 tab (F12 → Application → Clear Storage)
- [ ] Zoom 100%, bookmark bar ẩn
- [ ] Không có extension nào cản WebSocket

### Thẻ test VNPay Sandbox

```
Số thẻ:  9704198526191432198
Họ tên:  NGUYEN VAN A
Ngày:    07/15
OTP:     123456
```

---

## SƠ ĐỒ LUỒNG QUAY

```
[TAB-C]                          [TAB-V]              [TAB-A]
   │
   ├─① Login Customer
   ├─② Browse + Filter
   ├─③ Product Detail + Add Cart
   ├─④ Add cart shop 2 → /cart
   ├─⑤ Cart view
   ├─⑥ Checkout + Voucher SALE20
   ├─⑦ VNPay Sandbox
   ├─⑧ Payment Result SUCCESS
   ├─⑨ Orders → DELIVERED order
   ├─⑩ Write Review → xem Reviews
   ├─⑪ Notification dropdown
   │                                │
   │                                ├─⑫ Login Vendor
   │                                ├─⑬ Create Product + Cloudinary
   │                                ├─⑭ Order Management → SHIPPED
   │                                ├─⑮ Create Voucher
   │                                │                        │
   │                                │                        ├─⑯ Login Admin
   │                                │                        ├─⑰ Shop Approval
   │                                │                        └─⑱ Orders + System Voucher
   │                                │
   ├─⑲ [SPLIT] Chat STOMP ──────────┤ [SPLIT]
   └─⑳ [TAB-V] Vendor Notification ─┘
```

**Tổng thời gian:** ~15 phút

**Output:** 1 video liên tục + trích xuất ≥16 screenshots
