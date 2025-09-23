package org.example.stcapstonebackend.common.client;

import org.example.stcapstonebackend.common.client.dto.RiotAccountDto;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

@Component
public class RiotApiClient {

    private final WebClient webClient;

    public RiotApiClient(WebClient webClient) {
        this.webClient = webClient;
    }

    public Mono<RiotAccountDto> fetchAccountByRiotId(String gameName, String tagLine) {
        return webClient.get()
                .uri("/riot/account/v1/accounts/by-riot-id/{gameName}/{tagLine}", gameName, tagLine)
                .retrieve()
                .bodyToMono(RiotAccountDto.class);
    }
}