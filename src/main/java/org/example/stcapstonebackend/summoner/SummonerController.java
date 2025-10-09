package org.example.stcapstonebackend.summoner;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.client.dto.MatchDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@RestController
@RequestMapping("/summoner")
@RequiredArgsConstructor
public class SummonerController {
    private final SummonerService summonerService;

    @GetMapping("/account")
    public ResponseEntity<SummonerSearchResponseDto> searchSummoner(@Valid @RequestParam String fullName) {
        SummonerSearchResponseDto summoner = summonerService.searchSummoner(fullName);
        return ResponseEntity.status(HttpStatus.OK)
            .body(summoner);
    }

    @GetMapping("/test")
    public ResponseEntity<MatchDto> getMatch(@RequestParam String puuid){
        Mono<List<String>> matchIds = summonerService.getMatchlist(puuid);
        List<String> matchIdsBlock = matchIds.block();
        MatchDto match = summonerService.getMatch(matchIdsBlock.get(0)).block();
        return ResponseEntity.status(HttpStatus.OK)
            .body(match);
    }
}
