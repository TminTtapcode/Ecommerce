import React, { useEffect, useRef, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import { categoryApi, type CategoryResponse } from '../../api/categoryApi';
import type { ProductResponse } from '../../api/types/product.types';
import { ProductCard } from '../../components/products/ProductCard';
import { Pagination } from '../../components/common/Pagination';

const SORT_OPTIONS = [
  { value: 'id_desc',    label: 'Mới nhất' },
  { value: 'price_asc',  label: 'Giá tăng dần' },
  { value: 'price_desc', label: 'Giá giảm dần' },
];

const CATEGORY_ICONS: Record<string, string> = {
  'Thời Trang Nam': '👔', 'Điện Thoại': '📱', 'Thiết Bị Điện Tử': '💻',
  'Mẹ & Bé': '👶', 'Nhà Cửa & Đời Sống': '🏠', 'Sức Khỏe': '💊',
  'Giày Dép Nữ': '👠', 'Đồng Hồ': '⌚', 'Thể Thao': '⚽',
  'Ô Tô & Xe Máy': '🚗', 'Sách': '📚', 'Làm Đẹp': '💄',
};

export const ProductListPage: React.FC = () => {
  const [searchParams, setSearchParams] = useSearchParams();

  const page       = parseInt(searchParams.get('page')       || '0',  10);
  const size       = parseInt(searchParams.get('size')       || '12', 10);
  const keyword    = searchParams.get('keyword')    || '';
  const categoryId = searchParams.get('categoryId') ? Number(searchParams.get('categoryId')) : undefined;
  const sortBy     = searchParams.get('sortBy')     || 'id_desc';
  const minPriceRaw = searchParams.get('minPrice');
  const maxPriceRaw = searchParams.get('maxPrice');
  const minPrice   = minPriceRaw ? Number(minPriceRaw) : undefined;
  const maxPrice   = maxPriceRaw ? Number(maxPriceRaw) : undefined;

  const [products,   setProducts]   = useState<ProductResponse[]>([]);
  const [totalPages, setTotalPages] = useState(0);
  const [isLoading,  setIsLoading]  = useState(true);
  const [error,      setError]      = useState<string | null>(null);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);

  const [minInput, setMinInput] = useState(minPriceRaw || '');
  const [maxInput, setMaxInput] = useState(maxPriceRaw || '');
  const [priceError, setPriceError] = useState<string | null>(null);

  const fetchRevision = useRef(0);

  useEffect(() => {
    categoryApi.getAllCategories()
      .then(res => { if (res.data.data) setCategories(res.data.data); })
      .catch(() => {});
  }, []);

  const isRecommendationMode = !keyword && !categoryId && !minPrice && !maxPrice && (!sortBy || sortBy === 'id_desc');

  useEffect(() => {
    const rev = ++fetchRevision.current;
    setIsLoading(true);
    setError(null);

    const handleFetch = async () => {
      try {
        let res;
        if (isRecommendationMode) {
          try {
            res = await productApi.getRecommendations(size);
          } catch (recErr) {
            console.warn('Recommendation API failed, falling back to standard product listing:', recErr);
            res = await productApi.getProducts({ page, size, sortBy: 'id_desc' });
          }
        } else {
          res = await productApi.getProducts({ page, size, keyword, categoryId, minPrice, maxPrice, sortBy });
        }

        if (rev !== fetchRevision.current) return;
        if (res.data.data) {
          setProducts(res.data.data.content);
          setTotalPages(res.data.data.totalPages);
        }
      } catch {
        if (rev !== fetchRevision.current) return;
        setError('Không thể tải dữ liệu. Vui lòng thử lại sau.');
      } finally {
        if (rev === fetchRevision.current) setIsLoading(false);
      }
    };

    handleFetch();
  }, [page, size, keyword, categoryId, minPrice, maxPrice, sortBy, isRecommendationMode]);

  const updateParams = (updates: Record<string, string | undefined>) => {
    const next = new URLSearchParams(searchParams);
    Object.entries(updates).forEach(([k, v]) => {
      if (v === undefined || v === '') next.delete(k);
      else next.set(k, v);
    });
    next.delete('page');
    setSearchParams(next);
  };

  const handlePageChange = (newPage: number) => {
    const next = new URLSearchParams(searchParams);
    next.set('page', newPage.toString());
    setSearchParams(next);
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const handleCategoryClick = (id: number) => {
    if (categoryId === id) {
      updateParams({ categoryId: undefined });
    } else {
      updateParams({ categoryId: id.toString() });
    }
  };

  const handleSortChange = (value: string) => {
    updateParams({ sortBy: value === 'id_desc' ? undefined : value });
  };

  const applyPriceFilter = () => {
    const mn = minInput !== '' ? Number(minInput) : undefined;
    const mx = maxInput !== '' ? Number(maxInput) : undefined;
    if (mn !== undefined && mx !== undefined && mn > mx) {
      setPriceError('Giá tối thiểu không được lớn hơn tối đa');
      return;
    }
    setPriceError(null);
    updateParams({
      minPrice: mn !== undefined ? mn.toString() : undefined,
      maxPrice: mx !== undefined ? mx.toString() : undefined,
    });
  };

  const clearAllFilters = () => {
    setMinInput(''); setMaxInput(''); setPriceError(null);
    setSearchParams(new URLSearchParams({ size: size.toString() }));
  };

  const hasActiveFilters = !!(categoryId || minPrice || maxPrice || (sortBy && sortBy !== 'id_desc') || keyword);

  return (
    <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-6">

      <div className="flex gap-2 mb-6 h-48">
        <div className="w-2/3 bg-orange-100 rounded-sm overflow-hidden relative">
          <div className="absolute inset-0 bg-gradient-to-r from-orange-400 to-rose-400 flex flex-col justify-center px-10">
            <h2 className="text-white text-4xl font-bold mb-2">SIÊU SALE GIỮA THÁNG</h2>
            <p className="text-white/90 text-lg">Giảm đến 50% toàn sàn</p>
          </div>
        </div>
        <div className="w-1/3 flex flex-col gap-2">
          <div className="h-1/2 bg-blue-100 rounded-sm overflow-hidden relative">
            <div className="absolute inset-0 bg-gradient-to-r from-blue-400 to-indigo-400 flex flex-col justify-center px-6">
              <h3 className="text-white text-xl font-bold">FREESHIP 0Đ</h3>
            </div>
          </div>
          <div className="h-1/2 bg-green-100 rounded-sm overflow-hidden relative">
            <div className="absolute inset-0 bg-gradient-to-r from-green-400 to-emerald-400 flex flex-col justify-center px-6">
              <h3 className="text-white text-xl font-bold">HOÀN XU XTRA</h3>
            </div>
          </div>
        </div>
      </div>

      <div className="bg-white rounded-sm shadow-sm mb-6 p-4">
        <div className="flex items-center justify-between mb-3">
          <span className="uppercase text-gray-500 font-medium text-sm">Danh Mục</span>
          {categoryId && (
            <button
              id="clear-category-btn"
              onClick={() => updateParams({ categoryId: undefined })}
              className="text-xs text-orange-500 hover:underline"
            >
              Bỏ chọn
            </button>
          )}
        </div>
        <div className="flex flex-wrap gap-2">
          {categories.map(cat => {
            const icon = CATEGORY_ICONS[cat.name] || '🏷️';
            const active = categoryId === cat.id;
            return (
              <button
                key={cat.id}
                id={`category-btn-${cat.id}`}
                onClick={() => handleCategoryClick(cat.id)}
                className={`flex items-center gap-1.5 px-3 py-1.5 rounded-full text-sm border transition-all
                  ${active
                    ? 'border-orange-500 bg-orange-50 text-orange-600 font-medium'
                    : 'border-gray-200 hover:border-orange-300 hover:bg-orange-50 text-gray-700'}`}
              >
                <span>{icon}</span>
                <span>{cat.name}</span>
              </button>
            );
          })}
        </div>
      </div>

      <div className="flex gap-4">

        <aside className="hidden lg:block w-56 shrink-0">
          <div className="bg-white rounded-sm shadow-sm p-4 sticky top-[90px]">
            <div className="flex items-center justify-between mb-4">
              <span className="font-medium text-gray-800">Bộ lọc</span>
              {hasActiveFilters && (
                <button
                  id="clear-all-filters-btn"
                  onClick={clearAllFilters}
                  className="text-xs text-orange-500 hover:underline"
                >
                  Xóa tất cả
                </button>
              )}
            </div>

            <div className="mb-5">
              <p className="text-sm font-medium text-gray-600 mb-2">Sắp xếp</p>
              <div className="space-y-1.5">
                {SORT_OPTIONS.map(opt => (
                  <label key={opt.value} className="flex items-center gap-2 cursor-pointer group">
                    <input
                      type="radio"
                      id={`sort-${opt.value}`}
                      name="sortBy"
                      value={opt.value}
                      checked={sortBy === opt.value}
                      onChange={() => handleSortChange(opt.value)}
                      className="accent-orange-500"
                    />
                    <span className="text-sm text-gray-700 group-hover:text-orange-600">{opt.label}</span>
                  </label>
                ))}
              </div>
            </div>

            <div>
              <p className="text-sm font-medium text-gray-600 mb-2">Khoảng giá (đ)</p>
              <div className="flex gap-2 mb-2">
                <input
                  id="min-price-input"
                  type="number"
                  min={0}
                  placeholder="Từ"
                  value={minInput}
                  onChange={e => { setMinInput(e.target.value); setPriceError(null); }}
                  className="w-full border border-gray-200 rounded px-2 py-1.5 text-sm focus:outline-none focus:border-orange-400"
                />
                <input
                  id="max-price-input"
                  type="number"
                  min={0}
                  placeholder="Đến"
                  value={maxInput}
                  onChange={e => { setMaxInput(e.target.value); setPriceError(null); }}
                  className="w-full border border-gray-200 rounded px-2 py-1.5 text-sm focus:outline-none focus:border-orange-400"
                />
              </div>
              {priceError && <p className="text-xs text-red-500 mb-2">{priceError}</p>}
              <button
                id="apply-price-filter-btn"
                onClick={applyPriceFilter}
                className="w-full bg-orange-500 hover:bg-orange-600 text-white text-sm py-1.5 rounded transition-colors"
              >
                Áp dụng
              </button>
              {(minPrice || maxPrice) && (
                <button
                  id="clear-price-filter-btn"
                  onClick={() => {
                    setMinInput(''); setMaxInput(''); setPriceError(null);
                    updateParams({ minPrice: undefined, maxPrice: undefined });
                  }}
                  className="w-full mt-1 text-xs text-gray-500 hover:text-orange-500"
                >
                  Xóa bộ lọc giá
                </button>
              )}
            </div>
          </div>
        </aside>

        <div className="flex-1 min-w-0">

          <div className="flex items-center justify-between mb-4 sticky top-[85px] bg-[#F5F5F5] z-10 py-2">
            <div className="bg-white px-4 py-3 border-b-4 border-orange-500 rounded-sm shadow-sm">
              <h2 className="text-orange-500 uppercase font-medium">
                {categoryId
                  ? (categories.find(c => c.id === categoryId)?.name || 'Danh mục')
                  : keyword ? `Kết quả cho "${keyword}"` : 'Gợi Ý Hôm Nay'}
              </h2>
            </div>
            <select
              id="mobile-sort-select"
              value={sortBy}
              onChange={e => handleSortChange(e.target.value)}
              className="lg:hidden border border-gray-200 rounded px-2 py-1.5 text-sm bg-white"
            >
              {SORT_OPTIONS.map(o => <option key={o.value} value={o.value}>{o.label}</option>)}
            </select>
          </div>

          {hasActiveFilters && (
            <div className="flex flex-wrap gap-2 mb-3">
              {categoryId && (
                <span className="inline-flex items-center gap-1 bg-orange-50 border border-orange-200 text-orange-700 text-xs px-2 py-1 rounded-full">
                  📂 {categories.find(c => c.id === categoryId)?.name || `Danh mục #${categoryId}`}
                  <button onClick={() => updateParams({ categoryId: undefined })} className="ml-1 hover:text-orange-900">×</button>
                </span>
              )}
              {(minPrice || maxPrice) && (
                <span className="inline-flex items-center gap-1 bg-orange-50 border border-orange-200 text-orange-700 text-xs px-2 py-1 rounded-full">
                  💰 {minPrice ? `${minPrice.toLocaleString('vi')}đ` : '0đ'} – {maxPrice ? `${maxPrice.toLocaleString('vi')}đ` : '∞'}
                  <button onClick={() => { setMinInput(''); setMaxInput(''); updateParams({ minPrice: undefined, maxPrice: undefined }); }} className="ml-1 hover:text-orange-900">×</button>
                </span>
              )}
              {sortBy && sortBy !== 'id_desc' && (
                <span className="inline-flex items-center gap-1 bg-orange-50 border border-orange-200 text-orange-700 text-xs px-2 py-1 rounded-full">
                  ↕ {SORT_OPTIONS.find(o => o.value === sortBy)?.label}
                  <button onClick={() => handleSortChange('id_desc')} className="ml-1 hover:text-orange-900">×</button>
                </span>
              )}
            </div>
          )}

          {error && (
            <div className="bg-red-50 border-l-4 border-red-500 p-4 mb-6 rounded-r-lg">
              <p className="text-sm text-red-700">{error}</p>
              <button
                id="retry-btn"
                onClick={() => { setError(null); setIsLoading(true); fetchRevision.current++; }}
                className="text-xs text-red-600 underline mt-1"
              >
                Thử lại
              </button>
            </div>
          )}

          {isLoading ? (
            <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2">
              {Array.from({ length: 10 }).map((_, i) => (
                <div key={i} className="bg-white border border-gray-100 rounded-sm overflow-hidden h-72 flex flex-col animate-pulse">
                  <div className="bg-gray-200 aspect-square w-full" />
                  <div className="p-2 flex flex-col flex-1">
                    <div className="h-3 bg-gray-200 rounded w-full mb-2" />
                    <div className="h-3 bg-gray-200 rounded w-2/3 mb-4" />
                    <div className="mt-auto h-4 bg-gray-200 rounded w-1/2" />
                  </div>
                </div>
              ))}
            </div>
          ) : products.length === 0 ? (

            <div className="text-center py-16 bg-white rounded-sm">
              <svg className="mx-auto h-12 w-12 text-gray-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
              </svg>
              <h3 className="text-lg font-medium text-gray-900 mb-1">Không tìm thấy sản phẩm nào</h3>
              <p className="text-gray-500 text-sm mb-4">
                {hasActiveFilters
                  ? 'Không có sản phẩm phù hợp với bộ lọc hiện tại.'
                  : 'Hiện chưa có sản phẩm nào trong hệ thống.'}
              </p>
              {hasActiveFilters && (
                <button
                  id="clear-filters-empty-btn"
                  onClick={clearAllFilters}
                  className="text-sm text-orange-500 border border-orange-300 px-4 py-2 rounded hover:bg-orange-50"
                >
                  Xóa bộ lọc
                </button>
              )}
            </div>
          ) : (

            <>
              <div className="grid grid-cols-2 sm:grid-cols-3 md:grid-cols-4 lg:grid-cols-5 gap-2">
                {products.map(product => (
                  <ProductCard key={product.id} product={product} />
                ))}
              </div>
              <div className="mt-8 flex justify-center pb-8">
                {!isRecommendationMode && (
                  <Pagination
                    currentPage={page}
                    totalPages={totalPages}
                    onPageChange={handlePageChange}
                  />
                )}
              </div>
            </>
          )}
        </div>
      </div>
    </div>
  );
};
