package org.example.stcapstonebackend.user.dto;

import lombok.Builder;

/**
 * CoWriter 검증 응답을 위한 DTO입니다.
 *
 * @param isValid 회원가입 여부
 * @param userId 사용자 ID (회원인 경우)
 * @param displayName 표시될 이름 (riotName#riotTag 형식)
 */
@Builder
public record CoWriterValidationResponse(
        boolean isValid,
        Long userId,
        String displayName
) {
}
