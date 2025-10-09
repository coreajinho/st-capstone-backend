package org.example.stcapstonebackend.debate.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DebatePostRequest(
        @Size(min=1, max=50) @NotBlank String title,
        @Size(min=1) @NotBlank String content,
        @Size(min=5, max = 15) @NotBlank String writer,
        @Size(min=5, max = 15) @NotBlank String coWriter
) {
}
