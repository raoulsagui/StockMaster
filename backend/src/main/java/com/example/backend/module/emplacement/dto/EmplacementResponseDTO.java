package com.example.backend.module.emplacement.dto;

import com.example.backend.module.emplacement.entity.Emplacement;
import com.example.backend.module.emplacement.entity.Etagere;
import com.example.backend.module.emplacement.entity.Rayon;
import com.example.backend.module.zone.entity.Zone;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * DTO de réponse pour un emplacement.
 * Contient toute la hiérarchie pour l'affichage :
 *   entrepôt → zone → rayon → étagère → emplacement
 */
@Data
@Builder
public class EmplacementResponseDTO {

    private Long id;
    private String code;
    private String adresseComplete;
    private String type;
    private String statut;
    private Integer capaciteMax;
    private String description;
    private LocalDateTime dateCreation;

    // Hiérarchie
    private Long etagereId;
    private String etagereCode;
    private Long rayonId;
    private String rayonCode;
    private Long zoneId;
    private String zoneNom;
    private Long entrepotId;
    private String entrepotNom;

    public static EmplacementResponseDTO fromEntity(Emplacement e) {
        Etagere etagere = e.getEtagere();
        Rayon   rayon   = etagere.getRayon();
        Zone    zone    = rayon.getZone();

        return EmplacementResponseDTO.builder()
                .id(e.getId())
                .code(e.getCode())
                .adresseComplete(e.getAdresseComplete())
                .type(e.getType().name())
                .statut(e.getStatut().name())
                .capaciteMax(e.getCapaciteMax())
                .description(e.getDescription())
                .dateCreation(e.getDateCreation())
                .etagereId(etagere.getId())
                .etagereCode(etagere.getCode())
                .rayonId(rayon.getId())
                .rayonCode(rayon.getCode())
                .zoneId(zone.getId())
                .zoneNom(zone.getNom())
                .entrepotId(zone.getEntrepot().getId())
                .entrepotNom(zone.getEntrepot().getNom())
                .build();
    }
}
