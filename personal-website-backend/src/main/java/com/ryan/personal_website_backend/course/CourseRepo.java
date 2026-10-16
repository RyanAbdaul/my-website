package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.Course;


import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface CourseRepo extends JpaRepository<Course, UUID> {
    List<Course> findCourseByTitleContaining(String title);
    List<Course> findTop10ByOrderByCreateTimeDesc();
    @Query("""
        SELECT c FROM Course c
        WHERE (:title IS NULL OR c.title LIKE CONCAT('%', CAST(:title AS string), '%'))
        AND (:minPrice IS NULL OR c.price >= :minPrice)
        AND (:maxPrice IS NULL OR c.price <= :maxPrice)
    """)
    List<Course> findByFilters(
            @Param("title") String title,
            @Param("minPrice") Double minPrice,
            @Param("maxPrice") Double maxPrice
    );

}
