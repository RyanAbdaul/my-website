package com.ryan.personal_website_backend.course;

import com.ryan.personal_website_backend.course.CreateEnrollmentDTO;
import com.ryan.personal_website_backend.course.ResponseEnrollmentDTO;
import com.ryan.personal_website_backend.course.EnrollmentService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.core.Authentication;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/enrollments")
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    @PostMapping
    public ResponseEnrollmentDTO createEnrollment(@Valid @RequestBody CreateEnrollmentDTO input) {
        return enrollmentService.createEnrollment(input);
    }

    @PostMapping("/courses/{courseId}/me")
    public ResponseEnrollmentDTO enrollCurrentUser(@PathVariable UUID courseId, Authentication authentication) {
        return enrollmentService.enrollCurrentUser(authentication.getName(), courseId);
    }

    @GetMapping("/students/{studentId}")
    public List<ResponseEnrollmentDTO> getByStudentId(@PathVariable UUID studentId) {
        return enrollmentService.getEnrollmentsByStudentId(studentId);
    }

    @GetMapping("/courses/{courseId}")
    public List<ResponseEnrollmentDTO> getByCourseId(@PathVariable UUID courseId) {
        return enrollmentService.getEnrollmentsByCourseId(courseId);
    }

    @GetMapping("/courses/{courseId}/me")
    public boolean isCurrentUserEnrolled(@PathVariable UUID courseId, Authentication authentication) {
        return enrollmentService.isUserEnrolled(authentication.getName(), courseId);
    }

    @DeleteMapping("/{id}")
    public void deleteEnrollment(@PathVariable UUID id) {
        enrollmentService.deleteEnrollment(id);
    }
}
