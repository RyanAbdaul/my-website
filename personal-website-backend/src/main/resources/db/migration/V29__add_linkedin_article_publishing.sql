CREATE TABLE linkedin_connections (
    id UUID PRIMARY KEY,
    encrypted_access_token TEXT,
    person_urn VARCHAR(255),
    access_token_expires_at TIMESTAMP,
    oauth_state VARCHAR(255),
    oauth_state_expires_at TIMESTAMP
);

CREATE TABLE linkedin_publications (
    id UUID PRIMARY KEY,
    article_id UUID NOT NULL UNIQUE REFERENCES articles(id) ON DELETE CASCADE,
    status VARCHAR(32) NOT NULL,
    linkedin_post_urn VARCHAR(255),
    failure_message TEXT,
    published_at TIMESTAMP
);
