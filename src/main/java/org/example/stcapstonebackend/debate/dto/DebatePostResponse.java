package org.example.stcapstonebackend.debate.dto;

import lombok.Builder;
import org.example.stcapstonebackend.debate.model.DebateTag;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

@Builder
public record DebatePostResponse(
        Long id,
        String title,
        String content,
        String writer,
        String coWriter,
        String videoUrl,
        int views,
        int commentCount,
        Set<DebateTag> tags,
        LocalDateTime createdAt,
        LocalDateTime modifiedAt,
        List<DebateCommentResponse> comments
) {
}
