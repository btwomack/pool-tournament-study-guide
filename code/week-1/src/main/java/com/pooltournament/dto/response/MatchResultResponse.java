package com.pooltournament.dto.response;

import com.pooltournament.entity.Match;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class MatchResultResponse {
    private Match match;
    private Boolean bracketReset;
}
