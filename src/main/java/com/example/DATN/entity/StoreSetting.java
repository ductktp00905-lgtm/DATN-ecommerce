package com.example.DATN.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "store_settings")
public class StoreSetting {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "store_name", nullable = false, columnDefinition = "NVARCHAR(255)")
    private String storeName = "FlareHub Flagship Store — TP. Hồ Chí Minh";

    @Column(name = "address", nullable = false, columnDefinition = "NVARCHAR(500)")
    private String address = "Số 134 Nguyễn Thái Học, Phường Phạm Ngũ Lão, Quận 1, TP. Hồ Chí Minh";

    @Column(name = "phone", columnDefinition = "NVARCHAR(100)")
    private String phone = "1800.2097 - 098.123.4567";

    @Column(name = "email", length = 100)
    private String email = "cskh@flarehub.vn";

    @Column(name = "opening_hours", columnDefinition = "NVARCHAR(200)")
    private String openingHours = "08:00 - 21:30 (Cả Thứ 7, CN & Ngày lễ)";

    @Column(name = "map_embed_url", columnDefinition = "NVARCHAR(MAX)")
    private String mapEmbedUrl = "";

    @Column(name = "updated_at")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public StoreSetting() {}

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getOpeningHours() {
        return openingHours;
    }

    public void setOpeningHours(String openingHours) {
        this.openingHours = openingHours;
    }

    public String getMapEmbedUrl() {
        return mapEmbedUrl;
    }

    public void setMapEmbedUrl(String mapEmbedUrl) {
        this.mapEmbedUrl = mapEmbedUrl;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }
}
