CREATE TABLE reviews (
                         id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                         student_id UUID NOT NULL REFERENCES students(id),
                         course_id UUID NOT NULL REFERENCES courses(id),
                         rating INTEGER NOT NULL,
                         comment TEXT,
                         created_time TIMESTAMP NOT NULL DEFAULT now()
);