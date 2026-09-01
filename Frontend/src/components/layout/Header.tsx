import React from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';
import { useCart } from '../../contexts/CartContext';
import { useState, useEffect } from 'react';

export const Header: React.FC = () => {
  const { isAuthenticated, isAdmin, logout } = useAuth();
  const { cartItemCount } = useCart();
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const [keyword, setKeyword] = useState('');

  useEffect(() => {
    // Sync search input with URL if it exists
    const currentKeyword = searchParams.get('keyword');
    if (currentKeyword) {
      setKeyword(currentKeyword);
    } else {
      setKeyword('');
    }
  }, [searchParams]);

  const handleLogout = () => {
    logout();
    navigate('/login');
  };

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    if (keyword.trim()) {
      navigate(`/products?keyword=${encodeURIComponent(keyword.trim())}`);
    } else {
      navigate(`/products`);
    }
  };

  return (
    <header className="bg-gradient-to-b from-orange-500 to-orange-600 text-white shadow-md z-50 sticky top-0">
      {/* Top Bar */}
      <div className="container mx-auto px-4 max-w-7xl h-8 flex items-center justify-between text-sm font-medium">
        <div className="flex items-center space-x-4">
          <Link to="/seller" className="hover:text-white/80 transition-colors">Kênh Người Bán</Link>
          <span className="w-px h-3 bg-white/30"></span>
          <Link to="/app" className="hover:text-white/80 transition-colors">Tải ứng dụng</Link>
          <span className="w-px h-3 bg-white/30"></span>
          <div className="flex items-center space-x-2">
            <span>Kết nối</span>
            <svg className="w-4 h-4 cursor-pointer hover:text-white/80" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm1 15h-2v-2h2v2zm0-4h-2V7h2v6z"/></svg>
            <svg className="w-4 h-4 cursor-pointer hover:text-white/80" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2C6.48 2 2 6.48 2 12s4.48 10 10 10 10-4.48 10-10S17.52 2 12 2zm0 18c-4.41 0-8-3.59-8-8s3.59-8 8-8 8 3.59 8 8-3.59 8-8 8zm-1-13h2v6h-2zm0 8h2v2h-2z"/></svg>
          </div>
        </div>

        <div className="flex items-center space-x-4">
          <div className="flex items-center space-x-1 cursor-pointer hover:text-white/80 transition-colors">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 17h5l-1.405-1.405A2.032 2.032 0 0118 14.158V11a6.002 6.002 0 00-4-5.659V5a2 2 0 10-4 0v.341C7.67 6.165 6 8.388 6 11v3.159c0 .538-.214 1.055-.595 1.436L4 17h5m6 0v1a3 3 0 11-6 0v-1m6 0H9"/></svg>
            <span>Thông Báo</span>
          </div>
          <div className="flex items-center space-x-1 cursor-pointer hover:text-white/80 transition-colors">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8.228 9c.549-1.165 2.03-2 3.772-2 2.21 0 4 1.343 4 3 0 1.4-1.278 2.575-3.006 2.907-.542.104-.994.54-.994 1.093m0 3h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z"/></svg>
            <span>Hỗ Trợ</span>
          </div>
          <div className="flex items-center space-x-1 cursor-pointer hover:text-white/80 transition-colors">
            <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 5h12M9 3v2m1.048 9.5A18.022 18.022 0 016.412 9m6.088 9h7M11 21l5-10 5 10M12.751 5C11.783 10.77 8.07 15.61 3 18.129"/></svg>
            <span>Tiếng Việt</span>
            <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M19 9l-7 7-7-7"/></svg>
          </div>
          
          {isAuthenticated ? (
            <div className="flex items-center space-x-4 relative group">
              <div className="flex items-center space-x-2 cursor-pointer hover:text-white/80">
                <div className="w-6 h-6 bg-white/20 rounded-full flex items-center justify-center overflow-hidden">
                  <svg className="w-4 h-4 text-white" fill="currentColor" viewBox="0 0 20 20"><path fillRule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clipRule="evenodd"/></svg>
                </div>
                <span>Tài khoản của tôi</span>
              </div>
              
              {/* Dropdown Menu */}
              <div className="absolute right-0 top-full pt-2 opacity-0 invisible group-hover:opacity-100 group-hover:visible transition-all duration-200 z-50">
                <div className="bg-white text-gray-800 rounded-sm shadow-lg w-40 overflow-hidden border border-gray-100">
                  <Link to="/profile" className="block px-4 py-2 hover:bg-gray-50 hover:text-orange-500 text-sm">Tài khoản của tôi</Link>
                  <Link to="/orders" className="block px-4 py-2 hover:bg-gray-50 hover:text-orange-500 text-sm">Đơn Mua</Link>
                  <Link to="/seller" className="block px-4 py-2 hover:bg-gray-50 hover:text-orange-500 text-sm font-medium text-orange-600">Kênh Người Bán</Link>
                  {isAdmin && (
                    <Link to="/admin" className="block px-4 py-2 hover:bg-gray-50 hover:text-orange-500 text-sm">Trang Quản Trị</Link>
                  )}
                  <button onClick={handleLogout} className="block w-full text-left px-4 py-2 hover:bg-gray-50 hover:text-orange-500 text-sm">Đăng xuất</button>
                </div>
              </div>
            </div>
          ) : (
            <div className="flex items-center space-x-3">
              <Link to="/register" className="font-bold hover:text-white/80 transition-colors">Đăng Ký</Link>
              <span className="w-px h-3 bg-white/30"></span>
              <Link to="/login" className="font-bold hover:text-white/80 transition-colors">Đăng Nhập</Link>
            </div>
          )}
        </div>
      </div>

      {/* Main Header */}
      <div className="container mx-auto px-4 max-w-7xl h-[85px] flex items-center py-4">
        {/* Logo */}
        <Link to="/" className="flex items-center space-x-2 text-white w-48 shrink-0 mr-8">
          <svg xmlns="http://www.w3.org/2000/svg" className="w-10 h-10" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
            <path d="M12 5c.67 0 1.35.09 2 .26 1.78-2 5.03-2.84 6.42-2.26 1.4.58-.42 7-.42 7 .57 1.07 1 2.24 1 3.44C21 17.9 16.97 21 12 21s-9-3.1-9-7.56c0-1.25.5-2.4 1-3.44 0 0-1.89-6.42-.5-7 1.39-.58 4.72.23 6.5 2.23A9.04 9.04 0 0 1 12 5Z"/>
            <path d="M8 14v.5"/><path d="M16 14v.5"/><path d="M11.25 16.25h1.5L12 17l-.75-.75Z"/>
          </svg>
          <span className="text-3xl font-bold tracking-tighter">MintMark</span>
        </Link>

        {/* Search Bar */}
        <div className="flex-1 flex flex-col justify-center">
          <form onSubmit={handleSearch} className="relative flex items-center w-full h-10 bg-white rounded-sm shadow-sm p-1">
            <input 
              type="text" 
              placeholder="Sale khủng giá cực sốc hôm nay" 
              value={keyword}
              onChange={(e) => setKeyword(e.target.value)}
              className="flex-1 h-full px-3 text-gray-800 text-sm outline-none bg-transparent"
            />
            <button type="submit" className="h-8 px-5 bg-orange-500 hover:bg-orange-600 transition-colors rounded-sm text-white flex items-center justify-center">
              <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"/></svg>
            </button>
          </form>
          {/* Quick links below search */}
          <div className="flex space-x-3 mt-1 text-xs text-white/90">
            <span className="hover:text-white cursor-pointer">Áo Nữ</span>
            <span className="hover:text-white cursor-pointer">Săn iPhone 0 Đồng</span>
            <span className="hover:text-white cursor-pointer">Dép Sục Crocs</span>
            <span className="hover:text-white cursor-pointer">Đồ Ăn Vặt</span>
          </div>
        </div>

        {/* Cart */}
        <div className="w-24 shrink-0 flex justify-center items-start mt-2">
          <Link to="/cart" className="relative group p-2">
            <svg className="w-8 h-8 text-white" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z"/>
            </svg>
            <span className="absolute top-0 right-0 bg-white text-orange-500 text-[10px] font-bold px-1.5 py-0.5 rounded-full border-2 border-orange-500 leading-none shadow-sm">{cartItemCount}</span>
          </Link>
        </div>
      </div>
    </header>
  );
};
