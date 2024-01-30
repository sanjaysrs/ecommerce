package com.ecommerce.project.otp.service;

import com.ecommerce.project.entity.User;
import com.ecommerce.project.otp.dto.OtpDto;
import com.ecommerce.project.otp.entity.Otp;
import com.ecommerce.project.otp.repository.OtpRepository;
import com.ecommerce.project.otp.util.EmailUtil;
import com.ecommerce.project.otp.util.OtpUtil;
import com.ecommerce.project.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    OtpRepository otpRepository;

    @Autowired
    UserRepository userRepository;

    @Autowired
    OtpUtil otpUtil;

    @Autowired
    EmailUtil emailUtil;

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
        otp.setUser(userRepository.findUserByEmail(email).get());
        otpRepository.save(otp);

    }

    public OtpDto getOtpDto(String email) {
        OtpDto otpDto = new OtpDto();
        otpDto.setEmail(email);
        return otpDto;
    }

    public String resendOtp(String email) {
        User user = userRepository.findUserByEmail(email).get();

        //Get the user's existing OTP
        Otp otpToDelete = otpRepository.findByUser_Id(user.getId()).get();

        //Check if the OTP's time has not expired
        if (Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds() <= (2 * 60) ) {
            long timeLeft = (2 * 60) + 1 - Duration.between(otpToDelete.getOtpGeneratedTime(), LocalDateTime.now()).getSeconds();
            long minutesLeft = timeLeft/60;
            long secondsLeft = timeLeft%60;
            return "Resend OTP in " + minutesLeft + " minutes " + secondsLeft + " seconds";
        }

        //Delete the already existing otp entry
        otpRepository.delete(otpToDelete);

        //Send the new otp and save the new otp to database
        sendOtp(user.getEmail());

        return "New OTP has been sent";
    }
}
