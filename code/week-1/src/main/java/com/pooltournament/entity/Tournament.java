package com.pooltournament.entity;

import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.TournamentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "tournaments")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Tournament {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "join_code", unique = true, nullable = false)
    private String joinCode;

    @Builder.Default
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private TournamentStatus status = TournamentStatus.REGISTRATION;

    @Column(name = "max_players", nullable = false)
    private Integer maxPlayers; // 2-32

    @Enumerated(EnumType.STRING)
    @Column(name = "bracket_format")
    private BracketFormat bracketFormat;

    @Builder.Default
    @Column(name = "format_override")
    private Boolean formatOverride = false;

    @Builder.Default
    @Column(name = "entry_fee", nullable = false)
    private BigDecimal entryFee = BigDecimal.ZERO;

    @Builder.Default
    @Column(name = "race_to_default", nullable = false)
    private Integer raceToDefault = 3;

    @Builder.Default
    @Column(name = "enable_calcutta")
    private Boolean enableCalcutta = false;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "venue_id")
    private Venue venue;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "created_by_user_id", nullable = false)
    private User createdBy;

    private LocalDate date;
    private String location;

    @Builder.Default
    @Column(name = "created_at", nullable = false)
    private Instant createdAt = Instant.now();

    public boolean isOwnedBy(UUID userId) {
        return this.createdBy != null && this.createdBy.getId().equals(userId);
    }
}
