# VENDOR SCREENSHOT CHECKLIST

> Danh sách ảnh màn hình cần chụp cho phần 3.6.x (Giao diện Vendor/Shop).
> Frontend đang chạy tại: http://localhost:5173
> Role cần thiết: Tài khoản ROLE_VENDOR với Shop status = ACTIVE.

---

## Chuẩn bị trước khi chụp

### Tài khoản cần chuẩn bị

| Tài khoản | Mục đích |
|---|---|
| **Vendor (ROLE_VENDOR, shop ACTIVE)** | Chụp tất cả màn hình quản lý |
| **User mới chưa có shop** | Chụp màn hình đăng ký gian hàng |
| **User có shop status PENDING** | Chụp màn hình chờ duyệt |

### Dữ liệu cần có trong DB

- Shop ACTIVE với ≥5 sản phẩm (ACTIVE, OUT_OF_STOCK, HIDDEN)
- Ít nhất 3 đơn hàng ở các trạng thái khác nhau (CONFIRMED, SHIPPED, DELIVERED)
- Ít nhất 1 voucher đã tạo
- Ít nhất 1 đánh giá (review) chưa có vendor reply

### Kích thước màn hình khuyến nghị

- Desktop: 1440×900 hoặc 1280×800
- Browser: Chrome

---

## VS01 — Màn hình Đăng ký Gian hàng

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS01 |
| **Tên** | Form đăng ký gian hàng Vendor |
| **Feature** | Vendor Registration |
| **Role** | ROLE_USER (chưa có shop) |
| **Steps** | 1. Đăng nhập bằng tài khoản chưa có shop<br>2. Truy cập http://localhost:5173/vendor/products<br>3. `RequireShop` phát hiện chưa có shop → hiển thị form đăng ký<br>4. Chụp form đang trống (chưa điền) |
| **Expected result** | Form fullscreen với 2 ô: Tên Shop (required) + Mô tả (tùy chọn) + nút "Đăng Ký Ngay" màu cam |
| **Caption** | "Hình 3.41. Màn hình đăng ký tài khoản Người bán" |
| **Report location** | Mục 3.6.x.2 — Đăng ký Gian hàng |

---

## VS02 — Màn hình Chờ duyệt Gian hàng

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS02 |
| **Tên** | Màn hình chờ Admin duyệt shop |
| **Feature** | Shop Pending State |
| **Role** | ROLE_VENDOR (shop status = PENDING) |
| **Steps** | 1. Có tài khoản đã đăng ký shop nhưng chưa được Admin duyệt<br>2. Truy cập http://localhost:5173/vendor/products<br>3. Chụp màn hình hiển thị "Đang Chờ Duyệt" |
| **Expected result** | Icon đồng hồ vàng, tiêu đề "Đang Chờ Duyệt", mô tả thời gian 1-2 ngày, nút "Quay Về Trang Chủ" |
| **Caption** | "Hình 3.42. Màn hình thông báo gian hàng đang chờ phê duyệt" |
| **Report location** | Mục 3.6.x.3 — Chờ duyệt Gian hàng |

---

## VS03 — VendorNav và Tab điều hướng

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS03 |
| **Tên** | Thanh điều hướng Vendor (VendorNav) |
| **Feature** | VendorNav Header |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Đăng nhập Vendor (shop ACTIVE)<br>2. Truy cập http://localhost:5173/vendor/products<br>3. Chụp riêng phần VendorNav (crop phần trên của trang) |
| **Expected result** | Icon shop + tên shop + badge "Đang hoạt động" (xanh), nút "+ Thêm Sản Phẩm Mới" (cam), 3 tab: Sản Phẩm / Đơn Hàng / Voucher — tab Sản Phẩm đang active (gạch cam) |
| **Caption** | "Hình 3.43. Thanh điều hướng quản lý gian hàng Vendor" |
| **Report location** | Mục 3.6.x.5 — VendorNav |

---

