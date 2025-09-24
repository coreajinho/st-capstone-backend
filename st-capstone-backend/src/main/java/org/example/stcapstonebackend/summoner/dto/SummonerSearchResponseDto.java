package org.example.stcapstonebackend.summoner.dto;

import lombok.Builder;

import java.util.List;

@Builder
public record SummonerSearchResponseDto(
        String nickname,
        String tagline,
        String puuid,
        String soloTier,
        String soloDivision,
        int soloPoints,
        int soloWins,
        int soloLoses,
        String flexTier,
        String flexDivision,
        int flexPoints,
        int flexWins,
        int flexLoses
//        ,List<MatchDetailDto> recentMatchs
) {
}

