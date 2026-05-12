package com.pooltournament.entity;

import com.pooltournament.enums.PlayerStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "player_registrations")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class PlayerRegistration {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "player_name", nullable = false)
    private String playerName;

    @Column(name = "contact_info")
    private String contactInfo;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PlayerStatus status = PlayerStatus.PENDING;

    @Builder.Default
    @Column(name = "paid_flag", nullable = false)
    private Boolean paidFlag = false;

    @Column(name = "payment_intent_id")
    private String paymentIntentId;

    @Column(name = "seed_number")
    private Integer seedNumber;

    @Builder.Default
    @Column(name = "losses_count", nullable = false)
    private Integer lossesCount = 0;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public void incrementLosses() {
        this.lossesCount++;
    }
}
