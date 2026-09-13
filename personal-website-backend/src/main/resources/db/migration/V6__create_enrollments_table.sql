CREATE TABLE enrollments (
                             id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                             student_id UUID NOT NULL REFERENCES students(id),
                             course_id UUID NOT NULL REFERENCES courses(id),
                             payment_status VARCHAR(50),
                             enrolled_at TIMESTAMP NOT NULL DEFAULT now(),
                             UNIQUE (student_id, course_id)
);