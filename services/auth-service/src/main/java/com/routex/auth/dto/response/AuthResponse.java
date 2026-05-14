package com.routex.auth.dto.response;

import com.routex.auth.entity.Role;
import lombok.Builder;
import java.util.UUID;

@Builder
public record AuthResponse(
    String accessToken,
    String refreshToken,
    String tokenType,
    long expiresIn,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    Role role,
    UUID organizationId
) {}
