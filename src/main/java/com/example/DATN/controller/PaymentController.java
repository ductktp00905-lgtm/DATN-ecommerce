package com.example.DATN.controller;

import com.example.DATN.entity.Order;
import com.example.DATN.entity.Payment;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.PaymentRepository;
import com.example.DATN.service.OrderService;
import com.example.DATN.service.VnPayService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private VnPayService vnPayService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping("/vnpay-return")
    public String vnpayReturn(HttpServletRequest request, Authentication authentication, Model model) {
        Map<String, String> params = new HashMap<>();
        request.getParameterMap().forEach((k, v) -> params.put(k, v[0]));

        if (!vnPayService.verify(params)) {
            return result(model, false, "Chữ ký không hợp lệ, giao dịch bị từ chối", null);
        }

        Order order;
        try {
            Integer orderId = Integer.valueOf(params.get("vnp_TxnRef").split("T")[0]);
            order = orderService.getById(orderId);
        } catch (ResourceNotFoundException | NumberFormatException | NullPointerException e) {
            return result(model, false, "Không tìm thấy đơn hàng", null);
        }

        // Đơn phải thuộc về người đang đăng nhập
        if (order.getUser() == null || !order.getUser().getUsername().equals(authentication.getName())) {
            return result(model, false, "Đơn hàng không thuộc tài khoản của bạn", null);
        }

        // Số tiền VNPay trả về phải khớp tổng tiền đơn
        long expected = order.getTotalAmount().multiply(BigDecimal.valueOf(100)).longValue();
        long paidAmount;
        try {
            paidAmount = Long.parseLong(params.get("vnp_Amount"));
        } catch (NumberFormatException e) {
            return result(model, false, "Số tiền giao dịch không hợp lệ", order.getOrderID());
        }
        if (paidAmount != expected) {
            return result(model, false, "Số tiền thanh toán không khớp với đơn hàng", order.getOrderID());
        }

        boolean paid = "00".equals(params.get("vnp_ResponseCode"))
                && "00".equals(params.get("vnp_TransactionStatus"));

        Payment payment = paymentRepository.findByOrder_OrderID(order.getOrderID());
        if (payment != null && "PENDING".equals(payment.getStatus())) {
            if (paid) {
                orderService.markPaymentSuccess(order.getOrderID(), params.get("vnp_TransactionNo"));
            } else {
                orderService.updateStatus(order.getOrderID(), "CANCELLED"); // hoàn kho + hoàn voucher
            }
        } else if (payment != null && "SUCCESS".equals(payment.getStatus())) {
            paid = true; // người dùng tải lại trang kết quả
        }

        return result(model, paid,
                paid ? "Thanh toán thành công. Cảm ơn bạn đã mua hàng!"
                     : "Thanh toán không thành công, đơn hàng đã được hủy.",
                order.getOrderID());
    }

    private String result(Model model, boolean success, String message, Integer orderId) {
        model.addAttribute("success", success);
        model.addAttribute("message", message);
        model.addAttribute("orderId", orderId);
        return "order/payment-result";
    }
}