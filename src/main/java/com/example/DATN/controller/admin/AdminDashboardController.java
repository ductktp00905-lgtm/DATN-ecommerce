package com.example.DATN.controller.admin;

import com.example.DATN.dto.CategoryRevenueDTO;
import com.example.DATN.dto.CategoryShareDTO;
import com.example.DATN.dto.DailyRevenueDTO;
import com.example.DATN.dto.DashboardBestSellerDTO;
import com.example.DATN.dto.RecentOrderDTO;
import com.example.DATN.entity.Order;
import com.example.DATN.entity.Payment;
import com.example.DATN.repository.OrderRepository;
import com.example.DATN.repository.PaymentRepository;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/admin")
public class AdminDashboardController {

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PaymentRepository paymentRepository;

    /** /admin/overview cũ -> chuyển về trang tổng quan duy nhất. */
    @GetMapping("/overview")
    public String overviewRedirect() {
        return "redirect:/admin/dashboard";
    }

    /** Trang tổng quan: số liệu thống kê & phân tích doanh số toàn diện (hỗ trợ lọc theo ngày). */
    @GetMapping("/dashboard")
    public String overview(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate fromDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate toDate,
            Model model) {
        if (fromDate != null && toDate != null && fromDate.isAfter(toDate)) {
            LocalDate tmp = fromDate;
            fromDate = toDate;
            toDate = tmp;
        }

        final LocalDate filterFrom = fromDate;
        final LocalDate filterTo = toDate;
        boolean isFiltered = (filterFrom != null || filterTo != null);

        List<Order> allOrders = orderRepository.findAll();
        List<Order> orders = isFiltered
                ? allOrders.stream()
                        .filter(o -> {
                            if (o.getOrderDate() == null) return false;
                            LocalDate d = o.getOrderDate().toLocalDate();
                            if (filterFrom != null && d.isBefore(filterFrom)) return false;
                            if (filterTo != null && d.isAfter(filterTo)) return false;
                            return true;
                        })
                        .toList()
                : allOrders;

        LocalDate today = LocalDate.now();

        // 1. Chỉ số Tài chính
        BigDecimal completedRevenue = orders.stream()
                .filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal pendingRevenue = orders.stream()
                .filter(o -> "PENDING".equalsIgnoreCase(o.getStatus()) || "SHIPPING".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal todayRevenue = orders.stream()
                .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(today))
                .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // 2. Chỉ số Đơn hàng & Vận hành
        long totalOrders = orders.size();
        long pendingOrders = orders.stream().filter(o -> "PENDING".equalsIgnoreCase(o.getStatus())).count();
        long shippingOrders = orders.stream().filter(o -> "SHIPPING".equalsIgnoreCase(o.getStatus())).count();
        long completedOrders = orders.stream().filter(o -> "COMPLETED".equalsIgnoreCase(o.getStatus())).count();
        long cancelledOrders = orders.stream().filter(o -> "CANCELLED".equalsIgnoreCase(o.getStatus())).count();

        double completionRate = totalOrders > 0
                ? Math.round(((double) completedOrders * 1000.0 / totalOrders)) / 10.0
                : 0.0;

        BigDecimal aov = completedOrders > 0
                ? completedRevenue.divide(BigDecimal.valueOf(completedOrders), 0, RoundingMode.HALF_UP)
                : (totalOrders > 0
                    ? completedRevenue.add(pendingRevenue).divide(BigDecimal.valueOf(totalOrders), 0, RoundingMode.HALF_UP)
                    : BigDecimal.ZERO);

        // 3. Chỉ số Kho & Khách hàng
        long totalProducts = productRepository.count();
        long totalUsers = userRepository.count();
        long lowStockCount = productRepository.findAll().stream()
                .filter(p -> p.getStock() != null && p.getStock() <= 20)
                .count();

        // 4. Biến động doanh thu theo ngày (mặc định 7 ngày gần nhất, hoặc theo khoảng lọc)
        LocalDate chartEnd = (filterTo != null) ? filterTo : today;
        LocalDate chartStart = (filterFrom != null) ? filterFrom : chartEnd.minusDays(6);
        if (java.time.temporal.ChronoUnit.DAYS.between(chartStart, chartEnd) > 30) {
            chartStart = chartEnd.minusDays(30);
        }

        List<DailyRevenueDTO> dailyRevenues = new ArrayList<>();
        DateTimeFormatter dFmt = DateTimeFormatter.ofPattern("dd/MM");
        String[] dayNames = {"CN", "T2", "T3", "T4", "T5", "T6", "T7"};

        List<LocalDate> chartDays = new ArrayList<>();
        for (LocalDate d = chartStart; !d.isAfter(chartEnd); d = d.plusDays(1)) {
            chartDays.add(d);
        }

        BigDecimal maxDaily = BigDecimal.ONE;
        for (LocalDate day : chartDays) {
            BigDecimal dayRev = orders.stream()
                    .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(day))
                    .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);
            if (dayRev.compareTo(maxDaily) > 0) {
                maxDaily = dayRev;
            }
        }

        for (LocalDate day : chartDays) {
            BigDecimal dayRev = orders.stream()
                    .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(day))
                    .filter(o -> !"CANCELLED".equalsIgnoreCase(o.getStatus()))
                    .map(Order::getTotalAmount)
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            long dayCount = orders.stream()
                    .filter(o -> o.getOrderDate() != null && o.getOrderDate().toLocalDate().isEqual(day))
                    .count();

            String label = day.isEqual(today)
                    ? "Hôm nay"
                    : (chartDays.size() <= 7 ? dayNames[day.getDayOfWeek().getValue() % 7] : day.format(dFmt));
            int heightPct = maxDaily.compareTo(BigDecimal.ZERO) > 0
                    ? Math.max(12, dayRev.multiply(BigDecimal.valueOf(100)).divide(maxDaily, 0, RoundingMode.HALF_UP).intValue())
                    : 12;

            dailyRevenues.add(new DailyRevenueDTO(label, day.format(dFmt), day, dayRev, dayCount, heightPct));
        }

        String chartTitle = isFiltered
                ? "Biến động Doanh thu (" + chartStart.format(dFmt) + " - " + chartEnd.format(dFmt) + ")"
                : "Biến động Doanh thu 7 ngày gần nhất";

        // 5. Phân bổ doanh thu theo Danh mục sản phẩm
        List<CategoryRevenueDTO> catRevenues = productRepository.findActiveRevenueByCategory();
        BigDecimal totalCatRevenue = catRevenues.stream()
                .map(CategoryRevenueDTO::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        String[] palette = {"#D70018", "#2563EB", "#16A34A", "#EA580C", "#8B5CF6", "#06B6D4", "#EC4899"};
        List<CategoryShareDTO> categoryShares = new ArrayList<>();
        for (int i = 0; i < catRevenues.size(); i++) {
            CategoryRevenueDTO c = catRevenues.get(i);
            double pct = totalCatRevenue.compareTo(BigDecimal.ZERO) > 0
                    ? c.totalRevenue().multiply(BigDecimal.valueOf(100)).divide(totalCatRevenue, 1, RoundingMode.HALF_UP).doubleValue()
                    : 0.0;
            categoryShares.add(new CategoryShareDTO(
                    c.categoryName(),
                    c.totalQuantity(),
                    c.totalRevenue(),
                    pct,
                    palette[i % palette.length]
            ));
        }

        // 6. Top 5 Sản phẩm Bán chạy nhất
        List<DashboardBestSellerDTO> bestSellers = productRepository.findDashboardBestSellers(PageRequest.of(0, 5));

        // 7. Thông tin thanh toán & Đơn hàng gần đây
        List<Payment> payments = paymentRepository.findAll();
        Map<Integer, Payment> paymentMap = payments.stream()
                .filter(p -> p.getOrder() != null)
                .collect(Collectors.toMap(p -> p.getOrder().getOrderID(), p -> p, (p1, p2) -> p1));

        long codOrdersCount = orders.stream()
                .filter(o -> {
                    Payment p = paymentMap.get(o.getOrderID());
                    return p == null || "CASH".equalsIgnoreCase(p.getMethod());
                })
                .count();
        long vnpayOrdersCount = orders.stream()
                .filter(o -> {
                    Payment p = paymentMap.get(o.getOrderID());
                    return p != null && "VNPAY".equalsIgnoreCase(p.getMethod());
                })
                .count();

        List<Order> sortedOrders = orders.stream()
                .sorted(Comparator.comparing(Order::getOrderDate, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(6)
                .toList();

        List<RecentOrderDTO> recentOrders = sortedOrders.stream().map(o -> {
            String customerName = "Khách hàng #" + (o.getUser() != null ? o.getUser().getUserID() : "?");
            String customerPhone = "";
            if (o.getUser() != null) {
                if (o.getUser().getFullName() != null && !o.getUser().getFullName().isBlank()) {
                    customerName = o.getUser().getFullName();
                } else if (o.getUser().getUsername() != null) {
                    customerName = o.getUser().getUsername();
                }
                customerPhone = o.getUser().getPhone() != null ? o.getUser().getPhone() : "";
            }
            Payment p = paymentMap.get(o.getOrderID());
            String payMethod = (p != null && p.getMethod() != null) ? p.getMethod() : "CASH";
            String payStatus = (p != null && "SUCCESS".equalsIgnoreCase(p.getStatus())) ? "ĐÃ THANH TOÁN" : "CHỜ THU TIỀN";

            return new RecentOrderDTO(
                    o.getOrderID(),
                    customerName,
                    customerPhone,
                    o.getOrderDate(),
                    o.getTotalAmount(),
                    payMethod,
                    payStatus,
                    o.getStatus()
            );
        }).toList();

        // Đẩy toàn bộ dữ liệu vào Model
        model.addAttribute("totalRevenue", completedRevenue);
        model.addAttribute("pendingRevenue", pendingRevenue);
        model.addAttribute("todayRevenue", todayRevenue);
        model.addAttribute("aov", aov);

        model.addAttribute("totalOrders", totalOrders);
        model.addAttribute("pendingOrders", pendingOrders);
        model.addAttribute("shippingOrders", shippingOrders);
        model.addAttribute("completedOrders", completedOrders);
        model.addAttribute("cancelledOrders", cancelledOrders);
        model.addAttribute("completionRate", completionRate);

        model.addAttribute("totalProducts", totalProducts);
        model.addAttribute("totalUsers", totalUsers);
        model.addAttribute("lowStockCount", lowStockCount);

        model.addAttribute("dailyRevenues", dailyRevenues);
        model.addAttribute("categoryShares", categoryShares);
        model.addAttribute("bestSellers", bestSellers);
        model.addAttribute("recentOrders", recentOrders);

        model.addAttribute("codOrdersCount", codOrdersCount);
        model.addAttribute("vnpayOrdersCount", vnpayOrdersCount);

        model.addAttribute("fromDate", filterFrom);
        model.addAttribute("toDate", filterTo);
        model.addAttribute("isFiltered", isFiltered);
        model.addAttribute("chartTitle", chartTitle);

        return "admin/dashboard";
    }
}
