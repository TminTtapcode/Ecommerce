# FINAL LIMITATIONS — Hạn chế còn tồn tại của hệ thống

> Tài liệu này tổng hợp các hạn chế dựa trực tiếp trên source code thực tế.
> Mỗi hạn chế phải có bằng chứng cụ thể.
> Không liệt kê hạn chế không có cơ sở.

---

## L1. FRONTEND CHƯA HOÀN THIỆN

| ID | Hạn chế | Bằng chứng (source code) | Mức độ |
|---|---|---|---|
| L1.1 | `ProfilePage` chỉ hiển thị thông tin từ JWT, không có chức năng chỉnh sửa | `ProfilePage.tsx` không có form edit, không gọi `PUT` API | Trung bình |
| L1.2 | `FloatingChatWidget` (nút chat nổi) chưa có expanded panel | Component import nhưng chỉ có UI nút, không có chat panel mở rộng | Cao |
| L1.3 | `/chat-test` là developer interface, không phải UI end-user | Trang có STOMP Event Log, nhập Shop ID thủ công, không phải UX thông thường | Cao |
| L1.4 | Admin Dashboard placeholder — không có widget thống kê | `AdminLayout` → `Outlet` render text "Chào mừng bạn đến với trang quản trị" | Cao |
| L1.5 | Ô tìm kiếm nhanh trên Admin topbar không có chức năng | Input HTML trong `AdminLayout.tsx` không có `onChange` handler hay API call | Thấp |
| L1.6 | Không có màn hình quản lý danh mục (Admin) | Không tồn tại route `/admin/categories` trong `App.tsx` | Trung bình |
| L1.7 | Không có màn hình quản lý thương hiệu (Admin) | Không tồn tại route `/admin/brands` trong `App.tsx` | Thấp |
| L1.8 | Không có màn hình quản lý người dùng (Admin) | Không tồn tại route `/admin/users` | Trung bình |
| L1.9 | Không có bộ lọc sản phẩm nâng cao | `ProductListPage.tsx` chỉ filter `keyword` và `categoryId`, không có price range, màu sắc, size | Trung bình |

---

## L2. BACKEND CHƯA HOÀN THIỆN

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L2.1 | Không có API cập nhật thông tin người dùng | Không tìm thấy `PUT /api/v1/users/me` hay `UserUpdateRequest` | Trung bình |
| L2.2 | Không có API đổi mật khẩu | Không có endpoint `changePassword` trong IdentityController | Trung bình |
| L2.3 | Không có API thống kê doanh thu | Không có `StatisticsController` hay `/admin/statistics` | Cao |
| L2.4 | Schema quản lý bằng `ddl-auto: update` | `application.yml` — không dùng Flyway/Liquibase | Trung bình |
| L2.5 | SQL logging bật mặc định | `show-sql: true`, `format_sql: true` trong `application.yml` | Thấp |
| L2.6 | Không có full-text search | Product filter dùng `LIKE %keyword%` — không có Elasticsearch hay MySQL FTS | Trung bình |
| L2.7 | Không có hoàn tiền (Refund) | Không có RefundController hay logic hoàn tiền trong PaymentService | Cao |

---

## L3. FEATURE THIẾU

| ID | Feature | Mô tả | Mức độ |
|---|---|---|---|
| L3.1 | Quên mật khẩu / Reset password | Không có flow quên mật khẩu, không có email reset | Cao |
| L3.2 | Đăng nhập mạng xã hội (OAuth2) | Không có Google/Facebook login | Thấp |
| L3.3 | Wishlist / Danh sách yêu thích | Không có bảng `wishlist` trong DB, không có UI | Thấp |
| L3.4 | Theo dõi gian hàng (Follow Shop) | Không có bảng `shop_followers`, không có API | Thấp |
| L3.5 | Address Book (đa địa chỉ giao hàng) | Chỉ có 1 trường `shippingAddress` free-text trong Order | Trung bình |
| L3.6 | Tính phí vận chuyển theo khu vực | Không có shipping fee calculation | Trung bình |
| L3.7 | Hủy đơn hàng bởi Customer | Customer không có nút hủy đơn — chỉ Vendor mới hủy được | Cao |
| L3.8 | Hệ thống điểm thưởng / Loyalty | Không có bảng `points` hay reward system | Thấp |
| L3.9 | Báo cáo sản phẩm vi phạm | Không có tính năng report | Thấp |
| L3.10 | Xếp hạng shop (Shop Rating) | Không có tổng hợp rating theo shop | Thấp |
| L3.11 | Inventory management riêng (Vendor) | Tồn kho cập nhật qua form sản phẩm — không có màn hình Inventory độc lập | Trung bình |
| L3.12 | Quản lý thông tin shop (Vendor) | Không có trang edit `shopName`, `shopDescription`, `logo` | Trung bình |

---

## L4. UX (Trải nghiệm người dùng)

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L4.1 | Không có loading skeleton | Chỉ spinner hoặc `null` khi load — không có Skeleton UI | Thấp |
| L4.2 | Không có infinite scroll | Danh sách dùng pagination button | Thấp |
| L4.3 | Không có real-time stock update | Tồn kho hiển thị tại thời điểm load, không cập nhật realtime | Thấp |
| L4.4 | Không có preview ảnh zoom | `ProductDetailPage` không có lightbox hay zoom | Thấp |
| L4.5 | Không có breadcrumb navigation | Không có component Breadcrumb | Thấp |
| L4.6 | Không có "Sản phẩm liên quan" | `ProductDetailPage` không gợi ý sản phẩm tương tự | Thấp |
| L4.7 | Error message không nhất quán | Một số nơi dùng `alert()`, nơi khác dùng state message | Thấp |

