/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.service;

import com.example.DATN.entity.Product;
import java.math.BigDecimal;
import java.util.List;

public interface ProductService {
    List<Product> getAll();
    Product getById(Integer id);
    List<Product> searchByName(String keyword);
    List<Product> getByCategory(Integer categoryId);
    List<Product> filter(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice);
    Product create(Product product);
    Product update(Integer id, Product product);
    void delete(Integer id);
    void reduceStock(Integer productId, Integer quantity);
    void restoreStock(Integer productId, Integer quantity);
    
    List<Product> getRelatedProducts(Integer categoryId, Integer productId);
    List<Product> getBestSellingProducts();

}
