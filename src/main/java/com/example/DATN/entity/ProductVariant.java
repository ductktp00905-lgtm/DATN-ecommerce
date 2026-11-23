package com.example.DATN.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;

@Entity
@Table(name = "product_variants")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class ProductVariant {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "variantid")
    private Integer variantID;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "productid", nullable = false)
    @JsonIgnore
    private Product product;

    private String sku;

    @Column(name = "variant_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String variantName;

    @Column(name = "attribute_size", length = 50, columnDefinition = "NVARCHAR(50)")
    private String attributeSize; // Kích thước màn hình, công suất, dung tích

    @Column(name = "attribute_color", length = 50, columnDefinition = "NVARCHAR(50)")
    private String attributeColor; // Màu sắc

    @Column(name = "color_hex", length = 20)
    private String colorHex; // Mã màu hex (e.g. #000000, #FFFFFF)

    @Column(nullable = false, precision = 18, scale = 2)
    private BigDecimal price;

    @Column(nullable = false)
    private Integer stock = 0;

    @Column(name = "image_url")
    private String imageURL;

    @Column(name = "is_default")
    private Boolean isDefault = false;
}
