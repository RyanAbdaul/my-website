package com.ryan.personal_website_backend.dto;

import java.time.LocalDate;
import java.util.UUID;

public record ResponsePortfolioCertificateDTO(UUID id, String title, String issuer, LocalDate issuedDate, String imagePath) {}
