import React, { useState, useEffect } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import { reviewApi } from '../../api/reviewApi';
import { CreateReviewModal } from '../../components/reviews/CreateReviewModal';
import type { OrderResponse } from '../../api/types/order.types';
import type { ReviewResponse } from '../../api/types/review.types';

export const OrderDetailPage: React.FC = () => {
    const { id } = useParams<{ id: string }>();
    const navigate = useNavigate();
    const [order, setOrder] = useState<OrderResponse | null>(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [confirmingDelivery, setConfirmingDelivery] = useState(false);
    const [cancellingOrder, setCancellingOrder] = useState(false);
    const [reviewModalState, setReviewModalState] = useState<{
        isOpen: boolean;
        productId: number;
        orderId: number;
        variantId?: number;
        productName?: string;
        initialData?: ReviewResponse | null;
    }>({
        isOpen: false,
        productId: 0,
        orderId: 0
    });

    useEffect(() => {
        const fetchOrderDetail = async () => {
            if (!id) return;
            setLoading(true);
            try {
                const res = await orderApi.getOrderDetail(Number(id));
                setOrder(res.data);
                setError(null);
            } catch (err: any) {
                setError(err.response?.data?.message || 'Có lỗi xảy ra khi tải chi tiết đơn hàng');
            } finally {
                setLoading(false);
            }
        };
        fetchOrderDetail();
    }, [id]);

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    const formatDate = (dateString: string) => {
        const date = new Date(dateString);
        return date.toLocaleDateString('vi-VN', {
            hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit', year: 'numeric'
        });
    };

    const getStatusText = (status: string) => {
        const map: Record<string, string> = {
            'PENDING': 'Chờ xác nhận',
            'CONFIRMED': 'Đã xác nhận',
            'SHIPPED': 'Đang giao',
            'DELIVERED': 'Hoàn thành',
            'CANCELLED': 'Đã hủy'
        };
        return map[status] || status;
    };

    const handleReviewClick = async (productId: number, orderId: number, variantId?: number, productName?: string) => {
        let existingReview: ReviewResponse | null = null;
        try {
            const res = await reviewApi.getMyReviews(productId);
            const myReviews = res.data ?? [];
            existingReview = myReviews.find(r => r.orderId === orderId) || null;
        } catch (e) {

        }

        setReviewModalState({
            isOpen: true,
            productId,
            orderId,
            variantId,
            productName,
            initialData: existingReview
        });
    };

    const handleConfirmDelivery = async () => {
        if (!order || order.status !== 'SHIPPED' || confirmingDelivery) return;
        if (!window.confirm('Bạn xác nhận đã nhận đủ hàng? Thao tác này sẽ mở quyền đánh giá sản phẩm.')) return;
        setConfirmingDelivery(true);
        try {
            const response = await orderApi.confirmDelivery(order.id);
            setOrder(response.data);
            setError(null);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Không thể xác nhận nhận hàng. Vui lòng tải lại đơn hàng và thử lại.');
        } finally {
            setConfirmingDelivery(false);
        }
    };

    const handleCancelOrder = async () => {
        if (!order || order.status !== 'PENDING' || cancellingOrder) return;
        if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) return;
        setCancellingOrder(true);
        try {
            const response = await orderApi.cancelOrder(order.id);
            setOrder(response.data);
            setError(null);
        } catch (err: any) {
            setError(err.response?.data?.message || 'Không thể hủy đơn hàng. Vui lòng thử lại.');
        } finally {
            setCancellingOrder(false);
        }
    };

    if (loading) {
        return <div className="p-12 text-center text-gray-500">Đang tải chi tiết đơn hàng...</div>;
    }

    if (error || !order) {
        return (
            <div className="max-w-4xl mx-auto py-12 px-4">
                <div className="bg-red-50 text-red-500 p-6 rounded text-center mb-6">
                    {error || 'Không tìm thấy đơn hàng'}
                </div>
                <div className="text-center">
                    <button onClick={() => navigate('/orders')} className="text-orange-500 hover:underline">
                        &larr; Quay lại danh sách đơn hàng
                    </button>
                </div>
            </div>
        );
    }

    return (
        <div className="max-w-4xl mx-auto py-8 px-4">
            <div className="flex items-center justify-between mb-6">
                <button onClick={() => navigate('/orders')} className="text-gray-500 hover:text-orange-500 flex items-center gap-1 text-sm font-medium">
                    <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 19l-7-7 7-7"/></svg>
                    QUAY LẠI
                </button>
                <div className="text-sm">
                    <span className="text-gray-500 mr-2">MÃ ĐƠN HÀNG. {order.id}</span>
                    <span className="text-gray-300 mr-2">|</span>
                    <span className="text-orange-500 font-medium uppercase">{getStatusText(order.status)}</span>
                </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-6">
                <div className="bg-white p-6 rounded-sm shadow-sm">
                    <h3 className="text-lg font-medium text-gray-800 mb-4 pb-2 border-b">Địa Chỉ Nhận Hàng</h3>
                    <div className="text-gray-700 space-y-2 text-sm">
                        <p>{order.shippingAddress}</p>
                    </div>
                </div>
                <div className="bg-white p-6 rounded-sm shadow-sm">
                    <h3 className="text-lg font-medium text-gray-800 mb-4 pb-2 border-b">Thông Tin Thanh Toán</h3>
                    <div className="text-gray-700 space-y-2 text-sm">
                        <p>Phương thức: <span className="font-medium">{order.paymentMethod}</span></p>
                        <p>Ngày đặt: {formatDate(order.createdAt)}</p>
                        {order.deliveredAt && <p>Đã hoàn tất: {formatDate(order.deliveredAt)}</p>}
                    </div>
                </div>
            </div>

            <div className="bg-white rounded-sm shadow-sm mb-6">
                <div className="p-6 border-b border-gray-100 bg-gray-50/50">
                    <h3 className="text-lg font-medium text-gray-800">Sản Phẩm</h3>
                </div>
                <div>
                    {order.items.map(item => (
                        <div key={item.id} className="flex gap-4 p-6 border-b border-gray-50 last:border-0">
                            <div className="w-24 h-24 bg-gray-100 border border-gray-200 rounded-lg flex items-center justify-center text-gray-400 shrink-0 overflow-hidden">
                                {item.imageUrl ? (
                                    <img
                                        src={item.imageUrl}
                                        alt={item.productName}
                                        className="w-full h-full object-cover"
                                        onError={(e) => {
                                            (e.target as HTMLElement).style.display = 'none';
                                        }}
                                    />
                                ) : (
                                    <svg className="w-10 h-10 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                                )}
                            </div>
                            <div className="flex-1 flex flex-col justify-center">
                                <Link to={`/products/${item.productId || item.productVariantId}`} className="text-gray-800 hover:text-orange-500 text-lg mb-2">{item.productName}</Link>
                                <div className="text-gray-500 text-sm">Số lượng: x{item.quantity}</div>
                            </div>
                            <div className="text-right flex flex-col justify-center gap-2">
                                <div>
                                    <div className="text-sm text-gray-500 mb-1">{formatPrice(item.unitPrice)}</div>
                                    <div className="text-orange-500 font-medium">{formatPrice(item.subTotal)}</div>
                                </div>
                                {order.status === 'DELIVERED' && (
                                    <button
                                        onClick={() => handleReviewClick(item.productId || item.productVariantId, order.id, item.productVariantId, item.productName)}
                                        className="text-sm px-4 py-1.5 border border-orange-500 text-orange-600 font-medium rounded-lg hover:bg-orange-50 transition-colors shadow-xs cursor-pointer"
                                    >
                                        Đánh giá
                                    </button>
                                )}
                            </div>
                        </div>
                    ))}
                </div>
            </div>

            <div className="bg-white rounded-sm shadow-sm p-6 text-sm">
                <div className="flex justify-end mb-3">
                    <div className="w-64 flex justify-between text-gray-600">
                        <span>Tổng tiền hàng</span>
                        <span>{formatPrice(order.totalAmount)}</span>
                    </div>
                </div>
                <div className="flex justify-end mb-3">
                    <div className="w-64 flex justify-between text-gray-600">
                        <span>Phí vận chuyển</span>
                        <span>Miễn phí</span>
                    </div>
                </div>
                <div className="flex justify-end pt-4 border-t mt-4">
                    <div className="w-64 flex justify-between items-center">
                        <span className="text-gray-700">Thành tiền</span>
                        <span className="text-2xl font-bold text-orange-500">{formatPrice(order.totalAmount)}</span>
                    </div>
                </div>

                {order.status === 'PENDING' && (
                    <div className="flex justify-end pt-4 mt-4 border-t">
                        <button
                            disabled={cancellingOrder}
                            onClick={handleCancelOrder}
                            className="px-4 py-2 border border-gray-300 text-gray-700 hover:bg-gray-50 rounded text-sm transition-colors disabled:opacity-50"
                        >
                            {cancellingOrder ? 'Đang xử lý...' : 'Hủy Đơn Hàng'}
                        </button>
                    </div>
                )}
                {order.status === 'SHIPPED' && (
                    <div className="flex flex-col items-end gap-2 pt-4 mt-4 border-t">
                        <p className="text-sm text-gray-500">Chỉ xác nhận khi bạn đã nhận đủ hàng.</p>
                        <button
                            disabled={confirmingDelivery}
                            onClick={handleConfirmDelivery}
                            className="px-4 py-2 rounded text-sm bg-green-600 text-white hover:bg-green-700 disabled:opacity-50"
                        >
                            {confirmingDelivery ? 'Đang xác nhận...' : 'Đã nhận hàng'}
                        </button>
                    </div>
                )}
            </div>

            {reviewModalState.isOpen && (
                <CreateReviewModal
                    isOpen={reviewModalState.isOpen}
                    productId={reviewModalState.productId}
                    orderId={reviewModalState.orderId}
                    variantId={reviewModalState.variantId}
                    productName={reviewModalState.productName}
                    initialData={reviewModalState.initialData}
                    onClose={() => setReviewModalState(prev => ({ ...prev, isOpen: false }))}
                    onSuccess={() => {
                        setReviewModalState(prev => ({ ...prev, isOpen: false }));
                        alert('Cảm ơn bạn đã gửi đánh giá sản phẩm thành công!');
                    }}
                />
            )}
        </div>
    );
};
