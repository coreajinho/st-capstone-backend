package org.example.stcapstonebackend.debate;

import jakarta.persistence.EntityManager;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.example.stcapstonebackend.debate.model.DebateStatus;
import org.example.stcapstonebackend.debate.scheduler.DebateExpirationScheduler;
import org.example.stcapstonebackend.user.UserRepository;
import org.example.stcapstonebackend.user.model.Role;
import org.example.stcapstonebackend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 토론 만료 및 결산 통합 테스트입니다.
 * 테스트 프로필을 사용하여 H2 인메모리 DB와 짧은 만료 시간으로 테스트합니다.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
class DebateExpirationIntegrationTest {

    @Autowired
    private DebatePostRepository debatePostRepository;

    @Autowired
    private DebateCommentRepository debateCommentRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DebateExpirationScheduler debateExpirationScheduler;

    @Autowired
    private EntityManager entityManager;

    private User writer;
    private User coWriter;
    private User judge1;
    private User judge2;
    private User judge3;

    @BeforeEach
    void setUp() {
        // Given: 테스트용 사용자 생성
        writer = createUser("writer" + System.currentTimeMillis(), "Writer", "KR1");
        coWriter = createUser("cowriter" + System.currentTimeMillis(), "CoWriter", "KR2");
        judge1 = createUser("judge1" + System.currentTimeMillis(), "Judge1", "KR3");
        judge2 = createUser("judge2" + System.currentTimeMillis(), "Judge2", "KR4");
        judge3 = createUser("judge3" + System.currentTimeMillis(), "Judge3", "KR5");
    }

    @Test
    @DisplayName("Player1 승리 시나리오 - 작성자와 판결 작성자의 통계가 정확히 업데이트되어야 함")
    void testPlayer1Win_shouldUpdateStatsCorrectly() {
        // Given: Player1이 이기는 토론 게시글 생성 (3:1)
        DebatePost post = createDebatePost(writer, coWriter);

        // Player1에 3표
        createComment(post, judge1, DebateSide.PLAYER_1, "Player1 지지 1");
        createComment(post, judge2, DebateSide.PLAYER_1, "Player1 지지 2");
        createComment(post, judge3, DebateSide.PLAYER_1, "Player1 지지 3");

        // Player2에 1표
        User judge4 = createUser("judge4" + System.currentTimeMillis(), "Judge4", "KR6");
        createComment(post, judge4, DebateSide.PLAYER_2, "Player2 지지 1");

        // 만료 시간을 과거로 설정
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        setExpiresAtToPast(post);

        // 현재 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // When: 스케줄러 실행
        debateExpirationScheduler.expireDebatePosts();

        // 스케줄러 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // Then: 게시글 상태 확인
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        assertThat(post.getDebateStatus()).isEqualTo(DebateStatus.EXPIRED);

        // Writer (Player1) 승리 통계 확인
        writer = userRepository.findById(writer.getId()).orElseThrow();
        assertThat(writer.getDebateWins()).isEqualTo(1);
        assertThat(writer.getDebateLosses()).isEqualTo(0);
        assertThat(writer.getDebateDraws()).isEqualTo(0);

        // CoWriter (Player2) 패배 통계 확인
        coWriter = userRepository.findById(coWriter.getId()).orElseThrow();
        assertThat(coWriter.getDebateWins()).isEqualTo(0);
        assertThat(coWriter.getDebateLosses()).isEqualTo(1);
        assertThat(coWriter.getDebateDraws()).isEqualTo(0);

        // 판결 작성자 통계 확인
        judge1 = userRepository.findById(judge1.getId()).orElseThrow();
        assertThat(judge1.getJudgementSuccesses()).isEqualTo(1);
        assertThat(judge1.getJudgementFailures()).isEqualTo(0);

        judge2 = userRepository.findById(judge2.getId()).orElseThrow();
        assertThat(judge2.getJudgementSuccesses()).isEqualTo(1);
        assertThat(judge2.getJudgementFailures()).isEqualTo(0);

        judge3 = userRepository.findById(judge3.getId()).orElseThrow();
        assertThat(judge3.getJudgementSuccesses()).isEqualTo(1);
        assertThat(judge3.getJudgementFailures()).isEqualTo(0);

        judge4 = userRepository.findById(judge4.getId()).orElseThrow();
        assertThat(judge4.getJudgementSuccesses()).isEqualTo(0);
        assertThat(judge4.getJudgementFailures()).isEqualTo(1);
    }

