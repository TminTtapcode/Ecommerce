# P1-01 / F3 — Audit và Decision Set D2

Ngày inspect: **2026-09-09**. **Người dùng đã duyệt Decision Set 1–7, lựa chọn A, và yêu cầu triển khai F3 theo từng giai đoạn BE/FE/test.** Phạm vi: Admin order status mutation theo [P1 Feature Plan](PHASE2_P1_FEATURE_PLAN.md). Nội dung phân tích dưới đây giữ bối cảnh inspect; báo cáo triển khai ghi riêng kết quả kiểm chứng mới.

## 1. Hiện trạng source và khác biệt với plan

| Chủ đề | Hành vi hiện tại đã đọc | Yêu cầu/điểm còn mở |
|---|---|---|
| Admin | [AdminOrderController](../src/main/java/com/tmt/ecommerce/order/controller/AdminOrderController.java) chỉ GET list/detail với ROLE_ADMIN; [adminApi](../frontend/src/api/adminApi.ts) và AdminOrderDetailPage chưa có mutation | F3 cần command riêng, D2 chưa xác định transition |
| Vendor | Controller nhận VENDOR/ADMIN nhưng service vẫn kiểm tra chủ shop. Matrix CONFIRMED → SHIPPED/CANCELLED; SHIPPED → DELIVERED | Không dùng endpoint này để giả lập Admin can thiệp shop khác |
| Hủy | [OrderServiceImpl](../src/main/java/com/tmt/ecommerce/order/service/OrderServiceImpl.java) khóa Order, kiểm tra matrix, hoàn kho đồng bộ; không kiểm tra Payment | Vendor hiện có thể hủy CONFIRMED VNPAY đã SUCCESS. F3 không thể được mô tả là đã có guard tài chính dùng chung |
| Kho | [ProductInternalServiceImpl](../src/main/java/com/tmt/ecommerce/product/service/ProductInternalServiceImpl.java) restore MANDATORY, validate và kiểm tra row count variant/product | Admin hủy phải dùng cùng invariant/transaction; không dựng listener hoàn kho thứ hai |
| COD | Checkout tự CONFIRMED và phát event, không tạo bằng chứng đã thu tiền COD | Không bắt COD phải có Payment SUCCESS; cũng không kết luận COD chắc chắn chưa thu tiền |
| VNPAY | [PaymentGatewayService](../src/main/java/com/tmt/ecommerce/payment/service/PaymentGatewayService.java) kiểm chữ ký/amount, khóa attempt, chỉ xử lý PENDING. SUCCESS và confirm các Order PENDING commit cùng transaction | CONFIRMED của đường IPN bình thường có SUCCESS; dữ liệu seed/lịch sử có thể không khớp, không tự sửa bằng Admin |
| Payment group | N Order chung group; nhiều attempt có thể SUCCESS. getPaymentGroupStatus ưu tiên bất kỳ SUCCESS, còn PENDING/FAILED lấy attempt gần nhất | Không dùng attempt mới nhất để phủ nhận SUCCESS cũ; SUCCESS group không phải refund/settlement hoặc phân bổ tiền riêng từng Order |
| Tạo attempt | PaymentController kiểm ownership/status group trước khi gọi generatePaymentUrl; không có group mutex chung với IPN/Admin | Khóa các Payment rows hiện có không chứng minh ngăn attempt mới. Đặc biệt không đủ cho guard “chưa có SUCCESS nên được hủy” |
| Reason/audit | Vendor DTO nhận reason nhưng service không đọc/lưu; log transition trước commit; Order không có history/actor/reason fields | F3 cần quyết định persistence, không được gọi log hiện tại là durable audit |
| Notification | Shipped/Delivered xử lý sau commit; cancellation listener chỉ log | Giữ events hiện có, không tuyên bố đã gửi notification hủy hoặc tự gộp F1 |

Source lock: [OrderRepository](../src/main/java/com/tmt/ecommerce/order/repository/OrderRepository.java) có findByIdForUpdate và group lock ORDER BY id; [PaymentRepository](../src/main/java/com/tmt/ecommerce/payment/repository/PaymentRepository.java) chỉ khóa theo transactionId. Chưa có Payment internal API; Order không được inject PaymentGatewayService/PaymentRepository. Nếu thêm read contract, implementation chỉ phụ thuộc dữ liệu Payment, tránh vòng bean OrderService → PaymentGatewayService → OrderService.

Các mô tả audit trước Sprint 1 về bulk-update hồi sinh CANCELLED/hoàn kho after-commit đã cũ. S1-03/04 đã xử lý; F3 phải giữ các bảo đảm đó.

## 2. D2-A — Quyền và transition đề xuất

