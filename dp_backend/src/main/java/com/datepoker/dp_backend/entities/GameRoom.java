package com.datepoker.dp_backend.entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import org.springframework.data.annotation.LastModifiedDate;

import java.time.LocalDateTime;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String code;

    @ManyToOne
    @JoinColumn(name = "creator_id")
    private UserProfile creator;

    @ManyToOne
    @JoinColumn(name = "joiner_id")
    private UserProfile joiner;

    @OneToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "settings_id")
    private GameSettings settings;

    @Enumerated(EnumType.STRING)
    private Status status;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "gameRoom", cascade = CascadeType.ALL)
    private List<GameDateCard> gameCards;

    private Long acceptedCardId;

    private boolean isActive;

    @LastModifiedDate
    private LocalDateTime lastActivity;

    @Column(nullable = false)
    private boolean disconnected = false;

    private LocalDateTime expiresAt;

    @Enumerated(EnumType.STRING)
    private DisconnectionReason disconnectionReason;

    public enum DisconnectionReason {
        USER_LEFT,
        TIMEOUT,
        UNKNOWN
    }


    public enum Status {
        WAITING,
        ACTIVE,
        FINISHED,
        CANCELLED
    }

    // 👇 Convenience method (used in DTO)
    public String getGameCode() {
        return code;
    }

    // 👇 Optional: explicit "isActive()" getter
    public boolean isActive() {
        return isActive;
    }
}
