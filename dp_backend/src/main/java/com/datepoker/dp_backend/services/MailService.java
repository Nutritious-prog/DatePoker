package com.datepoker.dp_backend.services;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@Service
@RequiredArgsConstructor
public class MailService {

    private final JavaMailSender mailSender;

    public void sendActivationEmail(String toEmail, String name, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            // Load template from resources
            String htmlTemplate = loadEmailTemplate();
            String htmlContent = htmlTemplate
                    .replace("{{name}}", name)
                    .replace("{{code}}", code);

            helper.setTo(toEmail);
            helper.setSubject("💖 Activate Your DatePoker Account");
            helper.setText(htmlContent, true); // true enables HTML

            mailSender.send(message);
        } catch (Exception e) {
            throw new RuntimeException("Failed to send email", e);
        }
    }

    private String loadEmailTemplate() throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream("templates/email/activation.html");
        if (inputStream == null) {
            throw new FileNotFoundException("Template not found: " + "templates/email/activation.html");
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }
}


