# P3-01 / F11 — Delivery confirmation: pre-implementation design

Ngày inspect: **2026-09-10**. Trạng thái: **đề xuất chờ duyệt D8; chưa sửa code, schema hoặc chạy kiểm chứng P3-01**.

Nguồn đối chiếu: [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md), [Sprint 1 Plan](PHASE2_SPRINT1_PLAN.md), [Architecture](ARCHITECTURE.md), [Project Context](PROJECT_CONTEXT.md), các báo cáo Sprint 1/P1/P2 và source hiện tại. Khi tài liệu và source khác nhau, source là nguồn quyết định.

## 1. Current implementation

`OrderStatus` hiện có `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`; chưa có trạng thái giao vận, người giao, assignment, tracking, proof hoặc `deliveredAt`.

Luồng đang chạy là Vendor tự xác nhận giao thành công:

```text
VendorOrderListPage
  → PATCH /api/v1/vendor/orders/{id}/status { status: DELIVERED }
  → OrderServiceImpl.updateVendorOrderStatus @Transactional
  → OrderRepository.findByIdForUpdate (PESSIMISTIC_WRITE)
  → ShopInternalService.isShopOwner
  → SHIPPED → DELIVERED
  → OrderDeliveredEvent after commit
  → buyer receives ORDER_DELIVERED notification
```

Vendor matrix trong source cho phép `CONFIRMED → SHIPPED|CANCELLED` và `SHIPPED → DELIVERED`. Lệnh dùng khóa Order, do đó hai mutation cùng Order được tuần tự hóa; request đến sau phải đọc lại state đã commit. Tuy nhiên Vendor request không có `expectedStatus`, reason không tham gia policy giao hàng, và UI gọi DELIVERED bằng nút **Đã giao thành công**.

Admin có command độc lập đã được duyệt ở P1-01/D2: `PATCH /api/v1/admin/orders/{id}/status`. Nó cho phép `SHIPPED → DELIVERED` khi request có `expectedStatus`, reason 1–500 ký tự và payment policy đạt; có PESSIMISTIC_WRITE, history Admin cùng transaction và event sau commit. Đây là một ngoại lệ quan trọng phải được D8 giữ lại hoặc ghi rõ là bị thay thế; không thể âm thầm bỏ qua D2.

Buyer chỉ có GET danh sách/chi tiết Order và checkout. `OrderDetailPage` hiển thị trạng thái và cho review khi Order đã `DELIVERED`; chưa có nút xác nhận nhận hàng. `ReviewServiceImpl` chỉ kiểm tra `order.userId` và `status == DELIVERED`, vì vậy bất kỳ actor nào đưa Order vào DELIVERED đều mở review eligibility.

`OrderNotificationListener` tạo thông báo `ORDER_SHIPPED` và `ORDER_DELIVERED` cho buyer sau domain commit. P1-03 push notification chỉ push Notification đã persist và commit; do đó command mới phải tiếp tục phát đúng một `OrderDeliveredEvent` sau commit, không tự gửi trực tiếp từ controller.

Dashboard Admin và Shop Analytics P2-01 đều dùng trạng thái DELIVERED cho các metric hoàn tất, nhưng cohort hiện là `Order.createdAt`. Chúng không có `deliveredAt`; do đó không được diễn giải số liệu hiện tại là doanh thu theo ngày giao hàng.

Identity chỉ khởi tạo `ROLE_USER`, `ROLE_VENDOR`, `ROLE_ADMIN`; không có shipper/carrier entity hay assignment. Shop BANNED vẫn được xử lý đơn cũ theo D4 Sprint 1. Payment guard P1-01 chỉ nằm ở Admin command; P3-01 không nên tự mở thêm payment/refund policy khi chưa có D9.

## 2. Requirements and reusable components

Feature 11 yêu cầu chốt **D8**: ai xác nhận giao, có assignment/proof/thời hạn không. Plan cấm tự thêm SHIPPER hoặc auto-confirm sau một số ngày. Candidate `PATCH /api/v1/orders/{id}/confirm-delivery` chỉ phù hợp khi buyer là actor. `updatedAt` không đủ để backfill ngày giao hàng lịch sử.

