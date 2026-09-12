# SCREENSHOT_CHECKLIST.md
# Danh Sách Ảnh Cần Chụp — Enterprise Ecommerce

> ⚠️ Chỉ liệt kê ảnh cho features đã được xác minh là IMPLEMENTED trong source code.
> Cần chuẩn bị môi trường local: Backend running (port 8080) + Frontend running (port 5173)

---

## NHÓM A — TRANG CÔNG KHAI (Public)

### SCR-A01: Trang danh sách sản phẩm (mặc định)
- **Chức năng:** Browse sản phẩm, hiển thị grid
- **Role:** Anonymous / User
- **Màn hình:** `/products`
- **Trạng thái cần:** Có sản phẩm trong DB, có ảnh Cloudinary
- **Bước thực hiện:**
  1. Mở trình duyệt `http://localhost:5173/products`
  2. Đảm bảo có nhiều sản phẩm hiển thị (dùng DataInitializer)
  3. Chụp toàn trang (full page)
- **Dữ liệu cần:** 12+ sản phẩm đã seed, có ảnh
- **Vị trí báo cáo:** 3.6 Thiết kế giao diện, 3.7.3 Hình ảnh chạy thực tế

### SCR-A02: Tìm kiếm sản phẩm
- **Chức năng:** Search bar với kết quả tìm kiếm
- **Role:** Anonymous
- **Màn hình:** `/products?keyword=...`
- **Bước thực hiện:**
  1. Vào `/products`
  2. Gõ từ khóa vào search bar
  3. Chụp kết quả tìm kiếm
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-A03: Trang chi tiết sản phẩm
- **Chức năng:** Xem chi tiết, biến thể, ảnh gallery, mô tả
- **Role:** Anonymous / User
- **Màn hình:** `/products/{id}`
- **Trạng thái cần:** Sản phẩm có nhiều ảnh, có biến thể (size/color), có đánh giá
- **Bước thực hiện:**
  1. Click vào sản phẩm từ danh sách
  2. Chụp full page (bao gồm phần biến thể, nút Thêm vào giỏ, Chat ngay)
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-A04: Trang chi tiết sản phẩm — phần Đánh giá
- **Chức năng:** Xem rating summary + danh sách đánh giá
- **Role:** Anonymous
- **Màn hình:** `/products/{id}` (scroll xuống phần đánh giá)
- **Bước thực hiện:**
  1. Vào trang chi tiết sản phẩm có đánh giá
  2. Scroll xuống phần Reviews
  3. Chụp rating summary + một vài review
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM B — AUTHENTICATION

### SCR-B01: Trang đăng ký
- **Chức năng:** Form đăng ký tài khoản
- **Role:** Anonymous
- **Màn hình:** `/register`
- **Bước thực hiện:** Mở trang, chụp form trống
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-B02: Trang đăng nhập
- **Chức năng:** Form đăng nhập
- **Role:** Anonymous
- **Màn hình:** `/login`
- **Bước thực hiện:** Mở trang, chụp form
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM C — CUSTOMER SHOPPING FLOW

### SCR-C01: Giỏ hàng
- **Chức năng:** Danh sách sản phẩm trong giỏ, số lượng, tổng tiền
- **Role:** User (đăng nhập)
- **Màn hình:** `/cart`
- **Trạng thái cần:** 2-3 sản phẩm từ nhiều shop khác nhau trong giỏ
- **Bước thực hiện:**
  1. Đăng nhập với tài khoản buyer
  2. Thêm 2-3 sản phẩm từ ít nhất 2 shop
  3. Vào `/cart`
  4. Chụp giỏ hàng đầy đủ
- **Dữ liệu cần:** Buyer account + sản phẩm từ ≥2 shops
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-C02: Trang Checkout (Multi-vendor tách đơn)
- **Chức năng:** Checkout với nhiều shop, phần voucher, phương thức thanh toán
- **Role:** User
- **Màn hình:** `/checkout`
- **Trạng thái cần:** Giỏ hàng có sản phẩm từ ≥2 shops, một voucher SYSTEM hợp lệ
- **Bước thực hiện:**
  1. Vào giỏ hàng, bấm Checkout
  2. Chụp trang checkout (thấy tách đơn theo shop)
- **Vị trí báo cáo:** 3.6, 3.7.3 — **Quan trọng: chứng minh Multi-vendor**

### SCR-C03: Áp dụng voucher khi checkout
- **Chức năng:** Nhập mã voucher, xem số tiền giảm
- **Role:** User
- **Màn hình:** `/checkout` (sau khi nhập voucher code)
- **Bước thực hiện:**
  1. Ở trang checkout, nhập mã voucher hợp lệ
  2. Bấm áp dụng
  3. Chụp hiển thị tiền giảm
- **Dữ liệu cần:** Voucher SYSTEM hợp lệ (seed bởi VoucherDataInitializer)
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-C04: Redirect VNPay
- **Chức năng:** Giao diện thanh toán VNPay Sandbox
- **Màn hình:** VNPay sandbox payment page
- **Bước thực hiện:**
  1. Ở checkout, chọn VNPay, bấm Thanh toán
  2. Chụp trang VNPay sandbox
