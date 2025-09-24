package org.example.stcapstonebackend.summoner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.client.RiotApiClient;
import org.example.stcapstonebackend.summoner.dto.LeagueEntryDto;
import org.example.stcapstonebackend.summoner.dto.RiotAccountDto;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Optional;

@Service
@Slf4j
@RequiredArgsConstructor
public class SummonerService {
    private final RiotApiClient riotApiClient;
    public RiotAccountDto getRiotAccount(String fullname) {
        String[] ids=  fullname.split("#");
        String gameName = ids[0];
        String tagLine = ids[1];
        RiotAccountDto account = riotApiClient.fetchAccountByRiotId(gameName, tagLine)
                .block();
        return account;
    }
    public List<LeagueEntryDto> getLeagueEntry(String puuid){
        Flux<LeagueEntryDto> riotResponse = riotApiClient.fetchLeagueEntryByPuuid(puuid);
        Mono<List<LeagueEntryDto>> dtoList = riotResponse.collectList();
        return dtoList.block();
    }

//    public List<MatchDetailDto> getMatchlist(String puuid){
//
//    }

    public SummonerSearchResponseDto searchSummoner(String fullname) {
        // 닉네임과 태그로 puuid, 게임 닉네임 등 기본 정보를 조회
        RiotAccountDto riotAccountResponse = getRiotAccount(fullname);
        String puuid = riotAccountResponse.puuid();

        // puuid로 솔로 랭크와 자유 랭크 정보를 조회
        List<LeagueEntryDto> riotLeagueEntryResponse = getLeagueEntry(puuid);

        // 랭크 정보 리스트에서 솔로 랭크(RANKED_SOLO_5x5) 정보만 필터링
        Optional<LeagueEntryDto> soloRankOptional = riotLeagueEntryResponse.stream()
                .filter(entry -> "RANKED_SOLO_5x5".equals(entry.queueType()))
                .findFirst();

        // 랭크 정보 리스트에서 자유 랭크(RANKED_FLEX_SR) 정보만 필터링
        Optional<LeagueEntryDto> flexRankOptional = riotLeagueEntryResponse.stream()
                .filter(entry -> "RANKED_FLEX_SR".equals(entry.queueType()))
                .findFirst();

        // 조회된 모든 정보를 바탕으로 최종 응답 DTO를 생성
        return SummonerSearchResponseDto.builder()
                .nickname(riotAccountResponse.gameName())
                .tagline(riotAccountResponse.tagLine())
                .puuid(puuid)
                // 솔로 랭크 정보가 있으면 값을 채우고, 없으면(Optional이 비어있으면) 기본값(Unranked, 0)을 사용합니다.
                .soloTier(soloRankOptional.map(LeagueEntryDto::tier).orElse("UNRANKED"))
                .soloDivision(soloRankOptional.map(LeagueEntryDto::rank).orElse(""))
                .soloPoints(soloRankOptional.map(LeagueEntryDto::leaguePoints).orElse(0))
                .soloWins(soloRankOptional.map(LeagueEntryDto::wins).orElse(0))
                .soloLoses(soloRankOptional.map(LeagueEntryDto::losses).orElse(0))
                // 자유 랭크 정보가 있으면 값을 채우고, 없으면 기본값을 사용합니다.
                .flexTier(flexRankOptional.map(LeagueEntryDto::tier).orElse("UNRANKED"))
                .flexDivision(flexRankOptional.map(LeagueEntryDto::rank).orElse(""))
                .flexPoints(flexRankOptional.map(LeagueEntryDto::leaguePoints).orElse(0))
                .flexWins(flexRankOptional.map(LeagueEntryDto::wins).orElse(0))
                .flexLoses(flexRankOptional.map(LeagueEntryDto::losses).orElse(0))
                .build();
//    public SummonerSearchResponseDto searchSummoner(String fullname){
//        RiotAccountDto riotAccountResponse = getRiotAccount(fullname);
//        String puuid = riotAccountResponse.puuid();
//        List<LeagueEntryDto> riotLegueEntryResponse = getLeagueEntry(puuid);


//        List<MatchDetailDto> matches = getMatchlist(puuid);
//        //SummonerSearchResponseDto 생성 코드
//        return SummonerSearchResponseDto;
    }
}
