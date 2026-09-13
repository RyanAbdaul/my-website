package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SectionRepo extends JpaRepository<Section, UUID> {

    List<Section> findByCourseIdOrderByOrderIndexAsc(UUID courseId);

    List<Section> findByCourseId(UUID courseId);

    Optional<Section> findByIdAndCourseId(UUID id, UUID courseId);

    boolean existsByIdAndCourseId(UUID id, UUID courseId);

    void deleteByCourseId(UUID courseId);

    long countByCourseId(UUID courseId);

    List<Section> findByCourseIdAndOrderIndexGreaterThan(UUID courseId, Integer orderIndex);

    Optional<Section> findFirstByCourseIdOrderByOrderIndexDesc(UUID courseId);
}