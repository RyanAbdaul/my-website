package com.ryan.personal_website_backend.course;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateLessonDTO {

    @NotBlank(message = "Title is required")
    private String title;

    private String videoLink;
}
