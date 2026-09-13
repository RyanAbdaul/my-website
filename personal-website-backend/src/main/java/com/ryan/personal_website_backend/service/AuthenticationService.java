package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.AuthenticationRequest;
import com.ryan.personal_website_backend.dto.AuthenticationResponse;
import com.ryan.personal_website_backend.dto.LoginRequest;
import com.ryan.personal_website_backend.entity.Authority;
import com.ryan.personal_website_backend.entity.Role;
import com.ryan.personal_website_backend.entity.Member;
import com.ryan.personal_website_backend.entity.Student;
import com.ryan.personal_website_backend.entity.User;
import com.ryan.personal_website_backend.exception.StudentAlreadyExistsException;
import com.ryan.personal_website_backend.repository.MemberRepo;
import com.ryan.personal_website_backend.repository.StudentRepo;
import com.ryan.personal_website_backend.repository.UserRepo;
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
    private final MemberRepo memberRepo;
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

        boolean firstUser = userRepo.count() == 0;
        User user = createUser(authenticationRequest.getEmail(), authenticationRequest.getPassword(),
                firstUser ? List.of(Role.ADMIN, Role.STUDENT) : List.of(Role.STUDENT));
        Student newStudent = new Student();
        newStudent.setName(authenticationRequest.getName());
        newStudent.setUser(user);
        Student student = studentRepo.save(newStudent);
        user.setStudent(student);

        String jwt = jwtService.generateToken(user);

        return new AuthenticationResponse(jwt, firstUser ? Role.ADMIN : Role.STUDENT);
    }

    @Transactional
    public AuthenticationResponse registerMember(AuthenticationRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new IllegalArgumentException(
                    "An account with email " + request.getEmail() + " already exists"
            );
        }

        Member member = new Member();
        member.setId(java.util.UUID.randomUUID());
        member.setName(request.getName());
        member.setSlug(uniqueMemberSlug(request.getName()));
        member.setHeadline("Member");
        boolean firstUser = userRepo.count() == 0;
        User user = createUser(request.getEmail(), request.getPassword(),
                firstUser ? List.of(Role.ADMIN, Role.MEMBER) : List.of(Role.MEMBER));
        member.setUser(user);
        Member savedMember = memberRepo.save(member);
        user.setMember(savedMember);

        return new AuthenticationResponse(jwtService.generateToken(user), firstUser ? Role.ADMIN : Role.MEMBER);
    }

    @Transactional
    public AuthenticationResponse registerAdmin(AuthenticationRequest request) {
        if (userRepo.existsByEmail(request.getEmail())) {
            throw new StudentAlreadyExistsException(
                    "An account with email " + request.getEmail() + " already exists"
            );
        }

        User user = createUser(request.getEmail(), request.getPassword(), List.of(Role.ADMIN, Role.STUDENT));
        Student admin = new Student();
        admin.setName(request.getName());
        admin.setUser(user);
        Student savedAdmin = studentRepo.save(admin);
        user.setStudent(savedAdmin);

        return new AuthenticationResponse(jwtService.generateToken(user), Role.ADMIN);
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
                : user.getAuthorities().stream()
                        .anyMatch(authority -> authority.getAuthority().equals("ROLE_MEMBER"))
                        ? Role.MEMBER
                        : Role.STUDENT;
        return new AuthenticationResponse(jwt, role);
    }

    private User createUser(String email, String password, List<Role> roles) {
        return userRepo.save(new User(null, email, passwordEncoder.encode(password), null, null,
                roles.stream().map(role -> new Authority("ROLE_" + role.name())).toList()));
    }

    private String uniqueMemberSlug(String name) {
        String base = name == null ? "member" : name.trim().toLowerCase().replaceAll("[^a-z0-9]+", "-").replaceAll("(^-|-$)", "");
        if (base.isBlank()) base = "member";
        String slug = base;
        while (memberRepo.existsBySlugIgnoreCase(slug)) slug = base + "-" + java.util.UUID.randomUUID().toString().substring(0, 8);
        return slug;
    }
}
