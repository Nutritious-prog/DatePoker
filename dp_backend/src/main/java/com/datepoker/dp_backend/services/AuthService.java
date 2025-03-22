package com.datepoker.dp_backend.services;
import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.encryption.AESEncryptionUtil;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.exceptions.AuthenticationException;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.RoleRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.datepoker.dp_backend.security.JwtUtil;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Optional;
import java.util.Random;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;

    private final MailService mailService;
    private final UserProfileService userProfileService;

    private final ObjectMapper objectMapper;
    private final JwtUtil jwtUtil;
    private final RestTemplate restTemplate = new RestTemplate();

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil, MailService mailService, UserProfileService userProfileService, ObjectMapper objectMapper) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.mailService = mailService;
        this.userProfileService = userProfileService;
        this.objectMapper = objectMapper;
    }

    @Transactional
    public String registerUser(RegisterRequest request) {
        LOGGER.info("Checking if email {} is already registered", request.getEmail());

        if (userRepository.existsByEmail(request.getEmail())) {
            LOGGER.warn("Email {} is already in use!", request.getEmail());
            throw new RuntimeException("Error: Email is already in use!");
        }

        LOGGER.info("Registering new user: {}", request.getEmail());

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        Role userRole = roleRepository.findByName(RoleName.ROLE_USER)
                .orElseThrow(() -> new RuntimeException("Error: Default role USER not found"));

        User newUser = new User(request.getEmail(), encodedPassword, request.getName());
        newUser.addRole(userRole);
        String activationCode = String.format("%06d", new Random().nextInt(999999));
        newUser.setActivationCode(activationCode);
        newUser.setActivated(false);

        userRepository.save(newUser);
        mailService.sendActivationEmail(newUser.getEmail(), newUser.getName(), activationCode);
        userProfileService.createProfileIfNotExists(newUser);

        LOGGER.info("User {} registered successfully!", request.getEmail());
        return "User registered successfully!";
    }

    public String activateUser(String email, String code) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.isActivated()) {
            throw new IllegalStateException("Account already activated");
        }

        if (!code.equals(user.getActivationCode())) {
            throw new IllegalArgumentException("Invalid activation code");
        }

        user.setActivated(true);
        user.setActivationCode(null);
        userRepository.save(user);

        return "Account for " + email + " successfully activated!";
    }




    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() ||
                !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        if (!userOptional.get().isActivated()) {
            throw new AuthenticationException("Account not activated. Please check your email.");
        }


        // 🔑 Generate JWT Token
        String token = jwtUtil.generateToken(userOptional.get().getEmail());

        // 🔐 Encrypt the token before sending it back
        String encryptedToken = AESEncryptionUtil.encrypt(token);

        return new LoginResponse("Login successful", encryptedToken);
    }

    public LoginResponse socialLogin(SocialAuthRequest request) {
        String email = verifySocialToken(request.getProvider(), request.getToken());

        // Register the user if they don't exist
        User user = userRepository.findByEmail(email)
                .orElseGet(() -> {
                    User newUser = new User();
                    newUser.setEmail(email);
                    newUser.setPassword("SOCIAL_LOGIN"); // or random string
                    return userRepository.save(newUser);
                });

        // 🧾 Generate regular JWT
        String jwtToken = jwtUtil.generateToken(email);

        // 🔐 Encrypt JWT before returning
        String encryptedToken = AESEncryptionUtil.encrypt(jwtToken);

        return new LoginResponse("Login successful", encryptedToken);
    }


    private String verifySocialToken(String provider, String token) {
        String url;
        if ("google".equalsIgnoreCase(provider)) {
            url = "https://oauth2.googleapis.com/tokeninfo?id_token=" + token;
        } else if ("facebook".equalsIgnoreCase(provider)) {
            url = "https://graph.facebook.com/me?access_token=" + token + "&fields=email";
        } else {
            throw new IllegalArgumentException("Unsupported provider: " + provider);
        }

        // Call Google or Facebook to verify token
        Map response = restTemplate.getForObject(url, Map.class);
        if (response == null || response.get("email") == null) {
            throw new IllegalArgumentException("Invalid OAuth token");
        }

        return (String) response.get("email");
    }

    public String processEncryptedActivation(EncryptionRequest request) {
        try {
            String decryptedJson = AESEncryptionUtil.decrypt(request.getPayload().asText());
            ActivationRequest activationRequest = objectMapper.readValue(decryptedJson, ActivationRequest.class);
            return activateUser(activationRequest.getEmail(), activationRequest.getCode());
        } catch (Exception e) {
            throw new RuntimeException("Activation failed: " + e.getMessage(), e);
        }
    }

    public String processForgotPassword(ForgotPasswordRequest request) {
        String email = request.getEmail();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (!user.isActivated()) {
            throw new IllegalStateException("Account not activated");
        }

        String code = String.format("%06d", new SecureRandom().nextInt(999999));
        user.setResetCode(code);
        user.setResetCodeExpiry(LocalDateTime.now().plusMinutes(15));
        userRepository.save(user);

        mailService.sendPasswordResetEmail(email, code); // ✅ HTML email

        return email;
    }

    public String processResetPassword(ResetPasswordRequest request) {
        String email = request.getEmail();
        String code = request.getCode();
        String newPassword = request.getNewPassword();

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if (user.getResetCode() == null || !user.getResetCode().equals(code)) {
            throw new IllegalArgumentException("Invalid reset code");
        }

        if (user.getResetCodeExpiry() == null || user.getResetCodeExpiry().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Reset code expired");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        user.setResetCode(null);
        user.setResetCodeExpiry(null);
        userRepository.save(user);

        return email;
    }

    public <T> T decryptPayload(JsonNode payload, Class<T> targetClass) {
        try {
            String decryptedJson = AESEncryptionUtil.decrypt(payload.asText());
            return objectMapper.readValue(decryptedJson, targetClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to decrypt payload: " + e.getMessage(), e);
        }
    }

}

