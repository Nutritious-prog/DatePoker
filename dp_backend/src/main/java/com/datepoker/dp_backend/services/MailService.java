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
            String htmlTemplate = loadEmailTemplate("templates/email/activation.html");
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

    private String loadEmailTemplate(String path) throws IOException {
        InputStream inputStream = getClass().getClassLoader().getResourceAsStream(path);
        if (inputStream == null) {
            throw new FileNotFoundException("Template not found: " + path);
        }
        return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
    }

    public void sendPasswordResetEmail(String toEmail, String code) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, "utf-8");

            String template = loadEmailTemplate("templates/email/password-reset.html");

            // You can inject user name if you have it — for now just use email prefix
            String name = toEmail.split("@")[0];
            String html = template.replace("{{name}}", name)
                    .replace("{{code}}", code);

            helper.setTo(toEmail);
            helper.setSubject("🔐 Reset your DatePoker password");
            helper.setText(html, true);

            mailSender.send(message);

        } catch (Exception e) {
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

}


