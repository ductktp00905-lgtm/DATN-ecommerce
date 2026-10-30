package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import org.springframework.format.annotation.DateTimeFormat;

@Entity
@Table(name = "Vouchers")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Voucher {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer voucherID;

    @Column(nullable = false, unique = true, length = 50)
    private String code;

    @Column(nullable = false, length = 20)
    private String discountType; // PERCENT hoặc AMOUNT

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal discountValue;

    @Column(precision = 18, scale = 2)
    private BigDecimal minOrderAmount = BigDecimal.ZERO;

    @Column(precision = 18, scale = 2)
    private BigDecimal maxDiscount; // trần giảm khi dùng PERCENT, null = không giới hạn

    @Column(nullable = false)
    private Integer quantity;

    @Column(nullable = false)
    private Integer usedCount = 0;

       @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private LocalDateTime endDate;

    @Column(nullable = false)
    private Boolean active = true;
}