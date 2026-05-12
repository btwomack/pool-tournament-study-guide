package com.pooltournament.service;

import com.pooltournament.entity.CalcuttaBid;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.repository.CalcuttaBidRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import com.pooltournament.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CalcuttaService {
    private final CalcuttaBidRepository bidRepo;
    private final TournamentRepository tournamentRepo;
    private final PlayerRegistrationRepository playerRepo;
    private final UserRepository userRepo;

    @Transactional
    public CalcuttaBid placeBid(UUID tournamentId, UUID playerId, UUID userId, Double amount) {
        Tournament tournament = tournamentRepo.findById(tournamentId)
                .orElseThrow(() -> new RuntimeException("Tournament not found"));
        PlayerRegistration player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        User bidder = userRepo.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // Check for higher bids
        Double currentMax = bidRepo.findMaxBidByPlayerId(playerId).map(b -> b.getBidAmount().doubleValue()).orElse(0.0);
        if (currentMax != null && amount <= currentMax) {
            throw new IllegalArgumentException("Bid must be higher than the current maximum bid: " + currentMax);
        }

        CalcuttaBid bid = CalcuttaBid.builder()
                .tournament(tournament)
                .player(player)
                .bidderName(bidder.getEmail())
                .bidAmount(java.math.BigDecimal.valueOf(amount))
                .build();

        return bidRepo.save(bid);
    }

    public List<CalcuttaBid> getBidsByTournament(UUID tournamentId) {
        return bidRepo.findByTournament_Id(tournamentId);
    }

    public List<CalcuttaBid> getBidsByPlayer(UUID playerId) {
        return bidRepo.findByPlayer_Id(playerId);
    }
}
