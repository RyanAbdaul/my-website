package com.ryan.personal_website_backend.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.List;
import java.util.UUID;
import com.ryan.personal_website_backend.course.CourseStatus;

@Getter
@AllArgsConstructor
@Builder
public class ResponseCourseDTO {
    UUID id;
    String title;
    String description;
    Timestamp createTime;
    String thumbnailLink;
    BigDecimal price;
    CourseStatus status;
    List<ResponseEnrollmentDTO> enrollments;
    List<ResponseLessonDTO> lessons;
}
