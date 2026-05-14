package com.routex.auth.dto.request;

import com.routex.auth.entity.Role;
import jakarta.validation.constraints.*;
import java.util.UUID;

public record RegisterRequest(
    @NotBlank @Email String email,
    @NotBlank @Size(min = 8, max = 128) String password,
    @NotBlank @Size(min = 1, max = 100) String firstName,
    @NotBlank @Size(min = 1, max = 100) String lastName,
    @Pattern(regexp = "^\\+?[1-9]\\d{1,14}$") String phone,
    Role role,
    UUID organizationId,
    String deviceInfo,
    String ipAddress
) {}
