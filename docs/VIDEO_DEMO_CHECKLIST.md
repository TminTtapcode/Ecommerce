# VIDEO_DEMO_CHECKLIST.md
# Danh Sách Video Demo — Enterprise Ecommerce

> ⚠️ Chỉ demo features đã được xác minh IMPLEMENTED trong source code.
> Tất cả video demo ở môi trường local development.

---

## VID-01: Customer Purchase Flow (End-to-End)
**Mục đích:** Demo luồng chính của hệ thống từ đầu đến cuối
**Độ ưu tiên:** 🔴 **CAO NHẤT — BẮT BUỘC**
**Thời lượng ước tính:** 3-5 phút

### Kịch bản:
1. **Đăng nhập** tài khoản Buyer
2. **Duyệt sản phẩm** tại `/products` — tìm kiếm với keyword
3. **Xem chi tiết sản phẩm** — xem ảnh gallery, biến thể
4. **Thêm vào giỏ hàng** — 2 sản phẩm từ 2 shops khác nhau
5. **Xem giỏ hàng** — điều chỉnh số lượng
6. **Vào Checkout** — thấy tách đơn theo Shop (Multi-vendor)
7. **Áp dụng Voucher** — nhập mã, thấy giảm giá
8. **Chọn phương thức** — COD (để demo không bị gián đoạn bởi VNPay)
9. **Tạo đơn hàng** thành công
10. **Xem kết quả** và **Xem lịch sử đơn hàng**

**Điểm cần highlight:**
- Tách đơn Multi-vendor (đơn theo Shop)
- Voucher calculation realtime
- Order confirmation

**Cần chuẩn bị:**
- Buyer account sẵn sàng
- 2+ sản phẩm từ 2 shops khác nhau trong DB
- Voucher SYSTEM hợp lệ

**Vị trí trong báo cáo:** 3.7.3 Hình ảnh chạy thực tế

---

## VID-02: VNPay Payment Flow
**Mục đích:** Demo tích hợp cổng thanh toán VNPay
**Độ ưu tiên:** 🔴 **CAO**
**Thời lượng ước tính:** 2-3 phút

### Kịch bản:
1. Đăng nhập Buyer
2. Có sản phẩm trong giỏ hàng (đã chuẩn bị sẵn)
3. **Checkout** → chọn **VNPay**
4. Bấm **Thanh toán** → redirect đến VNPay Sandbox
5. Điền thông tin thẻ test VNPay:
   - Số thẻ: `9704198526191432198`
   - Tên: `NGUYEN VAN A`
   - Ngày phát hành: `07/15`
   - OTP: `123456`
6. Xác nhận thanh toán
7. **Redirect về** `/payment-result` — hiển thị CONFIRMED
8. Vào `/orders` — thấy đơn hàng CONFIRMED

**Điểm cần highlight:**
- Redirect VNPay Sandbox
- IPN webhook xử lý (backend tự động)
- Polling mechanism trên PaymentResultPage
- Orders chuyển sang CONFIRMED

**Cần chuẩn bị:**
- VNPay test card sẵn sàng
- Giỏ hàng có sản phẩm

**Vị trí trong báo cáo:** 3.7.3 (Payment Integration)

---

## VID-03: Vendor Management Flow
**Mục đích:** Demo toàn bộ luồng quản lý của Vendor
**Độ ưu tiên:** 🟠 **TRUNG BÌNH-CAO**
**Thời lượng ước tính:** 3-4 phút

### Kịch bản:
1. **Đăng nhập** tài khoản Vendor (shop đã ACTIVE)
2. **Xem danh sách sản phẩm** `/vendor/products`
3. **Tạo sản phẩm mới** `/vendor/products/new`:
   - Điền thông tin sản phẩm
   - **Upload ảnh** qua Dropzone (Cloudinary) — thấy spinner upload
   - Chọn ảnh Thumbnail
   - Thêm biến thể (size/color)
   - Lưu sản phẩm
4. **Xem danh sách đơn hàng** `/vendor/orders`
5. **Cập nhật trạng thái đơn**: CONFIRMED → SHIPPED
6. **Quản lý Voucher** `/vendor/vouchers` — tạo voucher SHOP
7. **Chat** với khách hàng qua FloatingChatWidget

**Điểm cần highlight:**
- Upload Cloudinary realtime
- State Machine transition CONFIRMED → SHIPPED
- Phân quyền Vendor (không thấy data của shop khác)
- Voucher SHOP scope

**Cần chuẩn bị:**
- Vendor account + ACTIVE shop
- Có đơn hàng CONFIRMED trong DB
- Ảnh sản phẩm để upload

**Vị trí trong báo cáo:** 3.7.3 (Vendor Portal)

---

