package org.example.stcapstonebackend.findTeam.scheduler;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.example.stcapstonebackend.findTeam.FindTeamPostRepository;
import org.example.stcapstonebackend.findTeam.model.FindTeamPost;
import org.example.stcapstonebackend.findTeam.model.PostStatus;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 팀 찾기 게시글의 자동 만료 및 매칭 처리를 하는 스케줄러입니다.
 * 설정된 주기마다 다음 작업을 수행합니다:
 * 1. PENDING 상태에서 매칭 시간이 지난 게시글을 MATCHED로 변경
 * 2. ACTIVE 상태에서 설정된 만료 시간이 지난 게시글을 EXPIRED로 변경
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostExpirationScheduler {

    private final FindTeamPostRepository findTeamPostRepository;

    @Value("${match.timeout}")
    private Duration matchTimeout;

    /**
     * 매칭 시간이 지난 PENDING 게시글을 MATCHED 상태로 변경합니다.
     * 설정된 주기(${scheduler.match-check-interval})마다 실행됩니다.
     */
    @Scheduled(fixedRateString = "${scheduler.match-check-interval}")
    @Transactional
    public void matchPendingPosts() {
        LocalDateTime now = LocalDateTime.now();

        List<FindTeamPost> pendingPosts = findTeamPostRepository.findExpiredPosts(
                PostStatus.PENDING,
                now
        );

        if (!pendingPosts.isEmpty()) {
            log.info("Found {} posts to match", pendingPosts.size());

            pendingPosts.forEach(post -> {
                post.matched();
                log.info("Post ID: {} matched successfully", post.getId());
            });

            log.info("Total {} posts have been matched", pendingPosts.size());
        }
    }

    /**
     * 생성 후 설정된 만료 시간이 지난 ACTIVE 게시글을 EXPIRED 상태로 변경합니다.
     * 설정된 주기(${scheduler.match-check-interval})마다 실행됩니다.
     */
    @Scheduled(fixedRateString = "${scheduler.match-check-interval}")
    @Transactional
    public void expireActivePosts() {
        LocalDateTime expirationTime = LocalDateTime.now().minus(matchTimeout);

        List<FindTeamPost> activePosts = findTeamPostRepository.findByStatus(PostStatus.ACTIVE);

        List<FindTeamPost> postsToExpire = activePosts.stream()
                .filter(post -> post.getCreatedAt().isBefore(expirationTime))
                .toList();

        if (!postsToExpire.isEmpty()) {
            log.info("Found {} active posts to expire", postsToExpire.size());

            postsToExpire.forEach(post -> {
                post.expire();
                log.info("Post ID: {} expired successfully", post.getId());
            });

            log.info("Total {} active posts have been expired", postsToExpire.size());
        }
    }
}

