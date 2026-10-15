import React, { useState, useEffect } from 'react';
import { 
  CheckCircle2, 
  XCircle, 
  Clock, 
  RefreshCw, 
  AlertCircle, 
  Wallet,
  Building,
  Check,
  X
} from 'lucide-react';
import { payoutApi } from '../../api/payoutApi';
import { getApiError } from '../../api/apiError';
import type { PayoutResponse, PayoutStatus } from '../../api/types/payout.types';

const fmtCurrency = (v?: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v ?? 0);

const TABS: { label: string; value?: PayoutStatus }[] = [
  { label: 'Tất cả' },
  { label: 'Chờ duyệt', value: 'PENDING' },
  { label: 'Đã chuyển tiền', value: 'APPROVED' },
  { label: 'Đã từ chối', value: 'REJECTED' },
];

export const AdminPayoutListPage: React.FC = () => {
  const [payouts, setPayouts] = useState<PayoutResponse[]>([]);
  const [activeTab, setActiveTab] = useState<PayoutStatus | undefined>(undefined);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [loading, setLoading] = useState(true);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [rejectId, setRejectId] = useState<number | null>(null);
  const [rejectReason, setRejectReason] = useState('');
  const [processingId, setProcessingId] = useState<number | null>(null);

  const fetchPayouts = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const res = await payoutApi.getAdminPayouts(activeTab, page, 15);
      if (res.data) {
        setPayouts(res.data.content);
        setTotalPages(res.data.totalPages);
      }
    } catch (err: unknown) {
      setErrorMsg(getApiError(err, 'Lỗi tải danh sách yêu cầu rút tiền').message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    setPage(0);
  }, [activeTab]);

  useEffect(() => {
    fetchPayouts();
  }, [activeTab, page]);

  const handleApprove = async (id: number) => {
    if (!window.confirm(`Xác nhận bạn đã chuyển khoản thành công cho lệnh #${id}?`)) return;

    setProcessingId(id);
    try {
      await payoutApi.approvePayout(id, { adminNote: 'Admin đã chuyển tiền thành công qua ngân hàng' });
      setSuccessMsg(`Đã phê duyệt lệnh rút #${id} thành công!`);
      setTimeout(() => setSuccessMsg(''), 4000);
      fetchPayouts();
    } catch (err: unknown) {
      alert(getApiError(err, 'Có lỗi xảy ra khi duyệt lệnh').message);
    } finally {
      setProcessingId(null);
    }
  };

  const handleRejectSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!rejectId || !rejectReason.trim()) return;

    setProcessingId(rejectId);
    try {
      await payoutApi.rejectPayout(rejectId, { reason: rejectReason.trim() });
      setSuccessMsg(`Đã từ chối lệnh rút #${rejectId}. Số tiền đã được hoàn lại vào số dư gian hàng.`);
      setTimeout(() => setSuccessMsg(''), 4000);
      setRejectId(null);
      setRejectReason('');
      fetchPayouts();
    } catch (err: unknown) {
      alert(getApiError(err, 'Có lỗi xảy ra khi từ chối lệnh').message);
    } finally {
      setProcessingId(null);
    }
  };

  return (
    <div className="space-y-6 font-['Outfit',sans-serif]">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2.5">
            <Wallet className="w-7 h-7 text-orange-600" />
            Quản Lý Yêu Cầu Rút Tiền
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Đối soát doanh thu, kiểm tra thông tin ngân hàng và phê duyệt chi trả cho các Shop
          </p>
        </div>

        <button
          onClick={fetchPayouts}
          disabled={loading}
          className="inline-flex items-center px-4 py-2 bg-white border border-gray-200 text-gray-700 text-sm font-medium rounded-xl hover:bg-gray-50 transition-colors shadow-xs cursor-pointer self-start sm:self-auto"
        >
          <RefreshCw className={`w-4 h-4 mr-2 text-gray-500 ${loading ? 'animate-spin' : ''}`} />
          Làm mới
        </button>
      </div>

      {successMsg && (
        <div className="p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
          <CheckCircle2 className="w-5 h-5 shrink-0 text-emerald-500" />
          <span>{successMsg}</span>
        </div>
      )}

      {errorMsg && (
        <div className="p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-center gap-2">
          <AlertCircle className="w-5 h-5 shrink-0 text-rose-500" />
          <span>{errorMsg}</span>
        </div>
      )}

      <div className="flex gap-2 border-b border-gray-200 pb-2 overflow-x-auto">
        {TABS.map((tab) => {
          const isActive = activeTab === tab.value;
          return (
            <button
              key={tab.label}
              onClick={() => setActiveTab(tab.value)}
              className={`px-4 py-2 text-sm font-bold rounded-xl transition-all cursor-pointer whitespace-nowrap ${
                isActive
                  ? 'bg-orange-500 text-white shadow-xs'
                  : 'text-gray-600 hover:bg-orange-50 hover:text-orange-600'
              }`}
            >
              {tab.label}
            </button>
          );
        })}
      </div>

      <div className="bg-white rounded-2xl border border-gray-200 shadow-xs overflow-hidden">
        {loading ? (
          <div className="py-16 text-center text-gray-400">Đang tải danh sách yêu cầu...</div>
        ) : payouts.length === 0 ? (
          <div className="py-16 text-center text-gray-400">
            Không có yêu cầu rút tiền nào trong mục này
          </div>
        ) : (
          <>
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead className="bg-gray-50/80 border-b border-gray-200 text-xs font-bold text-gray-500 uppercase tracking-wider">
                  <tr>
                    <th className="px-6 py-3.5">Mã lệnh</th>
                    <th className="px-6 py-3.5">Gian hàng</th>
                    <th className="px-6 py-3.5">Số tiền rút</th>
                    <th className="px-6 py-3.5">Tài khoản ngân hàng</th>
                    <th className="px-6 py-3.5">Thời gian tạo</th>
                    <th className="px-6 py-3.5">Trạng thái</th>
                    <th className="px-6 py-3.5 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {payouts.map((p) => {
                    const isProcessing = processingId === p.id;
                    return (
                      <tr key={p.id} className="hover:bg-gray-50/60 transition-colors">
                        <td className="px-6 py-4 font-bold text-gray-900">
                          #{p.id}
                        </td>
                        <td className="px-6 py-4">
                          <div className="flex items-center space-x-2">
                            <Building className="w-4 h-4 text-gray-400" />
                            <span className="font-bold text-gray-800">{p.shopName}</span>
                          </div>
                          <span className="text-[11px] text-gray-400">ID: {p.shopId}</span>
                        </td>
                        <td className="px-6 py-4 font-black text-orange-600 text-base">
                          {fmtCurrency(p.amount)}
                        </td>
                        <td className="px-6 py-4">
                          <p className="font-bold text-gray-900">{p.bankName}</p>
                          <p className="text-xs font-mono font-semibold text-gray-600">
                            STK: {p.bankAccountNumber}
                          </p>
                          <p className="text-xs font-bold text-gray-800 uppercase">
                            {p.bankAccountHolder}
                          </p>
                        </td>
                        <td className="px-6 py-4 text-xs text-gray-500">
                          {new Date(p.createdAt).toLocaleString('vi-VN')}
                        </td>
                        <td className="px-6 py-4">
                          {p.status === 'PENDING' && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-amber-50 text-amber-700 border border-amber-200">
                              <Clock className="w-3.5 h-3.5" /> Chờ duyệt
                            </span>
                          )}
                          {p.status === 'APPROVED' && (
                            <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-emerald-50 text-emerald-700 border border-emerald-200">
                              <CheckCircle2 className="w-3.5 h-3.5" /> Đã chuyển tiền
                            </span>
                          )}
                          {p.status === 'REJECTED' && (
                            <div>
                              <span className="inline-flex items-center gap-1 px-2.5 py-1 rounded-full text-xs font-bold bg-rose-50 text-rose-700 border border-rose-200">
                                <XCircle className="w-3.5 h-3.5" /> Bị từ chối
                              </span>
                              {p.adminNote && (
                                <p className="text-[11px] text-rose-600 mt-0.5 truncate max-w-xs" title={p.adminNote}>
                                  {p.adminNote}
                                </p>
                              )}
                            </div>
                          )}
                        </td>
                        <td className="px-6 py-4 text-right">
                          {p.status === 'PENDING' ? (
                            <div className="flex items-center justify-end gap-2">
                              <button
                                onClick={() => handleApprove(p.id)}
                                disabled={isProcessing}
                                className="inline-flex items-center px-3 py-1.5 bg-emerald-600 hover:bg-emerald-700 disabled:opacity-50 text-white text-xs font-bold rounded-lg transition-colors cursor-pointer shadow-2xs"
                              >
                                <Check className="w-3.5 h-3.5 mr-1" /> Duyệt
                              </button>
                              <button
                                onClick={() => { setRejectId(p.id); setRejectReason(''); }}
                                disabled={isProcessing}
                                className="inline-flex items-center px-3 py-1.5 bg-white border border-rose-300 text-rose-600 hover:bg-rose-50 disabled:opacity-50 text-xs font-bold rounded-lg transition-colors cursor-pointer"
                              >
                                <X className="w-3.5 h-3.5 mr-1" /> Từ chối
                              </button>
                            </div>
                          ) : (
                            <span className="text-xs text-gray-400">Đã xử lý</span>
                          )}
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>

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
          </>
        )}
      </div>

      {rejectId && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-gray-900/50 backdrop-blur-xs">
          <div className="bg-white rounded-2xl max-w-md w-full p-6 shadow-2xl space-y-4">
            <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2 text-rose-600">
              <XCircle className="w-5 h-5" />
              Từ Chối Lệnh Rút Tiền #{rejectId}
            </h3>
            <p className="text-xs text-gray-500">
              Vui lòng nhập lý do từ chối. Số tiền sẽ được hoàn lại trực tiếp vào số dư khả dụng của Shop.
            </p>

            <form onSubmit={handleRejectSubmit} className="space-y-4">
              <div>
                <label className="block text-xs font-bold text-gray-700 mb-1">
                  Lý do từ chối <span className="text-rose-500">*</span>
                </label>
                <textarea
                  required
                  rows={3}
                  placeholder="Ví dụ: Số tài khoản và tên chủ thẻ không trùng khớp..."
                  value={rejectReason}
                  onChange={(e) => setRejectReason(e.target.value)}
                  className="w-full px-3 py-2 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none"
                />
              </div>

              <div className="flex items-center justify-end gap-3 pt-2">
                <button
                  type="button"
                  onClick={() => setRejectId(null)}
                  className="px-4 py-2 text-sm font-semibold text-gray-600 hover:bg-gray-100 rounded-xl transition-colors cursor-pointer"
                >
                  Hủy bỏ
                </button>
                <button
                  type="submit"
                  disabled={processingId !== null}
                  className="px-4 py-2 text-sm font-bold text-white bg-rose-600 hover:bg-rose-700 rounded-xl transition-colors cursor-pointer"
                >
                  Xác nhận từ chối
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};
