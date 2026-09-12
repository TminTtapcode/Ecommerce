import { useCallback, useEffect, useMemo, useState } from 'react';
import { getApiError } from '../../api/apiError';
import { vendorAnalyticsApi, type VendorAnalytics } from '../../api/vendorAnalyticsApi';
import { VendorNav } from '../../components/vendor/VendorNav';

const STATUS_LABELS: Record<string, string> = {
  PENDING: 'Chờ thanh toán', CONFIRMED: 'Đã xác nhận', SHIPPED: 'Đang giao',
  DELIVERED: 'Hoàn thành', CANCELLED: 'Đã hủy',
};
const STATUS_COLORS: Record<string, string> = {
  PENDING: 'bg-amber-400', CONFIRMED: 'bg-blue-500', SHIPPED: 'bg-indigo-500',
  DELIVERED: 'bg-emerald-500', CANCELLED: 'bg-rose-500',
};

const today = () => new Date().toISOString().slice(0, 10);
const thirtyDaysAgo = () => {
  const date = new Date(); date.setDate(date.getDate() - 29);
  return date.toISOString().slice(0, 10);
};
const money = (value: number) => `${Number(value ?? 0).toLocaleString('vi-VN')}₫`;

function KpiCard({ title, value, detail, tone = 'text-orange-600' }: { title: string; value: string; detail: string; tone?: string }) {
  return <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
    <p className="text-sm font-medium text-gray-500">{title}</p>
    <p className={`mt-2 text-2xl font-bold ${tone}`}>{value}</p>
    <p className="mt-1 text-xs text-gray-400">{detail}</p>
  </section>;
}

