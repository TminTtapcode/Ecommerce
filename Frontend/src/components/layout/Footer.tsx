import React from 'react';
import { Link } from 'react-router-dom';

export const Footer: React.FC = () => {
  return (
    <footer className="bg-gray-50 border-t-4 border-orange-500 pt-10 pb-6 mt-10">
      <div className="container mx-auto px-4 max-w-7xl">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-8 mb-8">
          <div>
            <h3 className="font-bold text-gray-800 mb-4 uppercase text-sm">Chăm sóc khách hàng</h3>
            <ul className="text-sm text-gray-500 space-y-2">
              <li><Link to="#" className="hover:text-orange-500">Trung tâm trợ giúp</Link></li>
              <li><Link to="#" className="hover:text-orange-500">MintMark Blog</Link></li>
              <li><Link to="#" className="hover:text-orange-500">Hướng dẫn mua hàng</Link></li>
              <li><Link to="#" className="hover:text-orange-500">Chính sách bảo hành</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="font-bold text-gray-800 mb-4 uppercase text-sm">Về MintMark</h3>
            <ul className="text-sm text-gray-500 space-y-2">
              <li><Link to="#" className="hover:text-orange-500">Giới thiệu về MintMark</Link></li>
              <li><Link to="#" className="hover:text-orange-500">Tuyển dụng</Link></li>
              <li><Link to="#" className="hover:text-orange-500">Điều khoản MintMark</Link></li>
              <li><Link to="#" className="hover:text-orange-500">Chính sách bảo mật</Link></li>
            </ul>
          </div>
          <div>
            <h3 className="font-bold text-gray-800 mb-4 uppercase text-sm">Thanh toán</h3>
            <div className="flex flex-wrap gap-2">
              <div className="w-12 h-8 bg-white border border-gray-200 rounded flex items-center justify-center text-xs font-bold text-blue-600">VISA</div>
              <div className="w-12 h-8 bg-white border border-gray-200 rounded flex items-center justify-center text-xs font-bold text-red-500">MC</div>
              <div className="w-12 h-8 bg-white border border-gray-200 rounded flex items-center justify-center text-xs font-bold text-green-500">JCB</div>
            </div>
          </div>
          <div>
            <h3 className="font-bold text-gray-800 mb-4 uppercase text-sm">Theo dõi chúng tôi trên</h3>
            <ul className="text-sm text-gray-500 space-y-2">
              <li><Link to="#" className="hover:text-orange-500 flex items-center gap-2">
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M22 12c0-5.52-4.48-10-10-10S2 6.48 2 12c0 4.84 3.44 8.87 8 9.8V15H8v-3h2V9.5C10 7.57 11.57 6 13.5 6H16v3h-2c-.55 0-1 .45-1 1v2h3v3h-3v6.95c5.05-.5 9-4.76 9-9.95z"/></svg>
                Facebook
              </Link></li>
              <li><Link to="#" className="hover:text-orange-500 flex items-center gap-2">
                <svg className="w-4 h-4" fill="currentColor" viewBox="0 0 24 24"><path d="M12 2c5.514 0 10 4.486 10 10s-4.486 10-10 10-10-4.486-10-10 4.486-10 10-10zm0-2c-6.627 0-12 5.373-12 12s5.373 12 12 12 12-5.373 12-12-5.373-12-12-12zm-2 8c0 1.312-1.027 2.453-2.348 2.593-.687.073-1.353-.162-1.85-.609-.496-.448-.771-1.096-.771-1.782 0-1.401 1.139-2.54 2.54-2.54 1.401 0 2.541 1.139 2.541 2.541l-.112-.203zm5 2.593c-1.32-.14-2.348-1.281-2.348-2.593 0-1.401 1.139-2.54 2.54-2.54 1.401 0 2.541 1.139 2.541 2.541 0 .686-.275 1.334-.771 1.782-.497.447-1.163.682-1.85.609l-.112.201zm-2.5 1.407c-3.148 0-5.836 1.956-6.845 4.743h13.69c-1.009-2.787-3.697-4.743-6.845-4.743z"/></svg>
                Instagram
              </Link></li>
            </ul>
          </div>
        </div>

        <div className="border-t border-gray-200 pt-6 flex flex-col md:flex-row justify-between items-center text-xs text-gray-500">
          <p>© 2026 MintMark. Tất cả các quyền được bảo lưu.</p>
          <div className="flex space-x-4 mt-4 md:mt-0">
            <span>Quốc gia & Vùng lãnh thổ:</span>
            <span className="text-gray-600 font-medium">Việt Nam</span>
            <span>Singapore</span>
            <span>Thái Lan</span>
          </div>
        </div>
      </div>
    </footer>
  );
};
