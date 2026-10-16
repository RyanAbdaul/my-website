package com.ryan.personal_website_backend.article;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResponseArticleDTO {
    private UUID id;
    private String title;
    private String content;
    private Timestamp createTime;
    private List<String> tags;
}
