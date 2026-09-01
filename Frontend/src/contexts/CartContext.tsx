import React, { createContext, useContext, useState, useEffect } from 'react';
import { cartApi } from '../api/cartApi';
import { useAuth } from './AuthContext';
import type { CartResponse } from '../api/types/cart.types';

interface CartContextType {
  cart: CartResponse | null;
  cartItemCount: number;
  loading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (productVariantId: number, quantity: number) => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }
    
    setLoading(true);
    try {
      const response = await cartApi.getCart();
      setCart(response.data.data || null);
    } catch (error) {
      console.error('Failed to fetch cart', error);
    } finally {
      setLoading(false);
    }
  };

  useEffect(() => {
    fetchCart();
  }, [isAuthenticated]);

  const addToCart = async (productVariantId: number, quantity: number) => {
    if (!isAuthenticated) {
      alert("Vui lòng đăng nhập để thêm vào giỏ hàng");
      return;
    }
    
    try {
      await cartApi.addToCart({ productVariantId, quantity });
      // Refresh cart after adding
      await fetchCart();
      alert("Đã thêm vào giỏ hàng");
    } catch (error: any) {
      console.error('Failed to add to cart', error);
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
      throw error;
    }
  };

  // We consider item count as total quantity of all items or number of unique items?
  // Usually it's the total quantity, but number of unique items is also fine.
  // Let's use number of unique items to be simple, or sum of quantities. 
  // Let's use sum of quantities for accuracy.
  const cartItemCount = cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0;

  return (
    <CartContext.Provider value={{ cart, cartItemCount, loading, fetchCart, addToCart }}>
      {children}
    </CartContext.Provider>
  );
};

export const useCart = () => {
  const context = useContext(CartContext);
  if (context === undefined) {
    throw new Error('useCart must be used within a CartProvider');
  }
  return context;
};
