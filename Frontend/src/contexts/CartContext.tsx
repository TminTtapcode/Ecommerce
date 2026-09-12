import React, { createContext, useContext, useState, useEffect, useCallback, useMemo } from 'react';
import { cartApi } from '../api/cartApi';
import { useAuth } from './AuthContext';
import type { CartResponse } from '../api/types/cart.types';

interface CartContextType {
  cart: CartResponse | null;
  cartItemCount: number;
  loading: boolean;
  fetchCart: () => Promise<void>;
  addToCart: (variantId: number, quantity: number) => Promise<void>;
}

const CartContext = createContext<CartContextType | undefined>(undefined);

export const CartProvider: React.FC<{ children: React.ReactNode }> = ({ children }) => {
  const { isAuthenticated } = useAuth();
  const [cart, setCart] = useState<CartResponse | null>(null);
  const [loading, setLoading] = useState(false);

  const fetchCart = useCallback(async () => {
    if (!isAuthenticated) {
      setCart(null);
      return;
    }

    setLoading(true);
    try {
      const response = await cartApi.getCart();
      const rawData = response.data.data;
      if (rawData && rawData.items) {
        const normalizedItems = rawData.items.map((item) => ({
          ...item,
          id: item.cartItemId ?? item.id,
          cartItemId: item.cartItemId ?? item.id,
          variantId: item.productVariantId ?? item.variantId,
          productVariantId: item.productVariantId ?? item.variantId,
          unitPrice: item.unitPrice,
          subTotal: item.subTotal ?? item.subtotal,
          thumbnailUrl: item.thumbnailUrl ?? item.imageUrl,
          isAvailable: item.isAvailable ?? item.available ?? true,
        }));

        setCart({
          ...rawData,
          items: normalizedItems,
          totalAmount: rawData.totalAmount ?? rawData.totalPrice ?? 0,
        });
      } else {
        setCart(null);
      }
    } catch (error) {
      console.error('Failed to fetch cart', error);
      setCart(null);
    } finally {
      setLoading(false);
    }
  }, [isAuthenticated]);

  useEffect(() => {
    fetchCart();
  }, [fetchCart]);

  const addToCart = useCallback(async (variantId: number, quantity: number) => {
    if (!isAuthenticated) {
      alert("Vui lòng đăng nhập để thêm vào giỏ hàng");
      return;
    }

    try {
      await cartApi.addToCart({ variantId, quantity });
      await fetchCart();
      alert("Đã thêm vào giỏ hàng");
    } catch (error: any) {
      console.error('Failed to add to cart', error);
      alert(error.response?.data?.message || 'Có lỗi xảy ra khi thêm vào giỏ hàng');
      throw error;
    }
  }, [isAuthenticated, fetchCart]);

  const cartItemCount = useMemo(() => cart?.items?.reduce((sum, item) => sum + item.quantity, 0) || 0, [cart]);

  const contextValue = useMemo(() => ({
    cart,
    cartItemCount,
    loading,
    fetchCart,
    addToCart,
  }), [cart, cartItemCount, loading, fetchCart, addToCart]);

  return (
    <CartContext.Provider value={contextValue}>
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
