package com.routex.auth.repository;

import com.routex.auth.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmailAndDeletedFalse(String email);
    boolean existsByEmail(String email);
}
