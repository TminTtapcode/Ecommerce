# P1-03 / F1 — Notification realtime

Ngày kiểm chứng: **2026-09-09**. **Đã triển khai và kiểm chứng F1** theo [C1–C6 đã duyệt](PHASE2_P1_03_STOMP_DECISIONS.md). Hoàn thành BE transport/security → Notification transaction → FE → integration/regression → browser/review. Chưa triển khai F7/F2 hoặc đưa ứng dụng lên server.

## 1. Backend và transaction

`NotificationWriter.create` là điểm tạo notification trong module Notification, MANDATORY tham gia transaction của listener. Order/Review listener vẫn AFTER_COMMIT + REQUIRES_NEW; writer saveAndFlush rồi publish `Created(recipient, NotificationResponse)`. `NotificationPushListener` chỉ gửi sau commit của transaction Notification.

```text
Order/Review commit
  → listener mở transaction Notification
  → writer lưu Notification + publish Created
  → commit Notification → convertAndSendToUser
                       → /user/queue/notifications ở client
  → rollback Notification: không push
```

Push lỗi được log có notificationId và loại lỗi, không log token; không đảo bản ghi đã commit. Test chặn broker channel thật chứng minh service vẫn trả thành công, DB giữ bản ghi và HTTP đọc lại được. Order/Product/Payment không truy cập repository Notification; writer và mapper nằm trong Notification. Không sửa inventory, Payment guard hoặc Admin order policy.

Giữ nguyên producer/recipient: ORDER_CONFIRMED cho buyer, NEW_ORDER cho vendor, ORDER_SHIPPED/ORDER_DELIVERED cho buyer, NEW_REVIEW cho vendor. Shop approval vẫn chỉ email, cancellation vẫn chỉ log. Mark-read không tạo notification mới hoặc push Created.

Payload giữ `id, type, title, message, referenceId, isRead, createdAt`. Timestamp vẫn LocalDateTime, không tự gắn UTC/Z. REST giữ GET list, GET `/unread-count`, PUT `/{id}/read`, PUT `/read-all`. List sort `createdAt DESC, id DESC`, page ≥ 0 và size 1–100. Mark-read sai recipient hoặc không tồn tại trả 404 NOT_FOUND cùng envelope S1-02.

**Lệch contract phát hiện thêm khi test:** BE trả `{unreadCount: ...}`, FE cũ đọc `count`. F1 sửa FE theo DTO BE, đồng thời bỏ `content`/`metadata` giả trong type Notification. Không đổi response backend thành tên trường cũ của FE.

## 2. Transport và security

Config/interceptor đã chuyển từ `chat/config` sang `common/websocket`; Notification không phụ thuộc Chat implementation. `/ws` vẫn native WebSocket, không thêm SockJS/dependency/broker ngoài.

| Frame | Hành vi |
|---|---|
| CONNECT | Bearer JWT hợp lệ/chưa hết hạn, user tồn tại; ID claim phải khớp CurrentUserPrincipal từ UserDetails; không fallback email hoặc CONNECT lại để đổi user |
| SUBSCRIBE | Chỉ `/user/queue/messages` và `/user/queue/notifications` của principal hiện tại |
| SEND | Chỉ `/app/chat.send`; Chat tiếp tục kiểm room ownership và input |
| Raw broker queue, topic, user khác, wildcard/đường dẫn khác | Từ chối, kể cả ADMIN |
| ACK/NACK/BEGIN/COMMIT/ABORT | Từ chối do không thuộc client contract |
| UNSUBSCRIBE/DISCONNECT/heartbeat | Cleanup/transport, không mở quyền nghiệp vụ |

STOMP ERROR có `errorCode` và message UNAUTHORIZED/FORBIDDEN, không trả exception/stack trace cho client; session vi phạm bị đóng. Header Authorization được loại khỏi message sau khi lấy token để tránh giữ credential trong error log. HTTP vẫn dùng envelope riêng.

`SocketSessions` giữ session/expiry đã xác minh, timer đóng socket hết hạn kể cả subscriber không gửi frame; cleanup hủy timer khi đóng. Inbound nghiệp vụ kiểm expiry; outbound MESSAGE kiểm cả lúc gửi vào channel và trước handler thực thi. Không thể thu hồi bytes đã gửi trước expiry. Test dùng JWT ngắn hạn thực, đợi socket tự đóng rồi tạo notification mới để kiểm không nhận thêm; token mới reconnect được.

