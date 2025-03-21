package com.datepoker.dp_backend.services;
import com.datepoker.dp_backend.DTO.LoginRequest;
import com.datepoker.dp_backend.DTO.LoginResponse;
import com.datepoker.dp_backend.DTO.RegisterRequest;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.exceptions.AuthenticationException;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.RoleRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import com.datepoker.dp_backend.security.JwtUtil;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class AuthService {
    private final UserRepository userRepository;
    private final RoleRepository roleRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository, RoleRepository roleRepository, PasswordEncoder passwordEncoder, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.roleRepository = roleRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
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
        userRepository.save(newUser);

        LOGGER.info("User {} registered successfully!", request.getEmail());
        return "User registered successfully!";
    }

    public LoginResponse login(LoginRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isEmpty() || !passwordEncoder.matches(request.getPassword(), userOptional.get().getPassword())) {
            throw new AuthenticationException("Invalid email or password");
        }

        // Generate JWT Token
        String token = jwtUtil.generateToken(userOptional.get().getEmail());

        return new LoginResponse("Login successful", token);
    }
}

