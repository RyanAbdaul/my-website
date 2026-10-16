package com.ryan.personal_website_backend.course;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import java.math.BigDecimal;
import com.ryan.personal_website_backend.course.CourseStatus;
@Getter
@Setter
public class CreateCourseDTO {

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal price;

    @NotBlank
    private String title;

    @NotBlank
    private String description;

    private String thumbnailLink;

    private CourseStatus status = CourseStatus.ACTIVE;
}
