package com.tmt.ecommerce.product.service;

import com.tmt.ecommerce.product.entity.Product;
import com.tmt.ecommerce.product.entity.ProductVariant;
import com.tmt.ecommerce.product.repository.ProductRepository;
import com.tmt.ecommerce.product.repository.ProductVariantRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductInternalServiceImplTest {

    @Mock
    private ProductVariantRepository productVariantRepository;

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductInternalServiceImpl productInternalService;

    private Product product;
    private ProductVariant variant;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(100L)
                .name("Sản phẩm mẫu")
                .stockQuantity(50)
                .build();

        variant = ProductVariant.builder()
                .id(10L)
                .product(product)
                .sku("SKU-10")
                .stockQuantity(20)
                .build();
    }

    @Test
    void deductStock_ShouldSuccess_WhenStockSufficient() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.deductStock(10L, 5)).thenReturn(1);
        when(productRepository.deductStock(100L, 5)).thenReturn(1);

        assertDoesNotThrow(() -> productInternalService.deductStock(10L, 5));

        verify(productVariantRepository).deductStock(10L, 5);
        verify(productRepository).deductStock(100L, 5);
    }

    @Test
    void deductStock_ShouldThrow_WhenVariantStockInsufficient() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.deductStock(10L, 5)).thenReturn(0);

        assertThrows(IllegalStateException.class, () -> productInternalService.deductStock(10L, 5));
        verify(productRepository, never()).deductStock(anyLong(), anyInt());
    }

    @Test
    void restoreStock_ShouldInvokeAtomicRestoration_WhenValid() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));

        when(productVariantRepository.restoreStock(10L, 3)).thenReturn(1);
        when(productRepository.restoreStock(100L, 3)).thenReturn(1);
        productInternalService.restoreStock(10L, 3);

        verify(productVariantRepository).restoreStock(10L, 3);
        verify(productRepository).restoreStock(100L, 3);
    }

    @Test
    void restoreStock_ShouldReject_WhenQuantityZeroOrNegative() {
        assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> productInternalService.restoreStock(10L, 0));
        assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> productInternalService.restoreStock(10L, -5));
        assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> productInternalService.restoreStock(null, 5));

        assertThrows(com.tmt.ecommerce.common.exception.AppException.class, () -> productInternalService.restoreStock(10L, null));
        verify(productVariantRepository, never()).restoreStock(anyLong(), anyInt());
        verify(productRepository, never()).restoreStock(anyLong(), anyInt());
    }

    @Test
    void restoreStock_ShouldRejectMissingVariant() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.empty());
        assertEquals(com.tmt.ecommerce.common.exception.ErrorCode.VARIANT_NOT_FOUND,
                assertThrows(com.tmt.ecommerce.common.exception.AppException.class,
                        () -> productInternalService.restoreStock(10L, 3)).getErrorCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void restoreStock_ShouldRejectVariantUpdateWithNoRows() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.restoreStock(10L, 3)).thenReturn(0);
        assertEquals(com.tmt.ecommerce.common.exception.ErrorCode.INTERNAL_SERVER_ERROR,
                assertThrows(com.tmt.ecommerce.common.exception.AppException.class,
                        () -> productInternalService.restoreStock(10L, 3)).getErrorCode());
        verifyNoInteractions(productRepository);
    }

    @Test
    void restoreStock_ShouldRejectProductUpdateWithNoRows() {
        when(productVariantRepository.findById(10L)).thenReturn(Optional.of(variant));
        when(productVariantRepository.restoreStock(10L, 3)).thenReturn(1);
        when(productRepository.restoreStock(100L, 3)).thenReturn(0);
        assertEquals(com.tmt.ecommerce.common.exception.ErrorCode.INTERNAL_SERVER_ERROR,
                assertThrows(com.tmt.ecommerce.common.exception.AppException.class,
                        () -> productInternalService.restoreStock(10L, 3)).getErrorCode());
    }
}
