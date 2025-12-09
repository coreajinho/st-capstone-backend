package org.example.stcapstonebackend.user.dto;

import org.example.stcapstonebackend.user.model.Role;

public record UserResponse(
        Long id,
        String username,
        String riotName,
        String riotTag,
        Role role
) {
}