- **Vị trí báo cáo:** 3.7.3 (minh họa tích hợp VNPay)

### SCR-C05: Trang kết quả thanh toán
- **Chức năng:** Hiển thị kết quả sau thanh toán (CONFIRMED)
- **Role:** User
- **Màn hình:** `/payment-result`
- **Bước thực hiện:**
  1. Hoàn thành thanh toán VNPay (dùng test card)
  2. Chụp trang kết quả (trạng thái CONFIRMED)
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-C06: Lịch sử đơn hàng
- **Chức năng:** Danh sách đơn hàng với các trạng thái
- **Role:** User
- **Màn hình:** `/orders`
- **Trạng thái cần:** Có nhiều đơn với các status khác nhau (CONFIRMED, SHIPPED, DELIVERED)
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-C07: Chi tiết đơn hàng
- **Chức năng:** Chi tiết 1 đơn hàng (items, status, giá)
- **Role:** User
- **Màn hình:** `/orders/{id}`
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-C08: Viết đánh giá sản phẩm
- **Chức năng:** Form viết đánh giá (rating + comment) sau khi nhận hàng
- **Role:** User (đơn đã DELIVERED)
- **Màn hình:** `/products/{id}` (phần review form)
- **Bước thực hiện:**
  1. Có đơn hàng trạng thái DELIVERED với sản phẩm X
  2. Vào trang sản phẩm X
  3. Tìm form viết review
  4. Chụp form
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM D — REALTIME FEATURES

### SCR-D01: FloatingChatWidget — Đóng
- **Chức năng:** Widget chat nổi ở góc phải (trạng thái thu nhỏ)
- **Role:** User
- **Màn hình:** Bất kỳ trang nào (sau khi đăng nhập)
- **Bước thực hiện:** Đăng nhập, chụp widget nút pill màu cam ở góc dưới phải
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-D02: FloatingChatWidget — Mở danh sách phòng
- **Chức năng:** Danh sách hội thoại
- **Role:** User
- **Màn hình:** Widget mở (panel bên trái)
- **Bước thực hiện:**
  1. Bấm nút chat mở widget
  2. Chụp danh sách phòng chat
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-D03: FloatingChatWidget — Tin nhắn realtime
- **Chức năng:** Khung chat với bubble tin nhắn
- **Role:** User (Buyer)
- **Màn hình:** Widget mở, đang trong phòng chat
- **Trạng thái cần:** Có ít nhất 1 cuộc trò chuyện với shop
- **Bước thực hiện:**
  1. Vào trang sản phẩm, bấm "Chat ngay"
  2. Gửi tin nhắn
  3. Chụp bubble tin nhắn
- **Vị trí báo cáo:** 3.6, 3.7.3 — **Quan trọng: chứng minh realtime chat**

### SCR-D04: Notification Dropdown
- **Chức năng:** Dropdown thông báo với badge số chưa đọc
- **Role:** User
- **Màn hình:** Header → icon bell
- **Trạng thái cần:** Có thông báo chưa đọc (đơn hàng confirmed)
- **Bước thực hiện:**
  1. Có thông báo chưa đọc
  2. Chụp icon bell với badge số
  3. Click mở dropdown
  4. Chụp dropdown hiển thị thông báo
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM E — VENDOR PORTAL

### SCR-E01: Đăng ký gian hàng
- **Chức năng:** Form đăng ký shop
- **Role:** User/Vendor
- **Màn hình:** VendorShopRegistrationPage
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-E02: Danh sách sản phẩm của Vendor
- **Chức năng:** Quản lý sản phẩm của shop
- **Role:** Vendor
- **Màn hình:** `/vendor/products`
- **Trạng thái cần:** Shop đã ACTIVE, có sản phẩm
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-E03: Form tạo/sửa sản phẩm (Vendor)
- **Chức năng:** Form đầy đủ: tên, giá, category, variants, upload ảnh
- **Role:** Vendor
- **Màn hình:** `/vendor/products/new`
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-E04: Upload ảnh sản phẩm (Dropzone Cloudinary)
- **Chức năng:** Dropzone upload ảnh, thumbnail selection, spinner upload
- **Role:** Vendor
- **Màn hình:** `/vendor/products/new` (phần ảnh)
- **Bước thực hiện:**
  1. Vào form tạo sản phẩm
  2. Upload 2-3 ảnh
  3. Chọn thumbnail
  4. Chụp dropzone với ảnh đã upload + badge Thumbnail
- **Vị trí báo cáo:** 3.6, 3.7.3 — **Minh họa Cloudinary integration**

### SCR-E05: Danh sách đơn hàng của Vendor
- **Chức năng:** Xem đơn hàng theo trạng thái, phân trang
- **Role:** Vendor
- **Màn hình:** `/vendor/orders`
- **Trạng thái cần:** Có nhiều đơn hàng với các status khác nhau
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-E06: Cập nhật trạng thái đơn hàng
- **Chức năng:** Dropdown/button thay đổi CONFIRMED → SHIPPED
- **Role:** Vendor
- **Màn hình:** `/vendor/orders` (nút cập nhật trạng thái)
- **Bước thực hiện:**
  1. Có đơn CONFIRMED
  2. Chụp trước khi cập nhật
  3. Cập nhật → SHIPPED
  4. Chụp sau khi cập nhật
