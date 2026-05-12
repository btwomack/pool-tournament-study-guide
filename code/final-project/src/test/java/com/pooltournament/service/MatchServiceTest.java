package com.pooltournament.service;

import com.pooltournament.dto.request.RecordResultRequest;
import com.pooltournament.dto.response.MatchResultResponse;
import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.enums.BracketFormat;
import com.pooltournament.enums.BracketType;
import com.pooltournament.enums.PlayerStatus;
import com.pooltournament.enums.TournamentStatus;
import com.pooltournament.exception.InvalidMatchResultException;
import com.pooltournament.exception.UnauthorizedAccessException;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.repository.PlayerRegistrationRepository;
import com.pooltournament.repository.TournamentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for MatchService.
 *
 * Covers:
 *  - Score validation against race-to
 *  - Winner/loser determination
 *  - Player advancement (advanceWinner, advanceLoser, placePlayerInMatch)
 *  - Grand Final: WB champ wins → tournament complete, no reset
 *  - Grand Final: LB champ wins → bracket reset triggered
 *  - Grand Final Reset: winner is tournament champion
 *  - Bye auto-advancement
 *  - Ownership check
 */
@ExtendWith(MockitoExtension.class)
public class MatchServiceTest {

    @Mock private MatchRepository matchRepo;
    @Mock private PlayerRegistrationRepository playerRepo;
    @Mock private TournamentRepository tournamentRepo;
    @Mock private WebSocketService wsService;

    @InjectMocks
    private MatchService matchService;

    private Tournament tournament;
    private User admin;
    private UUID adminId;
    private PlayerRegistration player1;
    private PlayerRegistration player2;
    private Match match;
    private UUID matchId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        admin = User.builder().id(adminId).email("admin@test.com").build();
        tournament = Tournament.builder()
                .id(UUID.randomUUID())
                .name("Test Tournament")
                .createdBy(admin)
                .bracketFormat(BracketFormat.SINGLE_ELIMINATION)
                .build();

        player1 = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Player 1")
                .lossesCount(0)
                .build();
        player2 = PlayerRegistration.builder()
                .id(UUID.randomUUID())
                .playerName("Player 2")
                .lossesCount(0)
                .build();

