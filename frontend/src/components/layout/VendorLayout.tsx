import React, { useState, useEffect, useRef } from 'react';
import { Outlet, Link, useNavigate, useLocation } from 'react-router-dom';
import { 
  Home, 
  Package, 
  FileText, 
  Tag, 
  BarChart3, 
  Store, 
  LogOut, 
  ChevronDown,
  Menu,
  X,
  ExternalLink,
  Wallet
} from 'lucide-react';
import { useAuth } from '../../contexts/AuthContext';
import { shopApi } from '../../api/shopApi';
import type { ShopResponse } from '../../api/types/shop.types';

export const VendorLayout: React.FC = () => {
  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [isShopMenuOpen, setIsShopMenuOpen] = useState(false);
  const [isMobileMenuOpen, setIsMobileMenuOpen] = useState(false);
  const shopMenuRef = useRef<HTMLDivElement>(null);
  const { logout } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();

  useEffect(() => {
    shopApi.getMyShop()
      .then((res) => {
        if (res.data?.data) {
          setShop(res.data.data);
        }
      })
      .catch(() => {});
  }, []);

  useEffect(() => {
    const handleClickOutside = (event: MouseEvent) => {
      if (shopMenuRef.current && !shopMenuRef.current.contains(event.target as Node)) {
        setIsShopMenuOpen(false);
      }
    };
    document.addEventListener('mousedown', handleClickOutside);
    return () => document.removeEventListener('mousedown', handleClickOutside);
  }, []);

  useEffect(() => {
    setIsMobileMenuOpen(false);
  }, [location.pathname]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const navItems = [
    {
      id: 'overview',
      name: 'Tổng quan',
      path: '/vendor/overview',
      icon: Home,
      isActive: () => location.pathname === '/vendor/overview' || location.pathname === '/vendor',
    },
    {
      id: 'products',
      name: 'Sản phẩm',
      path: '/vendor/products',
      icon: Package,
      isActive: () => location.pathname.startsWith('/vendor/products'),
    },
    {
      id: 'orders',
      name: 'Đơn hàng',
      path: '/vendor/orders',
      icon: FileText,
      isActive: () => location.pathname.startsWith('/vendor/orders'),
    },
    {
      id: 'vouchers',
      name: 'Mã giảm giá',
      path: '/vendor/vouchers',
      icon: Tag,
      isActive: () => location.pathname.startsWith('/vendor/vouchers'),
    },
    {
      id: 'analytics',
      name: 'Báo cáo',
      path: '/vendor/analytics',
      icon: BarChart3,
      isActive: () => location.pathname.startsWith('/vendor/analytics'),
    },
    {
      id: 'payouts',
      name: 'Ví & Rút tiền',
      path: '/vendor/payouts',
      icon: Wallet,
      isActive: () => location.pathname.startsWith('/vendor/payouts'),
    },
  ];

  return (
    <div className="min-h-screen bg-[#F5F5F5] flex flex-col font-['Outfit',sans-serif]">
      <header className="h-16 bg-white border-b border-gray-200 px-4 sm:px-6 flex items-center justify-between z-20 shrink-0 shadow-xs">
        <div className="flex items-center space-x-3 sm:space-x-4">
          <button
            onClick={() => setIsMobileMenuOpen(!isMobileMenuOpen)}
            className="md:hidden p-1.5 text-gray-600 hover:text-orange-600 rounded-lg hover:bg-orange-50 cursor-pointer"
            aria-label="Toggle menu"
          >
            {isMobileMenuOpen ? <X className="w-5 h-5" /> : <Menu className="w-5 h-5" />}
          </button>

          <Link
            to="/vendor/overview"
            className="px-3.5 py-1.5 border border-orange-200 rounded-lg bg-orange-50 text-orange-700 font-bold text-xs tracking-wider uppercase flex items-center gap-1.5 hover:bg-orange-100 transition-colors shadow-2xs"
          >
            <Store className="w-4 h-4 text-orange-500" />
            <span className="truncate max-w-[130px] font-extrabold">{shop?.name ? shop.name.toUpperCase() : 'LOGO SHOP'}</span>
          </Link>

          <div className="h-5 w-px bg-gray-200 hidden sm:block"></div>

          <h1 className="text-sm sm:text-base md:text-lg font-bold text-gray-900 tracking-tight whitespace-nowrap">
            KÊNH NGƯỜI BÁN <span className="text-orange-600 font-black text-xs sm:text-sm tracking-normal">(VENDOR PORTAL)</span>
          </h1>
        </div>

        <div className="flex items-center space-x-2 sm:space-x-4">
          <div className="relative" ref={shopMenuRef}>
            <button
              onClick={() => setIsShopMenuOpen(!isShopMenuOpen)}
              className="flex items-center space-x-1.5 sm:space-x-2 px-2.5 sm:px-3 py-1.5 rounded-lg text-sm font-medium text-gray-700 hover:bg-orange-50 hover:text-orange-600 transition-colors cursor-pointer"
            >
              <Store className="w-4 h-4 text-orange-500" />
              <span className="font-semibold max-w-[100px] sm:max-w-[160px] truncate text-gray-800">
                {shop?.name || 'Tên Shop'}
              </span>
              <ChevronDown className={`w-3.5 h-3.5 text-gray-400 transition-transform ${isShopMenuOpen ? 'rotate-180' : ''}`} />
            </button>

            {isShopMenuOpen && (
              <div className="absolute right-0 mt-2 w-56 bg-white border border-gray-200 rounded-xl shadow-lg py-1.5 z-30 divide-y divide-gray-100">
                <div className="px-4 py-2.5">
                  <p className="text-xs text-gray-400">Đang quản lý</p>
                  <p className="text-sm font-bold text-gray-800 truncate">{shop?.name}</p>
                </div>
                {shop?.id && (
                  <Link
                    to={`/shops/${shop.id}`}
                    target="_blank"
                    rel="noopener noreferrer"
                    onClick={() => setIsShopMenuOpen(false)}
                    className="flex items-center justify-between px-4 py-2.5 text-xs text-gray-700 hover:bg-orange-50 hover:text-orange-600 transition-colors"
                  >
                    <span>Xem gian hàng</span>
                    <ExternalLink className="w-3.5 h-3.5 text-gray-400" />
                  </Link>
                )}
                <Link
                  to="/products"
                  onClick={() => setIsShopMenuOpen(false)}
                  className="flex items-center px-4 py-2.5 text-xs text-gray-700 hover:bg-orange-50 hover:text-orange-600 transition-colors"
                >
                  <span>Quay về trang mua sắm</span>
                </Link>
              </div>
            )}
          </div>

          <div className="h-5 w-px bg-gray-200 hidden sm:block"></div>

          <button
            onClick={handleLogout}
            className="flex items-center space-x-1.5 px-2.5 sm:px-3 py-1.5 text-sm font-medium text-gray-700 hover:text-rose-600 hover:bg-rose-50 rounded-lg transition-colors cursor-pointer"
          >
            <LogOut className="w-4 h-4" />
            <span className="hidden sm:inline">Đăng xuất</span>
          </button>
        </div>
      </header>

      <div className="flex-1 flex min-h-0 overflow-hidden">
        <aside className="w-60 bg-white border-r border-gray-200 flex flex-col shrink-0 hidden md:flex">
          <nav className="flex-1 py-4 px-3 space-y-1.5 overflow-y-auto">
            {navItems.map((item) => {
              const active = item.isActive();
              const Icon = item.icon;
              return (
                <Link
                  key={item.id}
                  to={item.path}
                  className={`flex items-center px-3.5 py-3 rounded-xl transition-all duration-150 ${
                    active
                      ? 'bg-orange-50 text-orange-600 font-bold border border-orange-200 shadow-xs'
                      : 'text-gray-600 hover:bg-orange-50/50 hover:text-orange-600 font-medium'
                  }`}
                >
                  <Icon className={`w-5 h-5 flex-shrink-0 ${active ? 'text-orange-500' : 'text-gray-400'}`} />
                  <span className="ml-3 text-sm">{item.name}</span>
                </Link>
              );
            })}
          </nav>
        </aside>

        {isMobileMenuOpen && (
          <div className="fixed inset-0 z-40 md:hidden flex">
            <div 
              className="fixed inset-0 bg-gray-900/40 backdrop-blur-xs transition-opacity" 
              onClick={() => setIsMobileMenuOpen(false)} 
            />
            <aside className="relative w-64 max-w-[80vw] bg-white h-full shadow-2xl flex flex-col z-50">
              <div className="p-4 border-b border-gray-200 flex items-center justify-between">
                <span className="font-bold text-gray-800 text-sm">Danh Mục Người Bán</span>
                <button 
                  onClick={() => setIsMobileMenuOpen(false)}
                  className="p-1 rounded-md text-gray-400 hover:text-gray-600 hover:bg-gray-100"
                >
                  <X className="w-5 h-5" />
                </button>
              </div>
              <nav className="flex-1 py-4 px-3 space-y-1.5 overflow-y-auto">
                {navItems.map((item) => {
                  const active = item.isActive();
                  const Icon = item.icon;
                  return (
                    <Link
                      key={item.id}
                      to={item.path}
                      onClick={() => setIsMobileMenuOpen(false)}
                      className={`flex items-center px-3.5 py-3 rounded-xl transition-all duration-150 ${
                        active
                          ? 'bg-orange-50 text-orange-600 font-bold border border-orange-200 shadow-xs'
                          : 'text-gray-600 hover:bg-orange-50/50 hover:text-orange-600 font-medium'
                      }`}
                    >
                      <Icon className={`w-5 h-5 flex-shrink-0 ${active ? 'text-orange-500' : 'text-gray-400'}`} />
                      <span className="ml-3 text-sm">{item.name}</span>
                    </Link>
                  );
                })}
              </nav>
            </aside>
          </div>
        )}

        <div className="flex-1 flex flex-col min-w-0 overflow-hidden bg-[#F5F5F5]">
          <main className="flex-1 overflow-y-auto">
            <Outlet />
          </main>
        </div>
      </div>
    </div>
  );
};
