package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.entities.User;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserProfileServiceTest {

    @Mock
    private UserProfileRepository profileRepository;

    @InjectMocks
    private UserProfileService profileService;

    @Test
    void createProfileIfNotExists_createsProfileWhenMissing() {
        User user = new User();
        user.setName("John");

        when(profileRepository.findByUser(user)).thenReturn(Optional.empty());

        profileService.createProfileIfNotExists(user);

        ArgumentCaptor<UserProfile> profileCaptor = ArgumentCaptor.forClass(UserProfile.class);
        verify(profileRepository).save(profileCaptor.capture());

        UserProfile saved = profileCaptor.getValue();
        assertEquals(user, saved.getUser());
        assertEquals("John", saved.getDisplayName());
    }

    @Test
    void createProfileIfNotExists_doesNothingIfProfileExists() {
        User user = new User();
        UserProfile existingProfile = new UserProfile();
        existingProfile.setUser(user);

        when(profileRepository.findByUser(user)).thenReturn(Optional.of(existingProfile));

        profileService.createProfileIfNotExists(user);

        verify(profileRepository, never()).save(any());
    }
}

