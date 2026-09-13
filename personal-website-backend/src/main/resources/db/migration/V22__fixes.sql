ALTER TABLE students
DROP COLUMN email,
  DROP COLUMN password,
  DROP COLUMN role;

ALTER TABLE instructors
DROP COLUMN email,
  DROP COLUMN password,
  DROP COLUMN role;