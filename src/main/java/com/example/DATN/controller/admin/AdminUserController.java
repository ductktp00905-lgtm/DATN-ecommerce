package com.example.DATN.controller.admin;

import com.example.DATN.entity.AdminActionLog;
import com.example.DATN.entity.Order;
import com.example.DATN.entity.ProductReview;
import com.example.DATN.entity.User;
import com.example.DATN.repository.AdminActionLogRepository;
import com.example.DATN.repository.OrderRepository;
import com.example.DATN.repository.ProductReviewRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.repository.WishlistRepository;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)\\d{9}$");
    private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    @Autowired private UserRepository userRepository;
    @Autowired private OrderRepository orderRepository;
    @Autowired private WishlistRepository wishlistRepository;
    @Autowired private ProductReviewRepository reviewRepository;
    @Autowired private AdminActionLogRepository logRepository;

    // ================= Tiện ích =================

    private void log(Authentication auth, User target, String action, String detail) {
        logRepository.save(new AdminActionLog(auth.getName(), target.getUserID(),
                target.getUsername(), action, detail));
    }

    private boolean isSelf(Authentication auth, User u) {
        return u.getUsername().equals(auth.getName());
    }

    private static boolean has(String field, String kw) {
        return field != null && field.toLowerCase().contains(kw);
    }

    // ================= Danh sách + tìm kiếm =================

    @GetMapping
    public String list(@RequestParam(required = false) String q,
                       @RequestParam(required = false) String role,
                       @RequestParam(required = false) String status,
                       Authentication auth, Model model) {
        List<User> all = userRepository.findAll();

        // Tự mở khóa các tài khoản đã hết thời hạn khóa
        LocalDateTime now = LocalDateTime.now();
        for (User u : all) {
            if (Boolean.FALSE.equals(u.getIsActive()) && u.getLockedUntil() != null
                    && !u.getLockedUntil().isAfter(now)) {
                u.setIsActive(true);
                u.setLockedUntil(null);
                u.setLockReason(null);
                userRepository.save(u);
            }
        }

        String kw = q == null ? "" : q.trim().toLowerCase();
        List<User> users = all.stream()
                .filter(u -> kw.isEmpty()
                        || String.valueOf(u.getUserID()).equals(kw)
                        || has(u.getUsername(), kw) || has(u.getFullName(), kw)
                        || has(u.getEmail(), kw) || has(u.getPhone(), kw))
                .filter(u -> role == null || role.isBlank() || role.equals(u.getRole()))
                .filter(u -> {
                    if (status == null || status.isBlank()) return true;
                    return switch (status) {
                        case "active" -> !u.isCurrentlyLocked();
                        case "locked" -> u.isCurrentlyLocked();
                        case "deletion" -> Boolean.TRUE.equals(u.getDeletionRequested());
                        default -> true;
                    };
                })
                .sorted(Comparator.comparing(User::getUserID))
                .toList();

        model.addAttribute("users", users);
        model.addAttribute("totalUsers", all.size());
        model.addAttribute("pendingDeletes",
                all.stream().filter(u -> Boolean.TRUE.equals(u.getDeletionRequested())).count());
        model.addAttribute("q", q);
        model.addAttribute("role", role);
        model.addAttribute("status", status);
        model.addAttribute("currentUsername", auth.getName());
        return "admin/user-manage";
    }

    // ================= Xem & chỉnh sửa thông tin =================

    @PostMapping("/update/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam String fullName,
                         @RequestParam String email,
                         @RequestParam(required = false) String phone,
                         Authentication auth, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        fullName = fullName.trim();
        email = email.trim();
        String newPhone = (phone == null || phone.isBlank()) ? null : phone.trim();

        if (fullName.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Họ tên không được để trống");
            return "redirect:/admin/users";
        }
        if (!EMAIL.matcher(email).matches()) {
            ra.addFlashAttribute("errorMessage", "Email không hợp lệ");
            return "redirect:/admin/users";
        }
        final String em = email;
        if (userRepository.findByEmail(em).filter(o -> !o.getUserID().equals(id)).isPresent()) {
            ra.addFlashAttribute("errorMessage", "Email đã được dùng bởi tài khoản khác");
            return "redirect:/admin/users";
        }
        if (newPhone != null && !PHONE.matcher(newPhone).matches()) {
            ra.addFlashAttribute("errorMessage", "Số điện thoại không hợp lệ");
            return "redirect:/admin/users";
        }

        List<String> changes = new ArrayList<>();
        if (!fullName.equals(u.getFullName())) {
            changes.add("Họ tên: " + u.getFullName() + " → " + fullName);
            u.setFullName(fullName);
        }
        if (!email.equals(u.getEmail())) {
            changes.add("Email: " + u.getEmail() + " → " + email);
            u.setEmail(email);
            u.setEmailVerified(false);
            u.setEmailVerifyCode(null);
            u.setEmailVerifyExpiry(null);
        }
        if (!java.util.Objects.equals(newPhone, u.getPhone())) {
            changes.add("SĐT: " + (u.getPhone() == null ? "—" : u.getPhone()) + " → " + (newPhone == null ? "—" : newPhone));
            u.setPhone(newPhone);
            u.setPhoneVerified(false);
            u.setPhoneVerifyCode(null);
            u.setPhoneVerifyExpiry(null);
        }

        if (changes.isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Không có thay đổi nào");
            return "redirect:/admin/users";
        }
        userRepository.save(u);
        log(auth, u, "Sửa thông tin", String.join("; ", changes));
        ra.addFlashAttribute("successMessage", "Đã cập nhật thông tin " + u.getUsername());
        return "redirect:/admin/users";
    }

    // ================= Phân quyền =================

    @PostMapping("/change-role/{id}")
    public String changeRole(@PathVariable Integer id, @RequestParam String role,
                             Authentication auth, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        if (!role.equals("ADMIN") && !role.equals("CUSTOMER")) {
            ra.addFlashAttribute("errorMessage", "Vai trò không hợp lệ");
        } else if (isSelf(auth, u)) {
            ra.addFlashAttribute("errorMessage", "Không thể tự đổi quyền của chính mình");
        } else if (!role.equals(u.getRole())) {
            String old = u.getRole();
            u.setRole(role);
            userRepository.save(u);
            log(auth, u, "Phân quyền", old + " → " + role);
            ra.addFlashAttribute("successMessage", u.getUsername() + " đã chuyển sang " + role);
        }
        return "redirect:/admin/users";
    }

    // ================= Khóa / mở khóa =================

    @PostMapping("/lock/{id}")
    public String lock(@PathVariable Integer id,
                       @RequestParam int days,                 // 0 = vĩnh viễn
                       @RequestParam(required = false) String reason,
                       Authentication auth, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        if (isSelf(auth, u)) {
            ra.addFlashAttribute("errorMessage", "Không thể tự khóa tài khoản của mình");
            return "redirect:/admin/users";
        }
        if (days < 0 || days > 3650) days = 0;

        String r = (reason == null || reason.isBlank()) ? null
                : reason.trim().substring(0, Math.min(reason.trim().length(), 300));
        u.setIsActive(false);
        u.setLockedUntil(days > 0 ? LocalDateTime.now().plusDays(days) : null);
        u.setLockReason(r);
        userRepository.save(u);

        String time = days > 0 ? "đến " + u.getLockedUntil().format(FMT) + " (" + days + " ngày)" : "vĩnh viễn";
        log(auth, u, "Khóa tài khoản", time + (r != null ? ". Lý do: " + r : ""));
        ra.addFlashAttribute("successMessage", "Đã khóa " + u.getUsername() + " " + time);
        return "redirect:/admin/users";
    }

    @PostMapping("/unlock/{id}")
    public String unlock(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {
        User u = userRepository.findById(id).orElseThrow();
        u.setIsActive(true);
        u.setLockedUntil(null);
        u.setLockReason(null);
        userRepository.save(u);
        log(auth, u, "Mở khóa", "Mở khóa thủ công");
        ra.addFlashAttribute("successMessage", "Đã mở khóa " + u.getUsername());
        return "redirect:/admin/users";
    }

    // ================= Lịch sử mua hàng =================

    @GetMapping("/{id}/orders")
    public String orders(@PathVariable Integer id, Model model) {
        User u = userRepository.findById(id).orElseThrow();
        List<Order> orders = orderRepository.findByUser_UserIDOrderByOrderDateDesc(id);
        BigDecimal spent = orders.stream()
                .filter(o -> "COMPLETED".equals(o.getStatus()))
                .map(Order::getTotalAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("target", u);
        model.addAttribute("orders", orders);
        model.addAttribute("spent", spent);
        return "admin/user-orders";
    }

    // ================= Lịch sử hoạt động & quản trị =================

    @Getter @AllArgsConstructor
    public static class UserEvent {
        private LocalDateTime time;
        private String group;   // "Hoạt động" | "Quản trị"
        private String text;
    }

    @GetMapping("/{id}/history")
    public String history(@PathVariable Integer id, Model model) {
        User u = userRepository.findById(id).orElseThrow();
        List<UserEvent> events = new ArrayList<>();

        if (u.getCreatedDate() != null) {
            events.add(new UserEvent(u.getCreatedDate(), "Hoạt động", "Tạo tài khoản"));
        }
        for (Order o : orderRepository.findByUser_UserIDOrderByOrderDateDesc(id)) {
            events.add(new UserEvent(o.getOrderDate(), "Hoạt động",
                    "Đặt đơn #" + o.getOrderID() + " (" + o.getTotalAmount().toBigInteger() + " đ) - " + o.getStatus()));
        }
        for (ProductReview r : reviewRepository.findByUser_UserID(id)) {
            events.add(new UserEvent(r.getCreatedDate(), "Hoạt động",
                    "Đánh giá " + r.getRating() + " sao: " + r.getProduct().getName()));
        }
        for (AdminActionLog l : logRepository.findByTargetUserIDOrderByCreatedDateDesc(id)) {
            events.add(new UserEvent(l.getCreatedDate(), "Quản trị",
                    l.getAction() + ": " + l.getDetail() + " (bởi " + l.getAdminUsername() + ")"));
        }
        events.removeIf(e -> e.getTime() == null);
        events.sort(Comparator.comparing(UserEvent::getTime).reversed());

        model.addAttribute("target", u);
        model.addAttribute("events", events);
        return "admin/user-history";
    }

    @GetMapping("/logs")
    public String allLogs(Model model) {
        model.addAttribute("logs", logRepository.findTop200ByOrderByCreatedDateDesc());
        return "admin/user-logs";
    }

    // ================= Yêu cầu xóa tài khoản =================

    @PostMapping("/approve-delete/{id}")
    @Transactional
    public String approveDelete(@PathVariable Integer id, Authentication auth) {
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

        wishlistRepository.deleteByUser_UserID(id);
        reviewRepository.deleteByUser_UserID(id);

        log(auth, user, "Xóa tài khoản", "Chấp thuận yêu cầu xóa; giữ " + orders.size() + " đơn hàng (ẩn danh)");
        userRepository.delete(user);
        return "redirect:/admin/users";
    }

    @PostMapping("/reject-delete/{id}")
    public String rejectDelete(@PathVariable Integer id, Authentication auth) {
        User user = userRepository.findById(id).orElseThrow();
        user.setDeletionRequested(false);
        user.setDeletionRequestedDate(null);
        user.setDeletionReason(null);
        userRepository.save(user);
        log(auth, user, "Từ chối xóa", "Từ chối yêu cầu xóa tài khoản");
        return "redirect:/admin/users";
    }
}