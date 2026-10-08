package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectRepo extends JpaRepository<Project, UUID> {
    List<Project> findAllByOrderByDisplayOrderAsc();
}
