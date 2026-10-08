package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.AdminProfileResponse;
import com.ryan.personal_website_backend.dto.UpdateAdminProfileDTO;
import com.ryan.personal_website_backend.entity.User;
import com.ryan.personal_website_backend.repository.UserRepo;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class AdminProfileService {
    private final UserRepo userRepo;
    private final AdminProfilePhotoStorageService photoStorageService;

    @Transactional(readOnly = true)
    public AdminProfileResponse getPublicProfile() {
        return response(adminUser());
    }

    @Transactional(readOnly = true)
    public AdminProfileResponse getProfile(String email) {
        return response(userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found")));
    }

    @Transactional
    public AdminProfileResponse updateProfile(String email, UpdateAdminProfileDTO request) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));
        user.setDisplayName(request.name().trim());
        user.setHeadline(blankToNull(request.headline()));
        user.setBiography(blankToNull(request.biography()));
        user.setPhotoUrl(blankToNull(request.photoUrl()));
        return response(userRepo.save(user));
    }

    @Transactional
    public AdminProfileResponse uploadPhoto(String email, MultipartFile photo) {
        User user = userRepo.findByEmail(email)
                .orElseThrow(() -> new IllegalArgumentException("Administrator not found"));
        user.setPhotoUrl(photoStorageService.store(photo));
        return response(userRepo.save(user));
    }

    private User adminUser() {
        return userRepo.findFirstByAuthorities_AuthorityAndDisplayNameIsNotNull("ROLE_ADMIN")
                .orElseThrow(() -> new IllegalArgumentException("Administrator profile has not been configured"));
    }

    private AdminProfileResponse response(User user) {
        return new AdminProfileResponse(user.getId(), user.getDisplayName(), user.getHeadline(), user.getBiography(), user.getPhotoUrl());
    }

    private String blankToNull(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }
}
