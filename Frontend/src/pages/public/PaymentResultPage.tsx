import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, Link } from 'react-router-dom';
import { paymentApi } from '../../api/paymentApi';

export const PaymentResultPage: React.FC = () => {
    const location = useLocation();
    const navigate = useNavigate();
    const [status, setStatus] = useState<'loading' | 'success' | 'failed' | 'timeout' | 'error'>('loading');
    const [message, setMessage] = useState('Đang xử lý kết quả thanh toán...');
    const [activeGroupId, setActiveGroupId] = useState<string | null>(null);

    const checkPayment = async (paymentGroupId: string) => {
        try {
            const res = await paymentApi.getPaymentStatus(paymentGroupId);
            const currentStatus = res.data?.status;

            if (currentStatus === 'SUCCESS') {
                setStatus('success');
                setMessage('Thanh toán thành công! Đơn hàng của bạn đã được xác nhận.');
                sessionStorage.removeItem('pendingPaymentGroupId');
                return true;
            } else if (currentStatus === 'FAILED') {
                setStatus('failed');
                setMessage('Thanh toán thất bại hoặc đã bị hủy.');
                sessionStorage.removeItem('pendingPaymentGroupId');
                return true;
            }
            return false;
        } catch (error) {
            console.error("Error checking payment status:", error);
            return false;
        }
    };

    const startPolling = (paymentGroupId: string) => {
        setStatus('loading');
        setMessage('Đang kết nối cổng thanh toán VNPAY để xác nhận giao dịch...');
        let pollCount = 0;
        const maxPolls = 10;
        const pollInterval = 3000;

        let intervalId: ReturnType<typeof setInterval>;

        checkPayment(paymentGroupId).then((stopped) => {
            if (stopped) return;

            intervalId = setInterval(async () => {
                pollCount++;
                const shouldStop = await checkPayment(paymentGroupId);

                if (shouldStop || pollCount >= maxPolls) {
                    clearInterval(intervalId);
                    if (!shouldStop) {

                        setStatus('timeout');
                        setMessage('Giao dịch đang được xử lý bởi ngân hàng / VNPAY (IPN có thể bị trễ một vài phút).');
                    }
                }
            }, pollInterval);
        });

        return () => {
            if (intervalId) clearInterval(intervalId);
        };
    };

    useEffect(() => {
        const searchParams = new URLSearchParams(location.search);
        const paymentGroupId = searchParams.get('paymentGroupId') || sessionStorage.getItem('pendingPaymentGroupId');

        if (!paymentGroupId) {
            navigate('/orders', { replace: true });
            return;
        }

        setActiveGroupId(paymentGroupId);
        let cleanup: (() => void) | undefined;

        const initPolling = async () => {

            if (location.search.includes('vnp_ResponseCode')) {
                try {
                    await paymentApi.syncVnpayIpn(location.search);
                } catch (e) {
                    console.error("Lỗi đồng bộ VNPAY IPN", e);
                }
            }
            cleanup = startPolling(paymentGroupId);
        };

        initPolling();

        return () => {
            if (cleanup) cleanup();
        };
    }, [location.search, navigate]);

    return (
        <div className="max-w-md mx-auto mt-16 bg-white p-8 rounded shadow-sm text-center">
            {status === 'loading' && (
                <div>
                    <svg className="animate-spin h-12 w-12 text-orange-500 mx-auto mb-4" xmlns="http://www.w3.org/2000/svg" fill="none" viewBox="0 0 24 24">
                        <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4"></circle>
                        <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4zm2 5.291A7.962 7.962 0 014 12H0c0 3.042 1.135 5.824 3 7.938l3-2.647z"></path>
                    </svg>
                    <h2 className="text-xl font-medium text-gray-800 mb-2">Đang xác nhận thanh toán</h2>
                    <p className="text-sm text-gray-500">{message}</p>
                </div>
            )}

            {status === 'success' && (
                <div>
                    <svg className="h-16 w-16 text-green-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 12l2 2 4-4m6 2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Thanh Toán Thành Công!</h2>
                    <p className="text-gray-600 mb-6">{message}</p>
                    <Link to="/orders" className="inline-block bg-orange-500 hover:bg-orange-600 text-white font-medium py-2 px-6 rounded transition-colors">
                        Xem Đơn Hàng
                    </Link>
                </div>
            )}

            {status === 'timeout' && (
                <div>
                    <svg className="h-16 w-16 text-amber-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                        <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z"></path>
                    </svg>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Đang Chờ Xác Nhận</h2>
                    <p className="text-gray-600 mb-6 text-sm leading-relaxed">{message}</p>
                    <div className="flex flex-col sm:flex-row gap-3 justify-center">
                        {activeGroupId && (
                            <button
                                onClick={() => startPolling(activeGroupId)}
                                className="border border-orange-500 text-orange-500 hover:bg-orange-50 font-medium py-2 px-4 rounded text-sm transition-colors"
                            >
                                Kiểm Tra Lại
                            </button>
                        )}
                        <Link to="/orders" className="bg-orange-500 hover:bg-orange-600 text-white font-medium py-2 px-4 rounded text-sm transition-colors">
                            Vào Xem Đơn Hàng
                        </Link>
                    </div>
                </div>
            )}

            {(status === 'failed' || status === 'error') && (
                <div>
                    <svg className="h-16 w-16 text-red-500 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M10 14l2-2m0 0l2-2m-2 2l-2-2m2 2l2 2m7-2a9 9 0 11-18 0 9 9 0 0118 0z"></path></svg>
                    <h2 className="text-2xl font-bold text-gray-800 mb-2">Thanh Toán Thất Bại</h2>
                    <p className="text-gray-600 mb-6">{message}</p>
                    <div className="space-x-4">
                        <Link to="/cart" className="inline-block border border-orange-500 text-orange-500 hover:bg-orange-50 font-medium py-2 px-6 rounded transition-colors">
                            Về Giỏ Hàng
                        </Link>
                        <Link to="/orders" className="inline-block bg-orange-500 hover:bg-orange-600 text-white font-medium py-2 px-6 rounded transition-colors">
                            Xem Đơn Hàng
                        </Link>
                    </div>
                </div>
            )}
        </div>
    );
};
