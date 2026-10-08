package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record UpdateAdminProfileDTO(
        @NotBlank String name,
        String headline,
        String biography,
        String photoUrl
) {}
