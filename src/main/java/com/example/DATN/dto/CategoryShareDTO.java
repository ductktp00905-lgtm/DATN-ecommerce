package com.example.DATN.dto;

import java.math.BigDecimal;

public record CategoryShareDTO(
    String categoryName,
    Long totalQuantity,
    BigDecimal totalRevenue,
    double percentage,
    String color
) {}
