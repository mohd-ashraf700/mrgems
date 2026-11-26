package com.mrgems.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    private final JavaMailSender mailSender;
    private final String fromEmail;
    public EmailService(JavaMailSender mailSender,
                        @Value("${spring.mail.username}") String fromEmail) {
        this.mailSender = mailSender;
        this.fromEmail = fromEmail;
    }

    public void sendPasswordResetOtp(String toEmail , String otp) {
        SimpleMailMessage message = new SimpleMailMessage();

        message.setFrom(fromEmail);
        message.setTo(toEmail);
        message.setSubject("MR Gems - Password Reset OTP");

        message.setText(
                "Hello Admin, \n\n" +
                        "Your Password reset OTP is: " + otp + "\n\n" +
                        "this OTP is valid for 5 minutes.\n" +
                        "please do not share this OTP with anyone.\n\n" +
                        "Regards,\nMR Gems Team"
        );
        mailSender.send(message);
    }
}
