package com.example.backend.module.categorie.dto;

import com.example.backend.module.categorie.entity.Categorie;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class CategorieResponseDTO {

    private Long id;
    private String nom;
    private String description;

    public static CategorieResponseDTO fromEntity(Categorie c) {
        return CategorieResponseDTO.builder()
                .id(c.getId())
                .nom(c.getNom())
                .description(c.getDescription())
                .build();
    }
}
