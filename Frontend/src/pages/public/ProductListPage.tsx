import React, { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import type { ProductResponse } from '../../api/types/product.types';
import { ProductCard } from '../../components/products/ProductCard';
import { Pagination } from '../../components/common/Pagination';

export const ProductListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '12', 10);
  const keyword = searchParams.get('keyword') || '';

  useEffect(() => {
    const fetchProducts = async () => {
      setIsLoading(true);
      setError(null);
      try {
        const response = await productApi.getProducts(page, size, keyword);
        setProducts(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
      } catch (err: any) {
        setError('Không thể tải danh sách sản phẩm. Vui lòng thử lại sau.');
        console.error('Error fetching products:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProducts();
  }, [page, size, keyword]);

  const handlePageChange = (newPage: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', newPage.toString());
    setSearchParams(newParams);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const categories = [
    { name: 'Thời Trang Nam', icon: '👔' },
    { name: 'Điện Thoại', icon: '📱' },
    { name: 'Thiết Bị Điện Tử', icon: '💻' },
    { name: 'Mẹ & Bé', icon: '👶' },
    { name: 'Nhà Cửa & Đời Sống', icon: '🏠' },
    { name: 'Sức Khỏe', icon: '💊' },
    { name: 'Giày Dép Nữ', icon: '👠' },
    { name: 'Đồng Hồ', icon: '⌚' },
    { name: 'Thể Thao', icon: '⚽' },
    { name: 'Ô Tô & Xe Máy', icon: '🚗' },
  ];

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">
      
      {/* Banner Section (Mock) */}
      <div className="flex gap-2 mb-6 h-64">
        <div className="w-2/3 bg-orange-100 rounded-sm overflow-hidden relative group cursor-pointer">
          <div className="absolute inset-0 bg-gradient-to-r from-orange-400 to-rose-400 flex flex-col justify-center px-10">
            <h2 className="text-white text-4xl font-bold mb-2">SIÊU SALE GIỮA THÁNG</h2>
            <p className="text-white/90 text-lg">Giảm đến 50% toàn sàn</p>
          </div>
        </div>
        <div className="w-1/3 flex flex-col gap-2">
          <div className="h-1/2 bg-blue-100 rounded-sm overflow-hidden relative cursor-pointer">
             <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-indigo-400 flex flex-col justify-center px-6">
              <h3 className="text-white text-xl font-bold">FREESHIP 0Đ</h3>
            </div>
          </div>
          <div className="h-1/2 bg-green-100 rounded-sm overflow-hidden relative cursor-pointer">
            <div className="absolute inset-0 bg-gradient-to-r from-green-400 to-emerald-400 flex flex-col justify-center px-6">
              <h3 className="text-white text-xl font-bold">HOÀN XU XTRA</h3>
            </div>
          </div>
        </div>
      </div>

      {/* Categories Section (Mock) */}
      <div className="bg-white rounded-sm shadow-sm mb-6 p-4">
        <div className="uppercase text-gray-500 font-medium mb-4">Danh Mục</div>
        <div className="grid grid-cols-5 md:grid-cols-10 gap-2 text-center">
          {categories.map((cat, i) => (
            <div key={i} className="flex flex-col items-center gap-2 cursor-pointer hover:shadow-md transition-shadow p-2 rounded border border-transparent hover:border-gray-100">
              <div className="w-14 h-14 bg-gray-50 rounded-full flex items-center justify-center text-2xl border border-gray-100">
                {cat.icon}
              </div>
              <span className="text-xs text-gray-700 leading-tight">{cat.name}</span>
            </div>
          ))}
        </div>
      </div>

      {/* Main Product Feed */}
      <div className="bg-transparent">
        <div className="flex items-center justify-between mb-4 sticky top-[85px] bg-[#F5F5F5] z-10 py-2">
          <div className="bg-white px-6 py-4 border-b-4 border-orange-500 rounded-sm shadow-sm">
            <h2 className="text-orange-500 uppercase font-medium text-lg">Gợi Ý Hôm Nay</h2>
          </div>
        </div>

        {/* Error State */}
        {error && (
          <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg">
            <div className="flex">
              <div className="flex-shrink-0">
                <svg className="h-5 w-5 text-red-400" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clipRule="evenodd" />
                </svg>
              </div>
              <div className="ml-3">
                <p className="text-sm text-red-700">{error}</p>
              </div>
            </div>
          </div>
        )}

        {/* Loading State (Skeletons) */}
        {isLoading ? (
          <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2">
            {Array.from({ length: 12 }).map((_, i) => (
              <div key={i} className="bg-white border border-gray-100 rounded-sm overflow-hidden h-72 flex flex-col animate-pulse">
                <div className="bg-gray-200 aspect-square w-full"></div>
                <div className="p-2 flex flex-col flex-1">
                  <div className="h-3 bg-gray-200 rounded w-full mb-2"></div>
                  <div className="h-3 bg-gray-200 rounded w-2/3 mb-4"></div>
                  <div className="mt-auto h-4 bg-gray-200 rounded w-1/2"></div>
                </div>
              </div>
            ))}
          </div>
        ) : (
          <>
            {/* Empty State */}
            {!isLoading && products.length === 0 ? (
              <div className="text-center py-16 bg-white rounded-sm">
                <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                </svg>
                <h3 className="text-lg font-medium text-gray-900 mb-1">Không tìm thấy sản phẩm nào</h3>
                <p className="text-gray-500">
                  {keyword ? `Không có kết quả phù hợp cho "${keyword}". Hãy thử từ khóa khác.` : 'Hiện chưa có sản phẩm nào trong hệ thống.'}
                </p>
              </div>
            ) : (
              /* Product Grid */
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-6 gap-2">
                {products.map(product => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
            )}

            {/* Pagination */}
            <div className="mt-8 flex justify-center pb-8">
              <Pagination 
                currentPage={page} 
                totalPages={totalPages} 
                onPageChange={handlePageChange} 
              />
            </div>
          </>
        )}
      </div>
    </div>
  );
};
