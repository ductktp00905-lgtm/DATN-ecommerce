/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.service;

import com.example.DATN.dto.Cart;
import com.example.DATN.entity.Order;
import java.util.List;

public interface OrderService {
        Order checkout(Integer userId, Cart cart, String shippingAddress, String paymentMethod, String voucherCode);
    List<Order> getOrdersByUser(Integer userId);
    Order getById(Integer orderId);
    Order updateStatus(Integer orderId, String status);
    List<Order> getAll();
        void markPaymentSuccess(Integer orderId, String transactionCode);
}
