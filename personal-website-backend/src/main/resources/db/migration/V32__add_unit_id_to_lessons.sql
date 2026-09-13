-- V32: link lessons to their parent unit.
-- Nullable so lessons created before this migration stay valid; the FK is
-- added with ON DELETE CASCADE to match the section -> unit cascade.
ALTER TABLE lessons
    ADD COLUMN unit_id UUID;

ALTER TABLE lessons
    ADD CONSTRAINT fk_lessons_unit
        FOREIGN KEY (unit_id)
            REFERENCES units(id)
            ON DELETE CASCADE;

CREATE INDEX idx_lessons_unit_id ON lessons(unit_id);