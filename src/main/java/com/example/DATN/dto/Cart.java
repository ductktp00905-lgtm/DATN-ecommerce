/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.dto;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

public class Cart implements Serializable {

    private Map<Integer, CartItem> items = new LinkedHashMap<>();

    public void addItem(CartItem newItem) {
        if (items.containsKey(newItem.getProductId())) {
            CartItem existing = items.get(newItem.getProductId());
            existing.setQuantity(existing.getQuantity() + newItem.getQuantity());
        } else {
            items.put(newItem.getProductId(), newItem);
        }
    }

    public void updateQuantity(Integer productId, Integer quantity) {
        if (items.containsKey(productId)) {
            if (quantity <= 0) {
                items.remove(productId);
            } else {
                items.get(productId).setQuantity(quantity);
            }
        }
    }

    public void removeItem(Integer productId) {
        items.remove(productId);
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

    public Map<Integer, CartItem> getItems() {
        return items;
    }
}
