package com.ryan.personal_website_backend.dto;

import com.ryan.personal_website_backend.entity.Role;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class AuthenticationResponse {

    private String token;
    private Role role;

}
