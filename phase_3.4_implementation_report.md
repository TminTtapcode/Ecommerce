# PHASE 3.4 — TRANSACTION AUDIT & IMPLEMENTATION REPORT

**Project:** Enterprise Ecommerce (Modular Monolith)  
**Date:** September 6, 2026  
**Status:** COMPLETE (All DoD items verified with 26/26 tests passing)

---

## Executive Summary

Phase 3.4 audited transaction boundaries across all services, repositories, and event listeners in the Modular Monolith application. Key transactional flaws were resolved:
1. `ReviewNotificationListener` was missing `Propagation.REQUIRES_NEW` on `AFTER_COMMIT`, which risked executing repository operations outside an active transactional context or in auto-commit mode without rollback isolation.
2. `ShopService.approveShop()` previously called `EmailService.sendShopApprovalNotification()` directly inside the active transaction body. This dispatched emails prematurely prior to database commit. It was refactored to publish a semantic `ShopApprovedEvent`, listened to by `ShopNotificationListener` with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
3. Read-only service methods were audited and annotated with `@Transactional(readOnly = true)`, ensuring lazy-loaded collections can be safely traversed and disabling Hibernate dirty checking overhead without overclaiming replica routing.
4. `rollbackFor` was audited across write methods: all write methods throw only unchecked exceptions (`IllegalArgumentException`, `IllegalStateException`, `RuntimeException`). Spring's default `@Transactional` already rolls back on `RuntimeException` and `Error`. No blanket `rollbackFor = Exception.class` was added.
5. Bulk JPQL update `OrderRepository.updateStatusByPaymentGroupId` was annotated with `@Modifying(clearAutomatically = true)` to prevent stale managed `Order` entities in the persistence context.
6. A comprehensive test suite in `TransactionAuditIntegrationTest` empirically proved:
   - Multi-shop checkout all-or-nothing rollback (atomicity).
   - Review notification `AFTER_COMMIT` execution in a new transaction and absence on rollback.
   - VNPay IPN transactionality: atomic success, exception rollback, and idempotent re-delivery.

---

## 1. Confirmed Issues & Implemented Fixes

### 1.1. ReviewNotificationListener Missing REQUIRES_NEW
- **Confirmed Issue:** `ReviewNotificationListener.handleReviewCreatedEvent` was annotated with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`. In Spring, `AFTER_COMMIT` listeners execute after the originating transaction has committed and closed. Without an explicit `@Transactional(propagation = Propagation.REQUIRES_NEW)`, database writes in the listener either execute without an active managed transaction or depend on auto-commit, preventing proper isolation.
- **Implemented Fix:** Added `@Transactional(propagation = Propagation.REQUIRES_NEW)` to `ReviewNotificationListener.handleReviewCreatedEvent`. This opens an independent, isolated transaction specifically to persist the `Notification`.
- **Files Modified:**
  - [`ReviewNotificationListener.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/notification/listener/ReviewNotificationListener.java)

### 1.2. Shop Approval Email Premature Dispatch
- **Confirmed Issue:** `ShopService.approveShop()` directly invoked `emailService.sendShopApprovalNotification(userEmail, shop.getName())` inside the transaction boundary. Even though the method is marked `@Async`, the asynchronous task was submitted to the executor pool immediately before database commit. If any downstream step failed or a constraint failed on commit, the email was already sent.
- **Implemented Fix:**
  - Created `ShopApprovedEvent` record in `com.tmt.ecommerce.shop.api.event.ShopApprovedEvent`.
  - In `ShopService.approveShop()`, removed direct `EmailService` dependency; published `ShopApprovedEvent(shop.getId(), shop.getUserId(), userEmail, shop.getName())`.
  - Created `ShopNotificationListener` in `com.tmt.ecommerce.notification.listener.ShopNotificationListener` with `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)`.
  - Email dispatch now triggers strictly AFTER successful database commit. If `approveShop` rolls back, the event listener is never invoked.
