package com.ryan.personal_website_backend.portfolio;

import java.time.LocalDate;
import java.util.UUID;

public record ResponseWorkExperienceDTO(UUID id, String company, String role, LocalDate startDate, LocalDate endDate, String summary, String[] highlights) {}
