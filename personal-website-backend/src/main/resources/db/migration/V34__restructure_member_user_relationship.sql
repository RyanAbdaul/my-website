-- V34: Restructure Member-User relationship so User is root and Member has foreign key user_id -> users(id)

-- 1. Add user_id column to members table
ALTER TABLE members ADD COLUMN user_id UUID;

-- 2. Backfill user_id from existing users table
UPDATE members m SET user_id = u.id FROM users u WHERE u.member_id = m.id;

-- 3. Ensure any orphan members get a user (shouldn't happen with correct data, but safe)
INSERT INTO users (id, email, password, created_at, updated_at)
SELECT gen_random_uuid(), m.slug || '@bashmobarmeg.local', '$2a$10$SZAmEcK.0sC1.woH7laR4uqzpKy6Yi94yMn..hmMaj7o8PZni2wra', now(), now()
FROM members m WHERE m.user_id IS NULL;

UPDATE members m SET user_id = u.id FROM users u WHERE u.email = m.slug || '@bashmobarmeg.local' AND m.user_id IS NULL;

-- 4. Set authorities for newly created users
INSERT INTO user_authorities (user_id, authority)
SELECT m.user_id, 'ROLE_MEMBER'
FROM members m WHERE NOT EXISTS (
    SELECT 1 FROM user_authorities ua WHERE ua.user_id = m.user_id
);

-- 5. Set user_id NOT NULL and add FK / unique constraints
ALTER TABLE members ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE members ADD CONSTRAINT fk_members_user FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE CASCADE;
ALTER TABLE members ADD CONSTRAINT uq_members_user UNIQUE (user_id);

-- 6. Drop old member_id from users table
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_member_id_fkey;
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_member_id_key;
ALTER TABLE users DROP COLUMN IF EXISTS member_id;