- **Vị trí báo cáo:** 3.6, 3.7.3 — **Minh họa State Machine**

### SCR-E07: Quản lý Voucher của Vendor
- **Chức năng:** Danh sách voucher shop + form tạo voucher
- **Role:** Vendor
- **Màn hình:** `/vendor/vouchers`
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM F — ADMIN PORTAL

### SCR-F01: Admin Dashboard (placeholder)
- **Chức năng:** Trang chào mừng admin (ghi chú: chỉ là placeholder)
- **Role:** Admin
- **Màn hình:** `/admin`
- **Vị trí báo cáo:** 3.6

### SCR-F02: Admin Danh sách Gian hàng
- **Chức năng:** Danh sách shop, filter theo status (PENDING/ACTIVE)
- **Role:** Admin
- **Màn hình:** `/admin/shop`
- **Trạng thái cần:** Có shop PENDING chờ duyệt
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-F03: Admin Duyệt Gian hàng
- **Chức năng:** Bấm Approve → shop chuyển ACTIVE
- **Role:** Admin
- **Màn hình:** `/admin/shop`
- **Bước thực hiện:**
  1. Có shop PENDING
  2. Chụp trước khi Approve (nút Approve hiển thị)
  3. Bấm Approve
  4. Chụp sau khi duyệt (status ACTIVE)
- **Vị trí báo cáo:** 3.7.3 — **Minh họa Admin approval flow**

### SCR-F04: Admin Danh sách Đơn hàng
- **Chức năng:** Xem tất cả đơn hàng toàn hệ thống
- **Role:** Admin
- **Màn hình:** `/admin/orders`
- **Vị trí báo cáo:** 3.6, 3.7.3

### SCR-F05: Admin Chi tiết Đơn hàng (View-only)
- **Chức năng:** Chi tiết đơn hàng — xem thông tin đầy đủ
- **Role:** Admin
- **Màn hình:** `/admin/orders/{id}`
- **Vị trí báo cáo:** 3.6

### SCR-F06: Admin Quản lý Voucher Hệ thống
- **Chức năng:** Danh sách + tạo voucher SYSTEM
- **Role:** Admin
- **Màn hình:** `/admin/vouchers`
- **Vị trí báo cáo:** 3.6, 3.7.3

---

## NHÓM G — TECHNICAL / SWAGGER

### SCR-G01: Swagger UI — API Documentation
- **Chức năng:** Danh sách toàn bộ API endpoints
- **Màn hình:** `http://localhost:8080/swagger-ui.html`
- **Bước thực hiện:** Mở Swagger UI, chụp toàn bộ endpoint list
- **Vị trí báo cáo:** Phụ lục

### SCR-G02: Swagger UI — Chi tiết 1 API (VD: Checkout)
- **Màn hình:** Swagger UI → POST /api/v1/orders/checkout
- **Vị trí báo cáo:** Phụ lục

---

## TỔNG KẾT

| Nhóm | Số ảnh | Độ ưu tiên |
|---|---|---|
| A. Public pages | 4 | Cao |
| B. Authentication | 2 | Cao |
| C. Shopping Flow | 8 | Cao |
| D. Realtime Features | 4 | Cao |
| E. Vendor Portal | 7 | Cao |
| F. Admin Portal | 6 | Trung bình |
| G. Technical | 2 | Thấp |
| **TỔNG** | **33** | |

## ĐỀ XUẤT THỨ TỰ CHỤP

1. Đảm bảo môi trường chạy (Backend + Frontend + DB đã seed)
2. **Chụp Admin flows** trước (dễ setup)
3. **Chụp Vendor flows** (đăng nhập vendor, tạo sản phẩm, quản lý đơn)
4. **Chụp Customer/Buyer flows** (checkout quan trọng nhất)
5. **Chụp Realtime Chat** (cần 2 tab hoặc 2 trình duyệt)
6. **Chụp Swagger UI**

## CHUẨN BỊ DỮ LIỆU CẦN THIẾT

```
Tài khoản cần có sẵn:
- Admin: admin@example.com / [password]
- Vendor 1: vendor1@example.com / [password] → Shop ACTIVE, có sản phẩm
- Vendor 2: vendor2@example.com / [password] → Shop ACTIVE, có sản phẩm
- Buyer: buyer@example.com / [password]

Trạng thái DB cần có:
- 12+ sản phẩm (từ ≥2 shops) với ảnh Cloudinary
- Ít nhất 1 shop PENDING (để chụp Admin Approve)
- Ít nhất 1 đơn hàng DELIVERED (để chụp Review)
- Ít nhất 1 đơn hàng CONFIRMED (để Vendor cập nhật SHIPPED)
- Voucher SYSTEM hợp lệ
- Notification chưa đọc cho buyer
- Chat room đã có tin nhắn giữa buyer và shop
```
