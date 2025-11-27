package org.example.stcapstonebackend.debate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.stcapstonebackend.debate.model.DebateSide;

public record DebateCommentRequest(
        @NotBlank String content,
        @NotBlank String writer,
        @NotNull DebateSide debateSide
) {
}
