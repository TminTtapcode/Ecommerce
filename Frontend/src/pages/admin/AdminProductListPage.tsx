import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import type { ProductResponse } from '../../api/types/product.types';
import { Pagination } from '../../components/common/Pagination';

export const AdminProductListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '10', 10);
  const keyword = searchParams.get('keyword') || '';

  const [searchInput, setSearchInput] = useState(keyword);

  const fetchProducts = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await productApi.getProducts({ page, size, keyword });
      if (response.data.data) {
        setProducts(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
      }
    } catch (err: any) {
      setError('Lỗi tải danh sách sản phẩm.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();

  }, [page, size, keyword]);

  const handleSearch = (e: React.FormEvent) => {
    e.preventDefault();
    const newParams = new URLSearchParams(searchParams);
    if (searchInput.trim()) {
      newParams.set('keyword', searchInput.trim());
    } else {
      newParams.delete('keyword');
    }
    newParams.set('page', '0');
    setSearchParams(newParams);
  };

  const handlePageChange = (newPage: number) => {
    const newParams = new URLSearchParams(searchParams);
    newParams.set('page', newPage.toString());
    setSearchParams(newParams);
  };

  const handleDelete = async (id: number) => {
    if (window.confirm('Bạn có chắc chắn muốn xóa sản phẩm này? Hành động này không thể hoàn tác.')) {
      try {
        await productApi.deleteProduct(id);
        fetchProducts();
      } catch (err: any) {
        alert(err.response?.data?.message || 'Có lỗi xảy ra khi xóa sản phẩm');
      }
    }
  };

  const formatter = new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  });

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-200 p-6 text-gray-900">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <div>
          <h1 className="text-2xl font-bold bg-gradient-to-r from-orange-500 to-amber-500 bg-clip-text text-transparent flex items-center gap-2">
            🛍️ Quản Lý Sản Phẩm
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            Theo dõi và kiểm duyệt toàn bộ danh mục sản phẩm trên sàn
          </p>
        </div>
        <Link
          to="/admin/products/create"
          className="bg-gradient-to-r from-orange-500 to-orange-600 hover:from-orange-600 hover:to-orange-700 text-white px-4 py-2.5 rounded-xl font-medium text-sm transition-all shadow-sm flex items-center gap-2"
        >
          <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 4v16m8-8H4" />
          </svg>
          Thêm sản phẩm mới
        </Link>
      </div>

      <div className="mb-6 flex flex-col sm:flex-row gap-4 justify-between">
        <form onSubmit={handleSearch} className="w-full sm:w-96 flex">
          <input
            type="text"
            placeholder="Tìm kiếm theo tên sản phẩm..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="flex-1 px-4 py-2.5 bg-white border border-gray-300 rounded-l-xl text-gray-900 text-sm focus:border-orange-500 focus:outline-none placeholder-gray-400"
          />
          <button
            type="submit"
            className="bg-orange-500 hover:bg-orange-600 border border-orange-500 px-5 py-2.5 rounded-r-xl text-white text-sm font-semibold transition-colors cursor-pointer"
          >
            Tìm
          </button>
        </form>
      </div>

      {error && (
        <div className="bg-rose-50 border border-rose-200 text-rose-700 p-4 mb-4 rounded-xl text-sm flex items-center gap-2">
          <span>⚠️</span>
          <span>{error}</span>
        </div>
      )}

      <div className="overflow-x-auto rounded-xl border border-gray-200">
        <table className="min-w-full divide-y divide-gray-200 text-left">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Sản phẩm
              </th>
              <th scope="col" className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Danh mục
              </th>
              <th scope="col" className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Giá (Thấp nhất)
              </th>
              <th scope="col" className="px-5 py-3.5 text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Tồn kho
              </th>
              <th scope="col" className="px-5 py-3.5 text-right text-xs font-semibold text-gray-600 uppercase tracking-wider">
                Thao tác
              </th>
            </tr>
          </thead>
          <tbody className="divide-y divide-gray-100 bg-white">
            {isLoading ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-gray-400">
                  <div className="inline-block animate-spin rounded-full h-8 w-8 border-b-2 border-orange-500 mb-2"></div>
                  <div>Đang tải dữ liệu sản phẩm...</div>
                </td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-12 text-center text-gray-400 bg-gray-50">
                  <div className="text-4xl mb-2">📦</div>
                  <p className="font-medium text-gray-700">Không tìm thấy sản phẩm nào.</p>
                </td>
              </tr>
            ) : (
              products.map((product) => {
                const thumbnail = product.imageResponses?.find(img => img.isThumbnail) || product.imageResponses?.[0];
                return (
                  <tr key={product.id} className="hover:bg-gray-50/80 transition-colors">
                    <td className="px-5 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-12 w-12 bg-gray-100 rounded-lg border border-gray-200 overflow-hidden">
                          {thumbnail ? (
                            <img className="h-12 w-12 object-cover" src={thumbnail.imageUrl} alt="" />
                          ) : (
                            <div className="h-12 w-12 flex items-center justify-center text-xs text-gray-400">No Img</div>
                          )}
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-semibold text-gray-900 truncate max-w-[220px]" title={product.name}>
                            {product.name}
                          </div>
                          <div className="text-xs text-gray-500">ID: #{product.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap">
                      <span className="px-2.5 py-1 inline-flex text-xs leading-5 font-semibold rounded-full bg-orange-50 text-orange-700 border border-orange-200">
                        {product.categoryName || 'Chưa phân loại'}
                      </span>
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-sm font-bold text-emerald-600">
                      {formatter.format(product.salePrice ?? product.price ?? product.originalPrice ?? 0)}
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-sm text-gray-700">
                      <span className="font-medium">{product.stockQuantity}</span>
                    </td>
                    <td className="px-5 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <div className="flex items-center justify-end space-x-2">
                        <Link
                          to={`/admin/products/${product.id}/edit`}
                          className="px-3 py-1.5 rounded-lg bg-orange-50 text-orange-600 hover:bg-orange-100 border border-orange-200 text-xs font-semibold transition-colors"
                        >
                          Sửa
                        </Link>
                        <button
                          onClick={() => handleDelete(product.id)}
                          className="px-3 py-1.5 rounded-lg bg-rose-50 text-rose-600 hover:bg-rose-100 border border-rose-200 text-xs font-semibold transition-colors cursor-pointer"
                        >
                          Xóa
                        </button>
                      </div>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {!isLoading && products.length > 0 && (
        <div className="mt-6 flex justify-center">
          <Pagination currentPage={page} totalPages={totalPages} onPageChange={handlePageChange} />
        </div>
      )}
    </div>
  );
};
