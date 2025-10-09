package org.example.stcapstonebackend.debate.dto;

import java.time.LocalDateTime;

public record DebatePostResponse(
        Long id,
        String title,
        String content,
        String writer,
        String coWriter,
        LocalDateTime createdTime,
        LocalDateTime modifiedTime
) {
}
