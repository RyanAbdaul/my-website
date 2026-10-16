package com.ryan.personal_website_backend.auth;

import com.ryan.personal_website_backend.auth.Role;
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
