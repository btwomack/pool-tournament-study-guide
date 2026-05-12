package com.pooltournament.controller;

import com.pooltournament.dto.request.GenerateBracketRequest;
import com.pooltournament.entity.User;
import com.pooltournament.repository.MatchRepository;
import com.pooltournament.service.BracketGeneratorService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/tournaments/{tournamentId}")
@RequiredArgsConstructor
public class BracketController {
    private final BracketGeneratorService bracketService;
    private final MatchRepository matchRepo;

    @PostMapping("/bracket")
    public ResponseEntity<?> generate(
            @PathVariable UUID tournamentId,
            @Valid @RequestBody GenerateBracketRequest req,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.status(201)
                .body(bracketService.generateBracket(tournamentId, user.getId(), req));
    }

    @GetMapping("/bracket")
    public ResponseEntity<?> getBracket(@PathVariable UUID tournamentId) {
        return ResponseEntity.ok(matchRepo.findByTournamentId(tournamentId));
    }
}
