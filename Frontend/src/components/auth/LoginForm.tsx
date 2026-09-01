import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { authApi } from '../../api/authApi';
import { useAuth } from '../../contexts/AuthContext';
import { AxiosError } from 'axios';
import type { ApiResponse } from '../../api/types/auth.types';

export const LoginForm: React.FC = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [isLoading, setIsLoading] = useState(false);
  const [globalError, setGlobalError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string }>({});

  const { login } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const from = location.state?.from?.pathname || '/';

  const validate = () => {
    let valid = true;
    const errors: { email?: string; password?: string } = {};

    if (!email) {
      errors.email = 'Vui lòng nhập email';
      valid = false;
    } else if (!/\S+@\S+\.\S+/.test(email)) {
      errors.email = 'Định dạng email không hợp lệ';
      valid = false;
    }

    if (!password) {
      errors.password = 'Vui lòng nhập mật khẩu';
      valid = false;
    }

    setFieldErrors(errors);
    return valid;
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setGlobalError(null);
    setFieldErrors({});

    if (!validate()) {
      return;
    }

    setIsLoading(true);
    try {
      const response = await authApi.login({ email, password });
      if (response.data?.token) {
        login(response.data.token);
        navigate(from, { replace: true });
      }
    } catch (error) {
      const axiosError = error as AxiosError<ApiResponse<any>>;
      if (axiosError.response) {
        const responseData = axiosError.response.data;
        if (axiosError.response.status === 400) {
          if (responseData?.data && typeof responseData.data === 'object') {
            // Lỗi Validation chi tiết từ backend (@Valid)
            setFieldErrors(responseData.data);
          } else {
            // Các lỗi Business logic khác (như sai tài khoản mật khẩu)
            setGlobalError(responseData?.message || 'Đăng nhập thất bại');
          }
        } else {
          setGlobalError(responseData?.message || 'Đăng nhập thất bại');
        }
      } else {
        setGlobalError('Lỗi kết nối máy chủ, vui lòng thử lại sau');
      }
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="space-y-4">
      <form onSubmit={handleSubmit} className="space-y-4">
        {globalError && (
          <div className="bg-red-50 border border-red-200 text-red-600 px-4 py-3 rounded text-sm">
            {globalError}
          </div>
        )}
        <Input
          label="Email/Số điện thoại/Tên đăng nhập"
          type="email"
          placeholder="Email/Số điện thoại/Tên đăng nhập"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={fieldErrors.email}
          disabled={isLoading}
        />
        <Input
          label="Mật khẩu"
          type="password"
          placeholder="Mật khẩu"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
          disabled={isLoading}
        />
        <Button type="submit" loading={isLoading}>
          Đăng nhập
        </Button>
      </form>

      <div className="flex justify-between items-center text-sm mt-2">
        <a href="#" className="text-blue-600 hover:opacity-80">Quên mật khẩu</a>
        <a href="#" className="text-blue-600 hover:opacity-80">Đăng nhập với SMS</a>
      </div>

      <div className="relative flex items-center py-4">
        <div className="flex-grow border-t border-gray-200"></div>
        <span className="flex-shrink-0 mx-4 text-gray-400 text-xs uppercase">Hoặc</span>
        <div className="flex-grow border-t border-gray-200"></div>
      </div>

      <div className="flex gap-3">
        <button className="w-full flex justify-center items-center py-3 px-4 border border-gray-100 rounded-2xl shadow-sm text-sm font-semibold text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 transition-all">
          <svg className="h-5 w-5 mr-2 text-[#1877F2]" fill="currentColor" viewBox="0 0 24 24"><path d="M24 12.073c0-6.627-5.373-12-12-12s-12 5.373-12 12c0 5.99 4.388 10.954 10.125 11.854v-8.385H7.078v-3.469h3.047V9.43c0-3.007 1.792-4.669 4.533-4.669 1.312 0 2.686.235 2.686.235v2.953H15.83c-1.491 0-1.956.925-1.956 1.874v2.25h3.328l-.532 3.469h-2.796v8.385C19.612 23.027 24 18.062 24 12.073z"/></svg>
          Facebook
        </button>
        <button className="w-full flex justify-center items-center py-3 px-4 border border-gray-100 rounded-2xl shadow-sm text-sm font-semibold text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 transition-all">
          <svg className="h-5 w-5 mr-2" viewBox="0 0 24 24" fill="none" xmlns="http://www.w3.org/2000/svg"><path fillRule="evenodd" clipRule="evenodd" d="M23.52 12.273c0-.85-.076-1.67-.22-2.455H12v4.64h6.455c-.276 1.496-1.118 2.764-2.39 3.614v3.003h3.873c2.264-2.083 3.582-5.155 3.582-8.802z" fill="#4285F4"/><path fillRule="evenodd" clipRule="evenodd" d="M12 24c3.24 0 5.956-1.076 7.942-2.914l-3.873-3.003c-1.076.721-2.455 1.147-4.069 1.147-3.13 0-5.782-2.115-6.726-4.96H1.272v3.104C3.264 21.328 7.309 24 12 24z" fill="#34A853"/><path fillRule="evenodd" clipRule="evenodd" d="M5.274 14.27c-.24-.721-.377-1.495-.377-2.27s.137-1.55.377-2.27V6.626H1.272C.464 8.236 0 10.05 0 12s.464 3.764 1.272 5.374l4.002-3.104z" fill="#FBBC05"/><path fillRule="evenodd" clipRule="evenodd" d="M12 4.823c1.762 0 3.344.606 4.588 1.792l3.435-3.435C17.95 1.18 15.234 0 12 0 7.31 0 3.264 2.671 1.272 6.626l4.002 3.104c.944-2.845 3.596-4.907 6.726-4.907z" fill="#EA4335"/></svg>
          Google
        </button>
      </div>
      
      <p className="text-xs text-center text-gray-400 mt-6 font-medium">
        Bằng việc đăng nhập, bạn đồng ý với <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Điều khoản dịch vụ</a> & <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Chính sách bảo mật</a>
      </p>
    </div>
  );
};
