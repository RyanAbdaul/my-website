package com.ryan.personal_website_backend.common;

public class ProgressNotFoundException extends RuntimeException {
    public ProgressNotFoundException(String message) {
        super(message);
    }
}
