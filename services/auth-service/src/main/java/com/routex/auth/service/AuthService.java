package com.routex.auth.service;

import com.routex.auth.dto.request.*;
import com.routex.auth.dto.response.*;
import java.util.UUID;

public interface AuthService {
    AuthResponse register(RegisterRequest request);
    AuthResponse login(LoginRequest request);
    AuthResponse refreshToken(RefreshTokenRequest request);
    void logout(String accessToken, String refreshToken);
    TokenValidationResponse validateToken(String token);
    void changePassword(UUID userId, ChangePasswordRequest request);
}
