package com.ryan.personal_website_backend.dto;

import java.util.List;
import java.util.UUID;

public record MemberProfileResponse(
        UUID id,
        String slug,
        String name,
        String headline,
        String biography,
        String photoUrl,
        String email,
        List<MemberExperienceResponse> experiences,
        List<ResponseProjectDTO> projects,
        List<ResponsePortfolioCertificateDTO> certificates,
        List<ResponseArticleDTO> articles,
        List<ResponseCourseDTO> courses
) {}
