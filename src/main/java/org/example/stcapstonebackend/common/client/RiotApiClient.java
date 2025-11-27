package org.example.stcapstonebackend.common.client;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.client.dto.LeagueEntryDto;
import org.example.stcapstonebackend.common.client.dto.MatchDto;
import org.example.stcapstonebackend.common.client.dto.RiotAccountDto;
import org.springframework.core.ParameterizedTypeReference;
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

    // puuid로 매치 ID 리스트를 가져오는 메서드
    public Mono<List<String>> fetchMatchIdsByPuuid(String puuid, int count) {
        return riotAsiaWebClient.get()
                .uri(uriBuilder -> uriBuilder
                        .path("/lol/match/v5/matches/by-puuid/{puuid}/ids")
                        .queryParam("count", count)
                        .build(puuid))
                .retrieve()
                .bodyToMono(new ParameterizedTypeReference<List<String>>() {});
    }

    // 기본 count 값을 10으로 설정한 오버로드 메서드
    public Mono<List<String>> fetchMatchIdsByPuuid(String puuid) {
        return fetchMatchIdsByPuuid(puuid, 10);
    }

    public Mono<MatchDto> fetchMatchByMatchId(String matchId) {
        return riotAsiaWebClient.get()
                .uri("/lol/match/v5/matches/{matchId}", matchId)
                .retrieve()
                .bodyToMono(MatchDto.class);
    }
}