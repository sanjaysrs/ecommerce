package com.ecommerce.project.service;

import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.entity.Wishlist;
import com.ecommerce.project.entity.WishlistItem;
import com.ecommerce.project.repository.WishlistItemRepository;
import com.ecommerce.project.repository.WishlistRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class WishlistService {

    @Autowired
    WishlistRepository wishlistRepository;

    @Autowired
    WishlistItemRepository wishlistItemRepository;

    @Autowired
    ProductService productService;

    public boolean addProductToWishlist(User user, long productId) {

        Product product = productService.getProductById(productId).orElse(null);
        Wishlist wishlist = user.getWishlist();

        Optional<WishlistItem> wishlistItemOptional =
                wishlistItemRepository.findByProductAndWishlist(product, wishlist);

        if (wishlistItemOptional.isPresent())
            return false;

        WishlistItem wishlistItem = new WishlistItem();
        wishlistItem.setProduct(product);
        wishlistItem.setWishlist(wishlist);
        wishlistItemRepository.save(wishlistItem);
        return true;

    }

    public boolean productExistsInWishlist(Product product, User user) {
        return wishlistItemRepository.existsByProductAndWishlist(product, user.getWishlist());
    }

}
