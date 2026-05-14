package com.routex.auth.controller;

import com.routex.auth.dto.request.*;
import com.routex.auth.dto.response.*;
import com.routex.auth.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Authentication", description = "JWT Authentication & Authorization endpoints")
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(summary = "Register a new user")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(summary = "Login with email and password")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh access token")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshTokenRequest request) {
        return ResponseEntity.ok(authService.refreshToken(request));
    }

    @PostMapping("/logout")
    @Operation(summary = "Logout and revoke tokens", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> logout(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(required = false) String refreshToken) {
        String accessToken = authHeader.substring(7);
        authService.logout(accessToken, refreshToken);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/validate")
    @Operation(summary = "Validate JWT token (used by API Gateway)")
    public ResponseEntity<TokenValidationResponse> validate(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.substring(7);
        return ResponseEntity.ok(authService.validateToken(token));
    }

    @PostMapping("/change-password")
    @Operation(summary = "Change user password", security = @SecurityRequirement(name = "bearerAuth"))
    public ResponseEntity<Void> changePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody ChangePasswordRequest request) {
        UUID userId = UUID.fromString(userDetails.getUsername());
        authService.changePassword(userId, request);
        return ResponseEntity.noContent().build();
    }
}
