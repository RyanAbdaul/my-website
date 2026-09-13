package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.*;
import com.ryan.personal_website_backend.entity.Authority;
import com.ryan.personal_website_backend.entity.Member;
import com.ryan.personal_website_backend.entity.MemberExperience;
import com.ryan.personal_website_backend.entity.Article;
import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.PortfolioCertificate;
import com.ryan.personal_website_backend.entity.User;
import com.ryan.personal_website_backend.repository.MemberExperienceRepo;
import com.ryan.personal_website_backend.repository.MemberRepo;
import com.ryan.personal_website_backend.repository.ProjectRepo;
import com.ryan.personal_website_backend.repository.PortfolioCertificateRepo;
import com.ryan.personal_website_backend.repository.ArticleRepo;
import com.ryan.personal_website_backend.repository.CourseRepo;
import com.ryan.personal_website_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class MemberService {
    private final MemberRepo memberRepo;
    private final MemberExperienceRepo experienceRepo;
    private final ProjectRepo projectRepo;
    private final PortfolioCertificateRepo certificateRepo;
    private final ArticleRepo articleRepo;
    private final CourseRepo courseRepo;
    private final MemberPhotoStorageService memberPhotoStorageService;
    private final UserRepo userRepo;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public MemberProfileResponse getBySlug(String slug) {
        return response(memberRepo.findBySlugIgnoreCase(slug)
                .orElseThrow(() -> new IllegalArgumentException("Member not found")));
    }

    @Transactional(readOnly = true)
    public MemberProfileResponse getById(UUID id) {
        return response(memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found")));
    }

    @Transactional(readOnly = true)
    public List<MemberProfileResponse> getAll() {
        return memberRepo.findAll().stream().map(this::response).toList();
    }

    @Transactional
    public MemberProfileResponse create(MemberRequest request) {
        String slug = request.slug().trim().toLowerCase();
        if (memberRepo.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("Member slug " + slug + " already exists");
        }

        String email = (request.email() != null && !request.email().isBlank())
                ? request.email().trim().toLowerCase()
                : slug + "@bashmobarmeg.local";

        if (userRepo.existsByEmail(email)) {
            throw new IllegalArgumentException("A user with email " + email + " already exists");
        }

        String rawPassword = (request.password() != null && !request.password().isBlank())
                ? request.password()
                : "password123";

        User user = new User(
                null,
                email,
                passwordEncoder.encode(rawPassword),
                null,
                null,
                List.of(new Authority("ROLE_MEMBER"))
        );
        User savedUser = userRepo.save(user);

        Member member = new Member();
        member.setId(UUID.randomUUID());
        member.setSlug(slug);
        member.setName(request.name().trim());
        member.setHeadline(blankToNull(request.headline()));
        member.setBiography(blankToNull(request.biography()));
        member.setPhotoUrl(blankToNull(request.photoUrl()));
        member.setUser(savedUser);

        Member savedMember = memberRepo.save(member);
        savedUser.setMember(savedMember);

        return response(savedMember);
    }

    @Transactional
    public MemberProfileResponse update(UUID id, MemberRequest request) {
        Member member = memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        String slug = request.slug().trim().toLowerCase();
        if (!member.getSlug().equalsIgnoreCase(slug) && memberRepo.existsBySlugIgnoreCase(slug)) {
            throw new IllegalArgumentException("Member slug " + slug + " already exists");
        }

        member.setSlug(slug);
        member.setName(request.name().trim());
        member.setHeadline(blankToNull(request.headline()));
        member.setBiography(blankToNull(request.biography()));
        member.setPhotoUrl(blankToNull(request.photoUrl()));

        if (request.email() != null && !request.email().isBlank() && member.getUser() != null) {
            String newEmail = request.email().trim().toLowerCase();
            if (!member.getUser().getEmail().equalsIgnoreCase(newEmail)) {
                if (userRepo.existsByEmail(newEmail)) {
                    throw new IllegalArgumentException("A user with email " + newEmail + " already exists");
                }
                member.getUser().setEmail(newEmail);
                userRepo.save(member.getUser());
            }
        }

        if (request.password() != null && !request.password().isBlank() && member.getUser() != null) {
            member.getUser().setPassword(passwordEncoder.encode(request.password()));
            userRepo.save(member.getUser());
        }

        return response(memberRepo.save(member));
    }

    @Transactional
    public void deleteMember(UUID id) {
        Member member = memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        User user = member.getUser();
        if (user != null) {
            userRepo.delete(user);
        } else {
            memberRepo.delete(member);
        }
    }

    @Transactional
    public MemberProfileResponse uploadPhoto(UUID id, MultipartFile photo) {
        Member member = memberRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));
        member.setPhotoUrl(memberPhotoStorageService.store(photo));
        return response(memberRepo.save(member));
    }

    @Transactional
    public MemberExperienceResponse addExperience(UUID memberId, MemberExperienceRequest request) {
        MemberExperience item = new MemberExperience();
        item.setMember(memberRepo.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found")));
        apply(item, request);
        return experienceResponse(experienceRepo.save(item));
    }

    @Transactional
    public MemberExperienceResponse updateExperience(UUID id, MemberExperienceRequest request) {
        MemberExperience item = experienceRepo.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member experience not found"));
        apply(item, request);
        return experienceResponse(experienceRepo.save(item));
    }

    public void deleteExperience(UUID id) {
        experienceRepo.deleteById(id);
    }

    private MemberProfileResponse response(Member member) {
        return new MemberProfileResponse(
                member.getId(),
                member.getSlug(),
                member.getName(),
                member.getHeadline(),
                member.getBiography(),
                member.getPhotoUrl(),
                member.getUser() != null ? member.getUser().getEmail() : null,
                experienceRepo.findByMemberIdOrderByDisplayOrderAsc(member.getId()).stream().map(this::experienceResponse).toList(),
                projectRepo.findByMemberIdOrderByDisplayOrderAsc(member.getId()).stream().map(item -> new ResponseProjectDTO(item.getId(), item.getTitle(), item.getDescription(), item.getImagePath(), item.getProjectUrl(), item.getDisplayOrder())).toList(),
                certificateRepo.findByMemberIdOrderByDisplayOrderAsc(member.getId()).stream().map(item -> new ResponsePortfolioCertificateDTO(item.getId(), item.getTitle(), item.getIssuer(), item.getIssuedDate(), item.getImagePath())).toList(),
                articleRepo.findByMemberIdOrderByCreateTimeDesc(member.getId()).stream().map(this::articleResponse).toList(),
                courseRepo.findByMembers_IdOrderByCreateTimeDesc(member.getId()).stream().map(this::courseResponse).toList()
        );
    }

    private ResponseArticleDTO articleResponse(Article item) {
        return ResponseArticleDTO.builder()
                .id(item.getId())
                .title(item.getTitle())
                .content(item.getContent())
                .createTime(item.getCreateTime())
                .tags(item.getTags() == null ? List.of() : item.getTags().stream().map(tag -> tag.getName()).toList())
                .build();
    }

    private ResponseCourseDTO courseResponse(Course item) {
        return new ResponseCourseDTO(
                item.getId(),
                item.getTitle(),
                item.getDescription(),
                item.getCreateTime(),
                item.getThumbnailLink(),
                item.getPrice(),
                item.getMembers().stream().map(member -> new ResponseMemberSummaryDTO(member.getId(), member.getSlug(), member.getName(), member.getHeadline(), member.getPhotoUrl())).toList(),
                item.getEnrollments().stream().map(enrollment -> new ResponseEnrollmentDTO(enrollment.getId(), enrollment.getStudent().getId(), enrollment.getStudent().getName(), enrollment.getCourse().getId(), enrollment.getCourse().getTitle(), enrollment.getPaymentStatus(), enrollment.getEnrolledAt())).toList(),
                item.getLessons().stream().map(lesson -> new ResponseLessonDTO(lesson.getId(), lesson.getCourse().getId(), lesson.getTitle(), lesson.getVideoLink(), lesson.getCreateTime())).toList()
        );
    }

    private MemberExperienceResponse experienceResponse(MemberExperience item) {
        return new MemberExperienceResponse(
                item.getId(),
                item.getRole(),
                item.getOrganization(),
                item.getOrganizationUrl(),
                item.getLocation(),
                item.getStartDate(),
                item.getEndDate(),
                item.getSummary(),
                item.getDisplayOrder()
        );
    }

    private void apply(MemberExperience item, MemberExperienceRequest request) {
        item.setRole(request.role().trim());
        item.setOrganization(request.organization().trim());
        item.setOrganizationUrl(blankToNull(request.organizationUrl()));
        item.setLocation(blankToNull(request.location()));
        item.setStartDate(request.startDate());
        item.setEndDate(request.endDate());
        item.setSummary(request.summary().trim());
        item.setDisplayOrder(request.displayOrder() == null ? 999 : request.displayOrder());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
