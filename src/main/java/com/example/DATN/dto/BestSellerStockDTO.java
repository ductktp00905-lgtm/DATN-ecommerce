/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.dto;

/**
 *
 * @author LENOVO
 */

public record BestSellerStockDTO(Integer productId,
                                 String name,
                                 String categoryName,
                                 Long totalQuantity,
                                 Integer stock) {
}
