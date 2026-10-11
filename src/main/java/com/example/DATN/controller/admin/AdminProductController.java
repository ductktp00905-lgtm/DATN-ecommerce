/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller.admin;

import com.example.DATN.entity.Product;
import com.example.DATN.service.CategoryService;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", productService.getAll());
        return "admin/product-manage";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("categories", categoryService.getAll());
        return "admin/product-form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute Product product,
                          @RequestParam Integer categoryId) {
        product.setCategory(categoryService.getById(categoryId));
        productService.create(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("product", productService.getById(id));
        model.addAttribute("categories", categoryService.getAll());
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
                          @ModelAttribute Product product,
                          @RequestParam Integer categoryId) {
        product.setCategory(categoryService.getById(categoryId));
        productService.update(id, product);
        return "redirect:/admin/products";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id) {
        productService.delete(id);
        return "redirect:/admin/products";
    }
}