package org.example.stcapstonebackend.summoner;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.client.RiotApiClient;
import org.example.stcapstonebackend.common.client.dto.LeagueEntryDto;
import org.example.stcapstonebackend.common.client.dto.MatchDto;
import org.example.stcapstonebackend.common.client.dto.RiotAccountDto;
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

    // 닉네임#테그 형식의 문자열을 받아서 계정정보(RiotAccountDto) 반환
    public RiotAccountDto getRiotAccount(String fullname) {
        String[] ids=  fullname.trim().split("#");
        String gameName = ids[0];
        String tagLine = ids[1];
        RiotAccountDto account = riotApiClient.fetchAccountByRiotId(gameName, tagLine)
                .block();
        return account;
    }
    // puuid를 받아서 해당 소환사의 리그 정보 리스트(LeagueEntryDto) 반환
    public List<LeagueEntryDto> getLeagueEntry(String puuid){
        Flux<LeagueEntryDto> riotResponse = riotApiClient.fetchLeagueEntryByPuuid(puuid);
        Mono<List<LeagueEntryDto>> dtoList = riotResponse.collectList();
        return dtoList.block();
    }

    // puuid를 받아서 해당 소환사의 최근 매치 ID 리스트 반환
    public Mono<List<String>> getMatchlist(String puuid){
        return riotApiClient.fetchMatchIdsByPuuid(puuid);
    }

    // 매치 ID를 받아서 해당 매치의 상세 정보(MatchDto) 반환
    public Mono<MatchDto> getMatch(String matchId){
        return riotApiClient.fetchMatchByMatchId(matchId);
    }

//    public Flux<>

    // 닉네임#태그 형식의 문자열을 받아서 소환사 검색 결과(SummonerSearchResponseDto) 반환
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
                // 솔로 랭크 정보가 있으면 값을 채우고, 없으면(Optional이 비어있으면) 기본값(Unranked, 0) 사용
                .soloTier(soloRankOptional.map(LeagueEntryDto::tier).orElse("UNRANKED"))
                .soloDivision(soloRankOptional.map(LeagueEntryDto::rank).orElse(""))
                .soloPoints(soloRankOptional.map(LeagueEntryDto::leaguePoints).orElse(0))
                .soloWins(soloRankOptional.map(LeagueEntryDto::wins).orElse(0))
                .soloLoses(soloRankOptional.map(LeagueEntryDto::losses).orElse(0))
                // 자유 랭크 정보가 있으면 값을 채우고, 없으면 기본값 사용
                .flexTier(flexRankOptional.map(LeagueEntryDto::tier).orElse("UNRANKED"))
                .flexDivision(flexRankOptional.map(LeagueEntryDto::rank).orElse(""))
                .flexPoints(flexRankOptional.map(LeagueEntryDto::leaguePoints).orElse(0))
                .flexWins(flexRankOptional.map(LeagueEntryDto::wins).orElse(0))
                .flexLoses(flexRankOptional.map(LeagueEntryDto::losses).orElse(0))
                .build();
    }
}
