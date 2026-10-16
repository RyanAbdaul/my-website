package com.ryan.personal_website_backend.common;

public class EnrollmentDoesNotExistException extends RuntimeException {
    public EnrollmentDoesNotExistException(String message) {
        super(message);
    }
}