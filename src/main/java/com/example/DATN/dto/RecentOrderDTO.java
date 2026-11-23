package com.example.DATN.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RecentOrderDTO(
    Integer orderId,
    String customerName,
    String customerPhone,
    LocalDateTime orderDate,
    BigDecimal totalAmount,
    String paymentMethod,
    String paymentStatus,
    String status
) {}
