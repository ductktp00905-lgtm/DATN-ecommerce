/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.dto;

/**
 *
 * @author LENOVO
 */
import java.math.BigDecimal;

public record CategoryRevenueDTO(String categoryName,
                                 Long totalQuantity,
                                 BigDecimal totalRevenue) {
}
