package com.ryan.personal_website_backend.config;

import com.ryan.personal_website_backend.entity.Authority;
import com.ryan.personal_website_backend.entity.Student;
import com.ryan.personal_website_backend.entity.User;
import com.ryan.personal_website_backend.repository.StudentRepo;
import com.ryan.personal_website_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AdminBootstrap implements ApplicationRunner {

    private final StudentRepo studentRepo;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.bootstrap-admin.email:}")
    private String email;

    @Value("${app.bootstrap-admin.password:}")
    private String password;

    @Value("${app.bootstrap-admin.name:Initial Administrator}")
    private String name;

    @Override
    public void run(ApplicationArguments args) {
        if (!StringUtils.hasText(email) && !StringUtils.hasText(password)) {
            return;
        }

        if (!StringUtils.hasText(email) || !StringUtils.hasText(password)) {
            throw new IllegalStateException(
                    "Both app.bootstrap-admin.email and app.bootstrap-admin.password must be configured"
            );
        }

        if (password.length() < 8) {
            throw new IllegalStateException("The bootstrap admin password must be at least 8 characters long");
        }

        if (userRepo.existsByAuthorities_Authority("ROLE_ADMIN")) {
            return;
        }

        if (userRepo.existsByEmail(email)) {
            throw new IllegalStateException("The bootstrap admin email is already in use");
        }

        User user = userRepo.save(new User(null, email, passwordEncoder.encode(password), null, null,
                List.of(new Authority("ROLE_ADMIN"))));
        user.setDisplayName(name);
        userRepo.save(user);
        Student admin = new Student();
        admin.setName(name);
        admin.setUser(user);
        Student savedAdmin = studentRepo.save(admin);
        user.setStudent(savedAdmin);
    }
}
