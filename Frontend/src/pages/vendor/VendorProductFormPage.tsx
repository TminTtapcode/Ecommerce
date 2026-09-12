import { useContext } from 'react';
import { ShopAccessContext } from '../../contexts/ShopAccessContext';
import { getApiError } from '../../api/apiError';
import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { vendorApi } from '../../api/vendorApi';
import { mediaApi } from '../../api/mediaApi';
import { categoryApi, type CategoryResponse } from '../../api/categoryApi';
import { shopApi } from '../../api/shopApi';
import type { ShopResponse } from '../../api/types/shop.types';
import type {
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductImageRequest,
  ProductVariantRequest
} from '../../api/types/product.types';
import { VendorNav } from '../../components/vendor/VendorNav';

export const VendorProductFormPage: React.FC = () => {
  const { banned } = useContext(ShopAccessContext);
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [globalError, setGlobalError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [shop, setShop] = useState<ShopResponse | null>(null);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState<number>(0);
  const [stockQuantity, setStockQuantity] = useState<number>(0);
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [images, setImages] = useState<ProductImageRequest[]>([]);
  const [variants, setVariants] = useState<ProductVariantRequest[]>([]);

  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    shopApi.getMyShop()
      .then(res => setShop(res.data.data ?? null))
      .catch(() => setGlobalError('Không tìm thấy thông tin Shop của bạn.'));

    const initData = async () => {
      try {
        const catRes = await categoryApi.getAllCategories();
        const loadedCategories = catRes.data.data || [];
        setCategories(loadedCategories);

        if (isEdit && id) {
          const prodRes = await vendorApi.getProductById(Number(id));
          const data = prodRes.data.data;
          if (data) {
            setName(data.name || '');
            setDescription(data.description || '');
            setPrice(data.price ?? data.salePrice ?? data.originalPrice ?? 0);
            setStockQuantity(data.stockQuantity ?? 0);

            if (data.categoryId) {
              setCategoryId(data.categoryId);
            } else if (data.categoryName && loadedCategories.length > 0) {
              const matched = loadedCategories.find(c => c.name === data.categoryName);
              if (matched) setCategoryId(matched.id);
            }

            const rawImages = data.imageResponses || data.images || [];
            setImages(rawImages.map(img => ({
              id: img.id,
              imageUrl: img.imageUrl,
              publicId: img.publicId,
              isThumbnail: img.isThumbnail,
              sortOrder: img.sortOrder
            })));

            if (data.variants && data.variants.length > 0) {
              setVariants(data.variants.map(v => ({
                id: v.id,
                sku: v.sku,
                price: v.price,
                stockQuantity: v.stockQuantity,
                attributes: v.attributes
              })));
            }
          }
        } else if (loadedCategories.length > 0) {
          setCategoryId(loadedCategories[0].id);
        }
      } catch (err: any) {
        setGlobalError(err.response?.data?.message || 'Lỗi tải thông tin sản phẩm.');
      } finally {
        setLoading(false);
      }
    };

    initData();
  }, [id, isEdit]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    const file = e.target.files[0];

    setIsUploading(true);
    setGlobalError('');
    try {
      const response = await mediaApi.uploadImage(file);
      const data = response.data;
      if (data && data.url) {
        const newImage: ProductImageRequest = {
          imageUrl: data.url,
          publicId: data.publicId,
          isThumbnail: images.length === 0,
          sortOrder: images.length
        };
        setImages([...images, newImage]);
      }
    } catch {
      setGlobalError('Tải ảnh lên thất bại. Vui lòng thử lại.');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleRemoveImage = (index: number) => {
    const updated = images.filter((_, i) => i !== index);
    if (images[index].isThumbnail && updated.length > 0) {
      updated[0].isThumbnail = true;
    }
    setImages(updated);
  };

  const handleSetThumbnail = (index: number) => {
    const updated = images.map((img, i) => ({
      ...img,
      isThumbnail: i === index
    }));
    setImages(updated);
  };

  const handleAddVariant = () => {
    const newVariant: ProductVariantRequest = {
      sku: `SKU-${Date.now().toString().slice(-4)}`,
      price: price || 0,
      stockQuantity: 10,
      attributes: { "Phiên bản": "Mặc định" }
    };
    setVariants([...variants, newVariant]);
  };

  const handleRemoveVariant = (index: number) => {
    setVariants(variants.filter((_, i) => i !== index));
  };

  const handleVariantChange = (index: number, field: keyof ProductVariantRequest, value: any) => {
    const updated = [...variants];
    updated[index] = { ...updated[index], [field]: value };
    setVariants(updated);
  };

  const handleVariantAttributeChange = (variantIndex: number, attrKey: string, attrVal: string) => {
    const updated = [...variants];
    updated[variantIndex] = {
      ...updated[variantIndex],
      attributes: {
        ...(updated[variantIndex].attributes || {}),
        [attrKey]: attrVal
      }
    };
    setVariants(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    if (banned) return;
    setSaving(true);
    setGlobalError('');
    setFieldErrors({});

    try {
      if (isEdit) {
        const updateData: ProductUpdateRequest = {
          name,
          description,
          price,
          stockQuantity,
          categoryId,
          images,
          variants
        };
        await vendorApi.updateProduct(Number(id), updateData);
        alert('Cập nhật sản phẩm thành công!');
      } else {
        const createData: ProductCreateRequest = {
          shopId: shop?.id,
          categoryId: categoryId || 1,
          name,
          description,
          price,
          stockQuantity,
          images,
          variants
        };
        await vendorApi.createProduct(createData);
        alert('Đăng sản phẩm thành công!');
      }
      navigate('/vendor/products');
    } catch (err: any) {
      const details = getApiError(err);
      if (details.fieldErrors) {
        setFieldErrors(details.fieldErrors);
        setGlobalError('Vui lòng kiểm tra lại thông tin nhập.');
      } else {
        setGlobalError(details.message);
      }
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return (
      <div className="container mx-auto px-4 py-12 text-center text-gray-500">
        <div className="w-8 h-8 border-4 border-orange-500 border-t-transparent rounded-full animate-spin mx-auto mb-3"></div>
        Đang tải thông tin sản phẩm...
      </div>
    );
  }

  return (
    <div className="container mx-auto px-4 py-6 max-w-5xl">
      <VendorNav />

      <div className="mb-6 flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-gray-900">
            {banned ? 'Chi Tiết Sản Phẩm' : isEdit ? 'Chỉnh Sửa Sản Phẩm' : 'Đăng Sản Phẩm Mới'}
          </h1>
          <p className="text-sm text-gray-500 mt-1">
            {shop ? `Gian hàng: ${shop.name}` : 'Kênh Người Bán'}
          </p>
        </div>
        <button
          onClick={() => navigate('/vendor/products')}
          className="px-4 py-2 text-sm font-medium text-gray-600 hover:text-gray-900 border border-gray-300 rounded-lg hover:bg-gray-50 transition-colors"
        >
          ← Quay lại danh sách
        </button>
      </div>

      {globalError && (
        <div className="mb-6 p-4 bg-red-50 border border-red-200 text-red-700 rounded-lg text-sm">
          {globalError}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6"><fieldset disabled={banned} className="space-y-6">
        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 space-y-4">
          <h2 className="text-lg font-semibold text-gray-900 border-b pb-3">1. Thông tin cơ bản</h2>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tên sản phẩm *</label>
            <input
              type="text"
              required
              value={name}
              onChange={(e) => setName(e.target.value)}
              placeholder="VD: Áo Thun Nam Thể Thao Dri-FIT"
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-sm transition-colors ${
                fieldErrors.name ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {fieldErrors.name && <p className="mt-1 text-xs text-red-500">{fieldErrors.name}</p>}
          </div>

          <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Danh mục sản phẩm *</label>
              <select
                required
                value={categoryId || ''}
                onChange={(e) => setCategoryId(Number(e.target.value))}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-sm bg-white"
              >
                {categories.map((c) => (
                  <option key={c.id} value={c.id}>
                    {c.name}
                  </option>
                ))}
              </select>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Giá bán cơ bản (VND) *</label>
              <input
                type="number"
                min="0"
                required
                value={price}
                onChange={(e) => setPrice(Number(e.target.value))}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-sm ${
                  fieldErrors.price ? 'border-red-500' : 'border-gray-300'
                }`}
              />
              {fieldErrors.price && <p className="mt-1 text-xs text-red-500">{fieldErrors.price}</p>}
            </div>
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Tổng số lượng tồn kho *</label>
            <input
              type="number"
              min="0"
              required
              value={stockQuantity}
              onChange={(e) => setStockQuantity(Number(e.target.value))}
              className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-sm ${
                fieldErrors.stockQuantity ? 'border-red-500' : 'border-gray-300'
              }`}
            />
            {fieldErrors.stockQuantity && <p className="mt-1 text-xs text-red-500">{fieldErrors.stockQuantity}</p>}
          </div>

          <div>
            <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả sản phẩm</label>
            <textarea
              rows={4}
              value={description}
              onChange={(e) => setDescription(e.target.value)}
              placeholder="Chi tiết sản phẩm, công năng, chất liệu, xuất xứ..."
              className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none text-sm"
            />
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 space-y-4">
          <div className="flex items-center justify-between border-b pb-3">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">2. Hình ảnh sản phẩm</h2>
              <p className="text-xs text-gray-500">Tải lên hình ảnh sản phẩm (ảnh đầu tiên hoặc có ngôi sao là ảnh bìa)</p>
            </div>
            <input
              type="file"
              ref={fileInputRef}
              onChange={handleFileChange}
              accept="image/*"
              className="hidden"
            />
            <button
              type="button"
              onClick={() => fileInputRef.current?.click()}
              disabled={isUploading}
              className="px-4 py-2 bg-gray-100 hover:bg-gray-200 text-gray-700 text-sm font-medium rounded-lg transition-colors flex items-center space-x-1"
            >
              {isUploading ? <span>Đang tải lên...</span> : <span>+ Tải ảnh từ máy</span>}
            </button>
          </div>

          <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-6 gap-4">
            {images.map((img, idx) => (
              <div key={idx} className="relative group border rounded-lg overflow-hidden aspect-square bg-gray-50">
                <img src={img.imageUrl} alt="" className="w-full h-full object-cover" />

                <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-between p-2">
                  <div className="flex justify-end">
                    <button
                      type="button"
                      onClick={() => handleRemoveImage(idx)}
                      className="p-1 bg-red-600 text-white rounded-full hover:bg-red-700"
                      title="Xóa ảnh"
                    >
                      <svg className="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M6 18L18 6M6 6l12 12"/></svg>
                    </button>
                  </div>
                  <button
                    type="button"
                    onClick={() => handleSetThumbnail(idx)}
                    className={`text-xs py-1 px-2 rounded font-medium text-center ${
                      img.isThumbnail ? 'bg-orange-500 text-white' : 'bg-white/80 text-gray-800 hover:bg-white'
                    }`}
                  >
                    {img.isThumbnail ? '★ Ảnh bìa' : 'Đặt làm bìa'}
                  </button>
                </div>

                {img.isThumbnail && (
                  <span className="absolute top-1 left-1 bg-orange-500 text-white text-[10px] px-1.5 py-0.5 rounded font-bold shadow">
                    Ảnh bìa
                  </span>
                )}
              </div>
            ))}
            {images.length === 0 && (
              <div
                onClick={() => fileInputRef.current?.click()}
                className="border-2 border-dashed border-gray-300 rounded-lg aspect-square flex flex-col items-center justify-center text-gray-400 hover:border-orange-500 hover:text-orange-500 cursor-pointer transition-colors p-4 text-center"
              >
                <svg className="w-8 h-8 mb-1" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/></svg>
                <span className="text-xs">Thêm ảnh</span>
              </div>
            )}
          </div>
        </div>

        <div className="bg-white p-6 rounded-lg shadow-sm border border-gray-200 space-y-4">
          <div className="flex items-center justify-between border-b pb-3">
            <div>
              <h2 className="text-lg font-semibold text-gray-900">3. Phân loại / Biến thể sản phẩm</h2>
              <p className="text-xs text-gray-500">Thêm các phân loại như Màu sắc, Kích thước hoặc Phiên bản</p>
            </div>
            <button
              type="button"
              onClick={handleAddVariant}
              className="px-3.5 py-1.5 bg-orange-50 text-orange-600 hover:bg-orange-100 text-xs font-semibold rounded-lg transition-colors border border-orange-200"
            >
              + Thêm phân loại
            </button>
          </div>

          {variants.length === 0 ? (
            <p className="text-sm text-gray-400 italic py-2">
              Sản phẩm chưa có biến thể riêng (sẽ sử dụng giá và kho hàng mặc định ở trên).
            </p>
          ) : (
            <div className="space-y-3">
              {variants.map((v, vIdx) => (
                <div key={vIdx} className="p-4 bg-gray-50 border border-gray-200 rounded-lg flex flex-col md:flex-row gap-3 items-start md:items-center">
                  <div className="w-full md:w-36">
                    <label className="block text-xs font-medium text-gray-600 mb-1">Mã SKU</label>
                    <input
                      type="text"
                      required
                      value={v.sku}
                      onChange={(e) => handleVariantChange(vIdx, 'sku', e.target.value)}
                      className="w-full px-2.5 py-1.5 text-xs border rounded bg-white"
                    />
                  </div>

                  <div className="w-full md:w-40">
                    <label className="block text-xs font-medium text-gray-600 mb-1">Giá biến thể (VND)</label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={v.price}
                      onChange={(e) => handleVariantChange(vIdx, 'price', Number(e.target.value))}
                      className="w-full px-2.5 py-1.5 text-xs border rounded bg-white"
                    />
                  </div>

                  <div className="w-full md:w-28">
                    <label className="block text-xs font-medium text-gray-600 mb-1">Số lượng kho</label>
                    <input
                      type="number"
                      min="0"
                      required
                      value={v.stockQuantity}
                      onChange={(e) => handleVariantChange(vIdx, 'stockQuantity', Number(e.target.value))}
                      className="w-full px-2.5 py-1.5 text-xs border rounded bg-white"
                    />
                  </div>

                  <div className="flex-1 w-full">
                    <label className="block text-xs font-medium text-gray-600 mb-1">Tên phân loại (Màu sắc / Size)</label>
                    <input
                      type="text"
                      placeholder="VD: Màu Đen, Size XL"
                      value={v.attributes ? Object.values(v.attributes).join(', ') : ''}
                      onChange={(e) => handleVariantAttributeChange(vIdx, 'Phân loại', e.target.value)}
                      className="w-full px-2.5 py-1.5 text-xs border rounded bg-white"
                    />
                  </div>

                  <div className="md:mt-5 self-end md:self-center">
                    <button
                      type="button"
                      onClick={() => handleRemoveVariant(vIdx)}
                      className="text-red-500 hover:text-red-700 p-1.5 text-xs font-medium rounded hover:bg-red-50"
                    >
                      Xóa
                    </button>
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex justify-end space-x-3 pt-4">
          <button
            type="button"
            onClick={() => navigate('/vendor/products')}
            className="px-5 py-2.5 border border-gray-300 rounded-lg text-sm font-medium text-gray-700 hover:bg-gray-50 transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-6 py-2.5 bg-orange-500 hover:bg-orange-600 text-white text-sm font-semibold rounded-lg shadow-sm transition-colors disabled:opacity-50 flex items-center space-x-2"
          >
            {saving ? (
              <>
                <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin"></div>
                <span>Đang lưu...</span>
              </>
            ) : (
              <span>{isEdit ? 'Lưu Thay Đổi' : 'Đăng Sản Phẩm'}</span>
            )}
          </button>
        </div>
      </fieldset></form>
    </div>
  );
};
