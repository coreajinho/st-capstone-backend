package org.example.stcapstonebackend.summoner.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record SummonerSearchRequestDto(
        @NotBlank(message = "소환사 이름과 태그는 비워둘 수 없습니다.")
        @Pattern(regexp = "^.+#.+$", message = "소환사 이름 형식이 올바르지 않습니다 (예: 닉네임#태그).")
        String fullname
) {
}
