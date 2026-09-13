package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateReviewDTO;
import com.ryan.personal_website_backend.dto.ResponseReviewDTO;
import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.Review;
import com.ryan.personal_website_backend.entity.Student;
import com.ryan.personal_website_backend.exception.CourseDoesNotExistException;
import com.ryan.personal_website_backend.exception.ReviewAlreadyExistsException;
import com.ryan.personal_website_backend.exception.ReviewNotFoundException;
import com.ryan.personal_website_backend.exception.StudentDoesNotExistException;
import com.ryan.personal_website_backend.repository.CourseRepo;
import com.ryan.personal_website_backend.repository.ReviewRepo;
import com.ryan.personal_website_backend.repository.StudentRepo;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
public class ReviewService {

    private final ReviewRepo reviewRepo;
    private final StudentRepo studentRepo;
    private final CourseRepo courseRepo;

    public List<ResponseReviewDTO> getReviewsByCourseId(UUID courseId) {
        if (!courseRepo.existsById(courseId)) {
            throw new CourseDoesNotExistException("Course with ID " + courseId + " does not exist");
        }
        return reviewRepo.findByCourseId(courseId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public List<ResponseReviewDTO> getReviewsByStudentId(UUID studentId) {
        if (!studentRepo.existsById(studentId)) {
            throw new StudentDoesNotExistException("Student with ID " + studentId + " does not exist");
        }
        return reviewRepo.findByStudentId(studentId)
                .stream()
                .map(this::mapToResponseDTO)
                .toList();
    }

    public ResponseReviewDTO createReview(CreateReviewDTO dto) {
        Student student = studentRepo.findById(dto.getStudentId())
                .orElseThrow(() -> new StudentDoesNotExistException("Student with ID " + dto.getStudentId() + " does not exist"));

        Course course = courseRepo.findById(dto.getCourseId())
                .orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + dto.getCourseId() + " does not exist"));

        if (reviewRepo.existsByStudentIdAndCourseId(dto.getStudentId(), dto.getCourseId())) {
            throw new ReviewAlreadyExistsException("Student has already reviewed this course");
        }

        Review review = new Review();
        review.setStudent(student);
        review.setCourse(course);
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());

        Review saved = reviewRepo.save(review);
        return mapToResponseDTO(saved);
    }

    public void deleteReview(UUID id) {
        if (!reviewRepo.existsById(id)) {
            throw new ReviewNotFoundException("Review with ID " + id + " not found");
        }
        reviewRepo.deleteById(id);
    }

    private ResponseReviewDTO mapToResponseDTO(Review review) {
        return ResponseReviewDTO.builder()
                .id(review.getId())
                .studentId(review.getStudent().getId())
                .studentName(review.getStudent().getName())
                .courseId(review.getCourse().getId())
                .courseTitle(review.getCourse().getTitle())
                .rating(review.getRating())
                .comment(review.getComment())
                .createTime(review.getCreateTime())
                .build();
    }
}
