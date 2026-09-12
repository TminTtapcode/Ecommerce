# P3-02 / F10 — Payment refund: capability gate and Decision Set D9

Ngày inspect: **2026-09-10**. Trạng thái: **chưa được phép implement refund**. F11 đã hoàn tất theo D8; F10 bị chặn có chủ đích cho đến khi D9 xác nhận capability và policy với merchant/gateway.

Nguồn: [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md), [Architecture](ARCHITECTURE.md), [Project Context](PROJECT_CONTEXT.md), S1-04 payment guard, P1-01 Admin order mutation, P3-01 delivery confirmation và source. Source là hiện trạng quyết định.

## 1. Current implementation

Payment module hiện chỉ có create URL, VNPay IPN và group-status read.

```text
Checkout creates N shop Orders with one paymentGroupId
  → PaymentGatewayService.generatePaymentUrl creates a PENDING Payment attempt
  → VNPay IPN locks that Payment by transactionId
  → verifies signature and amount
  → SUCCESS confirms only PENDING Orders in the group
```

`Payment` lưu group ID, amount, method, status (`PENDING`, `SUCCESS`, `FAILED`, `CANCELED`), merchant `transactionId`, optional gateway transaction ID và `rawData`. Không có refund entity, refund status, refund route, query/reconciliation client, ledger, idempotency key, currency field, refund amount hoặc explicit original-payment reference.

`paymentGroupId` có thể chứa nhiều Order từ nhiều shop; Payment attempt cũng có thể nhiều lần cho cùng group. `PaymentEligibilityQuery` chỉ trả true khi có một VNPAY `SUCCESS`, và không chọn một attempt chính thức để refund. Vì thế không thể suy ra "Payment nào được hoàn" bằng attempt mới nhất hay chỉ Payment group.

Order chỉ có snapshot `originalTotalAmount`, `discountAmount`, `totalAmount` từng shop. Một Order `CANCELLED` không đồng nghĩa đã refund: D1 hoàn stock; S1-04 ngăn IPN hồi sinh Order; không flow nào gọi gateway hoặc ghi nhận tiền hoàn. COD không có collection ledger, nên source không biết COD đã thu tiền hay chưa.

F3 Admin command cấm Admin cancel VNPAY nhưng Vendor vẫn có cancellation policy Sprint 1. D8 chỉ đổi delivery state/evidence và không thêm financial meaning. Không có frontend refund UI hoặc API client.

## 2. Official gateway capability versus project capability

