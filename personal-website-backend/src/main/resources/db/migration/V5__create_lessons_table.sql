CREATE TABLE lessons (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         course_id UUID NOT NULL REFERENCES courses(id),
                         title VARCHAR(255) NOT NULL,
                         video_link VARCHAR(255),
                         lesson_order INTEGER NOT NULL,
                         created_time TIMESTAMP NOT NULL DEFAULT now()
);