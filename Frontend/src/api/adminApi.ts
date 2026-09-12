import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type { ShopResponse } from './types/shop.types';
import type { OrderResponse } from './types/order.types';
import type { VoucherResponse, VoucherUpdateRequest } from './types/voucher.types';

export const adminApi = {

  getShops: (status?: string, page = 0, size = 10) => {
    return apiClient.get<ApiResponse<PageResponse<ShopResponse>>>('/api/v1/admin/shops', {
      params: { status, page, size }
    });
  },
  approveShop: (shopId: number) => {
    return apiClient.put<ApiResponse<void>>(`/api/v1/admin/shops/${shopId}/approve`);
  },
  banShop: (shopId: number, reason?: string) => {
    return apiClient.put<ApiResponse<void>>(`/api/v1/admin/shops/${shopId}/ban`, { reason });
  },
  unbanShop: (shopId: number) => {
    return apiClient.put<ApiResponse<void>>(`/api/v1/admin/shops/${shopId}/unban`);
  },

  getOrderActions: (id: number) => apiClient.get<ApiResponse<string[]>>(`/api/v1/admin/orders/${id}/allowed-transitions`),
  updateOrderStatus: (orderId: number, data: { expectedStatus: string; status: string; reason: string }) => {
    return apiClient.patch<ApiResponse<OrderResponse>>(`/api/v1/admin/orders/${orderId}/status`, data);
  },
  getOrderHistory: (orderId: number, page = 0, size = 10) => {
    return apiClient.get<ApiResponse<PageResponse<AdminOrderHistory>>>(`/api/v1/admin/orders/${orderId}/status-history`, {
      params: { page, size }
    });
  },
  interveneOrder: (id: number, payload: { targetStatus: string; reason: string }) =>
    apiClient.patch<ApiResponse<OrderResponse>>(`/api/v1/admin/orders/${id}/intervene-status`, payload),
  getOrders: (status?: string, page = 0, size = 10) => {
    return apiClient.get<ApiResponse<PageResponse<OrderResponse>>>('/api/v1/admin/orders', {
      params: { status, page, size }
    });
  },
  getOrderDetail: (orderId: number) => {
    return apiClient.get<ApiResponse<OrderResponse>>(`/api/v1/admin/orders/${orderId}`);
  },

  getSystemVouchers: (page = 0, size = 10) => {
    return apiClient.get<ApiResponse<PageResponse<VoucherResponse>>>('/api/v1/admin/vouchers', {
      params: { page, size }
    });
  },
  updateSystemVoucher: (id: number, data: VoucherUpdateRequest) => {
    return apiClient.put<ApiResponse<VoucherResponse>>(`/api/v1/admin/vouchers/${id}`, data);
  },
  deleteSystemVoucher: (id: number) => {
    return apiClient.delete<ApiResponse<void>>(`/api/v1/admin/vouchers/${id}`);
  },

  createRefund: (orderId: number, reason: string) => {
    return apiClient.post<ApiResponse<RefundAttemptResponse>>(`/api/v1/admin/orders/${orderId}/refunds`, { reason }, {
      headers: { 'Idempotency-Key': `refund-${orderId}-${Date.now()}` }
    });
  },
  getRefunds: (orderId: number) => {
    return apiClient.get<ApiResponse<RefundAttemptResponse[]>>(`/api/v1/admin/orders/${orderId}/refunds`);
  },

  getDashboardStats: (startDate?: string, endDate?: string) => {
    return apiClient.get<ApiResponse<AdminDashboardStats>>('/api/v1/admin/statistics/dashboard', {
      params: { startDate, endDate }
    });
  },
};

export interface AdminOrderHistory {
  id: number;
  orderId: number;
  actorUserId: number;
  oldStatus: string;
  newStatus: string;
  reason: string;
  createdAt: string;
}

export interface RefundAttemptResponse {
  id: number;
  orderId: number;
  amount: number;
  status: string;
  transactionId: string;
  reason: string;
  createdAt: string;
  updatedAt: string;
}

export interface AdminDashboardStats {
  totalUsers: number;
  totalShops: number;
  totalProducts: number;
  totalOrders: number;
  ordersByStatus: Record<string, number>;
  deliveredRevenue: number;
  averageOrderValue: number;
  dailyChart: DailyChartPoint[];
  recentOrders: RecentOrder[];
}

export interface DailyChartPoint {
  date: string;
  orderCount: number;
  revenue: number;
}

export interface RecentOrder {
  id: number;
  userId: number;
  shopId: number;
  status: string;
  totalAmount: number;
  paymentMethod: string;
  createdAt: string;
}
