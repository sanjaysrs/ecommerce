package com.ecommerce.project.controller;

import com.ecommerce.project.dto.WalletDTO;
import com.ecommerce.project.entity.TransactionDetails;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.entity.Wallet;
import com.ecommerce.project.service.*;
import com.razorpay.Payment;
import com.razorpay.RazorpayClient;
import com.razorpay.RazorpayException;
import jakarta.validation.Valid;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

@Controller
public class WalletController {

    @Autowired
    UserService userService;

    @Autowired
    WalletService walletService;

    @Autowired
    OrderService orderService;

    @Autowired
    RazorpayService razorpayService;

    @Autowired
    CartService cartService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/wallet")
    public String getWallet(Model model) {

        model.addAttribute("wallet", getCurrentUser().getWallet());
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        if (!model.containsAttribute("amount"))
            model.addAttribute("amount", new WalletDTO());
        if (!model.containsAttribute("display"))
            model.addAttribute("display", "none");
        return "wallet";
    }

    @PostMapping("/addAmountToWallet")
    public String addAmountToWallet(@ModelAttribute("amount") @Valid WalletDTO walletDTO,
                                    BindingResult bindingResult,
                                    RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("display", "block");
            redirectAttributes.addFlashAttribute("amount", walletDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.amount", bindingResult);
            return "redirect:/wallet";
        }

        TransactionDetails transactionDetails = razorpayService.createTransaction(walletDTO.getAmount());
        redirectAttributes.addFlashAttribute("amount", walletDTO.getAmount()*100);
        redirectAttributes.addFlashAttribute("orderId", transactionDetails.getOrderId());

        return "redirect:/addAmountToWallet";
    }

    @GetMapping("/addAmountToWallet")
    public String renderRazorpayWallet(Model model) {
        if (!model.containsAttribute("amount") || !model.containsAttribute("orderId"))
            return "redirect:/wallet";
        return "razorpayWallet";
    }

    @PostMapping("/addedToWallet")
    public String addedToWallet(@ModelAttribute("razorpay_payment_id") String id,
                                Model model,
                                Principal principal) throws RazorpayException {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Wallet wallet = userService.findUserByEmail(principal.getName()).getWallet();

        //Fetch the razorpay payment by its id
        RazorpayClient razorpayClient = new RazorpayClient("rzp_test_amAJ6g1mhBlQKL", "xW9gfY6xByn88aKq8GixUNZ0");
        Payment payment = razorpayClient.payments.fetch(id);
        int amountInt = payment.get("amount");
        double amount = (double) amountInt /100;

        wallet.setAmount(wallet.getAmount() + amount);
        walletService.save(wallet);
        model.addAttribute("wallet", wallet);
        model.addAttribute("addedToWallet", "The amount was successfully added to the wallet");

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "wallet";
    }


}
