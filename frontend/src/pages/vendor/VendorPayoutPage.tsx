import React, { useState, useEffect } from 'react';
import { 
  Wallet, 
  ArrowUpRight, 
  CheckCircle2, 
  Clock, 
  XCircle, 
  Building, 
  AlertCircle, 
  CreditCard,
  RefreshCw,
  Info
} from 'lucide-react';
import { payoutApi } from '../../api/payoutApi';
import { getApiError } from '../../api/apiError';
import type { PayoutBalanceResponse, PayoutResponse } from '../../api/types/payout.types';

const POPULAR_BANKS = [
  'Vietcombank (Ngân hàng Ngoại thương)',
  'MB Bank (Ngân hàng Quân Đội)',
  'Techcombank (Ngân hàng Kỹ thương)',
  'BIDV (Ngân hàng Đầu tư và Phát triển)',
  'VietinBank (Ngân hàng Công thương)',
  'ACB (Ngân hàng Á Châu)',
  'VPBank (Ngân hàng Việt Nam Thịnh Vượng)',
  'TPBank (Ngân hàng Tiên Phong)',
  'HDBank (Ngân hàng Phát triển TP.HCM)',
  'Sacombank (Ngân hàng Sài Gòn Thương Tín)'
];

const fmtCurrency = (v?: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v ?? 0);

const STATUS_CONFIG: Record<string, { label: string; bg: string; text: string; icon: React.ComponentType<{ className?: string }> }> = {
  PENDING: { label: 'Chờ duyệt', bg: 'bg-amber-50 border-amber-200', text: 'text-amber-700', icon: Clock },
  APPROVED: { label: 'Đã chuyển tiền', bg: 'bg-emerald-50 border-emerald-200', text: 'text-emerald-700', icon: CheckCircle2 },
  REJECTED: { label: 'Bị từ chối', bg: 'bg-rose-50 border-rose-200', text: 'text-rose-700', icon: XCircle },
};

