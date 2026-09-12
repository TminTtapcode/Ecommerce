# BÁO CÁO KIỂM TOÁN MÔ ĐUN: REVIEW (ĐÁNH GIÁ)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Review cho phép người mua phản hồi về chất lượng sản phẩm, kèm hình ảnh minh họa, sau khi giao dịch hoàn tất.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Xác thực quyền được phép đánh giá (Phải là người đã mua và đơn hàng đã giao thành công).
- Ngăn chặn Spam đánh giá trùng lặp.
- Hỗ trợ người bán (Vendor) phản hồi lại bình luận của người mua.

## 3. THỰC THỂ (ENTITIES)
- `Review`: Lưu `rating` (1-5 sao), `comment`, `vendor_reply`.
- `review_images`: Bảng phụ (`@ElementCollection`) lưu danh sách URL ảnh đi kèm đánh giá.

## 4. QUY TẮC NGHIỆP VỤ BẮT BUỘC (BUSINESS RULES)
1. Ràng buộc cơ sở dữ liệu `UNIQUE (user_id, order_id, product_id)`: Đảm bảo khách hàng chỉ được đánh giá 1 lần duy nhất cho mỗi sản phẩm trong 1 đơn hàng cụ thể.
2. Kiểm tra trạng thái đơn: Hệ thống gọi sang `OrderInternalService` để đảm bảo đơn hàng có chứa sản phẩm đó và đang ở trạng thái `DELIVERED`.

## 5. TÍCH HỢP EVENT-DRIVEN (SỰ KIỆN)
Khi người dùng đăng Review mới, hệ thống xuất bản một Event trong bộ nhớ (Memory Event). Mô đun Notification sẽ lắng nghe sự kiện này qua cờ `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` để gửi thông báo cho Chủ Shop mà không làm trì hoãn hay lỗi transaction lưu Review của khách hàng.
