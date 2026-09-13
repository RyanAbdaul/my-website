package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.MemberExperience;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface MemberExperienceRepo extends JpaRepository<MemberExperience, UUID> { List<MemberExperience> findByMemberIdOrderByDisplayOrderAsc(UUID memberId); }
