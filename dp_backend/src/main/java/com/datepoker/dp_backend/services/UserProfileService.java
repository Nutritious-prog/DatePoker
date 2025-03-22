package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.DTO.UserProfileResponse;
import com.datepoker.dp_backend.DTO.UserProfileUpdateRequest;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.repositories.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserProfileService {

    private final UserRepository userRepository;
    private final UserProfileRepository profileRepository;

    public void createProfileIfNotExists(User user) {
        profileRepository.findByUser(user).ifPresentOrElse(
                profile -> {
                    // Already exists, do nothing
                },
                () -> {
                    UserProfile profile = new UserProfile();
                    profile.setUser(user);
                    profile.setDisplayName(user.getName()); // You can default from auth name
                    profile.setProfilePictureUrl(null); // Default pic later if needed
                    profile.setEmail(user.getEmail());
                    profileRepository.save(profile);
                }
        );
    }

    public UserProfileResponse getMyProfile(User currentUser) {
        UserProfile profile = profileRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        return new UserProfileResponse(profile.getDisplayName(), profile.getProfilePictureUrl(), profile.getEmail());
    }

    public UserProfileResponse updateMyProfile(User currentUser, UserProfileUpdateRequest request) {
        UserProfile profile = profileRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setDisplayName(request.getDisplayName());
        profile.setProfilePictureUrl(request.getProfilePictureUrl());
        profileRepository.save(profile);

        return new UserProfileResponse(profile.getDisplayName(), profile.getProfilePictureUrl(), profile.getEmail());
    }

    // Stub - ready for file handling later
    public String updateProfilePicture(User currentUser, String newUrl) {
        UserProfile profile = profileRepository.findByUser(currentUser)
                .orElseThrow(() -> new RuntimeException("Profile not found"));

        profile.setProfilePictureUrl(newUrl);
        profileRepository.save(profile);
        return newUrl;
    }
}

