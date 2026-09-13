package com.ryan.personal_website_backend.dto;

import com.ryan.personal_website_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthenticatedUserResponse {
    private String email;
    private Role role;
}