- **Files Created/Modified:**
  - [`ShopApprovedEvent.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/shop/api/event/ShopApprovedEvent.java)
  - [`ShopService.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/shop/service/ShopService.java)
  - [`ShopNotificationListener.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/notification/listener/ShopNotificationListener.java)

### 1.3. Read-Only Transaction Optimization
- **Confirmed Issue:** Several read-only service methods lacked `@Transactional(readOnly = true)`. In methods loading lazy associations (e.g., `Order.items`, `Product.variants`, `Product.images`), traversing these associations outside an active transaction can lead to `LazyInitializationException` if called from non-transactional callers. Furthermore, read-only transactions optimize Hibernate sessions by enabling `FlushMode.MANUAL` and skipping dirty-checking snapshots.
- **Implemented Fix:** Audited candidate methods for entity mutations, event publications, or side effects. Annotated the following verified read-only methods with `@Transactional(readOnly = true)`:
  - `OrderServiceImpl`: `getMyOrders`, `getOrderDetail`, `getVendorOrders`, `getVendorOrderDetail`, `isOrderDeliveredAndBelongsToUser`, `getProductVariantIdsByOrderId`, `getDeliveredOrdersWithVariantsByUserId`.
  - `ProductServiceImpl`: `getAllProducts`, `getProductById`.
  - `ProductInternalServiceImpl`: `getVariantInfo`, `getProductIdByVariantId`, `getShopIdByProductId`.
  - `ReviewServiceImpl`: `getProductReviews`, `getRatingSummary`, `checkReviewEligibility`.
  - `IdentityInternalServiceImpl`: `getUserFullNameOrDefault`.
- **Note on Architecture:** No claim is made that `readOnly = true` routes queries to read-replicas, as the current datasource configuration uses a single primary database.

### 1.4. Audit of rollbackFor
- **Audit Findings:** Checked all write methods (`checkout`, `updateOrderStatusByGroup`, `updateVendorOrderStatus`, `processVnPayIpn`, `generatePaymentUrl`, `createShop`, `approveShop`, `createReview`, `updateReview`, `replyReview`, `deleteReview`).
- **Conclusion:** None of these methods declare or throw checked exceptions in their method signatures. All business and validation failures throw unchecked exceptions (`IllegalArgumentException`, `IllegalStateException`, `RuntimeException`).
- **Decision:** Spring's `@Transactional` defaults to rolling back on unchecked exceptions (`RuntimeException` and `Error`). Adding `@Transactional(rollbackFor = Exception.class)` blindly would be inaccurate and unnecessary. The default rollback policy is semantically correct.

### 1.5. Bulk UPDATE Persistence Context Invalidation
- **Confirmed Issue:** `OrderRepository.updateStatusByPaymentGroupId` executes a JPQL bulk update (`UPDATE Order o SET o.status = :status WHERE o.paymentGroupId = :paymentGroupId`). Bulk updates bypass the Hibernate first-level cache (EntityManager). In `OrderServiceImpl.updateOrderStatusByGroup`, `orderRepository.findByPaymentGroupId(...)` was called prior to the bulk update. The managed `Order` entities in the session would become stale.
- **Implemented Fix:** Added `clearAutomatically = true` to `@Modifying` on `updateStatusByPaymentGroupId`:
  ```java
  @Modifying(clearAutomatically = true)
  @Query("UPDATE Order o SET o.status = :status WHERE o.paymentGroupId = :paymentGroupId")
  void updateStatusByPaymentGroupId(@Param("paymentGroupId") String paymentGroupId, @Param("status") OrderStatus status);
  ```
  This instructs Spring Data JPA / Hibernate to clear the persistence context immediately after the query executes, preventing stale managed entities.
- **Files Modified:**
  - [`OrderRepository.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/main/java/com/tmt/ecommerce/order/repository/OrderRepository.java)

---

## 2. Empirical Test Evidence

