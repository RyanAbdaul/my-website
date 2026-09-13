package com.ryan.personal_website_backend.controller;

import com.ryan.personal_website_backend.dto.*;
import com.ryan.personal_website_backend.service.MemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.UUID;

@RestController
@RequestMapping("/members")
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @GetMapping public java.util.List<MemberProfileResponse> getMembers() { return memberService.getAll(); }
    @GetMapping("/{slug}") public MemberProfileResponse getMember(@PathVariable String slug) { return memberService.getBySlug(slug); }
    @GetMapping("/id/{id}") public MemberProfileResponse getMemberForAdmin(@PathVariable UUID id) { return memberService.getById(id); }
    @PutMapping("/{id}") public MemberProfileResponse updateMember(@PathVariable UUID id, @Valid @RequestBody MemberRequest request) { return memberService.update(id, request); }
    @PostMapping @ResponseStatus(HttpStatus.CREATED) public MemberProfileResponse createMember(@Valid @RequestBody MemberRequest request) { return memberService.create(request); }
    @PostMapping(path = "/{id}/photo", consumes = "multipart/form-data") public MemberProfileResponse uploadPhoto(@PathVariable UUID id, @RequestParam("photo") MultipartFile photo) { return memberService.uploadPhoto(id, photo); }
    @PostMapping("/{id}/experiences") @ResponseStatus(HttpStatus.CREATED) public MemberExperienceResponse addExperience(@PathVariable UUID id, @Valid @RequestBody MemberExperienceRequest request) { return memberService.addExperience(id, request); }
    @PutMapping("/experiences/{id}") public MemberExperienceResponse updateExperience(@PathVariable UUID id, @Valid @RequestBody MemberExperienceRequest request) { return memberService.updateExperience(id, request); }
    @DeleteMapping("/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteMember(@PathVariable UUID id) { memberService.deleteMember(id); }
    @DeleteMapping("/experiences/{id}") @ResponseStatus(HttpStatus.NO_CONTENT) public void deleteExperience(@PathVariable UUID id) { memberService.deleteExperience(id); }
}
