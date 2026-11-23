package com.example.DATN.controller.admin;

import com.example.DATN.entity.Order;
import com.example.DATN.entity.OrderDetail;
import com.example.DATN.entity.OrderStatusHistory;
import com.example.DATN.entity.Payment;
import com.example.DATN.repository.PaymentRepository;
import com.example.DATN.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.security.Principal;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/admin/orders")
public class AdminOrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    private PaymentRepository paymentRepository;

    @GetMapping
    public String list(@RequestParam(required = false, defaultValue = "ALL") String status, Model model) {
        List<Order> orders;
        if (status != null && !status.equalsIgnoreCase("ALL") && !status.trim().isEmpty()) {
            orders = orderService.getOrdersByStatus(status.trim().toUpperCase());
        } else {
            orders = orderService.getAllPrioritized();
        }

        model.addAttribute("orders", orders);
        model.addAttribute("currentStatus", status != null ? status.toUpperCase() : "ALL");
        model.addAttribute("pendingCount", orderService.countByStatus("PENDING"));
        model.addAttribute("shippingCount", orderService.countByStatus("SHIPPING"));
        model.addAttribute("completedCount", orderService.countByStatus("COMPLETED"));
        model.addAttribute("cancelledCount", orderService.countByStatus("CANCELLED"));
        model.addAttribute("allCount", orderService.getAllPrioritized().size());

        return "admin/order-manage";
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Order order = orderService.getById(id);
        Payment payment = paymentRepository.findByOrder_OrderID(id);
        List<OrderStatusHistory> histories = orderService.getOrderHistories(id);

        BigDecimal subtotal = BigDecimal.ZERO;
        if (order.getOrderDetails() != null) {
            for (OrderDetail d : order.getOrderDetails()) {
                if (d.getPrice() != null && d.getQuantity() != null) {
                    subtotal = subtotal.add(d.getPrice().multiply(BigDecimal.valueOf(d.getQuantity())));
                }
            }
        }

        model.addAttribute("order", order);
        model.addAttribute("payment", payment);
        model.addAttribute("histories", histories);
        model.addAttribute("subtotal", subtotal);

        return "admin/order-detail";
    }

    @PostMapping("/update-status/{id}")
    public String updateStatus(@PathVariable Integer id,
                               @RequestParam String status,
                               @RequestParam(required = false) String note,
                               @RequestParam(required = false, defaultValue = "ALL") String currentFilter,
                               Principal principal,
                               RedirectAttributes ra) {
        String adminUser = principal != null ? principal.getName() : "admin";
        try {
            orderService.updateStatus(id, status, adminUser, note);
            ra.addFlashAttribute("successMessage", "Đã cập nhật trạng thái đơn hàng #" + id + " thành công!");
        } catch (IllegalStateException e) {
            ra.addFlashAttribute("errorMessage", e.getMessage());
        }
        if ("DETAIL".equalsIgnoreCase(currentFilter)) {
            return "redirect:/admin/orders/" + id;
        }
        return "redirect:/admin/orders?status=" + currentFilter;
    }

    @PostMapping("/{id}/add-note")
    public String addNote(@PathVariable Integer id,
                          @RequestParam String note,
                          Principal principal,
                          RedirectAttributes ra) {
        if (note == null || note.trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Nội dung ghi chú không được để trống");
            return "redirect:/admin/orders/" + id;
        }
        String adminUser = principal != null ? principal.getName() : "admin";
        orderService.addOrderNote(id, adminUser, note.trim());
        ra.addFlashAttribute("successMessage", "Đã lưu ghi chú tiến độ thành công!");
        return "redirect:/admin/orders/" + id;
    }

    @PostMapping("/{id}/mark-paid")
    @Transactional
    public String markPaid(@PathVariable Integer id, Principal principal, RedirectAttributes ra) {
        String adminUser = principal != null ? principal.getName() : "admin";
        Payment payment = paymentRepository.findByOrder_OrderID(id);
        if (payment == null) {
            payment = new Payment();
            payment.setOrder(orderService.getById(id));
            payment.setMethod("CASH");
        }
        payment.setStatus("SUCCESS");
        payment.setPaymentDate(LocalDateTime.now());
        String code = "CASH_" + System.currentTimeMillis();
        payment.setTransactionCode(code);
        paymentRepository.save(payment);

        orderService.addOrderNote(id, adminUser, "Xác nhận đã thu đủ tiền mặt (COD). Mã biên nhận: " + code);

        ra.addFlashAttribute("successMessage", "Đã xác nhận thu tiền thành công cho đơn hàng #" + id + "!");
        return "redirect:/admin/orders/" + id;
    }
}
