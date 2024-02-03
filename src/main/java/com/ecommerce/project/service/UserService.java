package com.ecommerce.project.service;

import com.ecommerce.project.dto.RegisterDTO;
import com.ecommerce.project.entity.*;
import com.ecommerce.project.otp.dto.OtpDto;
import com.ecommerce.project.otp.entity.Otp;
import com.ecommerce.project.otp.repository.OtpRepository;
import com.ecommerce.project.otp.util.EmailUtil;
import com.ecommerce.project.otp.util.OtpUtil;
import com.ecommerce.project.repository.RoleRepository;
import com.ecommerce.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpUtil otpUtil;

    @Autowired
    EmailUtil emailUtil;

    @Autowired
    OtpRepository otpRepository;

    @Autowired
    RoleRepository roleRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    public List<User> findAll() {
        return userRepository.findAll();
    }

    public List<User> findAllVerifiedUsers() {
        return userRepository.findVerifiedUsersWithRoleUser();
    }

    public void deleteById(Integer id) {
        userRepository.deleteById(id);
    }

    public User findById(Integer id) {
        User user = userRepository.findById(id).get();
        return user;
    }

    public void save(User user) {
        userRepository.save(user);
    }

    public User findUserByEmail(String email) {
        return userRepository.findUserByEmail(email).get();
    }

    public void sendOtp(String email) {

        String otpString = otpUtil.generateOtp();

        try {
            emailUtil.sendOtpEmail(email, otpString);
        } catch (MessagingException e) {
            throw new RuntimeException("Unable to send OTP. Please try again.");
        }

        Otp otp = new Otp();
        otp.setOtp(otpString);
        otp.setOtpGeneratedTime(LocalDateTime.now());
        otp.setUser(findUserByEmail(email));
        otpRepository.save(otp);

    }

    public int verifyAccount(OtpDto otpDto) {

        String email = otpDto.getEmail();
        String otpString = otpDto.getOtp();

        User user = userRepository.findUserByEmail(email).get();
        Otp otp = otpRepository.findByUser_Id(user.getId()).get();

        if (otp.getOtp().equals(otpString) && Duration.between(otp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() <= (2 * 60) ) {
            user.setVerified(true);
            userRepository.save(user);
            otpRepository.delete(otp);
            return 1;
        }

        if (otp.getOtp().equals(otpString) && Duration.between(otp.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() > (2 * 60))
            return 2;

        return 0;
    }

    public void registerUser(RegisterDTO registerDTO) {
        User user = new User();
        user.setFirstName(registerDTO.getFirstName());
        user.setLastName(registerDTO.getLastName());
        user.setEmail(registerDTO.getEmail());
        user.setPassword(passwordEncoder.encode(registerDTO.getPassword()));

        List<Role> roles = new ArrayList<>();
        roles.add(roleRepository.findById(2).get());
        user.setRoles(roles);

        user.setEnabled(true);

        Cart cart = new Cart();
        cart.setUser(user);
        user.setCart(cart);

        Wishlist wishlist = new Wishlist();
        wishlist.setUser(user);
        user.setWishlist(wishlist);

        Wallet wallet = new Wallet();
        wallet.setUser(user);
        user.setWallet(wallet);

        userRepository.save(user);
    }

    public boolean unverifiedRegisteredUser(String email) {
        Optional<User> existing = userRepository.findUserByEmail(email);
        return existing.isPresent() && !existing.get().isVerified();
    }

    @Transactional
    public void deleteOldUserAndOtp(String email) {
        Optional<User> existing = userRepository.findUserByEmail(email);
        otpRepository.deleteByUser_Id(existing.get().getId());
        userRepository.deleteById(existing.get().getId());
    }

    public boolean verifiedExistingUser(String email) {
        Optional<User> existing = userRepository.findUserByEmail(email);
        return existing.isPresent() && existing.get().isVerified();
    }
}
