package org.example.stcapstonebackend.summoner.dto;

import java.util.List;

public record SummonerSearchResponseDto(
        String nickname,
        String tag,
        String soloTier,
        String soloDivision,
        int soloPoints,
        int soloWins,
        int soloLoses,
        String flexTier,
        String flexDivision,
        String flexPoints,
        String flexWins,
        String flexLoses,
        List<String> recentMatchIds
) {
}

