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

  // Aliases for compatibility
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

  // Aliases for compatibility
  totalPrice?: number;
  totalItems?: number;
}
