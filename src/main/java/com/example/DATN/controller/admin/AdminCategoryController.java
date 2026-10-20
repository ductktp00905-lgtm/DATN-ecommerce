package com.example.DATN.controller.admin;

import com.example.DATN.entity.Category;
import com.example.DATN.repository.CategoryRepository;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin/categories")
public class AdminCategoryController {

    @Autowired
    private CategoryService categoryService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("categories", categoryService.getAll());
        return "admin/category-manage";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("category", new Category());
        return "admin/category-form";
    }

    @PostMapping("/new")
    public String create(@RequestParam String categoryName, RedirectAttributes ra) {
        String name = categoryName.trim();
        if (name.isEmpty()) {
            ra.addFlashAttribute("error", "Tên danh mục không được để trống");
            return "redirect:/admin/categories/new";
        }
        if (categoryRepository.existsByCategoryNameIgnoreCase(name)) {
            ra.addFlashAttribute("error", "Danh mục \"" + name + "\" đã tồn tại");
            return "redirect:/admin/categories/new";
        }
        Category c = new Category();
        c.setCategoryName(name);
        categoryService.create(c);
        ra.addFlashAttribute("success", "Đã thêm danh mục: " + name);
        return "redirect:/admin/categories";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("category", categoryService.getById(id));
        return "admin/category-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
                         @RequestParam String categoryName,
                         RedirectAttributes ra) {
        String name = categoryName.trim();
        if (name.isEmpty()) {
            ra.addFlashAttribute("error", "Tên danh mục không được để trống");
            return "redirect:/admin/categories/edit/" + id;
        }
        if (categoryRepository.existsByCategoryNameIgnoreCaseAndCategoryIDNot(name, id)) {
            ra.addFlashAttribute("error", "Danh mục \"" + name + "\" đã tồn tại");
            return "redirect:/admin/categories/edit/" + id;
        }
        Category c = new Category();
        c.setCategoryName(name);
        categoryService.update(id, c);
        ra.addFlashAttribute("success", "Đã cập nhật danh mục");
        return "redirect:/admin/categories";
    }

    @PostMapping("/delete/{id}")
    public String delete(@PathVariable Integer id, RedirectAttributes ra) {
        long used = productRepository.countByCategory_CategoryID(id);
        if (used > 0) {
            ra.addFlashAttribute("error",
                    "Không thể xóa: danh mục đang có " + used + " sản phẩm. Hãy chuyển hoặc xóa sản phẩm trước.");
            return "redirect:/admin/categories";
        }
        categoryService.delete(id);
        ra.addFlashAttribute("success", "Đã xóa danh mục");
        return "redirect:/admin/categories";
    }
}
