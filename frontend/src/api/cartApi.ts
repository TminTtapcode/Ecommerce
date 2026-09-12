import apiClient from './apiClient';
import type { ApiResponse } from './types/auth.types';
import type { CartResponse, CartItemRequest, CartItemUpdateRequest } from './types/cart.types';

export const cartApi = {
  getCart: () => apiClient.get<ApiResponse<CartResponse>>('/api/v1/carts'),
  addToCart: (data: CartItemRequest) => {
    const variantId = data.productVariantId ?? data.variantId;
    const payload = {
      productVariantId: variantId,
      quantity: data.quantity,
    };
    return apiClient.post<ApiResponse<void>>('/api/v1/carts/items', payload);
  },
  updateQuantity: (itemId: number, data: CartItemUpdateRequest) => apiClient.put<ApiResponse<void>>(`/api/v1/carts/items/${itemId}`, data),
  removeCartItem: (itemId: number) => apiClient.delete<ApiResponse<void>>(`/api/v1/carts/items/${itemId}`),
  clearCart: () => apiClient.delete<ApiResponse<void>>('/api/v1/carts')
};
