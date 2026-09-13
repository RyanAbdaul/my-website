CREATE TABLE instructor_course (
                                   instructor_id UUID NOT NULL REFERENCES instructors(id),
                                   course_id UUID NOT NULL REFERENCES courses(id),
                                   PRIMARY KEY (instructor_id, course_id)
);