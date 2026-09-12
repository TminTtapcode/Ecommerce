import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { shopApi } from '../../api/shopApi';
import type { ShopResponse } from '../../api/types/shop.types';
import { Store } from 'lucide-react';

interface ShopInfoSectionProps {
  shopId: number;
}

export const ShopInfoSection: React.FC<ShopInfoSectionProps> = ({ shopId }) => {
  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchShop = async () => {
      try {
        const response = await shopApi.getShopPublicInfo(shopId);
        setShop(response.data.data || null);
      } catch (error) {
        console.error('Failed to fetch shop info', error);
      } finally {
        setLoading(false);
      }
    };
    fetchShop();
  }, [shopId]);

  if (loading) {
    return (
      <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mt-8 animate-pulse">
        <div className="h-6 w-32 bg-gray-200 rounded mb-4"></div>
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 bg-gray-200 rounded-full"></div>
          <div className="space-y-2 flex-1">
            <div className="h-5 w-48 bg-gray-200 rounded"></div>
            <div className="h-4 w-32 bg-gray-200 rounded"></div>
          </div>
        </div>
      </div>
    );
  }

  if (!shop) return null;

  return (
    <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 mt-8">
      <div className="flex flex-col sm:flex-row items-start sm:items-center justify-between gap-4">
        <div className="flex items-center gap-4">
          <div className="w-16 h-16 rounded-full bg-orange-100 text-orange-600 flex items-center justify-center shrink-0">
            <Store className="w-8 h-8" />
          </div>
          <div>
            <h3 className="text-lg font-bold text-gray-900 line-clamp-1">{shop.name}</h3>
            <div className="flex flex-wrap items-center gap-x-4 gap-y-1 text-sm text-gray-500 mt-1">
              <span className="flex items-center gap-1">
                {shop.description || 'Chưa có mô tả'}
              </span>
            </div>
          </div>
        </div>

        <div className="flex gap-3 w-full sm:w-auto">
          <Link
            to={`/shops/${shop.id}`}
            className="flex-1 sm:flex-none px-6 py-2 border border-orange-500 text-orange-600 hover:bg-orange-50 font-medium rounded-lg transition-colors text-center"
          >
            Xem Shop
          </Link>
        </div>
      </div>
    </div>
  );
};
