-- V25 assigned legacy portfolio courses to Ryan. Restore that association in
-- the new many-to-many model when a course did not already have an instructor.
INSERT INTO member_course (member_id, course_id)
SELECT m.id, c.id
FROM courses c
JOIN members m ON m.slug = 'ryan'
WHERE NOT EXISTS (
    SELECT 1 FROM member_course mc WHERE mc.course_id = c.id
)
ON CONFLICT DO NOTHING;
