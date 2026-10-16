package com.ryan.personal_website_backend.portfolio;

import com.ryan.personal_website_backend.portfolio.ResponsePortfolioCertificateDTO;
import com.ryan.personal_website_backend.portfolio.ResponseWorkExperienceDTO;
import com.ryan.personal_website_backend.portfolio.CreatePortfolioCertificateDTO;
import com.ryan.personal_website_backend.portfolio.CreateWorkExperienceDTO;
import com.ryan.personal_website_backend.portfolio.PortfolioService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class PortfolioController {
    private final PortfolioService portfolioService;

    @GetMapping("/admin/about")
    public void validateAdminAccess() {
    }

    @GetMapping("/work-experiences")
    public List<ResponseWorkExperienceDTO> getWorkExperiences() {
        return portfolioService.getWorkExperiences();
    }

    @GetMapping("/certificates")
    public List<ResponsePortfolioCertificateDTO> getCertificates() {
        return portfolioService.getCertificates();
    }

    @PostMapping("/work-experiences")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseWorkExperienceDTO createWorkExperience(@Valid @RequestBody CreateWorkExperienceDTO dto) {
        return portfolioService.createWorkExperience(dto);
    }

    @PutMapping("/work-experiences/{id}")
    public ResponseWorkExperienceDTO updateWorkExperience(@PathVariable UUID id, @Valid @RequestBody CreateWorkExperienceDTO dto) {
        return portfolioService.updateWorkExperience(id, dto);
    }

    @DeleteMapping("/work-experiences/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteWorkExperience(@PathVariable UUID id) {
        portfolioService.deleteWorkExperience(id);
    }

    @PostMapping("/certificates")
    @ResponseStatus(HttpStatus.CREATED)
    public ResponsePortfolioCertificateDTO createCertificate(@Valid @RequestBody CreatePortfolioCertificateDTO dto) {
        return portfolioService.createCertificate(dto);
    }

    @PutMapping("/certificates/{id}")
    public ResponsePortfolioCertificateDTO updateCertificate(@PathVariable UUID id, @Valid @RequestBody CreatePortfolioCertificateDTO dto) {
        return portfolioService.updateCertificate(id, dto);
    }

    @DeleteMapping("/certificates/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCertificate(@PathVariable UUID id) {
        portfolioService.deleteCertificate(id);
    }
}
