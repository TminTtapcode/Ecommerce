export type PayoutStatus = 'PENDING' | 'APPROVED' | 'REJECTED';

export interface PayoutResponse {
  id: number;
  shopId: number;
  shopName: string;
  amount: number;
  bankName: string;
  bankAccountNumber: string;
  bankAccountHolder: string;
  status: PayoutStatus;
  adminNote?: string;
  processedBy?: number;
  processedAt?: string;
  createdAt: string;
}

export interface PayoutBalanceResponse {
  shopId: number;
  shopName: string;
  deliveredOrderCount: number;
  grossDeliveredRevenue: number;
  commissionRate: number;
  totalCommission: number;
  netRevenue: number;
  totalWithdrawn: number;
  pendingWithdrawal: number;
  availableBalance: number;
}

export interface CreatePayoutRequest {
  amount: number;
  bankName: string;
  bankAccountNumber: string;
  bankAccountHolder: string;
}

export interface ApprovePayoutRequest {
  adminNote?: string;
}

export interface RejectPayoutRequest {
  reason: string;
}
