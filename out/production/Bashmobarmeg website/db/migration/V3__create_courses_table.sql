CREATE TABLE courses (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         title VARCHAR(255) NOT NULL,
                         description TEXT,
                         price NUMERIC NOT NULL,
                         created_time TIMESTAMP NOT NULL DEFAULT now(),
                         thumbnail_link VARCHAR(255)
);