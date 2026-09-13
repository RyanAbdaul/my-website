package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import java.util.UUID;
import java.util.List;
@Getter
@Setter
public class UpdateCourseDTO {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    private String thumbnailLink;

    @NotBlank
    private String title;

    @NotBlank
    private String description;
    private List<UUID> memberIds;
}
