/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.service.impl;

import com.example.DATN.dto.Cart;
import com.example.DATN.dto.CartItem;
import com.example.DATN.entity.*;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.OrderRepository;
import com.example.DATN.repository.PaymentRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.service.OrderService;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductService productService;

    @Autowired
    private PaymentRepository paymentRepository;

    @Override
    @Transactional
    public Order checkout(Integer userId, Cart cart, String shippingAddress, String paymentMethod) {

        if (cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Order order = new Order();
        order.setUser(user);
        order.setTotalAmount(cart.getTotalAmount());
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);

        List<OrderDetail> details = new ArrayList<>();
        for (CartItem item : cart.getItems().values()) {

            productService.reduceStock(item.getProductId(), item.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(productService.getById(item.getProductId()));
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice());
            details.add(detail);
        }
        order.setOrderDetails(details);

        Order saved = orderRepository.save(order);

        Payment payment = new Payment();
        payment.setOrder(saved);
        payment.setMethod(paymentMethod);
        payment.setStatus("PENDING");
        paymentRepository.save(payment);

        cart.clear();
        return saved;
    }

    @Override
    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUser_UserID(userId);
    }

    @Override
    public Order getById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng"));
    }

    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String status) {
        Order order = getById(orderId);
        order.setStatus(status);
        Order updated = orderRepository.save(order);

        if ("COMPLETED".equals(status)) {
            Payment payment = paymentRepository.findByOrder_OrderID(orderId);
            if (payment != null) {
                payment.setStatus("SUCCESS");
                payment.setPaymentDate(LocalDateTime.now());
                paymentRepository.save(payment);
            }
        }

        return updated;
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }
}