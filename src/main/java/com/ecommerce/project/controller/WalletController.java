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
                                RedirectAttributes redirectAttributes) throws RazorpayException {

        walletService.addToWallet(getCurrentUser(), razorpayService.getTransactionAmount(id));
        redirectAttributes.addFlashAttribute("addedToWallet", "The amount was successfully added to the wallet");
        return "redirect:/wallet";
    }

}
