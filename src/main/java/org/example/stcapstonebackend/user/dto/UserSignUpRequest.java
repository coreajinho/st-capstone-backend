package org.example.stcapstonebackend.user.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserSignUpRequest(
        @NotBlank(message = "아이디는 필수입니다")
        @Size(min = 4, max = 20, message = "아이디는 4자 이상 20자 이하여야 합니다")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다")
        @Size(min = 8, max = 20, message = "비밀번호는 8자 이상 20자 이하여야 합니다")
        String password,

        @NotBlank(message = "라이엇 게임 이름은 필수입니다")
        @Size(min = 1, max = 20, message = "라이엇 게임 이름은 1자 이상 20자 이하여야 합니다")
        String riotName,

        @NotBlank(message = "라이엇 태그는 필수입니다")
        @Size(min = 3, max = 5, message = "라이엇 태그는 3자 이상 5자 이하여야 합니다")
        String riotTag
) {
}

