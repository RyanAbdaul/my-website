CREATE EXTENSION IF NOT EXISTS pgcrypto;

CREATE TABLE students (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          name VARCHAR(255) NOT NULL,
                          email VARCHAR(255) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          created_time TIMESTAMP NOT NULL DEFAULT now()
);
