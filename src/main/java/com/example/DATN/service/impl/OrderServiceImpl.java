package com.example.DATN.service.impl;

import com.example.DATN.dto.Cart;
import com.example.DATN.dto.CartItem;
import com.example.DATN.entity.*;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.OrderRepository;
import com.example.DATN.repository.OrderStatusHistoryRepository;
import com.example.DATN.repository.PaymentRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.repository.VoucherRepository;
import com.example.DATN.service.OrderService;
import com.example.DATN.service.ProductService;
import com.example.DATN.service.VoucherService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
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

    @Autowired
    private OrderStatusHistoryRepository orderStatusHistoryRepository;

    @Override
    @Transactional
    public Order checkout(Integer userId, Cart cart, String shippingAddress, String paymentMethod, String voucherCode) {
        if (cart == null || cart.getItems().isEmpty()) {
            throw new IllegalStateException("Giỏ hàng đang trống");
        }
        if (shippingAddress == null || shippingAddress.trim().isEmpty()) {
            throw new IllegalStateException("Vui lòng nhập địa chỉ nhận hàng");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy người dùng"));

        Order order = new Order();
        order.setUser(user);
        order.setShippingAddress(shippingAddress.trim());
        order.setStatus("PENDING");
        order.setOrderDate(LocalDateTime.now());

        List<OrderDetail> details = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;

        for (CartItem item : cart.getItems().values()) {
            Product product = productService.getById(item.getProductId());
            if (item.getVariantId() != null) {
                productService.reduceStock(product.getProductID(), item.getVariantId(), item.getQuantity());
            } else {
                productService.reduceStock(product.getProductID(), item.getQuantity());
            }

            OrderDetail detail = new OrderDetail();
            detail.setOrder(order);
            detail.setProduct(product);
            detail.setVariantID(item.getVariantId());
            detail.setVariantInfo(item.getVariantName());
            detail.setQuantity(item.getQuantity());
            detail.setPrice(item.getPrice() != null ? item.getPrice() : product.getPrice());
            details.add(detail);

            total = total.add(detail.getPrice().multiply(BigDecimal.valueOf(item.getQuantity())));
        }

        if (voucherCode != null && !voucherCode.trim().isEmpty()) {
            Voucher voucher = voucherService.validate(voucherCode, total);
            if (voucherRepository.increaseUsedCount(voucher.getVoucherID()) == 0) {
                throw new IllegalStateException("Mã khuyến mãi đã hết lượt sử dụng");
            }
            BigDecimal discount = voucherService.calculateDiscount(voucher, total);
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

        // Lưu lịch sử khởi tạo đơn hàng
        OrderStatusHistory initHistory = new OrderStatusHistory();
        initHistory.setOrder(saved);
        initHistory.setFromStatus(null);
        initHistory.setToStatus("PENDING");
        initHistory.setUpdatedBy(user.getFullName() != null && !user.getFullName().isBlank() ? user.getFullName() : user.getUsername());
        initHistory.setActionTitle("Khách đặt hàng thành công");
        initHistory.setNote("Đơn hàng được khởi tạo qua website. Hình thức thanh toán: "
                + ("CASH".equalsIgnoreCase(paymentMethod) ? "Tiền mặt khi nhận hàng (COD)" : "Chuyển khoản online"));
        initHistory.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(initHistory);

        cart.clear();
        return saved;
    }

    @Override
    public List<Order> getOrdersByUser(Integer userId) {
        return orderRepository.findByUser_UserIDOrderByOrderDateDesc(userId);
    }

    @Override
    public Order getById(Integer orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy đơn hàng #" + orderId));
    }

    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String status) {
        return updateStatus(orderId, status, "Quản trị viên", null);
    }

    @Override
    @Transactional
    public Order updateStatus(Integer orderId, String status, String updatedBy, String note) {
        if (!List.of("PENDING", "SHIPPING", "COMPLETED", "CANCELLED").contains(status)) {
            throw new IllegalStateException("Trạng thái không hợp lệ: " + status);
        }

        Order order = getById(orderId);
        String old = order.getStatus();
        if (old.equals(status)) {
            return order;
        }

        // Khóa cứng các trạng thái kết thúc (Terminal State) để chống gian lận & sai lệch kho
        if ("CANCELLED".equals(old)) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " đã bị hủy, không thể thay đổi trạng thái!");
        }
        if ("COMPLETED".equals(old)) {
            throw new IllegalStateException("Đơn hàng #" + orderId + " đã hoàn thành thành công, không thể thay đổi trạng thái!");
        }

        // Quy tắc State Machine kiểm soát luồng di chuyển trạng thái
        if ("PENDING".equals(old)) {
            if (!"SHIPPING".equals(status) && !"CANCELLED".equals(status)) {
                throw new IllegalStateException("Từ trạng thái 'Chờ xử lý', đơn hàng phải được chuyển sang 'Đang giao hàng' hoặc 'Hủy đơn'!");
            }
        } else if ("SHIPPING".equals(old)) {
            if (!"COMPLETED".equals(status) && !"CANCELLED".equals(status)) {
                throw new IllegalStateException("Từ trạng thái 'Đang giao hàng', đơn hàng chỉ có thể chuyển sang 'Giao thành công' hoặc 'Hủy/Hoàn hàng'!");
            }
        }

        String actionTitle;
        if ("SHIPPING".equals(status)) {
            actionTitle = "Bàn giao vận chuyển & Bắt đầu giao hàng";
            if (note == null || note.isBlank()) {
                note = "Kỹ thuật viên FlareHub đã xuất kho và đang trên đường giao thiết bị đến khách hàng.";
            }
        } else if ("COMPLETED".equals(status)) {
            actionTitle = "Giao hàng thành công & Hoàn tất đơn";
            if (note == null || note.isBlank()) {
                note = "Khách hàng đã nhận máy, kiểm tra nguyên seal và hoàn tất đơn hàng.";
            }
            // Tự động đồng bộ thanh toán nếu chưa thanh toán (đơn COD)
            Payment payment = paymentRepository.findByOrder_OrderID(orderId);
            if (payment != null && "PENDING".equals(payment.getStatus())) {
                payment.setStatus("SUCCESS");
                payment.setPaymentDate(LocalDateTime.now());
                if (payment.getTransactionCode() == null || payment.getTransactionCode().isBlank()) {
                    payment.setTransactionCode("SETTLED_ON_DELIVERY_" + System.currentTimeMillis());
                }
                paymentRepository.save(payment);
            }
        } else if ("CANCELLED".equals(status)) {
            actionTitle = "Hủy đơn hàng & Hoàn trả tồn kho";
            if (note == null || note.isBlank()) {
                note = "Đơn hàng đã hủy bởi quản trị viên. Toàn bộ thiết bị đã được tự động hoàn lại kho.";
            }
            // Hoàn lại tồn kho an toàn cho từng biến thể
            for (OrderDetail d : order.getOrderDetails()) {
                productService.restoreStock(d.getProduct().getProductID(), d.getVariantID(), d.getQuantity());
            }
            if (order.getVoucher() != null) {
                voucherRepository.decreaseUsedCount(order.getVoucher().getVoucherID());
            }
            Payment payment = paymentRepository.findByOrder_OrderID(orderId);
            if (payment != null) {
                payment.setStatus("CANCELLED");
                paymentRepository.save(payment);
            }
        } else {
            actionTitle = "Cập nhật trạng thái sang " + status;
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);

        // Ghi lại lịch sử thay đổi trạng thái
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(updated);
        history.setFromStatus(old);
        history.setToStatus(status);
        history.setUpdatedBy(updatedBy != null && !updatedBy.isBlank() ? updatedBy : "Quản trị viên");
        history.setActionTitle(actionTitle);
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);

        return updated;
    }

    @Override
    @Transactional
    public void addOrderNote(Integer orderId, String updatedBy, String note) {
        Order order = getById(orderId);
        OrderStatusHistory history = new OrderStatusHistory();
        history.setOrder(order);
        history.setFromStatus(order.getStatus());
        history.setToStatus(order.getStatus());
        history.setUpdatedBy(updatedBy != null && !updatedBy.isBlank() ? updatedBy : "Quản trị viên");
        history.setActionTitle("Ghi chú tiến độ");
        history.setNote(note);
        history.setCreatedAt(LocalDateTime.now());
        orderStatusHistoryRepository.save(history);
    }

    @Override
    public List<OrderStatusHistory> getOrderHistories(Integer orderId) {
        return orderStatusHistoryRepository.findByOrder_OrderIDOrderByCreatedAtDesc(orderId);
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

            // Ghi vết vào lịch sử
            Order order = getById(orderId);
            OrderStatusHistory history = new OrderStatusHistory();
            history.setOrder(order);
            history.setFromStatus(order.getStatus());
            history.setToStatus(order.getStatus());
            history.setUpdatedBy("Cổng thanh toán");
            history.setActionTitle("Xác nhận thanh toán thành công");
            history.setNote("Đã nhận đủ tiền qua cổng thanh toán / tiền mặt. Mã GD: " + transactionCode);
            history.setCreatedAt(LocalDateTime.now());
            orderStatusHistoryRepository.save(history);
        }
    }

    @Override
    public List<Order> getAll() {
        return orderRepository.findAllPrioritized();
    }

    @Override
    public List<Order> getAllPrioritized() {
        return orderRepository.findAllPrioritized();
    }

    @Override
    public List<Order> getOrdersByStatus(String status) {
        return orderRepository.findByStatusOrderByOrderDateDesc(status);
    }

    @Override
    public long countByStatus(String status) {
        return orderRepository.countByStatus(status);
    }
}
