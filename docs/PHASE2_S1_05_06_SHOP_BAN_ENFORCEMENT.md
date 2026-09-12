# S1-05 + S1-06 — Ban/Unban và backend enforcement

Ngày kiểm chứng: **2026-09-09**. **Đã triển khai và kiểm chứng** theo [D3/D4 đã chốt](PHASE2_SPRINT1_DECISIONS.md). S1-07/08 chưa được tuyên bố hoàn tất.

## 1. State, quyền và audit

- Shop bổ sung `prior_status` nullable. Ban PENDING/ACTIVE lưu trạng thái hiện tại rồi chuyển BANNED; ban lặp trả `SHOP_INVALID_STATUS_TRANSITION`.
- Unban chỉ khôi phục prior status PENDING hoặc ACTIVE, sau đó xóa prior status. Legacy BANNED không có prior status trả **409 SHOP_PRIOR_STATUS_MISSING**, cần đối soát thủ công.
- Ban/unban không cấp hoặc thu hồi role. Chỉ approveShop được chuyển PENDING → ACTIVE và cấp ROLE_VENDOR qua Identity contract.
- Approve/ban/unban dùng cùng `findByIdForUpdate` với PESSIMISTIC_WRITE. Trạng thái được kiểm tra sau khi có khóa.
- API admin giữ nguyên PUT `/api/v1/admin/shops/{id}/ban`, `/unban`, `/approve`; kiểm tra ROLE_ADMIN tại controller. Actor ban/unban lấy từ CurrentUserId, không lấy trong body.
- Ban body không bắt buộc. Nếu có reason, giới hạn 500 ký tự (giới hạn kỹ thuật đã chọn cho input/log, không biến reason thành trường bắt buộc). Bean Validation và service đều kiểm tra độ dài.
- Phát `ShopStatusChangedEvent`, listener log sau commit: actorId/shopId/oldStatus/newStatus/reason. CR/LF/tab trong reason được chuyển thành khoảng trắng khi log. Không thêm bảng audit hay lưu reason trên Shop.
- ShopResponse bổ sung priorStatus để UI hiển thị trạng thái sẽ khôi phục và nhận biết shop cần đối soát.

## 2. Enforcement theo D4

| Luồng | Khi Shop BANNED |
|---|---|
| Public catalog/search | Loại sản phẩm trước phân trang và count |
| Public product detail | 404 PRODUCT_NOT_FOUND; detail public cũng không trả product HIDDEN |
| Vendor product list/detail | Chủ shop vẫn đọc được; detail kiểm tra ownership riêng |
| Product create/update/delete | Chặn ở service bằng SHOP_NOT_ACTIVE (403), áp dụng cả `/products` và `/vendor/products` |
| Cart hiện có | Giữ item, trả isAvailable=false, không cộng item đó vào total khả dụng |
| Cart add/increase | Chặn ở backend, giữ khóa Shop đến hết transaction |
| Cart decrease/delete | Vẫn cho phép; giảm quantity không phụ thuộc stock hiện tại |
| Checkout | Nếu selection chứa shop bị ban, từ chối toàn bộ trước stock/voucher/order/cart mutation |
| Vendor tạo voucher SHOP | Chặn vì đây là hoạt động bán mới; voucher SYSTEM không đổi |
| Existing orders | Giữ API đọc/chuyển trạng thái theo matrix và ownership cũ |
| Inventory nội bộ / IPN đơn cũ | Không thêm shop-sale guard vào restoreStock hoặc payment confirmation |
| Unban | Không mass-update product HIDDEN thành ACTIVE |

VendorProductController trước đây gọi public detail và không kiểm tra ownership. Đã tách `getVendorProductById(userId, id)`; admin cũng không được dùng endpoint vendor này để bỏ qua ownership. Không thêm API admin product moderation trong bước này.

Product catalog lấy banned shop IDs qua `ShopInternalService.getBannedShopIds`, rồi thêm predicate trên `Product.shopId` trong query của Product. Không join Shop entity/table từ Product repository, không lọc page trong Java sau khi query.

Shop contract `requireNotBannedForSale` yêu cầu transaction của caller (MANDATORY), dùng PESSIMISTIC_READ trên Shop; ban/unban dùng PESSIMISTIC_WRITE. Guard này chặn BANNED, không tự thêm policy mới cho PENDING. Quyền vendor và các kiểm tra hiện có vẫn áp dụng.

## 3. Transaction và cạnh tranh