Origin WS mặc định chỉ `http://localhost:5173` và `http://127.0.0.1:5173`. Property `application.websocket.allowed-origins` cấu hình origins khác, đồng thời được HTTP CORS sử dụng khi khai báo. Khi chưa override, HTTP CORS giữ loopback ports hiện có, WS vẫn giới hạn hai origins trên. Native client không Origin vẫn phải auth STOMP. FE dùng `VITE_API_URL` (mặc định localhost:8080), `VITE_WS_URL` nếu khai báo; nếu không thì suy ra ws/wss từ API URL. Không có domain production tự đặt.

## 3. Frontend và consistency

- `RealtimeProvider` là connection owner dưới AuthProvider. Notification/Chat chỉ đăng ký listener riêng. Mỗi reconnect subscribe cả hai destinations trước khi thông báo connected. Không log raw STOMP debug chứa CONNECT token.
- Thay token hoặc auth generation làm vô hiệu callbacks cũ và deactivate socket cũ trước khi activate mới. Login lại cùng ID/token vẫn là generation mới. Profile save cập nhật tên không tạo session mới hoặc reconnect.
- Notification state và cây Chat được remount theo generation; HTTP chụp token/generation, callback/response phiên cũ bị bỏ qua. AuthContext xử lý storage event để logout/đổi token ở tab khác cũng xóa dữ liệu và đóng socket tab hiện tại. Không triển khai server-side revoke JWT toàn thiết bị.
- Notification provider dedup ID bằng cache giới hạn 500 IDs; push kích hoạt reconcile từ REST, không cộng badge từ payload. GET list/count được serialize theo vòng reconcile, có revision/dirty flag để chạy lại khi push hoặc mutation đến giữa chừng; response cũ không ghi đè kết quả mới. List lấy 10 bản mới nhất, kể cả khi dropdown đóng, nhằm giữ state chung đơn giản.
- Reconcile sau login, subscribe/reconnect, mở dropdown, focus/visibility và read/read-all. Periodic polling dừng khi connected, fallback 60 giây khi disconnected. Read state của nhiều phiên hội tụ khi focus/open/reconnect, chưa có push riêng cho read mutation.
- Dropdown có loading/error/retry; không hiển thị empty giả khi GET lỗi. Chat giữ nghiệp vụ phòng/gửi/echo; các HTTP/callback bị ảnh hưởng được guard theo session. API lỗi vẫn dựa errorCode ở client chung, không so sánh chuỗi tiếng Việt.

## 4. Kiểm chứng

| Suite/check | Kết quả cuối |
|---|---:|
| NotificationPushIntegrationTest — mới, Spring/HTTP/STOMP/MySQL thật | **34/34** |
| NotificationPushListenerTest — lỗi transport unit | **1/1** |
| Full configured Maven suite, gồm 221 tests F6 | **256/256** |
| Failures / errors / skipped | **0 / 0 / 0** |
| BE package -DskipTests | PASS |
| FE TypeScript + Vite build | PASS |
| FE apiError/authSession/WebSocket tests | **15/15** |
| FE lint | Exit 0, **18 warnings** |
| Notification + Chat browser smoke | PASS, exit 0 |
| Profile browser regression | PASS, exit 0 |

Full run cuối không selector `-Dtest`, không thêm exclusion hoặc skip; giữ exclusion sẵn có FullSystemDataSeederTest. Tổng XML chỉ lấy suites xuất hiện trong log full run cuối, không cộng report cũ. Sáu FE tests mới chạy source transport thật với STOMP client giả có thể điều khiển, kiểm subscription độc lập, callback cũ, same-token relogin, reconnect, expiry và nhiều session changes được xếp hàng. Không dùng các unit này thay evidence STOMP wire.

