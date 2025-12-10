package org.example.stcapstonebackend.findTeam.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.common.model.PositionTag;
import org.example.stcapstonebackend.debate.model.BaseEntity;

import java.time.LocalDateTime;
import java.util.*;

import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 팀 찾기 게시글 엔티티입니다.
 * 리그 오브 레전드 게임 내 포지션별 팀원 모집을 위한 게시글 정보를 담고 있습니다.
 * 게시글은 ACTIVE, PENDING_EXPIRATION, EXPIRED 상태를 가질 수 있습니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Entity(name="find_team_post")
@Builder(toBuilder = true)
public class FindTeamPost extends BaseEntity {

    /**
     * 게시글 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * 게시글 제목
     */
    @Column(nullable = false)
    private String title;

    /**
     * 게시글 내용
     */
    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    /**
     * 작성자명
     */
    @Column(nullable = false)
    private String writer;

    /**
     * 작성자 ID
     */
    @Column(nullable = false)
    private Long writerId;

    /**
     * 게시글 상태 (ACTIVE, PENDING, MATCHED, EXPIRED)
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PostStatus status = PostStatus.ACTIVE;

    /**
     * 매칭 예정 시간 (모든 포지션이 수락되었을 때 설정됨)
     */
    @Column
    private LocalDateTime pendingExpirationAt;

    /**
     * 모집하는 포지션 태그 목록
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "find_team_post_tags", joinColumns = @JoinColumn(name = "find_team_post_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<PositionTag> tags = new HashSet<>();

    /**
     * 수락된 포지션 태그와 요청 ID 매핑
     * Key: PositionTag, Value: 해당 태그를 수락한 요청 ID
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "accepted_tags", joinColumns = @JoinColumn(name = "find_team_post_id"))
    @MapKeyEnumerated(EnumType.STRING)
    @MapKeyColumn(name = "tag")
    @Column(name = "request_id")
    @Builder.Default
    private Map<PositionTag, Long> acceptedTags = new HashMap<>();

    /**
     * 이 게시글에 대한 신청 요청 목록
     */
    @OneToMany(mappedBy = "findTeamPost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<FindTeamRequest> requests = new ArrayList<>();

    /**
     * 게시글에 신청 요청을 추가합니다.
     *
     * @param request 추가할 신청 요청
     */
    public void addRequest(FindTeamRequest request) {
        requests.add(request);
        request.setFindTeamPost(this);
    }

    /**
     * 게시글 정보를 수정합니다.
     *
     * @param title 새 제목
     * @param content 새 내용
     * @param writer 새 작성자명
     * @param writerId 새 작성자 ID
     * @param tags 새 태그 목록 (null이 아닌 경우만 업데이트)
     */
    public void update(String title, String content, String writer, Long writerId, Set<PositionTag> tags) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.writerId = writerId;
        if (tags != null) {
            this.tags = tags;
        }
    }

    /**
     * 특정 신청 요청을 수락하고 해당 태그를 수락 목록에 추가합니다.
     * 모든 태그가 수락되면 게시글 상태를 PENDING으로 변경합니다.
     *
     * @param requestId 수락할 신청 요청 ID
     * @param tag 수락할 포지션 태그
     * @throws IllegalStateException 해당 태그가 이미 수락된 경우
     */
    public void acceptRequest(Long requestId, PositionTag tag) {
        if (acceptedTags.containsKey(tag)) {
            throw new IllegalStateException("Already accepted for this tag");
        }

        acceptedTags.put(tag, requestId);

        if (acceptedTags.size() == tags.size()) {
            this.status = PostStatus.PENDING;
            this.pendingExpirationAt = LocalDateTime.now().plusMinutes(10);
        }
    }

    /**
     * 특정 신청 요청의 수락을 취소합니다.
     * 게시글이 PENDING 상태였다면 ACTIVE 상태로 복원합니다.
     *
     * @param requestId 수락을 취소할 신청 요청 ID
     */
    public void cancelAcceptance(Long requestId) {
        acceptedTags.entrySet()
                .removeIf(entry -> entry.getValue().equals(requestId));

        if (this.status == PostStatus.PENDING) {
            this.status = PostStatus.ACTIVE;
            this.pendingExpirationAt = null;
        }
    }

    /**
     * 게시글을 매칭 완료 상태로 변경합니다.
     * 모든 포지션이 수락된 후 10분이 지나면 호출됩니다.
     */
    public void matched() {
        this.status = PostStatus.MATCHED;
    }

    /**
     * 게시글을 만료 상태로 변경합니다.
     * 생성 후 1일이 지난 ACTIVE 게시글이 호출됩니다.
     */
    public void expire() {
        this.status = PostStatus.EXPIRED;
    }

    /**
     * 특정 태그가 이미 수락되었는지 확인합니다.
     *
     * @param tag 확인할 포지션 태그
     * @return 태그가 수락되었으면 true, 아니면 false
     */
    public boolean isTagAccepted(PositionTag tag) {
        return acceptedTags.containsKey(tag);
    }

    /**
     * 아직 수락되지 않은 태그 목록을 반환합니다.
     *
     * @return 사용 가능한 포지션 태그 목록
     */
    public Set<PositionTag> getAvailableTags() {
        Set<PositionTag> available = new HashSet<>(tags);
        available.removeAll(acceptedTags.keySet());
        return available;
    }
}
