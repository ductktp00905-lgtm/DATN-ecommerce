/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.service.impl;

import com.example.DATN.entity.Product;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Service
public class ProductServiceImpl implements ProductService {
    
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private com.example.DATN.repository.ProductVariantRepository variantRepository;

    @Override
    public List<Product> getAll() {
        return productRepository.findAll();
    }

    @Override
    public Product getById(Integer id) {
        return productRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy sản phẩm với ID: " + id));
    }

    @Override
    public List<Product> searchByName(String keyword) {
        return productRepository.findByNameContainingIgnoreCase(keyword);
    }

    @Override
    public List<Product> getByCategory(Integer categoryId) {
        return productRepository.findByCategory_CategoryID(categoryId);
    }

    @Override
    public List<Product> filter(String keyword, Integer categoryId, BigDecimal minPrice, BigDecimal maxPrice) {
        // Người dùng nhập ngược (từ > đến) thì tự đảo lại
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            BigDecimal tmp = minPrice;
            minPrice = maxPrice;
            maxPrice = tmp;
        }
        String kw = (keyword == null) ? "" : keyword.trim();
        return productRepository.filter(kw, categoryId, minPrice, maxPrice);
    }

    @Override
    public Product create(Product product) {
        product.setCreatedDate(LocalDateTime.now());
        return productRepository.save(product);
    }

    @Override
    public Product update(Integer id, Product product) {
        Product existing = getById(id);
        existing.setName(product.getName());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setCategory(product.getCategory());
        existing.setImageURL(product.getImageURL());
        existing.setUpdatedDate(LocalDateTime.now());
        return productRepository.save(existing);
    }

    @Override
    public void delete(Integer id) {
        Product existing = getById(id);
        productRepository.delete(existing);
    }

    @Override
    public void reduceStock(Integer productId, Integer quantity) {
        reduceStock(productId, null, quantity);
    }

    @Override
    public void reduceStock(Integer productId, Integer variantId, Integer quantity) {
        Product product = getById(productId);
        if (variantId != null) {
            com.example.DATN.entity.ProductVariant variant = variantRepository.findById(variantId).orElse(null);
            if (variant != null) {
                if (variant.getStock() < quantity) {
                    throw new IllegalStateException("Phiên bản '" + variant.getVariantName() + "' không đủ số lượng tồn kho");
                }
                variant.setStock(variant.getStock() - quantity);
                variantRepository.save(variant);
            }
        }
        if (product.getStock() < quantity) {
            throw new IllegalStateException("Sản phẩm '" + product.getName() + "' không đủ tồn kho");
        }
        product.setStock(product.getStock() - quantity);
        productRepository.save(product);
    }

    @Override
    public List<Product> getRelatedProducts(Integer categoryId, Integer productId) {
        return productRepository.findRelatedProducts(categoryId, productId);
    }

    @Override
    public List<Product> getBestSellingProducts() {
        return productRepository.findBestSellingProducts(
                org.springframework.data.domain.PageRequest.of(0, 4));
    }

    @Override
    public void restoreStock(Integer productId, Integer quantity) {
        restoreStock(productId, null, quantity);
    }

    @Override
    public void restoreStock(Integer productId, Integer variantId, Integer quantity) {
        Product product = getById(productId);
        if (variantId != null) {
            com.example.DATN.entity.ProductVariant variant = variantRepository.findById(variantId).orElse(null);
            if (variant != null) {
                variant.setStock(variant.getStock() + quantity);
                variantRepository.save(variant);
            }
        }
        product.setStock(product.getStock() + quantity);
        productRepository.save(product);
    }
}
