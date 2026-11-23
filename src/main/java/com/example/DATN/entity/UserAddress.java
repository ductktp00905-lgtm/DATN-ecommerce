package com.example.DATN.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "user_addresses")
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
public class UserAddress {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "address_id")
    private Integer addressId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "recipient_name", nullable = false, length = 100, columnDefinition = "NVARCHAR(100)")
    private String recipientName;

    @Column(nullable = false, length = 20)
    private String phone;

    @Column(name = "detail_address", nullable = false, length = 300, columnDefinition = "NVARCHAR(300)")
    private String detailAddress;

    @Column(name = "address_label", length = 50, columnDefinition = "NVARCHAR(50)")
    private String addressLabel = "Nhà riêng";

    @Column(name = "is_default", nullable = false)
    private Boolean isDefault = false;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    /**
     * Định dạng địa chỉ đầy đủ bao gồm thông tin liên hệ và vị trí.
     */
    public String getFullDisplay() {
        return recipientName + " (" + phone + ") - " + detailAddress;
    }
}
