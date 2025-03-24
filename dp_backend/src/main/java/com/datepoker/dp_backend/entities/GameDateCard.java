package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;

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

    public enum Status {
        UNDECIDED, ACCEPTED, REJECTED
    }
}

