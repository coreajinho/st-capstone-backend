package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.common.exception.UnauthorizedAccessException;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.debate.mapper.DebatePostMapper;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.example.stcapstonebackend.debate.model.DebateStatus;
import org.example.stcapstonebackend.debate.model.PopularType;
import org.example.stcapstonebackend.debate.model.SearchType;
import org.example.stcapstonebackend.user.UserRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class DebatePostService {
    private final DebatePostRepository debatePostRepository;
    private final DebatePostMapper debatePostMapper;
    private final DebateCommentRepository debateCommentRepository;
    private final UserRepository userRepository;

    //--------------------내부 메소드------------------------------------------
    private DebatePost getPostEntity(Long id) {
        return debatePostRepository.findById(id)
                .orElseThrow(() -> new DebatePostNotFoundException("해당id의 게시글이 없습니다. "));
    }

    //--------------------------서비스 메소드--------------------------------------------------

    //    게시글 생성
    public DebatePostResponse createPost(DebatePostRequest postDto) {
        DebatePost post = debatePostMapper.toEntity(postDto);
        DebatePost savedPost = debatePostRepository.save(post);
        return debatePostMapper.toDto(savedPost);
    }

    /**
     * id로 게시글을 수정합니다.
     * 작성자 또는 공동 작성자만 수정할 수 있습니다.
     *
     * @param postDto 수정할 게시글 정보
     * @param id 게시글 ID
     * @param userId 수정을 요청한 사용자 ID
     * @return 수정된 게시글 정보
     * @throws UnauthorizedAccessException 작성자가 아닌 경우
     */
    @Transactional
    public DebatePostResponse updatePost(DebatePostRequest postDto, Long id, Long userId) {
        DebatePost post = getPostEntity(id);

        // 작성자 또는 공동 작성자인지 확인
        if (!isAuthorOrCoAuthor(post, userId)) {
            throw new UnauthorizedAccessException("해당 게시글을 수정할 권한이 없습니다.");
        }

        // writerId, coWriterId 유효성 검증 (toEntity에서 수행)
        debatePostMapper.toEntity(postDto);

        post.update(
                postDto.title(),
                postDto.content(),
                postDto.writerId(),
                postDto.coWriterId(),
                postDto.videoUrl(),
                postDto.tags()
        );

        return debatePostMapper.toDto(post);
    }

    /**
     * id로 게시글을 삭제합니다.
     * 작성자 또는 공동 작성자만 삭제할 수 있습니다.
     *
     * @param id 게시글 ID
     * @param userId 삭제를 요청한 사용자 ID
     * @throws UnauthorizedAccessException 작성자가 아닌 경우
     */
    public void deletePost(Long id, Long userId) {
        DebatePost post = getPostEntity(id);

        // 작성자 또는 공동 작성자인지 확인
        if (!isAuthorOrCoAuthor(post, userId)) {
            throw new UnauthorizedAccessException("해당 게시글을 삭제할 권한이 없습니다.");
        }

        debatePostRepository.deleteById(id);
    }

    /**
     * 사용자가 게시글의 작성자 또는 공동 작성자인지 확인합니다.
     *
     * @param post 게시글
     * @param userId 사용자 ID
     * @return 작성자 또는 공동 작성자인 경우 true
     */
    private boolean isAuthorOrCoAuthor(DebatePost post, Long userId) {
        return post.getWriterId().equals(userId)
                || (post.getCoWriterId() != null && post.getCoWriterId().equals(userId));
    }

    //    id로 게시글 1개 조회
    @Transactional
    public DebatePostResponse getPost(Long id) {
        DebatePost post = getPostEntity(id);
        post.incrementViews(); // 조회수 자동 증가
        return debatePostMapper.toDto(post);
    }

    //    전체 게시글 조회
    @Transactional(readOnly = true)
    public List<DebatePostResponse> getAllPosts() {
        return debatePostRepository.findAll().stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 토론 게시글의 득표 결과를 조회합니다. (일반 버전 - 엔티티에서 계산)
     *
     * @param postId 게시글 ID
     * @return 득표 결과
     */
    public DebateVoteResultDto getVoteResult(Long postId) {
        DebatePost post = getPostEntity(postId);
        return DebateVoteResultDto.fromEntity(post);
    }

    /**
     * 특정 토론 게시글의 득표 결과를 조회합니다. (최적화 버전 - 쿼리로 집계)
     * 댓글 수가 많을 경우 이 메소드를 사용하면 성능이 더 좋습니다.
     *
     * @param postId 게시글 ID
     * @return 득표 결과
     */
    public DebateVoteResultDto getVoteResultOptimized(Long postId) {
        // 게시글 존재 확인
        if (!debatePostRepository.existsById(postId)) {
            throw new DebatePostNotFoundException("해당id의 게시글이 없습니다.");
        }

        // DB에서 집계된 투표 수 조회
        List<DebateCommentRepository.DebateVoteCount> voteCounts =
                debateCommentRepository.countByDebateSide(postId);

        // Map으로 변환
        Map<DebateSide, Long> countMap = voteCounts.stream()
                .collect(Collectors.toMap(
                        DebateCommentRepository.DebateVoteCount::getSide,
                        DebateCommentRepository.DebateVoteCount::getCount
                ));

        long player1Count = countMap.getOrDefault(DebateSide.PLAYER_1, 0L);
        long player2Count = countMap.getOrDefault(DebateSide.PLAYER_2, 0L);

        return DebateVoteResultDto.fromCounts(postId, player1Count, player2Count);
    }

    /**
     * 검색 타입과 키워드에 따라 토론 게시글을 검색합니다.
     *
     * @param searchType 검색 타입 (TITLE, CONTENT, TITLE_CONTENT, WRITER)
     * @param keyword 검색 키워드
     * @return 검색된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<DebatePostResponse> searchPosts(SearchType searchType, String keyword) {
        List<DebatePost> posts;

        switch (searchType) {
            case TITLE:
                posts = debatePostRepository.findByTitleContainingIgnoreCase(keyword);
                break;
            case CONTENT:
                posts = debatePostRepository.findByContentContainingIgnoreCase(keyword);
                break;
            case TITLE_CONTENT:
                posts = debatePostRepository.findByTitleContainingIgnoreCaseOrContentContainingIgnoreCase(keyword, keyword);
                break;
            case WRITER:
                posts = debatePostRepository.findByWriterOrCoWriterContainingIgnoreCase(keyword);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 검색 타입입니다: " + searchType);
        }

        return posts.stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 인기글을 조회합니다. (조회수 또는 댓글 수 기준 상위 10개)
     *
     * @param popularType 인기글 기준 (VIEWS: 조회수, COMMENTS: 댓글 수)
     * @return 인기글 목록 (상위 10개)
     */
    @Transactional(readOnly = true)
    public List<DebatePostResponse> getPopularPosts(PopularType popularType) {
        Pageable pageable = PageRequest.of(0, 10); // 상위 10개
        List<DebatePost> posts;

        switch (popularType) {
            case VIEWS:
                posts = debatePostRepository.findTopByOrderByViewsDesc(pageable);
                break;
            case COMMENTS:
                posts = debatePostRepository.findTopByOrderByCommentsDesc(pageable);
                break;
            default:
                throw new IllegalArgumentException("지원하지 않는 인기글 타입입니다: " + popularType);
        }

        return posts.stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 로그인한 사용자가 작성한 게시글 목록을 조회합니다.
     * 작성자 또는 공동 작성자로 등록된 게시글을 모두 조회합니다.
     * 생성일 기준 내림차순으로 정렬됩니다.
     *
     * @param userId 사용자 ID
     * @return 사용자가 작성한 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<DebatePostResponse> getMyPosts(Long userId) {
        return debatePostRepository.findByWriterIdOrCoWriterIdOrderByCreatedAtDesc(userId).stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 특정 상태의 토론 게시글 목록을 조회합니다.
     *
     * @param status 조회할 토론 상태
     * @return 해당 상태의 게시글 목록 (생성일 기준 내림차순)
     */
    @Transactional(readOnly = true)
    public List<DebatePostResponse> getPostsByStatus(DebateStatus status) {
        return debatePostRepository.findByDebateStatusOrderByCreatedAtDesc(status).stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    /**
     * 모든 토론 게시글을 상태 우선순위 정렬로 조회합니다.
     * 정렬 순서: ACTIVE > PENDING > EXPIRED, 같은 상태 내에서는 생성일 기준 내림차순
     *
     * @return 상태 우선순위로 정렬된 게시글 목록
     */
    @Transactional(readOnly = true)
    public List<DebatePostResponse> getAllPostsOrderedByStatus() {
        return debatePostRepository.findAllOrderedByStatusAndCreatedAt().stream()
                .map(debatePostMapper::toDto)
                .collect(Collectors.toList());
    }

    //--------------------결산 관련 메서드------------------------------------------

    /**
     * 토론 게시글의 결산을 수행합니다.
     * 투표 결과를 집계하고, 승리/패배/무승부를 판정하여 사용자 통계를 업데이트합니다.
     *
     * @param debatePost 결산할 토론 게시글
     */
    @Transactional
    public void settleDebate(DebatePost debatePost) {
        log.info("토론 결산 시작 - 게시글 ID: {}", debatePost.getId());

        // 1. 투표 집계
        DebateVoteResultDto voteResult = getVoteResultOptimized(debatePost.getId());
        long player1Votes = voteResult.getPlayer1Count();
        long player2Votes = voteResult.getPlayer2Count();

        log.info("투표 결과 - Player1: {}, Player2: {}", player1Votes, player2Votes);

        // 2. 승리 진영 판정
        DebateSide winningSide = null;
        boolean isDraw = player1Votes == player2Votes;

        if (!isDraw) {
            winningSide = player1Votes > player2Votes ? DebateSide.PLAYER_1 : DebateSide.PLAYER_2;
            log.info("승리 진영: {}", winningSide);
        } else {
            log.info("무승부로 결산");
        }

        // 3. writer 통계 업데이트
        updateWriterStats(debatePost.getWriterId(), DebateSide.PLAYER_1, winningSide, isDraw);

        // 4. coWriter 통계 업데이트 (존재하는 경우)
        if (debatePost.getCoWriterId() != null) {
            updateWriterStats(debatePost.getCoWriterId(), DebateSide.PLAYER_2, winningSide, isDraw);
        }

        // 5. 판결 작성자(댓글 작성자) 통계 업데이트
        updateJudgementStats(debatePost, winningSide, isDraw);

        log.info("토론 결산 완료 - 게시글 ID: {}", debatePost.getId());
    }

    /**
     * 토론 작성자(writer/coWriter)의 승패 통계를 업데이트합니다.
     *
     * @param userId 사용자 ID
     * @param userSide 사용자의 토론 진영
     * @param winningSide 승리 진영 (무승부인 경우 null)
     * @param isDraw 무승부 여부
     */
    private void updateWriterStats(Long userId, DebateSide userSide, DebateSide winningSide, boolean isDraw) {
        userRepository.findById(userId).ifPresent(user -> {
            if (isDraw) {
                user.incrementDebateDraws();
                log.debug("무승부 통계 업데이트 - 사용자 ID: {}", userId);
            } else if (userSide == winningSide) {
                user.incrementDebateWins();
                log.debug("승리 통계 업데이트 - 사용자 ID: {}", userId);
            } else {
                user.incrementDebateLosses();
                log.debug("패배 통계 업데이트 - 사용자 ID: {}", userId);
            }
        });
    }

    /**
     * 판결 작성자(댓글 작성자)들의 판결 성공/실패 통계를 업데이트합니다.
     * 무승부인 경우에는 판결 통계를 업데이트하지 않습니다.
     *
     * @param debatePost 토론 게시글
     * @param winningSide 승리 진영 (무승부인 경우 null)
     * @param isDraw 무승부 여부
     */
    private void updateJudgementStats(DebatePost debatePost, DebateSide winningSide, boolean isDraw) {
        // 무승부인 경우 판결 통계를 업데이트하지 않음
        if (isDraw) {
            log.debug("무승부로 인해 판결 통계 업데이트 생략");
            return;
        }

        List<DebateComment> comments = debatePost.getComments();

        for (DebateComment comment : comments) {
            userRepository.findById(comment.getWriterId()).ifPresent(user -> {
                if (comment.getDebateSide() == winningSide) {
                    user.incrementJudgementSuccesses();
                    log.debug("판결 성공 - 사용자 ID: {}", comment.getWriterId());
                } else {
                    user.incrementJudgementFailures();
                    log.debug("판결 실패 - 사용자 ID: {}", comment.getWriterId());
                }
            });
        }
    }
}
