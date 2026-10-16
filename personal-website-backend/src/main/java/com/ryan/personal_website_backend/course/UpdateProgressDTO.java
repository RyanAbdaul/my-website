package com.ryan.personal_website_backend.course;

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateProgressDTO {

    @NotNull(message = "isComplete is required")
    private Boolean isComplete;
}
