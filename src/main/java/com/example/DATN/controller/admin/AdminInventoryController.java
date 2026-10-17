/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller.admin;

/**
 *
 * @author LENOVO
 */

import com.example.DATN.entity.Product;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;

@Controller
@RequestMapping("/admin/inventory")
public class AdminInventoryController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String inventory(@RequestParam(required = false) Integer categoryId,
                            @RequestParam(defaultValue = "all") String status,
                            @RequestParam(defaultValue = "5") Integer threshold,
                            Model model) {

        // Số liệu tổng quan (trên toàn bộ sản phẩm)
        List<Product> all = productRepository.findAll();
        long outCount = all.stream().filter(p -> p.getStock() <= 0).count();
        long lowCount = all.stream().filter(p -> p.getStock() > 0 && p.getStock() <= threshold).count();
        long totalUnits = all.stream().mapToLong(Product::getStock).sum();
        BigDecimal totalValue = all.stream()
                .map(p -> p.getPrice().multiply(BigDecimal.valueOf(p.getStock())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        // Bảng tồn kho (lọc theo loại + trạng thái)
        List<Product> products = productRepository.findForInventory(categoryId).stream()
                .filter(p -> "out".equals(status) ? p.getStock() <= 0
                        : "low".equals(status) ? (p.getStock() > 0 && p.getStock() <= threshold)
                        : true)
                .toList();

        model.addAttribute("productCount", all.size());
        model.addAttribute("outCount", outCount);
        model.addAttribute("lowCount", lowCount);
        model.addAttribute("totalUnits", totalUnits);
        model.addAttribute("totalValue", totalValue);
        model.addAttribute("products", products);
        model.addAttribute("bestSellers", productRepository.findBestSellerStock(PageRequest.of(0, 10)));
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("status", status);
        model.addAttribute("threshold", threshold);
        return "admin/inventory";
    }
}
