# Phase 3.3 Implementation Report: N+1 Audit & Resolution (Final Verification)

## 1. Overview
The objective of Phase 3.3 was to identify and eliminate N+1 query loops across the application without breaking modular monolithic boundaries. Our query-driven audit focused on three distinct query patterns:
1. `FetchType.LAZY` child collections on domain entities (`Order.items`, `Product.variants`, `Product.images`).
2. Iterative cross-module API lookups inside DTO mapping methods (`OrderServiceImpl`, `CartServiceImpl`).
3. Iterative database existence checks inside business validation loops (`ReviewServiceImpl`).

---

## 2. Confirmed N+1 Issues & Fixes Implemented

### 2.1 Order Module
- **Issue 1 (Lazy Collection Amplification)**: 
  - Iterating over paginated orders (`orderPage.getContent()`) and accessing `order.getItems()` triggered one SQL query per order (`1 + N`). Using `@EntityGraph` on paginated queries caused in-memory pagination warnings and Cartesian product risks.
  - **Fix**: Replaced `@EntityGraph` with `@BatchSize(size = 50)` on `Order.items`. This bounds child query execution by batch size (`ceil(N / 50)` queries) and completely avoids one-query-per-parent behavior.
- **Issue 2 (Cross-Module Loop)**:
  - In `OrderServiceImpl.mapToOrderResponses()`, mapping each `OrderItem` originally invoked `productInternalService.getProductIdByVariantId(variantId)` inside a loop.
  - **Fix**: Created bulk API `ProductInternalService.getProductIdsByVariantIds(List<Long> variantIds)` using an `IN (:ids)` clause. The loop was replaced with a single bulk query producing an in-memory lookup map.

### 2.2 Cart Module
- **Issue 1 (Cart Items Collection)**:
  - `CartRepository.findByUserId` loaded the `Cart` entity, and accessing `cart.getItems()` performed an additional lazy initialization query.
  - **Fix**: Applied `@EntityGraph(attributePaths = {"items"})` on `CartRepository.findByUserId`. Because a user has exactly one cart (1-to-1 without pagination), an eager fetch join eliminates the extra query safely.
- **Issue 2 (Cross-Module Loop)**:
  - In `CartServiceImpl.getCart()`, each cart item triggered individual calls to `productInternalService.getProductVariantDetails(...)`.
  - **Fix**: Created bulk API `ProductInternalService.getVariantInfos(List<Long> variantIds)`. Variant, product, and image details are fetched in bulk and mapped in memory.

### 2.3 Product Module
- **Issue (Product Listing Collections & Relations)**:
  - In `ProductServiceImpl.getAllProducts()`, each product response required category name, variants, and images. Without batching or joins, this produced `1 (products) + N (categories) + N (variants) + N (images)` queries.
  - **Fix**: 
    - Added `@EntityGraph(attributePaths = {"category"})` on `ProductRepository.findByStatus` and `findByNameContainingIgnoreCaseAndStatus` (safe for `@ManyToOne` without Cartesian product).
    - Configured `@BatchSize(size = 50)` on `Product.variants` and `Product.images`.

### 2.4 Review Module
- **Issue (Iterative Review Existence Checks)**:
  - In `ReviewServiceImpl.checkReviewEligibility()`, the service iterated over delivered orders and executed `reviewRepository.existsByUserIdAndOrderIdAndProductId` once per candidate item.
  - **Fix**: Added `reviewRepository.findReviewedOrderIds(userId, productId, orderIds)` using a bulk `WHERE order_id IN :orderIds` query. Candidate orders are filtered in memory.

---

## 3. Query-Count Evidence (Empirically Measured via Hibernate Statistics)

All metrics below were measured via automated integration tests (`NPlus1AuditTest`) running against MySQL using `SessionFactory.getStatistics().getPrepareStatementCount()` after flushing and clearing the persistence context.

