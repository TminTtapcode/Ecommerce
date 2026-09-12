import apiClient from './apiClient';
import type { ApiResponse, PageResponse } from './types/common.types';
import type {
  ReviewCreateRequest,
  ReviewUpdateRequest,
  VendorReplyRequest,
  ReviewResponse,
  RatingSummaryResponse
} from './types/review.types';

export const reviewApi = {
  createReview: async (data: ReviewCreateRequest): Promise<ApiResponse<ReviewResponse>> => {
    const response = await apiClient.post<ApiResponse<ReviewResponse>>('/api/v1/reviews', data);
    return response.data;
  },

  updateReview: async (reviewId: number, data: ReviewUpdateRequest): Promise<ApiResponse<ReviewResponse>> => {
    const response = await apiClient.put<ApiResponse<ReviewResponse>>(`/api/v1/reviews/${reviewId}`, data);
    return response.data;
  },

  deleteReview: async (reviewId: number): Promise<ApiResponse<void>> => {
    const response = await apiClient.delete<ApiResponse<void>>(`/api/v1/reviews/${reviewId}`);
    return response.data;
  },

  getProductReviews: async (productId: number, params?: { page?: number; size?: number }): Promise<ApiResponse<PageResponse<ReviewResponse>>> => {
    const response = await apiClient.get<ApiResponse<PageResponse<ReviewResponse>>>(`/api/v1/products/${productId}/reviews`, { params });
    return response.data;
  },

  getRatingSummary: async (productId: number): Promise<ApiResponse<RatingSummaryResponse>> => {
    const response = await apiClient.get<ApiResponse<RatingSummaryResponse>>(`/api/v1/products/${productId}/reviews/summary`);
    return response.data;
  },

  getMyReviews: async (productId: number): Promise<ApiResponse<ReviewResponse[]>> => {
    const response = await apiClient.get<ApiResponse<ReviewResponse[]>>(`/api/v1/products/${productId}/reviews/my-reviews`);
    return response.data;
  },

  replyReview: async (reviewId: number, data: VendorReplyRequest): Promise<ApiResponse<ReviewResponse>> => {
    const payload = {
      replyComment: data.replyComment || data.reply || ''
    };
    const response = await apiClient.post<ApiResponse<ReviewResponse>>(`/api/v1/vendor/reviews/${reviewId}/reply`, payload);
    return response.data;
  },

  checkReviewEligibility: async (productId: number): Promise<ApiResponse<number[]>> => {
    const response = await apiClient.get<ApiResponse<number[]>>(`/api/v1/products/${productId}/reviews/eligibility`);
    return response.data;
  }
};
