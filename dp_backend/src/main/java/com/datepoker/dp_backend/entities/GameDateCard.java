package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameDateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    private GameRoom gameRoom;

    @ManyToOne
    private DateCard dateCard;

    @Enumerated(EnumType.STRING)
    private Status status; // ACCEPTED / REJECTED / UNDECIDED

    @OneToMany(mappedBy = "gameDateCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Vote> votes;

    public enum Status {
        UNDECIDED, ACCEPTED, REJECTED
    }
}

