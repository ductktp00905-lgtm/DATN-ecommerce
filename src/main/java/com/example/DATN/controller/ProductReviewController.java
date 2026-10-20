/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller;
import com.example.DATN.entity.Product;
import com.example.DATN.entity.ProductReview;
import com.example.DATN.entity.User;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.repository.ProductReviewRepository;
import com.example.DATN.repository.UserRepository;

import java.util.List;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
/**
 *
 * @author Admin
 */
@Controller
@RequestMapping("/reviews")
public class ProductReviewController {

    private final ProductReviewRepository reviewRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    public ProductReviewController(
            ProductReviewRepository reviewRepository,
            ProductRepository productRepository,
            UserRepository userRepository) {
        this.reviewRepository = reviewRepository;
        this.productRepository = productRepository;
        this.userRepository = userRepository;
    }

    @GetMapping
    public String list(
            @RequestParam Integer productId,
            Authentication auth,
            Model model) {

        Product product = productRepository.findById(productId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Không tìm thấy sản phẩm"));

        List<ProductReview> reviews =
                reviewRepository
                        .findByProduct_ProductIDOrderByCreatedDateDesc(
                                productId
                        );

        model.addAttribute("product", product);
        model.addAttribute("reviews", reviews);
        model.addAttribute("reviewCount", reviews.size());

        if (auth != null && auth.isAuthenticated()
                && !"anonymousUser".equals(auth.getName())) {

            User user = userRepository.findByUsername(auth.getName())
                    .orElse(null);

            if (user != null) {
                model.addAttribute(
                        "alreadyReviewed",
                        reviewRepository
                                .existsByUser_UserIDAndProduct_ProductID(
                                        user.getUserID(),
                                        productId
                                )
                );
            } else {
                model.addAttribute("alreadyReviewed", false);
            }
        } else {
            model.addAttribute("alreadyReviewed", false);
        }

        return "review/list";
    }

    @PostMapping("/add")
    public String add(
            @RequestParam Integer productId,
            @RequestParam Integer rating,
            @RequestParam(required = false) String comment,
            Authentication auth,
            RedirectAttributes redirectAttributes) {

        if (auth == null || !auth.isAuthenticated()
                || "anonymousUser".equals(auth.getName())) {
            return "redirect:/login";
        }

        if (rating < 1 || rating > 5) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Điểm đánh giá phải từ 1 đến 5 sao."
            );
            return "redirect:/reviews?productId=" + productId;
        }

        Product product = productRepository.findById(productId)
                .orElse(null);

        if (product == null) {
            return "redirect:/products";
        }

        User user = userRepository.findByUsername(auth.getName())
                .orElse(null);

        if (user == null) {
            return "redirect:/login";
        }

        boolean alreadyReviewed =
                reviewRepository
                        .existsByUser_UserIDAndProduct_ProductID(
                                user.getUserID(),
                                productId
                        );

        if (alreadyReviewed) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Bạn đã đánh giá sản phẩm này rồi."
            );
            return "redirect:/reviews?productId=" + productId;
        }

        if (comment != null && comment.length() > 1000) {
            redirectAttributes.addFlashAttribute(
                    "errorMessage",
                    "Nhận xét không được vượt quá 1000 ký tự."
            );
            return "redirect:/reviews?productId=" + productId;
        }

        ProductReview review = new ProductReview();
        review.setUser(user);
        review.setProduct(product);
        review.setRating(rating);
        review.setComment(
                comment == null || comment.isBlank()
                        ? null
                        : comment.trim()
        );

        reviewRepository.save(review);

        redirectAttributes.addFlashAttribute(
                "successMessage",
                "Đánh giá sản phẩm thành công!"
        );

        return "redirect:/reviews?productId=" + productId;
    }
}
