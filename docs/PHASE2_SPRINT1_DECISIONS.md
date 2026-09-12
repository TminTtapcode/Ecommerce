# Sprint 1 — Implementation contract đã chốt

Trạng thái: **được người dùng phê duyệt ngày 2026-09-09**, sau audit và Decision Set. Tài liệu này là contract cho D1/D3/D4 và Error; thay thế các mục tương ứng còn ghi “chờ chốt” trong kế hoạch trước. Phê duyệt thiết kế không đồng nghĩa implementation đã hoàn tất.

## D1 — Cancellation và inventory

- Chỉ Vendor CONFIRMED → CANCELLED, giữ ownership và role hiện có.
- Khóa bi quan Order; hủy đơn và hoàn stock variant + product cùng một transaction qua ProductInternalService. Không REQUIRES_NEW cho hoàn kho.
- Quantity lấy từ OrderItem, không từ client. Kiểm tra input/variant/row count; một item lỗi rollback toàn bộ status và stock.
- Gỡ listener cộng kho cũ trong cùng thay đổi. Domain event có thể tiếp tục phục vụ notification sau commit.
- Hủy lặp trả lỗi trạng thái, không hoàn thêm. Cancel/SHIPPED/IPN dùng guard nhất quán và thứ tự lock được kiểm tra.
- Không restore lịch sử CANCELLED hàng loạt, không tự thêm hoàn tiền hoặc đưa item về giỏ.

## S1-04 — Payment guard

- IPN thành công chỉ chuyển Order PENDING → CONFIRMED.
- Không ghi đè CANCELLED/CONFIRMED/SHIPPED/DELIVERED, không restore inventory từ IPN.
- Kiểm thử callback lặp và race với cancellation. Payment SUCCESS là sự kiện tiền độc lập với Order; không hồi sinh đơn hủy để làm khớp trạng thái.
- Không triển khai refund gateway trong Sprint 1; bất thường thanh toán phải được ghi nhận để đối soát.

## D3 — Ban/unban

- Cho ban PENDING hoặc ACTIVE; lưu prior status trong DB trước khi chuyển BANNED.
- Unban khôi phục chính xác PENDING hoặc ACTIVE đã lưu.
- Chỉ approveShop được chuyển PENDING → ACTIVE và cấp ROLE_VENDOR. Ban/unban không tự cấp/thu hồi role.
- BANNED cũ không có prior status: từ chối unban tự động, yêu cầu đối soát; không suy đoán từ role.
- Khóa cạnh tranh approve/ban/unban; migration prior status nullable, không backfill phỏng đoán.

## D4 — Dừng bán mới, xử lý đơn cũ

- Public catalog/detail không hiển thị sản phẩm của shop BANNED; lọc trước phân trang.
- Cart giữ item cũ và đánh dấu unavailable; cho xóa/giảm quantity, chặn thêm/tăng.
- Checkout selection chứa item của shop bị ban bị từ chối toàn bộ, không âm thầm bỏ item. Kiểm tra backend và phối hợp lock với ban.
- Product: cho chủ shop đọc detail có ownership, chặn mutation bán hàng trực tiếp; không dùng public detail làm đường quản trị bỏ qua quyền.
- Existing orders: chủ shop tiếp tục xem và chuyển trạng thái theo matrix hiện có. Ban không tự hủy đơn hoặc vô hiệu hóa thanh toán đơn cũ.
- Inventory nội bộ vẫn hoàn kho cho đơn hủy của shop BANNED.
- Unban không mass-update product HIDDEN thành ACTIVE.
- FE tách guard bán mới khỏi guard xử lý đơn cũ. Không coi FE guard là backend authorization.
- Reason tùy chọn; validate độ dài, log có cấu trúc actor/shop/old-new status/reason sau commit. Chưa thêm bảng audit riêng.

## S1-02 — Error response

- Thêm success boolean, giữ status HTTP code/errorCode/message.
- Error response luôn có data: null; HTTP status và body status nhất quán.
- Frontend dựa errorCode, không so sánh message. Giữ message hiển thị người dùng.
- Error envelope thống nhất cả controller và security filter. VNPay callback giữ RspCode/Message riêng.

```json
{
  "status": 404,
  "success": false,
  "errorCode": "SHOP_NOT_FOUND",
  "message": "Không tìm thấy gian hàng",
  "data": null
}
```

## Thứ tự và kiểm chứng

S1-01 baseline → S1-02 error → S1-03 inventory → S1-04 payment guard → S1-05/06 shop/enforcement → S1-07 tests → S1-08 verification.

Mỗi bước inspect/implement/test/review trước bước tiếp. Baseline ghi rõ lỗi có sẵn; không sửa nghiệp vụ để làm baseline xanh. Integration dùng MySQL riêng cho transaction/rollback/lock/concurrency; Mockito không thay được bằng chứng này.
