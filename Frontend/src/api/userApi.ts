import apiClient from './apiClient';
import type { SessionRequestConfig } from './apiClient';
import type { ApiResponse } from './types/common.types';

export interface Profile { id: number; email: string; fullName: string; phone: string | null }
export interface ProfileUpdate { fullName: string; phone: string | null }
export interface ProfileSession { token: string; generation: number }
const config = (session: ProfileSession): SessionRequestConfig => ({
  headers: { Authorization: `Bearer ${session.token}` }, authSessionGeneration: session.generation,
});
export const userApi = {
  getProfile: (session: ProfileSession) => apiClient.get<ApiResponse<Profile>>('/api/v1/users/me', config(session)),
  updateProfile: (body: ProfileUpdate, session: ProfileSession) => apiClient.put<ApiResponse<Profile>>('/api/v1/users/me', body, config(session)),
};
