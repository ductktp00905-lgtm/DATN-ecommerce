package com.example.DATN.repository;

import com.example.DATN.entity.ProductVariant;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProductVariantRepository extends JpaRepository<ProductVariant, Integer> {
    List<ProductVariant> findByProduct_ProductIDOrderByPriceAsc(Integer productId);
    void deleteByProduct_ProductID(Integer productId);
}
