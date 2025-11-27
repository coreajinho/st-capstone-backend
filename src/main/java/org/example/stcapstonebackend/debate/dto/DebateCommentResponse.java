package org.example.stcapstonebackend.debate.dto;

import lombok.Builder;
import org.example.stcapstonebackend.debate.model.DebateSide;

import java.time.LocalDateTime;

@Builder
public record DebateCommentResponse(
        Long id,
        String content,
        String writer,
        DebateSide debateSide,
        Integer likes,
        Integer dislikes,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt
) {
}
