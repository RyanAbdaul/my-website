-- V33: Restructure Student-User relationship so User is root and Student has foreign key user_id -> users(id)

-- 1. Add user_id column to students table
ALTER TABLE students ADD COLUMN user_id UUID;

-- 2. Backfill user_id from existing users table
UPDATE students s
SET user_id = u.id
FROM users u
WHERE u.student_id = s.id;

-- 3. In case any orphan students exist without a user, create user accounts for them
INSERT INTO users (id, email, password, created_at, updated_at)
SELECT gen_random_uuid(), 'student-' || s.id || '@bashmobarmeg.local', '$2a$10$SZAmEcK.0sC1.woH7laR4uqzpKy6Yi94yMn..hmMaj7o8PZni2wra', now(), now()
FROM students s
WHERE s.user_id IS NULL;

UPDATE students s
SET user_id = u.id
FROM users u
WHERE u.email = 'student-' || s.id || '@bashmobarmeg.local' AND s.user_id IS NULL;

-- 4. Ensure authority exists for any newly created users
INSERT INTO user_authorities (user_id, authority)
SELECT s.user_id, 'ROLE_STUDENT'
FROM students s
WHERE NOT EXISTS (
    SELECT 1 FROM user_authorities ua WHERE ua.user_id = s.user_id
);

-- 5. Set user_id NOT NULL and add foreign key / unique constraints
ALTER TABLE students ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE students ADD CONSTRAINT fk_students_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE students ADD CONSTRAINT uq_students_user UNIQUE (user_id);

-- 6. Drop the old student_id foreign key and column from users table
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_student_id_fkey;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_student_id_key;
ALTER TABLE users DROP COLUMN IF EXISTS student_id;
