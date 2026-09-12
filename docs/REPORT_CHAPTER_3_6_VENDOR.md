## 3.6.x. Thiết kế giao diện hệ thống — Vendor (Người bán)

Phần này mô tả các màn hình dành cho vai trò **Vendor (ROLE_VENDOR)** — người bán hàng trên hệ thống. Vendor truy cập thông qua cùng URL domain với Buyer, sử dụng route `/vendor/*` và layout `PublicLayout` với thanh điều hướng `VendorNav` đặc trưng.

---

### 3.6.x.1. Tổng quan giao diện Vendor

**Bảng 3.59.** Danh sách màn hình Vendor và route

| STT | Tên màn hình | Route | Yêu cầu | File |
|---|---|---|---|---|
| 1 | Đăng ký gian hàng | (modal/component) | ROLE_USER, chưa có shop | `VendorShopRegistrationPage.tsx` |
| 2 | Chờ duyệt gian hàng | (component) | Shop status = PENDING | `ShopPendingPage.tsx` |
| 3 | Gian hàng bị khóa | (component) | Shop status = BANNED | Inline trong `RequireShop.tsx` |
| 4 | Danh sách sản phẩm | `/vendor/products` | ROLE_VENDOR + ACTIVE shop | `VendorProductListPage.tsx` |
| 5 | Tạo sản phẩm | `/vendor/products/new` | ROLE_VENDOR + ACTIVE shop | `VendorProductFormPage.tsx` |
| 6 | Chỉnh sửa sản phẩm | `/vendor/products/:id/edit` | ROLE_VENDOR + ACTIVE shop | `VendorProductFormPage.tsx` |
| 7 | Quản lý đơn hàng | `/vendor/orders` | ROLE_VENDOR + ACTIVE shop | `VendorOrderListPage.tsx` |
| 8 | Quản lý voucher | `/vendor/vouchers` | ROLE_VENDOR + ACTIVE shop | `VendorVoucherListPage.tsx` |

#### Cơ chế điều hướng trạng thái Shop (RequireShop)

Trước khi vào bất kỳ trang Vendor nào, component `RequireShop` kiểm tra trạng thái shop:

```
Vào /vendor/* (ROLE_VENDOR)
    → RequireShop.checkShop() → GET /api/v1/shops/my-shop
        ├── Chưa có shop (400)         → render VendorShopRegistrationPage
        ├── shop.status == PENDING     → render ShopPendingPage
        ├── shop.status == BANNED      → render BannedPage (inline)
        └── shop.status == ACTIVE      → render <children> (trang thực tế)
```

---

### 3.6.x.2. Màn hình Đăng ký Gian hàng

**1. Tên nghiệp vụ:** Đăng ký tài khoản Vendor

**2. Mục đích:** Cho phép User có ROLE_USER tạo đăng ký mở gian hàng để bán hàng.

**3. Màn hình:** Hiển thị khi User có ROLE_VENDOR truy cập `/vendor/products` nhưng chưa có shop. Fullscreen, không có `VendorNav`.

**4. Thành phần giao diện:**
- Tiêu đề "Đăng ký Kênh Người Bán"
- Mô tả ngắn khuyến khích đăng ký
- **Form:** Tên shop (required, unique), Mô tả (tùy chọn, textarea)
- Nút "Đăng Ký Ngay" màu cam
- Thông báo lỗi validation (inline)

**5. User interaction:**
- Điền tên shop + mô tả → nhấn "Đăng Ký Ngay"
- Validation: tên không để trống, không trùng với shop khác (kiểm tra tại server)
- Nếu thành công: `RequireShop` tự động gọi `checkShop()` lại → `shop.status == PENDING` → chuyển sang `ShopPendingPage`

**6. API backend:** `POST /api/v1/shops` — tạo shop mới với `status = PENDING`

**7. Kết quả:** Shop tạo xong, Admin nhận task duyệt, User chuyển sang màn hình chờ duyệt

---

### 3.6.x.3. Màn hình Chờ duyệt Gian hàng

