package com.pooltournament.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class DropPlayerRequest {
    @NotBlank
    private String reason; // "dropped" or "disqualified"
}
