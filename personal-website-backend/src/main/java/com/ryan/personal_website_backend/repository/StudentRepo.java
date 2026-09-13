package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Student;
import com.ryan.personal_website_backend.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface StudentRepo extends JpaRepository<Student, UUID> {
    Optional<Student> findByUser_Email(String email);
}