**1. Tên nghiệp vụ:** Trạng thái chờ duyệt

**2. Mục đích:** Thông báo cho Vendor rằng đăng ký đang được xem xét.

**3. Màn hình:** Fullscreen, hiển thị khi `shop.status == PENDING`.

**4. Thành phần giao diện:**
- Icon đồng hồ màu vàng
- Tiêu đề "Đang Chờ Duyệt"
- Mô tả: "Hồ sơ đăng ký Kênh Người Bán của bạn đã được gửi đi và đang chờ Quản trị viên phê duyệt. Quá trình này có thể mất từ 1-2 ngày làm việc."
- Nút "Quay Về Trang Chủ"

**5. User interaction:** Chỉ xem thông tin, không có hành động. Trang tự động cập nhật nếu admin duyệt (cần reload).

**6. API backend:** Không gọi API riêng.

**7. Kết quả:** Vendor biết trạng thái và chờ Admin duyệt

---

### 3.6.x.4. Màn hình Gian hàng bị khóa

**1. Tên nghiệp vụ:** Thông báo gian hàng bị vô hiệu hóa

**2. Mục đích:** Hiển thị khi Admin ban shop vì vi phạm chính sách.

**3. Màn hình:** Fullscreen, hiển thị khi `shop.status == BANNED`.

**4. Thành phần giao diện:**
- Icon vòng tròn gạch chéo màu đỏ
- Tiêu đề "Gian Hàng Bị Khóa"
- Mô tả lý do + hướng dẫn liên hệ hỗ trợ
- Nút "Quay Về Trang Chủ"

---

### 3.6.x.5. Thanh điều hướng Vendor (VendorNav)

`VendorNav` là component header đặc trưng xuất hiện trên tất cả trang Vendor (ACTIVE). Gồm:

**Phần trên — Shop Info:**
- Avatar shop (icon cửa hàng màu cam)
- Tên shop (lấy từ `GET /api/v1/shops/my-shop`)
- Badge trạng thái: "Đang hoạt động" (xanh lá)
- Mô tả ngắn của shop

**Phần nút hành động:**
- Nút "**+ Thêm Sản Phẩm Mới**" (cam) → link `/vendor/products/new`

**Tab điều hướng (3 tab):**
- 📦 Quản Lý Sản Phẩm → `/vendor/products`
- 📋 Quản Lý Đơn Hàng → `/vendor/orders`
- 🏷️ Quản Lý Voucher → `/vendor/vouchers`

Tab active có gạch chân màu cam (`border-orange-500`).

---

### 3.6.x.6. Màn hình Danh sách Sản phẩm Vendor

**1. Tên nghiệp vụ:** Quản lý danh sách sản phẩm của gian hàng

**2. Mục đích:** Hiển thị toàn bộ sản phẩm của shop, cho phép tìm kiếm, lọc trạng thái, và thực hiện thao tác nhanh.

**3. Màn hình:** `/vendor/products`

**4. Thành phần giao diện:**
- `VendorNav` ở trên cùng (tên shop, tabs)
- **Thanh filter:** Ô tìm kiếm theo tên, dropdown filter trạng thái (Tất cả / ACTIVE / OUT_OF_STOCK / HIDDEN)
- **Bảng sản phẩm (Table):** Mỗi hàng gồm: Ảnh thumbnail, Tên sản phẩm, SKU đại diện, Giá, Số biến thể, Tồn kho, Trạng thái (badge màu), Cột hành động
- **Cột hành động:** Nút "Sửa" → `/vendor/products/:id/edit`, Nút "Xóa" (xác nhận)
- **Phân trang:** Cuối bảng

**5. User interaction:**
- Tìm kiếm theo tên → filter realtime hoặc server-side
- Filter theo trạng thái → fetch lại danh sách
- Nhấn "Sửa" → navigate đến form chỉnh sửa
- Nhấn "Xóa" → confirm dialog → `DELETE /api/v1/vendor/products/:id` → sản phẩm HIDDEN (soft delete)
- Nhấn "+ Thêm Sản Phẩm Mới" (trên VendorNav) → navigate đến form tạo mới

