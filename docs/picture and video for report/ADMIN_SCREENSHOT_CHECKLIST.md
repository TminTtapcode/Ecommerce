# ADMIN SCREENSHOT CHECKLIST

> Danh sách ảnh màn hình cần chụp cho phần 3.6.y (Giao diện Admin).
> Frontend đang chạy tại: http://localhost:5173
> Role cần thiết: Tài khoản ROLE_ADMIN.

---

## Chuẩn bị trước khi chụp

### Tài khoản cần chuẩn bị

| Tài khoản | Mục đích |
|---|---|
| **Admin (ROLE_ADMIN)** | Tất cả màn hình admin |

### Dữ liệu cần có trong DB

- Ít nhất 1 shop có status PENDING (để chụp màn hình duyệt)
- Ít nhất 2 shop có status ACTIVE
- Ít nhất 1 shop có status BANNED
- Ít nhất 5 sản phẩm của các shops khác nhau
- Ít nhất 5 đơn hàng ở các trạng thái khác nhau
- Ít nhất 2 voucher (1 SYSTEM, 1 SHOP)

### Kích thước màn hình

- Desktop: 1440×900 hoặc 1280×800 (cần đủ rộng để sidebar không bị che)
- Browser: Chrome, không thu nhỏ sidebar

---

## AS01 — Admin Layout tổng quan (Sidebar mở)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS01 |
| **Tên** | Layout Admin với sidebar đầy đủ |
| **Feature** | AdminLayout |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Đăng nhập Admin → truy cập http://localhost:5173/admin<br>2. Đảm bảo sidebar đang MỞ (không thu gọn)<br>3. Chụp toàn màn hình bao gồm: sidebar trái + topbar + vùng nội dung Dashboard |
| **Expected result** | Sidebar với logo "EC Admin", 5 menu items (Tổng quan, Sản phẩm, Đơn hàng, Cửa hàng, Voucher), Topbar với ô tìm kiếm + icon chuông + avatar "AD", vùng chính hiển thị "Tổng quan Dashboard" |
| **Caption** | "Hình 3.59. Giao diện quản trị hệ thống (Admin Dashboard)" |
| **Report location** | Mục 3.6.y.2 — AdminLayout |

---

## AS02 — Admin Layout Sidebar thu gọn

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS02 |
| **Tên** | Admin Layout với sidebar thu gọn (icon-only) |
| **Feature** | AdminLayout — Sidebar Collapse |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào /admin với sidebar đang mở<br>2. Nhấn nút hamburger để thu gọn sidebar<br>3. Chụp màn hình khi sidebar chỉ còn icon |
| **Expected result** | Sidebar hẹp chỉ hiển thị icon, vùng nội dung rộng hơn |
| **Caption** | "Hình 3.60. Sidebar thu gọn của giao diện Admin" |
| **Report location** | Mục 3.6.y.2 — AdminLayout |

---

## AS03 — Quản lý Gian hàng — Tab PENDING

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS03 |
| **Tên** | Danh sách gian hàng chờ duyệt |
| **Feature** | Admin Shop Management — Pending |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào http://localhost:5173/admin/shop<br>2. Filter trạng thái = PENDING<br>3. Đảm bảo có ≥1 shop PENDING<br>4. Chụp bảng danh sách với nút "Duyệt" và "Từ chối" |
| **Expected result** | Bảng shop với badge PENDING (vàng), nút "Duyệt" (xanh lá) + nút "Từ chối" (đỏ) trên mỗi hàng |
| **Caption** | "Hình 3.61. Danh sách gian hàng đang chờ phê duyệt" |
| **Report location** | Mục 3.6.y.4 — Quản lý Gian hàng |

---

## AS04 — Quản lý Gian hàng — Tất cả trạng thái

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS04 |
| **Tên** | Danh sách gian hàng với nhiều trạng thái |
| **Feature** | Admin Shop Management |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào /admin/shop<br>2. Filter = Tất cả<br>3. Đảm bảo có shop ở PENDING, ACTIVE, BANNED<br>4. Chụp bảng với nhiều trạng thái khác nhau |
| **Expected result** | Bảng shop với badge màu sắc khác nhau: vàng (PENDING), xanh lá (ACTIVE), đỏ (BANNED); nút hành động khác nhau theo status |
| **Caption** | "Hình 3.62. Quản lý gian hàng — Tổng hợp các trạng thái" |
| **Report location** | Mục 3.6.y.4 — Quản lý Gian hàng |

