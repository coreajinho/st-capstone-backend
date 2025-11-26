package org.example.stcapstonebackend.debate.dto;

import lombok.Builder;
import lombok.Getter;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.example.stcapstonebackend.debate.model.DebatePost;
import org.example.stcapstonebackend.debate.model.DebateSide;

import java.util.List;

@Getter
@Builder
public class DebateVoteResultDto {
    private Long debatePostId;
    private long player1Count;
    private long player2Count;
    private long totalCount;
    private double player1Percent;
    private double player2Percent;

    /**
     * DebatePost 엔티티로부터 득표 결과를 계산하여 DTO를 생성합니다.
     * @param debatePost 토론 게시글
     * @return 득표 결과 DTO
     */
    public static DebateVoteResultDto fromEntity(DebatePost debatePost) {
        List<DebateComment> comments = debatePost.getComments();

        long player1Count = comments.stream()
                .filter(c -> c.getDebateSide() == DebateSide.PLAYER_1)
                .count();

        long player2Count = comments.stream()
                .filter(c -> c.getDebateSide() == DebateSide.PLAYER_2)
                .count();

        long totalCount = player1Count + player2Count;

        double player1Percent = totalCount > 0 ? (player1Count * 100.0 / totalCount) : 0;
        double player2Percent = totalCount > 0 ? (player2Count * 100.0 / totalCount) : 0;

        return DebateVoteResultDto.builder()
                .debatePostId(debatePost.getId())
                .player1Count(player1Count)
                .player2Count(player2Count)
                .totalCount(totalCount)
                .player1Percent(player1Percent)
                .player2Percent(player2Percent)
                .build();
    }

    /**
     * 집계된 투표 수로부터 득표 결과를 계산하여 DTO를 생성합니다. (성능 최적화 버전)
     * @param debatePostId 토론 게시글 ID
     * @param player1Count PLAYER_1 투표 수
     * @param player2Count PLAYER_2 투표 수
     * @return 득표 결과 DTO
     */
    public static DebateVoteResultDto fromCounts(Long debatePostId, long player1Count, long player2Count) {
        long totalCount = player1Count + player2Count;

        double player1Percent = totalCount > 0 ? (player1Count * 100.0 / totalCount) : 0;
        double player2Percent = totalCount > 0 ? (player2Count * 100.0 / totalCount) : 0;

        return DebateVoteResultDto.builder()
                .debatePostId(debatePostId)
                .player1Count(player1Count)
                .player2Count(player2Count)
                .totalCount(totalCount)
                .player1Percent(player1Percent)
                .player2Percent(player2Percent)
                .build();
    }
}

