package com.erp.backend.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    public void sendOtpEmail(String toEmail, String otp) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("Your ERP Verification OH-TP Code");
        message.setText("Welcome to ERP Management System!\n\n" +
                "Your verification code is: " + otp + "\n" +
                "This code will expire in 5 minutes.\n\n" +
                "If you did not request this, please ignore this email.");
        
        mailSender.send(message);
    }
}
