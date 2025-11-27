package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.*;
import lombok.*;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "debatePost")
@Entity(name="debate_comment")
@Builder(toBuilder = true)
public class DebateComment extends BaseEntity{

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Column(nullable = false)
    private String writer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DebateSide debateSide;

    @Builder.Default
    @Column(nullable = false)
    private Integer likes = 0;

    @Builder.Default
    @Column(nullable = false)
    private Integer dislikes = 0;

    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "debate_post_id")
    private DebatePost debatePost;

    public void setDebatePost(DebatePost debatePost) {
        this.debatePost = debatePost;
    }

    public void update(String content, String writer) {
        this.content = content;
        this.writer = writer;
    }

    // TODO: 좋아요/싫어요 기능 - 나중에 구현 예정
    /*
    public void incrementLikes() {
        this.likes++;
    }

    public void decrementLikes() {
        if (this.likes > 0) {
            this.likes--;
        }
    }

    public void incrementDislikes() {
        this.dislikes++;
    }

    public void decrementDislikes() {
        if (this.dislikes > 0) {
            this.dislikes--;
        }
    }
    */
}