    @Test
    @DisplayName("Player2 승리 시나리오 - CoWriter가 승리하고 통계가 정확히 업데이트되어야 함")
    void testPlayer2Win_shouldUpdateStatsCorrectly() {
        // Given: Player2가 이기는 토론 게시글 생성 (1:2)
        DebatePost post = createDebatePost(writer, coWriter);

        // Player1에 1표
        createComment(post, judge1, DebateSide.PLAYER_1, "Player1 지지");

        // Player2에 2표
        createComment(post, judge2, DebateSide.PLAYER_2, "Player2 지지 1");
        createComment(post, judge3, DebateSide.PLAYER_2, "Player2 지지 2");

        // 만료 시간을 과거로 설정
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        setExpiresAtToPast(post);

        // 현재 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // When: 스케줄러 실행
        debateExpirationScheduler.expireDebatePosts();

        // 스케줄러 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // Then: Writer (Player1) 패배 통계 확인
        writer = userRepository.findById(writer.getId()).orElseThrow();
        assertThat(writer.getDebateWins()).isEqualTo(0);
        assertThat(writer.getDebateLosses()).isEqualTo(1);
        assertThat(writer.getDebateDraws()).isEqualTo(0);

        // CoWriter (Player2) 승리 통계 확인
        coWriter = userRepository.findById(coWriter.getId()).orElseThrow();
        assertThat(coWriter.getDebateWins()).isEqualTo(1);
        assertThat(coWriter.getDebateLosses()).isEqualTo(0);
        assertThat(coWriter.getDebateDraws()).isEqualTo(0);
    }

    @Test
    @DisplayName("무승부 시나리오 - 동점이고 연장 불가능한 경우 무승부로 처리되어야 함")
    void testDraw_shouldUpdateDrawStats() {
        // Given: 동점 토론 게시글 생성 (2:2)
        DebatePost post = createDebatePost(writer, coWriter);

        // Player1에 2표
        createComment(post, judge1, DebateSide.PLAYER_1, "Player1 지지 1");
        createComment(post, judge2, DebateSide.PLAYER_1, "Player1 지지 2");

        // Player2에 2표
        createComment(post, judge3, DebateSide.PLAYER_2, "Player2 지지 1");
        User judge4 = createUser("judge4" + System.currentTimeMillis(), "Judge4", "KR6");
        createComment(post, judge4, DebateSide.PLAYER_2, "Player2 지지 2");

        // 만료 시간을 과거로 설정하고 연장 불가능하도록 설정
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        // totalExtensionTimeHours를 debateDurationHours와 같게 만들어 연장 불가능하게 함
        DebatePost updatedPost = post.toBuilder()
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .totalExtensionTimeHours(post.getDebateDurationHours())
                .build();
        debatePostRepository.save(updatedPost);

        // 현재 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // When: 스케줄러 실행
        debateExpirationScheduler.expireDebatePosts();

        // 스케줄러 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // Then: 무승부 통계 확인
        writer = userRepository.findById(writer.getId()).orElseThrow();
        assertThat(writer.getDebateWins()).isEqualTo(0);
        assertThat(writer.getDebateLosses()).isEqualTo(0);
        assertThat(writer.getDebateDraws()).isEqualTo(1);

        coWriter = userRepository.findById(coWriter.getId()).orElseThrow();
        assertThat(coWriter.getDebateWins()).isEqualTo(0);
        assertThat(coWriter.getDebateLosses()).isEqualTo(0);
        assertThat(coWriter.getDebateDraws()).isEqualTo(1);

        // 무승부인 경우 판결 통계는 업데이트되지 않음
        judge1 = userRepository.findById(judge1.getId()).orElseThrow();
        assertThat(judge1.getJudgementSuccesses()).isEqualTo(0);
        assertThat(judge1.getJudgementFailures()).isEqualTo(0);
    }

