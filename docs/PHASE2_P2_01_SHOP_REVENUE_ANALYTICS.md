# P2-01 — Shop Revenue & Analytics

Ngày kiểm chứng: **2026-09-10**. Trạng thái: **đã triển khai backend/frontend và kiểm chứng tự động/API; browser smoke còn chờ vì môi trường không có browser surface.** Chưa triển khai P2-02/P2-03, không deploy và không thêm migration.

## 1. Phạm vi và metric dictionary

Endpoint `GET /api/v1/vendor/analytics` chỉ trả aggregate của shop được suy ra từ principal. Không nhận `shopId` từ client.

Mọi metric baseline có `dateBasis = ORDER_CREATED_AT`: `startDate`/`endDate` là ngày inclusive theo hành vi `LocalDateTime` server hiện có. Không dùng `updatedAt` để giả làm ngày giao hàng và không thêm `deliveredAt`.

| Field | Định nghĩa |
|---|---|
| `totalOrders` | Số Order của shop trong cohort ngày tạo, mọi status. |
| `ordersByStatus` | Số PENDING/CONFIRMED/SHIPPED/DELIVERED/CANCELLED trong cùng cohort. |
| `deliveredOrderCount` | Số Order hiện đang DELIVERED trong cohort. |
| `fulfilledGrossOrderValue` | `SUM(originalTotalAmount)` của DELIVERED. |
| `voucherDiscountAmount` | `SUM(discountAmount)` của DELIVERED; voucher đã được checkout phân bổ cấp shop. |
| `fulfilledOrderValue` | `SUM(totalAmount)` của DELIVERED, nhãn UI: **Giá trị đơn hoàn tất sau giảm giá**. |
| `averageFulfilledOrderValue` | `fulfilledOrderValue / deliveredOrderCount`, HALF_UP scale 2; zero khi không có DELIVERED. |
| `dailyChart` | Số Order theo ngày tạo và fulfilled order value của DELIVERED trong từng ngày. |

`fulfilledOrderValue` không phải lợi nhuận, settlement hay tiền thực thu. CANCELLED không đi vào các tổng tiền/AOV và không được hiểu là đã refund. Không join Payment: payment group có nhiều Order/shop và nhiều payment attempts nên join sẽ làm nhân tiền sai.

## 2. Kiến trúc và bảo mật

```text
GET /api/v1/vendor/analytics
  → VendorAnalyticsController @PreAuthorize(ROLE_VENDOR)
  → @CurrentUserId
  → VendorAnalyticsService (read-only, validate range)
  → ShopInternalService.findShopIdByUserId
  → OrderInternalService.getShopOrderAnalytics
  → OrderRepository DB aggregates WHERE shop_id = resolvedShopId
```

- `VendorAnalyticsController` không có query/path/body `shopId`.
- Service mới resolve ownership bên trong service; repository luôn lọc shop ID đã resolve.
- Vendor không có shop nhận `404 SHOP_NOT_FOUND`; buyer/admin không có `ROLE_VENDOR` nhận 403; unauthenticated nhận 401/403 theo security layer.
- Shop BANNED vẫn đọc analytics lịch sử theo D4, không gọi `requireNotBannedForSale`.
- Statistics không import Order/Product entity/repository. Nó chỉ dùng Shop và Order internal contract/DTO. Không có Payment/Product/Category dependency mới.

## 3. Implementation

- Order: thêm `ShopOrderAnalyticsDto`, aggregate internal contract và ba query DB-side: status counts, delivered snapshot amounts, daily chart. Không load toàn bộ Order vào Java.
- Statistics: `VendorAnalyticsService`, `VendorAnalyticsController`, `VendorAnalyticsResponse`; range đảo ngược trả `400 INVALID_INPUT` theo envelope S1-02.
- Frontend: `vendorAnalyticsApi`, `VendorAnalyticsPage`, route `/vendor/analytics` với `ProtectedRoute(requireVendor)` + `RequireShop allowBanned`, VendorNav link. UI có date filter, refresh, KPI, status bars, daily chart, loading, retry/error, empty và layout responsive.
- Không có migration; index `(shop_id, status, created_at)` và `(shop_id, created_at)` đã có trên Order.

## 4. API

```http
GET /api/v1/vendor/analytics?startDate=2026-09-01&endDate=2026-09-30
Authorization: Bearer <vendor token>
```

Response bọc `ApiResponse`, trả `dateBasis: "ORDER_CREATED_AT"`, status distribution, snapshot totals và `dailyChart`. Error dùng `success=false`, `errorCode`, `data:null`.

## 5. Tests và evidence đã chạy

Môi trường: Java 21.0.11, Maven 3.9.16 offline, MySQL 8.0.46 test tại `127.0.0.1:33317`, schema `ecommerce_s103_s104_s105_s108`. SMTP override loopback port 1 khi chạy Maven.

| Kiểm chứng | Kết quả |
|---|---:|
| `VendorAnalyticsIntegrationTest` mới, Spring Boot + HTTP + MySQL | **5/5 pass** |
| Full configured Maven suite | **283/283 pass**, 0 failure/error/skipped |
| Frontend `npm.cmd run build` | **PASS** |
| Frontend `npm.cmd run lint` | Exit 0, **18 warnings có sẵn** |
| Maven `package -DskipTests` với repackage skip | **PASS** |
| Maven normal repackage | Không pass vì `target/ecommerce-0.0.1-SNAPSHOT.jar` đang bị Java process ngoài lượt này khóa rename |
| Browser smoke | **Chưa chạy**: CUA trả `apps: []`, `browsers: []` |

Integration test chứng minh trực tiếp: scope Vendor A/B kể cả query `shopId` chế tác; unauthenticated/buyer/admin/no-shop vendor; BANNED shop; five statuses; chỉ DELIVERED vào money/AOV; voucher snapshots; empty shop; date filter ngày inclusive và range đảo ngược trả envelope chuẩn. Đây cũng là API verification với vendor account fixture trên DB thật.

## 6. Rollback và limitations

- Read-only feature, không có migration hay mutation dữ liệu; rollback API/UI không đảo dữ liệu Order.
- Không có top product/category hoặc item-level discount allocation. Không suy diễn category/product lịch sử từ model hiện tại.
- Không có refund, return, cost, commission, COD collection hoặc settlement ledger. Các metric đó chờ F10/F12 và contract riêng.
- Timezone giữ server behavior hiện có; cần chốt timezone vận hành trước deploy. `createdAt` cohort không phải ngày giao/thu tiền.
- JAR repackage mặc định cần chạy lại sau khi process đang giữ artifact được dừng; không tự dừng process đó vì không thuộc lượt P2-01.
- Browser smoke cần được chạy lại khi môi trường cung cấp browser surface. Vì yêu cầu visual chưa có evidence, **chưa cập nhật `PHASE2_IMPLEMENTATION_PLAN.md` để đánh dấu F14 hoàn tất**.

## 7. Bước còn lại

Mở browser surface, chạy smoke với vendor ACTIVE và BANNED: mở `/vendor/analytics`, xác minh nav, KPI, filter/refresh, empty/error UI và nhãn date basis. Sau khi pass, cập nhật Implementation Plan từ P2-01 pending thành completed.
