package com.ryan.personal_website_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResponseStudentDTO {
    private UUID id;
    private String name;
    private String email;
}