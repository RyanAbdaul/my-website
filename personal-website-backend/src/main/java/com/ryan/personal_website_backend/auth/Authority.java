package com.ryan.personal_website_backend.auth;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.security.core.GrantedAuthority;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
public class Authority implements GrantedAuthority {
    private String authority;

    @Override
    public String getAuthority() {
        return authority;
    }

    public Authority(String authority) {
        this.authority = authority;
    }
}
