package org.example.stcapstonebackend.summoner;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.client.dto.MatchDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/summoner")
@RequiredArgsConstructor
public class SummonerController {
    private final SummonerService summonerService;

    // 닉네임#태그 형식의 문자열을 받아서 소환사 검색 결과(SummonerSearchResponseDto) 반환
    @GetMapping("/account")
    public ResponseEntity<SummonerSearchResponseDto> searchSummoner(@Valid @RequestParam String fullName) {
        SummonerSearchResponseDto summoner = summonerService.searchSummoner(fullName);
        return ResponseEntity.status(HttpStatus.OK)
            .body(summoner);
    }

    // puuid를 받아서 해당 소환사의 최근 매치 상세 정보(MatchDto) 반환
    @GetMapping("/matchs")
    public ResponseEntity<MatchDto> getMatch(@RequestParam String puuid){
        Mono<List<String>> matchIds = summonerService.getMatchlist(puuid);
        List<String> matchIdsBlock = matchIds.block();
        MatchDto match = summonerService.getMatch(matchIdsBlock.get(0)).block();
        return ResponseEntity.status(HttpStatus.OK)
            .body(match);
    }
}
