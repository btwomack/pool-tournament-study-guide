package com.pooltournament.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "calcutta_bids")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class CalcuttaBid {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tournament_id", nullable = false)
    private Tournament tournament;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "player_id", nullable = false)
    private PlayerRegistration player;

    @Column(name = "bidder_name", nullable = false)
    private String bidderName;

    @Column(name = "bid_amount", nullable = false)
    private BigDecimal bidAmount;
}
