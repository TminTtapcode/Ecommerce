import React, { useState, useRef, useEffect } from 'react';
import { useChatContext } from '../../contexts/ChatContext';
import { useAuth } from '../../contexts/AuthContext';
import { useNavigate } from 'react-router-dom';
import {
  MessageSquare,
  X,
  Minus,
  Maximize2,
  Search,
  Send,
  Smile,
  ImageIcon,
  AlertCircle,
  Store,
  CheckCheck,
} from 'lucide-react';

export const FloatingChatWidget: React.FC = () => {
  const { isAuthenticated } = useAuth();
  const navigate = useNavigate();
  const {
    isOpen,
    setIsOpen,
    toggleChat,
    rooms,
    activeRoomId,
    setActiveRoomId,
    activeRoom,
    messages,
    sendMessage,
    isLoadingRooms,
    isLoadingMessages,
    totalUnreadCount,
    currentUserId,
  } = useChatContext();

  const [inputContent, setInputContent] = useState('');
  const [searchQuery, setSearchQuery] = useState('');
  const [filterType, setFilterType] = useState<'all' | 'unread'>('all');
  const messagesEndRef = useRef<HTMLDivElement>(null);
  const inputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    if (activeRoomId && messages.length > 0) {
      messagesEndRef.current?.scrollIntoView({ behavior: 'smooth' });
    }
  }, [messages, activeRoomId]);

  useEffect(() => {
    if (activeRoomId) {
      inputRef.current?.focus();
    }
  }, [activeRoomId]);

  const handleSendMessage = (e?: React.FormEvent) => {
    if (e) e.preventDefault();
    if (!inputContent.trim() || !activeRoomId) return;
    const sent = sendMessage(inputContent);
    if (sent) {
      setInputContent('');
    }
  };

  const handleKeyDown = (e: React.KeyboardEvent<HTMLInputElement>) => {
    if (e.key === 'Enter' && !e.shiftKey) {
      e.preventDefault();
      handleSendMessage();
    }
  };

  const filteredRooms = rooms.filter((r) => {
    const nameMatch = (r.shopName || `Shop #${r.shopId}`)
      .toLowerCase()
      .includes(searchQuery.toLowerCase());
    if (filterType === 'unread') {
      return nameMatch && (r.unreadCount ?? 0) > 0;
    }
    return nameMatch;
  });

  const formatTime = (dateStr?: string | null) => {
    if (!dateStr) return '';
    try {
      const date = new Date(dateStr);
      const now = new Date();
      const isToday = date.toDateString() === now.toDateString();
      if (isToday) {
        return date.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
      }
      return `${date.getDate().toString().padStart(2, '0')}/${(date.getMonth() + 1)
        .toString()
        .padStart(2, '0')}`;
    } catch {
      return '';
    }
  };

  const handleWidgetClick = () => {
    if (!isAuthenticated) {
      navigate('/login');
      return;
    }
    toggleChat();
  };

  return (
    <div className="fixed bottom-0 right-6 z-50 select-none font-sans">
      {!isOpen && (
        <button
          onClick={handleWidgetClick}
          className="flex items-center gap-2 bg-[#ee4d2d] hover:bg-[#d73211] text-white px-5 py-2.5 rounded-t-xl shadow-xl transition-all duration-200 transform hover:-translate-y-0.5 active:translate-y-0 cursor-pointer"
        >
          <div className="relative">
            <MessageSquare className="w-5 h-5 fill-current" />
            {totalUnreadCount > 0 && (
              <span className="absolute -top-2 -right-2.5 bg-yellow-400 text-[#ee4d2d] font-bold text-[11px] px-1.5 py-0.2 rounded-full ring-2 ring-[#ee4d2d]">
                {totalUnreadCount > 99 ? '99+' : totalUnreadCount}
              </span>
            )}
          </div>
          <span className="font-semibold text-sm tracking-wide">Chat</span>
        </button>
      )}

      {isOpen && (
        <div className="w-[720px] max-w-[calc(100vw-32px)] h-[540px] max-h-[calc(100vh-80px)] bg-white rounded-t-2xl shadow-2xl border border-gray-200 flex flex-col overflow-hidden animate-in fade-in slide-in-from-bottom-4 duration-200">
          <div className="bg-[#fffbe6] border-b border-[#ffe58f] px-3.5 py-1.5 flex items-center gap-2 text-xs text-[#d48806] shrink-0">
            <AlertCircle className="w-3.5 h-3.5 text-[#faad14] shrink-0" />
            <span className="truncate">
              Từ 14/9, tin nhắn với vai trò Người bán sẽ không còn hiển thị tại đây. Vui lòng chat với Người mua trên Kênh Người Bán
            </span>
          </div>

          <div className="flex items-center justify-between px-4 py-2.5 border-b border-gray-100 bg-white shrink-0">
            <div className="flex items-center gap-2">
              <span className="text-xl font-bold text-[#ee4d2d] tracking-tight">Chat</span>
              {totalUnreadCount > 0 && (
                <span className="bg-[#ee4d2d] text-white text-[11px] font-bold px-2 py-0.5 rounded-full">
                  {totalUnreadCount}
                </span>
              )}
            </div>
            <div className="flex items-center gap-1 text-gray-500">
              <button
                type="button"
                onClick={() => {}}
                title="Mở rộng"
                className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
              >
                <Maximize2 className="w-3.5 h-3.5" />
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                title="Thu nhỏ"
                className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
              >
                <Minus className="w-4 h-4" />
              </button>
              <button
                type="button"
                onClick={() => setIsOpen(false)}
                title="Đóng"
                className="p-1.5 hover:bg-gray-100 rounded-lg transition-colors cursor-pointer"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          </div>

          <div className="flex-1 flex overflow-hidden">
            <div className="w-[280px] border-r border-gray-100 flex flex-col bg-white shrink-0">
              <div className="p-2.5 border-b border-gray-100 flex items-center gap-2">
                <div className="relative flex-1">
                  <Search className="w-3.5 h-3.5 absolute left-2.5 top-1/2 -translate-y-1/2 text-gray-400" />
                  <input
                    type="text"
                    placeholder="Tìm theo tên..."
                    value={searchQuery}
                    onChange={(e) => setSearchQuery(e.target.value)}
                    className="w-full pl-8 pr-2 py-1.5 text-xs bg-gray-100 border-none rounded-lg focus:outline-none focus:ring-1 focus:ring-[#ee4d2d] text-gray-800 placeholder-gray-400"
                  />
                </div>
                <select
                  value={filterType}
                  onChange={(e) => setFilterType(e.target.value as 'all' | 'unread')}
                  className="text-xs bg-transparent text-gray-600 border border-gray-200 rounded-lg px-2 py-1.5 focus:outline-none cursor-pointer"
                >
                  <option value="all">Tất cả</option>
                  <option value="unread">Chưa đọc</option>
                </select>
              </div>

              <div className="flex-1 overflow-y-auto divide-y divide-gray-50">
                {isLoadingRooms ? (
                  <div className="p-4 text-center text-xs text-gray-400">Đang tải cuộc trò chuyện...</div>
                ) : filteredRooms.length === 0 ? (
                  <div className="p-8 text-center text-xs text-gray-400 flex flex-col items-center gap-2">
                    <MessageSquare className="w-8 h-8 text-gray-200 stroke-1" />
                    <span>Không tìm thấy cuộc trò chuyện</span>
                  </div>
                ) : (
                  filteredRooms.map((room) => {
                    const isSelected = room.id === activeRoomId;
                    const displayName = room.shopName || `Shop #${room.shopId}`;
                    return (
                      <div
                        key={room.id}
                        onClick={() => setActiveRoomId(room.id)}
                        className={`px-3 py-2.5 flex items-center gap-3 cursor-pointer transition-colors ${
                          isSelected
                            ? 'bg-orange-50/80 border-l-3 border-[#ee4d2d]'
                            : 'hover:bg-gray-50'
                        }`}
                      >
                        <div className="w-10 h-10 rounded-full bg-gradient-to-tr from-orange-400 to-red-500 text-white flex items-center justify-center font-bold text-sm shrink-0 shadow-sm">
                          {displayName.charAt(0).toUpperCase()}
                        </div>

                        <div className="flex-1 min-w-0">
                          <div className="flex items-center justify-between">
                            <span
                              className={`text-xs truncate ${
                                isSelected ? 'font-bold text-[#ee4d2d]' : 'font-semibold text-gray-800'
                              }`}
                            >
                              {displayName}
                            </span>
                            <span className="text-[10px] text-gray-400 shrink-0 ml-1">
                              {formatTime(room.lastMessageAt || room.updatedAt)}
                            </span>
                          </div>
                          <div className="flex items-center justify-between mt-1">
                            <p className="text-[11px] text-gray-500 truncate max-w-[160px]">
                              {room.lastMessage || 'Bắt đầu cuộc trò chuyện'}
                            </p>
                            {(room.unreadCount ?? 0) > 0 && (
                              <span className="bg-[#ee4d2d] text-white text-[10px] font-bold rounded-full h-4 min-w-[16px] px-1 flex items-center justify-center shrink-0">
                                {room.unreadCount}
                              </span>
                            )}
                          </div>
                        </div>
                      </div>
                    );
                  })
                )}
              </div>
            </div>

            <div className="flex-1 flex flex-col bg-[#F9FAFB] overflow-hidden">
              {!activeRoom ? (

                <div className="flex-1 flex flex-col items-center justify-center p-6 text-center">
                  <div className="relative mb-6">
                    <svg
                      className="w-48 h-36 text-gray-300"
                      viewBox="0 0 200 150"
                      fill="none"
                      xmlns="http://www.w3.org/2000/svg"
                    >
                      <rect
                        x="30"
                        y="20"
                        width="140"
                        height="92"
                        rx="8"
                        fill="#F3F4F6"
                        stroke="#9CA3AF"
                        strokeWidth="3"
                      />
                      <rect x="40" y="30" width="120" height="72" rx="4" fill="white" />
                      <rect x="48" y="38" width="54" height="26" rx="2" fill="#3B82F6" />
                      <line x1="54" y1="46" x2="80" y2="46" stroke="white" strokeWidth="2" strokeLinecap="round" />
                      <line x1="54" y1="52" x2="90" y2="52" stroke="white" strokeWidth="2" strokeLinecap="round" />
                      <line x1="48" y1="72" x2="110" y2="72" stroke="#E5E7EB" strokeWidth="3" strokeLinecap="round" />
                      <line x1="48" y1="80" x2="95" y2="80" stroke="#E5E7EB" strokeWidth="3" strokeLinecap="round" />
                      <line x1="48" y1="88" x2="105" y2="88" stroke="#E5E7EB" strokeWidth="3" strokeLinecap="round" />
                      <path
                        d="M15 116C15 113.791 16.7909 112 19 112H181C183.209 112 185 113.791 185 116V120C185 122.209 183.209 124 181 124H19C16.7909 124 15 122.209 15 120V116Z"
                        fill="#D1D5DB"
                        stroke="#9CA3AF"
                        strokeWidth="2"
                      />
                      <rect x="85" y="112" width="30" height="4" rx="2" fill="#9CA3AF" />
                    </svg>

                    <div className="absolute top-10 right-4 bg-[#ee4d2d] text-white rounded-xl rounded-bl-none px-2.5 py-1.5 shadow-lg flex items-center gap-1 animate-bounce">
                      <div className="w-1.5 h-1.5 rounded-full bg-white opacity-80" />
                      <div className="w-1.5 h-1.5 rounded-full bg-white opacity-80" />
                      <div className="w-1.5 h-1.5 rounded-full bg-white opacity-80" />
                    </div>
                  </div>

                  <h3 className="text-base font-bold text-gray-800">
                    Chào mừng bạn đến với Shopee Chat
                  </h3>
                  <p className="text-xs text-gray-400 mt-1">
                    Start chatting with our sellers now!
                  </p>
                </div>
              ) : (

                <>
                  <div className="px-4 py-2.5 bg-white border-b border-gray-100 flex items-center justify-between shrink-0 shadow-xs">
                    <div className="flex items-center gap-2.5">
                      <div className="w-8 h-8 rounded-full bg-orange-500 text-white flex items-center justify-center font-bold text-xs">
                        {(activeRoom.shopName || 'S').charAt(0).toUpperCase()}
                      </div>
                      <div>
                        <h4 className="text-xs font-bold text-gray-900 leading-tight">
                          {activeRoom.shopName || `Shop #${activeRoom.shopId}`}
                        </h4>
                        <div className="flex items-center gap-1.5 mt-0.5">
                          <span className="w-1.5 h-1.5 rounded-full bg-emerald-500 animate-pulse" />
                          <span className="text-[10px] text-gray-400">Đang hoạt động</span>
                        </div>
                      </div>
                    </div>
                    <button
                      onClick={() => navigate(`/products`)}
                      className="text-[11px] text-[#ee4d2d] hover:text-[#d73211] font-medium flex items-center gap-1 bg-orange-50 px-2.5 py-1 rounded-md transition-colors"
                    >
                      <Store className="w-3.5 h-3.5" />
                      <span>Xem Shop</span>
                    </button>
                  </div>

                  <div className="flex-1 overflow-y-auto p-4 space-y-3 bg-[#F8F9FA]">
                    {isLoadingMessages ? (
                      <div className="flex justify-center items-center h-full text-xs text-gray-400">
                        Đang tải tin nhắn...
                      </div>
                    ) : messages.length === 0 ? (
                      <div className="flex flex-col items-center justify-center h-full text-xs text-gray-400 space-y-1">
                        <MessageSquare className="w-6 h-6 text-gray-300" />
                        <span>Chưa có tin nhắn nào trong phòng này.</span>
                        <span className="text-[11px] text-gray-400">Hãy gửi lời chào đầu tiên đến Shop nhé!</span>
                      </div>
                    ) : (
                      messages.map((msg) => {
                        const isMine = msg.senderId === currentUserId;
                        return (
                          <div
                            key={msg.id}
                            className={`flex flex-col ${isMine ? 'items-end' : 'items-start'}`}
                          >
                            <div
                              className={`px-3.5 py-2 text-xs max-w-[78%] rounded-2xl shadow-xs break-words leading-relaxed ${
                                isMine
                                  ? 'bg-[#ee4d2d] text-white rounded-tr-none'
                                  : 'bg-white text-gray-800 border border-gray-200/70 rounded-tl-none'
                              }`}
                            >
                              {msg.content}
                            </div>
                            <div className="flex items-center gap-1 mt-1 px-1">
                              <span className="text-[9px] text-gray-400">{formatTime(msg.createdAt)}</span>
                              {isMine && (
                                <CheckCheck
                                  className={`w-3 h-3 ${msg.isRead ? 'text-blue-500' : 'text-gray-300'}`}
                                />
                              )}
                            </div>
                          </div>
                        );
                      })
                    )}
                    <div ref={messagesEndRef} />
                  </div>

                  <div className="p-2.5 bg-white border-t border-gray-100 shrink-0">
                    <div className="flex items-center gap-3 px-1 mb-2 text-gray-400">
                      <button
                        type="button"
                        className="hover:text-gray-600 transition-colors cursor-pointer"
                        title="Thêm biểu tượng cảm xúc"
                      >
                        <Smile className="w-4 h-4" />
                      </button>
                      <button
                        type="button"
                        className="hover:text-gray-600 transition-colors cursor-pointer"
                        title="Gửi hình ảnh"
                      >
                        <ImageIcon className="w-4 h-4" />
                      </button>
                    </div>

                    <form onSubmit={handleSendMessage} className="flex items-center gap-2">
                      <input
                        ref={inputRef}
                        type="text"
                        value={inputContent}
                        onChange={(e) => setInputContent(e.target.value)}
                        onKeyDown={handleKeyDown}
                        placeholder="Nhập tin nhắn..."
                        className="flex-1 bg-gray-100 text-xs px-3 py-2 rounded-lg focus:outline-none focus:ring-1 focus:ring-[#ee4d2d] text-gray-800 placeholder-gray-400"
                      />
                      <button
                        type="submit"
                        disabled={!inputContent.trim()}
                        className="bg-[#ee4d2d] hover:bg-[#d73211] disabled:opacity-40 disabled:hover:bg-[#ee4d2d] text-white p-2 rounded-lg transition-colors cursor-pointer shrink-0"
                      >
                        <Send className="w-3.5 h-3.5" />
                      </button>
                    </form>
                  </div>
                </>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
