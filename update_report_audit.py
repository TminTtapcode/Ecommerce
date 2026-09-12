import re
import sys

filepath = 'docs/REPORT_FULL_DETAIL.md'

with open(filepath, 'r', encoding='utf-8') as f:
    content = f.read()

# I will append the 4 endpoints to the MISSING list in the Feature Matrix
append_text = """
| Admin | Lịch sử trạng thái đơn hàng | MISSING | BE có `GET /api/v1/admin/orders/:id/status-history` nhưng FE không gọi |
| Admin | Chi tiết hoàn tiền (Refund Detail) | MISSING | BE có `GET /api/v1/admin/refunds/:id` nhưng FE không gọi |
| Admin | Đối soát hoàn tiền (Reconcile) | MISSING | BE có `POST /api/v1/admin/refunds/:id/reconcile` nhưng FE không gọi |
| Customer | Kiểm tra quyền review (Eligibility) | MISSING | BE có `GET /api/v1/products/:id/reviews/eligibility` nhưng FE dùng logic khác |
"""

if "Chi tiết hoàn tiền (Refund Detail)" not in content:
    content = content.replace(
        '| Customer | Gợi ý sản phẩm (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations` |',
        '| Customer | Gợi ý sản phẩm (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations` |' + append_text
    )

with open(filepath, 'w', encoding='utf-8') as f:
    f.write(content)

print("Updated REPORT_FULL_DETAIL.md with the 4 missing endpoints.")
