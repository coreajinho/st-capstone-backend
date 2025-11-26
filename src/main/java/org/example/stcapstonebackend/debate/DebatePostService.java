package org.example.stcapstonebackend.debate;

import lombok.RequiredArgsConstructor;
import org.example.stcapstonebackend.debate.dto.DebatePostRequest;
import org.example.stcapstonebackend.debate.dto.DebatePostResponse;
import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.exception.DebatePostNotFoundException;
import org.example.stcapstonebackend.debate.mapper.DebatePostMapper;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
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
                .orElseThrow( () -> new DebatePostNotFoundException("해당id의 게시글이 없습니다. "));
    }

    //--------------------------서비스 메소드--------------------------------------------------
    public DebatePostResponse createPost(DebatePostRequest postDto) {
        DebatePost post = debatePostMapper.toEntity(postDto);
        DebatePost savedPost = debatePostRepository.save(post);
        return debatePostMapper.toDto(savedPost);
    }

    @Transactional
    public DebatePostResponse updatePost(DebatePostRequest postDto, Long id) {
        DebatePost post = getPostEntity(id);

        post.update(postDto.title(), postDto.content(), postDto.writer(), postDto.coWriter());

        return debatePostMapper.toDto(post);
    }

    public void deletePost(Long id){
        debatePostRepository.deleteById(id);
    }

    public DebatePostResponse getPost(Long id) {
        DebatePost post = getPostEntity(id);
        return debatePostMapper.toDto(post);
    }

    /**
     * 특정 토론 게시글의 득표 결과를 조회합니다. (일반 버전 - 엔티티에서 계산)
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
}
