/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller.admin;

/**
 *
 * @author LENOVO
 */

import com.example.DATN.repository.ProductRepository;
import com.example.DATN.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import com.example.DATN.dto.CategoryRevenueDTO;
import java.math.BigDecimal;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;

@Controller
@RequestMapping("/admin/best-sellers")
public class AdminBestSellerController {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryService categoryService;

    @GetMapping
    public String stats(@RequestParam(defaultValue = "all") String period,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
                        @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
                        @RequestParam(required = false) Integer categoryId,
                        @RequestParam(defaultValue = "10") Integer limit,
                        Model model) {

        LocalDate ref = (date != null) ? date : LocalDate.now();
        LocalDate startDate = null;
        LocalDate endDate = null;   // tính cả ngày này

        switch (period) {
            case "week":
                startDate = ref.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
                endDate = startDate.plusDays(6);
                break;
            case "month":
                startDate = ref.with(TemporalAdjusters.firstDayOfMonth());
                endDate = ref.with(TemporalAdjusters.lastDayOfMonth());
                break;
            case "year":
                startDate = ref.with(TemporalAdjusters.firstDayOfYear());
                endDate = ref.with(TemporalAdjusters.lastDayOfYear());
                break;
            case "custom":
                startDate = from;
                endDate = to;
                break;
            default:
                period = "all";
        }

        var start = (startDate != null ? startDate : LocalDate.of(2000, 1, 1)).atStartOfDay();
        var end = (endDate != null ? endDate : LocalDate.of(2100, 1, 1)).plusDays(1).atStartOfDay();

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("dd/MM/yyyy");
        String rangeLabel = (startDate == null && endDate == null) ? "Toàn bộ thời gian"
                : (startDate != null ? startDate.format(fmt) : "...") + " → "
                + (endDate != null ? endDate.format(fmt) : "...");

                var revenueByCategory = productRepository.findRevenueByCategory(start, end);
        BigDecimal totalRevenue = revenueByCategory.stream()
                .map(CategoryRevenueDTO::totalRevenue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        model.addAttribute("revenueByCategory", revenueByCategory);
        model.addAttribute("totalRevenue", totalRevenue);
        model.addAttribute("stats", productRepository.findBestSellerStats(start, end, categoryId, PageRequest.of(0, limit)));
        model.addAttribute("categories", categoryService.getAll());
        model.addAttribute("period", period);
        model.addAttribute("refDate", ref);
        model.addAttribute("from", from);
        model.addAttribute("to", to);
        model.addAttribute("categoryId", categoryId);
        model.addAttribute("limit", limit);
        model.addAttribute("rangeLabel", rangeLabel);
        return "admin/best-seller";
    }
}
