import React, { useState, useEffect, useRef } from 'react';
import { useAuth } from '../../contexts/AuthContext';
import { useChat } from '../../hooks/useChat';
import { websocketService } from '../../services/websocket';

export const ChatTestPage: React.FC = () => {
  const { user } = useAuth();
  const {
    isConnected,
    rooms,
    activeRoomId,
    setActiveRoomId,
    messages,
    isLoadingRooms,
    isLoadingMessages,
    loadRooms,
    sendMessage,
    openChatWithShop,
    currentUserId,
  } = useChat();

  const [targetShopId, setTargetShopId] = useState('1');
  const [inputText, setInputText] = useState('');
  const [eventLogs, setEventLogs] = useState<string[]>([]);
  const messagesEndRef = useRef<HTMLDivElement>(null);

  useEffect(() => {
    messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
  }, [messages]);

  useEffect(() => {
    const unsub = websocketService.subscribe((msg) => {
      addLog(`[STOMP MESSAGE IN] Room ${msg.roomId} | From: User #${msg.senderId} | "${msg.content}"`);
    });
    return () => unsub();
  }, []);

  const addLog = (log: string) => {
    const time = new Date().toLocaleTimeString();
    setEventLogs((prev) => [`[${time}] ${log}`, ...prev.slice(0, 49)]);
  };

  const handleOpenRoom = async (e: React.FormEvent) => {
    e.preventDefault();
    const shopId = parseInt(targetShopId, 10);
    if (!shopId) return;
    addLog(`[ACTION] Tạo/Mở phòng chat với Shop ID #${shopId}...`);
    const room = await openChatWithShop(shopId);
    if (room) {
      addLog(`[SUCCESS] Đã vào phòng Chat Room #${room.id} (Shop #${room.shopId}, Buyer #${room.buyerId})`);
    } else {
      addLog(`[ERROR] Không thể tạo/mở phòng chat với Shop #${shopId}`);
    }
  };

  const handleSend = (e: React.FormEvent) => {
    e.preventDefault();
    if (!inputText.trim() || !activeRoomId) return;

    addLog(`[ACTION] Gửi tin nhắn đến Room #${activeRoomId}: "${inputText}"`);
    const ok = sendMessage(inputText);
    if (ok) {
      addLog(`[STOMP SEND OK] Đã gửi frame /app/chat.send thành công`);
      setInputText('');
    } else {
      addLog(`[STOMP SEND FAIL] Gửi thất bại - WebSocket chưa kết nối`);
    }
  };

  return (
    <div className="max-w-6xl mx-auto p-4 sm:p-6 space-y-6">
      <div className="bg-white rounded-lg shadow-sm border p-6 flex flex-col md:flex-row md:items-center justify-between gap-4">
        <div>
          <div className="flex items-center gap-3">
            <h1 className="text-2xl font-bold text-gray-800">WebSocket Realtime Chat Test Console</h1>
            <span
              className={`inline-flex items-center px-3 py-1 rounded-full text-xs font-semibold ${
                isConnected ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700 animate-pulse'
              }`}
            >
              <span className={`w-2 h-2 rounded-full mr-1.5 ${isConnected ? 'bg-green-500' : 'bg-red-500'}`} />
              {isConnected ? 'STOMP: Connected' : 'STOMP: Disconnected'}
            </span>
          </div>
          <p className="text-sm text-gray-500 mt-1">
            Trang kiểm thử dòng dữ liệu WebSocket STOMP hai chiều (Buyer ↔ Vendor)
          </p>
        </div>

        <div className="text-sm bg-gray-50 p-3 rounded border text-gray-700">
          <div>
            <span className="font-semibold">User ID:</span> {currentUserId || 'Chưa đăng nhập'}
          </div>
          <div>
            <span className="font-semibold">Email:</span> {user?.sub || 'N/A'}
          </div>
          <div>
            <span className="font-semibold">Roles:</span> {user?.roles?.join(', ') || 'None'}
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white rounded-lg shadow-sm border p-4 space-y-4 flex flex-col h-[600px]">
          <div>
            <h2 className="font-semibold text-gray-800 mb-2">1. Mở Chat với Shop</h2>
            <form onSubmit={handleOpenRoom} className="flex gap-2">
              <input
                type="number"
                min="1"
                value={targetShopId}
                onChange={(e) => setTargetShopId(e.target.value)}
                placeholder="Shop ID (vd: 1)"
                className="w-full border rounded px-3 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500"
              />
              <button
                type="submit"
                className="bg-orange-500 hover:bg-orange-600 text-white text-sm px-3 py-1.5 rounded whitespace-nowrap font-medium"
              >
                Vào Chat
              </button>
            </form>
          </div>

          <div className="flex-1 flex flex-col min-h-0 border-t pt-3">
            <div className="flex justify-between items-center mb-2">
              <h2 className="font-semibold text-gray-800">2. Danh sách phòng ({rooms.length})</h2>
              <button
                onClick={loadRooms}
                className="text-xs text-orange-500 hover:underline"
                disabled={isLoadingRooms}
              >
                Làm mới
              </button>
            </div>

            <div className="flex-1 overflow-y-auto divide-y divide-gray-100">
              {isLoadingRooms ? (
                <div className="p-4 text-center text-sm text-gray-400">Đang tải phòng...</div>
              ) : rooms.length === 0 ? (
                <div className="p-4 text-center text-sm text-gray-400">Chưa có phòng chat nào. Hãy nhập Shop ID ở trên để tạo.</div>
              ) : (
                rooms.map((r) => {
                  const isActive = r.id === activeRoomId;
                  return (
                    <div
                      key={r.id}
                      onClick={() => {
                        setActiveRoomId(r.id);
                        addLog(`[ACTION] Chọn phòng chat Room #${r.id}`);
                      }}
                      className={`p-3 cursor-pointer rounded transition-colors ${
                        isActive ? 'bg-orange-50 border-l-4 border-orange-500' : 'hover:bg-gray-50'
                      }`}
                    >
                      <div className="flex justify-between items-start">
                        <span className="font-medium text-sm text-gray-800">
                          Room #{r.id} (Shop #{r.shopId})
                        </span>
                        {r.unreadCount ? (
                          <span className="bg-red-500 text-white text-xs px-1.5 py-0.5 rounded-full font-bold">
                            {r.unreadCount}
                          </span>
                        ) : null}
                      </div>
                      <div className="text-xs text-gray-500 truncate mt-1">
                        {r.lastMessage || 'Chưa có tin nhắn'}
                      </div>
                      <div className="text-[10px] text-gray-400 mt-0.5">
                        Buyer ID: {r.buyerId}
                      </div>
                    </div>
                  );
                })
              )}
            </div>
          </div>
        </div>

        <div className="bg-white rounded-lg shadow-sm border p-4 flex flex-col h-[600px]">
          <div className="border-b pb-3 mb-3 flex justify-between items-center">
            <div>
              <h2 className="font-semibold text-gray-800">
                {activeRoomId ? `3. Hội thoại Room #${activeRoomId}` : '3. Hộp thoại Chat'}
              </h2>
              <p className="text-xs text-gray-500">
                {activeRoomId
                  ? `Đang kết nối realtime với Room #${activeRoomId}`
                  : 'Vui lòng chọn hoặc mở một phòng để trò chuyện'}
              </p>
            </div>
          </div>

          <div className="flex-1 overflow-y-auto space-y-3 p-2 bg-gray-50 rounded mb-3">
            {isLoadingMessages ? (
              <div className="text-center text-sm text-gray-400 p-8">Đang tải tin nhắn...</div>
            ) : !activeRoomId ? (
              <div className="text-center text-sm text-gray-400 p-8">Chưa chọn phòng chat</div>
            ) : messages.length === 0 ? (
              <div className="text-center text-sm text-gray-400 p-8">Chưa có tin nhắn nào trong phòng này. Hãy gửi tin nhắn đầu tiên!</div>
            ) : (
              messages.map((m) => {
                const isMe = m.senderId === currentUserId;
                return (
                  <div key={m.id} className={`flex flex-col ${isMe ? 'items-end' : 'items-start'}`}>
                    <div className="text-[10px] text-gray-400 mb-0.5 px-1">
                      {isMe ? 'Tôi' : `User #${m.senderId}`} • {new Date(m.createdAt).toLocaleTimeString()}
                    </div>
                    <div
                      className={`max-w-[80%] rounded-lg px-3 py-2 text-sm break-words ${
                        isMe
                          ? 'bg-orange-500 text-white rounded-br-none'
                          : 'bg-white text-gray-800 border rounded-bl-none shadow-sm'
                      }`}
                    >
                      {m.content}
                    </div>
                  </div>
                );
              })
            )}
            <div ref={messagesEndRef} />
          </div>

          <form onSubmit={handleSend} className="flex gap-2">
            <input
              type="text"
              value={inputText}
              onChange={(e) => setInputText(e.target.value)}
              placeholder={activeRoomId ? 'Nhập tin nhắn test STOMP...' : 'Vui lòng chọn phòng trước'}
              disabled={!activeRoomId || !isConnected}
              className="flex-1 border rounded px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-orange-500 disabled:bg-gray-100"
            />
            <button
              type="submit"
              disabled={!activeRoomId || !isConnected || !inputText.trim()}
              className="bg-orange-500 hover:bg-orange-600 disabled:opacity-50 text-white text-sm px-4 py-2 rounded font-medium transition-colors"
            >
              Gửi
            </button>
          </form>
        </div>

        <div className="bg-gray-900 rounded-lg shadow-sm border border-gray-800 p-4 flex flex-col h-[600px] text-gray-200">
          <div className="flex justify-between items-center border-b border-gray-800 pb-2 mb-2">
            <h2 className="font-mono text-sm font-semibold text-green-400">4. STOMP Event Log</h2>
            <button
              onClick={() => setEventLogs([])}
              className="text-xs text-gray-400 hover:text-white"
            >
              Xóa log
            </button>
          </div>
          <div className="flex-1 overflow-y-auto font-mono text-[11px] space-y-1.5 leading-relaxed">
            {eventLogs.length === 0 ? (
              <div className="text-gray-600">Chưa có sự kiện nào được ghi lại...</div>
            ) : (
              eventLogs.map((log, idx) => (
                <div
                  key={idx}
                  className={
                    log.includes('ERROR') || log.includes('FAIL')
                      ? 'text-red-400'
                      : log.includes('MESSAGE IN')
                      ? 'text-yellow-300'
                      : log.includes('SUCCESS') || log.includes('OK')
                      ? 'text-green-300'
                      : 'text-gray-300'
                  }
                >
                  {log}
                </div>
              ))
            )}
          </div>
        </div>
      </div>
    </div>
  );
};
