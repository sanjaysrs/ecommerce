package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ProductRepository extends JpaRepository<Product, Long> {
    List<Product> findAllByCategory_Id(int id);

    boolean existsByName(String name);

    List<Product> findAllByNameContaining(String keyword);

    List<Product> findAllByNameContainingAndCategory_Id(String keyword, int categoryId);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity + :additionalQuantity WHERE p.id = :productId")
    void increaseProductQuantity(@Param("productId") Long productId, @Param("additionalQuantity") long additionalQuantity);

    @Modifying
    @Transactional
    @Query("UPDATE Product p SET p.quantity = p.quantity - :decreaseQuantity WHERE p.id = :productId AND p.quantity >= :decreaseQuantity")
    int decreaseProductQuantity(@Param("productId") Long productId, @Param("decreaseQuantity") long decreaseQuantity);

    @Query(value = "SELECT p.* FROM product p " +
            "JOIN (SELECT c.category_id, MIN(p.id) AS product_id " +
            "      FROM product p " +
            "      JOIN category c ON p.category_id = c.category_id " +
            "      GROUP BY c.category_id " +
            "      ORDER BY RAND() " +
            "      LIMIT 3) AS subquery " +
            "ON p.id = subquery.product_id", nativeQuery = true)
    List<Product> findDistinctProductsByCategory();

}
