package com.datepoker.dp_backend.dev_util;
import com.datepoker.dp_backend.entities.DateCard;
import com.datepoker.dp_backend.entities.FeatureVector;
import com.datepoker.dp_backend.entities.Role;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.enums.RoleName;
import com.datepoker.dp_backend.logger.LOGGER;
import com.datepoker.dp_backend.repositories.DateCardRepository;
import com.datepoker.dp_backend.repositories.FeatureVectorRepository;
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
    private final DateCardRepository dateCardRepository;
    private final FeatureVectorRepository featureVectorRepository;

    public DatabaseSeeder(RoleRepository roleRepository, UserRepository userRepository, PasswordEncoder passwordEncoder, DateCardRepository dateCardRepository, FeatureVectorRepository featureVectorRepository) {
        this.roleRepository = roleRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.dateCardRepository = dateCardRepository;
        this.featureVectorRepository = featureVectorRepository;
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

        seedDateCards();
    }

    private void seedDateCards() {
        if (dateCardRepository.count() > 0) {
            LOGGER.info("Date cards already exist, skipping seeding.");
            return;
        }

        LOGGER.info("Seeding date cards...");

        List<Object[]> seedData = List.of(
                new Object[]{"Candlelight Dinner", "", "Poznan, Poland", "restaurant", "$$$", DateCard.Category.FOOD_DRINKS, List.of(1.00, 0.97, 0.52, 0.04, 0.22, 0.07, 0.81, 0.95)},
                new Object[]{"Board Games Café", "", "Poznan, Poland", "cafe", "$", DateCard.Category.CHILL, List.of(0.05, 0.99, 0.53, 0.62, 0.31, 0.94, 0.45, 0.02)},
                new Object[]{"Kayaking on the Lake", "", "Poznan, Poland", "tourist_attraction", "$$", DateCard.Category.ACTIVITY, List.of(0.51, 0.04, 0.91, 0.74, 0.93, 0.82, 0.97, 0.01)},
                new Object[]{"Wine Tasting Night", "", "Poznan, Poland", "bar", "$$$", DateCard.Category.FOOD_DRINKS, List.of(0.98, 1.00, 0.32, 0.27, 0.35, 0.12, 0.24, 0.94)},
                new Object[]{"Picnic in the Park", "", "Poznan, Poland", "park", "$", DateCard.Category.CHILL, List.of(0.01, 0.02, 0.79, 0.19, 0.29, 0.97, 1.00, 0.06)},
                new Object[]{"Escape Room Challenge", "", "Poznan, Poland", "amusement_center", "$$", DateCard.Category.ACTIVITY, List.of(0.47, 1.00, 0.42, 0.83, 0.91, 0.63, 0.28, 0.03)},
                new Object[]{"Sushi Dinner", "", "Poznan, Poland", "restaurant", "$$", DateCard.Category.FOOD_DRINKS, List.of(1.00, 0.98, 0.48, 0.23, 0.18, 0.31, 0.88, 1.00)},
                new Object[]{"Ice Skating Date", "", "Poznan, Poland", "ice_skating_rink", "$$", DateCard.Category.ACTIVITY, List.of(0.54, 0.51, 0.19, 0.58, 0.84, 0.72, 0.69, 0.04)},
                new Object[]{"Art Exhibition & Coffee", "", "Poznan, Poland", "museum", "$", DateCard.Category.CHILL, List.of(0.02, 1.00, 0.41, 0.24, 0.27, 0.92, 0.58, 0.05)},
                new Object[]{"Stargazing Night", "", "Poznan, Poland", "campground", "$", DateCard.Category.CHILL, List.of(0.00, 0.03, 0.63, 0.11, 0.47, 0.99, 0.96, 1.00)},
                new Object[]{"Sunset Hike", "", "Poznan, Poland", "tourist_attraction", "$", DateCard.Category.ACTIVITY, List.of(0.52, 0.01, 0.85, 0.38, 0.83, 0.94, 0.76, 0.15)},
                new Object[]{"Cooking Class", "", "Poznan, Poland", "school", "$$", DateCard.Category.FOOD_DRINKS, List.of(1.00, 1.00, 0.58, 0.45, 0.41, 0.70, 0.95, 0.0)},
                new Object[]{"Trampoline Park", "", "Poznan, Poland", "gym", "$$", DateCard.Category.ACTIVITY, List.of(0.47, 0.95, 0.71, 0.88, 0.96, 0.65, 0.48, 0.0)},
                new Object[]{"Rooftop Bar Night", "", "Poznan, Poland", "bar", "$$$", DateCard.Category.FOOD_DRINKS, List.of(1.00, 0.99, 0.75, 0.59, 0.45, 0.25, 0.30, 1.00)},
                new Object[]{"Pottery Workshop", "", "Poznan, Poland", "school", "$$", DateCard.Category.CHILL, List.of(0.33, 1.00, 0.45, 0.23, 0.28, 0.85, 0.62, 0.0)},
                new Object[]{"Jazz Concert", "", "Poznan, Poland", "night_club", "$$$", DateCard.Category.CHILL, List.of(0.60, 1.00, 0.51, 0.30, 0.32, 0.20, 0.55, 1.00)},
                new Object[]{"Bike Ride and Ice Cream", "", "Poznan, Poland", "tourist_attraction", "$", DateCard.Category.ACTIVITY, List.of(0.45, 0.0, 0.92, 0.65, 0.88, 0.95, 0.70, 0.0)},
                new Object[]{"Aquapark Adventure", "", "Poznan, Poland", "amusement_park", "$$", DateCard.Category.ACTIVITY, List.of(0.50, 0.50, 0.95, 0.78, 0.95, 0.75, 0.60, 0.0)},
                new Object[]{"Fondue Night at Home", "", "Poznan, Poland", "home", "$", DateCard.Category.FOOD_DRINKS, List.of(1.00, 1.00, 0.35, 0.18, 0.25, 0.90, 0.20, 1.00)},
                new Object[]{"Axe Throwing Match", "", "Poznan, Poland", "amusement_center", "$$", DateCard.Category.ACTIVITY, List.of(0.55, 0.95, 0.60, 0.81, 0.98, 0.55, 0.45, 0.0)},
                new Object[]{"Indoor Rock Climbing", "", "Poznan, Poland", "gym", "$$", DateCard.Category.ACTIVITY, List.of(0.53, 1.00, 0.69, 0.73, 0.99, 0.63, 0.78, 0.0)},
                new Object[]{"Open-Air Cinema", "", "Poznan, Poland", "movie_theater", "$", DateCard.Category.CHILL, List.of(0.20, 0.0, 0.88, 0.34, 0.37, 0.80, 0.40, 1.00)},
                new Object[]{"Bookstore Café Date", "", "Poznan, Poland", "book_store", "$", DateCard.Category.CHILL, List.of(0.25, 1.00, 0.50, 0.15, 0.20, 0.85, 0.65, 0.0)},
                new Object[]{"VR Arcade Night", "", "Poznan, Poland", "amusement_center", "$$", DateCard.Category.ACTIVITY, List.of(0.55, 1.00, 0.61, 0.92, 0.93, 0.40, 0.58, 1.00)},
                new Object[]{"Botanical Garden Walk", "", "Poznan, Poland", "park", "$", DateCard.Category.CHILL, List.of(0.15, 0.0, 0.83, 0.20, 0.35, 0.98, 0.90, 0.0)}
        );

        for (Object[] data : seedData) {
            DateCard card = DateCard.builder()
                    .title((String) data[0])
                    .imageUrl((String) data[1])
                    .location((String) data[2])
                    .attractionPlaceType((String) data[3])
                    .priceLevel((String) data[4])
                    .category((DateCard.Category) data[5])
                    .build();

            dateCardRepository.save(card);

            List<Double> vector = (List<Double>) data[6];
            for (int i = 0; i < vector.size(); i++) {
                FeatureVector fv = FeatureVector.builder()
                        .dateCard(card)
                        .index(i)
                        .value(vector.get(i))
                        .build();
                featureVectorRepository.save(fv);
            }
        }


        LOGGER.info("✅ Seeded {} date cards with vectors.", seedData.size());
    }


}


