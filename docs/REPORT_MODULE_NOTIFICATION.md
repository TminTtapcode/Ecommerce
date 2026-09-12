# BÁO CÁO KIỂM TOÁN MÔ ĐUN: NOTIFICATION (THÔNG BÁO)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Notification chịu trách nhiệm cảnh báo và thông báo trạng thái hoạt động đến người dùng (Cả Người mua, Người bán, và Quản trị viên).

## 2. TRÁCH NHIỆM MÔ ĐUN
- Lắng nghe các sự kiện (Domain Events) phát ra từ các mô đun khác (Ví dụ: Order created, Order shipped, Review posted).
- Lưu trữ thông báo xuống cơ sở dữ liệu.
- Đánh dấu trạng thái Đã đọc (is_read).

## 3. THỰC THỂ (ENTITIES)
- `Notification`: Chứa `user_id`, `title`, `message`, `type` (ORDER_STATUS, REVIEW_POSTED...), `is_read`.

## 4. KIẾN TRÚC EVENT-DRIVEN (GIAO TIẾP DỰA TRÊN SỰ KIỆN)
Hệ thống sử dụng ApplicationEventPublisher của Spring để xuất bản sự kiện. Notification module lắng nghe bằng `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
Thiết kế này đặc biệt quan trọng trong Modular Monolith, giúp mô đun Order không bị phụ thuộc code trực tiếp vào mô đun Notification, và nếu có lỗi khi lưu Notification, thì giao dịch lưu Order chính vẫn thành công (Vì xử lý ở pha AFTER_COMMIT).

## 5. HẠN CHẾ (LIMITATIONS) VÀ GIAO KẾT BE/FE
- Mặc dù hệ thống có Chat thời gian thực (WebSocket), nhưng tính năng Notification hiện tại trên Frontend vẫn đang sử dụng phương pháp **REST Polling** (Gửi request HTTP lên máy chủ mỗi 60 giây để hỏi xem có thông báo mới không). 
- Kỹ thuật Polling tuy đơn giản để triển khai nhưng tiêu tốn tài nguyên máy chủ, khác với WebSocket Push.
