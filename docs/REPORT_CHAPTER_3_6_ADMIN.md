## 3.6.y. Thiết kế giao diện hệ thống — Admin (Quản trị viên)

Phần này mô tả các màn hình dành cho vai trò **Admin (ROLE_ADMIN)** — quản trị viên hệ thống. Admin sử dụng layout riêng biệt (`AdminLayout`) với sidebar cố định bên trái, truy cập qua route `/admin/*`.

---

### 3.6.y.1. Tổng quan giao diện Admin

**Bảng 3.62.** Danh sách màn hình Admin và route

| STT | Tên màn hình | Route | File |
|---|---|---|---|
| 1 | Dashboard Tổng quan | `/admin` | Inline trong `App.tsx` |
| 2 | Danh sách sản phẩm | `/admin/products` | `AdminProductListPage.tsx` |
| 3 | Tạo sản phẩm | `/admin/products/create` | `AdminProductFormPage.tsx` |
| 4 | Chỉnh sửa sản phẩm | `/admin/products/:id/edit` | `AdminProductFormPage.tsx` |
| 5 | Quản lý gian hàng | `/admin/shop` | `AdminShopListPage.tsx` |
| 6 | Danh sách đơn hàng | `/admin/orders` | `AdminOrderListPage.tsx` |
| 7 | Chi tiết đơn hàng | `/admin/orders/:id` | `AdminOrderDetailPage.tsx` |
| 8 | Quản lý voucher | `/admin/vouchers` | `AdminVoucherListPage.tsx` |

**Kiểm soát truy cập:** Route `/admin` bọc trong `<ProtectedRoute requireAdmin={true}>` — chỉ user với `ROLE_ADMIN` mới truy cập được. Người dùng thường bị redirect về `/login`.

---

### 3.6.y.2. Layout Admin (AdminLayout)

`AdminLayout` là layout riêng biệt, hoàn toàn tách khỏi `PublicLayout` của Customer/Vendor. Gồm 2 vùng chính:

**Sidebar bên trái (collapsible):**
- Logo "EC Admin" (cam)
- Nút toggle thu gọn/mở rộng sidebar (hamburger icon)
- Menu điều hướng 4 mục với icon SVG:
  - 🏠 **Tổng quan** → `/admin`
  - 📦 **Sản phẩm** → `/admin/products`
  - 📋 **Đơn hàng** → `/admin/orders`
  - 🏪 **Cửa hàng** → `/admin/shop`
  - 🏷️ **Voucher** → `/admin/vouchers`
- Menu item active có nền cam nhạt (`bg-orange-50`) + text cam
- Khi thu gọn: chỉ hiện icon, tooltip khi hover

**Topbar (header cố định trên cùng):**
- Ô tìm kiếm nhanh (UI_ONLY — không có chức năng backend)
- `NotificationDropdown` — icon chuông thông báo (dùng chung component)
- Avatar "AD" (chữ tắt Admin) + dropdown: "Về trang chủ" / "Đăng xuất"

---

### 3.6.y.3. Màn hình Dashboard Tổng quan

**1. Tên nghiệp vụ:** Tổng quan hệ thống

**2. Màn hình:** `/admin`

**3. Thành phần giao diện:**
```
Tổng quan Dashboard
Chào mừng bạn đến với trang quản trị.
```

> **⚠️ Trạng thái:** Màn hình Dashboard chỉ hiển thị tiêu đề chào mừng đơn giản. **Chưa có widget thống kê** (doanh thu, đơn hàng, người dùng). Đây là placeholder — Dashboard thực sự **chưa được triển khai**.

**4. API backend:** Không gọi API nào.

---

### 3.6.y.4. Màn hình Quản lý Gian hàng (Shop Management)

**1. Tên nghiệp vụ:** Duyệt và quản lý trạng thái gian hàng

**2. Mục đích:** Admin xem tất cả gian hàng đã đăng ký, lọc theo trạng thái, duyệt (PENDING → ACTIVE) hoặc ban (ACTIVE → BANNED).

