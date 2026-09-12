import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { VoucherResponse, VoucherCreateRequest, VoucherUpdateRequest } from './types/voucher.types';

export const voucherApi = {

  getMyShopVouchers: (page = 0, size = 10) => {
    return apiClient.get<ApiResponse<PageResponse<VoucherResponse>>>('/api/v1/vouchers/my-shop', {
      params: { page, size }
    });
  },

  createVoucher: (data: VoucherCreateRequest) => {
    return apiClient.post<ApiResponse<VoucherResponse>>('/api/v1/vouchers', data);
  },

  updateShopVoucher: (id: number, data: VoucherUpdateRequest) => {
    return apiClient.put<ApiResponse<VoucherResponse>>(`/api/v1/vouchers/${id}`, data);
  },

  deleteShopVoucher: (id: number) => {
    return apiClient.delete<ApiResponse<void>>(`/api/v1/vouchers/${id}`);
  }
};
