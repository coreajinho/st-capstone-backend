package org.example.stcapstonebackend.summoner;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.summoner.dto.RiotAccountDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/summoner")
@RequiredArgsConstructor
public class SummonerController {
    private final SummonerService summonerService;

//    @GetMapping
//    public ResponseEntity<SummonerSearchResponseDto> searchSummoner(@RequestParam String fullName){
//        SummonerSearchResponseDto response = summonerService.searchSummoner(fullName);
//        return ResponseEntity.status(HttpStatus.OK)
//                .body(response);
//    }
    @GetMapping("/account")
    public ResponseEntity<SummonerSearchResponseDto> searchSummoner(@RequestParam String fullName) {
        SummonerSearchResponseDto summoner = summonerService.searchSummoner(fullName);
        return ResponseEntity.status(HttpStatus.OK)
            .body(summoner);
    }
}
