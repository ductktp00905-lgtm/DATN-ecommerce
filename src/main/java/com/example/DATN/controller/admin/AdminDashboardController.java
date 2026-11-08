/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller.admin;

import com.example.DATN.entity.Order;
import com.example.DATN.repository.OrderRepository;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import java.util.Comparator;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    /** /admin/overview cũ -> chuyển về trang tổng quan duy nhất. */
    @GetMapping("/overview")
    public String overviewRedirect() {
        return "redirect:/admin/dashboard";
    }

    /** Trang tổng quan: số liệu thống kê (trang chính của khu quản trị). */
    @GetMapping("/dashboard")
    public String overview(Model model) {
        List<Order> orders = orderRepository.findAll();

                BigDecimal totalRevenue = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);       

        long totalOrders = orders.size();
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();

        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
         model.addAttribute("recentOrders", orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(5).toList());

        return "admin/dashboard";
    }
}
