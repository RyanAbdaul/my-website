package com.ryan.personal_website_backend.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;
import java.util.List;

@Entity
@Table(name = "members")
@Getter
@Setter
@NoArgsConstructor
public class Member {
    @Id
    private UUID id;
    @Column(nullable = false, unique = true) private String slug;
    @Column(nullable = false) private String name;
    private String headline;
    @Column(columnDefinition = "TEXT") private String biography;
    @Column(name = "photo_url", columnDefinition = "TEXT") private String photoUrl;
    @OneToMany(mappedBy = "member") private List<Project> projects;
    @OneToMany(mappedBy = "member") private List<PortfolioCertificate> certificates;
    @OneToMany(mappedBy = "member") private List<Article> articles;
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;
    @JsonIgnore @ManyToMany(mappedBy = "members") private List<Course> courses;
}