**Khuyến nghị phạm vi đầu:** Admin xử lý mọi shop, gồm shop BANNED theo ngoại lệ đơn cũ D4; chỉ tiến trạng thái, không force/đưa lùi. Quyền được kiểm ở HTTP và public service command. Actor lấy từ principal, không nhận actorId/shopId tùy ý từ body.

| Trạng thái nguồn | Đích | COD | VNPAY | Side effects |
|---|---|---|---|---|
| PENDING | Mọi đích | Từ chối | Từ chối | Không thay đổi |
| CONFIRMED | SHIPPED | Cho phép theo trạng thái fulfillment | Chỉ khi có bằng chứng SUCCESS đã commit của group, method VNPAY | Event shipped; không đổi kho/tiền |
| CONFIRMED | CANCELLED | **Đề xuất cho phép** theo semantics hủy COD hiện có | **Đề xuất chưa mở trong F3** | Hoàn variant + product từ OrderItem, một transaction |
| SHIPPED | DELIVERED | Cho phép theo trạng thái fulfillment | Chỉ khi có bằng chứng SUCCESS đã commit của group, method VNPAY | Event delivered; không đổi kho/tiền |
| CANCELLED / DELIVERED | Mọi đích | Từ chối | Từ chối | Không thay đổi |
| Bất kỳ | Chính nó hoặc nhánh ngoài bảng | Từ chối | Từ chối | Không audit thành công/event/restore thêm |

Payment method không hỗ trợ/không xác định: từ chối command, cần đối soát; không tự diễn giải thành COD. DELIVERED tiếp tục có ý nghĩa hoàn tất hiện có, ảnh hưởng quyền review; Admin phải chịu trách nhiệm lý do can thiệp, không tự thêm proof/shipper của F11.

**Vì sao chưa mở PENDING:** không xác nhận thay gateway; không mở hủy unpaid khi attempt đang tồn tại có thể SUCCESS trễ và khi chưa có refund/reconciliation workflow. PENDING COD bất thường cũng không tự “sửa dữ liệu” trong F3.

**Lựa chọn cần người dùng xác nhận rõ nhất — hủy VNPAY:**

- **A, khuyến nghị cho F3:** Admin chỉ hủy COD CONFIRMED; VNPAY chưa có action hủy. Giảm việc mở thêm đường phát sinh nghĩa vụ hoàn tiền khi chưa có F10. Đây là giới hạn riêng cho command Admin, không phải policy chung đã tồn tại.
- **B, nếu chọn tính tương đồng Vendor:** Admin được hủy cả CONFIRMED VNPAY, kể cả SUCCESS; stock được hoàn nhưng Payment vẫn SUCCESS, tiền cần đối soát thủ công. Phải chấp nhận rõ nghĩa vụ này, không gọi hành động đó là refund.

Giữ nguyên Vendor theo Sprint 1 trong cả hai lựa chọn. Với A, Vendor vẫn có thể hủy VNPAY; **không thể tuyên bố toàn hệ thống đã chặn hủy đơn trả tiền**. Nếu muốn áp A cho Vendor nữa, cần quyết định thay đổi contract Sprint 1 và scope riêng. Với COD, cho phép hủy không chứng minh chưa thu tiền; chưa có collection ledger, khoản thực thu ngoài hệ thống vẫn cần xử lý vận hành.

## 3. D2-B — Payment guard và thứ tự khóa

Đề xuất internal contract đọc `PaymentEligibilityQuery` với DTO riêng: truy vấn bằng chứng **có Payment SUCCESS, method VNPAY, đúng paymentGroupId**, không dùng public controller hoặc endpoint status làm internal API. Group null/rỗng, không có SUCCESS, chỉ FAILED/PENDING hoặc method sai: từ chối ship/deliver VNPAY. Không dùng dữ liệu client làm bằng chứng tiền.

Guard này là bằng chứng dương tính theo semantics Payment hiện có, không phải đối soát gateway mới, kiểm tra đủ mọi nghĩa vụ tiền hoặc xác nhận merchant settlement. Nhiều SUCCESS cùng group không làm lặp fulfillment; không lấy tổng nhiều attempts để tính lại Order amount.

**Đề xuất không lấy Payment lock trong command Admin** với matrix A:

```text
Admin command transaction
  → khóa Order PESSIMISTIC_WRITE
  → kiểm expectedStatus, matrix, paymentMethod
  → nếu ship/deliver VNPAY: đọc SUCCESS đã commit qua Payment read contract
       (SELECT thường, không FOR UPDATE/SHARE, không sửa Payment)
  → nếu cancel COD: restore từng variant ID tăng dần → product tương ứng
  → cập nhật Order + INSERT history
  → commit; notification/log sau commit

IPN giữ nguyên
  → khóa Payment attempt → khóa group Orders theo ID → commit

Vendor giữ nguyên
  → khóa Order → nếu cancel: variant → product → commit
```

