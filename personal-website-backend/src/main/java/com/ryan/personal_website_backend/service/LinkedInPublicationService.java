package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.LinkedInConnectionResponse;
import com.ryan.personal_website_backend.dto.LinkedInPublicationResponse;
import com.ryan.personal_website_backend.entity.Article;
import com.ryan.personal_website_backend.entity.LinkedInConnection;
import com.ryan.personal_website_backend.entity.LinkedInPublication;
import com.ryan.personal_website_backend.entity.Tag;
import com.ryan.personal_website_backend.repository.ArticleRepo;
import com.ryan.personal_website_backend.repository.LinkedInConnectionRepo;
import com.ryan.personal_website_backend.repository.LinkedInPublicationRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestClient;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.sql.Timestamp;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class LinkedInPublicationService {

    private static final String NOT_CONNECTED = "NOT_CONNECTED";
    private static final String PENDING = "PENDING";
    private static final String PUBLISHED = "PUBLISHED";
    private static final String FAILED = "FAILED";

    private final LinkedInConnectionRepo connectionRepo;
    private final LinkedInPublicationRepo publicationRepo;
    private final ArticleRepo articleRepo;
    private final LinkedInTokenCipher tokenCipher;
    private final RestClient restClient = RestClient.create();

    @Value("${app.linkedin.client-id:}")
    private String clientId;

    @Value("${app.linkedin.client-secret:}")
    private String clientSecret;

    @Value("${app.linkedin.redirect-uri:}")
    private String redirectUri;

    @Value("${app.linkedin.api-version:202603}")
    private String apiVersion;

    @Value("${app.public-site-url}")
    private String publicSiteUrl;

    public LinkedInConnectionResponse connectionStatus() {
        LinkedInConnection connection = connectionRepo.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        boolean connected = connection != null
                && connection.getEncryptedAccessToken() != null
                && connection.getAccessTokenExpiresAt() != null
                && connection.getAccessTokenExpiresAt()
                .toInstant()
                .isAfter(Instant.now());

        return new LinkedInConnectionResponse(
                isConfigured(),
                connected,
                connection == null ? null : connection.getPersonUrn(),
                connection == null || connection.getAccessTokenExpiresAt() == null
                        ? null
                        : connection.getAccessTokenExpiresAt().toInstant()
        );
    }

    public String authorizationUrl() {
        requireConfigured();

        LinkedInConnection connection = connectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseGet(LinkedInConnection::new);

        connection.setOauthState(UUID.randomUUID().toString());

        connection.setOauthStateExpiresAt(
                Timestamp.from(
                        Instant.now().plus(10, ChronoUnit.MINUTES)
                )
        );

        connectionRepo.save(connection);

        return "https://www.linkedin.com/oauth/v2/authorization"
                + "?response_type=code"
                + "&client_id=" + encode(clientId)
                + "&redirect_uri=" + encode(redirectUri)
                + "&state=" + encode(connection.getOauthState())
                + "&scope=" + encode("openid profile w_member_social");
    }

    public void completeAuthorization(String code, String state) {
        requireConfigured();

        LinkedInConnection connection = connectionRepo.findAll()
                .stream()
                .findFirst()
                .orElseThrow(() ->
                        new IllegalArgumentException(
                                "No LinkedIn authorization request is pending"
                        )
                );

        if (connection.getOauthState() == null
                || !connection.getOauthState().equals(state)
                || connection.getOauthStateExpiresAt() == null
                || connection.getOauthStateExpiresAt()
                .toInstant()
                .isBefore(Instant.now())) {

            throw new IllegalArgumentException(
                    "The LinkedIn authorization request is invalid or has expired"
            );
        }

        /*
         * Exchange authorization code for access token.
         *
         * LinkedIn requires:
         * Content-Type: application/x-www-form-urlencoded
         */
        MultiValueMap<String, String> form =
                new LinkedMultiValueMap<>();

        form.add("grant_type", "authorization_code");
        form.add("code", code);
        form.add("redirect_uri", redirectUri);
        form.add("client_id", clientId);
        form.add("client_secret", clientSecret);

        Map<?, ?> token = restClient.post()
                .uri("https://www.linkedin.com/oauth/v2/accessToken")
                .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                .body(form)
                .retrieve()
                .body(Map.class);

        String accessToken = requiredString(token, "access_token");

        Number expiresIn = (Number) token.get("expires_in");

        /*
         * Get LinkedIn member information.
         *
         * OpenID Connect userinfo endpoint.
         */
        Map<?, ?> profile = restClient.get()
                .uri("https://api.linkedin.com/v2/userinfo")
                .header(
                        HttpHeaders.AUTHORIZATION,
                        "Bearer " + accessToken
                )
                .retrieve()
                .body(Map.class);

        /*
         * Store the access token encrypted.
         */
        connection.setEncryptedAccessToken(
                tokenCipher.encrypt(accessToken)
        );

        /*
         * LinkedIn's userinfo response contains "sub",
         * which is the member's subject identifier.
         */
        connection.setPersonUrn(
                "urn:li:person:" + requiredString(profile, "sub")
        );

        connection.setAccessTokenExpiresAt(
                Timestamp.from(
                        Instant.now().plus(
                                expiresIn == null
                                        ? 60
                                        : expiresIn.longValue(),
                                ChronoUnit.SECONDS
                        )
                )
        );

        connection.setOauthState(null);
        connection.setOauthStateExpiresAt(null);

        connectionRepo.save(connection);
    }

    @Async
    @Transactional
    public void publishAsync(UUID articleId) {
        try {
            publish(articleId);
        } catch (Exception ignored) {
            // The failure is persisted by publish;
            // article publishing must never fail because LinkedIn is unavailable.
        }
    }

    @Transactional
    public LinkedInPublicationResponse publish(UUID articleId) {

        Article article = articleRepo.findById(articleId)
                .orElseThrow(() ->
                        new IllegalArgumentException("Article not found")
                );

        LinkedInPublication publication =
                publicationRepo.findByArticleId(articleId)
                        .orElseGet(() -> {
                            LinkedInPublication created =
                                    new LinkedInPublication();

                            created.setArticle(article);

                            return created;
                        });

        if (PUBLISHED.equals(publication.getStatus())) {
            return response(publication);
        }

        LinkedInConnection connection = connectionRepo.findAll()
                .stream()
                .findFirst()
                .orElse(null);

        if (connection == null
                || connection.getEncryptedAccessToken() == null
                || connection.getAccessTokenExpiresAt() == null
                || connection.getAccessTokenExpiresAt()
                .toInstant()
                .isBefore(Instant.now())) {

            publication.setStatus(NOT_CONNECTED);

            publication.setFailureMessage(
                    "Connect a valid LinkedIn account before publishing."
            );

            return response(
                    publicationRepo.save(publication)
            );
        }

        publication.setStatus(PENDING);
        publication.setFailureMessage(null);

        publicationRepo.save(publication);

        try {

            Map<String, Object> post = new LinkedHashMap<>();

            post.put(
                    "author",
                    connection.getPersonUrn()
            );

            post.put(
                    "commentary",
                    commentary(article)
            );

            post.put(
                    "visibility",
                    "PUBLIC"
            );

            post.put(
                    "distribution",
                    Map.of(
                            "feedDistribution",
                            "MAIN_FEED",

                            "targetEntities",
                            List.of(),

                            "thirdPartyDistributionChannels",
                            List.of()
                    )
            );

            post.put(
                    "lifecycleState",
                    "PUBLISHED"
            );

            post.put(
                    "isReshareDisabledByAuthor",
                    false
            );

            String postUrn = restClient.post()
                    .uri("https://api.linkedin.com/rest/posts")
                    .header(
                            HttpHeaders.AUTHORIZATION,
                            "Bearer "
                                    + tokenCipher.decrypt(
                                    connection.getEncryptedAccessToken()
                            )
                    )
                    .header(
                            "Linkedin-Version",
                            apiVersion
                    )
                    .header(
                            "X-Restli-Protocol-Version",
                            "2.0.0"
                    )
                    .contentType(MediaType.APPLICATION_JSON)
                    .body(post)
                    .retrieve()
                    .toBodilessEntity()
                    .getHeaders()
                    .getFirst("x-restli-id");

            publication.setStatus(PUBLISHED);

            publication.setLinkedInPostUrn(postUrn);

            publication.setPublishedAt(
                    Timestamp.from(Instant.now())
            );

        } catch (Exception exception) {

            publication.setStatus(FAILED);

            publication.setFailureMessage(
                    message(exception)
            );
        }

        return response(
                publicationRepo.save(publication)
        );
    }

    public LinkedInPublicationResponse publicationStatus(
            UUID articleId
    ) {
        return publicationRepo.findByArticleId(articleId)
                .map(this::response)
                .orElse(
                        new LinkedInPublicationResponse(
                                articleId,
                                NOT_CONNECTED,
                                null,
                                "No LinkedIn publish attempt exists yet.",
                                null
                        )
                );
    }

    private String commentary(Article article) {

        String tags = article.getTags()
                .stream()
                .map(Tag::getName)
                .map(tag ->
                        "#" + tag.replaceAll(
                                "[^A-Za-z0-9]",
                                ""
                        )
                )
                .filter(tag -> tag.length() > 1)
                .reduce(
                        "",
                        (left, right) ->
                                left + " " + right
                );

        String excerpt = article.getContent() == null
                ? ""
                : article.getContent()
                .replaceAll(
                        "[#*_>`\\-]",
                        " "
                )
                .replaceAll(
                        "\\s+",
                        " "
                )
                .trim();

        if (excerpt.length() > 260) {
            excerpt = excerpt.substring(0, 257) + "...";
        }

        return article.getTitle()
                + "\n\n"
                + excerpt
                + "\n\nEnjoyed this article? Visit my website for more insights and updates: "
                + publicSiteUrl.replaceAll("/$", "")
                + "/articles/"
                + article.getId()
                + tags;
    }

    private LinkedInPublicationResponse response(
            LinkedInPublication publication
    ) {
        return new LinkedInPublicationResponse(
                publication.getArticle().getId(),
                publication.getStatus(),
                publication.getLinkedInPostUrn(),
                publication.getFailureMessage(),
                publication.getPublishedAt() == null
                        ? null
                        : publication.getPublishedAt().toInstant()
        );
    }

    private boolean isConfigured() {
        return clientId != null
                && !clientId.isBlank()
                && clientSecret != null
                && !clientSecret.isBlank()
                && tokenCipher.isConfigured();
    }

    private void requireConfigured() {
        if (!isConfigured()) {
            throw new IllegalStateException(
                    "Set LINKEDIN_CLIENT_ID, LINKEDIN_CLIENT_SECRET, "
                            + "and a 32-byte LINKEDIN_TOKEN_ENCRYPTION_KEY "
                            + "before connecting LinkedIn"
            );
        }
    }

    private static String requiredString(
            Map<?, ?> values,
            String key
    ) {
        Object value = values == null
                ? null
                : values.get(key);

        if (!(value instanceof String text)
                || text.isBlank()) {

            throw new IllegalStateException(
                    "LinkedIn response did not include " + key
            );
        }

        return text;
    }

    private static String encode(String value) {
        return URLEncoder.encode(
                value,
                StandardCharsets.UTF_8
        );
    }

    private static String message(Exception exception) {
        String value = exception.getMessage();

        return value == null
                ? "LinkedIn rejected the publish request."
                : value.substring(
                0,
                Math.min(value.length(), 1000)
        );
    }
}