---

## L5. SECURITY (Bảo mật)

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L5.1 | JWT không có cơ chế thu hồi (Token Revocation) | Không có `TokenBlacklist` table, không có Redis session store | Cao |
| L5.2 | Không có Rate Limiting | Không có dependency `bucket4j` hay rate limit middleware trong `pom.xml` | Cao |
| L5.3 | JWT expiration cố định 24 giờ | `expiration: 86400000` (ms) — không có refresh token mechanism | Trung bình |
| L5.4 | CORS chỉ cấu hình cho localhost | `app.frontend.url: http://localhost:5173` | Trung bình |
| L5.5 | SQL logging trong production config | `show-sql: true` ghi toàn bộ câu SQL ra console | Thấp |
| L5.6 | Không có HTTPS trong config mặc định | `application.yml` không cấu hình SSL/TLS | Trung bình |

---

## L6. TESTING (Kiểm thử)

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L6.1 | Không có unit test cho Service layer | Tất cả test file là `@SpringBootTest` (Integration Test) | Cao |
| L6.2 | Test phụ thuộc database thực | `FullSystemDataSeederTest.java`, `DataSeeder` — không có H2 in-memory DB | Trung bình |
| L6.3 | Không có frontend test | Không có Jest, Vitest, Playwright trong `package.json` | Cao |
| L6.4 | Không đo lường test coverage | Không có Jacoco plugin trong `pom.xml` | Trung bình |
| L6.5 | Test file tên `...IntegrationTest` — chưa phân loại rõ | Một số test là smoke test, không phải integration test thực sự | Thấp |

---

## L7. PAYMENT (Thanh toán)

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L7.1 | VNPay chỉ sandbox — không production | `pay-url: sandbox.vnpayment.vn` trong `application.yml` | Cao |
| L7.2 | IPN callback không tự động ở localhost | Return URL `localhost:5173` — VNPay không gọi được → workaround manual IPN sync | Cao |
| L7.3 | Không có hoàn tiền (Refund) | Không có RefundController, không có VNPay refund API call | Cao |
| L7.4 | Không lưu thông tin thẻ (Card on file) | Không có payment method management | Thấp |
| L7.5 | Chỉ hỗ trợ 2 phương thức TT | VNPAY và COD — không có Momo, ZaloPay, banking | Trung bình |

---

## L8. REALTIME & CHAT

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L8.1 | Chat UI production chưa hoàn thiện | `FloatingChatWidget` không có expanded chat panel | Cao |
| L8.2 | Không có typing indicator | Không có STOMP frame loại `TYPING` trong source code | Thấp |
| L8.3 | Không có push notification (Web Push / FCM) | Chỉ in-app notification polling | Trung bình |
| L8.4 | WebSocket in-memory broker — không scale horizontal | Spring in-memory STOMP broker — không phải Redis Pub/Sub hay RabbitMQ | Cao |
| L8.5 | Không có read receipts cho tin nhắn | `ChatMessage` không có `readAt` field hay read receipt event | Thấp |

---

## L9. DEPLOYMENT & SCALABILITY

| ID | Hạn chế | Bằng chứng | Mức độ |
|---|---|---|---|
| L9.1 | Không có Docker / Container hóa | Không tìm thấy `Dockerfile` hay `docker-compose.yml` trong project | Cao |
| L9.2 | Không có CI/CD pipeline | Không có `.github/workflows/`, `Jenkinsfile`, hay `.gitlab-ci.yml` | Cao |
| L9.3 | Không có load balancer | Single-instance localhost deployment | Cao |
| L9.4 | WebSocket không scale horizontal | In-memory broker không hoạt động multi-instance | Cao |
| L9.5 | Không có môi trường staging | Chỉ có config cho localhost | Cao |
| L9.6 | Không có production database migration | `ddl-auto: update` — nguy hiểm nếu dùng trên production | Cao |
| L9.7 | Không có Redis cache | Không có caching layer — mọi request đều hit database | Trung bình |
| L9.8 | Không có CDN cho frontend assets | Frontend phục vụ từ Vite dev server trực tiếp | Thấp |

---

## Tổng hợp theo mức độ ưu tiên

### Hạn chế nghiêm trọng cần giải quyết trước (Cao):

| ID | Mô tả |
|---|---|
| L1.2 | Chat widget production chưa hoàn thiện |
| L1.3 | Chat UI là developer page |
| L1.4 | Admin Dashboard placeholder |
| L2.1 | Không có API cập nhật user profile |
| L2.3 | Không có thống kê doanh thu |
| L3.1 | Quên mật khẩu chưa có |
| L3.7 | Customer không tự hủy đơn được |
| L5.1 | JWT không có token revocation |
| L5.2 | Không có rate limiting |
| L6.1 | Thiếu unit test cho service |
| L6.3 | Không có frontend test |
| L7.1 | VNPay chỉ sandbox |
| L7.2 | IPN workaround chỉ hoạt động ở localhost |
| L7.3 | Không có refund |
| L8.1 | Chat UI production chưa xong |
| L8.4 | WebSocket không scale |
| L9.1–L9.6 | Toàn bộ deployment infrastructure |

### Tổng số hạn chế: **52 điểm**

| Mức độ | Số lượng |
|---|---|
| Cao | 19 |
| Trung bình | 20 |
| Thấp | 13 |
