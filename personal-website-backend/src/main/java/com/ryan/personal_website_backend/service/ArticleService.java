package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateArticleDTO;
import com.ryan.personal_website_backend.dto.ResponseArticleDTO;
import com.ryan.personal_website_backend.dto.UpdateArticleDTO;
import com.ryan.personal_website_backend.entity.Article;
import com.ryan.personal_website_backend.entity.Tag;
import com.ryan.personal_website_backend.exception.ArticleNotFoundException;
import com.ryan.personal_website_backend.repository.ArticleRepo;
import com.ryan.personal_website_backend.repository.TagRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.domain.Sort;

import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ArticleService {

    private final ArticleRepo articleRepo;
    private final TagRepo tagRepo;
    private final LinkedInPublicationService linkedInPublicationService;

    public List<ResponseArticleDTO> getAllArticles(String query, List<String> tags, LocalDate from, LocalDate to) {
        Specification<Article> specification = Specification.allOf(
                hasQuery(query), hasAnyTag(tags), createdOnOrAfter(from), createdBefore(to)
        );
        return articleRepo.findAll(specification, Sort.by(Sort.Direction.DESC, "createTime"))
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ResponseArticleDTO getArticleById(UUID id) {
        Article article = articleRepo.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));
        return mapToResponseDTO(article);
    }

    public ResponseArticleDTO createArticle(CreateArticleDTO dto) {
        Article article = new Article();
        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setTags(resolveTags(dto.getTags()));
        Article saved = articleRepo.save(article);
        linkedInPublicationService.publishAsync(saved.getId());
        return mapToResponseDTO(saved);
    }

    public ResponseArticleDTO updateArticle(UUID id, UpdateArticleDTO dto) {
        Article article = articleRepo.findById(id)
                .orElseThrow(() -> new ArticleNotFoundException("Article with ID " + id + " not found"));

        article.setTitle(dto.getTitle());
        article.setContent(dto.getContent());
        article.setTags(resolveTags(dto.getTags()));
        Article updated = articleRepo.save(article);
        return mapToResponseDTO(updated);
    }

    public List<ResponseArticleDTO> getArticlesByTag(String tagName) {
        return articleRepo.findByTagsNameIgnoreCase(tagName)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public void deleteArticle(UUID id) {
        if (!articleRepo.existsById(id)) {
            throw new ArticleNotFoundException("Article with ID " + id + " not found");
        }
        articleRepo.deleteById(id);
    }

    private List<Tag> resolveTags(List<String> tagNames) {
        if (tagNames == null || tagNames.isEmpty()) {
            return new ArrayList<>();
        }
        List<Tag> tags = new ArrayList<>();
        for (String tagName : tagNames) {
            String trimmedName = tagName.trim();
            if (!trimmedName.isEmpty()) {
                Tag tag = tagRepo.findByNameIgnoreCase(trimmedName)
                        .orElseGet(() -> {
                            Tag newTag = new Tag();
                            newTag.setName(trimmedName);
                            return tagRepo.save(newTag);
                        });
                tags.add(tag);
            }
        }
        return tags;
    }

    private ResponseArticleDTO mapToResponseDTO(Article article) {
        List<String> tagNames = article.getTags() != null ?
                article.getTags().stream().map(Tag::getName).toList() : List.of();

        return ResponseArticleDTO.builder()
                .id(article.getId())
                .title(article.getTitle())
                .content(article.getContent())
                .createTime(article.getCreateTime())
                .tags(tagNames)
                .build();
    }

    private Specification<Article> hasQuery(String query) {
        return (root, criteriaQuery, builder) -> {
            if (query == null || query.isBlank()) return builder.conjunction();
            String pattern = "%" + query.trim().toLowerCase() + "%";
            return builder.or(
                    builder.like(builder.lower(root.get("title")), pattern),
                    builder.like(builder.lower(root.get("content")), pattern)
            );
        };
    }

    private Specification<Article> hasAnyTag(List<String> tags) {
        return (root, criteriaQuery, builder) -> {
            if (tags == null || tags.isEmpty()) return builder.conjunction();
            criteriaQuery.distinct(true);
            List<String> normalizedTags = tags.stream().filter(tag -> tag != null && !tag.isBlank())
                    .map(tag -> tag.trim().toLowerCase()).toList();
            if (normalizedTags.isEmpty()) return builder.conjunction();
            return builder.lower(root.join("tags").get("name")).in(normalizedTags);
        };
    }

    private Specification<Article> createdOnOrAfter(LocalDate from) {
        return (root, criteriaQuery, builder) -> from == null ? builder.conjunction()
                : builder.greaterThanOrEqualTo(root.get("createTime"), Timestamp.valueOf(from.atStartOfDay()));
    }

    private Specification<Article> createdBefore(LocalDate to) {
        return (root, criteriaQuery, builder) -> to == null ? builder.conjunction()
                : builder.lessThan(root.get("createTime"), Timestamp.valueOf(to.plusDays(1).atStartOfDay()));
    }
}
