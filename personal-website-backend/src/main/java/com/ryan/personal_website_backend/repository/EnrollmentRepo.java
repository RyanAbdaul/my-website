package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface EnrollmentRepo extends JpaRepository<Enrollment, UUID> {
    List<Enrollment> findByStudent_Id(UUID studentId);
    List<Enrollment> findByCourse_Id(UUID courseId);
    boolean existsByStudent_User_EmailAndCourse_Id(String email, UUID courseId);
}
