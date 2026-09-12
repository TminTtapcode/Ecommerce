export interface CartItemRequest {
  variantId?: number;
  productVariantId?: number;
  quantity: number;
}

export interface CartItemUpdateRequest {
  quantity: number;
}

export interface CartItemResponse {
  cartItemId: number;
  productVariantId: number;
  shopId: number;
  productName: string;
  sku: string;
  unitPrice: number;
  quantity: number;
  subTotal: number;
  thumbnailUrl: string;
  attributes?: Record<string, any>;
  isAvailable: boolean;

  id?: number;
  productId?: number;
  variantId?: number;
  subtotal?: number;
  imageUrl?: string;
  available?: boolean;
}

export interface CartResponse {
  cartId: number;
  items: CartItemResponse[];
  totalAmount: number;

  totalPrice?: number;
  totalItems?: number;
}
