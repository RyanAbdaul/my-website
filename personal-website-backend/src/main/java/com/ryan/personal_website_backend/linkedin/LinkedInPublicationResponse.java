package com.ryan.personal_website_backend.linkedin;

import java.time.Instant;
import java.util.UUID;

public record LinkedInPublicationResponse(UUID articleId, String status, String linkedInPostUrn, String failureMessage, Instant publishedAt) { }
