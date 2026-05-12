package com.pooltournament.entity;

import com.pooltournament.enums.BracketType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "matches")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Match {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @Column(name = "round_number", nullable = false)
    private Integer roundNumber;

    @Column(name = "match_number", nullable = false)
    private Integer matchNumber;

    @Enumerated(EnumType.STRING)
    @Column(name = "bracket_type", nullable = false)
    private BracketType bracketType; // MAIN, LOSERS, FINALS, FINALS_RESET

    @Column(name = "position_label")
    private String positionLabel;

    @Builder.Default
    @Column(name = "race_to", nullable = false)
    private Integer raceTo = 3;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player1_id")
    private PlayerRegistration player1;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player2_id")
    private PlayerRegistration player2;

    @Builder.Default
    @Column(name = "player1_score", nullable = false)
    private Integer player1Score = 0;

    @Builder.Default
    @Column(name = "player2_score", nullable = false)
    private Integer player2Score = 0;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "winner_id")
    private PlayerRegistration winner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "loser_id")
    private PlayerRegistration loser;

    // Self-referencing links for bracket advancement
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match_winner_id")
    private Match nextMatchWinner;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "next_match_loser_id")
    private Match nextMatchLoser;

    @Builder.Default
    @Column(name = "is_bye", nullable = false)
    private Boolean isBye = false;

    @Builder.Default
    @Column(name = "is_forfeit", nullable = false)
    private Boolean isForfeit = false;

    @Column(name = "completed_at")
    private Instant completedAt;

    public boolean isCompleted() {
        return this.completedAt != null;
    }

    public boolean hasBothPlayers() {
        return this.player1 != null && this.player2 != null;
    }
}
