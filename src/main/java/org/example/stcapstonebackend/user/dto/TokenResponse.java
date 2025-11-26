package org.example.stcapstonebackend.user.dto;

public record TokenResponse(
        String accessToken,
        String tokenType,
        Long expiresIn
) {
    public TokenResponse(String accessToken, Long expiresIn) {
        this(accessToken, "Bearer", expiresIn);
    }
}

