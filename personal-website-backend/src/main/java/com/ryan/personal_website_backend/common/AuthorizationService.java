package com.ryan.personal_website_backend.common;

import com.ryan.personal_website_backend.course.EnrollmentRepo;
import com.ryan.personal_website_backend.course.LessonRepo;
import com.ryan.personal_website_backend.course.ProgressRepo;
import com.ryan.personal_website_backend.student.StudentRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service("authorizationService")
@RequiredArgsConstructor
public class AuthorizationService {

    private final StudentRepo studentRepo;
    private final LessonRepo lessonRepo;
    private final EnrollmentRepo enrollmentRepo;
    private final ProgressRepo progressRepo;

    public boolean isStudent(UUID studentId, Authentication authentication) {
        return studentRepo.findByUser_Email(authentication.getName())
                .map(student -> student.getId().equals(studentId))
                .orElse(false);
    }

    public boolean ownsEnrollment(UUID enrollmentId, Authentication authentication) {
        return enrollmentRepo.findById(enrollmentId)
                .map(enrollment -> isStudent(enrollment.getStudent().getId(), authentication))
                .orElse(false);
    }

    public boolean ownsProgress(UUID progressId, Authentication authentication) {
        return progressRepo.findById(progressId)
                .map(progress -> isStudent(progress.getStudent().getId(), authentication))
                .orElse(false);
    }
}