## VS04 — Danh sách Sản phẩm Vendor

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS04 |
| **Tên** | Trang quản lý danh sách sản phẩm |
| **Feature** | Vendor Product List |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Đăng nhập Vendor<br>2. Truy cập http://localhost:5173/vendor/products<br>3. Đảm bảo có ≥4 sản phẩm với các trạng thái khác nhau<br>4. Chụp toàn bộ trang bao gồm VendorNav + bảng sản phẩm |
| **Expected result** | VendorNav + bảng sản phẩm với ≥4 hàng, badge trạng thái màu sắc (xanh ACTIVE, vàng OUT_OF_STOCK, xám HIDDEN), cột hành động Sửa/Xóa |
| **Caption** | "Hình 3.44. Trang quản lý danh sách sản phẩm của Vendor" |
| **Report location** | Mục 3.6.x.6 — Danh sách Sản phẩm |

---

## VS05 — Tạo Sản phẩm mới (Form cơ bản)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS05 |
| **Tên** | Form tạo sản phẩm mới — Phần thông tin cơ bản |
| **Feature** | Create Product Form |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Nhấn "+ Thêm Sản Phẩm Mới" từ VendorNav<br>2. Chụp form tạo mới với phần thông tin cơ bản đang trống hoặc đã điền một phần |
| **Expected result** | Form với các ô: Tên sản phẩm, Mô tả, Giá, Dropdown Danh mục, Dropdown Thương hiệu, Tồn kho |
| **Caption** | "Hình 3.45. Form tạo sản phẩm mới — Thông tin cơ bản" |
| **Report location** | Mục 3.6.x.7 — Tạo Sản phẩm |

---

## VS06 — Tạo Sản phẩm — Upload ảnh

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS06 |
| **Tên** | Form tạo sản phẩm — Upload ảnh Cloudinary |
| **Feature** | Product Image Upload |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào form tạo sản phẩm<br>2. Upload ít nhất 2 ảnh sản phẩm<br>3. Chụp khi đã có 2+ ảnh hiển thị trong grid (mỗi ảnh có nút đặt làm thumbnail và nút xóa) |
| **Expected result** | Grid ảnh đã upload: thumbnail nhỏ hiển thị, ảnh đầu tiên có badge "Ảnh bìa", nút xóa trên mỗi ảnh |
| **Caption** | "Hình 3.46. Upload và quản lý ảnh sản phẩm" |
| **Report location** | Mục 3.6.x.7 — Tạo Sản phẩm |

---

## VS07 — Tạo Sản phẩm — Quản lý biến thể

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS07 |
| **Tên** | Form tạo sản phẩm — Biến thể sản phẩm |
| **Feature** | Product Variants |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào form tạo sản phẩm<br>2. Thêm 2-3 biến thể (VD: màu đỏ size M, màu xanh size L)<br>3. Chụp phần biến thể với bảng biến thể đã tạo + form thêm mới |
| **Expected result** | Bảng biến thể với 2+ hàng (SKU, Giá, Tồn kho, Attributes JSON), form thêm biến thể mới phía dưới |
| **Caption** | "Hình 3.47. Quản lý biến thể sản phẩm (màu sắc, kích thước)" |
| **Report location** | Mục 3.6.x.7 — Tạo Sản phẩm |

---

## VS08 — Chỉnh sửa Sản phẩm (Edit form)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS08 |
| **Tên** | Form chỉnh sửa sản phẩm đã có |
| **Feature** | Edit Product |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Từ danh sách sản phẩm, nhấn nút "Sửa" trên một sản phẩm đã có ảnh và biến thể<br>2. Chụp form đã được pre-fill với dữ liệu hiện tại |
| **Expected result** | Form đã điền sẵn tên, mô tả, giá, danh mục, ảnh hiển thị sẵn, biến thể hiển thị sẵn |
| **Caption** | "Hình 3.48. Form chỉnh sửa thông tin sản phẩm" |
| **Report location** | Mục 3.6.x.7 — Chỉnh sửa Sản phẩm |

---

## VS09 — Quản lý Đơn hàng — Tab Tất cả

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS09 |
| **Tên** | Trang quản lý đơn hàng Vendor — Tab Tất cả |
| **Feature** | Vendor Order Management |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào http://localhost:5173/vendor/orders<br>2. Tab "Tất cả" đang active<br>3. Đảm bảo có ≥3 đơn ở các trạng thái khác nhau<br>4. Chụp toàn bộ trang |
| **Expected result** | VendorNav + tab "Tất cả" active + danh sách đơn hàng với badge màu sắc (vàng/xanh/tím/xanh lá/đỏ) |
| **Caption** | "Hình 3.49. Trang quản lý đơn hàng của Vendor" |
| **Report location** | Mục 3.6.x.8 — Quản lý Đơn hàng |

