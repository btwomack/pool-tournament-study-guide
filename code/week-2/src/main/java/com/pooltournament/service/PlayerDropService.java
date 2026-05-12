package com.pooltournament.service;

import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlayerDropService {
    private final MatchRepository matchRepo;
    private final PlayerRegistrationRepository playerRepo;
    private final MatchService matchService;
    private final WebSocketService wsService;

    @Transactional
    public void dropPlayer(UUID playerId, UUID tournamentId, String reason) {
        PlayerRegistration player = playerRepo.findById(playerId)
                .orElseThrow(() -> new RuntimeException("Player not found"));
        
        player.setStatus("disqualified".equals(reason)
                ? PlayerStatus.DISQUALIFIED
                : PlayerStatus.DROPPED);
        playerRepo.save(player);

        // Find all pending matches for this player
        List<Match> pending = matchRepo.findPendingByPlayer(tournamentId, playerId);
        for (Match m : pending) {
            PlayerRegistration opponent;
            if (m.getPlayer1() != null && m.getPlayer1().getId().equals(playerId)) {
                opponent = m.getPlayer2();
            } else {
                opponent = m.getPlayer1();
            }

            if (opponent != null) {
                m.setWinner(opponent);
                m.setLoser(player);
                m.setIsForfeit(true);
                m.setCompletedAt(Instant.now());
                matchRepo.save(m);

                if (m.getNextMatchWinner() != null) {
                    matchService.placePlayerInMatch(opponent, m.getNextMatchWinner());
                }
            }
        }
        wsService.broadcastPlayerDrop(tournamentId, playerId);
    }
}
