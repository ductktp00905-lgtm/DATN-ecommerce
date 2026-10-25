package com.example.DATN.repository;
import com.example.DATN.entity.Wishlist;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
/**
 *
 * @author Admin
 */
public interface WishlistRepository
        extends JpaRepository<Wishlist, Integer> {

    List<Wishlist> findByUser_UserIDOrderByCreatedDateDesc(
        Integer userId
    );

    Optional<Wishlist> findByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );

    boolean existsByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );

    void deleteByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );

    void deleteByUser_UserID(Integer userId); 
    
    void deleteByProduct_ProductID(Integer productId);
}