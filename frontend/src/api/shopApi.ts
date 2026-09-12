import apiClient from './apiClient';
import type { ApiResponse } from './types/auth.types';
import type { ShopResponse, ShopCreateRequest } from './types/shop.types';

export const shopApi = {
  getMyShop: () => {
    return apiClient.get<ApiResponse<ShopResponse>>('/api/v1/shops/my-shop');
  },
  registerShop: (data: ShopCreateRequest) => {
    return apiClient.post<ApiResponse<void>>('/api/v1/shops', data);
  },
  getShopPublicInfo: (id: number) => {
    return apiClient.get<ApiResponse<ShopResponse>>(`/api/v1/shops/${id}`);
  }
};
