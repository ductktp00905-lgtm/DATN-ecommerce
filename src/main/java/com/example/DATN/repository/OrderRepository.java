package com.example.DATN.repository;

import com.example.DATN.entity.Order;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderRepository extends JpaRepository<Order, Integer> {
    List<Order> findByUser_UserID(Integer userId);
    List<Order> findByUser_UserIDOrderByOrderDateDesc(Integer userId);

    @Query("SELECT o FROM Order o ORDER BY " +
           "CASE WHEN o.status = 'PENDING' THEN 1 " +
           "     WHEN o.status = 'SHIPPING' THEN 2 " +
           "     WHEN o.status = 'COMPLETED' THEN 3 " +
           "     ELSE 4 END ASC, " +
           "o.orderDate DESC")
    List<Order> findAllPrioritized();

    List<Order> findByStatusOrderByOrderDateDesc(String status);

    long countByStatus(String status);
}
