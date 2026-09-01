package com.tmt.ecommerce.voucher.service;

import com.tmt.ecommerce.voucher.api.VoucherInternalService;
import com.tmt.ecommerce.voucher.entity.Voucher;
import com.tmt.ecommerce.voucher.repository.VoucherRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class VoucherInternalServiceImpl implements VoucherInternalService {

    private final VoucherRepository voucherRepository;

    @Override
    @Transactional(readOnly = true)
    public Voucher validateAndGetVoucher(String code) {
        Voucher voucher = voucherRepository.findByCode(code)
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

        return voucher;
    }

    @Override
    @Transactional
    public boolean incrementUsage(Long voucherId) {
        int affectedRows = voucherRepository.incrementUsage(voucherId);
        return affectedRows > 0;
    }
}
