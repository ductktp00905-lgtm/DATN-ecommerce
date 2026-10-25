/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.controller;
import com.example.DATN.entity.Product;
import com.example.DATN.entity.User;
import com.example.DATN.entity.Wishlist;
import com.example.DATN.repository.ProductRepository;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.repository.WishlistRepository;

import java.util.Optional;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.transaction.annotation.Transactional;
/**
 *
 * @author Admin
 */
@Controller
@RequestMapping("/wishlist")
public class WishlistController {

    private final WishlistRepository wishlistRepository;
    private final UserRepository userRepository;
    private final ProductRepository productRepository;

    public WishlistController(
            WishlistRepository wishlistRepository,
            UserRepository userRepository,
            ProductRepository productRepository) {
        this.wishlistRepository = wishlistRepository;
        this.userRepository = userRepository;
        this.productRepository = productRepository;
    }

    private User current(Authentication auth) {
        return userRepository.findByUsername(auth.getName())
                .orElseThrow();
    }

    @GetMapping
    public String list(Authentication auth, Model model) {
        User user = current(auth);

        model.addAttribute(
                "items",
                wishlistRepository
                        .findByUser_UserIDOrderByCreatedDateDesc(
                                user.getUserID()
                        )
        );

        return "wishlist/list";
    }

    @PostMapping("/toggle/{productId}")
    public String toggle(
            @PathVariable Integer productId,
            @RequestParam(defaultValue = "/wishlist") String returnTo,
            Authentication auth,
            RedirectAttributes ra) {

        User user = current(auth);

        Product product = productRepository.findById(productId)
                .orElseThrow();

        Optional<Wishlist> existing =
                wishlistRepository
                        .findByUser_UserIDAndProduct_ProductID(
                                user.getUserID(),
                                productId
                        );

        if (existing.isPresent()) {
            wishlistRepository.delete(existing.get());

            ra.addFlashAttribute(
                    "successMessage",
                    "Đã bỏ sản phẩm khỏi danh sách yêu thích."
            );
        } else {
            Wishlist item = new Wishlist();
            item.setUser(user);
            item.setProduct(product);

            wishlistRepository.save(item);

            ra.addFlashAttribute(
                    "successMessage",
                    "Đã thêm sản phẩm vào danh sách yêu thích."
            );
        }

        // Chỉ cho phép chuyển hướng về đường dẫn nội bộ hợp lệ.
        if (returnTo == null
                || (!returnTo.startsWith("/products")
                && !returnTo.equals("/wishlist"))) {
            returnTo = "/wishlist";
        }

        return "redirect:" + returnTo;
    }

    @Transactional
    @PostMapping("/remove/{productId}")
    public String remove(
            @PathVariable Integer productId,
            Authentication auth,
            RedirectAttributes ra) {

        User user = current(auth);

        wishlistRepository
                .deleteByUser_UserIDAndProduct_ProductID(
                        user.getUserID(),
                        productId
                );

        ra.addFlashAttribute(
                "successMessage",
                "Đã xóa sản phẩm khỏi danh sách yêu thích."
        );

        return "redirect:/wishlist";
    }
}
