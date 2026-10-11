/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.repository;

import com.example.DATN.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.math.BigDecimal;
import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Integer> {
    List<Product> findByCategory_CategoryID(Integer categoryId);
    List<Product> findByNameContainingIgnoreCase(String keyword);
    long countByCategory_CategoryID(Integer categoryId);

    @Query("SELECT p FROM Product p WHERE LOWER(p.name) LIKE LOWER(CONCAT('%', :keyword, '%'))"
            + " AND (:categoryId IS NULL OR p.category.categoryID = :categoryId)"
            + " AND (:minPrice IS NULL OR p.price >= :minPrice)"
            + " AND (:maxPrice IS NULL OR p.price <= :maxPrice)")
    List<Product> filter(@Param("keyword") String keyword,
                         @Param("categoryId") Integer categoryId,
                         @Param("minPrice") BigDecimal minPrice,
                         @Param("maxPrice") BigDecimal maxPrice);
}
