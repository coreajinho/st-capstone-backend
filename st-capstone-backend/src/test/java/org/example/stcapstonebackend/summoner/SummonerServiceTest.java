package org.example.stcapstonebackend.summoner;

import org.example.stcapstonebackend.common.client.RiotApiClient;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
class SummonerServiceTest {

    @InjectMocks
    private SummonerService summonerService;

    @Mock
    private RiotApiClient riotApiClient;

    @Test
    void getRiotAccount() {
    }

    @Test
    void getLeagueEntry() {
    }

    @Test // 이 메소드가 테스트 케이스임을 나타냅니다.
    void getMatchlist() {
//        // 1. 준비 (Arrange / Given)
//        String testPuuid = "KR_123456789";
//        Flux<String> mockResponse = Flux.just("matchId_1", "matchId_2", "matchId_3");
//
//        // riotApiClient.fetchMatchIdsByPuuid 메소드가 어떤 문자열(anyString())로 호출되든,
//        // 미리 준비한 mockResponse를 반환하도록 가짜 객체의 행동을 정의합니다.
//        when(riotApiClient.fetchMatchIdsByPuuid(anyString())).thenReturn(mockResponse);
//
//        // 2. 실행 (Act / When)
//        // 실제로 테스트하고 싶은 메소드를 호출합니다.
//        Flux<String> resultFlux = matchService.getMatchlist(testPuuid);
//
//        // 3. 검증 (Assert / Then)
//        // StepVerifier를 사용하여 Flux의 동작을 단계별로 검증합니다.
//        StepVerifier.create(resultFlux)
//                .expectNext("matchId_1") // 첫 번째로 "matchId_1"이 방출될 것을 기대
//                .expectNext("matchId_2") // 두 번째로 "matchId_2"가 방출될 것을 기대
//                .expectNext("matchId_3") // 세 번째로 "matchId_3"이 방출될 것을 기대
//                .verifyComplete();       // 모든 데이터가 방출되고 성공적으로 완료되었는지 검증
    }

    @Test
    void searchSummoner() {
    }
}