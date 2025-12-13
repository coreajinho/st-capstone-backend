package org.example.stcapstonebackend.common.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * Riot API 호출을 위한 WebClient 설정 클래스입니다.
 * 지역별(KR, ASIA) WebClient 빈을 생성하여 제공합니다.
 */
@Configuration
public class WebClientConfig {

    @Value("${riot.api.key}")
    private String apiKey;

    @Value("${riot.api.url.kr}")
    private String krBaseUrl;

    @Value("${riot.api.url.asia}")
    private String asiaBaseUrl;

    /**
     * 한국 지역(KR) Riot API 호출을 위한 WebClient 빈을 생성합니다.
     *
     * @return KR 지역 WebClient 인스턴스
     */
    @Bean
    public WebClient riotKrWebClient() {
        return createRiotWebClient(krBaseUrl);
    }

    /**
     * 아시아 지역(ASIA) Riot API 호출을 위한 WebClient 빈을 생성합니다.
     *
     * @return ASIA 지역 WebClient 인스턴스
     */
    @Bean
    public WebClient riotAsiaWebClient() {
        return createRiotWebClient(asiaBaseUrl);
    }

    /**
     * 지정된 Base URL과 API Key를 사용하여 WebClient를 생성합니다.
     *
     * @param baseUrl Riot API Base URL
     * @return 설정된 WebClient 인스턴스
     */
    private WebClient createRiotWebClient(String baseUrl) {
        return WebClient.builder()
                .baseUrl(baseUrl)
                .defaultHeader("X-Riot-Token", apiKey)
                .build();
    }
}