import React, { useEffect, useState } from 'react';
import { NavLink } from 'react-router-dom';
import { shopApi } from '../../api/shopApi';
import type { ShopResponse } from '../../api/types/shop.types';

export const VendorNav: React.FC = () => {
  const [shop, setShop] = useState<ShopResponse | null>(null);

  useEffect(() => {
    shopApi.getMyShop()
      .then(res => {
        if (res.data.data) {
          setShop(res.data.data);
        }
      })
      .catch(() => {

      });
  }, []);

  return (
    <div className="bg-white border-b border-gray-200 mb-6 shadow-sm rounded-lg overflow-hidden">
      <div className="px-6 py-4 flex flex-col md:flex-row md:items-center md:justify-between border-b border-gray-100 gap-4">
        <div className="flex items-center space-x-3">
          <div className="w-10 h-10 rounded-lg bg-orange-100 text-orange-600 flex items-center justify-center font-bold text-lg">
            🏪
          </div>
          <div>
            <div className="flex items-center space-x-2">
              <h1 className="text-lg font-bold text-gray-900">
                {shop ? shop.name : 'Kênh Người Bán'}
              </h1>
              {shop && (
                <span className="text-xs px-2 py-0.5 rounded-full font-medium bg-green-100 text-green-700">
                  {shop.status === 'ACTIVE' ? 'Đang hoạt động' : shop.status}
                </span>
              )}
            </div>
            <p className="text-xs text-gray-500">
              {shop?.description || 'Hệ thống quản lý đơn hàng & sản phẩm'}
            </p>
          </div>
        </div>

        <div className="flex items-center space-x-2">
          <NavLink
            to="/vendor/products/new"
            className="inline-flex items-center px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white text-sm font-medium rounded-lg shadow-sm transition-colors"
          >
            <span className="mr-1.5 text-base leading-none">+</span> Thêm Sản Phẩm Mới
          </NavLink>
        </div>
      </div>

      <div className="flex space-x-8 px-6 text-sm font-medium border-t border-gray-50">
        <NavLink
          to="/vendor/products"
          className={({ isActive }) =>
            `py-3 border-b-2 font-semibold transition-colors flex items-center space-x-2 ${
              isActive
                ? 'border-orange-500 text-orange-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`
          }
        >
          <span>🏷️ Quản Lý Sản Phẩm</span>
        </NavLink>

        <NavLink
          to="/vendor/orders"
          className={({ isActive }) =>
            `py-3 border-b-2 font-semibold transition-colors flex items-center space-x-2 ${
              isActive
                ? 'border-orange-500 text-orange-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`
          }
        >
          <span>📦 Quản Lý Đơn Hàng</span>
        </NavLink>

        <NavLink
          to="/vendor/vouchers"
          className={({ isActive }) =>
            `py-3 border-b-2 font-semibold transition-colors flex items-center space-x-2 ${
              isActive
                ? 'border-orange-500 text-orange-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`
          }
        >
          <span>🎟️ Quản Lý Voucher</span>
        </NavLink>
        <NavLink
          to="/vendor/analytics"
          className={({ isActive }) =>
            `py-3 border-b-2 font-semibold transition-colors flex items-center space-x-2 ${
              isActive
                ? 'border-orange-500 text-orange-600'
                : 'border-transparent text-gray-500 hover:text-gray-700 hover:border-gray-300'
            }`
          }
        >
          <span>📊 Thống Kê</span>
        </NavLink>
      </div>
    </div>
  );
};