**3. Màn hình:** `/admin/shop`

**4. Thành phần giao diện:**
- **Tiêu đề:** "Quản lý Cửa hàng"
- **Filter bar:** Dropdown lọc theo trạng thái: Tất cả / PENDING / ACTIVE / BANNED
- **Bảng danh sách Shop:** Mỗi hàng gồm:
  - ID Shop, Tên Shop, Mô tả (rút gọn), User ID (chủ shop)
  - Trạng thái (badge màu): PENDING (vàng) / ACTIVE (xanh lá) / BANNED (đỏ)
  - Cột hành động phụ thuộc trạng thái hiện tại
- **Phân trang:** Cuối bảng

**5. Nút hành động theo trạng thái:**

| Trạng thái Shop | Nút hành động | API gọi |
|---|---|---|
| `PENDING` | ✅ "Duyệt" (xanh lá) + ❌ "Từ chối" (đỏ) | `PUT /api/v1/admin/shops/:id/approve` / `/reject` |
| `ACTIVE` | 🔒 "Khoá" (đỏ) | `PUT /api/v1/admin/shops/:id/ban` |
| `BANNED` | ✅ "Mở khóa" (xanh) | `PUT /api/v1/admin/shops/:id/approve` |

**6. Luồng duyệt shop (quan trọng):**
```
Admin nhấn "Duyệt" (PENDING shop)
→ PUT /api/v1/admin/shops/:id/approve
→ Backend: shop.status = ACTIVE
→ ShopApprovedEvent published
→ @TransactionalEventListener(AFTER_COMMIT):
    - User nhận thêm ROLE_VENDOR
    - Email thông báo gửi qua Gmail SMTP
→ Frontend reload danh sách, badge chuyển sang ACTIVE xanh
```

**7. API backend:**
- `GET /api/v1/admin/shops?page=&size=&status=` — danh sách shops
- `PUT /api/v1/admin/shops/:id/approve` — duyệt / mở khóa
- `PUT /api/v1/admin/shops/:id/ban` — khóa gian hàng

**8. Kết quả:** Khi duyệt: chủ shop nhận ROLE_VENDOR + email xác nhận → có thể đăng bán ngay

---

### 3.6.y.5. Màn hình Danh sách Sản phẩm (Admin)

**1. Tên nghiệp vụ:** Xem và kiểm duyệt sản phẩm toàn hệ thống

**2. Mục đích:** Admin xem tất cả sản phẩm từ mọi gian hàng, tìm kiếm, và quản lý.

**3. Màn hình:** `/admin/products`

**4. Thành phần giao diện:**
- **Thanh tìm kiếm:** Tìm theo tên sản phẩm
- **Nút "+ Thêm sản phẩm"** → `/admin/products/create`
- **Bảng sản phẩm:** Mỗi hàng gồm: ảnh thumbnail, tên, danh mục, giá, tồn kho, trạng thái (badge), Shop ID nguồn, Cột hành động (Sửa / Xóa)
- **Phân trang**

**5. User interaction:**
- Tìm kiếm → filter theo keyword
- Nhấn "Sửa" → navigate `/admin/products/:id/edit`
- Nhấn "Xóa" → confirm → xóa mềm (status = HIDDEN)
- Nhấn "+ Thêm sản phẩm" → navigate `/admin/products/create`

**6. API backend:**
- `GET /api/v1/products?keyword=&page=&size=` — danh sách sản phẩm (public endpoint, Admin xem tất cả)
- `DELETE /api/v1/admin/products/:id` — xóa / ẩn sản phẩm

**7. Kết quả:** Admin có thể kiểm soát toàn bộ catalog sản phẩm

---

### 3.6.y.6. Màn hình Tạo / Chỉnh sửa Sản phẩm (Admin)

**1. Tên nghiệp vụ:** Tạo hoặc chỉnh sửa sản phẩm (Admin level)

**2. Mục đích:** Admin có thể tạo sản phẩm mới hoặc chỉnh sửa bất kỳ sản phẩm nào trên hệ thống.

