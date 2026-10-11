/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller.admin;

import com.example.DATN.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/system")
public class AdminSystemController {

    @Autowired private PaymentRepository paymentRepository;
    @Autowired private OrderDetailRepository orderDetailRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private ProductRepository productRepository;
    @Autowired private CategoryRepository categoryRepository;
    @Autowired private UserRepository userRepository;

    @GetMapping("/reset")
    public String resetPage() {
        return "admin/system-reset";
    }

    @PostMapping("/reset-all")
    @Transactional
    public String resetAllData() {
        paymentRepository.deleteAllInBatch();
        orderDetailRepository.deleteAllInBatch();
        orderRepository.deleteAllInBatch();
        productRepository.deleteAllInBatch();
        categoryRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();
        return "redirect:/admin/system/reset?success=true";
    }
}
