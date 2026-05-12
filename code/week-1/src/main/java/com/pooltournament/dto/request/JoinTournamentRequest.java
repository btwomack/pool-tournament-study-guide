package com.pooltournament.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class JoinTournamentRequest {
    @NotBlank
    @Size(max = 50)
    @Pattern(regexp = "^[a-zA-Z0-9 .\\-]+$")
    private String playerName;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$")
    private String phone;

    private String paymentIntentId;
}
