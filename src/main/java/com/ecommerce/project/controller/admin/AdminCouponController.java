package com.ecommerce.project.controller.admin;

import com.ecommerce.project.dto.CouponDTO;
import com.ecommerce.project.entity.Coupon;
import com.ecommerce.project.service.CouponService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class AdminCouponController {

    @Autowired
    CouponService couponService;

    @GetMapping("/admin/coupons")
    public String getCoupons(Model model) {
        model.addAttribute("allCoupons", couponService.getAllCoupons());
        return "coupons";
    }

    @GetMapping("/admin/createCoupon")
    public String createCoupon(Model model) {

        if (!model.containsAttribute("coupon"))
            model.addAttribute("coupon", new CouponDTO());
        return "createCoupon";
    }

    @PostMapping("/admin/createCoupon")
    public String postCoupon(@ModelAttribute("coupon") @Valid CouponDTO couponDTO,
                             BindingResult bindingResult,
                             @ModelAttribute("status") int status,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("coupon", couponDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.coupon", bindingResult);
            if (status == 2)
                redirectAttributes.addFlashAttribute("edit", true);
            return "redirect:/admin/createCoupon";
        }

        if (status==1) {
            Optional<Coupon> couponOptional = couponService.getCouponByCouponCode(couponDTO.getCouponCode());
            if (couponOptional.isPresent()) {
                redirectAttributes.addFlashAttribute("coupon", couponDTO);
                redirectAttributes.addFlashAttribute("error", "Coupon with same coupon code already present");
                return "redirect:/admin/createCoupon";
            }
            redirectAttributes.addFlashAttribute("couponCreated", "Coupon was created successfully");
        }

        couponService.saveCoupon(couponDTO);
        if (status==2)
            redirectAttributes.addFlashAttribute("couponCreated", "Coupon was edited successfully");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/delete/{couponId}")
    public String deleteCoupon(@PathVariable("couponId") int couponId, RedirectAttributes redirectAttributes) {
        couponService.deleteCouponById(couponId);
        redirectAttributes.addFlashAttribute("couponDeleted", "Coupon was deleted successfully");
        return "redirect:/admin/coupons";
    }

    @GetMapping("/admin/coupons/edit/{couponId}")
    public String editCoupon(@PathVariable("couponId") int couponId, Model model) {

        Optional<Coupon> couponOptional = couponService.getCouponById(couponId);
        if (couponOptional.isEmpty())
            return "redirect:/admin/coupons";

        model.addAttribute("coupon", couponOptional.get());
        model.addAttribute("edit", true);
        return "createCoupon";
    }

}
