package com.ecommerce.project.controller;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.*;

@Controller
public class CheckoutController {

    @Autowired
    private ProductService productService;

    @Autowired
    private AddressService addressService;

    @Autowired
    private OrderService orderService;

    @Autowired
    private UserService userService;

    @Autowired
    private OrderStatusService orderStatusService;

    @Autowired
    private CartItemRepository cartItemRepository;

    @Autowired
    private PaymentMethodService paymentMethodService;

    @Autowired
    private WalletService walletService;

    @Autowired
    private CouponService couponService;

    @Autowired
    CartService cartService;

    @Autowired
    InventoryService inventoryService;

    @Autowired
    RazorpayService razorpayService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/checkout")
    public String checkout(Model model) {

        User user = getCurrentUser();

        if (cartService.isCartEmpty(user.getCart()))
            return "redirect:/cart";

        model.addAttribute("discount", couponService.getDiscountString(user.getCart()));
        model.addAttribute("cart", user.getCart());
        model.addAttribute("total", cartService.getCartTotalWithCouponDiscount(user));
        model.addAttribute("totalWithoutCoupon", cartService.getCartTotalWithoutCouponDiscount(user));
        model.addAttribute("userAddresses", addressService.getAddressesForUser(user));
        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());
        model.addAttribute("cartCount", cartService.getCartCount(user));

        return "checkoutNew";
    }

    @PostMapping("/add-address-checkout")
    public String addAddress(@ModelAttribute("address") AddressDTO addressDTO) {

        addressService.saveAddress(addressDTO, getCurrentUser());
        return "redirect:/checkout";

    }

    @PostMapping("/checkout/process")
    public String processOrder2(@ModelAttribute("address") Long selectedAddressId,
                                @ModelAttribute("paymentMethod") int paymentMethodId,
                                RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("address", selectedAddressId);
        redirectAttributes.addFlashAttribute("paymentMethod", paymentMethodId);

        if (paymentMethodId == 1)
            return "redirect:/checkout/COD";

        if (paymentMethodId == 2)
            return "redirect:/checkout/razorpay";

        if (paymentMethodId == 3)
            return "redirect:/checkout/wallet";

        return "redirect:/checkout";

    }

    @GetMapping("/checkout/COD")
    public String COD(@ModelAttribute("address") Long selectedAddressId,
                      Model model) {

        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), selectedAddressId, 1, cartService.getCartTotalWithCouponDiscount(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/razorpay")
    public String razorpay(@ModelAttribute("address") Long selectedAddressId,
                           Model model) {

        double total = cartService.getCartTotalWithCouponDiscount(getCurrentUser());

        TransactionDetails transactionDetails = razorpayService.createTransaction(total);

        model.addAttribute("amount", total*100);
        model.addAttribute("orderId", transactionDetails.getOrderId());
        model.addAttribute("address", selectedAddressId);

        return "xxx";

    }

    @PostMapping("/checkout/razorpay")
    public String processOrder(@ModelAttribute("razorpay_payment_id") String id, Model model) throws RazorpayException {

        JSONObject notes = razorpayService.fetchPaymentNotes(id);
        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), notes.getLong("address"), 2, cartService.getCartTotalWithCouponDiscount(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/wallet")
    public String checkoutWallet(@ModelAttribute("address") Long selectedAddressId,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (walletService.insufficientFundsInWallet(getCurrentUser())) {

            redirectAttributes.addFlashAttribute("insufficient", "Insufficient funds in wallet. Try another payment method.");
            return "checkoutNew";

        }

        walletService.debitFromWallet(getCurrentUser());
        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), selectedAddressId, 3, cartService.getCartTotalWithCouponDiscount(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/applyCoupon")
    public String applyCoupon(@RequestParam String couponCode, RedirectAttributes redirectAttributes) {

        Optional<Coupon> couponOptional = couponService.getCouponByCouponCode(couponCode);

        if (couponOptional.isEmpty()) {
            redirectAttributes.addFlashAttribute("invalidCoupon", "Invalid coupon code");
            return "redirect:/checkout";
        }

        Coupon coupon = couponOptional.get();

        double total = cartService.getCartTotalWithoutCouponDiscount(getCurrentUser());
        if (total < coupon.getMinimumPurchase()) {
            redirectAttributes.addFlashAttribute("invalidCoupon", "This coupon is only valid for purchases of " + coupon.getMinimumPurchase() + " and above");
            return "redirect:/checkout";
        }

        cartService.applyCouponToCart(getCurrentUser(), coupon);

        return "redirect:/checkout";

    }
}

