---

## VS10 — Quản lý Đơn hàng — Nút xử lý đơn CONFIRMED

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS10 |
| **Tên** | Xử lý đơn hàng đã xác nhận — chuyển sang Đang giao |
| **Feature** | Order Status Update (CONFIRMED → SHIPPED) |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào /vendor/orders<br>2. Chọn tab "Chờ xác nhận" (CONFIRMED)<br>3. Chụp một đơn hàng với nút "Giao cho ĐVVC" (xanh tím) + nút "Hủy đơn" (đỏ) |
| **Expected result** | Đơn hàng CONFIRMED với 2 nút action: "Giao cho ĐVVC" và "Hủy đơn" hiển thị rõ |
| **Caption** | "Hình 3.50. Nút xử lý đơn hàng — chuyển trạng thái Đang giao" |
| **Report location** | Mục 3.6.x.8 — Quản lý Đơn hàng |

---

## VS11 — Modal hủy đơn hàng

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS11 |
| **Tên** | Modal xác nhận hủy đơn hàng với lý do |
| **Feature** | Cancel Order Modal |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào /vendor/orders<br>2. Nhấn nút "Hủy đơn" trên một đơn CONFIRMED<br>3. Modal mở — chụp modal đang hiển thị với ô nhập lý do |
| **Expected result** | Modal popup với textarea nhập lý do hủy + nút "Xác nhận hủy" + nút "Đóng" |
| **Caption** | "Hình 3.51. Modal xác nhận hủy đơn hàng" |
| **Report location** | Mục 3.6.x.8 — Quản lý Đơn hàng |

---

## VS12 — Toast thông báo cập nhật trạng thái

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS12 |
| **Tên** | Toast notification sau cập nhật trạng thái đơn |
| **Feature** | Status Update Toast |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Cập nhật trạng thái một đơn hàng (VD: CONFIRMED → SHIPPED)<br>2. Chụp ngay khi toast "Cập nhật trạng thái thành công!" xuất hiện (trong 3 giây) |
| **Expected result** | Toast notification màu xanh lá hoặc cam xuất hiện góc phải/trên với text "Cập nhật trạng thái thành công!" |
| **Caption** | "Hình 3.52. Thông báo cập nhật trạng thái đơn hàng thành công" |
| **Report location** | Mục 3.6.x.8 — Quản lý Đơn hàng |

---

## VS13 — Danh sách Voucher

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS13 |
| **Tên** | Trang quản lý voucher của gian hàng |
| **Feature** | Vendor Voucher List |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào http://localhost:5173/vendor/vouchers<br>2. Đảm bảo có ≥2 voucher đã tạo<br>3. Chụp toàn bộ trang |
| **Expected result** | VendorNav + tab Voucher active + bảng voucher với thông tin: mã, loại, giá trị, thời hạn, số lần dùng + nút "+ Tạo Voucher Mới" (xanh dương) |
| **Caption** | "Hình 3.53. Trang quản lý mã giảm giá của gian hàng" |
| **Report location** | Mục 3.6.x.9 — Quản lý Voucher |

---

## VS14 — Modal tạo Voucher mới

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS14 |
| **Tên** | Modal tạo mã giảm giá mới |
| **Feature** | Create Voucher Modal |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào /vendor/vouchers<br>2. Nhấn "+ Tạo Voucher Mới"<br>3. Modal mở → điền một số thông tin (hoặc để trống)<br>4. Chụp modal đang hiển thị đầy đủ form |
| **Expected result** | Modal với đầy đủ fields: Mã voucher, Loại (radio PERCENTAGE/FIXED), Giá trị, Giảm tối đa, Đơn tối thiểu, Bắt đầu/Kết thúc, Giới hạn sử dụng, nút "Tạo Voucher" + "Hủy" |
| **Caption** | "Hình 3.54. Modal tạo mã giảm giá mới cho gian hàng" |
| **Report location** | Mục 3.6.x.9 — Quản lý Voucher |

---

