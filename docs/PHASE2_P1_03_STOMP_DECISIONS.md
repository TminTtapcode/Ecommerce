# P1-03 / F1 — Inspect và contract Notification push

Ngày inspect và phê duyệt: **2026-09-09**. Trạng thái: **người dùng đã duyệt C1–C6; đang triển khai F1**. Theo [P1 Feature Plan](PHASE2_P1_FEATURE_PLAN.md) và [Implementation Plan](PHASE2_IMPLEMENTATION_PLAN.md). Phần inspect bên dưới giữ bối cảnh trước code. Baseline lịch sử F6: 221/221 BE, 9/9 FE, lint 18 warnings; không phải evidence F1.

## 1. Hiện trạng và khác biệt so với audit

| Chủ đề | Source đã inspect | Kết luận |
|---|---|---|
| Tạo notification | [OrderNotificationListener](../src/main/java/com/tmt/ecommerce/notification/listener/OrderNotificationListener.java): confirmed tạo ORDER_CONFIRMED cho buyer và NEW_ORDER cho vendor; shipped/delivered cho buyer. [ReviewNotificationListener](../src/main/java/com/tmt/ecommerce/notification/listener/ReviewNotificationListener.java): NEW_REVIEW cho vendor | Các handler tạo dữ liệu chạy AFTER_COMMIT + REQUIRES_NEW, trực tiếp save repository; chưa push |
| Shop/cancellation | [ShopNotificationListener](../src/main/java/com/tmt/ecommerce/notification/listener/ShopNotificationListener.java) chỉ gửi email; cancellation chỉ log | Audit nói cả Shop listener lưu Notification là không đúng. Không tự bổ sung notification approval/ban/cancel |
| Persistence | Notification có ID, userId scalar, type/title/message/referenceId/isRead/LocalDateTime createdAt; không unique dedup key | Confirmed dùng exists-before-save; không chứng minh chống duplicate event đồng thời. Dedup UI theo ID không giải quyết duplicate rows |
| REST | [NotificationController](../src/main/java/com/tmt/ecommerce/notification/controller/NotificationController.java): GET list/count, PUT /{id}/read và /read-all, principal userId; service đọc/ghi theo recipient | REST là nguồn dữ liệu đã lưu. Sort chỉ createdAt, chưa tie-breaker ID; lỗi mark-read dùng IllegalArgumentException |
| Transport | [WebSocketConfig](../src/main/java/com/tmt/ecommerce/chat/config/WebSocketConfig.java): native /ws, simple broker /queue và /topic, /app, /user | Không có withSockJS(). PROJECT_CONTEXT mô tả SockJS đã cũ. Origin WS wildcard nhưng HTTP CORS hiện giới hạn loopback; không kết luận handshake mọi origin thành công nếu chưa test |
| Authentication | [JwtChannelInterceptor](../src/main/java/com/tmt/ecommerce/chat/config/JwtChannelInterceptor.java): chỉ xử lý CONNECT có Bearer, JWT valid thì gán principal userId, thiếu ID fallback email | Missing Bearer hoặc nhánh không gán authentication vẫn return message; một số token sai có thể throw khi parse. Không có explicit deny SUBSCRIBE/SEND hay kiểm expiry sau CONNECT |
| Chat | [ChatMessageController](../src/main/java/com/tmt/ecommerce/chat/controller/ChatMessageController.java) lấy sender từ principal; [ChatServiceImpl](../src/main/java/com/tmt/ecommerce/chat/service/ChatServiceImpl.java) kiểm buyer/shop owner | Giữ kiểm quyền room. Chat đang push trong transaction trước commit; đây là giới hạn Chat riêng, không lấy bảo đảm Notification áp cho Chat |
| FE connection | [websocket.ts](../frontend/src/services/websocket.ts): URL localhost cố định, một subscription messages; connect bỏ qua nếu client active, reconnect dùng token đã capture | Đổi token khi connection active có thể vẫn giữ session cũ; debug STOMP thô cần bỏ/redact để không log CONNECT credentials |
| FE ownership | [useChat](../frontend/src/hooks/useChat.ts) sở hữu connect; ChatProvider luôn mount, ChatTestPage còn gọi useChat lần nữa | Không để dropdown trở thành owner thứ hai tự disconnect Chat. Chat state/HTTP response chưa có guard session đầy đủ |
| FE notification | [NotificationDropdown](../frontend/src/components/notification/NotificationDropdown.tsx): count poll 60s; list lấy 10 khi mở; count trừ cục bộ/read-all đặt 0 | Chưa push/dedup/reconcile race/session guard. Type FE yêu cầu content nhưng BE trả message; cần sửa khớp DTO |
| F6 | AuthContext/authSession có generation, profile dùng token chụp tại request | Tái sử dụng để bảo vệ WS/Notification; không thay profile contract/JWT |

