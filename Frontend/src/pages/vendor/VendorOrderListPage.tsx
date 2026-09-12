import { useEffect, useState } from 'react';
import { orderApi } from '../../api/orderApi';
import type { OrderResponse } from '../../api/types/order.types';
import { VendorNav } from '../../components/vendor/VendorNav';

const STATUS_TABS = [
  { label: 'Tất cả', value: '' },
  { label: 'Chờ xác nhận', value: 'PENDING' },
  { label: 'Đã xác nhận', value: 'CONFIRMED' },
  { label: 'Đang giao', value: 'SHIPPED' },
  { label: 'Hoàn thành', value: 'DELIVERED' },
  { label: 'Đã hủy', value: 'CANCELLED' },
];

const STATUS_BADGE: Record<string, { label: string; className: string }> = {
  PENDING: { label: 'Chờ xác nhận', className: 'bg-yellow-100 text-yellow-700' },
  CONFIRMED: { label: 'Đã xác nhận', className: 'bg-blue-100 text-blue-700' },
  SHIPPED: { label: 'Đang giao', className: 'bg-indigo-100 text-indigo-700' },
  DELIVERED: { label: 'Hoàn thành', className: 'bg-green-100 text-green-700' },
  CANCELLED: { label: 'Đã hủy', className: 'bg-red-100 text-red-700' },
};

