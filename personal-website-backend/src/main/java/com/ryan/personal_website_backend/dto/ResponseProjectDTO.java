package com.ryan.personal_website_backend.dto;

import java.util.UUID;

public record ResponseProjectDTO(UUID id, String title, String description, String imagePath, String projectUrl, Integer displayOrder) {}
