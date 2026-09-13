package com.ryan.personal_website_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResponseTagDTO {
    private UUID id;
    private String name;
}
