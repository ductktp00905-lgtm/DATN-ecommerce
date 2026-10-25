package com.example.DATN.controller;

import com.example.DATN.entity.Product;
import com.example.DATN.repository.UserRepository;       // SỬA: thêm import
import com.example.DATN.repository.WishlistRepository;   // SỬA: thêm import
import org.springframework.security.authentication.AnonymousAuthenticationToken; // SỬA
import org.springframework.security.core.Authentication; // SỬA
import com.example.DATN.service.CategoryService;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import com.example.DATN.dto.RatingStatDTO;
import com.example.DATN.repository.ProductReviewRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/products")
public class ProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    // SỬA: thêm 2 field
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WishlistRepository wishlistRepository;
    
        @Autowired
    private ProductReviewRepository reviewRepository;

    @Value("${app.review.recommend-min-rating:4.0}")
    private double recommendMinRating;

    @Value("${app.review.recommend-min-reviews:1}")
    private long recommendMinReviews;

    @GetMapping
    public String list(@RequestParam(required = false) String keyword,
                        @RequestParam(required = false) Integer categoryId,
                        @RequestParam(required = false) BigDecimal minPrice,
                        @RequestParam(required = false) BigDecimal maxPrice,
                        Model model) {

        List<Product> products = productService.filter(keyword, categoryId, minPrice, maxPrice);

        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("keyword", keyword);
        model.addAttribute("selectedCategoryId", categoryId);
        model.addAttribute("minPrice", minPrice);
        model.addAttribute("maxPrice", maxPrice);        // Top bán chạy + sản phẩm có điểm đánh giá cao (chưa nằm trong top)
        List<Product> featured = new ArrayList<>(productService.getBestSellingProducts());
        Set<Integer> seen = new HashSet<>();
        featured.forEach(p -> seen.add(p.getProductID()));
        for (Product p : reviewRepository.findHighRatedProducts(
                recommendMinRating, recommendMinReviews, PageRequest.of(0, 4))) {
            if (seen.add(p.getProductID())) {
                featured.add(p);
            }
        }
        model.addAttribute("bestSellingProducts", featured);

        Map<Integer, RatingStatDTO> ratings = new HashMap<>();
        if (!featured.isEmpty()) {
            List<Integer> ids = featured.stream().map(Product::getProductID).toList();
            for (RatingStatDTO s : reviewRepository.findRatingStats(ids)) {
                ratings.put(s.productId(), s);
            }
        }
        model.addAttribute("ratings", ratings);
        return "product/list";
    }

    // SỬA: thêm tham số Authentication và tính inWishlist
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Authentication auth, Model model) {
        Product product = productService.getById(id);

        model.addAttribute("product", product);

        boolean inWishlist = false;
        if (auth != null && auth.isAuthenticated() && !(auth instanceof AnonymousAuthenticationToken)) {
            inWishlist = userRepository.findByUsername(auth.getName())
                    .map(u -> wishlistRepository.existsByUser_UserIDAndProduct_ProductID(
                            u.getUserID(), product.getProductID()))
                    .orElse(false);
        }
        model.addAttribute("inWishlist", inWishlist);

        model.addAttribute("relatedProducts",
                productService.getRelatedProducts(
                        product.getCategory().getCategoryID(),
                        product.getProductID()));

        return "product/detail";
    }

}