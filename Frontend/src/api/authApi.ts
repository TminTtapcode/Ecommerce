import apiClient from './apiClient';
import type { UserLoginRequest, UserRegisterRequest, AuthResponse, ApiResponse } from './types/auth.types';

export const authApi = {
  login: async (data: UserLoginRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/v1/auth/login', data);
    return response.data;
  },

  register: async (data: UserRegisterRequest): Promise<ApiResponse<AuthResponse>> => {
    const response = await apiClient.post<ApiResponse<AuthResponse>>('/api/v1/auth/register', data);
    return response.data;
  },
};
