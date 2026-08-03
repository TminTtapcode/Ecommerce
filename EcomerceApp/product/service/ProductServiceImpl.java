package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.dto.request.ProductCreateRequest;
import com.tmt.ecommerce.product.dto.request.ProductImageUpdateRequest;
import com.tmt.ecommerce.product.dto.request.ProductUpdateRequest;
import com.tmt.ecommerce.product.dto.request.ProductVariantUpdateRequest;
import com.tmt.ecommerce.product.dto.response.ProductImageResponse;
import com.tmt.ecommerce.product.entity.Category;
import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductImage;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.CategoryRepository;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.tmt.ecommerce.product.dto.response.ProductResponse;
import com.tmt.ecommerce.product.dto.response.ProductVariantResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final ProductVariantRepository productVariantRepository;
    private final CategoryRepository categoryRepository;

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ProductResponse createProduct(ProductCreateRequest request) {

        Category category = categoryRepository.findById(request.categoryId())
                .orElseThrow(() -> new IllegalArgumentException("Danh mục sản phẩm không tồn tại với ID: " + request.categoryId()));

        Product product = Product.builder()
                .shopId(request.shopId())
                .name(request.name())
                .description(request.description())
                .price(request.price())
                .stockQuantity(request.stockQuantity())
                .category(category)
                .status("ACTIVE")
                .build();

        if (request.images() != null && !request.images().isEmpty()) {
            List<ProductImage> productImages = request.images().stream().map(imgReq ->
                    ProductImage.builder()
                            .product(product)
                            .imageUrl(imgReq.imageUrl())
                            .isThumbnail(imgReq.isThumbnail())
                            .sortOrder(imgReq.sortOrder())
                            .build()
            ).toList();
            product.setImages(productImages);
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
                            .status("ACTIVE")
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
            productPage = productRepository.findByNameContainingIgnoreCase(keyword, pageable);
        } else {
            productPage = productRepository.findAll(pageable);
        }

        return productPage.map(this::mapToProductResponse);
    }

    @Override
    public ProductResponse getProductById(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));
        return mapToProductResponse(product);
    }

    @Override
    @Transactional
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

        product.setStatus("HIDDEN");
        productRepository.save(product);
    }

    @Override
    @Transactional
    public ProductResponse updateProduct(Long id, ProductUpdateRequest request) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy sản phẩm với ID: " + id));

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
                            .status("ACTIVE")
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
        if (request.images() != null) {
            // Lấy ra danh sách ID của các ảnh được gửi lên
            List<Long> requestImageIds = request.images().stream()
                    .map(ProductImageUpdateRequest::id)
                    .filter(Objects::nonNull)
                    .toList();

            // Xóa những ảnh trong DB không còn nằm trong request
            product.getImages().removeIf(existingImage ->
                    !requestImageIds.contains(existingImage.getId()));

            // Duyệt qua danh sách ảnh gửi lên để Thêm mới hoặc Cập nhật
            for (ProductImageUpdateRequest imgReq : request.images()) {
                if (imgReq.id() == null) {
                    // Thêm ảnh mới
                    ProductImage newImage = ProductImage.builder()
                            .product(product) // Gán quan hệ 2 chiều
                            .imageUrl(imgReq.imageUrl())
                            .isThumbnail(imgReq.isThumbnail())
                            .sortOrder(imgReq.sortOrder())
                            .build();
                    product.getImages().add(newImage);
                } else {
                    // Cập nhật ảnh đã có (ví dụ: đổi thứ tự hoặc đổi ảnh đại diện)
                    product.getImages().stream()
                            .filter(img -> img.getId().equals(imgReq.id()))
                            .findFirst()
                            .ifPresent(img -> {
                                img.setImageUrl(imgReq.imageUrl());
                                img.setThumbnail(imgReq.isThumbnail());
                                img.setSortOrder(imgReq.sortOrder());
                            });
                }
            }
        }

        // Lưu ý: Tạm thời hàm updateProduct vẫn chưa có logic cập nhật danh sách Images.
        // Sau này em cần bổ sung thêm logic so sánh và cập nhật hình ảnh tương tự như variants nhé.

        Product updatedProduct = productRepository.save(product);
        return mapToProductResponse(updatedProduct);
    }

    // GIỮ LẠI DUY NHẤT HÀM MAP NÀY
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
                                img.getId(), img.getImageUrl(),
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