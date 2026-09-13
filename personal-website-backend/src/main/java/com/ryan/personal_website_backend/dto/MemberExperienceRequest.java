package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public record MemberExperienceRequest(@NotBlank String role, @NotBlank String organization, String organizationUrl, String location, @NotNull LocalDate startDate, LocalDate endDate, @NotBlank String summary, Integer displayOrder) {}