Tài liệu sandbox chính thức của VNPay mô tả `POST` JSON API cho `querydr` và `refund`, nêu request refund dùng ID request merchant duy nhất, transaction type full (`02`) hoặc partial (`03`), chữ ký và amount không vượt giao dịch thanh toán. [VNPay QueryDR & Refund documentation](https://sandbox.vnpayment.vn/apis/docs/truy-van-hoan-tien/querydr%26refund.html) Tuy nhiên tài liệu công khai **không chứng minh** merchant của dự án được bật Refund, được allowlist, có production endpoint/credential phù hợp hoặc xác định chính sách settlement của merchant.

Source chưa thu thập các input cần thiết thành fields đáng tin: original VNPay transaction date/bank code/transaction number và canonical signed raw response. `rawData = params.toString()` là audit thô, không phải contract parse/replay an toàn. Vì vậy việc thấy `vnp_Command=refund` trong tài liệu không đủ để gọi gateway từ production.

## 3. D9 capability checklist — required evidence before code

Merchant owner/VNPay phải xác nhận bằng tài liệu hoặc sandbox credentials thực tế, không suy đoán từ demo values:

1. Refund API được enable cho merchant, môi trường sandbox và production, URL, TLS/IP allowlist, version và secret/key đúng loại API.
2. Full/partial refund có được phép; giới hạn amount, số lần refund, time window, currency/VND scaling và policy after settlement.
3. Field contract chính xác: request ID uniqueness window, original `TxnRef`, original gateway transaction number/date, amount, bank/payment information, operator identity/IP, order-info limits, signing canonicalization và response signature verification.
4. Semantics của mỗi response: accepted, final success, rejected, retryable error, timeout/unknown; có webhook/refund-query hay chỉ `querydr`, và khoảng thời gian reconciliation.
5. Idempotency behavior phía gateway khi merchant timeout sau khi gửi; một `vnp_RequestId` được retry như thế nào và cách tra kết quả trước khi gửi lại.
6. Merchant support/operations contact và sandbox scenario có thể chạy thật cho full, partial, duplicate request, invalid signature, timeout and query reconciliation.

Thiếu bất kỳ mục nào trên thì D9 là **NOT APPROVED** và F10 chỉ dừng ở design. Không tạo "refund giả" bằng cách đổi `PaymentStatus`, `OrderStatus` hoặc trả success frontend.

## 4. Business-policy decisions required for D9

Capability gateway không tự trả lời policy marketplace. Các quyết định cần người dùng duyệt:

| Decision | Current source | Required policy |
|---|---|---|
| Actor | Không có refund command | Buyer request, Vendor request, hay Admin-only approval/execute; khuyến nghị baseline **Admin-only execute**. |
| Trigger | CANCELLED không tự refund | Có tự tạo refund sau Vendor cancellation hay chỉ Admin explicit; khuyến nghị không auto-call gateway. |
| Scope | One payment group can be multi-shop | Whole group only hay per-Order partial; per-Order requires remaining paid/refunded allocation by `Order.totalAmount`. |
| Amount | Client cannot be trusted | Server computes eligible amount from order snapshots and confirmed refund ledger; request không nhận arbitrary amount unless a separately approved partial policy. |
| Eligibility | Payment group can have many attempts | Chỉ original VNPAY SUCCESS attempt with retained gateway references; COD needs separate collection/refund policy. |
| Status | Payment SUCCESS means paid, not refunded | Refund lifecycle must be independent: `REQUESTED`, `UNKNOWN`, `CONFIRMED`, `REJECTED` (exact enum after gateway semantics). |
| Timing | No return/dispute model | Define whether only CANCELLED orders qualify now; delivered/return refunds wait for F12/D10. |
| Notification/analytics | No refund meaning | Decide recipients and whether analytics stays fulfilled gross value until a separate net-refund metric contract. |

Recommended minimal policy, pending approval: **Admin executes an explicit VNPAY refund only for a CANCELLED order; each request uses the server-computed unreimbursed `order.totalAmount`; multi-shop group uses partial gateway refund; no COD, no delivered return, no buyer/vendor self-service, no automatic gateway call.** This is a policy proposal, not an inferred requirement.

## 5. Architecture proposed only after D9 approval

Keep gateway orchestration in Payment. Order exposes a narrow read-only internal DTO for the selected Order/payment-group snapshots; Payment must not inject Order repository. The command resolves actor server-side and locks its own refund record/payment reference; it must not create an Order → Payment lock inversion with IPN (`Payment → Order`).

```text
Admin refund command
  → authorize actor and load Order/payment data through internal contract
  → select canonical successful VNPay attempt + validate gateway identifiers
  → lock/create unique refund attempt (idempotency key)
  → calculate remaining refundable amount server-side
  → persist REQUESTED before remote call
  → gateway response/query reconciliation
  → update attempt CONFIRMED / REJECTED / UNKNOWN
```

The later additive ledger should have scalar references to order/payment/payment group, amount/currency, request/idempotency key, state, original and refund gateway references, sanitized response metadata, actor/reason and timestamps. It must enforce a unique idempotency key; amount limits require transactional aggregation/locking, not only a UI check. Do not overload `PaymentStatus.SUCCESS` or add a cross-module foreign-key graph without reviewing boundaries.

Timeout after request transmission is `UNKNOWN`, not failed and not safe to resend. Reconcile/query first. A gateway response must not be reported to UI as money received by buyer until D9 defines which gateway confirmation is final.

## 6. Future tests after D9 approval

- Official signed full/partial refund fixtures; wrong signature, wrong original reference, amount scaling and malformed response.
- Merchant request ID replay, duplicate HTTP/admin command, simultaneous refunds and timeout followed by reconciliation.
- Multi-shop group: a cancelled shop may refund only its remaining order allocation; total confirmed+pending guarded amount never exceeds successful payment amount.
- Many attempts: FAILED/PENDING/newer attempt cannot redirect refund away from canonical successful attempt.
- Authorization/ownership: malicious buyer/vendor/shop ID cannot refund another order; only approved actor; audit actor/reason server-derived.
- COD before/after collection follows approved policy; no inferred zero-refund behavior.
- Caller/database rollback, remote response handling, notification/UI states and no accidental change to Order/stock/IPN guards.
- VNPay sandbox tests only after merchant capability is confirmed; unit mocks are insufficient for final gateway contract evidence.

## 7. Recommended next action

Obtain the six capability confirmations and answer the policy table. Then record an approved D9 document and design the refund ledger/API before implementation. Until then, F10 remains blocked deliberately; F12 Return & Refund must not bypass it.
