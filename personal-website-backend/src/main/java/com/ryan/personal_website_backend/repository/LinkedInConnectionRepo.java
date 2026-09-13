package com.ryan.personal_website_backend.repository;

import com.ryan.personal_website_backend.entity.LinkedInConnection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface LinkedInConnectionRepo extends JpaRepository<LinkedInConnection, UUID> { }
