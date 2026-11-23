package com.example.DATN.controller;

import com.example.DATN.dto.Cart;
import com.example.DATN.dto.CartItem;
import com.example.DATN.entity.Product;
import com.example.DATN.entity.ProductVariant;
import com.example.DATN.repository.ProductVariantRepository;
import com.example.DATN.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductVariantRepository variantRepository;

    private Cart getCart(HttpSession session) {
        Cart cart = (Cart) session.getAttribute("cart");
        if (cart == null) {
            cart = new Cart();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @GetMapping
    public String viewCart(HttpSession session, Model model) {
        model.addAttribute("cart", getCart(session));
        return "cart/cart";
    }

    @PostMapping("/add")
    public String addToCart(@RequestParam Integer productId,
                            @RequestParam(required = false) Integer variantId,
                            @RequestParam Integer quantity,
                            HttpSession session, RedirectAttributes ra) {
        Product product = productService.getById(productId);
        Cart cart = getCart(session);

        if (quantity == null || quantity < 1) {
            ra.addFlashAttribute("cartError", "Số lượng phải lớn hơn 0");
            return "redirect:/cart";
        }

        ProductVariant variant = null;
        if (variantId != null) {
            variant = variantRepository.findById(variantId).orElse(null);
        } else if (product.getVariants() != null && !product.getVariants().isEmpty()) {
            // Nếu sản phẩm có biến thể mà form không gửi lên (fallback chọn biến thể đầu tiên/mặc định)
            variant = product.getVariants().stream()
                    .filter(v -> Boolean.TRUE.equals(v.getIsDefault()))
                    .findFirst()
                    .orElse(product.getVariants().get(0));
            variantId = variant.getVariantID();
        }

        String itemKey = productId + (variantId != null ? "_" + variantId : "");
        CartItem inCart = cart.getItems().get(itemKey);
        int currentInCart = (inCart == null ? 0 : inCart.getQuantity());
        int total = quantity + currentInCart;

        BigDecimal price = product.getPrice();
        String variantName = null;
        String imageUrl = product.getImageURL();
        int maxStock = product.getStock();

        if (variant != null) {
            price = variant.getPrice();
            variantName = variant.getVariantName();
            maxStock = variant.getStock();
            if (variant.getImageURL() != null && !variant.getImageURL().isBlank()) {
                imageUrl = variant.getImageURL();
            }
        }

        if (total > maxStock) {
            String label = variant != null ? ("'" + product.getName() + " (" + variantName + ")'") : ("'" + product.getName() + "'");
            ra.addFlashAttribute("cartError", label + " chỉ còn " + maxStock
                    + " sản phẩm trong kho (trong giỏ đã có " + currentInCart + ")");
            return "redirect:/cart";
        }

        cart.addItem(new CartItem(product.getProductID(), variantId, product.getName(),
                variantName, price, quantity, imageUrl));
        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam(required = false) String cartKey,
                             @RequestParam(required = false) Integer productId,
                             @RequestParam Integer quantity,
                             HttpSession session, RedirectAttributes ra) {
        String key = cartKey != null ? cartKey : String.valueOf(productId);
        CartItem inCart = getCart(session).getItems().get(key);
        if (inCart != null) {
            int maxStock = 999;
            if (inCart.getVariantId() != null) {
                ProductVariant v = variantRepository.findById(inCart.getVariantId()).orElse(null);
                if (v != null) maxStock = v.getStock();
            } else {
                Product p = productService.getById(inCart.getProductId());
                if (p != null) maxStock = p.getStock();
            }

            if (quantity > maxStock) {
                ra.addFlashAttribute("cartError", "Số lượng vượt quá tồn kho hiện có (" + maxStock + ")");
                return "redirect:/cart";
            }
        }

        getCart(session).updateQuantity(key, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam(required = false) String cartKey,
                                 @RequestParam(required = false) Integer productId,
                                 HttpSession session) {
        String key = cartKey != null ? cartKey : String.valueOf(productId);
        getCart(session).removeItem(key);
        return "redirect:/cart";
    }
}