**3. Màn hình:** `/admin/products/create` và `/admin/products/:id/edit`

**4. Thành phần giao diện:** Tương tự `VendorProductFormPage` với cùng chức năng:
- **Thông tin cơ bản:** Tên, Mô tả, Giá, Tồn kho, Danh mục (dropdown), Thương hiệu
- **Upload ảnh:** Nhiều ảnh, Cloudinary CDN, chọn ảnh bìa
- **Quản lý biến thể:** Thêm/xóa biến thể với SKU, giá, tồn kho, JSON attributes
- **Nút:** "Lưu" / "Hủy" → về danh sách

**Khác biệt với form Vendor:**
- Admin có thể chỉnh sửa sản phẩm của **bất kỳ shop nào** (không giới hạn theo shopId của người dùng)
- Admin form hỗ trợ upload nhiều ảnh cùng lúc (`multiple` file input) — trong khi Vendor form upload từng ảnh

**5. API backend:**
- `POST /api/v1/admin/products` — tạo mới
- `PUT /api/v1/admin/products/:id` — cập nhật
- `POST /api/v1/media/upload` — upload ảnh Cloudinary

---

### 3.6.y.7. Màn hình Danh sách Đơn hàng (Admin)

**1. Tên nghiệp vụ:** Theo dõi tất cả đơn hàng trong hệ thống

**2. Mục đích:** Admin xem, filter, tìm kiếm và điều tra toàn bộ đơn hàng từ mọi gian hàng.

**3. Màn hình:** `/admin/orders`

**4. Thành phần giao diện:**
- **Filter bar:** Dropdown lọc theo trạng thái (Tất cả / PENDING / CONFIRMED / SHIPPED / DELIVERED / CANCELLED)
- **Bảng đơn hàng:** Mỗi hàng gồm: ID đơn, User ID, Shop ID, Tổng tiền, Phương thức thanh toán, Trạng thái (badge màu), Ngày tạo, Nút "Xem chi tiết"
- **Phân trang:** 10 đơn/trang

**5. User interaction:**
- Chọn filter trạng thái → fetch lại danh sách
- Nhấn "Xem chi tiết" → navigate `/admin/orders/:id`
- Chuyển trang

**6. API backend:**
- `GET /api/v1/admin/orders?page=&size=&status=` — tất cả đơn hàng toàn hệ thống

**7. Kết quả:** Admin theo dõi hoạt động giao dịch toàn hệ thống

---

### 3.6.y.8. Màn hình Chi tiết Đơn hàng (Admin)

**1. Tên nghiệp vụ:** Xem chi tiết đầy đủ một đơn hàng

**2. Mục đích:** Admin xem toàn bộ thông tin đơn hàng bao gồm thông tin khách hàng, gian hàng, sản phẩm (snapshot), địa chỉ giao hàng.

**3. Màn hình:** `/admin/orders/:id`

**4. Thành phần giao diện:**
- **Header:** Tiêu đề "Chi tiết Đơn Hàng #[id]" + nút "← Quay lại"
- **Grid 2 cột thông tin:**
  - *Cột trái — Thông tin Khách hàng & Shop:* User ID, Tên khách hàng (customerName), Shop ID
  - *Cột phải — Thông tin Đơn hàng:* Trạng thái (badge), Phương thức thanh toán, Tổng tiền, Địa chỉ giao hàng, Ngày tạo
- **Bảng sản phẩm trong đơn:** Tên sản phẩm (snapshot), Đơn giá (snapshot), Số lượng, Thành tiền
- Nút quay lại danh sách

**5. User interaction:** Chỉ xem, không có hành động chỉnh sửa trạng thái từ Admin dashboard (trạng thái đơn chỉ được cập nhật bởi Vendor).

**6. API backend:**
- `GET /api/v1/admin/orders/:id` — chi tiết đơn hàng

---

### 3.6.y.9. Màn hình Quản lý Voucher (Admin)

