package org.example.stcapstonebackend.findTeam.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.common.model.PositionTag;
import org.example.stcapstonebackend.debate.model.BaseEntity;

import static jakarta.persistence.FetchType.LAZY;
import static jakarta.persistence.GenerationType.IDENTITY;

/**
 * 팀 찾기 게시글에 대한 신청 요청 엔티티입니다.
 * 사용자가 특정 게시글에 특정 포지션으로 신청할 때 생성됩니다.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@ToString(exclude = "findTeamPost")
@Entity(name="find_team_request")
@Builder(toBuilder = true)
public class FindTeamRequest extends BaseEntity {

    /**
     * 신청 요청 고유 식별자
     */
    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    /**
     * 신청 내용
     */
    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    /**
     * 신청자명
     */
    @Column(nullable = false)
    private String writer;

    /**
     * 신청자 ID
     */
    @Column(nullable = false)
    private Long writerId;

    /**
     * 희망하는 포지션 태그
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private PositionTag desiredTag;

    /**
     * 수락 여부
     */
    @Column(nullable = false)
    @Builder.Default
    private Boolean isAccepted = false;

    /**
     * 연관된 팀 찾기 게시글
     */
    @ManyToOne(fetch = LAZY)
    @JoinColumn(name = "find_team_post_id")
    private FindTeamPost findTeamPost;

    /**
     * 연관된 게시글을 설정합니다.
     *
     * @param findTeamPost 연관될 팀 찾기 게시글
     */
    public void setFindTeamPost(FindTeamPost findTeamPost) {
        this.findTeamPost = findTeamPost;
    }

    /**
     * 신청 요청 정보를 수정합니다.
     *
     * @param content 새 신청 내용
     * @param writer 새 신청자명
     * @param writerId 새 신청자 ID
     * @param desiredTag 새 희망 포지션 태그
     */
    public void update(String content, String writer, Long writerId, PositionTag desiredTag) {
        this.content = content;
        this.writer = writer;
        this.writerId = writerId;
        this.desiredTag = desiredTag;
    }

    /**
     * 신청 요청을 수락 상태로 변경합니다.
     */
    public void accept() {
        this.isAccepted = true;
    }

    /**
     * 신청 요청의 수락을 취소합니다.
     */
    public void cancelAccept() {
        this.isAccepted = false;
    }
}
