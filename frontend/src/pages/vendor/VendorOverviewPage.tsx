import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { 
  Package, 
  ShoppingBag, 
  CheckCircle, 
  Clock, 
  Truck, 
  XCircle, 
  TrendingUp, 
  Tag, 
  Plus, 
  ArrowRight,
  Store,
  AlertCircle,
  Wallet
} from 'lucide-react';
import { shopApi } from '../../api/shopApi';
import { orderApi } from '../../api/orderApi';
import { vendorApi } from '../../api/vendorApi';
import { vendorAnalyticsApi, type VendorAnalytics } from '../../api/vendorAnalyticsApi';
import type { ShopResponse } from '../../api/types/shop.types';
import type { OrderResponse } from '../../api/types/order.types';

const fmtCurrency = (val?: number) =>
  new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val ?? 0);

const STATUS_BADGE: Record<string, { label: string; className: string }> = {
  PENDING: { label: 'Chờ xác nhận', className: 'bg-amber-100 text-amber-700 border-amber-200' },
  CONFIRMED: { label: 'Đã xác nhận', className: 'bg-blue-100 text-blue-700 border-blue-200' },
  SHIPPED: { label: 'Đang giao', className: 'bg-purple-100 text-purple-700 border-purple-200' },
  DELIVERED: { label: 'Hoàn thành', className: 'bg-emerald-100 text-emerald-700 border-emerald-200' },
  CANCELLED: { label: 'Đã hủy', className: 'bg-rose-100 text-rose-700 border-rose-200' },
};

