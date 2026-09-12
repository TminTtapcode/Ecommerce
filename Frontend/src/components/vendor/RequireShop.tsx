import { ShopAccessContext } from '../../contexts/ShopAccessContext';
import React, { useEffect, useState } from 'react';
import { shopApi } from '../../api/shopApi';
import { getApiError } from '../../api/apiError';
import { ERROR_CODES } from '../../api/types/errorCodes';
import type { ShopResponse } from '../../api/types/shop.types';
import { VendorShopRegistrationPage } from '../../pages/vendor/VendorShopRegistrationPage';
import { ShopPendingPage } from '../../pages/vendor/ShopPendingPage';
import { Link, useLocation } from 'react-router-dom';

interface RequireShopProps {
  children: React.ReactNode;
  allowBanned?: boolean;
}

export const RequireShop: React.FC<RequireShopProps> = ({ children, allowBanned = false }) => {
  const { pathname } = useLocation();
  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [loading, setLoading] = useState(true);
  const [needsRegistration, setNeedsRegistration] = useState(false);
  const [globalError, setGlobalError] = useState<string | null>(null);

  const checkShop = async (background = false) => {
    if (!background) setLoading(true);
    setNeedsRegistration(false);
    setGlobalError(null);

    try {
      const res = await shopApi.getMyShop();
      setShop(res.data.data ?? null);
    } catch (err: any) {
      const details = getApiError(err, 'Có lỗi xảy ra khi tải thông tin Shop.');
      if (details.errorCode === ERROR_CODES.SHOP_NOT_FOUND) {
        setNeedsRegistration(true);
      } else {
        setGlobalError(details.message);
      }
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    checkShop();
    const refresh = () => { void checkShop(true); };
    window.addEventListener('focus', refresh);
    return () => window.removeEventListener('focus', refresh);
  }, [pathname]);

  if (loading) {
    return (
      <div className="flex justify-center items-center h-screen bg-gray-50">
        <div className="w-10 h-10 border-4 border-orange-500 border-t-transparent rounded-full animate-spin"></div>
      </div>
    );
  }

  if (globalError) {
    return (
      <div className="min-h-screen bg-gray-50 flex items-center justify-center px-4">
        <div className="max-w-md w-full text-center">
          <div className="text-red-500 mb-4">
            <svg className="w-16 h-16 mx-auto" fill="none" viewBox="0 0 24 24" stroke="currentColor">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
            </svg>
          </div>
          <h2 className="text-xl font-bold text-gray-900 mb-2">Lỗi truy cập</h2>
          <p className="text-gray-600 mb-6">{globalError}</p>
          <Link to="/" className="px-4 py-2 bg-gray-200 text-gray-800 rounded-lg hover:bg-gray-300 transition-colors">
            Quay về trang chủ
          </Link>
        </div>
      </div>
    );
  }

  if (needsRegistration) {
    return <VendorShopRegistrationPage onRegistered={checkShop} />;
  }

  if (shop?.status === 'PENDING') {
    return <ShopPendingPage />;
  }

  if (shop?.status === 'BANNED' && !allowBanned) {
    return <div className="p-8"><p>Gian hàng đang bị khóa, không thể thực hiện hoạt động bán mới.</p>
      <Link to="/vendor/orders">Xử lý đơn hàng hiện có</Link></div>;
  }

  return <ShopAccessContext.Provider value={{ banned: shop?.status === 'BANNED' }}>
    {shop?.status === 'BANNED' && <div role="status" className="bg-amber-100 p-4">
      Gian hàng đang bị khóa. Bạn vẫn có thể xem sản phẩm và xử lý đơn hàng hiện có.
    </div>}
    {children}
  </ShopAccessContext.Provider>;
};
