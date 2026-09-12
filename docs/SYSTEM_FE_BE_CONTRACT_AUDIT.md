# SYSTEM FE/BE CONTRACT AUDIT

## 1. Mismatches & Gaps

### Payment Refund
- **BE**: A partial refund gateway and IPN logic may exist, but there is no fully approved API endpoint to trigger refunds securely via an Admin interface.
- **FE**: No UI exists for Admins to trigger a refund.
- **Action**: Await F10 approval, build the POST /api/v1/admin/orders/{orderId}/refunds endpoint, and integrate FE.

### Return Flow
- **BE**: OrderStatus enum does not include RETURNING or REFUNDED natively (pending D10).
- **FE**: No UI exposes a "Return" button for Buyers.
- **Action**: Do nothing until D10 is finalized.

## 2. Verified Consistency
- **Checkout to Payment**: The multi-shop split creating a paymentGroupId is successfully mapped in the frontend CheckoutPage and PaymentResultPage.
- **Order Mutations**: Admin mutation (P1-01) properly connects the FE Admin modal to the BE API.
- **Delivery Confirmation**: The flow SHIPPED -> DELIVERED matches the Buyer confirmation (D8).
- **Chat**: STOMP JWT authentication aligns with the frontend FloatingChatWidget token passing.

