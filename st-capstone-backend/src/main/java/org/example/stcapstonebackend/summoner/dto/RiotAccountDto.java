package org.example.stcapstonebackend.summoner.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

public record RiotAccountDto(
        String puuid,
        String gameName,
        String tagLine
) {
}
