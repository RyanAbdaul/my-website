package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.AuthenticationRequest;
import com.ryan.personal_website_backend.dto.AuthenticationResponse;
import com.ryan.personal_website_backend.dto.AuthenticatedUserResponse;
import com.ryan.personal_website_backend.dto.LoginRequest;
import com.ryan.personal_website_backend.entity.Role;
import com.ryan.personal_website_backend.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.security.core.Authentication;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authenticationService;

    @PostMapping("/students")
    public AuthenticationResponse register(
            @Valid @RequestBody AuthenticationRequest request) {

        return authenticationService.registerStudent(request);
    }

    @PostMapping("/login")
    public AuthenticationResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authenticationService.login(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Authentication authentication) {
        Role role = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : authentication.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_MEMBER"))
                        ? Role.MEMBER
                        : Role.STUDENT;
        return new AuthenticatedUserResponse(authentication.getName(), role);
    }

    @PostMapping("/members")
    public AuthenticationResponse registerMember(
            @Valid @RequestBody AuthenticationRequest request) {

        return authenticationService.registerMember(request);
    }

    @PostMapping("/admin")
    public AuthenticationResponse registerAdmin(
            @Valid @RequestBody AuthenticationRequest request) {

        return authenticationService.registerAdmin(request);
    }
}
