/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "Users")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userID;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false, unique = true, length = 100)
    private String email;

    private String fullName;
    private String phone;

    @Column(nullable = false)
    private String role = "CUSTOMER";

    private Boolean isActive = true;

    private LocalDateTime createdDate = LocalDateTime.now();
    
        private Boolean emailVerified = false;
    private Boolean phoneVerified = false;

    private String emailVerifyCode;
    private LocalDateTime emailVerifyExpiry;
    private String phoneVerifyCode;
    private LocalDateTime phoneVerifyExpiry;

    private Boolean deletionRequested = false;
    private LocalDateTime deletionRequestedDate;

    @Column(length = 500)
    private String deletionReason;
    
        // Khóa có thời hạn: isActive=false + lockedUntil; khóa vĩnh viễn: isActive=false + lockedUntil=null
    private LocalDateTime lockedUntil;

    @Column(length = 300)
    private String lockReason;

        @Transient
    public boolean isCurrentlyLocked() {
        if (isActive == null || isActive) return false;
        return lockedUntil == null || lockedUntil.isAfter(LocalDateTime.now());
    }

    @PostLoad
    private void fillDefaults() {
        if (isActive == null) isActive = true;
        if (emailVerified == null) emailVerified = false;
        if (phoneVerified == null) phoneVerified = false;
        if (deletionRequested == null) deletionRequested = false;
    }
}
