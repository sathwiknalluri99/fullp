package com.erp.backend.service;

import com.erp.backend.entity.Otp;
import com.erp.backend.repository.OtpRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.Random;

@Service
public class OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;

    private static final int OTP_EXPIRY_MINUTES = 5;

    @Transactional
    public String generateAndSendOtp(String email) {
        // Delete any existing OTP for this email
        otpRepository.deleteByEmail(email);

        // Generate 6-digit OTP
        String otpCode = String.format("%06d", new Random().nextInt(1000000));

        // Save OTP
        Otp otp = new Otp(email, otpCode, OTP_EXPIRY_MINUTES);
        otpRepository.save(otp);

        // Send Email
        emailService.sendOtpEmail(email, otpCode);

        return otpCode;
    }

    public boolean validateOtp(String email, String otpCode) {
        Optional<Otp> otpOptional = otpRepository.findByEmail(email);

        if (otpOptional.isPresent()) {
            Otp otp = otpOptional.get();
            if (otp.getOtpCode().equals(otpCode) && !otp.isExpired()) {
                otpRepository.delete(otp);
                return true;
            }
        }
        return false;
    }
}
