package com.ecommerce.project.controller.admin;

import com.ecommerce.project.dto.CategoryDTO;
import com.ecommerce.project.entity.Category;
import com.ecommerce.project.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
public class AdminCategoriesController {

    @Autowired
    CategoryService categoryService;

    @GetMapping("/admin/categories")
    public String getCategories(Model model) {

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
        if (!model.containsAttribute("category"))
            model.addAttribute("category", new CategoryDTO());
        return "categoriesAdd";
    }

    @PostMapping("/admin/categories/add")
    public String postAddCategories(
            @Valid @ModelAttribute("category") CategoryDTO categoryDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("category", categoryDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.category", bindingResult);
            return "redirect:/admin/categories/add";
        }

        Optional<Category> existing = categoryService.getCategoryByName(categoryDTO.getName());

        if (existing.isPresent() && categoryDTO.getId()!=existing.get().getId()) {
            redirectAttributes.addFlashAttribute("category", categoryDTO);
            redirectAttributes.addFlashAttribute("categoryError", "Category already exists.");
            return "redirect:/admin/categories/add";
        }

        categoryService.saveCategory(categoryDTO);
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
    public String updateCategory(@PathVariable int id, RedirectAttributes redirectAttributes) {
        Optional<Category> category = categoryService.getCategoryById(id);
        if (category.isPresent()) {
            redirectAttributes.addFlashAttribute("category", categoryService.getCategoryDto(category.get()));
            return "redirect:/admin/categories/add";
        }
        return "404";
    }

}
