package org.example.stcapstonebackend.findTeam;

import org.example.stcapstonebackend.findTeam.dto.WriterTierInfoResponse;
import org.example.stcapstonebackend.findTeam.mapper.FindTeamPostMapper;
import org.example.stcapstonebackend.summoner.SummonerService;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;

/**
 * FindTeamPostService의 작성자 티어 정보 조회 기능에 대한 단위 테스트입니다.
 */
@ExtendWith(MockitoExtension.class)
class FindTeamPostServiceWriterTierInfoTest {

    @Mock
    private FindTeamPostRepository findTeamPostRepository;

    @Mock
    private FindTeamPostMapper findTeamPostMapper;

    @Mock
    private SummonerService summonerService;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private FindTeamPostService findTeamPostService;

    private User testUser;

    @BeforeEach
    void setUp() {
        // given: 테스트용 사용자 생성
        testUser = User.builder()
                .id(1L)
                .username("testuser")
                .password("password")
                .riotName("TestPlayer")
                .riotTag("KR1")
                .build();
    }

    @Test
    @DisplayName("작성자 티어 정보 조회 - 성공 (Gold 2 유저)")
    void getWriterTierInfo_Success_GoldUser() {
        // given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(summonerService.searchSummoner("TestPlayer#KR1"))
                .willReturn(org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto.builder()
                        .nickname("TestPlayer")
                        .tagline("KR1")
                        .puuid("test-puuid")
                        .soloTier("GOLD")
                        .soloDivision("II")
                        .soloPoints(26)
                        .soloWins(100)
                        .soloLoses(50)
                        .flexTier("SILVER")
                        .flexDivision("I")
                        .flexPoints(45)
                        .flexWins(80)
                        .flexLoses(40)
                        .isRegisteredUser(true)
                        .debateWins(0)
                        .debateLosses(0)
                        .debateDraws(0)
                        .judgementSuccesses(0)
                        .judgementFailures(0)
                        .build());

        // when
        WriterTierInfoResponse response = findTeamPostService.getWriterTierInfo(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.summonerName()).isEqualTo("TestPlayer");
        assertThat(response.summonerTag()).isEqualTo("KR1");

        // 솔로랭크: Gold 2 26LP = 1200 + (4-2) × 100 + 26 = 1426
        assertThat(response.soloTier()).isEqualTo("GOLD");
        assertThat(response.soloDivision()).isEqualTo("II");
        assertThat(response.soloLp()).isEqualTo(26);
        assertThat(response.soloTierScore()).isEqualTo(1426);

        // 자유랭크: Silver 1 45LP = 800 + (4-1) × 100 + 45 = 1145
        assertThat(response.flexTier()).isEqualTo("SILVER");
        assertThat(response.flexDivision()).isEqualTo("I");
        assertThat(response.flexLp()).isEqualTo(45);
        assertThat(response.flexTierScore()).isEqualTo(1145);

        // Gold 티어 듀오 범위: Silver 4 ~ Platinum 1 (800~1999)
        assertThat(response.soloRankMinScore()).isEqualTo(800);
        assertThat(response.soloRankMaxScore()).isEqualTo(1999);

        // 자유랭크/기타모드 범위: Iron 4 ~ Diamond 1 (0~2799)
        assertThat(response.flexRankMinScore()).isEqualTo(0);
        assertThat(response.flexRankMaxScore()).isEqualTo(2799);
        assertThat(response.flexRankMasterPlusAllowed()).isTrue();
    }

    @Test
    @DisplayName("작성자 티어 정보 조회 - 실패 (사용자 없음)")
    void getWriterTierInfo_Fail_UserNotFound() {
        // given
        given(userRepository.findById(999L)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> findTeamPostService.getWriterTierInfo(999L))
                .isInstanceOf(UserNotFoundException.class)
                .hasMessageContaining("사용자를 찾을 수 없습니다");
    }