## VS15 — Phản hồi đánh giá (Vendor Reply)

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS15 |
| **Tên** | Vendor phản hồi đánh giá của khách hàng |
| **Feature** | Review Reply |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Đăng nhập Vendor<br>2. Vào trang chi tiết sản phẩm của chính shop mình (http://localhost:5173/products/:id)<br>3. Scroll xuống phần đánh giá<br>4. Nhấn "Phản hồi đánh giá này" trên một review chưa có reply<br>5. Chụp khi textarea inline đang mở |
| **Expected result** | Textarea "Nhập phản hồi của shop..." + nút "Gửi phản hồi" (xanh) + nút "Hủy" (xám) hiển thị inline dưới review |
| **Caption** | "Hình 3.55. Giao diện Vendor phản hồi đánh giá sản phẩm" |
| **Report location** | Mục 3.6.x.10 — Review Reply |

---

## VS16 — Hiển thị Vendor Reply sau khi phản hồi

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS16 |
| **Tên** | Hiển thị phản hồi của Vendor dưới đánh giá |
| **Feature** | Review Reply Display |
| **Role** | Bất kỳ (Public) |
| **Steps** | 1. Xem trang chi tiết sản phẩm đã có review với vendor reply<br>2. Scroll xuống phần đánh giá có reply<br>3. Chụp phần review + reply block |
| **Expected result** | Review của khách + block "Phản hồi từ Người bán" với border trái xanh, nội dung reply và ngày phản hồi |
| **Caption** | "Hình 3.56. Hiển thị phản hồi của Vendor dưới đánh giá sản phẩm" |
| **Report location** | Mục 3.6.x.10 — Review Reply |

---

## VS17 — Thông báo NEW_ORDER cho Vendor

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS17 |
| **Tên** | Thông báo đơn hàng mới cho Vendor |
| **Feature** | Vendor Notification — NEW_ORDER |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Đăng nhập Vendor<br>2. Có Buyer đặt hàng từ shop này (cần đặt trước)<br>3. Nhấn icon chuông trên header<br>4. Chụp dropdown thông báo với thông báo "Đơn hàng mới" |
| **Expected result** | Dropdown thông báo mở, có thông báo NEW_ORDER với icon + text "Bạn có đơn hàng mới #[id]" |
| **Caption** | "Hình 3.57. Thông báo đơn hàng mới dành cho Vendor" |
| **Report location** | Mục 3.6.x.11 — Thông báo Vendor |

---

## VS18 — Quản lý Đơn hàng — Tab Hoàn thành

| Trường | Nội dung |
|---|---|
| **Screenshot ID** | VS18 |
| **Tên** | Danh sách đơn hàng DELIVERED của Vendor |
| **Feature** | Vendor Order Filter |
| **Role** | ROLE_VENDOR (shop ACTIVE) |
| **Steps** | 1. Vào /vendor/orders<br>2. Nhấn tab "Hoàn thành"<br>3. Chụp danh sách đơn DELIVERED |
| **Expected result** | Tab "Hoàn thành" active (gạch cam), danh sách đơn với badge xanh lá "Hoàn thành", không có nút action |
| **Caption** | "Hình 3.58. Lọc đơn hàng theo trạng thái Hoàn thành" |
| **Report location** | Mục 3.6.x.8 — Quản lý Đơn hàng |

---

## Tổng kết

| Tổng số ảnh | Bắt buộc | Tùy chọn |
|---|---|---|
| 18 | VS01–VS15 (15 ảnh) | VS16–VS18 (3 ảnh) |

### Thứ tự ưu tiên chụp

**Ưu tiên cao (cần nhất):**
VS03 → VS04 → VS05 → VS07 → VS09 → VS10 → VS13 → VS14

**Ưu tiên trung bình:**
VS01 → VS02 → VS06 → VS08 → VS11 → VS15

**Ưu tiên thấp (bổ sung):**
VS12 → VS16 → VS17 → VS18

---

### Lưu ý khi chụp

1. **VS05–VS07** (Form tạo sản phẩm): Có thể chụp trong một lần tạo thực tế, không cần submit
2. **VS12** (Toast): Cần phản xạ nhanh, toast chỉ hiển thị 3 giây — chuẩn bị sẵn tool chụp màn hình
3. **VS15–VS16**: Cần có review của Customer trước, sau đó Vendor vào reply
4. **VS02** (Pending): Cần có tài khoản shop PENDING — nếu không có, có thể tạo shop mới và chụp ngay trước khi Admin duyệt
