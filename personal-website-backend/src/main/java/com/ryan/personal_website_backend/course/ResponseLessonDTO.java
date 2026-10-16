package com.ryan.personal_website_backend.course;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.sql.Timestamp;
import java.util.UUID;

@Getter
@AllArgsConstructor
@Builder
public class ResponseLessonDTO {
    private UUID id;
    private UUID courseId;
    private String title;
    private String videoLink;
    private Timestamp createTime;
}