- Checkout lấy selection rồi khóa các Shop liên quan theo ID tăng dần, trước reserve voucher/trừ kho/tạo Order/xóa Cart. Ban thắng khóa → checkout đọc BANNED và thất bại toàn bộ. Checkout thắng → ban chờ checkout commit, sau đó chặn giao dịch mới.
- Product mutation và tạo voucher SHOP cũng giữ shared Shop lock đến commit. Ban không chỉnh hàng loạt Product/Order, tránh giữ thêm các khóa không cần thiết.
- Cart mutation khóa hàng Cart để so sánh quantity với dữ liệu hiện tại; add/increase còn giữ Shop lock. Giảm/xóa không bị ban chặn.
- Cart và checkout vẫn có thể gặp deadlock khi cạnh tranh qua nhiều tài nguyên với thứ tự khác nhau; chưa thêm retry hoặc tuyên bố loại bỏ mọi deadlock. Transaction thất bại phải rollback. Test cạnh tranh mới tập trung ban/checkout và approve/ban/unban.
- Catalog là read snapshot, không giữ Shop lock đến lúc client xem trang. Trang đã tải có thể cũ sau ban; backend Cart/Checkout mới là nơi quyết định có được tiếp tục giao dịch.

## 4. Frontend

- Admin cho ban cả PENDING/ACTIVE, hiển thị prior status; vô hiệu hóa unban nếu thiếu prior status. Xử lý lỗi đối soát bằng errorCode, khóa nút trong lúc gửi request.
- RequireShop cho phép BANNED vào danh sách/detail sản phẩm và orders; form sản phẩm chỉ đọc, không cho submit, nút ẩn sản phẩm bị vô hiệu hóa. Route tạo mới/voucher vẫn bị guard chặn và có đường về đơn cũ.
- Khi đổi route hoặc focus lại cửa sổ, guard tải lại trạng thái Shop; refresh nền không unmount form đang nhập chỉ vì lấy lại focus.
- Cart unavailable vẫn giảm/xóa được: nút giảm không phụ thuộc isAvailable; lớp phủ unavailable không chặn pointer. Nút tăng và chọn checkout vẫn bị khóa.
- UI không thay thế backend authorization. Test chính gọi HTTP bằng token đã cấp trước khi ban.

## 5. Kiểm chứng

**ShopBanEnforcementIntegrationTest: 23/23 pass**, Spring server + MySQL thật, không mock Shop/Order/Product/Cart/Identity hoặc transaction.

- HTTP admin ban/unban ACTIVE và PENDING; prior status/role đúng; approve BANNED bị từ chối; chỉ approve PENDING cấp role.
- Legacy prior NULL, duplicate transitions, missing shop, non-admin, optional reason và reason quá dài trả đúng error envelope/code.
- Public catalog keyword + page size 1 có totalElements/content đúng sau loại shop bị ban; public detail 404; own vendor detail 200, người khác 403.
- POST/PUT/DELETE cả hai Product API aliases đều 403 sau ban.
- Cart giữ item unavailable, add/increase 403, decrease/delete 200.
- Mixed-shop checkout bị từ chối toàn bộ: không có Order mới, stock hai shop và Cart không bị thay đổi.
- Existing order GET/CANCELLED/SHIPPED vẫn hoạt động; cancellation hoàn kho đúng.
- Unban ACTIVE khôi phục availability và checkout; HIDDEN vẫn HIDDEN.
- HTTP tạo voucher SHOP của vendor bị ban trả 403.
- Rollback ban phục hồi status/prior và không phát audit sau commit; audit thành công có actor/old/new/reason.
- Ban thắng checkout, checkout thắng ban, ban trước approve, approve trước ban, ban/unban và hai ban đồng thời. Dùng connection/transaction độc lập, latch giữ khóa và kiểm tra request thứ hai còn chờ trước khi giải phóng.

| Kiểm tra | Kết quả |
|---|---|
| ShopBanEnforcementIntegrationTest | 23/23 |
| ShopServiceTest + ShopApprovalIntegrationTest | 10/10 |
| InventoryCancellationIntegrationTest | 20/20 |
| PaymentGroupGuardIntegrationTest + PaymentIpnSecurityIntegrationTest | 17/17 |
| OrderServiceImplTest + VendorOrderManagementTest | 8/8 |
| ProductInternalServiceImplTest | 7/7 |
| ErrorResponseTest + JwtErrorResponseTest + ErrorContractIntegrationTest | 25/25 |
| VoucherInternalServiceImplTest | 4/4 |
| Backend package -DskipTests | PASS |
| Frontend build | PASS, bundle warning như trước |
| Frontend lint | Exit 0, 19 warnings như baseline |
| Frontend apiError tests hiện có | 6/6 |
| SQL migration trên fixture schema cũ | PASS |

