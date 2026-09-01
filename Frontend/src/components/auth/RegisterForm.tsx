import React, { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { authApi } from '../../api/authApi';
import { useAuth } from '../../contexts/AuthContext';
import { AxiosError } from 'axios';
import type { ApiResponse } from '../../api/types/auth.types';

export const RegisterForm: React.FC = () => {
  const [fullName, setFullName] = useState('');
  const [email, setEmail] = useState('');
  const [phone, setPhone] = useState('');
  const [password, setPassword] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  
  const [isLoading, setIsLoading] = useState(false);
  const [globalError, setGlobalError] = useState<string | null>(null);
  const [fieldErrors, setFieldErrors] = useState<{ email?: string; password?: string; fullName?: string; confirmPassword?: string; phone?: string }>({});

  const { login } = useAuth();
  const navigate = useNavigate();

  const validate = () => {
    let valid = true;
    const errors: { email?: string; password?: string; fullName?: string; confirmPassword?: string; phone?: string } = {};

    if (!fullName) {
      errors.fullName = 'Vui lòng nhập họ và tên';
      valid = false;
    }

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
    } else if (password.length < 6) {
      errors.password = 'Mật khẩu phải có ít nhất 6 ký tự';
      valid = false;
    }

    if (password !== confirmPassword) {
      errors.confirmPassword = 'Mật khẩu xác nhận không khớp';
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
      const response = await authApi.register({ email, password, fullName, phone });
      if (response.data?.token) {
        login(response.data.token);
        navigate('/');
      }
    } catch (error) {
      const axiosError = error as AxiosError<ApiResponse<any>>;
      if (axiosError.response) {
        const responseData = axiosError.response.data;
        if (axiosError.response.status === 400) {
          if (responseData?.data && typeof responseData.data === 'object') {
            // Lỗi Validation chi tiết từ backend (@Valid)
            setFieldErrors(responseData.data);
          } else if (responseData?.message && responseData.message.toLowerCase().includes('email')) {
            // Lỗi Business logic liên quan tới email (như trùng email)
            setFieldErrors({ email: responseData.message });
          } else {
            // Các lỗi Business logic khác
            setGlobalError(responseData?.message || 'Đăng ký thất bại');
          }
        } else {
          setGlobalError(responseData?.message || 'Đăng ký thất bại');
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
          label="Họ và tên"
          type="text"
          placeholder="Nhập họ và tên của bạn"
          value={fullName}
          onChange={(e) => setFullName(e.target.value)}
          error={fieldErrors.fullName}
          disabled={isLoading}
        />
        
        <Input
          label="Email"
          type="email"
          placeholder="Nhập địa chỉ email"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          error={fieldErrors.email}
          disabled={isLoading}
        />
        
        <Input
          label="Số điện thoại"
          type="tel"
          placeholder="Nhập số điện thoại (Tùy chọn)"
          value={phone}
          onChange={(e) => setPhone(e.target.value)}
          error={fieldErrors.phone}
          disabled={isLoading}
        />
        
        <Input
          label="Mật khẩu"
          type="password"
          placeholder="Tạo mật khẩu (Ít nhất 6 ký tự)"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          error={fieldErrors.password}
          disabled={isLoading}
        />
        
        <Input
          label="Xác nhận mật khẩu"
          type="password"
          placeholder="Nhập lại mật khẩu"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          error={fieldErrors.confirmPassword}
          disabled={isLoading}
        />
        
        <Button type="submit" loading={isLoading}>
          Đăng ký tài khoản
        </Button>
      </form>

      <p className="text-xs text-center text-gray-400 mt-6 font-medium">
        Bằng việc đăng ký, bạn đồng ý với <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Điều khoản dịch vụ</a> & <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Chính sách bảo mật</a>
      </p>
    </div>
  );
};
