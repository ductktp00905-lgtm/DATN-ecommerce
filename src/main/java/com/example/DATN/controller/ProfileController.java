/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller;

/**
 *
 * @author LENOVO
 */

import com.example.DATN.entity.User;
import com.example.DATN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Objects;
import java.util.regex.Pattern;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private static final Pattern EMAIL = Pattern.compile("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$");
    private static final Pattern PHONE = Pattern.compile("^(0|\\+84)\\d{9}$");
    private static final SecureRandom RANDOM = new SecureRandom();

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private User current(Authentication auth) {
        return userRepository.findByUsername(auth.getName()).orElseThrow();
    }

    private String fail(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute("errorMessage", msg);
        return "redirect:/profile";
    }

    private String ok(RedirectAttributes ra, String msg) {
        ra.addFlashAttribute("successMessage", msg);
        return "redirect:/profile";
    }

    @GetMapping
    public String view(Authentication auth, Model model) {
        model.addAttribute("user", current(auth));
        return "profile/profile";
    }

    // ---------- Sửa họ tên, số điện thoại, email ----------
    @PostMapping("/update")
    public String update(@RequestParam String fullName,
                         @RequestParam String phone,
                         @RequestParam String email,
                         Authentication auth, RedirectAttributes ra) {
        User u = current(auth);
        fullName = fullName.trim();
        phone = phone.trim();
        email = email.trim();

        if (fullName.isEmpty()) return fail(ra, "Họ tên không được để trống");
        if (!EMAIL.matcher(email).matches() || email.length() > 100) return fail(ra, "Email không hợp lệ");
        if (!phone.isEmpty() && !PHONE.matcher(phone).matches()) return fail(ra, "Số điện thoại không hợp lệ (VD: 0912345678)");
        if (!email.equalsIgnoreCase(u.getEmail()) && userRepository.existsByEmail(email)) {
            return fail(ra, "Email đã được sử dụng");
        }

        // Đổi email / SĐT thì phải xác minh lại
        if (!email.equalsIgnoreCase(u.getEmail())) {
            u.setEmailVerified(false);
            u.setEmailVerifyCode(null);
            u.setEmailVerifyExpiry(null);
        }
        String newPhone = phone.isEmpty() ? null : phone;
        if (!Objects.equals(newPhone, u.getPhone())) {
            u.setPhoneVerified(false);
            u.setPhoneVerifyCode(null);
            u.setPhoneVerifyExpiry(null);
        }

        u.setFullName(fullName);
        u.setEmail(email);
        u.setPhone(newPhone);
        userRepository.save(u);
        return ok(ra, "Đã cập nhật thông tin cá nhân");
    }

    // ---------- Đổi mật khẩu ----------
    @PostMapping("/password")
    public String changePassword(@RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 Authentication auth, RedirectAttributes ra) {
        User u = current(auth);

        if (!passwordEncoder.matches(currentPassword, u.getPassword())) return fail(ra, "Mật khẩu hiện tại không đúng");
        if (newPassword.length() < 6) return fail(ra, "Mật khẩu mới phải có ít nhất 6 ký tự");
        if (!newPassword.equals(confirmPassword)) return fail(ra, "Xác nhận mật khẩu không khớp");
        if (passwordEncoder.matches(newPassword, u.getPassword())) return fail(ra, "Mật khẩu mới phải khác mật khẩu cũ");

        u.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(u);
        return ok(ra, "Đã đổi mật khẩu");
    }

    // ---------- Xác minh email / số điện thoại ----------
    @PostMapping("/verify/{type}/send")
    public String sendCode(@PathVariable String type, Authentication auth, RedirectAttributes ra) {
        boolean isEmail = "email".equals(type);
        if (!isEmail && !"phone".equals(type)) return "redirect:/profile";

        User u = current(auth);
        String target = isEmail ? u.getEmail() : u.getPhone();
        if (target == null || target.isBlank()) return fail(ra, "Hãy cập nhật số điện thoại trước");
        if (Boolean.TRUE.equals(isEmail ? u.getEmailVerified() : u.getPhoneVerified())) {
            return fail(ra, "Đã được xác minh rồi");
        }

        String code = String.format("%06d", RANDOM.nextInt(1_000_000));
        LocalDateTime expiry = LocalDateTime.now().plusMinutes(10);
        if (isEmail) {
            u.setEmailVerifyCode(code);
            u.setEmailVerifyExpiry(expiry);
        } else {
            u.setPhoneVerifyCode(code);
            u.setPhoneVerifyExpiry(expiry);
        }
        userRepository.save(u);

        // TODO: chỗ này gắn dịch vụ gửi email / SMS thật. Hiện tại chỉ in ra console và hiện lên trang (chế độ demo).
        System.out.println("[VERIFY] " + type + " " + target + " -> " + code);
        return ok(ra, "Mã xác minh (demo, hiệu lực 10 phút): " + code);
    }

    @PostMapping("/verify/{type}/confirm")
    public String confirmCode(@PathVariable String type, @RequestParam String code,
                              Authentication auth, RedirectAttributes ra) {
        boolean isEmail = "email".equals(type);
        if (!isEmail && !"phone".equals(type)) return "redirect:/profile";

        User u = current(auth);
        String saved = isEmail ? u.getEmailVerifyCode() : u.getPhoneVerifyCode();
        LocalDateTime expiry = isEmail ? u.getEmailVerifyExpiry() : u.getPhoneVerifyExpiry();

        if (saved == null || expiry == null || expiry.isBefore(LocalDateTime.now())) {
            return fail(ra, "Mã đã hết hạn hoặc chưa được gửi, hãy gửi lại mã");
        }
        if (!saved.equals(code.trim())) return fail(ra, "Mã xác minh không đúng");

        if (isEmail) {
            u.setEmailVerified(true);
            u.setEmailVerifyCode(null);
            u.setEmailVerifyExpiry(null);
        } else {
            u.setPhoneVerified(true);
            u.setPhoneVerifyCode(null);
            u.setPhoneVerifyExpiry(null);
        }
        userRepository.save(u);
        return ok(ra, isEmail ? "Đã xác minh email" : "Đã xác minh số điện thoại");
    }

    // ---------- Yêu cầu xóa tài khoản ----------
    @PostMapping("/delete-request")
    public String requestDelete(@RequestParam String password,
                                @RequestParam(required = false) String reason,
                                Authentication auth, RedirectAttributes ra) {
        User u = current(auth);

        if ("ADMIN".equals(u.getRole())) return fail(ra, "Tài khoản ADMIN không thể yêu cầu xóa");
        if (!passwordEncoder.matches(password, u.getPassword())) return fail(ra, "Mật khẩu không đúng");

        String r = (reason == null) ? "" : reason.trim();
        if (r.length() > 500) r = r.substring(0, 500);

        u.setDeletionRequested(true);
        u.setDeletionRequestedDate(LocalDateTime.now());
        u.setDeletionReason(r.isEmpty() ? null : r);
        userRepository.save(u);
        return ok(ra, "Đã gửi yêu cầu xóa tài khoản, quản trị viên sẽ xử lý");
    }

    @PostMapping("/delete-cancel")
    public String cancelDelete(Authentication auth, RedirectAttributes ra) {
        User u = current(auth);
        u.setDeletionRequested(false);
        u.setDeletionRequestedDate(null);
        u.setDeletionReason(null);
        userRepository.save(u);
        return ok(ra, "Đã hủy yêu cầu xóa tài khoản");
    }
}
