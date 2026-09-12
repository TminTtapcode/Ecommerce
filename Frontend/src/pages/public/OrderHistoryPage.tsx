import React, { useState, useEffect } from 'react';
import { Link } from 'react-router-dom';
import { orderApi } from '../../api/orderApi';
import type { OrderResponse } from '../../api/types/order.types';

export const OrderHistoryPage: React.FC = () => {
    const [orders, setOrders] = useState<OrderResponse[]>([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState<string | null>(null);
    const [status, setStatus] = useState<string>('');
    const [page, setPage] = useState(0);
    const [totalPages, setTotalPages] = useState(1);

    const TABS = [
        { label: 'Tất cả', value: '' },
        { label: 'Chờ xác nhận', value: 'PENDING' },
        { label: 'Đã xác nhận', value: 'CONFIRMED' },
        { label: 'Đang giao', value: 'SHIPPED' },
        { label: 'Hoàn thành', value: 'DELIVERED' },
        { label: 'Đã hủy', value: 'CANCELLED' }
    ];

    useEffect(() => {
        const fetchOrders = async () => {
            setLoading(true);
            try {
                const res = await orderApi.getMyOrders({ page, size: 5, status: status || undefined });
                setOrders(res.data?.content || []);
                setTotalPages(res.data?.totalPages || 1);
                setError(null);
            } catch (err: any) {
                setError('Có lỗi xảy ra khi tải danh sách đơn hàng');
            } finally {
                setLoading(false);
            }
        };
        fetchOrders();
    }, [page, status]);

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

    const getStatusColor = (status: string) => {
        const map: Record<string, string> = {
            'PENDING': 'text-yellow-600',
            'CONFIRMED': 'text-blue-500',
            'SHIPPED': 'text-indigo-500',
            'DELIVERED': 'text-green-500',
            'CANCELLED': 'text-red-500'
        };
        return map[status] || 'text-gray-500';
    };

    return (
        <div className="max-w-5xl mx-auto py-8 px-4">
            <h1 className="text-2xl font-bold text-gray-800 mb-6">Đơn Mua Của Tôi</h1>

            <div className="flex bg-white shadow-sm rounded-sm mb-4 border-b border-gray-200">
                {TABS.map(tab => (
                    <button
                        key={tab.value}
                        onClick={() => { setStatus(tab.value); setPage(0); }}
                        className={`flex-1 py-4 text-center text-sm font-medium transition-colors ${
                            status === tab.value
                                ? 'text-orange-500 border-b-2 border-orange-500'
                                : 'text-gray-600 hover:text-orange-500'
                        }`}
                    >
                        {tab.label}
                    </button>
                ))}
            </div>

            {loading ? (
                <div className="text-center py-12 text-gray-500">Đang tải danh sách đơn hàng...</div>
            ) : error ? (
                <div className="bg-red-50 text-red-500 p-4 rounded text-center">{error}</div>
            ) : orders.length === 0 ? (
                <div className="bg-white p-12 text-center shadow-sm rounded-sm">
                    <svg className="w-24 h-24 text-gray-300 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M9 5H7a2 2 0 00-2 2v12a2 2 0 002 2h10a2 2 0 002-2V7a2 2 0 00-2-2h-2M9 5a2 2 0 002 2h2a2 2 0 002-2M9 5a2 2 0 012-2h2a2 2 0 012 2m-6 9l2 2 4-4"/></svg>
                    <p className="text-gray-500 text-lg mb-4">Chưa có đơn hàng nào</p>
                    <Link to="/" className="inline-block px-6 py-2 bg-orange-500 text-white rounded hover:bg-orange-600 transition-colors">Mua Sắm Ngay</Link>
                </div>
            ) : (
                <div className="space-y-4">
                    {orders.map(order => (
                        <div key={order.id} className="bg-white rounded-sm shadow-sm overflow-hidden">
                            <div className="flex justify-between items-center p-4 border-b border-gray-100 bg-gray-50">
                                <div className="text-sm">
                                    <span className="font-medium">Mã đơn: #{order.id}</span>
                                    <span className="mx-2 text-gray-300">|</span>
                                    <span className="text-gray-500">{formatDate(order.createdAt)}</span>
                                </div>
                                <div className={`text-sm font-medium uppercase ${getStatusColor(order.status)}`}>
                                    {getStatusText(order.status)}
                                </div>
                            </div>

                            <div className="p-4 border-b border-gray-100">
                                {order.items.map(item => (
                                    <div key={item.id} className="flex gap-4 py-3 first:pt-0 last:pb-0">
                                        <div className="w-20 h-20 bg-gray-100 border border-gray-200 rounded-lg flex items-center justify-center text-gray-400 shrink-0 overflow-hidden">
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
                                                <svg className="w-8 h-8 text-gray-300" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                                            )}
                                        </div>
                                        <div className="flex-1">
                                            <Link to={`/products/${item.productId || item.productVariantId}`} className="text-gray-800 hover:text-orange-500 text-base">{item.productName}</Link>
                                            <div className="text-sm text-gray-500 mt-1">x{item.quantity}</div>
                                        </div>
                                        <div className="text-right flex flex-col justify-center">
                                            <span className="text-orange-500">{formatPrice(item.unitPrice)}</span>
                                        </div>
                                    </div>
                                ))}
                            </div>

                            <div className="p-4 bg-orange-50/30 flex justify-between items-center">
                                <div className="text-sm text-gray-500">
                                    Thanh toán: <span className="font-medium text-gray-700">{order.paymentMethod}</span>
                                </div>
                                <div className="flex items-center gap-4">
                                    <div className="text-sm text-gray-700">
                                        Thành tiền: <span className="text-xl font-bold text-orange-500 ml-2">{formatPrice(order.totalAmount)}</span>
                                    </div>
                                    <div className="flex gap-2">
                                        {order.status === 'PENDING' && (
                                            <button
                                                onClick={async () => {
                                                    if (!window.confirm('Bạn có chắc chắn muốn hủy đơn hàng này không?')) return;
                                                    try {
                                                        await orderApi.cancelOrder(order.id);
                                                        setOrders(orders.map(o => o.id === order.id ? { ...o, status: 'CANCELLED' } : o));
                                                    } catch (err: any) {
                                                        alert(err.response?.data?.message || 'Không thể hủy đơn hàng. Vui lòng thử lại.');
                                                    }
                                                }}
                                                className="px-6 py-2 border border-gray-300 text-gray-700 rounded hover:bg-gray-50 transition-colors text-sm"
                                            >
                                                Hủy Đơn Hàng
                                            </button>
                                        )}
                                        {order.status === 'SHIPPED' && (
                                            <button
                                                onClick={async () => {
                                                    if (!window.confirm('Bạn xác nhận đã nhận đủ hàng? Thao tác này sẽ chuyển đơn sang Hoàn thành và mở quyền đánh giá.')) return;
                                                    try {
                                                        await orderApi.confirmDelivery(order.id);
                                                        setOrders(orders.map(o => o.id === order.id ? { ...o, status: 'DELIVERED' } : o));
                                                    } catch (err: any) {
                                                        alert(err.response?.data?.message || 'Không thể xác nhận nhận hàng. Vui lòng thử lại.');
                                                    }
                                                }}
                                                className="px-5 py-2 bg-emerald-600 hover:bg-emerald-700 text-white rounded font-medium transition-colors text-sm shadow-xs cursor-pointer"
                                            >
                                                Đã Nhận Hàng
                                            </button>
                                        )}
                                        <Link to={`/orders/${order.id}`} className="px-6 py-2 bg-orange-500 text-white rounded hover:bg-orange-600 transition-colors text-sm font-medium">
                                            Xem Chi Tiết
                                        </Link>
                                    </div>
                                </div>
                            </div>
                        </div>
                    ))}

                    {totalPages > 1 && (
                        <div className="flex justify-center mt-6 space-x-2">
                            <button
                                onClick={() => setPage(p => Math.max(0, p - 1))}
                                disabled={page === 0}
                                className="px-4 py-2 border rounded-sm hover:bg-gray-50 disabled:opacity-50"
                            >
                                Trước
                            </button>
                            <div className="flex items-center px-4 font-medium text-gray-700">
                                {page + 1} / {totalPages}
                            </div>
                            <button
                                onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                                disabled={page === totalPages - 1}
                                className="px-4 py-2 border rounded-sm hover:bg-gray-50 disabled:opacity-50"
                            >
                                Sau
                            </button>
                        </div>
                    )}
                </div>
            )}
        </div>
    );
};
