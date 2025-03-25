package com.datepoker.dp_backend.services;

import com.datepoker.dp_backend.DTO.UserDateResponse;
import com.datepoker.dp_backend.entities.*;
import com.datepoker.dp_backend.repositories.UserDateRepository;
import com.datepoker.dp_backend.repositories.UserProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DateHistoryService {

    private final UserDateRepository userDateRepository;
    private final UserProfileRepository userProfileRepository;

    public void saveForRoom(GameRoom room, DateCard winningCard) {
        if (room.getCreator() == null || room.getJoiner() == null || winningCard == null) {
            return;
        }

        LocalDateTime now = LocalDateTime.now();

        UserDate creatorEntry = UserDate.builder()
                .userProfile(room.getCreator())
                .dateCard(winningCard)
                .dateHappened(now)
                .build();

        UserDate joinerEntry = UserDate.builder()
                .userProfile(room.getJoiner())
                .dateCard(winningCard)
                .dateHappened(now)
                .build();

        userDateRepository.saveAll(List.of(creatorEntry, joinerEntry));
    }

    public List<UserDateResponse> getUserHistory(User user) {
        UserProfile profile = userProfileRepository.findByUserId(user.getId())
                .orElseThrow(() -> new IllegalStateException("User profile not found"));

        return userDateRepository.findByUserProfile(profile).stream()
                .map(UserDateResponse::from)
                .toList();
    }

}
