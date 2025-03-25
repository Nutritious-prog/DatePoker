package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Vote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_profile_id", nullable = false)
    private UserProfile userProfile;

    @ManyToOne
    @JoinColumn(name = "game_date_card_id", nullable = false)
    private GameDateCard gameDateCard;

    @Enumerated(EnumType.STRING)
    private VoteValue value;

    public enum VoteValue {
        LIKE, DISLIKE
    }
}

