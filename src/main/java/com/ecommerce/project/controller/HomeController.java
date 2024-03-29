package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.service.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
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

    @GetMapping("/readme")
    public String readme(HttpSession session) {
        session.setAttribute("readme", true);
        return "readme";
    }

    @GetMapping("/")
    public String home(Model model, HttpSession session) {

        if (session.getAttribute("readme")==null)
            return "redirect:/readme";

        if (getCurrentUserRole().equals("[ROLE_ANONYMOUS]")) {
            List<Product> products = productService.getThreeProductsWithDistinctCategory();
            model.addAttribute("products", products);
            model.addAttribute("urlList", storageService.getUrlList(products));
            return "indexNew";
        }

        if (getCurrentUserRole().equals("[ROLE_USER]")) {
            List<Product> products = productService.getThreeProductsWithDistinctCategory();
            model.addAttribute("products", products);
            model.addAttribute("urlList", storageService.getUrlList(products));
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
        model.addAttribute("urlList", storageService.getUrlListForProduct(product));

        if (getCurrentUserRole().equals("[ROLE_ANONYMOUS]")) {
            model.addAttribute("inStock", productService.isProductInStock(product));
            model.addAttribute("outOfStock", !productService.isProductInStock(product));
        }

        if (getCurrentUserRole().equals("[ROLE_USER]")) {

            model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
            model.addAttribute("quantityInCart", cartService.getQuantityOfProductInCart(product, getCurrentUser().getCart()));
            model.addAttribute("existsInWishlist", wishlistService.productExistsInWishlist(product, getCurrentUser()));

            model.addAttribute("inStock", cartService.isProductInStock(getCurrentUser().getCart(), product));
            model.addAttribute("equalStock", cartService.isProductEqualStock(getCurrentUser().getCart(), product));
            model.addAttribute("outOfStock", cartService.isProductOutOfStock(getCurrentUser().getCart(), product));

        }

        return "viewProduct";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

}















