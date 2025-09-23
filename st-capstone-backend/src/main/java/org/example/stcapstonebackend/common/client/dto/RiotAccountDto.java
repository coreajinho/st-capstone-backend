package org.example.stcapstonebackend.common.client.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@JsonIgnoreProperties(ignoreUnknown = true)
public record RiotAccountDto(
        String puuid,
        String gameName,
        String tagLine
) {
}
