package org.example.stcapstonebackend.findTeam;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.exception.UnauthorizedAccessException;
import org.example.stcapstonebackend.common.model.Division;
import org.example.stcapstonebackend.common.model.Tier;
import org.example.stcapstonebackend.common.util.TierCalculator;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostRequest;
import org.example.stcapstonebackend.findTeam.dto.FindTeamPostResponse;
import org.example.stcapstonebackend.findTeam.dto.WriterTierInfoResponse;
import org.example.stcapstonebackend.findTeam.exception.FindTeamPostNotFoundException;
import org.example.stcapstonebackend.findTeam.exception.InvalidTierRangeException;
import org.example.stcapstonebackend.findTeam.mapper.FindTeamPostMapper;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.example.stcapstonebackend.findTeam.model.MatchType;
import org.example.stcapstonebackend.findTeam.model.PostStatus;
import org.example.stcapstonebackend.summoner.SummonerService;
import org.example.stcapstonebackend.summoner.dto.SummonerSearchResponseDto;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.exception.UserNotFoundException;
import org.example.stcapstonebackend.user.model.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 팀 찾기 게시글 관리를 위한 서비스 클래스입니다.
 * 게시글의 생성, 조회, 수정, 삭제 및 검색 기능을 제공합니다.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class FindTeamPostService {

    private final FindTeamPostRepository findTeamPostRepository;
    private final FindTeamPostMapper findTeamPostMapper;
    private final SummonerService summonerService;
    private final UserRepository userRepository;

    /**
     * 게시글 ID로 게시글 엔티티를 조회합니다.
     *
     * @param id 게시글 ID
     * @return 조회된 게시글 엔티티
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    private FindTeamPost getPostEntity(Long id) {
        return findTeamPostRepository.findById(id)
                .orElseThrow(() -> new FindTeamPostNotFoundException("Cannot find post with id: " + id));
    }

    /**
     * 새로운 팀 찾기 게시글을 생성합니다.
     * 매치 타입에 따른 티어 범위 제약 조건을 검증합니다.
     *
     * @param request 게시글 생성 요청 정보
     * @return 생성된 게시글 정보
     * @throws InvalidTierRangeException 티어 범위가 제약 조건을 위반한 경우
     */
    @Transactional
    public FindTeamPostResponse createPost(FindTeamPostRequest request) {
        // 기본 유효성 검사
        validateBasicTierRange(request);

        // 작성자의 Riot 계정 정보 조회 (writer가 "닉네임#태그" 형식이라고 가정)
        String fullName = request.writer();

        // 매치 타입별 티어 제약 조건 검증
        validateTierRangeByMatchType(request, fullName);

        // 솔로랭크의 경우 포지션 태그가 최대 1개인지 검증
        if (request.matchType() == MatchType.SOLO_RANK && request.tags().size() > 1) {
            throw new InvalidTierRangeException("솔로랭크 모집은 최대 1개의 포지션만 선택할 수 있습니다.");
        }

        FindTeamPost post = findTeamPostMapper.toEntity(request);
        FindTeamPost savedPost = findTeamPostRepository.save(post);
        log.info("팀 찾기 게시글 생성 완료 - ID: {}, 작성자: {}, 매치타입: {}, 티어범위: {} ~ {}",
                savedPost.getId(), request.writer(), request.matchType(),
                request.minTier(), request.maxTier());
        return findTeamPostMapper.toDto(savedPost);
    }

    /**
     * 기본적인 티어 범위 유효성을 검사합니다.
     * TierRange를 점수로 변환하여 검증합니다.
     */
    private void validateBasicTierRange(FindTeamPostRequest request) {
        int minScore = convertTierRangeToScore(request.minTier());
        int maxScore = convertTierRangeToScore(request.maxTier());

        if (minScore > maxScore) {
            throw new InvalidTierRangeException(
                    String.format("최소 티어(%s)가 최대 티어(%s)보다 높을 수 없습니다.",
                            request.minTier(), request.maxTier())
            );
        }

        if (minScore < 0) {
            throw new InvalidTierRangeException("최소 티어는 Iron 4 이상이어야 합니다.");
        }
    }

    /**
     * TierRange를 점수로 변환하는 헬퍼 메서드입니다.
     */
    private int convertTierRangeToScore(org.example.stcapstonebackend.findTeam.dto.TierRange tierRange) {
        return TierCalculator.calculateScore(
                tierRange.tier(),
                tierRange.division(),
                tierRange.lp()
        );
    }

    /**
     * 매치 타입에 따른 티어 범위 제약 조건을 검증합니다.
     */
    private void validateTierRangeByMatchType(FindTeamPostRequest request, String writerFullName) {
        try {
            SummonerSearchResponseDto summonerInfo = summonerService.searchSummoner(writerFullName);

            switch (request.matchType()) {
                case SOLO_RANK -> validateSoloRankTierRange(request, summonerInfo);
                case FLEX_RANK -> validateFlexRankTierRange(request);
                case OTHER_MODES -> validateOtherModesTierRange(request);
            }
        } catch (Exception e) {
            log.warn("소환사 정보 조회 실패 - {}: {}", writerFullName, e.getMessage());
            // 소환사 정보를 가져올 수 없는 경우 기본 검증만 수행
            validateWithoutSummonerInfo(request);
        }
    }

    /**
     * 솔로랭크 티어 범위를 검증합니다.
     * 라이엇 정책: ±1 티어 (400점) 범위 내에서만 듀오 가능.
     * 마스터 이상은 듀오 불가.
     */
    private void validateSoloRankTierRange(FindTeamPostRequest request, SummonerSearchResponseDto summonerInfo) {
        int writerScore = TierCalculator.calculateScore(
                summonerInfo.soloTier(),
                summonerInfo.soloDivision(),
                summonerInfo.soloPoints()
        );

        // 마스터 이상은 솔로랭크 듀오 불가
        if (writerScore >= Tier.MASTER.getBaseScore()) {
            throw new InvalidTierRangeException("마스터 이상 티어는 솔로랭크 듀오를 할 수 없습니다.");
        }

        // 듀오 가능 범위 계산
        int[] duoRange = TierCalculator.calculateSoloRankDuoRange(writerScore);
        int allowedMin = duoRange[0];
        int allowedMax = duoRange[1];

        // 요청한 티어 범위를 점수로 변환
        int requestMinScore = convertTierRangeToScore(request.minTier());
        int requestMaxScore = convertTierRangeToScore(request.maxTier());

        // 요청한 범위가 허용 범위를 벗어나는지 확인
        if (requestMinScore < allowedMin || requestMaxScore > allowedMax) {
            TierCalculator.TierInfo minTierInfo = TierCalculator.scoreToTier(allowedMin);
            TierCalculator.TierInfo maxTierInfo = TierCalculator.scoreToTier(allowedMax);
            throw new InvalidTierRangeException(
                    String.format("솔로랭크 듀오는 작성자 티어 기준 ±1 티어 범위 내에서만 가능합니다. " +
                            "허용 범위: %s ~ %s", minTierInfo, maxTierInfo)
            );
        }
    }

    /**
     * 자유랭크 티어 범위를 검증합니다.
     * 기본: Iron 4 ~ Diamond 1 전체 가능
     * Master+ 체크 시: 슬라이더 상한은 Diamond 1이어야 하며 LP 상한 설정 가능
     */
    private void validateFlexRankTierRange(FindTeamPostRequest request) {
        if (request.requireMasterPlus()) {
            int diamond1Score = Tier.DIAMOND.getBaseScore() + 300; // 2700
            int maxScore = convertTierRangeToScore(request.maxTier());

            // 마스터+ 체크 시 슬라이더 상한은 반드시 Diamond 1이어야 함
            if (maxScore != diamond1Score) {
                throw new InvalidTierRangeException(
                        String.format("마스터 이상 모집 시 최대 티어는 반드시 Diamond I이어야 합니다. (입력: %s)",
                                request.maxTier())
                );
            }

            // LP 상한 검증
            if (request.masterPlusLpCap() != null && request.masterPlusLpCap() < 0) {
                throw new InvalidTierRangeException("마스터 이상 LP 상한은 0 이상이어야 합니다.");
            }
        }
    }

    /**
     * 기타 모드 티어 범위를 검증합니다.
     * 기타 모드는 티어 제한이 없습니다.
     * 마스터 이상 체크는 LP 상한 입력을 위한 것이며, 체크 시 슬라이더 상한이 다이아1이어야 합니다.
     */
    private void validateOtherModesTierRange(FindTeamPostRequest request) {
        if (request.requireMasterPlus()) {
            int diamond1Score = Tier.DIAMOND.getBaseScore() + 300; // 2700
            int maxScore = convertTierRangeToScore(request.maxTier());

            // 마스터+ 체크 시 슬라이더 상한은 반드시 Diamond 1이어야 함
            if (maxScore != diamond1Score) {
                throw new InvalidTierRangeException(
                        String.format("마스터 이상 모집 시 최대 티어는 반드시 Diamond I이어야 합니다. (입력: %s)",
                                request.maxTier())
                );
            }

            // LP 상한 검증 (0~9999 범위, null이면 무제한)
            if (request.masterPlusLpCap() != null) {
                int lpCap = request.masterPlusLpCap();
                if (lpCap < 0 || lpCap > 9999) {
                    throw new InvalidTierRangeException(
                        String.format("마스터 이상 LP 상한은 0~9999 범위여야 합니다. (입력값: %d)", lpCap)
                    );
                }
            }
            // null인 경우 무제한으로 처리 (별도 검증 불필요)
        }
    }

    /**
     * 소환사 정보 없이 기본 검증만 수행합니다.
     */
    private void validateWithoutSummonerInfo(FindTeamPostRequest request) {
        // 솔로랭크인 경우 마스터 이상 범위를 포함하지 않는지만 검증
        if (request.matchType() == MatchType.SOLO_RANK) {
            int maxScore = convertTierRangeToScore(request.maxTier());
            if (maxScore >= Tier.MASTER.getBaseScore()) {
                throw new InvalidTierRangeException(
                        String.format("솔로랭크는 마스터 이상 티어를 포함할 수 없습니다. (입력: %s)",
                                request.maxTier())
                );
            }
        }
    }

    /**
     * 기존 게시글을 수정합니다.
     * 작성자만 수정할 수 있습니다.
     *
     * @param id 수정할 게시글의 ID
     * @param request 수정할 게시글 정보
     * @param userId 수정을 요청한 사용자 ID
     * @return 수정된 게시글 정보
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     * @throws UnauthorizedAccessException 작성자가 아닌 경우
     */
    @Transactional
    public FindTeamPostResponse updatePost(Long id, FindTeamPostRequest request, Long userId) {
        FindTeamPost post = getPostEntity(id);

        // 작성자인지 확인
        if (!post.getWriterId().equals(userId)) {
            throw new UnauthorizedAccessException("해당 게시글을 수정할 권한이 없습니다.");
        }

        post.update(request.title(), request.content(), request.writer(), request.writerId(), request.tags());
        return findTeamPostMapper.toDto(post);
    }

    /**
     * 게시글을 삭제합니다.
     * 작성자만 삭제할 수 있습니다.
     *
     * @param id 삭제할 게시글의 ID
     * @param userId 삭제를 요청한 사용자 ID
     * @throws UnauthorizedAccessException 작성자가 아닌 경우
     */
    @Transactional
    public void deletePost(Long id, Long userId) {
        FindTeamPost post = getPostEntity(id);

        // 작성자인지 확인
        if (!post.getWriterId().equals(userId)) {
            throw new UnauthorizedAccessException("해당 게시글을 삭제할 권한이 없습니다.");
        }

        findTeamPostRepository.deleteById(id);
    }

    /**
     * ID로 특정 게시글을 조회합니다.
     *
     * @param id 조회할 게시글의 ID
     * @return 조회된 게시글 정보
     * @throws FindTeamPostNotFoundException 게시글을 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public FindTeamPostResponse getPost(Long id) {
        FindTeamPost post = getPostEntity(id);
        return findTeamPostMapper.toDto(post);
    }

    /**
     * 모든 게시글을 조회합니다.
     *
     * @return 전체 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getAllPosts() {
        return findTeamPostRepository.findAll().stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상태의 게시글만 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @return 활성 상태의 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getActivePosts() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.ACTIVE).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글을 검색합니다.
     * 검색 타입에 따라 제목, 내용, 제목+내용, 작성자 중 선택하여 검색합니다.
     *
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> searchPosts(org.example.stcapstonebackend.findTeam.model.SearchType searchType, String keyword) {
        List<FindTeamPost> posts = switch (searchType) {
            case TITLE -> findTeamPostRepository.findByTitleContainingIgnoreCase(keyword);
            case CONTENT -> findTeamPostRepository.findByContentContainingIgnoreCase(keyword);
            case TITLE_CONTENT -> findTeamPostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
            case WRITER -> findTeamPostRepository.findByWriterContainingIgnoreCase(keyword);
        };

        return posts.stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param username 사용자명 (로그인한 사용자)
     * @return 사용자가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getMyPosts(String username) {
        return findTeamPostRepository.findByWriterOrderByCreatedAtDesc(username).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 모든 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getAllPostsSortedByNewest() {
        return findTeamPostRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 모든 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getAllPostsSortedByOldest() {
        return findTeamPostRepository.findAllByOrderByCreatedAtAsc().stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 활성 상태 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getActivePostsSortedByOldest() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtAsc(PostStatus.ACTIVE).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 매칭 완료 상태 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getMatchedPostsSortedByNewest() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.MATCHED).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 매칭 완료 상태 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getMatchedPostsSortedByOldest() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtAsc(PostStatus.MATCHED).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 게시글을 생성일 기준 내림차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getExpiredPostsSortedByNewest() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtDesc(PostStatus.EXPIRED).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 만료된 게시글을 생성일 기준 오름차순으로 조회합니다.
     *
     * @return 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getExpiredPostsSortedByOldest() {
        return findTeamPostRepository.findByStatusOrderByCreatedAtAsc(PostStatus.EXPIRED).stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 티어 범위 교차 조건으로 활성 게시글을 필터링합니다.
     * 사용자가 입력한 티어 범위와 게시글의 모집 티어 범위가 조금이라도 겹치면 결과에 포함됩니다.
     *
     * @param userMinScore 사용자의 최소 티어 점수
     * @param userMaxScore 사용자의 최대 티어 점수
     * @return 필터링된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getPostsByTierRange(Integer userMinScore, Integer userMaxScore) {
        return findTeamPostRepository.findByTierRangeIntersection(userMinScore, userMaxScore, PostStatus.ACTIVE)
                .stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 티어 범위와 매치 타입으로 활성 게시글을 필터링합니다.
     *
     * @param userMinScore 사용자의 최소 티어 점수
     * @param userMaxScore 사용자의 최대 티어 점수
     * @param matchType 매치 타입
     * @return 필터링된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<FindTeamPostResponse> getPostsByTierRangeAndMatchType(Integer userMinScore, Integer userMaxScore, MatchType matchType) {
        return findTeamPostRepository.findByTierRangeAndMatchType(userMinScore, userMaxScore, matchType, PostStatus.ACTIVE)
                .stream()
                .map(findTeamPostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 게시글 작성자의 티어 정보 및 허용 가능한 점수 범위를 조회합니다.
     * 프론트엔드에서 게시글 작성 시 작성자의 티어 정보를 표시하고,
     * 팀원 모집 가능한 티어 범위를 제안하기 위해 사용됩니다.
     *
     * @param userId 작성자의 사용자 ID
     * @return 작성자의 티어 정보 및 허용 가능한 점수 범위
     * @throws UserNotFoundException 사용자를 찾을 수 없는 경우
     */
    @Transactional(readOnly = true)
    public WriterTierInfoResponse getWriterTierInfo(Long userId) {
        // 사용자 정보 조회
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("사용자를 찾을 수 없습니다. ID: " + userId));

        String fullName = user.getRiotName() + "#" + user.getRiotTag();

        // Riot API로 소환사 정보 조회
        SummonerSearchResponseDto summonerInfo = summonerService.searchSummoner(fullName);

        // 솔로랭크 티어 점수 계산
        int soloTierScore = TierCalculator.calculateScore(
                summonerInfo.soloTier(),
                summonerInfo.soloDivision(),
                summonerInfo.soloPoints()
        );

        // 솔로랭크 듀오 가능 범위 계산
        int[] soloRankDuoRange = TierCalculator.calculateSoloRankDuoRange(soloTierScore);

        // 점수를 TierRange로 변환
        org.example.stcapstonebackend.findTeam.dto.TierRange soloRankMinTier = null;
        org.example.stcapstonebackend.findTeam.dto.TierRange soloRankMaxTier = null;

        if (soloRankDuoRange[0] != -1 && soloRankDuoRange[1] != -1) {
            // 듀오 가능한 경우
            TierCalculator.TierInfo minTierInfo = TierCalculator.scoreToTier(soloRankDuoRange[0]);
            TierCalculator.TierInfo maxTierInfo = TierCalculator.scoreToTier(soloRankDuoRange[1]);

            soloRankMinTier = org.example.stcapstonebackend.findTeam.dto.TierRange.builder()
                    .tier(minTierInfo.tier().name())
                    .division(minTierInfo.division().name())
                    .lp(minTierInfo.lp())
                    .build();

            soloRankMaxTier = org.example.stcapstonebackend.findTeam.dto.TierRange.builder()
                    .tier(maxTierInfo.tier().name())
                    .division(maxTierInfo.division().name())
                    .lp(maxTierInfo.lp())
                    .build();
        }

        // 자유랭크/기타모드는 전체 범위 허용 (Iron 4 ~ Diamond 1)
        TierCalculator.TierInfo flexMinInfo = TierCalculator.scoreToTier(0);     // Iron 4 0LP
        TierCalculator.TierInfo flexMaxInfo = TierCalculator.scoreToTier(2799);  // Diamond 1 99LP

        org.example.stcapstonebackend.findTeam.dto.TierRange flexRankMinTier =
                org.example.stcapstonebackend.findTeam.dto.TierRange.builder()
                        .tier(flexMinInfo.tier().name())
                        .division(flexMinInfo.division().name())
                        .lp(flexMinInfo.lp())
                        .build();

        org.example.stcapstonebackend.findTeam.dto.TierRange flexRankMaxTier =
                org.example.stcapstonebackend.findTeam.dto.TierRange.builder()
                        .tier(flexMaxInfo.tier().name())
                        .division(flexMaxInfo.division().name())
                        .lp(flexMaxInfo.lp())
                        .build();

        boolean flexRankMasterPlusAllowed = true;

        log.info("작성자 티어 정보 조회 완료 - 사용자: {}#{}, 솔로: {} {} {}LP, " +
                        "자유: {} {} {}LP, 솔로랭크 듀오 범위: {} ~ {}",
                user.getRiotName(), user.getRiotTag(),
                summonerInfo.soloTier(), summonerInfo.soloDivision(), summonerInfo.soloPoints(),
                summonerInfo.flexTier(), summonerInfo.flexDivision(), summonerInfo.flexPoints(),
                soloRankMinTier, soloRankMaxTier);

        return WriterTierInfoResponse.builder()
                .summonerName(user.getRiotName())
                .summonerTag(user.getRiotTag())
                .soloTier(summonerInfo.soloTier())
                .soloDivision(summonerInfo.soloDivision())
                .soloLp(summonerInfo.soloPoints())
                .flexTier(summonerInfo.flexTier())
                .flexDivision(summonerInfo.flexDivision())
                .flexLp(summonerInfo.flexPoints())
                .soloRankMinTier(soloRankMinTier)
                .soloRankMaxTier(soloRankMaxTier)
                .flexRankMinTier(flexRankMinTier)
                .flexRankMaxTier(flexRankMaxTier)
                .flexRankMasterPlusAllowed(flexRankMasterPlusAllowed)
                .build();
    }
}
