package com.ryan.personal_website_backend.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResponseProgressDTO {
    private UUID id;
    private UUID studentId;
    private String studentName;
    private UUID lessonId;
    private String lessonTitle;
    private Boolean isComplete;
}