export const VendorOverviewPage: React.FC = () => {
  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [stats, setStats] = useState<VendorAnalytics | null>(null);
  const [recentOrders, setRecentOrders] = useState<OrderResponse[]>([]);
  const [totalProducts, setTotalProducts] = useState(0);
  const [loading, setLoading] = useState(true);

  const [pendingCount, setPendingCount] = useState(0);
  const [confirmedCount, setConfirmedCount] = useState(0);
  const [shippedCount, setShippedCount] = useState(0);
  const [cancelledCount, setCancelledCount] = useState(0);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const shopRes = await shopApi.getMyShop();
        if (shopRes.data?.data) {
          setShop(shopRes.data.data);
        }

        const statsRes = await vendorAnalyticsApi.get();
        if (statsRes.data?.data) {
          setStats(statsRes.data.data);
        }

        const ordersRes = await orderApi.getVendorOrders({ page: 0, size: 5 });
        if (ordersRes.data?.content) {
          setRecentOrders(ordersRes.data.content);
        }

        const [pendingRes, confirmedRes, shippedRes, cancelledRes, prodRes] = await Promise.all([
          orderApi.getVendorOrders({ page: 0, size: 1, status: 'PENDING' }),
          orderApi.getVendorOrders({ page: 0, size: 1, status: 'CONFIRMED' }),
          orderApi.getVendorOrders({ page: 0, size: 1, status: 'SHIPPED' }),
          orderApi.getVendorOrders({ page: 0, size: 1, status: 'CANCELLED' }),
          vendorApi.getProducts(0, 1),
        ]);

        setPendingCount(pendingRes.data?.totalElements ?? 0);
        setConfirmedCount(confirmedRes.data?.totalElements ?? 0);
        setShippedCount(shippedRes.data?.totalElements ?? 0);
        setCancelledCount(cancelledRes.data?.totalElements ?? 0);
        setTotalProducts(prodRes.data?.data?.totalElements ?? 0);
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6 space-y-6">
      <div className="bg-gradient-to-r from-orange-500 via-orange-600 to-amber-600 rounded-2xl p-6 text-white shadow-sm flex flex-col md:flex-row md:items-center md:justify-between gap-4">
        <div className="flex items-center space-x-4">
          <div className="w-14 h-14 rounded-2xl bg-white/15 backdrop-blur-xs flex items-center justify-center text-white border border-white/20 shadow-inner">
            <Store className="w-7 h-7" />
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-xl sm:text-2xl font-bold">
                {shop?.name || 'Kênh Người Bán'}
              </h1>
              <span className="px-2 py-0.5 rounded-full text-[11px] font-semibold bg-white/20 border border-white/30 text-white">
                {shop?.status === 'ACTIVE' ? 'Đang hoạt động' : 'Chờ duyệt'}
              </span>
            </div>
            <p className="text-sm text-orange-100/90 mt-0.5 line-clamp-1">
              {shop?.description || 'Chào mừng bạn đến với trang quản trị gian hàng'}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-3 self-end md:self-auto">
          <Link
            to="/vendor/products/new"
            className="inline-flex items-center px-4 py-2.5 rounded-xl bg-white text-orange-600 text-sm font-semibold hover:bg-orange-50 shadow-sm transition-all cursor-pointer"
          >
            <Plus className="w-4 h-4 mr-1.5" /> Thêm sản phẩm
          </Link>
          <Link
            to="/vendor/analytics"
            className="inline-flex items-center px-4 py-2.5 rounded-xl bg-black/20 hover:bg-black/30 border border-white/20 text-white text-sm font-semibold transition-all cursor-pointer"
          >
            Báo cáo chi tiết <ArrowRight className="w-4 h-4 ml-1.5" />
          </Link>
        </div>
      </div>

      <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
        <div className="flex items-center justify-between mb-4">
          <div>
            <h2 className="text-base font-bold text-gray-900 flex items-center gap-2">
              <Clock className="w-5 h-5 text-orange-500" />
              Danh Sách Cần Xử Lý
            </h2>
            <p className="text-xs text-gray-500 mt-0.5">Những đơn hàng cần bạn thao tác ngay</p>
          </div>
          <Link to="/vendor/orders" className="text-xs font-semibold text-orange-600 hover:text-orange-700 flex items-center gap-1">
            Xem tất cả đơn <ArrowRight className="w-3.5 h-3.5" />
          </Link>
        </div>

        <div className="grid grid-cols-2 sm:grid-cols-4 gap-4">
          <Link
            to="/vendor/orders"
            className="p-4 rounded-xl border border-amber-200 bg-amber-50/50 hover:bg-amber-50 transition-all group"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-amber-800">Chờ xác nhận</span>
              <AlertCircle className="w-4 h-4 text-amber-500" />
            </div>
            <div className="mt-2 text-2xl font-black text-amber-700 group-hover:scale-105 transition-transform">
              {loading ? '-' : pendingCount}
            </div>
            <p className="text-[11px] text-amber-600 mt-1">Cần xác nhận đơn</p>
          </Link>

          <Link
            to="/vendor/orders"
            className="p-4 rounded-xl border border-blue-200 bg-blue-50/50 hover:bg-blue-50 transition-all group"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-blue-800">Chờ gửi hàng</span>
              <Truck className="w-4 h-4 text-blue-500" />
            </div>
            <div className="mt-2 text-2xl font-black text-blue-700 group-hover:scale-105 transition-transform">
              {loading ? '-' : confirmedCount}
            </div>
            <p className="text-[11px] text-blue-600 mt-1">Giao cho ĐVVC</p>
          </Link>

          <Link
            to="/vendor/orders"
            className="p-4 rounded-xl border border-purple-200 bg-purple-50/50 hover:bg-purple-50 transition-all group"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-purple-800">Đang giao</span>
              <Package className="w-4 h-4 text-purple-500" />
            </div>
            <div className="mt-2 text-2xl font-black text-purple-700 group-hover:scale-105 transition-transform">
              {loading ? '-' : shippedCount}
            </div>
            <p className="text-[11px] text-purple-600 mt-1">Đơn vị đang giao</p>
          </Link>

          <Link
            to="/vendor/orders"
            className="p-4 rounded-xl border border-rose-200 bg-rose-50/50 hover:bg-rose-50 transition-all group"
          >
            <div className="flex items-center justify-between">
              <span className="text-xs font-semibold text-rose-800">Đã hủy</span>
              <XCircle className="w-4 h-4 text-rose-500" />
            </div>
            <div className="mt-2 text-2xl font-black text-rose-700 group-hover:scale-105 transition-transform">
              {loading ? '-' : cancelledCount}
            </div>
            <p className="text-[11px] text-rose-600 mt-1">Đơn đã hủy</p>
          </Link>
        </div>
      </div>

      <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-4 gap-4">
        <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-xs flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-orange-50 text-orange-600 flex items-center justify-center shrink-0">
            <TrendingUp className="w-6 h-6" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Doanh thu hoàn tất</p>
            <p className="text-xl font-bold text-gray-900 mt-0.5 truncate">
              {loading ? '...' : fmtCurrency(stats?.fulfilledOrderValue)}
            </p>
            <p className="text-[11px] text-emerald-600 font-medium mt-0.5">Từ các đơn thành công</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-xs flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-blue-50 text-blue-600 flex items-center justify-center shrink-0">
            <ShoppingBag className="w-6 h-6" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Tổng số đơn</p>
            <p className="text-xl font-bold text-gray-900 mt-0.5">
              {loading ? '...' : (stats?.totalOrders ?? 0).toLocaleString('vi-VN')}
            </p>
            <p className="text-[11px] text-gray-400 mt-0.5">Đơn hàng trong gian hàng</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-xs flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-emerald-50 text-emerald-600 flex items-center justify-center shrink-0">
            <CheckCircle className="w-6 h-6" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Đơn hoàn thành</p>
            <p className="text-xl font-bold text-gray-900 mt-0.5">
              {loading ? '...' : (stats?.deliveredOrderCount ?? 0).toLocaleString('vi-VN')}
            </p>
            <p className="text-[11px] text-emerald-600 mt-0.5">Giao hàng thành công</p>
          </div>
        </div>

        <div className="bg-white rounded-2xl p-5 border border-gray-200 shadow-xs flex items-center space-x-4">
          <div className="w-12 h-12 rounded-xl bg-purple-50 text-purple-600 flex items-center justify-center shrink-0">
            <Package className="w-6 h-6" />
          </div>
          <div className="min-w-0 flex-1">
            <p className="text-xs font-semibold text-gray-500 uppercase tracking-wider">Sản phẩm gian hàng</p>
            <p className="text-xl font-bold text-gray-900 mt-0.5">
              {loading ? '...' : totalProducts.toLocaleString('vi-VN')}
            </p>
            <p className="text-[11px] text-purple-600 mt-0.5">Sản phẩm đang quản lý</p>
          </div>
        </div>
      </div>

      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <div className="lg:col-span-2 bg-white rounded-2xl p-6 border border-gray-200 shadow-xs">
          <div className="flex items-center justify-between mb-4">
            <div>
              <h2 className="text-base font-bold text-gray-900">Đơn Hàng Gần Đây</h2>
              <p className="text-xs text-gray-500 mt-0.5">5 đơn hàng mới nhất nhận được</p>
            </div>
            <Link
              to="/vendor/orders"
              className="text-xs font-semibold text-orange-600 hover:text-orange-700 flex items-center gap-1"
            >
              Xem tất cả <ArrowRight className="w-3.5 h-3.5" />
            </Link>
          </div>

          {loading ? (
            <div className="py-12 text-center text-gray-400">Đang tải đơn hàng...</div>
          ) : recentOrders.length === 0 ? (
            <div className="py-12 text-center text-gray-400 border border-dashed border-gray-200 rounded-xl">
              Chưa có đơn hàng nào
            </div>
          ) : (
            <div className="overflow-x-auto">
              <table className="w-full text-left text-sm">
                <thead>
                  <tr className="border-b border-gray-100 text-xs font-semibold text-gray-400 uppercase">
                    <th className="pb-3">Mã đơn</th>
                    <th className="pb-3">Khách hàng</th>
                    <th className="pb-3">Tổng tiền</th>
                    <th className="pb-3">Trạng thái</th>
                    <th className="pb-3 text-right">Thao tác</th>
                  </tr>
                </thead>
                <tbody className="divide-y divide-gray-100">
                  {recentOrders.map((order) => {
                    const badge = STATUS_BADGE[order.status] || {
                      label: order.status,
                      className: 'bg-gray-100 text-gray-700 border-gray-200',
                    };
                    return (
                      <tr key={order.id} className="hover:bg-gray-50/80 transition-colors">
                        <td className="py-3 font-semibold text-gray-900">
                          #{order.id}
                        </td>
                        <td className="py-3 text-gray-600">
                          <p className="font-medium text-gray-800">{order.customerName || 'Khách hàng'}</p>
                          <p className="text-xs text-gray-400">{order.shippingAddress ? `${order.shippingAddress.slice(0, 25)}...` : new Date(order.createdAt).toLocaleDateString('vi-VN')}</p>
                        </td>
                        <td className="py-3 font-bold text-orange-600">
                          {fmtCurrency(order.totalAmount)}
                        </td>
                        <td className="py-3">
                          <span className={`inline-block text-xs px-2.5 py-1 rounded-full font-semibold border ${badge.className}`}>
                            {badge.label}
                          </span>
                        </td>
                        <td className="py-3 text-right">
                          <Link
                            to="/vendor/orders"
                            className="text-xs font-semibold text-orange-600 hover:text-orange-800 hover:underline"
                          >
                            Xử lý
                          </Link>
                        </td>
                      </tr>
                    );
                  })}
                </tbody>
              </table>
            </div>
          )}
        </div>

        <div className="bg-white rounded-2xl p-6 border border-gray-200 shadow-xs flex flex-col justify-between space-y-4">
          <div>
            <h2 className="text-base font-bold text-gray-900 mb-1">Lối Tắt Nhanh</h2>
            <p className="text-xs text-gray-500 mb-4">Các tính năng người bán thường dùng</p>

            <div className="space-y-2.5">
              <Link
                to="/vendor/products/new"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-gray-50/60 hover:bg-orange-50 hover:border-orange-200 transition-colors group cursor-pointer"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-orange-100 text-orange-600 flex items-center justify-center">
                    <Plus className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-800 group-hover:text-orange-600">Đăng sản phẩm mới</p>
                    <p className="text-[11px] text-gray-400">Mở rộng danh mục bán</p>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-orange-600" />
              </Link>

              <Link
                to="/vendor/vouchers"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-gray-50/60 hover:bg-orange-50 hover:border-orange-200 transition-colors group cursor-pointer"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-indigo-100 text-indigo-600 flex items-center justify-center">
                    <Tag className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-800 group-hover:text-orange-600">Tạo mã khuyến mãi</p>
                    <p className="text-[11px] text-gray-400">Tăng tỷ lệ chốt đơn</p>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-orange-600" />
              </Link>

              <Link
                to="/vendor/analytics"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-gray-50/60 hover:bg-orange-50 hover:border-orange-200 transition-colors group cursor-pointer"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-emerald-100 text-emerald-600 flex items-center justify-center">
                    <TrendingUp className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-800 group-hover:text-orange-600">Báo cáo doanh số</p>
                    <p className="text-[11px] text-gray-400">Biểu đồ & phân tích ngày</p>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-orange-600" />
              </Link>

              <Link
                to="/vendor/payouts"
                className="flex items-center justify-between p-3 rounded-xl border border-gray-100 bg-gray-50/60 hover:bg-orange-50 hover:border-orange-200 transition-colors group cursor-pointer"
              >
                <div className="flex items-center space-x-3">
                  <div className="w-8 h-8 rounded-lg bg-orange-100 text-orange-600 flex items-center justify-center">
                    <Wallet className="w-4 h-4" />
                  </div>
                  <div>
                    <p className="text-sm font-semibold text-gray-800 group-hover:text-orange-600">Ví & Rút tiền</p>
                    <p className="text-[11px] text-gray-400">Xem số dư & rút về ngân hàng</p>
                  </div>
                </div>
                <ArrowRight className="w-4 h-4 text-gray-400 group-hover:text-orange-600" />
              </Link>
            </div>
          </div>

          <div className="p-4 rounded-xl bg-orange-50 border border-orange-200">
            <p className="text-xs font-bold text-orange-800">💡 Mẹo bán hàng</p>
            <p className="text-xs text-orange-700 mt-1">
              Xác nhận đơn hàng trong vòng 24h giúp tăng độ tin cậy và sự hài lòng của khách hàng.
            </p>
          </div>
        </div>
      </div>
    </div>
  );
};
