package com.ryan.personal_website_backend.auth;

import com.ryan.personal_website_backend.auth.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUserResponse {
    private String email;
    private Role role;
}
