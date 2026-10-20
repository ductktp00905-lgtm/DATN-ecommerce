/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.repository;
import com.example.DATN.entity.ProductReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 *
 * @author Admin
 */

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Integer> {

    List<ProductReview> findByProduct_ProductIDOrderByCreatedDateDesc(
        Integer productId
    );

    Optional<ProductReview> findByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );

    boolean existsByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );

    long countByProduct_ProductID(Integer productId);
}
