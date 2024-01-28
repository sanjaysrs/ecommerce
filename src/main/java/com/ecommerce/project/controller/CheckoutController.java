package com.ecommerce.project.controller;

import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
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

    @PostMapping("/checkout/process2")
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
                      @ModelAttribute("paymentMethod") int paymentMethodId,
                      Model model) {

        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), selectedAddressId, paymentMethodId, cartService.getCartTotal(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/razorpay")
    public String razorpay(@ModelAttribute("address") Long selectedAddressId,
                           @ModelAttribute("paymentMethod") int paymentMethodId,
                           Model model) {

        double total = cartService.getCartTotal(getCurrentUser());

        TransactionDetails transactionDetails = razorpayService.createTransaction(total);

        model.addAttribute("amount", total*100);
        model.addAttribute("orderId", transactionDetails.getOrderId());
        model.addAttribute("address", selectedAddressId);
        model.addAttribute("paymentMethod", paymentMethodId);

        return "xxx";

    }

    @PostMapping("/checkout/razorpay")
    public String processOrder(@ModelAttribute("razorpay_payment_id") String id, Model model) throws RazorpayException {

        JSONObject notes = razorpayService.fetchPaymentNotes(id);
        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), notes.getLong("address"), notes.getInt("paymentMethod"), cartService.getCartTotal(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @GetMapping("/checkout/wallet")
    public String checkoutWallet(@ModelAttribute("address") Long selectedAddressId,
                                 @ModelAttribute("paymentMethod") int paymentMethodId,
                                 RedirectAttributes redirectAttributes,
                                 Model model) {

        if (walletService.insufficientFundsInWallet(getCurrentUser())) {

            redirectAttributes.addFlashAttribute("insufficient", "Insufficient funds in wallet. Try another payment method.");
            return "checkoutNew";

        }

        walletService.debitFromWallet(getCurrentUser());
        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), selectedAddressId, paymentMethodId, cartService.getCartTotal(getCurrentUser()));
        cartService.clearCart(getCurrentUser().getCart());

        model.addAttribute("successMessage", "Your order has been placed successfully!");
        model.addAttribute("cartCount", 0);

        return "checkoutConfirmation";
    }

    @PostMapping("/checkout/applyCoupon")
    public String applyCoupon(@ModelAttribute("couponCode") String couponCode, Model model, Principal principal) {

        Optional<Coupon> couponOptional = couponService.getCouponByCouponCode(couponCode);

        User user = userService.findUserByEmail(principal.getName());
        List<Address> userAddresses = addressService.getAddressesForUser(user);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();

        double total = 0.0;
        if (user != null) {
            Cart userCart = user.getCart();
            if (userCart != null) {
                List<CartItem> cartItems = userCart.getCartItems();
                for (CartItem cartItem : cartItems) {
                    total += cartItem.getProduct().getPrice() * cartItem.getQuantity();
                }
            }
        }

        if (couponOptional.isEmpty()) {
            model.addAttribute("invalidCoupon", "Invalid coupon code");

            model.addAttribute("cart", cartItemList);

            model.addAttribute("total", total);

            model.addAttribute("userAddresses", userAddresses);

            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

            //  Add cartCount
            int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
            model.addAttribute("cartCount", cartCount);

            return "checkoutNew";
        }

        if (total < couponOptional.get().getMinimumPurchase()) {
            model.addAttribute("invalidCoupon", "This coupon is only valid for purchases of " + couponOptional.get().getMinimumPurchase() + " and above");

            model.addAttribute("cart", cartItemList);

            model.addAttribute("total", total);

            model.addAttribute("userAddresses", userAddresses);

            model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

            //  Add cartCount
            int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
            model.addAttribute("cartCount", cartCount);

            return "checkoutNew";
        }

        if (couponOptional.get().getDiscountType().equals("ABSOLUTE")) {
            total = total - couponOptional.get().getDiscountValue();
            model.addAttribute("discountApplied", "You get a discount of ₹" + couponOptional.get().getDiscountValue());
        }

        if (couponOptional.get().getDiscountType().equals("PERCENTAGE")) {
            total = total - (couponOptional.get().getDiscountValue()/100*total);
            model.addAttribute("discountApplied", "You get a discount of " + couponOptional.get().getDiscountValue() + "%");
        }

        model.addAttribute("couponApplied", "Coupon applied");

        model.addAttribute("cart", cartItemList);

        model.addAttribute("total", total);

        model.addAttribute("userAddresses", userAddresses);

        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "checkoutNew";

    }
}

























