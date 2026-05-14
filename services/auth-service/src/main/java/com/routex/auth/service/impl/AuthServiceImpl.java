package com.routex.auth.service.impl;

import com.routex.auth.dto.request.*;
import com.routex.auth.dto.response.*;
import com.routex.auth.entity.*;
import com.routex.auth.exception.*;
import com.routex.auth.kafka.AuthEventProducer;
import com.routex.auth.repository.*;
import com.routex.auth.security.JwtTokenProvider;
import com.routex.auth.service.AuthService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.authentication.*;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtTokenProvider jwtTokenProvider;
    private final PasswordEncoder passwordEncoder;
    private final RedisTemplate<String, Object> redisTemplate;
    private final AuthEventProducer authEventProducer;
    private final AuthenticationManager authenticationManager;

    @Value("${app.redis.token-blacklist-prefix}")
    private String blacklistPrefix;

    @Value("${app.redis.refresh-token-prefix}")
    private String refreshTokenPrefix;

    @Value("${app.jwt.refresh-token-expiry-ms}")
    private long refreshTokenExpiryMs;

    @Value("${app.security.rate-limit.login-attempts}")
    private int maxLoginAttempts;

    @Value("${app.security.rate-limit.lockout-duration-minutes}")
    private int lockoutDurationMinutes;

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException("Email already registered: " + request.email());
        }

        User user = User.builder()
                .email(request.email().toLowerCase())
                .passwordHash(passwordEncoder.encode(request.password()))
                .firstName(request.firstName())
                .lastName(request.lastName())
                .phone(request.phone())
                .role(Optional.ofNullable(request.role()).orElse(Role.CUSTOMER))
                .organizationId(request.organizationId())
                .enabled(true)
                .emailVerified(false)
                .build();

        user = userRepository.save(user);
        log.info("Registered new user: {} with role: {}", user.getEmail(), user.getRole());

        authEventProducer.publishUserRegistered(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user, request.deviceInfo(), request.ipAddress());

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse login(LoginRequest request) {
        User user = userRepository.findByEmailAndDeletedFalse(request.email().toLowerCase())
                .orElseThrow(() -> new InvalidCredentialsException("Invalid credentials"));

        // Check account lock
        if (!user.isAccountNonLocked()) {
            throw new AccountLockedException("Account locked until: " + user.getLockedUntil());
        }

        // Authenticate
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(request.email().toLowerCase(), request.password())
            );
        } catch (BadCredentialsException e) {
            handleFailedLogin(user);
            throw new InvalidCredentialsException("Invalid credentials");
        }

        // Reset failed attempts on success
        user.setFailedLoginAttempts(0);
        user.setLockedUntil(null);
        user.setLastLoginAt(Instant.now());
        userRepository.save(user);

        String accessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken refreshToken = createRefreshToken(user, request.deviceInfo(), request.ipAddress());

        log.info("User logged in: {}", user.getEmail());
        authEventProducer.publishUserLoggedIn(user);

        return buildAuthResponse(user, accessToken, refreshToken.getToken());
    }

    @Override
    @Transactional
    public AuthResponse refreshToken(RefreshTokenRequest request) {
        RefreshToken refreshToken = refreshTokenRepository.findByToken(request.refreshToken())
                .orElseThrow(() -> new InvalidTokenException("Refresh token not found"));

        if (!refreshToken.isValid()) {
            throw new InvalidTokenException("Refresh token is expired or revoked");
        }

        User user = refreshToken.getUser();
        if (!user.isEnabled() || user.isDeleted()) {
            throw new AccountDisabledException("User account is disabled");
        }

        // Rotate refresh token
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        String newAccessToken = jwtTokenProvider.generateAccessToken(user);
        RefreshToken newRefreshToken = createRefreshToken(user, refreshToken.getDeviceInfo(), refreshToken.getIpAddress());

        log.info("Refreshed tokens for user: {}", user.getEmail());
        return buildAuthResponse(user, newAccessToken, newRefreshToken.getToken());
    }

    @Override
    @Transactional
    public void logout(String accessToken, String refreshTokenValue) {
        // Blacklist the access token in Redis
        try {
            String jti = jwtTokenProvider.extractJti(accessToken);
            long ttl = jwtTokenProvider.extractExpiration(accessToken).getTime() - System.currentTimeMillis();
            if (ttl > 0) {
                redisTemplate.opsForValue().set(
                        blacklistPrefix + jti,
                        "revoked",
                        Duration.ofMillis(ttl)
                );
            }
        } catch (Exception e) {
            log.warn("Could not blacklist access token: {}", e.getMessage());
        }

        // Revoke refresh token
        if (refreshTokenValue != null) {
            refreshTokenRepository.findByToken(refreshTokenValue).ifPresent(rt -> {
                rt.setRevoked(true);
                refreshTokenRepository.save(rt);
            });
        }

        log.info("User logged out, tokens invalidated");
    }

    @Override
    public TokenValidationResponse validateToken(String token) {
        if (!jwtTokenProvider.isTokenValid(token)) {
            return TokenValidationResponse.invalid("Token is invalid or expired");
        }

        // Check blacklist
        String jti = jwtTokenProvider.extractJti(token);
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistPrefix + jti))) {
            return TokenValidationResponse.invalid("Token has been revoked");
        }

        return TokenValidationResponse.valid(
                jwtTokenProvider.extractUserId(token),
                jwtTokenProvider.extractEmail(token),
                jwtTokenProvider.extractRole(token)
        );
    }

    @Override
    @Transactional
    public void changePassword(UUID userId, ChangePasswordRequest request) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new UserNotFoundException("User not found"));

        if (!passwordEncoder.matches(request.currentPassword(), user.getPasswordHash())) {
            throw new InvalidCredentialsException("Current password is incorrect");
        }

        user.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(user);

        // Revoke all refresh tokens
        refreshTokenRepository.revokeAllByUser(user);
        log.info("Password changed for user: {}", user.getEmail());
    }

    // ─── Private helpers ───────────────────────────────────────────────────────

    private RefreshToken createRefreshToken(User user, String deviceInfo, String ipAddress) {
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token(jwtTokenProvider.generateRefreshTokenValue())
                .expiresAt(Instant.now().plusMillis(refreshTokenExpiryMs))
                .deviceInfo(deviceInfo)
                .ipAddress(ipAddress)
                .build();
        return refreshTokenRepository.save(refreshToken);
    }

    private void handleFailedLogin(User user) {
        user.setFailedLoginAttempts(user.getFailedLoginAttempts() + 1);
        if (user.getFailedLoginAttempts() >= maxLoginAttempts) {
            user.setLockedUntil(Instant.now().plus(Duration.ofMinutes(lockoutDurationMinutes)));
            log.warn("Account locked for user: {} after {} failed attempts", user.getEmail(), user.getFailedLoginAttempts());
        }
        userRepository.save(user);
    }

    private AuthResponse buildAuthResponse(User user, String accessToken, String refreshToken) {
        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMs() / 1000)
                .userId(user.getId())
                .email(user.getEmail())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .role(user.getRole())
                .organizationId(user.getOrganizationId())
                .build();
    }
}
