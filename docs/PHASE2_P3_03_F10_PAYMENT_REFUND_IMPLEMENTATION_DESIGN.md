# P3-03 / F10 — Payment refund implementation design

Date: **2026-09-10**. Status: **design only; no implementation authorized by this document**.

This document is the implementation design that follows a declared D9 approval. It does not itself prove VNPay merchant capability, nor does it replace the merchant/VNPay evidence record required by [D9](PHASE2_P3_02_D9_PAYMENT_REFUND_DESIGN.md). The current source remains payment-URL creation plus VNPay IPN; it contains no refund implementation.

## 1. Approval boundary and implementation scope

Before implementation, the D9 approval record must name the approving owner/date and select exact policy values for actor, trigger, eligible order states, VNPAY/COD scope, full/partial rules, amount calculation, lifecycle, reconciliation owner, notifications, and analytics. This design must not silently turn D9's former *recommended minimal policy* into an approved rule.

If the approved policy is the proposed minimal baseline, F10 scope is:

- an Admin executes an explicit VNPAY refund for one `CANCELLED` Order;
- amount is the server-calculated unreimbursed `Order.totalAmount`;
- a multi-shop payment group is refunded through a partial gateway refund for that Order;
- no COD, buyer/vendor self-service, automatic refund after cancellation, delivered/return refund, or `PaymentStatus` mutation.

Any other approved choice changes the API authorization, eligibility query, amount allocation, and tests below; it must be documented before coding.

## 2. Architecture boundary

Refund orchestration, the gateway client, the refund ledger, and refund REST endpoints belong to **Payment**. Payment owns its new entities/repositories and must not access `OrderRepository`, `Order` entities, or Order tables directly.

Order exposes one narrow read-only internal contract, for example:

```java
RefundableOrderSnapshot getRefundableOrderSnapshot(long orderId);
```

The DTO contains only: order ID, buyer/shop IDs as required for authorization/audit, payment group ID, payment method, current order status, and immutable `totalAmount` snapshot. It must not expose a JPA entity. The Order implementation reads its own repository and returns a not-found/eligible-state result according to the approved policy.

Payment resolves the authenticated actor in its service and calls this contract. It selects and locks its own payment/refund rows. The command must not hold an Order lock and then acquire Payment locks: IPN currently acquires **Payment then Order**. The preferred refund flow calls the read-only Order contract outside the Payment lock section, then performs all financial serialization on Payment-owned rows. A final lightweight Order eligibility recheck is required only if D9 defines a state race that matters; its lock order must be reviewed explicitly rather than introduced incidentally.

Scalar IDs (`order_id`, `payment_id`, `payment_group_id`) in the Payment-owned ledger are references for audit/query, not JPA relationships or cross-module foreign-key associations.

## 3. Canonical successful VNPay attempt

Do not use latest attempt or merely `paymentGroupId`. For a refund of a selected order:

1. load VNPAY attempts for the group under Payment ownership;
2. select exactly one `SUCCESS` attempt that is eligible under the approved rule;
3. require retained, validated original gateway references required by VNPay;
4. reject as non-actionable when there is no unique canonical attempt or required source fields are absent.

The selection rule must be deterministic and persisted in `refund_attempt.payment_id`; a later PENDING/FAILED attempt cannot change it. Under the baseline, the canonical attempt must cover the group paid amount and have no ambiguity caused by multiple successful attempts. If multiple `SUCCESS` attempts are possible in production, D9 must define which represents settled funds; source does not currently supply this policy or evidence.

## 4. Refund ledger and migration proposal

Add a Payment-owned `refund_attempts` table and a `RefundStatus` independent of `PaymentStatus`:

