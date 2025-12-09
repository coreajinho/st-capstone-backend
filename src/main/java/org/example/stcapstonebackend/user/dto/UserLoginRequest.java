package org.example.stcapstonebackend.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record UserLoginRequest(
        @NotBlank(message = "아이디는 필수입니다")
        String username,

        @NotBlank(message = "비밀번호는 필수입니다")
        String password
) {
}

