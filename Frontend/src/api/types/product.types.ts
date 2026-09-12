export interface ProductImageResponse {
  id: number;
  imageUrl: string;
  publicId?: string;
  isThumbnail: boolean;
  sortOrder: number;
}

export interface ProductVariantResponse {
  id: number;
  sku: string;
  name: string;
  originalPrice: number;
  salePrice: number;
  stockQuantity: number;
  imageUrl?: string;

  price?: number;
  attributes?: Record<string, any>;
}

export interface ProductResponse {
  id: number;
  shopId: number;
  name: string;
  description: string;
  originalPrice: number;
  salePrice: number;
  stockQuantity: number;
  categoryName: string | null;
  categoryId?: number | null;
  brandName?: string | null;
  status?: string;
  images: ProductImageResponse[];
  variants: ProductVariantResponse[];

  price?: number;
  thumbnailUrl?: string;
  imageResponses?: ProductImageResponse[];
}

export interface ProductVariantRequest {
  id?: number;
  sku: string;
  name?: string;
  originalPrice?: number;
  salePrice?: number;
  price?: number;
  stockQuantity: number;
  imageUrl?: string;
  attributes?: Record<string, any>;
}

export interface ProductImageRequest {
  id?: number;
  imageUrl: string;
  publicId?: string;
  isThumbnail: boolean;
  sortOrder: number;
}

export interface ProductCreateRequest {
  name: string;
  description?: string;
  categoryId: number;
  brandId?: number;
  price: number;
  stockQuantity: number;
  images: ProductImageRequest[];
  variants: ProductVariantRequest[];

  shopId?: number;
  originalPrice?: number;
  salePrice?: number;
}

export interface ProductUpdateRequest {
  name: string;
  description?: string;
  categoryId?: number;
  brandId?: number;
  price: number;
  stockQuantity: number;
  images: ProductImageRequest[];
  variants: ProductVariantRequest[];

  originalPrice?: number;
  salePrice?: number;
}

export interface PageResponse<T> {
  content: T[];
  pageable?: {
    pageNumber: number;
    pageSize: number;
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
