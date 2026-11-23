/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller;

import com.example.DATN.dto.Cart;
import com.example.DATN.entity.Order;
import com.example.DATN.entity.User;
import com.example.DATN.entity.UserAddress;
import com.example.DATN.entity.Voucher;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.UserAddressRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.service.OrderService;
import com.example.DATN.service.VnPayService;
import com.example.DATN.service.VoucherService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

    @Autowired
    private UserAddressRepository userAddressRepository;

    private User getCurrentUser(Authentication authentication) {
        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalStateException("Không tìm thấy user đang đăng nhập"));
    }

    private void populateAddresses(User user, Model model) {
        List<UserAddress> addresses = userAddressRepository.findByUser_UserIDOrderByIsDefaultDescCreatedAtDesc(user.getUserID());
        UserAddress selectedAddress = addresses.stream()
                .filter(a -> Boolean.TRUE.equals(a.getIsDefault()))
                .findFirst()
                .orElse(addresses.isEmpty() ? null : addresses.get(0));

        model.addAttribute("currentUser", user);
        model.addAttribute("addresses", addresses);
        model.addAttribute("selectedAddress", selectedAddress);
    }

    @GetMapping("/checkout")
    public String checkoutPage(HttpSession session, Authentication authentication, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        User user = getCurrentUser(authentication);
        populateAddresses(user, model);
        model.addAttribute("cart", cart);
        return "order/checkout";
    }

    @PostMapping("/address/quick-add")
    @Transactional
    public String quickAddAddress(@RequestParam String recipientName,
                                  @RequestParam String phone,
                                  @RequestParam String detailAddress,
                                  @RequestParam(defaultValue = "Nhà riêng") String addressLabel,
                                  @RequestParam(defaultValue = "false") boolean isDefault,
                                  Authentication authentication,
                                  RedirectAttributes ra) {
        User user = getCurrentUser(authentication);
        recipientName = recipientName.trim();
        phone = phone.trim();
        detailAddress = detailAddress.trim();

        if (recipientName.isEmpty() || phone.isEmpty() || detailAddress.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Vui lòng nhập đầy đủ họ tên, SĐT và địa chỉ chi tiết");
            return "redirect:/orders/checkout";
        }

        if (isDefault || userAddressRepository.countByUser_UserID(user.getUserID()) == 0) {
            userAddressRepository.clearDefaultForUser(user.getUserID());
            isDefault = true;
        }

        UserAddress addr = new UserAddress();
        addr.setUser(user);
        addr.setRecipientName(recipientName);
        addr.setPhone(phone);
        addr.setDetailAddress(detailAddress);
        addr.setAddressLabel(addressLabel != null && !addressLabel.isBlank() ? addressLabel.trim() : "Nhà riêng");
        addr.setIsDefault(isDefault);
        addr.setCreatedAt(LocalDateTime.now());
        userAddressRepository.save(addr);

        ra.addFlashAttribute("successMessage", "Đã thêm địa chỉ giao hàng mới");
        return "redirect:/orders/checkout";
    }

    @PostMapping("/checkout")
    public String placeOrder(@RequestParam String shippingAddress,
                             @RequestParam String paymentMethod,
                             @RequestParam(required = false) String voucherCode,
                             @RequestParam(required = false) String orderNote,
                             HttpServletRequest request,
                             HttpSession session, Authentication authentication, Model model) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null || cart.getItems().isEmpty()) {
            return "redirect:/cart";
        }
        User user = getCurrentUser(authentication);
        if (!"CASH".equals(paymentMethod) && !"VNPAY".equals(paymentMethod)) {
            model.addAttribute("errorMessage", "Phương thức thanh toán không hợp lệ");
            model.addAttribute("cart", cart);
            model.addAttribute("voucherCode", voucherCode);
            populateAddresses(user, model);
            return "order/checkout";
        }
        try {
            String fullAddress = shippingAddress != null ? shippingAddress.trim() : "";
            if (orderNote != null && !orderNote.trim().isEmpty()) {
                fullAddress += " (Ghi chú: " + orderNote.trim() + ")";
            }

            Order order = orderService.checkout(user.getUserID(), cart, fullAddress, paymentMethod, voucherCode);
            if ("VNPAY".equals(paymentMethod)) {
                return "redirect:" + vnPayService.createPaymentUrl(order, request.getRemoteAddr());
            }
            return "redirect:/orders/history";
        } catch (IllegalStateException | ResourceNotFoundException e) {
            model.addAttribute("errorMessage", e.getMessage());
            model.addAttribute("cart", cart);
            model.addAttribute("voucherCode", voucherCode);
            populateAddresses(user, model);
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
