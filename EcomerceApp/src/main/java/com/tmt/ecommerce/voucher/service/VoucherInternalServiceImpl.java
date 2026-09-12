package com.tmt.ecommerce.voucher.service;

import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.tmt.ecommerce.voucher.api.dto.VoucherCalculationRequest;
import com.tmt.ecommerce.voucher.api.dto.VoucherDiscountResult;

@Service
@RequiredArgsConstructor
public class VoucherInternalServiceImpl implements VoucherInternalService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public VoucherDiscountResult calculateDiscount(VoucherCalculationRequest request) {
        Voucher voucher = voucherRepository.findByCode(request.voucherCode())
                .orElseThrow(() -> new IllegalArgumentException("Mã giảm giá không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        if (now.isBefore(voucher.getStartDate())) {
            throw new IllegalArgumentException("Mã giảm giá chưa đến thời gian sử dụng");
        }
        if (now.isAfter(voucher.getEndDate())) {
            throw new IllegalArgumentException("Mã giảm giá đã hết hạn");
        }
        if (voucher.getUsageLimit() != null && voucher.getUsedCount() >= voucher.getUsageLimit()) {
            throw new IllegalArgumentException("Mã giảm giá đã hết lượt sử dụng");
        }

        BigDecimal eligibleAmount = BigDecimal.ZERO;
        Map<Long, BigDecimal> shopTotals = request.shopTotals();

        if (voucher.getScope() == com.tmt.ecommerce.voucher.entity.VoucherScope.SHOP) {
            if (!shopTotals.containsKey(voucher.getShopId())) {
                throw new IllegalArgumentException("Voucher này không áp dụng cho các sản phẩm trong giỏ hàng");
            }
            eligibleAmount = shopTotals.get(voucher.getShopId());
        } else {

            for (BigDecimal amount : shopTotals.values()) {
                eligibleAmount = eligibleAmount.add(amount);
            }
        }

        if (voucher.getMinOrderValue() != null && eligibleAmount.compareTo(voucher.getMinOrderValue()) < 0) {
            throw new IllegalArgumentException("Đơn hàng chưa đạt giá trị tối thiểu để áp dụng mã giảm giá này");
        }

        BigDecimal totalDiscount = BigDecimal.ZERO;
        if (voucher.getType() == com.tmt.ecommerce.voucher.entity.VoucherType.PERCENTAGE) {
            totalDiscount = eligibleAmount.multiply(voucher.getDiscountValue()).divide(BigDecimal.valueOf(100));
            if (voucher.getMaxDiscount() != null && totalDiscount.compareTo(voucher.getMaxDiscount()) > 0) {
                totalDiscount = voucher.getMaxDiscount();
            }
        } else {
            totalDiscount = voucher.getDiscountValue();
        }

        if (totalDiscount.compareTo(eligibleAmount) > 0) {
            totalDiscount = eligibleAmount;
        }

        Map<Long, BigDecimal> discountPerShop = new HashMap<>();

        if (totalDiscount.compareTo(BigDecimal.ZERO) > 0) {
            if (voucher.getScope() == com.tmt.ecommerce.voucher.entity.VoucherScope.SHOP) {
                discountPerShop.put(voucher.getShopId(), totalDiscount);
            } else {

                for (Map.Entry<Long, BigDecimal> entry : shopTotals.entrySet()) {
                    Long shopId = entry.getKey();
                    BigDecimal shopOrderTotal = entry.getValue();

                    BigDecimal shopDiscount = totalDiscount.multiply(shopOrderTotal)
                            .divide(eligibleAmount, 0, java.math.RoundingMode.HALF_UP);

                    if (shopDiscount.compareTo(shopOrderTotal) > 0) {
                        shopDiscount = shopOrderTotal;
                    }
                    if (shopDiscount.compareTo(BigDecimal.ZERO) > 0) {
                        discountPerShop.put(shopId, shopDiscount);
                    }
                }
            }
        }

        return new VoucherDiscountResult(voucher.getId(), voucher.getCode(), discountPerShop);
    }

    @Override
    @Transactional
    public boolean incrementUsage(Long voucherId) {
        int affectedRows = voucherRepository.incrementUsage(voucherId);
        return affectedRows > 0;
    }
}
