package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.List;
import java.util.UUID;

public interface MemberRepo extends JpaRepository<Member, UUID> {
    Optional<Member> findBySlugIgnoreCase(String slug);
    Optional<Member> findByUser_Email(String email);
    boolean existsByUser_EmailAndCourses_Id(String email, UUID courseId);
    boolean existsBySlugIgnoreCase(String slug);
}
