package com.pooltournament.service;

import com.pooltournament.dto.request.GenerateBracketRequest;
import com.pooltournament.dto.response.BracketResponse;
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
import com.pooltournament.security.TournamentOwnershipChecker;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for BracketGeneratorService.
 *
 * Covers:
 *  - Single elimination: 4 players (no byes), 5 players (byes), 8 players (no byes)
 *  - Double elimination: 10, 16, 32 players
 *  - Bye auto-advancement
 *  - Grand Final and Grand Final Reset wiring
 *  - Format selection logic
 */
@ExtendWith(MockitoExtension.class)
public class BracketGeneratorServiceTest {

    @Mock private MatchRepository matchRepository;
    @Mock private PlayerRegistrationRepository playerRepo;
    @Mock private TournamentRepository tournamentRepo;
    @Mock private WebSocketService wsService;
    @Mock private TournamentOwnershipChecker ownershipChecker;
    @Mock private MatchService matchService;

    @InjectMocks
    private BracketGeneratorService bracketGeneratorService;

    private Tournament tournament;
    private User admin;
    private UUID tournamentId;
    private UUID adminId;

    @BeforeEach
    void setUp() {
        adminId = UUID.randomUUID();
        admin = User.builder().id(adminId).email("admin@test.com").build();
        tournamentId = UUID.randomUUID();
        tournament = Tournament.builder()
                .id(tournamentId)
                .name("Test Tournament")
                .createdBy(admin)
                .raceToDefault(3)
                .build();
    }

