package com.ryan.personal_website_backend.common;

public class ProgressAlreadyExistsException extends RuntimeException {
    public ProgressAlreadyExistsException(String message) {
        super(message);
    }
}
