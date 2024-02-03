package com.ecommerce.project.repository;

import com.ecommerce.project.entity.Coupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface CouponRepository extends JpaRepository<Coupon, Integer> {

    @Query("SELECT c FROM Coupon c WHERE BINARY(c.couponCode) = :couponCode")
    Optional<Coupon> findCouponByCouponCode(@Param("couponCode") String couponCode);
}
