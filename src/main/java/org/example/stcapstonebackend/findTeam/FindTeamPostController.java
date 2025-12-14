package org.example.stcapstonebackend.findTeam;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.common.util.TierCalculator;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.example.stcapstonebackend.findTeam.dto.WriterTierInfoResponse;
import org.example.stcapstonebackend.findTeam.model.MatchType;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 팀 찾기 게시글 관련 REST API를 제공하는 컨트롤러입니다.
 * 게시글의 CRUD 작업 및 검색 기능을 제공합니다.
 */
@RestController
@RequestMapping("/api/find-team/posts")
@RequiredArgsConstructor
public class FindTeamPostController {

    private final FindTeamPostService findTeamPostService;
    private final UserRepository userRepository;

    /**
     * 새로운 findTeam 게시글을 생성합니다.
     *
     * @param request 게시글 생성 요청 데이터
     * @return 생성된 게시글 정보
     */
    @PostMapping
    public ResponseEntity<FindTeamPostResponse> createPost(@Valid @RequestBody FindTeamPostRequest request) {
        FindTeamPostResponse response = findTeamPostService.createPost(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    /**
     * 기존 findTeam 게시글을 수정합니다.
     *
     * @param id 수정할 게시글의 ID
     * @param request 수정할 게시글 데이터
     * @param authentication 인증된 사용자 정보
     * @return 수정된 게시글 정보
     */
    @PutMapping("/{id}")
    public ResponseEntity<FindTeamPostResponse> updatePost(
            @PathVariable Long id,
            @Valid @RequestBody FindTeamPostRequest request,
            Authentication authentication
    ) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        FindTeamPostResponse response = findTeamPostService.updatePost(id, request, user.getId());
        return ResponseEntity.ok(response);
    }

    /**
     * findTeam 게시글을 삭제합니다.
     *
     * @param id 삭제할 게시글의 ID
     * @param authentication 인증된 사용자 정보
     * @return 204 No Content 응답
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePost(@PathVariable Long id, Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다."));

        findTeamPostService.deletePost(id, user.getId());
        return ResponseEntity.noContent().build();
    }

    /**
     * ID로 특정 findTeam 게시글을 조회합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 조회된 게시글 정보
     */
    @GetMapping("/{id}")
    public ResponseEntity<FindTeamPostResponse> getPost(@PathVariable Long id) {
        FindTeamPostResponse response = findTeamPostService.getPost(id);
        return ResponseEntity.ok(response);
    }

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 전체 게시글 목록
     */
    @GetMapping
    public ResponseEntity<List<FindTeamPostResponse>> getAllPosts() {
        List<FindTeamPostResponse> responses = findTeamPostService.getAllPosts();
        return ResponseEntity.ok(responses);
    }

    /**
     * 활성 상태의 게시글만 조회합니다.
     *
     * @return 활성 상태의 게시글 목록
     */
    @GetMapping("/active")
    public ResponseEntity<List<FindTeamPostResponse>> getActivePosts() {
        List<FindTeamPostResponse> responses = findTeamPostService.getActivePosts();
        return ResponseEntity.ok(responses);
    }

    /**
     * 팀 찾기 게시글을 검색합니다.
     *
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @GetMapping("/search")
    public ResponseEntity<List<FindTeamPostResponse>> searchPosts(
            @RequestParam org.example.stcapstonebackend.findTeam.model.SearchType searchType,
            @RequestParam String keyword) {
        List<FindTeamPostResponse> responses = findTeamPostService.searchPosts(searchType, keyword);
        return ResponseEntity.ok(responses);
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param authentication 인증된 사용자 정보
     * @return 사용자가 작성한 게시글 목록
     */
    @GetMapping("/my-posts")
    public ResponseEntity<List<FindTeamPostResponse>> getMyPosts(Authentication authentication) {
        String username = authentication.getName();
        List<FindTeamPostResponse> responses = findTeamPostService.getMyPosts(username);
        return ResponseEntity.ok(responses);
    }

    /**
     * 티어 범위로 활성 게시글을 필터링합니다. (TierRange 기반)
     * 사용자의 티어 범위와 게시글의 모집 티어 범위가 조금이라도 겹치면 결과에 포함됩니다.
     *
     * @param minTier 사용자의 최소 티어
     * @param minDivision 사용자의 최소 Division
     * @param minLp 사용자의 최소 LP (기본값 0)
     * @param maxTier 사용자의 최대 티어
     * @param maxDivision 사용자의 최대 Division
     * @param maxLp 사용자의 최대 LP (기본값 99)
     * @return 필터링된 게시글 목록
     */
    @GetMapping("/filter-by-tier")
    public ResponseEntity<List<FindTeamPostResponse>> getPostsByTierRange(
            @RequestParam String minTier,
            @RequestParam String minDivision,
            @RequestParam(required = false, defaultValue = "0") Integer minLp,
            @RequestParam String maxTier,
            @RequestParam String maxDivision,
            @RequestParam(required = false, defaultValue = "99") Integer maxLp) {

        // TierRange → Score 변환 (내부 처리)
        int minScore = TierCalculator.calculateScore(minTier, minDivision, minLp);
        int maxScore = TierCalculator.calculateScore(maxTier, maxDivision, maxLp);

        List<FindTeamPostResponse> responses = findTeamPostService.getPostsByTierRange(minScore, maxScore);
        return ResponseEntity.ok(responses);
    }

    /**
     * 티어 범위와 매치 타입으로 활성 게시글을 필터링합니다. (TierRange 기반)
     *
     * @param minTier 사용자의 최소 티어
     * @param minDivision 사용자의 최소 Division
     * @param minLp 사용자의 최소 LP (기본값 0)
     * @param maxTier 사용자의 최대 티어
     * @param maxDivision 사용자의 최대 Division
     * @param maxLp 사용자의 최대 LP (기본값 99)
     * @param matchType 매치 타입 (SOLO_RANK, FLEX_RANK, OTHER_MODES)
     * @return 필터링된 게시글 목록
     */
    @GetMapping("/filter-by-tier-and-match")
    public ResponseEntity<List<FindTeamPostResponse>> getPostsByTierRangeAndMatchType(
            @RequestParam String minTier,
            @RequestParam String minDivision,
            @RequestParam(required = false, defaultValue = "0") Integer minLp,
            @RequestParam String maxTier,
            @RequestParam String maxDivision,
            @RequestParam(required = false, defaultValue = "99") Integer maxLp,
            @RequestParam MatchType matchType) {

        // TierRange → Score 변환 (내부 처리)
        int minScore = TierCalculator.calculateScore(minTier, minDivision, minLp);
        int maxScore = TierCalculator.calculateScore(maxTier, maxDivision, maxLp);

        List<FindTeamPostResponse> responses = findTeamPostService.getPostsByTierRangeAndMatchType(
                minScore, maxScore, matchType);
        return ResponseEntity.ok(responses);
    }

    /**
     * 게시글 작성자의 티어 정보 및 허용 가능한 점수 범위를 조회합니다.
     * 프론트엔드에서 게시글 작성 시 작성자의 티어 정보를 표시하고,
     * 팀원 모집 가능한 티어 범위를 제안하기 위해 사용됩니다.
     *
     * @param userId 작성자의 사용자 ID
     * @return 작성자의 티어 정보 및 허용 가능한 점수 범위
     */
    @GetMapping("/writer-tier-info/{userId}")
    public ResponseEntity<WriterTierInfoResponse> getWriterTierInfo(@PathVariable Long userId) {
        WriterTierInfoResponse response = findTeamPostService.getWriterTierInfo(userId);
        return ResponseEntity.ok(response);
    }
}
