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

        if (!isAnonymous())
            return "redirect:/";

        Exception exception = (Exception) session.getAttribute("SPRING_SECURITY_LAST_EXCEPTION");
        String error = exception instanceof BadCredentialsException ? "Invalid password" : exception.getMessage();
        redirectAttributes.addFlashAttribute("error", error);
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
            RedirectAttributes redirectAttributes,
            HttpSession session) {

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

        UUID token = generateToken();
        session.setAttribute("token", token);
        redirectAttributes.addFlashAttribute("token", token);

        return "redirect:/register/verify";
    }

    @GetMapping("/register/verify")
    public String renderRegistrationConfirmation(HttpSession session, Model model) {
        if (isValidToken(session.getAttribute("token"), model.getAttribute("token"))) {
            session.removeAttribute("token");
            return "registration-confirmation";
        }
        return "redirect:/register";
    }

    @PostMapping("/verify-account")
    public String postOtp(@ModelAttribute OtpDto otpDto, RedirectAttributes redirectAttributes, HttpSession session) {

        UUID token = generateToken();
        session.setAttribute("token", token);
        redirectAttributes.addFlashAttribute("token", token);

        int flag = userService.verifyAccount(otpDto);
        if (flag==1)
            return "redirect:/userVerified";

        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("otpDto", otpDto);

        if (flag==2)
            redirectAttributes.addFlashAttribute("message", "Time's up. Resend OTP");
        else
            redirectAttributes.addFlashAttribute("message", "Wrong OTP. Please try again.");

        return "redirect:/register/verify";
    }

    @GetMapping("/userVerified")
    public String userVerified(HttpSession session, Model model) {
        if (isValidToken(session.getAttribute("token"), model.getAttribute("token"))) {
            session.removeAttribute("token");
            return "user-verified";
        }
        return "redirect:/";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@ModelAttribute OtpDto otpDto, RedirectAttributes redirectAttributes, HttpSession session) {

        redirectAttributes.addFlashAttribute("message", otpService.resendOtp(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("otpDto", otpService.getOtpDto(otpDto.getEmail()));

        UUID token = generateToken();
        session.setAttribute("token", token);
        redirectAttributes.addFlashAttribute("token", token);

        return "redirect:/register/verify";
    }

    @GetMapping("/access-denied")
    public String access() {
        return "access-denied";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication==null || authentication instanceof AnonymousAuthenticationToken);
    }

    private UUID generateToken() {
        return UUID.randomUUID();
    }

    private boolean isValidToken(Object sessionToken, Object modelToken) {
        return  (sessionToken != null && sessionToken.equals(modelToken));
    }

}
