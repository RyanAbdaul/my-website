package com.ryan.personal_website_backend.util;

import com.ryan.personal_website_backend.entity.Course;
import org.springframework.security.core.Authentication;

import java.nio.file.AccessDeniedException;
import java.util.UUID;

public interface AuthorizationHelper {
    public void authorizeCourseOwner(Course theCourse, Authentication authentication) throws AccessDeniedException;
}
