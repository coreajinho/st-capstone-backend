package org.example.stcapstonebackend.user.model;

import jakarta.persistence.*;
import lombok.*;
import org.example.stcapstonebackend.debate.model.BaseEntity;

import static jakarta.persistence.GenerationType.IDENTITY;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Entity(name = "users")
@Builder(toBuilder = true)
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;


    @Column(nullable = false, length = 50)
    private String riotName;

    @Column(nullable = false, length = 10)
    private String riotTag;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    @Column(nullable = false)
    @Builder.Default
    private Integer debateWins = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer debateLosses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer debateDraws = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer judgementSuccesses = 0;

    @Column(nullable = false)
    @Builder.Default
    private Integer judgementFailures = 0;

    /**
     * 토론 승리 횟수를 1 증가시킵니다.
     */
    public void incrementDebateWins() {
        this.debateWins++;
    }

    /**
     * 토론 패배 횟수를 1 증가시킵니다.
     */
    public void incrementDebateLosses() {
        this.debateLosses++;
    }

    /**
     * 토론 무승부 횟수를 1 증가시킵니다.
     */
    public void incrementDebateDraws() {
        this.debateDraws++;
    }

    /**
     * 판결 성공 횟수를 1 증가시킵니다.
     */
    public void incrementJudgementSuccesses() {
        this.judgementSuccesses++;
    }

    /**
     * 판결 실패 횟수를 1 증가시킵니다.
     */
    public void incrementJudgementFailures() {
        this.judgementFailures++;
    }
}

