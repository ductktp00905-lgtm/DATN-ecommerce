package com.example.DATN.controller.admin;

import com.example.DATN.entity.Product;
import com.example.DATN.entity.ProductImage;
import com.example.DATN.entity.ProductVariant;
import com.example.DATN.repository.OrderDetailRepository;
import com.example.DATN.repository.ProductImageRepository;
import com.example.DATN.repository.ProductReviewRepository;
import com.example.DATN.repository.ProductVariantRepository;
import com.example.DATN.repository.WishlistRepository;
import com.example.DATN.service.CategoryService;
import com.example.DATN.service.FileUploadService;
import com.example.DATN.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;
import java.util.List;

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

    @Autowired
    private FileUploadService fileUploadService;

    @Autowired
    private ProductVariantRepository productVariantRepository;

    @Autowired
    private ProductImageRepository productImageRepository;

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
    @Transactional
    public String create(@ModelAttribute Product product,
                         @RequestParam Integer categoryId,
                         @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "variantNames", required = false) List<String> variantNames,
                         @RequestParam(value = "attributeSizes", required = false) List<String> attributeSizes,
                         @RequestParam(value = "attributeColors", required = false) List<String> attributeColors,
                         @RequestParam(value = "colorHexes", required = false) List<String> colorHexes,
                         @RequestParam(value = "variantPrices", required = false) List<BigDecimal> variantPrices,
                         @RequestParam(value = "variantStocks", required = false) List<Integer> variantStocks,
                         @RequestParam(value = "variantSkus", required = false) List<String> variantSkus,
                         RedirectAttributes ra) {

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm không được để trống");
            return "redirect:/admin/products/new";
        }

        // Tải ảnh chính từ máy nếu có
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String uploadedUrl = fileUploadService.saveFile(mainImageFile);
            product.setImageURL(uploadedUrl);
        }

        // Tự động tính toán giá & tồn kho nếu sản phẩm có các biến thể
        if (variantPrices != null && !variantPrices.isEmpty() && variantPrices.get(0) != null) {
            BigDecimal minP = variantPrices.stream().filter(p -> p != null).min(BigDecimal::compareTo).orElse(product.getPrice());
            if (minP != null) {
                product.setPrice(minP);
            }
        }
        if (variantStocks != null && !variantStocks.isEmpty()) {
            int totalS = variantStocks.stream().filter(s -> s != null).mapToInt(Integer::intValue).sum();
            if (totalS > 0 || (product.getStock() == null || product.getStock() <= 0)) {
                product.setStock(totalS);
            }
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("errorMessage", "Giá sản phẩm không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/new";
        }
        if (product.getStock() == null || product.getStock() < 0) {
            ra.addFlashAttribute("errorMessage", "Số lượng tồn kho không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/new";
        }

        product.setName(product.getName().trim());
        product.setCategory(categoryService.getById(categoryId));
        Product savedProduct = productService.create(product);

        // Lưu ảnh đại diện vào gallery
        if (savedProduct.getImageURL() != null && !savedProduct.getImageURL().isBlank()) {
            ProductImage primaryImg = new ProductImage();
            primaryImg.setProduct(savedProduct);
            primaryImg.setImageURL(savedProduct.getImageURL());
            primaryImg.setDisplayOrder(1);
            primaryImg.setIsPrimary(true);
            productImageRepository.save(primaryImg);
        }

        // Lưu danh sách file ảnh chi tiết tải lên
        if (imageFiles != null) {
            int order = 2;
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String path = fileUploadService.saveFile(file);
                    ProductImage pi = new ProductImage();
                    pi.setProduct(savedProduct);
                    pi.setImageURL(path);
                    pi.setDisplayOrder(order++);
                    pi.setIsPrimary(false);
                    productImageRepository.save(pi);
                }
            }
        }

        // Lưu các biến thể nếu có
        if (variantNames != null && !variantNames.isEmpty()) {
            for (int i = 0; i < variantNames.size(); i++) {
                String vName = variantNames.get(i);
                if (vName != null && !vName.trim().isEmpty()) {
                    ProductVariant pv = new ProductVariant();
                    pv.setProduct(savedProduct);
                    pv.setVariantName(vName.trim());
                    pv.setAttributeSize(attributeSizes != null && i < attributeSizes.size() ? attributeSizes.get(i) : null);
                    pv.setAttributeColor(attributeColors != null && i < attributeColors.size() ? attributeColors.get(i) : null);
                    pv.setColorHex(colorHexes != null && i < colorHexes.size() ? colorHexes.get(i) : null);
                    BigDecimal p = (variantPrices != null && i < variantPrices.size() && variantPrices.get(i) != null)
                            ? variantPrices.get(i) : savedProduct.getPrice();
                    pv.setPrice(p);
                    Integer s = (variantStocks != null && i < variantStocks.size() && variantStocks.get(i) != null)
                            ? variantStocks.get(i) : 0;
                    pv.setStock(s);
                    pv.setSku(variantSkus != null && i < variantSkus.size() ? variantSkus.get(i) : null);
                    pv.setImageURL(savedProduct.getImageURL());
                    pv.setIsDefault(i == 0);
                    productVariantRepository.save(pv);
                }
            }
        }

        ra.addFlashAttribute("successMessage", "Thêm sản phẩm mới thành công!");
        return "redirect:/admin/products";
    }

    @GetMapping("/edit/{id}")
    public String editForm(@PathVariable Integer id, Model model) {
        Product product = productService.getById(id);
        model.addAttribute("product", product);
        model.addAttribute("categories", categoryService.getAll());
        return "admin/product-form";
    }

    @PostMapping("/edit/{id}")
    @Transactional
    public String update(@PathVariable Integer id,
                         @ModelAttribute Product product,
                         @RequestParam Integer categoryId,
                         @RequestParam(value = "mainImageFile", required = false) MultipartFile mainImageFile,
                         @RequestParam(value = "imageFiles", required = false) List<MultipartFile> imageFiles,
                         @RequestParam(value = "variantNames", required = false) List<String> variantNames,
                         @RequestParam(value = "attributeSizes", required = false) List<String> attributeSizes,
                         @RequestParam(value = "attributeColors", required = false) List<String> attributeColors,
                         @RequestParam(value = "colorHexes", required = false) List<String> colorHexes,
                         @RequestParam(value = "variantPrices", required = false) List<BigDecimal> variantPrices,
                         @RequestParam(value = "variantStocks", required = false) List<Integer> variantStocks,
                         @RequestParam(value = "variantSkus", required = false) List<String> variantSkus,
                         RedirectAttributes ra) {

        Product existing = productService.getById(id);

        if (product.getName() == null || product.getName().trim().isEmpty()) {
            ra.addFlashAttribute("errorMessage", "Tên sản phẩm không được để trống");
            return "redirect:/admin/products/edit/" + id;
        }

        // Tải ảnh chính mới từ máy nếu có
        if (mainImageFile != null && !mainImageFile.isEmpty()) {
            String uploadedUrl = fileUploadService.saveFile(mainImageFile);
            existing.setImageURL(uploadedUrl);
        }

        // Tự động tính toán giá & tồn kho nếu sản phẩm có các biến thể
        if (variantPrices != null && !variantPrices.isEmpty() && variantPrices.get(0) != null) {
            BigDecimal minP = variantPrices.stream().filter(p -> p != null).min(BigDecimal::compareTo).orElse(product.getPrice());
            if (minP != null) {
                product.setPrice(minP);
            }
        }
        if (variantStocks != null && !variantStocks.isEmpty()) {
            int totalS = variantStocks.stream().filter(s -> s != null).mapToInt(Integer::intValue).sum();
            if (totalS > 0 || (product.getStock() == null || product.getStock() <= 0)) {
                product.setStock(totalS);
            }
        }

        if (product.getPrice() == null || product.getPrice().compareTo(BigDecimal.ZERO) < 0) {
            ra.addFlashAttribute("errorMessage", "Giá sản phẩm không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/edit/" + id;
        }
        if (product.getStock() == null || product.getStock() < 0) {
            ra.addFlashAttribute("errorMessage", "Số lượng tồn kho không hợp lệ (không được để trống hoặc âm)");
            return "redirect:/admin/products/edit/" + id;
        }

        existing.setName(product.getName().trim());
        existing.setDescription(product.getDescription());
        existing.setPrice(product.getPrice());
        existing.setStock(product.getStock());
        existing.setCategory(categoryService.getById(categoryId));
        productService.update(id, existing);

        // Upload thêm ảnh gallery chi tiết
        if (imageFiles != null) {
            int currentMaxOrder = existing.getImages() != null ? existing.getImages().size() + 1 : 1;
            for (MultipartFile file : imageFiles) {
                if (file != null && !file.isEmpty()) {
                    String path = fileUploadService.saveFile(file);
                    ProductImage pi = new ProductImage();
                    pi.setProduct(existing);
                    pi.setImageURL(path);
                    pi.setDisplayOrder(currentMaxOrder++);
                    pi.setIsPrimary(false);
                    productImageRepository.save(pi);
                }
            }
        }

        // Cập nhật danh sách biến thể nếu có nhập liệu
        if (variantNames != null && !variantNames.isEmpty()) {
            productVariantRepository.deleteByProduct_ProductID(id);
            for (int i = 0; i < variantNames.size(); i++) {
                String vName = variantNames.get(i);
                if (vName != null && !vName.trim().isEmpty()) {
                    ProductVariant pv = new ProductVariant();
                    pv.setProduct(existing);
                    pv.setVariantName(vName.trim());
                    pv.setAttributeSize(attributeSizes != null && i < attributeSizes.size() ? attributeSizes.get(i) : null);
                    pv.setAttributeColor(attributeColors != null && i < attributeColors.size() ? attributeColors.get(i) : null);
                    pv.setColorHex(colorHexes != null && i < colorHexes.size() ? colorHexes.get(i) : null);
                    BigDecimal p = (variantPrices != null && i < variantPrices.size() && variantPrices.get(i) != null)
                            ? variantPrices.get(i) : existing.getPrice();
                    pv.setPrice(p);
                    Integer s = (variantStocks != null && i < variantStocks.size() && variantStocks.get(i) != null)
                            ? variantStocks.get(i) : 0;
                    pv.setStock(s);
                    pv.setSku(variantSkus != null && i < variantSkus.size() ? variantSkus.get(i) : null);
                    pv.setImageURL(existing.getImageURL());
                    pv.setIsDefault(i == 0);
                    productVariantRepository.save(pv);
                }
            }
        }

        ra.addFlashAttribute("successMessage", "Cập nhật sản phẩm thành công!");
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
        productVariantRepository.deleteByProduct_ProductID(id);
        productImageRepository.deleteByProduct_ProductID(id);
        productService.delete(id);
        ra.addFlashAttribute("successMessage", "Đã xóa sản phẩm thành công!");
        return "redirect:/admin/products";
    }
}
