package com.pooltournament.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Entity
@Table(name = "venues")
@Data @Builder @NoArgsConstructor @AllArgsConstructor
public class Venue {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(name = "logo_url")
    private String logoUrl;

    @Column(name = "menu_url")
    private String menuUrl;

    @Column(name = "specials_text")
    private String specialsText;
}
