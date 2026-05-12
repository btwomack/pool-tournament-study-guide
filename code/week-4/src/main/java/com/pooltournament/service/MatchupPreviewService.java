package com.pooltournament.service;

import com.pooltournament.entity.Match;
import com.pooltournament.entity.PlayerRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class MatchupPreviewService {

    /**
     * In a real application, this would call an LLM (OpenAI, Gemini, etc.)
     * For this project, we'll use a template-based approach to generate previews.
     */
    public Map<String, String> generateMatchupPreview(Match match) {
        PlayerRegistration p1 = match.getPlayer1();
        PlayerRegistration p2 = match.getPlayer2();

        if (p1 == null || p2 == null) {
            return Map.of("preview", "Matchup pending players.");
        }

        String preview = String.format("A high-stakes matchup between %s (Seed #%d) and %s (Seed #%d). " +
                "With a race to %d, both players need to stay focused. " +
                "%s has %d losses, while %s has %d losses. Expect a thrilling contest!",
                p1.getPlayerName(), p1.getSeedNumber(), p2.getPlayerName(), p2.getSeedNumber(),
                match.getRaceTo(), p1.getPlayerName(), p1.getLossesCount(), p2.getPlayerName(), p2.getLossesCount());

        Map<String, String> response = new HashMap<>();
        response.put("matchId", match.getId().toString());
        response.put("preview", preview);
        response.put("generatedAt", String.valueOf(System.currentTimeMillis()));
        return response;
    }

    public Map<String, String> generateMatchRecap(Match match) {
        if (match.getWinner() == null) {
            return Map.of("recap", "Match result not recorded yet.");
        }

        String recap = String.format("%s secured a hard-fought victory over %s with a score of %d-%d. " +
                "The win moves %s further in the bracket, while %s faces a tough path ahead.",
                match.getWinner().getPlayerName(), match.getLoser().getPlayerName(),
                match.getPlayer1Score(), match.getPlayer2Score(),
                match.getWinner().getPlayerName(), match.getLoser().getPlayerName());

        Map<String, String> response = new HashMap<>();
        response.put("matchId", match.getId().toString());
        response.put("recap", recap);
        return response;
    }
}
