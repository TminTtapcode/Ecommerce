import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { 
  PayoutResponse, 
  PayoutBalanceResponse, 
  CreatePayoutRequest, 
  ApprovePayoutRequest, 
  RejectPayoutRequest,
  PayoutStatus 
} from './types/payout.types';

export const payoutApi = {
  getShopBalance: async (): Promise<ApiResponse<PayoutBalanceResponse>> => {
    const res = await apiClient.get<ApiResponse<PayoutBalanceResponse>>('/api/v1/vendor/payouts/balance');
    return res.data;
  },

  createPayout: async (data: CreatePayoutRequest): Promise<ApiResponse<PayoutResponse>> => {
    const res = await apiClient.post<ApiResponse<PayoutResponse>>('/api/v1/vendor/payouts', data);
    return res.data;
  },

  getMyPayouts: async (page = 0, size = 15): Promise<ApiResponse<PageResponse<PayoutResponse>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<PayoutResponse>>>('/api/v1/vendor/payouts', {
      params: { page, size }
    });
    return res.data;
  },

  getAdminPayouts: async (status?: PayoutStatus, page = 0, size = 15): Promise<ApiResponse<PageResponse<PayoutResponse>>> => {
    const res = await apiClient.get<ApiResponse<PageResponse<PayoutResponse>>>('/api/v1/admin/payouts', {
      params: { status: status || undefined, page, size }
    });
    return res.data;
  },

  approvePayout: async (id: number, data?: ApprovePayoutRequest): Promise<ApiResponse<PayoutResponse>> => {
    const res = await apiClient.put<ApiResponse<PayoutResponse>>(`/api/v1/admin/payouts/${id}/approve`, data || {});
    return res.data;
  },

  rejectPayout: async (id: number, data: RejectPayoutRequest): Promise<ApiResponse<PayoutResponse>> => {
    const res = await apiClient.put<ApiResponse<PayoutResponse>>(`/api/v1/admin/payouts/${id}/reject`, data);
    return res.data;
  }
};
