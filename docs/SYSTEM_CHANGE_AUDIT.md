# SYSTEM CHANGE AUDIT

| Feature | Old report | Current BE | Current FE | Result |
|---|---|---|---|---|
| Payment | VNPay basic | VNPay/COD + Pessimistic Lock | Polling Result Page | Improved |
| Delivery | Vendor confirm | Buyer confirm (D8) | Implemented | Improved |
| Refund | None | VnPayRefundGateway in progress | No UI | BE only |
| Chat | Planned | STOMP/WebSocket | FloatingChatWidget | Improved |
| Notification | Eventual | STOMP push + Polling | Polling + Dropdown | Improved |
| Order Status | Vendor confirm | Admin mutation (P1-01) | Admin Modal | Improved |
| Statistics | Basic | Module with KPI cards | Admin Dashboard | Improved |
| Recommendation | None | Rule-based exact category | Related items UI | Improved |
| Inventory Cancel | Manual | Auto restore (S1-03) | Handled | Improved |
