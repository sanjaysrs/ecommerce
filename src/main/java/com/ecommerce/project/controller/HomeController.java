package com.ecommerce.project.controller;

import com.ecommerce.project.entity.CartItem;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.CartItemRepository;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.CategoryService;
import com.ecommerce.project.service.ProductService;
import com.ecommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class HomeController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    CartService cartService;

    private String getCurrentUserRole() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication.getAuthorities().toString();
    }

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/")
    public String home(Model model) {

        if (getCurrentUserRole().equals("[ROLE_ANONYMOUS]")) {
            return "indexNew";
        }

        if (getCurrentUserRole().equals("[ROLE_USER]")) {
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
            return "indexNew";
        }

        return "redirect:/admin";
    }

    @GetMapping("/shop")
    public String shop(Model model) {
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("products", productService.getAllProducts());

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        return "shop";
    }

    @GetMapping("/shop/search")
    public String searchForProducts(@RequestParam String keyword, @RequestParam Integer within, Model model) {

        List<Product> searchResult;

        if (within==0)
            searchResult = productService.getProductsByNameContaining(keyword);
        else
            searchResult = productService.getProductsByNameAndCategory(keyword, within);

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        if (searchResult.isEmpty()) {
            model.addAttribute("notFound", "Your search did not match any products");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "shop";
        }

        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "shop";
    }

    @GetMapping("/shop/category/{id}")
    public String shopByCategory(@PathVariable("id") int id, Model model) {

        model.addAttribute("products", productService.getAllProductsByCategoryId(id));
        model.addAttribute("categories", categoryService.getAllCategories());

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        return "shop";
    }

    @GetMapping("/shop/viewproduct/{id}")
    public String viewProduct(@PathVariable long id, Principal principal, Model model) {

        // Get the currently logged-in user
        User user = userService.findUserByEmail(principal.getName());

        Optional<CartItem> cartItemOptional =
                cartItemRepository.findCartItemByProductAndCart(productService.getProductById(id).get(),user.getCart());

        if (cartItemOptional.isPresent()) {
            CartItem cartItem = cartItemOptional.get();
            model.addAttribute("quantityInCart", cartItem.getQuantity());
        } else {
            model.addAttribute("quantityInCart", 0);
        }

        boolean flag = user.getWishlist().getWishlistStatus(productService.getProductById(id).get());

        if (flag) {
            model.addAttribute("ondu", "Product is there in wishlist");
        } else {
            model.addAttribute("illa", "Product is not there in wishlist");
        }

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        model.addAttribute("product", productService.getProductById(id).get());

        //Check inventory for stock availability and add the required model attributes accordingly
        Product product = productService.getProductById(id).get();
        Optional<CartItem> cartItemOptional2 = cartItemRepository.findCartItemByProductAndCart(product, user.getCart());
        if (product.getQuantity()==0)
            model.addAttribute("outOfStock", "OUT OF STOCK");
        else if (cartItemOptional2.isPresent())
            if (product.getQuantity()==cartItemOptional2.get().getQuantity())
                model.addAttribute("equalStock", "QUANTITY IN CART ALREADY EQUALS STOCK IN INVENTORY");
            else if (product.getQuantity()<cartItemOptional2.get().getQuantity())
                model.addAttribute("outOfStock", "OUT OF STOCK");
            else
                model.addAttribute("inStock", "IN STOCK");
        else
            model.addAttribute("inStock", "IN STOCK");

        return "viewProduct";
    }

}















