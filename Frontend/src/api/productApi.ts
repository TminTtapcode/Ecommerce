import apiClient from './apiClient';
import type { 
  ProductResponse, 
  PageResponse, 
  ProductCreateRequest, 
  ProductUpdateRequest 
} from './types/product.types';
import type { ApiResponse } from './types/auth.types'; // Reuse ApiResponse structure

export const productApi = {
  getProducts: (page: number = 0, size: number = 10, keyword?: string) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (keyword) {
      params.append('keyword', keyword);
    }
    return apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(`/api/v1/products?${params.toString()}`);
  },

  getProductById: (id: number) => {
    return apiClient.get<ApiResponse<ProductResponse>>(`/api/v1/products/${id}`);
  },

  createProduct: (data: ProductCreateRequest) => {
    return apiClient.post<ApiResponse<ProductResponse>>('/api/v1/products', data);
  },

  updateProduct: (id: number, data: ProductUpdateRequest) => {
    return apiClient.put<ApiResponse<ProductResponse>>(`/api/v1/products/${id}`, data);
  },

  deleteProduct: (id: number) => {
    return apiClient.delete<ApiResponse<void>>(`/api/v1/products/${id}`);
  }
};
