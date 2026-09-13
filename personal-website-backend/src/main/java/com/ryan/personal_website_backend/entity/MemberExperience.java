package com.ryan.personal_website_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "member_experiences")
@Getter
@Setter
@NoArgsConstructor
public class MemberExperience {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @ManyToOne(fetch = FetchType.LAZY) @JoinColumn(name = "member_id", nullable = false) private Member member;
    @Column(nullable = false) private String role;
    @Column(nullable = false) private String organization;
    @Column(name = "organization_url", columnDefinition = "TEXT") private String organizationUrl;
    private String location;
    @Column(name = "start_date", nullable = false) private LocalDate startDate;
    @Column(name = "end_date") private LocalDate endDate;
    @Column(nullable = false, columnDefinition = "TEXT") private String summary;
    @Column(name = "display_order", nullable = false) private Integer displayOrder;
}
