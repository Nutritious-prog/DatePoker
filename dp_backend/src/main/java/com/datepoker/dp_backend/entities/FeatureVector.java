package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FeatureVector {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private int index;

    private double value;

    @ManyToOne
    @JoinColumn(name = "date_card_id")
    private DateCard dateCard;
}
