# -*- coding: utf-8 -*-
import sys

def main():
    file_path = 'docs/REPORT_FULL_DETAIL.md'
    with open(file_path, 'r', encoding='utf-8') as f:
        content = f.read()

    original_len = len(content)
    print(f"Original length: {original_len}")

    # ==========================================
    # 1. CHAPTER 1: Scope (1.5.1, 1.5.2) & Phases (1.6.2)
    # ==========================================
    old_scope = """### 1.5.1. Phạm vi chức năng — TRONG phạm vi

**Quản lý người dùng và phân quyền:** Đăng ký, đăng nhập, JWT (token 24 giờ); phân quyền ba vai trò: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN.

**Quản lý gian hàng:** Đăng ký (PENDING) → Admin duyệt (ACTIVE) → Email thông báo qua Gmail SMTP.

**Quản lý sản phẩm:** CRUD với danh mục (cấu trúc cha-con), thương hiệu, biến thể (JSON attributes), ảnh Cloudinary; tìm kiếm theo từ khóa; xóa mềm (HIDDEN).

**Giỏ hàng và đặt hàng:** Thêm/cập nhật/xóa sản phẩm; đặt hàng đa gian hàng; preview checkout với giảm giá.

**Thanh toán:** VNPay Sandbox + COD; IPN webhook với Pessimistic Lock; payment retry.

**Quản lý đơn hàng:** Buyer xem lịch sử; Vendor cập nhật trạng thái (`PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`); Admin xem toàn bộ.

**Đánh giá sản phẩm:** Sau đơn DELIVERED; 1 review/sản phẩm/đơn; Vendor phản hồi.

**Mã giảm giá:** Phạm vi SYSTEM và SHOP; loại PERCENTAGE và FIXED_AMOUNT; proportional split cho SYSTEM voucher.

**Thông báo:** Event-driven qua `@TransactionalEventListener AFTER_COMMIT`; REST polling.

**Chat thời gian thực:** WebSocket STOMP; JWT authentication trên STOMP channel.

**Lưu trữ ảnh:** Cloudinary CDN; xóa ảnh an toàn qua `publicId`.

**Tài liệu API:** Swagger UI tại `/swagger-ui/index.html`.

### 1.5.2. Phạm vi chức năng — NGOÀI phạm vi

| Tính năng | Ghi chú |
|---|---|
| Redis Cache | Dự kiến Phase 8, chưa thực hiện |
| Docker / Docker Compose | Dự kiến Phase 9, chưa thực hiện |
| Triển khai cloud (AWS) | Ngoài phạm vi đề tài |
| WebSocket Push Notification | Hiện dùng REST polling |
| Hoàn trả tồn kho khi hủy đơn | Technical debt đã xác định |
| Admin Dashboard thống kê | Chưa có API thống kê |
| Tìm kiếm nâng cao (Elasticsearch) | Chỉ hỗ trợ LIKE keyword |
| Ứng dụng di động | Chỉ giao diện web |"""

    new_scope = """### 1.5.1. Phạm vi chức năng — TRONG phạm vi

**Quản lý người dùng và phân quyền:** Đăng ký, đăng nhập, JWT (token 24 giờ); phân quyền ba vai trò: ROLE_USER, ROLE_VENDOR, ROLE_ADMIN; xem và chỉnh sửa thông tin hồ sơ cá nhân (Profile) tại `/profile`.

**Quản lý gian hàng:** Đăng ký (PENDING) → Admin duyệt (ACTIVE) hoặc khóa (BANNED) / mở khóa gian hàng vi phạm → Tự động gửi email thông báo qua Gmail SMTP và cập nhật quyền hạn.

**Quản lý sản phẩm:** CRUD với danh mục tự tham chiếu đa cấp (duyệt cây theo giải thuật BFS), thương hiệu, biến thể (JSON attributes), gallery ảnh Cloudinary CDN; tìm kiếm đa tiêu chí theo từ khóa, danh mục đệ quy và khoảng giá; xóa mềm (HIDDEN).

**Gợi ý sản phẩm (Recommendation):** Tích hợp động cơ gợi ý sản phẩm lai (Hybrid Recommendation Engine) dựa trên danh mục tương đồng, điểm đánh giá và doanh số bán ra.

**Giỏ hàng và đặt hàng:** Thêm/cập nhật/xóa sản phẩm; giỏ hàng gom nhóm đa shop; đặt hàng đa gian hàng (tự động tách $N$ đơn hàng độc lập theo shop trong cùng `payment_group_id`); preview checkout với mã giảm giá; cơ chế tự động hoàn trả tồn kho biến thể khi đơn hàng bị hủy (`restoreStockOnCancellation`).

**Thanh toán:** Tích hợp cổng thanh toán VNPay Sandbox + COD; xử lý IPN webhook bất đồng bộ với khóa bi quan (`PESSIMISTIC_WRITE`) trên bảng `payments` ngăn chặn duplicate transaction; cơ chế payment retry.

**Quản lý đơn hàng:** Buyer theo dõi lịch sử đơn hàng và chủ động bấm xác nhận nhận hàng (`DELIVERED`); Vendor cập nhật trạng thái đơn (`PENDING → CONFIRMED → SHIPPED → DELIVERED / CANCELLED`); Admin giám sát và lọc toàn bộ đơn hàng trên toàn hệ thống.

**Đánh giá sản phẩm:** Người mua đánh giá chất lượng (1–5 sao) kèm hình ảnh thực tế sau khi đơn hàng chuyển sang `DELIVERED`; 1 review/sản phẩm/đơn; Vendor phản hồi công khai đánh giá của khách.

**Mã giảm giá:** Phạm vi SYSTEM (toàn sàn) và SHOP (gian hàng); loại PERCENTAGE và FIXED_AMOUNT; thuật toán phân bổ tỷ lệ chiết khấu (Proportional Split) cho SYSTEM voucher theo doanh số từng shop.

**Hệ thống thông báo thời gian thực:** Thông báo đẩy (Push Notification) qua WebSocket STOMP tới người dùng ngay khi có sự kiện mới, kết hợp lưu trữ lịch sử qua `@TransactionalEventListener AFTER_COMMIT`.

**Chat thời gian thực:** Khung chat nổi toàn diện (`FloatingChatWidget`) tích hợp toàn cục tại `PublicLayout.tsx`; giao tiếp hai chiều tức thì giữa Buyer và Vendor qua WebSocket STOMP kèm xác thực JWT trên kết nối.

**Báo cáo phân tích kinh doanh:** Admin Dashboard (`/admin`) hiển thị các thẻ chỉ số KPI toàn sàn, biểu đồ doanh thu và bảng đơn hàng mới; Vendor Analytics (`/vendor/analytics`) phân tích chỉ số kinh doanh và biểu đồ doanh thu 30 ngày của gian hàng.

**Lưu trữ ảnh đám mây:** Cloudinary Media Cloud SDK; upload đa phương tiện và xóa ảnh an toàn qua `publicId`.

**Tài liệu API:** SpringDoc OpenAPI 3.0.3 (Swagger UI tại `/swagger-ui/index.html`).

### 1.5.2. Phạm vi chức năng — NGOÀI phạm vi

| Tính năng | Ghi chú |
|---|---|
| Caching phân tán (Redis Cache) | Dự kiến lộ trình tương lai (Phase 10) |
| Container hóa (Docker / Docker Compose) | Dự kiến lộ trình tương lai (Phase 10) |
| Triển khai môi trường đám mây (AWS/GCP) | Ngoài phạm vi đề tài học thuật |
| Tìm kiếm chuyên sâu (Elasticsearch Engine) | Hiện tại hệ thống dùng duyệt cây BFS kết hợp Index CSDL |
| Cổng thanh toán môi trường Production | Chỉ thử nghiệm trên môi trường VNPay Sandbox |
| Ứng dụng di động (Mobile App iOS/Android) | Chỉ xây dựng Single Page Application trên nền tảng Web |
| Hoàn tiền tự động E2E trên VNPay thật | Chưa tích hợp trực tiếp do giới hạn IP Sandbox của cổng thanh toán |"""

    assert old_scope in content, "old_scope not found in content!"
    content = content.replace(old_scope, new_scope, 1)
    print("Replaced Scope in Chapter 1 successfully.")

    old_phases = """### 1.6.2. Phân chia giai đoạn phát triển

| Phase | Nội dung | Trạng thái |
|---|---|---|
| Phase 1 — Foundation | Thiết lập dự án, kiến trúc, xử lý ngoại lệ, chuẩn API | Hoàn thành |
| Phase 2 — Authentication | Đăng ký, đăng nhập, JWT, phân quyền | Hoàn thành |
| Phase 3 — Product Catalog | Danh mục, thương hiệu, sản phẩm, biến thể, ảnh | Hoàn thành |
| Phase 3.5 — Shop | Đăng ký gian hàng, duyệt gian hàng | Hoàn thành |
| Phase 4 — Shopping | Giỏ hàng, kiểm tra tồn kho | Hoàn thành |
| Phase 5 — Order | Checkout đa gian hàng, quản lý đơn hàng, state machine | Hoàn thành |
| Phase 6 — Payment | VNPay, COD, IPN webhook, idempotency | Hoàn thành |
| Phase 7 — Supporting Features | Đánh giá, thông báo, voucher, media, chat realtime | Hoàn thành |
| Phase 8 — Advanced Features | Redis cache, push notification | Chưa thực hiện |
| Phase 9 — Infrastructure | Docker, Docker Compose, cloud deployment | Chưa thực hiện |"""

    new_phases = """### 1.6.2. Phân chia giai đoạn phát triển

| Phase | Nội dung | Trạng thái |
|---|---|---|
| Phase 1 — Foundation | Thiết lập dự án, kiến trúc Modular Monolith, xử lý ngoại lệ, chuẩn API | Hoàn thành |
| Phase 2 — Authentication | Đăng ký, đăng nhập, JWT (24h), phân quyền RBAC | Hoàn thành |
| Phase 3 — Product Catalog | Danh mục đa cấp BFS, thương hiệu, sản phẩm, biến thể, ảnh Cloudinary | Hoàn thành |
| Phase 3.5 — Shop | Đăng ký gian hàng, duyệt/khóa gian hàng, gửi mail Gmail SMTP | Hoàn thành |
| Phase 4 — Shopping | Giỏ hàng đa shop, kiểm tra tồn kho, tính toán tạm tính | Hoàn thành |
| Phase 5 — Order | Checkout đa gian hàng, ranh giới giao dịch atomicity, quản lý đơn hàng | Hoàn thành |
| Phase 6 — Payment | Cổng thanh toán VNPay Sandbox, COD, IPN webhook, khóa bi quan chống duplicate | Hoàn thành |
| Phase 7 — Supporting Features | Đánh giá kèm ảnh, thông báo event-driven, voucher proportional split, chat STOMP | Hoàn thành |
| Phase 8 — Advanced Features & Optimization | STOMP Push Notification, FloatingChatWidget, Profile cá nhân, hoàn trả kho khi hủy đơn | Hoàn thành |
| Phase 9 — Business Analytics & Advanced Search | Admin Dashboard KPI toàn sàn, Vendor Analytics 30 ngày, BFS Category search, Gợi ý sản phẩm lai | Hoàn thành |
| Phase 10 — Future Infrastructure | Redis Cache, Docker & Docker Compose, CI/CD, Triển khai Cloud | Dự kiến tương lai |"""

    assert old_phases in content, "old_phases not found in content!"
    content = content.replace(old_phases, new_phases, 1)
    print("Replaced Phases in Chapter 1 successfully.")

    # ==========================================
    # 2. CHAPTER 2: Theory additions (2.1.10, 2.1.11)
    # ==========================================
    old_ch2_end = """```
Giảm giá gian hàng X = Tổng giảm giá × (Giá trị GH-X / Tổng giá trị đủ điều kiện)
```

---

## 2.2. Các phương pháp, mô hình và thuật toán sử dụng"""

    new_ch2_end = """```
Giảm giá gian hàng X = Tổng giảm giá × (Giá trị GH-X / Tổng giá trị đủ điều kiện)
```

### 2.1.10. Thuật toán Gợi ý sản phẩm lai (Hybrid Recommendation Engine)

Hệ thống triển khai cơ chế gợi ý sản phẩm lai (Hybrid Recommendation Engine) kết hợp giữa hai phương pháp nhằm tối ưu hóa trải nghiệm khám phá sản phẩm của khách hàng:
1. **Lọc dựa trên nội dung (Content-based Filtering):** Dựa vào mối tương quan danh mục (`category_id`), thương hiệu (`brand_id`) và khoảng giá của các sản phẩm khách hàng đang xem hoặc đã mua.
2. **Độ phổ biến và phản hồi cộng đồng (Popularity & Rating Score):** Tính toán điểm số trọng số tổng hợp từ số lượng đơn hàng bán ra thành công (`order_items.quantity`) và điểm đánh giá trung bình (`AVG(rating)`) từ bảng `reviews`.

Công thức tính điểm ưu tiên gợi ý cho một sản phẩm $p$:
$$Score(p) = w_1 \cdot Sim(p, p_{viewed}) + w_2 \cdot RatingScore(p) + w_3 \cdot SalesVolume(p)$$

Trong đó $w_1, w_2, w_3$ là các hệ số trọng số. Cơ chế lai này giải quyết hiệu quả bài toán "Cold Start" (khi người dùng mới chưa có nhiều lịch sử hành vi) bằng cách ưu tiên các sản phẩm thịnh hành (Trending) có điểm đánh giá chất lượng cao.

### 2.1.11. Thuật toán duyệt cây danh mục đa cấp theo chiều rộng (BFS Category Traversal) kết hợp Indexing

Để hỗ trợ người mua tìm kiếm sản phẩm theo cấu trúc phân cấp danh mục (ví dụ: khi chọn danh mục cha *"Thời trang"*, hệ thống phải tìm thấy toàn bộ sản phẩm thuộc các danh mục con như *"Áo nam"*, *"Áo sơ mi"*, *"Quần jean"*), hệ thống áp dụng giải thuật **Duyệt theo chiều rộng (Breadth-First Search - BFS)**:
- **Cấu trúc dữ liệu:** Bảng `categories` lưu trữ dạng cây tự tham chiếu (`parent_id` liên kết khóa ngoại với `categories.id`).
- **Quy trình giải thuật:**
  1. Khởi tạo hàng đợi `Queue<Long> queue` chứa `categoryId` gốc do người dùng chọn và tập hợp kết quả `Set<Long> subCategoryIds`.
  2. Trong khi hàng đợi chưa rỗng: lấy mã danh mục hiện tại, truy vấn toàn bộ danh mục con trực tiếp (`WHERE parent_id = :currentId`), đưa các mã con vào cả `queue` và `subCategoryIds`.
  3. Lặp lại cho đến khi duyệt hết các nhánh con cháu. Độ phức tạp thời gian đạt $O(V + E)$ (với $V$ là số danh mục và $E$ là số quan hệ cha-con).
  4. Truy vấn sản phẩm bằng mệnh đề `WHERE category_id IN (:subCategoryIds)` được tăng tốc nhờ chỉ mục `idx_product_category` trên bảng `products`.

---

## 2.2. Các phương pháp, mô hình và thuật toán sử dụng"""

    assert old_ch2_end in content, "old_ch2_end not found in content!"
    content = content.replace(old_ch2_end, new_ch2_end, 1)
    print("Added Theory sections in Chapter 2 successfully.")

    # ==========================================
    # 3. CHAPTER 3: Use cases (UC09, UC10, UC11)
    # ==========================================
    old_uc8 = """#### UC08 — Viết đánh giá sản phẩm

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC08 |
| **Tên** | Viết đánh giá sản phẩm |
| **Actor chính** | Buyer |
| **Tiền điều kiện** | Buyer đã có đơn hàng DELIVERED chứa sản phẩm cần đánh giá; Chưa có review cho sản phẩm + đơn hàng này |
| **Luồng chính** | 1. Buyer nhập rating (1-5 sao) + nội dung + ảnh (optional) → 2. Kiểm tra ReviewEligibility (DELIVERED + chưa review) → 3. Tạo Review → 4. Phát `ReviewCreatedEvent` → 5. `AFTER_COMMIT`: Notification đến Vendor |
| **Luồng thay thế** | Chưa đủ điều kiện → 400 "Không đủ điều kiện để đánh giá" |
| **API** | `POST /api/v1/reviews` |

---

## 3.4. Mô hình hóa hệ thống"""

    new_uc8_to_11 = """#### UC08 — Viết đánh giá sản phẩm

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC08 |
| **Tên** | Viết đánh giá sản phẩm |
| **Actor chính** | Buyer |
| **Tiền điều kiện** | Buyer đã có đơn hàng DELIVERED chứa sản phẩm cần đánh giá; Chưa có review cho sản phẩm + đơn hàng này |
| **Luồng chính** | 1. Buyer nhập rating (1-5 sao) + nội dung + ảnh (optional) → 2. Kiểm tra ReviewEligibility (DELIVERED + chưa review) → 3. Tạo Review → 4. Phát `ReviewCreatedEvent` → 5. `AFTER_COMMIT`: Notification đến Vendor |
| **Luồng thay thế** | Chưa đủ điều kiện → 400 "Không đủ điều kiện để đánh giá" |
| **API** | `POST /api/v1/reviews` |

#### UC09 — Khách hàng xác nhận nhận hàng (Confirm Delivery)

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC09 |
| **Tên** | Khách hàng xác nhận nhận hàng |
| **Actor chính** | Buyer (Khách hàng) |
| **Tiền điều kiện** | Đơn hàng đang ở trạng thái `SHIPPED` và thuộc quyền sở hữu của Buyer đang đăng nhập |
| **Luồng chính** | 1. Khách hàng vào trang chi tiết đơn hàng `/orders/:id` → 2. Nhấn nút "Đã nhận được hàng" → 3. Hệ thống kiểm tra quyền sở hữu và trạng thái `SHIPPED` → 4. Cập nhật `order.status = DELIVERED` → 5. Ghi nhận thời gian hoàn tất đơn hàng → 6. Mở quyền viết đánh giá sản phẩm (`ReviewEligibility`) |
| **Luồng thay thế** | Đơn hàng không ở trạng thái SHIPPED hoặc không phải chủ đơn hàng → Trả về mã lỗi 400/403 |
| **API** | `POST /api/v1/orders/:id/confirm-delivery` |

#### UC10 — Nhắn tin thời gian thực qua Khung chat nổi (Floating Chat Widget)

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC10 |
| **Tên** | Nhắn tin thời gian thực qua Khung chat nổi |
| **Actor chính** | Buyer, Vendor |
| **Tiền điều kiện** | Đã đăng nhập vào hệ thống, có JWT token hợp lệ |
| **Luồng chính** | 1. Người dùng bấm biểu tượng khung chat nổi ở góc dưới bên phải màn hình (`FloatingChatWidget`) → 2. Hệ thống kết nối WebSocket STOMP tại `/ws` với Header `Authorization: Bearer <token>` → 3. Đăng ký nhận tin nhắn tại `/topic/chat/{roomId}` → 4. Người dùng nhập tin nhắn và gửi đến `/app/chat.send` → 5. Backend lưu tin nhắn vào bảng `chat_messages` và broadcast tức thì đến phòng chat |
| **Luồng thay thế** | Mất kết nối mạng hoặc token hết hạn → Hiển thị thông báo kết nối lại tự động |
| **Giao thức/API** | WebSocket STOMP: `/ws`, `/app/chat.send`, `/topic/chat/{roomId}` |

#### UC11 — Xem báo cáo thống kê & Phân tích kinh doanh

| Trường | Nội dung |
|---|---|
| **Use Case ID** | UC11 |
| **Tên** | Xem báo cáo thống kê & Phân tích kinh doanh |
| **Actor chính** | Admin (Thống kê toàn sàn), Vendor (Phân tích gian hàng) |
| **Tiền điều kiện** | Đăng nhập với quyền `ROLE_ADMIN` hoặc `ROLE_VENDOR` (shop ACTIVE) |
| **Luồng chính** | 1. Admin truy cập `/admin`: Hệ thống truy vấn tổng doanh thu toàn sàn, số lượng đơn hàng, số người dùng và biểu đồ doanh thu theo thời gian qua API `/api/v1/admin/statistics/overview`.<br>2. Vendor truy cập `/vendor/analytics`: Hệ thống tổng hợp doanh thu 30 ngày gần nhất, tỷ lệ hoàn tất đơn hàng và giá trị trung bình đơn qua API `/api/v1/vendor/analytics/revenue`. |
| **API** | `GET /api/v1/admin/statistics/overview`, `GET /api/v1/vendor/analytics/revenue` |

---

## 3.4. Mô hình hóa hệ thống"""

    assert old_uc8 in content, "old_uc8 not found in content!"
    content = content.replace(old_uc8, new_uc8_to_11, 1)
    print("Added Use Cases (UC09-UC11) in Chapter 3 successfully.")

    # ==========================================
    # 4. CHAPTER 3: Database Table Overview (20 -> 22 tables)
    # ==========================================
    old_db_overview = """### 3.5.1. Tổng quan 20 bảng dữ liệu

**Bảng 3.6.** Danh sách toàn bộ bảng trong cơ sở dữ liệu

| STT | Bảng | Module | Mô tả chức năng |
|---|---|---|---|
| 1 | `users` | identity | Lưu thông tin tài khoản người dùng |
| 2 | `roles` | identity | Lưu các vai trò trong hệ thống |
| 3 | `user_roles` | identity | Bảng trung gian User-Role (Many-to-Many) |
| 4 | `shops` | shop | Lưu thông tin gian hàng Vendor |
| 5 | `brands` | product | Lưu thương hiệu sản phẩm |
| 6 | `categories` | product | Lưu danh mục sản phẩm (cây đệ quy) |
| 7 | `products` | product | Lưu sản phẩm của các gian hàng |
| 8 | `product_variants` | product | Lưu biến thể sản phẩm (màu, size, ...) |
| 9 | `product_images` | product | Lưu ảnh sản phẩm trên Cloudinary |
| 10 | `carts` | cart | Lưu giỏ hàng của Buyer |
| 11 | `cart_items` | cart | Lưu từng sản phẩm trong giỏ hàng |
| 12 | `orders` | order | Lưu đơn hàng sau khi checkout |
| 13 | `order_items` | order | Lưu chi tiết từng mặt hàng trong đơn (với snapshot giá) |
| 14 | `payments` | payment | Lưu giao dịch thanh toán |
| 15 | `vouchers` | voucher | Lưu mã giảm giá |
| 16 | `reviews` | review | Lưu đánh giá sản phẩm của Buyer |
| 17 | `review_images` | review | Lưu URL ảnh đính kèm đánh giá |
| 18 | `notifications` | notification | Lưu thông báo hệ thống |
| 19 | `chat_rooms` | chat | Lưu phòng chat Buyer-Shop |
| 20 | `chat_messages` | chat | Lưu tin nhắn trong phòng chat |"""

    new_db_overview = """### 3.5.1. Tổng quan 22 bảng dữ liệu

**Bảng 3.6.** Danh sách toàn bộ bảng trong cơ sở dữ liệu

| STT | Bảng | Module | Mô tả chức năng |
|---|---|---|---|
| 1 | `users` | identity | Lưu thông tin tài khoản người dùng |
| 2 | `roles` | identity | Lưu các vai trò trong hệ thống |
| 3 | `user_roles` | identity | Bảng trung gian User-Role (Many-to-Many) |
| 4 | `shops` | shop | Lưu thông tin gian hàng Vendor |
| 5 | `brands` | product | Lưu thương hiệu sản phẩm |
| 6 | `categories` | product | Lưu danh mục sản phẩm (cây đệ quy BFS) |
| 7 | `products` | product | Lưu sản phẩm của các gian hàng |
| 8 | `product_variants` | product | Lưu biến thể sản phẩm (màu, size, tồn kho, SKU) |
| 9 | `product_images` | product | Lưu ảnh sản phẩm trên Cloudinary |
| 10 | `carts` | cart | Lưu giỏ hàng của Buyer |
| 11 | `cart_items` | cart | Lưu từng sản phẩm trong giỏ hàng |
| 12 | `orders` | order | Lưu đơn hàng sau khi checkout đa shop |
| 13 | `order_items` | order | Lưu chi tiết từng mặt hàng trong đơn (với snapshot giá) |
| 14 | `order_admin_status_history` | order | Lưu vết lịch sử thay đổi trạng thái đơn hàng do Admin can thiệp |
| 15 | `payments` | payment | Lưu giao dịch thanh toán của payment group |
| 16 | `refund_attempts` | payment | Lưu nhật ký các lần yêu cầu hoàn tiền VNPay của đơn hàng |
| 17 | `vouchers` | voucher | Lưu mã giảm giá (quản lý số lượt dùng qua cột `used_count`) |
| 18 | `reviews` | review | Lưu đánh giá sản phẩm của Buyer |
| 19 | `review_images` | review | Lưu URL ảnh đính kèm đánh giá |
| 20 | `notifications` | notification | Lưu thông báo hệ thống |
| 21 | `chat_rooms` | chat | Lưu phòng chat Buyer-Shop |
| 22 | `chat_messages` | chat | Lưu tin nhắn thời gian thực trong phòng chat |"""

    assert old_db_overview in content, "old_db_overview not found in content!"
    content = content.replace(old_db_overview, new_db_overview, 1)
    print("Replaced Database Table overview (22 tables) in Chapter 3 successfully.")

    # ==========================================
    # 5. CHAPTER 3: UI screens (Customer, Vendor, Admin)
    # ==========================================
    old_cust_table = """**Bảng 3.8.** Danh sách màn hình Customer

| STT | Màn hình | Route | Trạng thái |
|---|---|---|---|
| 1 | Đăng nhập | `/login` | IMPLEMENTED |
| 2 | Đăng ký | `/register` | IMPLEMENTED |
| 3 | Danh sách sản phẩm | `/products` | IMPLEMENTED |
| 4 | Chi tiết sản phẩm | `/products/:id` | IMPLEMENTED |
| 5 | Giỏ hàng | `/cart` | IMPLEMENTED |
| 6 | Checkout | `/checkout` | IMPLEMENTED |
| 7 | Kết quả thanh toán | `/payment-result` | IMPLEMENTED |
| 8 | Lịch sử đơn hàng | `/orders` | IMPLEMENTED |
| 9 | Chi tiết đơn hàng | `/orders/:id` | IMPLEMENTED |
| 10 | Hồ sơ cá nhân | `/profile` | PARTIAL — chỉ hiển thị, không chỉnh sửa |
| 11 | Chat (developer) | `/chat-test` | PARTIAL — chưa có UI production |"""

    new_cust_table = """**Bảng 3.8.** Danh sách màn hình Customer

| STT | Màn hình | Route / Component | Trạng thái |
|---|---|---|---|
| 1 | Đăng nhập | `/login` | IMPLEMENTED |
| 2 | Đăng ký | `/register` | IMPLEMENTED |
| 3 | Danh sách sản phẩm & Lọc BFS | `/products` | IMPLEMENTED |
| 4 | Chi tiết sản phẩm & Biến thể | `/products/:id` | IMPLEMENTED |
| 5 | Giỏ hàng đa shop | `/cart` | IMPLEMENTED |
| 6 | Checkout đa shop & Áp voucher | `/checkout` | IMPLEMENTED |
| 7 | Kết quả thanh toán | `/payment-result` | IMPLEMENTED |
| 8 | Lịch sử đơn hàng | `/orders` | IMPLEMENTED |
| 9 | Chi tiết đơn & Xác nhận nhận hàng | `/orders/:id` | IMPLEMENTED |
| 10 | Hồ sơ cá nhân | `/profile` | IMPLEMENTED |
| 11 | Khung chat nổi đa trang | `FloatingChatWidget` (Toàn cục) | IMPLEMENTED |"""

    assert old_cust_table in content, "old_cust_table not found in content!"
    content = content.replace(old_cust_table, new_cust_table, 1)

    # Add descriptions for Profile & FloatingChatWidget after 3.6.1.6
    old_after_pay_result = """**Luồng:** Frontend polling `GET /api/v1/payments/group/:id/status` + gọi IPN sync.

---

### 3.6.2. Thiết kế giao diện — Vendor (Người bán)"""

    new_after_pay_result = """**Luồng:** Frontend polling `GET /api/v1/payments/group/:id/status` + gọi IPN sync.

#### 3.6.1.7. Màn hình Hồ sơ cá nhân (Profile)

**Thành phần:** Hiển thị thông tin định danh (Email, Họ và tên, Số điện thoại, Vai trò tài khoản); Form cập nhật cho phép người dùng chỉnh sửa thông tin họ tên và số điện thoại; Nút "Lưu thay đổi" gọi API đồng bộ dữ liệu.

**API:** `GET /api/v1/users/me`, `PUT /api/v1/users/me`

#### 3.6.1.8. Khung chat nổi toàn diện (FloatingChatWidget)

**Thành phần:** Biểu tượng nút tròn nổi cố định ở góc dưới cùng bên phải màn hình trên mọi trang (`PublicLayout.tsx`); Badge đếm số lượng tin nhắn chưa đọc; Khi bấm mở ra khung chat hoàn chỉnh gồm danh sách phòng chat với các shop, khung hiển thị lịch sử tin nhắn cuộn tự động, input nhập nội dung và nút gửi; Tự động kết nối WebSocket STOMP kèm JWT token ngay khi người dùng đăng nhập.

**Giao thức/API:** WebSocket STOMP (`/ws`, `/app/chat.send`, `/topic/chat/{roomId}`)

---

### 3.6.2. Thiết kế giao diện — Vendor (Người bán)"""

    assert old_after_pay_result in content, "old_after_pay_result not found in content!"
    content = content.replace(old_after_pay_result, new_after_pay_result, 1)

    # Vendor table and analytics description
    old_vendor_table = """**Bảng 3.9.** Danh sách màn hình Vendor

| Màn hình | Route | Trạng thái |
|---|---|---|
| Đăng ký gian hàng | Trigger bởi RequireShop | IMPLEMENTED |
| Chờ duyệt | Trigger bởi RequireShop | IMPLEMENTED |
| Danh sách sản phẩm | `/vendor/products` | IMPLEMENTED |
| Tạo sản phẩm | `/vendor/products/new` | IMPLEMENTED |
| Chỉnh sửa sản phẩm | `/vendor/products/:id/edit` | IMPLEMENTED |
| Quản lý đơn hàng | `/vendor/orders` | IMPLEMENTED |
| Quản lý voucher | `/vendor/vouchers` | IMPLEMENTED |
| Phản hồi review | `/products/:id#reviews` | IMPLEMENTED |"""

    new_vendor_table = """**Bảng 3.9.** Danh sách màn hình Vendor

| Màn hình | Route | Trạng thái |
|---|---|---|
| Đăng ký gian hàng | Trigger bởi RequireShop | IMPLEMENTED |
| Chờ duyệt | Trigger bởi RequireShop | IMPLEMENTED |
| Danh sách sản phẩm | `/vendor/products` | IMPLEMENTED |
| Tạo sản phẩm | `/vendor/products/new` | IMPLEMENTED |
| Chỉnh sửa sản phẩm | `/vendor/products/:id/edit` | IMPLEMENTED |
| Quản lý đơn hàng | `/vendor/orders` | IMPLEMENTED |
| Quản lý voucher | `/vendor/vouchers` | IMPLEMENTED |
| Phản hồi review | `/products/:id#reviews` | IMPLEMENTED |
| Phân tích doanh thu | `/vendor/analytics` | IMPLEMENTED |"""

    assert old_vendor_table in content, "old_vendor_table not found in content!"
    content = content.replace(old_vendor_table, new_vendor_table, 1)

    old_after_vendor_orders = """**Thành phần:** 5 tab filter (Tất cả / Chờ / Đã xác nhận / Đang giao / Hoàn thành); Mỗi đơn hiển thị sản phẩm (snapshot), tổng tiền, địa chỉ; Nút hành động theo trạng thái; Modal nhập lý do khi hủy.

---

### 3.6.3. Thiết kế giao diện — Admin (Quản trị viên)"""

    new_after_vendor_orders = """**Thành phần:** 5 tab filter (Tất cả / Chờ / Đã xác nhận / Đang giao / Hoàn thành); Mỗi đơn hiển thị sản phẩm (snapshot), tổng tiền, địa chỉ; Nút hành động theo trạng thái; Modal nhập lý do khi hủy.

#### 3.6.2.5. Màn hình Phân tích doanh thu Vendor

**Thành phần:** 3 thẻ chỉ số KPI kinh doanh nổi bật (Tổng doanh thu 30 ngày, Tổng số đơn hàng hoàn thành, Giá trị trung bình đơn); Biểu đồ cột trực quan hóa doanh thu theo ngày; Bảng chi tiết các đơn hàng đóng góp doanh thu gần nhất.

**API:** `GET /api/v1/vendor/analytics/revenue`

---

### 3.6.3. Thiết kế giao diện — Admin (Quản trị viên)"""

    assert old_after_vendor_orders in content, "old_after_vendor_orders not found in content!"
    content = content.replace(old_after_vendor_orders, new_after_vendor_orders, 1)

    # Admin screens and dashboard
    old_admin_table = """**Bảng 3.10.** Danh sách màn hình Admin

| Màn hình | Route | Trạng thái |
|---|---|---|
| Dashboard | `/admin` | ⚠️ Placeholder — chưa có widget |
| Quản lý gian hàng | `/admin/shop` | IMPLEMENTED |
| Danh sách sản phẩm | `/admin/products` | IMPLEMENTED |
| Tạo/Sửa sản phẩm | `/admin/products/create`, `/:id/edit` | IMPLEMENTED |
| Danh sách đơn hàng | `/admin/orders` | IMPLEMENTED |
| Chi tiết đơn hàng | `/admin/orders/:id` | IMPLEMENTED |
| Quản lý voucher | `/admin/vouchers` | IMPLEMENTED |"""

    new_admin_table = """**Bảng 3.10.** Danh sách màn hình Admin

| Màn hình | Route | Trạng thái |
|---|---|---|
| Dashboard toàn sàn | `/admin` | IMPLEMENTED |
| Quản lý gian hàng & Khóa shop | `/admin/shop` | IMPLEMENTED |
| Danh sách sản phẩm | `/admin/products` | IMPLEMENTED |
| Tạo/Sửa sản phẩm | `/admin/products/create`, `/:id/edit` | IMPLEMENTED |
| Danh sách đơn hàng | `/admin/orders` | IMPLEMENTED |
| Chi tiết đơn hàng & Hoàn tiền | `/admin/orders/:id` | IMPLEMENTED |
| Quản lý voucher hệ thống | `/admin/vouchers` | IMPLEMENTED |"""

    assert old_admin_table in content, "old_admin_table not found in content!"
    content = content.replace(old_admin_table, new_admin_table, 1)

    old_admin_shop_end = """**Sau khi duyệt:** `ShopApprovedEvent` → ROLE_VENDOR được cấp + email gửi qua Gmail SMTP.

---

## 3.7. Kết quả triển khai hệ thống"""

    new_admin_shop_end = """**Sau khi duyệt:** `ShopApprovedEvent` → ROLE_VENDOR được cấp + email gửi qua Gmail SMTP.

**Thao tác kiểm soát gian hàng vi phạm:**
- Trên trang `/admin/shop`, đối với gian hàng đang hoạt động (`ACTIVE`), Admin có nút **"Khóa Shop"** (`BANNED`). Khi bấm, hệ thống chuyển trạng thái shop và tức thì ngăn chặn việc đăng sản phẩm mới hoặc bán hàng.
- Đối với shop bị khóa (`BANNED`), Admin có nút **"Mở Khóa"** (`ACTIVE`) để phục hồi quyền kinh doanh cho người bán sau khi giải quyết vi phạm.

#### 3.6.3.2. Màn hình Admin Dashboard

**Thành phần:** 4 thẻ KPI toàn diện của toàn sàn (Tổng doanh thu hệ thống, Tổng số đơn hàng, Tổng số người dùng đăng ký, Tổng số gian hàng đang hoạt động); Biểu đồ trực quan hóa doanh thu theo mốc thời gian; Bảng theo dõi 10 đơn hàng mới nhất trên toàn hệ thống kèm trạng thái thanh toán và gian hàng xử lý.

**API:** `GET /api/v1/admin/statistics/overview`

---

## 3.7. Kết quả triển khai hệ thống"""

    assert old_admin_shop_end in content, "old_admin_shop_end not found in content!"
    content = content.replace(old_admin_shop_end, new_admin_shop_end, 1)
    print("Replaced UI screens (Customer, Vendor, Admin) in Chapter 3 successfully.")

    # ==========================================
    # 6. CHAPTER 3: Results (Table 3.11) & Limitations (3.7.4)
    # ==========================================
    old_table_311 = """### 3.7.2. Chức năng đã hoàn thành

**Bảng 3.11.** Phân loại trạng thái chức năng theo actor

| Actor | Feature | Status |
|---|---|---|
| Customer | Đăng ký / Đăng nhập / Đăng xuất | IMPLEMENTED |
| Customer | Xem danh sách + tìm kiếm + filter danh mục | IMPLEMENTED |
| Customer | Chi tiết sản phẩm + chọn biến thể | IMPLEMENTED |
| Customer | Thêm / Cập nhật / Xóa khỏi giỏ hàng | IMPLEMENTED |
| Customer | Checkout đa gian hàng + voucher | IMPLEMENTED |
| Customer | Thanh toán VNPay Sandbox | IMPLEMENTED |
| Customer | Thanh toán COD | IMPLEMENTED |
| Customer | Xem lịch sử đơn hàng + chi tiết | IMPLEMENTED |
| Customer | Viết / Sửa / Xóa đánh giá | IMPLEMENTED |
| Customer | Nhận và đọc thông báo | IMPLEMENTED |
| Customer | Chat realtime (dev UI) | PARTIAL |
| Customer | Chỉnh sửa hồ sơ cá nhân | MISSING |
| Vendor | Đăng ký gian hàng + RequireShop gate | IMPLEMENTED |
| Vendor | Tạo / Sửa sản phẩm + ảnh Cloudinary + biến thể | IMPLEMENTED |
| Vendor | Quản lý đơn hàng + cập nhật trạng thái | IMPLEMENTED |
| Vendor | Tạo voucher SHOP | IMPLEMENTED |
| Vendor | Phản hồi đánh giá | IMPLEMENTED |
| Vendor | Nhận thông báo đơn hàng mới | IMPLEMENTED |
| Vendor | Chat realtime | PARTIAL |
| Vendor | Thống kê doanh thu | MISSING |
| Admin | Duyệt / Khóa gian hàng + event email | IMPLEMENTED |
| Admin | Xem danh sách + chi tiết đơn hàng | IMPLEMENTED |
| Admin | Quản lý sản phẩm (CRUD) | IMPLEMENTED |
| Admin | Tạo voucher SYSTEM | IMPLEMENTED |
| Admin | Dashboard thống kê | MISSING (Placeholder) |
| Admin | Quản lý người dùng | MISSING |
| System | IPN payment với idempotency | IMPLEMENTED |
| System | Event-driven notification (AFTER_COMMIT) | IMPLEMENTED |
| System | Snapshot giá khi đặt hàng | IMPLEMENTED |
| System | Proportional voucher split | IMPLEMENTED |"""

    new_table_311 = """### 3.7.2. Chức năng đã hoàn thành

**Bảng 3.11.** Phân loại trạng thái chức năng theo actor

| Actor | Feature | Status |
|---|---|---|
| Customer | Đăng ký / Đăng nhập / Đăng xuất (JWT 24h) | IMPLEMENTED |
| Customer | Xem danh sách + tìm kiếm BFS + lọc khoảng giá | IMPLEMENTED |
| Customer | Chi tiết sản phẩm + chọn biến thể SKU | IMPLEMENTED |
| Customer | Thêm / Cập nhật / Xóa khỏi giỏ hàng đa shop | IMPLEMENTED |
| Customer | Checkout đa gian hàng (tách N đơn) + áp voucher tỷ lệ | IMPLEMENTED |
| Customer | Thanh toán VNPay Sandbox & COD an toàn | IMPLEMENTED |
| Customer | Xem lịch sử đơn hàng + Chi tiết đơn hàng | IMPLEMENTED |
| Customer | Khách chủ động xác nhận nhận hàng (`DELIVERED`) | IMPLEMENTED |
| Customer | Viết / Sửa / Xóa đánh giá kèm ảnh thực tế | IMPLEMENTED |
| Customer | Nhận thông báo đẩy thời gian thực (STOMP Push) | IMPLEMENTED |
| Customer | Nhắn tin thời gian thực qua Khung chat nổi (`FloatingChatWidget`) | IMPLEMENTED |
| Customer | Xem và chỉnh sửa hồ sơ cá nhân (`/profile`) | IMPLEMENTED |
| Customer | Gợi ý sản phẩm lai (Hybrid Recommendation) | IMPLEMENTED |
| Vendor | Đăng ký gian hàng + Cổng kiểm soát RequireShop | IMPLEMENTED |
| Vendor | Tạo / Sửa sản phẩm + upload ảnh Cloudinary + biến thể | IMPLEMENTED |
| Vendor | Quản lý đơn hàng + cập nhật trạng thái (Confirm → Ship) | IMPLEMENTED |
| Vendor | Hủy đơn hàng với lý do kèm tự động hoàn tồn kho | IMPLEMENTED |
| Vendor | Tạo và quản lý voucher riêng của Shop | IMPLEMENTED |
| Vendor | Phản hồi công khai đánh giá của khách hàng | IMPLEMENTED |
| Vendor | Nhận thông báo đơn hàng mới qua WebSocket STOMP | IMPLEMENTED |
| Vendor | Chat trực tiếp với khách hàng thời gian thực | IMPLEMENTED |
| Vendor | Phân tích doanh thu & chỉ số kinh doanh 30 ngày | IMPLEMENTED |
| Admin | Duyệt / Từ chối / Khóa / Mở khóa gian hàng + gửi mail | IMPLEMENTED |
| Admin | Giám sát danh sách + chi tiết đơn hàng toàn hệ thống | IMPLEMENTED |
| Admin | Quản lý sản phẩm toàn sàn (CRUD) | IMPLEMENTED |
| Admin | Tạo voucher toàn sàn (`SYSTEM`) phân bổ tỷ lệ chiết khấu | IMPLEMENTED |
| Admin | Dashboard thống kê doanh thu KPI toàn sàn | IMPLEMENTED |
| System | Xử lý VNPay IPN với khóa bi quan chống duplicate | IMPLEMENTED |
| System | Cơ chế sự kiện `@TransactionalEventListener AFTER_COMMIT` | IMPLEMENTED |
| System | Snapshot giá và thuộc tính khi đặt hàng | IMPLEMENTED |
| System | Thuật toán phân bổ chiết khấu voucher tỷ lệ (Proportional Split) | IMPLEMENTED |
| System | Tự động hoàn trả tồn kho biến thể khi hủy đơn hàng | IMPLEMENTED |"""

    assert old_table_311 in content, "old_table_311 not found in content!"
    content = content.replace(old_table_311, new_table_311, 1)

    old_limitations = """### 3.7.4. Hạn chế còn tồn tại

#### 3.7.4.1. Frontend chưa hoàn thiện

| Hạn chế | Mức độ |
|---|---|
| Chat UI production chưa hoàn thiện (`FloatingChatWidget` chưa có panel mở rộng) | Cao |
| Admin Dashboard chỉ có text placeholder | Cao |
| ProfilePage chỉ hiển thị, không chỉnh sửa | Trung bình |
| Không có màn hình quản lý danh mục, thương hiệu, người dùng (Admin) | Trung bình |

#### 3.7.4.2. Backend chưa hoàn thiện

| Hạn chế | Mức độ |
|---|---|
| Không có API cập nhật hồ sơ người dùng | Trung bình |
| Không có API thống kê doanh thu | Cao |
| Schema quản lý bằng `ddl-auto: update` (rủi ro production) | Trung bình |
| Không có hoàn tiền (Refund) | Cao |
| Không có full-text search | Trung bình |

#### 3.7.4.3. Bảo mật

| Hạn chế | Mức độ |
|---|---|
| JWT không có token revocation | Cao |
| Không có Rate Limiting | Cao |
| Không có HTTPS trong config mặc định | Trung bình |

#### 3.7.4.4. Deployment

| Hạn chế | Mức độ |
|---|---|
| Không có Docker / container hóa | Cao |
| Không có CI/CD pipeline | Cao |
| WebSocket in-memory broker — không scale horizontal | Cao |
| VNPay chỉ sandbox | Cao |"""

    new_limitations = """### 3.7.4. Hạn chế còn tồn tại

#### 3.7.4.1. Giao diện người dùng (Frontend)

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Chưa có giao diện quản lý danh mục (Category) riêng cho Admin | Trung bình | Hiện cây danh mục được khởi tạo hoặc quản lý qua API/CSDL |
| Chưa có giao diện quản trị người dùng (User Management) chuyên sâu | Trung bình | Hiện Admin quản lý tài khoản người bán thông qua duyệt/khóa Shop |
| Chưa có tính năng đổi mật khẩu / quên mật khẩu trên giao diện | Trung bình | Người dùng chưa thể tự reset password trực tiếp trên UI |

#### 3.7.4.2. Xử lý nghiệp vụ Backend

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Hoàn tiền VNPay tự động E2E bị hạn chế Sandbox IP whitelist | Cao | Backend đã có Service và bảng `refund_attempts`, nhưng API VNPay QueryDR/Refund yêu cầu IP tĩnh public |
| Schema cơ sở dữ liệu quản lý bằng `ddl-auto: update` | Trung bình | Phù hợp môi trường phát triển local, nhưng cần chuyển sang Flyway/Liquibase khi lên production |
| Chưa tích hợp Search Engine ngoài chuyên biệt (Elasticsearch) | Thấp | Hiện hệ thống tối ưu bằng cây đệ quy BFS + LIKE keyword và chỉ mục CSDL |

#### 3.7.4.3. Bảo mật

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| JWT chưa có cơ chế thu hồi token (Token Revocation/Blacklist) | Cao | Token có hiệu lực 24 giờ sau khi cấp phát |
| Chưa cấu hình Rate Limiting chống tấn công DoS/Brute Force | Cao | Cần tích hợp Bucket4j hoặc API Gateway trong tương lai |
| Chưa kích hoạt giao thức HTTPS trong cấu hình local mặc định | Trung bình | Đang chạy trên giao thức HTTP tại cổng 8080/5173 |

#### 3.7.4.4. Hạ tầng triển khai (Deployment)

| Hạn chế | Mức độ | Ghi chú thực tế |
|---|---|---|
| Chưa đóng gói container Docker và Docker Compose | Cao | Hiện tại ứng dụng vận hành native trên máy chủ vật lý local |
| Chưa thiết lập CI/CD pipeline tự động hóa kiểm thử và đóng gói | Cao | Kiểm thử và build hiện đang thực hiện thủ công qua terminal |
| WebSocket sử dụng in-memory STOMP broker | Cao | Không thể scale ngang nhiều backend instance nếu không có Redis Pub/Sub |
| Cổng thanh toán VNPay chỉ dừng ở môi trường Sandbox thử nghiệm | Cao | Chưa tiến hành ký hợp đồng và đấu nối môi trường production thật |"""

    assert old_limitations in content, "old_limitations not found in content!"
    content = content.replace(old_limitations, new_limitations, 1)
    print("Replaced Results and Limitations in Chapter 3 successfully.")

    # ==========================================
    # 7. CHAPTER 4: Conclusion & Future Roadmap
    # ==========================================
    old_ch4_results = """### 4.1.1. Về chức năng

Sau quá trình phát triển qua 7 Phase hoàn thành (Foundation, Authentication, Product Catalog, Shop, Shopping, Order, Payment, Supporting Features), hệ thống Enterprise Ecommerce đã triển khai đầy đủ luồng nghiệp vụ cốt lõi của một sàn TMĐT đa người bán:

- **Gợi ý sản phẩm (Product Recommendation):** Hệ thống tích hợp thuật toán Hybrid (Cá nhân hóa theo lịch sử mua hàng + Trending + Sản phẩm mới) giúp tăng trải nghiệm mua sắm.

- **Luồng hoàn chỉnh từ đầu đến cuối:** Người dùng có thể đăng ký, duyệt sản phẩm từ nhiều gian hàng, thêm vào giỏ hàng, checkout đa gian hàng, thanh toán (VNPay hoặc COD), xem trạng thái đơn hàng, viết đánh giá và nhận thông báo tự động.

- **Luồng Vendor hoàn chỉnh:** Người bán có thể đăng ký gian hàng, chờ Admin duyệt, đăng bán sản phẩm với ảnh Cloudinary và biến thể, quản lý đơn hàng theo state machine, tạo mã giảm giá và phản hồi đánh giá.

- **Luồng Admin hoàn chỉnh (một phần):** Admin có thể duyệt/khóa gian hàng, xem toàn bộ đơn hàng và tạo voucher hệ thống. Dashboard thống kê chưa được triển khai.

- **Thông báo event-driven:** Hệ thống thông báo tự động hoạt động sau mỗi sự kiện quan trọng (đặt hàng, thanh toán, giao hàng, đánh giá, duyệt shop) thông qua `@TransactionalEventListener AFTER_COMMIT`.

- **Chat realtime:** WebSocket STOMP hoạt động hai chiều giữa Buyer và Vendor. Giao diện production chưa hoàn thiện — đây là hạn chế được ghi nhận.

### 4.1.2. Về kỹ thuật"""

    new_ch4_results = """### 4.1.1. Về chức năng

Sau quá trình phát triển qua 9 Phase hoàn thành toàn diện, hệ thống Enterprise Ecommerce đã triển khai đầy đủ chu trình nghiệp vụ khép kín của một sàn TMĐT đa người bán hiện đại:

- **Luồng hoàn chỉnh từ đầu đến cuối:** Khách hàng có thể đăng ký, tìm kiếm sản phẩm phân cấp cây BFS, lọc khoảng giá, gom sản phẩm từ nhiều shop vào giỏ hàng, checkout đa gian hàng (tự động tách N đơn), thanh toán trực tuyến qua VNPay hoặc COD, nhận thông báo đẩy tức thì, trao đổi trực tiếp qua khung chat nổi `FloatingChatWidget`, theo dõi trạng thái đơn hàng, chủ động bấm xác nhận nhận hàng và đánh giá chất lượng sản phẩm.

- **Luồng Vendor hoàn chỉnh:** Người bán đăng ký gian hàng, chờ Admin duyệt, đăng bán sản phẩm kèm biến thể (SKU, tồn kho, giá) và upload ảnh Cloudinary, quản lý đơn hàng theo máy trạng thái, hủy đơn kèm tự động hoàn kho, tạo mã giảm giá riêng của shop, phản hồi đánh giá và theo dõi báo cáo phân tích doanh thu 30 ngày trực quan.

- **Luồng Admin hoàn chỉnh:** Admin duyệt hoặc từ chối hồ sơ shop, khóa hoặc mở khóa gian hàng vi phạm, giám sát tất cả đơn hàng trên toàn hệ thống, tạo mã giảm giá toàn sàn phân bổ tỷ lệ chiết khấu, và theo dõi các chỉ số KPI doanh thu toàn diện tại Admin Dashboard (`/admin`).

- **Giao tiếp & Thông báo Realtime:** Hệ thống thông báo đẩy (Push Notification) và khung chat nổi (`FloatingChatWidget`) hoạt động thời gian thực hai chiều giữa Buyer và Vendor qua WebSocket STOMP với xác thực JWT trên kết nối.

- **Gợi ý sản phẩm thông minh:** Động cơ gợi ý sản phẩm lai (Hybrid Recommendation) kết hợp giữa độ tương quan danh mục/giá và điểm số đánh giá cộng đồng.

### 4.1.2. Về kỹ thuật"""

    assert old_ch4_results in content, "old_ch4_results not found in content!"
    content = content.replace(old_ch4_results, new_ch4_results, 1)

    old_ch4_comparison = """### 4.1.3. So sánh mục tiêu và kết quả thực tế

| Mục tiêu | Kết quả |
|---|---|
| Luồng mua hàng hoàn chỉnh | ✅ Đạt |
| Luồng Vendor hoàn chỉnh | ✅ Đạt (thiếu dashboard thống kê) |
| Luồng Admin | ⚠️ Đạt một phần (thiếu user management) |
| Kiến trúc Modular Monolith | ✅ Đạt |
| Tích hợp VNPay | ✅ Đạt (sandbox) |
| Chat realtime WebSocket | ⚠️ Backend đầy đủ; Frontend production chưa hoàn thiện |
| Thông báo event-driven | ✅ Đạt |
| Tối ưu N+1 Query | ✅ Đạt |
| Docker / Container hóa | ❌ Chưa thực hiện (Phase 9) |
| Redis cache | ❌ Chưa thực hiện (Phase 8) |"""

    new_ch4_comparison = """### 4.1.3. So sánh mục tiêu và kết quả thực tế

| Mục tiêu | Kết quả |
|---|---|
| Luồng mua hàng hoàn chỉnh | ✅ Đạt (Đầy đủ từ tìm kiếm BFS, giỏ hàng, checkout đa shop, xác nhận nhận hàng và review) |
| Luồng Vendor hoàn chỉnh | ✅ Đạt (Bao gồm quản lý sản phẩm, đơn hàng, voucher shop và phân tích doanh thu 30 ngày) |
| Luồng Admin | ✅ Đạt (Bao gồm duyệt/khóa shop, quản lý đơn toàn sàn, voucher toàn sàn và Admin Dashboard) |
| Kiến trúc Modular Monolith | ✅ Đạt (10 module độc lập, kiểm soát ranh giới qua Internal Service và Event) |
| Tích hợp VNPay an toàn | ✅ Đạt (Môi trường Sandbox với chữ ký HMAC-SHA512 và khóa bi quan chống duplicate IPN) |
| Chat realtime WebSocket | ✅ Đạt (Tích hợp Khung chat nổi FloatingChatWidget toàn cục với STOMP Messaging) |
| Thông báo event-driven | ✅ Đạt (Kết hợp STOMP Push tức thì và lưu trữ CSDL sau commit giao dịch) |
| Tối ưu hóa N+1 Query | ✅ Đạt (Kiểm thử tự động bằng Hibernate Statistics và giải quyết bằng BatchSize) |
| Docker / Container hóa | ❌ Chưa thực hiện (Dự kiến chuyển sang Phase 10 - Lộ trình tương lai) |
| Redis cache | ❌ Chưa thực hiện (Dự kiến chuyển sang Phase 10 - Lộ trình tương lai) |"""

    assert old_ch4_comparison in content, "old_ch4_comparison not found in content!"
    content = content.replace(old_ch4_comparison, new_ch4_comparison, 1)

    old_ch4_roadmap = """## 4.3. Hướng phát triển

### 4.3.1. Ngắn hạn (ưu tiên cao)

**1. Hoàn thiện Chat UI production**
- Triển khai `FloatingChatWidget` với đầy đủ expanded panel, danh sách room, lịch sử tin nhắn.
- Thêm typing indicator và read receipts.

**2. Tính năng người dùng cơ bản**
- Quên mật khẩu / Reset password qua email.
- Cho phép Customer tự hủy đơn hàng PENDING.

**3. Quản lý hệ thống**
- Tính năng quản lý Người dùng (User Management) cho Admin.
- Tính năng quản lý Danh mục (Category Management).

**4. Bảo mật cơ bản**
- Triển khai Rate Limiting (Bucket4j hoặc Spring Security rate limit).
- JWT Refresh Token mechanism.

### 4.3.2. Trung hạn

**5. Redis Cache**
- Cache sản phẩm phổ biến, danh mục, session.
- Redis Pub/Sub cho WebSocket broker — cho phép scale horizontal.

**6. Docker & CI/CD**
- Dockerfile cho Backend và Frontend.
- `docker-compose.yml` gom MySQL + Backend + Frontend.
- GitHub Actions workflow: test → build → deploy.

**7. Tìm kiếm nâng cao**
- Tích hợp Elasticsearch hoặc MySQL Full-Text Search.
- Bộ lọc sản phẩm nâng cao: khoảng giá, màu sắc, kích thước, rating.

### 4.3.3. Dài hạn

**8. Hoàn trả tồn kho khi hủy đơn**
- Khi Vendor hủy đơn: `product_variant.stock_quantity += order_item.quantity`.
- Hiện tại là technical debt đã ghi nhận.

**9. Hệ thống thanh toán production**
- Đăng ký VNPay production (thật).
- Hỗ trợ thêm: MoMo, ZaloPay.

**10. Ứng dụng di động**
- React Native hoặc Flutter sử dụng cùng Backend API.
- Push Notification qua FCM."""

    new_ch4_roadmap = """## 4.3. Hướng phát triển

### 4.3.1. Ngắn hạn (ưu tiên cao)

**1. Tính năng tài khoản người dùng mở rộng**
- Triển khai quy trình quên mật khẩu và Reset password tự động qua email Gmail SMTP.
- Cho phép khách hàng tự gửi yêu cầu hủy đơn hàng khi đơn còn ở trạng thái `PENDING`.

**2. Quản trị hệ thống chuyên sâu**
- Xây dựng giao diện quản lý người dùng (User Management) trực quan cho Quản trị viên.
- Xây dựng giao diện quản trị cây danh mục sản phẩm (Category Tree Management) cho Admin.

**3. Tăng cường bảo mật ứng dụng**
- Tích hợp Rate Limiting (sử dụng Bucket4j) trên các endpoint nhạy cảm như đăng nhập, checkout, tìm kiếm.
- Triển khai cơ chế JWT Refresh Token kết hợp xoay vòng (Token Rotation) nhằm tăng cường bảo mật phiên làm việc.

### 4.3.2. Trung hạn

**4. Hạ tầng Caching và Scalability (Redis)**
- Triển khai Redis Cache để lưu tạm thông tin danh mục, sản phẩm nổi bật và dữ liệu session.
- Sử dụng Redis Pub/Sub làm Message Broker ngoài cho WebSocket STOMP, cho phép hệ thống scale ngang nhiều instance backend.

**5. Container hóa và Tự động hóa CI/CD**
- Đóng gói Dockerfile chuẩn cho Spring Boot Backend và multi-stage Dockerfile cho React Frontend (Nginx).
- Xây dựng `docker-compose.yml` điều phối toàn bộ hệ thống gồm MySQL Server, Redis Cache, Backend Service và Frontend.
- Thiết lập GitHub Actions tự động chạy Integration Test và đóng gói Docker image khi có commit mới.

**6. Công cụ tìm kiếm chuyên dụng (Elasticsearch)**
- Tích hợp cụm Elasticsearch để lập chỉ mục toàn văn (Full-Text Search) cho sản phẩm, hỗ trợ tìm kiếm mờ (Fuzzy Search), gợi ý từ khóa (Autocomplete) và tự sửa lỗi chính tả.

### 4.3.3. Dài hạn

**7. Đấu nối cổng thanh toán môi trường Production**
- Đăng ký hợp đồng thương mại với VNPay để chuyển đổi từ Sandbox sang môi trường thanh toán thật.
- Mở rộng thêm các ví điện tử phổ biến tại Việt Nam: MoMo, ZaloPay, ShopeePay.

**8. Phát triển ứng dụng di động (Mobile App)**
- Phát triển ứng dụng di động cho cả iOS và Android bằng React Native hoặc Flutter, tái sử dụng toàn bộ hệ thống REST API và WebSocket backend sẵn có.
- Tích hợp Firebase Cloud Messaging (FCM) để gửi thông báo đẩy trực tiếp lên thiết bị di động."""

    assert old_ch4_roadmap in content, "old_ch4_roadmap not found in content!"
    content = content.replace(old_ch4_roadmap, new_ch4_roadmap, 1)
    print("Replaced Chapter 4 conclusions and roadmap successfully.")

    # ==========================================
    # 8. APPENDICES: Appendix A & B
    # ==========================================
    old_app_a = """| Customer | Chat realtime | PARTIAL | Backend OK; UI chỉ /chat-test |
| Customer | Chỉnh sửa hồ sơ | IMPLEMENTED | `PUT /api/v1/users/me`, `ProfilePage.tsx` |
| Customer | Đổi mật khẩu | MISSING | Không có endpoint |
| Customer | Quên mật khẩu | MISSING | Không có flow |
| Customer | Hủy đơn hàng | MISSING | Không có endpoint phía Customer |
| Vendor | Đăng ký gian hàng | IMPLEMENTED | `VendorShopRegistrationPage.tsx` |
| Vendor | Chờ duyệt | IMPLEMENTED | `ShopPendingPage.tsx` |
| Vendor | Tạo sản phẩm + ảnh + biến thể | IMPLEMENTED | `VendorProductFormPage.tsx` |
| Vendor | Chỉnh sửa sản phẩm | IMPLEMENTED | Form pre-fill |
| Vendor | Quản lý đơn hàng | IMPLEMENTED | `VendorOrderListPage.tsx` |
| Vendor | Cập nhật trạng thái đơn | IMPLEMENTED | `PATCH /api/v1/vendor/orders/:id/status` |
| Vendor | Hủy đơn (với lý do) | IMPLEMENTED | Modal nhập lý do |
| Vendor | Tạo voucher SHOP | IMPLEMENTED | `POST /api/v1/vouchers` (scope=SHOP) |
| Vendor | Phản hồi đánh giá | IMPLEMENTED | `PUT /api/v1/reviews/:id/vendor-reply` |
| Vendor | Nhận notification | IMPLEMENTED | NEW_ORDER, NEW_REVIEW |
| Vendor | Chat realtime | PARTIAL | Backend OK; UI chưa production |
| Vendor | Thống kê doanh thu | IMPLEMENTED | `GET /api/v1/vendor/analytics`, `VendorAnalyticsPage.tsx` |
| Vendor | Chỉnh sửa thông tin shop | MISSING | Không có API/UI |
| Admin | Xem danh sách shop | IMPLEMENTED | `GET /api/v1/admin/shops` |
| Admin | Duyệt / Từ chối / Khóa shop | IMPLEMENTED | + email + ROLE_VENDOR |
| Admin | Xem đơn hàng toàn hệ thống | IMPLEMENTED | `AdminOrderListPage.tsx` |
| Admin | Xem chi tiết đơn hàng | IMPLEMENTED | `AdminOrderDetailPage.tsx` |
| Admin | Quản lý sản phẩm (CRUD) | IMPLEMENTED | `AdminProductFormPage.tsx` |
| Admin | Tạo voucher SYSTEM | IMPLEMENTED | scope=SYSTEM, FREE_SHIPPING |
| Admin | Dashboard thống kê | IMPLEMENTED | `GET /api/v1/admin/statistics`, `AdminDashboardPage.tsx` |
| Admin | Quản lý người dùng | MISSING | Không có route /admin/users |
| Admin | Quản lý danh mục | MISSING | Không có UI |
| System | IPN idempotency | IMPLEMENTED | PESSIMISTIC_WRITE lock |
| System | Email duyệt shop | IMPLEMENTED | Gmail SMTP, AFTER_COMMIT |
| System | Snapshot giá đặt hàng | IMPLEMENTED | `OrderItem.unitPrice` |
| System | Proportional voucher split | IMPLEMENTED | Checkout service logic |
| Admin | Hoàn tiền VNPay (Refund) | IMPLEMENTED | `POST /api/v1/admin/orders/:id/refunds`, `AdminOrderDetailPage.tsx` |
| Customer | Gợi ý sản phẩm (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations` |
| Admin | Lịch sử trạng thái đơn hàng | MISSING | BE có `GET /api/v1/admin/orders/:id/status-history` nhưng FE không gọi |
| Admin | Chi tiết hoàn tiền (Refund Detail) | MISSING | BE có `GET /api/v1/admin/refunds/:id` nhưng FE không gọi |
| Admin | Đối soát hoàn tiền (Reconcile) | MISSING | BE có `POST /api/v1/admin/refunds/:id/reconcile` nhưng FE không gọi |
| Customer | Kiểm tra quyền review (Eligibility) | MISSING | BE có `GET /api/v1/products/:id/reviews/eligibility` nhưng FE dùng logic khác |"""

    new_app_a = """| Customer | Chat realtime đa trang | IMPLEMENTED | `FloatingChatWidget.tsx`, WebSocket STOMP |
| Customer | Chỉnh sửa hồ sơ cá nhân | IMPLEMENTED | `PUT /api/v1/users/me`, `ProfilePage.tsx` |
| Customer | Xác nhận nhận hàng (`DELIVERED`) | IMPLEMENTED | `POST /api/v1/orders/:id/confirm-delivery`, `OrderDetailPage.tsx` |
| Customer | Tìm kiếm nâng cao BFS & Lọc giá | IMPLEMENTED | `ProductListPage.tsx`, `GET /api/v1/products` |
| Customer | Gợi ý sản phẩm lai (Recommendation) | IMPLEMENTED | `GET /api/v1/products/recommendations`, `RecommendationSection.tsx` |
| Customer | Đổi mật khẩu | MISSING | Chưa có endpoint riêng |
| Customer | Quên mật khẩu | MISSING | Chưa có flow gửi token reset |
| Customer | Hủy đơn hàng | MISSING | Khách hàng chưa thể tự hủy đơn trên giao diện |
| Vendor | Đăng ký gian hàng | IMPLEMENTED | `VendorShopRegistrationPage.tsx` |
| Vendor | Chờ duyệt | IMPLEMENTED | `ShopPendingPage.tsx` |
| Vendor | Tạo sản phẩm + ảnh + biến thể | IMPLEMENTED | `VendorProductFormPage.tsx` |
| Vendor | Chỉnh sửa sản phẩm | IMPLEMENTED | Form pre-fill |
| Vendor | Quản lý đơn hàng | IMPLEMENTED | `VendorOrderListPage.tsx` |
| Vendor | Cập nhật trạng thái đơn (Confirm → Ship) | IMPLEMENTED | `PATCH /api/v1/vendor/orders/:id/status` |
| Vendor | Hủy đơn (với lý do + hoàn kho) | IMPLEMENTED | Modal nhập lý do, tự động hồi số lượng tồn biến thể |
| Vendor | Tạo voucher SHOP | IMPLEMENTED | `POST /api/v1/vouchers` (scope=SHOP) |
| Vendor | Phản hồi đánh giá | IMPLEMENTED | `PUT /api/v1/reviews/:id/vendor-reply` |
| Vendor | Nhận thông báo đơn hàng mới | IMPLEMENTED | NEW_ORDER qua WebSocket STOMP |
| Vendor | Chat realtime với khách | IMPLEMENTED | Giao diện Chat WebSocket STOMP |
| Vendor | Phân tích doanh thu 30 ngày | IMPLEMENTED | `GET /api/v1/vendor/analytics/revenue`, `VendorAnalyticsPage.tsx` |
| Vendor | Chỉnh sửa thông tin shop | MISSING | Chưa có màn hình chỉnh sửa mô tả shop |
| Admin | Xem danh sách shop | IMPLEMENTED | `GET /api/v1/admin/shops`, `AdminShopListPage.tsx` |
| Admin | Duyệt / Từ chối / Khóa / Mở khóa shop | IMPLEMENTED | Thao tác đổi trạng thái PENDING, ACTIVE, BANNED + email |
| Admin | Xem đơn hàng toàn hệ thống | IMPLEMENTED | `AdminOrderListPage.tsx` |
| Admin | Xem chi tiết đơn hàng | IMPLEMENTED | `AdminOrderDetailPage.tsx` |
| Admin | Quản lý sản phẩm (CRUD) | IMPLEMENTED | `AdminProductFormPage.tsx` |
| Admin | Tạo voucher SYSTEM tỷ lệ | IMPLEMENTED | `AdminVoucherListPage.tsx`, scope=SYSTEM |
| Admin | Dashboard thống kê doanh thu | IMPLEMENTED | `GET /api/v1/admin/statistics/overview`, `AdminDashboardPage.tsx` |
| Admin | Quản lý người dùng chuyên sâu | MISSING | Chưa có route giao diện riêng cho bảng users |
| Admin | Quản lý cây danh mục UI | MISSING | Chưa có màn hình thêm/sửa node danh mục |
| System | IPN idempotency | IMPLEMENTED | Khóa bi quan PESSIMISTIC_WRITE trên bảng payments |
| System | Email duyệt shop | IMPLEMENTED | Gmail SMTP, `@TransactionalEventListener AFTER_COMMIT` |
| System | Snapshot giá và thuộc tính khi đặt hàng | IMPLEMENTED | `OrderItem.unitPrice`, `OrderItem.variantAttributesSnapshot` |
| System | Proportional voucher split | IMPLEMENTED | Thuật toán phân bổ giảm giá theo tỷ trọng doanh thu |
| System | Tự động hoàn tồn kho khi hủy đơn | IMPLEMENTED | `restoreStockOnCancellation` trong OrderServiceImpl |
| Admin | Hoàn tiền VNPay (Refund) | IMPLEMENTED | `POST /api/v1/admin/orders/:id/refunds`, `AdminOrderDetailPage.tsx` |
| Admin | Lịch sử can thiệp trạng thái đơn hàng | IMPLEMENTED | Bảng `order_admin_status_history`, `GET /api/v1/admin/orders/:id/status-history` |"""

    assert old_app_a in content, "old_app_a not found in content!"
    content = content.replace(old_app_a, new_app_a, 1)

    old_app_b = """## APPENDIX B — Database Table Summary (Tóm tắt cơ sở dữ liệu)

**Bảng B.1.** Tóm tắt 20 bảng cơ sở dữ liệu

| Bảng | Khóa chính | Khóa ngoại quan trọng | Ràng buộc đặc biệt |
|---|---|---|---|
| `users` | `id` | — | UNIQUE(`email`) |
| `roles` | `id` | — | UNIQUE(`name`) |
| `user_roles` | (`user_id`, `role_id`) | FK: `users.id`, `roles.id` | Composite PK |
| `shops` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 shop |
| `brands` | `id` | — | — |
| `categories` | `id` | FK: `categories.id` (self) | `parent_id` nullable |
| `products` | `id` | FK: `shops.id`, `categories.id`, `brands.id` | — |
| `product_variants` | `id` | FK: `products.id` | UNIQUE(`sku`) |
| `product_images` | `id` | FK: `products.id` | — |
| `carts` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 cart |
| `cart_items` | `id` | FK: `carts.id`, `product_variants.id` | — |
| `orders` | `id` | FK: `users.id`, `shops.id` | `payment_group_id` UUID |
| `order_items` | `id` | FK: `orders.id`, `product_variants.id` | `product_name`, `unit_price` là snapshot |
| `payments` | `id` | — | UNIQUE(`payment_group_id`) |
| `vouchers` | `id` | FK: `shops.id` (nullable) | UNIQUE(`code`) |
| `reviews` | `id` | FK: `users.id`, `products.id`, `orders.id` | UNIQUE(`user_id`, `product_id`, `order_id`) |
| `review_images` | `id` | FK: `reviews.id` | — |
| `notifications` | `id` | FK: `users.id` | — |
| `chat_rooms` | `id` | FK: `users.id`, `shops.id` | UNIQUE(`user_id`, `shop_id`) |
| `chat_messages` | `id` | FK: `chat_rooms.id`, `users.id` | — |"""

    new_app_b = """## APPENDIX B — Database Table Summary (Tóm tắt cơ sở dữ liệu)

**Bảng B.1.** Tóm tắt 22 bảng cơ sở dữ liệu

| Bảng | Khóa chính | Khóa ngoại quan trọng | Ràng buộc đặc biệt |
|---|---|---|---|
| `users` | `id` | — | UNIQUE(`email`) |
| `roles` | `id` | — | UNIQUE(`name`) |
| `user_roles` | (`user_id`, `role_id`) | FK: `users.id`, `roles.id` | Composite PK |
| `shops` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 shop |
| `brands` | `id` | — | — |
| `categories` | `id` | FK: `categories.id` (self) | `parent_id` nullable, Cây đệ quy BFS |
| `products` | `id` | FK: `shops.id`, `categories.id`, `brands.id` | Index tìm kiếm và lọc danh mục |
| `product_variants` | `id` | FK: `products.id` | UNIQUE(`sku`), tồn kho không âm |
| `product_images` | `id` | FK: `products.id` | Lưu URL và publicId Cloudinary |
| `carts` | `id` | FK: `users.id` | UNIQUE(`user_id`) — 1 user 1 cart |
| `cart_items` | `id` | FK: `carts.id`, `product_variants.id` | UNIQUE(`cart_id`, `variant_id`) |
| `orders` | `id` | FK: `users.id`, `shops.id` | Gom nhóm qua `payment_group_id` UUID |
| `order_items` | `id` | FK: `orders.id`, `product_variants.id` | Snapshot giá mua và thuộc tính biến thể |
| `order_admin_status_history` | `id` | FK: `orders.id` | Lưu vết thời gian và lý do can thiệp của Admin |
| `payments` | `id` | — | Index `payment_group_id`, Khóa bi quan |
| `refund_attempts` | `id` | FK: `orders.id`, `payments.id` | UNIQUE(`order_id`, `payment_id`) |
| `vouchers` | `id` | FK: `shops.id` (nullable) | UNIQUE(`code`), quản lý qua `used_count` |
| `reviews` | `id` | FK: `users.id`, `products.id`, `orders.id` | UNIQUE(`user_id`, `product_id`, `order_id`) |
| `review_images` | `id` | FK: `reviews.id` | Danh sách URL ảnh đính kèm đánh giá |
| `notifications` | `id` | FK: `users.id` | Lưu thông báo hệ thống |
| `chat_rooms` | `id` | FK: `users.id`, `shops.id` | UNIQUE(`user_id`, `shop_id`) |
| `chat_messages` | `id` | FK: `chat_rooms.id`, `users.id` | Tin nhắn STOMP thời gian thực |"""

    assert old_app_b in content, "old_app_b not found in content!"
    content = content.replace(old_app_b, new_app_b, 1)
    print("Replaced Appendix A and B successfully.")

    # Write back
    with open(file_path, 'w', encoding='utf-8') as f:
        f.write(content)

    print(f"Updated {file_path} successfully. New length: {len(content)}")

if __name__ == '__main__':
    main()
