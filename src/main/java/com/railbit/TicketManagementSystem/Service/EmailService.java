package com.railbit.TicketManagementSystem.Service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    public void sendResetEmail(String toEmail, String token) {
        String link = "http://localhost:8080/reset-password?token=" + token;
        String subject = "Reset Your Password";
        String body = "Click this link to reset your password:\n" + link;

        // Use JavaMailSender or log to console
        System.out.println("To: " + toEmail);
        System.out.println("Subject: " + subject);
        System.out.println("Body:\n" + body);
    }

}
