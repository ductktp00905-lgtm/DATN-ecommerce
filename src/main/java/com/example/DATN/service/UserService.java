/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */

package com.example.DATN.service;

import com.example.DATN.entity.User;

public interface UserService {
    User register(String username, String rawPassword, String email, String fullName, String phone);
    boolean isUsernameTaken(String username);
    boolean isEmailTaken(String email);
}
