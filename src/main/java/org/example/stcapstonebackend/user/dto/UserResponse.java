package org.example.stcapstonebackend.user.dto;

import org.example.stcapstonebackend.user.model.Role;

public record UserResponse(
        Long id,
        String email,
        String nickname,
        Role role
) {
}

