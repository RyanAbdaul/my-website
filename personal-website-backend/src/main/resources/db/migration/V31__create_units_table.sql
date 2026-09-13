-- V31: create the units table, the child of sections.
-- Mirrors V30__add_section_table (same columns, FK to sections with ON DELETE CASCADE).
-- A unit groups a set of lessons together within a section.

CREATE TABLE units (
                      id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                      title VARCHAR(255) NOT NULL,
                      description TEXT,
                      order_index INTEGER,
                      created_time TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
                      section_id UUID NOT NULL,
                      CONSTRAINT fk_units_section
                          FOREIGN KEY (section_id)
                              REFERENCES sections(id)
                              ON DELETE CASCADE
);

-- Index for loading a unit's section and for ordering units within a section.
CREATE INDEX idx_units_section_id ON units(section_id);