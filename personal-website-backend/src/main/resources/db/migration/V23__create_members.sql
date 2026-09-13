CREATE TABLE members (
    id UUID PRIMARY KEY,
    slug VARCHAR(120) NOT NULL UNIQUE,
    name VARCHAR(160) NOT NULL,
    headline VARCHAR(220),
    biography TEXT,
    photo_url TEXT
);

CREATE TABLE member_experiences (
    id UUID PRIMARY KEY,
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    role VARCHAR(200) NOT NULL,
    organization VARCHAR(200) NOT NULL,
    start_date DATE NOT NULL,
    end_date DATE,
    summary TEXT NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 999
);

INSERT INTO members (id, slug, name, headline, biography)
VALUES ('0f58af04-1b8f-4d51-8bb4-f32b361d0be9', 'tasneem', 'Tasneem', 'Member', 'Welcome to Tasneem’s personal member page.')
ON CONFLICT (slug) DO NOTHING;
