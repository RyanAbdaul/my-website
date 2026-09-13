CREATE TABLE member_course (
    member_id UUID NOT NULL REFERENCES members(id) ON DELETE CASCADE,
    course_id UUID NOT NULL REFERENCES courses(id) ON DELETE CASCADE,
    PRIMARY KEY (member_id, course_id)
);

-- Preserve existing instructor-course associations by reusing a same-named member,
-- or by creating a member profile for an instructor that does not have one yet.
INSERT INTO members (id, slug, name, headline)
SELECT i.id, 'legacy-instructor-' || replace(i.id::text, '-', ''), i.name, 'Member'
FROM instructors i
WHERE NOT EXISTS (
    SELECT 1 FROM members m WHERE lower(trim(m.name)) = lower(trim(i.name))
);

INSERT INTO member_course (member_id, course_id)
SELECT m.id, ic.course_id
FROM instructor_course ic
JOIN instructors i ON i.id = ic.instructor_id
JOIN members m ON lower(trim(m.name)) = lower(trim(i.name))
ON CONFLICT DO NOTHING;

ALTER TABLE users ADD COLUMN member_id UUID UNIQUE REFERENCES members(id);

UPDATE users u
SET member_id = m.id
FROM instructors i
JOIN members m ON lower(trim(m.name)) = lower(trim(i.name))
WHERE u.instructor_id = i.id;

UPDATE user_authorities SET authority = 'ROLE_MEMBER' WHERE authority = 'ROLE_INSTRUCTOR';

ALTER TABLE users DROP COLUMN instructor_id;
ALTER TABLE instructor_course DROP CONSTRAINT IF EXISTS instructor_course_instructor_id_fkey;
DROP TABLE instructor_course;
DROP TABLE instructors;

ALTER TABLE courses DROP COLUMN member_id;
