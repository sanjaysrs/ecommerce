package com.ecommerce.project.controller.admin;

import com.ecommerce.project.aws.service.StorageService;
import com.ecommerce.project.dto.ProductDTO;
import com.ecommerce.project.entity.Product;
import com.ecommerce.project.entity.ProductImage;
import com.ecommerce.project.service.CategoryService;
import com.ecommerce.project.service.ProductImageService;
import com.ecommerce.project.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminProductAddController {

    @Autowired
    CategoryService categoryService;

    @Autowired
    ProductService productService;

    @Autowired
    StorageService storageService;

    @GetMapping("/admin/products/add")
    public String addProduct(Model model) {

        if (!model.containsAttribute("productDTO"))
            model.addAttribute("productDTO", new ProductDTO());
        model.addAttribute("categories", categoryService.getAllCategories());
        return "productsAdd";
    }

    @PostMapping("/admin/products/add")
    public String productAddPost(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("productImage") List<MultipartFile> files,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productDTO", bindingResult);
            return "redirect:/admin/products/add";
        }

        if (productService.isFilesEmpty(files)) {
            redirectAttributes.addFlashAttribute("imageRequired", "Select at least 1 image");
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            return "redirect:/admin/products/add";
        }

        if (productService.existsByName(productDTO.getName())) {
            redirectAttributes.addFlashAttribute("duplicateName", "Duplicate product name");
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            return "redirect:/admin/products/add";
        }

        productService.saveProduct(productDTO, files);
        redirectAttributes.addFlashAttribute("addOrUpdate", "Product was added successfully");
        return "redirect:/admin/products";
    }

    @GetMapping("/admin/product/update/{id}")
    public String updateProduct(@PathVariable long id, Model model) {

        Optional<Product> productOptional = productService.getProductById(id);

        if (productOptional.isEmpty())
            return "redirect:/admin/products";

        Product product = productOptional.get();

        if (!model.containsAttribute("productDTO"))
            model.addAttribute("productDTO", productService.getProductDTO(product));
        model.addAttribute("categories", categoryService.getAllCategories());
        model.addAttribute("urlList", storageService.getUrlListForProduct(product));
        return "productsUpdate";

    }

    @PostMapping("/admin/products/update")
    public String productUpdatePost(@Valid @ModelAttribute("productDTO") ProductDTO productDTO,
                                 BindingResult bindingResult,
                                 @RequestParam("productImage") List<MultipartFile> files,
                                 RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.productDTO", bindingResult);
            return "redirect:/admin/product/update/" + productDTO.getId();
        }

        Optional<Product> productOptional = productService.getProductById(productDTO.getId());

        if (productOptional.isEmpty())
            return "redirect:/admin/products";

        Product product = productOptional.get();

        if (productService.isNameChanged(product, productDTO) && productService.existsByName(productDTO.getName())) {
            redirectAttributes.addFlashAttribute("duplicateName", "Duplicate product name");
            redirectAttributes.addFlashAttribute("productDTO", productDTO);
            return "redirect:/admin/product/update/" + productDTO.getId();
        }

        productService.updateProduct(product, productDTO, files);
        redirectAttributes.addFlashAttribute("addOrUpdate", "Product was updated successfully");
        return "redirect:/admin/products";
    }

}