    private List<PlayerRegistration> createPlayers(int count) {
        List<PlayerRegistration> players = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            players.add(PlayerRegistration.builder()
                    .id(UUID.randomUUID())
                    .playerName("Player " + (i + 1))
                    .status(PlayerStatus.CONFIRMED)
                    .seedNumber(i + 1)
                    .build());
        }
        return players;
    }

    private BracketResponse generateBracket(int playerCount) {
        List<PlayerRegistration> players = createPlayers(playerCount);
        when(ownershipChecker.verifyAndLoad(tournamentId, adminId)).thenReturn(tournament);
        when(playerRepo.findByTournamentAndStatus(tournament, PlayerStatus.CONFIRMED)).thenReturn(players);
        GenerateBracketRequest request = new GenerateBracketRequest();
        request.setSeedingMode("manual");
        return bracketGeneratorService.generateBracket(tournamentId, adminId, request);
    }

    // =========================================================================
    // SINGLE ELIMINATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Single Elimination Bracket Tests")
    class SingleEliminationTests {

        @Test
        @DisplayName("4 players — no byes, 3 total matches (2 in R1, 1 in R2)")
        void singleElim_4Players_NoByes() {
            BracketResponse response = generateBracket(4);

            assertEquals(BracketFormat.SINGLE_ELIMINATION.name(), response.getFormat());
            // 4 players: R1 has 2 matches, R2 has 1 match = 3 total
            assertEquals(3, response.getMatches().size());
            assertFalse(response.getHasGrandFinalReset());

            // All matches should be MAIN bracket type
            assertTrue(response.getMatches().stream()
                    .allMatch(m -> m.getBracketType() == BracketType.MAIN));

            // No bye matches (4 is a power of 2)
            assertTrue(response.getMatches().stream()
                    .noneMatch(m -> Boolean.TRUE.equals(m.getIsBye())));

            // Verify all R1 matches have both players
            List<Match> round1 = response.getMatches().stream()
                    .filter(m -> m.getRoundNumber() == 1)
                    .collect(Collectors.toList());
            assertEquals(2, round1.size());
            assertTrue(round1.stream().allMatch(m -> m.getPlayer1() != null && m.getPlayer2() != null));

            // Verify advancement wiring: R1 matches point to the R2 match
            List<Match> round2 = response.getMatches().stream()
                    .filter(m -> m.getRoundNumber() == 2)
                    .collect(Collectors.toList());
            assertEquals(1, round2.size());
            assertTrue(round1.stream().allMatch(m -> m.getNextMatchWinner() != null));

            verify(matchRepository).saveAll(any());
        }

        @Test
        @DisplayName("5 players — 3 byes needed, bracket size 8")
        void singleElim_5Players_WithByes() {
            BracketResponse response = generateBracket(5);

            assertEquals(BracketFormat.SINGLE_ELIMINATION.name(), response.getFormat());
            // 5 players: next power of 2 is 8, so 4 matches in R1, 2 in R2, 1 in R3 = 7 total
            assertEquals(7, response.getMatches().size());

            // Exactly 3 bye matches (8 - 5 = 3)
            long byeCount = response.getMatches().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsBye()))
                    .count();
            assertEquals(3, byeCount);

            // Bye matches should only have player1 set (no player2)
            response.getMatches().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsBye()))
                    .forEach(m -> {
                        assertNotNull(m.getPlayer1());
                        assertNull(m.getPlayer2());
                    });

            // autoAdvanceBye should have been called for each bye match
            verify(matchService, times(3)).autoAdvanceBye(any());
        }

        @Test
        @DisplayName("8 players — no byes, 7 total matches")
        void singleElim_8Players_NoByes() {
            BracketResponse response = generateBracket(8);

            assertEquals(BracketFormat.SINGLE_ELIMINATION.name(), response.getFormat());
            // 8 players: 4 + 2 + 1 = 7 matches
            assertEquals(7, response.getMatches().size());

            // No byes
            assertTrue(response.getMatches().stream()
                    .noneMatch(m -> Boolean.TRUE.equals(m.getIsBye())));

            // R1 has 4 matches, R2 has 2, R3 has 1
            assertEquals(4, response.getMatches().stream().filter(m -> m.getRoundNumber() == 1).count());
            assertEquals(2, response.getMatches().stream().filter(m -> m.getRoundNumber() == 2).count());
            assertEquals(1, response.getMatches().stream().filter(m -> m.getRoundNumber() == 3).count());

            // Verify full advancement chain: every non-final match has a nextMatchWinner
            response.getMatches().stream()
                    .filter(m -> m.getRoundNumber() < 3)
                    .forEach(m -> assertNotNull(m.getNextMatchWinner(),
                            "Match R" + m.getRoundNumber() + "." + m.getMatchNumber() + " should have nextMatchWinner"));
        }

        @Test
        @DisplayName("Race-to values are applied correctly from config")
        void singleElim_RaceToConfig_Applied() {
            List<PlayerRegistration> players = createPlayers(4);
            when(ownershipChecker.verifyAndLoad(tournamentId, adminId)).thenReturn(tournament);
            when(playerRepo.findByTournamentAndStatus(tournament, PlayerStatus.CONFIRMED)).thenReturn(players);

            GenerateBracketRequest request = new GenerateBracketRequest();
            request.setSeedingMode("manual");
            // R1 race-to-5, R2 race-to-7
            request.setRaceToConfig(java.util.Map.of(1, 5, 2, 7));

            BracketResponse response = bracketGeneratorService.generateBracket(tournamentId, adminId, request);

            response.getMatches().stream()
                    .filter(m -> m.getRoundNumber() == 1)
                    .forEach(m -> assertEquals(5, m.getRaceTo()));
            response.getMatches().stream()
                    .filter(m -> m.getRoundNumber() == 2)
                    .forEach(m -> assertEquals(7, m.getRaceTo()));
        }
    }

    // =========================================================================
    // DOUBLE ELIMINATION TESTS
    // =========================================================================

    @Nested
    @DisplayName("Double Elimination Bracket Tests")
    class DoubleEliminationTests {

        @Test
        @DisplayName("10 players — double elim, has GF and GF Reset")
        void doubleElim_10Players() {
            BracketResponse response = generateBracket(10);

            assertEquals(BracketFormat.DOUBLE_ELIMINATION.name(), response.getFormat());
            assertTrue(response.getHasGrandFinalReset());

            List<Match> matches = response.getMatches();
            assertTrue(matches.size() > 10, "Should have many more matches than players");

            // Should have winners, losers, GF, and GF Reset
            assertTrue(matches.stream().anyMatch(m -> m.getBracketType() == BracketType.MAIN));
            assertTrue(matches.stream().anyMatch(m -> m.getBracketType() == BracketType.LOSERS));
            assertTrue(matches.stream().anyMatch(m -> m.getBracketType() == BracketType.FINALS));
            assertTrue(matches.stream().anyMatch(m -> m.getBracketType() == BracketType.FINALS_RESET));

            // GF Reset should exist
            long gfResetCount = matches.stream()
                    .filter(m -> m.getBracketType() == BracketType.FINALS_RESET)
                    .count();
            assertEquals(1, gfResetCount);

            // Grand Final should have exactly one match
            long gfCount = matches.stream()
                    .filter(m -> m.getBracketType() == BracketType.FINALS)
                    .count();
            assertEquals(1, gfCount);
        }

        @Test
        @DisplayName("16 players — double elim, correct match counts")
        void doubleElim_16Players() {
            BracketResponse response = generateBracket(16);

            assertEquals(BracketFormat.DOUBLE_ELIMINATION.name(), response.getFormat());

            // 16 players: WB=15, LB=11, GF+GFR=2 = 28 total
            assertEquals(28, response.getMatches().size(),
                    "16-player double elim should have 28 matches, got: " + response.getMatches().size());

            // Winners bracket should have 4 rounds
            long wbRounds = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.MAIN)
                    .mapToInt(Match::getRoundNumber)
                    .distinct()
                    .count();
            assertEquals(4, wbRounds, "16-player WB should have 4 rounds");
        }

        @Test
        @DisplayName("32 players — double elim, correct structure")
        void doubleElim_32Players() {
            BracketResponse response = generateBracket(32);

            assertEquals(BracketFormat.DOUBLE_ELIMINATION.name(), response.getFormat());

            // 32 players: WB=31, LB=23, GF+GFR=2 = 56 total
            assertEquals(56, response.getMatches().size(),
                    "32-player double elim should have 56 matches, got: " + response.getMatches().size());

            // Winners bracket should have 5 rounds
            long wbRounds = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.MAIN)
                    .mapToInt(Match::getRoundNumber)
                    .distinct()
                    .count();
            assertEquals(5, wbRounds, "32-player WB should have 5 rounds");
        }

        @Test
        @DisplayName("Grand Final wiring: WB champ and LB champ both feed into GF")
        void doubleElim_GrandFinalWiring() {
            BracketResponse response = generateBracket(10);

            Match gf = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.FINALS)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Grand Final not found"));

            Match gfReset = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.FINALS_RESET)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("Grand Final Reset not found"));

            // GF should point to GF Reset as nextMatchWinner
            assertNotNull(gf.getNextMatchWinner(), "GF should have nextMatchWinner (GF Reset)");
            assertEquals(gfReset.getPositionLabel(), gf.getNextMatchWinner().getPositionLabel());

            // The last WB match should feed into GF
            Match wbChampMatch = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.MAIN)
                    .max(java.util.Comparator.comparingInt(Match::getRoundNumber))
                    .orElseThrow();
            assertNotNull(wbChampMatch.getNextMatchWinner(), "WB champ match should feed into GF");

            // The last LB match should feed into GF
            Match lbChampMatch = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.LOSERS)
                    .max(java.util.Comparator.comparingInt(Match::getRoundNumber))
                    .orElseThrow();
            assertNotNull(lbChampMatch.getNextMatchWinner(), "LB champ match should feed into GF");
        }

        @Test
        @DisplayName("Losers bracket wiring: WB losers drop into LB")
        void doubleElim_LosersDropdownWiring() {
            BracketResponse response = generateBracket(16);

            // Every WB match except the final should have a nextMatchLoser pointing to LB
            List<Match> wbNonFinal = response.getMatches().stream()
                    .filter(m -> m.getBracketType() == BracketType.MAIN)
                    .filter(m -> m.getNextMatchWinner() != null) // not the WB final
                    .collect(Collectors.toList());

            // Most WB matches should have a loser destination (except byes)
            long wbWithLoserDest = wbNonFinal.stream()
                    .filter(m -> !Boolean.TRUE.equals(m.getIsBye()))
                    .filter(m -> m.getNextMatchLoser() != null)
                    .count();

            assertTrue(wbWithLoserDest > 0,
                    "Some WB matches should have nextMatchLoser pointing to LB");
        }

        @Test
        @DisplayName("Format auto-selection: >= 10 players → double elimination")
        void formatSelection_10OrMore_DoubleElim() {
            BracketResponse response = generateBracket(10);
            assertEquals(BracketFormat.DOUBLE_ELIMINATION.name(), response.getFormat());
        }

        @Test
        @DisplayName("Format auto-selection: < 10 players → single elimination")
        void formatSelection_Under10_SingleElim() {
            BracketResponse response = generateBracket(8);
            assertEquals(BracketFormat.SINGLE_ELIMINATION.name(), response.getFormat());
        }
    }

    // =========================================================================
    // BYE AUTO-ADVANCEMENT TESTS
    // =========================================================================

    @Nested
    @DisplayName("Bye Auto-Advancement Tests")
    class ByeTests {

        @Test
        @DisplayName("Bye matches trigger autoAdvanceBye for each bye")
        void byeMatches_AutoAdvanceCalled() {
            // 6 players: next power of 2 is 8, so 2 byes
            BracketResponse response = generateBracket(6);

            long byeCount = response.getMatches().stream()
                    .filter(m -> Boolean.TRUE.equals(m.getIsBye()))
                    .count();
            assertEquals(2, byeCount);

            // autoAdvanceBye should be called once per bye match
            verify(matchService, times(2)).autoAdvanceBye(any());
        }

        @Test
        @DisplayName("No byes for power-of-2 player counts")
        void noByes_PowerOf2Players() {
            // 4, 8 are powers of 2 — no byes
            for (int count : new int[]{4, 8}) {
                BracketResponse response = generateBracket(count);
                long byeCount = response.getMatches().stream()
                        .filter(m -> Boolean.TRUE.equals(m.getIsBye()))
                        .count();
                assertEquals(0, byeCount, "No byes expected for " + count + " players");
            }
        }
    }

    // =========================================================================
    // GENERAL TESTS
    // =========================================================================

    @Test
    @DisplayName("Minimum player count: 2 players should work")
    void minimumPlayers_2_Success() {
        BracketResponse response = generateBracket(2);
        assertNotNull(response);
        assertEquals(1, response.getMatches().size());
    }

    @Test
    @DisplayName("Less than 2 players throws IllegalStateException")
    void tooFewPlayers_ThrowsException() {
        List<PlayerRegistration> players = createPlayers(1);
        when(ownershipChecker.verifyAndLoad(tournamentId, adminId)).thenReturn(tournament);
        when(playerRepo.findByTournamentAndStatus(tournament, PlayerStatus.CONFIRMED)).thenReturn(players);

        GenerateBracketRequest request = new GenerateBracketRequest();
        request.setSeedingMode("manual");

        assertThrows(IllegalStateException.class, () ->
                bracketGeneratorService.generateBracket(tournamentId, adminId, request));
    }

    @Test
    @DisplayName("WebSocket broadcast is called after bracket generation")
    void webSocketBroadcast_CalledAfterGeneration() {
        generateBracket(4);
        verify(wsService).broadcastBracketUpdate(tournamentId);
    }

    @Test
    @DisplayName("Tournament status is set to ACTIVE after bracket generation")
    void tournamentStatus_SetToActive() {
        generateBracket(4);
        assertEquals(com.pooltournament.enums.TournamentStatus.ACTIVE, tournament.getStatus());
    }
}
