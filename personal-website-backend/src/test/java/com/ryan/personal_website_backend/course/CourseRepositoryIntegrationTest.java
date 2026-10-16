package com.ryan.personal_website_backend.course;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Testcontainers
@SpringBootTest
class CourseRepositoryIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:17-alpine");

    @Autowired
    private CourseRepo courseRepo;

    @Test
    void saveAndFindCourseAgainstRealPostgres() {
        Course course = new Course();
        course.setTitle("Testcontainers Course");
        course.setDescription("Persisted against a real PostgreSQL test container");
        course.setPrice(BigDecimal.ZERO);
        course.setStatus(CourseStatus.ACTIVE);

        Course saved = courseRepo.save(course);

        assertTrue(courseRepo.existsById(saved.getId()));

        Optional<Course> loaded = courseRepo.findById(saved.getId());
        assertTrue(loaded.isPresent());
        assertEquals("Testcontainers Course", loaded.get().getTitle());
    }
}
