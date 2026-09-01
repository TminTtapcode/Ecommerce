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
      const response = await productApi.getProducts(page, size, keyword);
      setProducts(response.data.data.content);
      setTotalPages(response.data.data.totalPages);
    } catch (err: any) {
      setError('Lỗi tải danh sách sản phẩm.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchProducts();
    // eslint-disable-next-line react-hooks/exhaustive-deps
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
        // Reload page
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
    <div className="bg-white shadow rounded-lg p-6">
      <div className="flex flex-col sm:flex-row justify-between items-start sm:items-center mb-6 gap-4">
        <h1 className="text-2xl font-bold text-gray-900">Quản lý Sản phẩm</h1>
        <Link 
          to="/admin/products/create" 
          className="bg-orange-500 hover:bg-orange-600 text-white px-4 py-2 rounded-md font-medium text-sm transition-colors flex items-center gap-2"
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
            placeholder="Tìm kiếm theo tên..."
            value={searchInput}
            onChange={(e) => setSearchInput(e.target.value)}
            className="flex-1 px-4 py-2 border border-gray-300 rounded-l-md focus:ring-orange-500 focus:border-orange-500 sm:text-sm"
          />
          <button type="submit" className="bg-gray-100 hover:bg-gray-200 border border-l-0 border-gray-300 px-4 py-2 rounded-r-md text-gray-700">
            Tìm
          </button>
        </form>
      </div>

      {error && (
        <div className="bg-red-50 text-red-500 p-4 mb-4 rounded-md">
          {error}
        </div>
      )}

      <div className="overflow-x-auto border border-gray-200 rounded-md">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Sản phẩm
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Danh mục
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Giá (Thấp nhất)
              </th>
              <th scope="col" className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Tồn kho
              </th>
              <th scope="col" className="relative px-6 py-3">
                <span className="sr-only">Thao tác</span>
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {isLoading ? (
              <tr>
                <td colSpan={5} className="px-6 py-10 text-center text-gray-500">
                  Đang tải dữ liệu...
                </td>
              </tr>
            ) : products.length === 0 ? (
              <tr>
                <td colSpan={5} className="px-6 py-10 text-center text-gray-500">
                  Không tìm thấy sản phẩm nào.
                </td>
              </tr>
            ) : (
              products.map((product) => {
                const thumbnail = product.imageResponses?.find(img => img.isThumbnail) || product.imageResponses?.[0];
                return (
                  <tr key={product.id} className="hover:bg-gray-50">
                    <td className="px-6 py-4 whitespace-nowrap">
                      <div className="flex items-center">
                        <div className="flex-shrink-0 h-12 w-12 bg-gray-100 rounded overflow-hidden">
                          {thumbnail ? (
                            <img className="h-12 w-12 object-cover" src={thumbnail.imageUrl} alt="" />
                          ) : (
                            <div className="h-12 w-12 bg-gray-200 flex items-center justify-center text-xs text-gray-400">No Img</div>
                          )}
                        </div>
                        <div className="ml-4">
                          <div className="text-sm font-medium text-gray-900 truncate max-w-[200px]" title={product.name}>
                            {product.name}
                          </div>
                          <div className="text-sm text-gray-500">ID: {product.id}</div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap">
                      <span className="px-2 inline-flex text-xs leading-5 font-semibold rounded-full bg-blue-100 text-blue-800">
                        {product.categoryName || 'Chưa phân loại'}
                      </span>
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {formatter.format(product.price)}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-sm text-gray-900">
                      {product.stockQuantity}
                    </td>
                    <td className="px-6 py-4 whitespace-nowrap text-right text-sm font-medium">
                      <Link to={`/admin/products/${product.id}/edit`} className="text-indigo-600 hover:text-indigo-900 mr-4">
                        Sửa
                      </Link>
                      <button onClick={() => handleDelete(product.id)} className="text-red-600 hover:text-red-900">
                        Xóa
                      </button>
                    </td>
                  </tr>
                );
              })
            )}
          </tbody>
        </table>
      </div>

      {!isLoading && products.length > 0 && (
        <Pagination currentPage={page} totalPages={totalPages} onPageChange={handlePageChange} />
      )}
    </div>
  );
};
