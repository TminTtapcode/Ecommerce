export interface ReviewCreateRequest {
  productId: number;
  orderId: number;
  rating: number;
  comment: string;
  variantId?: number;
  images?: string[];
  imageUrls?: string[];
}

export interface ReviewUpdateRequest {
  rating: number;
  comment: string;
  images?: string[];
  imageUrls?: string[];
}

export interface VendorReplyRequest {
  replyComment: string;
  reply?: string;
}

export interface ReviewResponse {
  id: number;
  productId: number;
  userId: number;
  userName: string;
  userAvatar?: string;
  orderId: number;
  rating: number;
  comment: string;
  images: string[];
  reply: string | null;
  repliedAt: string | null;
  createdAt: string;

  userFullName?: string;
  imageUrls?: string[];
  vendorReply?: string | null;
  vendorRepliedAt?: string | null;
}

export interface RatingSummaryResponse {
  averageRating: number;
  totalReviews: number;
  ratingCounts: Record<number, number>;
}
