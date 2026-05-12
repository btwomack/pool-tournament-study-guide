package com.pooltournament.service;

import com.pooltournament.entity.User;
import com.pooltournament.enums.UserRole;
import com.pooltournament.repository.UserRepository;
import com.pooltournament.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider tokenProvider;

    @Transactional
    public User register(String email, String password) {
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already registered");
        }
        User user = User.builder()
                .email(email)
                .passwordHash(passwordEncoder.encode(password))
                .role(UserRole.ADMIN)
                .build();
        return userRepository.save(user);
    }

    public String login(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));

        if (!passwordEncoder.matches(password, user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return tokenProvider.generateToken(
                user.getId(), user.getEmail(), user.getRole()
        );
    }
}
