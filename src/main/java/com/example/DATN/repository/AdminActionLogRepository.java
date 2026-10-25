/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.repository;

/**
 *
 * @author LENOVO
 */

import com.example.DATN.entity.AdminActionLog;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Integer> {
    List<AdminActionLog> findByTargetUserIDOrderByCreatedDateDesc(Integer targetUserID);
    List<AdminActionLog> findTop200ByOrderByCreatedDateDesc();
}
