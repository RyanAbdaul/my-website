package com.ryan.personal_website_backend.portfolio;

import jakarta.validation.constraints.NotBlank;
public record CreateProjectDTO(@NotBlank String title, String description, String imagePath, @NotBlank String projectUrl, Integer displayOrder) {}
