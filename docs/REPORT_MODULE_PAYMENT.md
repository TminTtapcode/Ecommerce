# BÁO CÁO KIỂM TOÁN MÔ ĐUN: PAYMENT (THANH TOÁN)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Payment chịu trách nhiệm giao tiếp với cổng thanh toán (Payment Gateway), cụ thể là VNPay Sandbox, và quản lý sổ cái hoàn tiền (Refund Ledger). Đây là mô đun đòi hỏi mức độ cẩn trọng cao nhất về concurrency (xử lý đồng thời).

## 2. TRÁCH NHIỆM MÔ ĐUN
- Tạo URL thanh toán VNPay chuyển hướng người dùng (Redirect URL).
- Xử lý Webhook (IPN - Instant Payment Notification) trả về từ VNPay.
- Quản lý trạng thái thanh toán của Aggregate Root `PaymentGroup`.
- Phối hợp với Admin Module để thực hiện lệnh hoàn tiền (Refund F10).

## 3. THỰC THỂ (ENTITIES)
- `Payment`: Quản lý 1 phiên thanh toán cho 1 `payment_group_id` UUID. Lưu trữ `amount`, `method` (VN_PAY, COD), `transaction_id`.
- `RefundAttempt`: Sổ cái ghi nhận nỗ lực hoàn tiền từ Admin. UNIQUE trên `(order_id, payment_id)`. Đảm bảo mỗi đơn hàng chỉ được hoàn 1 lần duy nhất để tránh thất thoát (Idempotency Key).

## 4. IPN WEBHOOK VÀ PESSIMISTIC LOCKING
Đây là điểm sáng kỹ thuật quan trọng:
VNPay có thể gọi webhook IPN nhiều lần cho cùng 1 giao dịch (ví dụ do rớt mạng). Nếu không xử lý kỹ, hệ thống có thể xác nhận đơn hàng 2 lần.
**Giải pháp hiện hành:** Hàm xử lý IPN sử dụng khóa bi quan (Pessimistic Write Lock) thông qua JPA Query (`@Lock(LockModeType.PESSIMISTIC_WRITE)`). Khi luồng 1 đang xử lý IPN của mã GD X, luồng 2 sẽ bị chặn lại ở mức Database cho đến khi luồng 1 hoàn tất, đảm bảo giao dịch chỉ chuyển trạng thái `SUCCESS` đúng 1 lần.

## 5. HOÀN TIỀN (REFUND WORKFLOW)
1. Quản trị viên bấm nút Refund trên một Đơn hàng `CANCELLED`.
2. Tạo bản ghi `RefundAttempt` với trạng thái `REQUESTED`.
3. Backend gọi REST API sang VNPay Refund Gateway (Môi trường Sandbox).
4. Phân tích JSON Response từ VNPay. Nếu thành công (`vnp_ResponseCode == "00"`), cập nhật `RefundAttempt` thành `CONFIRMED`.

## 6. HẠN CHẾ (LIMITATIONS)
- Cổng thanh toán VNPay Refund đòi hỏi IP whitelist, do đó chức năng Hoàn tiền chưa được kiểm chứng (Unverified) End-to-End đầy đủ trên môi trường production, dù code logic đã hoàn thiện.
