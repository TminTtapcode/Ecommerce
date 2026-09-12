export interface ApiResponse<T> {
  success: boolean;
  status: number;
  errorCode?: string;
  fieldErrors?: Record<string, string>;
  message: string;
  data: T | null;
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
