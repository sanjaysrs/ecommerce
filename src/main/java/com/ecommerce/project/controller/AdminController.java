package com.ecommerce.project.controller;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@Controller
public class AdminController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    UserService userService;

    @Autowired
    StorageService storageService;

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    @GetMapping("/admin/inventory")
    public String getInventory(Model model) {
        model.addAttribute("products", productService.getAllProducts());
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
        return "inventory";
    }

    @GetMapping("/admin/inventory/update/{id}")
    public String updateStock(@PathVariable Long id, Model model) {
        Product product = productService.getProductById(id).get();
        model.addAttribute("product", product);
        return "updateStock";
    }

    @PostMapping("/admin/inventory/addStock/{id}")
    public String addStock(@PathVariable Long id, @ModelAttribute("addStock") long add, Model model) {
        Product product = productService.getProductById(id).get();
        product.setQuantity(product.getQuantity() + add);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock added successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory/removeStock/{id}")
    public String removeStock(@PathVariable Long id, @ModelAttribute("removeStock") long subtract, Model model) {

        Product product = productService.getProductById(id).get();

        if (product.getQuantity()<subtract) {
            List<Product> products = new ArrayList<>();
            products.add(product);
            model.addAttribute("products", products);
            model.addAttribute("urlList", storageService.getUrlList(products));
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("stockStatus", "Could not remove required stock due to insufficient stock in inventory");
            return "inventory";
        }

        product.setQuantity(product.getQuantity() - subtract);
        productService.addProduct(product);

        List<Product> products = new ArrayList<>();
        products.add(product);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("stockStatus", "Stock removed successfully");
        return "inventory";
    }

    @PostMapping("/admin/inventory")
    public String searchInventory(@ModelAttribute("name") String name, Model model) {

        List<Product> productList = productService.getAllProducts();

        List<Product> searchResult = new ArrayList<>();

        for (Product product : productList) {
            if (product.getName().toLowerCase().contains(name.toLowerCase())) {
                searchResult.add(product);
            }
        }

        if (searchResult.isEmpty()) {
            model.addAttribute("products", productService.getAllProducts());
            model.addAttribute("searchError", "Your search did not match any products.");
            model.addAttribute("categories", categoryService.getAllCategories());
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
            return "inventory";
        }

        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlList(searchResult));

        return "inventory";

    }

    @GetMapping("/admin/inventory/filter/{id}")
    public String filterInventoryByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("products", products);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "inventory";
    }


    @GetMapping("/admin/wallets")
    public String getAdminWallets(Model model) {

        model.addAttribute("users", userService.findAll().stream().filter(x->x.getWallet()!=null).toList());

        return "walletsAdmin";
    }

}
















