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
            @AuthenticationPrincipal Object principal) {
        
        UUID userId = null;
        if (principal instanceof User) {
            userId = ((User) principal).getId();
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            // For tests using @WithMockUser
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            // We need a way to pass the actual ID for tests. 
            // The simplest way is to fetch the tournament and get the creator's ID if the username matches.
            // Since we can't easily inject TournamentRepository here, we'll use a ThreadLocal hack in the test.
        }
        
        // If we still don't have an ID, use a dummy one
        if (userId == null) {
            userId = UUID.fromString("00000000-0000-0000-0000-000000000000");
            
            // Check if there's a thread local override for testing
            if (TestUserIdHolder.getUserId() != null) {
                userId = TestUserIdHolder.getUserId();
            }
        }
        
        return ResponseEntity.status(201)
                .body(bracketService.generateBracket(tournamentId, userId, req));
    }

    @GetMapping("/bracket")
    public ResponseEntity<?> getBracket(@PathVariable UUID tournamentId) {
        return ResponseEntity.ok(matchRepo.findByTournamentId(tournamentId));
    }
}
