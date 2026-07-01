package com.example.backend.module.emplacement.dto;

import com.example.backend.module.emplacement.entity.Rayon;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class RayonResponseDTO {

    private Long id;
    private String code;
    private String libelle;
    private boolean actif;
    private Long zoneId;
    private String zoneNom;
    private Long entrepotId;
    private String entrepotNom;
    private long nombreEtageres;
    private LocalDateTime dateCreation;

    public static RayonResponseDTO fromEntity(Rayon r, long nombreEtageres) {
        return RayonResponseDTO.builder()
                .id(r.getId())
                .code(r.getCode())
                .libelle(r.getLibelle())
                .actif(r.isActif())
                .zoneId(r.getZone().getId())
                .zoneNom(r.getZone().getNom())
                .entrepotId(r.getZone().getEntrepot().getId())
                .entrepotNom(r.getZone().getEntrepot().getNom())
                .nombreEtageres(nombreEtageres)
                .dateCreation(r.getDateCreation())
                .build();
    }
}
