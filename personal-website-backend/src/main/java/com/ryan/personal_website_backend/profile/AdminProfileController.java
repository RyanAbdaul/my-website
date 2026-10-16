package com.ryan.personal_website_backend.profile;

import com.ryan.personal_website_backend.profile.AdminProfileResponse;
import com.ryan.personal_website_backend.profile.UpdateAdminProfileDTO;
import com.ryan.personal_website_backend.profile.AdminProfileService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequiredArgsConstructor
public class AdminProfileController {
    private final AdminProfileService adminProfileService;

    @GetMapping("/profile")
    public AdminProfileResponse getPublicProfile() {
        return adminProfileService.getPublicProfile();
    }

    @GetMapping("/admin/profile")
    public AdminProfileResponse getProfile(Authentication authentication) {
        return adminProfileService.getProfile(authentication.getName());
    }

    @PutMapping("/admin/profile")
    public AdminProfileResponse updateProfile(Authentication authentication, @Valid @RequestBody UpdateAdminProfileDTO request) {
        return adminProfileService.updateProfile(authentication.getName(), request);
    }

    @PostMapping(path = "/admin/profile/photo", consumes = "multipart/form-data")
    public AdminProfileResponse uploadPhoto(Authentication authentication, @RequestParam("photo") MultipartFile photo) {
        return adminProfileService.uploadPhoto(authentication.getName(), photo);
    }
}