Test hiện có chứng minh một số đường notification persistence (COD checkout, Review commit/rollback), chưa có suite STOMP isolation/expiry/raw destination trong source đã tìm. Tài liệu workflow được rules tham chiếu `docs/MODULE_DEVELOPMENT_WORKFLOW.md` không tồn tại; áp dụng ARCHITECTURE và rules hiện có.

## 2. DECISION SET đề xuất để duyệt

### C1 — Phạm vi và payload

Push **notification mới thực sự được lưu** từ các producer Order/Review hiện có, đúng recipient hiện có. Không thêm loại thông báo, browser permission/service worker, email mới hoặc notification toàn Admin.

Destination subscribe: **/user/queue/notifications**. Server dùng recipient ID đã lưu với `convertAndSendToUser(userId.toString(), "/queue/notifications", dto)`. Payload là NotificationResponse hiện có: `id, type, title, message, referenceId, isRead, createdAt`; không thêm token/recipient do client chọn. Giữ LocalDateTime hiện tại, không tự gắn Z/đổi timezone dữ liệu cũ. Đồng bộ type FE với BE.

### C2 — Transaction và lỗi delivery

Gom tạo Notification vào service nội bộ module Notification tham gia transaction đang mở (MANDATORY). Listener Order/Review giữ transaction REQUIRES_NEW sau commit domain. Sau save, publish event nội bộ chứa recipient + DTO snapshot; consumer AFTER_COMMIT của **transaction Notification** mới gửi STOMP. Không gọi từ Order/Product/Payment sang Notification repository.

```text
Order/Review commit
  → listener mở transaction Notification
  → save Notification + đăng ký event push
  → Notification commit → push
                       rollback → không push
```

Lỗi push được log không chứa token, không đảo dữ liệu đã commit; REST vẫn đọc được. Lỗi lưu không được coi là delivery thành công. Không thêm outbox/retry broker/unique constraint. Process chết trước khi lưu Notification vẫn có thể mất notification; REST chỉ khôi phục được bản đã lưu. Không hứa exactly-once hoặc atomicity giữa domain commit và Notification commit.

### C3 — Authentication và destination authorization

Giữ handshake /ws công khai để browser gửi JWT trong **STOMP CONNECT Authorization: Bearer**; không đưa token vào URL. CONNECT bắt buộc JWT hợp lệ/chưa hết hạn, user tồn tại; ID claim phải khớp CurrentUserPrincipal từ UserDetails đã load. Principal name chỉ numeric userId, **bỏ fallback email**; token thiếu/sai ID bị từ chối. Không truy cập Identity entity/repository từ transport.

