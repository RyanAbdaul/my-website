package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CourseProgress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CourseProgressRepo extends JpaRepository<CourseProgress, UUID> {
    Optional<CourseProgress> findByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
