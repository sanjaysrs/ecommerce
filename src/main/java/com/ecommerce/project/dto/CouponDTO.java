package com.ecommerce.project.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CouponDTO {

    private int couponId;

    @NotBlank(message = "Coupon code must not be blank")
    private String couponCode;

    @NotBlank(message = "Select a valid discount type")
    private String discountType;

    @Min(value = 1, message = "Discount value must be greater than or equal to 1")
    private double discountValue;

    @Min(value = 0, message = "Minimum purchase must be greater than or equal to ₹0")
    private double minimumPurchase;

}
