package org.example.stcapstonebackend.summoner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.client.RiotApiClient;
import org.example.stcapstonebackend.common.client.dto.RiotAccountDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchRequestDto;
import org.springframework.stereotype.Service;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummonerService {
    private final RiotApiClient riotApiClient;

    public String getPuuid(String fullname) {
        String[] ids=  fullname.split("#");
        String gameName = ids[0];
        String tagLine = ids[1];
        RiotAccountDto accountDto = riotApiClient.fetchAccountByRiotId(gameName, tagLine)
                .block();
        return accountDto.puuid();
    }
}
