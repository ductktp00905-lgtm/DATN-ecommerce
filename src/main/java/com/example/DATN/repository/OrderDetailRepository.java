/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.repository;

import com.example.DATN.entity.OrderDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {

    @Query("SELECT COUNT(od) FROM OrderDetail od "
         + "WHERE od.order.user.userID = :userId "
         + "AND od.product.productID = :productId "
         + "AND od.order.status = 'COMPLETED'")
    long countPurchased(@Param("userId") Integer userId, @Param("productId") Integer productId);
    boolean existsByProduct_ProductID(Integer productId);
}
