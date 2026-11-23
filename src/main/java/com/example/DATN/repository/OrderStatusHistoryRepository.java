package com.example.DATN.repository;

import com.example.DATN.entity.OrderStatusHistory;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface OrderStatusHistoryRepository extends JpaRepository<OrderStatusHistory, Integer> {
    List<OrderStatusHistory> findByOrder_OrderIDOrderByCreatedAtAsc(Integer orderId);
    List<OrderStatusHistory> findByOrder_OrderIDOrderByCreatedAtDesc(Integer orderId);
}