**Cơ sở:** trong source hiện tại SUCCESS là terminal, không có đường đổi SUCCESS sang FAILED/refunded. Một SUCCESS đã commit không bị attempt mới PENDING/FAILED phủ nhận. Do đó guard “đã có SUCCESS” có thể đọc không khóa; không cần guard “không có SUCCESS” vốn dễ bị race. Nếu refund/status semantics thay đổi ở F10 thì phải review lại điều kiện này.

MySQL consistent read có thể chưa thấy SUCCESS mới nhất tùy snapshot: fail closed và cho client refetch/retry, không cho phép nhờ đọc trạng thái chưa commit. Integration phải kiểm tra đúng isolation thực tế. Admin đang giữ Order không được chờ Payment khóa bởi IPN; nếu không thấy bằng chứng đã commit thì kết thúc bằng lỗi, thả Order cho IPN tiếp tục.

Không chứng minh hết deadlock của các mutation stock khác; giữ giới hạn S1-03. Nếu muốn matrix phức tạp hơn cần cả đọc/sửa/khóa Payment, phải thiết kế coordinator với thứ tự **Payment → Order**, đồng thời xử lý attempt creation; không thêm FOR UPDATE Payment sau khi đã giữ Order. Khóa một attempt hoặc tất cả attempt hiện tại không thay thế group mutex mà mọi writer đều tham gia.

## 4. D2-C — Reason và audit đề xuất

- **Reason bắt buộc cho mọi mutation Admin**, trim, không rỗng, tối đa **500 ký tự sau trim**. Đây là đề xuất mới cho D2, không suy từ Ban. Actor/time do server xác định.
- History bền vững do **Order module** sở hữu, bảng đề xuất `order_admin_status_history`: id, order_id, actor_user_id, old_status, new_status, reason, created_at. Scalar actor ID, không tham chiếu Identity entity/repository hoặc FK xuyên module; order reference ở cùng module.
- INSERT history **cùng transaction với Order và stock**. History insert lỗi phải rollback status/stock; restore lỗi không để lại history thành công. Chỉ lưu can thiệp Admin từ khi triển khai; không gọi đây là timeline đầy đủ của Vendor/IPN/lịch sử.
- Đề xuất timestamp UTC theo clock server, contract trả thời gian rõ timezone. Không backfill lý do/actor giả cho đơn cũ. History append-only ở API, không cung cấp sửa/xóa history trong F3.
- Log có cấu trúc sau commit gồm historyId/orderId/actor/old/new; không bắt buộc nhân bản reason vào log. History là evidence chính; log không thay thế DB. Request lỗi bị từ chối không tạo history chuyển trạng thái thành công.
- Admin detail có thể xem history qua endpoint phân trang chỉ ADMIN; buyer/vendor không được đọc lý do nội bộ qua OrderResponse chung.

## 5. D2-D — API, stale UI và lỗi đề xuất

```text
PATCH /api/v1/admin/orders/{orderId}/status
{ "expectedStatus": "CONFIRMED", "status": "SHIPPED", "reason": "..." }
```

`expectedStatus` bắt buộc, chỉ để phát hiện màn hình cũ, không thay thế đọc trạng thái/khóa DB. Sau khi khóa, mismatch trả conflict và không thực hiện command. Điều này ngăn hai thao tác có thể hợp lệ nối tiếp nhưng được gửi từ cùng snapshot cũ. Không nhận `force`, paymentStatus hoặc actorId làm input quyền lực.

Đề xuất GET Admin detail thêm dữ liệu action bằng DTO riêng/phần mở rộng dành Admin: `allowedTransitions` và mã lý do không được thao tác nếu cần. Backend dùng cùng policy evaluator cho GET và PATCH nhưng GET chỉ gợi ý tại thời điểm đọc; PATCH luôn kiểm tra lại trong transaction. Không sửa enum rồi để FE cho chọn mọi trạng thái.

Đề xuất GET `/api/v1/admin/orders/{orderId}/status-history` phân trang; index order_id + id phục vụ lịch sử, stable sort. PATCH trả Order detail cập nhật theo envelope S1-02. FE disable khi submit, hiển thị lý do bắt buộc và lịch sử Admin, refetch detail/actions/history sau thành công hoặc conflict; lỗi theo errorCode.

| Tình huống | Contract lỗi đề xuất |
|---|---|
| Không đăng nhập / không phải Admin | 401 / 403, envelope security hiện có |
| Không có Order | 404 ORDER_NOT_FOUND |
| Body/enum/reason/expectedStatus thiếu hoặc sai | 400 theo validation/error contract hiện có |
| expectedStatus không còn khớp | 409 ORDER_STATE_CONFLICT — mã mới |
| Nhánh không thuộc matrix, bao gồm self-transition | 400 INVALID_ORDER_STATE_TRANSITION — giữ mã hiện có |
| Matrix hợp lệ nhưng không thỏa payment policy | 409 ORDER_PAYMENT_CONDITION_NOT_MET — mã mới |
| Restore/audit DB lỗi | Domain/system error tương ứng, rollback toàn transaction |

