package com.ryan.personal_website_backend.util;

import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.Member;
import com.ryan.personal_website_backend.entity.User;
import com.ryan.personal_website_backend.repository.MemberRepo;
import com.ryan.personal_website_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class AuthorizationHelperImpl implements AuthorizationHelper {
    private final MemberRepo memberRepo;

    public void authorizeCourseOwner(Course course, Authentication authentication) {
        boolean isAdmin = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));
        if (isAdmin) return;

        User user = (User) authentication.getPrincipal();
        Member member = memberRepo.findByUser_Email(user.getEmail())
                .orElseThrow(() -> new AccessDeniedException("You are not a member"));

        course.getMembers().stream()
                .map(Member::getId)
                .filter(id -> id.equals(member.getId()))
                .findFirst()
                .orElseThrow(() -> new AccessDeniedException("Not a course owner"));
    }
}