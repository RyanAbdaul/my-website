package com.ryan.personal_website_backend.dto;

import java.util.UUID;

public record AdminProfileResponse(
        UUID id,
        String name,
        String headline,
        String biography,
        String photoUrl
) {}
