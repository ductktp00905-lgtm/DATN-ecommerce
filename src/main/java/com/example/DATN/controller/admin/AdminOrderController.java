/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller.admin;

import com.example.DATN.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("orders", orderService.getAll());
        return "admin/order-manage";
    }

    @PostMapping("/update-status/{id}")
public String updateStatus(@PathVariable Integer id, @RequestParam String status, RedirectAttributes ra) {
    try {
        orderService.updateStatus(id, status);
    } catch (IllegalStateException e) {
        ra.addFlashAttribute("errorMessage", e.getMessage());
    }
    return "redirect:/admin/orders";
}
    
}
