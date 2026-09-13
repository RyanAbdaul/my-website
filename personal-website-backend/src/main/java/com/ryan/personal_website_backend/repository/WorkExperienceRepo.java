package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.WorkExperience;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface WorkExperienceRepo extends JpaRepository<WorkExperience, UUID> {
    List<WorkExperience> findAllByOrderByDisplayOrderAsc();
}
