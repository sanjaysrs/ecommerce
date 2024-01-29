package com.ecommerce.project.service;

import com.ecommerce.project.entity.Cart;
import com.ecommerce.project.entity.Coupon;
import com.ecommerce.project.repository.CouponRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class CouponService {

    @Autowired
    CouponRepository couponRepository;

    public List<Coupon> getAllCoupons() {
        return couponRepository.findAll();
    }

    public void saveCoupon(Coupon coupon) {
        couponRepository.save(coupon);
    }

    public void deleteCouponById(int couponId) {
        couponRepository.deleteById(couponId);
    }

    public Optional<Coupon> getCouponById(int couponId) {
        return couponRepository.findById(couponId);
    }

    public Optional<Coupon> getCouponByCouponCode(String couponCode) {
        return couponRepository.findCouponByCouponCode(couponCode);
    }

    public String getDiscountString(Cart cart) {

        Coupon coupon = cart.getCoupon();

        if (coupon==null)
            return null;

        if (coupon.getDiscountType().equals("ABSOLUTE")) {
            return "You get a discount of ₹" + coupon.getDiscountValue();
        }

        return "You get a discount of " + coupon.getDiscountValue() + "%";

    }

    public boolean isCouponCodeValid(String couponCode) {
        Optional<Coupon> couponOptional = couponRepository.findCouponByCouponCode(couponCode);
        return couponOptional.isPresent();
    }
}
