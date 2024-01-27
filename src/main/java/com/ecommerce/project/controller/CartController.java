package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
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
    public String addToCart(@PathVariable long id, RedirectAttributes redirectAttributes) {

        Optional<Product> productOptional = productService.getProductById(id);

        if (productOptional.isEmpty())
            return "redirect:/shop";

        boolean addedToCart = cartService.addProductToCart(getCurrentUser(), productOptional.get());

        if (addedToCart)
            redirectAttributes.addFlashAttribute("addedToCart", true);
        else
            redirectAttributes.addFlashAttribute("notAddedToCart", true);

        return "redirect:/shop/viewproduct/" + id;

    }

    @GetMapping("/removeFromCart/{id}")
    public String removeFromCart(@PathVariable long id, RedirectAttributes redirectAttributes) {

        Product product = productService.getProductById(id).orElse(null);

        boolean removedFromCart = cartService.removeProductFromCart(getCurrentUser(), product);

        redirectAttributes.addFlashAttribute("removedFromCart", removedFromCart);

        return "redirect:/shop/viewproduct/" + id;

    }

    @GetMapping("/cart")
    public String getCart(Model model) {

        User user = getCurrentUser();
        Cart cart = user.getCart();
        List<CartItem> cartItems = cart.getCartItems();

        model.addAttribute("cartCount", cartService.getCartCount(user));
        model.addAttribute("total", cartService.getCartTotal(user));
        model.addAttribute("cartItems", cartItems);
        model.addAttribute("urlList", storageService.getUrlListForSingleCart(cart));

        return "cart";
    }

    @GetMapping("/cart/removeItem/{id}")
    public String cartItemRemove(@PathVariable long id) {

        cartService.deleteCartItemFromCart(id, getCurrentUser());
        return "redirect:/cart";

    }

    @GetMapping("/checkout")
    public String checkout(Model model) {

        User user = getCurrentUser();

        if (cartService.isCartEmpty(user.getCart()))
            return "redirect:/cart";

        model.addAttribute("cartItems", user.getCart().getCartItems());
        model.addAttribute("total", cartService.getCartTotal(user));
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

}