**1. Tên nghiệp vụ:** Tạo và quản lý voucher hệ thống (scope=SYSTEM)

**2. Mục đích:** Admin tạo voucher toàn hệ thống — áp dụng được cho tất cả gian hàng, không giới hạn shop.

**3. Màn hình:** `/admin/vouchers`

**4. Thành phần giao diện:**

**Bảng danh sách Voucher:**
- Mã voucher, Scope (SYSTEM/SHOP), Loại (PERCENTAGE/FIXED_AMOUNT), Giá trị, Điều kiện (tối đa giảm / đơn tối thiểu), Thời hạn (Từ / Đến), Số lần dùng (`usedCount/usageLimit`)
- Phân trang

**Nút "+ Tạo Voucher Hệ Thống"** (xanh tím `indigo-600`) → mở modal

**Modal tạo Voucher Hệ Thống — Form đầy đủ:**
- Mã voucher (tự động uppercase)
- Loại giảm giá: dropdown PERCENTAGE (%) / FIXED_AMOUNT (VND) / **FREE_SHIPPING** (Miễn phí vận chuyển — tùy chọn thêm so với Vendor form)
- Mức giảm (số)
- Tối đa giảm (optional)
- Đơn tối thiểu (optional)
- Bắt đầu / Kết thúc (datetime-local)
- Giới hạn lần dùng (optional, NULL = không giới hạn)

**5. User interaction:**
- Nhấn tạo → modal mở → điền form → nhấn "Tạo Voucher" → `POST /api/v1/vouchers`
- Voucher tạo với `scope = SYSTEM` — không gắn với shop nào
- Danh sách reload sau khi tạo thành công

**6. API backend:**
- `GET /api/v1/admin/vouchers?page=&size=` — danh sách voucher (cả SYSTEM và SHOP)
- `POST /api/v1/vouchers` — tạo voucher mới với `scope = SYSTEM`

**7. Kết quả:** Voucher SYSTEM có thể được áp dụng bởi bất kỳ Buyer nào khi checkout, không giới hạn gian hàng

---

### 3.6.y.10. Tổng hợp trạng thái hoàn thiện

**Bảng 3.63.** Trạng thái hoàn thiện các tính năng Admin

| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| AdminLayout (sidebar + topbar) | ✅ Hoàn thiện | Sidebar collapsible, Notification dropdown |
| Dashboard Tổng quan | ⚠️ Placeholder | Chỉ có text chào mừng, chưa có widget thống kê |
| Ô tìm kiếm nhanh (topbar) | ⚠️ UI_ONLY | Input có UI nhưng chưa có chức năng tìm kiếm |
| Quản lý gian hàng | ✅ Hoàn thiện | Duyệt/Từ chối/Khóa/Mở khóa — event email |
| Danh sách sản phẩm (Admin) | ✅ Hoàn thiện | Xem, tìm kiếm, xóa |
| Tạo/Chỉnh sửa sản phẩm (Admin) | ✅ Hoàn thiện | Tương đương Vendor form + multi-upload ảnh |
| Danh sách đơn hàng (Admin) | ✅ Hoàn thiện | Filter trạng thái, phân trang |
| Chi tiết đơn hàng (Admin) | ✅ Hoàn thiện | Xem đầy đủ, không có action chỉnh sửa |
| Tạo voucher hệ thống | ✅ Hoàn thiện | Scope=SYSTEM, đầy đủ các option |
| Xem voucher của shop | ✅ Hoàn thiện | Admin xem cả voucher SYSTEM và SHOP |
| Thống kê doanh thu | ❌ Chưa triển khai | Không có biểu đồ hay số liệu tổng hợp |
| Quản lý người dùng | ❌ Chưa triển khai | Không có màn hình user list / user detail |
| Quản lý danh mục | ❌ Chưa triển khai | Không có màn hình CRUD Category |
| Quản lý thương hiệu | ❌ Chưa triển khai | Không có màn hình CRUD Brand |

---

*Kết thúc phần 3.6.y — Giao diện Admin.*