Kiểm tra theo thứ tự role → body → Order tồn tại → expectedStatus → matrix → payment policy. Duplicate với expectedStatus cũ nhận 409; gửi lại cùng trạng thái hiện tại nhận invalid transition. Không có idempotency key hoặc hứa trả success cho replay trong F3.

## 6. Ảnh hưởng và kiểm chứng bắt buộc khi implement

| Module | Thay đổi dự kiến / bảo đảm giữ lại |
|---|---|
| Order | Admin command/policy/history/DTO; dùng chung phần transition side effects với Vendor để không lệch restore/events, giữ policy Vendor riêng |
| Product | Tái dùng restoreStock MANDATORY; không thêm stock listener/đổi quantity theo body |
| Payment | Thêm read contract/implementation độc lập; giữ IPN và attempt statuses, không refund/khóa Payment từ Admin |
| Shop | Admin xử lý đơn cũ kể cả BANNED; không ban eligibility guard cho mutation đơn cũ |
| Cart/Voucher | Không trả item về Cart, không hoàn lượt voucher, không đổi discount/total snapshot |
| Notification/Review | Giữ shipped/delivered events và review eligibility; không tự thêm cancellation notification hoặc realtime |
| Frontend | Admin action/reason/history/policy errors; không mở thay đổi quyền cho Vendor UI |
| Tests | Test mới cho Admin, giữ regression S1-03/04/05 và payment amount/security |

Các case acceptance cần có (chưa chạy trong lượt này):

1. Toàn matrix COD/VNPAY/unknown; role thực HTTP/service; Admin khác shop và BANNED; reason trim/blank/limit; actor lấy từ principal.
2. VNPAY: không attempt, chỉ FAILED/PENDING, SUCCESS cũ + attempt mới FAILED/PENDING, nhiều SUCCESS, SUCCESS group/method khác; guard không nhận dữ liệu tiền client.
3. MySQL: hai Admin cùng snapshot; Admin cancel vs Vendor ship/cancel; stale SHIPPED → DELIVERED; IPN giữ Payment chờ Order và Admin giữ Order đọc Payment **không tạo vòng khóa**; cả hai thứ tự commit.
4. Nếu chọn A, Admin VNPAY cancellation bị từ chối cả khi IPN trễ; các regression Vendor cancel/IPN vẫn giữ nguyên. COD cancellation không bị IPN sai group làm đổi semantics.
5. Cancel COD nhiều variants → stock chính xác; variant thứ hai/row count/DB lỗi → rollback; history insert lỗi → rollback stock/status; caller rollback → không history/event sau commit. Duplicate không restore/history thêm.
6. History chỉ ADMIN, paging đúng, không PII thừa; legacy Order không có history vẫn đọc được. Browser action/reason/conflict/history; frontend API errorCode, build/lint.

Migration additive chỉ bảng history/index sau duyệt; xác minh trên schema fixture riêng, không dùng ddl-auto làm bằng chứng migration. Rollback dừng command Admin mới, giữ history và mọi trạng thái đã commit; không rollback bằng đưa Order lùi hoặc bật listener hoàn kho cũ.

## 7. DECISION SET D2 — bản đề nghị duyệt

1. **Admin mọi shop, chỉ ba nhánh fulfillment trong bảng; không mở PENDING hoặc trạng thái cuối; không force.**
2. **Chọn A:** Admin hủy **COD CONFIRMED**; chưa mở hủy VNPAY. Giữ Vendor policy Sprint 1, công bố khác biệt và giới hạn COD collection.
3. **Ship/deliver VNPAY cần SUCCESS đã commit, đúng group/method; COD không cần Payment record.** Không sửa tiền hoặc coi state là refund.
4. **Order lock + payment read không khóa; IPN Payment → Order giữ nguyên.** Phụ thuộc tính terminal của SUCCESS hiện có, phải review lại khi thêm refund.
5. **Reason bắt buộc mọi mutation, trim 1–500 ký tự; history Admin bền vững cùng transaction**, actor server, log sau commit.
6. **expectedStatus bắt buộc**, mismatch 409; backend cung cấp action eligibility nhưng luôn revalidate PATCH; duplicate không thành công lại.
7. **Giữ nguyên D1/D3/D4/S1-04**, events hiện có; không refund, voucher restore, Cart restore, history backfill hoặc sửa Vendor ngoài phạm vi.

Các mục 1–7, lựa chọn A, đã được user xác nhận để triển khai F3. Nếu đổi sang B hoặc mở thêm transition về sau, phải cập nhật matrix/payment/race tests trước code; không chỉ thêm nút Admin.
