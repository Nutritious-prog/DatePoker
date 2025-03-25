package com.datepoker.dp_backend.repositories;

import com.datepoker.dp_backend.entities.GameDateCard;
import com.datepoker.dp_backend.entities.UserProfile;
import com.datepoker.dp_backend.entities.Vote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface VoteRepository extends JpaRepository<Vote, Long> {
    List<Vote> findByGameDateCard(GameDateCard card);
    Optional<Vote> findByUserProfileAndGameDateCard(UserProfile user, GameDateCard card);
}

