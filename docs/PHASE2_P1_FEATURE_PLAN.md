# P1 — Kế hoạch triển khai từng feature

Ngày lập: **2026-09-09**. Trạng thái: **P1-01/F3, P1-02/F6, P1-03/F1 và P1-04/F7 đã triển khai, kiểm chứng đầy đủ; P1-05 chưa triển khai**. [Báo cáo mới nhất F7](PHASE2_P1_04_ADVANCED_SEARCH.md) và [Báo cáo F1](PHASE2_P1_03_NOTIFICATION_PUSH.md). Nguồn: [Implementation Plan — P1](PHASE2_IMPLEMENTATION_PLAN.md#4-p1--vận-hành-và-trải-nghiệm), [Dependency Graph](PHASE2_DEPENDENCY_GRAPH.md), [Feature Audit](PHASE2_FEATURE_AUDIT.md), [Sprint 1 verification](PHASE2_S1_07_08_VERIFICATION.md).

P1 là nhóm ưu tiên, không mặc định cả nhóm phải làm trong một sprint. Chia thành **5 lượt, mỗi lượt hoàn thành một feature** từ inspect → chốt contract → implement → test → review → báo cáo. Không tự chuyển sang feature kế tiếp khi người dùng chỉ yêu cầu một feature.

## 1. Thứ tự và điểm bắt đầu

| Lượt | Feature | Đầu ra chính | Quyết định trước code |
|---|---|---|---|
| P1-01 | **F3 — Admin order status mutation** | Admin can thiệp đơn theo matrix được duyệt, dùng chung invariant Order/stock/payment | **D2:** transitions, payment conditions, reason/audit |
| P1-02 | **F6 — Profile** | Đọc/sửa hồ sơ chính mình, cập nhật tên hiển thị | **D6:** trường editable và validation |
| P1-03 | **F1 — WebSocket notification push** | Notification đã lưu được push đúng recipient, có HTTP reconcile/fallback | Contract STOMP, token lifecycle, destination authorization |
| P1-04 | **F7 — Advanced search** | Bộ lọc/sort/URL/paging đúng và giữ eligibility shop/product | **D7:** giá, category descendants, sort |
| P1-05 | **F2 — Admin dashboard statistics** | KPI/chart/recent orders từ aggregate có định nghĩa rõ | **D5:** metric, trạng thái, date cohort/timezone |

Thứ tự này theo chuỗi đề xuất trong Implementation Plan. F6/F1/F7 không phụ thuộc F3; F2 cũng không cần F3 để làm counts. Nếu D2 chưa được duyệt, ghi nhận blocker của F3 và có thể chọn feature độc lập đã đủ contract; không lấy việc chuyển lượt làm phê duyệt D2.

F8 test và F9 error contract tiếp tục trong **mọi lượt**, không mở lại thành feature nền tảng riêng. Kết quả 155/155 backend, 6/6 frontend của Sprint 1 là baseline lịch sử, không phải kết quả test P1 mới.

## 2. P1-01 / F3 — Admin đổi trạng thái đơn

**D2 đã duyệt và F3 đã hoàn thành:** [Decision Set](PHASE2_P1_01_D2_DECISIONS.md), [Implementation/verification](PHASE2_P1_01_ADMIN_ORDER.md). Phần kế hoạch phía dưới giữ bối cảnh trước implementation; lựa chọn A có hiệu lực.

**Điểm xuất phát:** AdminOrderController hiện chỉ GET list/detail; AdminOrderDetailPage chỉ xem. Sprint 1 đã có pessimistic Order lock, hoàn kho đồng bộ và IPN chỉ confirm PENDING. Không sử dụng mô tả bulk-update không guard trong audit cũ làm hiện trạng mới.

**Chốt D2 trước implementation:**

- Bảng `source status → target status → điều kiện payment → reason → side effects` cho từng lệnh Admin; đặc biệt PENDING, đơn VNPAY đã SUCCESS và các trạng thái cuối.
- Admin có dùng đúng matrix Vendor hay được mở rộng nhánh nào? Chưa có quyền mở rộng mặc định.
- Reason bắt buộc cho lệnh nào, giới hạn bao nhiêu? Actor/reason lưu bền vững hay log sau commit? Không lấy policy reason Ban áp sang Order.
- Nếu điều kiện dựa vào Payment: xác định đánh giá theo Order/payment group/attempt nào, trường hợp nhiều SUCCESS và nguồn dữ liệu COD. Không coi CANCELLED là đã refund.

**Khuyến nghị để duyệt:** bắt đầu với tập transition nhỏ được nêu rõ trong D2; tách quyền Admin khỏi ownership Vendor nhưng dùng chung state/inventory invariant. Nếu chưa có refund policy, không tự mở lệnh can thiệp tiền hoặc ép xác nhận thanh toán.

**Các bước trong lượt:**

1. Trace Admin/Vendor Order → Order lock → Product restore → Payment group; đối chiếu matrix và test hiện có. Ghi mọi xung đột trước sửa.
2. Thiết kế DTO/command và đề xuất `PATCH /api/v1/admin/orders/{orderId}/status`; status/reason theo D2, actor từ principal. Thiết kế cách FE nhận transitions được backend cho phép.
3. Implement trong Order, chỉ gọi Product/Payment qua internal contract. Không gọi Vendor command bằng shop giả hoặc bỏ mọi kiểm tra vì ROLE_ADMIN.
4. Dùng cùng khóa Order cho Admin/Vendor/IPN. Nếu cần Payment lock, thiết kế thống nhất thứ tự khóa với IPN hiện **Payment → Order** trước code; tránh thêm đường **Order → Payment** gây deadlock. Kiểm tra lại điều kiện trong transaction.
5. Thêm action/modal Admin detail, disable khi gửi, báo lỗi theo code, refetch khi thành công/conflict. Nếu chọn history bền vững, lưu cùng transaction và có migration additive.
6. Test và review riêng feature trước ghi hoàn tất.

**Test/DoD:** ma trận cho phép/bị cấm; missing Order; thiếu/sai role qua HTTP; reason theo D2; actor không giả mạo; hủy hoàn cả variant/product đúng một lần; restore lỗi rollback cả trạng thái/audit nếu có. MySQL integration cho Admin đối đầu Vendor, IPN và hai Admin, duplicate request; browser smoke Admin. Không làm mất các bảo đảm S1-03/04 hoặc thay Vendor matrix ngoài D2.

**Migration/rollback:** chỉ thêm history nếu được chọn; không backfill actor/reason giả. Khi có sự cố dừng action mới, giữ audit và trạng thái đã commit; không đảo trạng thái hàng loạt để rollback code.

**Đầu ra:** contract D2 được duyệt, code/test và `PHASE2_P1_01_ADMIN_ORDER.md` ghi API, matrix, transaction/lock, evidence và limitations.

## 3. P1-02 / F6 — Profile

**F6 đã hoàn thành theo D6:** [Decision Set](PHASE2_P1_02_D6_DECISIONS.md), [báo cáo BE/FE/test/verification](PHASE2_P1_02_PROFILE.md). Full regression 221/221 backend + 9/9 FE tests, build/Profile và F3 browser smoke pass, lint 18 warnings. Các lựa chọn bên dưới là bối cảnh lập kế hoạch, được thay thế bởi contract đã duyệt.

**Điểm xuất phát:** ProfilePage đọc AuthContext, chưa có form save. User có fullName/phone; profile API là phần cần triển khai theo plan.

**Chốt D6:** cho sửa trường nào; fullName có được rỗng, trim/giới hạn; phone optional hay bắt buộc, format/normalization và có yêu cầu unique không. Đề xuất phạm vi đầu chỉ **fullName + phone**, không tự thêm avatar, đổi email/password hoặc xác minh số điện thoại.

**Các bước trong lượt:**

1. Inspect Identity entity/schema, login/register/JWT/AuthContext; chốt request/response và validation khớp giới hạn DB.
2. Đề xuất GET/PUT `/api/v1/users/me`, DTO whitelist và service Identity lấy userId từ principal. Chốt rõ reject hay ignore trường JSON ngoài contract; chúng không được làm thay đổi email/roles/password/userId.
3. Implement profile form, loading/error/saving/success, userApi và error codes cần thiết. Cập nhật AuthContext qua dữ liệu server; không sửa JWT payload trong localStorage để giả token mới.
4. Hydrate lại tên từ profile API khi reload nếu JWT vẫn chứa tên cũ; xử lý login/logout và đổi tài khoản không giữ hồ sơ của user trước.

**Test/DoD:** GET/update đúng principal; 401; trường quá dài/rỗng/phone theo D6; mass assignment không thay quyền/danh tính; không trả passwordHash; lưu DB và refresh/login vẫn đúng tên. Browser smoke save, validation và logout/login. Regression Auth/security + frontend build/lint.

**Migration/rollback:** fullName/phone không cần cột mới; xác minh schema trước khi kết luận. Giữ API/FE tương thích, không phụ thuộc đổi token format.

**Đầu ra:** D6, implementation/test và `PHASE2_P1_02_PROFILE.md`.

## 4. P1-03 / F1 — Notification realtime
 
**Đã hoàn thành P1-03/F1:** [Contract STOMP C1–C6 đã duyệt](PHASE2_P1_03_STOMP_DECISIONS.md), [Báo cáo hoàn thành](PHASE2_P1_03_NOTIFICATION_PUSH.md). 34/34 integration test cases pass, 1/1 listener test pass, 15/15 frontend tests pass, browser smoke pass. Hỗ trợ push realtime STOMP `/user/queue/notifications`, listener sau commit transaction, HTTP fallback & reconciliation 60s, expiry timer, session invalidation và ID dedup cache.

**Điểm xuất phát theo audit:** Notification đã có DB/REST, frontend polling; STOMP dùng chung với chat. Các listener lưu notification ở nhiều nơi. Phải inspect lại Principal/interceptor và mọi đường save trước code.

**Contract cần xác định:** Principal recipient thống nhất theo userId; destination đề xuất `/user/queue/notifications`; payload có ID để dedup; CONNECT/SUBSCRIBE/SEND được phép; xử lý token hết hạn, logout/đổi user và reconnect. Đây là thiết kế kỹ thuật cần ghi rõ, không tự thêm loại notification mới. Hook hủy hiện chỉ log; việc tạo thông báo hủy cần scope riêng nếu được yêu cầu.

**Các bước trong lượt:**

1. Trace Order/Shop/Review listeners → Notification transaction → DB, cùng chat websocket/client subscription lifecycle.
2. Chặn CONNECT thiếu/sai token, kiểm tra destination authorization và origin theo môi trường. Không cho client tự chọn recipient hoặc SEND giả thông báo; giữ chat đang dùng được.
3. Gom điểm persist cần thiết, phát push **sau commit của transaction lưu Notification**, không chỉ sau commit Order. Rollback Notification không được push.
4. Mở rộng websocket service bằng subscription riêng, tránh Notification disconnect toàn kết nối Chat. Dedup bằng ID, reconnect/resubscribe, clear dữ liệu khi đổi account.
5. Reconcile unread/list bằng HTTP khi kết nối lại và xử lý push đối đầu mark-read. Polling fallback khi mất kết nối; REST vẫn là nguồn đọc bền vững. Giữ PUT `/{id}/read` và `/read-all` hiện có.

**Test/DoD:** hai user thực qua STOMP không nhận thông báo của nhau; thử raw queue subscription và SEND trái phép; missing/invalid/expired token theo thiết kế; logout/reconnect; duplicate và push/HTTP race; MySQL rollback không push. Browser hai phiên user, badge/list/mark-read; chat gửi/nhận vẫn pass. Push bị mất khi process chết phải có khả năng lấy lại từ REST, không hứa exactly-once delivery.

**Migration/rollback:** không mặc định bảng/unique key mới. Nếu cần constraint dedup phải audit dữ liệu trước. Có thể tắt push và giữ persistence/REST/polling; thay Principal cần FE/BE đồng bộ và reconnect session cũ.

**Đầu ra:** STOMP/security contract, code/test và `PHASE2_P1_03_NOTIFICATION_PUSH.md`.

## 5. P1-04 / F7 — Advanced search

**Đã hoàn thành P1-04/F7:** [Quyết định D7 đã duyệt], [Báo cáo hoàn thành](PHASE2_P1_04_ADVANCED_SEARCH.md). 16/16 test cases mới pass, 28/28 regression test pass, TypeScript pass. Hỗ trợ duyệt cây danh mục con BFS (D7.2), lọc theo `product.price` (D7.1), whitelist sort `price_asc`/`price_desc`/`id_desc` (D7.4), kẹp trần size 50 và bảo đảm eligibility ACTIVE/Non-banned shop (D7.5). Frontend có filter sidebar, đồng bộ URL SearchParams, reset page khi đổi filter và stale-response guard.

## 6. P1-05 / F2 — Admin Dashboard

**Điểm xuất phát theo audit:** Admin dashboard là placeholder, dữ liệu Order có createdAt/totals/status/payment group. Không đủ ledger cho thực thu COD/settlement/refund/profit; cần xác minh lại fields khi bắt đầu.

**Chốt D5:** danh sách KPI/chart, metric label/formula, tập trạng thái, cohort date/timezone và biên khoảng ngày; counts nào toàn thời gian, counts nào theo khoảng ngày. Chốt AOV và empty denominator nếu chọn. Không gắn date filter cho User/Shop/Product khi không có timestamp tương ứng.

**Khuyến nghị để duyệt:** counts users/shops/products/orders, order status breakdown, recent orders; order value/AOV chỉ sau định nghĩa tập trạng thái. Gọi đúng **giá trị đơn**, không gọi CONFIRMED/SHIPPED/DELIVERED là tiền thực thu.

**Các bước trong lượt:**

1. Lập bảng metric → formula → nguồn/module → thời gian/trạng thái → empty behavior và duyệt D5.
2. Aggregate tại module sở hữu, composition dùng internal DTO/contracts; không service dashboard inject repository nhiều module. Chọn nơi đặt composition phù hợp architecture, không tạo microservice mới.
3. Đề xuất GET `/api/v1/admin/statistics/dashboard`, validate date range, giới hạn truy vấn hợp lý; server trả dữ liệu tối thiểu cho recent orders.
4. Implement Admin KPI/chart/date controls/loading/error/empty; nhãn/số liệu khớp contract. Không thêm chart ngoài dữ liệu đã chốt.
5. Kiểm chứng aggregation và query; chỉ thêm index sau đo, không tải toàn entity để cộng trong Java.

**Test/DoD:** 401/403; dataset rỗng; cancelled, unpaid VNPAY, COD; nhiều shop và nhiều attempts không nhân count/tổng; BigDecimal, AOV denominator, biên ngày/timezone. Fixture expected totals tính độc lập; browser đổi khoảng ngày và đối chiếu API. Payment SUCCESS ở group có CANCELLED không được tự biến thành metric doanh thu nếu D5 không định nghĩa như vậy.

**Migration/rollback:** counts cơ bản dự kiến không cần schema; không backfill timestamp bằng ngày migrate. Read-only rollback giữ fallback UI; đổi nghĩa metric phải đổi tên/version rõ.

**Đầu ra:** D5 metric dictionary, code/test và `PHASE2_P1_05_ADMIN_DASHBOARD.md`.

## 7. Cách thực hiện và nghiệm thu mỗi lượt

1. **Inspect:** đọc plan/contract/source/test liên quan; báo rõ khác biệt giữa audit cũ và code hiện tại. Kiểm tra đúng DB test trước mọi integration run.
2. **Contract:** ghi quyết định nghiệp vụ đã duyệt và thiết kế API/transaction/security. Chỉ dừng nhánh phụ thuộc quyết định còn thiếu; không tự xem đề xuất trong tài liệu này là đã được duyệt.
3. **Implement:** hoàn thành BE + FE của đúng feature, F8/F9 đi cùng. Không tự gộp refund, delivery, recommendation, analytics shop hoặc xử lý Voucher rounding đang chờ policy.
4. **Verify:** test hành vi và API trực tiếp; MySQL cho transaction/concurrency liên quan; build BE/FE, lint, browser smoke của feature. So sánh baseline, phân biệt failure cũ với regression mới.
5. **Review/checkpoint:** ghi số tests thực chạy, command, môi trường, migration/rollback, limitations; cập nhật Implementation Plan và báo cáo riêng feature. Không đánh dấu pass bằng số test dự kiến hoặc mock thay cho lock DB.

Sau lượt cuối, chạy regression toàn bộ suite được cấu hình + FE checks và smoke các luồng P1/Sprint 1 liên quan; lập báo cáo tổng hợp P1. Đây là kiểm chứng kết thúc nhóm, không phải feature thứ sáu. Không dùng số 155 của Sprint 1 làm tổng mới sau khi đã bổ sung test.

Không chạy build/test/DB hoặc sửa code nghiệp vụ trong lượt lập kế hoạch này. Các tên báo cáo P1-01…05 phía trên là đầu ra dự kiến, chưa phải tài liệu nghiệm thu đã có.

## 8. Prompt sử dụng cho từng lần

**Bắt đầu P1-01:**

> Dựa vào PHASE2_P1_FEATURE_PLAN.md, bắt đầu P1-01 — F3 Admin order status mutation. Inspect Order/Product/Payment và frontend liên quan; đối chiếu với bảo đảm Sprint 1. Chưa sửa code nghiệp vụ. Đề xuất Decision Set D2 cụ thể gồm transition matrix, payment conditions, reason/audit, transaction và lock order. Báo rõ conflict giữa plan/source và các quyết định cần tôi chốt.

**Sau khi đã duyệt contract của một feature:**

> Triển khai P1-0N — [tên feature] theo contract đã chốt và PHASE2_P1_FEATURE_PLAN.md. Hoàn thành backend, frontend, tests, verification và báo cáo của riêng feature này. Kiểm chứng trực tiếp API; dùng DB thật cho transaction/concurrency liên quan. Giữ các bảo đảm Sprint 1, báo rõ regression và limitations. Chưa chuyển sang feature kế tiếp.

Với feature tiếp theo còn thiếu D6/D7/D5 hoặc STOMP contract, bắt đầu bằng inspect/chốt contract tương tự P1-01, thay vì yêu cầu implement toàn bộ P1 một lần.
