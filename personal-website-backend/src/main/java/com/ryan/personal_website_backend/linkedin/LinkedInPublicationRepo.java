package com.ryan.personal_website_backend.linkedin;

import com.ryan.personal_website_backend.linkedin.LinkedInPublication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface LinkedInPublicationRepo extends JpaRepository<LinkedInPublication, UUID> {
    Optional<LinkedInPublication> findByArticleId(UUID articleId);
}
