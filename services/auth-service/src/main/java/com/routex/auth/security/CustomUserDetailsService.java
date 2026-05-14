package com.routex.auth.security;

import com.routex.auth.entity.User;
import com.routex.auth.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String userIdOrEmail) throws UsernameNotFoundException {
        User user;
        try {
            java.util.UUID id = java.util.UUID.fromString(userIdOrEmail);
            user = userRepository.findById(id)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userIdOrEmail));
        } catch (IllegalArgumentException e) {
            user = userRepository.findByEmailAndDeletedFalse(userIdOrEmail)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + userIdOrEmail));
        }

        if (!user.isEnabled() || user.isDeleted()) {
            throw new DisabledException("Account is disabled");
        }

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getId().toString())
                .password(user.getPasswordHash())
                .authorities(user.getRole().getAuthorities())
                .accountExpired(false)
                .accountLocked(!user.isAccountNonLocked())
                .credentialsExpired(false)
                .disabled(!user.isEnabled())
                .build();
    }
}
