package org.example.stcapstonebackend.common.client;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.summoner.dto.LeagueEntryDto;
import org.example.stcapstonebackend.summoner.dto.RiotAccountDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
@RequiredArgsConstructor
public class RiotApiClient {
    private final WebClient riotKrWebClient;
    private final WebClient riotAsiaWebClient;

    public Mono<RiotAccountDto> fetchAccountByRiotId(String gameName, String tagLine) {
        return riotAsiaWebClient.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                .retrieve()
                .bodyToMono(RiotAccountDto.class);
    }

    public Flux<LeagueEntryDto> fetchLeagueEntryByPuuid(String encryptedPUUID) {
        return riotKrWebClient.get()
                .uri("/lol/league/v4/entries/by-puuid/{encryptedPUUID}", encryptedPUUID)
                .retrieve()
                .bodyToFlux(LeagueEntryDto.class);
    }
}