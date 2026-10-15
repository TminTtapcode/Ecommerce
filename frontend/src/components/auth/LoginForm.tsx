import React, { useState } from 'react';
import { useNavigate, useLocation } from 'react-router-dom';
import { Input } from '../common/Input';
import { Button } from '../common/Button';
import { authApi } from '../../api/authApi';
import { useAuth } from '../../contexts/AuthContext';
import { getApiError } from '../../api/apiError';

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
      const details = getApiError(error, 'Không thể đăng nhập hoặc đăng ký. Vui lòng thử lại.');
      if (details.fieldErrors) {
        setFieldErrors(details.fieldErrors);
      } else {
        setGlobalError(details.message);
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

      <div className="flex justify-end items-center text-sm mt-2">
        <a href="#" className="text-blue-600 hover:opacity-80">Quên mật khẩu</a>
      </div>



      <p className="text-xs text-center text-gray-400 mt-6 font-medium">
        Bằng việc đăng nhập, bạn đồng ý với <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Điều khoản dịch vụ</a> & <a href="#" className="text-gray-600 hover:text-orange-500 transition-colors">Chính sách bảo mật</a>
      </p>
    </div>
  );
};
