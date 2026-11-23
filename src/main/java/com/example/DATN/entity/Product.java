/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Entity
@Table(name = "Products")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class Product {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer productID;

    @Column(nullable = false, length = 200, columnDefinition = "NVARCHAR(200)")
    private String name;

    @Column(columnDefinition = "NVARCHAR(MAX)")
    private String description;

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @ManyToOne
    @JoinColumn(name = "CategoryID", nullable = false)
    private Category category;

    private String imageURL;

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("price ASC")
    private List<ProductVariant> variants = new ArrayList<>();

    @OneToMany(mappedBy = "product", cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("displayOrder ASC")
    private List<ProductImage> images = new ArrayList<>();

    private LocalDateTime createdDate = LocalDateTime.now();
    private LocalDateTime updatedDate;

    public BigDecimal getMinPrice() {
        if (variants == null || variants.isEmpty()) {
            return price;
        }
        return variants.stream()
                .map(ProductVariant::getPrice)
                .min(BigDecimal::compareTo)
                .orElse(price);
    }

    public BigDecimal getMaxPrice() {
        if (variants == null || variants.isEmpty()) {
            return price;
        }
        return variants.stream()
                .map(ProductVariant::getPrice)
                .max(BigDecimal::compareTo)
                .orElse(price);
    }

    public int getTotalVariantStock() {
        if (variants == null || variants.isEmpty()) {
            return stock != null ? stock : 0;
        }
        return variants.stream().mapToInt(v -> v.getStock() != null ? v.getStock() : 0).sum();
    }

    public List<String> getUniqueSizes() {
        if (variants == null) return List.of();
        return variants.stream()
                .map(ProductVariant::getAttributeSize)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }

    public List<String> getUniqueColors() {
        if (variants == null) return List.of();
        return variants.stream()
                .map(ProductVariant::getAttributeColor)
                .filter(Objects::nonNull)
                .map(String::trim)
                .filter(s -> !s.isEmpty())
                .distinct()
                .collect(Collectors.toList());
    }
}
