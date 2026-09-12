# P1-01 / F3 — Admin order status mutation

Ngày kiểm chứng: **2026-09-09**. **Đã triển khai và kiểm chứng F3** theo [D2 đã duyệt, lựa chọn A](PHASE2_P1_01_D2_DECISIONS.md). Đã hoàn thành các giai đoạn BE/migration → FE → integration/regression → browser smoke/review. Chưa triển khai feature P1 tiếp theo hoặc deploy DB ứng dụng.

## 1. Backend và transaction

`AdminOrderService` là command riêng, kiểm ROLE_ADMIN tại service và controller. Actor lấy từ `CurrentUserPrincipal` trong SecurityContext; body không thể gán actor. Admin không cần sở hữu shop, xử lý đơn cũ của cả shop BANNED.

| Nguồn → đích | COD | VNPAY |
|---|---|---|
| CONFIRMED → SHIPPED | Cho phép | Cần SUCCESS đúng group/method đã commit |
| CONFIRMED → CANCELLED | Cho phép | Từ chối |
| SHIPPED → DELIVERED | Cho phép | Cần SUCCESS đúng group/method đã commit |
| PENDING, trạng thái cuối, self-transition hoặc nhánh khác | Từ chối | Từ chối |

Method khác/không xác định bị từ chối. Không tự xác nhận PENDING, không force, không refund. Vendor giữ matrix/policy Sprint 1, gồm khả năng hủy CONFIRMED VNPAY; giới hạn hủy COD chỉ áp dụng command Admin theo D2.

```text
AdminOrderService.update @Transactional
  → role/principal + input validation
  → OrderRepository.findByIdForUpdate (PESSIMISTIC_WRITE)
  → expectedStatus phải khớp → matrix → payment policy
  → PaymentEligibilityQuery.hasSuccessfulVnpayPayment nếu cần
       SELECT exists thông thường, không khóa Payment
  → OrderServiceImpl.applyOrderTransition (nội bộ cùng module)
       dùng chung với Vendor: đổi status, restoreStock nếu hủy, publish event
       cancellation: từng variant ID tăng dần → atomic variant/product restore
  → history.saveAndFlush
  → commit Order + stock + history, hoặc rollback tất cả
  → structured log/notification sau commit
```

Không copy logic hoàn kho sang command Admin: đã tách side effects từ Vendor vào method cùng module dùng chung; mỗi caller chịu trách nhiệm khóa và policy. Product restore vẫn MANDATORY. Không thay checkout, Voucher/Cart hoặc IPN; không thêm listener cộng kho.

Payment read contract mới ở `payment.api`, implementation chỉ dùng PaymentRepository, không gọi ngược Order. Order không truy cập Payment entity/repository/service implementation. Predicate đúng group + VNPAY + SUCCESS, không dựa attempt gần nhất. SUCCESS cũ vẫn có hiệu lực khi attempt mới PENDING/FAILED; nhiều SUCCESS không nhân số transition. Không bổ sung Payment lock sau Order, nên không tạo vòng khóa với IPN **Payment → Order** trong các case đã kiểm chứng.

Cơ sở guard là SUCCESS terminal trong source hiện có. Snapshot không thấy SUCCESS mới có thể từ chối an toàn; cần refetch/retry. Đây không phải xác minh settlement/đủ mọi nghĩa vụ tiền. Khi bổ sung refund hoặc đổi semantics SUCCESS phải review lại guard.

## 2. API, reason và history

| API | Quyền / dữ liệu |
|---|---|
| PATCH `/api/v1/admin/orders/{orderId}/status` | ADMIN; expectedStatus, status, reason |
| GET `/api/v1/admin/orders/{orderId}/allowed-transitions` | ADMIN; danh sách trạng thái đích hợp lệ tại thời điểm đọc |
| GET `/api/v1/admin/orders/{orderId}/status-history?page=0&size=10` | ADMIN; Page history, id giảm dần; page ≥ 0, size 1–100 |

GET list/detail Admin hiện có được giữ. So với đề xuất mở rộng Admin detail trong D2, eligibility được cung cấp qua endpoint riêng để không thay `OrderResponse` dùng chung với buyer/vendor. GET chỉ gợi ý; PATCH luôn revalidate dưới khóa.

```json
{
  "expectedStatus": "CONFIRMED",
  "status": "CANCELLED",
  "reason": "Xác nhận yêu cầu hủy đơn COD"
}
```

Reason trim trước validation, bắt buộc 1–500 ký tự sau trim; service cũng kiểm input khi không đi qua HTTP. Missing Order 404 `ORDER_NOT_FOUND`; stale expectedStatus 409 `ORDER_STATE_CONFLICT`; ngoài matrix 400 `INVALID_ORDER_STATE_TRANSITION`; payment policy không đạt 409 `ORDER_PAYMENT_CONDITION_NOT_MET`. Validation dùng 400 `INVALID_INPUT`. Envelope S1-02, lỗi có success=false/data=null. Duplicate snapshot cũ trả conflict, không restore/history/event thêm.

