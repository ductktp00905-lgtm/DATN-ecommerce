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
        model.addAttribute("maxPrice", maxPrice);
        model.addAttribute("bestSellingProducts",
                productService.getBestSellingProducts());
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