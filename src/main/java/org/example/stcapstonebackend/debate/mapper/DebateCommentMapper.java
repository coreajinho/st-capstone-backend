package org.example.stcapstonebackend.debate.mapper;

import org.example.stcapstonebackend.debate.dto.DebateCommentResponse;
import org.example.stcapstonebackend.debate.model.DebateComment;
import org.springframework.stereotype.Component;

@Component
public class DebateCommentMapper {

    public DebateCommentResponse toDto(DebateComment debateComment) {
        return new DebateCommentResponse(
                debateComment.getId(),
                debateComment.getContent(),
                debateComment.getWriter(),
                debateComment.getDebateSide(),
                debateComment.getLikes(),
                debateComment.getDislikes(),
                debateComment.getCreatedAt(),
                debateComment.getModifiedAt()
        );
    }
}
