package com.example.DATN.config;

import com.example.DATN.entity.Category;
import com.example.DATN.entity.StoreSetting;
import com.example.DATN.repository.CategoryRepository;
import com.example.DATN.repository.StoreSettingRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@ControllerAdvice
public class GlobalControllerAdvice {

    @Autowired
    private StoreSettingRepository storeSettingRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @ModelAttribute("storeSetting")
    public StoreSetting populateStoreSetting() {
        return storeSettingRepository.findTopByOrderByIdAsc().orElseGet(() -> {
            StoreSetting def = new StoreSetting();
            def.setStoreName("FlareHub Flagship Store — TP. Hồ Chí Minh");
            def.setAddress("Số 134 Nguyễn Thái Học, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh");
            def.setPhone("1800.2097 - 098.123.4567");
            def.setEmail("cskh@flarehub.vn");
            def.setOpeningHours("08:00 - 21:30 (Cả Thứ 7, CN & Ngày lễ)");
            return def;
        });
    }

    @ModelAttribute("storeMapUrl")
    public String populateStoreMapUrl() {
        StoreSetting setting = populateStoreSetting();
        String raw = setting.getMapEmbedUrl();
        if (raw != null && !raw.trim().isEmpty()) {
            raw = raw.trim();
            if (raw.toLowerCase().contains("<iframe")) {
                Pattern pattern = Pattern.compile("src=[\"']([^\"']+)[\"']", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(raw);
                if (matcher.find()) {
                    return matcher.group(1);
                }
            } else if (raw.startsWith("http://") || raw.startsWith("https://")) {
                return raw;
            }
        }

        // Tự động sinh link Google Maps embed từ địa chỉ cửa hàng
        String addr = setting.getAddress() != null && !setting.getAddress().trim().isEmpty()
                ? setting.getAddress().trim()
                : "134 Nguyễn Thái Học, Quận 1, Hồ Chí Minh";
        return "https://maps.google.com/maps?q=" + URLEncoder.encode(addr, StandardCharsets.UTF_8) + "&t=&z=15&ie=UTF8&iwloc=&output=embed";
    }

    @ModelAttribute("globalCategories")
    public List<Category> populateCategories() {
        try {
            return categoryRepository.findAll();
        } catch (Exception e) {
            return Collections.emptyList();
        }
    }
}
