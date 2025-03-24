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
public class DateCard {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String title;

    private String imageUrl;

    private String location;

    private String priceLevel; // $, $$, $$$

    @Enumerated(EnumType.STRING)
    private Category category;

    // Later: if we want to match cards to GameSettings
    //0	Plan type	            1.0 = Food & Drinks, 0.5 = Activities, 0.0 = Chill
    //1	Location type	        1.0 = Indoor, 0.0 = Outdoor, 0.5 = Both
    //2	Season (hot to cold)	0.0 = Winter → 1.0 = Summer
    //3	Romantic vs Fun	        0.0 = Romantic → 1.0 = Fun
    //4	Chill vs Adventure	    0.0 = Chill → 1.0 = Adventure
    //5	Budget friendly	        1.0 = Budget, 0.0 = Expensive
    //6	Healthy	                1.0 = Healthy, 0.0 = Not healthy
    //7	Night/Day	            1.0 = Night, 0.0 = Day
    @OneToMany(mappedBy = "dateCard", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<FeatureVector> featureVector;

    @ManyToOne
    @JoinColumn(name = "game_room_id")
    private GameRoom gameRoom;

    public enum Category {
        FOOD_DRINKS, ACTIVITY, CHILL, OTHER
    }
}

