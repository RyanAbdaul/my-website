package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Course;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface CourseRepo extends JpaRepository<Course, UUID> {
    List<Course> findCourseByTitleContaining(String title);
    List<Course> findByMembers_IdOrderByCreateTimeDesc(UUID memberId);
    List<Course> findTop10DistinctByMembers_NameContainingIgnoreCaseOrderByCreateTimeDesc(String memberName);
    @Query("""
        SELECT DISTINCT c FROM Course c
        LEFT JOIN c.members m
        WHERE (:title IS NULL OR c.title LIKE CONCAT('%', CAST(:title AS string), '%'))
        AND (:minPrice IS NULL OR c.price >= :minPrice)
        AND (:maxPrice IS NULL OR c.price <= :maxPrice)
        AND (:memberName IS NULL OR m.name LIKE CONCAT('%', CAST(:memberName AS string), '%'))
    """)
    List<Course> findByFilters(
            @Param("title") String title,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice,
            @Param("memberName") String memberName
    );

}
