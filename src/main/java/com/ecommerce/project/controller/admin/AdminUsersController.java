package com.ecommerce.project.controller.admin;

import com.ecommerce.project.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@Controller
public class AdminUsersController {

    @Autowired
    UserService userService;

    @GetMapping("/admin/users")
    public String getUsers(Model model) {
        model.addAttribute("users", userService.findAllVerifiedUsers());
        return "users";
    }

    @GetMapping("/admin/users/delete/{id}")
    public String deleteUser(@PathVariable("id") Integer id) {
        userService.deleteById(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/enable/{id}")
    public String enableUser(@PathVariable("id") Integer id) {
        userService.enableUser(id);
        return "redirect:/admin/users";
    }

    @GetMapping("/admin/users/disable/{id}")
    public String disableUser(@PathVariable("id") Integer id) {
        userService.disableUser(id);
        return "redirect:/admin/users";
    }

}
