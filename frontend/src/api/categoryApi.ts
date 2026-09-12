import apiClient from './apiClient';
import type { ApiResponse } from './types/auth.types';

export interface CategoryResponse {
  id: number;
  name: string;
  description: string;
}

export const categoryApi = {
  getAllCategories: () => {
    return apiClient.get<ApiResponse<CategoryResponse[]>>('/api/v1/categories');
  }
};
