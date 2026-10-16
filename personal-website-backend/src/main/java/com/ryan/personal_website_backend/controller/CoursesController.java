package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.annotation.RateLimit;
import com.ryan.personal_website_backend.annotation.RateLimitTier;
import com.ryan.personal_website_backend.dto.*;
import com.ryan.personal_website_backend.service.CourseService;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/courses")
public class CoursesController {
    private final CourseService courseService;
    @GetMapping("/featured")
    public List<ResponseCourseDTO> getFeaturedCourses(){
        return courseService.getFeaturedCourses();
    }

    @GetMapping("/search/{title}")
    public List<ResponseCourseDTO> getCourseByName(@PathVariable String title){
        return courseService.getCourseByName(title);
    }

    @GetMapping("/{courseId}")
    public ResponseCourseDTO getCourseById(@PathVariable UUID courseId){
        return courseService.getCourseById(courseId);
    }

    @GetMapping
    @RateLimit(RateLimitTier.RELAXED)
    public List<ResponseCourseDTO> getAllCourses(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) Double minPrice,
            @RequestParam(required = false) Double maxPrice
            ){
        return courseService.getAllCourses(title, minPrice, maxPrice);
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseCourseDTO createNewCourse(@RequestBody @Valid CreateCourseDTO newCourseInput, Authentication authentication){
        return courseService.createCourse(newCourseInput, authentication);
    }


    @DeleteMapping("{courseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourseById(
            @PathVariable UUID courseId
    ) {
        courseService.deleteCourseById(courseId);
    }
    
    @PutMapping("{id}")
    public ResponseCourseDTO updateCourseById(@PathVariable UUID id, @Valid @RequestBody UpdateCourseDTO updatedCourse, Authentication authentication){
        return courseService.updateCourseById(id, updatedCourse, authentication);
    }
}
