package com.ryan.personal_website_backend.auth;

import com.ryan.personal_website_backend.auth.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepo extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    boolean existsByAuthorities_Authority(String authority);
    Optional<User> findFirstByAuthorities_AuthorityAndDisplayNameIsNotNull(String authority);
}
