package com.ecommerce.project.entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
@Entity
public class Wishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id")
    private User user;

    @OneToMany(mappedBy = "wishlist")
    private List<WishlistItem> wishlistItems;

    public boolean getWishlistStatus(Product product) {
        boolean flag=false;
        for (WishlistItem wishlistItem : wishlistItems) {
            if (wishlistItem.getProduct().equals(product)){
                flag = true;
                break;
            }
        }
        return flag;
    }

}
