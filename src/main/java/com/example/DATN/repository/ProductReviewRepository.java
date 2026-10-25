package com.example.DATN.repository;
import com.example.DATN.entity.ProductReview;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import com.example.DATN.dto.RatingStatDTO;
import com.example.DATN.entity.Product;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
/**
 *
 * @author Admin
 */

public interface ProductReviewRepository
        extends JpaRepository<ProductReview, Integer> {
    

    List<ProductReview> findByProduct_ProductIDOrderByCreatedDateDesc(
        Integer productId
    );

    Optional<ProductReview> findByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );
    
    List<ProductReview> findAllByOrderByCreatedDateDesc();

    boolean existsByUser_UserIDAndProduct_ProductID(
        Integer userId,
        Integer productId
    );
    
        @Query("SELECT r.product FROM ProductReview r "
         + "GROUP BY r.product "
         + "HAVING AVG(r.rating) >= :minRating AND COUNT(r) >= :minReviews "
         + "ORDER BY AVG(r.rating) DESC, COUNT(r) DESC")
    List<Product> findHighRatedProducts(@Param("minRating") double minRating,
                                        @Param("minReviews") long minReviews,
                                        Pageable pageable);

    @Query("SELECT new com.example.DATN.dto.RatingStatDTO(r.product.productID, AVG(r.rating), COUNT(r)) "
         + "FROM ProductReview r WHERE r.product.productID IN :ids "
         + "GROUP BY r.product.productID")
    List<RatingStatDTO> findRatingStats(@Param("ids") List<Integer> ids);

    long countByProduct_ProductID(Integer productId);

    List<ProductReview> findByUser_UserID(Integer userId);
    
    void deleteByUser_UserID(Integer userId); 
    
    void deleteByProduct_ProductID(Integer productId);
}