package com.ecommerce.project.controller;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressService addressService;

    @Autowired
    CartService cartService;

    private User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userService.findUserByEmail(authentication.getName());
    }

    @GetMapping("/profile")
    public String getProfile(Model model) {

        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        return "profile";
    }

    @GetMapping("/profileDetails")
    public String getProfileDetails(Model model, Principal principal) {

        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        model.addAttribute("user", getCurrentUser());
        return "profileDetails";
    }

    @GetMapping("/addresses")
    public String listAddresses(Model model) {

        User user = getCurrentUser();
        model.addAttribute("userAddresses", user.getAddresses());
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        return "addresses";
    }

    @GetMapping("/add-address")
    public String showAddAddressForm(Model model, HttpServletRequest request) {

        if (!model.containsAttribute("address")) {
            AddressDTO addressDTO = new AddressDTO();
            addressDTO.setReferer(request.getHeader("Referer"));
            model.addAttribute("address", addressDTO);
        }

        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        return "add-address";
    }

    @PostMapping("/add-address")
    public String addAddress(@Valid @ModelAttribute("address") AddressDTO addressDTO,
                             BindingResult bindingResult,
                             RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("address", addressDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.address", bindingResult);
            return "redirect:/add-address";
        }

        addressService.saveAddress(addressDTO, getCurrentUser());
        redirectAttributes.addFlashAttribute("info", "Address added");
        String referer = addressDTO.getReferer();
        return "redirect:" + (referer != null ? referer : "/addresses");
    }

    @GetMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        if (addressService.deleteAddressById(id))
            redirectAttributes.addFlashAttribute("info", "Address deleted");
        return "redirect:/addresses";
    }

    @GetMapping("/addresses/edit/{id}")
    public String editAddress(@PathVariable Long id, Model model) {

        if (!model.containsAttribute("address"))
            model.addAttribute("address", addressService.getAddressDTOById(id, getCurrentUser()));
        model.addAttribute("cartCount", cartService.getCartCount(getCurrentUser()));
        return "edit-address";
    }

    @PostMapping("/edit-address")
    public String postEditAddress(@ModelAttribute("address") @Valid AddressDTO addressDTO,
                                  BindingResult bindingResult,
                                  RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("address", addressDTO);
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.address", bindingResult);
            return "redirect:/addresses/edit/" + addressDTO.getId();
        }

        addressService.saveAddress(addressDTO, getCurrentUser());
        redirectAttributes.addFlashAttribute("info", "Address edited");
        return "redirect:/addresses";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

}
