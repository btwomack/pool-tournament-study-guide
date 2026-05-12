package com.pooltournament.service;

import org.springframework.stereotype.Service;
import java.util.UUID;

/**
 * Stub for WebSocketService. 
 * Real implementation with SimpMessagingTemplate will be added in Week 3.
 */
@Service
public class WebSocketService {
    public void broadcastBracketUpdate(UUID tournamentId) {
        // Placeholder for Week 3
    }

    public void broadcastMatchResult(UUID tournamentId, UUID matchId) {
        // Placeholder for Week 3
    }

    public void broadcastBracketReset(UUID tournamentId) {
        // Placeholder for Week 3
    }

    public void broadcastPlayerDrop(UUID tournamentId, UUID playerId) {
        // Placeholder for Week 3
    }
}
