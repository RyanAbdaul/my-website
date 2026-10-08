-- Convert the former multi-member portfolio into one administrator profile.
-- Ryan's profile and experience history are retained; content remains global.

ALTER TABLE users ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS headline VARCHAR(255);
ALTER TABLE users ADD COLUMN IF NOT EXISTS biography TEXT;
ALTER TABLE users ADD COLUMN IF NOT EXISTS photo_url TEXT;

-- Copy the canonical Ryan profile to the user account created by V34.
UPDATE users u
SET display_name = m.name,
    headline = m.headline,
    biography = m.biography,
    photo_url = m.photo_url
FROM members m
WHERE m.user_id = u.id
  AND m.slug = 'ryan';

-- Keep Ryan's member experience as global portfolio experience records.
INSERT INTO work_experiences (id, company, role, start_date, end_date, summary, highlights, display_order)
SELECT me.id,
       me.organization,
       me.role,
       me.start_date,
       me.end_date,
       me.summary,
       ARRAY[]::TEXT[],
       me.display_order
FROM member_experiences me
JOIN members m ON m.id = me.member_id
WHERE m.slug = 'ryan'
  AND NOT EXISTS (
      SELECT 1
      FROM work_experiences we
      WHERE we.company = me.organization
        AND we.start_date = me.start_date
  )
ON CONFLICT (id) DO NOTHING;

-- Ryan is the sole administrator. Member-only accounts and authorities are removed.
INSERT INTO user_authorities (user_id, authority)
SELECT m.user_id, 'ROLE_ADMIN'
FROM members m
WHERE m.slug = 'ryan'
ON CONFLICT DO NOTHING;

DELETE FROM user_authorities WHERE authority = 'ROLE_MEMBER';

DELETE FROM users u
USING members m
WHERE m.user_id = u.id
  AND m.slug <> 'ryan';

ALTER TABLE projects DROP COLUMN IF EXISTS member_id;
ALTER TABLE certificates DROP COLUMN IF EXISTS member_id;
ALTER TABLE articles DROP COLUMN IF EXISTS member_id;
ALTER TABLE courses DROP COLUMN IF EXISTS member_id;

DROP TABLE IF EXISTS member_course;
DROP TABLE IF EXISTS member_experiences;
DROP TABLE IF EXISTS members;
