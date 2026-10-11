/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.service;

import com.example.DATN.entity.Category;
import java.util.List;

public interface CategoryService {
    List<Category> getAll();
    Category getById(Integer id);
    Category create(Category category);
    Category update(Integer id, Category category);
    void delete(Integer id);
}
