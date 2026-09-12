import axios from 'axios';
import { getApiError } from './apiError';
import { ERROR_CODES } from './types/errorCodes';

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

// Tự động gắn Bearer Token vào Header Authorization cho mọi request
apiClient.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => Promise.reject(error)
);

// Bắt lỗi 401 Unauthorized toàn cục để tự động đăng xuất và chuyển về trang Login
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const details = getApiError(error);
    if (details.status === 401 || details.errorCode === ERROR_CODES.UNAUTHORIZED) {
      localStorage.removeItem('token');
      if (!error.config?.url?.startsWith('/api/v1/auth/')) {
        window.location.href = '/login';
      }
    }
    return Promise.reject(error);
  }
);

export default apiClient;
