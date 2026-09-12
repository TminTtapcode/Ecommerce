import axios from 'axios';
import type { AxiosRequestConfig } from 'axios';
import { getApiError } from './apiError';
import { ERROR_CODES } from './types/errorCodes';
import { authSessionGuard } from '../contexts/authSession';
const requestSessions = new WeakMap<object, { generation: number; token: string | null }>();
export type SessionRequestConfig = AxiosRequestConfig & { authSessionGeneration?: number };

const apiClient = axios.create({
  baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8080',
  headers: {
    'Content-Type': 'application/json',
  },
});

apiClient.interceptors.request.use(
  (config) => {
    const explicit = config.headers?.Authorization;
    const token = typeof explicit === 'string' && explicit.startsWith('Bearer ') ? explicit.slice(7) : localStorage.getItem('token');
    requestSessions.set(config, { generation: (config as SessionRequestConfig).authSessionGeneration ?? authSessionGuard.capture(), token });
    if (token && config.headers) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    const session = error.config && requestSessions.get(error.config);
    if (session && (!authSessionGuard.accepts(session.generation) || session.token !== localStorage.getItem('token'))) return Promise.reject(error);
    const details = getApiError(error);
    if (details.status === 401 || details.errorCode === ERROR_CODES.UNAUTHORIZED) {

      localStorage.removeItem('token');

      if (!error.config?.url?.startsWith('/api/v1/auth/')) window.location.href = '/login';
    }
    return Promise.reject(error);
  }
);

export default apiClient;
