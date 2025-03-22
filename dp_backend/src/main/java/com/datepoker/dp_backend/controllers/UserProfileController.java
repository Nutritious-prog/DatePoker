package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.DTO.ApiResponse;
import com.datepoker.dp_backend.DTO.UserProfileResponse;
import com.datepoker.dp_backend.DTO.UserProfileUpdateRequest;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> getProfile(@CurrentUser User currentUser) {
        UserProfileResponse profile = profileService.getMyProfile(currentUser);
        return ResponseEntity.ok(ApiResponse.success("Profile loaded", profile));
    }

    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UserProfileResponse>> updateProfile(
            @CurrentUser User currentUser,
            @Valid @RequestBody UserProfileUpdateRequest request
    ) {
        UserProfileResponse profile = profileService.updateMyProfile(currentUser, request);
        return ResponseEntity.ok(ApiResponse.success("Profile updated", profile));
    }

    @PostMapping("/picture")
    public ResponseEntity<ApiResponse<String>> updatePicture(
            @CurrentUser User currentUser,
            @RequestParam String imageUrl
    ) {
        String url = profileService.updateProfilePicture(currentUser, imageUrl);
        return ResponseEntity.ok(ApiResponse.success("Picture updated", url));
    }

    @GetMapping("/debug/whoami")
    public String whoami() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth == null ? "No auth" : auth.getName();
    }

}
