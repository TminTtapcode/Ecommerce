import apiClient from './apiClient';
import type { ApiResponse } from './types/common.types';

export interface VendorAnalyticsDailyPoint {
  date: string;
  createdOrderCount: number;
  fulfilledOrderValue: number;
}

export interface VendorAnalytics {
  dateBasis: 'ORDER_CREATED_AT';
  startDate: string | null;
  endDate: string | null;
  totalOrders: number;
  ordersByStatus: Record<string, number>;
  deliveredOrderCount: number;
  fulfilledGrossOrderValue: number;
  voucherDiscountAmount: number;
  fulfilledOrderValue: number;
  averageFulfilledOrderValue: number;
  dailyChart: VendorAnalyticsDailyPoint[];
}

export const vendorAnalyticsApi = {
  get: (startDate?: string, endDate?: string) => apiClient.get<ApiResponse<VendorAnalytics>>(
    '/api/v1/vendor/analytics', { params: { startDate, endDate } }
  ),
};
