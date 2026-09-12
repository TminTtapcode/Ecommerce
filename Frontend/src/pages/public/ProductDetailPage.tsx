import React, { useEffect, useState } from 'react';
import { useParams, Link, useNavigate } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import type { ProductResponse, ProductVariantResponse } from '../../api/types/product.types';
import { useCart } from '../../contexts/CartContext';
import { useChatContext } from '../../contexts/ChatContext';
import { cartApi } from '../../api/cartApi';
import { ProductReviewSection } from '../../components/reviews/ProductReviewSection';
import { RelatedProductsSection } from '../../components/products/RelatedProductsSection';
import { ShopInfoSection } from '../../components/shop/ShopInfoSection';
import { MessageSquare } from 'lucide-react';

export const ProductDetailPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const [product, setProduct] = useState<ProductResponse | null>(null);
  const [selectedVariant, setSelectedVariant] = useState<ProductVariantResponse | null>(null);
  const [activeImage, setActiveImage] = useState<string>('');
  const [isLoading, setIsLoading] = useState(true);
  const [error, setError] = useState<string | null>(null);
  const { addToCart, fetchCart } = useCart();
  const { openChatWithShop } = useChatContext();
  const [quantity, setQuantity] = useState(1);

  useEffect(() => {
    const fetchProduct = async () => {
      setIsLoading(true);
      setError(null);
      try {
        if (!id) return;
        const response = await productApi.getProductById(parseInt(id, 10));
        const p = response.data.data;
        setProduct(p || null);

        if (p?.variants && p.variants.length > 0) {
          setSelectedVariant(p.variants[0]);
        }

        const allImages = p?.images || p?.imageResponses || [];
        const thumbnail = allImages.find(img => img.isThumbnail) || allImages[0];
        if (thumbnail) {
          setActiveImage(thumbnail.imageUrl);
        } else {
          setActiveImage('https://placehold.co/600x600?text=No+Image');
        }
      } catch (err: any) {
        setError('Không tìm thấy sản phẩm. Có thể sản phẩm đã bị xóa hoặc đường dẫn không chính xác.');
        console.error('Error fetching product:', err);
      } finally {
        setIsLoading(false);
      }
    };

    fetchProduct();
  }, [id]);

  const formatter = new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  });

  const displayPrice = selectedVariant
    ? (selectedVariant.salePrice ?? selectedVariant.price ?? selectedVariant.originalPrice ?? 0)
    : (product?.salePrice ?? product?.price ?? product?.originalPrice ?? 0);
  const displayOriginalPrice = selectedVariant
    ? selectedVariant.originalPrice
    : product?.originalPrice;
  const hasDiscount = displayOriginalPrice && displayPrice && displayOriginalPrice > displayPrice;
  const displayStock = selectedVariant ? selectedVariant.stockQuantity : product?.stockQuantity || 0;

  if (isLoading) {
    return (
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-8 animate-pulse">
        <div className="flex flex-col md:flex-row gap-8">
          <div className="md:w-1/2">
            <div className="bg-gray-200 h-96 w-full rounded-xl mb-4"></div>
            <div className="flex gap-4">
              {[1, 2, 3].map(i => <div key={i} className="bg-gray-200 h-20 w-20 rounded-md"></div>)}
            </div>
          </div>
          <div className="md:w-1/2 space-y-6">
            <div className="h-8 bg-gray-200 rounded w-3/4"></div>
            <div className="h-6 bg-gray-200 rounded w-1/4"></div>
            <div className="h-4 bg-gray-200 rounded w-full"></div>
            <div className="h-4 bg-gray-200 rounded w-5/6"></div>
            <div className="h-10 bg-gray-200 rounded w-1/3 mt-8"></div>
          </div>
        </div>
      </div>
    );
  }

  if (error || !product) {
    return (
      <div className="max-w-3xl mx-auto px-4 py-16 text-center">
        <svg className="mx-auto h-16 w-16 text-red-400 mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <h2 className="text-2xl font-bold text-gray-900 mb-2">{error || 'Không tìm thấy sản phẩm'}</h2>
        <Link to="/products" className="inline-flex items-center px-4 py-2 border border-transparent rounded-md shadow-sm text-sm font-medium text-white bg-orange-600 hover:bg-orange-700 mt-6">
          Quay lại danh sách sản phẩm
        </Link>
      </div>
    );
  }

  return (
    <div className="bg-gray-50 min-h-screen py-8">
      <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">

        <nav className="flex text-sm text-gray-500 mb-8" aria-label="Breadcrumb">
          <ol className="inline-flex items-center space-x-1 md:space-x-3">
            <li className="inline-flex items-center">
              <Link to="/products" className="hover:text-orange-600 transition-colors">Sản phẩm</Link>
            </li>
            {product.categoryName && (
              <li>
                <div className="flex items-center">
                  <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                    <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                  </svg>
                  <span className="ml-1 md:ml-2">{product.categoryName}</span>
                </div>
              </li>
            )}
            <li aria-current="page">
              <div className="flex items-center">
                <svg className="w-4 h-4 mx-1" fill="currentColor" viewBox="0 0 20 20">
                  <path fillRule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clipRule="evenodd" />
                </svg>
                <span className="ml-1 md:ml-2 text-gray-800 line-clamp-1">{product.name}</span>
              </div>
            </li>
          </ol>
        </nav>

        <div className="bg-white rounded-2xl shadow-sm border border-gray-100 overflow-hidden">
          <div className="flex flex-col md:flex-row">

            <div className="md:w-1/2 p-6 border-b md:border-b-0 md:border-r border-gray-100">
              <div className="aspect-w-1 aspect-h-1 rounded-xl overflow-hidden bg-gray-100 mb-4 h-80 sm:h-96 relative">
                <img
                  src={activeImage}
                  alt={product.name}
                  className="w-full h-full object-contain"
                />
              </div>

              {(() => {
                const galleryImages = product.images || product.imageResponses || [];
                return galleryImages.length > 0 ? (
                  <div className="flex gap-3 overflow-x-auto pb-2 custom-scrollbar">
                    {galleryImages.map((img) => (
                      <button
                        key={img.id}
                        onClick={() => setActiveImage(img.imageUrl)}
                        className={`flex-shrink-0 w-20 h-20 rounded-lg overflow-hidden border-2 transition-all ${
                          activeImage === img.imageUrl ? 'border-orange-500' : 'border-transparent hover:border-gray-300'
                        }`}
                      >
                        <img src={img.imageUrl} alt="Thumbnail" className="w-full h-full object-cover" />
                      </button>
                    ))}
                  </div>
                ) : null;
              })()}
            </div>

            <div className="md:w-1/2 p-6 md:p-10 flex flex-col">
              <h1 className="text-2xl sm:text-3xl font-bold text-gray-900 mb-2">{product.name}</h1>

              <div className="flex items-center gap-4 mb-6 pb-6 border-b border-gray-100">
                <span className="text-3xl font-bold text-orange-600">
                  {formatter.format(displayPrice)}
                </span>
                {hasDiscount && (
                  <span className="text-base text-gray-400 line-through">
                    {formatter.format(displayOriginalPrice!)}
                  </span>
                )}
                <span className="text-sm text-gray-500 bg-gray-100 px-3 py-1 rounded-full">
                  Kho: {displayStock}
                </span>
              </div>

              {product.variants && product.variants.length > 0 && (
                <div className="mb-6">
                  <h3 className="text-sm font-medium text-gray-900 mb-3">Tùy chọn sản phẩm</h3>
                  <div className="flex flex-wrap gap-2">
                    {product.variants.map(variant => {
                      const formatAttributes = (attrs?: Record<string, any>) => {
                        if (!attrs || Object.keys(attrs).length === 0) return null;
                        return Object.entries(attrs).map(([k, v]) => `${k}: ${v}`).join(', ');
                      };
                      let variantLabel = formatAttributes(variant.attributes) || variant.name || variant.sku;
                      return (
                        <button
                          key={variant.id}
                          onClick={() => setSelectedVariant(variant)}
                          className={`px-4 py-2 text-sm border rounded-lg transition-colors ${
                            selectedVariant?.id === variant.id
                              ? 'border-orange-500 text-orange-700 bg-orange-50 font-medium shadow-sm'
                              : 'border-gray-300 text-gray-700 hover:border-gray-400'
                          }`}
                        >
                          {variantLabel}
                        </button>
                      );
                    })}
                  </div>
                </div>
              )}

              <div className="mb-8 flex-1">
                <h3 className="text-sm font-medium text-gray-900 mb-3">Mô tả sản phẩm</h3>
                <div className="text-gray-600 text-sm leading-relaxed prose prose-sm max-w-none">
                  {product.description ? (
                    <div dangerouslySetInnerHTML={{ __html: product.description.replace(/\n/g, '<br/>') }} />
                  ) : (
                    <p className="italic text-gray-400">Chưa có mô tả cho sản phẩm này.</p>
                  )}
                </div>
              </div>

              <div className="pt-6 border-t border-gray-100 mt-auto">
                <div className="flex items-center gap-4 mb-4">
                  <span className="text-sm font-medium text-gray-700">Số lượng:</span>
                  <div className="flex items-center border border-gray-300 rounded-md">
                    <button
                      type="button"
                      onClick={() => setQuantity(Math.max(1, quantity - 1))}
                      className="px-3 py-1.5 text-gray-600 hover:bg-gray-100 disabled:opacity-50"
                      disabled={displayStock <= 0}
                    >-</button>
                    <input
                      type="number"
                      value={quantity}
                      readOnly
                      className="w-12 text-center text-sm font-medium border-x border-gray-300 py-1.5 focus:outline-none"
                    />
                    <button
                      type="button"
                      onClick={() => setQuantity(Math.min(displayStock, quantity + 1))}
                      className="px-3 py-1.5 text-gray-600 hover:bg-gray-100 disabled:opacity-50"
                      disabled={displayStock <= 0}
                    >+</button>
                  </div>
                </div>

                <div className="flex gap-4">
                  <button
                    type="button"
                    onClick={() => product.shopId && openChatWithShop(product.shopId)}
                    className="border-2 border-[#ee4d2d] bg-orange-50 hover:bg-orange-100 text-[#ee4d2d] font-semibold py-3 px-6 rounded-xl shadow-xs transition-colors flex items-center justify-center gap-2 cursor-pointer"
                  >
                    <MessageSquare className="w-5 h-5" />
                    Chat ngay
                  </button>
                  <button
                    disabled={displayStock <= 0 || !selectedVariant}
                    onClick={() => selectedVariant && addToCart(selectedVariant.id, quantity)}
                    className="bg-orange-100 hover:bg-orange-200 text-orange-600 border border-orange-500 font-medium py-3 px-6 rounded-xl shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
                  >
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                      <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M3 3h2l.4 2M7 13h10l4-8H5.4M7 13L5.4 5M7 13l-2.293 2.293c-.63.63-.184 1.707.707 1.707H17m0 0a2 2 0 100 4 2 2 0 000-4zm-8 2a2 2 0 11-4 0 2 2 0 014 0z" />
                    </svg>
                    Thêm vào giỏ
                  </button>
                  <button
                    disabled={displayStock <= 0 || !selectedVariant}
                    onClick={async () => {
                      if (!selectedVariant) return;
                      const token = localStorage.getItem('token');
                      if (!token) {
                        alert("Vui lòng đăng nhập để tiếp tục thanh toán");
                        navigate('/login');
                        return;
                      }
                      try {
                        await cartApi.addToCart({ productVariantId: selectedVariant.id, quantity });
                        await fetchCart();
                        const response = await cartApi.getCart();
                        const rawData = response.data.data;
                        if (rawData && rawData.items) {
                          const addedItem = rawData.items.find(
                            (item: any) => (item.productVariantId ?? item.variantId) === selectedVariant.id
                          );
                          if (addedItem) {
                             navigate('/checkout', { state: { selectedItemIds: [addedItem.cartItemId ?? addedItem.id] } });
                             return;
                          }
                        }
                        navigate('/cart');
                      } catch (err: any) {
                        console.error('Failed to buy now', err);
                        alert(err.response?.data?.message || 'Có lỗi xảy ra khi xử lý mua ngay');
                      }
                    }}
                    className="flex-1 bg-orange-500 hover:bg-orange-600 text-white font-medium py-3 px-6 rounded-xl shadow-sm transition-colors disabled:opacity-50 disabled:cursor-not-allowed flex items-center justify-center gap-2 cursor-pointer"
                  >
                    {displayStock > 0 ? 'Mua Ngay' : 'Hết hàng'}
                  </button>
                </div>
              </div>
            </div>

          </div>
        </div>

        {product?.shopId && <ShopInfoSection shopId={product.shopId} />}

        {product && <ProductReviewSection productId={product.id} />}

        {product && <RelatedProductsSection productId={product.id} />}
      </div>
    </div>
  );
};
