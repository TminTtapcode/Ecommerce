export type VoucherScope = 'SYSTEM' | 'SHOP';
export type VoucherType = 'PERCENTAGE' | 'FIXED_AMOUNT';

export interface VoucherResponse {
  id: number;
  code: string;
  scope: VoucherScope;
  shopId?: number;
  type: VoucherType;
  discountValue: number;
  maxDiscount?: number;
  minOrderValue?: number;
  startDate: string;
  endDate: string;
  usageLimit?: number;
  usedCount: number;
}

export interface VoucherCreateRequest {
  code: string;
  scope: VoucherScope;
  type: VoucherType;
  discountValue: number;
  maxDiscount?: number;
  minOrderValue?: number;
  startDate: string;
  endDate: string;
  usageLimit?: number;
}

export interface VoucherUpdateRequest {
  type: VoucherType;
  discountValue: number;
  maxDiscount?: number;
  minOrderValue?: number;
  startDate: string;
  endDate: string;
  usageLimit?: number;
}
