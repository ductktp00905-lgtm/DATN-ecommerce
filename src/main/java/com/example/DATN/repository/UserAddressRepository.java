package com.example.DATN.repository;

import com.example.DATN.entity.UserAddress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Integer> {

    List<UserAddress> findByUser_UserIDOrderByIsDefaultDescCreatedAtDesc(Integer userId);

    Optional<UserAddress> findByUser_UserIDAndIsDefaultTrue(Integer userId);

    Optional<UserAddress> findByAddressIdAndUser_UserID(Integer addressId, Integer userId);

    long countByUser_UserID(Integer userId);

    @Modifying
    @Query("UPDATE UserAddress a SET a.isDefault = false WHERE a.user.userID = :userId")
    void clearDefaultForUser(@Param("userId") Integer userId);
}