## VID-04: Admin Management Flow
**Mục đích:** Demo chức năng quản trị hệ thống của Admin
**Độ ưu tiên:** 🟠 **TRUNG BÌNH**
**Thời lượng ước tính:** 2-3 phút

### Kịch bản:
1. **Đăng nhập** tài khoản Admin
2. **Admin Dashboard** — trang tổng quan
3. **Duyệt Gian hàng** `/admin/shop`:
   - Thấy shop có status PENDING
   - Bấm **Approve**
   - Shop chuyển → ACTIVE
   - (Nền tảng: email được gửi qua ShopNotificationListener)
4. **Quản lý Sản phẩm** `/admin/products`
5. **Xem Đơn hàng** `/admin/orders` — xem toàn bộ đơn hàng hệ thống
6. **Xem Chi tiết** đơn hàng `/admin/orders/{id}`
7. **Quản lý Voucher** `/admin/vouchers` — tạo voucher SYSTEM

**Điểm cần highlight:**
- Phân quyền Admin (thấy tất cả shops/orders)
- Shop Approval flow
- View-only Order (ghi chú hạn chế)

**Cần chuẩn bị:**
- Admin account
- Ít nhất 1 shop PENDING
- Dữ liệu đơn hàng đa dạng

**Vị trí trong báo cáo:** 3.7.3 (Admin Portal)

---

## VID-05: Realtime Chat Flow
**Mục đích:** Demo hệ thống nhắn tin thời gian thực
**Độ ưu tiên:** 🔴 **CAO** (unique feature)
**Thời lượng ước tính:** 2-3 phút

### Kịch bản:
**Mở 2 tab trình duyệt (hoặc 2 trình duyệt khác nhau):**
- **Tab 1:** Đăng nhập Buyer
- **Tab 2:** Đăng nhập Vendor (shop owner)

**Demo flow:**
1. **Tab 1 (Buyer):** Vào trang sản phẩm của Shop → bấm **"Chat ngay"**
2. Thấy FloatingChatWidget mở, tạo phòng chat với Shop
3. **Buyer gõ tin nhắn** → gửi → thấy ngay trong bubble (Optimistic UI)
4. **Tab 2 (Vendor):** Thấy tin nhắn xuất hiện realtime (không cần reload)
5. **Vendor trả lời** → Buyer thấy ngay
6. Thấy badge số tin chưa đọc thay đổi
7. Demo **đánh dấu đã đọc** (`PUT /api/v1/chat/rooms/{id}/read`)

**Điểm cần highlight:**
- WebSocket STOMP P2P messaging
- Optimistic UI (0ms latency khi gửi)
- Realtime delivery (không cần reload)
- Unread count badge

**Cần chuẩn bị:**
- Buyer account + Vendor account
- 2 trình duyệt/tab
- Sản phẩm thuộc Shop của Vendor

**Vị trí trong báo cáo:** 3.7.3 (Realtime Features)

---

## VID-06 (Optional): Review Flow
**Mục đích:** Demo hệ thống đánh giá sản phẩm
**Độ ưu tiên:** 🟡 **THẤP-TRUNG BÌNH**
**Thời lượng ước tính:** 1-2 phút

### Kịch bản:
1. Buyer có đơn hàng đã DELIVERED
2. Vào trang chi tiết sản phẩm đã mua
3. Thấy form viết review (eligibility check pass)
4. Điền rating + comment
5. Bấm gửi → review xuất hiện trong danh sách
6. Vendor thấy thông báo "Có đánh giá mới" (Notification)

**Vị trí trong báo cáo:** 3.7.3

---

## TỔNG KẾT VIDEO

| ID | Tên | Thời lượng | Ưu tiên | Bắt buộc |
|---|---|---|---|---|
| VID-01 | Customer Purchase Flow | 3-5 phút | Cao nhất | ✅ |
| VID-02 | VNPay Payment | 2-3 phút | Cao | ✅ |
| VID-03 | Vendor Management | 3-4 phút | Trung bình-Cao | ✅ |
| VID-04 | Admin Management | 2-3 phút | Trung bình | ✅ |
| VID-05 | Realtime Chat | 2-3 phút | Cao | ✅ |
| VID-06 | Review Flow | 1-2 phút | Thấp | ⬜ Optional |
| **TỔNG** | | **13-20 phút** | | |

## HƯỚNG DẪN QUAY VIDEO

**Tools gợi ý:**
- OBS Studio (miễn phí)
- Loom
- ShareX (miễn phí)

**Settings:**
- Resolution: 1920x1080 (1080p)
- FPS: 30
- Format: MP4

**Lưu ý quan trọng:**
- Ẩn thông tin nhạy cảm (password, API key trong .env)
- Giải thích bằng giọng nói hoặc annotation khi demo
- Làm chậm lại ở các bước quan trọng
- Nếu VNPay demo fail → dùng COD thay thế
