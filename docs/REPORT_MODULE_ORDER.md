# BÁO CÁO KIỂM TOÁN MÔ ĐUN: ORDER (ĐƠN HÀNG)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Order là trái tim của quy trình giao dịch (Transaction), đóng vai trò biên dịch sản phẩm từ Giỏ hàng thành một ràng buộc tài chính vĩnh viễn (Snapshot) giữa Người mua và Người bán.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Tách giỏ hàng đa shop thành các đơn hàng độc lập (Order Splitting).
- Snapshot dữ liệu (Lưu lại giá bán, tên sản phẩm vĩnh viễn vào thời điểm mua).
- Trừ tồn kho vật lý (Inventory deduction).
- Quản lý vòng đời trạng thái đơn hàng (State Machine).
- Ghi vết (Audit trail) các hành động can thiệp của Quản trị viên (Admin).

## 3. THỰC THỂ (ENTITIES)
- `Order`: Lưu `total_amount`, `shop_id`, `user_id`, `payment_group_id` (UUID).
- `OrderItem`: Lưu `product_name`, `unit_price`, `quantity`, `sub_total`. Đóng vai trò Snapshot dữ liệu để không bị ảnh hưởng nếu sản phẩm gốc bị đổi tên/giá sau này.
- `OrderAdminStatusHistory`: Lưu lịch sử thay đổi trạng thái của Admin (`reason`, `old_status`, `new_status`).

## 4. QUY TRÌNH CHECKOUT ĐA GIAN HÀNG (MULTI-VENDOR SPLIT)
Đây là quy trình phức tạp nhất hệ thống:
1. Nhận mảng `cartItemIds` từ Request.
2. Group các items theo `shop_id`.
3. Tạo 1 UUID chung duy nhất gọi là `payment_group_id`.
4. Sinh ra N Đơn hàng (`Order`), mỗi đơn đại diện cho 1 Shop, nhưng cùng chia sẻ chung 1 `payment_group_id`.
5. Tính tổng giá trị và trừ Voucher (gọi sang Voucher Module).
6. Chuyển quyền thanh toán sang Mô đun Payment (Bằng cách phát sinh `Payment` record theo `payment_group_id`).

## 5. VÒNG ĐỜI ĐƠN HÀNG (STATE MACHINE)
Trạng thái di chuyển theo hướng 1 chiều:
`PENDING` (Chờ thanh toán) -> `CONFIRMED` (Shop đã duyệt) -> `SHIPPED` (Đang giao) -> `DELIVERED` (Đã nhận hàng).
- Trạng thái `CANCELLED` (Đã hủy) có thể được kích hoạt từ `PENDING` hoặc `CONFIRMED`.

## 6. HẠN CHẾ (LIMITATIONS)
- Khi đơn hàng bị `CANCELLED`, hệ thống hiện tại KHÔNG thực hiện rollback (hoàn trả) lại số lượng `stock_quantity` vào mô đun Product. Đây là điểm yếu cần cải tiến.
- Tính năng Admin can thiệp đơn hàng đã được thiết kế tại Backend qua `OrderAdminStatusHistory` nhưng giao diện Frontend chỉ mới hỗ trợ View-only, thiếu nút thao tác (Force Cancel).
