package com.ryan.personal_website_backend.exception;

public class CourseIsNotFreeException extends RuntimeException {
    public CourseIsNotFreeException(String message) {
        super(message);
    }
}
