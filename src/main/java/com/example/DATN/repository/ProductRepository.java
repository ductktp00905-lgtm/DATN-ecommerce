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
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import com.example.DATN.dto.BestSellerDTO;
import java.time.LocalDateTime;
import com.example.DATN.dto.CategoryRevenueDTO;
import com.example.DATN.dto.BestSellerStockDTO;
import com.example.DATN.entity.Product;

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


    @Query("SELECT p FROM Product p " +
           "WHERE p.category.categoryID = :categoryId " +
           "AND p.productID <> :productId " +
           "AND p.stock > 0")
    List<Product> findRelatedProducts(
            @Param("categoryId") Integer categoryId,
            @Param("productId") Integer productId);


    @Query("SELECT od.product FROM OrderDetail od " +
           "WHERE od.order.status = 'COMPLETED' " +
           "GROUP BY od.product " +
           "ORDER BY SUM(od.quantity) DESC")
    List<Product> findBestSellingProducts(Pageable pageable);
    
        @Query("SELECT new com.example.DATN.dto.BestSellerDTO(p.productID, p.name, p.category.categoryName, "
         + "SUM(od.quantity), SUM(od.price * od.quantity)) "
         + "FROM OrderDetail od JOIN od.product p "
         + "WHERE od.order.status = 'COMPLETED' "
         + "AND od.order.orderDate >= :from AND od.order.orderDate < :to "
         + "AND (:categoryId IS NULL OR p.category.categoryID = :categoryId) "
         + "GROUP BY p.productID, p.name, p.category.categoryName "
         + "ORDER BY SUM(od.quantity) DESC")
    List<BestSellerDTO> findBestSellerStats(@Param("from") LocalDateTime from,
                                            @Param("to") LocalDateTime to,
                                            @Param("categoryId") Integer categoryId,
                                            Pageable pageable);
    
        @Query("SELECT new com.example.DATN.dto.CategoryRevenueDTO(p.category.categoryName, "
         + "SUM(od.quantity), SUM(od.price * od.quantity)) "
         + "FROM OrderDetail od JOIN od.product p "
         + "WHERE od.order.status = 'COMPLETED' "
         + "AND od.order.orderDate >= :from AND od.order.orderDate < :to "
         + "GROUP BY p.category.categoryName "
         + "ORDER BY SUM(od.price * od.quantity) DESC")
    List<CategoryRevenueDTO> findRevenueByCategory(@Param("from") LocalDateTime from,
                                                   @Param("to") LocalDateTime to);
    
        @Query("SELECT p FROM Product p "
         + "WHERE (:categoryId IS NULL OR p.category.categoryID = :categoryId) "
         + "ORDER BY p.stock ASC")
    List<Product> findForInventory(@Param("categoryId") Integer categoryId);

    @Query("SELECT new com.example.DATN.dto.BestSellerStockDTO(p.productID, p.name, p.category.categoryName, "
         + "SUM(od.quantity), p.stock) "
         + "FROM OrderDetail od JOIN od.product p "
         + "WHERE od.order.status = 'COMPLETED' "
         + "GROUP BY p.productID, p.name, p.category.categoryName, p.stock "
         + "ORDER BY SUM(od.quantity) DESC")
    List<BestSellerStockDTO> findBestSellerStock(Pageable pageable);
}

