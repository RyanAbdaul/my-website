package com.ryan.personal_website_backend.dto;

import java.time.Instant;

public record LinkedInConnectionResponse(boolean configured, boolean connected, String personUrn, Instant expiresAt) { }
