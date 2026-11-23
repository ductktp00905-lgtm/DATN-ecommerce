package com.example.DATN.dto;

import java.io.Serializable;
import java.math.BigDecimal;

public class CartItem implements Serializable {
    private Integer productId;
    private Integer variantId;
    private String productName;
    private String variantName;
    private BigDecimal price;
    private Integer quantity;
    private String imageURL;

    public CartItem() {}

    public CartItem(Integer productId, Integer variantId, String productName, String variantName, BigDecimal price, Integer quantity, String imageURL) {
        this.productId = productId;
        this.variantId = variantId;
        this.productName = productName;
        this.variantName = variantName;
        this.price = price;
        this.quantity = quantity;
        this.imageURL = imageURL;
    }

    // Constructor tương thích ngược
    public CartItem(Integer productId, String productName, BigDecimal price, Integer quantity, String imageURL) {
        this(productId, null, productName, null, price, quantity, imageURL);
    }

    public String getItemKey() {
        return productId + (variantId != null ? "_" + variantId : "");
    }

    public BigDecimal getSubTotal() {
        return price != null ? price.multiply(BigDecimal.valueOf(quantity)) : BigDecimal.ZERO;
    }

    public Integer getProductId() { return productId; }
    public void setProductId(Integer productId) { this.productId = productId; }

    public Integer getVariantId() { return variantId; }
    public void setVariantId(Integer variantId) { this.variantId = variantId; }

    public String getProductName() { return productName; }
    public void setProductName(String productName) { this.productName = productName; }

    public String getVariantName() { return variantName; }
    public void setVariantName(String variantName) { this.variantName = variantName; }

    public BigDecimal getPrice() { return price; }
    public void setPrice(BigDecimal price) { this.price = price; }

    public Integer getQuantity() { return quantity; }
    public void setQuantity(Integer quantity) { this.quantity = quantity; }

    public String getImageURL() { return imageURL; }
    public void setImageURL(String imageURL) { this.imageURL = imageURL; }

    public String getImageUrl() { return imageURL; }
    public void setImageUrl(String imageUrl) { this.imageURL = imageUrl; }
}
