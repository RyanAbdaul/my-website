package com.ryan.personal_website_backend.course;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@AllArgsConstructor
public class ResponseEnrollmentDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID courseId;
    private String courseTitle;
    private String paymentStatus;
    private Timestamp enrolledAt;
}
