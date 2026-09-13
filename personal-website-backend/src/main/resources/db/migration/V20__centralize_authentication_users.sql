CREATE TABLE users (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT now(),
    updated_at TIMESTAMP NOT NULL DEFAULT now()
);

CREATE TABLE user_authorities (
    user_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    authority VARCHAR(255) NOT NULL,
    PRIMARY KEY (user_id, authority)
);

ALTER TABLE students ADD COLUMN user_id UUID UNIQUE REFERENCES users(id);
ALTER TABLE instructors ADD COLUMN user_id UUID UNIQUE REFERENCES users(id);

INSERT INTO users (id, email, password, created_at, updated_at)
SELECT gen_random_uuid(), email, password, created_time, created_time FROM students
UNION ALL
SELECT gen_random_uuid(), email, password, COALESCE(created_time, now()), COALESCE(created_time, now()) FROM instructors;

UPDATE students s SET user_id = u.id FROM users u WHERE u.email = s.email;
UPDATE instructors i SET user_id = u.id FROM users u WHERE u.email = i.email;

INSERT INTO user_authorities (user_id, authority)
SELECT u.id, 'ROLE_' || COALESCE(s.role, 'STUDENT') FROM students s JOIN users u ON u.id = s.user_id
UNION ALL
SELECT u.id, 'ROLE_' || COALESCE(i.role, 'INSTRUCTOR') FROM instructors i JOIN users u ON u.id = i.user_id;

ALTER TABLE students ALTER COLUMN user_id SET NOT NULL;
ALTER TABLE instructors ALTER COLUMN user_id SET NOT NULL;
