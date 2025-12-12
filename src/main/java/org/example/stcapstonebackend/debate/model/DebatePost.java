package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.common.model.PositionTag;

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
}