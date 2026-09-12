# BÁO CÁO KIỂM TOÁN MÔ ĐUN: CART (GIỎ HÀNG)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Cart là hệ thống tạm lưu trữ sản phẩm dự định mua của người dùng. Trong kiến trúc đa gian hàng, mô đun này phải đảm bảo gom nhóm trực quan các sản phẩm của nhiều Shop khác nhau.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Quản lý Giỏ hàng cá nhân (1 User - 1 Cart).
- Thêm, sửa số lượng, xóa các `CartItem`.
- Đảm bảo tính nhất quán (Consistency) của dữ liệu giỏ hàng với giá và tên thực tế của biến thể sản phẩm (Variant).

## 3. THỰC THỂ (ENTITIES)
- `Cart`: Chứa `user_id` (Khóa ngoại Unique).
- `CartItem`: Chứa `product_variant_id` và số lượng (`quantity`). Không lưu lại giá cứng trong giỏ, mà sẽ nạp giá động từ `ProductVariant` lúc truy vấn.

## 4. LUỒNG THÊM VÀO GIỎ (ADD TO CART WORKFLOW)
1. Nhận `productVariantId` và `quantity`.
2. Kiểm tra `stock_quantity` của Variant (Gọi sang ProductInternalService). Nếu mua quá số lượng tồn, chặn báo lỗi.
3. Nếu sản phẩm đã có trong giỏ, cộng dồn số lượng. Nếu chưa, tạo bản ghi `CartItem` mới.

## 5. ĐẶC TẢ API (APIs)
- `GET /api/v1/cart`: Lấy toàn bộ giỏ hàng (Backend tự động group by `shopId` trả về cho Frontend).
- `POST /api/v1/cart/items`: Thêm item.
- `PUT /api/v1/cart/items/{id}`: Thay đổi số lượng.
- `DELETE /api/v1/cart/items/{id}`: Xóa item khỏi giỏ.

## 6. LƯU Ý KỸ THUẬT
Do cấu trúc chia nhỏ Module, Cart không can thiệp vào quá trình tạo Đơn hàng. Thay vào đó, mô đun `Order` sẽ đọc danh sách ID của các `CartItem` được tick chọn để checkout, và trực tiếp chuyển chúng thành `OrderItem`, sau đó xóa các `CartItem` đó khỏi cơ sở dữ liệu.
