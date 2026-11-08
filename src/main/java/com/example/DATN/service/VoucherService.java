package com.example.DATN.service;

import com.example.DATN.entity.Voucher;
import java.math.BigDecimal;

public interface VoucherService {
    Voucher validate(String code, BigDecimal orderTotal);          // ném IllegalStateException nếu không hợp lệ
    BigDecimal calculateDiscount(Voucher voucher, BigDecimal orderTotal);
}