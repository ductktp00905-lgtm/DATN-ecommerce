package com.example.DATN.service.impl;

import com.example.DATN.entity.Voucher;
import com.example.DATN.repository.VoucherRepository;
import com.example.DATN.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.Locale;

@Service
public class VoucherServiceImpl implements VoucherService {

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    public Voucher validate(String code, BigDecimal orderTotal) {
        if (code == null || code.trim().isEmpty()) {
            throw new IllegalStateException("Vui lòng nhập mã khuyến mãi");
        }
        Voucher v = voucherRepository.findByCodeIgnoreCase(code.trim())
                .orElseThrow(() -> new IllegalStateException("Mã khuyến mãi không tồn tại"));

        LocalDateTime now = LocalDateTime.now();
        if (!Boolean.TRUE.equals(v.getActive())) {
            throw new IllegalStateException("Mã khuyến mãi đã bị vô hiệu hóa");
        }
        if (v.getStartDate() != null && now.isBefore(v.getStartDate())) {
            throw new IllegalStateException("Mã khuyến mãi chưa đến thời gian áp dụng");
        }
        if (v.getEndDate() != null && now.isAfter(v.getEndDate())) {
            throw new IllegalStateException("Mã khuyến mãi đã hết hạn");
        }
        if (v.getUsedCount() >= v.getQuantity()) {
            throw new IllegalStateException("Mã khuyến mãi đã hết lượt sử dụng");
        }
        if (v.getMinOrderAmount() != null && orderTotal.compareTo(v.getMinOrderAmount()) < 0) {
            throw new IllegalStateException("Đơn hàng cần tối thiểu "
                    + String.format(Locale.US, "%,.0f", v.getMinOrderAmount()) + " ₫ để dùng mã này");
        }
        return v;
    }

    @Override
    public BigDecimal calculateDiscount(Voucher v, BigDecimal orderTotal) {
        BigDecimal discount;
        if ("PERCENT".equals(v.getDiscountType())) {
            discount = orderTotal.multiply(v.getDiscountValue())
                    .divide(BigDecimal.valueOf(100), 0, RoundingMode.DOWN);
            if (v.getMaxDiscount() != null && discount.compareTo(v.getMaxDiscount()) > 0) {
                discount = v.getMaxDiscount();
            }
        } else {
            discount = v.getDiscountValue();
        }
        if (discount.compareTo(orderTotal) > 0) {
            discount = orderTotal;
        }
        return discount;
    }
}