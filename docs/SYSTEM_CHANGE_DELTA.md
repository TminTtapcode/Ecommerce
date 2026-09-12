# SYSTEM CHANGE DELTA

| Feature | Old State (Previous Final Report) | Current State | What Changed | Verified? | Action |
|---|---|---|---|---|---|
| Admin Order Status | View Only | Mutatable | Added PATCH /api/v1/admin/orders/{orderId}/status | YES | None |
| Delivery Confirm | Vendor controlled | Buyer controlled | Updated to comply with D8 (Buyer confirms delivery) | YES | None |
| Statistics | Missing / Partial | Complete Dashboard | Added KPI cards, charts, and date filters | YES | None |
| Chat | Planned | Complete STOMP | Fully implemented with Optimistic UI | YES | None |
| Notification | Planned | Complete STOMP/REST | Push notifications + Dropdown implemented | YES | None |
| Payment Refund | Not mentioned | Implementation Design (F10) | Gateway file created, but full ledger/API missing | NO | Implement Stage 1 |

## Outdated Claims in Old Report
- The old report implied complete Order lifecycles, but Delivery Confirmation and Cancellation Inventory Restoration were only properly secured and bounded in recent sprints. These are now correctly implemented.

