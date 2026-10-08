package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface LessonRepo extends JpaRepository<Lesson, UUID> {
    List<Lesson> findByCourseId(UUID courseId);
    List<Lesson> findByCourseIdAndTitleContainingIgnoreCase(UUID courseId, String title);
}
