package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProgressRepo extends JpaRepository<Progress, UUID> {
    List<Progress> findByStudentId(UUID studentId);
    List<Progress> findByLessonId(UUID lessonId);
    Boolean existsByStudentIdAndLessonId(UUID studentId, UUID lessonId);
    Optional<Progress> findByStudentIdAndLessonId(UUID studentId, UUID lessonId);
    List<Progress> findByStudentIdAndLesson_Course_Id(UUID studentId, UUID courseId);
}
