package com.ryan.personal_website_backend.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "certificates")
@Getter
@Setter
@NoArgsConstructor
public class PortfolioCertificate {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String title;

    private String issuer;

    @Column(name = "issued_date")
    private LocalDate issuedDate;

    @Column(name = "image_path", nullable = false)
    private String imagePath;

    @Column(name = "display_order", nullable = false)
    private Integer displayOrder;
}
