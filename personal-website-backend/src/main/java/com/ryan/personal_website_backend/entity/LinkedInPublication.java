package com.ryan.personal_website_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.sql.Timestamp;
import java.util.UUID;

@Entity
@Table(name = "linkedin_publications")
@Getter
@Setter
@NoArgsConstructor
public class LinkedInPublication {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "article_id", nullable = false, unique = true)
    private Article article;

    @Column(nullable = false, length = 32)
    private String status;

    @Column(name = "linkedin_post_urn")
    private String linkedInPostUrn;

    @Column(name = "failure_message", columnDefinition = "TEXT")
    private String failureMessage;

    @Column(name = "published_at")
    private Timestamp publishedAt;
}
