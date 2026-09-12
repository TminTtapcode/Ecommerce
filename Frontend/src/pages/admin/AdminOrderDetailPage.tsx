import React, { useEffect, useState, useCallback, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { adminApi } from '../../api/adminApi';
import type { OrderResponse } from '../../api/types/order.types';
import type { AdminOrderHistory } from '../../api/adminApi';
import { getApiError } from '../../api/apiError';
import { ERROR_CODES } from '../../api/types/errorCodes';

export const AdminOrderDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [order, setOrder] = useState<OrderResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [actions, setActions] = useState<string[]>([]);
  const [history, setHistory] = useState<AdminOrderHistory[]>([]);
  const [historyPage, setHistoryPage] = useState(0);
  const [historyPages, setHistoryPages] = useState(0);
  const [target, setTarget] = useState('');
  const [reason, setReason] = useState('');
  const [saving, setSaving] = useState(false);
  const [notice, setNotice] = useState('');
  const activeId = useRef(id);
  activeId.current = id;
  const requestSequence = useRef(0);

  const refresh = useCallback(async () => {
    if (!id) return;
    const sequence = ++requestSequence.current;
    const [detail, allowed, audit] = await Promise.all([
      adminApi.getOrderDetail(Number(id)), adminApi.getOrderActions(Number(id)), adminApi.getOrderHistory(Number(id), historyPage),
    ]);
    if (activeId.current !== id || sequence !== requestSequence.current) return;
    setOrder(detail.data.data);
    setActions(allowed.data.data ?? []);
    setHistory(audit.data.data?.content ?? []);
    setHistoryPages(audit.data.data?.totalPages ?? 0);
  }, [id, historyPage]);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setLoading(true);
        await refresh();
        setError('');
      } catch (err: any) {
        setError(getApiError(err, 'Lỗi tải chi tiết đơn hàng').message);
      } finally {
        setLoading(false);
      }
    };
    fetchOrder();
  }, [refresh]);

  const submit = async (event: React.FormEvent) => {
    event.preventDefault();
    if (!order || saving || !target || !reason.trim()) return;
    setSaving(true); setNotice('');
    try {
      await adminApi.updateOrderStatus(order.id, { expectedStatus: order.status, status: target, reason: reason.trim() });
      setTarget(''); setReason(''); setNotice('Đã cập nhật trạng thái đơn hàng.');
    } catch (err) {
      const details = getApiError(err, 'Không thể cập nhật đơn hàng');
      setNotice(details.errorCode === ERROR_CODES.ORDER_STATE_CONFLICT ? 'Đơn hàng đã thay đổi. Kiểm tra trạng thái mới trước khi thao tác lại.' : details.message);
      setTarget('');
    } finally {
      try { await refresh(); } catch { setActions([]); setNotice('Không tải được trạng thái mới. Vui lòng tải lại trang trước khi thao tác tiếp.'); }
      setSaving(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  if (loading) return (
    <div className="p-12 text-center text-gray-400">
      <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mb-2"></div>
      <div>Đang tải chi tiết đơn hàng...</div>
    </div>
  );
  if (error) return (
    <div className="bg-rose-50 border border-rose-200 text-rose-700 p-6 rounded-2xl">
      ⚠️ {error}
    </div>
  );
  if (!order) return <div className="p-6 text-gray-500">Không tìm thấy đơn hàng</div>;

  return (
    <div className="bg-white border border-gray-200 rounded-2xl shadow-sm p-6 text-gray-900">
      <div className="flex justify-between items-center mb-6">
        <div>
          <h2 className="text-2xl font-bold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent flex items-center gap-2">
            📦 Chi Tiết Đơn Hàng #{order.id}
          </h2>
          <p className="text-sm text-gray-500 mt-1">Thông tin chi tiết và quyền can thiệp quản trị</p>
        </div>
        <button
          onClick={() => navigate('/admin/orders')}
          className="px-4 py-2 bg-white hover:bg-gray-50 border border-gray-300 text-gray-700 text-sm font-semibold rounded-xl transition-colors cursor-pointer"
        >
          &larr; Quay lại danh sách
        </button>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mb-8">
        <div className="bg-gray-50 border border-gray-200 p-5 rounded-xl">
          <h3 className="text-base font-bold text-gray-900 mb-3 flex items-center gap-2">
            👤 Thông tin Khách hàng & Shop
          </h3>
          <div className="space-y-2 text-sm">
            <p className="text-gray-600"><span className="text-gray-400">User ID:</span> #{order.userId}</p>
            <p className="text-gray-600"><span className="text-gray-400">Khách hàng:</span> <span className="text-gray-900 font-semibold">{order.customerName}</span></p>
            <p className="text-gray-600"><span className="text-gray-400">Shop ID:</span> #{order.shopId}</p>
          </div>
        </div>

        <div className="bg-gray-50 border border-gray-200 p-5 rounded-xl">
          <h3 className="text-base font-bold text-gray-900 mb-3 flex items-center gap-2">
            📋 Thông tin Đơn hàng
          </h3>
          <div className="space-y-2 text-sm">
            <p className="text-gray-600">
              <span className="text-gray-400">Trạng thái:</span>{' '}
              <span className="px-2.5 py-0.5 inline-flex text-xs leading-5 font-semibold rounded-full bg-orange-50 text-orange-600 border border-orange-200">
                {order.status}
              </span>
            </p>
            <p className="text-gray-600"><span className="text-gray-400">Thanh toán:</span> <span className="text-gray-900 font-semibold">{order.paymentMethod}</span></p>
            <p className="text-gray-600"><span className="text-gray-400">Tổng tiền:</span> <span className="text-emerald-600 font-bold">{formatCurrency(order.totalAmount)}</span></p>
            <p className="text-gray-600"><span className="text-gray-400">Địa chỉ giao:</span> <span className="text-gray-700">{order.shippingAddress}</span></p>
            {order.createdAt && <p className="text-gray-600"><span className="text-gray-400">Ngày tạo:</span> {new Date(order.createdAt).toLocaleString('vi-VN')}</p>}
          </div>
        </div>
      </div>

      <section className="bg-gray-50 border border-gray-200 rounded-xl p-5 mb-6">
        <h3 className="text-base font-bold text-gray-900 mb-3 flex items-center gap-2">
          ⚡ Can thiệp trạng thái đơn
        </h3>
        {notice && (
          <div className="bg-orange-50 border border-orange-200 text-orange-700 p-3 rounded-xl mb-4 text-xs">
            {notice}
          </div>
        )}
        {actions.length === 0 ? (
          <p className="text-gray-500 text-sm">Không có thao tác phù hợp với trạng thái và điều kiện thanh toán hiện tại.</p>
        ) : (
          <form onSubmit={submit}>
            <fieldset disabled={saving} className="space-y-4">
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                  Trạng thái mới
                </label>
                <select
                  required
                  value={target}
                  onChange={e => setTarget(e.target.value)}
                  className="bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:outline-none"
                >
                  <option value="">Chọn thao tác</option>
                  {actions.map(action => (
                    <option key={action} value={action}>
                      {({ SHIPPED: 'Giao vận chuyển', DELIVERED: 'Xác nhận đã giao', CANCELLED: 'Hủy đơn COD' } as Record<string, string>)[action] ?? action}
                    </option>
                  ))}
                </select>
              </div>
              <div>
                <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                  Lý do can thiệp (bắt buộc, tối đa 500 ký tự)
                </label>
                <textarea
                  required
                  maxLength={500}
                  value={reason}
                  onChange={e => setReason(e.target.value)}
                  className="block bg-white border border-gray-300 rounded-xl p-3 w-full text-gray-900 text-sm focus:border-orange-500 focus:outline-none placeholder-gray-400"
                  placeholder="Nhập lý do điều chỉnh trạng thái đơn hàng..."
                />
              </div>
              {target === 'CANCELLED' && (
                <p className="text-xs text-amber-600">⚠️ Hủy đơn sẽ hoàn kho. Thao tác này không tự động thực hiện hoàn tiền trực tuyến.</p>
              )}
              <button
                disabled={!target || !reason.trim()}
                className="px-5 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white text-sm font-semibold rounded-xl shadow-sm disabled:opacity-50 transition-all cursor-pointer"
              >
                {saving ? 'Đang lưu...' : 'Xác nhận cập nhật'}
              </button>
            </fieldset>
          </form>
        )}
      </section>

      <section className="bg-gray-50 border border-gray-200 rounded-xl p-5 mb-6">
        <h3 className="text-base font-bold text-gray-900 mb-3">🕒 Lịch sử can thiệp Admin</h3>
        {history.length === 0 && <p className="text-sm text-gray-500">Chưa có can thiệp Admin nào được ghi nhận.</p>}
        {history.map(entry => (
          <div key={entry.id} className="border-b border-gray-200 py-3 text-sm">
            <p className="font-semibold text-orange-600">
              {entry.oldStatus} → {entry.newStatus} · Admin #{entry.actorUserId} · {new Date(entry.createdAt).toLocaleString('vi-VN')}
            </p>
            <p className="whitespace-pre-wrap break-words text-gray-700 mt-1">{entry.reason}</p>
          </div>
        ))}
        {historyPages > 1 && (
          <div className="flex gap-4 mt-3">
            <button
              disabled={saving || historyPage === 0}
              onClick={() => setHistoryPage(p => p - 1)}
              className="px-3 py-1 bg-white rounded-lg border border-gray-300 text-xs text-gray-700 disabled:opacity-40"
            >
              Trước
            </button>
            <span className="text-xs text-gray-500">Trang {historyPage + 1}/{historyPages}</span>
            <button
              disabled={saving || historyPage + 1 >= historyPages}
              onClick={() => setHistoryPage(p => p + 1)}
              className="px-3 py-1 bg-white rounded-lg border border-gray-300 text-xs text-gray-700 disabled:opacity-40"
            >
              Sau
            </button>
          </div>
        )}
      </section>

      {order.status === 'CANCELLED' && order.paymentMethod === 'VNPAY' && (
        <section className="bg-amber-50/60 border border-amber-200 rounded-xl p-5 mb-6">
          <h3 className="text-base font-bold text-amber-800 mb-2">💳 Hoàn Tiền (VNPAY)</h3>
          <p className="mb-4 text-xs text-amber-700">Đơn hàng đã thanh toán qua VNPAY và bị hủy. Bạn có thể thực hiện lệnh hoàn tiền trực tuyến.</p>
          <div className="flex flex-wrap gap-3">
            <button
              onClick={async () => {
                const r = window.prompt('Nhập lý do hoàn tiền (không bắt buộc):', 'Khách hàng hủy đơn');
                if (r === null) return;
                try {
                  setSaving(true);
                  await adminApi.createRefund(order.id, r);
                  alert('Lệnh hoàn tiền đã được gửi thành công!');
                } catch (err: any) {
                  alert(getApiError(err).message);
                } finally {
                  setSaving(false);
                }
              }}
              disabled={saving}
              className="px-4 py-2 bg-amber-600 hover:bg-amber-700 text-white text-xs font-semibold rounded-xl transition-colors disabled:opacity-50 cursor-pointer shadow-sm"
            >
              {saving ? 'Đang xử lý...' : 'Thực hiện Hoàn Tiền'}
            </button>
            <button
              onClick={async () => {
                try {
                  setSaving(true);
                  const res = await adminApi.getRefunds(order.id);
                  const data = res.data.data;
                  if (data && data.length > 0) {
                    alert('Lịch sử hoàn tiền:\n' + data.map(d => `- ${d.status}: ${d.amount} VND (${new Date(d.createdAt).toLocaleString('vi-VN')})`).join('\n'));
                  } else {
                    alert('Chưa có lịch sử hoàn tiền nào cho đơn này.');
                  }
                } catch (err: any) {
                  alert(getApiError(err).message);
                } finally {
                  setSaving(false);
                }
              }}
              disabled={saving}
              className="px-4 py-2 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 text-xs font-semibold rounded-xl transition-colors disabled:opacity-50 cursor-pointer"
            >
              Xem lịch sử hoàn tiền
            </button>
          </div>
        </section>
      )}

      <h3 className="text-base font-bold text-gray-900 mb-4">🛒 Sản phẩm trong đơn</h3>
      <div className="overflow-x-auto rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200 text-left">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Sản phẩm</th>
              <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Đơn giá</th>
              <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Số lượng</th>
              <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thành tiền</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-white">
            {order.items.map((item) => (
              <tr key={item.id} className="hover:bg-gray-50/80 transition-colors">
                <td className="px-5 py-4 whitespace-nowrap text-sm font-medium text-gray-900">
                  <div className="flex items-center gap-3">
                    <div className="w-10 h-10 bg-gray-100 border border-gray-200 rounded-lg flex items-center justify-center shrink-0 overflow-hidden">
                      {item.imageUrl ? (
                        <img src={item.imageUrl} alt={item.productName} className="w-full h-full object-cover" />
                      ) : (
                        <span className="text-xs text-gray-400">📦</span>
                      )}
                    </div>
                    <span>{item.productName}</span>
                  </div>
                </td>
                <td className="px-5 py-4 whitespace-nowrap text-sm text-gray-500 text-right">{formatCurrency(item.unitPrice)}</td>
                <td className="px-5 py-4 whitespace-nowrap text-sm text-gray-500 text-right">{item.quantity}</td>
                <td className="px-5 py-4 whitespace-nowrap text-sm font-bold text-emerald-600 text-right">{formatCurrency(item.unitPrice * item.quantity)}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};
