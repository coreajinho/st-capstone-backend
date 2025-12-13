package org.example.stcapstonebackend.debate.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.debate.DebateCommentRepository;
import org.example.stcapstonebackend.debate.DebatePostRepository;
import org.example.stcapstonebackend.debate.DebatePostService;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;
import org.example.stcapstonebackend.debate.model.DebateStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 토론 게시글의 자동 만료 및 연장 처리를 하는 스케줄러입니다.
 * 설정된 주기마다 만료된 토론을 확인하고, 동점인 경우 연장하거나 결산을 수행합니다.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DebateExpirationScheduler {

    private final DebatePostRepository debatePostRepository;
    private final DebateCommentRepository debateCommentRepository;
    private final DebatePostService debatePostService;

    @Value("${debate.pending-extension-duration:PT1H}")
    private Duration pendingExtensionDuration;

    /**
     * 만료된 토론 게시글을 처리합니다.
     * 설정된 주기(${scheduler.match-check-interval} 또는 ${scheduler.match-check-interval-override})마다 실행됩니다.
     *
     * 처리 로직:
     * 1. ACTIVE 상태에서 만료 시간이 지난 게시글 조회
     * 2. 투표 결과가 동점인 경우:
     *    - 연장 가능하면 PENDING 상태로 전환 및 1시간 연장
     *    - 연장 불가능하면 EXPIRED 처리 및 결산
     * 3. 투표 결과가 차이 나는 경우: EXPIRED 처리 및 결산
     * 4. PENDING 상태에서 만료된 게시글도 동일하게 처리
     */
    @Scheduled(fixedRateString = "${scheduler.match-check-interval-override:${scheduler.match-check-interval}}")
    @Transactional
    public void expireDebatePosts() {
        LocalDateTime now = LocalDateTime.now();
        long extensionHours = pendingExtensionDuration.toHours();

        log.info("토론 만료 스케줄러 실행 - 현재 시간: {}", now);

        // 1. ACTIVE 상태에서 만료된 게시글 처리
        List<DebatePost> expiredActivePosts = debatePostRepository
                .findByDebateStatusAndExpiresAtBefore(DebateStatus.ACTIVE, now);

        if (!expiredActivePosts.isEmpty()) {
            log.info("만료된 ACTIVE 게시글 발견: {} 건", expiredActivePosts.size());
            expiredActivePosts.forEach(post -> processExpiredPost(post, extensionHours));
        }

        // 2. PENDING 상태에서 만료된 게시글 처리
        List<DebatePost> expiredPendingPosts = debatePostRepository
                .findByDebateStatusAndExpiresAtBefore(DebateStatus.PENDING, now);

        if (!expiredPendingPosts.isEmpty()) {
            log.info("만료된 PENDING 게시글 발견: {} 건", expiredPendingPosts.size());
            expiredPendingPosts.forEach(post -> processExpiredPost(post, extensionHours));
        }

        log.info("토론 만료 스케줄러 완료");
    }

    /**
     * 만료된 게시글을 처리합니다.
     * 동점 여부와 연장 가능 여부를 확인하여 적절한 상태로 전환합니다.
     *
     * @param post 처리할 게시글
     * @param extensionHours 연장 시간 (시간 단위)
     */
    private void processExpiredPost(DebatePost post, long extensionHours) {
        log.info("게시글 처리 시작 - ID: {}, 상태: {}, 만료 시간: {}",
                post.getId(), post.getDebateStatus(), post.getExpiresAt());

        // 투표 집계
        List<DebateCommentRepository.DebateVoteCount> voteCounts =
                debateCommentRepository.countByDebateSide(post.getId());

        Map<DebateSide, Long> countMap = voteCounts.stream()
                .collect(Collectors.toMap(
                        DebateCommentRepository.DebateVoteCount::getSide,
                        DebateCommentRepository.DebateVoteCount::getCount
                ));

        long player1Votes = countMap.getOrDefault(DebateSide.PLAYER_1, 0L);
        long player2Votes = countMap.getOrDefault(DebateSide.PLAYER_2, 0L);

        log.info("투표 결과 - ID: {}, Player1: {}, Player2: {}",
                post.getId(), player1Votes, player2Votes);

        // 동점 여부 확인
        boolean isTied = player1Votes == player2Votes;

        if (isTied && post.canExtend(extensionHours)) {
            // 동점이고 연장 가능한 경우: PENDING 상태로 전환 및 연장
            post.markAsPending(extensionHours);
            log.info("토론 연장 - ID: {}, 새 만료 시간: {}, 총 연장 시간: {} 시간",
                    post.getId(), post.getExpiresAt(), post.getTotalExtensionTimeHours());
        } else {
            // 승부가 갈리거나 연장 불가능한 경우: EXPIRED 처리 및 결산
            post.markAsExpired();
            debatePostService.settleDebate(post);
            log.info("토론 만료 및 결산 완료 - ID: {}, 최종 상태: EXPIRED", post.getId());
        }
    }
}

