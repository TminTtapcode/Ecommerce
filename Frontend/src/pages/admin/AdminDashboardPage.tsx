import { useState, useEffect, useCallback } from 'react';
import { adminApi, type AdminDashboardStats, type RecentOrder, type DailyChartPoint } from '../../api/adminApi';

const STATUS_CONFIG: Record<string, { label: string; color: string; bg: string }> = {
  PENDING:   { label: 'Chờ xác nhận', color: '#d97706', bg: '#fef3c7' },
  CONFIRMED: { label: 'Đã xác nhận',  color: '#2563eb', bg: '#dbeafe' },
  SHIPPED:   { label: 'Đang giao',    color: '#7c3aed', bg: '#ede9fe' },
  DELIVERED: { label: 'Hoàn thành',   color: '#059669', bg: '#d1fae5' },
  CANCELLED: { label: 'Đã hủy',       color: '#dc2626', bg: '#fee2e2' },
};

const fmtCurrency = (v: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(v);

const fmtNumber = (v: number) =>
  new Intl.NumberFormat('vi-VN').format(v);

const today = () => new Date().toISOString().split('T')[0];
const thirtyDaysAgo = () => {
  const d = new Date(); d.setDate(d.getDate() - 29); return d.toISOString().split('T')[0];
};

function KpiCard({ title, value, sub, icon, accent }: {
  title: string; value: string; sub?: string; icon: string; accent: string;
}) {
  return (
    <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-xs flex flex-col justify-between transition-all hover:shadow-md hover:border-gray-300">
      <div className="flex items-center justify-between gap-2 mb-3">
        <span className="text-xs font-semibold text-gray-500 uppercase tracking-wider">{title}</span>
        <div
          className="w-10 h-10 rounded-xl flex items-center justify-center text-xl flex-shrink-0 shadow-xs"
          style={{ background: `${accent}15` }}
        >
          {icon}
        </div>
      </div>
      <div>
        <div className="text-xl sm:text-2xl font-black text-gray-900 tracking-tight whitespace-nowrap">
          {value}
        </div>
        {sub ? (
          <p className="text-xs text-gray-400 mt-1 font-medium truncate" title={sub}>{sub}</p>
        ) : (
          <div className="h-4 mt-1" />
        )}
      </div>
    </div>
  );
}

function OrderStatusBar({ ordersByStatus, total }: {
  ordersByStatus: Record<string, number>; total: number;
}) {
  if (total === 0) return (
    <p className="text-gray-400 text-center py-6 text-sm">Không có đơn hàng trong khoảng thời gian này.</p>
  );
  return (
    <div className="flex flex-col gap-3">
      {Object.entries(STATUS_CONFIG).map(([key, cfg]) => {
        const count = ordersByStatus[key] ?? 0;
        const pct = total > 0 ? Math.round((count / total) * 100) : 0;
        return (
          <div key={key} className="flex items-center gap-3 text-sm">
            <span
              className="min-w-[110px] text-xs font-semibold px-2.5 py-1 rounded-md"
              style={{ color: cfg.color, background: cfg.bg }}
            >
              {cfg.label}
            </span>
            <div className="flex-1 bg-gray-100 rounded-full h-2 overflow-hidden">
              <div
                className="h-full rounded-full transition-all duration-500"
                style={{ width: `${pct}%`, background: cfg.color }}
              />
            </div>
            <span className="min-w-[60px] text-right text-gray-600 font-medium text-xs">
              {fmtNumber(count)} ({pct}%)
            </span>
          </div>
        );
      })}
    </div>
  );
}

function DailyChart({ data }: { data: DailyChartPoint[] }) {
  if (!data.length) return (
    <p className="text-gray-400 text-center py-6 text-sm">Không có dữ liệu biểu đồ.</p>
  );
  const maxCount = Math.max(...data.map(d => d.orderCount), 1);
  return (
    <div className="flex items-end gap-1.5 h-32 overflow-x-auto pb-2 pt-4">
      {data.map(pt => {
        const h = Math.max(Math.round((pt.orderCount / maxCount) * 100), 6);
        return (
          <div key={pt.date} className="flex flex-col items-center gap-1 min-w-[28px] flex-1">
            <span className="text-[10px] text-gray-500 font-semibold">{pt.orderCount > 0 ? pt.orderCount : ''}</span>
            <div
              title={`${pt.date}: ${pt.orderCount} đơn`}
              className="w-full max-w-[22px] rounded-t-md bg-gradient-to-b from-orange-500 to-amber-400 transition-all duration-300"
              style={{ height: `${h}%` }}
            />
            <span className="text-[9px] text-gray-400 -rotate-45 whitespace-nowrap mt-2">
              {pt.date.slice(5)}
            </span>
          </div>
        );
      })}
    </div>
  );
}

function RecentOrdersTable({ orders }: { orders: RecentOrder[] }) {
  if (!orders.length) return (
    <p className="text-gray-400 text-center py-6 text-sm">Chưa có đơn hàng nào.</p>
  );
  return (
    <div className="overflow-x-auto">
      <table className="w-full text-left text-sm">
        <thead>
          <tr className="border-b border-gray-200 bg-gray-50/50">
            {['ID', 'Khách hàng', 'Shop', 'Trạng thái', 'Tổng tiền', 'Thanh toán', 'Ngày đặt'].map(h => (
              <th key={h} className="p-3 text-xs font-semibold text-gray-500 uppercase tracking-wider">{h}</th>
            ))}
          </tr>
        </thead>
        <tbody className="divide-y divide-gray-100">
          {orders.map((o) => {
            const cfg = STATUS_CONFIG[o.status];
            return (
              <tr key={o.id} className="hover:bg-gray-50/60 transition-colors">
                <td className="p-3 text-orange-600 font-bold">#{o.id}</td>
                <td className="p-3 text-gray-700">#{o.userId}</td>
                <td className="p-3 text-gray-700">#{o.shopId}</td>
                <td className="p-3">
                  <span
                    className="px-2.5 py-0.5 rounded-full text-xs font-semibold"
                    style={{ background: cfg?.bg ?? '#f1f5f9', color: cfg?.color ?? '#475569' }}
                  >
                    {cfg?.label ?? o.status}
                  </span>
                </td>
                <td className="p-3 text-emerald-600 font-bold">{fmtCurrency(o.totalAmount)}</td>
                <td className="p-3 text-gray-600 text-xs">{o.paymentMethod}</td>
                <td className="p-3 text-gray-400 text-xs">
                  {new Date(o.createdAt).toLocaleString('vi-VN', { hour12: false })}
                </td>
              </tr>
            );
          })}
        </tbody>
      </table>
    </div>
  );
}

export default function AdminDashboardPage() {
  const [stats, setStats]       = useState<AdminDashboardStats | null>(null);
  const [loading, setLoading]   = useState(true);
  const [error, setError]       = useState<string | null>(null);
  const [startDate, setStartDate] = useState(thirtyDaysAgo());
  const [endDate, setEndDate]     = useState(today());

  const load = useCallback(async () => {
    setLoading(true);
    setError(null);
    try {
      const res = await adminApi.getDashboardStats(startDate, endDate);
      setStats(res.data.data);
    } catch {
      setError('Không thể tải dữ liệu thống kê. Vui lòng thử lại.');
    } finally {
      setLoading(false);
    }
  }, [startDate, endDate]);

  useEffect(() => { load(); }, [load]);

  return (
    <div className="text-gray-900 font-['Outfit',sans-serif]">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h1 className="text-2xl font-extrabold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent">
            📊 Tổng Quan Hệ Thống
          </h1>
          <p className="text-sm text-gray-500 mt-1">Báo cáo và số liệu hoạt động toàn sàn</p>
        </div>

        <div className="flex items-center gap-3 flex-wrap">
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-500 font-medium">Từ:</label>
            <input
              type="date"
              value={startDate}
              max={endDate}
              onChange={e => setStartDate(e.target.value)}
              className="bg-white border border-gray-300 rounded-lg text-gray-800 px-3 py-1.5 text-xs cursor-pointer focus:border-orange-500 focus:outline-none"
            />
          </div>
          <div className="flex items-center gap-2">
            <label className="text-xs text-gray-500 font-medium">Đến:</label>
            <input
              type="date"
              value={endDate}
              min={startDate}
              max={today()}
              onChange={e => setEndDate(e.target.value)}
              className="bg-white border border-gray-300 rounded-lg text-gray-800 px-3 py-1.5 text-xs cursor-pointer focus:border-orange-500 focus:outline-none"
            />
          </div>
          <button
            onClick={load}
            disabled={loading}
            className="px-4 py-1.5 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white font-semibold text-xs rounded-lg shadow-sm disabled:opacity-50 transition-all cursor-pointer"
          >
            {loading ? '⏳ Đang tải...' : '🔄 Làm mới'}
          </button>
        </div>
      </div>

      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 p-4 rounded-xl mb-6 text-sm">
          ⚠️ {error}
        </div>
      )}

      {loading && !stats && (
        <div className="grid grid-cols-2 md:grid-cols-3 2xl:grid-cols-6 gap-4 mb-6">
          {[...Array(6)].map((_, i) => (
            <div key={i} className="bg-white border border-gray-200 rounded-2xl h-28 animate-pulse" />
          ))}
        </div>
      )}

      {stats && (
        <>
          <div className="grid grid-cols-2 md:grid-cols-3 2xl:grid-cols-6 gap-4 mb-6">
            <KpiCard title="Người dùng" value={fmtNumber(stats.totalUsers)}       icon="👤" accent="#ea580c" />
            <KpiCard title="Gian hàng"  value={fmtNumber(stats.totalShops)}       icon="🏪" accent="#8b5cf6" />
            <KpiCard title="Sản phẩm"   value={fmtNumber(stats.totalProducts)}    icon="📦" accent="#06b6d4" />
            <KpiCard title="Đơn hàng"   value={fmtNumber(stats.totalOrders)}      icon="🛒" accent="#f59e0b" sub={`${startDate} → ${endDate}`} />
            <KpiCard title="Doanh thu"   value={fmtCurrency(stats.deliveredRevenue)} icon="💰" accent="#10b981" sub="Đơn DELIVERED" />
            <KpiCard title="AOV"         value={fmtCurrency(stats.averageOrderValue)} icon="📈" accent="#ec4899" sub="Giá trị TB" />
          </div>

          <div className="grid grid-cols-1 lg:grid-cols-2 gap-5 mb-6">
            <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
              <h3 className="text-base font-bold text-gray-900 mb-4 flex items-center gap-2">
                📋 Đơn hàng theo trạng thái
              </h3>
              <OrderStatusBar ordersByStatus={stats.ordersByStatus} total={stats.totalOrders} />
            </div>

            <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
              <h3 className="text-base font-bold text-gray-900 mb-4 flex items-center gap-2">
                📅 Đơn hàng theo ngày
              </h3>
              <DailyChart data={stats.dailyChart} />
            </div>
          </div>

          <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
            <h3 className="text-base font-bold text-gray-900 mb-4 flex items-center gap-2">
              🕐 10 đơn hàng gần nhất
            </h3>
            <RecentOrdersTable orders={stats.recentOrders} />
          </div>
        </>
      )}
    </div>
  );
}
