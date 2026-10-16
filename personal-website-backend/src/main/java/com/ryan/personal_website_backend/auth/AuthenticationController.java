package com.ryan.personal_website_backend.auth;

import com.ryan.personal_website_backend.common.RateLimit;
import com.ryan.personal_website_backend.common.RateLimitTier;
import com.ryan.personal_website_backend.auth.AuthenticationRequest;
import com.ryan.personal_website_backend.auth.AuthenticationResponse;
import com.ryan.personal_website_backend.auth.AuthenticatedUserResponse;
import com.ryan.personal_website_backend.auth.LoginRequest;
import com.ryan.personal_website_backend.auth.Role;
import com.ryan.personal_website_backend.auth.AuthenticationService;
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
    @RateLimit(RateLimitTier.STRICT)
    public AuthenticationResponse register(
            @Valid @RequestBody AuthenticationRequest request) {

        return authenticationService.registerStudent(request);
    }

    @PostMapping("/login")
    @RateLimit(RateLimitTier.STRICT)
    public AuthenticationResponse login(
            @Valid @RequestBody LoginRequest request) {

        return authenticationService.login(request);
    }

    @GetMapping("/me")
    public AuthenticatedUserResponse currentUser(Authentication authentication) {
        Role role = authentication.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : Role.STUDENT;
        return new AuthenticatedUserResponse(authentication.getName(), role);
    }

}
