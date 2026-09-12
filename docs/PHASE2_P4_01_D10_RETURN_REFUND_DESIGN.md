# P4-01 / F12 — Return & refund: D10 gate and source inspection

Ngày inspect: **2026-09-10**. Trạng thái: **BLOCKED pending D10 policy**. Đây là tài liệu inspect/design, không phải approval để implement Return hoặc gọi gateway refund.

Nguồn: [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md), [Architecture](ARCHITECTURE.md), F10 sandbox design/source và Order/Payment source hiện tại.

## Status boundary

| Hạng mục | Trạng thái |
|---|---|
| P4/F12 source inspection | **Complete** |
| D10 business policy / state machine | **Pending approval** |
| Return implementation design | Chưa tạo |
| Return module/code, database schema | Chưa implement/tạo |
| Payment refund integration, F12 tests, final verification | Chưa có |
| F12 | **BLOCKED** |

The candidate model below is only an inspection aid. It is not an approved state machine, schema, or indication that F12 is in progress.

## 1. Verified from source

- `OrderStatus` chỉ có `PENDING`, `CONFIRMED`, `SHIPPED`, `DELIVERED`, `CANCELLED`; không có return status và không được thêm `RETURNED_AND_REFUNDED` vào enum.
- Buyer chỉ có delivery-confirm cho Order của chính mình khi `SHIPPED → DELIVERED`. Vendor chỉ chuyển `CONFIRMED → SHIPPED/CANCELLED`; cancellation restore stock từ `OrderItem` snapshot trong transaction hiện tại.
- `DELIVERED` giữ delivery timestamp, buyer confirmer và source; không có return deadline, reason, evidence, item quantity, inspection/disposition, warehouse receipt, return shipping tracking hoặc history.
- Payment hiện có F10 sandbox-only refund ledger/API cho **Admin explicit + CANCELLED + VNPAY**. F10 đang frozen pending real sandbox verification; `DELIVERED`/return không phải eligibility và F12 không được bypass hoặc tự gọi nó.
- COD vẫn không có collection ledger, nên source không biết đã thu tiền hay chưa. Không thể suy ra refund amount/eligibility cho COD.
- Notification hiện có Order lifecycle events; không có return notification. Analytics là delivered gross/order value, không có net-return/refund contract.

## 2. D10 decisions required

Các quyết định dưới đây cần người sở hữu nghiệp vụ duyệt trước implementation; source không đủ để suy diễn:

| Decision | Required policy |
|---|---|
| Actor | Buyer request; Vendor receive/inspect; Admin approval/dispute/override; quyền và ownership từng command. |
| Eligibility | Chỉ `DELIVERED` hay gồm trạng thái khác; time window/timezone; excluded goods; duplicate/in-progress request. |
| Scope | Entire order hay selected items; quantity ceiling theo `OrderItem` snapshot; multi-shop/payment-group handling. |
| Evidence & transport | Reason taxonomy, photo/video, return carrier/tracking, privacy/access/retention and validation limits. |
| Workflow | Request, approve/reject, ship-back, received, inspected, dispute, close; permitted state transitions và actor cho từng transition. |
| Goods disposition | Restock/scrap/quarantine/vendor action; điểm nào gọi Product stock contract; idempotency khi receive/inspect retry. |
| Money | Refund trigger (approval, receipt, inspection, admin decision); partial/full; VNPAY versus COD; relation to frozen F10 and UNKNOWN/reconciliation. |
| Notification & analytics | Recipients/timing; gross vs net values; return/refund metrics and unresolved-financial states. |
| Operations | SLA/escalation, audit requirements, retention, manual correction policy. |

## 3. Required D10 policy before implementation design

D10 must become a short, written business-policy/state-machine decision before any F12 implementation design is created. At minimum it approves the actor/permission matrix, `DELIVERED` eligibility and deadline, excluded goods, order-vs-item scope and partial quantities, exact state transitions, evidence rules, stock/disposition point, financial trigger, COD collection treatment, and notification/analytics meanings.

The state names in any future workflow must be selected by D10. A sample such as `REQUESTED → APPROVED → SHIPPING → RECEIVED → INSPECTING → REFUND_PENDING → CLOSED` is not a default and must not be implemented merely because it appears in discussion.

## 4. Architecture after D10 approval

Introduce a dedicated **Return** module/aggregate only if D10 needs a lifecycle independent from `Order`; do not overload `OrderStatus`. It owns return entities, repositories, command services, controllers, history, and evidence references.

Cross-module access remains through narrow contracts:

```text
Return → Order internal snapshot (owner/shop/status/items/amount)
Return → Product internal stock action (only at approved disposition point)
Return → Payment refund command/query (only after approved financial trigger)
Return → Notification event (after commit)
```

Return must not import `Order`, `Payment`, `Product`, or their repositories/entities. Store scalar IDs in its own tables. Payment retains financial locking and gateway orchestration; Return has no authority to mark a refund confirmed.

## 5. Candidate data model — not approved and not created

Possible additive tables after D10:

```text
return_requests: order_id, buyer_id, shop_id, status, reason, requested_at,
                 approved/rejected/received/inspected data, actor/audit fields
return_items:    return_request_id, order_item scalar reference, requested_qty,
                 approved_qty, received_qty, disposition
return_evidence: return_request_id, media scalar reference, type, uploader, timestamps
return_history:  return_request_id, old_status, new_status, actor, reason, timestamp
```

The exact schema, status enum, foreign keys, locking strategy, and whether item-level tables exist depend on D10. Do not backfill delivered Orders as returns, restore stock solely from a request, or infer a refund from approval.

## 6. Required future tests

- buyer/vendor/admin authorization and cross-order/shop isolation;
- eligibility, deadline, duplicate and concurrent commands;
- full/partial item quantities and immutable OrderItem snapshots;
- evidence validation/access/retention;
- received/inspection disposition and exactly-once stock effect;
- VNPAY request/UNKNOWN/reconciliation only at approved trigger; COD only after an explicit collection policy;
- notification/analytics meanings; rollback and retry boundaries;
- regression of cancellation, F11 delivery confirmation, and frozen F10 refund behavior.

## 7. Next step

Approve D10 as a written policy/state-machine decision. Only then create `PHASE2_P4_01_F12_RETURN_IMPLEMENTATION_DESIGN.md` with exact Return module files, schema migration, API commands, stock and Payment contracts. Until then F12 remains blocked deliberately.
