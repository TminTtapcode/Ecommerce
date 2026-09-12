import apiClient from './apiClient';
import type { ApiResponse } from './types/common.types';

export interface PaymentCreateRequest {
    paymentGroupId: string;
}

export interface PaymentCreateResponse {
    paymentUrl: string;
}

export interface PaymentGroupStatusResponse {
    paymentGroupId: string;
    status: string;
}

export const paymentApi = {
    createPaymentUrl: async (data: PaymentCreateRequest): Promise<ApiResponse<PaymentCreateResponse>> => {
        const response = await apiClient.post<ApiResponse<PaymentCreateResponse>>('/api/v1/payments/create', data);
        return response.data;
    },

    getPaymentStatus: async (paymentGroupId: string): Promise<ApiResponse<PaymentGroupStatusResponse>> => {
        const response = await apiClient.get<ApiResponse<PaymentGroupStatusResponse>>(`/api/v1/payments/group/${paymentGroupId}/status`);
        return response.data;
    },

    syncVnpayIpn: async (queryString: string): Promise<any> => {
        const response = await apiClient.get(`/api/v1/payments/vnpay/ipn${queryString}`);
        return response.data;
    }
};
