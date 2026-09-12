import React, { useEffect, useState } from 'react';
import { adminApi } from '../../api/adminApi';
import { voucherApi } from '../../api/voucherApi';
import type { VoucherResponse, VoucherCreateRequest, VoucherUpdateRequest, VoucherType } from '../../api/types/voucher.types';

export const AdminVoucherListPage: React.FC = () => {
  const [vouchers, setVouchers] = useState<VoucherResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [successMsg, setSuccessMsg] = useState('');
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);

  const [showCreateModal, setShowCreateModal] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [createError, setCreateError] = useState('');

  const [formData, setFormData] = useState<Partial<VoucherCreateRequest>>({
    code: '',
    scope: 'SYSTEM',
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
      const res = await adminApi.getSystemVouchers(page, 10);
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
        scope: 'SYSTEM',
        type: formData.type || 'PERCENTAGE',
        discountValue: Number(formData.discountValue),
        maxDiscount: formData.maxDiscount ? Number(formData.maxDiscount) : undefined,
        minOrderValue: formData.minOrderValue ? Number(formData.minOrderValue) : undefined,
        startDate: formData.startDate + ':00',
        endDate: formData.endDate + ':00',
        usageLimit: formData.usageLimit ? Number(formData.usageLimit) : undefined
      };

      await voucherApi.createVoucher(data);
      setShowCreateModal(false);
      setSuccessMsg(`Tạo mã voucher "${data.code}" thành công!`);
      setTimeout(() => setSuccessMsg(''), 4000);
      fetchVouchers();

      setFormData({
        code: '',
        scope: 'SYSTEM',
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

      await adminApi.updateSystemVoucher(editingVoucher.id, data);
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
      await adminApi.deleteSystemVoucher(deletingVoucher.id);
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
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 text-gray-900">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center gap-4 mb-6">
        <div>
          <h2 className="text-2xl font-bold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent flex items-center gap-2">
            🎟️ Quản Lý Voucher Hệ Thống
          </h2>
          <p className="text-sm text-gray-500 mt-1">
            Thiết lập và quản lý các mã giảm giá toàn sàn dành cho khách hàng
          </p>
        </div>
        <button
          onClick={() => setShowCreateModal(true)}
          className="px-4 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white font-medium rounded-xl shadow-sm flex items-center gap-2 transition-all cursor-pointer"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Tạo Voucher
        </button>
      </div>

      {successMsg && (
        <div className="bg-emerald-50 border border-emerald-200 text-emerald-700 px-4 py-3 rounded-xl mb-4 text-sm flex items-center gap-2">
          <span>✅</span>
          <span>{successMsg}</span>
        </div>
      )}

      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 px-4 py-3 rounded-xl mb-4 text-sm flex items-center gap-2">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      {loading ? (
        <div className="text-center py-12 text-gray-400">
          <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mb-2"></div>
          <div>Đang tải dữ liệu voucher...</div>
        </div>
      ) : vouchers.length === 0 ? (
        <div className="text-center py-12 text-gray-400 bg-gray-50 rounded-xl border border-gray-200">
          <div className="text-4xl mb-2">🎫</div>
          <p className="text-base font-medium text-gray-700">Chưa có voucher nào trên hệ thống</p>
          <p className="text-xs text-gray-500 mt-1">Nhấn "+ Tạo Voucher" để tạo mã giảm giá mới</p>
        </div>
      ) : (
        <div className="overflow-x-auto rounded-xl border border-gray-200">
          <table className="min-w-full divide-y divide-gray-200 text-left">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Mã / Loại</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Giảm Giá</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Tối đa / Tối thiểu</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Hạn sử dụng</th>
                <th className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">Lượt dùng</th>
                <th className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">Thao Tác</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-gray-100 bg-white">
              {vouchers.map((v) => (
                <tr key={v.id} className="hover:bg-gray-50/80 transition-colors">
                  <td className="px-5 py-4 whitespace-nowrap">
                    <span className="font-mono font-bold text-orange-600 bg-orange-50 border border-orange-200 px-2.5 py-1 rounded-lg text-sm inline-block tracking-wide shadow-xs">
                      {v.code}
                    </span>
                    <div className="text-xs text-gray-500 mt-1.5 flex items-center gap-1.5">
                      <span className="w-1.5 h-1.5 rounded-full bg-orange-400"></span>
                      {v.type === 'PERCENTAGE' ? 'Phần trăm (%)' : 'Số tiền cố định (VNĐ)'}
                    </div>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap">
                    <div className="text-base font-bold text-emerald-600">
                      {v.type === 'PERCENTAGE' ? `${v.discountValue}%` : formatCurrency(v.discountValue)}
                    </div>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-600">
                    <div className="flex items-center gap-1">
                      <span className="text-gray-400">Tối đa:</span>
                      <span className="font-medium text-gray-800">{v.maxDiscount ? formatCurrency(v.maxDiscount) : 'Không giới hạn'}</span>
                    </div>
                    <div className="flex items-center gap-1 mt-1">
                      <span className="text-gray-400">Đơn từ:</span>
                      <span className="font-medium text-gray-800">{v.minOrderValue ? formatCurrency(v.minOrderValue) : '0 ₫'}</span>
                    </div>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap text-xs text-gray-600">
                    <div>
                      <span className="text-gray-400">Từ:</span> {new Date(v.startDate).toLocaleString('vi-VN')}
                    </div>
                    <div className="mt-1">
                      <span className="text-gray-400">Đến:</span> {new Date(v.endDate).toLocaleString('vi-VN')}
                    </div>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap">
                    <span className="inline-flex items-center px-2.5 py-1 rounded-full text-xs font-medium bg-gray-100 text-gray-700 border border-gray-200">
                      {v.usedCount} / {v.usageLimit ? v.usageLimit : '∞'}
                    </span>
                  </td>
                  <td className="px-5 py-4 whitespace-nowrap text-right text-sm font-medium">
                    <div className="flex items-center justify-end space-x-2">
                      <button
                        onClick={() => openEditModal(v)}
                        className="px-3 py-1.5 rounded-lg bg-orange-50 text-orange-600 hover:bg-orange-100 border border-orange-200 text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
                        title="Chỉnh sửa voucher"
                      >
                        <svg className="w-3.5 h-3.5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M11 5H6a2 2 0 00-2 2v11a2 2 0 002 2h11a2 2 0 002-2v-5m-1.414-9.414a2 2 0 112.828 2.828L11.828 15H9v-2.828l8.586-8.586z" />
                        </svg>
                        Sửa
                      </button>
                      <button
                        onClick={() => setDeletingVoucher(v)}
                        className="px-3 py-1.5 rounded-lg bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 text-xs font-semibold flex items-center gap-1 transition-colors cursor-pointer"
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

      {showCreateModal && (
        <div className="fixed inset-0 z-50 flex items-center justify-center p-4 overflow-y-auto">
          <div
            className="fixed inset-0 bg-black/40 backdrop-blur-xs transition-opacity"
            onClick={() => setShowCreateModal(false)}
          />
          <div className="relative z-10 bg-white border border-gray-200 rounded-2xl text-left overflow-hidden shadow-2xl transform transition-all sm:my-8 sm:max-w-lg sm:w-full">
            <form onSubmit={handleCreate}>
              <div className="p-6">
                <div className="flex justify-between items-center mb-5 pb-3 border-b border-gray-100">
                  <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                    ✨ Tạo Voucher Hệ Thống
                  </h3>
                  <button
                    type="button"
                    onClick={() => setShowCreateModal(false)}
                    className="text-gray-400 hover:text-gray-600 text-xl font-bold cursor-pointer"
                  >
                    ×
                  </button>
                </div>

                {createError && (
                  <div className="bg-rose-50 border border-rose-200 text-rose-700 p-3 rounded-xl mb-4 text-xs">
                    {createError}
                  </div>
                )}

                <div className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Mã Voucher</label>
                    <input
                      required
                      type="text"
                      placeholder="VD: SYSTEM50, FREESHIP..."
                      value={formData.code}
                      onChange={e => setFormData({...formData, code: e.target.value.toUpperCase()})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 font-mono uppercase text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                    />
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Loại Giảm Giá</label>
                    <select
                      required
                      value={formData.type}
                      onChange={e => setFormData({...formData, type: e.target.value as any})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                    >
                      <option value="PERCENTAGE">Phần trăm (%)</option>
                      <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                      Mức Giảm {formData.type === 'PERCENTAGE' ? '(%)' : '(VNĐ)'}
                    </label>
                    <input
                      required
                      type="number"
                      min="1"
                      max={formData.type === 'PERCENTAGE' ? 100 : undefined}
                      value={formData.discountValue || ''}
                      onChange={e => setFormData({...formData, discountValue: e.target.value as any})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      placeholder={formData.type === 'PERCENTAGE' ? 'VD: 15 (nghĩa là giảm 15%)' : 'VD: 50000'}
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                        Tối Đa Giảm (VNĐ)
                        {formData.type === 'PERCENTAGE' && <span className="text-rose-500">*</span>}
                      </label>
                      <input
                        type="number"
                        min="0"
                        required={formData.type === 'PERCENTAGE'}
                        value={formData.maxDiscount || ''}
                        onChange={e => setFormData({...formData, maxDiscount: e.target.value as any})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                        placeholder="VD: 50000"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Đơn Tối Thiểu (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={formData.minOrderValue || ''}
                        onChange={e => setFormData({...formData, minOrderValue: e.target.value as any})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                        placeholder="VD: 100000"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Bắt Đầu</label>
                      <input
                        required
                        type="datetime-local"
                        value={formData.startDate}
                        onChange={e => setFormData({...formData, startDate: e.target.value})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Kết Thúc</label>
                      <input
                        required
                        type="datetime-local"
                        value={formData.endDate}
                        onChange={e => setFormData({...formData, endDate: e.target.value})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Giới Hạn Số Lần Dùng</label>
                    <input
                      type="number"
                      min="1"
                      value={formData.usageLimit || ''}
                      onChange={e => setFormData({...formData, usageLimit: e.target.value as any})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      placeholder="Để trống nếu không giới hạn"
                    />
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 px-6 py-4 flex flex-row-reverse gap-3 border-t border-gray-100">
                <button
                  type="submit"
                  disabled={isSubmitting}
                  className="px-5 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white text-sm font-semibold rounded-xl shadow-sm disabled:opacity-50 transition-all cursor-pointer"
                >
                  {isSubmitting ? 'Đang tạo...' : 'Tạo Voucher'}
                </button>
                <button
                  type="button"
                  onClick={() => setShowCreateModal(false)}
                  className="px-4 py-2.5 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 text-sm font-semibold rounded-xl transition-colors cursor-pointer"
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
            className="fixed inset-0 bg-black/40 backdrop-blur-xs transition-opacity"
            onClick={() => setEditingVoucher(null)}
          />
          <div className="relative z-10 bg-white border border-gray-200 rounded-2xl text-left overflow-hidden shadow-2xl transform transition-all sm:my-8 sm:max-w-lg sm:w-full">
            <form onSubmit={handleUpdate}>
              <div className="p-6">
                <div className="flex justify-between items-center mb-5 pb-3 border-b border-gray-100">
                  <div>
                    <h3 className="text-lg font-bold text-gray-900 flex items-center gap-2">
                      ✏️ Chỉnh Sửa Voucher
                    </h3>
                    <p className="text-xs text-gray-500 mt-0.5">
                      Đang sửa mã: <span className="font-mono font-bold text-orange-600 bg-orange-50 px-2 py-0.5 rounded border border-orange-200">{editingVoucher.code}</span>
                    </p>
                  </div>
                  <button
                    type="button"
                    onClick={() => setEditingVoucher(null)}
                    className="text-gray-400 hover:text-gray-600 text-xl font-bold cursor-pointer"
                  >
                    ×
                  </button>
                </div>

                {editError && (
                  <div className="bg-rose-50 border border-rose-200 text-rose-700 p-3 rounded-xl mb-4 text-xs">
                    {editError}
                  </div>
                )}

                <div className="space-y-4">
                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Loại Giảm Giá</label>
                    <select
                      required
                      value={editFormData.type}
                      onChange={e => setEditFormData({...editFormData, type: e.target.value as any})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                    >
                      <option value="PERCENTAGE">Phần trăm (%)</option>
                      <option value="FIXED_AMOUNT">Số tiền cố định (VNĐ)</option>
                    </select>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                      Mức Giảm {editFormData.type === 'PERCENTAGE' ? '(%)' : '(VNĐ)'}
                    </label>
                    <input
                      required
                      type="number"
                      min="1"
                      max={editFormData.type === 'PERCENTAGE' ? 100 : undefined}
                      value={editFormData.discountValue ?? ''}
                      onChange={e => setEditFormData({...editFormData, discountValue: Number(e.target.value)})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                    />
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">
                        Tối Đa Giảm (VNĐ)
                        {editFormData.type === 'PERCENTAGE' && <span className="text-rose-500">*</span>}
                      </label>
                      <input
                        type="number"
                        min="0"
                        required={editFormData.type === 'PERCENTAGE'}
                        value={editFormData.maxDiscount ?? ''}
                        onChange={e => setEditFormData({...editFormData, maxDiscount: e.target.value ? Number(e.target.value) : undefined})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                        placeholder="Không giới hạn"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Đơn Tối Thiểu (VNĐ)</label>
                      <input
                        type="number"
                        min="0"
                        value={editFormData.minOrderValue ?? ''}
                        onChange={e => setEditFormData({...editFormData, minOrderValue: e.target.value ? Number(e.target.value) : undefined})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                        placeholder="0"
                      />
                    </div>
                  </div>

                  <div className="grid grid-cols-2 gap-3">
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Bắt Đầu</label>
                      <input
                        required
                        type="datetime-local"
                        value={editFormData.startDate || ''}
                        onChange={e => setEditFormData({...editFormData, startDate: e.target.value})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      />
                    </div>
                    <div>
                      <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Kết Thúc</label>
                      <input
                        required
                        type="datetime-local"
                        value={editFormData.endDate || ''}
                        onChange={e => setEditFormData({...editFormData, endDate: e.target.value})}
                        className="block w-full bg-white border border-gray-300 rounded-xl px-3 py-2 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      />
                    </div>
                  </div>

                  <div>
                    <label className="block text-xs font-semibold text-gray-700 uppercase mb-1.5">Giới Hạn Số Lần Dùng</label>
                    <input
                      type="number"
                      min="1"
                      value={editFormData.usageLimit ?? ''}
                      onChange={e => setEditFormData({...editFormData, usageLimit: e.target.value ? Number(e.target.value) : undefined})}
                      className="block w-full bg-white border border-gray-300 rounded-xl px-3.5 py-2.5 text-gray-900 text-sm focus:border-orange-500 focus:ring-1 focus:ring-orange-500 focus:outline-none transition-colors"
                      placeholder="Không giới hạn"
                    />
                  </div>
                </div>
              </div>

              <div className="bg-gray-50 px-6 py-4 flex flex-row-reverse gap-3 border-t border-gray-100">
                <button
                  type="submit"
                  disabled={isEditSubmitting}
                  className="px-5 py-2.5 bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white text-sm font-semibold rounded-xl shadow-sm disabled:opacity-50 transition-all cursor-pointer"
                >
                  {isEditSubmitting ? 'Đang lưu...' : 'Lưu Thay Đổi'}
                </button>
                <button
                  type="button"
                  onClick={() => setEditingVoucher(null)}
                  className="px-4 py-2.5 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 text-sm font-semibold rounded-xl transition-colors cursor-pointer"
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
            className="fixed inset-0 bg-black/40 backdrop-blur-xs transition-opacity"
            onClick={() => setDeletingVoucher(null)}
          />
          <div className="relative z-10 bg-white border border-gray-200 rounded-2xl text-left overflow-hidden shadow-2xl transform transition-all sm:my-8 sm:max-w-md sm:w-full p-6">
            <div className="flex items-center gap-3 text-rose-600 mb-4">
              <div className="w-10 h-10 rounded-full bg-rose-50 border border-rose-200 flex items-center justify-center text-xl">
                ⚠️
              </div>
              <h3 className="text-lg font-bold text-gray-900">Xác Nhận Xóa Voucher</h3>
            </div>

            <p className="text-sm text-gray-600 mb-6">
              Bạn có chắc chắn muốn xóa vĩnh viễn voucher{' '}
              <span className="font-mono font-bold text-orange-600 bg-orange-50 px-2 py-0.5 rounded border border-orange-200">
                {deletingVoucher.code}
              </span>{' '}
              khỏi hệ thống? Hành động này không thể hoàn tác.
            </p>

            <div className="flex justify-end gap-3">
              <button
                type="button"
                disabled={isDeleting}
                onClick={() => setDeletingVoucher(null)}
                className="px-4 py-2 bg-white border border-gray-300 hover:bg-gray-50 text-gray-700 text-sm font-semibold rounded-xl transition-colors cursor-pointer"
              >
                Hủy bỏ
              </button>
              <button
                type="button"
                disabled={isDeleting}
                onClick={handleDelete}
                className="px-4 py-2 bg-rose-600 hover:bg-rose-700 text-white text-sm font-semibold rounded-xl shadow-sm disabled:opacity-50 transition-all cursor-pointer"
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
