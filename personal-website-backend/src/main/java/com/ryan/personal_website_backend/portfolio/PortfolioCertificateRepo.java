package com.ryan.personal_website_backend.portfolio;

import com.ryan.personal_website_backend.portfolio.PortfolioCertificate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface PortfolioCertificateRepo extends JpaRepository<PortfolioCertificate, UUID> {
    List<PortfolioCertificate> findAllByOrderByDisplayOrderAsc();
}