Integration bao phủ recipient isolation/multiple sessions; các producer hiện có, duplicate confirmed event tuần tự; domain rollback; writer MANDATORY; transaction worker giữ trước commit với connection đọc độc lập; SQL lỗi title vượt varchar làm rollback toàn bộ Notification đã lưu trước đó; broker send thất bại nhưng DB/HTTP còn bản ghi. Security cases gồm missing/malformed/expired/missing-ID/mismatched-ID token, raw queue thực đã resolve theo session, foreign/wildcard destinations, client SEND giả, ADMIN không bypass, origin sai, CONNECT lặp, idle expiry/new token và Chat room ownership. REST kiểm 401/400/404, ownership, paging tie-breaker và read rollback.

Lượt integration đầu **23/25 pass**: phát hiện mismatch unreadCount/count và mã lỗi của frame trước CONNECT; đã sửa và kiểm lại. Các lượt full trước bổ sung case cuối là 255/255; kết quả nghiệm thu là **256/256 từ một lần full run cuối**. Compile đã cần quyền đọc cache jakarta.mail-api ngoài sandbox; package từng vướng JAR đang chạy, đã dừng đúng backend smoke rồi đóng gói lại. Không đổi dependency/pom để né lỗi.

### Browser evidence

Harness [notification.browser-smoke.mjs](../frontend/tests/notification.browser-smoke.mjs) dùng Edge headless 152, viewport 1440×1000, dist đã build + JAR cuối + MySQL. Fixture register/approve/product/cart/checkout qua HTTP; chỉ bootstrap ROLE_ADMIN bằng SQL trong schema smoke cố định, không dùng script cho production.

1. Hai browser contexts buyer/vendor: checkout tạo đúng loại notification, badge tự cập nhật, mỗi trang chỉ một socket STOMP; approval không tạo in-app notification.
2. Replay một MESSAGE đã nhận bằng WebSocket event không nhân badge. Read-all bằng click UI, đối chiếu count DB/HTTP. Chat gửi/nhận hai chiều trong cùng kết nối.
3. Đóng WS và chặn reconnect tạm thời nhưng giữ HTTP; gọi callback interval 60 giây đã đăng ký để mô phỏng một timer tick, xác nhận fallback lấy notification đã bỏ lỡ. Bỏ chặn, reconnect/resubscribe/reconcile thành công. Đây không phải đo đợi đủ 60 giây wall clock.
4. Giữ response GET count cũ bằng CDP, phát push mới và read-all, nhả response: count hội tụ đúng. Inject HTTP 500 để kiểm error/retry và giữ session.
5. Logout/login tài khoản khác bằng UI khi GET cũ còn pending; trả 401 cũ: phiên mới không bị logout, chỉ thấy notification/Chat của phiên mới.
6. Hai phiên cùng vendor: một phiên mark-read, phiên còn lại focus thì count hội tụ.
7. Mở thêm tab cùng browser context: logout ở tab đầu gây storage event thật, tab thứ hai đóng socket và về login.

Profile regression giữ đầy đủ luồng save tiếng Việt, phone clear, JWT không đổi, reload/login lấy tên DB, lỗi GET/retry và response cũ sau đổi account hoặc relogin cùng user. Đã xem screenshot dropdown buyer để kiểm bố cục/nội dung.

Harness dùng **127.0.0.1:5173**, tránh `localhost` đi vào Vite đang chạy sẵn qua IPv6. Các lượt smoke ban đầu phát hiện socket HMR do nhầm server và menu logout chưa chờ hiện; đã sửa harness, chạy lại trên dist. Không dừng/sửa Vite của người dùng. Các lỗi HTTP, duplicate frame và timer tick nêu trên là fault injection, không được gọi là lỗi server thật.

## 5. Môi trường và tái chạy

Java 21, Maven 3.9.16 offline, MySQL 8.0.46 riêng **127.0.0.1:33317**, datadir `target/s1-baseline/mysql-data`. Regression schema **ecommerce_s103_s104_s105_p101_p102_p103**, smoke **ecommerce_p103_smoke**. Test account `s1baseline` tại host 127.0.0.1, password `s1-local-only`, chỉ có quyền schema test; không dùng schema ecommerce hoặc thay MySQL80 service. SMTP 127.0.0.1:1 với timeout 1000ms, không gửi mail ngoài.

Sau khi khởi động/xác minh đúng test instance và grant schema, Maven full regression:

