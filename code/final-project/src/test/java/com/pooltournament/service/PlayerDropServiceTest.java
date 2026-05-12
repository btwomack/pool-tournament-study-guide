package com.pooltournament.service;

import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.BracketType;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PlayerDropService.
 *
 * Covers:
 *  - Player drop: status set to DROPPED
 *  - Player disqualification: status set to DISQUALIFIED
 *  - Pending matches: opponent is auto-advanced
 *  - Forfeit flag is set on the match
 *  - WebSocket broadcast is called
 */
@ExtendWith(MockitoExtension.class)
public class PlayerDropServiceTest {

    @Mock private MatchRepository matchRepo;
    @Mock private PlayerRegistrationRepository playerRepo;
    @Mock private TournamentRepository tournamentRepo;
    @Mock private WebSocketService wsService;
    @Mock private MatchService matchService;

    @InjectMocks
    private PlayerDropService playerDropService;

    private Tournament tournament;
    private User admin;
    private UUID adminId;
    private PlayerRegistration droppingPlayer;
    private PlayerRegistration opponent;
    private Match pendingMatch;
    private UUID tournamentId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        admin = User.builder().id(adminId).email("admin@test.com").build();
        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Test Tournament")
                .createdBy(admin)
                .bracketFormat(BracketFormat.DOUBLE_ELIMINATION)
                .build();

        droppingPlayer = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Dropping Player")
                .tournament(tournament)
                .status(PlayerStatus.CONFIRMED)
                .lossesCount(0)
                .build();

        opponent = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Opponent")
                .tournament(tournament)
                .status(PlayerStatus.CONFIRMED)
                .lossesCount(0)
                .build();

        Match nextMatch = Match.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .bracketType(BracketType.MAIN)
                .raceTo(3)
                .build();

        pendingMatch = Match.builder()
                .id(UUID.randomUUID())
                .tournament(tournament)
                .player1(droppingPlayer)
                .player2(opponent)
                .raceTo(3)
                .bracketType(BracketType.MAIN)
                .nextMatchWinner(nextMatch)
                .build();
    }

    @Test
    @DisplayName("Drop player: status set to DROPPED")
    void dropPlayer_StatusSetToDropped() {
        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of());

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "Voluntary withdrawal");

        assertEquals(PlayerStatus.DROPPED, droppingPlayer.getStatus());
        verify(playerRepo).save(droppingPlayer);
    }

    @Test
    @DisplayName("Disqualify player: status set to DISQUALIFIED")
    void disqualifyPlayer_StatusSetToDisqualified() {
        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of());

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "DISQUALIFIED");

        assertEquals(PlayerStatus.DISQUALIFIED, droppingPlayer.getStatus());
    }

    @Test
    @DisplayName("Drop player with pending match: opponent is auto-advanced")
    void dropPlayer_WithPendingMatch_OpponentAdvanced() {
        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of(pendingMatch));

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "Voluntary withdrawal");

        // Match should be marked as forfeit
        assertTrue(Boolean.TRUE.equals(pendingMatch.getIsForfeit()));
        assertNotNull(pendingMatch.getCompletedAt());

        // Opponent should be set as winner
        assertEquals(opponent, pendingMatch.getWinner());
        assertEquals(droppingPlayer, pendingMatch.getLoser());

        // Opponent should be advanced
        verify(matchService).placePlayerInMatch(eq(opponent), any());
    }

    @Test
    @DisplayName("Drop player with no pending matches: no advancement needed")
    void dropPlayer_NoPendingMatches_NoAdvancement() {
        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of());

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "Voluntary withdrawal");

        // No advancement should happen
        verify(matchService, never()).placePlayerInMatch(any(), any());
    }

    @Test
    @DisplayName("WebSocket broadcast is called after player drop")
    void dropPlayer_WebSocketBroadcast_Called() {
        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of());

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "Voluntary withdrawal");

        verify(wsService).broadcastPlayerDrop(tournamentId, droppingPlayer.getId());
    }

    @Test
    @DisplayName("Drop player where they are player2: opponent (player1) is advanced")
    void dropPlayer_AsPlayer2_OpponentPlayer1Advanced() {
        // Swap roles: dropping player is player2
        pendingMatch.setPlayer1(opponent);
        pendingMatch.setPlayer2(droppingPlayer);

        when(playerRepo.findById(droppingPlayer.getId())).thenReturn(Optional.of(droppingPlayer));
        when(matchRepo.findPendingByPlayer(tournamentId, droppingPlayer.getId()))
                .thenReturn(List.of(pendingMatch));

        playerDropService.dropPlayer(droppingPlayer.getId(), tournamentId, "Voluntary withdrawal");

        // Opponent (player1) should be the winner
        assertEquals(opponent, pendingMatch.getWinner());
        assertEquals(droppingPlayer, pendingMatch.getLoser());
    }
}
