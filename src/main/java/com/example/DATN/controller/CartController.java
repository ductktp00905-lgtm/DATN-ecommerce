/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.controller;

import com.example.DATN.dto.Cart;
import com.example.DATN.dto.CartItem;
import com.example.DATN.entity.Product;
import com.example.DATN.service.ProductService;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/cart")
public class CartController {

    @Autowired
    private ProductService productService;

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
                             @RequestParam Integer quantity,
                             HttpSession session) {

        Product product = productService.getById(productId);
        Cart cart = getCart(session);

        cart.addItem(new CartItem(
                product.getProductID(),
                product.getName(),
                product.getPrice(),
                quantity,
                product.getImageURL()
        ));

        return "redirect:/cart";
    }

    @PostMapping("/update")
    public String updateCart(@RequestParam Integer productId,
                              @RequestParam Integer quantity,
                              HttpSession session) {
        getCart(session).updateQuantity(productId, quantity);
        return "redirect:/cart";
    }

    @PostMapping("/remove")
    public String removeFromCart(@RequestParam Integer productId, HttpSession session) {
        getCart(session).removeItem(productId);
        return "redirect:/cart";
    }
}
