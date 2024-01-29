package com.ecommerce.project.controller;

import com.ecommerce.project.dto.AddressDTO;
import com.ecommerce.project.entity.Address;
import com.ecommerce.project.entity.User;
import com.ecommerce.project.repository.AddressRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.AddressService;
import com.ecommerce.project.service.CartService;
import com.ecommerce.project.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Optional;

@Controller
public class ProfileController {

    @Autowired
    private UserService userService;

    @Autowired
    private AddressRepository addressRepository;

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
    public String showAddAddressForm(Model model) {

        if (!model.containsAttribute("address"))
            model.addAttribute("address", new AddressDTO());
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
        return "redirect:/addresses";
    }

    @GetMapping("/addresses/delete/{id}")
    public String deleteAddress(@PathVariable Long id, RedirectAttributes redirectAttributes) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        if (addressOptional.isPresent()) {
            try {
                addressRepository.deleteById(id);
            } catch (Exception e) {
                redirectAttributes.addFlashAttribute("cannotDelete", "This address is associated with an order and cannot be deleted");
                return "redirect:/addresses";
            }
        }

        return "redirect:/addresses";
    }

    @GetMapping("/addresses/edit/{id}")
    public String editAddress(@PathVariable Long id, Model model) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        Address address = addressOptional.get();

        model.addAttribute("address", address);

        //  Add cartCount
        int cartCount = userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).getCart().getCartItems().stream().map(x->x.getQuantity()).reduce(0,(a,b)->a+b);
        model.addAttribute("cartCount", cartCount);

        return "edit-address";
    }

    @PostMapping("/edit-address/{id}")
    public String postEditAddress(@PathVariable Long id,
                                  @ModelAttribute Address addressFromForm) {

        //Already logged-in user block
        if (!userService.findUserByEmail(SecurityContextHolder.getContext().getAuthentication().getName()).isEnabled()) {
            return "redirect:/logout";
        }

        Optional<Address> addressOptional = addressRepository.findById(id);
        Address address = addressOptional.get();

        addressFromForm.setId(address.getId());
        addressFromForm.setUser(address.getUser());

        addressRepository.save(addressFromForm);

        return "redirect:/addresses";
    }

}
