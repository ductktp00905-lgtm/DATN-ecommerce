package com.example.DATN.dto;

import java.math.BigDecimal;

public record DashboardBestSellerDTO(
    Integer productId,
    String name,
    String categoryName,
    String imageURL,
    Long totalQuantity,
    BigDecimal totalRevenue,
    Integer stock
) {}
