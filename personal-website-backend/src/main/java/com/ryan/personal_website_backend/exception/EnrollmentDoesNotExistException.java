package com.ryan.personal_website_backend.exception;

public class EnrollmentDoesNotExistException extends RuntimeException {
    public EnrollmentDoesNotExistException(String message) {
        super(message);
    }
}