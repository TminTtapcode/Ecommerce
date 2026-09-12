# Phase 2 — Kế hoạch Sprint 1 (P0)

Ngày cập nhật: **2026-09-09**. Trạng thái: **D1/D3/D4 và Error đã được phê duyệt; S1-01 đã ghi nhận baseline: 41/55 test pass, 14 test fail/error**.

Kết quả trước implementation: [S1-01 Baseline](PHASE2_S1_01_BASELINE.md). **S1-02 đến S1-08 đã hoàn thành**: [Error](PHASE2_S1_02_ERROR_CONTRACT.md), [Inventory](PHASE2_S1_03_INVENTORY_CANCELLATION.md), [Payment Guard](PHASE2_S1_04_PAYMENT_GROUP_GUARD.md), [Ban/Unban và enforcement](PHASE2_S1_05_06_SHOP_BAN_ENFORCEMENT.md), [S1-07/08 và final audit](PHASE2_S1_07_08_VERIFICATION.md). Full configured suite **155/155 backend + 6/6 frontend pass**, gồm E2E/NPlus1; build BE/FE và Edge browser smoke pass, lint 19 warnings. Giới hạn, migration và rollback ghi trong báo cáo cuối.

Contract có hiệu lực: [Decision Set đã chốt](PHASE2_SPRINT1_DECISIONS.md). Các mục “chờ chốt”/lựa chọn bên dưới lưu lại bối cảnh lập kế hoạch và được thay thế bởi contract này. Trạng thái thực thi mới nhất nằm trong báo cáo từng bước bên trên.

Tài liệu được đồng bộ sau yêu cầu chỉ audit/plan và không tự thêm business rule. Tham chiếu chính: [Feature Audit](PHASE2_FEATURE_AUDIT.md), [Dependency Graph](PHASE2_DEPENDENCY_GRAPH.md), [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md).

## 1. Điều chỉnh so với bản kế hoạch trước

- Vendor thực tế chỉ được hủy **CONFIRMED → CANCELLED**. Bỏ giả định cho hủy PENDING.
- Hoàn kho đồng bộ trong transaction là phương án kỹ thuật đề xuất, chưa được coi là thiết kế đã duyệt.
- Không tự giới hạn ban chỉ ACTIVE; backend hiện cho cả PENDING. Ban/unban PENDING và cấp role cần chốt.
- Không tự bắt buộc reason hoặc thêm ban_reason. Validation/persistence phụ thuộc quyết định policy.
- Hiệu lực ban đối với catalog, checkout, tạo/sửa product và xử lý đơn cũ phải được xác định trước implementation nhánh đó.
- Audit chỉ dựa source, chưa xác minh DB runtime hoặc chạy lại tests.

## 2. Mục tiêu

Hoàn thiện F4 inventory cancellation, F9 error codes, F5 ban/unban và F8 core tests. Không triển khai Admin order mutation/dashboard/profile/notification push/search/refund trong Sprint 1.

Đã có restoreStock/listener, ErrorCode/AppException, endpoint/action ban/unban và 21 unit tests; công việc là hoàn thiện tính đúng đắn và thiếu sót, không tạo lại.

## 3. Quyết định trước nhánh công việc liên quan

| ID | Nội dung | Đề xuất để xem xét |
|---|---|---|
| D1 | Transaction hoàn kho | A: Order gọi Product contract cùng transaction; B: after-commit + outbox/dedup/retry. A ít hạ tầng hơn trong monolith. |
| D3 | Ban PENDING/unban | Chốt có cho phép, phục hồi status nào, tương tác approve/ROLE_VENDOR. Không tự chọn policy thay người dùng. |
| D4 | Hiệu lực ban/audit | Chốt hạn chế mua/bán, vận hành đơn cũ, reason có bắt buộc không và lưu log/DB. |

Các mục này được định nghĩa tại [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md). Có thể chuẩn bị baseline/error contract trước; không triển khai nhánh phụ thuộc khi chưa chốt.

## 4. Backlog

