package org.example.stcapstonebackend.summoner;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.client.dto.RiotAccountDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchRequestDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/summoner")
@RequiredArgsConstructor
public class SummonerController {
    private final SummonerService summonerService;

//    @GetMapping
//    public ResponseEntity<SummonerSearchResponseDto> searchSummoner(){
//        summonerService.getPuuidByName();
//
//    }

    @GetMapping
    public String getPuuidByName(@RequestParam String fullName){
        String puuid = summonerService.getPuuid(fullName);
        return puuid;
    }
}
