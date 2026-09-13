package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.*;

import lombok.Getter;

import lombok.Setter;

@Setter
@Getter
public class CreateStudentDTO {
    @NotBlank
    @Pattern(regexp = "^[a-zA-Z ]+$")
    private String name;

    @Email
    @NotBlank
    private String email;

    @NotBlank
    @Size(min = 8, message = "Password must be at least 8 characters")
    private String password;
}