`order_admin_status_history` lưu orderId, actorUserId, old/new status, reason và Instant UTC (API dạng Z). History insert cùng transaction; lỗi insert rollback cả stock/status. Chỉ ghi can thiệp Admin thành công từ lúc triển khai, không phải timeline đầy đủ Vendor/IPN. Không endpoint sửa/xóa history, không lộ history qua API buyer/vendor. Log sau commit có event/historyId/orderId/actorId/old/new; không nhân bản reason vào log.

## 3. Frontend

AdminOrderDetailPage có form chọn action do backend trả, reason bắt buộc, disable khi gửi/thiếu input, thông báo riêng khi hủy không thực hiện hoàn tiền. Hiển thị history Admin và phân trang. Error logic dùng errorCode; API helper/type và hai mã mới đã bổ sung.

Sau mutation hoặc conflict, tải lại detail/actions/history. Khi refresh thất bại, đóng actions và yêu cầu tải lại, tránh thao tác tiếp trên dữ liệu cũ. Với conflict, clear target để người dùng chọn lại; response tải dữ liệu cũ bị chặn theo route ID/request sequence. Không thay Vendor UI hoặc cơ chế token.

## 4. Test và verification

| Kiểm chứng | Kết quả |
|---|---:|
| AdminOrderIntegrationTest — mới, Spring/HTTP/MySQL thật | **33/33** |
| Toàn bộ suite Maven được cấu hình, gồm 155 tests Sprint 1 | **188/188** |
| Failures / errors / skipped của full run cuối | **0 / 0 / 0** |
| Backend package -DskipTests | PASS |
| Frontend TypeScript + Vite build | PASS |
| Frontend apiError tests | **6/6** |
| Frontend lint | Exit 0, **19 warnings** |
| Edge desktop browser smoke | PASS, exit 0 |

33 cases mới gồm:

- Cancel COD của shop BANNED/khác owner; actor server, trim reason, stock nhiều variants và duplicate history.
- PENDING/terminal/self/nhánh không hợp lệ; COD ship/deliver giữ stock; history stable paging/giới hạn/legacy rỗng.
- VNPAY không attempt/PENDING/FAILED/sai group/sai method/unknown method; nhiều SUCCESS và attempt mới terminal khác; VNPAY cancel bị chặn, ship/deliver đúng guard.
- Variant thứ hai mất, caller rollback, **CHECK constraint DB thật làm history insert thất bại**: status/stock/history rollback. Constraint chỉ tạo trong schema test, xóa trong finally.
- Service không Admin bị chặn; HTTP 401/403, không đọc history/actions của Admin, 404, 409 và error envelope; actor giả trong body không được dùng; reason blank/500/501, expectedStatus thiếu.
- Hai Admin hủy cùng snapshot; Vendor ship thắng Admin cancel; Admin ship thắng request delivery gửi từ snapshot CONFIRMED cũ. Worker/transaction riêng, latch giữ khóa và xác minh request cạnh tranh phải đợi.
- Admin giữ Order trong khi IPN giữ Payment: với SUCCESS cũ thì Admin ship được; không có SUCCESS đã commit thì từ chối, IPN tiếp tục commit. Cả hai worker hoàn thành trong timeout, không lấy Payment lock ngược chiều. IPN commit trước: Admin chờ Order rồi thấy SUCCESS và ship. Không coi đây là stress test mọi interleaving.

Lần đầu có **32/33 pass, 1 error fixture**: tạo trigger yêu cầu quyền SUPER do binary logging. Đã đổi sang CHECK constraint trong schema test, không cấp SUPER/đổi cấu hình DB toàn cục. Lần full cuối chạy Maven không selector `-Dtest`, giữ exclusion sẵn có `FullSystemDataSeederTest`; không thêm skip/exclude. XML tổng hợp chỉ lấy suites xuất hiện trong log lần full cuối, không cộng report cũ.

Browser harness `frontend/tests/admin-order.browser-smoke.mjs` dùng dist đã build, backend JAR và MySQL thật, Edge 152.0.4191.66, viewport 1440×1000. Fixture register/approve/product/cart/checkout qua REST; bootstrap ROLE_ADMIN giới hạn schema smoke cố định. Kết quả:

