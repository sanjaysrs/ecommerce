package com.ecommerce.project.controller;

import com.ecommerce.project.dto.RegisterDTO;
import com.ecommerce.project.entity.*;
//import com.ecommerce.project.global.GlobalData;
import com.ecommerce.project.otp.dto.OtpDto;
import com.ecommerce.project.otp.entity.Otp;
import com.ecommerce.project.otp.repository.OtpRepository;
import com.ecommerce.project.otp.service.OtpService;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import com.ecommerce.project.service.UserService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.propertyeditors.StringTrimmerEditor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.InitBinder;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Controller
public class LoginController {

    @Autowired
    private UserService userService;

    @Autowired
    private OtpService otpService;

    @GetMapping("/login")
    public String login() {

        if(isAnonymous())
            return "login";

        return "redirect:/";

    }

    @GetMapping("/login/failure")
    public String loginFailure(RedirectAttributes redirectAttributes, HttpSession session) {

        Exception exception = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");

        if (!isAnonymous() || exception == null)
            return "redirect:/";

        String error = exception instanceof BadCredentialsException ? "Invalid password" : exception.getMessage();
        session.removeAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        redirectAttributes.addFlashAttribute("error", error);
        return "redirect:/login";

    }

    @GetMapping("/login/logout")
    public String logout(RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("logout", "You have been successfully logged out.");
        return "redirect:/login";
    }

    @GetMapping("/register")
    public String registerGet(Model model) {

        if(isAnonymous()) {
            if (!model.containsAttribute("user"))
                model.addAttribute("user", new RegisterDTO());
            return "register";
        }

        return "redirect:/";

    }

    @PostMapping("/register")
    public String registerPost(
            @Valid @ModelAttribute("user") RegisterDTO registerDTO,
            BindingResult bindingResult,
            RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()){
            redirectAttributes.addFlashAttribute("org.springframework.validation.BindingResult.user", bindingResult);
            redirectAttributes.addFlashAttribute("user", registerDTO);
            return "redirect:/register";
        }

        if (userService.unverifiedRegisteredUser(registerDTO.getEmail()))
            userService.deleteOldUserAndOtp(registerDTO.getEmail());

        if (userService.verifiedExistingUser(registerDTO.getEmail())){
            redirectAttributes.addFlashAttribute("user", registerDTO);
            redirectAttributes.addFlashAttribute("registrationError", "Email" + registerDTO.getEmail() + "is already registered.");
            return "redirect:/register";
        }

        userService.registerUser(registerDTO);
        otpService.sendOtp(registerDTO.getEmail());
        redirectAttributes.addFlashAttribute("otpDto", otpService.getOtpDto(registerDTO.getEmail()));
        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(registerDTO.getEmail()));

        return "redirect:/register/verify";
    }

    @GetMapping("/register/verify")
    public String renderRegistrationConfirmation(Model model) {
        if (model.containsAttribute("otpDto")) {
            return "registration-confirmation";
        }
        return "redirect:/register";
    }

    @PostMapping("/verify-account")
    public String postOtp(@ModelAttribute OtpDto otpDto, RedirectAttributes redirectAttributes) {

        int flag = userService.verifyAccount(otpDto);
        if (flag==1) {
            redirectAttributes.addFlashAttribute("redirect", true);
            return "redirect:/userVerified";
        }

        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("otpDto", otpDto);

        if (flag==2)
            redirectAttributes.addFlashAttribute("message", "Time's up. Resend OTP");
        else
            redirectAttributes.addFlashAttribute("message", "Wrong OTP. Please try again.");

        return "redirect:/register/verify";
    }

    @GetMapping("/userVerified")
    public String userVerified(Model model) {
        if (model.containsAttribute("redirect")) {
            return "user-verified";
        }
        return "redirect:/";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@ModelAttribute OtpDto otpDto, RedirectAttributes redirectAttributes) {

        redirectAttributes.addFlashAttribute("message", otpService.resendOtp(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("otpDto", otpService.getOtpDto(otpDto.getEmail()));
        return "redirect:/register/verify";
    }

    @GetMapping("/access-denied")
    public String access() {
        return "access-denied";
    }

    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication==null || authentication instanceof AnonymousAuthenticationToken);
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

}
