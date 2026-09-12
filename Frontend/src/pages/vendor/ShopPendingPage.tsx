import React from 'react';
import { Link } from 'react-router-dom';

export const ShopPendingPage: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50 flex flex-col items-center justify-center py-12 px-4 sm:px-6 lg:px-8">
      <div className="max-w-md w-full space-y-8 bg-white p-10 rounded-2xl shadow-sm border border-gray-100 text-center">
        <div className="mx-auto flex items-center justify-center h-20 w-20 rounded-full bg-yellow-100">
          <svg className="h-10 w-10 text-yellow-600" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
        </div>
        <div>
          <h2 className="mt-4 text-3xl font-extrabold text-gray-900">
            Đang Chờ Duyệt
          </h2>
          <p className="mt-3 text-base text-gray-500">
            Hồ sơ đăng ký Kênh Người Bán của bạn đã được gửi đi và đang chờ Quản trị viên phê duyệt.
            Quá trình này có thể mất từ 1-2 ngày làm việc.
          </p>
        </div>
        <div className="pt-4">
          <Link
            to="/"
            className="inline-flex justify-center w-full px-4 py-3 border border-gray-300 shadow-sm text-sm font-medium rounded-lg text-gray-700 bg-white hover:bg-gray-50 focus:outline-none focus:ring-2 focus:ring-offset-2 focus:ring-orange-500 transition-colors"
          >
            Quay Về Trang Chủ
          </Link>
        </div>
      </div>
    </div>
  );
};
