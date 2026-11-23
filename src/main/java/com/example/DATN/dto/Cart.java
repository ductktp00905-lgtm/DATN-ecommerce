package com.example.DATN.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart implements Serializable {

    private Map<String, CartItem> items = new LinkedHashMap<>();

    public void addItem(CartItem newItem) {
        String key = newItem.getItemKey();
        if (items.containsKey(key)) {
            CartItem existing = items.get(key);
            existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
        } else {
            items.put(key, newItem);
        }
    }

    public void updateQuantity(String key, Integer quantity) {
        if (items.containsKey(key)) {
            if (quantity <= 0) {
                items.remove(key);
            } else {
                items.get(key).setQuantity(quantity);
            }
        }
    }

    // Tương thích ngược với Integer productId
    public void updateQuantity(Integer productId, Integer quantity) {
        updateQuantity(String.valueOf(productId), quantity);
    }

    public void removeItem(String key) {
        items.remove(key);
    }

    // Tương thích ngược với Integer productId
    public void removeItem(Integer productId) {
        // Remove either matching exact key or matching productId
        items.remove(String.valueOf(productId));
        items.entrySet().removeIf(e -> e.getValue().getProductId().equals(productId));
    }

    public void clear() {
        items.clear();
    }

    public BigDecimal getTotalAmount() {
        return items.values().stream()
                .map(CartItem::getSubTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public int getTotalItems() {
        return items.values().stream().mapToInt(CartItem::getQuantity).sum();
    }

    public Map<String, CartItem> getItems() {
        return items;
    }
}