Có thể tái sử dụng:

- `OrderRepository.findByIdForUpdate`, transaction boundary và `OrderServiceImpl.applyOrderTransition` để mọi đường DELIVERED dùng chung state/event invariant.
- Ownership buyer hiện có ở `OrderService.getOrderDetail(userId, orderId)` và `Order.userId`; không nhận `userId` từ body/query.
- `@CurrentUserId`, `ApiResponse`, error envelope S1-02, `OrderResponse`, Order event/notification chain và UI order detail.
- P1-01 Admin locking, optimistic `expectedStatus`, audit history và payment eligibility policy cho admin-only path.

Không tái sử dụng Vendor endpoint cho buyer bằng role giả hoặc shop giả. Không thêm dependency Order → Payment lock, Product, Cart, Voucher, Shop catalog hoặc một module shipper mới cho baseline buyer confirmation.

## 3. Business rules requiring D8

Các rule dưới đây là đề xuất an toàn, chưa có hiệu lực cho đến khi duyệt.

| Chủ đề | Hiện trạng | Đề xuất D8 | Lý do/tác động |
|---|---|---|---|
| Actor chính | Vendor tự DELIVERED | Buyer là actor xác nhận đã nhận hàng | Phân biệt vendor giao cho đơn vị vận chuyển (`SHIPPED`) và buyer nhận thực tế (`DELIVERED`). |
| Vendor quyền sau D8 | Có `SHIPPED → DELIVERED` | Vendor chỉ `CONFIRMED → SHIPPED` và cancellation theo D1; bỏ nhánh DELIVERED khỏi vendor command/UI | Ngăn shop tự mở review/metric completed. |
| Admin D2 | Có quyền `SHIPPED → DELIVERED` với reason/history/payment guard | Giữ như ngoại lệ vận hành đã duyệt D2, hoặc D8 phải nói rõ thay thế phần này | Đây là conflict thực tế giữa buyer-only và D2; cần quyết định rõ trước code. |
| State transition | SHIPPED → DELIVERED | Giữ đúng một transition; không thêm status mới | Không có contract cho carrier states/returns. |
| Proof/tracking | Không có | Không thêm ở baseline | Chưa có media ownership, retention, carrier integration hoặc policy tranh chấp. |
| Auto-confirm/deadline | Không có | Không thêm | Plan cấm default auto-complete/time limit. |
| Payment/refund | No refund; admin có guard riêng | Buyer confirmation chỉ kiểm SHIPPED và ownership; không tạo payment/refund action | `SHIPPED` là state gate; D9 mới định nghĩa hoàn tiền. |
| Banned shop | Đơn cũ vẫn vận hành | Buyer vẫn xác nhận đơn SHIPPED của shop BANNED | Phù hợp D4, không ảnh hưởng catalog/checkout mới. |

Nếu chọn actor SHIPPER hoặc carrier callback thay buyer, phạm vi thay đổi căn bản: thêm role/assignment hoặc signature/provider contract, proof/tracking lifecycle, UI khác và test authorization khác. Không nên gộp vào P3-01 baseline.

## 4. Delivery record and historical data

Source hiện không lưu ai, khi nào hoặc bằng cách nào xác nhận giao. D8 cần chốt mức audit cần thiết:

1. **Không thêm dữ liệu mới:** nhanh nhất nhưng không chứng minh được buyer hay admin là actor, và không có delivery timestamp.
2. **Khuyến nghị tối thiểu:** migration additive nullable trên `orders` gồm `delivered_at`, `delivery_confirmed_by_user_id` và `delivery_confirmation_source` (`BUYER`/`ADMIN`). Chỉ set một lần trong transition DELIVERED, không expose ID actor công khai. Đây là bằng chứng đủ cho action mới mà không tạo module shipper/proof.

Nếu lựa chọn 2 được duyệt, `deliveredAt` phải là thời điểm server ghi thành công, không suy diễn thời điểm nhận thực tế; dữ liệu DELIVERED lịch sử giữ `NULL`/unknown, không backfill từ `updatedAt`. P2-01 analytics vẫn giữ `ORDER_CREATED_AT` cho đến khi có metric date-basis mới được duyệt.

