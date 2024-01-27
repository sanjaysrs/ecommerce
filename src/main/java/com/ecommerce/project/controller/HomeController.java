package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

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
    CartService cartService;

    @Autowired
    StorageService storageService;

    @Autowired
    WishlistService wishlistService;

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
        model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        return "shop";
    }

    @GetMapping("/shop/search")
    public String searchForProducts(@RequestParam String keyword, @RequestParam Integer categoryId, Model model) {

        List<Product> searchResult = productService.getSearchResult(keyword, categoryId);

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        if (searchResult.isEmpty()) {
            model.addAttribute("notFound", "Your search did not match any products");
            model.addAttribute("categories", categoryService.getAllCategories());
            return "shop";
        }

        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(searchResult));

        return "shop";
    }

    @GetMapping("/shop/category/{id}")
    public String shopByCategory(@PathVariable("id") int id, Model model) {

        model.addAttribute("products", productService.getAllProductsByCategoryId(id));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(productService.getAllProductsByCategoryId(id)));

        if (getCurrentUserRole().equals("[ROLE_USER]"))
            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));

        return "shop";
    }

    @GetMapping("/shop/viewproduct/{id}")
    public String viewProduct(@PathVariable long id, Model model, HttpServletRequest request) {

        Optional<Product> productOptional = productService.getProductById(id);

        if (productOptional.isEmpty()) {
            String referer = request.getHeader("Referer");
            return "redirect:" + (referer!=null ? referer : "/shop");
        }

        Product product = productOptional.get();

        model.addAttribute("product", product);
        model.addAttribute("inStock", productService.isProductInStock(product));
        model.addAttribute("urlList", storageService.getUrlListForSingleProduct(product));
        model.addAttribute("equalStock", false);
        
        if (getCurrentUserRole().equals("[ROLE_USER]")) {

            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
            model.addAttribute("quantityInCart", cartService.getQuantityOfProductInCart(product, getCurrentUser().getCart()));
            model.addAttribute("existsInWishlist", wishlistService.productExistsInWishlist(product, getCurrentUser()));
            model.addAttribute("equalStock", cartService.isQuantityInCartEqualToOrGreaterThanStock(product, getCurrentUser().getCart()));

        }

        return "viewProduct";
    }

}















