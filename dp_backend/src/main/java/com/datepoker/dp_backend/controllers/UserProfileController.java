package com.datepoker.dp_backend.controllers;

import com.datepoker.dp_backend.DTO.*;
import com.datepoker.dp_backend.annotations.CurrentUser;
import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import com.datepoker.dp_backend.services.DateHistoryService;
import com.datepoker.dp_backend.services.UserProfileService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/v1/profile")
@RequiredArgsConstructor
public class UserProfileController {

    private final UserProfileService profileService;
    private final DateHistoryService dateHistoryService;
    private final UserProfileRepository userProfileRepository;

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

    @GetMapping("/dates-history")
    public ResponseEntity<ApiResponse<List<UserDateResponse>>> getDateHistory(@CurrentUser User user) {
        List<UserDateResponse> history = dateHistoryService.getUserHistory(user);
        return ResponseEntity.ok(ApiResponse.success("Date history loaded", history));
    }

    @PostMapping("/location")
    public ResponseEntity<ApiResponse<Void>> updateLocation(
            @RequestBody LocationUpdateRequest request,
            @CurrentUser User user) {

        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new RuntimeException("User profile not found"));

        profile.setLatitude(request.latitude());
        profile.setLongitude(request.longitude());
        userProfileRepository.save(profile);

        return ResponseEntity.ok(ApiResponse.success("Location updated", null));
    }


}