---

## AS05 — Sau khi Duyệt shop thành công

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS05 |
| **Tên** | Shop được chuyển sang ACTIVE sau khi duyệt |
| **Feature** | Approve Shop |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào /admin/shop với 1 shop PENDING<br>2. Nhấn "Duyệt" trên shop đó<br>3. Chờ trang reload<br>4. Chụp shop vừa được duyệt với badge ACTIVE xanh lá |
| **Expected result** | Shop hiển thị badge "ACTIVE" xanh lá, nút hành động chuyển sang "Khoá" |
| **Caption** | "Hình 3.63. Gian hàng sau khi được Admin phê duyệt" |
| **Report location** | Mục 3.6.y.4 — Quản lý Gian hàng |

---

## AS06 — Danh sách Sản phẩm (Admin)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS06 |
| **Tên** | Trang quản lý sản phẩm toàn hệ thống |
| **Feature** | Admin Product List |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào http://localhost:5173/admin/products<br>2. Đảm bảo có ≥5 sản phẩm từ các shops khác nhau<br>3. Chụp toàn bộ trang bao gồm sidebar + bảng sản phẩm |
| **Expected result** | Sidebar với "Sản phẩm" active (cam), bảng sản phẩm với ảnh thumbnail, tên, danh mục, giá, tồn kho, badge trạng thái, Shop ID, nút Sửa/Xóa; nút "+ Thêm sản phẩm" ở trên |
| **Caption** | "Hình 3.64. Trang quản lý sản phẩm của Quản trị viên" |
| **Report location** | Mục 3.6.y.5 — Danh sách Sản phẩm |

---

## AS07 — Tạo Sản phẩm (Admin Form)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS07 |
| **Tên** | Form tạo sản phẩm mới (Admin) |
| **Feature** | Admin Create Product |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Từ /admin/products → nhấn "+ Thêm sản phẩm"<br>2. Chụp form tạo mới đang trống hoặc điền một phần |
| **Expected result** | Form với các ô: Tên, Mô tả, Giá, Tồn kho, Dropdown Danh mục; phần upload ảnh; phần biến thể |
| **Caption** | "Hình 3.65. Form tạo sản phẩm mới (Admin)" |
| **Report location** | Mục 3.6.y.6 — Tạo Sản phẩm |

---

## AS08 — Danh sách Đơn hàng (Admin) — Tab Tất cả

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS08 |
| **Tên** | Trang quản lý đơn hàng toàn hệ thống |
| **Feature** | Admin Order List |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào http://localhost:5173/admin/orders<br>2. Filter = Tất cả<br>3. Đảm bảo có ≥5 đơn ở trạng thái khác nhau<br>4. Chụp toàn bộ trang |
| **Expected result** | Sidebar với "Đơn hàng" active, dropdown filter trạng thái, bảng đơn hàng với badge màu sắc đa dạng, cột User ID, Shop ID, Tổng tiền, Phương thức TT, nút "Xem chi tiết" |
| **Caption** | "Hình 3.66. Trang quản lý đơn hàng toàn hệ thống" |
| **Report location** | Mục 3.6.y.7 — Danh sách Đơn hàng |

---

## AS09 — Đơn hàng filter theo PENDING

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS09 |
| **Tên** | Filter đơn hàng theo trạng thái PENDING |
| **Feature** | Admin Order Filter |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào /admin/orders<br>2. Chọn filter "Chờ thanh toán" (PENDING)<br>3. Chụp kết quả filter |
| **Expected result** | Chỉ hiển thị đơn PENDING với badge vàng |
| **Caption** | "Hình 3.67. Lọc đơn hàng theo trạng thái" |
| **Report location** | Mục 3.6.y.7 — Danh sách Đơn hàng |

---

## AS10 — Chi tiết Đơn hàng (Admin)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS10 |
| **Tên** | Trang chi tiết đơn hàng — góc nhìn Admin |
| **Feature** | Admin Order Detail |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Từ danh sách đơn hàng → nhấn "Xem chi tiết" trên một đơn DELIVERED<br>2. Chụp toàn bộ trang chi tiết |
| **Expected result** | Grid 2 cột thông tin (Khách hàng + Đơn hàng), bảng sản phẩm với giá snapshot, tổng tiền, nút "← Quay lại" |
| **Caption** | "Hình 3.68. Trang chi tiết đơn hàng (góc nhìn Quản trị viên)" |
| **Report location** | Mục 3.6.y.8 — Chi tiết Đơn hàng |

