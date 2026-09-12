import apiClient from './apiClient';
import type {
  ProductResponse,
  PageResponse,
  ProductCreateRequest,
  ProductUpdateRequest
} from './types/product.types';
import type { ApiResponse } from './types/auth.types';

export interface ProductSearchParams {
  page?: number;
  size?: number;
  keyword?: string;
  categoryId?: number;
  shopId?: number;
  minPrice?: number;
  maxPrice?: number;

  sortBy?: string;
}

export const productApi = {
  getProducts: (params: ProductSearchParams = {}) => {
    const { page = 0, size = 10, keyword, categoryId, shopId, minPrice, maxPrice, sortBy } = params;
    const qs = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (keyword)    qs.append('keyword',    keyword);
    if (categoryId != null) qs.append('categoryId', categoryId.toString());
    if (shopId != null)     qs.append('shopId',     shopId.toString());
    if (minPrice != null)   qs.append('minPrice',   minPrice.toString());
    if (maxPrice != null)   qs.append('maxPrice',   maxPrice.toString());
    if (sortBy)     qs.append('sortBy',     sortBy);
    return apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(`/api/v1/products?${qs.toString()}`);
  },

  getVendorProducts: (page: number = 0, size: number = 10, keyword?: string) => {
    const params = new URLSearchParams({
      page: page.toString(),
      size: size.toString(),
    });
    if (keyword) {
      params.append('keyword', keyword);
    }
    return apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(`/api/v1/vendor/products?${params.toString()}`);
  },

  getProductById: (id: number) => {
    return apiClient.get<ApiResponse<ProductResponse>>(`/api/v1/products/${id}`);
  },

  getRelatedProducts: (id: number, limit?: number) =>
    apiClient.get<ApiResponse<ProductResponse[]>>(`/api/v1/products/${id}/related`, {
      params: limit == null ? undefined : { limit },
    }),

  getRecommendations: (limit?: number) =>
    apiClient.get<ApiResponse<PageResponse<ProductResponse>>>(`/api/v1/products/recommendations`, {
      params: limit == null ? undefined : { limit },
    }),

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