    @Test
    @DisplayName("작성자 티어 정보 조회 - 마스터 티어 (듀오 불가)")
    void getWriterTierInfo_MasterTier_NoDuo() {
        // given: 마스터 500LP 유저
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(summonerService.searchSummoner("TestPlayer#KR1"))
                .willReturn(org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto.builder()
                        .nickname("TestPlayer")
                        .tagline("KR1")
                        .puuid("test-puuid")
                        .soloTier("MASTER")
                        .soloDivision("I")
                        .soloPoints(500)
                        .soloWins(200)
                        .soloLoses(100)
                        .flexTier("UNRANKED")
                        .flexDivision("")
                        .flexPoints(0)
                        .flexWins(0)
                        .flexLoses(0)
                        .isRegisteredUser(true)
                        .debateWins(0)
                        .debateLosses(0)
                        .debateDraws(0)
                        .judgementSuccesses(0)
                        .judgementFailures(0)
                        .build());

        // when
        WriterTierInfoResponse response = findTeamPostService.getWriterTierInfo(1L);

        // then
        assertThat(response).isNotNull();
        // 마스터 티어는 솔로랭크 듀오 불가 (-1, -1)
        assertThat(response.soloRankMinScore()).isEqualTo(-1);
        assertThat(response.soloRankMaxScore()).isEqualTo(-1);

        // Master 500LP = 2800 + 500 = 3300
        assertThat(response.soloTierScore()).isEqualTo(3300);
    }

    @Test
    @DisplayName("작성자 티어 정보 조회 - 언랭 유저")
    void getWriterTierInfo_UnrankedUser() {
        // given: 언랭 유저 (리그 정보 없음)
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(summonerService.searchSummoner("TestPlayer#KR1"))
                .willReturn(org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto.builder()
                        .nickname("TestPlayer")
                        .tagline("KR1")
                        .puuid("test-puuid")
                        .soloTier("UNRANKED")
                        .soloDivision("")
                        .soloPoints(0)
                        .soloWins(0)
                        .soloLoses(0)
                        .flexTier("UNRANKED")
                        .flexDivision("")
                        .flexPoints(0)
                        .flexWins(0)
                        .flexLoses(0)
                        .isRegisteredUser(true)
                        .debateWins(0)
                        .debateLosses(0)
                        .debateDraws(0)
                        .judgementSuccesses(0)
                        .judgementFailures(0)
                        .build());

        // when
        WriterTierInfoResponse response = findTeamPostService.getWriterTierInfo(1L);

        // then
        assertThat(response).isNotNull();
        assertThat(response.soloTier()).isEqualTo("UNRANKED");
        assertThat(response.soloDivision()).isEmpty();
        assertThat(response.soloLp()).isEqualTo(0);
        assertThat(response.soloTierScore()).isEqualTo(0);

        // 언랭은 Iron 4 ~ Silver 1 범위 (0~1199)
        assertThat(response.soloRankMinScore()).isEqualTo(0);
        assertThat(response.soloRankMaxScore()).isEqualTo(1199);
    }

    @Test
    @DisplayName("작성자 티어 정보 조회 - Diamond 티어")
    void getWriterTierInfo_DiamondTier() {
        // given: Diamond 3 50LP 유저
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(summonerService.searchSummoner("TestPlayer#KR1"))
                .willReturn(org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto.builder()
                        .nickname("TestPlayer")
                        .tagline("KR1")
                        .puuid("test-puuid")
                        .soloTier("DIAMOND")
                        .soloDivision("III")
                        .soloPoints(50)
                        .soloWins(150)
                        .soloLoses(130)
                        .flexTier("UNRANKED")
                        .flexDivision("")
                        .flexPoints(0)
                        .flexWins(0)
                        .flexLoses(0)
                        .isRegisteredUser(true)
                        .debateWins(0)
                        .debateLosses(0)
                        .debateDraws(0)
                        .judgementSuccesses(0)
                        .judgementFailures(0)
                        .build());

        // when
        WriterTierInfoResponse response = findTeamPostService.getWriterTierInfo(1L);

        // then
        assertThat(response).isNotNull();
        // Diamond 3 50LP = 2400 + (4-3) × 100 + 50 = 2550
        assertThat(response.soloTierScore()).isEqualTo(2550);

        // Diamond 3 듀오 범위: Emerald 1 ~ Diamond 1 (2300~2799)
        assertThat(response.soloRankMinScore()).isEqualTo(2300);
        assertThat(response.soloRankMaxScore()).isEqualTo(2799);
    }
}

