package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Article;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;
import java.util.UUID;

public interface ArticleRepo extends JpaRepository<Article, UUID>, JpaSpecificationExecutor<Article> {
    List<Article> findByTagsNameIgnoreCase(String tagName);
}