**Regression chọn lọc: 114/114 pass, 0 failures/errors/skipped.** ShopServiceTest baseline đã được sửa khi cập nhật Shop: verify đúng overload `publishEvent(ShopApprovedEvent)`; fixture unban bổ sung prior ACTIVE và mock repository dùng lock query mới. Không xóa/skip test để làm xanh.

Không chạy lại full E2E/NPlus1 hoặc browser smoke. Build/lint và kiểm tra source UI không thay bằng chứng thao tác trình duyệt; bộ HTTP integration chứng minh enforcement API. Không tuyên bố toàn bộ suite dự án đã xanh hoặc Sprint 1 hoàn tất.

## 6. Migration và rollback

Migration một lần: [S1_05_shop_prior_status.sql](migrations/S1_05_shop_prior_status.sql). Thêm `VARCHAR(20) NULL`, không backfill. Kiểm tra cột đã tồn tại hay chưa trước khi apply, nhất là môi trường bật Hibernate ddl-auto=update. Không chạy ALTER lặp lại.

Đã kiểm chứng script trên schema fixture riêng gồm BANNED/PENDING/ACTIVE: cả ba status giữ nguyên, prior_status đều NULL. Không chạy migration trên schema ứng dụng của người dùng.

Deploy schema trước rồi backend/frontend đồng bộ. Không để bản cũ có unban→ACTIVE hoặc không có enforcement tiếp tục nhận request song song bản mới. SQL là additive; nếu rollback app, giữ cột và dữ liệu prior status, không DROP ngay. Dừng nhận ban/unban và sale flows bị ảnh hưởng trước khi rollback về bản thiếu guard; đối soát rồi mới mở lại. Không tự suy đoán prior status từ role hoặc cập nhật lịch sử BANNED.

## 7. Môi trường, evidence và giới hạn

MySQL 8.0.46 loopback **127.0.0.1:33317**, datadir test S1-01. Schema regression mới `ecommerce_s103_s104_s105`; account s1baseline chỉ có quyền schema test. Schema migration fixture `ecommerce_s105_migration` độc lập. Payment test guard tên schema được mở từ exact s103_s104 sang prefix s103_s104 để chạy regression trên schema mới; không bỏ guard test DB.

Instance MySQL test đã shutdown sau kiểm chứng; service MySQL80 của máy vẫn Running.

Maven 3.9.16 offline, Java 21. Dependency cache ngoài workspace cần approval review khi sandbox không đọc được jakarta.mail-api. Không nâng dependency. SMTP override 127.0.0.1:1, timeout 1000ms; lỗi kết nối email trong log là dự kiến, không gửi mail ngoài.

Selector và datasource:

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
-Dtest=ShopBanEnforcementIntegrationTest,PaymentGroupGuardIntegrationTest,InventoryCancellationIntegrationTest,ShopApprovalIntegrationTest,ShopServiceTest,OrderServiceImplTest,ProductInternalServiceImplTest,VendorOrderManagementTest,ErrorResponseTest,JwtErrorResponseTest,ErrorContractIntegrationTest,VoucherInternalServiceImplTest,PaymentIpnSecurityIntegrationTest
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103_s104_s105?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline
-Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1
-Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000
test
```

Password chỉ dành local test. Khởi động/xác minh đúng instance trước khi chạy; không bỏ datasource override. Frontend chạy `npm.cmd run build`, `npm.cmd run lint`, `node --test tests/apiError.test.mjs`.

Evidence: [tests-final.log](../target/s1-05-06/tests-final.log), [test-summary.json](../target/s1-05-06/test-summary.json), [backend build](../target/s1-05-06/build.log), [frontend build](../target/s1-05-06/frontend-build.log), [lint](../target/s1-05-06/frontend-lint.log), [frontend tests](../target/s1-05-06/frontend-tests.log), [migration](../target/s1-05-06/migration.log), [MySQL identity](../target/s1-05-06/mysql-identity.txt), [lifecycle](../target/s1-05-06/mysql-server.log). XML reports tại target/s1-05-06/reports, có thể mất khi clean.

Giới hạn còn lại: catalog truyền danh sách banned IDs qua contract, chưa tối ưu cho số lượng shop bị ban rất lớn; chưa có durable audit/outbox; không giải quyết mọi race giữa checkout và sửa stock trực tiếp; không mở rộng payment protocol/refund ngoài S1-04. **[S1-07/08 đã hoàn thành](PHASE2_S1_07_08_VERIFICATION.md)**: full configured suite 155/155 backend, 6/6 frontend, build và browser smoke pass; lint còn 19 warnings. Báo cáo này giữ evidence tại thời điểm S1-05/06.
