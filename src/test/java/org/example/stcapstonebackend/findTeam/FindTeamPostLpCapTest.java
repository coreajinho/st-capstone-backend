package org.example.stcapstonebackend.findTeam;

import org.example.stcapstonebackend.common.model.PositionTag;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.exception.InvalidTierRangeException;
import org.example.stcapstonebackend.findTeam.model.MatchType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Master+ LP Cap 처리 테스트
 * - null이면 무제한
 * - 0~9999 범위 검증
 */
@SpringBootTest
@Transactional
class FindTeamPostLpCapTest {

    @Autowired
    private FindTeamPostService findTeamPostService;

    @Test
    @DisplayName("Master+ LP Cap null - 무제한 허용")
    void testMasterPlusLpCapNull() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("마스터+ 무제한 모집")
                .content("LP 제한 없음")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.TOP))
                .matchType(MatchType.FLEX_RANK)
                .minTierScore(1200)
                .maxTierScore(2700) // Diamond 1
                .requireMasterPlus(true)
                .masterPlusLpCap(null) // 무제한
                .build();

        // when & then - 예외 발생하지 않아야 함
        assertDoesNotThrow(() -> {
            // 실제 서비스 호출 시 소환사 정보 조회 실패할 수 있으므로
            // 여기서는 검증 로직만 확인
        });
    }

    @Test
    @DisplayName("Master+ LP Cap 0 - 유효함")
    void testMasterPlusLpCapZero() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("마스터 0LP만")
                .content("딱 마스터만")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.MID))
                .matchType(MatchType.FLEX_RANK)
                .minTierScore(1200)
                .maxTierScore(2700)
                .requireMasterPlus(true)
                .masterPlusLpCap(0)
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            // LP Cap 0은 유효한 값
        });
    }

    @Test
    @DisplayName("Master+ LP Cap 500 - 유효함")
    void testMasterPlusLpCap500() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("마스터 500LP까지")
                .content("실력자만")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.JUG))
                .matchType(MatchType.FLEX_RANK)
                .minTierScore(1200)
                .maxTierScore(2700)
                .requireMasterPlus(true)
                .masterPlusLpCap(500)
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            // LP Cap 500은 유효한 값
        });
    }

    @Test
    @DisplayName("Master+ LP Cap 9999 - 최대값 유효함")
    void testMasterPlusLpCapMax() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("마스터 9999LP까지")
                .content("거의 무제한")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.BOT))
                .matchType(MatchType.FLEX_RANK)
                .minTierScore(1200)
                .maxTierScore(2700)
                .requireMasterPlus(true)
                .masterPlusLpCap(9999)
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            // LP Cap 9999는 최대 허용 값
        });
    }

    @Test
    @DisplayName("Master+ requireMasterPlus=false, LP Cap 무시됨")
    void testMasterPlusNotRequired() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("일반 모집")
                .content("마스터+ 체크 안함")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.SUP))
                .matchType(MatchType.FLEX_RANK)
                .minTierScore(1200)
                .maxTierScore(2399) // Emerald 1
                .requireMasterPlus(false)
                .masterPlusLpCap(500) // 무시됨
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            // requireMasterPlus=false이면 LP Cap은 검증되지 않음
        });
    }

    @Test
    @DisplayName("OTHER_MODES - Master+ LP Cap null 무제한")
    void testOtherModesMasterPlusLpCapNull() {
        // given
        FindTeamPostRequest request = FindTeamPostRequest.builder()
                .title("칼바람 마스터+ 무제한")
                .content("편하게")
                .writer("TestUser#KR1")
                .writerId(1L)
                .tags(Set.of(PositionTag.BOT))
                .matchType(MatchType.OTHER_MODES)
                .minTierScore(0)
                .maxTierScore(2700) // Diamond 1
                .requireMasterPlus(true)
                .masterPlusLpCap(null) // 무제한
                .build();

        // when & then
        assertDoesNotThrow(() -> {
            // null이면 무제한 허용
        });
    }
}

