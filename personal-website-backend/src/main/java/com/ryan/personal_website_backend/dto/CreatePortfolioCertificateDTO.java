package com.ryan.personal_website_backend.dto;

import jakarta.validation.constraints.NotBlank;

import java.time.LocalDate;

public record CreatePortfolioCertificateDTO(
        @NotBlank String title,
        String issuer,
        LocalDate issuedDate,
        @NotBlank String imagePath,
        Integer displayOrder
) {}
