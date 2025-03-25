package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Size;

@Entity
@Table(name = "user_profiles")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Display name is required")
    @Size(min = 3, max = 50, message = "Display name must be between 3 and 50 characters")
    @Column(name = "display_name", nullable = false, length = 50)
    private String displayName;

    @Size(max = 2048, message = "Profile picture URL is too long")
    @Column(name = "profile_picture_url")
    private String profilePictureUrl;

    @OneToOne
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(name = "email")
    private String email;

    public UserProfile(String displayName, String profilePictureUrl, User user, String email) {
        this.displayName = displayName;
        this.profilePictureUrl = profilePictureUrl;
        this.user = user;
        this.email = email;
    }
}

