package com.ryan.personal_website_backend.student;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UpdateStudentDTO {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private String name;

    @Email
    @NotBlank
    private String email;

}
