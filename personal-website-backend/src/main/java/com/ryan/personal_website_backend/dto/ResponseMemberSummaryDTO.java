package com.ryan.personal_website_backend.dto;

import java.util.UUID;

public record ResponseMemberSummaryDTO(UUID id, String slug, String name, String headline, String photoUrl) {
}
