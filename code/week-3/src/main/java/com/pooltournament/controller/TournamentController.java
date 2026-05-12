package com.pooltournament.controller;

import com.pooltournament.dto.request.CreateTournamentRequest;
import com.pooltournament.entity.Tournament;
import com.pooltournament.entity.User;
import com.pooltournament.service.TournamentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments")
@RequiredArgsConstructor
public class TournamentController {
    private final TournamentService tournamentService;

    @PostMapping
    public ResponseEntity<Tournament> create(
            @Valid @RequestBody CreateTournamentRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201).body(tournamentService.createTournament(req, user.getId()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<Tournament> getById(@PathVariable UUID id) {
        return ResponseEntity.ok(tournamentService.getById(id));
    }

    @GetMapping("/join/{joinCode}")
    public ResponseEntity<Tournament> getByJoinCode(@PathVariable String joinCode) {
        return ResponseEntity.ok(tournamentService.getByJoinCode(joinCode));
    }
}
