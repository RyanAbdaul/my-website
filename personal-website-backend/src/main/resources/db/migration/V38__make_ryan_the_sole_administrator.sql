-- Existing development data can contain historical test administrators.
-- Keep Ryan as the only administrator; those accounts remain students when applicable.
DELETE FROM user_authorities ua
WHERE ua.authority = 'ROLE_ADMIN'
  AND ua.user_id NOT IN (
      SELECT id FROM users WHERE display_name = 'Ryan Abdulaziz'
  );
