package org.example.stcapstonebackend.debate.model;

import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString
@Entity(name="debate_post")
@Builder(toBuilder = true)
public class DebatePost extends BaseEntity{

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

    @OneToMany(mappedBy = "debatePost", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default // Builder 사용 시 기본값 설정
    private List<DebateComment> comments = new ArrayList<>();

    public void addComment(DebateComment comment) {
        comments.add(comment);
        comment.setDebatePost(this);
    }

    public void update(String title, String content, String writer, String coWriter) {
        this.title = title;
        this.content = content;
        this.writer = writer;
        this.coWriter = coWriter;
    }
}