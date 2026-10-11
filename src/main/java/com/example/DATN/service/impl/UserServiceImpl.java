/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.service.impl;

import com.example.DATN.entity.User;
import com.example.DATN.repository.UserRepository;
import com.example.DATN.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public User register(String username, String rawPassword, String email, String fullName, String phone) {
        if (isUsernameTaken(username)) {
            throw new IllegalStateException("Tên đăng nhập đã tồn tại");
        }
        if (isEmailTaken(email)) {
            throw new IllegalStateException("Email đã được sử dụng");
        }

        User user = new User();
        user.setUsername(username);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setEmail(email);
        user.setFullName(fullName);
        user.setPhone(phone);
        user.setRole("CUSTOMER");
        user.setIsActive(true);

        return userRepository.save(user);
    }

    @Override
    public boolean isUsernameTaken(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean isEmailTaken(String email) {
        return userRepository.existsByEmail(email);
    }
}
