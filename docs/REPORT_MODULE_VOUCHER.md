# BÁO CÁO KIỂM TOÁN MÔ ĐUN: VOUCHER (KHUYẾN MÃI)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Voucher xử lý các chiến dịch giảm giá. Hệ thống hỗ trợ giảm giá theo % (PERCENTAGE) hoặc số tiền cố định (FIXED_AMOUNT).

## 2. TRÁCH NHIỆM MÔ ĐUN
- Cho phép Admin tạo mã giảm giá toàn sàn (SYSTEM scope).
- Cho phép Vendor tạo mã giảm giá riêng biệt cho shop (SHOP scope).
- Chia đều (Proportional Split) mức giảm giá xuống cấp độ từng Đơn hàng (Order) trong một phiên Checkout đa gian hàng.

## 3. THỰC THỂ (ENTITIES)
- `Voucher`: Lưu mã `code`, loại (`PERCENTAGE`, `FIXED_AMOUNT`), giới hạn (`usage_limit`), và số lần đã dùng (`used_count`).

**Lưu ý Audit:** Hệ thống KHÔNG sử dụng bảng trung gian `voucher_usage` để đếm như trong các tài liệu nháp cũ. Logic trừ mã được thực hiện bằng cách tăng tuyến tính cột `used_count`.

## 4. LUỒNG PHÂN BỔ GIẢM GIÁ (PROPORTIONAL SPLIT)
Khi Khách hàng áp dụng 1 Voucher toàn sàn trị giá 100K cho 1 Giỏ hàng gồm 2 Shop (Shop A mua 300K, Shop B mua 100K):
Hệ thống không thể gán cục giảm giá 100K cho Shop A. Phải chia theo tỷ lệ trọng số (Weight).
- Tổng tiền = 400K.
- Trọng số Shop A = 300/400 = 75%.
- Trọng số Shop B = 100/400 = 25%.
=> Hệ thống tự động trừ 75K vào đơn hàng của Shop A, và 25K vào đơn hàng của Shop B. Thuật toán này đã được Verify (xác minh) trong `VoucherInternalServiceImpl`.

## 5. HẠN CHẾ VÀ RỦI RO ĐỒNG THỜI (CONCURRENCY)
Việc sử dụng cột `used_count = used_count + 1` không có cơ chế Optimistic Locking (`@Version`) hoặc Pessimistic Locking hiện hành. Trong môi trường High-Traffic (Traffic cực cao như Flash Sale), có nguy cơ cấp phát mã vượt quá `usage_limit`.
