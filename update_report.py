import re
import sys

filepath = 'docs/REPORT_FULL_DETAIL.md'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# Update 1: Chỉnh sửa hồ sơ -> IMPLEMENTED
content = content.replace(
    '| Customer | Chỉnh sửa hồ sơ | MISSING | Không có API PUT /users/me |',
    '| Customer | Chỉnh sửa hồ sơ | IMPLEMENTED | `PUT /api/v1/users/me`, `ProfilePage.tsx` |'
)

# Update 2: Thống kê doanh thu -> IMPLEMENTED
content = content.replace(
    '| Vendor | Thống kê doanh thu | MISSING | Không có API/UI |',
    '| Vendor | Thống kê doanh thu | IMPLEMENTED | `GET /api/v1/vendor/analytics`, `VendorAnalyticsPage.tsx` |'
)

# Update 3: Admin Dashboard thống kê -> IMPLEMENTED
content = content.replace(
    '| Admin | Dashboard thống kê | MISSING | Chỉ có text placeholder |',
    '| Admin | Dashboard thống kê | IMPLEMENTED | `GET /api/v1/admin/statistics`, `AdminDashboardPage.tsx` |'
)

# Update 4: Admin Hoàn tiền (Refund)
# Find the line or add it if missing. But refund is Admin?
content = content.replace(
    '| System | Proportional voucher split | IMPLEMENTED | Checkout service logic |',
    '| System | Proportional voucher split | IMPLEMENTED | Checkout service logic |\n| Admin | Hoàn tiền VNPay (Refund) | IMPLEMENTED | `POST /api/v1/admin/orders/:id/refunds`, `AdminOrderDetailPage.tsx` |\n| Customer | Gợi ý sản phẩm (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations` |'
)

# Update 5: In Section 4.1.3 (So sánh mục tiêu)
content = content.replace(
    '| Luồng Admin | ⚠️ Đạt một phần (thiếu dashboard, user management) |',
    '| Luồng Admin | ⚠️ Đạt một phần (thiếu user management) |'
)

# Update 6: In Section 4.2.2 (Hạn chế nghiệp vụ)
content = content.replace(
    'Customer tự hủy đơn, Refund, Address Book, tính phí vận chuyển theo khu vực, thống kê doanh thu Vendor/Admin',
    'Customer tự hủy đơn, Address Book, tính phí vận chuyển theo khu vực'
)

# Update 7: In Section 4.3.1 (Ngắn hạn)
content = content.replace(
    '**2. Tính năng người dùng cơ bản**\n- API và UI cập nhật hồ sơ cá nhân.\n- Quên mật khẩu / Reset password qua email.\n- Cho phép Customer tự hủy đơn hàng PENDING.',
    '**2. Tính năng người dùng cơ bản**\n- Quên mật khẩu / Reset password qua email.\n- Cho phép Customer tự hủy đơn hàng PENDING.'
)

content = content.replace(
    '**3. Admin Dashboard thống kê**\n- API `GET /api/v1/admin/statistics`: tổng doanh thu, đơn hàng theo ngày/tháng, top sản phẩm.\n- Biểu đồ thống kê trên Admin UI.',
    '**3. Quản lý hệ thống**\n- Tính năng quản lý Người dùng (User Management) cho Admin.\n- Tính năng quản lý Danh mục (Category Management).'
)

# Update 8: In Section 4.3.3 (Dài hạn)
content = content.replace(
    '- Tích hợp hoàn tiền (Refund).\n',
    ''
)

# Update 9: Add Recommendation to Section 4.1.1
if 'Hybrid Recommendation' not in content:
    content = content.replace(
        '- **Luồng hoàn chỉnh từ đầu đến cuối:**',
        '- **Gợi ý sản phẩm (Product Recommendation):** Hệ thống tích hợp thuật toán Hybrid (Cá nhân hóa theo lịch sử mua hàng + Trending + Sản phẩm mới) giúp tăng trải nghiệm mua sắm.\n\n- **Luồng hoàn chỉnh từ đầu đến cuối:**'
    )

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated REPORT_FULL_DETAIL.md successfully!")
