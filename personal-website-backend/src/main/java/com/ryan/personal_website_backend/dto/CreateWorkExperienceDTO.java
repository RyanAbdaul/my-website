package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDate;
import java.util.List;

public record CreateWorkExperienceDTO(
        @NotBlank String company,
        @NotBlank String role,
        @NotNull LocalDate startDate,
        LocalDate endDate,
        @NotBlank String summary,
        List<String> highlights,
        Integer displayOrder
) {}
