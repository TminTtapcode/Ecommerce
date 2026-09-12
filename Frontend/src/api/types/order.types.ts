export interface OrderItemResponse {
  id: number;
  productVariantId: number;
  productId: number;
  productName: string;
  quantity: number;
  unitPrice: number;
  subTotal: number;
  imageUrl?: string;
}

export interface OrderResponse {
  id: number;
  userId: number;
  customerName: string;
  shopId: number;
  status: 'PENDING' | 'CONFIRMED' | 'SHIPPED' | 'DELIVERED' | 'CANCELLED' | string;
  shippingAddress: string;
  paymentMethod: string;
  totalAmount: number;
  createdAt: string;
  deliveredAt?: string | null;
  deliveryConfirmationSource?: 'BUYER' | 'ADMIN' | null;
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

export interface CartPreviewItemResponse {
  variantId: number;
  productName: string;
  variantName: string;
  quantity: number;
  originalPrice: number;
  salePrice: number;
  subtotal: number;
}

export interface CartPreviewResponse {
  items?: CartPreviewItemResponse[];
  subtotal: number;
  discount: number;
  total: number;

  totalOriginalPrice?: number;
  totalDiscount?: number;
  voucherDiscount?: number;
  finalTotal?: number;
}

export interface CheckoutResponse {
  paymentGroupId: string;
  orders: OrderResponse[];
}

export interface VendorOrderStatusUpdateRequest {
  status: 'CONFIRMED' | 'SHIPPED' | 'CANCELLED';
  reason?: string;
}
