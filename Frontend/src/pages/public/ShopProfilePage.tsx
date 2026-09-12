import React, { useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { shopApi } from '../../api/shopApi';
import { productApi } from '../../api/productApi';
import type { ShopResponse } from '../../api/types/shop.types';
import type { ProductResponse } from '../../api/types/product.types';
import { Store, Package } from 'lucide-react';
import { ProductCard } from '../../components/products/ProductCard';

export const ShopProfilePage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const shopId = id ? parseInt(id, 10) : 0;

  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [loadingShop, setLoadingShop] = useState(true);
  const [loadingProducts, setLoadingProducts] = useState(true);
  const [error, setError] = useState<string | null>(null);

  const [page, setPage] = useState(0);
  const [totalPages, setTotalPages] = useState(0);
  const size = 12;

  useEffect(() => {
    if (!shopId) return;

    const fetchShopInfo = async () => {
      setLoadingShop(true);
      setError(null);
      try {
        const response = await shopApi.getShopPublicInfo(shopId);
        setShop(response.data.data || null);
      } catch (err: any) {
        console.error('Lỗi khi tải thông tin shop:', err);
        setError('Không thể tải thông tin shop. Có thể shop không tồn tại.');
      } finally {
        setLoadingShop(false);
      }
    };

    fetchShopInfo();
  }, [shopId]);

  useEffect(() => {
    if (!shopId) return;

    const fetchProducts = async () => {
      setLoadingProducts(true);
      try {
        const response = await productApi.getProducts({ shopId, page, size });
        setProducts(response.data.data?.content || []);
        setTotalPages(response.data.data?.totalPages || 0);
      } catch (err) {
        console.error('Lỗi khi tải sản phẩm của shop:', err);
      } finally {
        setLoadingProducts(false);
      }
    };

    fetchProducts();
  }, [shopId, page]);

  if (error) {
    return (
      <div className="min-h-[50vh] flex flex-col items-center justify-center">
        <Store className="w-16 h-16 text-gray-300 mb-4" />
        <h2 className="text-xl font-medium text-gray-600">{error}</h2>
      </div>
    );
  }

  if (loadingShop) {
    return (
      <div className="bg-gray-50 min-h-screen py-8 animate-pulse">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="bg-white rounded-2xl shadow-sm p-6 mb-8 h-40"></div>
          <div className="grid grid-cols-2 md:grid-cols-4 gap-6">
            {[1, 2, 3, 4].map(i => (
              <div key={i} className="bg-white rounded-xl h-80 shadow-sm"></div>
            ))}
          </div>
        </div>
      </div>
    );
  }

  if (!shop) return null;

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-8 mb-8">
          <div className="flex flex-col md:flex-row items-center md:items-start gap-8">
            <div className="w-24 h-24 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center shrink-0">
              <Store className="w-12 h-12" />
            </div>

            <div className="flex-1 text-center md:text-left">
              <h1 className="text-3xl font-bold text-gray-900 mb-2">{shop.name}</h1>

              <div className="grid grid-cols-1 sm:grid-cols-2 gap-y-3 gap-x-8 mt-6">
                <div className="flex items-center gap-2 text-gray-600 sm:col-span-2">
                  <span>{shop.description || 'Chưa có mô tả'}</span>
                </div>
                <div className="flex items-center gap-2 text-gray-600">
                  <Package className="w-5 h-5 text-gray-400" />
                  <span>Sản phẩm: {loadingProducts ? '...' : products.length}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <div>
          <div className="flex items-center gap-2 mb-6">
            <h2 className="text-xl font-bold text-gray-900">Sản phẩm của Shop</h2>
          </div>

          {loadingProducts ? (
            <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6 animate-pulse">
              {[1, 2, 3, 4].map(i => (
                <div key={i} className="bg-white rounded-xl h-80 shadow-sm border border-gray-100"></div>
              ))}
            </div>
          ) : products.length > 0 ? (
            <>
              <div className="grid grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-6">
                {products.map(product => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>

              {totalPages > 1 && (
                <div className="flex justify-center mt-12 gap-2">
                  <button
                    onClick={() => setPage(p => Math.max(0, p - 1))}
                    disabled={page === 0}
                    className="px-4 py-2 border rounded-md disabled:opacity-50"
                  >
                    Trước
                  </button>
                  <span className="px-4 py-2">Trang {page + 1} / {totalPages}</span>
                  <button
                    onClick={() => setPage(p => Math.min(totalPages - 1, p + 1))}
                    disabled={page >= totalPages - 1}
                    className="px-4 py-2 border rounded-md disabled:opacity-50"
                  >
                    Sau
                  </button>
                </div>
              )}
            </>
          ) : (
            <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-12 text-center">
              <Package className="w-16 h-16 text-gray-300 mx-auto mb-4" />
              <h3 className="text-lg font-medium text-gray-900 mb-2">Chưa có sản phẩm</h3>
              <p className="text-gray-500">Shop này hiện tại chưa đăng sản phẩm nào.</p>
            </div>
          )}
        </div>

      </div>
    </div>
  );
};
