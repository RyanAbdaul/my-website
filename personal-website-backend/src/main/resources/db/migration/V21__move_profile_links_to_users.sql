ALTER TABLE users ADD COLUMN student_id UUID UNIQUE REFERENCES students(id);
ALTER TABLE users ADD COLUMN instructor_id UUID UNIQUE REFERENCES instructors(id);

UPDATE users u SET student_id = s.id FROM students s WHERE s.user_id = u.id;
UPDATE users u SET instructor_id = i.id FROM instructors i WHERE i.user_id = u.id;

ALTER TABLE students DROP CONSTRAINT IF EXISTS students_user_id_key;
ALTER TABLE instructors DROP CONSTRAINT IF EXISTS instructors_user_id_key;
ALTER TABLE students DROP CONSTRAINT IF EXISTS students_user_id_fkey;
ALTER TABLE instructors DROP CONSTRAINT IF EXISTS instructors_user_id_fkey;
ALTER TABLE students DROP COLUMN user_id;
ALTER TABLE instructors DROP COLUMN user_id;
