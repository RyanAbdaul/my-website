package com.ryan.personal_website_backend.auth;

import com.ryan.personal_website_backend.auth.AuthenticationRequest;
import com.ryan.personal_website_backend.auth.AuthenticationResponse;
import com.ryan.personal_website_backend.auth.LoginRequest;
import com.ryan.personal_website_backend.auth.Authority;
import com.ryan.personal_website_backend.auth.Role;
import com.ryan.personal_website_backend.student.Student;
import com.ryan.personal_website_backend.auth.User;
import com.ryan.personal_website_backend.common.StudentAlreadyExistsException;
import com.ryan.personal_website_backend.student.StudentRepo;
import com.ryan.personal_website_backend.auth.UserRepo;
import lombok.AllArgsConstructor;
import lombok.NonNull;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
public class AuthenticationService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final StudentRepo studentRepo;
    private final PasswordEncoder passwordEncoder;
    private final UserRepo userRepo;

    @Transactional
    public AuthenticationResponse registerStudent(
             AuthenticationRequest authenticationRequest) {

        if (userRepo.existsByEmail(authenticationRequest.getEmail())) {
            throw new StudentAlreadyExistsException(
                    "Student with email " + authenticationRequest.getEmail() + " already exists"
            );
        }

        User user = createUser(authenticationRequest.getEmail(), authenticationRequest.getPassword(),
                List.of(Role.STUDENT));
        Student newStudent = new Student();
        newStudent.setName(authenticationRequest.getName());
        newStudent.setUser(user);
        Student student = studentRepo.save(newStudent);
        user.setStudent(student);

        String jwt = jwtService.generateToken(user);

        return new AuthenticationResponse(jwt, Role.STUDENT);
    }

    public AuthenticationResponse login(
            @NonNull LoginRequest authenticationRequest) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        authenticationRequest.getEmail(),
                        authenticationRequest.getPassword()
                )
        );

        User user = userRepo.findByEmail(authenticationRequest.getEmail()).orElseThrow();

        String jwt = jwtService.generateToken(user);

        Role role = user.getAuthorities().stream()
                .anyMatch(authority -> authority.getAuthority().equals("ROLE_ADMIN"))
                ? Role.ADMIN
                : Role.STUDENT;
        return new AuthenticationResponse(jwt, role);
    }

    private User createUser(String email, String password, List<Role> roles) {
        return userRepo.save(new User(null, email, passwordEncoder.encode(password), null, null,
                roles.stream().map(role -> new Authority("ROLE_" + role.name())).toList()));
    }
}
