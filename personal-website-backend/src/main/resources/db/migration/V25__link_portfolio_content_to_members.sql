INSERT INTO members (id, slug, name, headline, biography)
VALUES ('3b0d8226-f692-4c6e-b2de-bc9e4c85e56e', 'ryan', 'Ryan Abdulaziz', 'Spring Boot Engineer · Instructor · Content Creator', 'I turn curious ideas into useful experiences, then share what I learn through projects, courses, and articles.')
ON CONFLICT (slug) DO NOTHING;

ALTER TABLE projects ADD COLUMN member_id UUID REFERENCES members(id) ON DELETE SET NULL;
ALTER TABLE certificates ADD COLUMN member_id UUID REFERENCES members(id) ON DELETE SET NULL;
ALTER TABLE articles ADD COLUMN member_id UUID REFERENCES members(id) ON DELETE SET NULL;
ALTER TABLE courses ADD COLUMN member_id UUID REFERENCES members(id) ON DELETE SET NULL;

UPDATE projects SET member_id = '3b0d8226-f692-4c6e-b2de-bc9e4c85e56e' WHERE member_id IS NULL;
UPDATE certificates SET member_id = '3b0d8226-f692-4c6e-b2de-bc9e4c85e56e' WHERE member_id IS NULL;
UPDATE articles SET member_id = '3b0d8226-f692-4c6e-b2de-bc9e4c85e56e' WHERE member_id IS NULL;
UPDATE courses SET member_id = '3b0d8226-f692-4c6e-b2de-bc9e4c85e56e' WHERE member_id IS NULL;
