package com.example.backend.module.fournisseur.dto;

import com.example.backend.module.fournisseur.entity.Fournisseur;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse renvoyé au client pour un fournisseur.
 */
@Data
@Builder
public class FournisseurResponseDTO {

    private Long id;
    private String nom;
    private String adresse;
    private String telephone;
    private String email;
    private String contactPrincipal;
    private boolean actif;
    private LocalDateTime dateCreation;

    public static FournisseurResponseDTO fromEntity(Fournisseur f) {
        return FournisseurResponseDTO.builder()
                .id(f.getId())
                .nom(f.getNom())
                .adresse(f.getAdresse())
                .telephone(f.getTelephone())
                .email(f.getEmail())
                .contactPrincipal(f.getContactPrincipal())
                .actif(f.isActif())
                .dateCreation(f.getDateCreation())
                .build();
    }
}
