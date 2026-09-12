# CHECKLIST MINH CHỨNG (SCREENSHOTS & DIAGRAMS)
# PHỤC VỤ BÁO CÁO ĐỒ ÁN NGÀNH

Dưới đây là danh sách toàn bộ các Hình ảnh chụp màn hình (Screenshot) và Lược đồ (Diagram) bạn cần phải tự chuẩn bị và thay thế vào các vị trí đã đánh dấu `[INSERT ...]` trong tài liệu `docs/REPORT_PRINT_VERSION.md`.

## 1. Lược đồ (Diagrams)
*Sinh viên có thể sử dụng Draw.io, StarUML, Lucidchart hoặc Visual Paradigm để vẽ lại các lược đồ này dựa trên kiến trúc Modular Monolith thực tế.*

- [ ] `DIAGRAM-USE-CASE-OVERVIEW`: Sơ đồ Use Case tổng quát (Có đủ 3 Actor: Customer, Vendor, Admin và các UC tương ứng).
- [ ] `DIAGRAM-CLASS-PRODUCT`: Sơ đồ lớp (Class Diagram) tập trung vào nhánh Product (Product, ProductVariant, ProductImage, Category).
- [ ] `DIAGRAM-CLASS-ORDER`: Sơ đồ lớp (Class Diagram) tập trung vào nhánh Order (Order, OrderItem, Payment, Cart).
- [ ] `DIAGRAM-SEQUENCE-CHECKOUT`: Sơ đồ tuần tự (Sequence Diagram) luồng Checkout đa gian hàng (OrderService gọi Product/Voucher/Payment).
- [ ] `DIAGRAM-SEQUENCE-IPN`: Sơ đồ tuần tự (Sequence Diagram) xử lý Webhook VNPay IPN với khóa `PESSIMISTIC_WRITE`.
- [ ] `DIAGRAM-SEQUENCE-CHAT`: Sơ đồ tuần tự (Sequence Diagram) nhắn tin qua WebSocket STOMP (Pub/Sub).
- [ ] `DIAGRAM-ACTIVITY-ORDER-PAYMENT`: Sơ đồ hoạt động (Activity Diagram) từ lúc bấm Mua hàng -> Checkout -> Thanh toán VNPay -> Thành công.
- [ ] `DIAGRAM-ACTIVITY-SHOP-APPROVAL`: Sơ đồ hoạt động (Activity Diagram) Đăng ký gian hàng -> Chờ Admin phê duyệt -> Gán Role Vendor.
- [ ] `DIAGRAM-STATE-ORDER`: Sơ đồ trạng thái (State Machine) Đơn hàng (`PENDING -> CONFIRMED -> SHIPPED -> DELIVERED / CANCELLED`).
- [ ] `DIAGRAM-STATE-SHOP`: Sơ đồ trạng thái (State Machine) Gian hàng (`PENDING -> ACTIVE / BANNED`).

## 2. Hình ảnh chụp màn hình (Screenshots)
*Chạy cả hệ thống Backend và Frontend (React) để chụp ảnh màn hình hoạt động thực tế. Lưu ý chọn dữ liệu đẹp (ví dụ: Tên sản phẩm rõ ràng, Hình ảnh test sắc nét).*

- [ ] `IMG-HOME`: Chụp trang chủ (Home Page) giao diện Customer, thể hiện được Slider và danh sách sản phẩm.
- [ ] `IMG-REGISTER-LOGIN`: Chụp form Đăng ký hoặc Đăng nhập (Authentication Modal/Page).
- [ ] `IMG-PRODUCT-DETAIL`: Chụp màn hình chi tiết 1 sản phẩm, có hiện chọn Màu sắc/Kích cỡ (Biến thể) và hiển thị giá tương ứng.
- [ ] `IMG-VENDOR-REGISTER`: Chụp form người dùng điền thông tin Đăng ký mở Gian hàng.
- [ ] `IMG-VENDOR-PRODUCT-CREATE`: Chụp màn hình người bán điền Form Tạo sản phẩm (Có khu vực upload ảnh và quản lý bảng biến thể/SKU).
- [ ] `IMG-CART`: Chụp màn hình Giỏ hàng mua sắm đang chứa các sản phẩm từ ít nhất 2 Shop khác nhau.
- [ ] `IMG-CHECKOUT`: Chụp màn hình Thanh toán Checkout, hiển thị rõ tổng tiền, tiền vận chuyển, mã voucher và 2 phương thức thanh toán VNPay/COD.
- [ ] `IMG-VENDOR-ORDERS`: Chụp bảng điều khiển Đơn hàng của người bán (Vendor Order List), đang chuyển trạng thái 1 đơn hàng (Ví dụ: Từ Chờ xác nhận sang Đang giao).
- [ ] `IMG-ADMIN-SHOP`: Chụp giao diện Admin bảng duyệt Gian hàng (Shop Approval) với trạng thái `PENDING` -> `ACTIVE`.

---
**Hướng dẫn sử dụng:**
Sau khi chuẩn bị xong hình ảnh nào, hãy copy tệp hình vào thư mục `docs/images/` của mã nguồn, và vào file `REPORT_PRINT_VERSION.md` đổi thẻ `[INSERT FIGURE: IMG-XXX]` thành mã Markdown (Ví dụ: `![Trang Chủ](./images/img-home.png)`).
