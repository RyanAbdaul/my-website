package com.ryan.personal_website_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "linkedin_connections")
@Getter
@Setter
@NoArgsConstructor
public class LinkedInConnection {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "encrypted_access_token", columnDefinition = "TEXT")
    private String encryptedAccessToken;

    @Column(name = "person_urn")
    private String personUrn;

    @Column(name = "access_token_expires_at")
    private Timestamp accessTokenExpiresAt;

    @Column(name = "oauth_state")
    private String oauthState;

    @Column(name = "oauth_state_expires_at")
    private Timestamp oauthStateExpiresAt;
}
