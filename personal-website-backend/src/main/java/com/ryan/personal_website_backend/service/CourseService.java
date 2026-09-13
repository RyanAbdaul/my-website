package com.ryan.personal_website_backend.service;

import com.ryan.personal_website_backend.dto.CreateCourseDTO;
import com.ryan.personal_website_backend.dto.ResponseCourseDTO;
import com.ryan.personal_website_backend.dto.UpdateCourseDTO;
import com.ryan.personal_website_backend.dto.ResponseEnrollmentDTO;
import com.ryan.personal_website_backend.dto.ResponseLessonDTO;
import com.ryan.personal_website_backend.entity.Course;
import com.ryan.personal_website_backend.entity.Member;
import com.ryan.personal_website_backend.exception.CourseDoesNotExistException;
import com.ryan.personal_website_backend.repository.CourseRepo;
import com.ryan.personal_website_backend.repository.MemberRepo;
import com.ryan.personal_website_backend.util.AuthorizationHelper;
import com.ryan.personal_website_backend.util.AuthorizationHelperImpl;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import com.ryan.personal_website_backend.entity.User;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import org.springframework.security.access.AccessDeniedException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CourseService {
    private final CourseRepo courseRepo;
    private final MemberRepo memberRepo;
    private final AuthorizationHelper authorizationHelper;

    public List<ResponseCourseDTO> getCourseByName(String title) { return courseRepo.findCourseByTitleContaining(title).stream().map(this::response).toList(); }
    public ResponseCourseDTO getCourseById(UUID id) {
        return response(courseRepo.findById(id).orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + id + " does not exist"))); }
    public List<ResponseCourseDTO> getAllCourses(String title, Double minPrice, Double maxPrice, String memberName) { return courseRepo.findByFilters(title, minPrice, maxPrice, memberName).stream().map(this::response).toList(); }
    public List<ResponseCourseDTO> getFeaturedCourses() { return courseRepo.findTop10DistinctByMembers_NameContainingIgnoreCaseOrderByCreateTimeDesc("Ryan").stream().map(this::response).toList(); }


    
    @Transactional
    public ResponseCourseDTO createCourse(CreateCourseDTO input, Authentication authentication) {
        Course course = new Course();
        course.setTitle(input.getTitle());
        course.setDescription(input.getDescription());
        course.setCreateTime(new Timestamp(System.currentTimeMillis()));
        course.setPrice(input.getPrice());
        course.setThumbnailLink(input.getThumbnailLink());
        course.setMembers(membersFor(input.getMemberIds(), authentication));
        return response(courseRepo.save(course));
    }


    public void deleteCourseById(UUID courseId) throws AccessDeniedException, java.nio.file.AccessDeniedException {
        Course course = courseRepo.findById(courseId).orElseThrow(() -> new CourseDoesNotExistException("Course with id " + courseId + " Does not exist"));
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        authorizationHelper.authorizeCourseOwner(course, authentication);

        courseRepo.delete(courseRepo.findById(courseId).orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + courseId + " does not exist")));
    }




    @Transactional
    public ResponseCourseDTO updateCourseById(UUID id, UpdateCourseDTO input, Authentication authentication) throws java.nio.file.AccessDeniedException {
        Course course = courseRepo.findById(id).orElseThrow(() -> new CourseDoesNotExistException("Course with ID " + id + " does not exist"));

        authorizationHelper.authorizeCourseOwner(course, authentication);

        course.setPrice(input.getPrice());
        course.setTitle(input.getTitle());
        course.setThumbnailLink(input.getThumbnailLink());
        course.setDescription(input.getDescription());

        if (input.getMemberIds() != null) course.setMembers(membersFor(input.getMemberIds(), authentication));
        return response(courseRepo.save(course));
    }

    private List<Member> membersFor(List<UUID> requestedIds, Authentication authentication) {
        List<UUID> ids = requestedIds == null ? new ArrayList<>() : new ArrayList<>(requestedIds);
        memberRepo.findByUser_Email(authentication.getName()).map(Member::getId).filter(id -> !ids.contains(id)).ifPresent(ids::add);
        List<Member> members = memberRepo.findAllById(ids);
        if (members.size() != ids.stream().distinct().count()) {
            throw new IllegalArgumentException("One or more members were not found");
        }
        return members;

    }

    private ResponseCourseDTO response(Course course) {
        return new ResponseCourseDTO(course.getId(), course.getTitle(), course.getDescription(), course.getCreateTime(),
                course.getThumbnailLink(), course.getPrice(), course.getMembers().stream()
                .map(member -> new com.ryan.personal_website_backend.dto.ResponseMemberSummaryDTO(member.getId(), member.getSlug(), member.getName(), member.getHeadline(), member.getPhotoUrl())).toList(),
                course.getEnrollments().stream().map(this::enrollmentResponse).toList(),
                course.getLessons().stream().map(this::lessonResponse).toList());
    }

    private ResponseEnrollmentDTO enrollmentResponse(com.ryan.personal_website_backend.entity.Enrollment enrollment) {
        return new ResponseEnrollmentDTO(enrollment.getId(), enrollment.getStudent().getId(), enrollment.getStudent().getName(),
                enrollment.getCourse().getId(), enrollment.getCourse().getTitle(), enrollment.getPaymentStatus(), enrollment.getEnrolledAt());
    }

    private ResponseLessonDTO lessonResponse(com.ryan.personal_website_backend.entity.Lesson lesson) {
        return new ResponseLessonDTO(lesson.getId(), lesson.getCourse().getId(), lesson.getTitle(), lesson.getVideoLink(), lesson.getCreateTime());
    }
}