```text
refund_attempts
  id                         BIGINT PK
  order_id                   BIGINT NOT NULL             -- scalar reference
  payment_id                 BIGINT NOT NULL             -- canonical successful Payment ID
  payment_group_id           VARCHAR(...) NOT NULL
  amount                     DECIMAL(15,2) NOT NULL
  currency                   CHAR(3) NOT NULL
  idempotency_key            VARCHAR(...) NOT NULL UNIQUE
  merchant_request_id        VARCHAR(...) NOT NULL UNIQUE -- if contract says distinct
  status                     VARCHAR(...) NOT NULL        -- REQUESTED/UNKNOWN/CONFIRMED/REJECTED
  actor_user_id              BIGINT NOT NULL
  reason                     VARCHAR(500) NOT NULL
  original_txn_ref           VARCHAR(...) NOT NULL
  original_gateway_txn_no    VARCHAR(...) NULL
  original_gateway_txn_at    DATETIME NULL
  refund_gateway_txn_no      VARCHAR(...) NULL
  gateway_response_code      VARCHAR(...) NULL
  sanitized_gateway_metadata TEXT NULL
  requested_at, resolved_at, created_at, updated_at
```

Required constraints/indexes:

- unique `idempotency_key` for an external Admin command replay;
- unique merchant request ID only if VNPay confirms its uniqueness semantics;
- index `(payment_id, status)` for guarded amount aggregation and reconciliation;
- index `(order_id, created_at)` for order history;
- check/validation that amount is positive and currency is the approved VNPay currency, where database conventions permit.

Do **not** backfill cancelled orders, infer a refund, or migrate any `Payment.status`. Existing `payments` needs additive original-reference columns only if approved gateway evidence requires source fields not safely available from the existing record; do not parse/rely on historic `rawData = params.toString()` as a canonical record.

## 5. Command, idempotency, locking, and state transitions

`REQUESTED`, `UNKNOWN`, `CONFIRMED`, and `REJECTED` are proposed internal names; their exact mapping to VNPay response semantics requires confirmed gateway evidence.

1. Authenticate and authorize the approved actor server-side. Validate the request body only for stable command data (for example `reason` and an idempotency key); never accept amount, payment ID, group ID, or original transaction references from the client as authority.
2. Obtain `RefundableOrderSnapshot` through the Order internal contract and enforce the D9 state/method policy.
3. In one short database transaction, locate and pessimistically lock the canonical successful `Payment`, then find-or-create the refund attempt by idempotency key. A duplicate returns its existing representation and never calls VNPay again.
4. With the same Payment lock held, aggregate `REQUESTED`, `UNKNOWN`, and `CONFIRMED` amounts for that canonical payment. Create a `REQUESTED` row only if the approved remaining-amount rule permits it. Treat unresolved attempts as reserved so concurrent commands cannot exceed paid amount.
5. Commit the local transaction before the remote call. Never retain DB locks while waiting on VNPay.
6. Send the gateway request using the persisted merchant request ID. Persist a verified definitive result as `CONFIRMED` or `REJECTED` in a new transaction.
7. If transmission may have occurred but the response is absent, malformed, unverifiable, or non-final, persist `UNKNOWN`. `UNKNOWN` is neither failure nor permission to resend.

The Payment lock serializes refund amounts for one successful attempt and matches the current IPN direction (Payment before Order). A unique key alone prevents duplicate command identity but does not prevent over-refunding across different idempotency keys.

## 6. UNKNOWN and reconciliation

Create a Payment-owned reconciliation operation (operator-triggered or scheduled only if D9 approves scheduling) that locks the refund attempt, queries VNPay using the confirmed query contract, verifies the response signature, and moves only `REQUESTED`/`UNKNOWN` to a terminal state when evidence is final.

Before a retry, reconciliation must prove the merchant request was not accepted. The retry must reuse or replace the merchant request ID strictly according to VNPay's written idempotency contract. The UI must show an unresolved financial state; it must not say the buyer has received money until the approved final confirmation condition is met.

## 7. Proposed REST contract

The final route/body follow D9 actor policy. For the Admin-only baseline:

```text
POST /api/v1/admin/orders/{orderId}/refunds
Idempotency-Key: <opaque client-generated key>
{ "reason": "..." }

GET /api/v1/admin/orders/{orderId}/refunds
GET /api/v1/admin/refunds/{refundId}
POST /api/v1/admin/refunds/{refundId}/reconcile   # only if manual operation is approved
```