export const VendorAnalyticsPage = () => {
  const [startDate, setStartDate] = useState(thirtyDaysAgo());
  const [endDate, setEndDate] = useState(today());
  const [stats, setStats] = useState<VendorAnalytics | null>(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setLoading(true); setError(null);
    try {
      const response = await vendorAnalyticsApi.get(startDate || undefined, endDate || undefined);
      if (!response.data.data) throw new Error('Thiếu dữ liệu thống kê');
      setStats(response.data.data);
    } catch (err) {
      setError(getApiError(err, 'Không thể tải thống kê gian hàng. Vui lòng thử lại.').message);
    } finally { setLoading(false); }
  }, [startDate, endDate]);

  useEffect(() => { void load(); }, [load]);

  const maxDailyCount = useMemo(() => Math.max(1, ...(stats?.dailyChart.map(x => x.createdOrderCount) ?? [0])), [stats]);
  const empty = stats?.totalOrders === 0;

  return <div className="mx-auto max-w-7xl px-4 py-8">
    <VendorNav />
    <header className="mb-6 flex flex-wrap items-end justify-between gap-4">
      <div>
        <h1 className="text-2xl font-bold text-gray-900">Thống kê gian hàng</h1>
        <p className="mt-1 text-sm text-gray-500">Số liệu được lọc theo <strong>ngày tạo đơn</strong>, không phải ngày giao hàng hoặc tiền thực thu.</p>
      </div>
      <div className="flex flex-wrap items-end gap-2 rounded-lg border border-gray-200 bg-white p-3 shadow-sm">
        <label className="text-xs font-medium text-gray-600">Từ
          <input aria-label="Từ ngày" type="date" value={startDate} max={endDate || today()} onChange={e => setStartDate(e.target.value)} className="ml-2 rounded border border-gray-300 px-2 py-1.5 text-sm" />
        </label>
        <label className="text-xs font-medium text-gray-600">Đến
          <input aria-label="Đến ngày" type="date" value={endDate} min={startDate} max={today()} onChange={e => setEndDate(e.target.value)} className="ml-2 rounded border border-gray-300 px-2 py-1.5 text-sm" />
        </label>
        <button type="button" onClick={() => void load()} disabled={loading} className="rounded bg-orange-500 px-3 py-1.5 text-sm font-semibold text-white hover:bg-orange-600 disabled:cursor-not-allowed disabled:bg-orange-300">
          {loading ? 'Đang tải...' : 'Làm mới'}
        </button>
      </div>
    </header>

    {error && <div role="alert" className="mb-6 rounded-lg border border-red-200 bg-red-50 p-4 text-red-700">
      <p>{error}</p><button type="button" onClick={() => void load()} className="mt-2 font-semibold underline">Thử lại</button>
    </div>}

    {loading && !stats && <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
      {Array.from({ length: 6 }, (_, i) => <div key={i} className="h-32 animate-pulse rounded-xl bg-gray-200" />)}
    </div>}

    {stats && <>
      <div className="grid grid-cols-1 gap-4 sm:grid-cols-2 lg:grid-cols-3">
        <KpiCard title="Tổng đơn" value={stats.totalOrders.toLocaleString('vi-VN')} detail="Trong khoảng ngày tạo đơn" />
        <KpiCard title="Đơn hoàn thành" value={stats.deliveredOrderCount.toLocaleString('vi-VN')} detail="Trạng thái DELIVERED hiện tại" tone="text-emerald-600" />
        <KpiCard title="Giá trị trước giảm giá" value={money(stats.fulfilledGrossOrderValue)} detail="Chỉ đơn DELIVERED" tone="text-slate-700" />
        <KpiCard title="Giảm giá voucher" value={money(stats.voucherDiscountAmount)} detail="Phân bổ ở cấp đơn của shop" tone="text-rose-600" />
        <KpiCard title="Giá trị đơn hoàn tất sau giảm giá" value={money(stats.fulfilledOrderValue)} detail="Không phải lợi nhuận hay tiền thực thu" tone="text-emerald-600" />
        <KpiCard title="AOV đơn hoàn tất" value={money(stats.averageFulfilledOrderValue)} detail="Giá trị sau giảm giá / đơn DELIVERED" tone="text-indigo-600" />
      </div>

      {empty ? <div className="mt-6 rounded-xl border border-dashed border-gray-300 bg-white px-6 py-14 text-center">
        <h2 className="text-lg font-semibold text-gray-700">Chưa có đơn hàng trong khoảng đã chọn</h2>
        <p className="mt-2 text-sm text-gray-500">Thay đổi khoảng ngày hoặc quay lại khi gian hàng có đơn mới.</p>
      </div> : <div className="mt-6 grid grid-cols-1 gap-6 lg:grid-cols-2">
        <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
          <h2 className="font-semibold text-gray-900">Đơn hàng theo trạng thái</h2>
          <div className="mt-5 space-y-3">
            {Object.entries(stats.ordersByStatus).map(([status, count]) => {
              const width = stats.totalOrders ? Math.round((count / stats.totalOrders) * 100) : 0;
              return <div key={status}>
                <div className="mb-1 flex justify-between text-sm"><span className="text-gray-600">{STATUS_LABELS[status] ?? status}</span><strong>{count}</strong></div>
                <div className="h-2 rounded-full bg-gray-100"><div className={`h-2 rounded-full ${STATUS_COLORS[status] ?? 'bg-gray-400'}`} style={{ width: `${width}%` }} /></div>
              </div>;
            })}
          </div>
        </section>
        <section className="rounded-xl border border-gray-200 bg-white p-5 shadow-sm">
          <h2 className="font-semibold text-gray-900">Số đơn theo ngày tạo</h2>
          <p className="mt-1 text-xs text-gray-500">Cột thể hiện số đơn; giá trị hoàn tất sau giảm giá hiển thị bên dưới.</p>
          <div className="mt-5 flex min-h-52 items-end gap-2 overflow-x-auto pb-6">
            {stats.dailyChart.map(point => <div key={point.date} className="flex min-w-12 flex-1 flex-col items-center justify-end gap-2" title={`${point.date}: ${point.createdOrderCount} đơn`}>
              <span className="text-xs font-semibold text-gray-700">{point.createdOrderCount}</span>
              <div className="w-full max-w-9 rounded-t bg-orange-400" style={{ height: `${Math.max(8, (point.createdOrderCount / maxDailyCount) * 140)}px` }} />
              <span className="whitespace-nowrap text-[10px] text-gray-500">{point.date.slice(5)}</span>
              <span className="whitespace-nowrap text-[10px] text-emerald-700">{money(point.fulfilledOrderValue)}</span>
            </div>)}
          </div>
        </section>
      </div>}
    </>}
  </div>;
};
