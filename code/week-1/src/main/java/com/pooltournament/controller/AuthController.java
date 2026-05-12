package com.pooltournament.controller;

import com.pooltournament.dto.request.LoginRequest;
import com.pooltournament.dto.request.RegisterRequest;
import com.pooltournament.entity.User;
import com.pooltournament.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        User user = authService.register(req.getEmail(), req.getPassword());
        return ResponseEntity.status(201).body(Map.of(
                "id", user.getId(),
                "email", user.getEmail()));
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@Valid @RequestBody LoginRequest req) {
        String token = authService.login(req.getEmail(), req.getPassword());
        return ResponseEntity.ok(Map.of("token", token));
    }
}
