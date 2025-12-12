package org.example.stcapstonebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * CoWriter 검증 요청을 위한 DTO입니다.
 * 사용자가 입력한 riotName과 riotTag로 회원가입 여부를 확인합니다.
 *
 * @param riotName 라이엇 게임 닉네임
 * @param riotTag 라이엇 태그 (예: KR1)
 */
public record CoWriterValidationRequest(
        @NotBlank @Size(min = 2, max = 50) String riotName,
        @NotBlank @Size(min = 2, max = 10) String riotTag
) {
}


