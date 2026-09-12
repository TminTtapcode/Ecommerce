import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { adminApi } from '../../api/adminApi';
import type { OrderResponse } from '../../api/types/order.types';

const STATUS_BADGES: Record<string, { label: string; bg: string; text: string; border: string }> = {
  PENDING:   { label: 'Chờ xác nhận', bg: 'bg-amber-50',   text: 'text-amber-700',   border: 'border-amber-200' },
  CONFIRMED: { label: 'Đã xác nhận',  bg: 'bg-blue-50',    text: 'text-blue-700',    border: 'border-blue-200' },
  SHIPPING:  { label: 'Đang giao',    bg: 'bg-purple-50',  text: 'text-purple-700',  border: 'border-purple-200' },
  SHIPPED:   { label: 'Đã gửi hàng',  bg: 'bg-purple-50',  text: 'text-purple-700',  border: 'border-purple-200' },
  DELIVERED: { label: 'Hoàn thành',   bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200' },
  COMPLETED: { label: 'Hoàn thành',   bg: 'bg-emerald-50', text: 'text-emerald-700', border: 'border-emerald-200' },
  CANCELLED: { label: 'Đã hủy',       bg: 'bg-rose-50',    text: 'text-rose-700',    border: 'border-rose-200' },
};

export const AdminOrderListPage: React.FC = () => {
  const navigate = useNavigate();
  const [orders, setOrders] = useState<OrderResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchOrders = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getOrders(statusFilter || undefined, page, 10);
      if (!res.data.data) throw new Error('Thiếu dữ liệu phản hồi');
      setOrders(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Lỗi tải danh sách đơn hàng');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchOrders();
  }, [page, statusFilter]);

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 text-gray-900">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h2 className="text-2xl font-bold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent flex items-center gap-2">
            📦 Quản Lý Đơn Hàng
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Theo dõi và quản lý tất cả đơn hàng giao dịch qua hệ thống sàn
          </p>
        </div>
        <div className="flex items-center gap-2">
          <label className="text-xs text-gray-500 font-semibold uppercase">Lọc theo:</label>
          <select
            value={statusFilter}
            onChange={(e) => {
              setStatusFilter(e.target.value);
              setPage(0);
            }}
            className="bg-white border border-gray-300 rounded-xl px-3 py-2 text-sm text-gray-800 focus:border-orange-500 focus:outline-none transition-colors"
          >
            <option value="">Tất cả trạng thái</option>
            <option value="PENDING">Chờ xác nhận</option>
            <option value="CONFIRMED">Đã xác nhận</option>
            <option value="SHIPPING">Đang giao</option>
            <option value="DELIVERED">Hoàn thành</option>
            <option value="CANCELLED">Đã hủy</option>
          </select>
        </div>
      </div>

      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-xl mb-4 text-sm flex items-center gap-2">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="text-center py-12 text-gray-400">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mb-2"></div>
          <div>Đang tải danh sách đơn hàng...</div>
        </div>
      ) : orders.length === 0 ? (
        <div className="text-center py-12 text-gray-400 bg-gray-50 rounded-xl border border-gray-200">
          <div className="text-4xl mb-2">📦</div>
          <p className="text-base font-medium text-gray-700">Không tìm thấy đơn hàng nào</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="min-w-full divide-y divide-gray-200 text-left">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã Đơn</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Khách Hàng</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Gian Hàng</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Tổng Tiền</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Thanh Toán</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Trạng Thái</th>
                <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {orders.map((order) => {
                const badge = STATUS_BADGES[order.status] || {
                  label: order.status,
                  bg: 'bg-gray-100',
                  text: 'text-gray-700',
                  border: 'border-gray-200'
                };
                return (
                  <tr key={order.id} className="hover:bg-gray-50/80 transition-colors">
                    <td className="px-5 py-4 whitespace-nowrap text-sm font-bold text-orange-600">
                      #{order.id}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                      {order.customerName || `User #${order.userId}`}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-500">
                      Shop #{order.shopId}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-sm font-bold text-emerald-600">
                      {formatCurrency(order.totalAmount)}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-500">
                      {order.paymentMethod}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap">
                      <span className={`px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border ${badge.bg} ${badge.text} ${badge.border}`}>
                        {badge.label}
                      </span>
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <button
                        onClick={() => navigate(`/admin/orders/${order.id}`)}
                        className="px-3 py-1.5 rounded-lg bg-orange-50 text-orange-600 hover:bg-orange-100 border border-orange-200 text-xs font-semibold transition-colors cursor-pointer"
                      >
                        Chi tiết →
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>

          {totalPages > 1 && (
            <div className="flex justify-between items-center px-6 py-4 bg-gray-50 border-t border-gray-200">
              <button
                disabled={page === 0}
                onClick={() => setPage(p => p - 1)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                ← Trước
              </button>
              <span className="text-xs text-gray-500">
                Trang <span className="text-gray-900 font-semibold">{page + 1}</span> / {totalPages}
              </span>
              <button
                disabled={page === totalPages - 1}
                onClick={() => setPage(p => p + 1)}
                className="px-3 py-1.5 border border-gray-300 rounded-lg text-sm text-gray-700 bg-white hover:bg-gray-50 disabled:opacity-40 disabled:cursor-not-allowed transition-colors"
              >
                Sau →
              </button>
            </div>
          )}
        </div>
      )}
    </div>
  );
};
