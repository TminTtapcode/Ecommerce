import React, { useEffect, useState, useCallback } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { adminApi } from '../../api/adminApi';
import type { OrderResponse } from '../../api/types/order.types';
import type { AdminOrderHistory, RefundAttemptResponse } from '../../api/adminApi';
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

  const [refunds, setRefunds] = useState<RefundAttemptResponse[]>([]);
  const [isRefundModalOpen, setIsRefundModalOpen] = useState(false);
  const [refundReason, setRefundReason] = useState('Khách hàng yêu cầu hủy đơn');
  const [refundLoading, setRefundLoading] = useState(false);
  const [refundMessage, setRefundMessage] = useState<{ type: 'success' | 'error'; text: string } | null>(null);

  const refresh = useCallback(async () => {
    if (!id) return;
    const [detail, allowed, audit] = await Promise.all([
      adminApi.getOrderDetail(Number(id)),
      adminApi.getOrderActions(Number(id)),
      adminApi.getOrderHistory(Number(id), historyPage),
    ]);
    const orderData = detail.data.data;
    setOrder(orderData);
    setActions(allowed.data.data ?? []);
    setHistory(audit.data.data?.content ?? []);
    setHistoryPages(audit.data.data?.totalPages ?? 0);

    if (orderData && orderData.status === 'CANCELLED' && orderData.paymentMethod === 'VNPAY') {
      try {
        const refundRes = await adminApi.getRefunds(Number(id));
        setRefunds(refundRes.data.data ?? []);
      } catch {
        setRefunds([]);
      }
    }
  }, [id, historyPage]);

  useEffect(() => {
    const fetchOrder = async () => {
      try {
        setLoading(true);
        await refresh();
        setError('');
      } catch (err: unknown) {
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
    setSaving(true);
    setNotice('');
    try {
      await adminApi.updateOrderStatus(order.id, {
        expectedStatus: order.status,
        status: target,
        reason: reason.trim(),
      });
      setTarget('');
      setReason('');
      setNotice('Đã cập nhật trạng thái đơn hàng.');
    } catch (err) {
      const details = getApiError(err, 'Không thể cập nhật đơn hàng');
      setNotice(
        details.errorCode === ERROR_CODES.ORDER_STATE_CONFLICT
          ? 'Đơn hàng đã thay đổi. Kiểm tra trạng thái mới trước khi thao tác lại.'
          : details.message
      );
      setTarget('');
    } finally {
      try {
        await refresh();
      } catch {
        setActions([]);
        setNotice('Không tải được trạng thái mới. Vui lòng tải lại trang trước khi thao tác tiếp.');
      }
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
        <section className="bg-gradient-to-br from-amber-50/70 to-orange-50/30 border border-amber-200 rounded-2xl p-6 mb-6 shadow-xs">
          <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-4 mb-4">
            <div>
              <h3 className="text-base font-bold text-amber-900 flex items-center gap-2">
                💳 Quản Lý Hoàn Tiền (VNPAY Sandbox)
              </h3>
              <p className="text-xs text-amber-700 mt-1">
                Đơn hàng thanh toán trực tuyến qua VNPAY bị hủy. Admin có thể thực hiện hoàn tiền an toàn (có Idempotency-Key chống click đúp).
              </p>
            </div>
            <button
              onClick={() => {
                setRefundMessage(null);
                setIsRefundModalOpen(true);
              }}
              disabled={saving || refundLoading}
              className="px-4 py-2 bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-700 hover:to-orange-700 text-white text-xs font-semibold rounded-xl transition-all shadow-xs disabled:opacity-50 flex items-center gap-1.5 cursor-pointer shrink-0"
            >
              <span>+ Tạo Lệnh Hoàn Tiền</span>
            </button>
          </div>

          {refundMessage && (
            <div
              className={`p-3.5 rounded-xl mb-4 text-xs font-medium border flex items-center justify-between ${
                refundMessage.type === 'success'
                  ? 'bg-emerald-50 text-emerald-800 border-emerald-200'
                  : 'bg-rose-50 text-rose-800 border-rose-200'
              }`}
            >
              <span>{refundMessage.type === 'success' ? '✅ ' : '⚠️ '}{refundMessage.text}</span>
              <button
                onClick={() => setRefundMessage(null)}
                className="text-gray-400 hover:text-gray-600 text-sm ml-2 cursor-pointer"
              >
                ✕
              </button>
            </div>
          )}

          {refunds.length === 0 ? (
            <div className="bg-white/90 border border-amber-100 rounded-xl p-4 text-center text-xs text-gray-500">
              Chưa có lệnh hoàn tiền nào được tạo cho đơn hàng này. Bấm nút <strong>"Tạo Lệnh Hoàn Tiền"</strong> để gửi yêu cầu tới VNPAY.
            </div>
          ) : (
            <div className="overflow-x-auto rounded-xl border border-amber-200/80 bg-white shadow-xs">
              <table className="min-w-full divide-y divide-gray-100 text-left text-xs">
                <thead className="bg-amber-50/60 font-semibold text-gray-600">
                  <tr>
                    <th className="px-4 py-3">Mã Lệnh</th>
                    <th className="px-4 py-3">Số Tiền</th>
                    <th className="px-4 py-3">Trạng Thái</th>
                    <th className="px-4 py-3">Thời Gian Yêu Cầu</th>
                    <th className="px-4 py-3 text-right">Thao Tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {refunds.map((ref) => {
                    const statusBadge: Record<string, { label: string; className: string }> = {
                      CONFIRMED: { label: 'Đã hoàn tiền', className: 'bg-emerald-50 text-emerald-700 border-emerald-200' },
                      REQUESTED: { label: 'Đang xử lý', className: 'bg-blue-50 text-blue-700 border-blue-200' },
                      UNKNOWN:   { label: 'Cần đối soát', className: 'bg-amber-50 text-amber-700 border-amber-200' },
                      REJECTED:  { label: 'Từ chối', className: 'bg-rose-50 text-rose-700 border-rose-200' },
                    };
                    const badge = statusBadge[ref.status] || { label: ref.status, className: 'bg-gray-100 text-gray-700 border-gray-200' };

                    return (
                      <tr key={ref.id} className="hover:bg-amber-50/30 transition-colors">
                        <td className="px-4 py-3 font-medium text-gray-900">#{ref.id}</td>
                        <td className="px-4 py-3 font-bold text-emerald-600">{formatCurrency(ref.amount)}</td>
                        <td className="px-4 py-3">
                          <span className={`px-2.5 py-0.5 rounded-full text-[11px] font-semibold border ${badge.className}`}>
                            {badge.label}
                          </span>
                        </td>
                        <td className="px-4 py-3 text-gray-500">
                          {ref.requestedAt ? new Date(ref.requestedAt).toLocaleString('vi-VN') : '—'}
                        </td>
                        <td className="px-4 py-3 text-right">
                          {ref.status === 'UNKNOWN' ? (
                            <button
                              onClick={async () => {
                                setRefundLoading(true);
                                setRefundMessage(null);
                                try {
                                  const res = await adminApi.reconcileRefund(ref.id);
                                  setRefundMessage({
                                    type: 'success',
                                    text: `Đối soát thành công! Trạng thái mới: ${res.data.data?.status}`
                                  });
                                  await refresh();
                                } catch (err: unknown) {
                                  setRefundMessage({
                                    type: 'error',
                                    text: getApiError(err, 'Lỗi khi đối soát với VNPay').message
                                  });
                                } finally {
                                  setRefundLoading(false);
                                }
                              }}
                              disabled={refundLoading}
                              className="px-2.5 py-1 bg-amber-600 hover:bg-amber-700 text-white rounded-lg text-[11px] font-medium transition-colors disabled:opacity-50 shadow-xs cursor-pointer"
                            >
                              {refundLoading ? 'Đang đối soát...' : 'Đối soát VNPay'}
                            </button>
                          ) : (
                            <span className="text-[11px] text-gray-400">Không cần đối soát</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}

          {isRefundModalOpen && (
            <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 backdrop-blur-xs p-4">
              <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-xl border border-gray-100">
                <div className="flex items-center justify-between mb-4">
                  <h4 className="text-base font-bold text-gray-900 flex items-center gap-2">
                    💸 Xác Nhận Hoàn Tiền VNPAY
                  </h4>
                  <button
                    onClick={() => setIsRefundModalOpen(false)}
                    className="text-gray-400 hover:text-gray-600 text-lg cursor-pointer"
                  >
                    ✕
                  </button>
                </div>

                <form
                  onSubmit={async (e) => {
                    e.preventDefault();
                    if (!order || !refundReason.trim()) return;
                    setRefundLoading(true);
                    setRefundMessage(null);
                    try {
                      await adminApi.createRefund(order.id, refundReason.trim());
                      setRefundMessage({ type: 'success', text: 'Lệnh hoàn tiền VNPAY đã được gửi thành công!' });
                      setIsRefundModalOpen(false);
                      setRefundReason('Khách hàng yêu cầu hủy đơn');
                      await refresh();
                    } catch (err: unknown) {
                      setRefundMessage({ type: 'error', text: getApiError(err, 'Không thể tạo lệnh hoàn tiền').message });
                    } finally {
                      setRefundLoading(false);
                    }
                  }}
                  className="space-y-4"
                >
                  <div className="bg-amber-50 border border-amber-200/80 rounded-xl p-3 text-xs text-amber-800 space-y-1">
                    <p><strong>Mã đơn hàng:</strong> #{order.id}</p>
                    <p><strong>Số tiền hoàn:</strong> <span className="font-bold text-emerald-600 text-sm">{formatCurrency(order.totalAmount)}</span></p>
                    <p><strong>Cổng thanh toán:</strong> VNPAY Sandbox (Toàn bộ giá trị đơn)</p>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1">
                      Lý do hoàn tiền (Bắt buộc)
                    </label>
                    <textarea
                      required
                      maxLength={500}
                      rows={3}
                      value={refundReason}
                      onChange={(e) => setRefundReason(e.target.value)}
                      className="block w-full bg-white border border-gray-300 rounded-xl p-3 text-sm text-gray-900 focus:border-orange-500 focus:outline-none"
                      placeholder="Nhập lý do hoàn tiền cho khách hàng..."
                    />
                  </div>

                  <div className="flex justify-end gap-2 pt-2">
                    <button
                      type="button"
                      onClick={() => setIsRefundModalOpen(false)}
                      disabled={refundLoading}
                      className="px-4 py-2 border border-gray-300 text-gray-700 text-xs font-semibold rounded-xl hover:bg-gray-50 transition-colors cursor-pointer"
                    >
                      Hủy bỏ
                    </button>
                    <button
                      type="submit"
                      disabled={refundLoading || !refundReason.trim()}
                      className="px-4 py-2 bg-gradient-to-r from-amber-600 to-orange-600 hover:from-amber-700 hover:to-orange-700 text-white text-xs font-semibold rounded-xl transition-all shadow-sm disabled:opacity-50 cursor-pointer"
                    >
                      {refundLoading ? 'Đang gửi lệnh...' : 'Xác Nhận Hoàn Tiền'}
                    </button>
                  </div>
                </form>
              </div>
            </div>
          )}
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
