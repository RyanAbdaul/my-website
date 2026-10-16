package com.ryan.personal_website_backend.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SectionContentDTO {

    private UUID id;
    private String title;
    private String description;
    private Integer orderIndex;
    private List<UnitContentDTO> units;
}
