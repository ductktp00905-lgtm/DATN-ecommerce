/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller;

import jakarta.servlet.http.HttpServletRequest;
import com.example.DATN.service.VnPayService;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import com.example.DATN.entity.Voucher;
import com.example.DATN.service.VoucherService;
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
    
    @Autowired
    private VoucherService voucherService;
    
    @Autowired
    private VnPayService vnPayService;

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
                         @RequestParam(required = false) String voucherCode,
                         HttpServletRequest request,
                         HttpSession session, Authentication authentication, Model model) {
    Cart cart = (Cart) session.getAttribute("cart");
    if (cart == null || cart.getItems().isEmpty()) {
        return "redirect:/cart";
    }
    User user = getCurrentUser(authentication);
    try {
        Order order = orderService.checkout(user.getUserID(), cart, shippingAddress.trim(), paymentMethod, voucherCode);
        if ("VNPAY".equals(paymentMethod)) {
            return "redirect:" + vnPayService.createPaymentUrl(order, request.getRemoteAddr());
        }
        return "redirect:/orders/history";
    } catch (IllegalStateException | ResourceNotFoundException e) {
        model.addAttribute("errorMessage", e.getMessage());
        model.addAttribute("cart", cart);
        model.addAttribute("voucherCode", voucherCode);
        return "order/checkout";
    }
}

    @GetMapping("/voucher/check")
    @ResponseBody
    public Map<String, Object> checkVoucher(@RequestParam String code, HttpSession session) {
        Map<String, Object> result = new HashMap<>();
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty()) {
            result.put("valid", false);
            result.put("message", "Giỏ hàng đang trống");
            return result;
        }
        try {
            BigDecimal total = cart.getTotalAmount();
            Voucher v = voucherService.validate(code, total);
            BigDecimal discount = voucherService.calculateDiscount(v, total);
            result.put("valid", true);
            result.put("message", "Áp dụng mã " + v.getCode() + " thành công");
            result.put("discount", discount);
            result.put("finalTotal", total.subtract(discount));
        } catch (IllegalStateException e) {
            result.put("valid", false);
            result.put("message", e.getMessage());
        }
        return result;
    }
    
    @GetMapping("/history")
    public String history(Authentication authentication, Model model) {
        User user = getCurrentUser(authentication);
        List<Order> orders = orderService.getOrdersByUser(user.getUserID());
        model.addAttribute("orders", orders);
        return "order/history";
    }
}
