package com.tmt.ecommerce.shop.service;

import com.tmt.ecommerce.common.exception.AppException;
import com.tmt.ecommerce.common.exception.ErrorCode;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.shop.dto.ShopCreateRequest;
import com.tmt.ecommerce.shop.entity.Shop;
import com.tmt.ecommerce.shop.enums.ShopStatus;
import com.tmt.ecommerce.shop.repository.ShopRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShopServiceTest {

    @Mock
    private ShopRepository shopRepository;

    @Mock
    private IdentityInternalService identityInternalService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private ShopService shopService;

    private Shop pendingShop;
    private Shop activeShop;
    private Shop bannedShop;

    @BeforeEach
    void setUp() {
        pendingShop = Shop.builder()
                .id(1L)
                .userId(10L)
                .name("Shop Test Pending")
                .description("Mô tả shop")
                .status(ShopStatus.PENDING)
                .build();

        activeShop = Shop.builder()
                .id(2L)
                .userId(20L)
                .name("Shop Test Active")
                .description("Mô tả shop")
                .status(ShopStatus.ACTIVE)
                .build();

        bannedShop = Shop.builder()
                .id(3L)
                .userId(30L)
                .name("Shop Test Banned")
                .description("Mô tả shop")
                .status(ShopStatus.BANNED)
                .priorStatus(ShopStatus.ACTIVE)
                .build();
    }

    @Test
    void createShop_ShouldSuccess_WhenValid() {
        when(shopRepository.existsByUserId(10L)).thenReturn(false);
        when(shopRepository.existsByName("Shop Moi")).thenReturn(false);

        ShopCreateRequest request = new ShopCreateRequest();
        request.setName("Shop Moi");
        request.setDescription("Mo ta shop moi");
        assertDoesNotThrow(() -> shopService.createShop(10L, request));

        verify(shopRepository).save(any(Shop.class));
    }

    @Test
    void createShop_ShouldThrow_WhenUserAlreadyHasShop() {
        when(shopRepository.existsByUserId(10L)).thenReturn(true);

        ShopCreateRequest request = new ShopCreateRequest();
        request.setName("Shop Moi");
        request.setDescription("Mo ta");
        assertEquals(ErrorCode.SHOP_ALREADY_EXISTS, assertThrows(AppException.class, () -> shopService.createShop(10L, request)).getErrorCode());
    }

    @Test
    void createShop_ShouldThrow_WhenShopNameDuplicate() {
        when(shopRepository.existsByUserId(10L)).thenReturn(false);
        when(shopRepository.existsByName("Shop Da Co")).thenReturn(true);

        ShopCreateRequest request = new ShopCreateRequest();
        request.setName("Shop Da Co");
        request.setDescription("Mo ta");
        assertEquals(ErrorCode.SHOP_NAME_DUPLICATE, assertThrows(AppException.class, () -> shopService.createShop(10L, request)).getErrorCode());
    }

    @Test
    void approveShop_ShouldSuccess_WhenPending() {
        when(shopRepository.findByIdForUpdate(1L)).thenReturn(Optional.of(pendingShop));
        when(identityInternalService.assignShopOwnerRole(10L)).thenReturn("owner@test.com");

        shopService.approveShop(1L);

        assertEquals(ShopStatus.ACTIVE, pendingShop.getStatus());
        verify(shopRepository).save(pendingShop);
        verify(eventPublisher).publishEvent(any(com.tmt.ecommerce.shop.api.event.ShopApprovedEvent.class));
    }

    @Test
    void approveShop_ShouldThrow_WhenNotPending() {
        when(shopRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(activeShop));

        assertEquals(ErrorCode.SHOP_INVALID_STATUS_TRANSITION, assertThrows(AppException.class, () -> shopService.approveShop(2L)).getErrorCode());
    }

    @Test
    void banShop_ShouldSuccess_WhenActive() {
        when(shopRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(activeShop));

        shopService.banShop(99L, 2L, "Vi phạm bản quyền");

        assertEquals(ShopStatus.BANNED, activeShop.getStatus());
        verify(shopRepository).save(activeShop);
    }

    @Test
    void banShop_ShouldThrow_WhenAlreadyBanned() {
        when(shopRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(bannedShop));

        assertThrows(AppException.class, () -> shopService.banShop(99L, 3L, "Lý do"));
    }

    @Test
    void unbanShop_ShouldSuccess_WhenBanned() {
        when(shopRepository.findByIdForUpdate(3L)).thenReturn(Optional.of(bannedShop));

        shopService.unbanShop(99L, 3L);

        assertEquals(ShopStatus.ACTIVE, bannedShop.getStatus());
        verify(shopRepository).save(bannedShop);
    }

    @Test
    void unbanShop_ShouldThrow_WhenNotBanned() {
        when(shopRepository.findByIdForUpdate(2L)).thenReturn(Optional.of(activeShop));

        assertThrows(AppException.class, () -> shopService.unbanShop(99L, 2L));
    }
}
