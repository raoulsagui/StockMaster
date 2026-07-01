package com.example.backend.module.emplacement.dto;

import com.example.backend.module.emplacement.entity.Etagere;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class EtagereResponseDTO {

    private Long id;
    private String code;
    private String libelle;
    private Integer niveaux;
    private boolean actif;
    private Long rayonId;
    private String rayonCode;
    private Long zoneId;
    private String zoneNom;
    private Long entrepotId;
    private String entrepotNom;
    private long nombreEmplacements;
    private LocalDateTime dateCreation;

    public static EtagereResponseDTO fromEntity(Etagere e, long nombreEmplacements) {
        return EtagereResponseDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .libelle(e.getLibelle())
                .niveaux(e.getNiveaux())
                .actif(e.isActif())
                .rayonId(e.getRayon().getId())
                .rayonCode(e.getRayon().getCode())
                .zoneId(e.getRayon().getZone().getId())
                .zoneNom(e.getRayon().getZone().getNom())
                .entrepotId(e.getRayon().getZone().getEntrepot().getId())
                .entrepotNom(e.getRayon().getZone().getEntrepot().getNom())
                .nombreEmplacements(nombreEmplacements)
                .dateCreation(e.getDateCreation())
                .build();
    }
}
