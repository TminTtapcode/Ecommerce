import React, { useEffect, useState } from 'react';
import { reviewApi } from '../../api/reviewApi';
import type { ReviewResponse, RatingSummaryResponse } from '../../api/types/review.types';
import { useAuth } from '../../contexts/AuthContext';
import { CreateReviewModal } from './CreateReviewModal';

interface ProductReviewSectionProps {
  productId: number;
}

export const ProductReviewSection: React.FC<ProductReviewSectionProps> = ({ productId }) => {
  const [reviews, setReviews] = useState<ReviewResponse[]>([]);
  const [summary, setSummary] = useState<RatingSummaryResponse | null>(null);
  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const { user } = useAuth();
  
  // State for vendor reply
  const [replyingTo, setReplyingTo] = useState<number | null>(null);
  const [replyComment, setReplyComment] = useState('');
  const [isSubmittingReply, setIsSubmittingReply] = useState(false);

  // State for Review features
  const [eligibleOrderIds, setEligibleOrderIds] = useState<number[]>([]);
  const [isModalOpen, setIsModalOpen] = useState(false);
  const [editingReview, setEditingReview] = useState<ReviewResponse | null>(null);
  const [currentOrderId, setCurrentOrderId] = useState<number>(0);

  const fetchReviewsAndSummary = async () => {
    setIsLoading(true);
    try {
      const [reviewsRes, summaryRes] = await Promise.all([
        reviewApi.getProductReviews(productId, { page, size: 5 }),
        reviewApi.getRatingSummary(productId),
      ]);
      setReviews(reviewsRes.data.content);
      setTotalPages(reviewsRes.data.totalPages);
      setSummary(summaryRes.data);
    } catch (error) {
      console.error('Error fetching reviews:', error);
    } finally {
      setIsLoading(false);
    }
  };

  const checkEligibility = async () => {
    if (user && !user.roles.includes('ROLE_VENDOR') && !user.roles.includes('ROLE_ADMIN')) {
      try {
        const res = await reviewApi.checkReviewEligibility(productId);
        setEligibleOrderIds(res.data);
      } catch (error) {
        console.error('Error checking review eligibility', error);
      }
    }
  };

  useEffect(() => {
    fetchReviewsAndSummary();
    checkEligibility();
  }, [productId, page, user]);

  useEffect(() => {
    // Check if we need to open modal automatically from URL params
    const params = new URLSearchParams(window.location.search);
    const action = params.get('action');
    const orderIdStr = params.get('orderId');
    if (action === 'review' && orderIdStr && !isModalOpen) {
      setCurrentOrderId(parseInt(orderIdStr, 10));
      setEditingReview(null);
      setIsModalOpen(true);
      // Clean up URL to avoid reopening on refresh
      window.history.replaceState({}, '', window.location.pathname);
    }
  }, []);

  const handleReplySubmit = async (reviewId: number) => {
    if (!replyComment.trim()) return;
    
    setIsSubmittingReply(true);
    try {
      const response = await reviewApi.replyReview(reviewId, { replyComment });
      setReviews(reviews.map(r => r.id === reviewId ? response.data : r));
      setReplyingTo(null);
      setReplyComment('');
    } catch (error) {
      console.error('Error replying to review:', error);
      alert('Không thể phản hồi đánh giá. Bạn có chắc là chủ shop của sản phẩm này?');
    } finally {
      setIsSubmittingReply(false);
    }
  };

  const handleDeleteReview = async (reviewId: number) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa đánh giá này?')) {
      try {
        await reviewApi.deleteReview(reviewId);
        fetchReviewsAndSummary();
        checkEligibility();
      } catch (error) {
        console.error('Error deleting review', error);
        alert('Xóa đánh giá thất bại.');
      }
    }
  };

  const handleEditClick = (review: ReviewResponse) => {
    setEditingReview(review);
    setCurrentOrderId(review.orderId);
    setIsModalOpen(true);
  };

  const handleWriteReviewClick = () => {
    if (eligibleOrderIds.length > 0) {
      setCurrentOrderId(eligibleOrderIds[0]);
      setEditingReview(null);
      setIsModalOpen(true);
    }
  };

  const isVendor = user?.roles.includes('ROLE_VENDOR') || user?.roles.includes('ROLE_ADMIN');

  if (isLoading && !summary) {
    return <div className="py-8 text-center text-gray-500">Đang tải đánh giá...</div>;
  }

  return (
    <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 mt-8" id="reviews">
      <div className="flex justify-between items-center mb-6">
        <h2 className="text-2xl font-bold text-gray-800">Đánh giá sản phẩm</h2>
        {eligibleOrderIds.length > 0 && (
          <button
            onClick={handleWriteReviewClick}
            className="bg-blue-600 hover:bg-blue-700 text-white px-4 py-2 rounded-md font-medium shadow-sm transition-colors"
          >
            Viết đánh giá
          </button>
        )}
      </div>
      
      {summary && summary.totalReviews > 0 ? (
        <>
          <div className="flex flex-col md:flex-row gap-8 mb-8 pb-8 border-b border-gray-200">
            {/* Rating Summary */}
            <div className="flex flex-col items-center justify-center md:w-1/3">
              <div className="text-5xl font-bold text-gray-800 mb-2">
                {summary.averageRating.toFixed(1)}
              </div>
              <div className="flex text-yellow-400 mb-2">
                {[1, 2, 3, 4, 5].map((star) => (
                  <svg key={star} className={`w-6 h-6 ${summary.averageRating >= star ? 'text-yellow-400' : 'text-gray-300'}`} fill="currentColor" viewBox="0 0 20 20">
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                ))}
              </div>
              <div className="text-sm text-gray-500">
                {summary.totalReviews} đánh giá
              </div>
            </div>
            
            {/* Rating Breakdown */}
            <div className="flex-1">
              {[5, 4, 3, 2, 1].map((star) => {
                const count = summary.ratingCounts[star] || 0;
                const percentage = summary.totalReviews > 0 ? (count / summary.totalReviews) * 100 : 0;
                return (
                  <div key={star} className="flex items-center mt-2">
                    <span className="text-sm font-medium text-gray-600 w-8">{star} sao</span>
                    <div className="w-full h-2 bg-gray-200 rounded-full mx-3 overflow-hidden">
                      <div className="h-full bg-yellow-400 rounded-full" style={{ width: `${percentage}%` }}></div>
                    </div>
                    <span className="text-sm text-gray-500 w-8 text-right">{count}</span>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Review List */}
          <div className="space-y-6">
            {reviews.map((review) => (
              <div key={review.id} className="border-b border-gray-100 pb-6 last:border-0 relative">
                <div className="flex items-start justify-between">
                  <div>
                    <div className="font-semibold text-gray-800">{review.userFullName}</div>
                    <div className="flex items-center mt-1 mb-2">
                      <div className="flex text-yellow-400">
                        {[1, 2, 3, 4, 5].map((star) => (
                          <svg key={star} className={`w-4 h-4 ${review.rating >= star ? 'text-yellow-400' : 'text-gray-300'}`} fill="currentColor" viewBox="0 0 20 20">
                            <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                          </svg>
                        ))}
                      </div>
                      <span className="text-xs text-gray-400 ml-2">
                        {new Date(review.createdAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                  </div>
                  
                  {/* Actions for review owner */}
                  {user && user.userId === review.userId && (
                    <div className="flex space-x-3">
                      <button onClick={() => handleEditClick(review)} className="text-sm text-blue-500 hover:text-blue-700 font-medium">Sửa</button>
                      <button onClick={() => handleDeleteReview(review.id)} className="text-sm text-red-500 hover:text-red-700 font-medium">Xóa</button>
                    </div>
                  )}
                </div>
                
                <p className="text-gray-700 mt-2 whitespace-pre-line">{review.comment}</p>
                
                {/* Images */}
                {review.imageUrls && review.imageUrls.length > 0 && (
                  <div className="mt-3 flex flex-wrap gap-2">
                    {review.imageUrls.map((url, idx) => (
                      <a key={idx} href={url} target="_blank" rel="noopener noreferrer" className="block">
                        <img src={url} alt={`Review img ${idx}`} className="w-20 h-20 object-cover rounded-md border border-gray-200 hover:opacity-80 transition-opacity" />
                      </a>
                    ))}
                  </div>
                )}
                
                {/* Vendor Reply Display */}
                {review.vendorReply && (
                  <div className="mt-4 bg-gray-50 p-4 rounded-md border-l-4 border-blue-500 ml-4">
                    <div className="flex justify-between">
                      <span className="font-semibold text-sm text-gray-800">Phản hồi từ Người bán</span>
                      <span className="text-xs text-gray-500">
                        {review.vendorRepliedAt && new Date(review.vendorRepliedAt).toLocaleDateString('vi-VN')}
                      </span>
                    </div>
                    <p className="text-sm text-gray-600 mt-1 whitespace-pre-line">{review.vendorReply}</p>
                  </div>
                )}
                
                {/* Vendor Reply Action */}
                {isVendor && !review.vendorReply && replyingTo !== review.id && (
                  <button 
                    onClick={() => setReplyingTo(review.id)}
                    className="mt-3 text-sm text-blue-600 hover:text-blue-800 font-medium"
                  >
                    Phản hồi đánh giá này
                  </button>
                )}
                
                {/* Vendor Reply Input */}
                {replyingTo === review.id && (
                  <div className="mt-4 ml-4">
                    <textarea
                      className="w-full border rounded p-2 text-sm focus:ring-1 focus:ring-blue-500 focus:border-blue-500"
                      rows={3}
                      placeholder="Nhập phản hồi của shop..."
                      value={replyComment}
                      onChange={(e) => setReplyComment(e.target.value)}
                    ></textarea>
                    <div className="flex justify-end space-x-2 mt-2">
                      <button 
                        onClick={() => { setReplyingTo(null); setReplyComment(''); }}
                        className="px-3 py-1 text-sm border rounded text-gray-600 hover:bg-gray-50"
                      >
                        Hủy
                      </button>
                      <button 
                        onClick={() => handleReplySubmit(review.id)}
                        disabled={isSubmittingReply || !replyComment.trim()}
                        className="px-3 py-1 text-sm bg-blue-600 text-white rounded hover:bg-blue-700 disabled:opacity-50"
                      >
                        {isSubmittingReply ? 'Đang gửi...' : 'Gửi phản hồi'}
                      </button>
                    </div>
                  </div>
                )}
              </div>
            ))}
          </div>
          
          {/* Pagination */}
          {totalPages > 1 && (
            <div className="mt-8 flex justify-center space-x-2">
              <button
                disabled={page === 0}
                onClick={() => setPage(page - 1)}
                className="px-3 py-1 border rounded text-gray-600 disabled:opacity-50 hover:bg-gray-50"
              >
                Trước
              </button>
              <span className="px-3 py-1 text-gray-600">
                Trang {page + 1} / {totalPages}
              </span>
              <button
                disabled={page >= totalPages - 1}
                onClick={() => setPage(page + 1)}
                className="px-3 py-1 border rounded text-gray-600 disabled:opacity-50 hover:bg-gray-50"
              >
                Sau
              </button>
            </div>
          )}
        </>
      ) : (
        <div className="text-center py-10 bg-gray-50 rounded-lg border border-dashed border-gray-300">
          <svg className="mx-auto h-12 w-12 text-gray-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1} d="M8 12h.01M12 12h.01M16 12h.01M21 12c0 4.418-4.03 8-9 8a9.863 9.863 0 01-4.255-.949L3 20l1.395-3.72C3.512 15.042 3 13.574 3 12c0-4.418 4.03-8 9-8s9 3.582 9 8z" />
          </svg>
          <h3 className="mt-2 text-sm font-medium text-gray-900">Chưa có đánh giá nào</h3>
          <p className="mt-1 text-sm text-gray-500">Hãy là người đầu tiên đánh giá sản phẩm này sau khi mua hàng!</p>
        </div>
      )}

      {/* Review Modal */}
      <CreateReviewModal
        productId={productId}
        orderId={currentOrderId}
        isOpen={isModalOpen}
        onClose={() => setIsModalOpen(false)}
        initialData={editingReview}
        onSuccess={() => {
          fetchReviewsAndSummary();
          checkEligibility();
        }}
      />
    </div>
  );
};