| ID | Công việc | File/module trọng tâm | Điều kiện hoàn thành |
|---|---|---|---|
| S1-01 | Baseline và test environment | pom.xml, src/test, frontend/package.json | Có execution report mới khi triển khai; DB integration riêng |
| S1-02 | Error contract và consumers | common/exception, common/security, api/types, RequireShop | Mã/status/message thống nhất, FE xử lý không phụ thuộc chuỗi |
| S1-03 | Cancellation và hoàn kho theo D1 | OrderServiceImpl/OrderRepository, ProductInternalService/implementation/repositories/listener | Hủy không mất/hoàn trùng stock; lỗi rollback hoặc có durable recovery đúng semantics đã chọn |
| S1-04 | Guard payment group status | OrderInternalService/implementation, PaymentGatewayService tests | IPN trễ/lặp không hồi sinh CANCELLED hoặc ghi lùi trạng thái cuối |
| S1-05 | Ban/unban theo D3/D4 | Shop service/controller/repository/contract, DTO/entity nếu cần | Transition/role/reason/audit đúng policy |
| S1-06 | Enforce và UI theo D4 | Product/Order/Cart liên quan, AdminShopListPage/RequireShop/adminApi | API trực tiếp và UI cùng policy; paging/catalog đúng; đơn cũ đúng quyền |
| S1-07 | Bổ sung core tests | Order/Product/Shop/Voucher/Payment/Common tests | Behavioral coverage cho rủi ro P0, không chỉ mirror implementation |
| S1-08 | Verification và báo cáo | Backend/frontend/docs | Build/lint/smoke và kết quả test, migration/rollback ghi rõ |

Thứ tự S1-01 → S1-02 → S1-03 → S1-04 → S1-05 → S1-06 → S1-07 → S1-08; test viết cùng từng thay đổi. Ước lượng tổng **7–10 ngày công** sau chốt policy và sẵn sàng DB test.

## 5. Ma trận test

| Nhóm | Kịch bản bắt buộc |
|---|---|
| Kho | Checkout giảm variant/product; hủy CONFIRMED phục hồi đúng; nhiều variant chung product; missing variant/invalid quantity/row count |
| Transaction | Item thứ hai lỗi; hủy đồng thời; hủy đối đầu SHIPPED/IPN; retry/event trùng theo D1 |
| Trạng thái/quyền | PENDING/SHIPPED/DELIVERED/CANCELLED không được Vendor hủy theo matrix hiện tại; shop khác không sửa |
| Checkout | N shop → N order; thiếu hàng; giỏ cũ sau ban theo D4; voucher scope/min/max/expiry/usage và phân bổ rounding |
| Shop | Transition đã chốt, not found/non-admin, concurrent approve/ban/unban; role không bypass; reason theo D4 |
| FE/enforcement | Catalog/detail/paging, API product trực tiếp, trạng thái cũ trên FE, retry/double click, đơn cũ theo D4 |
| Error | Domain code, validation/binding, 401/403/404/500, RequireShop tương thích, callback VNPay giữ format riêng |
| Payment | Signature, amount sai/thiếu, duplicate callback/attempts, late IPN; interpretation gateway fields sau xác minh contract |

Unit tests không chứng minh DB lock/transaction. Dùng integration với kết nối/transaction độc lập cho concurrency. Không tự sửa voucher rounding thành rule chưa chốt; ghi nhận test hiện chấp nhận tổng 9 cho discount 10 và xác định expected behavior trước.

## 6. Migration và rollback

- D1-A dùng pessimistic lock có thể không cần schema; nếu đổi optimistic cần version. D1-B cần durable operation/outbox/dedup và quy trình retry.
- Không để listener cũ và đường restore mới cùng cộng stock.
- Ban reason/audit/prior status chỉ migration sau D3/D4; không tự đoán trạng thái trước ban của dữ liệu cũ.
- Không tự hoàn lại kho hoặc tiền cho toàn bộ CANCELLED lịch sử.
- Tắt listener rồi tiếp tục nhận hủy không phải rollback an toàn. Dừng nhánh lỗi và đối soát dữ liệu; giữ ledger nếu đã dùng.
- errorCode là field response, không phải cột DB.

## 7. Definition of Done

- [x] D1/D3/D4 được chốt và ghi lại, không còn giả định business rule.
- [x] Hoàn kho có bảo đảm nguyên tử theo D1; không double restore trong các case đã kiểm chứng.
- [x] IPN/cancellation race không hồi sinh đơn hủy.
- [x] Error contract core và FE consumers nhất quán.
- [x] Ban/unban và enforcement theo policy, authorization kiểm thử bằng API.
- [x] Core unit/integration có execution evidence mới; frontend build/lint/smoke được ghi nhận.
- [x] Migration/rollback có giới hạn rõ, cập nhật audit bằng bằng chứng thực thi.

Các mục thiết kế/lựa chọn phía trên giữ bối cảnh trước implementation; Decision Set và báo cáo S1-07/08 là trạng thái hiện tại. Workspace chưa được Git nhận diện; đường docs/MODULE_DEVELOPMENT_WORKFLOW.md chưa có. Không có commit hoặc deploy ứng dụng trong kết quả kiểm chứng này.
