package com.example.DATN.controller.admin;

import com.example.DATN.entity.Voucher;
import com.example.DATN.exception.ResourceNotFoundException;
import com.example.DATN.repository.VoucherRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.Optional;

@Controller
@RequestMapping("/admin/vouchers")
public class AdminVoucherController {

    @Autowired
    private VoucherRepository voucherRepository;

    @GetMapping
    public String list(Model model) {
        model.addAttribute("vouchers", voucherRepository.findAll(Sort.by(Sort.Direction.DESC, "voucherID")));
        return "admin/voucher-manage";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        Voucher v = new Voucher();
        v.setDiscountType("PERCENT");
        model.addAttribute("voucher", v);
        return "admin/voucher-form";
    }

    @PostMapping("/new")
    public String create(@ModelAttribute("voucher") Voucher form, BindingResult br,
                         Model model, RedirectAttributes ra) {
        form.setVoucherID(null);
        form.setUsedCount(0);
        form.setActive(true);
        String err = br.hasErrors() ? "Dữ liệu nhập không hợp lệ" : validate(form, null);
        if (err != null) {
            model.addAttribute("error", err);
            return "admin/voucher-form";
        }
        voucherRepository.save(form);
        ra.addFlashAttribute("success", "Đã thêm voucher: " + form.getCode());
        return "redirect:/admin/vouchers";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        model.addAttribute("voucher", find(id));
        return "admin/voucher-form";
    }

    @PostMapping("/edit/{id}")
    public String update(@PathVariable Integer id,
                         @ModelAttribute("voucher") Voucher form, BindingResult br,
                         Model model, RedirectAttributes ra) {
        Voucher existing = find(id);
        form.setVoucherID(id);
        form.setUsedCount(existing.getUsedCount());
        form.setActive(existing.getActive());
        String err = br.hasErrors() ? "Dữ liệu nhập không hợp lệ" : validate(form, id);
        if (err != null) {
            model.addAttribute("error", err);
            return "admin/voucher-form";
        }
        voucherRepository.save(form);
        ra.addFlashAttribute("success", "Đã cập nhật voucher: " + form.getCode());
        return "redirect:/admin/vouchers";
    }

    @PostMapping("/toggle/{id}")
    public String toggle(@PathVariable Integer id, RedirectAttributes ra) {
        Voucher v = find(id);
        v.setActive(!Boolean.TRUE.equals(v.getActive()));
        voucherRepository.save(v);
        ra.addFlashAttribute("success",
                (v.getActive() ? "Đã bật voucher " : "Đã tắt voucher ") + v.getCode());
        return "redirect:/admin/vouchers";
    }

    private Voucher find(Integer id) {
        return voucherRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Không tìm thấy voucher"));
    }

    /** Trả về thông báo lỗi, hoặc null nếu hợp lệ. currentId = null khi tạo mới. */
    private String validate(Voucher v, Integer currentId) {
        if (v.getCode() == null || v.getCode().trim().isEmpty()) {
            return "Mã voucher không được để trống";
        }
        v.setCode(v.getCode().trim().toUpperCase());

        Optional<Voucher> dup = voucherRepository.findByCodeIgnoreCase(v.getCode());
        if (dup.isPresent() && !dup.get().getVoucherID().equals(currentId)) {
            return "Mã \"" + v.getCode() + "\" đã tồn tại";
        }
        if (!"PERCENT".equals(v.getDiscountType()) && !"AMOUNT".equals(v.getDiscountType())) {
            return "Loại giảm giá không hợp lệ";
        }
        if (v.getDiscountValue() == null || v.getDiscountValue().compareTo(BigDecimal.ZERO) <= 0) {
            return "Giá trị giảm phải lớn hơn 0";
        }
        if ("PERCENT".equals(v.getDiscountType())) {
            if (v.getDiscountValue().compareTo(BigDecimal.valueOf(100)) > 0) {
                return "Phần trăm giảm không được vượt quá 100";
            }
            if (v.getMaxDiscount() != null && v.getMaxDiscount().compareTo(BigDecimal.ZERO) <= 0) {
                v.setMaxDiscount(null);
            }
        } else {
            v.setMaxDiscount(null); // giảm số tiền cố định thì không cần trần giảm
        }
        if (v.getMinOrderAmount() == null) {
            v.setMinOrderAmount(BigDecimal.ZERO);
        } else if (v.getMinOrderAmount().compareTo(BigDecimal.ZERO) < 0) {
            return "Đơn tối thiểu không được âm";
        }
        if (v.getQuantity() == null || v.getQuantity() < 1) {
            return "Số lượng phải từ 1 trở lên";
        }
        if (v.getQuantity() < v.getUsedCount()) {
            return "Số lượng không được nhỏ hơn số lượt đã dùng (" + v.getUsedCount() + ")";
        }
        if (v.getStartDate() != null && v.getEndDate() != null && v.getEndDate().isBefore(v.getStartDate())) {
            return "Ngày kết thúc phải sau ngày bắt đầu";
        }
        return null;
    }
}