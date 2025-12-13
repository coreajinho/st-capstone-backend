package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.common.model.PositionTag;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Entity(name="debate_post")
@Builder(toBuilder = true)
public class DebatePost extends BaseEntity{

//    필드
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Column(nullable = false)
    private String writer;

    @Column(nullable = false)
    private Long writerId;

    private String coWriter;

    private Long coWriterId;

    private String videoUrl;

    @Column(nullable = false)
    @Builder.Default
    private int views = 0;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private DebateStatus debateStatus = DebateStatus.ACTIVE;

    @Column(nullable = false)
    private Long debateDurationHours;

    @Column(nullable = false)
    private LocalDateTime expiresAt;

    @Column(nullable = false)
    @Builder.Default
    private Long totalExtensionTimeHours = 0L;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "debate_post_tags", joinColumns = @JoinColumn(name = "debate_post_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<PositionTag> tags = new HashSet<>();

    @OneToMany(mappedBy = "debatePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 기본값 설정
    private List<DebateComment> comments = new ArrayList<>();

//    헬퍼 메소드
    public void addComment(DebateComment comment) {
        comments.add(comment);
        comment.setDebatePost(this);
    }

    public void incrementViews() {
        this.views++;
    }

    /**
     * 게시글 정보를 업데이트합니다.
     *
     * @param title 제목
     * @param content 내용
     * @param writerId 작성자 ID
     * @param coWriterId 공동 작성자 ID
     * @param videoUrl 비디오 URL
     * @param tags 포지션 태그 목록
     */
    public void update(String title, String content, Long writerId, Long coWriterId, String videoUrl, Set<PositionTag> tags) {
        this.title = title;
        this.content = content;
        this.writerId = writerId;
        this.coWriterId = coWriterId;
        this.videoUrl = videoUrl;
        if (tags != null) {
            this.tags = tags;
        }
    }

    /**
     * 토론을 PENDING 상태로 전환하고 만료 시간을 연장합니다.
     *
     * @param extensionHours 연장할 시간 (시간 단위)
     */
    public void markAsPending(long extensionHours) {
        this.debateStatus = DebateStatus.PENDING;
        this.expiresAt = this.expiresAt.plusHours(extensionHours);
        this.totalExtensionTimeHours += extensionHours;
    }

    /**
     * 토론을 EXPIRED 상태로 전환합니다.
     */
    public void markAsExpired() {
        this.debateStatus = DebateStatus.EXPIRED;
    }

    /**
     * 추가 연장이 가능한지 확인합니다.
     * 총 연장 시간이 원래 토론 기간을 초과하지 않았는지 체크합니다.
     *
     * @param extensionHours 연장하려는 시간 (시간 단위)
     * @return 연장 가능 여부
     */
    public boolean canExtend(long extensionHours) {
        return (this.totalExtensionTimeHours + extensionHours) <= this.debateDurationHours;
    }
}