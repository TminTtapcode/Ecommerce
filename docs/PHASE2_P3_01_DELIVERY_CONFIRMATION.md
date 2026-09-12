# P3-01 / F11 — Delivery confirmation

Ngày kiểm chứng: **2026-09-10**. Trạng thái: **đã triển khai theo D8 và kiểm chứng local trên MySQL cách ly; chưa deploy**.

## Thay đổi

- Buyer xác nhận nhận hàng bằng `PATCH /api/v1/orders/{id}/confirm-delivery`. Server lấy buyer từ JWT, khóa Order bằng `PESSIMISTIC_WRITE`, chỉ cho phép Order thuộc buyer đang `SHIPPED`, rồi chuyển `DELIVERED`.
- Vendor matrix chỉ còn `CONFIRMED → SHIPPED|CANCELLED`; Vendor không thể gọi `DELIVERED` qua API cũ hoặc UI.
- Admin giữ ngoại lệ D2 `SHIPPED → DELIVERED`, reason/history/payment guard không đổi.
- Migration additive [P3_01_delivery_confirmation.sql](migrations/P3_01_delivery_confirmation.sql) thêm nullable `delivered_at`, `delivery_confirmed_by_user_id`, `delivery_confirmation_source`. Buyer ghi source `BUYER`; Admin ghi `ADMIN`; historical order giữ `NULL`, không backfill từ `updated_at`.
- Metadata, status và `OrderDeliveredEvent` cùng transaction. Rollback không lưu evidence hay push notification; confirmation trùng bị từ chối trước khi event được publish.
- Buyer detail có action **Đã nhận hàng** chỉ ở `SHIPPED`, disabled khi gửi; Vendor không còn action tự hoàn tất. `OrderResponse` trả `deliveredAt` và source, không lộ actor ID.

## Security và business boundary

- Không nhận `userId`, `shopId`, actor, status, proof hoặc timestamp từ client. Buyer khác owner nhận `404 ORDER_NOT_FOUND`; unauthenticated nhận `401`; state không phải SHIPPED/replay nhận `400 INVALID_ORDER_STATE_TRANSITION` theo D8.
- Shop BANNED vẫn được buyer xác nhận đơn SHIPPED cũ theo D4. Không thêm SHIPPER, tracking, proof, auto-confirm, settlement, refund, return hay payment mutation.
- Review chỉ mở sau `DELIVERED` như contract sẵn có. Analytics tiếp tục cohort `createdAt`; `deliveredAt` không tự đổi nghĩa metric thành ngày giao hàng.

## Kiểm chứng

| Check | Kết quả |
|---|---:|
| `DeliveryConfirmationIntegrationTest` — HTTP + MySQL | **7/7 pass** |
| Regression Maven configured suite | **295/295 pass** |
| Backend compile | PASS |
| Frontend production build | PASS |
| Frontend lint | Exit 0, 18 warnings có sẵn |

Integration chứng minh buyer owner, foreign buyer/unauthenticated, duplicate và source states sai, Vendor bị chặn, Admin force-complete có history/evidence, shop BANNED, và hai buyer confirmation đồng thời chỉ một request thành công cùng một `ORDER_DELIVERED` notification. Full regression ban đầu có 4 E2E failure vì suite còn gọi Vendor `DELIVERED`; fixture đã đổi sang buyer endpoint theo D8 và lần chạy cuối xanh.

Môi trường: Java 21, Maven offline, MySQL 8.0.46 `127.0.0.1:33317`, schema test `ecommerce_s103_s104_s105_s108`, SMTP loopback port 1. Không dùng GUI/browser surface; frontend được kiểm bằng headless TypeScript/Vite build và lint.

## Deploy và rollback

Chạy migration additive trước backend mới, sau đó deploy backend và frontend cùng nhau. Không rollback dữ liệu `DELIVERED` hoặc xóa evidence; nếu rollback ứng dụng, dừng mutation delivery trước và giữ các cột mới. Không bật lại Vendor `DELIVERED` trên một instance cũ trong lúc instance mới đang chạy.

`deliveredAt` là thời điểm server commit xác nhận, không phải bằng chứng carrier hoặc thời điểm giao thực tế. D8 vẫn chưa giải quyết dispute workflow, proof, tracking, refund/settlement và return; các phần đó cần contract riêng.
