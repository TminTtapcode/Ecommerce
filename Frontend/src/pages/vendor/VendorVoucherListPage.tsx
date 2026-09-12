import React, { useEffect, useState } from 'react';
import { voucherApi } from '../../api/voucherApi';
import type { VoucherResponse, VoucherCreateRequest, VoucherUpdateRequest, VoucherType } from '../../api/types/voucher.types';
import { VendorNav } from '../../components/vendor/VendorNav';
import { Pagination } from '../../components/common/Pagination';

export const VendorVoucherListPage: React.FC = () => {
  const [vouchers, setVouchers] = useState<VoucherResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [showModal, setShowModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [createError, setCreateError] = useState('');

  const [formData, setFormData] = useState<Partial<VoucherCreateRequest>>({
    code: '',
    scope: 'SHOP',
    type: 'PERCENTAGE',
    discountValue: 0,
    startDate: '',
    endDate: ''
  });

  const [editingVoucher, setEditingVoucher] = useState<VoucherResponse | null>(null);
  const [editFormData, setEditFormData] = useState<Partial<VoucherUpdateRequest>>({});
  const [editError, setEditError] = useState('');
  const [isEditSubmitting, setIsEditSubmitting] = useState(false);

  const [deletingVoucher, setDeletingVoucher] = useState<VoucherResponse | null>(null);
  const [isDeleting, setIsDeleting] = useState(false);

  const fetchVouchers = async () => {
    try {
      setLoading(true);
      const res = await voucherApi.getMyShopVouchers(page, 10);
      if (!res.data.data) throw new Error('Thiếu dữ liệu phản hồi');
      setVouchers(res.data.data.content);
      setTotalPages(res.data.data.totalPages);
      setError('');
    } catch (err: any) {
      setError(err.response?.data?.message || 'Lỗi tải danh sách voucher');
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchVouchers();
  }, [page]);

  const toInputDateTime = (dateStr?: string) => {
    if (!dateStr) return '';
    const d = new Date(dateStr);
    if (isNaN(d.getTime())) return '';
    const pad = (n: number) => n.toString().padStart(2, '0');
    return `${d.getFullYear()}-${pad(d.getMonth() + 1)}-${pad(d.getDate())}T${pad(d.getHours())}:${pad(d.getMinutes())}`;
  };

  const handleCreate = async (e: React.FormEvent) => {
    e.preventDefault();
    setCreateError('');
    setIsSubmitting(true);

    try {
      const data: VoucherCreateRequest = {
        code: (formData.code || '').trim().toUpperCase(),
        scope: 'SHOP',
        type: formData.type || 'PERCENTAGE',
        discountValue: Number(formData.discountValue),
        maxDiscount: formData.maxDiscount ? Number(formData.maxDiscount) : undefined,
        minOrderValue: formData.minOrderValue ? Number(formData.minOrderValue) : undefined,
        startDate: formData.startDate + ':00',
        endDate: formData.endDate + ':00',
        usageLimit: formData.usageLimit ? Number(formData.usageLimit) : undefined
      };

      await voucherApi.createVoucher(data);
      setShowModal(false);
      setSuccessMsg(`Tạo mã voucher "${data.code}" thành công!`);
      setTimeout(() => setSuccessMsg(''), 4000);
      fetchVouchers();

      setFormData({
        code: '',
        scope: 'SHOP',
        type: 'PERCENTAGE',
        discountValue: 0,
        startDate: '',
        endDate: ''
      });
    } catch (err: any) {
      setCreateError(err.response?.data?.message || 'Lỗi tạo voucher');
    } finally {
      setIsSubmitting(false);
    }
  };

  const openEditModal = (voucher: VoucherResponse) => {
    setEditingVoucher(voucher);
    setEditError('');
    setEditFormData({
      type: voucher.type,
      discountValue: voucher.discountValue,
      maxDiscount: voucher.maxDiscount,
      minOrderValue: voucher.minOrderValue,
      startDate: toInputDateTime(voucher.startDate),
      endDate: toInputDateTime(voucher.endDate),
      usageLimit: voucher.usageLimit
    });
  };

  const handleUpdate = async (e: React.FormEvent) => {
    e.preventDefault();
    if (!editingVoucher) return;
    setEditError('');
    setIsEditSubmitting(true);

    try {
      const data: VoucherUpdateRequest = {
        type: (editFormData.type as VoucherType) || 'PERCENTAGE',
        discountValue: Number(editFormData.discountValue),
        maxDiscount: editFormData.maxDiscount ? Number(editFormData.maxDiscount) : undefined,
        minOrderValue: editFormData.minOrderValue ? Number(editFormData.minOrderValue) : undefined,
        startDate: editFormData.startDate?.length === 16 ? editFormData.startDate + ':00' : (editFormData.startDate || ''),
        endDate: editFormData.endDate?.length === 16 ? editFormData.endDate + ':00' : (editFormData.endDate || ''),
        usageLimit: editFormData.usageLimit ? Number(editFormData.usageLimit) : undefined
      };

      await voucherApi.updateShopVoucher(editingVoucher.id, data);
      setEditingVoucher(null);
      setSuccessMsg(`Cập nhật voucher "${editingVoucher.code}" thành công!`);
      setTimeout(() => setSuccessMsg(''), 4000);
      fetchVouchers();
    } catch (err: any) {
      setEditError(err.response?.data?.message || 'Lỗi cập nhật voucher');
    } finally {
      setIsEditSubmitting(false);
    }
  };

  const handleDelete = async () => {
    if (!deletingVoucher) return;
    setIsDeleting(true);
    try {
      await voucherApi.deleteShopVoucher(deletingVoucher.id);
      setSuccessMsg(`Đã xóa voucher "${deletingVoucher.code}" thành công!`);
      setTimeout(() => setSuccessMsg(''), 4000);
      setDeletingVoucher(null);
      fetchVouchers();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Lỗi xóa voucher');
    } finally {
      setIsDeleting(false);
    }
  };

  const formatCurrency = (amount: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(amount);
  };

  return (
    <div className="container mx-auto px-4 py-6 max-w-7xl">
      <VendorNav />
      <div className="bg-white rounded-xl shadow-sm border border-gray-200 overflow-hidden mt-6">
        <div className="p-6 border-b border-gray-200 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-gray-900 flex items-center gap-2">
              🏷️ Voucher Của Shop
            </h2>
            <p className="text-sm text-gray-500 mt-1">
              Quản lý các mã khuyến mãi áp dụng riêng cho các sản phẩm của gian hàng
            </p>
          </div>
          <button
            onClick={() => setShowModal(true)}
            className="px-4 py-2.5 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 shadow-sm flex items-center gap-2 transition-colors cursor-pointer self-start md:self-auto"
          >
            <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
            </svg>
            Tạo Voucher Mới
          </button>
        </div>

        <div className="p-6">
          {successMsg && (
            <div className="bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-3 rounded-lg mb-4 text-sm flex items-center gap-2">
              <span>✅</span>
              <span>{successMsg}</span>
            </div>
          )}

          {error && (
            <div className="bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-lg mb-4 text-sm flex items-center gap-2">
              <span>⚠️</span>
              <span>{error}</span>
            </div>
          )}

          {loading ? (
            <div className="text-center py-12 text-gray-500">
              <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-indigo-600 mb-2"></div>
              <div>Đang tải voucher của shop...</div>
            </div>
          ) : vouchers.length === 0 ? (
            <div className="text-center py-12 text-gray-500 bg-gray-50 rounded-lg border border-dashed border-gray-300">
              <div className="text-4xl mb-2">🏷️</div>
              <p className="text-base font-medium text-gray-700">Shop chưa có voucher nào</p>
              <p className="text-sm text-gray-500 mt-1">Tạo mã giảm giá để thu hút khách hàng mua sắm nhiều hơn</p>
            </div>
          ) : (
            <div className="overflow-x-auto rounded-lg border border-gray-200">
              <table className="min-w-full divide-y divide-gray-200 text-left">
                <thead className="bg-gray-50">
                  <tr>
                    <th className="px-6 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã / Loại</th>
                    <th className="px-6 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Giảm Giá</th>
                    <th className="px-6 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Tối đa / Tối thiểu</th>
                    <th className="px-6 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Hạn sử dụng</th>
                    <th className="px-6 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Lượt dùng</th>
                    <th className="px-6 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thao Tác</th>
                  </tr>
                </thead>
                <tbody className="bg-white divide-y divide-gray-200">
                  {vouchers.map((v) => (
                    <tr key={v.id} className="hover:bg-gray-50/80 transition-colors">
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="font-mono font-bold text-indigo-700 bg-indigo-50 border border-indigo-200 px-2.5 py-1 rounded-md text-sm inline-block">
                          {v.code}
                        </span>
                        <div className="text-xs text-gray-500 mt-1">
                          {v.type === 'PERCENTAGE' ? 'Phần trăm (%)' : 'Số tiền cố định (VNĐ)'}
                        </div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap">
                        <span className="text-base font-bold text-emerald-600">
                          {v.type === 'PERCENTAGE' ? `${v.discountValue}%` : formatCurrency(v.discountValue)}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        <div><span className="text-gray-400">Tối đa:</span> {v.maxDiscount ? formatCurrency(v.maxDiscount) : 'Không'}</div>
                        <div className="mt-0.5"><span className="text-gray-400">Tối thiểu:</span> {v.minOrderValue ? formatCurrency(v.minOrderValue) : '0 ₫'}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        <div><span className="text-gray-400">Từ:</span> {new Date(v.startDate).toLocaleString('vi-VN')}</div>
                        <div className="mt-0.5"><span className="text-gray-400">Đến:</span> {new Date(v.endDate).toLocaleString('vi-VN')}</div>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-600">
                        <span className="inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium bg-gray-100 text-gray-800">
                          {v.usedCount} / {v.usageLimit ? v.usageLimit : '∞'}
                        </span>
                      </td>
                      <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                        <div className="flex items-center justify-end space-x-2">
                          <button
                            onClick={() => openEditModal(v)}
                            className="px-3 py-1.5 rounded-md bg-indigo-50 text-indigo-600 hover:bg-indigo-100 border border-indigo-200 text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
                            title="Chỉnh sửa voucher"
                          >
                            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                            </svg>
                            Sửa
                          </button>
                          <button
                            onClick={() => setDeletingVoucher(v)}
                            className="px-3 py-1.5 rounded-md bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
                            title="Xóa voucher"
                          >
                            <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16" />
                            </svg>
                            Xóa
                          </button>
                        </div>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>

              {totalPages > 1 && (
                <div className="flex justify-center p-4 border-t border-gray-200">
                  <Pagination
                    currentPage={page}
                    totalPages={totalPages}
                    onPageChange={(p) => setPage(p)}
                  />
                </div>
              )}
            </div>
          )}
        </div>
      </div>

      {showModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div
            className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
            onClick={() => setShowModal(false)}
          />
          <div className="relative z-10 bg-white rounded-xl text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:max-w-lg sm:w-full">
            <form onSubmit={handleCreate}>
              <div className="bg-white px-6 pt-6 pb-4">
                <div className="flex justify-between items-center mb-4 pb-3 border-b border-gray-100">
                  <h3 className="text-lg font-bold text-gray-900">Tạo Voucher Shop</h3>
                  <button
                    type="button"
                    onClick={() => setShowModal(false)}
                    className="text-gray-400 hover:text-gray-600 text-xl"
                  >
                    ×
                  </button>
                </div>

                {createError && (
                  <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg mb-4 text-sm">{createError}</div>
                )}

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Mã Voucher</label>
                    <input
                      required
                      type="text"
                      placeholder="VD: SHOP20, SALE50K..."
                      value={formData.code}
                      onChange={e => setFormData({...formData, code: e.target.value.toUpperCase()})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm font-mono uppercase focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    />
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Loại Giảm Giá</label>
                    <select
                      required
                      value={formData.type}
                      onChange={e => setFormData({...formData, type: e.target.value as any})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    >
                      <option value="PERCENTAGE">Phần trăm (%)</option>
                      <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Mức giảm {formData.type === 'PERCENTAGE' ? '(%)' : '(VNĐ)'}
                    </label>
                    <input
                      required
                      type="number"
                      min="1"
                      max={formData.type === 'PERCENTAGE' ? 100 : undefined}
                      value={formData.discountValue || ''}
                      onChange={e => setFormData({...formData, discountValue: e.target.value as any})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      placeholder={formData.type === 'PERCENTAGE' ? 'VD: 10 (nghĩa là giảm 10%)' : 'VD: 30000'}
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tối đa giảm (VNĐ) {formData.type === 'PERCENTAGE' && <span className="text-red-500">*</span>}
                      </label>
                      <input
                        type="number"
                        min="0"
                        required={formData.type === 'PERCENTAGE'}
                        value={formData.maxDiscount || ''}
                        onChange={e => setFormData({...formData, maxDiscount: e.target.value as any})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="VD: 50000"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Đơn tối thiểu (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={formData.minOrderValue || ''}
                        onChange={e => setFormData({...formData, minOrderValue: e.target.value as any})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="VD: 100000"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Bắt đầu</label>
                      <input
                        required
                        type="datetime-local"
                        value={formData.startDate}
                        onChange={e => setFormData({...formData, startDate: e.target.value})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Kết thúc</label>
                      <input
                        required
                        type="datetime-local"
                        value={formData.endDate}
                        onChange={e => setFormData({...formData, endDate: e.target.value})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Giới hạn số lần dùng</label>
                    <input
                      type="number"
                      min="1"
                      value={formData.usageLimit || ''}
                      onChange={e => setFormData({...formData, usageLimit: e.target.value as any})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      placeholder="Để trống nếu không giới hạn"
                    />
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 px-6 py-4 flex flex-row-reverse gap-3 border-t border-gray-100">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-4 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 shadow-sm disabled:opacity-50 text-sm transition-colors cursor-pointer"
                >
                  {isSubmitting ? 'Đang tạo...' : 'Tạo Voucher'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowModal(false)}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 text-sm transition-colors cursor-pointer"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {editingVoucher && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div
            className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
            onClick={() => setEditingVoucher(null)}
          />
          <div className="relative z-10 bg-white rounded-xl text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:max-w-lg sm:w-full">
            <form onSubmit={handleUpdate}>
              <div className="bg-white px-6 pt-6 pb-4">
                <div className="flex justify-between items-center mb-4 pb-3 border-b border-gray-100">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900">Chỉnh Sửa Voucher Shop</h3>
                    <p className="text-xs text-gray-500 mt-0.5">
                      Mã voucher: <span className="font-mono font-bold text-indigo-600">{editingVoucher.code}</span>
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setEditingVoucher(null)}
                    className="text-gray-400 hover:text-gray-600 text-xl"
                  >
                    ×
                  </button>
                </div>

                {editError && (
                  <div className="bg-red-50 border border-red-200 text-red-700 p-3 rounded-lg mb-4 text-sm">{editError}</div>
                )}

                <div className="space-y-4">
                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Loại Giảm Giá</label>
                    <select
                      required
                      value={editFormData.type}
                      onChange={e => setEditFormData({...editFormData, type: e.target.value as any})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    >
                      <option value="PERCENTAGE">Phần trăm (%)</option>
                      <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">
                      Mức giảm {editFormData.type === 'PERCENTAGE' ? '(%)' : '(VNĐ)'}
                    </label>
                    <input
                      required
                      type="number"
                      min="1"
                      max={editFormData.type === 'PERCENTAGE' ? 100 : undefined}
                      value={editFormData.discountValue ?? ''}
                      onChange={e => setEditFormData({...editFormData, discountValue: Number(e.target.value)})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">
                        Tối đa giảm (VNĐ) {editFormData.type === 'PERCENTAGE' && <span className="text-red-500">*</span>}
                      </label>
                      <input
                        type="number"
                        min="0"
                        required={editFormData.type === 'PERCENTAGE'}
                        value={editFormData.maxDiscount ?? ''}
                        onChange={e => setEditFormData({...editFormData, maxDiscount: e.target.value ? Number(e.target.value) : undefined})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="Không giới hạn"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Đơn tối thiểu (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={editFormData.minOrderValue ?? ''}
                        onChange={e => setEditFormData({...editFormData, minOrderValue: e.target.value ? Number(e.target.value) : undefined})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                        placeholder="0"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-4">
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Bắt đầu</label>
                      <input
                        required
                        type="datetime-local"
                        value={editFormData.startDate || ''}
                        onChange={e => setEditFormData({...editFormData, startDate: e.target.value})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                    <div>
                      <label className="block text-sm font-medium text-gray-700 mb-1">Kết thúc</label>
                      <input
                        required
                        type="datetime-local"
                        value={editFormData.endDate || ''}
                        onChange={e => setEditFormData({...editFormData, endDate: e.target.value})}
                        className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-sm font-medium text-gray-700 mb-1">Giới hạn số lần dùng</label>
                    <input
                      type="number"
                      min="1"
                      value={editFormData.usageLimit ?? ''}
                      onChange={e => setEditFormData({...editFormData, usageLimit: e.target.value ? Number(e.target.value) : undefined})}
                      className="block w-full border border-gray-300 rounded-lg p-2.5 text-sm focus:ring-2 focus:ring-indigo-500 focus:border-indigo-500"
                      placeholder="Không giới hạn"
                    />
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 px-6 py-4 flex flex-row-reverse gap-3 border-t border-gray-100">
                <button
                  type="submit"
                  disabled={isEditSubmitting}
                  className="px-4 py-2 bg-indigo-600 text-white font-medium rounded-lg hover:bg-indigo-700 shadow-sm disabled:opacity-50 text-sm transition-colors cursor-pointer"
                >
                  {isEditSubmitting ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                </button>
                <button
                  type="button"
                  onClick={() => setEditingVoucher(null)}
                  className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 text-sm transition-colors cursor-pointer"
                >
                  Hủy
                </button>
              </div>
            </form>
          </div>
        </div>
      )}

      {deletingVoucher && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div
            className="fixed inset-0 bg-black bg-opacity-50 transition-opacity"
            onClick={() => setDeletingVoucher(null)}
          />
          <div className="relative z-10 bg-white rounded-xl text-left overflow-hidden shadow-xl transform transition-all sm:my-8 sm:max-w-md sm:w-full p-6">
            <div className="flex items-center gap-3 text-red-600 mb-4">
              <div className="w-10 h-10 rounded-full bg-red-100 flex items-center justify-center text-xl">
                ⚠️
              </div>
              <h3 className="text-lg font-bold text-gray-900">Xác Nhận Xóa Voucher</h3>
            </div>

            <p className="text-sm text-gray-600 mb-6">
              Bạn có chắc chắn muốn xóa voucher{' '}
              <span className="font-mono font-bold text-indigo-600 bg-indigo-50 px-2 py-0.5 rounded">
                {deletingVoucher.code}
              </span>{' '}
              của shop? Hành động này không thể hoàn tác.
            </p>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                disabled={isDeleting}
                onClick={() => setDeletingVoucher(null)}
                className="px-4 py-2 bg-white border border-gray-300 rounded-lg text-gray-700 font-medium hover:bg-gray-50 text-sm transition-colors cursor-pointer"
              >
                Hủy bỏ
              </button>
              <button
                type="button"
                disabled={isDeleting}
                onClick={handleDelete}
                className="px-4 py-2 bg-red-600 hover:bg-red-700 text-white font-medium rounded-lg shadow-sm disabled:opacity-50 text-sm transition-colors cursor-pointer"
              >
                {isDeleting ? 'Đang xóa...' : 'Đồng ý Xóa'}
              </button>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
