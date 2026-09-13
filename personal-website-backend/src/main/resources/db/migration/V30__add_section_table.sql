-- V{version}__create_sections_table.sql

CREATE TABLE sections (
                          id UUID PRIMARY KEY,
                          title VARCHAR(255) NOT NULL,
                          description TEXT,
                          order_index INTEGER,
                          created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          course_id UUID NOT NULL,
                          CONSTRAINT fk_sections_course
                              FOREIGN KEY (course_id)
                                  REFERENCES courses(id)
                                  ON DELETE CASCADE
);

-- Add index for better query performance on course_id
CREATE INDEX idx_sections_course_id ON sections(course_id);