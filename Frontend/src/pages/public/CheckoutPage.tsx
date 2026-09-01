import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { useCart } from '../../contexts/CartContext';
import { orderApi } from '../../api/orderApi';
import { paymentApi } from '../../api/paymentApi';
import type { CartItemResponse } from '../../api/types/cart.types';

export const CheckoutPage: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const { cart, loading: cartLoading, fetchCart } = useCart();
    const [submitting, setSubmitting] = useState(false);
    const [shippingAddress, setShippingAddress] = useState('');
    const [paymentMethod, setPaymentMethod] = useState('COD');
    const [error, setError] = useState<string | null>(null);
    const [voucherCode, setVoucherCode] = useState('');
    const [previewData, setPreviewData] = useState<import('../../api/types/order.types').CartPreviewResponse | null>(null);
    const [voucherError, setVoucherError] = useState<string | null>(null);
    const [applyingVoucher, setApplyingVoucher] = useState(false);

    const state = location.state as { selectedItemIds?: number[] };
    const selectedItemIds = state?.selectedItemIds || [];

    useEffect(() => {
        if (selectedItemIds.length === 0) {
            navigate('/cart');
        }
    }, [selectedItemIds, navigate]);

    if (cartLoading) {
        return <div className="p-8 text-center text-gray-500">Đang tải thông tin...</div>;
    }

    const itemsToCheckout: CartItemResponse[] = cart?.items.filter(item => selectedItemIds.includes(item.cartItemId)) || [];
    
    // Default fallback calculate in frontend if API fails
    const fallbackTotalAmount = itemsToCheckout.reduce((sum, item) => sum + item.subTotal, 0);

    const fetchPreview = async (code: string = '') => {
        if (selectedItemIds.length === 0) return;
        setApplyingVoucher(true);
        setVoucherError(null);
        try {
            const res = await orderApi.previewCheckout({
                cartItemIds: selectedItemIds,
                voucherCode: code
            });
            if (res.data) {
                setPreviewData(res.data);
            }
        } catch (err: any) {
            setVoucherError(err.response?.data?.message || 'Không thể tính toán giỏ hàng hoặc mã giảm giá không hợp lệ');
            if (code) {
                setVoucherCode(''); // Reset if invalid
            }
        } finally {
            setApplyingVoucher(false);
        }
    };

    useEffect(() => {
        if (!cartLoading && selectedItemIds.length > 0) {
            fetchPreview(voucherCode);
        }
    }, [cartLoading, selectedItemIds]);

    const formatPrice = (price: number) => {
        return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(price);
    };

    const handleCheckout = async (e: React.FormEvent) => {
        e.preventDefault();
        setError(null);
        if (!shippingAddress.trim()) {
            setError("Vui lòng nhập địa chỉ giao hàng");
            return;
        }

        setSubmitting(true);
        try {
            const result = await orderApi.checkout({
                shippingAddress,
                paymentMethod,
                cartItemIds: selectedItemIds,
                voucherCode: voucherCode.trim() || undefined
            });
            await fetchCart(); // Refresh cart to reflect removed items
            
            if (paymentMethod === 'VNPAY' && result.data && result.data.paymentGroupId) {
                try {
                    const paymentGroupId = result.data.paymentGroupId;
                    const paymentRes = await paymentApi.createPaymentUrl({ paymentGroupId });
                    if (paymentRes.data?.paymentUrl) {
                        sessionStorage.setItem('pendingPaymentGroupId', paymentGroupId);
                        window.location.href = paymentRes.data.paymentUrl;
                        return;
                    }
                } catch (paymentErr: any) {
                    setError('Đặt hàng thành công nhưng có lỗi khi tạo thanh toán VNPAY');
                    setSubmitting(false);
                    return;
                }
            }

            navigate('/orders', { replace: true });
        } catch (err: any) {
            setError(err.response?.data?.message || 'Có lỗi xảy ra khi đặt hàng');
            setSubmitting(false);
        }
    };

    return (
        <div className="max-w-4xl mx-auto py-8 px-4">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Thanh Toán</h1>
            
            <form onSubmit={handleCheckout}>
                <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
                    {/* Left Column - Form inputs */}
                    <div className="md:col-span-2 space-y-6">
                        {error && (
                            <div className="bg-red-50 text-red-500 p-3 rounded text-sm">
                                {error}
                            </div>
                        )}
                        
                        <div className="bg-white p-6 rounded shadow-sm">
                            <h2 className="text-lg font-medium mb-4 flex items-center text-gray-800">
                                <svg className="w-5 h-5 mr-2 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M17.657 16.657L13.414 20.9a1.998 1.998 0 01-2.827 0l-4.243-4.243a8 8 0 1111.314 0z"/><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M15 11a3 3 0 11-6 0 3 3 0 016 0z"/></svg>
                                Địa Chỉ Giao Hàng
                            </h2>
                            <div className="mb-4">
                                <label className="block text-sm font-medium text-gray-700 mb-1">Địa chỉ nhận hàng</label>
                                <textarea 
                                    className="w-full border border-gray-300 rounded p-2 focus:ring-orange-500 focus:border-orange-500 outline-none" 
                                    rows={3}
                                    placeholder="Nhập địa chỉ chi tiết (VD: Số 1, Đường 2, Phường 3, Quận 4, TP.HCM)"
                                    value={shippingAddress}
                                    onChange={(e) => setShippingAddress(e.target.value)}
                                    required
                                />
                            </div>
                        </div>

                        <div className="bg-white p-6 rounded shadow-sm">
                            <h2 className="text-lg font-medium mb-4 flex items-center text-gray-800">
                                <svg className="w-5 h-5 mr-2 text-orange-500" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 10h18M7 15h1m4 0h1m-7 4h12a3 3 0 003-3V8a3 3 0 00-3-3H6a3 3 0 00-3 3v8a3 3 0 003 3z"/></svg>
                                Phương Thức Thanh Toán
                            </h2>
                            <div className="space-y-3">
                                <label className="flex items-center p-3 border border-gray-200 rounded cursor-pointer hover:bg-orange-50">
                                    <input 
                                        type="radio" 
                                        name="paymentMethod" 
                                        value="COD" 
                                        className="w-4 h-4 accent-orange-500" 
                                        checked={paymentMethod === 'COD'}
                                        onChange={() => setPaymentMethod('COD')}
                                    />
                                    <span className="ml-3 font-medium text-gray-700">Thanh toán khi nhận hàng (COD)</span>
                                </label>
                                <label className="flex items-center p-3 border border-gray-200 rounded cursor-pointer hover:bg-orange-50">
                                    <input 
                                        type="radio" 
                                        name="paymentMethod" 
                                        value="VNPAY" 
                                        className="w-4 h-4 accent-orange-500"
                                        checked={paymentMethod === 'VNPAY'}
                                        onChange={() => setPaymentMethod('VNPAY')} 
                                    />
                                    <span className="ml-3 font-medium text-gray-700">Thanh toán qua VNPAY</span>
                                </label>
                            </div>
                        </div>
                    </div>

                    {/* Right Column - Order Summary */}
                    <div className="md:col-span-1">
                        <div className="bg-white p-6 rounded shadow-sm sticky top-24">
                            <h2 className="text-lg font-medium mb-4 text-gray-800 border-b pb-2">Đơn Hàng Của Bạn</h2>
                            
                            <div className="max-h-60 overflow-y-auto mb-4 space-y-3 pr-2">
                                {itemsToCheckout.map(item => (
                                    <div key={item.cartItemId} className="flex justify-between items-start text-sm border-b border-gray-100 pb-2 last:border-0">
                                        <div className="flex-1 pr-2">
                                            <div className="text-gray-800 line-clamp-2">{item.productName}</div>
                                            <div className="text-gray-500 text-xs mt-1">SL: {item.quantity}</div>
                                        </div>
                                        <div className="font-medium text-gray-700 whitespace-nowrap">
                                            {formatPrice(item.subTotal)}
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="border-t pt-4 mt-4 space-y-4">
                                <div>
                                    <label className="block text-sm font-medium text-gray-700 mb-1">Mã Giảm Giá</label>
                                    <div className="flex">
                                        <input 
                                            type="text" 
                                            placeholder="Nhập mã giảm giá..."
                                            value={voucherCode}
                                            onChange={(e) => setVoucherCode(e.target.value.toUpperCase())}
                                            className="flex-1 border border-gray-300 rounded-l p-2 text-sm focus:outline-none focus:border-orange-500 uppercase"
                                        />
                                        <button 
                                            type="button"
                                            onClick={() => fetchPreview(voucherCode)}
                                            disabled={applyingVoucher || !voucherCode.trim()}
                                            className="bg-gray-800 text-white px-4 rounded-r text-sm font-medium hover:bg-gray-700 disabled:opacity-70 disabled:cursor-not-allowed"
                                        >
                                            {applyingVoucher ? 'Đang áp dụng...' : 'Áp dụng'}
                                        </button>
                                    </div>
                                    {voucherError && (
                                        <p className="text-red-500 text-xs mt-1">{voucherError}</p>
                                    )}
                                </div>
                            </div>

                            <div className="border-t pt-4 mt-4 space-y-2 text-sm">
                                <div className="flex justify-between text-gray-600">
                                    <span>Tạm tính</span>
                                    <span>{formatPrice(previewData ? previewData.subtotal : fallbackTotalAmount)}</span>
                                </div>
                                <div className="flex justify-between text-gray-600">
                                    <span>Phí vận chuyển</span>
                                    <span>Miễn phí</span>
                                </div>
                                {previewData && previewData.discount > 0 && (
                                    <div className="flex justify-between text-green-600">
                                        <span>Giảm giá</span>
                                        <span>- {formatPrice(previewData.discount)}</span>
                                    </div>
                                )}
                                <div className="flex justify-between items-center text-lg font-bold text-gray-800 pt-2 border-t mt-2">
                                    <span>Tổng cộng</span>
                                    <span className="text-orange-500">{formatPrice(previewData ? previewData.total : fallbackTotalAmount)}</span>
                                </div>
                            </div>

                            <button 
                                type="submit" 
                                disabled={submitting || itemsToCheckout.length === 0 || applyingVoucher}
                                className="w-full bg-orange-500 hover:bg-orange-600 text-white font-medium py-3 rounded mt-6 transition-colors disabled:opacity-70 disabled:cursor-not-allowed"
                            >
                                {submitting ? 'Đang xử lý...' : 'ĐẶT HÀNG'}
                            </button>
                        </div>
                    </div>
                </div>
            </form>
        </div>
    );
};