```text
mvn -o -Dmaven.repo.local=C:/Users/Dell/.m2/repository
  -Dspring.datasource.url=jdbc:mysql://127.0.0.1:33317/ecommerce_s103_s104_s105_p101_p102_p103?allowPublicKeyRetrieval=true
  -Dspring.datasource.username=s1baseline -Dspring.datasource.password=s1-local-only
  -Dspring.mail.host=127.0.0.1 -Dspring.mail.port=1
  -Dspring.mail.properties.mail.smtp.connectiontimeout=1000
  -Dspring.mail.properties.mail.smtp.timeout=1000 test
mvn -o -Dmaven.repo.local=C:/Users/Dell/.m2/repository -DskipTests package
```

PowerShell phải quote từng tham số `-D...`; không bỏ datasource override. F1 test guard đúng tên schema regression trên. Frontend: `npm.cmd run build`, `npm.cmd run lint`, `node --experimental-strip-types --test tests/apiError.test.mjs tests/authSession.test.mjs tests/websocket.test.mjs`.

Browser: JAR cổng 8080 dùng `--spring.config.location=file:src/test/resources/application.yml`, datasource smoke và SMTP local như trên; Edge/profile riêng dưới target, CDP 9222. Từ root chạy `node frontend/tests/notification.browser-smoke.mjs`; harness tự serve dist IPv4 cổng 5173. Profile regression là bản harness F6 copy trong target, đổi evidence path và origin sang IPv4; source harness F6 giữ nguyên.

Evidence: [full run](../target/p1-03/full-tests-final.log), [summary](../target/p1-03/test-summary.json), [integration đầu](../target/p1-03/tests-initial.log), [build BE](../target/p1-03/build.log), [build FE](../target/p1-03/frontend-build.log), [lint](../target/p1-03/frontend-lint.log), [FE tests](../target/p1-03/frontend-tests.log), [browser JSON](../target/p1-03/browser-smoke.json), [browser log](../target/p1-03/browser-smoke.log), [Profile smoke](../target/p1-03/profile-regression/browser-smoke.json), [schema](../target/p1-03/schema-check.txt), [MySQL identity](../target/p1-03/mysql-identity.txt), [artifact](../target/p1-03/artifact-check.json). XML tại target/p1-03/reports; screenshots tại target/p1-03/screenshots. Target có thể mất khi clean.

## 6. Migration, rollback và giới hạn

- **Không thêm schema migration** hoặc backfill notification. Artifact đã xác nhận không còn config/interceptor ở package Chat cũ và không có ProductOrderCancelledListener. Không trộn class cũ/mới khi chạy bằng IDE hoặc copy class thủ công.
- Đưa FE/BE transport mới vào cùng phiên bản chạy, cấu hình origins/API URL và reconnect sessions. Khi cần rollback push, giữ persistence/REST/polling; không rollback về destination authorization thiếu bảo vệ. Không xóa dữ liệu đã lưu.
- Simple broker/in-memory session phù hợp một application instance hiện tại; chưa có broker relay/multi-instance delivery, durable outbox hoặc retry delivery. Process chết trước khi lưu Notification vẫn có thể mất thông báo; đã lưu thì REST đọc lại được.
- Dedup ID trên FE không phải DB/domain-event idempotency. Exists-before-save cho confirmed chưa có unique constraint; không hứa exactly-once hoặc sửa duplicate dữ liệu lịch sử.
- Read state hội tụ, không push tức thời mọi tab; REST list/count không phải atomic snapshot. Notification tạo trước khi read-all commit có thể được đánh dấu đọc theo semantics hiện có.
- Chat vẫn push trước transaction commit theo source cũ; F1 chỉ bảo đảm after-commit cho Notification. Không sửa optimistic message matching, NEW_REVIEW route mapping legacy hoặc các nghiệp vụ ngoài scope.
- Logout không revoke token đã sao chép ở thiết bị khác; token vẫn theo expiry. Không bổ sung policy user-status/Shop mới. Chưa kiểm mobile/mọi browser, tải lớn hoặc mọi lịch cạnh tranh. Bundle >500 kB và 18 lint warnings vẫn còn.

Bước tiếp theo là **P1-04 / F7 Advanced search**, bắt đầu inspect/chốt D7; chưa triển khai trong lượt F1.
