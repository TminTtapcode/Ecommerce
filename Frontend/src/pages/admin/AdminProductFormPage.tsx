import { getApiError } from '../../api/apiError';
import React, { useEffect, useState, useRef } from 'react';
import { useParams, useNavigate } from 'react-router-dom';
import { productApi } from '../../api/productApi';
import { mediaApi } from '../../api/mediaApi';
import { categoryApi, type CategoryResponse } from '../../api/categoryApi';
import type {
  ProductCreateRequest,
  ProductUpdateRequest,
  ProductImageRequest,
  ProductVariantRequest
} from '../../api/types/product.types';

export const AdminProductFormPage: React.FC = () => {
  const { id } = useParams<{ id: string }>();
  const isEdit = !!id;
  const navigate = useNavigate();

  const [loading, setLoading] = useState(isEdit);
  const [saving, setSaving] = useState(false);
  const [globalError, setGlobalError] = useState('');
  const [fieldErrors, setFieldErrors] = useState<Record<string, string>>({});

  const [name, setName] = useState('');
  const [description, setDescription] = useState('');
  const [price, setPrice] = useState(0);
  const [stockQuantity, setStockQuantity] = useState(0);
  const [categoryId, setCategoryId] = useState<number | undefined>(undefined);
  const [categories, setCategories] = useState<CategoryResponse[]>([]);
  const [images, setImages] = useState<ProductImageRequest[]>([]);
  const [variants, setVariants] = useState<ProductVariantRequest[]>([]);

  const [isUploading, setIsUploading] = useState(false);
  const fileInputRef = useRef<HTMLInputElement>(null);

  useEffect(() => {
    const initData = async () => {
      try {
        const catRes = await categoryApi.getAllCategories();
        const loadedCategories = catRes.data.data || [];
        setCategories(loadedCategories);

        if (isEdit && id) {
          const response = await productApi.getProductById(Number(id));
          const data = response.data.data;
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
        setGlobalError(err.response?.data?.message || 'Lỗi tải dữ liệu sản phẩm.');
      } finally {
        setLoading(false);
      }
    };

    initData();
  }, [id, isEdit]);

  const handleFileChange = async (e: React.ChangeEvent<HTMLInputElement>) => {
    if (!e.target.files || e.target.files.length === 0) return;
    setIsUploading(true);
    const files = Array.from(e.target.files);

    try {
      const newImages: ProductImageRequest[] = [];
      for (const file of files) {
        const uploadRes = await mediaApi.uploadImage(file);
        newImages.push({
          imageUrl: uploadRes.data.url,
          publicId: uploadRes.data.publicId,
          isThumbnail: false,
          sortOrder: images.length + newImages.length,
        });
      }
      if (images.length === 0 && newImages.length > 0) {
        newImages[0].isThumbnail = true;
      }
      setImages([...images, ...newImages]);
    } catch (error) {
      alert('Tải ảnh thất bại. Vui lòng thử lại.');
    } finally {
      setIsUploading(false);
      if (fileInputRef.current) fileInputRef.current.value = '';
    }
  };

  const handleSetThumbnail = (index: number) => {
    setImages(images.map((img, i) => ({ ...img, isThumbnail: i === index })));
  };

  const handleRemoveImage = (index: number) => {
    const updated = images.filter((_, i) => i !== index);
    if (images[index].isThumbnail && updated.length > 0) {
      updated[0].isThumbnail = true;
    }
    setImages(updated.map((img, i) => ({ ...img, sortOrder: i })));
  };

  const handleAddVariant = () => {
    setVariants([...variants, { sku: '', price: 0, stockQuantity: 0, attributes: {} }]);
  };

  const handleRemoveVariant = (index: number) => {
    setVariants(variants.filter((_, i) => i !== index));
  };

  const handleVariantChange = (index: number, field: keyof ProductVariantRequest, value: any) => {
    const updated = [...variants];
    updated[index] = { ...updated[index], [field]: value };
    setVariants(updated);
  };

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setSaving(true);
    setGlobalError('');
    setFieldErrors({});

    try {
      if (isEdit) {
        const updateData: ProductUpdateRequest = {
          name, description, price, stockQuantity, categoryId, images, variants
        };
        await productApi.updateProduct(Number(id), updateData);
        alert('Cập nhật sản phẩm thành công!');
      } else {
        const createData: ProductCreateRequest = {
          shopId: 1,
          categoryId: categoryId || 1,
          name, description, price, stockQuantity, images, variants
        };
        await productApi.createProduct(createData);
        alert('Tạo sản phẩm thành công!');
      }
      navigate('/admin/products');
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

  if (loading) return <div className="p-8 text-center text-gray-500">Đang tải...</div>;

  return (
    <div className="max-w-5xl mx-auto pb-12">
      <div className="mb-6 flex items-center justify-between">
        <h1 className="text-2xl font-bold text-gray-900">
          {isEdit ? 'Chỉnh sửa sản phẩm' : 'Thêm sản phẩm mới'}
        </h1>
        <button
          onClick={() => navigate('/admin/products')}
          className="text-gray-500 hover:text-gray-700"
        >
          Quay lại
        </button>
      </div>

      {globalError && (
        <div className="bg-red-50 text-red-600 p-4 rounded-md mb-6 border border-red-200">
          {globalError}
        </div>
      )}

      <form onSubmit={handleSubmit} className="space-y-6">
        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <h2 className="text-lg font-semibold text-gray-900 mb-4 border-b pb-2">Thông tin cơ bản</h2>

          <div className="space-y-4">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Tên sản phẩm *</label>
              <input
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors ${fieldErrors.name ? 'border-red-500' : 'border-gray-300'}`}
              />
              {fieldErrors.name && <p className="mt-1 text-sm text-red-500">{fieldErrors.name}</p>}
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Mô tả</label>
              <textarea
                rows={4}
                value={description}
                onChange={(e) => setDescription(e.target.value)}
                className="w-full px-4 py-2 border border-gray-300 rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors"
              />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Giá cơ bản *</label>
                <input
                  type="number"
                  min="0"
                  required
                  value={price}
                  onChange={(e) => setPrice(Number(e.target.value))}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors ${fieldErrors.price ? 'border-red-500' : 'border-gray-300'}`}
                />
                {fieldErrors.price && <p className="mt-1 text-sm text-red-500">{fieldErrors.price}</p>}
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Số lượng tồn kho *</label>
                <input
                  type="number"
                  min="0"
                  required
                  value={stockQuantity}
                  onChange={(e) => setStockQuantity(Number(e.target.value))}
                  className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors ${fieldErrors.stockQuantity ? 'border-red-500' : 'border-gray-300'}`}
                />
                {fieldErrors.stockQuantity && <p className="mt-1 text-sm text-red-500">{fieldErrors.stockQuantity}</p>}
              </div>
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">Danh mục ngành hàng *</label>
              <select
                required
                value={categoryId || ''}
                onChange={(e) => setCategoryId(Number(e.target.value))}
                className={`w-full px-4 py-2 border rounded-lg focus:ring-2 focus:ring-orange-500 focus:border-orange-500 outline-none transition-colors bg-white ${fieldErrors.categoryId ? 'border-red-500' : 'border-gray-300'}`}
              >
                {categories.length === 0 && <option value="">Đang tải danh mục...</option>}
                {categories.map((cat) => (
                  <option key={cat.id} value={cat.id}>
                    {cat.name}
                  </option>
                ))}
              </select>
              {fieldErrors.categoryId && <p className="mt-1 text-sm text-red-500">{fieldErrors.categoryId}</p>}
            </div>
          </div>
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-center mb-4 border-b pb-2">
            <h2 className="text-lg font-semibold text-gray-900">
              Hình ảnh sản phẩm
              <span className="ml-2 text-sm font-normal text-gray-400">({images.length} ảnh)</span>
            </h2>
            {images.length > 0 && (
              <button
                type="button"
                onClick={() => fileInputRef.current?.click()}
                disabled={isUploading}
                className="text-sm bg-orange-100 text-orange-600 px-3 py-1.5 rounded-md hover:bg-orange-200 font-medium disabled:opacity-50 transition-colors"
              >
                {isUploading ? 'Đang tải lên...' : '+ Thêm ảnh'}
              </button>
            )}
            <input type="file" ref={fileInputRef} multiple accept="image/*" className="hidden" onChange={handleFileChange} />
          </div>

          {fieldErrors.images && <p className="mb-2 text-sm text-red-500">{fieldErrors.images}</p>}

          {images.length === 0 ? (
            <div
              onClick={() => !isUploading && fileInputRef.current?.click()}
              className="border-2 border-dashed border-gray-300 rounded-xl p-10 text-center bg-gray-50 hover:border-orange-400 hover:bg-orange-50 transition-colors cursor-pointer group"
            >
              {isUploading ? (
                <div className="flex flex-col items-center gap-3">
                  <svg className="w-10 h-10 text-orange-400 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  <p className="text-sm text-orange-500 font-medium">Đang tải ảnh lên Cloudinary...</p>
                </div>
              ) : (
                <div className="flex flex-col items-center gap-3">
                  <svg className="w-12 h-12 text-gray-300 group-hover:text-orange-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={1.5} d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z" />
                  </svg>
                  <p className="text-sm text-gray-500 group-hover:text-orange-600 font-medium transition-colors">
                    Kéo thả hoặc nhấn để chọn ảnh
                  </p>
                  <p className="text-xs text-gray-400">JPEG, PNG, WEBP — Tối đa 5MB mỗi ảnh</p>
                </div>
              )}
            </div>
          ) : (
            <>
              {isUploading && (
                <div className="flex items-center gap-2 mb-3 text-sm text-orange-500">
                  <svg className="w-4 h-4 animate-spin" fill="none" viewBox="0 0 24 24">
                    <circle className="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" strokeWidth="4" />
                    <path className="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z" />
                  </svg>
                  Đang tải ảnh lên...
                </div>
              )}
              <div className="grid grid-cols-2 sm:grid-cols-4 md:grid-cols-5 gap-4">
                {images.map((img, index) => (
                  <div
                    key={index}
                    className={`relative rounded-lg overflow-hidden group border-2 transition-all duration-200 ${
                      img.isThumbnail
                        ? 'border-orange-500 ring-2 ring-orange-200 shadow-md'
                        : 'border-gray-200 hover:border-gray-300 hover:shadow-sm'
                    }`}
                  >
                    <img src={img.imageUrl} alt={`Ảnh ${index + 1}`} className="w-full h-32 object-cover bg-gray-100" />
                    <div className="absolute inset-0 bg-black/40 opacity-0 group-hover:opacity-100 transition-opacity flex flex-col justify-between p-2">
                      <button
                        type="button"
                        onClick={() => handleRemoveImage(index)}
                        className="self-end bg-red-500 text-white rounded-full p-1 hover:bg-red-600 transition-colors shadow"
                        title="Xóa ảnh"
                      >
                        <svg className="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" />
                        </svg>
                      </button>
                      {!img.isThumbnail && (
                        <button
                          type="button"
                          onClick={() => handleSetThumbnail(index)}
                          className="bg-white text-gray-800 text-xs px-2 py-1 rounded shadow hover:bg-gray-100 font-medium transition-colors"
                        >
                          ★ Đặt làm ảnh bìa
                        </button>
                      )}
                    </div>
                    {img.isThumbnail && (
                      <div className="absolute top-2 left-2 bg-gradient-to-r from-orange-500 to-amber-500 text-white text-[10px] uppercase font-bold px-2 py-0.5 rounded-full shadow-sm tracking-wider">
                        ★ Ảnh bìa
                      </div>
                    )}
                  </div>
                ))}
              </div>
            </>
          )}
        </div>

        <div className="bg-white p-6 rounded-xl shadow-sm border border-gray-200">
          <div className="flex justify-between items-center mb-4 border-b pb-2">
            <h2 className="text-lg font-semibold text-gray-900">Phân loại hàng (Variants)</h2>
            <button
              type="button"
              onClick={handleAddVariant}
              className="text-sm bg-orange-100 text-orange-600 px-3 py-1.5 rounded-md hover:bg-orange-200 font-medium"
            >
              + Thêm phân loại
            </button>
          </div>

          {variants.length === 0 ? (
            <p className="text-gray-500 text-sm italic">Sản phẩm không có phân loại.</p>
          ) : (
            <div className="space-y-4">
              {variants.map((variant, index) => (
                <div key={index} className="flex flex-wrap items-end gap-4 p-4 border border-gray-100 bg-gray-50 rounded-lg relative group">
                  <button type="button" onClick={() => handleRemoveVariant(index)} className="absolute top-2 right-2 text-gray-400 hover:text-red-500">
                    <svg className="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24"><path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M6 18L18 6M6 6l12 12" /></svg>
                  </button>
                  <div className="flex-1 min-w-[150px]">
                    <label className="block text-xs font-medium text-gray-700 mb-1">SKU</label>
                    <input type="text" value={variant.sku} onChange={(e) => handleVariantChange(index, 'sku', e.target.value)} required className="w-full px-3 py-1.5 text-sm border border-gray-300 rounded outline-none focus:border-orange-500" />
                  </div>
                  <div className="w-32">
                    <label className="block text-xs font-medium text-gray-700 mb-1">Giá</label>
                    <input type="number" min="0" value={variant.price} onChange={(e) => handleVariantChange(index, 'price', Number(e.target.value))} required className="w-full px-3 py-1.5 text-sm border border-gray-300 rounded outline-none focus:border-orange-500" />
                  </div>
                  <div className="w-32">
                    <label className="block text-xs font-medium text-gray-700 mb-1">Tồn kho</label>
                    <input type="number" min="0" value={variant.stockQuantity} onChange={(e) => handleVariantChange(index, 'stockQuantity', Number(e.target.value))} required className="w-full px-3 py-1.5 text-sm border border-gray-300 rounded outline-none focus:border-orange-500" />
                  </div>
                  <div className="w-full mt-2">
                    <label className="block text-xs font-medium text-gray-700 mb-1">Thuộc tính (JSON format, vd: {`{"Color":"Red"}`})</label>
                    <input
                      type="text"
                      value={typeof variant.attributes === 'string' ? variant.attributes : JSON.stringify(variant.attributes || {})}
                      onChange={(e) => {
                        try {
                          handleVariantChange(index, 'attributes', JSON.parse(e.target.value));
                        } catch(err) {

                          handleVariantChange(index, 'attributes', e.target.value as any);
                        }
                      }}
                      className="w-full px-3 py-1.5 text-sm border border-gray-300 rounded outline-none focus:border-orange-500 font-mono"
                    />
                  </div>
                </div>
              ))}
            </div>
          )}
        </div>

        <div className="flex justify-end gap-4 pt-4">
          <button
            type="button"
            onClick={() => navigate('/admin/products')}
            className="px-6 py-2 border border-gray-300 rounded-lg text-gray-700 hover:bg-gray-50 font-medium transition-colors"
          >
            Hủy
          </button>
          <button
            type="submit"
            disabled={saving}
            className="px-8 py-2 bg-orange-500 text-white rounded-lg hover:bg-orange-600 font-medium transition-colors disabled:opacity-50"
          >
            {saving ? 'Đang lưu...' : (isEdit ? 'Lưu thay đổi' : 'Tạo sản phẩm')}
          </button>
        </div>
      </form>
    </div>
  );
};
