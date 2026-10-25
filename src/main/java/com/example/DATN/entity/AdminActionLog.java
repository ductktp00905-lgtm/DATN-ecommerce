/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.entity;

/**
 *
 * @author LENOVO
 */

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "AdminActionLogs")
@Getter @Setter
@NoArgsConstructor
public class AdminActionLog {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer logID;

    private String adminUsername;
    private Integer targetUserID;
    private String targetUsername;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(length = 500)
    private String detail;
    
    

    private LocalDateTime createdDate = LocalDateTime.now();

    public AdminActionLog(String adminUsername, Integer targetUserID, String targetUsername,
                          String action, String detail) {
        this.adminUsername = adminUsername;
        this.targetUserID = targetUserID;
        this.targetUsername = targetUsername;
        this.action = action;
        this.detail = detail;
    }
}
