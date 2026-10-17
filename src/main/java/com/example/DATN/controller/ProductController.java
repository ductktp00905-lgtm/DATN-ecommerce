/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller;

import com.example.DATN.entity.Product;
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

    
    @GetMapping("/{id}")
    public String detail(@PathVariable Integer id, Model model) {
        Product product = productService.getById(id);

        model.addAttribute("product", product);
        model.addAttribute("relatedProducts",
                productService.getRelatedProducts(
                        product.getCategory().getCategoryID(),
                        product.getProductID()));

        return "product/detail";
    }

}
