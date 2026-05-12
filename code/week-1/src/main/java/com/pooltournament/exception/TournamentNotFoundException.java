package com.pooltournament.exception;

import java.util.UUID;

public class TournamentNotFoundException extends RuntimeException {
    public TournamentNotFoundException(UUID id) {
        super("Tournament not found with ID: " + id);
    }

    public TournamentNotFoundException(String message) {
        super(message);
    }
}
