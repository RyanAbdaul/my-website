package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.CreateArticleDTO;
import com.ryan.personal_website_backend.dto.ResponseArticleDTO;
import com.ryan.personal_website_backend.dto.UpdateArticleDTO;
import com.ryan.personal_website_backend.service.ArticleService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.time.LocalDate;
import org.springframework.format.annotation.DateTimeFormat;
@RestController
@AllArgsConstructor
public class ArticleController {

    private final ArticleService articleService;

    @GetMapping("/articles")
    public List<ResponseArticleDTO> getAllArticles(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) List<String> tags,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return articleService.getAllArticles(query, tags, from, to);
    }

    @GetMapping("/articles/{id}")
    public ResponseArticleDTO getArticleById(@PathVariable UUID id) {
        return articleService.getArticleById(id);
    }

    @GetMapping("/articles/tag/{tagName}")
    public List<ResponseArticleDTO> getArticlesByTag(@PathVariable String tagName) {
        return articleService.getArticlesByTag(tagName);
    }

    @PostMapping("/admin/articles")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseArticleDTO createArticle(@Valid @RequestBody CreateArticleDTO dto) {
        return articleService.createArticle(dto);
    }

    @PutMapping("/admin/articles/{id}")
    public ResponseArticleDTO updateArticle(@PathVariable UUID id, @Valid @RequestBody UpdateArticleDTO dto) {
        return articleService.updateArticle(id, dto);
    }

    @DeleteMapping("/admin/articles/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteArticle(@PathVariable UUID id) {
        articleService.deleteArticle(id);
    }
}