export interface ReviewCreateRequest {
  productId: number;
  orderId: number;
  rating: number; // 1 to 5
  comment: string;
  imageUrls?: string[];
}

export interface ReviewUpdateRequest {
  rating: number; // 1 to 5
  comment: string;
  imageUrls?: string[];
}

export interface VendorReplyRequest {
  replyComment: string;
}

export interface ReviewResponse {
  id: number;
  userId: number;
  userFullName: string;
  productId: number;
  orderId: number;
  rating: number;
  comment: string;
  imageUrls: string[];
  vendorReply: string | null;
  vendorRepliedAt: string | null;
  createdAt: string;
}

export interface RatingSummaryResponse {
  averageRating: number;
  totalReviews: number;
  ratingCounts: Record<number, number>; // { 5: 12, 4: 3, 3: 1, 2: 0, 1: 0 }
}