export const VendorOrderListPage = () => {
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [activeTab, setActiveTab] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [updatingId, setUpdatingId] = useState<number | null>(null);
  const [toast, setToast] = useState<string | null>(null);
  const [cancelModalOrderId, setCancelModalOrderId] = useState<number | null>(null);
  const [cancelReason, setCancelReason] = useState('');

  const fetchOrders = async () => {
    setLoading(true);
    try {
      const params: any = { page, size: 10 };
      if (activeTab) params.status = activeTab;
      const res = await orderApi.getVendorOrders(params);
      if (!res.data) throw new Error('Thiếu dữ liệu phản hồi');
      setOrders(res.data.content);
      setTotalPages(res.data.totalPages);
    } catch {
      setOrders([]);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setPage(0);
  }, [activeTab]);

  useEffect(() => {
    fetchOrders();
  }, [activeTab, page]);

  const showToast = (message: string) => {
    setToast(message);
    setTimeout(() => setToast(null), 3000);
  };

  const handleUpdateStatus = async (orderId: number, newStatus: 'CONFIRMED' | 'SHIPPED' | 'CANCELLED', reason?: string) => {
    setUpdatingId(orderId);
    try {
      await orderApi.updateVendorOrderStatus(orderId, { status: newStatus, reason });
      showToast('Cập nhật trạng thái thành công!');
      fetchOrders();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi cập nhật trạng thái.');
    } finally {
      setUpdatingId(null);
    }
  };

  const handleCancelSubmit = () => {
    if (cancelModalOrderId !== null) {
      handleUpdateStatus(cancelModalOrderId, 'CANCELLED', cancelReason);
      setCancelModalOrderId(null);
      setCancelReason('');
    }
  };

  const renderActionButtons = (order: OrderResponse) => {
    const isUpdating = updatingId === order.id;
    const btnBase = 'px-3 py-1.5 text-xs font-medium rounded-md transition-colors disabled:opacity-50';

    switch (order.status) {
      case 'PENDING':
        return (
          <div className="flex gap-2">
            <button
              disabled={isUpdating}
              onClick={() => handleUpdateStatus(order.id, 'CONFIRMED')}
              className={`${btnBase} bg-emerald-600 text-white hover:bg-emerald-700`}
            >
              {isUpdating ? '...' : '✓ Xác nhận đơn'}
            </button>
          </div>
        );
      case 'CONFIRMED':
        return (
          <div className="flex gap-2">
            <button
              disabled={isUpdating}
              onClick={() => handleUpdateStatus(order.id, 'SHIPPED')}
              className={`${btnBase} bg-indigo-500 text-white hover:bg-indigo-600`}
            >
              {isUpdating ? '...' : '📦 Giao cho ĐVVC'}
            </button>
            <button
              disabled={isUpdating}
              onClick={() => { setCancelModalOrderId(order.id); setCancelReason(''); }}
              className={`${btnBase} bg-white text-red-600 border border-red-300 hover:bg-red-50`}
            >
              Hủy đơn
            </button>
          </div>
        );
      default:
        return null;
    }
  };

  return (
    <div className="max-w-6xl mx-auto py-8 px-4">
      <VendorNav />

      <div className="flex gap-1 mb-6 overflow-x-auto pb-1 border-b border-gray-200">
        {STATUS_TABS.map((tab) => (
          <button
            key={tab.value}
            onClick={() => setActiveTab(tab.value)}
            className={`px-4 py-2 text-sm font-medium rounded-t-lg whitespace-nowrap transition-colors ${
              activeTab === tab.value
                ? 'bg-orange-500 text-white'
                : 'text-gray-600 hover:text-orange-600 hover:bg-orange-50'
            }`}
          >
            {tab.label}
          </button>
        ))}
      </div>

      {loading ? (
        <div className="flex justify-center items-center py-20">
          <svg className="w-8 h-8 text-orange-400 animate-spin" fill="none" viewBox="0 0 24 24">
            <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
            <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
          </svg>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-20 text-gray-400">
          <svg className="w-16 h-16 mx-auto mb-4 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M20 7l-8-4-8 4m16 0l-8 4m8-4v10l-8 4m0-10L4 7m8 4v10M4 7v10l8 4" />
          </svg>
          <p className="text-lg font-medium">Chưa có đơn hàng nào trong mục này</p>
        </div>
      ) : (
        <div className="space-y-4">
          {orders.map((order) => {
            const badge = STATUS_BADGE[order.status] || { label: order.status, className: 'bg-gray-100 text-gray-600' };
            return (
              <div key={order.id} className="bg-white rounded-xl border border-gray-200 shadow-sm overflow-hidden hover:shadow-md transition-shadow">
                <div className="flex flex-wrap items-center justify-between gap-3 px-5 py-3 bg-gray-50 border-b border-gray-100">
                  <div className="flex items-center gap-3">
                    <span className="text-sm font-semibold text-gray-800">Đơn #{order.id}</span>
                    <span className={`text-xs font-semibold px-2.5 py-0.5 rounded-full ${badge.className}`}>
                      {badge.label}
                    </span>
                    <span className="text-sm text-gray-600 bg-gray-200 px-2 rounded">
                      Khách hàng: {order.customerName}
                    </span>
                  </div>
                  <div className="flex items-center gap-4 text-xs text-gray-500">
                    <span>🕐 {new Date(order.createdAt).toLocaleString('vi-VN')}</span>
                    <span className="font-semibold text-orange-600 text-sm">
                      {order.totalAmount.toLocaleString('vi-VN')}₫
                    </span>
                  </div>
                </div>

                <div className="px-5 py-3">
                  <div className="space-y-2">
                    {order.items.map((item) => (
                      <div key={item.id} className="flex justify-between items-center text-sm py-1">
                        <div className="flex items-center gap-3 flex-1 min-w-0 pr-2">
                          <div className="w-10 h-10 bg-gray-100 border border-gray-200 rounded flex items-center justify-center shrink-0 overflow-hidden">
                            {item.imageUrl ? (
                              <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
                            ) : (
                              <span className="text-xs text-gray-400">📦</span>
                            )}
                          </div>
                          <div className="truncate">
                            <span className="text-gray-800 font-medium">{item.productName}</span>
                            <span className="text-gray-400 text-xs ml-2">x{item.quantity}</span>
                          </div>
                        </div>
                        <span className="text-gray-600 font-medium whitespace-nowrap">{item.subTotal.toLocaleString('vi-VN')}₫</span>
                      </div>
                    ))}
                  </div>

                  <div className="mt-3 pt-3 border-t border-gray-100 text-xs text-gray-500">
                    <span>📍 {order.shippingAddress}</span>
                  </div>
                </div>

                {renderActionButtons(order) && (
                  <div className="px-5 py-3 bg-gray-50 border-t border-gray-100 flex justify-end">
                    {renderActionButtons(order)}
                  </div>
                )}
              </div>
            );
          })}

          {totalPages > 1 && (
            <div className="flex justify-center gap-2 pt-4">
              <button
                onClick={() => setPage(Math.max(0, page - 1))}
                disabled={page === 0}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 transition-colors"
              >
                ← Trước
              </button>
              <span className="px-4 py-2 text-sm text-gray-600">
                Trang {page + 1} / {totalPages}
              </span>
              <button
                onClick={() => setPage(Math.min(totalPages - 1, page + 1))}
                disabled={page >= totalPages - 1}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 transition-colors"
              >
                Sau →
              </button>
            </div>
          )}
        </div>
      )}

      {cancelModalOrderId !== null && (
        <div className="fixed inset-0 bg-black/40 z-50 flex items-center justify-center">
          <div className="bg-white rounded-xl shadow-2xl w-full max-w-md mx-4 p-6">
            <h3 className="text-lg font-bold text-gray-900 mb-2">Hủy đơn hàng #{cancelModalOrderId}</h3>
            <p className="text-sm text-gray-500 mb-4">Vui lòng nhập lý do hủy đơn hàng:</p>
            <textarea
              value={cancelReason}
              onChange={(e) => setCancelReason(e.target.value)}
              placeholder="Lý do hủy đơn (ví dụ: Hết hàng, sai thông tin...)"
              rows={3}
              className="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
            />
            <div className="flex justify-end gap-3 mt-4">
              <button
                onClick={() => { setCancelModalOrderId(null); setCancelReason(''); }}
                className="px-4 py-2 text-sm border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 transition-colors"
              >
                Quay lại
              </button>
              <button
                onClick={handleCancelSubmit}
                className="px-4 py-2 text-sm bg-red-500 text-white rounded-lg hover:bg-red-600 font-medium transition-colors"
              >
                Xác nhận hủy đơn
              </button>
            </div>
          </div>
        </div>
      )}

      {toast && (
        <div className="fixed bottom-6 right-6 z-50 bg-green-500 text-white px-5 py-3 rounded-xl shadow-lg text-sm font-medium animate-[fadeIn_0.3s_ease-out]">
          ✅ {toast}
        </div>
      )}
    </div>
  );
};
