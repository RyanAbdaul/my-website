package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.LinkedInPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LinkedInPublicationRepo extends JpaRepository<LinkedInPublication, UUID> {
    Optional<LinkedInPublication> findByArticleId(UUID articleId);
}
