# REPORT_RECONSTRUCTION_AUDIT.md
# Old Report Difference Audit — Enterprise Ecommerce

This document outlines the differences between the historical `REPORT_FULL_DETAIL.md` and the verified, actual current state of the system based on source code inspection.

## 1. Features incorrectly marked completed / Missing from Source
- **Redis Caching**: Older documentation frequently mentions Redis. **Current source:** `pom.xml` does not contain any Redis dependencies. Caching is not implemented.
- **Docker/Docker Compose Deployment**: Older documentation mentions containerization. **Current source:** There is no `Dockerfile` or `docker-compose.yml`. The system only runs natively via Maven and Vite.
- **Order Cancellation Inventory Restoration**: Canceling an order does not automatically return the `stock_quantity` to the `ProductVariant`. This is a technical debt not fully accounted for in previous reports.
- **Admin Ban Shop**: Admin panel has "Approve" shop functionality, but the Ban functionality is missing.
- **Admin Category/User UI**: The Backend has APIs for Category and User management, but Frontend has no UI components to consume them (BACKEND_ONLY).

## 2. Database Discrepancies
- **Voucher Usage Table**: The old report claimed a `voucher_usage` table existed. **Current source:** This table does not exist. Usage is tracked directly on the `vouchers` table via the `used_count` column.
- **Refund Attempts Table**: The old report lacked details on refund ledgering. **Current source:** A `refund_attempts` table exists in the Payment module to track F10 VNPay refunds safely.
- **Admin Order History**: **Current source:** Added `order_admin_status_history` to track when Admins forcefully change order statuses.

## 3. Partially Implemented / Unverified Features
- **F10 Refund Orchestration (Sandbox VNPay)**: 
  - *Source Evidence:* Backend logic (`RefundService`, `VnPayRefundGateway`) and DB (`refund_attempts`) are completely implemented. Admin UI `/admin/orders/:id` has the Refund button.
  - *Status:* **UNVERIFIED**. It has not been run end-to-end against the real VNPay sandbox to generate idempotency/reconciliation evidence.
- **F12 Return/Dispute**: 
  - *Status:* **MISSING / BLOCKED**. There is no workflow, UI, or backend logic for buyers to dispute or return an order after delivery.
- **Admin Order Management**:
  - *Source Evidence:* Admin Order view is present.
  - *Status:* **PARTIAL**. The ability for Admin to mutate order statuses (force cancel, etc.) is missing from the Frontend UI (View-only), despite backend support.
- **Admin Dashboard Statistics**:
  - *Status:* **UI_ONLY**. The `/admin` index page exists but lacks the actual statistics API integration.

## 4. Newly Implemented & Verified Features (Accurate in Current System)
- **Multi-vendor Checkout Splitting**: 1 Cart checkout securely splits into N orders and 1 Payment Group UUID. (Verified in `OrderServiceImpl.checkout`).
- **Realtime Chat**: WebSocket STOMP implementation with Optimistic UI on frontend (`ChatController`, `FloatingChatWidget`).
- **Payment IPN Idempotency**: Pessimistic Locking implemented in `PaymentRepository` to prevent race conditions during VNPay Webhooks.
- **Cloudinary Integration**: Fully working in `MediaController` and `ProductForm`.
- **Event-Driven Notifications**: `@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)` successfully handles async notifications after Order and Review events without blocking the main transaction.

## 5. Security Improvements
- **Zero-Trust Media Deletion**: Client cannot pass arbitrary Cloudinary `publicId`s to delete; the backend handles it via DB references.
- **Ownership Checks**: Vendor order endpoints verify `shopId` against the Principal (`VendorOrderController`).

## 6. Known Limitations to Report
- Frontend notification is REST polling (every 60s), not WebSocket Push.
- JWT tokens are stateless without a revocation list (Blacklist). Logout only removes the token from local storage.
- Architecture is Modular Monolith but relies heavily on synchronous HTTP requests if separated (currently internal Java interface calls `InternalService` are strictly adhered to).
