package com.ryan.personal_website_backend.portfolio;

import com.ryan.personal_website_backend.portfolio.Project;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ProjectRepo extends JpaRepository<Project, UUID> {
    List<Project> findAllByOrderByDisplayOrderAsc();
}
