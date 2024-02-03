package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.Optional;

@Controller
public class CartController {

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    CartService cartService;

    @Autowired
    AddressService addressService;

    @Autowired
    PaymentMethodService paymentMethodService;

    @Autowired
    StorageService storageService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/addToCart/{id}")
    public String addToCart(@PathVariable long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        Optional<Product> productOptional = productService.getProductById(id);

        if (productOptional.isEmpty())
            return "redirect:/shop";

        boolean addedToCart = cartService.addProductToCart(getCurrentUser(), productOptional.get());

        String referer = request.getHeader("Referer");
        if (referer!=null && referer.contains("cart"))
            return "redirect:/cart";

        if (addedToCart)
            redirectAttributes.addFlashAttribute("addedToCart", true);
        else
            redirectAttributes.addFlashAttribute("notAddedToCart", true);

        return "redirect:/shop/viewproduct/" + id;

    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(@PathVariable long id, RedirectAttributes redirectAttributes, HttpServletRequest request) {

        Product product = productService.getProductById(id).orElse(null);

        boolean removedFromCart = cartService.removeProductFromCart(getCurrentUser(), product);

        String referer = request.getHeader("Referer");
        if (referer!=null && referer.contains("cart"))
            return "redirect:/cart";

        redirectAttributes.addFlashAttribute("removedFromCart", removedFromCart);

        return "redirect:/shop/viewproduct/" + id;

    }

    @GetMapping("/cart")
    public String getCart(Model model) {

        User user = getCurrentUser();
        Cart cart = user.getCart();

        model.addAttribute("cartCount", cartService.getCartCount(user));
        model.addAttribute("total", cartService.getCartTotalWithCouponDiscount(user));
        model.addAttribute("totalWithoutCoupon", cartService.getCartTotalWithoutCouponDiscount(user));
        model.addAttribute("cart", cart);
        model.addAttribute("urlList", storageService.getUrlListForCart(cart));

        return "cart";
    }

    @GetMapping("/cart/removeItem/{id}")
    public String cartItemRemove(@PathVariable long id) {

        cartService.deleteCartItemFromCart(id, getCurrentUser());
        return "redirect:/cart";

    }

}
