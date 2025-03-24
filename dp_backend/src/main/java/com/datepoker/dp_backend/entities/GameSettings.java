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
public class GameSettings {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private PlanType planType;

    @Enumerated(EnumType.STRING)
    private LocationType locationType;

    @Enumerated(EnumType.STRING)
    private Season season;

    private float romanticFun; // 0.0 romantic ←→ 1.0 fun
    private float chillAdventure; // 0.0 chill ←→ 1.0 adventure

    private boolean budgetFriendly;
    private boolean healthyOptions;
    private boolean nightDate;

    public enum PlanType {
        FOOD_DRINKS, ACTIVITIES, CHILL
    }

    public enum LocationType {
        INDOOR, OUTDOOR, BOTH
    }

    public enum Season {
        WINTER, AUTUMN, SPRING, SUMMER, ANY
    }

    public double[] toFeatureVector() {
        double seasonValue = switch (season) {
            case WINTER -> 0.0;
            case AUTUMN -> 0.33;
            case SPRING -> 0.66;
            case SUMMER -> 1.0;
            case ANY -> 0.5;
        };

        return new double[]{
                planType == PlanType.FOOD_DRINKS ? 1.0 : planType == PlanType.ACTIVITIES ? 0.5 : 0.0,
                locationType == LocationType.INDOOR ? 1.0 : locationType == LocationType.OUTDOOR ? 0.0 : 0.5,
                seasonValue,
                romanticFun,
                chillAdventure,
                budgetFriendly ? 1.0 : 0.0,
                healthyOptions ? 1.0 : 0.0,
                nightDate ? 1.0 : 0.0
        };
    }

    public static GameSettings fromOptions(List<String> options, UserProfile userProfile) {
        GameSettings.GameSettingsBuilder builder = GameSettings.builder()
                .planType(null)
                .locationType(null)
                .season(Season.ANY)
                .romanticFun(0.5f)
                .chillAdventure(0.5f)
                .budgetFriendly(false)
                .healthyOptions(false)
                .nightDate(false);

        for (String option : options) {
            switch (option.toLowerCase()) {
                case "food_and_drinks" -> builder.planType(PlanType.FOOD_DRINKS);
                case "activities" -> builder.planType(PlanType.ACTIVITIES);
                case "plan_chill" -> builder.planType(PlanType.CHILL);

                case "indoor" -> builder.locationType(LocationType.INDOOR);
                case "outdoor" -> builder.locationType(LocationType.OUTDOOR);
                case "both" -> builder.locationType(LocationType.BOTH);

                case "winter" -> builder.season(Season.WINTER);
                case "autumn" -> builder.season(Season.AUTUMN);
                case "spring" -> builder.season(Season.SPRING);
                case "summer" -> builder.season(Season.SUMMER);

                case "romantic" -> builder.romanticFun(0.0f);
                case "fun" -> builder.romanticFun(1.0f);

                case "adventure" -> builder.chillAdventure(1.0f);
                case "chill" -> builder.chillAdventure(0.0f);

                case "budget_friendly" -> builder.budgetFriendly(true);
                case "healthy" -> builder.healthyOptions(true);
                case "night" -> builder.nightDate(true);
            }
        }

        return builder.build();
    }

}


