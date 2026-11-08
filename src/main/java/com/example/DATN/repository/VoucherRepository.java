package com.example.DATN.repository;

import com.example.DATN.entity.Voucher;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.Optional;

public interface VoucherRepository extends JpaRepository<Voucher, Integer> {
    Optional<Voucher> findByCodeIgnoreCase(String code);

    // Chỉ tăng khi còn lượt -> tránh 2 người dùng cùng lúc vượt số lượng
    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount + 1 WHERE v.voucherID = :id AND v.usedCount < v.quantity")
    int increaseUsedCount(@Param("id") Integer id);

    @Modifying
    @Query("UPDATE Voucher v SET v.usedCount = v.usedCount - 1 WHERE v.voucherID = :id AND v.usedCount > 0")
    int decreaseUsedCount(@Param("id") Integer id);
}