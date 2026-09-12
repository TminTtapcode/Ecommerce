import { getApiError } from '../../api/apiError';
import { ERROR_CODES } from '../../api/types/errorCodes';
import React, { useEffect, useState } from 'react';
import { adminApi } from '../../api/adminApi';
import type { ShopResponse } from '../../api/types/shop.types';

const mutationError = (error: unknown) => {
  const details = getApiError(error);
  return details.errorCode === ERROR_CODES.SHOP_PRIOR_STATUS_MISSING
    ? 'Cần đối soát trạng thái trước khi khóa của gian hàng này trước khi mở lại.'
    : details.message;
};

export const AdminShopListPage: React.FC = () => {
  const [busy, setBusy] = useState<number | null>(null);
  const [shops, setShops] = useState<ShopResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const fetchShops = async () => {
    try {
      setLoading(true);
      const res = await adminApi.getShops(statusFilter || undefined, page, 10);
      if (!res.data.data) throw new Error('Thiếu dữ liệu phản hồi');
      setShops(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Lỗi tải danh sách cửa hàng');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchShops();
  }, [page, statusFilter]);

  const handleApprove = async (shopId: number) => {
    try {
      setBusy(shopId);
      await adminApi.approveShop(shopId);
      fetchShops();
    } catch (err: any) {
      alert(mutationError(err));
    } finally {
      setBusy(null);
    }
  };

  const handleBan = async (shopId: number) => {
    const reason = prompt('Nhập lý do khóa gian hàng (không bắt buộc):');
    if (reason === null) return;
    try {
      setBusy(shopId);
      await adminApi.banShop(shopId, reason || undefined);
      fetchShops();
    } catch (err: any) {
      alert(mutationError(err));
    } finally {
      setBusy(null);
    }
  };

  const handleUnban = async (shopId: number) => {
    if (!confirm('Bạn có chắc chắn muốn mở khóa cho gian hàng này không?')) return;
    try {
      setBusy(shopId);
      await adminApi.unbanShop(shopId);
      fetchShops();
    } catch (err: any) {
      alert(mutationError(err));
    } finally {
      setBusy(null);
    }
  };

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 text-gray-900">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h2 className="text-2xl font-bold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent flex items-center gap-2">
            🏪 Quản Lý Gian Hàng
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Duyệt đăng ký mới, kiểm soát trạng thái hoạt động của các đối tác bán hàng
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
            <option value="PENDING">Chờ duyệt</option>
            <option value="ACTIVE">Hoạt động</option>
            <option value="BANNED">Đã khóa</option>
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
          <div>Đang tải danh sách gian hàng...</div>
        </div>
      ) : shops.length === 0 ? (
        <div className="text-center py-12 text-gray-400 bg-gray-50 rounded-xl border border-gray-200">
          <div className="text-4xl mb-2">🏪</div>
          <p className="text-base font-medium text-gray-700">Không tìm thấy gian hàng nào</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="min-w-full divide-y divide-gray-200 text-left">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">ID</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Tên Gian Hàng</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Chủ Sở Hữu</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mô Tả</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Trạng Thái</th>
                <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {shops.map((shop) => (
                <tr key={shop.id} className="hover:bg-gray-50/80 transition-colors">
                  <td className="px-5 py-4 whitespace-nowrap text-sm font-bold text-orange-600">#{shop.id}</td>
                  <td className="px-5 py-4 whitespace-nowrap text-sm font-semibold text-gray-900">{shop.name}</td>
                  <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-500">User ID: #{shop.userId}</td>
                  <td className="px-5 py-4 text-xs text-gray-600 max-w-xs truncate">{shop.description || 'Chưa cập nhật'}</td>
                  <td className="px-5 py-4 whitespace-nowrap">
                    <span className={`px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full border
                      ${shop.status === 'ACTIVE' ? 'bg-emerald-50 text-emerald-700 border-emerald-200' :
                        shop.status === 'PENDING' ? 'bg-amber-50 text-amber-700 border-amber-200' :
                        'bg-rose-50 text-rose-700 border-rose-200'}`}>
                      {shop.status === 'ACTIVE' ? 'Hoạt động' : shop.status === 'PENDING' ? 'Chờ duyệt' : 'Đã khóa'}
                      {shop.status === 'BANNED' && shop.priorStatus && (
                        <span className="ml-1 opacity-80 font-normal">({shop.priorStatus})</span>
                      )}
                    </span>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end space-x-2">
                      {shop.status === 'PENDING' && (
                        <button
                          disabled={busy !== null}
                          onClick={() => handleApprove(shop.id)}
                          className="px-3 py-1.5 rounded-lg bg-blue-50 text-blue-600 hover:bg-blue-100 border border-blue-200 text-xs font-semibold transition-colors disabled:opacity-50 cursor-pointer"
                        >
                          Phê duyệt
                        </button>
                      )}
                      {(shop.status === 'ACTIVE' || shop.status === 'PENDING') && (
                        <button
                          disabled={busy !== null}
                          onClick={() => handleBan(shop.id)}
                          className="px-3 py-1.5 rounded-lg bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 text-xs font-semibold transition-colors disabled:opacity-50 cursor-pointer"
                        >
                          Khóa Shop
                        </button>
                      )}
                      {shop.status === 'BANNED' && (
                        <button
                          disabled={busy !== null || !shop.priorStatus}
                          title={shop.priorStatus ? `Khôi phục ${shop.priorStatus}` : 'Cần đối soát thủ công'}
                          onClick={() => handleUnban(shop.id)}
                          className="px-3 py-1.5 rounded-lg bg-emerald-50 text-emerald-600 hover:bg-emerald-100 border border-emerald-200 text-xs font-semibold transition-colors disabled:opacity-50 cursor-pointer"
                        >
                          Mở Khóa
                        </button>
                      )}
                    </div>
                  </td>
                </tr>
              ))}
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
