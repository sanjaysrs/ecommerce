package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
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
    AddressRepository addressRepository;

    @Autowired
    PaymentMethodService paymentMethodService;

    @Autowired
    StorageService storageService;

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().toString();
    }

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
        List<Address> userAddresses = addressService.getAddressesForUser(user);

        Cart userCartEntity = user.getCart();
        List<CartItem> cartItemList = userCartEntity.getCartItems();
        if (cartItemList.isEmpty())
            return "redirect:/cart";
        for (CartItem cartItem : cartItemList) {
            if (cartItem.getQuantity()>cartItem.getProduct().getQuantity()) {
                return "redirect:/cart";
            }
        }

        model.addAttribute("cart", cartItemList);

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

        model.addAttribute("total", total);
        model.addAttribute("userAddresses", userAddresses);
        model.addAttribute("paymentMethods", paymentMethodService.getAllPaymentMethods());

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "checkoutNew";
    }

    @PostMapping("/add-address-checkout")
    public String addAddress(@ModelAttribute("address") Address address) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String userEmail = authentication.getName();

        User user = userService.findUserByEmail(userEmail);

        Address newAddress = new Address(user);
        newAddress.setStreetAddress(address.getStreetAddress());
        newAddress.setCity(address.getCity());
        newAddress.setState(address.getState());
        newAddress.setPostalCode(address.getPostalCode());
        newAddress.setCountry(address.getCountry());

        addressRepository.save(newAddress);

        return "redirect:/checkout";
    }

}
