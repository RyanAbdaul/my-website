package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.ResponsePortfolioCertificateDTO;
import com.ryan.personal_website_backend.dto.ResponseWorkExperienceDTO;
import com.ryan.personal_website_backend.dto.CreatePortfolioCertificateDTO;
import com.ryan.personal_website_backend.dto.CreateWorkExperienceDTO;
import com.ryan.personal_website_backend.entity.PortfolioCertificate;
import com.ryan.personal_website_backend.entity.WorkExperience;
import com.ryan.personal_website_backend.repository.PortfolioCertificateRepo;
import com.ryan.personal_website_backend.repository.WorkExperienceRepo;
import com.ryan.personal_website_backend.repository.MemberRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PortfolioService {
    private final WorkExperienceRepo workExperienceRepo;
    private final PortfolioCertificateRepo certificateRepo;
    private final MemberRepo memberRepo;

    public List<ResponseWorkExperienceDTO> getWorkExperiences() {
        return workExperienceRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(item -> new ResponseWorkExperienceDTO(item.getId(), item.getCompany(), item.getRole(), item.getStartDate(), item.getEndDate(), item.getSummary(), item.getHighlights()))
                .toList();
    }

    public List<ResponsePortfolioCertificateDTO> getCertificates() {
        return certificateRepo.findAllByOrderByDisplayOrderAsc().stream()
                .map(item -> new ResponsePortfolioCertificateDTO(item.getId(), item.getTitle(), item.getIssuer(), item.getIssuedDate(), item.getImagePath()))
                .toList();
    }

    public ResponseWorkExperienceDTO createWorkExperience(CreateWorkExperienceDTO dto) {
        WorkExperience item = new WorkExperience();
        item.setCompany(dto.company());
        item.setRole(dto.role());
        item.setStartDate(dto.startDate());
        item.setEndDate(dto.endDate());
        item.setSummary(dto.summary());
        item.setHighlights(dto.highlights() == null ? new String[0] : dto.highlights().toArray(String[]::new));
        item.setDisplayOrder(dto.displayOrder() == null ? 999 : dto.displayOrder());
        WorkExperience saved = workExperienceRepo.save(item);
        return new ResponseWorkExperienceDTO(saved.getId(), saved.getCompany(), saved.getRole(), saved.getStartDate(), saved.getEndDate(), saved.getSummary(), saved.getHighlights());
    }

    public void deleteWorkExperience(UUID id) {
        workExperienceRepo.deleteById(id);
    }

    public ResponseWorkExperienceDTO updateWorkExperience(UUID id, CreateWorkExperienceDTO dto) {
        WorkExperience item = workExperienceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Work experience not found"));
        item.setCompany(dto.company());
        item.setRole(dto.role());
        item.setStartDate(dto.startDate());
        item.setEndDate(dto.endDate());
        item.setSummary(dto.summary());
        item.setHighlights(dto.highlights() == null ? new String[0] : dto.highlights().toArray(String[]::new));
        if (dto.displayOrder() != null) item.setDisplayOrder(dto.displayOrder());
        WorkExperience saved = workExperienceRepo.save(item);
        return new ResponseWorkExperienceDTO(saved.getId(), saved.getCompany(), saved.getRole(), saved.getStartDate(), saved.getEndDate(), saved.getSummary(), saved.getHighlights());
    }

    public ResponsePortfolioCertificateDTO createCertificate(CreatePortfolioCertificateDTO dto) {
        PortfolioCertificate item = new PortfolioCertificate();
        item.setTitle(dto.title());
        item.setIssuer(dto.issuer());
        item.setIssuedDate(dto.issuedDate());
        item.setImagePath(dto.imagePath());
        item.setDisplayOrder(dto.displayOrder() == null ? 999 : dto.displayOrder());
        if (dto.memberId() != null) item.setMember(memberRepo.findById(dto.memberId()).orElseThrow(() -> new IllegalArgumentException("Member not found")));
        PortfolioCertificate saved = certificateRepo.save(item);
        return new ResponsePortfolioCertificateDTO(saved.getId(), saved.getTitle(), saved.getIssuer(), saved.getIssuedDate(), saved.getImagePath());
    }

    public void deleteCertificate(UUID id) {
        certificateRepo.deleteById(id);
    }

    public ResponsePortfolioCertificateDTO updateCertificate(UUID id, CreatePortfolioCertificateDTO dto) {
        PortfolioCertificate item = certificateRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Certificate not found"));
        item.setTitle(dto.title());
        item.setIssuer(dto.issuer());
        item.setIssuedDate(dto.issuedDate());
        item.setImagePath(dto.imagePath());
        if (dto.displayOrder() != null) item.setDisplayOrder(dto.displayOrder());
        if (dto.memberId() != null) item.setMember(memberRepo.findById(dto.memberId()).orElseThrow(() -> new IllegalArgumentException("Member not found")));
        PortfolioCertificate saved = certificateRepo.save(item);
        return new ResponsePortfolioCertificateDTO(saved.getId(), saved.getTitle(), saved.getIssuer(), saved.getIssuedDate(), saved.getImagePath());
    }
}
