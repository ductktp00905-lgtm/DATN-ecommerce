/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.example.DATN.entity;
import jakarta.persistence.*;
import java.time.LocalDateTime;
import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
/**
 *
 * @author Admin
 */
@Entity
@Table(
    name = "ProductReviews",
    uniqueConstraints = @UniqueConstraint(
        columnNames = {"UserID", "ProductID"}
    )
)
@Getter
@Setter
@NoArgsConstructor
public class ProductReview {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer reviewID;

    @ManyToOne(optional = false)
    @JoinColumn(name = "UserID", nullable = false)
    private User user;

    @ManyToOne(optional = false)
    @JoinColumn(name = "ProductID", nullable = false)
    private Product product;

    @Column(nullable = false)
    private Integer rating;

    @Column(length = 1000)
    private String comment;

    @Column(nullable = false)
    private LocalDateTime createdDate = LocalDateTime.now();
}
