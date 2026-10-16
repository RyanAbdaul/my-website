package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ReviewRepo extends JpaRepository<Review, UUID> {
    List<Review> findByCourseId(UUID courseId);
    List<Review> findByStudentId(UUID studentId);
    Boolean existsByStudentIdAndCourseId(UUID studentId, UUID courseId);
}
