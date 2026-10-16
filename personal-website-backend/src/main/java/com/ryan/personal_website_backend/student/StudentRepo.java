package com.ryan.personal_website_backend.student;

import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.auth.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepo extends JpaRepository<Student, UUID> {
    Optional<Student> findByUser_Email(String email);
}
