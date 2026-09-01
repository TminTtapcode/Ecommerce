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
  price: number;
  stockQuantity: number;
  attributes: Record<string, any>;
}

export interface ProductResponse {
  id: number;
  shopId: number;
  name: string;
  description: string;
  price: number;
  stockQuantity: number;
  categoryName: string | null;
  variants: ProductVariantResponse[];
  imageResponses: ProductImageResponse[];
}

export interface ProductVariantRequest {
  id?: number; // Optional for new variants
  sku: string;
  price: number;
  stockQuantity: number;
  attributes: Record<string, any>;
}

export interface ProductImageRequest {
  id?: number; // Optional for new images
  imageUrl: string;
  publicId?: string;
  isThumbnail: boolean;
  sortOrder: number;
}

export interface ProductCreateRequest {
  shopId: number;
  name: string;
  description?: string;
  price: number;
  stockQuantity: number;
  categoryId: number;
  brandId?: number;
  variants: ProductVariantRequest[];
  images: ProductImageRequest[];
}

export interface ProductUpdateRequest {
  name: string;
  description?: string;
  price: number;
  stockQuantity: number;
  categoryId?: number;
  variants: ProductVariantRequest[]; 
  images: ProductImageRequest[]; 
}

export interface PageResponse<T> {
  content: T[];
  pageable: {
    pageNumber: number;
    pageSize: number;
    sort: { empty: boolean; sorted: boolean; unsorted: boolean };
    offset: number;
    paged: boolean;
    unpaged: boolean;
  };
  last: boolean;
  totalPages: number;
  totalElements: number;
  size: number;
  number: number;
  sort: { empty: boolean; sorted: boolean; unsorted: boolean };
  first: boolean;
  numberOfElements: number;
  empty: boolean;
}
