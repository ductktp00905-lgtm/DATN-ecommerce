/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller;

import com.example.DATN.dto.Cart;
import com.example.DATN.entity.Order;
import com.example.DATN.entity.User;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.service.OrderService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.DATN.exception.ResourceNotFoundException;

import java.util.List;

@Controller
@RequestMapping("/orders")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user đang đăng nhập"));
    }

    @GetMapping("/checkout")
public String checkoutPage(HttpSession session, Model model) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItems().isEmpty()) {
        return "redirect:/cart";
    }
    model.addAttribute("cart", cart);
    return "order/checkout";
}

@PostMapping("/checkout")
public String placeOrder(@RequestParam String shippingAddress, @RequestParam String paymentMethod,
                         HttpSession session, Authentication authentication, Model model) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItems().isEmpty()) {
        return "redirect:/cart";
    }
    User user = getCurrentUser(authentication);
    try {
        orderService.checkout(user.getUserID(), cart, shippingAddress.trim(), paymentMethod);
        return "redirect:/orders/history";
    } catch (IllegalStateException | ResourceNotFoundException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("cart", cart);
        return "order/checkout";
    }
}

    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Order> orders = orderService.getOrdersByUser(user.getUserID());
        model.addAttribute("orders", orders);
        return "order/history";
    }
}
