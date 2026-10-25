/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller.admin;

/**
 *
 * @author LENOVO
 */
import com.example.DATN.entity.ProductReview;
import com.example.DATN.repository.ProductReviewRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/reviews")
public class AdminReviewController {

    private final ProductReviewRepository reviewRepository;

    public AdminReviewController(ProductReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    @GetMapping
    public String list(@RequestParam(required = false) Integer maxRating, Model model) {
        List<ProductReview> reviews = reviewRepository.findAllByOrderByCreatedDateDesc();
        if (maxRating != null) {
            reviews = reviews.stream().filter(r -> r.getRating() <= maxRating).toList();
        }
        model.addAttribute("reviews", reviews);
        model.addAttribute("maxRating", maxRating);
        return "admin/review-manage";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id,
                         @RequestParam(required = false) Integer maxRating,
                         RedirectAttributes ra) {
        if (reviewRepository.existsById(id)) {
            reviewRepository.deleteById(id);
            ra.addFlashAttribute("successMessage", "Đã gỡ đánh giá #" + id);
        }
        return "redirect:/admin/reviews" + (maxRating != null ? "?maxRating=" + maxRating : "");
    }
}
