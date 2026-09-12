import React, { useState, useEffect } from 'react';
import { reviewApi } from '../../api/reviewApi';
import { mediaApi } from '../../api/mediaApi';
import type { ReviewResponse } from '../../api/types/review.types';

interface CreateReviewModalProps {
  productId: number;
  orderId: number;
  variantId?: number;
  productName?: string;
  isOpen: boolean;
  onClose: () => void;
  onSuccess: () => void;
  initialData?: ReviewResponse | null;
}

export const CreateReviewModal: React.FC<CreateReviewModalProps> = ({
  productId,
  orderId,
  variantId,
  productName,
  isOpen,
  onClose,
  onSuccess,
  initialData,
}) => {
  const [rating, setRating] = useState<number>(0);
  const [hoverRating, setHoverRating] = useState<number>(0);
  const [comment, setComment] = useState('');
  const [imageUrls, setImageUrls] = useState<string[]>([]);
  const [imageFiles, setImageFiles] = useState<File[]>([]);
  const [imagePreviews, setImagePreviews] = useState<string[]>([]);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [error, setError] = useState<string | null>(null);

  useEffect(() => {
    if (isOpen) {
      if (initialData) {
        setRating(initialData.rating);
        setComment(initialData.comment);
        setImageUrls(initialData.imageUrls || []);
      } else {
        setRating(5);
        setComment('');
        setImageUrls([]);
      }
      setImageFiles([]);
      setImagePreviews([]);
      setError(null);
    }
  }, [isOpen, initialData]);

  const handleImageChange = (e: React.ChangeEvent<HTMLInputElement>) => {
    if (e.target.files) {
      const filesArray = Array.from(e.target.files);
      setImageFiles((prev) => [...prev, ...filesArray]);

      const newPreviews = filesArray.map(file => URL.createObjectURL(file));
      setImagePreviews((prev) => [...prev, ...newPreviews]);
    }
  };

  const removeImagePreview = (index: number) => {
    setImageFiles((prev) => prev.filter((_, i) => i !== index));
    setImagePreviews((prev) => {
      const newPreviews = [...prev];
      URL.revokeObjectURL(newPreviews[index]);
      newPreviews.splice(index, 1);
      return newPreviews;
    });
  };

  const removeExistingImage = (index: number) => {
    setImageUrls((prev) => prev.filter((_, i) => i !== index));
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (rating === 0) {
      setError('Vui lòng chọn số sao đánh giá');
      return;
    }
    if (!comment.trim()) {
      setError('Vui lòng nhập nội dung đánh giá');
      return;
    }

    setIsSubmitting(true);
    setError(null);
    try {

      const newUploadedUrls: string[] = [];
      for (const file of imageFiles) {
        const uploadRes = await mediaApi.uploadImage(file);
        newUploadedUrls.push(uploadRes.data.url);
      }

      const finalImageUrls = [...imageUrls, ...newUploadedUrls];

      if (initialData) {

        await reviewApi.updateReview(initialData.id, {
          rating,
          comment,
          imageUrls: finalImageUrls,
        });
      } else {

        await reviewApi.createReview({
          productId,
          orderId,
          variantId,
          rating,
          comment,
          imageUrls: finalImageUrls,
        });
      }

      onSuccess();
      onClose();
    } catch (err: any) {
      setError(err.response?.data?.message || 'Có lỗi xảy ra khi lưu đánh giá');
    } finally {
      setIsSubmitting(false);
    }
  };

  if (!isOpen) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/60 backdrop-blur-xs p-4 animate-in fade-in duration-200">
      <div className="bg-white rounded-2xl shadow-2xl w-full max-w-lg p-6 max-h-[90vh] overflow-y-auto border border-gray-100">
        <div className="flex justify-between items-start mb-4">
          <div>
            <h2 className="text-xl font-bold text-gray-900">{initialData ? 'Sửa đánh giá' : 'Đánh giá sản phẩm'}</h2>
            {productName && (
              <p className="text-sm text-gray-500 font-medium truncate max-w-sm mt-0.5">{productName}</p>
            )}
          </div>
          <button onClick={onClose} className="p-1 rounded-full text-gray-400 hover:text-gray-600 hover:bg-gray-100 transition-colors">
            <svg className="w-6 h-6" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
            </svg>
          </button>
        </div>

        {error && (
          <div className="mb-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleSubmit}>
          <div className="mb-5 flex flex-col items-center py-2 bg-gray-50/60 rounded-xl border border-gray-100">
            <label className="block text-gray-700 text-xs font-bold uppercase tracking-wider mb-2">Chất lượng sản phẩm</label>
            <div className="flex space-x-2">
              {[1, 2, 3, 4, 5].map((star) => (
                <button
                  type="button"
                  key={star}
                  onClick={() => setRating(star)}
                  onMouseEnter={() => setHoverRating(star)}
                  onMouseLeave={() => setHoverRating(0)}
                  className="focus:outline-none transition-transform hover:scale-110 active:scale-95 duration-150 cursor-pointer"
                >
                  <svg
                    className={`w-9 h-9 ${(hoverRating || rating) >= star ? 'text-amber-400' : 'text-gray-300'}`}
                    fill="currentColor"
                    viewBox="0 0 20 20"
                  >
                    <path d="M9.049 2.927c.3-.921 1.603-.921 1.902 0l1.07 3.292a1 1 0 00.95.69h3.462c.969 0 1.371 1.24.588 1.81l-2.8 2.034a1 1 0 00-.364 1.118l1.07 3.292c.3.921-.755 1.688-1.54 1.118l-2.8-2.034a1 1 0 00-1.175 0l-2.8 2.034c-.784.57-1.838-.197-1.539-1.118l1.07-3.292a1 1 0 00-.364-1.118L2.98 8.72c-.783-.57-.38-1.81.588-1.81h3.461a1 1 0 00.951-.69l1.07-3.292z" />
                  </svg>
                </button>
              ))}
            </div>
            <div className="text-xs font-semibold text-orange-600 mt-2">
              {rating === 1 && 'Tệ'}
              {rating === 2 && 'Không hài lòng'}
              {rating === 3 && 'Bình thường'}
              {rating === 4 && 'Hài lòng'}
              {rating === 5 && 'Tuyệt vời'}
            </div>
          </div>

          <div className="mb-4">
            <label className="block text-gray-700 text-sm font-semibold mb-1.5">Nhận xét của bạn</label>
            <textarea
              className="border border-gray-300 rounded-xl w-full py-2.5 px-3 text-gray-700 leading-relaxed focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500 text-sm"
              rows={4}
              placeholder="Hãy chia sẻ những điều bạn thích về sản phẩm này nhé"
              value={comment}
              onChange={(e) => setComment(e.target.value)}
              maxLength={1000}
            ></textarea>
            <div className="text-right text-xs text-gray-400 mt-1">
              {comment.length}/1000
            </div>
          </div>

          <div className="mb-6">
            <label className="block text-gray-700 text-sm font-semibold mb-1.5">Thêm hình ảnh (Tuỳ chọn)</label>
            <div className="flex flex-wrap gap-2.5 mb-2">
              {imageUrls.map((url, index) => (
                <div key={`existing-${index}`} className="relative group">
                  <img src={url} alt="Review" className="w-16 h-16 object-cover rounded-xl border border-gray-200" />
                  <button
                    type="button"
                    onClick={() => removeExistingImage(index)}
                    className="absolute -top-1.5 -right-1.5 bg-red-500 text-white rounded-full p-0.5 hover:bg-red-600 shadow-sm transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}

              {imagePreviews.map((preview, index) => (
                <div key={`new-${index}`} className="relative group">
                  <img src={preview} alt="Preview" className="w-16 h-16 object-cover rounded-xl border border-gray-200" />
                  <button
                    type="button"
                    onClick={() => removeImagePreview(index)}
                    className="absolute -top-1.5 -right-1.5 bg-red-500 text-white rounded-full p-0.5 hover:bg-red-600 shadow-sm transition-colors"
                  >
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                    </svg>
                  </button>
                </div>
              ))}

              <label className="w-16 h-16 flex flex-col items-center justify-center border-2 border-dashed border-gray-300 rounded-xl cursor-pointer hover:border-orange-500 hover:bg-orange-50/30 transition-colors">
                <input type="file" multiple accept="image/*" className="hidden" onChange={handleImageChange} />
                <svg className="w-6 h-6 text-gray-400" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 6v6m0 0v6m0-6h6m-6 0H6" />
                </svg>
              </label>
            </div>
          </div>

          <div className="flex justify-end gap-3 pt-2 border-t border-gray-100">
            <button
              type="button"
              onClick={onClose}
              className="bg-white border border-gray-300 text-gray-700 px-5 py-2.5 rounded-xl font-medium text-sm hover:bg-gray-50 focus:outline-none transition-colors cursor-pointer"
              disabled={isSubmitting}
            >
              Trở lại
            </button>
            <button
              type="submit"
              className="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white font-semibold py-2.5 px-6 rounded-xl shadow-sm focus:outline-none focus:ring-2 focus:ring-orange-500 disabled:opacity-50 flex items-center gap-2 text-sm transition-all cursor-pointer"
              disabled={isSubmitting}
            >
              {isSubmitting ? (
                <>
                  <svg className="animate-spin h-4 w-4 text-white" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                  </svg>
                  <span>Đang xử lý...</span>
                </>
              ) : (
                'Hoàn thành'
              )}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
