CREATE TABLE articles (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          title VARCHAR(255) NOT NULL,
                          content TEXT,
                          created_time TIMESTAMP NOT NULL DEFAULT now()
);