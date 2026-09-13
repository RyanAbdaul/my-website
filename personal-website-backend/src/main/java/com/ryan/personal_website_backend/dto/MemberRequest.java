package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MemberRequest(
        @NotBlank String slug,
        @NotBlank String name,
        String headline,
        String biography,
        String photoUrl,
        String email,
        String password
) {}
