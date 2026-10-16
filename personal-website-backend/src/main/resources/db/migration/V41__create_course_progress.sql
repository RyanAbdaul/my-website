-- V41: track the last-viewed lesson per student/course so the course-content
-- page can resume where the learner left off, synced across devices.

CREATE TABLE course_progress (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    student_id UUID NOT NULL,
    course_id UUID NOT NULL,
    last_lesson_id UUID,
    updated_at TIMESTAMP NOT NULL DEFAULT now(),
    CONSTRAINT fk_course_progress_student
        FOREIGN KEY (student_id) REFERENCES students(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_progress_course
        FOREIGN KEY (course_id) REFERENCES courses(id) ON DELETE CASCADE,
    CONSTRAINT fk_course_progress_lesson
        FOREIGN KEY (last_lesson_id) REFERENCES lessons(id) ON DELETE SET NULL,
    CONSTRAINT uq_course_progress_student_course UNIQUE (student_id, course_id)
);

CREATE INDEX idx_course_progress_student_course ON course_progress(student_id, course_id);
