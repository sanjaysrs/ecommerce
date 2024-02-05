package com.ecommerce.project.controller;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import com.razorpay.RazorpayException;
import jakarta.servlet.http.HttpSession;
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

    @PostMapping("/checkout/process")
    public String processOrder(@ModelAttribute("address") Long addressId,
                                @ModelAttribute("paymentMethod") int paymentMethodId,
                                RedirectAttributes redirectAttributes) {

        if (!inventoryService.checkInventory(getCurrentUser().getCart()))
            return "redirect:/cart";

        redirectAttributes.addFlashAttribute("addressId", addressId);

        if (paymentMethodId == 1)
            return "redirect:/checkout/COD";

        if (paymentMethodId == 2)
            return "redirect:/checkout/razorpay";

        if (paymentMethodId == 3)
            return "redirect:/checkout/wallet";

        return "redirect:/checkout";

    }

    @GetMapping("/checkout/COD")
    public String COD(@ModelAttribute("addressId") Long addressId,
                      RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("addressId", addressId);
        redirectAttributes.addFlashAttribute("paymentMethodId", 1);
        return "redirect:/checkoutConfirmation";

    }

    @GetMapping("/checkout/razorpay")
    public String razorpay(@ModelAttribute("addressId") Long addressId,
                           Model model) {

        double total = cartService.getCartTotalWithCouponDiscount(getCurrentUser());

        TransactionDetails transactionDetails = razorpayService.createTransaction(total);

        model.addAttribute("amount", total*100);
        model.addAttribute("orderId", transactionDetails.getOrderId());
        model.addAttribute("address", addressId);

        return "razorpayCheckout";

    }

    @PostMapping("/checkout/razorpay")
    public String processRazorpayOrder(@ModelAttribute("razorpay_payment_id") String id, RedirectAttributes redirectAttributes) throws RazorpayException {

        JSONObject notes = razorpayService.fetchPaymentNotes(id);
        redirectAttributes.addFlashAttribute("addressId", notes.getLong("address"));
        redirectAttributes.addFlashAttribute("paymentMethodId", 2);
        return "redirect:/checkoutConfirmation";

    }

    @GetMapping("/checkout/wallet")
    public String checkoutWallet(@ModelAttribute("addressId") Long addressId,
                                 RedirectAttributes redirectAttributes) {

        if (walletService.insufficientFundsInWallet(getCurrentUser())) {

            redirectAttributes.addFlashAttribute("insufficient", "Insufficient funds in wallet. Try another payment method.");
            return "redirect:/checkout";

        }

        walletService.debitFromWallet(getCurrentUser());
        redirectAttributes.addFlashAttribute("addressId", addressId);
        redirectAttributes.addFlashAttribute("paymentMethodId", 3);

        return "redirect:/checkoutConfirmation";
    }

    @GetMapping("/checkoutConfirmation")
    public String confirmCheckout(@ModelAttribute("addressId") Long addressId,
                                  @ModelAttribute("paymentMethodId") int paymentMethodId,
                                  HttpSession session,
                                  RedirectAttributes redirectAttributes) {

        inventoryService.updateInventory(getCurrentUser().getCart());
        orderService.createOrderAndSave(getCurrentUser(), addressId, paymentMethodId, cartService.getCartTotalWithCouponDiscount(getCurrentUser()));
        cartService.removeCouponFromCart(getCurrentUser());
        cartService.clearCart(getCurrentUser().getCart());

        UUID token = generateToken();
        session.setAttribute("token", token);
        redirectAttributes.addFlashAttribute("token", token);

        return "redirect:/checkout/confirm";
    }

    @GetMapping("/checkout/confirm")
    public String renderCheckoutConfirmation(Model model, HttpSession session) {

        if (isValidToken(session.getAttribute("token"), model.getAttribute("token"))) {
            session.removeAttribute("token");
            model.addAttribute("cartCount", 0);
            return "checkoutConfirmation";
        }

        return "redirect:/cart";
    }

    @GetMapping("/checkout/applyCoupon")
    public String applyCoupon(@RequestParam String couponCode, RedirectAttributes redirectAttributes) {

        if (!couponService.isCouponCodeValid(couponCode)) {
            redirectAttributes.addFlashAttribute("invalidCoupon", "Invalid coupon code");
            return "redirect:/checkout";
        }

        Coupon coupon = couponService.getCouponByCouponCode(couponCode).orElseThrow();

        if (!cartService.hasMinimumPurchase(coupon, getCurrentUser())) {
            redirectAttributes.addFlashAttribute("invalidCoupon", "This coupon is only valid for purchases of " + coupon.getMinimumPurchase() + " and above");
            return "redirect:/checkout";
        }

        cartService.applyCouponToCart(getCurrentUser(), coupon);

        return "redirect:/checkout";

    }

    @GetMapping("/checkout/removeCoupon")
    public String removeCoupon() {

        cartService.removeCouponFromCart(getCurrentUser());
        return "redirect:/checkout";
    }

    private UUID generateToken() {
        return UUID.randomUUID();
    }

    private boolean isValidToken(Object sessionToken, Object modelToken) {
        return  (sessionToken != null && sessionToken.equals(modelToken));
    }
}

























