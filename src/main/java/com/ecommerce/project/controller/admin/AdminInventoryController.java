package com.ecommerce.project.controller.admin;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.service.CategoryService;
import com.ecommerce.project.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminInventoryController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    StorageService storageService;

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

    @GetMapping("/admin/inventory/search")
    public String searchInventory(@ModelAttribute("name") String keyword, RedirectAttributes redirectAttributes) {

        List<Product> searchResult = productService.getProductsByNameContaining(keyword);

        if (searchResult.isEmpty()) {
            redirectAttributes.addFlashAttribute("searchError", "Your search did not match any products.");
            return "redirect:/admin/inventory";
        }

        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(searchResult));
        redirectAttributes.addFlashAttribute("products", searchResult);

        return "redirect:/admin/inventory";

    }

    @GetMapping("/admin/inventory/filter/{id}")
    public String filterInventoryByCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(products));
        redirectAttributes.addFlashAttribute("products", products);
        return "redirect:/admin/inventory";
    }

}
