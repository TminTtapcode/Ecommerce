import React from 'react';
import { Link } from 'react-router-dom';
import type { ProductResponse } from '../../api/types/product.types';

interface ProductCardProps {
  product: ProductResponse;
}

export const ProductCard: React.FC<ProductCardProps> = ({ product }) => {
  const allImages = product.images || product.imageResponses || [];
  const thumbnail = allImages.find(img => img.isThumbnail)
    || allImages[0]
    || { imageUrl: 'https://placehold.co/400x400?text=No+Image' };

  const formatter = new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
  });

  const displayPrice = product.salePrice ?? product.price ?? product.originalPrice ?? 0;
  const originalPrice = product.originalPrice;
  const hasDiscount = originalPrice && displayPrice && originalPrice > displayPrice;

  const mockSoldCount = React.useMemo(() => Math.floor(Math.random() * 500) + 10, [product.id]);

  return (
    <div className="bg-white hover:-translate-y-1 hover:border-orange-500 hover:shadow-[0_4px_12px_rgba(238,77,45,0.15)] transition-all duration-300 border border-transparent rounded-sm overflow-hidden flex flex-col h-full shadow-[0_1px_2px_0_rgba(0,0,0,0.1)] relative">
      <Link to={`/products/${product.id}`} className="relative block aspect-square bg-gray-50 overflow-hidden">
        <img
          src={thumbnail.imageUrl}
          alt={product.name}
          className="w-full h-full object-cover transition-opacity duration-300 hover:opacity-90"
          loading="lazy"
        />
        <div className="absolute top-1.5 -left-1 bg-orange-500 text-white text-[10px] font-medium px-1.5 py-0.5 rounded-r">
          Yêu thích+
        </div>

        {product.stockQuantity <= 0 && (
          <div className="absolute inset-0 bg-black/40 flex items-center justify-center">
            <span className="bg-gray-800/80 text-white text-xs font-medium px-3 py-1 rounded-sm uppercase tracking-wider">
              Hết hàng
            </span>
          </div>
        )}
      </Link>

      <div className="p-2 flex-1 flex flex-col justify-between">
        <Link to={`/products/${product.id}`} className="group">
          <h3 className="text-sm font-normal text-gray-800 line-clamp-2 leading-tight group-hover:text-orange-600 mb-1">
            {product.name}
          </h3>
        </Link>

        <div className="mt-auto pt-2 flex flex-col gap-1">
          <div className="flex items-center justify-between">
            <p className="text-base font-semibold text-orange-500">
              {formatter.format(displayPrice)}
            </p>
            <div className="text-[11px] text-gray-500">
              Đã bán {mockSoldCount >= 1000 ? `${(mockSoldCount/1000).toFixed(1)}k` : mockSoldCount}
            </div>
          </div>
          {hasDiscount && (
            <p className="text-xs text-gray-400 line-through">
              {formatter.format(originalPrice)}
            </p>
          )}
        </div>
      </div>
    </div>
  );
};
