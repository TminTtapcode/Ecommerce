import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { cartApi } from '../../api/cartApi';
import type { CartItemResponse } from '../../api/types/cart.types';

export const CartPage: React.FC = () => {
  const { cart, loading, fetchCart } = useCart();
  const [selectedItemIds, setSelectedItemIds] = useState<number[]>([]);
  const [updatingItemId, setUpdatingItemId] = useState<number | null>(null);
  const navigate = useNavigate();

  // Group items by shopId
  const groupedItems = cart?.items.reduce((acc, item) => {
    if (!acc[item.shopId]) {
      acc[item.shopId] = [];
    }
    acc[item.shopId].push(item);
    return acc;
  }, {} as Record<number, CartItemResponse[]>) || {};

  const allAvailableItems = cart?.items.filter(item => item.isAvailable) || [];
  const allAvailableIds = allAvailableItems.map(item => item.cartItemId);
  
  const handleSelectAll = (checked: boolean) => {
    if (checked) {
      setSelectedItemIds(allAvailableIds);
    } else {
      setSelectedItemIds([]);
    }
  };

  const handleSelectShop = (shopId: number, checked: boolean) => {
    const shopItemIds = (groupedItems[shopId] || [])
      .filter(item => item.isAvailable)
      .map(item => item.cartItemId);

    if (checked) {
      setSelectedItemIds(prev => Array.from(new Set([...prev, ...shopItemIds])));
    } else {
      setSelectedItemIds(prev => prev.filter(id => !shopItemIds.includes(id)));
    }
  };

  const handleSelectItem = (itemId: number, checked: boolean) => {
    if (checked) {
      setSelectedItemIds(prev => [...prev, itemId]);
    } else {
      setSelectedItemIds(prev => prev.filter(id => id !== itemId));
    }
  };

  const handleUpdateQuantity = async (itemId: number, newQuantity: number) => {
    if (newQuantity < 1) return;
    setUpdatingItemId(itemId);
    try {
      await cartApi.updateQuantity(itemId, { quantity: newQuantity });
      await fetchCart();
    } catch (error: any) {
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi cập nhật số lượng');
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleRemoveItem = async (itemId: number, skipConfirm = false) => {
    if (!skipConfirm && !window.confirm("Bạn có chắc muốn xóa sản phẩm này khỏi giỏ hàng?")) return;
    setUpdatingItemId(itemId);
    try {
      await cartApi.removeCartItem(itemId);
      setSelectedItemIds(prev => prev.filter(id => id !== itemId)); // Remove from selected
      await fetchCart();
    } catch (error: any) {
      alert('Có lỗi xảy ra khi xóa sản phẩm');
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleBulkRemove = async () => {
    if (selectedItemIds.length === 0) return;
    if (!window.confirm(`Bạn có chắc muốn xóa ${selectedItemIds.length} sản phẩm đã chọn?`)) return;
    
    setUpdatingItemId(-1); // -1 indicates bulk update
    try {
      // Execute all delete requests in parallel
      await Promise.all(selectedItemIds.map(id => cartApi.removeCartItem(id)));
      setSelectedItemIds([]);
      await fetchCart();
    } catch (error) {
      alert('Có lỗi xảy ra khi xóa sản phẩm');
    } finally {
      setUpdatingItemId(null);
    }
  };

  const handleClearCart = async () => {
    if (!cart || cart.items.length === 0) return;
    if (!window.confirm("Bạn có chắc muốn dọn sạch toàn bộ giỏ hàng? Hành động này không thể hoàn tác.")) return;
    
    setUpdatingItemId(-2); // -2 indicates clear cart
    try {
      await cartApi.clearCart();
      setSelectedItemIds([]);
      await fetchCart();
    } catch (error) {
      alert('Có lỗi xảy ra khi dọn dẹp giỏ hàng');
    } finally {
      setUpdatingItemId(null);
    }
  };

  // Calculate selected total
  const selectedTotal = allAvailableItems
    .filter(item => selectedItemIds.includes(item.cartItemId))
    .reduce((sum, item) => sum + item.subTotal, 0);

  const formatPrice = (price: number) => {
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
  };

  const isAllSelected = allAvailableIds.length > 0 && selectedItemIds.length === allAvailableIds.length;

  if (loading && !cart) {
    return <div className="p-8 text-center text-gray-500">Đang tải giỏ hàng...</div>;
  }

  if (!cart || cart.items.length === 0) {
    return (
      <div className="max-w-4xl mx-auto py-16 text-center">
        <div className="mb-4">
          <svg className="w-24 h-24 text-gray-300 mx-auto" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
          </svg>
        </div>
        <p className="text-gray-500 mb-6">Giỏ hàng của bạn còn trống</p>
        <Link to="/" className="px-6 py-2 bg-orange-500 text-white rounded hover:bg-orange-600 transition-colors">
          MUA SẮM NGAY
        </Link>
      </div>
    );
  }

  return (
    <div className="max-w-6xl mx-auto py-8 px-4 pb-32">
      {/* Header Row */}
      <div className="grid grid-cols-12 gap-4 bg-white p-4 rounded-sm shadow-sm text-sm text-gray-500 mb-4">
        <div className="col-span-6 flex items-center gap-4">
          <input 
            type="checkbox" 
            className="w-4 h-4 accent-orange-500"
            checked={isAllSelected}
            onChange={(e) => handleSelectAll(e.target.checked)}
          />
          <span>Sản Phẩm</span>
        </div>
        <div className="col-span-2 text-center">Đơn Giá</div>
        <div className="col-span-2 text-center">Số Lượng</div>
        <div className="col-span-1 text-center">Số Tiền</div>
        <div className="col-span-1 text-center">Thao Tác</div>
      </div>

      {/* Shop Groups */}
      {Object.entries(groupedItems).map(([shopId, items]) => {
        const shopAvailableIds = items.filter(item => item.isAvailable).map(item => item.cartItemId);
        const isShopAllSelected = shopAvailableIds.length > 0 && shopAvailableIds.every(id => selectedItemIds.includes(id));

        return (
          <div key={shopId} className="bg-white rounded-sm shadow-sm mb-4">
            {/* Shop Header */}
            <div className="p-4 border-b border-gray-100 flex items-center gap-3">
              <input 
                type="checkbox" 
                className="w-4 h-4 accent-orange-500"
                checked={isShopAllSelected}
                onChange={(e) => handleSelectShop(Number(shopId), e.target.checked)}
              />
              <span className="font-medium">Shop ID: {shopId}</span>
              <svg className="w-4 h-4 text-gray-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z"/></svg>
            </div>

            {/* Shop Items */}
            <div>
              {items.map((item) => (
                <div key={item.cartItemId} className="grid grid-cols-12 gap-4 p-4 border-b border-gray-50 items-center last:border-0 relative">
                  {!item.isAvailable && (
                    <div className="absolute inset-0 bg-white/60 z-10 flex items-center justify-center">
                      <span className="bg-gray-800 text-white px-3 py-1 text-xs rounded uppercase font-bold">Ngừng kinh doanh / Hết hàng</span>
                    </div>
                  )}
                  
                  <div className="col-span-6 flex items-start gap-4">
                    <input 
                      type="checkbox" 
                      className="w-4 h-4 mt-1 accent-orange-500"
                      checked={selectedItemIds.includes(item.cartItemId)}
                      onChange={(e) => handleSelectItem(item.cartItemId, e.target.checked)}
                      disabled={!item.isAvailable}
                    />
                    <img src={item.thumbnailUrl} alt={item.productName} className="w-20 h-20 object-cover border" />
                    <div className="flex-1">
                      <Link to={`/product/${item.productVariantId}`} className="text-sm text-gray-800 line-clamp-2 hover:text-orange-500 mb-1">
                        {item.productName}
                      </Link>
                      {item.attributes && Object.keys(item.attributes).length > 0 && (
                        <div className="text-xs text-gray-500">
                          Phân loại hàng: {Object.values(item.attributes).join(', ')}
                        </div>
                      )}
                    </div>
                  </div>
                  
                  <div className="col-span-2 text-center text-sm">
                    {formatPrice(item.unitPrice)}
                  </div>
                  
                  <div className="col-span-2 flex justify-center">
                    <div className="flex items-center border border-gray-300 rounded overflow-hidden h-8 w-24">
                      <button 
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity - 1)}
                        disabled={updatingItemId === item.cartItemId || !item.isAvailable}
                        className="px-2 bg-gray-50 hover:bg-gray-100 text-gray-600 border-r disabled:opacity-50"
                      >
                        -
                      </button>
                      <input 
                        type="text" 
                        value={item.quantity} 
                        readOnly 
                        className="w-10 text-center text-sm outline-none bg-white"
                      />
                      <button 
                        onClick={() => handleUpdateQuantity(item.cartItemId, item.quantity + 1)}
                        disabled={updatingItemId === item.cartItemId || !item.isAvailable}
                        className="px-2 bg-gray-50 hover:bg-gray-100 text-gray-600 border-l disabled:opacity-50"
                      >
                        +
                      </button>
                    </div>
                  </div>
                  
                  <div className="col-span-1 text-center text-sm text-orange-500 font-medium">
                    {formatPrice(item.subTotal)}
                  </div>
                  
                  <div className="col-span-1 text-center">
                    <button 
                      onClick={() => handleRemoveItem(item.cartItemId)}
                      className="text-gray-500 hover:text-red-500 text-sm relative z-20"
                    >
                      Xóa
                    </button>
                  </div>
                </div>
              ))}
            </div>
          </div>
        );
      })}

      {/* Sticky Footer for Checkout */}
      <div className="fixed bottom-0 left-0 right-0 bg-white border-t border-gray-200 shadow-[0_-4px_10px_rgba(0,0,0,0.05)] z-40 py-4">
        <div className="max-w-6xl mx-auto px-4 flex items-center justify-between">
          <div className="flex items-center gap-6">
            <label className="flex items-center gap-2 cursor-pointer text-gray-700">
              <input 
                type="checkbox" 
                className="w-4 h-4 accent-orange-500"
                checked={isAllSelected}
                onChange={(e) => handleSelectAll(e.target.checked)}
              />
              <span>Chọn Tất Cả ({allAvailableIds.length})</span>
            </label>
            <button 
              className="text-gray-500 hover:text-red-500"
              disabled={selectedItemIds.length === 0 || updatingItemId === -1}
              onClick={handleBulkRemove}
            >
              Xóa Đã Chọn
            </button>
            <button 
              className="text-gray-500 hover:text-red-500 ml-4 border-l border-gray-300 pl-4"
              disabled={updatingItemId === -2}
              onClick={handleClearCart}
            >
              Dọn Sạch Giỏ Hàng
            </button>
          </div>
          
          <div className="flex items-center gap-6">
            <div className="text-right">
              <div className="text-gray-700">
                Tổng thanh toán ({selectedItemIds.length} Sản phẩm): <span className="text-2xl text-orange-500 font-medium ml-2">{formatPrice(selectedTotal)}</span>
              </div>
            </div>
            <button 
              className={`px-10 py-3 text-white rounded-sm text-lg shadow-sm transition-colors ${selectedItemIds.length > 0 ? 'bg-orange-500 hover:bg-orange-600' : 'bg-gray-300 cursor-not-allowed'}`}
              disabled={selectedItemIds.length === 0}
              onClick={() => navigate('/checkout', { state: { selectedItemIds } })}
            >
              Mua Hàng
            </button>
          </div>
        </div>
      </div>
    </div>
  );
};
