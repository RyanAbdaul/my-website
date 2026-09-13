package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProgressRepo extends JpaRepository<Progress, UUID> {
    List<Progress> findByStudentId(UUID studentId);
    List<Progress> findByLessonId(UUID lessonId);
    Boolean existsByStudentIdAndLessonId(UUID studentId, UUID lessonId);
}
