package com.example.DATN.service.impl;

import com.example.DATN.repository.VoucherRepository;
import com.example.DATN.service.VoucherService;
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
    
    @Autowired
    private VoucherService voucherService;

    @Autowired
    private VoucherRepository voucherRepository;

    @Override
    @Transactional
        public Order checkout(Integer userId, Cart cart, String shippingAddress, String paymentMethod, String voucherCode) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalStateException("Vui lòng nhập địa chỉ nhận hàng");
        }
        shippingAddress = shippingAddress.trim();

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Order order = new Order();
        order.setUser(user);
        order.setStatus("PENDING");
        order.setShippingAddress(shippingAddress);
        
        List<OrderDetail> details = new ArrayList<>();
        java.math.BigDecimal total = java.math.BigDecimal.ZERO;
        for (CartItem item : cart.getItems().values()) {
            Product product = productService.getById(item.getProductId());
            productService.reduceStock(product.getProductID(), item.getQuantity());

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setQuantity(item.getQuantity());
            detail.setPrice(product.getPrice());   // giá hiện tại
            details.add(detail);

            total = total.add(product.getPrice().multiply(java.math.BigDecimal.valueOf(item.getQuantity())));
        }

                if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Voucher voucher = voucherService.validate(voucherCode, total);
            if (voucherRepository.increaseUsedCount(voucher.getVoucherID()) == 0) {
                throw new IllegalStateException("Mã khuyến mãi đã hết lượt sử dụng");
            }
            java.math.BigDecimal discount = voucherService.calculateDiscount(voucher, total);
            order.setVoucher(voucher);
            order.setDiscountAmount(discount);
            total = total.subtract(discount);
        }
        order.setTotalAmount(total);
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
        if (!List.of("PENDING", "SHIPPING", "COMPLETED", "CANCELLED").contains(status)) {
            throw new IllegalStateException("Trạng thái không hợp lệ");
        }
        Order order = getById(orderId);
        String old = order.getStatus();
        if (old.equals(status)) {
            return order;
        }
        if ("CANCELLED".equals(old)) {
            throw new IllegalStateException("Đơn #" + orderId + " đã hủy, không thể đổi trạng thái");
        }
        if ("COMPLETED".equals(old)) {
            throw new IllegalStateException("Đơn #" + orderId + " đã hoàn thành, không thể đổi trạng thái");
        }

        // Hủy đơn -> hoàn lại tồn kho
        if ("CANCELLED".equals(status)) {
            for (OrderDetail d : order.getOrderDetails()) {
                productService.restoreStock(d.getProduct().getProductID(), d.getQuantity());
            }
                        if (order.getVoucher() != null) {
                voucherRepository.decreaseUsedCount(order.getVoucher().getVoucherID());
            }
            Payment payment = paymentRepository.findByOrder_OrderID(orderId);
            if (payment != null) {
                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
            }
        }

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
    @Transactional
    public void markPaymentSuccess(Integer orderId, String transactionCode) {
        Payment payment = paymentRepository.findByOrder_OrderID(orderId);
        if (payment != null && "PENDING".equals(payment.getStatus())) {
            payment.setStatus("SUCCESS");
            payment.setTransactionCode(transactionCode);
            payment.setPaymentDate(LocalDateTime.now());
            paymentRepository.save(payment);
        }
    }
    @Override
    public List<Order> getAll() {
        return orderRepository.findAll();
    }
}
