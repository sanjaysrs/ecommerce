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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

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

        if (!model.containsAttribute("products"))
            model.addAttribute("products", productService.getAllProducts());
        if (!model.containsAttribute("urlList"))
            model.addAttribute("urlList", storageService.getUrlList(productService.getAllProducts()));
        model.addAttribute("categories", categoryService.getAllCategories());
        return "inventory";
    }

    @GetMapping("/admin/inventory/update/{id}")
    public String updateStock(@PathVariable Long id, Model model) {
        model.addAttribute("productId", id);
        return "updateStock";
    }

    @PostMapping("/admin/inventory/addStock/{id}")
    public String addStock(@PathVariable Long id, @ModelAttribute("addStock") long add, RedirectAttributes redirectAttributes) {

        if (!productService.existsById(id))
            return "redirect:/admin/inventory/update/" + id;

        productService.addStock(id, add);

        Product product = productService.getProductById(id).get();
        List<Product> products = List.of(product);

        redirectAttributes.addFlashAttribute("products", products);
        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(products));
        redirectAttributes.addFlashAttribute("stockStatus", "Stock added successfully");
        return "redirect:/admin/inventory";
    }

    @PostMapping("/admin/inventory/removeStock/{id}")
    public String removeStock(@PathVariable Long id, @ModelAttribute("removeStock") long subtract, RedirectAttributes redirectAttributes) {

        if (!productService.existsById(id))
            return "redirect:/admin/inventory/update/" + id;

        int removed = productService.removeStock(id, subtract);

        Product product = productService.getProductById(id).get();
        List<Product> products = List.of(product);

        redirectAttributes.addFlashAttribute("products", products);
        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(products));

        if (removed==0)
            redirectAttributes.addFlashAttribute("stockStatus", "Could not remove required stock due to insufficient stock in inventory");
        else
            redirectAttributes.addFlashAttribute("stockStatus", "Stock removed successfully");

        return "redirect:/admin/inventory";
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
















