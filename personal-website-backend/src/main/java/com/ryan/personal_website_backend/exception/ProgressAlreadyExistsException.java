package com.ryan.personal_website_backend.exception;

public class ProgressAlreadyExistsException extends RuntimeException {
    public ProgressAlreadyExistsException(String message) {
        super(message);
    }
}
