/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.service;

import com.example.DATN.entity.Product;
import java.util.List;

public interface ProductService {
    List<Product> getAll();
    Product getById(Integer id);
    List<Product> searchByName(String keyword);
    List<Product> getByCategory(Integer categoryId);
    Product create(Product product);
    Product update(Integer id, Product product);
    void delete(Integer id);
    void reduceStock(Integer productId, Integer quantity);
}
