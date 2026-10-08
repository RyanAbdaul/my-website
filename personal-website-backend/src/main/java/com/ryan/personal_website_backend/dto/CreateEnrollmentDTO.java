package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter
@Setter
public class CreateEnrollmentDTO {
    @NotNull
    private UUID studentId;

    @NotNull
    private UUID courseId;
}
