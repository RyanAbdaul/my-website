package com.ryan.personal_website_backend.linkedin;

import com.ryan.personal_website_backend.linkedin.LinkedInConnectionResponse;
import com.ryan.personal_website_backend.linkedin.LinkedInPublicationResponse;
import com.ryan.personal_website_backend.linkedin.LinkedInPublicationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/linkedin")
@RequiredArgsConstructor
public class LinkedInController {
    private final LinkedInPublicationService linkedInPublicationService;

    @GetMapping("/connection")
    public LinkedInConnectionResponse connection() { return linkedInPublicationService.connectionStatus(); }

    @PostMapping("/authorization-url")
    public Map<String, String> authorizationUrl() { return Map.of("authorizationUrl", linkedInPublicationService.authorizationUrl()); }

    @GetMapping("/callback")
    public ResponseEntity<Map<String, String>> callback(
            @RequestParam(required = false) String code,
            @RequestParam(required = false) String state,
            @RequestParam(required = false) String error,
            @RequestParam(name = "error_description", required = false) String errorDescription) {
        if (error != null) {
            return ResponseEntity.badRequest().body(Map.of("error", error, "message", errorDescription == null ? "LinkedIn declined the authorization request." : errorDescription));
        }
        if (code == null || state == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "invalid_callback", "message", "LinkedIn did not return an authorization code."));
        }
        try {
            linkedInPublicationService.completeAuthorization(code, state);
            return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
        } catch (RestClientResponseException exception) {
            return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(Map.of(
                    "error", "linkedin_token_exchange_failed",
                    "message", "LinkedIn rejected the authorization exchange (HTTP " + exception.getStatusCode().value() + "). Verify the Client ID, regenerated Client Secret, and redirect URI.",
                    "linkedinResponse", truncate(exception.getResponseBodyAsString())
            ));
        }
    }

    @GetMapping("/articles/{articleId}")
    public LinkedInPublicationResponse publication(@PathVariable UUID articleId) { return linkedInPublicationService.publicationStatus(articleId); }

    @PostMapping("/articles/{articleId}/publish")
    public LinkedInPublicationResponse publish(@PathVariable UUID articleId) { return linkedInPublicationService.publish(articleId); }

    private String truncate(String value) {
        if (value == null || value.isBlank()) return "No response body was returned by LinkedIn.";
        return value.substring(0, Math.min(value.length(), 500));
    }
}