| Operation / Flow | Scenario / Dataset Tested | Measured Query Count | Breakdown of Executed Queries |
| :--- | :--- | :---: | :--- |
| **Product Listing**<br>`productService.getAllProducts(0, 10, null)` | Page of products with variants, images, and category accessed for all items | **4** | 1. Products + Category `LEFT JOIN` (via `@EntityGraph`)<br>2. Pagination `COUNT(*)`<br>3. Batch `variants` fetch with `product_id IN (?, ...)` (`@BatchSize`)<br>4. Batch `images` fetch with `product_id IN (?, ...)` (`@BatchSize`) |
| **Order Listing**<br>`orderService.getMyOrders(userId, null, 0, 10)` | 4 Orders (8 OrderItems) with variant-to-product mapping and customer name | **7** | 1. Orders page `SELECT`<br>2. Batch `order_items` fetch with `order_id IN (?, ?, ?, ?)` (`@BatchSize`)<br>3. Bulk `product_variants` fetch with `id IN (?, ?)` (`getProductIdsByVariantIds`)<br>4–7. Customer name lookups (see limitations section below) |
| **Review Eligibility**<br>`reviewService.checkReviewEligibility(userId, productId)` | 4 Delivered Orders (multiple variants) + 1 existing review | **3** | 1. `findDeliveredOrderVariants` (JPQL projection selecting order IDs & variant IDs)<br>2. Bulk variant-to-product lookup (`id IN (?, ?)`)<br>3. Bulk review existence query (`findReviewedOrderIds` with `order_id IN (?, ?)`) |
| **Get Cart**<br>`cartService.getCart(userId)` | 1 Cart containing 2 distinct items from 2 shops | **3** | 1. Cart + CartItems join (`@EntityGraph`)<br>2. Bulk variant & product join (`getVariantInfos` with `id IN (?, ?)`)<br>3. Batch product images fetch (`@BatchSize`) |
| **Checkout**<br>`orderService.checkout(userId, req)` | Multi-shop checkout (2 shops, 2 items) | **15** | Includes: Cart load (1), Bulk variant load (1), Batch images (1), Atomic inventory updates per variant/product (4), Order & OrderItem inserts per shop (4), Cart items cleanup (2), Identity & Shop validation queries (2) |

---

## 4. Performance Behavior Inferred from the Implementation

1. **Hibernate Batch Fetching (`@BatchSize(size = 50)`)**:
   - Reduces N+1 query amplification by replacing `N` individual child queries with `ceil(N / 50)` batch queries using SQL `IN` predicates.
   - For any standard page size $\le 50$, lazy collection loading for that collection executes in at most 1 query.
   - Growth is strictly bounded by batch size, avoiding one-query-per-parent behavior.

2. **Bulk Internal APIs**:
   - `getProductIdsByVariantIds` and `getVariantInfos` execute exactly 1 SQL query for up to standard SQL `IN` parameter limits.
   - Eliminates per-item cross-module RPC/service loops across module boundaries.

3. **Cart Fetching**:
   - When cart size exceeds the image batch size (50), Hibernate will issue additional batch queries for images (`ceil(distinct_products / 50)`). Hence, cart queries are bounded by batch size rather than strictly $O(1)$ under arbitrary scales.

---

## 5. Remaining Performance Limitations & Next Steps

1. **Customer Name Lookup in Order DTO Mapping**:
   - In `OrderServiceImpl.mapToOrderResponseWithMap`, `identityInternalService.getUserFullNameOrDefault(order.getUserId())` is called once per order.
   - In `getMyOrders`, all orders belong to the same buyer; however, because `getMyOrders` is not annotated with `@Transactional`, each call opens a separate session when not cached. In `getVendorOrders`, orders come from different buyers, resulting in 1 query per distinct customer.
   - **Recommended Future Work**: Introduce a bulk `IdentityInternalService.getUserFullNamesByIds(List<Long> userIds)` API during Phase 3.6 or when vendor order volumes warrant it.

2. **Large `IN` Predicates**:
   - Both `@BatchSize(size = 50)` and bulk APIs rely on `IN` clauses. If lists exceed hundreds or thousands of elements, batch chunking (e.g. `Lists.partition(list, 500)`) should be maintained.

---

## 6. Verification & Test Summary
- Full project test suite run via `.\mvnw clean test`.
- **Result**: `Tests run: 23, Failures: 0, Errors: 0, Skipped: 0` (BUILD SUCCESS).
- Architecture contracts preserved: No cross-module entity/repository leaks introduced; communication remains strictly on internal service interfaces and DTOs.
- **Status**: Phase 3.3 is fully verified and ready for final approval.
