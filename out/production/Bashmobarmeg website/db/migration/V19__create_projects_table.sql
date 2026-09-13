CREATE TABLE projects (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    title VARCHAR(255) NOT NULL,
    description TEXT,
    image_path VARCHAR(1000),
    project_url VARCHAR(1000) NOT NULL,
    display_order INTEGER NOT NULL DEFAULT 999
);