---

## AS11 — Quản lý Voucher (Admin) — Danh sách

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS11 |
| **Tên** | Trang quản lý voucher hệ thống |
| **Feature** | Admin Voucher List |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào http://localhost:5173/admin/vouchers<br>2. Đảm bảo có ≥2 voucher (SYSTEM và SHOP)<br>3. Chụp toàn bộ trang |
| **Expected result** | Sidebar "Voucher" active, bảng voucher với cột: Mã, Scope (SYSTEM/SHOP), Loại, Giá trị, Điều kiện, Thời hạn, Số lần dùng; nút "+ Tạo Voucher Hệ Thống" |
| **Caption** | "Hình 3.69. Trang quản lý voucher của Quản trị viên" |
| **Report location** | Mục 3.6.y.9 — Quản lý Voucher |

---

## AS12 — Modal Tạo Voucher Hệ thống

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS12 |
| **Tên** | Modal tạo voucher hệ thống (scope=SYSTEM) |
| **Feature** | Create System Voucher |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào /admin/vouchers<br>2. Nhấn "+ Tạo Voucher Hệ Thống"<br>3. Modal mở → chụp form đầy đủ |
| **Expected result** | Modal với tiêu đề "Tạo Voucher Hệ Thống", form đầy đủ: Mã voucher, dropdown Loại (PERCENTAGE/FIXED_AMOUNT/FREE_SHIPPING), Mức giảm, Tối đa giảm, Đơn tối thiểu, Bắt đầu/Kết thúc, Giới hạn lần dùng; nút "Tạo Voucher" (indigo) + "Hủy" |
| **Caption** | "Hình 3.70. Modal tạo mã giảm giá hệ thống" |
| **Report location** | Mục 3.6.y.9 — Quản lý Voucher |

---

## AS13 — Admin Notification Dropdown

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS13 |
| **Tên** | Dropdown thông báo trong Admin panel |
| **Feature** | Admin Notification |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Đăng nhập Admin, có ≥1 thông báo hệ thống<br>2. Nhấn icon chuông trong topbar<br>3. Chụp dropdown đang mở |
| **Expected result** | Dropdown thông báo mở với ≥1 thông báo, icon chuông có badge đỏ |
| **Caption** | "Hình 3.71. Dropdown thông báo trong giao diện Admin" |
| **Report location** | Mục 3.6.y.2 — AdminLayout |

---

## AS14 — Topbar với Avatar Menu

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | AS14 |
| **Tên** | Menu dropdown của Admin |
| **Feature** | Admin Avatar Dropdown |
| **Role** | ROLE_ADMIN |
| **Steps** | 1. Vào bất kỳ trang /admin/*<br>2. Hover vào avatar "AD" góc phải topbar<br>3. Dropdown xuất hiện → chụp |
| **Expected result** | Dropdown với: "Quản trị viên" (label), "Về trang chủ", "Đăng xuất" |
| **Caption** | "Hình 3.72. Menu quản trị viên trong Admin panel" |
| **Report location** | Mục 3.6.y.2 — AdminLayout |

---

## Tổng kết

| Tổng số ảnh | Bắt buộc | Tùy chọn |
|---|---|---|
| 14 | AS01, AS03–AS12 (11 ảnh) | AS02, AS13, AS14 (3 ảnh) |

### Thứ tự ưu tiên chụp

**Ưu tiên cao (cần nhất):**
AS01 → AS03 → AS04 → AS06 → AS08 → AS10 → AS11 → AS12

**Ưu tiên trung bình:**
AS05 → AS07 → AS09

**Ưu tiên thấp (bổ sung):**
AS02 → AS13 → AS14

---

### Lưu ý khi chụp Admin

1. **AS03 + AS05**: Chụp trước/sau khi duyệt 1 shop — nên lưu AS03 trước khi nhấn Duyệt, rồi chụp AS05 sau khi duyệt
2. **AS01**: Cần màn hình đủ rộng (≥1280px) để thấy sidebar đầy đủ
3. **AS14**: Cần hover (không click) — dùng tool chụp sau 2-3 giây delay nếu cần
4. Tất cả ảnh Admin nên thấy sidebar để xác định rõ context giao diện Admin
