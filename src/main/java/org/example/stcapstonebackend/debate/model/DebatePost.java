package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.*;
import lombok.*;

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
    private String coWriter;

    private String videoUrl;

    @Column(nullable = false)
    private int views = 0;

    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "debate_post_tags", joinColumns = @JoinColumn(name = "debate_post_id"))
    @Column(name = "tag")
    @Builder.Default
    private Set<DebateTag> tags = new HashSet<>();

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

    public void update(String title, String content, String writer, String coWriter, String videoUrl, Set<DebateTag> tags) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.coWriter = coWriter;
        this.videoUrl = videoUrl;
        if (tags != null) {
            this.tags = tags;
        }
    }
}