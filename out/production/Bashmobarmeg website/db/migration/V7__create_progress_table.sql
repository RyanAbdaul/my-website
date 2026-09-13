CREATE TABLE progress (
                          id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                          student_id UUID NOT NULL REFERENCES students(id),
                          lesson_id UUID NOT NULL REFERENCES lessons(id),
                          is_complete BOOLEAN NOT NULL DEFAULT false,
                          UNIQUE (student_id, lesson_id)
);