1. Admin mở đơn COD của shop BANNED; thiếu reason không submit; hủy bằng UI → stock 8 về 10, history hiện đúng actor/reason.
2. HTTP replay → 409 ORDER_STATE_CONFLICT.
3. Đơn thứ hai: Admin mở form CONFIRMED, Vendor chuyển SHIPPED qua API; Admin submit cancellation cũ → conflict, refetch action DELIVERED, không ghi history sai.
4. Admin xác nhận DELIVERED bằng UI → một history, không đổi stock; trạng thái cuối không còn action. Đã xem screenshot conflict để kiểm tra bố cục/thông báo.

Không thêm frontend unit framework; browser smoke kiểm luồng mới, 6 unit tests FE vẫn kiểm helper error hiện có. Cảnh báo bundle >500 kB và 19 lint warnings vẫn còn.

## 5. Môi trường, migration và evidence

Java 21.0.11, Maven 3.9.16 offline, MySQL 8.0.46 riêng **127.0.0.1:33317**, datadir test S1-01. Regression schema **ecommerce_s103_s104_s105_p101**; smoke **ecommerce_p101_smoke**; migration fixture **ecommerce_p101_migration**. User s1baseline chỉ có quyền schema test, password s1-local-only chỉ dành local. SMTP 127.0.0.1:1, timeout 1000ms, không gửi mail ngoài. Cache jakarta.mail-api và Edge CDP cần chạy lại qua approval review khi sandbox không truy cập/khởi động được.

Sau verification đã dừng backend smoke, đóng Edge test và shutdown MySQL test; MySQL80 của máy vẫn Running. Giữ lại data/evidence test.

Tham số Maven full regression sau khi khởi động/xác minh đúng instance test:

```text
-o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
-Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103_s104_s105_p101?allowPublicKeyRetrieval=true
-Dspring.datasource.username=s1baseline -Dspring.datasource.password=s1-local-only
-Dspring.mail.host=127.0.0.1 -Dspring.mail.port=1
-Dspring.mail.properties.mail.smtp.connectiontimeout=1000
-Dspring.mail.properties.mail.smtp.timeout=1000 test
```

Test F3 có guard prefix DB trước fixture, phải dùng datasource trên; không bỏ override rồi chạy vào datasource ứng dụng. Frontend chạy npm.cmd run build/lint và `node --experimental-strip-types --test tests/apiError.test.mjs`. Browser harness chạy từ root khi backend dùng schema ecommerce_p101_smoke tại 8080, Edge/profile test CDP 9222; harness tự serve dist ở 5173. Không dùng script bootstrap admin này cho production.

Migration **[P1_01_order_admin_status_history.sql](migrations/P1_01_order_admin_status_history.sql)** đã chạy bằng SQL thật trên schema fixture trống; SHOW CREATE TABLE kiểm columns/index và insert/rollback còn 0 rows. Không dùng ddl-auto làm bằng chứng script migration. Không FK xuyên module, actor là scalar ID; không backfill. Chưa áp dụng migration vào schema ứng dụng.

Evidence: [full tests](../target/p1-01/full-tests.log), [test summary](../target/p1-01/test-summary.json), [lần đầu](../target/p1-01/tests-initial.log), [backend build](../target/p1-01/build.log), [FE build](../target/p1-01/frontend-build.log), [lint](../target/p1-01/frontend-lint.log), [FE tests](../target/p1-01/frontend-tests.log), [browser JSON](../target/p1-01/browser-smoke.json), [browser log](../target/p1-01/browser-smoke.log), [migration](../target/p1-01/migration.log), [DB identity](../target/p1-01/mysql-identity.txt). XML tại target/p1-01/reports, screenshots tại target/p1-01/screenshots. Target có thể mất khi clean; báo cáo giữ kết quả/cách tái chạy.

## 6. Rollback và giới hạn

- Deploy migration additive trước backend/FE mới; không mở action trước khi backend sẵn sàng. Khi rollback, dừng mutation Admin mới, giữ history và status/stock đã commit; không đưa Order lùi hàng loạt, không bật lại listener hoàn kho cũ. Không rollback Payment guard Sprint 1 riêng lẻ.
- History chỉ can thiệp Admin, chưa phải durable notification outbox; notification sau commit giữ giới hạn hiện tại. Cancellation hook vẫn chỉ log, chưa F1.
- Guard VNPAY không phải refund hoặc xác minh settlement; COD chưa có collection ledger. Admin hủy COD không tự hoàn tiền đã thu ngoài hệ thống. Vendor hủy VNPAY vẫn được giữ theo D1/D2, không tuyên bố đã cấm toàn hệ thống.
- Stock concurrency ngoài các lịch đã test giữ giới hạn Sprint 1; không thêm retry deadlock. Chưa kiểm mọi browser/mobile hoặc tải lớn.
- Workspace không được Git nhận diện; chưa tạo commit hoặc deploy. Feature tiếp theo là P1-02/F6 Profile sau inspect/chốt D6, không tự triển khai trong lượt này.