| Frame client | Quyền đề xuất |
|---|---|
| CONNECT | Xác thực như trên; không cho đổi principal bằng CONNECT lặp trong cùng session |
| SUBSCRIBE | Chỉ đúng `/user/queue/messages` và `/user/queue/notifications` của session đã auth |
| SEND | Chỉ `/app/chat.send`, sau đó Chat vẫn kiểm room ownership và input |
| Raw /queue/**, /topic/**, /user/{id}/**, destination khác, SEND giả notification | Từ chối mặc định, kể cả ADMIN |
| UNSUBSCRIBE / DISCONNECT / heartbeat | Cho cleanup của session; không tạo đường đọc/ghi nghiệp vụ |
| Transaction/ACK frames không được client hiện tại sử dụng | Từ chối thay vì mở quyền không cần thiết |

STOMP lỗi auth/quyền trả ERROR với mã máy đọc được (đề xuất UNAUTHORIZED/FORBIDDEN), không stack trace/token; đóng session vi phạm. HTTP tiếp tục envelope S1-02, không ép STOMP thành HTTP response. Kiểm thực tế interceptor ordering và user-destination translation bằng wire tests, không chỉ mock accessor.

### C4 — Token lifecycle và origin

Server lưu expiry đã xác minh theo session, từ chối inbound nghiệp vụ và chặn outbound dữ liệu khi hết hạn; chủ động đóng socket hết hạn bằng cơ chế lifecycle của transport. **Chỉ kiểm inbound là chưa đủ**, vì client đã subscribe có thể ngồi nhận mãi. Hủy timer/state khi socket đóng. Client ngừng reconnect token hết hạn; chỉ token/session mới được kết nối lại. Lỗi mạng không tự logout.

Logout phía app đóng socket của phiên trình duyệt, clear state và hủy pending callbacks. Không triển khai revoke JWT toàn thiết bị: token đã sao chép vẫn có hiệu lực đến expiry theo auth hiện tại. Không thêm policy khóa tài khoản/Shop mới; Shop BANNED vẫn nhận notification của đơn cũ.

Origin dùng allowlist cấu hình, mặc định local `http://localhost:5173` và `http://127.0.0.1:5173`; môi trường khác khai báo rõ và đồng bộ HTTP CORS. Native test client không Origin vẫn phải auth STOMP. FE URL cấu hình theo môi trường, hỗ trợ ws/wss; không thêm SockJS. Chưa có domain triển khai nên không tự đặt domain production.

### C5 — Một connection owner, session isolation

Một provider transport dưới AuthProvider quản lý socket cho cả Chat/Notification. Hai consumer chỉ đăng ký/hủy listener riêng; đóng dropdown hoặc ChatTestPage không đóng socket toàn app. Shared BE transport/security có thể chuyển về `common/websocket` để Notification không phụ thuộc Chat implementation.

Đổi token **hoặc auth generation**, kể cả login lại cùng ID/token, phải vô hiệu hóa callback cũ, deactivate connection cũ và tạo connection mới; chờ/guard async deactivate để callback close của client cũ không tác động client mới. Resubscribe cả hai destinations mỗi reconnect. F6 save đổi tên không làm reconnect.

HTTP Notification chụp token/generation ngay khi gọi như Profile; response cũ bị bỏ qua. Chat cũng cần clear rooms/messages/activeRoom và guard response/callback theo session trong phần lifecycle bị ảnh hưởng. Không refactor nghiệp vụ Chat hoặc thay room permissions.

### C6 — UI, reconcile và mark-read

Tập trung Notification state trong provider, dropdown chỉ hiển thị/thao tác. Push dedup theo ID trong cache có giới hạn; không cộng count vô điều kiện. Push kích hoạt HTTP reconcile count và list đang mở, gộp nhiều trigger gần nhau; payload mới không được biến bản đã read thành unread.

Reconcile khi login, reconnect sau khi đăng ký subscriptions, mở dropdown, tab trở lại foreground và sau PUT read/read-all. Trong khi GET đang chạy, push/mutation mới đánh dấu cần fetch lại; bỏ response generation/request cũ. Sau mark-read lấy count từ server, không mặc định read-all nghĩa count vĩnh viễn bằng 0 khi có notification mới cạnh tranh.

Dừng periodic polling khi connected ổn định theo plan; disconnected dùng fallback **60 giây** như hiện tại. Multi-tab read state sẽ hội tụ qua mở dropdown/focus/reconnect; chưa push riêng sự kiện read/read-all. REST list/count là hai request nên không hứa snapshot atomic; bảo đảm hội tụ sau khi mutation ngừng. Lỗi GET hiển thị retry, giữ dữ liệu hợp lệ đã có, không báo empty giả. Sort ổn định createdAt DESC + id DESC; validate page ≥ 0, size 1–100 tại REST, lỗi theo errorCode.

## 3. Các giai đoạn sau khi duyệt

1. **BE transport/security:** common config/interceptor/lifecycle, strict destination matrix, origin/expiry. Giữ Chat routing và kiểm tra wire authentication trước.
2. **BE Notification:** điểm persist/event sau commit, mapper DTO dùng chung, REST paging/error cần thiết. Không sửa Order inventory/Payment guard/Shop policy.
3. **FE:** shared connection owner, Notification provider/dropdown, token-generation guards và Chat session cleanup; giữ profile hydration.
4. **Tests:** MySQL + server port thật + STOMP clients độc lập; FE helper/session/race và browser hai phiên.
5. **Verification/review:** full configured backend regression, BE/FE build, FE tests/lint, Notification + Chat + Profile smoke; báo cáo `PHASE2_P1_03_NOTIFICATION_PUSH.md` với evidence thực chạy.

Phạm vi file/module cần thay: Notification; shared WS/security config đang ở Chat; frontend websocket/Auth session exposure/App/providers/useChat/dropdown/API types; cấu hình local và tests. Đây là dependency trực tiếp của F1. Chat push-before-commit, optimistic chat matching, outbox/domain-event dedup không tự đưa vào lượt này.

## 4. Acceptance tests bắt buộc

- Hai recipient và nhiều session cùng recipient: chỉ người đúng nhận; buyer/vendor nhận đúng producer; không tạo approval/cancellation notification mới.
- CONNECT thiếu/sai/expired/ID mismatch; raw queue kể cả tên queue đã resolve, user destination khác, wildcard subscribe và SEND giả bị chặn; token hợp lệ Chat send/echo hoạt động, non-member bị từ chối.
- Token hết hạn khi socket đang subscribe và không gửi frame: không nhận push mới; reconnect token hết hạn thất bại, token mới thành công. Origin hợp lệ/sai, logout/account switch/relogin cùng user.
- Domain rollback không tạo Notification; Notification rollback/save thất bại không push; commit có push với ID có thể đọc qua connection DB độc lập. Push failure không xóa DB; HTTP lấy lại notification khi reconnect.
- Duplicate delivery ID không nhân dòng/badge; push đối đầu GET/read/read-all; multi-tab/focus, fallback mất mạng, HTTP lỗi/retry; callback/HTTP cũ không ghi state hay logout phiên mới.
- Chat đang mở vẫn gửi/nhận khi dropdown mount/unmount và reconnect; session cũ không lộ rooms/messages. Profile save/reload token cũ vẫn đúng theo F6.

Chưa có số test dự kiến dùng làm evidence. Không chạy DB ứng dụng. Dự kiến không migration; nếu phát hiện cần constraint/schema phải báo lại. Rollback push giữ REST/persistence/fallback, deploy FE/BE transport đồng bộ và reconnect sessions; không tự quay lại cấu hình destination thiếu authorization.

**Cần duyệt C1–C6 trước implementation**, đặc biệt phạm vi chỉ push loại đã có, kiểm expiry cả outbound, chỉnh lifecycle Chat dùng chung và mức hội tụ read-state multi-tab. Đây là đề xuất kỹ thuật cụ thể, chưa phải quyết định người dùng đã chốt.
