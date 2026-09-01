package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductImageUpdateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.request.ProductVariantUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductImageResponse;
import com.tmt.ecommerce.product.enums.ProductStatus;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductImage;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import com.tmt.ecommerce.common.service.FileStorageService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import com.tmt.ecommerce.product.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;
    private final FileStorageService fileStorageService;
    private final ShopInternalService shopInternalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(Long userId, ProductCreateRequest request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục sản phẩm không tồn tại với ID: " + request.categoryId()));

        if (!shopInternalService.isShopOwner(request.shopId(), userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đăng sản phẩm cho shop này.");
        }

        Product product = Product.builder()
                .shopId(request.shopId())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(category)
                .status(ProductStatus.ACTIVE)
                .build();

        if (request.images() != null && !request.images().isEmpty()) {
            List<ProductImage> productImages = request.images().stream().map(imgReq ->
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(imgReq.imageUrl())
                            .publicId(imgReq.publicId())
                            .isThumbnail(Boolean.TRUE.equals(imgReq.isThumbnail()))
                            .sortOrder(imgReq.sortOrder() != null ? imgReq.sortOrder() : 0)
                            .build()
            ).toList();
            product.setImages(new java.util.ArrayList<>(productImages));
            normalizeThumbnails(product.getImages());
            normalizeSortOrders(product.getImages());
        }

        Product savedProduct = productRepository.save(product);

        if (request.variants() != null && !request.variants().isEmpty()) {
            List<ProductVariant> variantsToSave = request.variants().stream().map(vReq ->
                    ProductVariant.builder()
                            .product(savedProduct)
                            .sku(vReq.sku())
                            .price(vReq.price())
                            .stockQuantity(vReq.stockQuantity())
                            .attributes(vReq.attributes())
                            .status(ProductStatus.ACTIVE)
                            .build()
            ).toList();

            productVariantRepository.saveAll(variantsToSave);
            savedProduct.setVariants(variantsToSave);
        }

        return mapToProductResponse(savedProduct);
    }

    @Override
    public Page<ProductResponse> getAllProducts(int page, int size, String keyword) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage;
        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByNameContainingIgnoreCaseAndStatus(keyword, ProductStatus.ACTIVE, pageable);
        } else {
            productPage = productRepository.findByStatus(ProductStatus.ACTIVE, pageable);
        }

        return productPage.map(this::mapToProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long userId, Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        if (!shopInternalService.isShopOwner(product.getShopId(), userId)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên sản phẩm này.");
        }

        product.setStatus(ProductStatus.HIDDEN);
        productRepository.save(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long userId, Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        if (!shopInternalService.isShopOwner(product.getShopId(), userId)) {
            throw new IllegalArgumentException("Bạn không có quyền thao tác trên sản phẩm này.");
        }

        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        if (request.variants() != null) {
            List<Long> requestVariantIds = request.variants().stream()
                    .map(ProductVariantUpdateRequest::id)
                    .filter(Objects::nonNull)
                    .toList();

            product.getVariants().removeIf(existingVariant ->
                    !requestVariantIds.contains(existingVariant.getId()));

            for (ProductVariantUpdateRequest vReq : request.variants()) {
                if (vReq.id() == null) {
                    ProductVariant newVariant = ProductVariant.builder()
                            .product(product)
                            .sku(vReq.sku())
                            .price(vReq.price())
                            .stockQuantity(vReq.stockQuantity())
                            .attributes(vReq.attributes())
                            .status(ProductStatus.ACTIVE)
                            .build();
                    product.getVariants().add(newVariant);
                } else {
                    product.getVariants().stream()
                            .filter(v -> v.getId().equals(vReq.id()))
                            .findFirst()
                            .ifPresent(v -> {
                                v.setSku(vReq.sku());
                                v.setPrice(vReq.price());
                                v.setStockQuantity(vReq.stockQuantity());
                                v.setAttributes(vReq.attributes());
                            });
                }
            }
        }

        List<String> publicIdsToDelete = new java.util.ArrayList<>();

        if (request.images() != null) {
            List<Long> requestImageIds = request.images().stream()
                    .map(ProductImageUpdateRequest::id)
                    .filter(Objects::nonNull)
                    .toList();

            // Collect publicIds strictly from existing DB entities to avoid untrusted client publicIds
            product.getImages().removeIf(existingImage -> {
                boolean shouldRemove = !requestImageIds.contains(existingImage.getId());
                if (shouldRemove && existingImage.getPublicId() != null && !existingImage.getPublicId().isBlank()) {
                    publicIdsToDelete.add(existingImage.getPublicId());
                }
                return shouldRemove;
            });

            for (int i = 0; i < request.images().size(); i++) {
                ProductImageUpdateRequest imgReq = request.images().get(i);
                if (imgReq.id() == null) {
                    ProductImage newImage = ProductImage.builder()
                            .product(product)
                            .imageUrl(imgReq.imageUrl())
                            .publicId(imgReq.publicId())
                            .isThumbnail(Boolean.TRUE.equals(imgReq.isThumbnail()))
                            .sortOrder(i)
                            .build();
                    product.getImages().add(newImage);
                } else {
                    product.getImages().stream()
                            .filter(img -> img.getId().equals(imgReq.id()))
                            .findFirst()
                            .ifPresent(img -> {
                                img.setImageUrl(imgReq.imageUrl());
                                if (imgReq.publicId() != null && !imgReq.publicId().isBlank()) {
                                    img.setPublicId(imgReq.publicId());
                                }
                                img.setThumbnail(Boolean.TRUE.equals(imgReq.isThumbnail()));
                                img.setSortOrder(i);
                            });
                }
            }
            normalizeThumbnails(product.getImages());
            normalizeSortOrders(product.getImages());
        }

        Product updatedProduct = productRepository.save(product);

        // Safe post-DB cleanup for Cloudinary outside transaction rollback risk
        if (!publicIdsToDelete.isEmpty()) {
            TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                @Override
                public void afterCommit() {
                    for (String publicId : publicIdsToDelete) {
                        try {
                            fileStorageService.deleteImage(publicId);
                        } catch (Exception e) {
                            log.warn("Failed to delete image from Cloudinary with publicId {}: {}", publicId, e.getMessage());
                        }
                    }
                }
            });
        }

        return mapToProductResponse(updatedProduct);
    }

    /**
     * Enforce exactly 1 thumbnail when images exist.
     * If 0 images, allow 0 thumbnails without throwing exceptions.
     */
    private void normalizeThumbnails(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return;

        boolean foundFirstThumbnail = false;
        for (ProductImage img : images) {
            if (img.isThumbnail()) {
                if (!foundFirstThumbnail) {
                    foundFirstThumbnail = true;
                } else {
                    img.setThumbnail(false);
                }
            }
        }
        if (!foundFirstThumbnail) {
            images.get(0).setThumbnail(true);
        }
    }

    /**
     * Normalize sortOrder sequentially (0, 1, 2...) based on list position.
     */
    private void normalizeSortOrders(List<ProductImage> images) {
        if (images == null || images.isEmpty()) return;
        for (int i = 0; i < images.size(); i++) {
            images.get(i).setSortOrder(i);
        }
    }

    private ProductResponse mapToProductResponse(Product product) {
        List<ProductVariantResponse> variantResponses = product.getVariants() != null ?
                product.getVariants().stream()
                        .map(v -> new ProductVariantResponse(
                                v.getId(), v.getSku(), v.getPrice(),
                                v.getStockQuantity(), v.getAttributes()
                        )).toList() : List.of();

        List<ProductImageResponse> imageResponses = product.getImages() != null ?
                product.getImages().stream()
                        .map(img -> new ProductImageResponse(
                                img.getId(), img.getImageUrl(), img.getPublicId(),
                                img.isThumbnail(), img.getSortOrder()
                        )).toList() : List.of();

        return new ProductResponse(
                product.getId(),
                product.getShopId(),
                product.getName(),
                product.getDescription(),
                product.getPrice(),
                product.getStockQuantity(),
                product.getCategory() != null ? product.getCategory().getName() : null,
                variantResponses,
                imageResponses
        );
    }
}