Admin history P1-01 không thay cho delivery record vì chỉ ghi action Admin, không bao phủ buyer/vendor hay Order lịch sử.

## 5. Proposed API and security design

Nếu chọn Buyer baseline, API đề xuất:

```http
PATCH /api/v1/orders/{orderId}/confirm-delivery
Authorization: Bearer <buyer token>
```

Không nhận `userId`, `shopId`, status, actor, timestamp, proof hay payment flag từ client. Body có thể rỗng; nếu sau này cần proof/dispute thì phải có contract mới.

Server flow đề xuất:

```text
Customer controller + @CurrentUserId
  → DeliveryConfirmationService.confirmBuyerReceipt @Transactional
  → OrderRepository.findByIdForUpdate
  → order.userId == principal userId
  → status phải là SHIPPED
  → set immutable delivery data nếu D8 chọn
  → OrderServiceImpl.applyOrderTransition(order, DELIVERED)
  → commit Order + delivery data
  → OrderDeliveredEvent / notification only after commit
```

Mọi lệnh Vendor, Admin và Buyer cùng khóa một hàng Order. Không lấy Payment lock sau Order: Admin giữ read-only payment eligibility theo D2; buyer command không gọi Payment. Request buyer trùng hoặc đối đầu Admin/Vendor chỉ một transition commit; request sau không được phát event hay mở review lần hai.

Error contract đề xuất: missing Order `ORDER_NOT_FOUND` 404; Order của buyer khác trả cùng 404 để không lộ existence; non-SHIPPED/replay trả `INVALID_ORDER_STATE_TRANSITION` 400 theo Vendor state contract hiện có; missing authentication 401 và sai role/security 403. Nếu muốn phân biệt stale UI thành 409, cần chốt thêm `expectedStatus` request giống Admin D2; không nên tự thay đổi semantics.

## 6. Backend implementation plan after approval

1. Ghi D8 actor matrix, Admin exception/supersession, audit data option và error semantic vào decision document.
2. Nếu chọn audit fields, thêm migration additive, entity/DTO mapping và test migration; không backfill historical DELIVERED.
3. Tách buyer command service trong Order module. Reuse `findByIdForUpdate` và shared transition; remove only Vendor `SHIPPED → DELIVERED` if D8 says buyer-only.
4. Add customer endpoint; controller and service both enforce principal ownership. Do not make a client `shopId`/`userId` authoritative.
5. Ensure exactly one event/notification after commit; keep Review service eligibility based on final status without granting review early.
6. Keep Admin P1-01 command unchanged unless D8 explicitly changes its transition matrix. If it stays, wire delivery metadata through its shared transition in a defined actor/source-safe way.

Expected modules: `order` controller/service/entity/repository/DTO/events/tests; migration only if D8 selects audit fields; customer/vendor/admin frontend and API types. Notification and Review should require regression tests, but need no new architecture. Identity only changes if a later D8 chooses SHIPPER, which is not recommended for this baseline.

## 7. Frontend implementation plan after approval

- Buyer `OrderDetailPage`: display progress; when `SHIPPED`, show a clear confirmation action with an irreversible-action confirmation dialog. Disable while pending; reload canonical order after success/conflict.
- Buyer order history may show the same entry point or link to detail, but one owner of submit state prevents duplicate clicks.
- Vendor list/detail: retain **Giao cho ĐVVC** for `CONFIRMED → SHIPPED`; remove **Đã giao thành công** if buyer-only is approved. Preserve management of old orders from BANNED shops.
- Admin detail: keep D2 actions/history as already approved, or update it only after D8 explicitly changes Admin policy.
- Use S1-02 `errorCode`, not message text; show retryable network errors without changing local order status. If audit fields exist, show delivered time/source only to the allowed audience under the D8 visibility rule.
- No proof upload, carrier dashboard, tracking page or auto-completion timer.

## 8. Test plan

Integration tests on MySQL and direct HTTP must cover:

