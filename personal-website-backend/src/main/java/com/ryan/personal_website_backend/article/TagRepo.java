package com.ryan.personal_website_backend.article;

import com.ryan.personal_website_backend.article.Tag;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface TagRepo extends JpaRepository<Tag, UUID> {
    Optional<Tag> findByNameIgnoreCase(String name);
    Boolean existsByNameIgnoreCase(String name);
}
