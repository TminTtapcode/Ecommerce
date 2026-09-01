export interface CartItemRequest {
  productVariantId: number;
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
  attributes: Record<string, any>;
  isAvailable: boolean;
}

export interface CartResponse {
  cartId: number;
  items: CartItemResponse[];
  totalAmount: number;
}

