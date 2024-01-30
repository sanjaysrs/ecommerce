package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.repository.WishlistItemRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.ProductService;
import com.ecommerce.project.service.UserService;
import com.ecommerce.project.service.WishlistService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Optional;

@Controller
public class WishlistController {

    @Autowired
    UserService userService;

    @Autowired
    WishlistService wishlistService;

    @Autowired
    StorageService storageService;

    @Autowired
    CartService cartService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/wishlist")
    public String getWishlist(Model model) {

        Wishlist wishlist = getCurrentUser().getWishlist();
        model.addAttribute("wishlist", wishlist);
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        model.addAttribute("urlList", storageService.getUrlListForSingleWishlist(wishlist));

        return "wishlist";
    }

    @GetMapping("/addToWishlist/{id}")
    public String addToWishlist(@PathVariable("id") long productId, RedirectAttributes redirectAttributes) {

        boolean addedToWishlist = wishlistService.addProductToWishlist(getCurrentUser(), productId);

        if (addedToWishlist)
            redirectAttributes.addFlashAttribute("addedToWishlist", "Product added to wishlist");

        return "redirect:/shop/viewproduct/" + productId;
    }

    @GetMapping("/removeFromWishlist/{id}")
    public String removeFromWishlist(@PathVariable("id") long productId, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        boolean removedFromWishlist = wishlistService.removeProductFromWishlist(getCurrentUser(), productId);

        if (removedFromWishlist)
            redirectAttributes.addFlashAttribute("deletedFromWishlist", "Product was deleted from wishlist");

        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/wishlist");
    }

}
