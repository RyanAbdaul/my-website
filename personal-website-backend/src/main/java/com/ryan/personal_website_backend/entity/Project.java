package com.ryan.personal_website_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "projects")
@Getter
@Setter
@NoArgsConstructor
public class Project {
    @Id @GeneratedValue(strategy = GenerationType.UUID) private UUID id;
    @Column(nullable = false) private String title;
    @Column(columnDefinition = "TEXT") private String description;
    @Column(name = "image_path") private String imagePath;
    @Column(name = "project_url", nullable = false) private String projectUrl;
    @Column(name = "display_order", nullable = false) private Integer displayOrder;
}
