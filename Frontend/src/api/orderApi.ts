import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { CheckoutRequest, CheckoutResponse, OrderResponse, VendorOrderStatusUpdateRequest } from './types/order.types';

export const orderApi = {
    checkout: async (data: CheckoutRequest): Promise<ApiResponse<CheckoutResponse>> => {
        const response = await apiClient.post<ApiResponse<CheckoutResponse>>('/api/v1/orders/checkout', data);
        return response.data;
    },

    previewCheckout: async (data: import('./types/order.types').CartPreviewRequest): Promise<ApiResponse<import('./types/order.types').CartPreviewResponse>> => {
        const response = await apiClient.post<ApiResponse<import('./types/order.types').CartPreviewResponse>>('/api/v1/orders/preview', data);
        return response.data;
    },

    getMyOrders: async (params?: { page?: number; size?: number; status?: string }): Promise<ApiResponse<PageResponse<OrderResponse>>> => {
        const response = await apiClient.get<ApiResponse<PageResponse<OrderResponse>>>('/api/v1/orders', { params });
        return response.data;
    },

    getOrderDetail: async (id: number): Promise<ApiResponse<OrderResponse>> => {
        const response = await apiClient.get<ApiResponse<OrderResponse>>(`/api/v1/orders/${id}`);
        return response.data;
    },

    // === VENDOR ORDER MANAGEMENT ===

    getVendorOrders: async (params?: { page?: number; size?: number; status?: string }): Promise<ApiResponse<PageResponse<OrderResponse>>> => {
        const response = await apiClient.get<ApiResponse<PageResponse<OrderResponse>>>('/api/v1/vendor/orders', { params });
        return response.data;
    },

    getVendorOrderDetail: async (id: number): Promise<ApiResponse<OrderResponse>> => {
        const response = await apiClient.get<ApiResponse<OrderResponse>>(`/api/v1/vendor/orders/${id}`);
        return response.data;
    },

    updateVendorOrderStatus: async (id: number, data: VendorOrderStatusUpdateRequest): Promise<ApiResponse<OrderResponse>> => {
        const response = await apiClient.patch<ApiResponse<OrderResponse>>(`/api/v1/vendor/orders/${id}/status`, data);
        return response.data;
    }
};
