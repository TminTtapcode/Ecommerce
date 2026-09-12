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
import com.tmt.ecommerce.product.repository.ProductImageRepository;
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
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;
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
    private final ProductImageRepository productImageRepository;

    @org.springframework.beans.factory.annotation.Autowired
    @org.springframework.context.annotation.Lazy
    private com.tmt.ecommerce.order.api.OrderInternalService orderInternalService;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(Long userId, ProductCreateRequest request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục sản phẩm không tồn tại với ID: " + request.categoryId()));

        Long targetShopId = request.shopId();
        if (targetShopId == null || targetShopId <= 0) {
            targetShopId = shopInternalService.getShopIdByUserId(userId);
        } else if (!shopInternalService.isShopOwner(targetShopId, userId)) {
            throw new IllegalArgumentException("Bạn không có quyền đăng sản phẩm cho shop này.");
        }

        shopInternalService.requireNotBannedForSale(targetShopId);

        Product product = Product.builder()
                .shopId(targetShopId)
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
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(int page, int size, String keyword,
                                               Long categoryId, Long shopId, BigDecimal minPrice, BigDecimal maxPrice,
                                               String sortBy) {

        Sort sort = switch (sortBy != null ? sortBy.trim() : "") {
            case "price_asc"  -> Sort.by("price").ascending().and(Sort.by("id").descending());
            case "price_desc" -> Sort.by("price").descending().and(Sort.by("id").descending());
            default           -> Sort.by("id").descending();
        };

        int safeSize = Math.min(Math.max(size, 1), 50);
        int safePage = Math.max(page, 0);
        Pageable pageable = PageRequest.of(safePage, safeSize, sort);

        Set<Long> categoryIds = null;
        if (categoryId != null) {
            categoryIds = new java.util.HashSet<>();
            categoryIds.add(categoryId);
            java.util.Queue<Long> queue = new java.util.LinkedList<>(List.of(categoryId));
            while (!queue.isEmpty()) {
                Long current = queue.poll();
                List<Long> children = categoryRepository.findDirectChildIds(current);
                for (Long child : children) {
                    if (categoryIds.add(child)) {
                        queue.add(child);
                    }
                }
            }
        }

        List<Long> bannedShopIds = shopInternalService.getBannedShopIds();

        final Set<Long> finalCategoryIds = categoryIds;

        final BigDecimal safeMin = (minPrice != null && minPrice.compareTo(BigDecimal.ZERO) >= 0) ? minPrice : null;
        final BigDecimal safeMax = (maxPrice != null && maxPrice.compareTo(BigDecimal.ZERO) > 0) ? maxPrice : null;

        Page<Product> productPage = productRepository.findAll(
            (org.springframework.data.jpa.domain.Specification<Product>) (root, query, cb) -> {
                var predicates = new java.util.ArrayList<jakarta.persistence.criteria.Predicate>();

                predicates.add(cb.equal(root.get("status"), ProductStatus.ACTIVE));

                if (!bannedShopIds.isEmpty()) {
                    predicates.add(cb.not(root.get("shopId").in(bannedShopIds)));
                }

                if (shopId != null) {
                    predicates.add(cb.equal(root.get("shopId"), shopId));
                }

                if (keyword != null && !keyword.isBlank()) {
                    String escaped = keyword.toLowerCase(java.util.Locale.ROOT)
                            .replace("!", "!!").replace("%", "!%").replace("_", "!_");
                    predicates.add(cb.like(cb.lower(root.get("name")), "%" + escaped + "%", '!'));
                }

                if (finalCategoryIds != null && !finalCategoryIds.isEmpty()) {
                    predicates.add(root.get("category").get("id").in(finalCategoryIds));
                }

                if (safeMin != null) {
                    predicates.add(cb.greaterThanOrEqualTo(root.get("price"), safeMin));
                }
                if (safeMax != null) {
                    predicates.add(cb.lessThanOrEqualTo(root.get("price"), safeMax));
                }

                return cb.and(predicates.toArray(jakarta.persistence.criteria.Predicate[]::new));
            }, pageable);

        return productPage.map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getVendorProducts(Long userId, int page, int size, String keyword) {
        Long shopId = shopInternalService.getShopIdByUserId(userId);
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());

        Page<Product> productPage;
        if (keyword != null && !keyword.isBlank()) {
            productPage = productRepository.findByShopIdAndNameContainingIgnoreCase(shopId, keyword, pageable);
        } else {
            productPage = productRepository.findByShopId(shopId, pageable);
        }

        return productPage.map(this::mapToProductResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getRecommendedProducts(Long userId, int limit) {
        int safeLimit = Math.max(1, Math.min(limit, 50));
        int bufferLimit = safeLimit * 3;

        java.util.Set<Product> recommendationSet = new java.util.LinkedHashSet<>();
        java.util.List<Long> purchasedProductIds = new java.util.ArrayList<>();
        java.util.List<Long> bannedShopIds = shopInternalService.getBannedShopIds();

        if (userId != null) {
            List<Long> recentVariantIds = orderInternalService.getRecentlyPurchasedVariantIds(userId, bufferLimit);
            if (!recentVariantIds.isEmpty()) {
                List<ProductVariant> purchasedVariants = productVariantRepository.findByIdIn(recentVariantIds);
                List<Product> purchasedProducts = purchasedVariants.stream()
                        .map(ProductVariant::getProduct)
                        .filter(p -> p.getStatus() == ProductStatus.ACTIVE)
                        .distinct()
                        .toList();

                purchasedProductIds = purchasedProducts.stream().map(Product::getId).toList();

                List<Long> categoryIds = purchasedProducts.stream()
                        .map(p -> p.getCategory().getId())
                        .distinct()
                        .toList();

                if (!categoryIds.isEmpty()) {
                    List<Long> excluded = purchasedProductIds.isEmpty() ? new java.util.ArrayList<>(List.of(-1L)) : new java.util.ArrayList<>(purchasedProductIds);
                    Pageable pageable = PageRequest.of(0, safeLimit, Sort.by("id").descending());
                    List<Product> personalized = bannedShopIds.isEmpty()
                            ? productRepository.findByCategoryIdsExcluding(categoryIds, excluded, ProductStatus.ACTIVE, pageable)
                            : productRepository.findByCategoryIdsExcluding(categoryIds, excluded, ProductStatus.ACTIVE, bannedShopIds, pageable);

                    recommendationSet.addAll(personalized);
                }
            }
        }

        if (recommendationSet.size() < safeLimit) {
            List<Long> topVariantIds = orderInternalService.getTopSellingVariantIds(bufferLimit);
            if (!topVariantIds.isEmpty()) {
                List<ProductVariant> topVariants = productVariantRepository.findByIdIn(topVariantIds);

                java.util.Map<Long, Product> variantToProductMap = new java.util.HashMap<>();
                for (ProductVariant pv : topVariants) {
                    if (pv.getProduct().getStatus() == ProductStatus.ACTIVE) {
                        variantToProductMap.put(pv.getId(), pv.getProduct());
                    }
                }

                for (Long vid : topVariantIds) {
                    Product p = variantToProductMap.get(vid);
                    if (p != null && !purchasedProductIds.contains(p.getId()) && !recommendationSet.contains(p)) {
                        if (bannedShopIds.isEmpty() || !bannedShopIds.contains(p.getShopId())) {
                            recommendationSet.add(p);
                            if (recommendationSet.size() >= safeLimit) break;
                        }
                    }
                }
            }
        }

        if (recommendationSet.size() < safeLimit) {
            List<Long> excluded = recommendationSet.stream().map(Product::getId).collect(java.util.stream.Collectors.toList());
            excluded.addAll(purchasedProductIds);
            if (excluded.isEmpty()) excluded.add(-1L);

            int needed = safeLimit - recommendationSet.size();
            Pageable pageable = PageRequest.of(0, needed, Sort.by("id").descending());
            List<Product> newest = bannedShopIds.isEmpty()
                    ? productRepository.findNewestExcluding(excluded, ProductStatus.ACTIVE, pageable)
                    : productRepository.findNewestExcluding(excluded, ProductStatus.ACTIVE, bannedShopIds, pageable);

            recommendationSet.addAll(newest);
        }

        List<Product> finalProducts = recommendationSet.stream().limit(safeLimit).toList();

        List<ProductResponse> content = mapToProductResponsesSafe(finalProducts);

        return new org.springframework.data.domain.PageImpl<>(content, PageRequest.of(0, safeLimit), content.size());
    }

    private List<ProductResponse> mapToProductResponsesSafe(List<Product> products) {
        if (products == null || products.isEmpty()) return List.of();

        List<Long> productIds = products.stream().map(Product::getId).toList();

        List<ProductVariant> allVariants = productVariantRepository.findByProduct_IdIn(productIds);
        List<ProductImage> allImages = productImageRepository.findByProduct_IdIn(productIds);

        java.util.Map<Long, List<ProductVariant>> variantsMap = allVariants.stream()
                .collect(java.util.stream.Collectors.groupingBy(v -> v.getProduct().getId()));

        java.util.Map<Long, List<ProductImage>> imagesMap = allImages.stream()
                .collect(java.util.stream.Collectors.groupingBy(img -> img.getProduct().getId()));

        return products.stream().map(product -> {
            List<ProductVariant> pVariants = variantsMap.getOrDefault(product.getId(), List.of());
            List<ProductImage> pImages = imagesMap.getOrDefault(product.getId(), List.of());

            List<ProductVariantResponse> variantResponses = pVariants.stream()
                    .map(v -> new ProductVariantResponse(
                            v.getId(), v.getSku(), v.getPrice(),
                            v.getStockQuantity(), v.getAttributes()
                    )).toList();

            List<ProductImageResponse> imageResponses = pImages.stream()
                    .map(img -> new ProductImageResponse(
                            img.getId(), img.getImageUrl(), img.getPublicId(),
                            img.isThumbnail(), img.getSortOrder()
                    )).toList();

            return new ProductResponse(
                    product.getId(),
                    product.getShopId(),
                    product.getName(),
                    product.getDescription(),
                    product.getPrice(),
                    product.getStockQuantity(),
                    product.getCategory() != null ? product.getCategory().getId() : null,
                    product.getCategory() != null ? product.getCategory().getName() : null,
                    product.getStatus() != null ? product.getStatus().name() : "ACTIVE",
                    variantResponses,
                    imageResponses
            );
        }).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getProductById(Long id) {
        return mapToProductResponse(findPublicProduct(id, shopInternalService.getBannedShopIds()));
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getRelatedProducts(Long productId, Integer limit) {
        if (limit == null || limit < 1) {
            throw new com.tmt.ecommerce.common.exception.AppException(
                    com.tmt.ecommerce.common.exception.ErrorCode.INVALID_INPUT);
        }

        int safeLimit = Math.min(limit, 8);
        List<Long> bannedShopIds = shopInternalService.getBannedShopIds();
        Product seed = findPublicProduct(productId, bannedShopIds);
        Pageable pageable = PageRequest.of(0, safeLimit, Sort.by("id").descending());

        List<Product> related = bannedShopIds.isEmpty()
                ? productRepository.findByCategory_IdAndStatusAndIdNot(
                        seed.getCategory().getId(), ProductStatus.ACTIVE, seed.getId(), pageable)
                : productRepository.findByCategory_IdAndStatusAndIdNotAndShopIdNotIn(
                        seed.getCategory().getId(), ProductStatus.ACTIVE, seed.getId(), bannedShopIds, pageable);

        return related.stream().map(this::mapToProductResponse).toList();
    }

    @Override
    @Transactional(readOnly = true)
    public ProductResponse getVendorProductById(Long userId, Long id) {
        Product product = productRepository.findById(id).orElseThrow(() ->
                new com.tmt.ecommerce.common.exception.AppException(com.tmt.ecommerce.common.exception.ErrorCode.PRODUCT_NOT_FOUND));
        if (!shopInternalService.isShopOwner(product.getShopId(), userId)) {
            throw new org.springframework.security.access.AccessDeniedException("Product belongs to another shop");
        }
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

        shopInternalService.requireNotBannedForSale(product.getShopId());
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

        shopInternalService.requireNotBannedForSale(product.getShopId());
        product.setName(request.name());
        product.setDescription(request.description());
        product.setPrice(request.price());
        product.setStockQuantity(request.stockQuantity());

        if (request.categoryId() != null) {
            Category category = categoryRepository.findById(request.categoryId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy danh mục với ID: " + request.categoryId()));
            product.setCategory(category);
        }

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
                    final int finalI = i;
                    product.getImages().stream()
                            .filter(img -> img.getId().equals(imgReq.id()))
                            .findFirst()
                            .ifPresent(img -> {
                                img.setImageUrl(imgReq.imageUrl());
                                if (imgReq.publicId() != null && !imgReq.publicId().isBlank()) {
                                    img.setPublicId(imgReq.publicId());
                                }
                                img.setThumbnail(Boolean.TRUE.equals(imgReq.isThumbnail()));
                                img.setSortOrder(finalI);
                            });
                }
            }
            normalizeThumbnails(product.getImages());
            normalizeSortOrders(product.getImages());
        }

        Product updatedProduct = productRepository.save(product);

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
                product.getCategory() != null ? product.getCategory().getId() : null,
                product.getCategory() != null ? product.getCategory().getName() : null,
                product.getStatus() != null ? product.getStatus().name() : "ACTIVE",
                variantResponses,
                imageResponses
        );
    }

    private Product findPublicProduct(Long id, List<Long> bannedShopIds) {
        Product product = productRepository.findByIdAndStatus(id, ProductStatus.ACTIVE)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.AppException(
                        com.tmt.ecommerce.common.exception.ErrorCode.PRODUCT_NOT_FOUND));
        if (bannedShopIds.contains(product.getShopId())) {
            throw new com.tmt.ecommerce.common.exception.AppException(
                        com.tmt.ecommerce.common.exception.ErrorCode.PRODUCT_NOT_FOUND);
        }
        return product;
    }
}
