package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.debate.mapper.DebatePostMapper;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.example.stcapstonebackend.debate.model.PopularType;
import org.example.stcapstonebackend.debate.model.SearchType;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebatePostService {
    private final DebatePostRepository debatePostRepository;
    private final DebatePostMapper debatePostMapper;
    private final DebateCommentRepository debateCommentRepository;

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

    //  id로 게시글 수정
    @Transactional
    public DebatePostResponse updatePost(DebatePostRequest postDto, Long id) {
        DebatePost post = getPostEntity(id);

        post.update(postDto.title(), postDto.content(), postDto.writer(), postDto.coWriter(), postDto.videoUrl(), postDto.tags());

        return debatePostMapper.toDto(post);
    }

    //   id로 게시글 삭제
    public void deletePost(Long id) {
        debatePostRepository.deleteById(id);
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
}
