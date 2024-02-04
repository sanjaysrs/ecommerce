package com.ecommerce.project.controller.admin;

import com.ecommerce.project.service.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminWalletController {

    @Autowired
    WalletService walletService;

    @GetMapping("/admin/wallets")
    public String getAdminWallets(Model model) {

        model.addAttribute("wallets", walletService.getAmountAndUserEmail());
        return "walletsAdmin";
    }

}
