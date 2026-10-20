/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller.admin;

import com.example.DATN.entity.User;
import com.example.DATN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import java.util.UUID;
import com.example.DATN.entity.Order;
import com.example.DATN.repository.OrderRepository;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    @Autowired
    private UserRepository userRepository;
    
        @Autowired
    private OrderRepository orderRepository;
    
        @Autowired
    private PasswordEncoder passwordEncoder;

       @GetMapping
    public String list(Model model) {
        List<User> users = userRepository.findAll().stream()
                .filter(u -> !u.getUsername().startsWith("deleted_"))
                .toList();
        model.addAttribute("users", users);
        return "admin/user-manage";
    }

    @PostMapping("/toggle-active/{id}")
    public String toggleActive(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setIsActive(!user.getIsActive());
        userRepository.save(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable Integer id, @RequestParam String role) {
        User user = userRepository.findById(id).orElseThrow();
        user.setRole(role);
        userRepository.save(user);
        return "redirect:/admin/users";
    }
    
           @PostMapping("/approve-delete/{id}")
    @Transactional
    public String approveDelete(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        if (!Boolean.TRUE.equals(user.getDeletionRequested()) || "ADMIN".equals(user.getRole())) {
            return "redirect:/admin/users";
        }

        // Giữ đơn hàng (doanh thu, tồn kho) nhưng gỡ mọi thông tin liên quan tới người dùng
        List<Order> orders = orderRepository.findByUser_UserID(id);
        for (Order o : orders) {
            o.setUser(null);
            o.setShippingAddress(null);
        }
        orderRepository.saveAll(orders);
        orderRepository.flush();

        userRepository.delete(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/reject-delete/{id}")
    public String rejectDelete(@PathVariable Integer id) {
        User user = userRepository.findById(id).orElseThrow();
        user.setDeletionRequested(false);
        user.setDeletionRequestedDate(null);
        user.setDeletionReason(null);
        userRepository.save(user);
        return "redirect:/admin/users";
    }
}