        matchId = UUID.randomUUID();
        match = Match.builder()
                .id(matchId)
                .tournament(tournament)
                .player1(player1)
                .player2(player2)
                .raceTo(3)
                .bracketType(BracketType.MAIN)
                .build();
    }

    // =========================================================================
    // SCORE VALIDATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Score Validation Tests")
    class ScoreValidationTests {

        @Test
        @DisplayName("Valid score: winner reaches race-to — succeeds")
        void validScore_WinnerReachesRaceTo_Success() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            MatchResultResponse response = matchService.recordResult(matchId, request, adminId);

            assertNotNull(response);
            assertEquals(player1, response.getMatch().getWinner());
            assertEquals(3, response.getMatch().getPlayer1Score());
        }

        @Test
        @DisplayName("Invalid score: winner score below race-to — throws InvalidMatchResultException")
        void invalidScore_BelowRaceTo_ThrowsException() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(2); // race-to is 3, so 2 is invalid
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            assertThrows(InvalidMatchResultException.class, () ->
                    matchService.recordResult(matchId, request, adminId));
        }

        @Test
        @DisplayName("Score of exactly race-to is valid")
        void exactRaceToScore_IsValid() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player2.getId());
            request.setPlayer2Score(3); // exactly race-to
            request.setPlayer1Score(0);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            MatchResultResponse response = matchService.recordResult(matchId, request, adminId);

            assertEquals(player2, response.getMatch().getWinner());
        }

        @Test
        @DisplayName("Score above race-to is valid (e.g., 5-3 in race-to-3)")
        void scoreAboveRaceTo_IsValid() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(5); // above race-to, still valid
            request.setPlayer2Score(3);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            assertDoesNotThrow(() -> matchService.recordResult(matchId, request, adminId));
        }
    }

    // =========================================================================
    // WINNER/LOSER DETERMINATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Winner and Loser Determination Tests")
    class WinnerLoserTests {

        @Test
        @DisplayName("Player 1 wins — player1 is winner, player2 is loser")
        void player1Wins_CorrectAssignment() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            MatchResultResponse response = matchService.recordResult(matchId, request, adminId);

            assertEquals(player1, response.getMatch().getWinner());
            assertEquals(player2, response.getMatch().getLoser());
        }

        @Test
        @DisplayName("Player 2 wins — player2 is winner, player1 is loser")
        void player2Wins_CorrectAssignment() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player2.getId());
            request.setPlayer1Score(1);
            request.setPlayer2Score(3);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            MatchResultResponse response = matchService.recordResult(matchId, request, adminId);

            assertEquals(player2, response.getMatch().getWinner());
            assertEquals(player1, response.getMatch().getLoser());
        }

        @Test
        @DisplayName("Loser gets loss count incremented")
        void loser_LossCountIncremented() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            assertEquals(1, player2.getLossesCount());
        }

        @Test
        @DisplayName("Loser in single elimination is ELIMINATED after 1 loss")
        void loser_SingleElim_EliminatedAfter1Loss() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            assertEquals(PlayerStatus.ELIMINATED, player2.getStatus());
        }

        @Test
        @DisplayName("Loser in double elimination is NOT eliminated after 1 loss")
        void loser_DoubleElim_NotEliminatedAfter1Loss() {
            tournament.setBracketFormat(BracketFormat.DOUBLE_ELIMINATION);
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            // After 1 loss in double elim, player should NOT be eliminated
            assertNotEquals(PlayerStatus.ELIMINATED, player2.getStatus());
        }
    }

    // =========================================================================
    // PLAYER ADVANCEMENT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Player Advancement Tests")
    class AdvancementTests {

        @Test
        @DisplayName("Winner advances to nextMatchWinner when it exists")
        void winner_AdvancesToNextMatch() {
            Match nextMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .bracketType(BracketType.MAIN)
                    .raceTo(3)
                    .build();
            match.setNextMatchWinner(nextMatch);

            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            // player1 should have been placed in nextMatch
            assertEquals(player1, nextMatch.getPlayer1());
        }

        @Test
        @DisplayName("Winner with no nextMatchWinner is set as tournament WINNER")
        void winner_NoNextMatch_SetAsChampion() {
            // match has no nextMatchWinner
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            assertEquals(PlayerStatus.WINNER, player1.getStatus());
            assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        }

        @Test
        @DisplayName("Loser advances to nextMatchLoser in double elimination")
        void loser_AdvancesToLosersMatch_DoubleElim() {
            tournament.setBracketFormat(BracketFormat.DOUBLE_ELIMINATION);
            Match losersMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .bracketType(BracketType.LOSERS)
                    .raceTo(3)
                    .build();
            match.setNextMatchLoser(losersMatch);

            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            matchService.recordResult(matchId, request, adminId);

            // player2 (loser) should have been placed in losersMatch
            assertEquals(player2, losersMatch.getPlayer1());
        }

        @Test
        @DisplayName("placePlayerInMatch fills player1 first, then player2")
        void placePlayerInMatch_FillsSlots() {
            Match target = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .bracketType(BracketType.MAIN)
                    .raceTo(3)
                    .build();

            matchService.placePlayerInMatch(player1, target);
            assertEquals(player1, target.getPlayer1());
            assertNull(target.getPlayer2());

            matchService.placePlayerInMatch(player2, target);
            assertEquals(player1, target.getPlayer1());
            assertEquals(player2, target.getPlayer2());
        }

        @Test
        @DisplayName("Bye auto-advancement: player1 advances to next match")
        void autoAdvanceBye_Player1AdvancesToNext() {
            Match nextMatch = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .bracketType(BracketType.MAIN)
                    .raceTo(3)
                    .build();
            match.setNextMatchWinner(nextMatch);
            match.setIsBye(true);

            matchService.autoAdvanceBye(match);

            assertEquals(player1, match.getWinner());
            assertNotNull(match.getCompletedAt());
            assertEquals(player1, nextMatch.getPlayer1());
        }
    }

    // =========================================================================
    // GRAND FINAL TESTS
    // =========================================================================

    @Nested
    @DisplayName("Grand Final Tests")
    class GrandFinalTests {

        private Match grandFinal;
        private Match grandFinalReset;

        @BeforeEach
        void setUpGrandFinal() {
            tournament.setBracketFormat(BracketFormat.DOUBLE_ELIMINATION);

            grandFinalReset = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .bracketType(BracketType.FINALS_RESET)
                    .raceTo(3)
                    .build();

            grandFinal = Match.builder()
                    .id(UUID.randomUUID())
                    .tournament(tournament)
                    .player1(player1) // player1 = WB champ (convention)
                    .player2(player2) // player2 = LB champ
                    .raceTo(3)
                    .bracketType(BracketType.FINALS)
                    .nextMatchWinner(grandFinalReset)
                    .build();
        }

        @Test
        @DisplayName("Grand Final: WB champ (player1) wins → tournament COMPLETE, no reset")
        void grandFinal_WBChampWins_TournamentComplete_NoReset() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId()); // WB champ wins
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(grandFinal.getId())).thenReturn(Optional.of(grandFinal));

            MatchResultResponse response = matchService.recordResult(grandFinal.getId(), request, adminId);

            // No bracket reset
            assertFalse(Boolean.TRUE.equals(response.getBracketReset()));

            // Tournament is completed
            assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());

            // WB champ is the winner
            assertEquals(PlayerStatus.WINNER, player1.getStatus());
            assertEquals(PlayerStatus.ELIMINATED, player2.getStatus());

            // GF Reset should be deactivated (marked as bye)
            assertTrue(Boolean.TRUE.equals(grandFinalReset.getIsBye()));
        }

        @Test
        @DisplayName("Grand Final: LB champ (player2) wins → bracket reset triggered")
        void grandFinal_LBChampWins_BracketReset() {
            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player2.getId()); // LB champ wins
            request.setPlayer1Score(1);
            request.setPlayer2Score(3);

            when(matchRepo.findById(grandFinal.getId())).thenReturn(Optional.of(grandFinal));

            MatchResultResponse response = matchService.recordResult(grandFinal.getId(), request, adminId);

            // Bracket reset should be signaled
            assertTrue(Boolean.TRUE.equals(response.getBracketReset()));

            // GF Reset should be populated with both players
            assertEquals(player2, grandFinalReset.getPlayer1()); // LB champ
            assertEquals(player1, grandFinalReset.getPlayer2()); // WB champ

            // Tournament should NOT be completed yet
            assertNotEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        }

        @Test
        @DisplayName("Grand Final Reset: winner is tournament champion")
        void grandFinalReset_Winner_IsTournamentChampion() {
            grandFinalReset.setPlayer1(player2); // LB champ in GF2
            grandFinalReset.setPlayer2(player1); // WB champ in GF2

            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player2.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(2);

            when(matchRepo.findById(grandFinalReset.getId())).thenReturn(Optional.of(grandFinalReset));

            matchService.recordResult(grandFinalReset.getId(), request, adminId);

            assertEquals(PlayerStatus.WINNER, player2.getStatus());
            assertEquals(PlayerStatus.ELIMINATED, player1.getStatus());
            assertEquals(TournamentStatus.COMPLETED, tournament.getStatus());
        }
    }

    // =========================================================================
    // SECURITY TESTS
    // =========================================================================

    @Nested
    @DisplayName("Security and Ownership Tests")
    class SecurityTests {

        @Test
        @DisplayName("Non-owner cannot record match results — throws UnauthorizedAccessException")
        void nonOwner_CannotRecordResult() {
            UUID nonOwnerId = UUID.randomUUID(); // Different from adminId

            RecordResultRequest request = new RecordResultRequest();
            request.setWinnerId(player1.getId());
            request.setPlayer1Score(3);
            request.setPlayer2Score(1);

            when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

            assertThrows(UnauthorizedAccessException.class, () ->
                    matchService.recordResult(matchId, request, nonOwnerId));
        }
    }

    // =========================================================================
    // WEBSOCKET BROADCAST TESTS
    // =========================================================================

    @Test
    @DisplayName("WebSocket broadcast is called after recording a result")
    void webSocketBroadcast_CalledAfterResult() {
        RecordResultRequest request = new RecordResultRequest();
        request.setWinnerId(player1.getId());
        request.setPlayer1Score(3);
        request.setPlayer2Score(1);

        when(matchRepo.findById(matchId)).thenReturn(Optional.of(match));

        matchService.recordResult(matchId, request, adminId);

        verify(wsService).broadcastMatchResult(tournament.getId(), matchId);
    }
}
