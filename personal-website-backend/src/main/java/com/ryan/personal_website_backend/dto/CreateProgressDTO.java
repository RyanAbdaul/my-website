package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateProgressDTO {

    @NotNull(message = "Student ID is required")
    private UUID studentId;

    @NotNull(message = "Lesson ID is required")
    private UUID lessonId;
}
