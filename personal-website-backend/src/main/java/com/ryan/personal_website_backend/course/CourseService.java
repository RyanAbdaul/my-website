package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CreateCourseDTO;
import com.ryan.personal_website_backend.course.ResponseCourseDTO;
import com.ryan.personal_website_backend.course.ResponseEnrollmentDTO;
import com.ryan.personal_website_backend.course.ResponseLessonDTO;
import com.ryan.personal_website_backend.course.UpdateCourseDTO;
import com.ryan.personal_website_backend.course.Course;
import com.ryan.personal_website_backend.common.CourseDoesNotExistException;
import com.ryan.personal_website_backend.course.CourseRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;

    public List<ResponseCourseDTO> getCourseByName(String title) {
        return courseRepo.findCourseByTitleContaining(title).stream().map(this::response).toList();
    }

    public ResponseCourseDTO getCourseById(UUID id) {
        return response(courseRepo.findById(id)
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + id + " does not exist")));
    }

    public List<ResponseCourseDTO> getAllCourses(String title, Double minPrice, Double maxPrice) {
        return courseRepo.findByFilters(title, minPrice, maxPrice).stream().map(this::response).toList();
    }

    public List<ResponseCourseDTO> getFeaturedCourses() {
        return courseRepo.findTop10ByOrderByCreateTimeDesc().stream().map(this::response).toList();
    }

    @Transactional
    public ResponseCourseDTO createCourse(CreateCourseDTO input, Authentication authentication) {
        Course course = new Course();
        course.setTitle(input.getTitle());
        course.setDescription(input.getDescription());
        course.setCreateTime(new Timestamp(System.currentTimeMillis()));
        course.setPrice(input.getPrice());
        course.setThumbnailLink(input.getThumbnailLink());
        course.setStatus(input.getStatus());
        return response(courseRepo.save(course));
    }

    public void deleteCourseById(UUID courseId) {
        Course course = courseRepo.findById(courseId)
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + courseId + " does not exist"));
        courseRepo.delete(course);
    }

    @Transactional
    public ResponseCourseDTO updateCourseById(UUID id, UpdateCourseDTO input, Authentication authentication) {
        Course course = courseRepo.findById(id)
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + id + " does not exist"));
        course.setPrice(input.getPrice());
        course.setTitle(input.getTitle());
        course.setThumbnailLink(input.getThumbnailLink());
        course.setDescription(input.getDescription());
        course.setStatus(input.getStatus());
        return response(courseRepo.save(course));
    }

    private ResponseCourseDTO response(Course course) {
        return new ResponseCourseDTO(
                course.getId(), course.getTitle(), course.getDescription(), course.getCreateTime(),
                course.getThumbnailLink(), course.getPrice(), course.getStatus(),
                course.getEnrollments().stream().map(this::enrollmentResponse).toList(),
                course.getLessons().stream().map(this::lessonResponse).toList()
        );
    }

    private ResponseEnrollmentDTO enrollmentResponse(com.ryan.personal_website_backend.course.Enrollment enrollment) {
        return new ResponseEnrollmentDTO(enrollment.getId(), enrollment.getStudent().getId(), enrollment.getStudent().getName(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(), enrollment.getPaymentStatus(), enrollment.getEnrolledAt());
    }

    private ResponseLessonDTO lessonResponse(com.ryan.personal_website_backend.course.Lesson lesson) {
        return new ResponseLessonDTO(lesson.getId(), lesson.getCourse().getId(), lesson.getTitle(), lesson.getVideoLink(), lesson.getCreateTime());
    }
}
