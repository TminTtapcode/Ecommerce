package com.tmt.ecommerce.voucher.service;

import com.tmt.ecommerce.common.exception.ResourceNotFoundException;
import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.voucher.dto.request.VoucherUpdateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.entity.VoucherType;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VoucherServiceImplTest {

    @Mock
    private VoucherRepository voucherRepository;

    @Mock
    private ShopInternalService shopInternalService;

    @Mock
    private IdentityInternalService identityInternalService;

    @InjectMocks
    private VoucherServiceImpl voucherService;

    private Voucher systemVoucher;
    private Voucher shopVoucher;

    @BeforeEach
    void setUp() {
        systemVoucher = Voucher.builder()
                .id(1L)
                .code("SYS10")
                .scope(VoucherScope.SYSTEM)
                .type(VoucherType.PERCENTAGE)
                .discountValue(new BigDecimal("10"))
                .maxDiscount(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("100000"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(10))
                .usageLimit(100)
                .usedCount(0)
                .build();

        shopVoucher = Voucher.builder()
                .id(2L)
                .code("SHOP20")
                .scope(VoucherScope.SHOP)
                .shopId(10L)
                .type(VoucherType.PERCENTAGE)
                .discountValue(new BigDecimal("20"))
                .maxDiscount(new BigDecimal("30000"))
                .minOrderValue(new BigDecimal("50000"))
                .startDate(LocalDateTime.now().minusDays(1))
                .endDate(LocalDateTime.now().plusDays(5))
                .usageLimit(50)
                .usedCount(0)
                .build();
    }

    @Test
    void updateVoucher_systemVoucher_asAdmin_success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));
        when(identityInternalService.hasRole(99L, "ROLE_ADMIN")).thenReturn(true);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("50000"))
                .minOrderValue(new BigDecimal("200000"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(15))
                .usageLimit(200)
                .build();

        VoucherResponse response = voucherService.updateVoucher(99L, 1L, req);

        assertNotNull(response);
        assertEquals(VoucherType.FIXED_AMOUNT, response.getType());
        assertEquals(new BigDecimal("50000"), response.getDiscountValue());
        assertEquals(200, response.getUsageLimit());
    }

    @Test
    void updateVoucher_systemVoucher_asNonAdmin_throwsAccessDenied() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));
        when(identityInternalService.hasRole(5L, "ROLE_ADMIN")).thenReturn(false);

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.PERCENTAGE)
                .discountValue(new BigDecimal("15"))
                .maxDiscount(new BigDecimal("50000"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .build();

        assertThrows(AccessDeniedException.class, () -> voucherService.updateVoucher(5L, 1L, req));
    }

    @Test
    void updateVoucher_percentageOver100_throwsIllegalArgument() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.PERCENTAGE)
                .discountValue(new BigDecimal("105"))
                .maxDiscount(new BigDecimal("50000"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(10))
                .build();

        assertThrows(IllegalArgumentException.class, () -> voucherService.updateVoucher(99L, 1L, req));
    }

    @Test
    void updateVoucher_startDateAfterEndDate_throwsIllegalArgument() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("10000"))
                .startDate(LocalDateTime.now().plusDays(5))
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(IllegalArgumentException.class, () -> voucherService.updateVoucher(99L, 1L, req));
    }

    @Test
    void updateVoucher_shopVoucher_asOwner_success() {
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(shopVoucher));
        when(shopInternalService.getShopIdByUserId(5L)).thenReturn(10L);
        doNothing().when(shopInternalService).requireNotBannedForSale(10L);
        when(voucherRepository.save(any(Voucher.class))).thenAnswer(invocation -> invocation.getArgument(0));

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.PERCENTAGE)
                .discountValue(new BigDecimal("25"))
                .maxDiscount(new BigDecimal("40000"))
                .minOrderValue(new BigDecimal("60000"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(7))
                .usageLimit(100)
                .build();

        VoucherResponse response = voucherService.updateVoucher(5L, 2L, req);

        assertNotNull(response);
        assertEquals(new BigDecimal("25"), response.getDiscountValue());
        verify(shopInternalService).requireNotBannedForSale(10L);
    }

    @Test
    void updateVoucher_shopVoucher_wrongOwner_throwsAccessDenied() {
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(shopVoucher));
        when(shopInternalService.getShopIdByUserId(9L)).thenReturn(88L); // Different shop

        VoucherUpdateRequest req = VoucherUpdateRequest.builder()
                .type(VoucherType.FIXED_AMOUNT)
                .discountValue(new BigDecimal("10000"))
                .startDate(LocalDateTime.now())
                .endDate(LocalDateTime.now().plusDays(2))
                .build();

        assertThrows(AccessDeniedException.class, () -> voucherService.updateVoucher(9L, 2L, req));
    }

    @Test
    void deleteVoucher_systemVoucher_asAdmin_success() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));
        when(identityInternalService.hasRole(99L, "ROLE_ADMIN")).thenReturn(true);

        voucherService.deleteVoucher(99L, 1L);

        verify(voucherRepository).delete(systemVoucher);
    }

    @Test
    void deleteVoucher_systemVoucher_asNonAdmin_throwsAccessDenied() {
        when(voucherRepository.findById(1L)).thenReturn(Optional.of(systemVoucher));
        when(identityInternalService.hasRole(5L, "ROLE_ADMIN")).thenReturn(false);

        assertThrows(AccessDeniedException.class, () -> voucherService.deleteVoucher(5L, 1L));
        verify(voucherRepository, never()).delete(any());
    }

    @Test
    void deleteVoucher_shopVoucher_asOwner_success() {
        when(voucherRepository.findById(2L)).thenReturn(Optional.of(shopVoucher));
        when(shopInternalService.getShopIdByUserId(5L)).thenReturn(10L);
        doNothing().when(shopInternalService).requireNotBannedForSale(10L);

        voucherService.deleteVoucher(5L, 2L);

        verify(voucherRepository).delete(shopVoucher);
    }

    @Test
    void deleteVoucher_notFound_throwsResourceNotFound() {
        when(voucherRepository.findById(999L)).thenReturn(Optional.empty());

        assertThrows(ResourceNotFoundException.class, () -> voucherService.deleteVoucher(1L, 999L));
    }
}
