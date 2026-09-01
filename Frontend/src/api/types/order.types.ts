export interface OrderItemResponse {
    id: number;
    productVariantId: number;
    productId: number;
    productName: string;
    quantity: number;
    unitPrice: number;
    subTotal: number;
}

export interface OrderResponse {
    id: number;
    userId: number;
    customerName: string;
    shopId: number;
    status: string; // PENDING, SHIPPING, DELIVERED, CANCELLED
    shippingAddress: string;
    paymentMethod: string;
    totalAmount: number;
    createdAt: string;
    items: OrderItemResponse[];
}

export interface CheckoutRequest {
    shippingAddress: string;
    paymentMethod: string;
    cartItemIds: number[];
    voucherCode?: string;
}

export interface CartPreviewRequest {
    cartItemIds: number[];
    voucherCode?: string;
}

export interface CartPreviewResponse {
    subtotal: number;
    discount: number;
    total: number;
}

export interface CheckoutResponse {
    paymentGroupId: string;
    orders: OrderResponse[];
}

export interface VendorOrderStatusUpdateRequest {
    status: 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED';
    reason?: string;
}
