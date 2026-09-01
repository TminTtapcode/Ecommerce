import React from 'react';
import { RegisterForm } from '../../components/auth/RegisterForm';
import { Navigate, Link } from 'react-router-dom';
import { useAuth } from '../../contexts/AuthContext';

export const RegisterPage: React.FC = () => {
  const { isAuthenticated } = useAuth();

  if (isAuthenticated) {
    return <Navigate to="/" replace />;
  }

  return (
    <div className="min-h-screen soft-gradient-bg flex items-center justify-center p-4 sm:p-6 lg:p-8 relative overflow-hidden">
      
      {/* Background Blink Blink Elements */}
      <div className="absolute top-0 left-0 w-64 h-64 bg-orange-400/20 rounded-full blur-3xl z-0 animate-pulse delay-1000 -translate-x-1/2 -translate-y-1/2"></div>
      <div className="absolute bottom-0 right-0 w-80 h-80 bg-blue-400/20 rounded-full blur-3xl z-0 animate-pulse delay-2000 translate-x-1/3 translate-y-1/3"></div>
      
      {/* Twinkling Stars (moved to edges so they aren't hidden by the card) */}
      <svg className="absolute top-20 right-20 w-8 h-8 text-orange-400/60 z-0 animate-twinkle delay-1000" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" /></svg>
      <svg className="absolute bottom-32 left-20 w-6 h-6 text-purple-400/60 z-0 animate-twinkle delay-2000" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" /></svg>
      <svg className="absolute top-32 left-32 w-5 h-5 text-blue-400/60 z-0 animate-twinkle delay-1500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" /></svg>
      <svg className="absolute bottom-20 right-1/3 w-7 h-7 text-rose-400/60 z-0 animate-twinkle delay-1000" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" /></svg>
      <svg className="absolute top-1/3 right-10 w-4 h-4 text-yellow-400/60 z-0 animate-twinkle delay-1500" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2L15.09 8.26L22 9.27L17 14.14L18.18 21.02L12 17.77L5.82 21.02L7 14.14L2 9.27L8.91 8.26L12 2Z" /></svg>

      <div className="w-full max-w-5xl bg-white rounded-3xl shadow-[0_20px_50px_-12px_rgba(0,0,0,0.08)] overflow-hidden flex flex-col md:flex-row relative z-10 border border-white/50 backdrop-blur-sm">
        
        {/* Left Side: Soft Pastel Area with Branding */}
        <div className="md:w-5/12 p-10 md:p-14 flex flex-col justify-center relative overflow-hidden bg-gradient-to-br from-orange-50 to-rose-50 border-r border-gray-100">
          {/* Decorative soft blobs */}
          <div className="absolute top-0 right-0 w-64 h-64 bg-orange-200/40 rounded-full blur-3xl -translate-y-1/2 translate-x-1/3"></div>
          <div className="absolute bottom-0 left-0 w-48 h-48 bg-rose-200/40 rounded-full blur-3xl translate-y-1/3 -translate-x-1/4"></div>
          
          <div className="relative z-10">
            {/* Cute Cat Icon */}
            <div className="w-16 h-16 bg-white rounded-2xl shadow-sm flex items-center justify-center mb-8 text-orange-500 relative group cursor-pointer hover:shadow-md transition-shadow">
              <svg xmlns="http://www.w3.org/2000/svg" className="w-9 h-9 group-hover:scale-110 group-hover:rotate-6 transition-all duration-300" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M12 5c.67 0 1.35.09 2 .26 1.78-2 5.03-2.84 6.42-2.26 1.4.58-.42 7-.42 7 .57 1.07 1 2.24 1 3.44C21 17.9 16.97 21 12 21s-9-3.1-9-7.56c0-1.25.5-2.4 1-3.44 0 0-1.89-6.42-.5-7 1.39-.58 4.72.23 6.5 2.23A9.04 9.04 0 0 1 12 5Z"/>
                <path d="M8 14v.5"/><path d="M16 14v.5"/><path d="M11.25 16.25h1.5L12 17l-.75-.75Z"/>
              </svg>
              {/* Cute notification dot */}
              <span className="absolute -top-1 -right-1 w-4 h-4 bg-orange-500 rounded-full border-2 border-white animate-pulse"></span>
            </div>
            
            <h1 className="text-3xl md:text-4xl font-bold text-gray-800 mb-4 tracking-tight leading-tight">
              Tham gia<br/>
              <span className="text-orange-500 text-4xl md:text-5xl mt-1 block">MintMark</span>
            </h1>
            <p className="text-gray-600 font-medium text-lg max-w-sm mt-4 leading-relaxed">
              Trở thành một phần của cộng đồng mua sắm thông minh và nhận nhiều ưu đãi.
            </p>
          </div>
        </div>

        {/* Right Side: Register Form Card */}
        <div className="md:w-7/12 bg-white/80 p-10 md:p-14 flex flex-col justify-center">
          <div className="max-w-md w-full mx-auto">
            <h2 className="text-2xl font-bold text-gray-900 mb-2">Tạo tài khoản mới</h2>
            <p className="text-gray-500 mb-8 font-medium text-sm">Điền thông tin của bạn để bắt đầu trải nghiệm.</p>
            
            <RegisterForm />
            
            <div className="mt-6 text-center text-sm font-medium">
              <span className="text-gray-500">Đã có tài khoản? </span>
              <Link to="/login" className="text-orange-500 hover:text-orange-600 transition-colors">
                Đăng nhập ngay
              </Link>
            </div>
          </div>
        </div>
      </div>
    </div>
  );
};