    @Test
    @DisplayName("동점 연장 시나리오 - 동점이고 연장 가능한 경우 PENDING 상태로 전환되어야 함")
    void testTieExtension_shouldTransitionToPending() {
        // Given: 동점 토론 게시글 생성 (1:1)
        DebatePost post = createDebatePost(writer, coWriter);

        // Player1에 1표
        createComment(post, judge1, DebateSide.PLAYER_1, "Player1 지지");

        // Player2에 1표
        createComment(post, judge2, DebateSide.PLAYER_2, "Player2 지지");

        // 만료 시간을 과거로 설정 (연장 가능)
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        setExpiresAtToPast(post);

        // 현재 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // When: 스케줄러 실행
        debateExpirationScheduler.expireDebatePosts();

        // 스케줄러 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // Then: PENDING 상태로 전환되고 연장되어야 함
        post = debatePostRepository.findById(post.getId()).orElseThrow();
        assertThat(post.getDebateStatus()).isEqualTo(DebateStatus.PENDING);
        assertThat(post.getTotalExtensionTimeHours()).isGreaterThan(0);
        assertThat(post.getExpiresAt()).isAfter(LocalDateTime.now());

        // 아직 결산되지 않았으므로 통계는 변하지 않음
        writer = userRepository.findById(writer.getId()).orElseThrow();
        assertThat(writer.getDebateWins()).isEqualTo(0);
        assertThat(writer.getDebateLosses()).isEqualTo(0);
        assertThat(writer.getDebateDraws()).isEqualTo(0);
    }

    @Test
    @DisplayName("CoWriter가 없는 게시글 - Writer만 통계가 업데이트되어야 함")
    void testWithoutCoWriter_shouldUpdateOnlyWriterStats() {
        // Given: CoWriter 없이 토론 게시글 생성 (Player1만 참여)
        DebatePost post = DebatePost.builder()
                .title("테스트 토론 - CoWriter 없음")
                .content("테스트 내용")
                .writer(writer.getRiotName() + "#" + writer.getRiotTag())
                .writerId(writer.getId())
                .coWriter(null)
                .coWriterId(null)
                .debateStatus(DebateStatus.ACTIVE)
                .debateDurationHours(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        post = debatePostRepository.save(post);

        // Player1에 2표
        createComment(post, judge1, DebateSide.PLAYER_1, "Player1 지지 1");
        createComment(post, judge2, DebateSide.PLAYER_1, "Player1 지지 2");

        // 만료 시간을 과거로 설정
        setExpiresAtToPast(post);

        // 현재 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // When: 스케줄러 실행
        debateExpirationScheduler.expireDebatePosts();

        // 스케줄러 트랜잭션의 변경사항을 DB에 반영하고 캐시 비우기
        entityManager.flush();
        entityManager.clear();

        // Then: Writer만 통계 확인
        writer = userRepository.findById(writer.getId()).orElseThrow();
        assertThat(writer.getDebateWins()).isEqualTo(1);

        // CoWriter는 영향 받지 않음
        coWriter = userRepository.findById(coWriter.getId()).orElseThrow();
        assertThat(coWriter.getDebateWins()).isEqualTo(0);
        assertThat(coWriter.getDebateLosses()).isEqualTo(0);
    }

    // ==================== 헬퍼 메서드 ====================

    /**
     * 테스트용 사용자를 생성합니다.
     */
    private User createUser(String username, String riotName, String riotTag) {
        User user = User.builder()
                .username(username)
                .password("password")
                .riotName(riotName)
                .riotTag(riotTag)
                .role(Role.USER)
                .debateWins(0)
                .debateLosses(0)
                .debateDraws(0)
                .judgementSuccesses(0)
                .judgementFailures(0)
                .build();
        return userRepository.save(user);
    }

    /**
     * 테스트용 토론 게시글을 생성합니다.
     */
    private DebatePost createDebatePost(User writer, User coWriter) {
        DebatePost post = DebatePost.builder()
                .title("테스트 토론")
                .content("테스트 내용")
                .writer(writer.getRiotName() + "#" + writer.getRiotTag())
                .writerId(writer.getId())
                .coWriter(coWriter.getRiotName() + "#" + coWriter.getRiotTag())
                .coWriterId(coWriter.getId())
                .debateStatus(DebateStatus.ACTIVE)
                .debateDurationHours(1L)
                .expiresAt(LocalDateTime.now().plusHours(1))
                .build();
        return debatePostRepository.save(post);
    }

    /**
     * 테스트용 댓글을 생성합니다.
     */
    private DebateComment createComment(DebatePost post, User writer, DebateSide side, String content) {
        DebateComment comment = DebateComment.builder()
                .content(content)
                .writer(writer.getRiotName() + "#" + writer.getRiotTag())
                .writerId(writer.getId())
                .debateSide(side)
                .likes(0)
                .dislikes(0)
                .debatePost(post)
                .build();
        return debateCommentRepository.save(comment);
    }

    /**
     * 게시글의 만료 시간을 과거로 설정합니다.
     */
    private void setExpiresAtToPast(DebatePost post) {
        DebatePost updatedPost = post.toBuilder()
                .expiresAt(LocalDateTime.now().minusMinutes(1))
                .build();
        debatePostRepository.save(updatedPost);
    }
}