- Buyer owning a SHIPPED order confirms successfully; response, DB status, chosen delivery fields and exactly one delivery notification/event are correct after commit.
- Buyer A cannot confirm Buyer B’s order, including a crafted order ID; no state/event/record leakage.
- Unauthenticated request and non-buyer/Vendor/Admin requests against buyer endpoint follow the approved security contract.
- PENDING, CONFIRMED, CANCELLED, DELIVERED and duplicate confirmation are rejected; no duplicate notification, review eligibility or audit row/field rewrite.
- Two buyer confirmations concurrently, buyer confirmation racing Vendor shipping, and buyer confirmation racing Admin delivery use separate transactions and prove only one wins under the Order lock.
- Vendor cannot call its status endpoint to DELIVERED after buyer-only policy; Vendor can still ship an owned CONFIRMED order. Existing cancellation/stock guarantees remain intact.
- Admin delivery behavior follows the explicit D8/D2 choice, including reason/history/payment condition and metadata source if retained.
- Shop BANNED historical order behavior follows D4; no catalog/checkout rule is accidentally applied to delivery confirmation.
- Review is unavailable before final DELIVERED and available afterward only to the order buyer. Analytics sees status DELIVERED but remains created-at cohort; no claim of delivery-date metrics.
- Transaction rollback after delivery metadata write or event registration leaves order/status/metadata unchanged and sends no notification.

Frontend tests/smoke should cover SHIPPED visibility, submit/disable/reload, stale or duplicate error handling, ownership route data, Vendor no-longer-deliver action, Admin policy as chosen, and mobile/desktop layout. Full regression must include Sprint 1 cancellation/IPN/payment guard, F3 Admin order mutation, F1 notification push, Review and P2-01 analytics.

## 9. Risks and limitations

- A buyer confirmation is an assertion by the buyer, not carrier proof, settlement, COD collection, refund acceptance, return handling or dispute resolution.
- Removing Vendor DELIVERED changes an existing workflow; FE and BE must deploy together so an old vendor client cannot retain the transition.
- Keeping Admin DELIVERED as D2 exception means not every DELIVERED was buyer-confirmed; persisted source is needed if that distinction matters.
- No backfill can accurately give historical DELIVERED orders a confirmation timestamp/actor.
- Current `OrderResponse` omits `updatedAt`; it must not be relabeled as delivery time.
- Existing vendor ship flow does not itself add a carrier assignment/tracking contract. P3-01 must not imply real-world logistics integration.
- Rollback is high-risk after data mutation: stop delivery commands, deploy a compatible version, preserve committed delivery records/statuses, and do not bulk reverse Orders or re-emit notifications.

## 10. DECISION SET D8 requiring approval

1. **Actor:** Approve buyer-only `PATCH /api/v1/orders/{id}/confirm-delivery` for normal confirmation; no SHIPPER/carrier callback, proof, tracking or auto-confirmation in P3-01.
2. **Vendor matrix:** Remove Vendor `SHIPPED → DELIVERED`; Vendor retains `CONFIRMED → SHIPPED` and existing cancellation policy.
3. **Admin conflict:** Choose one explicitly:
   - **A (recommended):** retain P1-01/D2 Admin `SHIPPED → DELIVERED` as exceptional, audited operation with its existing reason/history/payment guard; source is `ADMIN`.
   - **B:** supersede that D2 Admin transition and make Admin unable to mark delivered; its UI/action must be removed.
4. **Delivery evidence:** Choose no new fields, or approve the recommended nullable additive `delivered_at`, `delivery_confirmed_by_user_id`, `delivery_confirmation_source`. Historical DELIVERED stays unknown in either case.
5. **Conflict response:** approve existing `INVALID_ORDER_STATE_TRANSITION` 400 for a non-SHIPPED/replayed buyer confirmation, or require `expectedStatus` and `ORDER_STATE_CONFLICT` 409 for stale UI.
6. **BANNED-shop orders:** preserve D4: buyer can confirm a SHIPPED historical order, and shop ban does not block this internal lifecycle action.
7. **Financial meaning:** confirmation changes only Order status/review/notification/current fulfilled aggregates. It does not confirm payment collection, refund, settlement, return acceptance or delivery proof.

After D8 is approved, implementation should proceed in this order: migration decision → shared Order command/lock and direct HTTP tests → frontend buyer/vendor/admin updates → notification/review/analytics regression → full verification and dedicated P3-01 report. No code has been changed by this inspect.
