import apiClient from './apiClient';
import type { ApiResponse } from './types/common.types';

export interface Profile {
  id: number;
  email: string;
  fullName: string;
  phone: string | null;
}

export interface ProfileUpdate {
  fullName: string;
  phone: string | null;
}

export const userApi = {
  getProfile: () => apiClient.get<ApiResponse<Profile>>('/api/v1/users/me'),
  updateProfile: (body: ProfileUpdate) => apiClient.put<ApiResponse<Profile>>('/api/v1/users/me', body),
};
