package org.example.stcapstonebackend.findTeam.mapper;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.util.TierCalculator;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.example.stcapstonebackend.findTeam.dto.TierRange;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.springframework.stereotype.Component;

import java.util.stream.Collectors;

/**
 * 팀 찾기 게시글 엔티티와 DTO 간의 변환을 담당하는 매퍼 클래스입니다.
 * 프론트엔드는 티어 기반으로 통신하고, 백엔드 내부는 점수 기반으로 처리합니다.
 */
@Component
@RequiredArgsConstructor
public class FindTeamPostMapper {

    private final FindTeamRequestMapper findTeamRequestMapper;

    /**
     * 요청 DTO를 엔티티로 변환합니다.
     * TierRange를 점수(score)로 변환하여 저장합니다.
     *
     * @param request 게시글 요청 DTO
     * @return 변환된 게시글 엔티티
     */
    public FindTeamPost toEntity(FindTeamPostRequest request) {
        // TierRange → Score 변환
        int minScore = convertTierRangeToScore(request.minTier());
        int maxScore = convertTierRangeToScore(request.maxTier());

        return FindTeamPost.builder()
                .title(request.title())
                .content(request.content())
                .writer(request.writer())
                .writerId(request.writerId())
                .tags(request.tags())
                .matchType(request.matchType())
                .minTierScore(minScore)
                .maxTierScore(maxScore)
                .requireMasterPlus(request.requireMasterPlus())
                .masterPlusLpCap(request.masterPlusLpCap())
                .build();
    }

    /**
     * 엔티티를 응답 DTO로 변환합니다.
     * 점수(score)를 TierRange로 변환하여 반환합니다.
     *
     * @param post 게시글 엔티티
     * @return 변환된 게시글 응답 DTO
     */
    public FindTeamPostResponse toDto(FindTeamPost post) {
        // Score → TierRange 변환
        TierRange minTier = convertScoreToTierRange(post.getMinTierScore());
        TierRange maxTier = convertScoreToTierRange(post.getMaxTierScore());

        return new FindTeamPostResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getWriter(),
                post.getWriterId(),
                post.getTags(),
                post.getAcceptedTags(),
                post.getAvailableTags(),
                post.getStatus(),
                post.getPendingExpirationAt(),
                post.getRequests().size(),
                post.getCreatedAt(),
                post.getModifiedAt(),
                post.getRequests().stream()
                        .map(findTeamRequestMapper::toDto)
                        .collect(Collectors.toList()),
                post.getMatchType(),
                minTier,
                maxTier,
                post.getRequireMasterPlus(),
                post.getMasterPlusLpCap()
        );
    }

    /**
     * TierRange를 점수로 변환합니다.
     *
     * @param tierRange 티어 범위
     * @return 변환된 점수
     */
    private int convertTierRangeToScore(TierRange tierRange) {
        return TierCalculator.calculateScore(
                tierRange.tier(),
                tierRange.division(),
                tierRange.lp()
        );
    }

    /**
     * 점수를 TierRange로 변환합니다.
     *
     * @param score 점수
     * @return 변환된 티어 범위
     */
    private TierRange convertScoreToTierRange(int score) {
        TierCalculator.TierInfo tierInfo = TierCalculator.scoreToTier(score);
        return TierRange.builder()
                .tier(tierInfo.tier().name())
                .division(tierInfo.division().name())
                .lp(tierInfo.lp())
                .build();
    }
}
