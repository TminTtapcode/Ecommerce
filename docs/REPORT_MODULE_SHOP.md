# BÁO CÁO KIỂM TOÁN MÔ ĐUN: SHOP (GIAN HÀNG)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Shop quản lý thực thể Gian hàng, nơi người bán (Vendor) thiết lập định danh kinh doanh trên sàn thương mại điện tử đa người bán.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Khởi tạo gian hàng cho người dùng thông thường (`ROLE_USER` thành `ROLE_VENDOR`).
- Quản lý quy trình kiểm duyệt gian hàng của Admin.
- Cung cấp API nội bộ (`ShopInternalService`) để các mô đun khác kiểm tra gian hàng bị cấm (Banned).

## 3. THỰC THỂ (ENTITY)
- `Shop`: Liên kết 1-1 với `User` thông qua `user_id` (Unique).
- Enum Trạng thái: `PENDING` (Chờ duyệt), `ACTIVE` (Hoạt động), `BANNED` (Bị cấm).

## 4. LUỒNG NGHIỆP VỤ (WORKFLOW)
- **Đăng ký:** Người dùng điền thông tin Shop. Hệ thống gán trạng thái `PENDING`.
- **Kiểm duyệt (Admin):** Quản trị viên duyệt Shop thành `ACTIVE`. Người dùng tự động được cấp thêm quyền `ROLE_VENDOR` thông qua cơ chế tự động nạp lại Role khi đăng nhập lại hoặc qua token mới.

## 5. PHÂN QUYỀN SỞ HỮU (OWNERSHIP)
Mọi thao tác sửa đổi Shop đều sử dụng `@CurrentUserId` và đối chiếu với cột `user_id` của bản ghi trong database, chặn đứng nguy cơ chiếm quyền điều khiển chéo (IDOR).

## 6. GIAO KẾT BE/FE VÀ HẠN CHẾ
- Chức năng Admin Approve Shop đã được triển khai Backend và Frontend.
- Chức năng Admin Ban Shop (Cấm gian hàng) chưa được xây dựng hoàn thiện trên giao diện Frontend, mặc dù Backend đã hỗ trợ cờ trạng thái `BANNED`.
