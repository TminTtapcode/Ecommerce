# BÁO CÁO KIỂM TOÁN MÔ ĐUN: CHAT (TƯƠNG TÁC)
# ĐỒ ÁN NGÀNH — Enterprise Ecommerce

## 1. TỔNG QUAN MÔ ĐUN
Mô đun Chat xây dựng hệ thống nhắn tin P2P (Peer to Peer) thời gian thực giữa Khách hàng và Chủ Shop, giúp tăng tỷ lệ chuyển đổi mua hàng và giải quyết khiếu nại.

## 2. TRÁCH NHIỆM MÔ ĐUN
- Khởi tạo phòng chat (Chat Room) giữa 2 người dùng.
- Lưu trữ nội dung tin nhắn.
- Truyền tải tin nhắn tức thời qua giao thức WebSocket (STOMP Protocol).

## 3. THỰC THỂ (ENTITIES)
- `ChatRoom`: Lưu `buyer_id` và `shop_id`. Ràng buộc `UNIQUE(buyer_id, shop_id)` để đảm bảo 1 cặp Buyer-Shop chỉ có 1 phòng chat chung duy nhất xuyên suốt quá trình kinh doanh.
- `ChatMessage`: Chứa nội dung, `sender_id`.

## 4. CẤU TRÚC GIAO TIẾP WEBSOCKET
- Endpoint kết nối: `/ws/chat`.
- Cấu hình Spring WebSocket Message Broker (`@EnableWebSocketMessageBroker`).
- Client (Frontend React) đăng ký theo dõi (Subscribe) một Kênh (Channel) riêng biệt, ví dụ: `/topic/chat/{roomId}`.
- Khi một Client gửi tin nhắn qua `MessageMapping`, Backend lưu xuống Database và dùng `SimpMessagingTemplate` để phát chùm (Broadcast) tin nhắn đó lại cho tất cả những người đang Subscribe channel.

## 5. OPTIMISTIC UI TRÊN FRONTEND
Trên giao diện Frontend React, hệ thống áp dụng kỹ thuật **Optimistic UI**. Ngay khi người dùng nhấn "Gửi", tin nhắn lập tức hiện lên màn hình của chính họ với trạng thái "Đang gửi...", đồng thời tiến hành Emit WebSocket ngầm. Khi Server xác nhận lưu thành công, trạng thái chữ mờ sẽ chuyển sang rõ. Kỹ thuật này che lấp hoàn toàn độ trễ mạng (Latency), đem lại trải nghiệm mượt mà như các ứng dụng chat native.
