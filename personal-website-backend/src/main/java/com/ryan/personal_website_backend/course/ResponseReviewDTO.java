package com.ryan.personal_website_backend.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResponseReviewDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID courseId;
    private String courseTitle;
    private Integer rating;
    private String comment;
    private Timestamp createTime;
}
