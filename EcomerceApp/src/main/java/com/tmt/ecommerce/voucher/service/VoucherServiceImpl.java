package com.tmt.ecommerce.voucher.service;

import com.tmt.ecommerce.shop.api.ShopInternalService;
import com.tmt.ecommerce.voucher.dto.request.VoucherCreateRequest;
import com.tmt.ecommerce.voucher.dto.response.VoucherResponse;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.entity.VoucherScope;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class VoucherServiceImpl implements VoucherService {

    private final VoucherRepository voucherRepository;
    private final ShopInternalService shopInternalService;

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
        if (request.getScope() == VoucherScope.SHOP) {
            shopId = shopInternalService.getShopIdByUserId(userId);
            if (shopId == null) {
                throw new IllegalStateException("Bạn chưa đăng ký Shop");
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
    public Page<VoucherResponse> getShopVouchers(Long shopId, int page, int size) {
        // Implement when needed (requires custom query by shopId)
        return null; 
    }

    @Override
    @Transactional(readOnly = true)
    public Page<VoucherResponse> getSystemVouchers(int page, int size) {
         // Implement when needed
         return null;
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
