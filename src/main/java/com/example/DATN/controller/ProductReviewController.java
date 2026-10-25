package com.example.DATN.controller;

import com.example.DATN.entity.Product;
import com.example.DATN.entity.ProductReview;
import com.example.DATN.entity.User;
import com.example.DATN.repository.OrderDetailRepository;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.repository.ProductReviewRepository;
import com.example.DATN.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

@Controller
@RequestMapping("/reviews")
public class ProductReviewController {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;
    private final OrderDetailRepository orderDetailRepository;

    @Value("${app.review.edit-window-minutes:15}")
    private long editWindowMinutes;

    public ProductReviewController(ProductReviewRepository reviewRepository,
                                   ProductRepository productRepository,
                                   UserRepository userRepository,
                                   OrderDetailRepository orderDetailRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
        this.orderDetailRepository = orderDetailRepository;
    }

    // ---------- tiện ích ----------
    private User currentUser(Authentication auth) {
        if (auth == null || !auth.isAuthenticated() || auth instanceof AnonymousAuthenticationToken) {
            return null;
        }
        return userRepository.findByUsername(auth.getName()).orElse(null);
    }

    private String back(Integer productId) {
        return "redirect:/reviews?productId=" + productId;
    }

    private String fail(RedirectAttributes ra, Integer productId, String msg) {
        ra.addFlashAttribute("errorMessage", msg);
        return back(productId);
    }

    private boolean withinWindow(ProductReview r) {
        return LocalDateTime.now().isBefore(r.getCreatedDate().plusMinutes(editWindowMinutes));
    }

    private String cleanComment(String c) {
        return (c == null || c.isBlank()) ? null : c.trim();
    }

    // ---------- Xem đánh giá ----------
    @GetMapping
    public String list(@RequestParam Integer productId, Authentication auth, Model model) {
        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }

        List<ProductReview> reviews = reviewRepository.findByProduct_ProductIDOrderByCreatedDateDesc(productId);
        User user = currentUser(auth);

        ProductReview mine = null;
        boolean purchased = false;
        if (user != null) {
            mine = reviewRepository.findByUser_UserIDAndProduct_ProductID(user.getUserID(), productId).orElse(null);
            purchased = orderDetailRepository.countPurchased(user.getUserID(), productId) > 0;
        }

        boolean canEditMine = false;
        long minutesLeft = 0;
        if (mine != null && withinWindow(mine)) {
            canEditMine = true;
            long seconds = Duration.between(LocalDateTime.now(),
                    mine.getCreatedDate().plusMinutes(editWindowMinutes)).getSeconds();
            minutesLeft = Math.max(1, (seconds + 59) / 60);
        }

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());
        model.addAttribute("myReview", mine);
        model.addAttribute("alreadyReviewed", mine != null);
        model.addAttribute("canReview", purchased && mine == null);
        model.addAttribute("canEditMine", canEditMine);
        model.addAttribute("minutesLeft", minutesLeft);
        model.addAttribute("editWindow", editWindowMinutes);
        return "review/list";
    }

    // ---------- Đăng đánh giá ----------
    @PostMapping("/add")
    public String add(@RequestParam Integer productId,
                      @RequestParam Integer rating,
                      @RequestParam(required = false) String comment,
                      Authentication auth, RedirectAttributes ra) {

        User user = currentUser(auth);
        if (user == null) {
            return "redirect:/auth/login";
        }

        Product product = productRepository.findById(productId).orElse(null);
        if (product == null) {
            return "redirect:/products";
        }

        if (rating < 1 || rating > 5) {
            return fail(ra, productId, "Điểm đánh giá phải từ 1 đến 5 sao.");
        }
        if (comment != null && comment.length() > 1000) {
            return fail(ra, productId, "Nhận xét không được vượt quá 1000 ký tự.");
        }
        if (orderDetailRepository.countPurchased(user.getUserID(), productId) == 0) {
            return fail(ra, productId, "Bạn chỉ có thể đánh giá sản phẩm đã mua (đơn hàng đã hoàn thành).");
        }
        if (reviewRepository.existsByUser_UserIDAndProduct_ProductID(user.getUserID(), productId)) {
            return fail(ra, productId, "Bạn đã đánh giá sản phẩm này rồi.");
        }

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(cleanComment(comment));
        reviewRepository.save(review);

        ra.addFlashAttribute("successMessage", "Đánh giá sản phẩm thành công!");
        return back(productId);
    }

    // ---------- Sửa đánh giá (trong thời gian cho phép) ----------
    @PostMapping("/edit/{id}")
    public String edit(@PathVariable Integer id,
                       @RequestParam Integer rating,
                       @RequestParam(required = false) String comment,
                       Authentication auth, RedirectAttributes ra) {

        User user = currentUser(auth);
        if (user == null) {
            return "redirect:/auth/login";
        }
        ProductReview review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return "redirect:/products";
        }
        Integer productId = review.getProduct().getProductID();

        if (!review.getUser().getUserID().equals(user.getUserID())) {
            return fail(ra, productId, "Bạn chỉ có thể sửa đánh giá của chính mình.");
        }
        if (!withinWindow(review)) {
            return fail(ra, productId, "Đã quá " + editWindowMinutes + " phút kể từ lúc đăng, không thể sửa đánh giá.");
        }
        if (rating < 1 || rating > 5) {
            return fail(ra, productId, "Điểm đánh giá phải từ 1 đến 5 sao.");
        }
        if (comment != null && comment.length() > 1000) {
            return fail(ra, productId, "Nhận xét không được vượt quá 1000 ký tự.");
        }

        review.setRating(rating);
        review.setComment(cleanComment(comment));
        review.setUpdatedDate(LocalDateTime.now());
        reviewRepository.save(review);

        ra.addFlashAttribute("successMessage", "Đã cập nhật đánh giá.");
        return back(productId);
    }

    // ---------- Thu hồi (xóa) đánh giá (trong thời gian cho phép) ----------
    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, Authentication auth, RedirectAttributes ra) {

        User user = currentUser(auth);
        if (user == null) {
            return "redirect:/auth/login";
        }
        ProductReview review = reviewRepository.findById(id).orElse(null);
        if (review == null) {
            return "redirect:/products";
        }
        Integer productId = review.getProduct().getProductID();

        if (!review.getUser().getUserID().equals(user.getUserID())) {
            return fail(ra, productId, "Bạn chỉ có thể thu hồi đánh giá của chính mình.");
        }
        if (!withinWindow(review)) {
            return fail(ra, productId, "Đã quá " + editWindowMinutes + " phút kể từ lúc đăng, không thể thu hồi đánh giá.");
        }

        reviewRepository.delete(review);
        ra.addFlashAttribute("successMessage", "Đã thu hồi đánh giá. Bạn có thể viết đánh giá mới.");
        return back(productId);
    }
}