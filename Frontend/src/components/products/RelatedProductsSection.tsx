import { useCallback, useEffect, useRef, useState } from 'react';
import { getApiError } from '../../api/apiError';
import { productApi } from '../../api/productApi';
import type { ProductResponse } from '../../api/types/product.types';
import { ProductCard } from './ProductCard';

interface RelatedProductsSectionProps {
  productId: number;
}

export const RelatedProductsSection = ({ productId }: RelatedProductsSectionProps) => {
  const [products, setProducts] = useState<ProductResponse[]>([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const requestRevision = useRef(0);

  const load = useCallback(async () => {
    const revision = ++requestRevision.current;
    setLoading(true);
    setError(null);
    try {
      const response = await productApi.getRelatedProducts(productId);
      if (revision !== requestRevision.current) return;
      setProducts((response.data.data ?? []).filter(product => product.id !== productId));
    } catch (err) {
      if (revision !== requestRevision.current) return;
      setProducts([]);
      setError(getApiError(err, 'Không thể tải sản phẩm liên quan.').message);
    } finally {
      if (revision === requestRevision.current) setLoading(false);
    }
  }, [productId]);

  useEffect(() => {
    void load();
    return () => { requestRevision.current += 1; };
  }, [load]);

  return (
    <section className="mt-10 border-t border-gray-200 pt-8" aria-labelledby="related-products-heading">
      <div className="mb-5 flex flex-wrap items-center justify-between gap-3">
        <div>
          <h2 id="related-products-heading" className="text-xl font-bold text-gray-900">Sản phẩm liên quan</h2>
          <p className="mt-1 text-sm text-gray-500">Các sản phẩm cùng danh mục đang được hiển thị.</p>
        </div>
      </div>

      {loading && (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4" aria-label="Đang tải sản phẩm liên quan">
          {Array.from({ length: 4 }, (_, index) => <div key={index} className="aspect-[3/4] animate-pulse rounded bg-gray-200" />)}
        </div>
      )}

      {!loading && error && (
        <div role="alert" className="rounded-lg border border-amber-200 bg-amber-50 p-4 text-sm text-amber-900">
          <p>{error}</p>
          <button type="button" onClick={() => void load()} className="mt-2 font-semibold underline">Thử lại</button>
        </div>
      )}

      {!loading && !error && products.length === 0 && (
        <p className="rounded-lg border border-dashed border-gray-300 bg-white p-5 text-sm text-gray-500">
          Chưa có sản phẩm liên quan trong danh mục này.
        </p>
      )}

      {!loading && !error && products.length > 0 && (
        <div className="grid grid-cols-2 gap-3 sm:grid-cols-3 lg:grid-cols-4">
          {products.map(product => <ProductCard key={product.id} product={product} />)}
        </div>
      )}
    </section>
  );
};
