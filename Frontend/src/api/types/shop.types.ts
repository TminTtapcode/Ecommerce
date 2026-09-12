export type ShopStatus = 'PENDING' | 'ACTIVE' | 'BANNED';

export interface ShopResponse {
  id: number;
  userId: number;
  name: string;
  description: string;
  status: ShopStatus;
  priorStatus?: ShopStatus | null;
}

export interface ShopCreateRequest {
  name: string;
  description?: string;
}