The create response returns a Payment-owned refund view: refund ID, order ID, amount/currency, lifecycle status, requested/resolved timestamps, and a safe display message. It never returns secret material, signature inputs, raw gateway payloads, or gateway identifiers not approved for exposure. A duplicate idempotency key returns the same refund representation; it does not create another refund.

HTTP status and error codes should be finalized with the existing error contract: authorization failure, missing order, ineligible order/method/state, missing canonical-payment evidence, duplicate-key payload mismatch, amount exhausted, and gateway-unresolved are distinct business outcomes.

## 8. VNPay evidence still required

Source configuration proves only sandbox pay URL and environment-variable names for pay credentials. It does **not** prove any refund entitlement. Before gateway code, obtain the merchant/VNPay evidence for:

- refund and query URLs, API version, HTTP method/JSON shape, TLS/IP allowlist, and credential/key type for sandbox and production;
- enabled merchant capability; full/partial permission; amount scale, limits, settlement/time window, and multiple-refund policy;
- exact original fields (transaction number, transaction date, bank/payment data, `TxnRef`) and whether the current Payment record can supply them reliably;
- merchant request-ID uniqueness/retry behavior and whether retry is legal after timeout;
- request signing canonicalization, response signature verification, response-code meanings, and whether acknowledgement is final;
- query/reconciliation request/response contract, result availability period, webhook availability, and operational support contact;
- signed sandbox fixtures that cover success, reject, duplicate, invalid signature, timeout, and query reconciliation.

Until those facts are available, gateway client interfaces may be designed but must not be implemented against guessed fields, endpoints, or status mappings.

## 9. Test plan after implementation approval

- Unit: canonical selection; no-success/ambiguous-success rejection; server amount calculation; D9 eligibility; no `PaymentStatus` or Order transition mutation.
- Persistence/concurrency: same idempotency key replay; distinct simultaneous keys; `REQUESTED`/`UNKNOWN` reservation; total guarded amount never exceeds canonical paid amount; rollback before remote call.
- Gateway contract: official signed full/partial/query fixtures, invalid/malformed response, scaling, original-reference validation, response signature, and every documented response mapping.
- Unknown/reconciliation: timeout after transmission, duplicate request-ID behavior, query before retry, terminal idempotency, and no duplicate remote call.
- Security/boundaries: only approved actor; no foreign order/shop leakage; request cannot choose amount/payment/reference; Payment never injects Order repository/entity; secret/raw data absent from responses/logs.
- Regression: VNPay IPN plus vendor cancellation/stock restoration, multi-shop group behavior, COD exclusion or approved COD policy, notifications, and analytics semantics.

## 10. Proposed file change list — do not create yet

Payment module:

- `payment/entity/RefundAttempt.java`, `payment/enums/RefundStatus.java`, `payment/repository/RefundAttemptRepository.java`
- `payment/service/RefundService.java`, `payment/service/RefundReconciliationService.java`, and a gateway-facing refund/query client interface plus VNPay implementation only after evidence
- refund request/response DTOs and `payment/controller/AdminRefundController.java`
- `payment/repository/PaymentRepository.java` for deterministic canonical-attempt lookup/locking
- additive Payment reference fields only after confirmed gateway requirements

Order module:

- `order/api/OrderInternalService.java`, a minimal `order/api/dto/RefundableOrderSnapshot.java`, and its Order-owned implementation

Cross-cutting, only after D9 notification/analytics decisions:

- payment refund events and Notification listener/type changes;
- statistics contract/query changes for explicitly approved net-refund reporting;
- database migration under the project's existing migration location (none is created by this design).

Tests:

- payment unit/integration tests for ledger, gateway adapter, reconciliation, API/security, and concurrent-refund cases; regression additions to current payment/order tests.

No change is proposed to `PaymentStatus`, `OrderStatus`, F11 delivery confirmation, or existing cancellation semantics.
