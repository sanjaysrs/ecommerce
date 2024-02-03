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
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class AdminProductController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    StorageService storageService;

    @GetMapping("/admin/products")
    public String getProducts(Model model) {

        List<Product> products = productService.getAllProducts();
        if (!model.containsAttribute("urlList"))
            model.addAttribute("urlList", storageService.getUrlList(products));
        if (!model.containsAttribute("products"))
            model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @GetMapping("/admin/products/filter/{id}")
    public String filterProductByCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(products));
        redirectAttributes.addFlashAttribute("products", products);
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/products/search")
    public String searchProducts(@ModelAttribute("name") String keyword, RedirectAttributes redirectAttributes) {

        List<Product> searchResult = productService.getProductsByNameContaining(keyword);

        if (searchResult.isEmpty()) {
            redirectAttributes.addFlashAttribute("searchError", "Your search did not match any products.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("urlList", storageService.getUrlList(searchResult));
        redirectAttributes.addFlashAttribute("products", searchResult);

        return "redirect:/admin/products";

    }

    @GetMapping("/admin/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") long id, RedirectAttributes redirectAttributes) {
        try {
            productService.removeProductById(id);
        } catch (Exception exception) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete a product that has already been ordered.");
            return "redirect:/admin/products";
        }

        redirectAttributes.addFlashAttribute("deleted", "The product was deleted.");
        return "redirect:/admin/products";
    }

}
