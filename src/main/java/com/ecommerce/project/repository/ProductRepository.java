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

}
