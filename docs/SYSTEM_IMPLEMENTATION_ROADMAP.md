# SYSTEM IMPLEMENTATION ROADMAP

## Stage 0 — Stabilization
- **Goal**: Clean up any remaining minor FE/BE mismatches before introducing new major flows.
- **Features**: Delivery confirmation edge cases (if any remaining tracking fields needed).
- **Exit criteria**: Test suite 100% green.

## Stage 1 — F10 Sandbox Refund
- **Goal**: Implement canonical VNPAY refund.
- **Dependencies**: F10 Implementation Design approval.
- **BE changes**: Implement Refund Attempt ledger, RefundStatus, Pessimistic locking for refunds, POST /api/v1/admin/orders/{orderId}/refunds.
- **FE changes**: Add "Issue Refund" action in Admin Order Details page.
- **Verification**: Real VNPay Sandbox execution, verifying idempotency and correct order status.
- **Exit criteria**: VNPay successful refund evidence.

## Stage 2 — D10/F12 Return
- **Goal**: Implement Return business logic.
- **Dependencies**: D10 Approval.
- **Changes**: State machine updates (RETURNING), Buyer UI for return requests, Admin UI for processing.
- **Exit criteria**: Return flow e2e verified.

## Stage 3 — Remaining Feature Improvements
- **Goal**: Implement advanced recommendations and caching (Redis).
- **Exit criteria**: Performance metrics improved, UI displays relevant products.

## Stage 4 — Full Regression & Final Report
- **Goal**: Execute full E2E test suite, collect screenshot/video evidence, write FINAL_REPORT_UPDATED.md.
- **Exit criteria**: Project is ready for submission.

