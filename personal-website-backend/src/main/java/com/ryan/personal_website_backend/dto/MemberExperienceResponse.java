package com.ryan.personal_website_backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record MemberExperienceResponse(UUID id, String role, String organization, String organizationUrl, String location, LocalDate startDate, LocalDate endDate, String summary, Integer displayOrder) {}
