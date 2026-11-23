package com.example.DATN.controller.admin;

import com.example.DATN.entity.AdminActionLog;
import com.example.DATN.entity.StoreSetting;
import com.example.DATN.repository.AdminActionLogRepository;
import com.example.DATN.repository.StoreSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.LocalDateTime;

@Controller
@RequestMapping("/admin/store-settings")
public class AdminStoreSettingController {

    @Autowired
    private StoreSettingRepository storeSettingRepository;

    @Autowired
    private AdminActionLogRepository adminActionLogRepository;

    @GetMapping
    public String showForm(Model model) {
        StoreSetting setting = storeSettingRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            StoreSetting def = new StoreSetting();
            return storeSettingRepository.save(def);
        });
        model.addAttribute("setting", setting);
        return "admin/store-settings";
    }

    @PostMapping
    public String updateStoreSetting(@ModelAttribute StoreSetting form,
                                     Principal principal,
                                     RedirectAttributes ra) {
        StoreSetting setting = storeSettingRepository.findTopByOrderByIdAsc().orElseGet(StoreSetting::new);

        setting.setStoreName(form.getStoreName() != null ? form.getStoreName().trim() : "FlareHub");
        setting.setAddress(form.getAddress() != null ? form.getAddress().trim() : "");
        setting.setPhone(form.getPhone() != null ? form.getPhone().trim() : "");
        setting.setEmail(form.getEmail() != null ? form.getEmail().trim() : "");
        setting.setOpeningHours(form.getOpeningHours() != null ? form.getOpeningHours().trim() : "");
        setting.setMapEmbedUrl(form.getMapEmbedUrl() != null ? form.getMapEmbedUrl().trim() : "");
        setting.setUpdatedAt(LocalDateTime.now());

        storeSettingRepository.save(setting);

        // Ghi log hành động admin
        String adminUser = (principal != null) ? principal.getName() : "admin";
        adminActionLogRepository.save(new AdminActionLog(
                adminUser,
                null,
                "STORE_SYSTEM",
                "UPDATE_STORE_SETTINGS",
                "Cập nhật thông tin cửa hàng & bản đồ Google Maps: " + setting.getAddress()
        ));

        ra.addFlashAttribute("successMessage", "Đã cập nhật thông tin cửa hàng & bản đồ Google Maps thành công!");
        return "redirect:/admin/store-settings";
    }
}
