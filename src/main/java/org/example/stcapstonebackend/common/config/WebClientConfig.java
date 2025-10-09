package org.example.stcapstonebackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Bean
    public WebClient riotKrWebClient() {
        return createRiotWebClient("kr");
    }

    @Bean
    public WebClient riotAsiaWebClient() {
        return createRiotWebClient("asia");
    }

    private WebClient createRiotWebClient(String region) {
        return WebClient.builder()
                .baseUrl(String.format("https://%s.api.riotgames.com", region))
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }
}