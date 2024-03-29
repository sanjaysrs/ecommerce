package com.ecommerce.project.repository;

import com.ecommerce.project.entity.*;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WishlistItemRepository extends JpaRepository<WishlistItem, Long> {

    Optional<WishlistItem> findByProductAndWishlist(Product product, Wishlist wishlist);

    boolean existsByProductAndWishlist(Product product, Wishlist wishlist);

}