**6. API backend:**
- `GET /api/v1/vendor/products?page=&size=&status=` — danh sách sản phẩm của shop hiện tại

**7. Kết quả:** Vendor xem và quản lý toàn bộ sản phẩm của mình

---

### 3.6.x.7. Màn hình Tạo / Chỉnh sửa Sản phẩm

**1. Tên nghiệp vụ:** Tạo mới hoặc chỉnh sửa sản phẩm

**2. Mục đích:** Form đa bước cho phép Vendor nhập đầy đủ thông tin sản phẩm bao gồm thông tin cơ bản, biến thể động, và ảnh.

**3. Màn hình:** `/vendor/products/new` (tạo mới) và `/vendor/products/:id/edit` (chỉnh sửa). Dùng chung component `VendorProductFormPage`.

**4. Thành phần giao diện:**

**Phần 1 — Thông tin cơ bản:**
- Tên sản phẩm (required)
- Mô tả (textarea)
- Giá cơ bản (DECIMAL)
- Danh mục (dropdown từ `GET /api/v1/categories`)
- Thương hiệu (dropdown từ `GET /api/v1/brands`, optional)
- Tổng tồn kho (số nguyên)

**Phần 2 — Quản lý ảnh sản phẩm:**
- Nút "Upload ảnh" → input file → upload lên Cloudinary qua `POST /api/v1/media/upload`
- Spinner khi đang upload (`isUploading` state)
- Grid ảnh đã upload: thumbnail nhỏ, nút đặt làm ảnh bìa, nút xóa ảnh
- Ảnh đầu tiên upload tự động đặt làm thumbnail (`isThumbnail = true`)

**Phần 3 — Quản lý biến thể (Variants):**
- Danh sách biến thể hiện tại (bảng): SKU, Giá, Tồn kho, Attributes, Trạng thái, nút Xóa
- Form thêm biến thể mới: SKU, Giá biến thể, Tồn kho biến thể, JSON Attributes (ví dụ: `{"color":"Đỏ","size":"L"}`)
- Nút "Thêm Biến Thể"

**Nút hành động:**
- "Lưu Sản Phẩm" (xanh lá hoặc cam) → submit toàn bộ form
- "Hủy" → quay lại danh sách

**5. User interaction:**
- Điền thông tin cơ bản
- Upload ảnh (có thể nhiều ảnh, ảnh đầu tiên = thumbnail)
- Thêm ít nhất 1 biến thể với SKU unique
- Nhấn "Lưu Sản Phẩm" → POST hoặc PUT

**Khi chỉnh sửa:**
- Form pre-filled với dữ liệu từ `GET /api/v1/vendor/products/:id`
- Ảnh và biến thể hiện tại hiển thị sẵn
- Có thể thêm/xóa ảnh và biến thể

**6. API backend:**
- `POST /api/v1/vendor/products` — tạo mới
- `PUT /api/v1/vendor/products/:id` — cập nhật
- `POST /api/v1/media/upload` — upload ảnh lên Cloudinary
- `GET /api/v1/categories` — lấy danh sách danh mục
- `GET /api/v1/brands` — lấy danh sách thương hiệu

**7. Kết quả:** Sản phẩm được tạo/cập nhật với đầy đủ biến thể và ảnh; ngay lập tức hiển thị trên public catalog nếu `status = ACTIVE`

---

### 3.6.x.8. Màn hình Quản lý Đơn hàng Vendor

**1. Tên nghiệp vụ:** Xem và xử lý đơn hàng của gian hàng

**2. Mục đích:** Cho phép Vendor xem tất cả đơn hàng từ shop của mình, lọc theo trạng thái, và cập nhật tiến trình xử lý đơn.

**3. Màn hình:** `/vendor/orders`

