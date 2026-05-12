package com.pooltournament.config;

import com.pooltournament.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtAuthenticationFilter jwtFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm ->
                        sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/auth/**").permitAll()
                        .requestMatchers("/api/tournaments/join/**").permitAll()
                        .requestMatchers("/api/webhooks/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/tournaments/*/bracket").permitAll()
                        .requestMatchers("/ws/**").permitAll()
                        // Admin-only endpoints
                        .requestMatchers(HttpMethod.POST, "/api/tournaments").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/api/tournaments/*/bracket").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/tournaments/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/tournaments/**").hasRole("ADMIN")
                        // Everything else requires authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