export const VendorPayoutPage: React.FC = () => {
  const [balance, setBalance] = useState<PayoutBalanceResponse | null>(null);
  const [payouts, setPayouts] = useState<PayoutResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [errorMsg, setErrorMsg] = useState('');
  const [successMsg, setSuccessMsg] = useState('');

  const [amount, setAmount] = useState<number | ''>('');
  const [bankName, setBankName] = useState(POPULAR_BANKS[0]);
  const [bankAccountNumber, setBankAccountNumber] = useState('');
  const [bankAccountHolder, setBankAccountHolder] = useState('');

  const fetchData = async () => {
    setLoading(true);
    setErrorMsg('');
    try {
      const [balanceRes, payoutsRes] = await Promise.all([
        payoutApi.getShopBalance(),
        payoutApi.getMyPayouts(0, 15)
      ]);

      if (balanceRes.data) {
        setBalance(balanceRes.data);
      }
      if (payoutsRes.data?.content) {
        setPayouts(payoutsRes.data.content);
        if (payoutsRes.data.content.length > 0) {
          const last = payoutsRes.data.content[0];
          setBankName(last.bankName || POPULAR_BANKS[0]);
          setBankAccountNumber(last.bankAccountNumber || '');
          setBankAccountHolder(last.bankAccountHolder || '');
        }
      }
    } catch (err: unknown) {
      setErrorMsg(getApiError(err, 'Lỗi tải thông tin tài chính').message);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchData();
  }, []);

  const handleWithdrawAll = () => {
    if (balance && balance.availableBalance > 0) {
      setAmount(balance.availableBalance);
    }
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMsg('');
    setSuccessMsg('');

    if (!amount || Number(amount) < 50000) {
      setErrorMsg('Số tiền rút tối thiểu là 50.000 VNĐ');
      return;
    }

    if (balance && Number(amount) > balance.availableBalance) {
      setErrorMsg('Số tiền rút không được vượt quá số dư khả dụng');
      return;
    }

    if (!bankAccountNumber.trim() || !bankAccountHolder.trim()) {
      setErrorMsg('Vui lòng điền đầy đủ số tài khoản và tên chủ tài khoản');
      return;
    }

    setSubmitting(true);
    try {
      await payoutApi.createPayout({
        amount: Number(amount),
        bankName,
        bankAccountNumber: bankAccountNumber.trim(),
        bankAccountHolder: bankAccountHolder.trim().toUpperCase()
      });

      setSuccessMsg('Gửi yêu cầu rút tiền thành công! Admin sẽ kiểm tra và chuyển khoản cho bạn sớm nhất.');
      setAmount('');
      fetchData();
    } catch (err: unknown) {
      setErrorMsg(getApiError(err, 'Có lỗi xảy ra khi tạo yêu cầu rút tiền.').message);
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
        <div>
          <h1 className="text-2xl font-bold text-gray-900 flex items-center gap-2.5">
            <Wallet className="w-7 h-7 text-orange-500" />
            Tài Chính & Rút Tiền
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Quản lý số dư thực nhận sau chiết khấu sàn và tạo lệnh rút tiền về tài khoản ngân hàng
          </p>
        </div>
        <button
          onClick={fetchData}
          disabled={loading}
          className="inline-flex items-center px-3.5 py-2 bg-white border border-gray-200 text-gray-700 text-sm font-medium rounded-xl hover:bg-gray-50 hover:border-gray-300 transition-colors cursor-pointer self-start sm:self-auto shadow-xs"
        >
          <RefreshCw className={`w-4 h-4 mr-2 text-gray-500 ${loading ? 'animate-spin' : ''}`} />
          Làm mới
        </button>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-gradient-to-br from-orange-500 via-orange-600 to-amber-600 rounded-3xl p-6 sm:p-8 text-white shadow-md relative overflow-hidden flex flex-col justify-between">
          <div className="relative z-10">
            <div className="flex items-center justify-between">
              <span className="text-xs sm:text-sm font-bold tracking-wider uppercase text-orange-100 flex items-center gap-1.5">
                <CreditCard className="w-4 h-4" /> Số Dư Khả Dụng Có Thể Rút
              </span>
              <span className="px-3 py-1 rounded-full text-xs font-bold bg-white/20 backdrop-blur-xs border border-white/30 text-white">
                Chiết khấu Sàn: 5%
              </span>
            </div>
            <div className="mt-4 text-3xl sm:text-5xl font-black tracking-tight">
              {loading ? '...' : fmtCurrency(balance?.availableBalance)}
            </div>
            <p className="text-xs sm:text-sm text-orange-100/90 mt-2 flex items-center gap-1.5">
              <Info className="w-4 h-4 shrink-0" />
              Chỉ tính từ các đơn giao hàng thành công (DELIVERED) đã trừ 5% hoa hồng sàn
            </p>
          </div>

          <div className="relative z-10 mt-6 pt-6 border-t border-white/20 grid grid-cols-2 sm:grid-cols-4 gap-4">
            <div>
              <p className="text-[11px] text-orange-200 uppercase font-semibold">Doanh thu đơn giao</p>
              <p className="text-sm sm:text-base font-bold text-white mt-0.5">
                {loading ? '...' : fmtCurrency(balance?.grossDeliveredRevenue)}
              </p>
            </div>
            <div>
              <p className="text-[11px] text-orange-200 uppercase font-semibold">Hoa hồng sàn (5%)</p>
              <p className="text-sm sm:text-base font-bold text-orange-200 mt-0.5">
                - {loading ? '...' : fmtCurrency(balance?.totalCommission)}
              </p>
            </div>
            <div>
              <p className="text-[11px] text-orange-200 uppercase font-semibold">Đã rút về TK</p>
              <p className="text-sm sm:text-base font-bold text-white mt-0.5">
                {loading ? '...' : fmtCurrency(balance?.totalWithdrawn)}
              </p>
            </div>
            <div>
              <p className="text-[11px] text-orange-200 uppercase font-semibold">Đang chờ duyệt</p>
              <p className="text-sm sm:text-base font-bold text-amber-200 mt-0.5">
                {loading ? '...' : fmtCurrency(balance?.pendingWithdrawal)}
              </p>
            </div>
          </div>
        </div>

        <div className="bg-white rounded-3xl p-6 border border-gray-200 shadow-xs flex flex-col justify-between space-y-4">
          <div>
            <h2 className="text-base font-bold text-gray-900 flex items-center gap-2">
              <Building className="w-5 h-5 text-orange-500" />
              Chính Sách Quyết Toán
            </h2>
            <ul className="mt-3 space-y-2.5 text-xs text-gray-600">
              <li className="flex items-start gap-2">
                <span className="text-orange-500 font-bold shrink-0">✓</span>
                <span>Tiền đơn hàng được giữ ký quỹ tại Sàn cho đến khi đơn hoàn thành (DELIVERED).</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-orange-500 font-bold shrink-0">✓</span>
                <span>Hệ thống tự động trừ <strong>5% chiết khấu sàn</strong> để duy trì nền tảng và cổng thanh toán.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-orange-500 font-bold shrink-0">✓</span>
                <span>Hạn mức rút tối thiểu: <strong>50.000 VNĐ</strong> / lần rút.</span>
              </li>
              <li className="flex items-start gap-2">
                <span className="text-orange-500 font-bold shrink-0">✓</span>
                <span>Admin sẽ kiểm tra và chuyển khoản trong vòng 24 giờ làm việc.</span>
              </li>
            </ul>
          </div>

          <div className="p-3.5 rounded-2xl bg-amber-50/70 border border-amber-200/80 text-xs text-amber-800">
            <strong>Lưu ý:</strong> Thông tin tài khoản ngân hàng sẽ được lưu lại cho lần rút tiếp theo để bạn không cần nhập lại.
          </div>
        </div>
      </div>

      <div className="bg-white rounded-2xl p-6 sm:p-8 border border-gray-200 shadow-xs">
        <h2 className="text-lg font-bold text-gray-900 flex items-center gap-2 mb-1">
          <ArrowUpRight className="w-5 h-5 text-orange-500" />
          Tạo Lệnh Rút Tiền Về Tài Khoản Ngân Hàng
        </h2>
        <p className="text-xs text-gray-500 mb-6">
          Nhập số tiền và thông tin tài khoản ngân hàng nhận tiền của chủ gian hàng
        </p>

        {errorMsg && (
          <div className="mb-5 p-4 rounded-xl bg-rose-50 border border-rose-200 text-rose-700 text-sm flex items-center gap-2">
            <AlertCircle className="w-5 h-5 shrink-0 text-rose-500" />
            <span>{errorMsg}</span>
          </div>
        )}

        {successMsg && (
          <div className="mb-5 p-4 rounded-xl bg-emerald-50 border border-emerald-200 text-emerald-700 text-sm flex items-center gap-2">
            <CheckCircle2 className="w-5 h-5 shrink-0 text-emerald-500" />
            <span>{successMsg}</span>
          </div>
        )}

        <form onSubmit={handleSubmit} className="space-y-6">
          <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
            <div className="md:col-span-2">
              <div className="flex items-center justify-between mb-2">
                <label className="text-sm font-semibold text-gray-700">
                  Số tiền muốn rút (VNĐ) <span className="text-rose-500">*</span>
                </label>
                <button
                  type="button"
                  onClick={handleWithdrawAll}
                  className="text-xs font-bold text-orange-600 hover:text-orange-700 cursor-pointer"
                >
                  Rút toàn bộ số dư ({fmtCurrency(balance?.availableBalance)})
                </button>
              </div>
              <div className="relative">
                <input
                  type="number"
                  min="50000"
                  step="1000"
                  placeholder="Ví dụ: 200000 (Tối thiểu 50.000đ)"
                  value={amount}
                  onChange={(e) => setAmount(e.target.value === '' ? '' : Number(e.target.value))}
                  className="w-full px-4 py-3 text-lg font-bold border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-gray-900"
                />
                <span className="absolute right-4 top-3.5 text-sm font-bold text-gray-400">VNĐ</span>
              </div>
              <div className="flex flex-wrap gap-2 mt-2.5">
                {[100000, 200000, 500000, 1000000].map((quickVal) => (
                  <button
                    key={quickVal}
                    type="button"
                    onClick={() => setAmount(quickVal)}
                    className="px-3 py-1 text-xs font-medium bg-gray-100 hover:bg-orange-50 hover:text-orange-600 rounded-lg transition-colors cursor-pointer"
                  >
                    {fmtCurrency(quickVal)}
                  </button>
                ))}
              </div>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Ngân hàng nhận tiền <span className="text-rose-500">*</span>
              </label>
              <select
                value={bankName}
                onChange={(e) => setBankName(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none bg-white text-gray-800"
              >
                {POPULAR_BANKS.map((b) => (
                  <option key={b} value={b}>{b}</option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Số tài khoản ngân hàng <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                placeholder="Ví dụ: 0123456789"
                value={bankAccountNumber}
                onChange={(e) => setBankAccountNumber(e.target.value)}
                className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-gray-800 font-mono font-semibold"
              />
            </div>

            <div className="md:col-span-2">
              <label className="block text-sm font-semibold text-gray-700 mb-2">
                Tên chủ tài khoản (Người thụ hưởng) <span className="text-rose-500">*</span>
              </label>
              <input
                type="text"
                placeholder="Ví dụ: NGUYEN VAN A"
                value={bankAccountHolder}
                onChange={(e) => setBankAccountHolder(e.target.value.toUpperCase())}
                className="w-full px-3.5 py-2.5 text-sm border border-gray-300 rounded-xl focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-gray-800 uppercase font-bold"
              />
              <p className="text-xs text-gray-400 mt-1">
                Lưu ý: Tên chủ tài khoản phải trùng với tài khoản ngân hàng đã đăng ký để tránh bị ngân hàng từ chối.
              </p>
            </div>
          </div>

          <button
            type="submit"
            disabled={submitting || !balance || balance.availableBalance < 50000}
            className="w-full sm:w-auto px-8 py-3 bg-orange-500 hover:bg-orange-600 disabled:bg-gray-300 disabled:cursor-not-allowed text-white text-sm font-bold rounded-xl shadow-xs transition-colors cursor-pointer"
          >
            {submitting ? 'Đang gửi yêu cầu...' : 'Xác Nhận Rút Tiền'}
          </button>
        </form>
      </div>

      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
        <h2 className="text-base font-bold text-gray-900 mb-1">Lịch Sử Rút Tiền</h2>
        <p className="text-xs text-gray-500 mb-4">Danh sách các yêu cầu rút tiền của gian hàng</p>

        {loading ? (
          <div className="py-12 text-center text-gray-400">Đang tải lịch sử...</div>
        ) : payouts.length === 0 ? (
          <div className="py-12 text-center text-gray-400 border border-dashed border-gray-200 rounded-xl">
            Chưa có yêu cầu rút tiền nào được tạo
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left text-sm">
              <thead>
                <tr className="border-b border-gray-100 text-xs font-semibold text-gray-400 uppercase">
                  <th className="pb-3">Mã lệnh</th>
                  <th className="pb-3">Số tiền</th>
                  <th className="pb-3">Thông tin nhận tiền</th>
                  <th className="pb-3">Thời gian tạo</th>
                  <th className="pb-3">Trạng thái</th>
                  <th className="pb-3">Ghi chú</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {payouts.map((p) => {
                  const cfg = STATUS_CONFIG[p.status] || STATUS_CONFIG.PENDING;
                  const Icon = cfg.icon;
                  return (
                    <tr key={p.id} className="hover:bg-gray-50/80 transition-colors">
                      <td className="py-3.5 font-bold text-gray-900">
                        #{p.id}
                      </td>
                      <td className="py-3.5 font-bold text-orange-600 text-base">
                        {fmtCurrency(p.amount)}
                      </td>
                      <td className="py-3.5 text-gray-700">
                        <p className="font-semibold text-gray-900">{p.bankName}</p>
                        <p className="text-xs font-mono text-gray-500">STK: {p.bankAccountNumber} - {p.bankAccountHolder}</p>
                      </td>
                      <td className="py-3.5 text-xs text-gray-500">
                        {new Date(p.createdAt).toLocaleString('vi-VN')}
                      </td>
                      <td className="py-3.5">
                        <span className={`inline-flex items-center gap-1.5 px-3 py-1 rounded-full text-xs font-bold border ${cfg.bg} ${cfg.text}`}>
                          <Icon className="w-3.5 h-3.5" />
                          {cfg.label}
                        </span>
                      </td>
                      <td className="py-3.5 text-xs text-gray-500 max-w-xs truncate">
                        {p.adminNote || '-'}
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}
      </div>
    </div>
  );
};
