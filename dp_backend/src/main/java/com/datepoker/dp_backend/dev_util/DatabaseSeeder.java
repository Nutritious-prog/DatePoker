package com.datepoker.dp_backend.dev_util;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.RoleRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

@Component
public class DatabaseSeeder implements CommandLineRunner {
    private final RoleRepository roleRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DatabaseSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        LOGGER.info("🚀 Starting Database Seeder...");

        // ✅ Ensure roles exist
        if (roleRepository.count() == 0) {
            roleRepository.saveAll(List.of(
                    new Role(RoleName.ROLE_USER),
                    new Role(RoleName.ROLE_PREMIUM),
                    new Role(RoleName.ROLE_ADMIN)
            ));
            LOGGER.info("Roles added to the database!");
        } else {
            LOGGER.info("Roles already exist, skipping role seeding.");
        }

        // ✅ Add test users if they don’t exist
        if (userRepository.count() == 0) {
            Role userRole = roleRepository.findByName(RoleName.ROLE_USER).orElseThrow();
            Role premiumRole = roleRepository.findByName(RoleName.ROLE_PREMIUM).orElseThrow();
            Role adminRole = roleRepository.findByName(RoleName.ROLE_ADMIN).orElseThrow();

            User normalUser = new User("user@example.com", passwordEncoder.encode("password123"), "John Doe");
            normalUser.addRole(userRole);

            User premiumUser = new User("premium@example.com", passwordEncoder.encode("password123"), "Jane Premium");
            premiumUser.addRole(premiumRole);

            User adminUser = new User("admin@example.com", passwordEncoder.encode("adminpass"), "Super Admin");
            adminUser.addRole(adminRole);

            userRepository.saveAll(List.of(normalUser, premiumUser, adminUser));

            LOGGER.info("Test users added!");
        } else {
            LOGGER.info("Users already exist, skipping user seeding.");
        }
    }
}


