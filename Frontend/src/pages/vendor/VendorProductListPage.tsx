import { useContext } from 'react';
import { ShopAccessContext } from '../../contexts/ShopAccessContext';
import React, { useEffect, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { vendorApi } from '../../api/vendorApi';
import type { ProductResponse } from '../../api/types/product.types';
import { Pagination } from '../../components/common/Pagination';
import { VendorNav } from '../../components/vendor/VendorNav';

export const VendorProductListPage: React.FC = () => {
  const { banned } = useContext(ShopAccessContext);
  const [searchParams, setSearchParams] = useSearchParams();
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [totalElements, setTotalElements] = useState(0);
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const [deletingId, setDeletingId] = useState<number | null>(null);

  const page = parseInt(searchParams.get('page') || '0', 10);
  const size = parseInt(searchParams.get('size') || '10', 10);
  const keyword = searchParams.get('keyword') || '';

  const [searchInput, setSearchInput] = useState(keyword);

  const fetchVendorProducts = async () => {
    setIsLoading(true);
    setError(null);
    try {
      const response = await vendorApi.getProducts(page, size, keyword);
      if (response.data.data) {
        setProducts(response.data.data.content);
        setTotalPages(response.data.data.totalPages);
        setTotalElements(response.data.data.totalElements || response.data.data.content.length);
      }
    } catch (err: any) {
      setError(err.response?.data?.message || 'Lỗi tải danh sách sản phẩm của Shop.');
      console.error(err);
    } finally {
      setIsLoading(false);
    }
  };

  useEffect(() => {
    fetchVendorProducts();

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
    if (!window.confirm('Bạn có chắc chắn muốn ẩn sản phẩm này khỏi gian hàng?')) {
      return;
    }

    setDeletingId(id);
    try {
      await vendorApi.deleteProduct(id);
      alert('Đã ẩn sản phẩm thành công!');
      fetchVendorProducts();
    } catch (err: any) {
      alert(err.response?.data?.message || 'Có lỗi xảy ra khi ẩn sản phẩm.');
    } finally {
      setDeletingId(null);
    }
  };

  const formatCurrency = (val?: number) => {
    if (val === undefined || val === null) return '0 ₫';
    return new Intl.NumberFormat('vi-VN', { style: 'currency', currency: 'VND' }).format(val);
  };

  return (
    <div className="container mx-auto px-4 py-6 max-w-7xl">
      <VendorNav />

      <div className="bg-white rounded-lg shadow-sm border border-gray-200 overflow-hidden">
        <div className="p-6 border-b border-gray-200 flex flex-col md:flex-row md:items-center md:justify-between gap-4">
          <div>
            <h2 className="text-xl font-bold text-gray-900">Danh Sách Sản Phẩm</h2>
            <p className="text-sm text-gray-500 mt-0.5">
              Tổng cộng có <span className="font-semibold text-gray-900">{totalElements}</span> sản phẩm trong gian hàng
            </p>
          </div>

          <div className="flex items-center space-x-3">
            <form onSubmit={handleSearch} className="flex items-center">
              <div className="relative w-72">
                <input
                  type="text"
                  placeholder="Tìm kiếm sản phẩm..."
                  value={searchInput}
                  onChange={(e) => setSearchInput(e.target.value)}
                  className="w-full pl-9 pr-4 py-2 text-sm border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-orange-500 focus:border-orange-500"
                />
                <svg
                  className="w-4 h-4 text-gray-400 absolute left-3 top-3"
                  fill="none"
                  stroke="currentColor"
                  viewBox="0 0 24 24"
                >
                  <path
                    strokeLinecap="round"
                    strokeLinejoin="round"
                    strokeWidth="2"
                    d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z"
                  />
                </svg>
              </div>
              <button
                type="submit"
                className="ml-2 px-3.5 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-medium rounded-lg transition-colors"
              >
                Tìm
              </button>
            </form>
          </div>
        </div>

        {isLoading ? (
          <div className="p-12 text-center text-gray-500 flex flex-col items-center justify-center">
            <div className="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mb-3"></div>
            <p>Đang tải danh sách sản phẩm...</p>
          </div>
        ) : error ? (
          <div className="p-8 text-center text-red-500">
            <p>{error}</p>
            <button
              onClick={fetchVendorProducts}
              className="mt-3 px-4 py-2 bg-orange-50 text-orange-600 rounded-lg text-sm font-medium hover:bg-orange-100"
            >
              Thử lại
            </button>
          </div>
        ) : products.length === 0 ? (
          <div className="p-12 text-center">
            <div className="text-4xl mb-3">🏷️</div>
            <h3 className="text-base font-semibold text-gray-900 mb-1">Gian hàng chưa có sản phẩm</h3>
            <p className="text-sm text-gray-500 mb-4">
              Bắt đầu đăng sản phẩm đầu tiên để tiếp cận hàng triệu khách hàng ngay hôm nay.
            </p>
            <Link
              to={banned ? "/vendor/orders" : "/vendor/products/new"} aria-disabled={banned} style={banned ? { display: "none" } : undefined}
              className="inline-flex items-center px-4 py-2 bg-orange-500 hover:bg-orange-600 text-white text-sm font-medium rounded-lg shadow-sm"
            >
              + Đăng Sản Phẩm Mới
            </Link>
          </div>
        ) : (
          <div className="overflow-x-auto">
            <table className="w-full text-left border-collapse text-sm">
              <thead>
                <tr className="bg-gray-50 border-b border-gray-200 text-xs font-semibold text-gray-500 uppercase tracking-wider">
                  <th className="py-3 px-4">Sản phẩm</th>
                  <th className="py-3 px-4">Danh mục</th>
                  <th className="py-3 px-4">Giá bán</th>
                  <th className="py-3 px-4">Kho</th>
                  <th className="py-3 px-4">Biến thể</th>
                  <th className="py-3 px-4">Trạng thái</th>
                  <th className="py-3 px-4 text-right">Thao tác</th>
                </tr>
              </thead>
              <tbody className="divide-y divide-gray-100">
                {products.map((product) => {
                  const thumbnail =
                    product.thumbnailUrl ||
                    product.imageResponses?.find((img) => img.isThumbnail)?.imageUrl ||
                    product.imageResponses?.[0]?.imageUrl ||
                    'https://placehold.co/100x100?text=No+Image';

                  return (
                    <tr key={product.id} className="hover:bg-gray-50/80 transition-colors">
                      <td className="py-3.5 px-4 flex items-center space-x-3 max-w-xs">
                        <img
                          src={thumbnail}
                          alt={product.name}
                          className="w-12 h-12 object-cover rounded-md border border-gray-200 shrink-0"
                          onError={(e) => {
                            (e.target as HTMLImageElement).src = 'https://placehold.co/100x100?text=SP';
                          }}
                        />
                        <div className="truncate">
                          <Link
                            to={`/products/${product.id}`}
                            target="_blank"
                            className="font-medium text-gray-900 hover:text-orange-600 line-clamp-1"
                            title={product.name}
                          >
                            {product.name}
                          </Link>
                          <span className="text-xs text-gray-400">ID: #{product.id}</span>
                        </div>
                      </td>

                      <td className="py-3.5 px-4 text-gray-600">
                        {product.categoryName || 'Mặc định'}
                      </td>

                      <td className="py-3.5 px-4 font-semibold text-orange-600">
                        {formatCurrency(product.price ?? product.salePrice ?? product.originalPrice)}
                      </td>

                      <td className="py-3.5 px-4 text-gray-700 font-medium">
                        {product.stockQuantity}
                      </td>

                      <td className="py-3.5 px-4 text-gray-500">
                        {product.variants && product.variants.length > 0 ? (
                          <span className="inline-flex items-center px-2 py-0.5 rounded text-xs font-medium bg-blue-50 text-blue-700">
                            {product.variants.length} phân loại
                          </span>
                        ) : (
                          <span className="text-xs text-gray-400">Mặc định</span>
                        )}
                      </td>

                      <td className="py-3.5 px-4">
                        <span
                          className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${
                            product.status === 'ACTIVE'
                              ? 'bg-green-100 text-green-800'
                              : 'bg-gray-100 text-gray-800'
                          }`}
                        >
                          {product.status === 'ACTIVE' ? 'Đang bán' : 'Đã ẩn'}
                        </span>
                      </td>

                      <td className="py-3.5 px-4 text-right space-x-2 whitespace-nowrap">
                        <Link
                          to={`/vendor/products/${product.id}/edit`}
                          className="inline-flex items-center px-2.5 py-1.5 border border-gray-300 rounded text-xs font-medium text-gray-700 bg-white hover:bg-gray-50 transition-colors"
                        >
                          {banned ? 'Xem chi tiết' : '✏️ Sửa'}
                        </Link>
                        <Link
                          to={`/products/${product.id}#reviews`}
                          target="_blank"
                          className="inline-flex items-center px-2.5 py-1.5 border border-blue-200 rounded text-xs font-medium text-blue-600 bg-blue-50 hover:bg-blue-100 transition-colors"
                        >
                          ⭐ Xem đánh giá
                        </Link>
                        <button
                          onClick={() => handleDelete(product.id)}
                          disabled={banned || deletingId === product.id}

                          className="inline-flex items-center px-2.5 py-1.5 border border-red-200 rounded text-xs font-medium text-red-600 bg-red-50 hover:bg-red-100 transition-colors disabled:opacity-50"
                        >
                          {deletingId === product.id ? '...' : 'Ẩn'}
                        </button>
                      </td>
                    </tr>
                  );
                })}
              </tbody>
            </table>
          </div>
        )}

        {totalPages > 1 && (
          <div className="p-4 border-t border-gray-200 flex justify-center">
            <Pagination
              currentPage={page}
              totalPages={totalPages}
              onPageChange={handlePageChange}
            />
          </div>
        )}
      </div>
    </div>
  );
};
