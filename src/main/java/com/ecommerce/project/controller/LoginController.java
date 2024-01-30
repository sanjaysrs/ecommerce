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
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private OtpService otpService;

    private boolean isAnonymous() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return (authentication==null || authentication instanceof AnonymousAuthenticationToken);
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

    @GetMapping("/access-denied")
    public String access() {
        return "access-denied";
    }

    @GetMapping("/login")
    public String login() {

        if(isAnonymous())
            return "login";

        return "redirect:/";

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

        int flag = userService.verifyAccount(otpDto);
        if (flag==1)
            return "user-verified";

        redirectAttributes.addFlashAttribute("user", userService.findUserByEmail(otpDto.getEmail()));
        redirectAttributes.addFlashAttribute("otpDto", otpDto);

        if (flag==2)
            redirectAttributes.addFlashAttribute("error", "Time's up. Resend OTP");
        else
            redirectAttributes.addFlashAttribute("error", "Wrong OTP. Please try again.");

        UUID token = generateToken();
        session.setAttribute("token", token);
        redirectAttributes.addFlashAttribute("token", token);

        return "redirect:/register/verify";
    }

    @PostMapping("/resend-otp")
    public String resendOtp(@ModelAttribute OtpDto otpDto, Model model) {

        User user = userService.findUserByEmail(otpDto.getEmail());

        //Get the user's existing OTP
        Otp otpToDelete = otpRepository.findByUser_Id(user.getId()).get();

        //Check if the OTP's time has not expired
        if (Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() <= (2 * 60) ) {
            long timeLeft = (2 * 60) + 1 - Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds();
            long minutesLeft = timeLeft/60;
            long secondsLeft = timeLeft%60;
            model.addAttribute("error", "Resend OTP in " + minutesLeft + " minutes " + secondsLeft + " seconds");
            model.addAttribute("user", user);
            return "registration-confirmation";
        }

        //Delete the already existing otp entry
        otpRepository.delete(otpToDelete);

        //Send the new otp and save the new otp to database
        otpService.sendOtp(user.getEmail());

        model.addAttribute("error", "New OTP has been sent");
        model.addAttribute("user", user);
        return "registration-confirmation";
    }

    @InitBinder
    public void initBinder(WebDataBinder dataBinder) {

        StringTrimmerEditor stringTrimmerEditor = new StringTrimmerEditor(true);

        dataBinder.registerCustomEditor(String.class, stringTrimmerEditor);
    }

    private UUID generateToken() {
        return UUID.randomUUID();
    }

    private boolean isValidToken(Object sessionToken, Object modelToken) {
        return  (sessionToken != null && sessionToken.equals(modelToken));
    }

}
