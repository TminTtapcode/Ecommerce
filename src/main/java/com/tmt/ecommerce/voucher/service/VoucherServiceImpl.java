package com.tmt.ecommerce.voucher.service;

import com.tmt.ecommerce.identity.api.IdentityInternalService;
import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.voucher.dto.request.VoucherCreateRequest;
import com.tmt.ecommerce.voucher.dto.request.VoucherUpdateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.entity.VoucherType;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import java.math.BigDecimal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final ShopInternalService shopInternalService;
    private final IdentityInternalService identityInternalService;

    @Override
    @Transactional
    public VoucherResponse createVoucher(Long userId, VoucherCreateRequest request) {
        if (voucherRepository.findByCode(request.getCode()).isPresent()) {
            throw new IllegalArgumentException("Mã giảm giá đã tồn tại");
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        Long shopId = null;
        if (request.getScope() == VoucherScope.SYSTEM) {
            if (!identityInternalService.hasRole(userId, "ROLE_ADMIN")) {
                throw new AccessDeniedException("Chỉ Admin mới có quyền tạo mã giảm giá hệ thống");
            }
        } else if (request.getScope() == VoucherScope.SHOP) {
            shopId = shopInternalService.getShopIdByUserId(userId);
            shopInternalService.requireNotBannedForSale(shopId);
            if (shopId == null) {
                throw new AccessDeniedException("Bạn chưa đăng ký Shop");
            }
        }

        Voucher voucher = Voucher.builder()
                .code(request.getCode())
                .scope(request.getScope())
                .shopId(shopId)
                .type(request.getType())
                .discountValue(request.getDiscountValue())
                .maxDiscount(request.getMaxDiscount())
                .minOrderValue(request.getMinOrderValue())
                .startDate(request.getStartDate())
                .endDate(request.getEndDate())
                .usageLimit(request.getUsageLimit())
                .usedCount(0)
                .build();

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getShopVouchers(Long userId, int page, int size) {
        Long shopId = shopInternalService.getShopIdByUserId(userId);
        if (shopId == null) {
            throw new IllegalStateException("Bạn chưa đăng ký Shop");
        }
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Voucher> voucherPage = voucherRepository.findByShopId(shopId, pageable);
        return voucherPage.map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getSystemVouchers(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        Page<Voucher> voucherPage = voucherRepository.findByScope(VoucherScope.SYSTEM, pageable);
        return voucherPage.map(this::mapToResponse);
    }

    @Override
    @Transactional
    public VoucherResponse updateVoucher(Long userId, Long voucherId, VoucherUpdateRequest request) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.ResourceNotFoundException("Không tìm thấy mã giảm giá"));

        if (request.getType() == VoucherType.PERCENTAGE) {
            if (request.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                throw new IllegalArgumentException("Giảm giá theo phần trăm không được vượt quá 100%");
            }
            if (request.getMaxDiscount() == null) {
                throw new IllegalArgumentException("Voucher theo phần trăm phải có mức giảm tối đa (maxDiscount)");
            }
        }

        if (request.getStartDate().isAfter(request.getEndDate())) {
            throw new IllegalArgumentException("Ngày bắt đầu không được sau ngày kết thúc");
        }

        validateVoucherAccess(userId, voucher);

        voucher.setType(request.getType());
        voucher.setDiscountValue(request.getDiscountValue());
        voucher.setMaxDiscount(request.getMaxDiscount());
        voucher.setMinOrderValue(request.getMinOrderValue());
        voucher.setStartDate(request.getStartDate());
        voucher.setEndDate(request.getEndDate());
        voucher.setUsageLimit(request.getUsageLimit());

        voucher = voucherRepository.save(voucher);
        return mapToResponse(voucher);
    }

    @Override
    @Transactional
    public void deleteVoucher(Long userId, Long voucherId) {
        Voucher voucher = voucherRepository.findById(voucherId)
                .orElseThrow(() -> new com.tmt.ecommerce.common.exception.ResourceNotFoundException("Không tìm thấy mã giảm giá"));

        validateVoucherAccess(userId, voucher);

        voucherRepository.delete(voucher);
    }

    private void validateVoucherAccess(Long userId, Voucher voucher) {
        if (voucher.getScope() == VoucherScope.SYSTEM) {
            if (!identityInternalService.hasRole(userId, "ROLE_ADMIN")) {
                throw new AccessDeniedException("Chỉ Admin mới có quyền thao tác mã giảm giá hệ thống");
            }
        } else if (voucher.getScope() == VoucherScope.SHOP) {
            Long userShopId = shopInternalService.getShopIdByUserId(userId);
            if (userShopId == null || !userShopId.equals(voucher.getShopId())) {
                throw new AccessDeniedException("Bạn không có quyền thao tác mã giảm giá của gian hàng này");
            }
            shopInternalService.requireNotBannedForSale(userShopId);
        }
    }

    private VoucherResponse mapToResponse(Voucher voucher) {
        return VoucherResponse.builder()
                .id(voucher.getId())
                .code(voucher.getCode())
                .scope(voucher.getScope())
                .shopId(voucher.getShopId())
                .type(voucher.getType())
                .discountValue(voucher.getDiscountValue())
                .maxDiscount(voucher.getMaxDiscount())
                .minOrderValue(voucher.getMinOrderValue())
                .startDate(voucher.getStartDate())
                .endDate(voucher.getEndDate())
                .usageLimit(voucher.getUsageLimit())
                .usedCount(voucher.getUsedCount())
                .build();
    }
}
