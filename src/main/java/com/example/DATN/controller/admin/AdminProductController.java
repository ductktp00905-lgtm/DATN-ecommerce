package com.example.DATN.controller.admin;

import com.example.DATN.entity.Product;
import com.example.DATN.repository.OrderDetailRepository;
import com.example.DATN.repository.ProductReviewRepository;
import com.example.DATN.repository.WishlistRepository;
import com.example.DATN.service.CategoryService;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/products")
public class AdminProductController {

    @Autowired
    private ProductService productService;

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private WishlistRepository wishlistRepository;

    @Autowired
    private ProductReviewRepository reviewRepository;
    
    @Autowired 
    private OrderDetailRepository orderDetailRepository;

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
                         @RequestParam Integer categoryId,
                         RedirectAttributes ra) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm không được để trống");
            return "redirect:/admin/products/new";
        }
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("errorMessage", "Giá sản phẩm không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/new";
        }
        if (product.getStock() == null || product.getStock() < 0) {
            ra.addFlashAttribute("errorMessage", "Số lượng tồn kho không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/new";
        }
        product.setName(product.getName().trim());
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
                         @RequestParam Integer categoryId,
                         RedirectAttributes ra) {
        if (product.getName() == null || product.getName().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm không được để trống");
            return "redirect:/admin/products/edit/" + id;
        }
        if (product.getPrice() == null || product.getPrice().compareTo(java.math.BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("errorMessage", "Giá sản phẩm không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/edit/" + id;
        }
        if (product.getStock() == null || product.getStock() < 0) {
            ra.addFlashAttribute("errorMessage", "Số lượng tồn kho không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/edit/" + id;
        }
        product.setName(product.getName().trim());
        product.setCategory(categoryService.getById(categoryId));
        productService.update(id, product);
        return "redirect:/admin/products";
    }
    
    @PostMapping("/delete/{id}")
    @Transactional
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        if (orderDetailRepository.existsByProduct_ProductID(id)) {
            ra.addFlashAttribute("errorMessage",
                    "Không thể xóa: sản phẩm đã có trong đơn hàng. Hãy đặt tồn kho về 0 thay vì xóa.");
            return "redirect:/admin/products";
        }
        wishlistRepository.deleteByProduct_ProductID(id);
        reviewRepository.deleteByProduct_ProductID(id);
        productService.delete(id);
        return "redirect:/admin/products";
    }
}
