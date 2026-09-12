import apiClient from './apiClient';

export interface UploadResponse {
  url: string;
  publicId: string;
}

export interface ApiResponse<T> {
  status: number;
  message: string;
  data: T;
}

export const mediaApi = {
  uploadImage: async (file: File): Promise<ApiResponse<UploadResponse>> => {
    const formData = new FormData();
    formData.append('file', file);

    const response = await apiClient.post<ApiResponse<UploadResponse>>('/api/v1/media/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data',
      },
    });
    return response.data;
  },
};
