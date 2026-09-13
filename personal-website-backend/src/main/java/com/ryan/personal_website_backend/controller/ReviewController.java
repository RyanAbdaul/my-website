package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.CreateReviewDTO;
import com.ryan.personal_website_backend.dto.ResponseReviewDTO;
import com.ryan.personal_website_backend.service.ReviewService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/reviews")
public class ReviewController {

    private final ReviewService reviewService;

    @GetMapping("/course/{courseId}")
    public List<ResponseReviewDTO> getReviewsByCourseId(@PathVariable UUID courseId) {
        return reviewService.getReviewsByCourseId(courseId);
    }

    @GetMapping("/student/{studentId}")
    public List<ResponseReviewDTO> getReviewsByStudentId(@PathVariable UUID studentId) {
        return reviewService.getReviewsByStudentId(studentId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseReviewDTO createReview(@Valid @RequestBody CreateReviewDTO dto) {
        return reviewService.createReview(dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteReview(@PathVariable UUID id) {
        reviewService.deleteReview(id);
    }
}
