package com.example.DATN.service;

import com.example.DATN.dto.Cart;
import com.example.DATN.entity.Order;
import com.example.DATN.entity.OrderStatusHistory;
import java.util.List;

public interface OrderService {
    Order checkout(Integer userId, Cart cart, String shippingAddress, String paymentMethod, String voucherCode);
    List<Order> getOrdersByUser(Integer userId);
    Order getById(Integer orderId);
    Order updateStatus(Integer orderId, String status);
    Order updateStatus(Integer orderId, String status, String updatedBy, String note);
    void addOrderNote(Integer orderId, String updatedBy, String note);
    List<OrderStatusHistory> getOrderHistories(Integer orderId);
    List<Order> getAll();
    List<Order> getAllPrioritized();
    List<Order> getOrdersByStatus(String status);
    long countByStatus(String status);
    void markPaymentSuccess(Integer orderId, String transactionCode);
}
