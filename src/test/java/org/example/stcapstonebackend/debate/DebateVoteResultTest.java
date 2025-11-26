package org.example.stcapstonebackend.debate;

import org.example.stcapstonebackend.debate.dto.DebateVoteResultDto;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class DebateVoteResultTest {

    @Autowired
    private DebatePostRepository debatePostRepository;

    @Autowired
    private DebateCommentRepository debateCommentRepository;

    @Autowired
    private DebatePostService debatePostService;

    @Test
    @DisplayName("득표 결과 계산 테스트 - 정상 케이스")
    void testGetVoteResult_Success() {
        // Given: 토론 게시글 생성
        DebatePost post = DebatePost.builder()
                .title("테스트 토론")
                .content("테스트 내용")
                .writer("작성자1")
                .coWriter("작성자2")
                .build();
        DebatePost savedPost = debatePostRepository.save(post);

        // PLAYER_1에 3표
        for (int i = 0; i < 3; i++) {
            DebateComment comment = DebateComment.builder()
                    .content("PLAYER_1 지지")
                    .writer("사용자" + i)
                    .debateSide(DebateSide.PLAYER_1)
                    .likes(0)
                    .dislikes(0)
                    .debatePost(savedPost)
                    .build();
            debateCommentRepository.save(comment);
        }

        // PLAYER_2에 2표
        for (int i = 0; i < 2; i++) {
            DebateComment comment = DebateComment.builder()
                    .content("PLAYER_2 지지")
                    .writer("사용자" + (i + 3))
                    .debateSide(DebateSide.PLAYER_2)
                    .likes(0)
                    .dislikes(0)
                    .debatePost(savedPost)
                    .build();
            debateCommentRepository.save(comment);
        }

        // When: 득표 결과 조회
        DebateVoteResultDto result = debatePostService.getVoteResultOptimized(savedPost.getId());

        // Then: 검증
        assertThat(result).isNotNull();
        assertThat(result.getDebatePostId()).isEqualTo(savedPost.getId());
        assertThat(result.getPlayer1Count()).isEqualTo(3);
        assertThat(result.getPlayer2Count()).isEqualTo(2);
        assertThat(result.getTotalCount()).isEqualTo(5);
        assertThat(result.getPlayer1Percent()).isEqualTo(60.0);
        assertThat(result.getPlayer2Percent()).isEqualTo(40.0);
    }

    @Test
    @DisplayName("득표 결과 계산 테스트 - 댓글이 없는 경우")
    void testGetVoteResult_NoComments() {
        // Given: 댓글이 없는 토론 게시글
        DebatePost post = DebatePost.builder()
                .title("테스트 토론")
                .content("테스트 내용")
                .writer("작성자1")
                .coWriter("작성자2")
                .build();
        DebatePost savedPost = debatePostRepository.save(post);

        // When: 득표 결과 조회
        DebateVoteResultDto result = debatePostService.getVoteResultOptimized(savedPost.getId());

        // Then: 검증
        assertThat(result).isNotNull();
        assertThat(result.getPlayer1Count()).isEqualTo(0);
        assertThat(result.getPlayer2Count()).isEqualTo(0);
        assertThat(result.getTotalCount()).isEqualTo(0);
        assertThat(result.getPlayer1Percent()).isEqualTo(0.0);
        assertThat(result.getPlayer2Percent()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("득표 결과 계산 테스트 - 한쪽만 투표가 있는 경우")
    void testGetVoteResult_OneSideOnly() {
        // Given: PLAYER_1만 투표가 있는 토론 게시글
        DebatePost post = DebatePost.builder()
                .title("테스트 토론")
                .content("테스트 내용")
                .writer("작성자1")
                .coWriter("작성자2")
                .build();
        DebatePost savedPost = debatePostRepository.save(post);

        // PLAYER_1에만 5표
        for (int i = 0; i < 5; i++) {
            DebateComment comment = DebateComment.builder()
                    .content("PLAYER_1 지지")
                    .writer("사용자" + i)
                    .debateSide(DebateSide.PLAYER_1)
                    .likes(0)
                    .dislikes(0)
                    .debatePost(savedPost)
                    .build();
            debateCommentRepository.save(comment);
        }

        // When: 득표 결과 조회
        DebateVoteResultDto result = debatePostService.getVoteResultOptimized(savedPost.getId());

        // Then: 검증
        assertThat(result).isNotNull();
        assertThat(result.getPlayer1Count()).isEqualTo(5);
        assertThat(result.getPlayer2Count()).isEqualTo(0);
        assertThat(result.getTotalCount()).isEqualTo(5);
        assertThat(result.getPlayer1Percent()).isEqualTo(100.0);
        assertThat(result.getPlayer2Percent()).isEqualTo(0.0);
    }

    @Test
    @DisplayName("득표 퍼센트 합계는 항상 100% 또는 0%여야 함")
    void testVotePercentagesSum() {
        // Given: 토론 게시글과 댓글 생성
        DebatePost post = DebatePost.builder()
                .title("테스트 토론")
                .content("테스트 내용")
                .writer("작성자1")
                .coWriter("작성자2")
                .build();
        DebatePost savedPost = debatePostRepository.save(post);

        // PLAYER_1에 7표, PLAYER_2에 3표
        for (int i = 0; i < 7; i++) {
            DebateComment comment = DebateComment.builder()
                    .content("PLAYER_1 지지")
                    .writer("사용자" + i)
                    .debateSide(DebateSide.PLAYER_1)
                    .likes(0)
                    .dislikes(0)
                    .debatePost(savedPost)
                    .build();
            debateCommentRepository.save(comment);
        }

        for (int i = 0; i < 3; i++) {
            DebateComment comment = DebateComment.builder()
                    .content("PLAYER_2 지지")
                    .writer("사용자" + (i + 7))
                    .debateSide(DebateSide.PLAYER_2)
                    .likes(0)
                    .dislikes(0)
                    .debatePost(savedPost)
                    .build();
            debateCommentRepository.save(comment);
        }

        // When: 득표 결과 조회
        DebateVoteResultDto result = debatePostService.getVoteResultOptimized(savedPost.getId());

        // Then: 퍼센트 합계는 100% 또는 0% (댓글이 없는 경우)
        double percentSum = result.getPlayer1Percent() + result.getPlayer2Percent();
        assertThat(percentSum).isIn(0.0, 100.0);

        // 댓글이 있는 경우 합계는 100%
        if (result.getTotalCount() > 0) {
            assertThat(percentSum).isEqualTo(100.0);
        }
    }
}

