package com.example.DATN.repository;

import com.example.DATN.entity.ProductImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductImageRepository extends JpaRepository<ProductImage, Integer> {
    List<ProductImage> findByProduct_ProductIDOrderByDisplayOrderAsc(Integer productId);
    void deleteByProduct_ProductID(Integer productId);
}