Integration tests were implemented in [`TransactionAuditIntegrationTest.java`](file:///c:/Users/Dell/IdeaProjects/ecommerce/src/test/java/com/tmt/ecommerce/TransactionAuditIntegrationTest.java) and verified against real MySQL database tables.

### 2.1. Multi-Shop Checkout Rollback (All-or-Nothing Atomicity)
- **Scenario:**
  - Buyer cart contains items from Shop A (Variant A, stock = 10, quantity = 2) and Shop B (Variant B, stock = 1, quantity = 5).
  - System voucher with `usedCount = 0`.
  - Checkout is initiated. Variant A stock is deducted first. Variant B deduction encounters insufficient stock and throws `IllegalStateException`.
- **Empirical Assertions Verified:**
  - Orders: Exactly 0 orders exist in `orders` table (`assertEquals(0, ordersInDb.size())`).
  - Stock A: Stock of Variant A returned to initial value `10` (did not persist partial decrement to `8`).
  - Stock B: Stock of Variant B remained `1`.
  - Voucher: Voucher `usedCount` remained `0`.
  - Cart: Both cart items remained in `cart_items` table.
  - **Verdict: PASSED (Zero partial state).**

### 2.2. Review Notification AFTER_COMMIT & Rollback
- **Scenario:**
  - Case A (Commit): Buyer submits review for a delivered order. Transaction commits -> Review persisted (`reviews` count = 1) -> Notification created for vendor in new transaction (`notifications` count = 1, type = `NEW_REVIEW`).
  - Case B (Rollback): Transaction publishes `ReviewCreatedEvent` but is rolled back (`TransactionTemplate` with simulated exception).
- **Empirical Assertions Verified:**
  - Notification count did NOT increase after transaction rollback (`assertEquals(1, postRollbackNotifs.size())`).
  - **Verdict: PASSED (AFTER_COMMIT only fires on commit; REQUIRES_NEW isolates write).**

### 2.3. VNPay IPN Transaction (Atomicity, Rollback, Idempotency)
- **Scenario:**
  - 2 Orders in `PENDING` status sharing `paymentGroupId`. 1 Payment in `PENDING` status.
  - Case B (Rollback): IPN with mismatched amount throws `IllegalArgumentException`.
    - Assert Payment remains `PENDING`.
    - Assert Orders remain `PENDING`.
  - Case A (Success): Valid IPN processed (`vnp_ResponseCode = "00"`).
    - Assert Payment transitions to `SUCCESS`.
    - Assert both Orders transition to `CONFIRMED`.
  - Case C (Duplicate IPN): Identical IPN sent again.
    - Pessimistic lock acquired (`findByTransactionIdForUpdate`).
    - Status check (`payment.getStatus() != PaymentStatus.PENDING`) returns immediately.
    - Assert Payment remains `SUCCESS`, Orders remain `CONFIRMED`, no duplicate events.
  - **Verdict: PASSED.**

---

## 3. Test Suite Verification Summary

Execution of test suite:
```
.\mvnw test -Dtest=TransactionAuditIntegrationTest
Tests run: 3, Failures: 0, Errors: 0, Skipped: 0
Time elapsed: 12.66 s
BUILD SUCCESS

.\mvnw clean test
Tests run: 26, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS (Total time: 01:04 min)
```

Breakdown of the 26 passing tests:
- `EcommerceApplicationTests`: 1
- `NPlus1AuditTest`: 7
- `CodCheckoutNotificationTest`: 1
- `VendorOrderManagementTest`: 2
- `PaymentAggregationIntegrationTest`: 3
- `PaymentIpnSecurityIntegrationTest`: 4
- `ShopApprovalIntegrationTest`: 1
- `TransactionAuditIntegrationTest`: 3
- `VoucherInternalServiceImplTest`: 4

---

## 4. Pending Business Rules & Known Limitations

1. **Vendor Order Cancellation Stock/Voucher Restoration:**
   - As specified in Phase 3.4 guidelines, inventory restoration and voucher refunds upon order cancellation belong to the cancellation/refund business workflow and were **not** implemented in this phase. Documented as a pending business rule for future phases.
2. **Read Replica Routing:**
   - `@Transactional(readOnly = true)` annotations optimize Hibernate's session flushing (`FlushMode.MANUAL`) and avoid dirty-checking snapshots. They do not automatically route to read replicas until Spring's `AbstractRoutingDataSource` and multi-datasource configuration are introduced.
3. **Pessimistic Locking & Lock Timeout:**
   - `PaymentRepository.findByTransactionIdForUpdate` locks the payment row during IPN processing. Database lock timeout defaults to MySQL server configurations.
