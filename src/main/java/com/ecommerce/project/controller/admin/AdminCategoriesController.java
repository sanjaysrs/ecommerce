package com.ecommerce.project.controller.admin;

import com.ecommerce.project.entity.Category;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Controller
public class AdminCategoriesController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("/admin/categories")
    public String getCategories(@ModelAttribute("exception") String exception,
                                @ModelAttribute("deleted") String deleted,
                                Model model) {

        if (!exception.isEmpty())
            model.addAttribute("exceptionCheck", "Exception Check");
        if (!deleted.isEmpty())
            model.addAttribute("deletedCheck", "Deleted Check");

        if (!model.containsAttribute("categories"))
            model.addAttribute("categories", categoryService.getAllCategories());
        return "categories";
    }

    @GetMapping("/admin/categories/search")
    public String searchCategories(@RequestParam("name") String keyword, RedirectAttributes redirectAttributes) {

        List<Category> searchResult = categoryService.searchCategory(keyword);

        if (searchResult.isEmpty()) {
            redirectAttributes.addFlashAttribute("searchError", "Category could not be found.");
            return "redirect:/admin/categories";
        }

        redirectAttributes.addFlashAttribute("categories", searchResult);
        return "redirect:/admin/categories";

    }

    @GetMapping("/admin/categories/add")
    public String addCategories(Model model) {
        model.addAttribute("category", new Category());
        return "categoriesAdd";
    }

    @PostMapping("/admin/categories/add")
    public String postAddCategories(
            @Valid @ModelAttribute("category") Category category,
            BindingResult bindingResult,
            Model model
    ) {

        if (bindingResult.hasErrors()){
            return "categoriesAdd";
        }

        Optional<Category> existing = categoryService.getCategoryByName(category.getName());

        if (existing.isPresent()) {
            model.addAttribute("category", new Category());
            model.addAttribute("categoryError", "Category already exists.");
            return "categoriesAdd";
        }

        categoryService.addCategory(category);
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/delete/{id}")
    public String deleteCategory(@PathVariable("id") int id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryById(id);
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("exception", "Cannot delete this category as products under this category have been ordered.");
            return "redirect:/admin/categories";
        }
        redirectAttributes.addFlashAttribute("deleted", "Category and the products under it have been deleted.");
        return "redirect:/admin/categories";
    }

    @GetMapping("/admin/categories/update/{id}")
    public String updateCategory(@PathVariable int id, Model model) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            model.addAttribute("category", category.get());
            return "categoriesAdd";
        }
        return "404";
    }


}
