package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Cart;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;


public interface CartRepository extends JpaRepository<Cart, Long> {

    @Modifying
    @Query("UPDATE Cart c SET c.coupon = null WHERE c.coupon.id = :couponId")
    void removeCouponFromCart(@Param("couponId") int couponId);

}