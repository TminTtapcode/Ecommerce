import { getApiError } from '../../api/apiError';
import React, { useState } from 'react';
import { shopApi } from '../../api/shopApi';

export const VendorShopRegistrationPage: React.FC<{ onRegistered: () => void }> = ({ onRegistered }) => {
  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setLoading(true);
    setError('');

    try {
      await shopApi.registerShop({ name, description });
      onRegistered();
    } catch (err: any) {
      const details = getApiError(err);
      if (details.fieldErrors) {

        const errs = details.fieldErrors;
        const msgs = Object.values(errs).join(', ');
        setError(msgs);
      } else {
        setError(details.message);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 py-12 px-4 sm:px-6 lg:px-8 flex justify-center">
      <div className="max-w-md w-full space-y-8 bg-white p-8 rounded-xl shadow-sm border border-gray-100">
        <div>
          <h2 className="mt-6 text-center text-3xl font-extrabold text-gray-900">
            Đăng ký Kênh Người Bán
          </h2>
          <p className="mt-2 text-center text-sm text-gray-600">
            Trở thành nhà cung cấp và bắt đầu bán hàng ngay hôm nay.
          </p>
        </div>

        {error && (
          <div className="bg-red-50 border-l-4 border-red-400 p-4 rounded">
            <p className="text-sm text-red-700">{error}</p>
          </div>
        )}

        <form className="mt-8 space-y-6" onSubmit={handleSubmit}>
          <div className="space-y-4 rounded-md shadow-sm">
            <div>
              <label htmlFor="shop-name" className="block text-sm font-medium text-gray-700">
                Tên Shop <span className="text-red-500">*</span>
              </label>
              <input
                id="shop-name"
                name="name"
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
                placeholder="Ví dụ: Cửa hàng Thời trang Z"
              />
            </div>
            <div>
              <label htmlFor="shop-desc" className="block text-sm font-medium text-gray-700">
                Mô tả (Tùy chọn)
              </label>
              <textarea
                id="shop-desc"
                name="description"
                rows={3}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="mt-1 appearance-none relative block w-full px-3 py-2 border border-gray-300 rounded-md focus:outline-none focus:ring-orange-500 focus:border-orange-500 focus:z-10 sm:text-sm"
                placeholder="Kinh doanh thời trang, mỹ phẩm..."
              />
            </div>
          </div>

          <div>
            <button
              type="submit"
              disabled={loading}
              className="group relative w-full flex justify-center py-2 px-4 border border-transparent text-sm font-medium rounded-md text-white bg-orange-600 hover:bg-orange-700 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 disabled:opacity-50 transition-colors"
            >
              {loading ? 'Đang đăng ký...' : 'Đăng Ký Ngay'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
