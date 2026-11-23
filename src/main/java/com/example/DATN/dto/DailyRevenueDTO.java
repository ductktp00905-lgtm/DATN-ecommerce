package com.example.DATN.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

public record DailyRevenueDTO(
    String dayLabel,        // e.g. "T2", "Hôm nay"
    String dateStr,         // "22/11"
    LocalDate date,
    BigDecimal revenue,     // Doanh thu ngày
    long orderCount,        // Số đơn hàng
    int heightPercentage    // Chiều cao % cho biểu đồ cột fallback (10 - 100)
) {}
