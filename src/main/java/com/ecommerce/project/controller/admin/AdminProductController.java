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

import java.util.ArrayList;
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
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @GetMapping("/admin/products/filter/{id}")
    public String filterProductByCategory(@PathVariable("id") int id, Model model) {

        List<Product> products = productService.getAllProductsByCategoryId(id);
        model.addAttribute("urlList", storageService.getUrlList(products));
        model.addAttribute("products", products);
        model.addAttribute("categories", categoryService.getAllCategories());
        return "products";
    }

    @PostMapping("/admin/products")
    public String searchProducts(@ModelAttribute("name") String name, Model model) {

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
            return "products";
        }

        model.addAttribute("urlList", storageService.getUrlList(searchResult));
        model.addAttribute("products", searchResult);
        model.addAttribute("categories", categoryService.getAllCategories());

        return "products";

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
