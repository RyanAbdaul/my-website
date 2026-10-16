package com.ryan.personal_website_backend.article;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CreateTagDTO {

    @NotBlank(message = "Tag name is required")
    private String name;
}
