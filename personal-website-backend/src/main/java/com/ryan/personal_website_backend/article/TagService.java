package com.ryan.personal_website_backend.article;

import com.ryan.personal_website_backend.article.CreateTagDTO;
import com.ryan.personal_website_backend.article.ResponseTagDTO;
import com.ryan.personal_website_backend.article.Tag;
import com.ryan.personal_website_backend.common.TagAlreadyExistsException;
import com.ryan.personal_website_backend.common.TagNotFoundException;
import com.ryan.personal_website_backend.article.TagRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

import com.ryan.personal_website_backend.article.Article;
import org.springframework.transaction.annotation.Transactional;

@Service
@AllArgsConstructor
public class TagService {

    private final TagRepo tagRepo;

    public List<ResponseTagDTO> getAllTags() {
        return tagRepo.findAll()
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ResponseTagDTO getTagById(UUID id) {
        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag with ID " + id + " not found"));
        return mapToResponseDTO(tag);
    }

    public ResponseTagDTO createTag(CreateTagDTO dto) {
        String trimmedName = dto.getName().trim();
        if (tagRepo.existsByNameIgnoreCase(trimmedName)) {
            throw new TagAlreadyExistsException("Tag with name '" + trimmedName + "' already exists");
        }

        Tag tag = new Tag();
        tag.setName(trimmedName);

        Tag saved = tagRepo.save(tag);
        return mapToResponseDTO(saved);
    }

    @Transactional
    public void deleteTag(UUID id) {
        Tag tag = tagRepo.findById(id)
                .orElseThrow(() -> new TagNotFoundException("Tag with ID " + id + " not found"));

        if (tag.getArticles() != null && !tag.getArticles().isEmpty()) {
            for (Article article : tag.getArticles()) {
                article.getTags().remove(tag);
            }
        }
        tagRepo.delete(tag);
    }

    private ResponseTagDTO mapToResponseDTO(Tag tag) {
        return ResponseTagDTO.builder()
                .id(tag.getId())
                .name(tag.getName())
                .build();
    }
}