**4. Thành phần giao diện:**
- `VendorNav` ở trên
- **Tab bar (Status Filter):** Tất cả / Chờ thanh toán / Chờ xác nhận / Đang giao / Hoàn thành / Đã hủy
- **Danh sách đơn hàng (bảng/card):** Mỗi đơn gồm: Mã đơn (#id), Thời gian đặt, Thông tin người mua (userId), Danh sách sản phẩm (tên + số lượng + giá snapshot), Tổng tiền, Trạng thái (badge màu), Cột hành động
- **Toast notification:** "Cập nhật trạng thái thành công!" xuất hiện 3 giây sau mỗi action thành công
- **Modal hủy đơn:** Khi nhấn "Hủy đơn" → modal popup yêu cầu nhập lý do hủy
- **Phân trang:** 10 đơn/trang

**5. User interaction và luồng cập nhật trạng thái:**

Mỗi đơn hàng có các nút hành động phụ thuộc trạng thái hiện tại:

| Trạng thái hiện tại | Hành động Vendor | Trạng thái mới |
|---|---|---|
| `CONFIRMED` | Nhấn "Giao cho ĐVVC" | `SHIPPED` |
| `CONFIRMED` | Nhấn "Hủy đơn" (cần lý do) | `CANCELLED` |
| `SHIPPED` | Nhấn "Xác nhận đã giao" | `DELIVERED` |
| `PENDING` | Không có nút (chờ payment) | — |
| `DELIVERED` | Không có nút | — |
| `CANCELLED` | Không có nút | — |

> **Lưu ý:** Trạng thái `PENDING → CONFIRMED` được cập nhật **tự động** bởi hệ thống khi thanh toán thành công (VNPay IPN), không phải thủ công bởi Vendor.

**6. API backend:**
- `GET /api/v1/vendor/orders?page=&size=&status=` — danh sách đơn của shop
- `PATCH /api/v1/vendor/orders/:id/status` — cập nhật trạng thái `{ status, reason }`

**7. Kết quả:** Vendor theo dõi và xử lý đơn hàng theo đúng quy trình vận hành

---

### 3.6.x.9. Màn hình Quản lý Voucher Vendor

**1. Tên nghiệp vụ:** Tạo và quản lý mã giảm giá của gian hàng

**2. Mục đích:** Vendor tạo voucher scope=SHOP để khuyến mãi cho khách hàng của mình.

**3. Màn hình:** `/vendor/vouchers`

**4. Thành phần giao diện:**

**Danh sách voucher (bảng):**
- Mã voucher, Loại (PERCENTAGE/FIXED_AMOUNT), Giá trị, Ngày bắt đầu/kết thúc, Giới hạn sử dụng, Số lần đã dùng, Trạng thái (còn hiệu lực/hết hạn)

**Nút "+ Tạo Voucher Mới"** (xanh dương `indigo-600`) → mở modal

**Modal tạo voucher (form):**
- Mã voucher (text, required)
- Loại giảm giá: radio PERCENTAGE (%) / FIXED_AMOUNT (VND)
- Giá trị giảm (number)
- Giảm tối đa (chỉ hiện khi PERCENTAGE) — optional
- Đơn hàng tối thiểu — optional
- Thời gian: Bắt đầu (datetime-local), Kết thúc (datetime-local)
- Giới hạn số lần dùng (number, optional — NULL = không giới hạn)
- Nút "Tạo Voucher" / "Hủy"

**Phân trang:** 10 voucher/trang

**5. User interaction:**
- Nhấn "Tạo Voucher Mới" → modal mở
- Điền form → nhấn "Tạo Voucher" → `POST /api/v1/vouchers`
- Nếu thành công: modal đóng, danh sách reload

**6. API backend:**
- `GET /api/v1/vendor/vouchers?page=&size=` — danh sách voucher của shop
- `POST /api/v1/vouchers` — tạo voucher mới với `scope = SHOP`, `shopId` tự động từ JWT

**7. Kết quả:** Voucher được tạo với scope SHOP; Customer áp dụng được tại checkout nếu trong thời hạn và điều kiện hợp lệ

---

### 3.6.x.10. Tính năng phản hồi đánh giá (Review Reply)

**1. Tên nghiệp vụ:** Vendor phản hồi đánh giá của Customer

**2. Mục đích:** Vendor trả lời từng đánh giá của người mua trực tiếp trên trang chi tiết sản phẩm.

**3. Màn hình:** `/products/:id#reviews` — Khi Vendor đăng nhập và xem trang sản phẩm của mình

**4. Thành phần giao diện (trong `ProductReviewSection`):**
- Mỗi review hiển thị nội dung + rating
- Nếu Vendor chưa reply và đang xem trang sản phẩm của shop mình: hiện nút **"Phản hồi đánh giá này"** (text link màu xanh)
- Nhấn nút → mở inline textarea "Nhập phản hồi của shop..."
- Nút "Gửi phản hồi" (xanh dương) / "Hủy"
- Sau khi reply: hiển thị block quote "Phản hồi từ Người bán" với border trái xanh

**5. User interaction:**
- Vendor vào trang sản phẩm (public URL `/products/:id`) → cuộn xuống phần đánh giá
- Nhấn "Phản hồi đánh giá này" → textarea mở inline
- Nhập reply → nhấn "Gửi phản hồi"
- Reply hiện ngay, nút "Phản hồi" biến mất (chỉ reply 1 lần/review)

**6. API backend:**
- `PUT /api/v1/reviews/:id/vendor-reply` — `{ vendorReply: string }`

**7. Kết quả:** Reply hiển thị công khai trên trang sản phẩm, tạo uy tín cho gian hàng

---

### 3.6.x.11. Thông báo cho Vendor

Vendor nhận thông báo qua cùng cơ chế `NotificationDropdown` ở Header:

**Bảng 3.60.** Các loại thông báo Vendor nhận

| Loại | Trigger | Nội dung |
|---|---|---|
| `NEW_ORDER` | Buyer đặt hàng thành công | "Bạn có đơn hàng mới #[orderId]" |
| `NEW_REVIEW` | Buyer viết đánh giá sản phẩm | "Sản phẩm X nhận được đánh giá mới" |

Thông báo được tạo bởi `@TransactionalEventListener(AFTER_COMMIT)` — gửi sau khi transaction commit thành công, đảm bảo không mất thông báo nếu rollback.

---

### 3.6.x.12. Tổng hợp trạng thái hoàn thiện

**Bảng 3.61.** Trạng thái hoàn thiện các tính năng Vendor

| Tính năng | Trạng thái | Ghi chú |
|---|---|---|
| Đăng ký gian hàng | ✅ Hoàn thiện | Form + API hoạt động |
| Chờ duyệt / Bị khóa | ✅ Hoàn thiện | UI đầy đủ, logic đúng |
| VendorNav (header) | ✅ Hoàn thiện | Hiển thị tên shop, tabs |
| Danh sách sản phẩm | ✅ Hoàn thiện | Filter, pagination hoạt động |
| Tạo sản phẩm + ảnh + biến thể | ✅ Hoàn thiện | Upload Cloudinary, JSON attributes |
| Chỉnh sửa sản phẩm | ✅ Hoàn thiện | Pre-fill, update ảnh/biến thể |
| Quản lý đơn hàng | ✅ Hoàn thiện | Tab filter, update status, cancel modal |
| Tạo voucher | ✅ Hoàn thiện | Modal form, PERCENTAGE/FIXED |
| Phản hồi đánh giá | ✅ Hoàn thiện | Inline reply form |
| Thông báo Vendor | ✅ Hoàn thiện | NEW_ORDER, NEW_REVIEW qua AFTER_COMMIT |
| Thống kê doanh thu | ❌ Chưa triển khai | Không có màn hình dashboard doanh thu |
| Quản lý thông tin shop | ❌ Chưa triển khai | Không có trang edit shop info |

---

*Kết thúc phần 3.6.x — Giao diện Vendor.*
